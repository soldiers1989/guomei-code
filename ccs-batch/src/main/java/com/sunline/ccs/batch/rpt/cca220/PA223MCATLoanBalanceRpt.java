package com.sunline.ccs.batch.rpt.cca220;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ccs.batch.common.DateUtils;
import com.sunline.ccs.batch.rpt.cca220.item.MCATLoanBalanceRptItem;
import com.sunline.ccs.batch.rpt.common.RptBatchUtil;
import com.sunline.ccs.facility.BlockCodeUtils;
import com.sunline.ccs.facility.MicroCreditRescheduleUtils;
import com.sunline.ccs.infrastructure.server.repos.RCcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsPlan;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.enums.CalcMethod;
import com.sunline.ccs.param.def.enums.PrepaymentFeeMethod;
import com.sunline.pcm.param.def.Product;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanFeeMethod;
import com.sunline.ppy.dictionary.enums.PlanType;

/**
 *	随借随还贷款余额报表
 */
public class PA223MCATLoanBalanceRpt implements ItemProcessor<CcsAcct, MCATLoanBalanceRptItem> {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private UnifiedParameterFacility unifiedParameterFacility;
	@Autowired
	private RptBatchUtil rptBatchUtil;
	@Autowired
	private MicroCreditRescheduleUtils microCreditRescheduleUtils;
	@Autowired
	private BlockCodeUtils blockCodeUtils;
	@Autowired
	private RCcsLoan rLoan;
	@Autowired
	private BatchStatusFacility batchFacility;
	@PersistenceContext
    private EntityManager em;
	private final static int MONTHS_DAYS = 30;
	@Override
	public MCATLoanBalanceRptItem process(CcsAcct acct) throws Exception {
		MCATLoanBalanceRptItem item = new MCATLoanBalanceRptItem();
		logger.info("===随借随还贷款余额报表Process开始, 账户[{}][{}]===", acct.getAcctNbr(), acct.getAcctType());

		//取产品参数
		rptBatchUtil.setCurrOrgNoToContext();
		Product product = unifiedParameterFacility.loadParameter(acct.getProductCd() , Product.class);
		ProductCredit productCredit = unifiedParameterFacility.loadParameter(acct.getProductCd() , ProductCredit.class);
		String loanCd = productCredit.loanPlansMap.get(productCredit.defaultLoanType);
		LoanPlan loanPlan = unifiedParameterFacility.loadParameter(loanCd,LoanPlan.class);
		
		//设置产品、贷款、客户信息
		item.productCd = product.productCode;
		item.productDesc = product.description;
		item.loanCode = loanPlan.loanCode;
		item.loanDesc = loanPlan.description;
		item.contrNbr = acct.getContrNbr();
		item.searchDate = batchFacility.getBatchDate();
		item.contrEstablishDate = acct.getSetupDate();
		item.contrCreditLmt = acct.getCreditLmt().setScale(2, RoundingMode.HALF_UP);
		item.name = acct.getName();
		item.idNo = acct.getCustId().toString();
		
		QCcsLoan qLoan = QCcsLoan.ccsLoan;
		Iterator<CcsLoan> loans = rLoan.findAll(qLoan.acctNbr.eq(acct.getAcctNbr()).and(qLoan.acctType.eq(acct.getAcctType()))).iterator(); 
		
		CcsLoan loan = null;
		if(loans.hasNext()){
			loan  = loans.next();
			if(loans.hasNext()) 
				logger.warn("账户有多个贷款Loan,取得Loan[id:{}]", loan.getLoanId());
		}
		if(loan == null){
			return initItem(item);
		}
		//贷款还清日期在贷款有效期之后才会赋值
		if(loan != null && loan.getPaidOutDate() != null){
			return null;
		}
		
		logger.info("合同编号[{}]借据号[{}]账户号[{}]账户类型[{}]LoanId[{}]",
				loan.getContrNbr(),loan.getDueBillNo(),loan.getAcctNbr(),loan.getAcctType(),loan.getLoanId());
		
		//逾期天数
		if(loan.getOverdueDate() != null){
			item.overDueDayCount = DateUtils.getIntervalDays(loan.getOverdueDate(), batchFacility.getBatchDate());
		}else{
			item.overDueDayCount = 0;
			
		}
		//入催天数
		if(loan.getCpdBeginDate() != null){
			item.CPDDayCount = DateUtils.getIntervalDays(loan.getCpdBeginDate(), batchFacility.getBatchDate());
		}else{
			item.CPDDayCount = 0;
		}
		
		//贷款本金余额
		BigDecimal balance = new BigDecimal(0);
		BigDecimal trialInterest = new BigDecimal(0);// 已出未还 + 非延迟列席
		BigDecimal trialSvcFee = new BigDecimal(0);// 已出未还 + 日累计
		BigDecimal trialLifeInsFee = new BigDecimal(0);// 已出未还 + 日累计
		BigDecimal trialLpc = new BigDecimal(0);// 已出未还
		BigDecimal trialAnnualFee = new BigDecimal(0);// 已出未还
		BigDecimal trialTxnFee = new BigDecimal(0);// 已出未还
		BigDecimal trialPenalty = new BigDecimal(0); // 已出未还 + 罚息累计
		BigDecimal accruPrinSum = new BigDecimal(0);
		
		QCcsPlan qPlan = QCcsPlan.ccsPlan;
		List<CcsPlan> plans =new JPAQuery(em).from(qPlan)
				.where(qPlan.acctNbr.eq(loan.getAcctNbr()).and(qPlan.acctType.eq(loan.getAcctType()))
				.and(qPlan.refNbr.eq(loan.getRefNbr()))
				.and(qPlan.planType.in(PlanType.L, PlanType.J)))
				.list(qPlan);
		//已出未还贷款本金余额
		for(CcsPlan plan : plans){
			logger.info("loanId[{}]planId[{}]planType[{}]",loan.getLoanId(), plan.getPlanId(), plan.getPlanType());
			balance = balance.add(plan.getPastPrincipal()).add(plan.getCtdPrincipal());
			trialLpc = trialLpc.add(plan.getCtdLateFee()).add(plan.getPastLateFee());
			trialInterest = trialInterest.add(plan.getCtdInterest()).add(plan.getPastInterest()).add(plan.getNodefbnpIntAcru());
			trialSvcFee = trialSvcFee.add(plan.getCtdSvcFee().add(plan.getPastSvcFee()));
			trialLifeInsFee = trialLifeInsFee.add(plan.getCtdLifeInsuAmt().add(plan.getPastLifeInsuAmt()));
			trialAnnualFee = trialAnnualFee.add(plan.getCtdCardFee()).add(plan.getPastCardFee());
			trialTxnFee = trialTxnFee.add(plan.getCtdTxnFee()).add(plan.getPastTxnFee());
			trialPenalty = trialPenalty.add(plan.getCtdPenalty().add(plan.getPastPenalty()));
			//超过宽限日加上罚息累计
			int overGraceDays = DateUtils.getIntervalDays(acct.getGraceDate(), batchFacility.getBatchDate() );
			if(overGraceDays  >= 0){
				logger.info("超过宽限日[{}][{}]天，预提罚息加上罚息累计", 
						new SimpleDateFormat("yyyy-MM-dd").format(acct.getGraceDate()),overGraceDays);
				trialPenalty = trialPenalty.add(plan.getPenaltyAcru());
			}
			
			accruPrinSum = accruPrinSum.add(plan.getAccruPrinSum());
		}
		
		LoanFeeDef loanFeeDef = microCreditRescheduleUtils.getLoanFeeDef(loan.getLoanCode(),  loan.getLoanInitTerm(), 
				loan.getLoanInitPrin(),loan.getLoanFeeDefId());
		if(!blockCodeUtils.getMergedSvcfeeInd(acct.getBlockCode()) 
				&& loanFeeDef != null 
				&& accruPrinSum.compareTo(BigDecimal.ZERO)>0
				&& loanFeeDef.installmentFeeCalMethod.equals(PrepaymentFeeMethod.R)){
			//服务费计算按比例计算
			BigDecimal fRate = loan.getInstallmentFeeRate();
			if(fRate == null){
				fRate = loanFeeDef.installmentFeeRate;
			}
			BigDecimal monthFeeRate = fRate.multiply(new BigDecimal(1.0/loan.getLoanInitTerm()).setScale(20, RoundingMode.HALF_UP));
			BigDecimal feeRate = monthFeeRate.multiply(new BigDecimal(1.0/MONTHS_DAYS).setScale(20, RoundingMode.HALF_UP));
			trialSvcFee = trialSvcFee.add(feeRate.multiply(accruPrinSum).setScale(2, RoundingMode.HALF_UP));
		}
		//寿险费率
		if(!blockCodeUtils.getMergedOtherfeeInd(acct.getBlockCode()) 
				&& loanFeeDef != null 
				&& accruPrinSum.compareTo(BigDecimal.ZERO)>0 
				&& loanFeeDef.lifeInsuFeeCalMethod.equals(PrepaymentFeeMethod.R)){
			//寿险费计算按比例计算
			BigDecimal fRate = loan.getLifeInsuFeeRate();
			if(fRate == null){
				fRate = loanFeeDef.lifeInsuFeeRate;
			}
			if(Indicator.Y.equals(loan.getJoinLifeInsuInd())){
				BigDecimal monthLifeInsuRate = fRate.multiply(new BigDecimal(1.0/loan.getLoanInitTerm()).setScale(20, RoundingMode.HALF_UP));
				BigDecimal lifeInsuRate = monthLifeInsuRate.multiply(new BigDecimal(1.0/MONTHS_DAYS).setScale(20, RoundingMode.HALF_UP));
				trialLifeInsFee = trialLifeInsFee.add(lifeInsuRate.multiply(accruPrinSum).setScale(2, RoundingMode.HALF_UP));
			}
		}
		
		item.balance = balance.setScale(2, RoundingMode.HALF_UP);
		item.trialInterest = trialInterest.setScale(2, RoundingMode.HALF_UP);
		item.trialSvcFee = trialSvcFee.setScale(2, RoundingMode.HALF_UP); 
		item.trialLifeInsFee = trialLifeInsFee.setScale(2, RoundingMode.HALF_UP); 
		item.trialTxnFee = trialTxnFee.setScale(2, RoundingMode.HALF_UP);
		item.trialAnnualFee = trialAnnualFee.setScale(2, RoundingMode.HALF_UP);
		item.trialLpc = trialLpc.setScale(2, RoundingMode.HALF_UP);
		item.trialPenalty = trialPenalty.setScale(2, RoundingMode.HALF_UP);
		return item;
	}

	private MCATLoanBalanceRptItem initItem(MCATLoanBalanceRptItem item) {
		item.balance = new BigDecimal(0).setScale(2);
		item.trialInterest = new BigDecimal(0).setScale(2);
		item.trialSvcFee = new BigDecimal(0).setScale(2);
		item.trialLifeInsFee = new BigDecimal(0).setScale(2);
		item.trialTxnFee = new BigDecimal(0).setScale(2);
		item.trialAnnualFee = new BigDecimal(0).setScale(2);
		item.trialLpc = new BigDecimal(0).setScale(2);
		item.trialPenalty = new BigDecimal(0).setScale(2);
		item.overDueDayCount = 0;
		item.CPDDayCount = 0;
		return item;
	}

}
