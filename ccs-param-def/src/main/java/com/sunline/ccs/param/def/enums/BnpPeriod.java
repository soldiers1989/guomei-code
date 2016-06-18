package com.sunline.ccs.param.def.enums;

import com.sunline.ark.support.meta.EnumInfo;

/**
 * 余额成份账期
* @author fanghj
 *
 */
@EnumInfo({
	"CTD|当期",
	"PAST|往期"
})
public enum BnpPeriod {
	/**
	 * 当期
	 */
	CTD,
	/**
	 * 往期
	 */
	PAST
}
