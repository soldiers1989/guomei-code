package com.sunline.ccs.param.def.enums;

import com.sunline.ark.support.meta.EnumInfo;

/**
 * 调整状态
* @author fanghj
 * @date 2013-6-21  上午11:20:42
 * @version 1.0
 */
@EnumInfo({
	"Y|退货退积分",
    "N|退货不退积分"
})
public enum ReturnPointInd {

	/**
	 * 退货退积分
	 */
	Y,
	/**
	 * 退货不退积分
	 */
	N
}
