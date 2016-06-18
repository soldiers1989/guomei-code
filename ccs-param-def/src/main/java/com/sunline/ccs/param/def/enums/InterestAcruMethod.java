package com.sunline.ccs.param.def.enums;

import com.sunline.ark.support.meta.EnumInfo;

/**
* @author fanghj
 *	计息方式
 */
@EnumInfo({
	"D|按日计息",
	"P|按期计息"
})
public enum InterestAcruMethod {
	/**
	 * 按日计息
	 */
	D,
	/**
	 * 按期计息
	 */
	P
}
