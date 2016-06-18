package com.sunline.ccs.param.def.enums;

import com.sunline.ark.support.meta.EnumInfo;

/**
 * 分期子产品状态
* @author wangz
 * @version 1.0
 */
@EnumInfo({
	"I|初建未使用",
	"A|使用中",
	"P|产品关闭"
})
public enum LoanFeeDefStatus {
	/**
	 * 初建未使用
	 */
	I,
	/**
	 * 使用中
	 */
	A,
	/**
	 * 产品关闭
	 */
	P
}
