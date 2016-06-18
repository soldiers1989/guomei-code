package com.sunline.ccs.param.def.enums;

import com.sunline.ark.support.meta.EnumInfo;

/**
 * 支付方式
 * @author zhengjf
 *
 */
@EnumInfo({
	"L|放款申请",
	"A|放款重提",
	"Z|正常代扣 ",
	"X|主动还款" ,
	"Y|催收实时扣款", 
	"W|拆分代扣(CPD代扣)"
})
public enum PaymentPurpose {
	/**
	 * 放款申请
	 */
	L,
	/**
	 * 放款重提
	 */
	A,
	/**
	 * 正常代扣
	 */
	Z,
	/**
	 * 主动还款
	 */
	X,
	/**
	 * 催收实时扣款
	 */
	Y,
	/**
	 * 拆分代扣(CPD代扣)
	 */
	W,
}
