package com.sunline.ccs.param.def.enums;

import com.sunline.ark.support.meta.EnumInfo;

/**
 *无卡自助识别模式
 *
* @author fanghj
 *@time 2014-4-22 下午6:01:53
 */
@EnumInfo({
	"A|银联代发辅助识别",
	"B|银联代发自主识别",
	"C|自主发送自主识别"
})
public enum NoCardSelfDiscern {

	/**
	 * 银联代发辅助识别
	 */
	A,
	
	/**
	 * 银联代发自主识别
	 */
	B,
	
	/**
	 * 自主发送自主识别
	 */
	C
}
