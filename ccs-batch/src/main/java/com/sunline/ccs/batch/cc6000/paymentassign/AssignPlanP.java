package com.sunline.ccs.batch.cc6000.paymentassign;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.sunline.ccs.batch.cc6000.S6000AcctInfo;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;


/**
 * @see 类名：AssignPlanP
 * @see 描述：等额本息的转出计划，不接受还款，不结息,不免息
 *
 * @see 创建日期：   2015-6-24下午6:53:57
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Component
public class AssignPlanP extends Assign {

	@Override
	protected boolean needWaiveInt(CcsPlan plan, S6000AcctInfo item, BigDecimal bal) {
		return false;
	}

	@Override
	protected boolean needSettleInt(CcsPlan plan, S6000AcctInfo item, BigDecimal bal) {
		return false;
	}

	@Override
	protected boolean acceptPay() {
		return false;
	}


}
