package com.sunline.ccs.param.def.enums;

import com.sunline.ark.support.meta.EnumInfo;

/**
 * 分配方式
* @author fanghj
 *
 */
@EnumInfo({
	"F|按月平分",
	"S|分配表"
})
public enum DistributeMethod {
	/**
	 * 按月平分
	 */
	F,
	/**
	 * 分配表
	 */
	S
}
