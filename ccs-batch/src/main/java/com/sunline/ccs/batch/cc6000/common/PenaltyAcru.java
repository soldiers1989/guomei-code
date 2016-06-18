package com.sunline.ccs.batch.cc6000.common;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sunline.ccs.batch.cc6000.S6000AcctInfo;
import com.sunline.ccs.facility.BlockCodeUtils;
import com.sunline.ccs.facility.MicroCreditRescheduleUtils;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.param.def.InterestTable;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.param.def.PlanTemplate;
import com.sunline.ccs.param.def.enums.BnpPeriod;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.BucketType;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.PenaltyAccuBase;
import com.sunline.ppy.dictionary.enums.PlanType;
/**
 * 罚息特殊计算规则组件
 * @author Lisy
 *
 */
@Component
public class PenaltyAcru{
	@Autowired
	private UnifiedParamFacilityProvide unifiedParameterFacilityProvide;
	@Autowired
	private BlockCodeUtils blockCodeUtils;
	@Autowired
	private UnifiedParameterFacility parameterFacility;
	@Autowired
	private BnpManager bnpManager;
	@PersistenceContext
	private EntityManager em;
	@Autowired
	private Calculator calculator;
	
 	public void processUnpaidPrin(S6000AcctInfo item,Integer days) throws Exception {
		for(CcsLoan loan:item.getLoans()){
			//是否发生逾期
//			if(loan.getOverdueDate()==null&&loan.getCpdBeginDate()==null) return;
			// 判断锁定码定义是否需要进行计息
			if (!blockCodeUtils.getMergedIntAccuralInd(item.getAccount().getBlockCode())) return;
			
			//获取贷款参数
			LoanPlan loanPlan = unifiedParameterFacilityProvide.loanPlan(loan.getLoanCode());
		
			// 处理按全部未还本金计罚息的贷款
			if(PenaltyAccuBase.U==loanPlan.penaltyAccuBase){
				// 获取最新的一期转入计划
				int currTerm=0;
				for(CcsPlan plan:item.getPlans()){
					if(plan.getTerm()!=null&&plan.getPlanType().isXfrIn()){
						if(plan.getTerm()>currTerm){
							currTerm=plan.getTerm();
						}
					}
				}
				for(CcsPlan plan:item.getPlans()){
					// 是否转入转出计划上本金部分的罚息
					Boolean isAccuXfroutBal = false;
					PlanTemplate planTemplate = parameterFacility.loadParameter(plan.getPlanNbr(), PlanTemplate.class);
					// 获取最新一期转入计划-先生成转入计划后期数+1
					if(plan.getPlanType().isXfrIn()){
						// 随借随还直接取转入计划
						if(plan.getPlanType()==PlanType.L){
							isAccuXfroutBal = true;
						}
						// 等额本息取最新一期
						else if(plan.getPlanType()==PlanType.Q&&plan.getTerm().compareTo(currTerm)==0){
							isAccuXfroutBal = true;
						}
					}
					//判断本金余额成分是否指定了利息表，未指定利息表，则不计息
					if(planTemplate.intParameterBuckets.get(BucketType.Pricinpal) == null) continue;
					if (planTemplate.intParameterBuckets.get(BucketType.Pricinpal).intTableId == null) continue;
				
					// 该余额成份往期余额
					BigDecimal pastBalance = bnpManager.getBucketAmount(plan, BucketType.Pricinpal, BnpPeriod.PAST);

					// 该余额成份当期余额
					BigDecimal ctdBalance = bnpManager.getBucketAmount(plan, BucketType.Pricinpal, BnpPeriod.CTD);
					
					// 判断余额成份当前值如果为0，则不进行利息处理
					// 且不为罚息转入的计划时
					if (pastBalance.add(ctdBalance).compareTo(BigDecimal.ZERO) <=0/* && !isAccuXfroutBal*/) continue;
					
					// 判断本金是否享受免息期
					Boolean b = planTemplate.intParameterBuckets.get(BucketType.Pricinpal).intWaive;
					b =( b==null?false:b);
					if (b) {
						// 尚未全额还款,往期余额进行利息累积。已全额还款往期余额则不进行利息累积。
						// 往期余额产生的利息记入往期累积延时利息
						// 当期余额进行利息累积，产生的利息记入当期累积延时利息
						if(plan.getUsePlanRate() == Indicator.Y){
							//使用协议罚息利率,如果为null,取罚息利率表
							if(plan.getPenaltyRate() == null && planTemplate.intTableId !=null){
								plan.setPenaltyAcru(plan.getPenaltyAcru().add(computeInterest(getInterestTable(planTemplate.intTableId), days, pastBalance.add(ctdBalance).add(isAccuXfroutBal?getAccuXfroutBal(item.getPlans()):BigDecimal.ZERO))));
							}else{
								plan.setPenaltyAcru(plan.getPenaltyAcru().add(computeInterest(plan.getPenaltyRate(), pastBalance.add(ctdBalance).add(isAccuXfroutBal?getAccuXfroutBal(item.getPlans()):BigDecimal.ZERO), days)));
							}
								
						}else{
							//使用利率表
							if(planTemplate.intTableId!=null){
								plan.setPenaltyAcru(plan.getPenaltyAcru().add(computeInterest(getInterestTable(planTemplate.intTableId), days, pastBalance.add(ctdBalance).add(isAccuXfroutBal?getAccuXfroutBal(item.getPlans()):BigDecimal.ZERO))));
							}
						}
					} else {
						// 不可免利息累积后,记入非延迟利息
						if(plan.getUsePlanRate() == Indicator.Y){
							//使用协议罚息利率,如果为null,取罚息利率表
							if(plan.getPenaltyRate() == null && planTemplate.intTableId!=null ){
								plan.setPenaltyAcru(plan.getPenaltyAcru().add(computeInterest(getInterestTable(planTemplate.intTableId), days, pastBalance.add(ctdBalance).add(isAccuXfroutBal?getAccuXfroutBal(item.getPlans()):BigDecimal.ZERO))));
							}else{
								plan.setPenaltyAcru(plan.getPenaltyAcru().add(computeInterest(plan.getPenaltyRate(), pastBalance.add(ctdBalance).add(isAccuXfroutBal?getAccuXfroutBal(item.getPlans()):BigDecimal.ZERO), days)));
							}
						}else{
							plan.setPenaltyAcru(plan.getPenaltyAcru().add(computeInterest(getInterestTable(planTemplate.intTableId), days, pastBalance.add(ctdBalance).add(isAccuXfroutBal?getAccuXfroutBal(item.getPlans()):BigDecimal.ZERO))));
						}
					}
				}
			}
		}
	}
	
	private BigDecimal getAccuXfroutBal(List<CcsPlan> planList){
		CcsPlan xfroutPlan = null;
		for(CcsPlan plan:planList){
			if(plan.getPlanType().isXfrOut()){
				xfroutPlan = plan;
			}
		}
		if(xfroutPlan==null){
			// 考虑到计划转出失效天数，不抛出异常
//			throw new Exception("找不到对应的转出计划，账户号["+planList.get(0).getAcctNbr()+"]");
			// 无对应转出计划，则本金已经全额转出到转入计划上
			return BigDecimal.ZERO;
		}
		else return xfroutPlan.getPastPrincipal();
	}

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
	private BigDecimal computeInterest(BigDecimal rate,BigDecimal bal,int days){
		BigDecimal bd = new BigDecimal(1.0/MicroCreditRescheduleUtils.YEAR_DAYS).setScale(20,RoundingMode.HALF_UP);
		bd = bd.multiply(bal).multiply(rate).multiply(BigDecimal.valueOf(days));
		return bd.setScale(6, RoundingMode.HALF_UP);
	}

}

