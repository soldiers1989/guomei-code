package com.sunline.ccs.param.def.enums;

import com.sunline.ark.support.meta.EnumInfo;

/**
 *  拿去花 贷款来源
 * @author zhengjf
 */
@EnumInfo({
	"C|当期还款",
	"B|逾期还款",
	"F|未来期还款"
})
public enum RepayType {
	/**
	 * 当期还款
	 */
	C,
	/**
	 * 逾期还款
	 */
	B,
	/**
	 * 未来期还款
	 */
	F
}
