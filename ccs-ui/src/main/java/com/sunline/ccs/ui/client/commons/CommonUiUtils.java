package com.sunline.ccs.ui.client.commons;

import java.util.LinkedHashMap;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sunline.kylin.web.ark.client.helper.SelectItem;
import com.sunline.kylin.web.ark.client.ui.KylinButton;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.ui.event.client.IClickEventListener;

/**
 * 
 * @说明 通用自定义组件包装类
 * 
 * @version 1.0 
 *
 * @Date Jun 17, 2015
 *
 * @作者 dch
 *
 * @修改记录 
 * 
 */
public class CommonUiUtils {
	/**
	 * 
	 *
	 * @param revokestagetermination 按钮名称
	 * 
	 * @param url 按钮背景图片URL
	 * 
	 * @param listener 事件监听器对象
	 * 
	 * @return KylinButton
	 *
	 * @说明 创建按钮
	 *
	 * @author dch
	 *
	 * @修改记录 
	 *
	 */
	public static KylinButton createButton(BtnName revokestagetermination, String url, IClickEventListener listener){
		KylinButton btn = new KylinButton(revokestagetermination.getBtnName(), url);
		btn.addClickEventListener(listener);
		return btn;
	}
	
	/**
	 * 
	 *
	 * @param constants 下拉列表项常量值
	 * 
	 * @return 下拉列表项
	 *
	 * @说明 账标识下拉框值
	 *
	 * @author dch
	 *
	 * @修改记录 
	 *
	 */
	@SuppressWarnings("all")
	public static SelectItem<String> cardOrAcctSelectItem(UIUtilConstants constants) {
		SelectItem<String> cardOrAcct = new SelectItem();
		LinkedHashMap<String, String> valueMap = new LinkedHashMap<String, String>();
		valueMap.put(UIUtilConstants.INDICATOR_VAL_ACCT, constants.acctIndTitle());
		valueMap.put(UIUtilConstants.INDICATOR_VAL_CARD, constants.cardIndTitle());
		cardOrAcct.setValue(valueMap);
		return cardOrAcct;
	}
	
	/**
	 * 
	 *
	 * @param constants 渠道下拉列表项常量值
	 * 
	 * @return 渠道下拉列表项
	 *
	 * @说明 创建自定义渠道下拉列表项
	 *
	 * @author dch
	 *
	 * @修改记录 
	 *
	 */
	@SuppressWarnings("all")
	public static SelectItem<String> channalSelectItem(UIUtilConstants constants) {
		SelectItem<String> channal = new SelectItem();
		LinkedHashMap<String, String> channalValue = new LinkedHashMap<String, String>();
		channalValue.put(UIUtilConstants.CHANNEL_ALL, constants.channelAll());
		channalValue.put(UIUtilConstants.CHANNEL_BANK, constants.channelBank());
		channalValue.put(UIUtilConstants.CHANNEL_CUP, constants.channelCup());
		channalValue.put(UIUtilConstants.CHANNEL_VISA, constants.channelVisa());
		channalValue.put(UIUtilConstants.CHANNEL_MC, constants.channelMc());
		channalValue.put(UIUtilConstants.CHANNEL_AMEX, constants.channelAmex());
		channalValue.put(UIUtilConstants.CHANNEL_ICL, constants.channelIcl());
		channalValue.put(UIUtilConstants.CHANNEL_JCB, constants.channelJcb());
    	channal.setValue(channalValue);
		return channal;
	}
	
	/**
	 *
	 * @param constants 币种下拉列表项常量值
	 * 
	 * @return 自定义币种下拉列表项
	 *
	 * @说明 创建自定义币种下拉列表项
	 *
	 * @author dch
	 *
	 * @修改记录 
	 *
	 */
	public static SelectItem<String> currencySelectItem(UIUtilConstants constants){
		SelectItem<String> currencyType = new SelectItem<String>();
		LinkedHashMap<String, String> currencyValue = new LinkedHashMap<String, String>();
		currencyValue.put(UIUtilConstants.RMBTITLE, constants.labelCurrencyRMB());
		currencyValue.put(UIUtilConstants.USDTITLE, constants.labelCurrencyUS());
		currencyType.setValue(currencyValue);
		return currencyType;
	}
	
	public static native JavaScriptObject currencyItem()/*-{
		var currencyItem = {'人民币':'156'};
		return currencyItem;
	}-*/;
	
	public static HorizontalPanel lineLayoutForm(KylinForm form,KylinButton button,String width,String height){
		HorizontalPanel hPanel = new HorizontalPanel();
		if(height != null){
			hPanel.setHeight(height);
		}
		if(width != null){
			hPanel.setWidth(width);
		}
		VerticalPanel vPanel = new VerticalPanel();
		VerticalPanel blank = new VerticalPanel();
		blank.setHeight("10px");
		Element vElement = blank.getElement();
		hPanel.add(form);
		vPanel.add(blank);
		vPanel.add(button);
		hPanel.add(vPanel);
		Element pElement = vElement.getParentElement();
		pElement.setAttribute("style", "vertical-align: top;height:10px;");
		return hPanel;
	}
	
}
