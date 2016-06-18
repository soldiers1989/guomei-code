package com.sunline.ccs.param.def.enums;

import com.sunline.ark.support.meta.EnumInfo;

/**
 * 首次年费收取方式
 * I - issue
 * S - first stmt
 * A - activate
 * T - first transaction
 */
@EnumInfo({
	"I|发卡收取",
	"A|激活",
	"T|首次交易"
})
public enum FirstCardFeeInd{
	/**
	 * 发卡收取
	 */
	I,
	/**
	 * 激活
	 */
	A,
	/**
	 * 首次交易
	 */
	T
}
