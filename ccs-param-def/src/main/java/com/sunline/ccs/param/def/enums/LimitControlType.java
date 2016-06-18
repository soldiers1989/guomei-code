package com.sunline.ccs.param.def.enums;

import com.sunline.ark.support.meta.EnumInfo;

/**
 * 额度控制类型
 * H|highest-最高额度控制
 * S|sum-汇总额度控制
 */
@EnumInfo({
	"H|最高额度控制",
	"S|汇总额度控制"
})
public enum LimitControlType{H,S}
