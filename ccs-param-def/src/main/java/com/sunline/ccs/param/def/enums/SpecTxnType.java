package com.sunline.ccs.param.def.enums;

import com.sunline.ark.support.meta.EnumInfo;

/**
 * 特定交易类型
* @author fanghj
 *
 */
@EnumInfo({
	"A01|有卡自助",
	"A02|无卡自助",
	"A03|互联网消费",
	"A04|moto",
	"A09|代收"
})
public enum SpecTxnType {
	/**
	 * 有卡自助
	 */
	A01,
	/**
	 * 无卡自助
	 */
	A02,
	/**
	 * 互联网消费
	 */
	A03,
	/**
	 * moto
	 */
	A04,
	/**
	 * 代收
	 */
	A09
}
