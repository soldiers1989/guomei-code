package com.sunline.ccs.ui.client.commons;

import com.sunline.ark.support.meta.EnumInfo;


/**
 * 
 * @说明 自定义KylinButton配置信息枚举
 * 
 * @version 1.0 
 *
 * @Date Jun 17, 2015
 *
 * @作者 yeyu
 *
 * @修改记录 
 * [编号：20150617_01]，[修改人：yeyu ]，[修改说明：加入注释]
 */
@EnumInfo({
	"AGREE|同意",
	"REFUSE|拒绝"
})
public enum CommonBtnCustomizeConfig {

	/**
	 * 同意
	 */
	AGREE("同意"),
	/**
	 * 拒绝
	 */
	REFUSE("拒绝"),
	
	FETCHPLAN("获取信用计划");
	
	private String  btnName;
	
	/**
	 * 
	 * @param btnName 按钮名称
	 */
	private CommonBtnCustomizeConfig(String btnName)
	{
		this.btnName = btnName;
	}
	
	/**
	 * 
	 * @return 自定义按钮显示内容
	 *
	 * @说明 获取自定义按钮显示内容
	 *
	 * @author yeyu
	 *
	 * @修改记录 
	 * [编号：20150617_01]，[修改人：yeyu ]，[修改说明：加入注释]
	 */
	public String getBtnName() {
		return btnName;
	}
}
