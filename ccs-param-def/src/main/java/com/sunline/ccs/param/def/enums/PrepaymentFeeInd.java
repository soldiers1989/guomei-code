package com.sunline.ccs.param.def.enums;

import com.sunline.ark.support.meta.EnumInfo;

/**
 * 提前还款退手续费标志
 * @version 1.0
 */
@EnumInfo({
	"N|提前还款不退手续费",
	"B|提前还款退还全部手续费",
	"P|提前还款按已出账单期数的比例退手续费"
})
public enum PrepaymentFeeInd {

	/**
	 * 提前还款不退手续费
	 */
	N,
	/**
	 * 提前还款退还全部手续费
	 */
	B,
	/**
	 * 提前还款按已出账单期数的比例退手续费
	 */
	P
}
