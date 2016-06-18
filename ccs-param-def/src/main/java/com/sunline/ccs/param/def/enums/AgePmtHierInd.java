package com.sunline.ccs.param.def.enums;

import com.sunline.ark.support.meta.EnumInfo;

/**
 * 还款帐龄冲销优先方式
* @author fanghj
 *
 */
@EnumInfo({
	"B|BUCKET优先",
	"P|PLAN优先",
	"D|有DUE(往期欠款)的PLAN优先（有DUE的PLAN之间按照PLAN优先级顺序）",
	"I|有保费(保费优先)的PLAN优先（有保费的PLAN之间按照PLAN优先级顺序）"
})
public enum AgePmtHierInd {

	/**
	 * BUCKET优先
	 */
	B,
	/**
	 * PLAN优先
	 */
	P,
	/**
	 * 有DUE的PLAN优先
	 */
	D,
	/**
	 * 有保费(保费优先)的PLAN优先
	 */
	I
}
