package com.sunline.ccs.batch.rpt.cca200;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
import com.sunline.ccs.batch.rpt.cca200.items.LoanBalanceRptItem;
import com.sunline.ccs.batch.rpt.common.RptParamFacility;
import com.sunline.ccs.facility.MicroCreditRescheduleUtils;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctKey;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.CcsRepaySchedule;
import com.sunline.ccs.infrastructure.shared.model.CcsSettleClaim;
import com.sunline.ccs.infrastructure.shared.model.QCcsPlan;
import com.sunline.ccs.infrastructure.shared.model.QCcsSettleClaim;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.param.def.enums.LoanTerminateReason;
import com.sunline.pcm.param.def.Product;
import com.sunline.ppy.dictionary.enums.LoanStatus;
import com.sunline.ppy.dictionary.enums.PlanType;

/**
 *	贷款余额报表
 */
public class PA202LoanBalanceRpt implements ItemProcessor<CcsLoan, LoanBalanceRptItem> {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private RCcsAcct rAcct;
	
	@Autowired
	private BatchStatusFacility batchFacility;
	@PersistenceContext
    private EntityManager em;
	@Autowired
	private RptParamFacility codeProv;
	@Autowired
	private MicroCreditRescheduleUtils rescheduleUtils;
	
	@Override
	public LoanBalanceRptItem process(CcsLoan loan) throws Exception {
		
		LoanBalanceRptItem item = new LoanBalanceRptItem();
		
		CcsAcct acct = null;
		
		acct = rAcct.findOne(new CcsAcctKey(loan.getAcctNbr(), loan.getAcctType()));
		if(acct == null ){
			return item;
		}
		
		Product product = codeProv.loadProduct(acct.getProductCd());
		LoanPlan loanPlan = codeProv.loadLoanPlan(loan.getLoanCode());
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
		BigDecimal trialIns  = new BigDecimal(0);
		BigDecimal trialMulct = new BigDecimal(0);
		
		//理赔
		if(loan.getTerminalReasonCd() == LoanTerminateReason.C){
			QCcsSettleClaim qClaim = QCcsSettleClaim.ccsSettleClaim;
			CcsSettleClaim claim = new JPAQuery(em).from(qClaim)
					.where(qClaim.loanId.eq(loan.getLoanId()))
					.singleResult(qClaim);
			logger.info("已申请理赔，未理赔成功");
			logger.info("合同编号[{}]保单号[{}]借据号[{}]账户号[{}]账户类型[{}]LoanId[{}]",
					claim.getContrNbr(),claim.getGuarantyId(),claim.getDueBillNo(),claim.getAcctNbr(),claim.getLoanId());
			
			trialInterest = claim.getSettleInterest();
			trialIns = claim.getSettleInsuranceAmt();
			trialMulct = claim.getSettleMulct();
		}else{
			logger.info("合同编号[{}]保单号[{}]借据号[{}]账户号[{}]账户类型[{}]LoanId[{}]",
					loan.getContrNbr(),loan.getGuarantyId(),loan.getDueBillNo(),loan.getAcctNbr(),loan.getAcctType(),loan.getLoanId());
			for(CcsPlan plan : plans){
				logger.debug("往期planId[{}]term[{}]",plan.getPlanId(), plan.getTerm());
				trialMulct = trialMulct.add(plan.getCtdMulctAmt()).add(plan.getPastMulctAmt());
				trialInterest = trialInterest.add(plan.getCtdInterest()).add(plan.getPastInterest());
				trialIns = trialIns.add(plan.getCtdInsurance()).add(plan.getPastInsurance());
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
					if(pastPlanDays.compareTo(0)>0 && totalPlanDays.compareTo(0)>0 ){
						if(totalPlanDays.compareTo(pastPlanDays)>0){
							trialInterest = trialInterest.add(origNextSchedule.getLoanTermInt()
									.multiply(new BigDecimal(pastPlanDays))
									.divide(new BigDecimal(totalPlanDays), 2, RoundingMode.HALF_UP));
							
							trialIns = trialIns.add((origNextSchedule.getLoanInsuranceAmt())
									.multiply(new BigDecimal(pastPlanDays))
									.divide(new BigDecimal(totalPlanDays), 2, RoundingMode.HALF_UP));
						}else{
							trialInterest = trialInterest.add(origNextSchedule.getLoanTermInt());
							trialIns = trialIns.add(origNextSchedule.getLoanInsuranceAmt());
						}
					}
					
				}
			}
		}
		item.trialInterest = trialInterest.setScale(2, RoundingMode.HALF_UP);
		item.trialIns = trialIns.setScale(2, RoundingMode.HALF_UP);
		item.trialMulct = trialMulct.setScale(2, RoundingMode.HALF_UP);
		
		return item;
	}

}
