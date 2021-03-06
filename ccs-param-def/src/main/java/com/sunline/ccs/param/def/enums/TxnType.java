package com.sunline.ccs.param.def.enums;

import com.sunline.ark.support.meta.EnumInfo;

/**
 * 交易类型
* @author fanghj
 */
@EnumInfo({
	"T01|消费",
	"T02|预授权",
	"T03|退货",
	"T04|取现",
	"T05|还款",
	"T06|转账",
	"T07|圈存",
	"T08|圈提",
	"T09|分期",
	"T10|利息",
	"T11|滞纳金",
	"T12|超限费",
	"T13|手续费",
	"T14|积分增加",
	"T15|积分减少",
	"T16|积分兑换",
	"T17|保险费",
	"T18|印花税",
	"T19|寿险计划包费",
	"T20|罚金"
})
public enum TxnType {
	/**
	 * 消费
	 */
	T01,
	/**
	 * 预授权
	 */
	T02,
	/**
	 * 退货
	 */
	T03,
	/**
	 * 取现
	 */
	T04,
	/**
	 * 还款
	 */
	T05,
	/**
	 * 转账
	 */
	T06,
	/**
	 * 圈存
	 */
	T07,
	/**
	 * 圈提
	 */
	T08,
	/**
	 * 分期
	 */
	T09,
	/**
	 * 利息
	 */
	T10,
	/**
	 * 滞纳金
	 */
	T11,
	/**
	 * 超限费
	 */
	T12,
	/**
	 * 手续费
	 */
	T13,
	/**
	 * 积分增加
	 */
	T14,
	/**
	 * 积分减少
	 */
	T15,
	/**
	 * 积分兑换
	 */
	T16,
	/**
	 * 保险费
	 */
	T17,
	/**
	 * 印花税
	 */
	T18,
	/**
	 * 寿险计划包费
	 */
	T19,
	/**
	 * 罚金
	 */
	T20
	
}
