package com.sunline.ccs.param.def.enums;

import com.sunline.ark.support.meta.EnumInfo;

/**
 * 调整状态
* @author fanghj
 * @date 2013-6-21  上午11:20:42
 * @version 1.0
 */
@EnumInfo({
	"W|等待复核调整",
	"A|同意复核调整",
	"R|拒绝复核调整"
})
public enum AdjState {

	/**
	 * 等待复核调整
	 */
	W,
	/**
	 * 同意复核调整
	 */
	A,
	/**
	 * 拒绝复核调整
	 */
	R
}
