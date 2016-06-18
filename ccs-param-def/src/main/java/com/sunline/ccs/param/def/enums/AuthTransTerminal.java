package com.sunline.ccs.param.def.enums;

import com.sunline.ark.support.meta.EnumInfo;

@EnumInfo({
	"T00|其他",
	"OTC|柜面",
	"ATM|ATM",
	"POS|POS普通",
	"PHT|POSMOTO手工",
	"PHE|POSMOTO电子",
	"CS|客服",
	"IVR|IVR",
	"EB|EBANK网银",
	"MB|EBANK手机银行",
	"HOST|内管",
	"THIRD|第三方支付",
	"APP|手机客户端"
})
/**
 * 交易渠道
 */
public enum AuthTransTerminal{
	/**
	 * 其他
	 */
	T00,
	/**
	 * 柜面
	 */
	OTC, 
	/**
	 * ATM
	 */
	ATM, 
	/**
	 * POS普通
	 */
	POS, 
	/**
	 * POS手工MOTO
	 */
	PHT, 
	/**
	 * POS手工电子
	 */
	PHE, 
	/**
	 * IVR
	 */
	IVR, 
	/**
	 * EBANK网银
	 */
	EB, 
	/**
	 * EBANK手机银行
	 */
	MB, 
	/**
	 * 内管
	 */
	HOST, 
	/**
	 * 第三方支付
	 */
	THIRD, 
	/**
	 * 客服
	 */
	CS,	/**
	 * 手机APP
	 */
	APP
	
	}
