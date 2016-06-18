package com.sunline.ccs.param.def.enums;

import com.sunline.ark.support.meta.EnumInfo;

/**
 * 入账许可指示
* @author fanghj
 *
 */
@EnumInfo({
	"A|正常入账",
	"R|拒绝入账",
	"D|只允许贷记入账"
})
public enum PostAvailiableInd {

	/**
	 *	正常入账 
	 */
	A,
	/**
	 *	拒绝入账 
	 */
	R,
	/**
	 *	只允许贷记入账
	 */
	D
}
