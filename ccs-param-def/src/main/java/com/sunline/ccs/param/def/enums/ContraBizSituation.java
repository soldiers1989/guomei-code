package com.sunline.ccs.param.def.enums;

import com.sunline.ark.support.meta.EnumInfo;

/**
 * 合同状态
* @author yuemk
 *
 */
@EnumInfo({
	"A|合同贷款结清", 
    "B|合同逾期",
    "C|已放款(未逾期) 当前有应还款额",
    "D|已放款（未结清，已跑批生成还款计划），但当前无应还款额",
    "E|放款成功但未跑批",
    "F|放款联机交易处理中",
    "G|开户但未放款",
    "H|开户未放款且合同已过期",
    "I|退货"
    
})
public enum ContraBizSituation {
	/**
	 * 合同贷款结清
	 */
	A,
	/**
	 * 合同逾期
	 */
	B,
	/**
	 * 已放款(未逾期) 当前有应还款额
	 */
	C,
	/**
	 * 已放款（未结清，已跑批生成还款计划），但当前无应还款额
	 */
	D,
	/**
	 * 放款成功但未跑批
	 */
	E,
	/**
	 * 放款联机交易处理中
	 */
	F,
	/**
	 * 开户但未放款
	 */
	G,
	/**
	 *  开户未放款且合同已过期
	 */
	H,
	/**
	 *  退货
	 */
	I
}
