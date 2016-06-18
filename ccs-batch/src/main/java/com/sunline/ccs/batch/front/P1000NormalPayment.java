package com.sunline.ccs.batch.front;

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
import org.springframework.stereotype.Component;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ccs.facility.MicroCreditRescheduleUtils;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctKey;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctOKey;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsOrder;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.CcsRepaySchedule;
import com.sunline.ccs.infrastructure.shared.model.QCcsPlan;
import com.sunline.ccs.loan.McLoanProvideImpl;
import com.sunline.ppy.dictionary.enums.LoanUsage;
import com.sunline.ppy.dictionary.enums.PlanType;

/**
 * 正常代扣
 * @author zhangqiang
 *
 */
@Component
public class P1000NormalPayment implements ItemProcessor<CcsRepaySchedule, Object> {
	
	private static final Logger logger = LoggerFactory.getLogger(P1000NormalPayment.class);
	
	@Autowired
	private FrontBatchUtil initOrderUtil;
	
	@Autowired
	private McLoanProvideImpl mcLoanProvideImpl;
	
	@Autowired
	private MicroCreditRescheduleUtils rescheduleUtils;
	
	@Autowired
	private FrontBatchUtil frontBatchUtil;
	
	@PersistenceContext
	private EntityManager em;
	
	@Autowired
	private BatchStatusFacility batchStatusFacility;
	
