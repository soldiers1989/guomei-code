package com.sunline.ccs.batch.cc6000.interestaccrue;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.beans.factory.annotation.Autowired;

import com.sunline.ccs.batch.cc6000.common.BnpManager;
import com.sunline.ccs.batch.cc6000.common.Calculator;
import com.sunline.ccs.facility.MicroCreditRescheduleUtils;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.param.def.InterestTable;
import com.sunline.ccs.param.def.PlanTemplate;
import com.sunline.ccs.param.def.enums.BnpPeriod;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.BucketType;
import com.sunline.ppy.dictionary.enums.Indicator;


/**
 * @see 类名：Accrue
 * @see 描述：利息累计
 *
 * @see 创建日期：   2015-6-24下午6:54:47
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public abstract class Accrue {
	@Autowired
	private Calculator calculator;
	@Autowired
	private BnpManager bnpManager;
	@Autowired
	private UnifiedParameterFacility parameterFacility;
	
	/**
	 * @see 方法名：computeInterest 
	 * @see 描述：计算利息
	 * @see 创建日期：2015-10-16下午6:55:05
	 * @author liuqi
	 *  
	 * @param interestTbl
	 * @param days
	 * @param computeAmount
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	protected BigDecimal computeInterest(InterestTable interestTbl, Integer days, BigDecimal computeAmount) {
		if(interestTbl == null){
			return BigDecimal.ZERO;
		}
		BigDecimal bd = new BigDecimal(1.0/interestTbl.baseYear).setScale(20,RoundingMode.HALF_UP);
		BigDecimal interest = calculator.getFeeAmount(interestTbl.tierInd, interestTbl.chargeRates, computeAmount);
		bd = bd.multiply(interest).multiply(BigDecimal.valueOf(days));
		return bd.setScale(6, RoundingMode.HALF_UP);
	}
	
	public InterestTable getInterestTable(BucketType type, PlanTemplate template) {
		return parameterFacility.loadParameter(template.intParameterBuckets.get(type).intTableId,InterestTable.class);
	}
	public InterestTable getInterestTable(int intTableId) {
		return parameterFacility.loadParameter(intTableId,InterestTable.class);
	}
	public BigDecimal getPastBalance(CcsPlan plan, BucketType type) {
		return bnpManager.getBucketAmount(plan, type, BnpPeriod.PAST);
	}
	public BigDecimal getCtdBalance(CcsPlan plan, BucketType type) {
		return bnpManager.getBucketAmount(plan, type, BnpPeriod.CTD);
	}
	
	/**
	 * 计算贷款利率
	 * @param rate
	 * @param bal
	 * @param days
	 * @return
	 */
	public BigDecimal computeInterest(BigDecimal rate,BigDecimal bal,int days){
		BigDecimal bd = new BigDecimal(1.0/MicroCreditRescheduleUtils.YEAR_DAYS).setScale(20,RoundingMode.HALF_UP);
		bd = bd.multiply(bal).multiply(rate).multiply(BigDecimal.valueOf(days));
		return bd.setScale(6, RoundingMode.HALF_UP);
	}
	/**
	 * //余额成分为利息类型，并且使用plan的利率（协议利率），产生的利息都计入复利累计中
	 * @param plan
	 * @param type
	 * @param template
	 * @param days
	 * @return
	 */
	public CcsPlan acruCompound(CcsPlan plan,BucketType type,BigDecimal bal, PlanTemplate template, int days){
		if(plan.getUsePlanRate() == Indicator.Y){
			plan.setCompoundAcru(plan.getCompoundAcru().add(computeInterest(plan.getCompoundRate(),bal,days)));
		}else{
			plan.setCompoundAcru(plan.getCompoundAcru().add(computeInterest(getInterestTable(type, template), days, bal)));
		}
		return plan;
	}
	
	/**
	 * 是否利息余额成分
	 * @param b
	 * @return
	 */
	public boolean isInterest(BucketType b){
		return b == BucketType.Interest || b == BucketType.Penalty || b == BucketType.Compound;
	}
	//是否按照逾期本金计算罚息
	public abstract void accure(CcsPlan plan, BucketType type, PlanTemplate template, 
			int days, Indicator paidOut, Boolean isOverduePrin, Boolean needIntAcruBackstage);
}
