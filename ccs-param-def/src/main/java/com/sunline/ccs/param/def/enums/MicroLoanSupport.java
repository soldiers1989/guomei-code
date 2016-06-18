package com.sunline.ccs.param.def.enums;

import com.sunline.ark.support.meta.EnumInfo;

@EnumInfo({ 
	"V|虚拟卡方式(virtualLoanCard)",
	"Q|准贷记卡方式(quasiCreditCard)"
	})
public enum MicroLoanSupport {
	
	/**
	 * 虚拟卡方式
	 */
	V,
	
	/**
	 * 准贷记卡方式
	 */
	Q

}
