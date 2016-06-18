package com.sunline.ccs.batch.front;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
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
import com.sunline.ccs.infrastructure.shared.model.QCcsRepaySchedule;
import com.sunline.ccs.loan.McLoanProvideImpl;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.pcm.param.def.FinancialOrg;
import com.sunline.pcm.param.def.Mulct;
import com.sunline.pcm.param.def.Split;
import com.sunline.pcm.param.def.enums.MulctMethod;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanUsage;
import com.sunline.ppy.dictionary.enums.OrderStatus;
import com.sunline.ppy.dictionary.enums.PlanType;

/**
 * 逾期拆分代扣
 * @author zhangqiang
 *
 */
@Component
public class P2000OverduePayment implements ItemProcessor<CcsLoan, SFrontInfo> {
	
	private static final Logger logger = LoggerFactory.getLogger(P2000OverduePayment.class);
	
	@Autowired
	private BatchStatusFacility batchStatusFacility;
	
	@Autowired
	private UnifiedParameterFacility unifiedParameterFacility;
	
	@Autowired
	private FrontBatchUtil frontBatchUtil;
	
	@Autowired
	private McLoanProvideImpl mcLoanProvideImpl;
	@PersistenceContext
	private EntityManager em;
	@Autowired
	private MicroCreditRescheduleUtils rescheduleUtils;
	
