package com.sunline.ccs.param.def.enums;

import com.sunline.ark.support.meta.EnumInfo;

/**
 * 信用状况
* @author fanghj
 *
 */
@EnumInfo({
	"N|正常",
	"D|拖欠"
})
public enum CreditStatus {

	/**
	 *	正常 
	 */
	N,
	/**
	 *	拖欠 
	 */
	D
}
