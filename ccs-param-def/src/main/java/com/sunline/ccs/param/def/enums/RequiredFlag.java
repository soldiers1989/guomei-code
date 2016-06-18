package com.sunline.ccs.param.def.enums;

import com.sunline.ark.support.meta.EnumInfo;

/**
 * 选择标识
* @author yuemk
 *
 */
@EnumInfo({
	"M|必输",
	"O|选输",
	"N|不输"
})
public enum RequiredFlag {

	/**
	 * 必输
	 */
	M,
	/**
	 * 选输
	 */
	O,
	/**
	 * 不输
	 */
	N,
}
