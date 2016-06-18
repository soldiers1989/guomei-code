package com.sunline.ccs.batch.cc6000.interestaccrue;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.param.def.PlanTemplate;
import com.sunline.ppy.dictionary.enums.BucketType;
import com.sunline.ppy.dictionary.enums.Indicator;


/**
 * @see 类名：PostDateAccrue
 * @see 描述：入账日起息
 *
 * @see 创建日期：   2015-6-24下午6:56:30
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Component
public class PostDateAccrue extends Accrue {
//	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Override
	public void accure(CcsPlan plan, BucketType type, PlanTemplate template, 
			int days, Indicator paidOut, Boolean isOverduePrin, Boolean needGraceIntAcruBackstage) {
		
		// 判断该余额成份是否享受免息期
		Boolean b = template.intParameterBuckets.get(type).intWaive;
		b =( b==null?false:b);
		if (b) {
			// 尚未全额还款,往期余额进行利息累积。已全额还款往期余额则不进行利息累积。
			// 往期余额产生的利息记入往期累积延时利息
			// 当期余额进行利息累积，产生的利息记入当期累积延时利息
			if(type == BucketType.Pricinpal){
				if(plan.getUsePlanRate() == Indicator.Y){
					//使用plan利率，不在区分是否全额还款，全计入非延时利息
					BigDecimal nodefbnpIntIncre = computeInterest(plan.getInterestRate(), getPastBalance(plan, type).add(getCtdBalance(plan, type)), days);
					if( needGraceIntAcruBackstage ){
//						logger.debug("累计宽限日利息{}", nodefbnpIntIncre);
						plan.setGraceNodefbnpIntAcru(plan.getGraceNodefbnpIntAcru().add(nodefbnpIntIncre));
					}else{
//						logger.debug("累计非延迟利息{}", nodefbnpIntIncre);
						plan.setNodefbnpIntAcru(plan.getNodefbnpIntAcru().add(nodefbnpIntIncre));
					}
					//使用协议罚息利率,如果为null,取罚息利率表
					if(plan.getPenaltyRate() == null && template.intTableId !=null&&isOverduePrin){
						plan.setPenaltyAcru(plan.getPenaltyAcru().add(computeInterest(getInterestTable(template.intTableId), days, getPastBalance(plan, type).add(getCtdBalance(plan, type)))));
					}else{
						if(isOverduePrin){
							plan.setPenaltyAcru(plan.getPenaltyAcru().add(computeInterest(plan.getPenaltyRate(), getPastBalance(plan, type).add(getCtdBalance(plan, type)), days)));
						}
					}
					//代收罚息日累
					if(plan.getPenaltyRate() == null && template.repPenaltyIntId !=null){
						plan.setReplacePenaltyAcru(plan.getReplacePenaltyAcru().add(computeInterest(getInterestTable(template.repPenaltyIntId), days, getPastBalance(plan, type).add(getCtdBalance(plan, type)))));
					}else{
						plan.setReplacePenaltyAcru(plan.getReplacePenaltyAcru().add(computeInterest(plan.getReplacePenaltyRate(), getPastBalance(plan, type).add(getCtdBalance(plan, type)), days)));
					}
				}else{
					if (paidOut == Indicator.N) {
						plan.setBegDefbnpIntAcru(plan.getBegDefbnpIntAcru().add(computeInterest(getInterestTable(type, template), days, getPastBalance(plan, type))));									
					}
					plan.setCtdDefbnpIntAcru(plan.getCtdDefbnpIntAcru().add(computeInterest(getInterestTable(type, template), days, getCtdBalance(plan, type))));
					//使用利率表
					if(template.intTableId!=null&&isOverduePrin){
						plan.setPenaltyAcru(plan.getPenaltyAcru().add(computeInterest(getInterestTable(template.intTableId), days, getPastBalance(plan, type).add(getCtdBalance(plan, type)))));
					}
					if(template.repPenaltyIntId!=null){
						plan.setReplacePenaltyAcru(plan.getReplacePenaltyAcru().add(computeInterest(getInterestTable(template.repPenaltyIntId), days, getPastBalance(plan, type).add(getCtdBalance(plan, type)))));
					}
				}
			
			}else if(isInterest(type)){
				BigDecimal bal = getPastBalance(plan, type).add(getCtdBalance(plan, type));
				//余额成分为利息类型.产生的利息都计入复利累计中
				plan = this.acruCompound(plan, type,bal, template, days);
			}else{
				//其他余额成分
				if (paidOut == Indicator.N) {
					plan.setBegDefbnpIntAcru(plan.getBegDefbnpIntAcru().add(computeInterest(getInterestTable(type, template), days, getPastBalance(plan, type))));									
				}
				plan.setCtdDefbnpIntAcru(plan.getCtdDefbnpIntAcru().add(computeInterest(getInterestTable(type, template), days, getCtdBalance(plan, type))));
			}
		} else {
			// 不可免利息累积后,记入非延迟利息
			if(type == BucketType.Pricinpal ){
				BigDecimal bal = getPastBalance(plan, type).add(getCtdBalance(plan, type));
				if(plan.getUsePlanRate() == Indicator.Y){
					//本金产生的利息,都计入非延时利息
					BigDecimal nodefbnpIntIncre = computeInterest(plan.getInterestRate(), bal, days);
					if( needGraceIntAcruBackstage ){
						plan.setGraceNodefbnpIntAcru(plan.getGraceNodefbnpIntAcru().add(nodefbnpIntIncre));
					}else{
						plan.setNodefbnpIntAcru(plan.getNodefbnpIntAcru().add(nodefbnpIntIncre));
					}
					//使用协议罚息利率,如果为null,取罚息利率表
					if(plan.getPenaltyRate() == null && template.intTableId!=null&&isOverduePrin){
						plan.setPenaltyAcru(plan.getPenaltyAcru().add(computeInterest(getInterestTable(template.intTableId), days, getPastBalance(plan, type).add(getCtdBalance(plan, type)))));
					}else{
						if(isOverduePrin){
							plan.setPenaltyAcru(plan.getPenaltyAcru().add(computeInterest(plan.getPenaltyRate(), getPastBalance(plan, type).add(getCtdBalance(plan, type)), days)));
						}
					}
					//代收罚息日累
					if(plan.getReplacePenaltyRate() == null && template.repPenaltyIntId!=null){
						plan.setReplacePenaltyAcru(plan.getReplacePenaltyAcru().add(computeInterest(getInterestTable(template.repPenaltyIntId), days, getPastBalance(plan, type).add(getCtdBalance(plan, type)))));
					}else{
						plan.setReplacePenaltyAcru(plan.getReplacePenaltyAcru().add(computeInterest(plan.getReplacePenaltyRate(), getPastBalance(plan, type).add(getCtdBalance(plan, type)), days)));
					}
				}else{
					BigDecimal nodefbnpIntIncre = computeInterest(getInterestTable(type, template), days, bal);
					if( needGraceIntAcruBackstage ){
						plan.setGraceNodefbnpIntAcru(plan.getGraceNodefbnpIntAcru().add(nodefbnpIntIncre));
					}else{
						plan.setNodefbnpIntAcru(plan.getNodefbnpIntAcru().add(nodefbnpIntIncre));
					}
					if(isOverduePrin){
						plan.setPenaltyAcru(plan.getPenaltyAcru().add(computeInterest(getInterestTable(template.intTableId), days, getPastBalance(plan, type).add(getCtdBalance(plan, type)))));
					}
					//代收罚息日累
					plan.setReplacePenaltyAcru(plan.getReplacePenaltyAcru().add(computeInterest(getInterestTable(template.repPenaltyIntId), days, getPastBalance(plan, type).add(getCtdBalance(plan, type)))));
				}
			}else if(isInterest(type) ){
				BigDecimal bal = getPastBalance(plan, type).add(getCtdBalance(plan, type));
				plan = this.acruCompound(plan, type,bal, template, days);
			}else{
				//其他余额成分都计入非延时利息
				BigDecimal nodefbnpIntIncre = computeInterest(getInterestTable(type, template), days, getPastBalance(plan, type).add(getCtdBalance(plan, type)));
				if( needGraceIntAcruBackstage ){
					plan.setGraceNodefbnpIntAcru(plan.getGraceNodefbnpIntAcru().add(nodefbnpIntIncre));
				}else{
					plan.setNodefbnpIntAcru(plan.getNodefbnpIntAcru().add(nodefbnpIntIncre));
				}
			}
		}
	}

}
