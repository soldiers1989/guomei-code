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
	"B|银行配送兑换",
	"C|客户自行领取兑换"
})
public enum ExchType {

	B("银行配送兑换"), C("客户自行领取兑换");

	private String desc;

	private ExchType(String desc) {
		this.desc = desc;
	}

	public String getDesc() {
		return desc;
	}
}
