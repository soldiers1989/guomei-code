package com.sunline.ccs.param.def.enums;

import com.sunline.ark.support.meta.EnumInfo;
/**
 * 锁定码层级
* @author fanghj
 *
 */
@EnumInfo({
	"All|所有",
	"ACCT|账户层",
	"CARD|卡片层"
})
public enum BlockLevel {

	/**
	 * 所有
	 */
	All,
	/**
	 * 账户层
	 */
	ACCT,
	/**
	 * 卡片层
	 */
	CARD
}
