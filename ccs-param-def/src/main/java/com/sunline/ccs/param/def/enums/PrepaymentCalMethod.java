package com.sunline.ccs.param.def.enums;

import com.sunline.ark.support.meta.EnumInfo;

@EnumInfo({
	"B|按账单日计算",
	"A|按预约日计算"
})
public enum PrepaymentCalMethod {
	/**
	 * 按账单日计算
	 */
	B,
	/**
	 * 按预约日计算
	 */
	A
}
