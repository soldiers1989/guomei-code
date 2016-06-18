package com.sunline.ccs.param.def.enums;

import com.sunline.ark.support.meta.EnumInfo;

/**
 * 还款计算方式
* @author fanghj
 *
 */
@EnumInfo({
	"B|要求全部余额进行还款",
	"N|正常还款"
	//	TODO 二期实现
	/*,
	"S|跳过还款"*/
})
public enum PaymentCalcMethod {

	/**
	 *	要求全部余额进行还款 
	 */
	B,
	/**
	 *	正常还款 
	 */
	N
	//	TODO 二期实现
	//,
	/**
	 *	跳过还款 
	 */
	//S
}
