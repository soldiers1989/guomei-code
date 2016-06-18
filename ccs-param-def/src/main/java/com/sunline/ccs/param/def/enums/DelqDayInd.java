package com.sunline.ccs.param.def.enums;

import com.sunline.ark.support.meta.EnumInfo;

/**
 * 账龄增加日类型
 */
@EnumInfo({
	"P|到期还款日",
	"G|宽限日",
	"C|账单日"
})
public enum DelqDayInd{
	/**
	 * 到期还款日
	 */
	P,
	/**
	 * 宽限日
	 */
	G,
	/**
	 * 账单日
	 */
	C}
