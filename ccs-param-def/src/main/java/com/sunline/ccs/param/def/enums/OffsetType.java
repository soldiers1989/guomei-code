package com.sunline.ccs.param.def.enums;

import com.sunline.ark.support.meta.EnumInfo;

/**
 * 抵扣类型
 * @author zhengjf
 *
 */
@EnumInfo({
	"I|利息", 
	"F|费用"
})
public enum OffsetType {
	
	/**
	 * 利息
	 */
	I,
	/**
	 * 费用
	 */
	F
}
