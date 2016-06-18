package com.sunline.ccs.param.def.enums;

import com.sunline.ark.support.meta.EnumInfo;

/**
 * 额度计算类型
* @author fanghj
 *
 */
@EnumInfo({
	"S|SUM，客户额度取其下账户额度之和",
	"H|HIGHEST，客户额度去其下账户额度最大值"
})
public enum LimitType {

	/**
	 *	SUM，客户额度取其下账户额度之和 
	 */
	S,

	/**
	 *	HIGHEST，客户额度去其下账户额度最大值 
	 */
	H
}
