package com.sunline.ccs.batch.cc6000.paymentassign;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

import com.sunline.ccs.batch.cc6000.S6000AcctInfo;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;

/**
 * 
 * @see 类名：AssignPlanQ
 * @see 描述：等额本息转入计划，接受还款
 *            批量日期<账户宽限日，且还款金额>plan当前余额+非延时利息+非延时罚息+非延时复利，则执行结息，否则不结息
 *
 * @see 创建日期：   2015-6-24下午6:54:19
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Component
public class AssignPlanQ extends Assign {

	@Override
	protected boolean needWaiveInt(CcsPlan plan, S6000AcctInfo item,BigDecimal bal) {
		if(!isExpired(plan, item)){
			if(bal.compareTo(plan.getCurrBal())>=0){
				return true;
			}
		}
		return false;
	}

	@Override
	protected boolean needSettleInt(CcsPlan plan, S6000AcctInfo item,BigDecimal bal) {
		if(isExpired(plan, item)){
			BigDecimal sumBal = plan.getCurrBal()
					.add(plan.getNodefbnpIntAcru().setScale(2, RoundingMode.HALF_UP))
					.add(plan.getCompoundAcru().setScale(2, RoundingMode.HALF_UP))
					.add(plan.getPenaltyAcru().setScale(2, RoundingMode.HALF_UP))
					.add(plan.getReplacePenaltyAcru().setScale(2,RoundingMode.HALF_UP));
			if(bal.compareTo(sumBal)>=0){
				return true;
			}
		}
		return false;
	}

	@Override
	protected boolean acceptPay() {
		return true;
	}


}
