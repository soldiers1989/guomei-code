package com.sunline.ccs.param.def.enums;

import com.sunline.ark.support.meta.EnumInfo;

/**
 * 浮动比例标识：
 * N - 不浮动
 * D - 下浮
 * U - 上浮
 */
@EnumInfo({
	"N|不浮动",
	"D|下浮",
	"U|上浮"
})
public enum FloatRate{
	/**
	 * 不浮动
	 */
	N,
	/**
	 * 下浮
	 */
	D,
	/**
	 * 上浮
	 */
	U
}
