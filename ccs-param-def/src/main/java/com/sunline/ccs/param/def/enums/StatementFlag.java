package com.sunline.ccs.param.def.enums;

import com.sunline.ark.support.meta.EnumInfo;

/**
 * 输出账单类型标志
* @author fanghj
 *
 */
@EnumInfo({
	"N|normal指正常出账单",
	"H|hold指不出任何形式的账单",
	"O|online指仅出联机账单"
})
public enum StatementFlag {

	/**
	 *	正常出账单 
	 */
	N,

	/**
	 *	不出任何形式的账单 
	 */
	H,

	/**
	 *	仅出联机账单 
	 */
	O
}