	@Override
	public Object process(CcsRepaySchedule repaySchedule) throws Exception {
		
		CcsLoan loan = em.find(CcsLoan.class, repaySchedule.getLoanId());
		CcsAcctO accto = em.find(CcsAcctO.class, new CcsAcctOKey(loan.getAcctNbr(), loan.getAcctType()));
		// 未匹配贷记金额
		BigDecimal memoCr = accto.getMemoCr();
		
		if (logger.isDebugEnabled()) {
			logger.debug("正常代扣开始...");
			logger.debug("LoanId" + "[" + loan.getLoanId() + "]");
			logger.debug("ScheduleId" + "[" + repaySchedule.getScheduleId() + "]");
		}
		
		// 若有处理中订单一律不出代扣
		if (frontBatchUtil.getWOrderCount(loan.getAcctNbr(), loan.getAcctType()) > 0) {
			if (logger.isDebugEnabled()) {
				logger.debug("存在处理中订单，不出代扣");
			}
			return null;
		}
		
		// 逾期并且之前未发生过逾期代扣，不出正常代扣，出逾期代扣
		Date businessDate = batchStatusFacility.getSystemStatus().getBusinessDate();
		if (loan.getOverdueDate() != null && 
				frontBatchUtil.getOrderCount(loan.getAcctNbr(), loan.getAcctType(), null, businessDate, LoanUsage.O) == 0) {
			if (logger.isDebugEnabled()) {
				logger.debug("逾期并且之前未发生过逾期代扣，不出正常代扣，出逾期代扣");
			}
			return null;
		}
		
		// 未逾期并且当日已有正常/提前的成功扣款，则不出代扣
		if (loan.getOverdueDate() == null && 
				frontBatchUtil.getOrderCount(loan.getAcctNbr(), loan.getAcctType(), null, null, LoanUsage.N, LoanUsage.M) > 0)
			return null;
		
		//当天存在退货成功交易，不出正常代扣
		if(frontBatchUtil.isExistRefund(repaySchedule.getAcctNbr(),repaySchedule.getAcctType(),batchStatusFacility.getSystemStatus().getBusinessDate())){
			if(logger.isDebugEnabled()){
				logger.debug("当天存在退货成功交易，不出正常代扣");
			}
			return null;
		}
		
		// 金额 = 期供应还金额
		BigDecimal txnAmt = repaySchedule.getLoanTermPrin()
				.add(repaySchedule.getLoanTermInt())
				.add(repaySchedule.getLoanTermFee())
				.add(repaySchedule.getLoanStampdutyAmt())
				.add(repaySchedule.getLoanInsuranceAmt())
				.add(repaySchedule.getLoanSvcFee())
				.add(repaySchedule.getLoanLifeInsuAmt())
				.add(repaySchedule.getLoanReplaceSvcFee())
				.add(repaySchedule.getLoanPrepayPkgAmt());
		
		// 加上转出计划的非延迟利息
		QCcsPlan qPlan = QCcsPlan.ccsPlan;
		List<CcsPlan> planList = new JPAQuery(em).from(qPlan).where(qPlan.acctNbr.eq(loan.getAcctNbr()).and(qPlan.acctType.eq(loan.getAcctType()))).list(qPlan);
		
		// 加上因容忍金额导致可能存在的欠款
		BigDecimal currbal = BigDecimal.ZERO;
		
		if(planList != null){
			for(CcsPlan plan : planList){
				if (PlanType.Q.equals(plan.getPlanType())) {
					currbal = currbal.add(plan.getCurrBal());
					//加上转入计划上的利息累计
					currbal = currbal.add(plan.getNodefbnpIntAcru());
					currbal = currbal.add(plan.getPenaltyAcru().add(plan.getCompoundAcru().add(plan.getReplacePenaltyAcru())));
					//正常情况下账单日结息前不累计罚息/复利
					//针对退货未结清的特殊情况，考虑将罚息复利罚金加入
					currbal = currbal.add(plan.getPastPenalty().add(plan.getPastCompound().add(plan.getCtdMulctAmt())));
				}
			}
		}
		
		txnAmt = txnAmt.add(currbal.setScale(2,RoundingMode.HALF_UP));
		
		// 减去溢缴款
		txnAmt = txnAmt.subtract(mcLoanProvideImpl.genLoanDeposit(loan));
			
		// 减去豁免金额
		txnAmt = txnAmt.subtract(frontBatchUtil.getTxnWaiveAmt(loan.getLoanId()));
		
		//减去未匹配贷记金额
		txnAmt = txnAmt.subtract(memoCr);
		
		//存在优惠券扣款订单
		CcsOrder order = frontBatchUtil.getCouponAmout(loan.getAcctNbr(), loan.getAcctType());
		if(order!=null){
			// 利息成分累计
			BigDecimal intBal = BigDecimal.ZERO;
			for(CcsPlan plan : planList){
				if (plan.getPlanType().isXfrIn()) {
					//加上转入计划上的利息累计
					intBal = intBal.add(plan.getNodefbnpIntAcru());
					intBal = intBal.add(plan.getPenaltyAcru().add(plan.getCompoundAcru().add(plan.getReplacePenaltyAcru())));
					//正常情况下账单日结息前不累计罚息/复利
					//针对退货未结清的特殊情况，考虑将罚息复利罚金加入
					intBal = intBal.add(plan.getPastPenalty().add(plan.getPastCompound().add(plan.getCtdMulctAmt())));
				}
			}
			// 利息成分=罚息、复利、非延、代收罚息累计+期供利息
			intBal = intBal.setScale(2,RoundingMode.HALF_UP);
			intBal = intBal.add(repaySchedule.getLoanTermInt());
			logger.debug("当期应还利息："+intBal);
			// 实际抵扣利息金额
			BigDecimal creditAmt = intBal.subtract(order.getTxnAmt());
			if(creditAmt.compareTo(BigDecimal.ZERO)<0){
				creditAmt = intBal;
			}else{
				creditAmt = order.getTxnAmt();
			}
			logger.debug("实际抵扣金额："+creditAmt);
			logger.debug("还款金额："+ mcLoanProvideImpl.genLoanDeposit(loan));
			logger.debug("订单金额："+ txnAmt);
			txnAmt = txnAmt.subtract(creditAmt);
		}
				
		if(txnAmt.compareTo(BigDecimal.ZERO) <= 0)
			return null;
		
		CcsAcct acct = em.find(CcsAcct.class, new CcsAcctKey(loan.getAcctNbr(), loan.getAcctType()));
		CcsCustomer cust = em.find(CcsCustomer.class, acct.getCustId());
		
		if (logger.isDebugEnabled()) {
			logger.debug("正常代扣：Org[" + acct.getOrg()
					+ "],AcctType[" + acct.getAcctType()
					+ "],AcctNo[" + acct.getAcctNbr()
					+ "],DueBillNo[" + loan.getDueBillNo()
					+ "]");
		}
		
		// 生成订单
		initOrderUtil.initOrder(acct, cust, loan, LoanUsage.N, txnAmt, null);
		
		return null;
	}

}
