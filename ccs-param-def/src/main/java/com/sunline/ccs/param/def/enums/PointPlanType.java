package com.sunline.ccs.param.def.enums;

import com.sunline.ark.support.meta.EnumInfo;

/**
 * 积分计划类型
* @author fanghj
 *
 */
@EnumInfo({
	"S|单笔交易积分",
	"A|累积交易积分",
	"F|非交易积分"
})
public enum PointPlanType {
	S,
	A,
	F
}
