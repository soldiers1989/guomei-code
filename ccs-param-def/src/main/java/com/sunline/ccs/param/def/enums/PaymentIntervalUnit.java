package com.sunline.ccs.param.def.enums;

import com.sunline.ark.support.meta.EnumInfo;

/**
* @author fanghj
 * 还款间隔单位
 */
@EnumInfo({
	"D|日",
	"W|周",
	"H|半月",
	"M|月",
	"Q|季",
	"Y|年"
})
public enum PaymentIntervalUnit {
	/**
	 * 日
	 */
	D,
	/**
	 * 周
	 */
	W,
	/**
	 * 半月
	 */
	H,
	/**
	 * 月
	 */
	M,
	/**
	 * 季
	 */
	Q,
	/**
	 * 年
	 */
	Y
}
