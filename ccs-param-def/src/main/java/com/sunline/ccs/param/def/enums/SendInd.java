package com.sunline.ccs.param.def.enums;

import com.sunline.ark.support.meta.EnumInfo;

/**
 * 描述：积分兑换方式枚举
 * 
* @author fanghj
 * @date 2013-5-6 下午3:58:34
 * @version 1.0
 */
@EnumInfo({
	"C|客户自定义的寄送地址",
	"U|按照地址类型中定义的地址寄送"
})
public enum SendInd {

	C("客户自定义的寄送地址"), U("按照地址类型中定义的地址寄送");

	private String desc;

	private SendInd(String desc) {
		this.desc = desc;
	}

	public String getDesc() {
		return desc;
	}
}
