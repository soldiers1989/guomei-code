package com.sunline.ccs.ui.client.pub;

import com.sunline.ark.support.meta.EnumInfo;


@EnumInfo({
	"AGREE|同意",
	"REFUSE|拒绝",
	"STAGETERMINATION|分期交易终止",
	"REVOKESTAGETERMINATION|撤销分期终止",
	"ACCRTRIAL|计提试算",
	"ACCRAPPLY|计提申请",
	"FETCHPLAN|获取信用计划"
})
/*
 * 按钮名称
 */
public enum BtnName {

	/**
	 * 同意
	 */
	AGREE("同意"),
	/**
	 * 拒绝
	 */
	REFUSE("拒绝"),
	
	STAGETERMINATION("分期交易终止"),
	
	REVOKESTAGETERMINATION("撤销分期终止"),
	ACCRTRIAL("计提试算"),
	ACCRAPPLY("计提申请"),
	
	FETCHPLAN("获取信用计划");
	
	private String  btnName;
	
	private BtnName(String btnName)
	{
		this.btnName = btnName;
	}
	
	public String getBtnName() {
		return btnName;
	}
}
