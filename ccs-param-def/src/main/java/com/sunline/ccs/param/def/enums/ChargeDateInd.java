package com.sunline.ccs.param.def.enums;

import com.sunline.ark.support.meta.EnumInfo;

/**
 * 超限费收取日期：
 * P - 超限当天收取（posting date）
 * C - 账单日收取（cycle date）
 */
@EnumInfo({
	"P|超限当天收取（posting date）",
	"C|账单日收取（cycle date）"
})
public enum ChargeDateInd{
	/**
	 * 超限当天收取
	 */
	P,
	/**
	 * 账单日收取
	 */
	C
}
