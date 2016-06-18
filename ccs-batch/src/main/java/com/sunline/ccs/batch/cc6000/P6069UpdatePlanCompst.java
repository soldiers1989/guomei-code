package com.sunline.ccs.batch.cc6000;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.batch.common.DateUtils;
import com.sunline.ccs.batch.common.SettleRecordUtil;
import com.sunline.ccs.facility.MicroCreditRescheduleUtils;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.enums.CompensateStatus;
import com.sunline.ccs.param.def.enums.SettleTxnDirection;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.exception.ProcessException;

public class P6069UpdatePlanCompst implements ItemProcessor<S6000AcctInfo, S6000AcctInfo> {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private SettleRecordUtil settleUtil;
	@Autowired
	private UnifiedParameterFacility parameterFacility;
	@Autowired
	private BatchStatusFacility batchFacility;
	@Autowired
	private MicroCreditRescheduleUtils rescheduleUtils;
	
	@Override
	public S6000AcctInfo process(S6000AcctInfo item) throws Exception {
		logger.debug("处理代偿Acct--{}{}", item.getAccount().getAcctNbr(), item.getAccount().getAcctType() );
		
		OrganizationContextHolder.setCurrentOrg(item.getAccount().getOrg());
		ProductCredit pc = parameterFacility.loadParameter(item.getAccount().getProductCd(), ProductCredit.class);
		Integer overdueCompstDays = pc.overdueToCompstDays;
		
		logger.debug("产品{}是否代偿标志{}", pc.productCd, pc.needCompensate);
		if(pc.needCompensate == null || pc.needCompensate == Indicator.N){
			return item;
		}
		if(overdueCompstDays == null)
			throw new ProcessException("产品["+pc.productCd+"]设置为需要代偿，但代偿逾期天数没有设置" );
		
		for (CcsPlan plan : item.getPlans()) {
			// 跳过非转入计划
			if( !plan.getPlanType().isXfrIn() ) continue;
			// 跳过计划建立日期在账户建立日期之前的计划
			if( plan.getPlanAddDate().after(batchFacility.getBatchDate())) continue; 
			
			// 获取Plan所属Loan
			CcsLoan loan = null;
			for(CcsLoan l : item.getLoans()){
				if(plan.getRefNbr().equals(l.getRefNbr())){
					loan = l;
				}
			}
			// 跳过Plan期数大于Loan当前期数的计划
			if( plan.getTerm() > loan.getCurrTerm()) continue;
			// 执行代偿逻辑
			handleCompstStatus(plan, item.getAccount(), loan, overdueCompstDays);
			
		}
		
		return item;
	}

	/**
	 * 根据还款状态、逾期天数和代偿状态生成结算记录
	 * @param plan
	 * @param account
	 * @param overdueCompstDays 
	 * @param loans
	 */
	private void handleCompstStatus(CcsPlan plan, CcsAcct account, CcsLoan loan, Integer overdueCompstDays) {
		
		if( plan.getPaidOutDate() == null){
			// 检查代偿条件 ： 状态为未代偿，且改期逾期天数>=逾期代偿天数
			if(loan.getOverdueDate() != null && CompensateStatus.NotCompensated.equals(plan.getCompensateStatus())){
				
				Date pmtDueDate = rescheduleUtils.getNextPaymentDay(account.getProductCd(), plan.getPlanAddDate());
				// 
				if( DateUtils.truncatedCompareTo(loan.getOverdueDate(), pmtDueDate, Calendar.DAY_OF_MONTH) <= 0 &&
						DateUtils.getIntervalDays(pmtDueDate, batchFacility.getBatchDate()) >= overdueCompstDays ){
					
					BigDecimal compensateAmt = calcCompstAmt(plan);
					if (compensateAmt.compareTo(BigDecimal.ZERO) > 0 ){
						
						settleUtil.saveCompstSettleRecord(account, loan, plan,
								compensateAmt, SettleTxnDirection.FromCoop);
						
						plan.setCompensateAmt(compensateAmt);
						plan.setCompensateStatus(CompensateStatus.CompstRecorded);
						loan.setCompensateAmtSum(loan.getCompensateAmtSum().add(compensateAmt));
						loan.setCompensateCount(loan.getCompensateCount() + 1 );
					}
				}
			}
			
		}else{ // 已还清
			// 检查代偿退还条件 ：该期还清，状态为已代偿
			if(CompensateStatus.CompstRecorded.equals(plan.getCompensateStatus())){
				
				settleUtil.saveCompstSettleRecord(account, loan, plan, plan.getCompensateAmt(), SettleTxnDirection.ToCoop);
				
				plan.setCompensateStatus(CompensateStatus.CompstRefundRecorded);
				loan.setCompensateRefundAmtSum(loan.getCompensateRefundAmtSum().add(plan.getCompensateAmt()));
				loan.setCompensateRefundCount(loan.getCompensateRefundCount() + 1 );
			
			}
		}
	}

	/**
	 * 获取Plan代偿金额
	 * @param plan
	 * @return
	 */
	private BigDecimal calcCompstAmt(CcsPlan plan) {
		//去掉逾期息费及合作方代收息费
		BigDecimal penaltyAmt = plan.getCtdMulctAmt().add(plan.getPastMulctAmt())
				.add(plan.getCtdCompound()).add(plan.getPastCompound())
				.add(plan.getCtdPenalty().add(plan.getPastPenalty()))
				.add(plan.getCtdLateFee().add(plan.getPastLateFee()))
				.add(plan.getCtdReplaceMulct().add(plan.getPastReplaceMulct())
				.add(plan.getCtdReplacePenalty().add(plan.getPastReplacePenalty()))
				.add(plan.getCtdReplaceLateFee().add(plan.getPastReplaceLateFee())));
		
		BigDecimal replaceFeeAmt = plan.getCtdReplaceSvcFee().add(plan.getPastReplaceSvcFee())
				.add(plan.getCtdReplaceTxnFee().add(plan.getPastReplaceTxnFee()))
				.add(plan.getCtdInsurance()).add(plan.getPastInsurance());
		
		BigDecimal compstAmt = plan.getCurrBal().subtract(replaceFeeAmt).subtract(penaltyAmt);
		return compstAmt;
	}
}




