package com.sunline.ccs.param.def.enums;

import com.sunline.ark.support.meta.EnumInfo;

/**
 * 首次用卡标志
* @author fanghj
 *
 */
@EnumInfo({
	"A|未用卡",
	"B|已首次用卡",
	"C|已首次用卡且已收年费"
})
public enum FirstUsageIndicator {

	/**
	 *	未用卡 
	 */
	A,
	/**
	 *	已首次用卡 
	 */
	B,
	/**
	 * 	已首次用卡且已收年费
	 */
	C
}
