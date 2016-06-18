package com.sunline.ccs.service.handler.query;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.api.GlobalManagementService;
import com.sunline.ccs.facility.DateUtils;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.facility.MicroCreditRescheduleUtils;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.facility.contract.AcctOTBCal;
import com.sunline.ccs.facility.contract.AcctUnpostAmtCalUtil;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.CcsRepaySchedule;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.service.context.TxnContext;
import com.sunline.ccs.service.context.TxnInfo;
import com.sunline.ccs.service.handler.AppapiCommService;
import com.sunline.ccs.service.handler.OpenAcctCommService;
import com.sunline.ccs.service.handler.QueryCommService;
import com.sunline.ccs.service.msentity.MsResponseInfo;
import com.sunline.ccs.service.msentity.TNTLMCATWithdrawCalcReq;
import com.sunline.ccs.service.msentity.TNTLMCATWithdrawCalcResp;
import com.sunline.ppy.dictionary.enums.LoanMold;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.MsRespCode;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * 提现试算
 * @author wanghl
 *
 */
@Service
public class TNTLMCATWithdrawCalc {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	@PersistenceContext
	private EntityManager em;
	@Autowired
	AppapiCommService appapiCommService;
	@Autowired
	private OpenAcctCommService openAcctCommService;
	@Autowired
	private GlobalManagementService globalManagementService;
	@Autowired
	private MicroCreditRescheduleUtils microCreditRescheduleUtils;
    @Autowired
    AcctOTBCal acctOTBCal;
    @Autowired
    UnifiedParamFacilityProvide unifiedParamFacilityProvide;
	@Autowired
	private AcctUnpostAmtCalUtil acctUnpostAmtCalUtil;
    @Autowired
    private QueryCommService queryCommService;
    
	private QCcsAcct qAcct = QCcsAcct.ccsAcct;
	private QCcsAcctO qAccto = QCcsAcctO.ccsAcctO;
	private QCcsLoan qCcsLoan = QCcsLoan.ccsLoan;
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	
	public TNTLMCATWithdrawCalcResp handler(TNTLMCATWithdrawCalcReq req){
		TxnInfo txnInfo = new TxnInfo();
		TxnContext context = new TxnContext();
		context.setTxnInfo(txnInfo);
		
		LogTools.printLogger(logger, "TNTLMCATWithdrawCalcReq", "提现试算", req, true);
		TNTLMCATWithdrawCalcResp resp = new TNTLMCATWithdrawCalcResp();
		try {
			
			Date bizDate = globalManagementService.getSystemStatus().getBusinessDate();
			txnInfo.setContrNo(req.getContrNbr());
			txnInfo.setBizDate(bizDate );
			
			this.checkReq(req, txnInfo);
			
			appapiCommService.loadAcctByContrNo(context, true);
			appapiCommService.loadAcctOByAcct(context, true);
			openAcctCommService.getProdParam(context);
			
			this.validateForApply(context);
			
			CcsAcct acct = new JPAQuery(em).from(qAcct).where(qAcct.contrNbr.eq(req.getContrNbr()))
					.singleResult(qAcct);

			if(acct == null) throw new ProcessException(MsRespCode.E_1003.getCode(), MsRespCode.E_1003.getMessage());
			
			if(acct.getAcctExpireDate().compareTo(bizDate)<0){
				throw new ProcessException(MsRespCode.E_1056.getCode(), MsRespCode.E_1056.getMessage());
			}
			
			CcsAcctO accto = new JPAQuery(em).from(qAccto)
					.where(qAccto.acctNbr.eq(acct.getAcctNbr()).and(qAccto.acctType.eq(acct.getAcctType())))
					.singleResult(qAccto);
			
			CcsLoan loan = new JPAQuery(em).from(qCcsLoan).where(qCcsLoan.contrNbr.eq(req.getContrNbr()))
					.singleResult(qCcsLoan); 

			BigDecimal sumAmount = BigDecimal.ZERO;
			BigDecimal txnFee = BigDecimal.ZERO;//试算的手续费
			BigDecimal interestAmt = BigDecimal.ZERO;//试算的利息
			BigDecimal acctUnpostAmt = BigDecimal.ZERO;//未入账累积利息费用
			if ( null != loan  ) {
				List<CcsPlan> planlist = microCreditRescheduleUtils.getPlanIListByLoanId(loan);
				List<CcsRepaySchedule> scheduleList = queryCommService.getSchedulelistByLoan(loan.getLoanId());
				LoanFeeDef loanFeeDef=unifiedParamFacilityProvide.loanFeeDefByKey(loan.getLoanCode(),Integer.valueOf(acct.getLoanFeeDefId()));
				acctUnpostAmt=acctUnpostAmtCalUtil.AcctUnpostAmtCal(loanFeeDef, acct, loan, planlist, scheduleList, req.getBizDate());
			}else {
				acctUnpostAmt = BigDecimal.ZERO;
			}
			
			BigDecimal svnFee = BigDecimal.ZERO;//未入账提现手续费
			svnFee=appapiCommService.sumWithDrawTxnFee(acct);//提现手续费
			//是否存在溢缴款=账户余额+未入账累积息费+未入账手续费+memo_db-memo_cr
			sumAmount = acct.getCurrBal().add(acctUnpostAmt).add(svnFee).add(accto.getMemoDb()).subtract(accto.getMemoCr());
			if(sumAmount.compareTo(BigDecimal.ZERO) < 0) {
				txnFee = appapiCommService.loadWithDrawTxnFee(acct.getProductCd(), acct.getOrg(), req.getAmount().add(sumAmount));
				//计算到下个账单日的利息
				interestAmt = computeInterest(acct.getInterestRate(), req.getAmount().add(sumAmount), 1 + getStmtLength(bizDate, acct.getNextStmtDate()));
			}else {
				txnFee = appapiCommService.loadWithDrawTxnFee(acct.getProductCd(), acct.getOrg(), req.getAmount());
				//计算到下个账单日的利息
				interestAmt = computeInterest(acct.getInterestRate(), req.getAmount(), 1 + getStmtLength(bizDate, acct.getNextStmtDate()));
			}
			//校验体现手续费小于0
			if(txnFee.compareTo(BigDecimal.ZERO)<0){
				txnFee = BigDecimal.ZERO;
			}
			//校验计算到下个账单日的利息小于0
			if(interestAmt.compareTo(BigDecimal.ZERO)<0){
				interestAmt= BigDecimal.ZERO;
			}
			//校验不存在溢缴款
			if (sumAmount.compareTo(BigDecimal.ZERO)>0) {
				sumAmount = BigDecimal.ZERO;
			}
			//提现金额+手续费+利息
			BigDecimal withdrawQualGraceBal = 
					req.getAmount()
					.add(txnFee)
					.add(interestAmt)
					.add(sumAmount);
			//小于0修正
			if(withdrawQualGraceBal.compareTo(BigDecimal.ZERO) < 0){
				withdrawQualGraceBal = BigDecimal.ZERO;
			}
			
			resp.setContrNbr(req.getContrNbr());
			
			resp.setWithdrawQualGraceBal(withdrawQualGraceBal.setScale(2, RoundingMode.HALF_UP) );
			resp.setCashCharge(txnFee.setScale(2, RoundingMode.HALF_UP));
			
			BigDecimal contraRemain = acctOTBCal.getAcctOTB(null, acct, accto, bizDate);
			//可用额度=信用额度-memodb+memocr-账户余额-未入账提现手续费-未入账的累积息费  by lizz 20160323
			resp.setContraRemain(contraRemain.subtract(svnFee).subtract(acctUnpostAmt) );
			
			resp.setContrLmt(acct.getCreditLmt().setScale(2, RoundingMode.HALF_UP));
			
			Date nextPaymentDay = microCreditRescheduleUtils.getNextPaymentDay(acct.getProductCd(), acct.getNextStmtDate());
			resp.setPmtDueDate(sdf.format(nextPaymentDay));
		} catch (ProcessException pe) {
			appapiCommService.preException(pe, pe, txnInfo);
			logger.error(pe.getMessage(), pe);
		} catch (Exception e){
			appapiCommService.preException(e, null, txnInfo);
			logger.error(e.getMessage(), e);
		}
		setResponse(resp, txnInfo);
		
		LogTools.printLogger(logger, "TNTLMCATWithdrawCalcResp", "提现试算", resp, false);
		return resp;
	}

