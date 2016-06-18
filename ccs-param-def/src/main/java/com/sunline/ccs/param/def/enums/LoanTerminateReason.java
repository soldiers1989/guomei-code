package com.sunline.ccs.param.def.enums;

import com.sunline.ark.support.meta.EnumInfo;

/**
 * 分期中止原因代码
* @author fanghj
 *
 */
@EnumInfo({
	"P|提前还款",
	"M|银行业务人员手工终止（manual）",
	"D|逾期自动终止（delinquency）",
	"R|锁定码终止(Refund)",
	"V|持卡人手动终止",
	"C|理赔终止",
	"T|退货终止"
})
public enum LoanTerminateReason {

	/**
	 *	提前还款 
	 */
	P,
	/**
	 *	银行业务人员手工终止（manual） 
	 */
	M,
	/**
	 *	逾期自动终止（delinquency） 
	 */
	D,
	/**
	 *	锁定码终止(Refund)
	 */
	R,
	/**
	 * 持卡人手动终止
	 */
	V,
	/**
	 * 理赔终止
	 */
	C,
	/**
	 * 退货终止
	 */
	T
}
