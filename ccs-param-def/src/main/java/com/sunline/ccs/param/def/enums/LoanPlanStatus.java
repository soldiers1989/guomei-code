package com.sunline.ccs.param.def.enums;

import com.sunline.ark.support.meta.EnumInfo;

/**
 * 分期产品状态
* @author fanghj
 * @date 2013-6-21  上午11:20:42
 * @version 1.0
 */
@EnumInfo({
	"A|使用中",
	"S|产品暂停使用"
})
public enum LoanPlanStatus {
	/**
	 * 使用中
	 */
	A,
	/**
	 * 产品暂停使用
	 */
	S
}
