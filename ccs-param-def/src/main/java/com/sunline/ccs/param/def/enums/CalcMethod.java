package com.sunline.ccs.param.def.enums;

import com.sunline.ark.support.meta.EnumInfo;

/**
 * 附加费用计算方式
* @author fanghj
 *
 */
@EnumInfo({
	"R|附加金额按比例计算",
	"A|附加金额为固定金额"
})
public enum CalcMethod {
	/**
	 * 按比例
	 */
	R,
	/**
	 * 固定金额
	 */
	A
}
