package com.sunline.ccs.ui.client.commons;

import com.google.gwt.i18n.client.Constants;

/**
 * 
 * @说明 用户界面工具类
 * 
 * @version 1.0 
 *
 * @Date Jun 17, 2015
 *
 * @作者 fanghj
 *
 * @修改记录 
 * 
 */
public interface UIUtilConstants extends Constants {
	//卡账指示 A-账户交易 C-卡片交易
	public static final String INDICATOR_NAME_CARD_ACCT = "cardAccountIndicator";
	public static final String INDICATOR_VAL_ACCT = "A";
	public static final String INDICATOR_VAL_CARD = "C";
	public static final String CHANNEL_ALL = "ALL";
	public static final String CHANNEL_CUP = "CUP";
	public static final String CHANNEL_BANK = "BANK";
	public static final String CHANNEL_VISA = "VISA";
	public static final String CHANNEL_MC = "MC";
	public static final String CHANNEL_AMEX = "AMEX";
	public static final String CHANNEL_ICL = "ICL";
	public static final String CHANNEL_JCB = "JCB";
	
	public static final String RMBTITLE = "156";
	public static final String USDTITLE = "840";
	
	String cardAccIndTitle();

	String cardIndTitle();

	String acctIndTitle();
	
	String btnTitleClear();
	
	String labelCurrencyRMB();
	
	String labelCurrencyUS();
	
	String labelCurrency();
	
	String channelAll();
	String channelCup();
	String channelBank();
	String channelVisa();
	String channelMc();
	String channelAmex();
	String channelIcl();
	String channelJcb();
}
