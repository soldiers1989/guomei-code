package com.sunline.ccs.param.def.enums;

import com.sunline.ark.support.meta.EnumInfo;

/**
 * 约定还款日指示
* @author fanghj
 *
 */
@EnumInfo({
	"P|到期还款日前若干天",
	"F|每月固定日期"
})
public enum DirectDbIndicator {
	/**
	 * 到期还款日前若干天
	 */
	P,
	/**
	 * 每月固定日期
	 */
	F;
}
