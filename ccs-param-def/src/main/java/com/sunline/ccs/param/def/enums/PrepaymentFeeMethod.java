package com.sunline.ccs.param.def.enums;

import com.sunline.ark.support.meta.EnumInfo;

/**
 * 提前还款手续费计算方式
 * @date 2013-6-21  上午11:20:42
 * @version 1.0
 */
@EnumInfo({
	"R|按比例计算",
	"A|固定金额"
})
public enum PrepaymentFeeMethod {

	/**
	 * 按比例计算
	 */
	R,
	/**
	 * 固定金额
	 */
	A
}
