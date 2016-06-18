package com.sunline.ccs.param.def.enums;

import com.sunline.ark.support.meta.EnumInfo;

/**
 * 
 * 提前还款支持标识
 * @version 1.0
 */
@EnumInfo({
	"N|不允许提前还款",
	"B|只允许全额提前还款"
})
public enum PrepaymentInd {

	/**
	 * 不允许提前还款
	 */
	N,
	/**
	 * 只允许全额提前还款
	 */
	B
}
