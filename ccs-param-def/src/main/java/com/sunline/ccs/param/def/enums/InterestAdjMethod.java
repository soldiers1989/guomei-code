package com.sunline.ccs.param.def.enums;

import com.sunline.ark.support.meta.EnumInfo;

/**
* @author fanghj
 *	利率调整方式
 */
@EnumInfo({
	"F|固定利率",
	"L|浮动利率",
	"M|混合利率"
})
public enum InterestAdjMethod {
	/**
	 *固定利率 
	 */
	F,
	/**
	 * 浮动利率
	 */
	L,
	/**
	 * 混合利率
	 */
	M
}
