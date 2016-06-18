package com.sunline.ccs.batch.cc6000.common;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sunline.ark.support.utils.DateUtils;
import com.sunline.ccs.batch.cc6000.S6000AcctInfo;
import com.sunline.ccs.batch.cc6000.interestaccrue.Accrue;
import com.sunline.ccs.batch.cc6000.interestaccrue.AccrueTypeFactory;
import com.sunline.ccs.facility.BlockCodeUtils;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.param.def.PlanTemplate;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.enums.BnpPeriod;
import com.sunline.ccs.param.def.enums.IntAccumFrom;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.BucketType;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.PenaltyAccuBase;


/** 
 * @see 类名：InterestAcru
 * @see 描述：利息处理业务组件
 *
 * @see 创建日期：   2015年6月25日 下午3:04:11
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Component
public class InterestAcru {
	private Logger logger = LoggerFactory.getLogger(getClass());

	public static final String INTEREST_TIER_ERROR = "利息表中的计息方式不正确";
	
	@Autowired
	private UnifiedParameterFacility parameterFacility;
	@Autowired
	private BnpManager bnpManager;
	@Autowired
	private BlockCodeUtils blockCodeUtils;
	@Autowired
	private AccrueTypeFactory factory;
	
	
	/**
	 * 利息累计
	 * 不考虑利息累积日期中，跨过账单日、宽限日的情况
	 * @param item 账户信息
	 * @param days 计息天数
	 * @param currentDate 当前日期
	 */
	public void accumulateInterest(S6000AcctInfo item, Integer days, Date currentDate)
	{
		//	判断锁定码定义是否需要进行计息
		if (!blockCodeUtils.getMergedIntAccuralInd(item.getAccount().getBlockCode())) return;

		Boolean needGraceIntAcruBackstage = judgeGraceIntAcruBackstage(item.getAccount(), currentDate);
		
		for (CcsPlan plan : item.getPlans())	{
			for(BucketType b : BucketType.values()){
				// 获取计息参数
				PlanTemplate planTemplate = parameterFacility.loadParameter(plan.getPlanNbr(), PlanTemplate.class);
				//判断该余额成份是否指定了利息表，未指定利息表，则不计息
				if(planTemplate.intParameterBuckets.get(b) == null) continue;
				if (planTemplate.intParameterBuckets.get(b).intTableId == null) continue;
//				该余额成份往期余额
				BigDecimal pastBalance = bnpManager.getBucketAmount(plan, b, BnpPeriod.PAST);

				//	该余额成份当期余额
				BigDecimal ctdBalance = bnpManager.getBucketAmount(plan, b, BnpPeriod.CTD);
				
				// 判断余额成份当前值如果为0，则不进行利息处理
				if (pastBalance.add(ctdBalance).compareTo(BigDecimal.ZERO) != 0){
					//获取计息开始日期类型（暂只支持入账日起息和账单日起息）
					IntAccumFrom f = planTemplate.intParameterBuckets.get(b).intAccumFrom;
					Accrue a = factory.getAccrue(f==null?IntAccumFrom.C:f);
					//获取罚息累计基数-若不按逾期部分本金计算，则不在当前步骤累计罚息
					Boolean isOverduePrin = true;
					if(item.getLoans().size()!=0){
						LoanPlan loanPlan = parameterFacility.loadParameter(item.getLoans().get(0).getLoanCode(), LoanPlan.class);
						if(loanPlan!=null){
							if(loanPlan.penaltyAccuBase==null||loanPlan.penaltyAccuBase==PenaltyAccuBase.O){
								isOverduePrin = true;
							}else{
								isOverduePrin = false;
							}
						}
					}
					
					a.accure(plan, b, planTemplate, days, item.getAccount().getGraceDaysFullInd(), isOverduePrin, 
							needGraceIntAcruBackstage && plan.getPlanType().isXfrIn());
				}
			}
			
//			logger.debug("计划{}当前累计利息：graceDayInt{}nodefbnpInt{}", plan.getPlanId(), plan.getGraceNodefbnpIntAcru(), plan.getNodefbnpIntAcru());
			if (plan.getPlanType().isXfrIn() && currentDate.compareTo(item.getAccount().getGraceDate()) == 0 ) {
				// 宽限日将Plan上累计的宽限日利息累加到非延迟利息，宽限日利息清零
				logger.debug("宽限日，宽限日累计利息恢复到非延迟利息planId-{}", plan.getPlanId());
				plan.setNodefbnpIntAcru(plan.getNodefbnpIntAcru().add(plan.getGraceNodefbnpIntAcru()));
				plan.setGraceNodefbnpIntAcru(BigDecimal.ZERO);
			}
		}
	}


	private Boolean judgeGraceIntAcruBackstage(CcsAcct account, Date currentDate) {
		
		ProductCredit pc = parameterFacility.loadParameter(account.getProductCd(), ProductCredit.class);
		logger.info("ProductCredit[{}].isGracedayIntWaive-[{}]", pc.productCd, pc.isGracedayIntWaive);
		if(pc.isGracedayIntWaive == Indicator.Y && 
				DateUtils.truncatedCompareTo(account.getPmtDueDate(), currentDate, Calendar.DAY_OF_MONTH) < 0 &&
				DateUtils.truncatedCompareTo(currentDate, account.getGraceDate(), Calendar.DAY_OF_MONTH) <= 0 ){
			logger.debug("累计宽限日利息");
			return true;
		}else{
			return false;
		}
		
	}

}
