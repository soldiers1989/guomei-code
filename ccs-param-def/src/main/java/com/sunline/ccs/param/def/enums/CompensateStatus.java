package com.sunline.ccs.param.def.enums;

import com.sunline.ark.support.meta.EnumInfo;

/**
 * @author lin
 *
 */
@EnumInfo({
	"NotCompensated|未代偿",
	"CompstRecorded|代偿申请已记录",
	"CompstRefundRecorded|代偿退还申请已记录"
})
public enum CompensateStatus {
	
	/**
	 * 未代偿
	 */
	NotCompensated,

	/**
	 * 代偿申请已记录
	 */
	CompstRecorded,
	
	/**
	 * 代偿退还申请已记录
	 */
	CompstRefundRecorded


}
