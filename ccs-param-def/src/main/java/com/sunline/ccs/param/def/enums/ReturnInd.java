package com.sunline.ccs.param.def.enums;

import com.sunline.ark.support.meta.EnumInfo;

/**
 * 调整状态
* @author fanghj
 * @date 2013-6-21  上午11:20:42
 * @version 1.0
 */
@EnumInfo({
	"N|不允许退货",
	"B|只允许全额退货"
})
public enum ReturnInd {

	/**
	 * 不允许退货
	 */
	N,
	/**
	 * 只允许全额退货
	 */
	B
}
