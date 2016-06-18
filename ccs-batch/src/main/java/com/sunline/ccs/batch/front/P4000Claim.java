package com.sunline.ccs.batch.front;

import java.math.BigDecimal;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.facility.DateUtils;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.infrastructure.server.repos.RCcsSettleClaim;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctKey;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanRegHst;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.CcsSettleClaim;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoanRegHst;
import com.sunline.ccs.infrastructure.shared.model.QCcsPlan;
import com.sunline.ccs.loan.McLoanProvideImpl;
import com.sunline.ccs.loan.TrialResp;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanAction;
import com.sunline.ppy.dictionary.enums.LoanRegStatus;
import com.sunline.ppy.dictionary.enums.LoanStatus;
import com.sunline.ppy.dictionary.enums.LoanUsage;

/**
 * 理赔代扣
 * @author zhangqiang
 *
 */
@Component
public class P4000Claim implements ItemProcessor<CcsLoan, SFrontInfo> {
	
	private static final Logger logger = LoggerFactory.getLogger(P4000Claim.class);
	
	@Autowired
	private BatchStatusFacility batchStatusFacility;
	
	@Autowired
	private FrontBatchUtil frontBatchUtil;
	
	@PersistenceContext
	private EntityManager em;
	
	@Autowired
	private McLoanProvideImpl mcLoanProvideImpl;
	
	@Autowired
	private RCcsSettleClaim r;
	@Autowired
	private UnifiedParamFacilityProvide unifiedParameterFacilityProvide;
	
	@Override
	public SFrontInfo process(CcsLoan loan) throws Exception {
		// reader中未到理赔天数 直接返回
		if(loan==null){
			return null;
		}
		// 若有处理中订单不出代扣
		if(frontBatchUtil.getWOrderCount(loan.getAcctNbr(), loan.getAcctType())>0)
			return null;
		
		OrganizationContextHolder.setCurrentOrg(loan.getOrg());
		
		CcsAcctKey acctKey = new CcsAcctKey();
		acctKey.setAcctNbr(loan.getAcctNbr());
		acctKey.setAcctType(loan.getAcctType());
		CcsAcct acct = em.find(CcsAcct.class, acctKey);
		// 获取理赔天数参数
		ProductCredit productCredit = unifiedParameterFacilityProvide.productCredit(acct.getProductCd());
		int claimDays = productCredit.claimsDays;
		int overdueDays = DateUtils.getIntervalDays(loan.getOverdueDate(), batchStatusFacility.getSystemStatus().getBusinessDate());
		// 未到理赔天数不设置信息进行理赔
		if(overdueDays < claimDays){
			return null;
		}
		
		SFrontInfo info = new SFrontInfo();
		CcsCustomer cust = em.find(CcsCustomer.class, acct.getCustId());
		
		info.setAcct(acct);
		info.setCust(cust);
		info.setLoan(loan);
		
		// 若无理赔结清的loanReg则生成
		QCcsLoanRegHst qLoanRegHst = QCcsLoanRegHst.ccsLoanRegHst;
		QCcsLoanReg qLoanReg = QCcsLoanReg.ccsLoanReg;
		CcsLoanRegHst loanRegHst = new JPAQuery(em).from(qLoanRegHst)
				.where(qLoanRegHst.dueBillNo.eq(loan.getDueBillNo()).and(qLoanRegHst.loanAction.eq(LoanAction.C)))
				.singleResult(qLoanRegHst);
		CcsLoanReg loanReg = new JPAQuery(em).from(qLoanReg)
				.where(qLoanReg.dueBillNo.eq(loan.getDueBillNo()).and(qLoanReg.loanAction.eq(LoanAction.C)))
				.singleResult(qLoanReg);
		
		// 调用试算方法
		QCcsPlan q = QCcsPlan.ccsPlan;
		List<CcsPlan> plans = new JPAQuery(em).from(q).where(q.acctNbr.eq(info.getAcct().getAcctNbr()).and(q.acctType.eq(info.getAcct().getAcctType()))).list(q);
		TrialResp trialResp = mcLoanProvideImpl.mCLoanTodaySettlement(info.getLoan(),null, batchStatusFacility.getSystemStatus().getBusinessDate(), LoanUsage.C, new TrialResp(), plans,null);
		// 订单金额
		BigDecimal txnAmt = BigDecimal.ZERO;
		if(loanRegHst==null && loanReg==null){
			// 新建loanReg
			CcsLoanReg newLoanReg = setLoanRegInfo(info.getLoan());
			// 保存
			em.persist(newLoanReg);
			// 试算金额
			txnAmt = trialResp.getTotalAMT();
		}else{
			// 若超过理赔天数, 金额从理赔结清表取
			CcsSettleClaim settleClaim = r.findOne(info.getLoan().getLoanId());
			if(settleClaim == null){
				txnAmt = trialResp.getTotalAMT();
			}else{
				txnAmt = settleClaim.getSettleAmt();
			}
			
		}
		
		if (logger.isDebugEnabled()) {
			logger.debug("理赔代扣：Org["+info.getAcct().getOrg()
					+"],AcctType["+info.getAcct().getAcctType()
					+"],AcctNo["+info.getAcct().getAcctNbr()
					+"],DueBillNo["+info.getLoan().getDueBillNo()
					+"]");
		}
		
		// 贷款设为终止
		loan.setLoanStatus(LoanStatus.T);
		
		// 减去豁免金额
		txnAmt = txnAmt.subtract(frontBatchUtil.getTxnWaiveAmt(info.getLoan().getLoanId()));
		
		frontBatchUtil.initOrder(info.getAcct(), info.getCust(), info.getLoan(), LoanUsage.C, txnAmt, null);
		
		return info;
	}