	private void checkReq(TNTLMCATWithdrawCalcReq req, TxnInfo txnInfo) {
		if(req.getAmount().compareTo(BigDecimal.ZERO)<=0){
			throw new ProcessException(MsRespCode.E_1043.getCode(),MsRespCode.E_1043 .getMessage()+ ",字段名称{AMOUNT},必须大于0");
		}
	}

	private void validateForApply(TxnContext context) {
		CcsAcct acct = context.getAccount();
		TxnInfo txnInfo = context.getTxnInfo();
		LoanPlan loanPlan = context.getLoanPlan();
		
		if(loanPlan.loanType != LoanType.MCAT){
			throw new ProcessException(MsRespCode.E_1052.getCode(),MsRespCode.E_1052.getMessage()+"，非随借随还贷款产品");
		}
		
		if(loanPlan.loanMold != LoanMold.C){
			throw new ProcessException(MsRespCode.E_1052.getCode(),MsRespCode.E_1052.getMessage()+"，非随借随还贷款产品");
		}
		if(logger.isDebugEnabled())
			logger.debug("合同放款有效期:[{}],业务日期：[{}]",acct.getAcctExpireDate(),txnInfo.getBizDate());
		if(acct.getAcctExpireDate().compareTo(txnInfo.getBizDate())<0){
			throw new ProcessException(MsRespCode.E_1056.getCode(), MsRespCode.E_1056.getMessage());
		}
	}

	private int getStmtLength(Date bizDate, Date nextStmtDate) {
		return DateUtils.getIntervalDays(bizDate, nextStmtDate);
	}
	
	private void setResponse(MsResponseInfo resp, TxnInfo txnInfo) {
		
		resp.setErrorCode(txnInfo.getResponsCode());
		resp.setErrorMessage(txnInfo.getResponsDesc());
		if(StringUtils.equals(MsRespCode.E_0000.getCode(), txnInfo.getResponsCode())){
			resp.setStatus("S");//交易成功
		}else{
			resp.setStatus("F");//交易失败
		}
	}
	/**
	 * 
	 * @param rate
	 * @param bal
	 * @param days
	 * @return
	 */
	public BigDecimal computeInterest(BigDecimal rate,BigDecimal bal,long days){
		BigDecimal bd = new BigDecimal(1.0/MicroCreditRescheduleUtils.YEAR_DAYS).setScale(20,RoundingMode.HALF_UP);
		bd = bd.multiply(bal).multiply(rate).multiply(BigDecimal.valueOf(days));
		return bd.setScale(6, RoundingMode.HALF_UP);
	}


}