	@Override
	public SFrontInfo process(CcsLoan loan) throws Exception {
		
		OrganizationContextHolder.setCurrentOrg(loan.getOrg());
		
		LoanFeeDef loanFeeDef = rescheduleUtils.getLoanFeeDef(loan.getLoanCode(), loan.getLoanInitTerm(),
				loan.getLoanInitPrin(),loan.getLoanFeeDefId());
		Mulct fine = unifiedParameterFacility.loadParameter(loanFeeDef.mulctTableId, Mulct.class);
		int overdueDays = 0;
		if(fine.mulctMethod == MulctMethod.CPD ){
			if(loan.getCpdBeginDate() != null){
				overdueDays = DateUtils.getIntervalDays(loan.getCpdBeginDate(), batchStatusFacility.getSystemStatus().getBusinessDate());
			}else{
				return null;	
			}
			
		}
		if(fine.mulctMethod == MulctMethod.DPD ){
			if(loan.getOverdueDate() != null){
				overdueDays = DateUtils.getIntervalDays(loan.getOverdueDate(), batchStatusFacility.getSystemStatus().getBusinessDate());
			}else{
				return null;	
			}
		}
		
		// 若有处理中订单一律不出代扣
		if(frontBatchUtil.getWOrderCount(loan.getAcctNbr(), loan.getAcctType())>0){
			return null;
		}
		// 若当日有正常/逾期/提前/PTP的成功扣款, 则不出代扣
		if(frontBatchUtil.getOrderCount(loan.getAcctNbr(), loan.getAcctType(), null, null, LoanUsage.N, LoanUsage.O, LoanUsage.M,LoanUsage.P)>0){
			return null;
		}
		
		CcsAcct acct = em.find(CcsAcct.class, new CcsAcctKey(loan.getAcctNbr(), loan.getAcctType()));
		CcsCustomer cust = em.find(CcsCustomer.class, acct.getCustId());
		CcsAcctO accto = em.find(CcsAcctO.class, new CcsAcctOKey(loan.getAcctNbr(), loan.getAcctType()));
		// 未匹配贷记金额
		BigDecimal memoCr = accto.getMemoCr();
		
		// 当日是否有当期的期供
		QCcsRepaySchedule s = QCcsRepaySchedule.ccsRepaySchedule;
		CcsRepaySchedule schedule = new JPAQuery(em).from(s)
				.where(s.loanId.eq(loan.getLoanId())
					.and(s.loanPmtDueDate.eq(batchStatusFacility.getSystemStatus().getBusinessDate())))
				.singleResult(s);
			
		if (logger.isDebugEnabled()) {
			logger.debug("逾期代扣：Org["+acct.getOrg()
					+"],AcctType["+acct.getAcctType()
					+"],AcctNo["+acct.getAcctNbr()
					+"],DueBillNo["+loan.getDueBillNo()
					+"]");
		}
		
		// 获取理赔天数参数
		ProductCredit productCredit = unifiedParameterFacility.loadParameter(acct.getProductCd(), ProductCredit.class);
		int claimDays = productCredit.claimsDays;
		// 逾期天数>=理赔天数   则不代扣
		if(overdueDays >= claimDays){
			return null;
		}
		
		//当天存在退货成功交易，不出逾期代扣
		if(frontBatchUtil.isExistRefund(loan.getAcctNbr(),loan.getAcctType(),batchStatusFacility.getSystemStatus().getBusinessDate())){
			if(logger.isDebugEnabled()){
				logger.debug("当天存在退货成功交易，不出逾期代扣");
			}
			return null;
		}
				
		// 使用追偿拆分规则
		ProductCredit pc = unifiedParameterFacility.loadParameter(acct.getProductCd(), ProductCredit.class);
		FinancialOrg finanicalOrg = unifiedParameterFacility.loadParameter(pc.financeOrgNo, FinancialOrg.class);
		Split split = unifiedParameterFacility.loadParameter(finanicalOrg.splitTableId, Split.class);
		// 待拆分金额  = 逾期代扣金额
		BigDecimal needSplitAmt = mcLoanProvideImpl.genLoanBal(loan);
		// 若当日有正常到期的期供, 还应加上到期期供的应还款
		if(schedule != null){
			BigDecimal scheduleAmt = schedule.getLoanTermPrin()
					.add(schedule.getLoanTermInt())
					.add(schedule.getLoanTermFee())
					.add(schedule.getLoanStampdutyAmt())
					.add(schedule.getLoanInsuranceAmt())
					.add(schedule.getLoanLifeInsuAmt())
					.add(schedule.getLoanSvcFee())
					.add(schedule.getLoanReplaceSvcFee())
					.add(schedule.getLoanPrepayPkgAmt());
			needSplitAmt = needSplitAmt.add(scheduleAmt);
		}
		
		// 加上转入计划的非延迟利息-->增加对宽限日判断，宽限日后逾期金额加上转入计划的罚息累计和复利累计
		QCcsPlan qPlan = QCcsPlan.ccsPlan;
		List<CcsPlan> planList = new JPAQuery(em).from(qPlan).where(qPlan.acctNbr.eq(loan.getAcctNbr()).and(qPlan.acctType.eq(loan.getAcctType()))).list(qPlan);
		for (CcsPlan plan : planList) {
			if (PlanType.Q.equals(plan.getPlanType())) {
				needSplitAmt = needSplitAmt.add(plan.getNodefbnpIntAcru().setScale(2, RoundingMode.HALF_UP));
				if(DateUtils.truncatedCompareTo(batchStatusFacility.getSystemStatus().getBusinessDate(), acct.getGraceDate(), Calendar.DATE) > 0){
					needSplitAmt = needSplitAmt.add(plan.getPenaltyAcru().setScale(2, RoundingMode.HALF_UP)).add(plan.getCompoundAcru().setScale(2, RoundingMode.HALF_UP));
					needSplitAmt = needSplitAmt.add(plan.getReplacePenaltyAcru().setScale(2, RoundingMode.HALF_UP));
				}
			}
		}
		
		// 减去豁免金额
		needSplitAmt = needSplitAmt.subtract(frontBatchUtil.getTxnWaiveAmt(loan.getLoanId()));
		
		// 减去未匹配贷记金额
		needSplitAmt = needSplitAmt.subtract(memoCr);
		
		if(needSplitAmt.compareTo(BigDecimal.ZERO) <= 0)
			return null;
		
		// 生成原始订单
		CcsOrder origOrder = frontBatchUtil.initOrder(acct, cust, loan, LoanUsage.O, needSplitAmt, null);
		origOrder.setOrderStatus(OrderStatus.G);
		origOrder.setMatchInd(Indicator.N);
		
		//*如果扣款银行为光大银行
		if("0303".equals(origOrder.getOpenBankId())){
			BigDecimal splitAmt1 = needSplitAmt.multiply(new BigDecimal(0.8)).setScale(2,BigDecimal.ROUND_HALF_UP);
			CcsOrder splitOrder = frontBatchUtil.initOrder(acct, cust, loan, LoanUsage.O, splitAmt1, null);
			splitOrder.setOriOrderId(origOrder.getOrderId());
			CcsOrder splitOrder2 = frontBatchUtil.initOrder(acct, cust, loan, LoanUsage.O, needSplitAmt.subtract(splitAmt1), null);
			splitOrder2.setOriOrderId(origOrder.getOrderId());
		}else{
			// 拆分后金额
			List<BigDecimal> splitAmts = frontBatchUtil.splitPayment(needSplitAmt, split);
			for(BigDecimal splitAmt : splitAmts){
				CcsOrder splitOrder = frontBatchUtil.initOrder(acct, cust, loan, LoanUsage.O, splitAmt, null);
				splitOrder.setOriOrderId(origOrder.getOrderId());
			}
		}
		return null;
	}

}