	private CcsLoanReg setLoanRegInfo(CcsLoan loan) {
		CcsLoanReg loanReg = new CcsLoanReg();
		
		loanReg.setOrg(OrganizationContextHolder.getCurrentOrg());
		loanReg.setAcctNbr(loan.getAcctNbr());
		loanReg.setAcctType(loan.getAcctType());
		loanReg.setRegisterDate(loan.getRegisterDate());
		loanReg.setRequestTime(loan.getRequestTime());
		loanReg.setLogicCardNbr(loan.getLogicCardNbr());
		loanReg.setCardNbr(loan.getCardNbr());
		loanReg.setRefNbr(loan.getRefNbr());
		loanReg.setLoanType(loan.getLoanType());
		loanReg.setLoanRegStatus(LoanRegStatus.A);
		loanReg.setLoanInitTerm(loan.getLoanInitTerm());// 分期期数
		loanReg.setLoanInitPrin(loan.getLoanInitPrin());// 分期总本金
		loanReg.setLoanCode(loan.getLoanCode());//分期计划代码
		loanReg.setMatched(Indicator.N);
		loanReg.setInterestRate(loan.getInterestRate());
		loanReg.setInsuranceRate(loan.getInsuranceRate());
		loanReg.setLoanFeeMethod(loan.getLoanFeeMethod());
		loanReg.setLoanFinalTermFee(loan.getLoanFinalTermFee());
		loanReg.setLoanFinalTermPrin(loan.getLoanFinalTermPrin());
		loanReg.setLoanInitFee(loan.getLoanInitFee());
		loanReg.setLoanFixedFee(loan.getLoanFixedFee());
		loanReg.setLoanFirstTermFee(loan.getLoanFirstTermFee());
		loanReg.setLoanInitPrin(loan.getLoanInitPrin());
		loanReg.setLoanFixedPmtPrin(loan.getLoanFixedPmtPrin());
		loanReg.setLoanFirstTermPrin(loan.getLoanFirstTermPrin());
		loanReg.setOrigTxnAmt(loan.getOrigTxnAmt());
		loanReg.setOrigTransDate(loan.getOrigTransDate());
		loanReg.setOrigAuthCode(loan.getOrigAuthCode());
		loanReg.setGuarantyId(loan.getGuarantyId());
		loanReg.setDueBillNo(loan.getDueBillNo());
		loanReg.setLoanAction(LoanAction.C);
		loanReg.setAgreementRateInd(loan.getAgreementRateInd());
		
		return loanReg;
	}
	
}
