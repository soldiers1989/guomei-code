package com.sunline.ccs.batch.rpt.cca210;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
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
import com.sunline.ccs.batch.rpt.cca210.item.MsLoanBalanceRptItem;
import com.sunline.ccs.batch.rpt.common.RptBatchUtil;
import com.sunline.ccs.facility.MicroCreditRescheduleUtils;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctKey;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.CcsRepaySchedule;
import com.sunline.ccs.infrastructure.shared.model.QCcsPlan;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.pcm.param.def.Product;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.LoanStatus;
import com.sunline.ppy.dictionary.enums.PlanType;

/**
 *	马上贷贷款余额报表
 */
public class PA213MsLoanBalanceRpt implements ItemProcessor<CcsLoan, MsLoanBalanceRptItem> {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private RCcsAcct rAcct;
	
	@Autowired
	private BatchStatusFacility batchFacility;
	@Autowired
	private RptBatchUtil rptBatchUtil;
	@Autowired
	private UnifiedParameterFacility unifiedParameterFacility;

	@PersistenceContext
    private EntityManager em;
	@Autowired
	private MicroCreditRescheduleUtils rescheduleUtils;
	
	@Override
	public MsLoanBalanceRptItem process(CcsLoan loan) throws Exception {
		MsLoanBalanceRptItem item = new MsLoanBalanceRptItem();
		logger.info("马上贷贷款余额报表==Loan_Id[{}]==", loan.getLoanId());
		
		CcsAcct acct = null;
		
		acct = rAcct.findOne(new CcsAcctKey(loan.getAcctNbr(), loan.getAcctType()));
		if(acct == null  ){
			return item;
		}
		rptBatchUtil.setCurrOrgNoToContext();
		Product product = unifiedParameterFacility.loadParameter(acct.getProductCd() , Product.class);
		LoanPlan loanPlan = unifiedParameterFacility.loadParameter(loan.getLoanCode(),LoanPlan.class);

		item.productCd = product.productCode;
		item.productDesc = product.description;
		item.loanCode = loanPlan.loanCode;
		item.loanDesc = loanPlan.description;
		item.contrNbr = loan.getContrNbr();
		item.name = acct.getName();
		item.idNo = acct.getCustId().toString();
		item.searchDate = batchFacility.getBatchDate();
		item.activeDate = loan.getActiveDate();
		item.loanAmt = loan.getLoanInitPrin().setScale(2, RoundingMode.HALF_UP);
		item.loanTerm = loan.getLoanInitTerm();
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
		QCcsPlan qPlan = QCcsPlan.ccsPlan;
		List<CcsPlan> plans =new JPAQuery(em).from(qPlan)
				.where(qPlan.acctNbr.eq(loan.getAcctNbr()).and(qPlan.acctType.eq(loan.getAcctType()))
				.and(qPlan.refNbr.eq(loan.getRefNbr()))
				.and(qPlan.planType.eq(PlanType.Q)))
				.list(qPlan);
		//已出未还贷款本金余额
		for(CcsPlan plan : plans){
			balance = balance.add(plan.getPastPrincipal()).add(plan.getCtdPrincipal());
		}
		//未出贷款本金余额
		balance = balance.add(loan.getUnstmtPrin());
		item.balance = balance.setScale(2, RoundingMode.HALF_UP);
		
		
		
		BigDecimal trialInterest = new BigDecimal(0);
		BigDecimal trialSvcFee  = new BigDecimal(0);
		BigDecimal trialMulct = new BigDecimal(0);
		BigDecimal trialOptionalSvcFee = new BigDecimal(0);
		BigDecimal trialPrepayPkgFee = new BigDecimal(0);
		BigDecimal trialPenalty = new BigDecimal(0);
		BigDecimal trialLpc = new BigDecimal(0);
		
		logger.info("合同编号[{}]保单号[{}]借据号[{}]账户号[{}]账户类型[{}]LoanId[{}]",
				loan.getContrNbr(),loan.getGuarantyId(),loan.getDueBillNo(),loan.getAcctNbr(),loan.getAcctType(),loan.getLoanId());
		for(CcsPlan plan : plans){
			logger.debug("往期planId[{}]term[{}]",plan.getPlanId(), plan.getTerm());
			trialMulct = trialMulct.add(plan.getCtdMulctAmt()).add(plan.getPastMulctAmt());
			trialInterest = trialInterest.add(plan.getCtdInterest()).add(plan.getPastInterest());
			trialSvcFee = trialSvcFee.add(plan.getCtdSvcFee()).add(plan.getPastSvcFee());
			trialOptionalSvcFee = trialOptionalSvcFee.add(plan.getCtdLifeInsuAmt()).add(plan.getPastLifeInsuAmt());
			trialPrepayPkgFee = trialPrepayPkgFee.add(plan.getCtdPrepayPkgFee()).add(plan.getPastPrepayPkgFee());
			trialPenalty.add(plan.getCtdPenalty().add(plan.getPastPenalty()));
			trialLpc = trialLpc.add(plan.getCtdLateFee()).add(plan.getPastLateFee());
			//超过宽限日加上罚息累计
			int overGraceDays = DateUtils.getIntervalDays(acct.getGraceDate(), batchFacility.getBatchDate() );
			if(overGraceDays  >= 0){
				logger.debug("超过宽限日[{}][{}]天，预提罚息加上罚息累计", 
						new SimpleDateFormat("yyyy-MM-dd").format(acct.getGraceDate()),overGraceDays);
				trialPenalty = trialPenalty.add(plan.getPenaltyAcru());
			}
		}
		
		if(loan.getLoanStatus() != LoanStatus.T && loan.getLoanStatus() != LoanStatus.F ){
			logger.info("贷款未完成或终止，取下一期的还款计划");
			List<CcsRepaySchedule> origSchedules = rescheduleUtils.getRepayScheduleListByLoanId(loan.getLoanId());
			//当期的还款计划开始日期
			Date beginDate = loan.getActiveDate();
			//下一期的还款计划
			CcsRepaySchedule origNextSchedule = null;
			for (CcsRepaySchedule s : origSchedules) {
				if(s.getCurrTerm().intValue() == loan.getCurrTerm().intValue()){
					beginDate = s.getLoanPmtDueDate();
				}
				if(s.getCurrTerm().intValue() == loan.getCurrTerm().intValue()+1){
					origNextSchedule = s;
				}
			}
			if(origNextSchedule != null){
				//当期已过天数
				Integer pastPlanDays = DateUtils.getIntervalDays(beginDate, batchFacility.getBatchDate());
				//当期总天数
				Integer totalPlanDays = DateUtils.getIntervalDays(beginDate, origNextSchedule.getLoanPmtDueDate());
				logger.debug("当期已过天数[{}],当期总天数[{}]", pastPlanDays, totalPlanDays);
				if(pastPlanDays.compareTo(0)>0 && totalPlanDays.compareTo(0)>0 ){
					if(totalPlanDays.compareTo(pastPlanDays)>0){
						trialInterest = trialInterest.add(origNextSchedule.getLoanTermInt()
								.multiply(new BigDecimal(pastPlanDays))
								.divide(new BigDecimal(totalPlanDays), 2, RoundingMode.HALF_UP));
						
						trialSvcFee = trialSvcFee.add((origNextSchedule.getLoanTermFee())
								.multiply(new BigDecimal(pastPlanDays))
								.divide(new BigDecimal(totalPlanDays), 2, RoundingMode.HALF_UP));
						
						trialOptionalSvcFee = trialOptionalSvcFee.add((origNextSchedule.getLoanLifeInsuAmt())
								.multiply(new BigDecimal(pastPlanDays))
								.divide(new BigDecimal(totalPlanDays), 2, RoundingMode.HALF_UP));
						
						trialPrepayPkgFee = trialPrepayPkgFee.add((origNextSchedule.getLoanPrepayPkgAmt())
								.multiply(new BigDecimal(pastPlanDays))
								.divide(new BigDecimal(totalPlanDays), 2, RoundingMode.HALF_UP));
					}else{
						trialInterest = trialInterest.add(origNextSchedule.getLoanTermInt());
						trialSvcFee = trialSvcFee.add(origNextSchedule.getLoanTermFee());
						trialOptionalSvcFee = trialOptionalSvcFee.add(origNextSchedule.getLoanLifeInsuAmt());
						trialPrepayPkgFee = trialPrepayPkgFee.add(origNextSchedule.getLoanPrepayPkgAmt());
					}
				}
				
			}
		}
		item.trialInterest = trialInterest.setScale(2, RoundingMode.HALF_UP);
		item.trialSvcFee = trialSvcFee.setScale(2, RoundingMode.HALF_UP);
		item.trialLifeInsFee = trialOptionalSvcFee.setScale(2, RoundingMode.HALF_UP);
		item.trialMulct = trialMulct.setScale(2, RoundingMode.HALF_UP);
		item.trialPrepayPKGAmt = trialPrepayPkgFee.setScale(2, RoundingMode.HALF_UP);
		item.trialPenalty = trialPenalty.setScale(2, RoundingMode.HALF_UP);
		item.trialLpc = trialLpc.setScale(2, RoundingMode.HALF_UP);
		return item;
	}

}
