package com.sunline.ccs.param.def.enums;

import com.sunline.ark.support.meta.EnumInfo;

@EnumInfo({
	"B1|卡面损坏",
	"B2|磁条损坏",
	"D1|CVV错误",
	"I1|制卡错误",
	"C1|更换姓名",
	"C2|没收到",	
	"O1|其他原因",
	"E1|到期换卡",
	"R1|被盗挂失",
	"R2|丢失挂失",
	"F1|被伪冒卡换卡"
})
/**
 * 换卡原因
 * 
* @author fanghj
 *
 */
public enum ChangeReason {
	// 损坏类
	B1("卡面损坏"),
	B2("磁条损坏"),
	D1("CVV错误"),
	I1("制卡错误"),
	C1("更换姓名"),
	C2("没收到"),	
	O1("其他原因"),
	
	// 新增服务到期换卡
	E1("到期换卡"),
	
	// 挂失类
	R1("被盗挂失"),
	R2("丢失挂失"),
	F1("被伪冒卡换卡");
	
	private String desc;

	private ChangeReason(String desc) {
		this.desc = desc;
	}

	public String getDesc() {
		return desc;
	}
}
