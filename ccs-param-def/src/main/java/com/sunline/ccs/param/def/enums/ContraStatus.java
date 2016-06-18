package com.sunline.ccs.param.def.enums;

import com.sunline.ark.support.meta.EnumInfo;

/**
 * 合同状态
* @author yuemk
 *
 */
@EnumInfo({
	"N|正常",
	"X|超出有效期停用",
	"F|冻结或止付",
	"T|系统强制结清"
})
public enum ContraStatus {
	/**
	 * 正常
	 */
	N,
	/**
	 * 超出有效期停用
	 */
	X,
	/**
	 * 冻结或止付
	 */
	F,
	/**
	 * 系统强制结清
	 */
	T
}
