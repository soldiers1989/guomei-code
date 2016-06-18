package com.sunline.ccs.batch.cc6000.interestaccrue;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.param.def.PlanTemplate;
import com.sunline.ppy.dictionary.enums.BucketType;
import com.sunline.ppy.dictionary.enums.Indicator;


/**
 * @see 类名：StmtAccrue
 * @see 描述： 账单日起息, 只需要对往期余额进行计息
 *
 * @see 创建日期：   2015-6-24下午6:56:48
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Component
public class StmtAccrue extends Accrue {
	@Override
	public void accure(CcsPlan plan, BucketType type, PlanTemplate template,
			int days, Indicator paidOut, Boolean isOverduePrin, Boolean needGraceIntAcruBackstage) {
		Boolean b = template.intParameterBuckets.get(type).intWaive;
		b = (b==null?false:b);
		if (b) {
			// 尚未全额还款,进行往期累积延时利息累积。已全额还款则不进行利息累积
			if(type == BucketType.Pricinpal){
				if(plan.getUsePlanRate() == Indicator.Y){
					BigDecimal nodefbnpIntIncre = computeInterest(plan.getInterestRate(), getPastBalance(plan, type), days);
					if( needGraceIntAcruBackstage ){
						plan.setGraceNodefbnpIntAcru(plan.getGraceNodefbnpIntAcru().add(nodefbnpIntIncre));
					}else{
						plan.setNodefbnpIntAcru(plan.getNodefbnpIntAcru().add(nodefbnpIntIncre));
					}
					//本金产生的罚息,都计入罚息累计
					//使用协议罚息利率,如果为null,取罚息利率表
					if(plan.getPenaltyRate() == null && template.intTableId!=null){
						if(isOverduePrin){
							plan.setPenaltyAcru(plan.getPenaltyAcru().add(computeInterest(getInterestTable(template.intTableId), days, getPastBalance(plan, type))));
						}
					}else{
						if(isOverduePrin){
							plan.setPenaltyAcru(plan.getPenaltyAcru().add(computeInterest(plan.getPenaltyRate(), getPastBalance(plan, type), days)));
						}
					}
				}else{
					if (paidOut == Indicator.N) {
						plan.setBegDefbnpIntAcru(plan.getBegDefbnpIntAcru().add(computeInterest(getInterestTable(type, template), days, getPastBalance(plan, type))));		
						if(template.intTableId!=null){
							if(isOverduePrin){
								plan.setPenaltyAcru(plan.getPenaltyAcru().add(computeInterest(getInterestTable(template.intTableId), days, getPastBalance(plan, type))));
							}
						}
					}
				}
				
			}else if(isInterest(type)){
				BigDecimal bal = getPastBalance(plan, type);
				//余额成分为利息类型.产生的利息都计入复利累计中
				plan = this.acruCompound(plan, type,bal, template, days);
			}else{
				//其他余额成分
				if (paidOut == Indicator.N) {
					plan.setBegDefbnpIntAcru(plan.getBegDefbnpIntAcru().add(computeInterest(getInterestTable(type, template), days, getPastBalance(plan, type))));									
				}
			}
		} else {
			// 不可免利息累积后,记入非延迟利息
			if(type == BucketType.Pricinpal ){
				BigDecimal bal = getPastBalance(plan, type);
				if(plan.getUsePlanRate() == Indicator.Y){
					//本金产生的利息,都计入非延时利息
					BigDecimal nodefbnpIntIncre = computeInterest(plan.getInterestRate(), bal, days);
					if( needGraceIntAcruBackstage ){
						plan.setGraceNodefbnpIntAcru(plan.getGraceNodefbnpIntAcru().add(nodefbnpIntIncre));
					}else{
						plan.setNodefbnpIntAcru(plan.getNodefbnpIntAcru().add(nodefbnpIntIncre));
					}
				}else{
					BigDecimal nodefbnpIntIncre = computeInterest(getInterestTable(type, template), days, bal);
					if( needGraceIntAcruBackstage ){
						plan.setGraceNodefbnpIntAcru(plan.getGraceNodefbnpIntAcru().add(nodefbnpIntIncre));
					}else{
						plan.setNodefbnpIntAcru(plan.getNodefbnpIntAcru().add(nodefbnpIntIncre));
					}
				}
				//本金产生的罚息,都计入罚息累计
				if(isOverduePrin){
					plan.setPenaltyAcru(plan.getPenaltyAcru().add(computeInterest(plan.getPenaltyRate(), bal, days)));
				}
			}else if(isInterest(type) ){
				BigDecimal bal = getPastBalance(plan, type);
				plan = this.acruCompound(plan, type,bal, template, days);
			}else{
				//其他余额成分都计入非延时利息
				BigDecimal nodefbnpIntIncre = computeInterest(getInterestTable(type, template), days, getPastBalance(plan, type));
				if( needGraceIntAcruBackstage ){
					plan.setGraceNodefbnpIntAcru(plan.getGraceNodefbnpIntAcru().add(nodefbnpIntIncre));
				}else{
					plan.setNodefbnpIntAcru(plan.getNodefbnpIntAcru().add(nodefbnpIntIncre));
				}
			}
		}
	}
}
