package com.sunline.ccs.param.def.enums;

import com.sunline.ark.support.meta.EnumInfo;

/**
 * 分期产品状态
* @author fanghj
 * @date 2013-6-21  上午11:20:42
 * @version 1.0
 */
@EnumInfo({
	"I|初建未使用",
	"A|使用中",
	"S|活动暂停使用",
	"P|活动关闭"
})
public enum ProgramStatus {

	/**
	 * 初建未使用
	 */
	I,
	/**
	 * 使用中
	 */
	A,
	/**
	 * 活动暂停使用
	 */
	S,
	/**
	 * 活动关闭
	 */
	P
}
