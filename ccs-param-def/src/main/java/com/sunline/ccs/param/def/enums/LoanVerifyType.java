package com.sunline.ccs.param.def.enums;

import com.sunline.ark.support.meta.EnumInfo;

/**
 * 放款审核类型
* @author lisy
 * @date 2015-9-23
 * @version 1.2
 */
@EnumInfo({
	"P|放款人工审核",
	"O|放款自动审核"
})
public enum LoanVerifyType {

	/**
	 * 放款人工审核
	 */
	P,
	/**
	 * 放款自审核
	 */
	O
}
