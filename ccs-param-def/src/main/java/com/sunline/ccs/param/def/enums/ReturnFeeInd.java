package com.sunline.ccs.param.def.enums;

import com.sunline.ark.support.meta.EnumInfo;

/**
 * 调整状态
* @author fanghj
 * @date 2013-6-21  上午11:20:42
 * @version 1.0
 */
@EnumInfo({
	"N|退货不退手续费",
	"B|退货退还全部手续费",
	"P|退货按已出账单期数的比例退手续费"
})
public enum ReturnFeeInd {

	/**
	 * 退货不退手续费
	 */
	N,
	/**
	 * 退货退还全部手续费
	 */
	B,
	/**
	 * 退货按已出账单期数的比例退手续费
	 */
	P
}
