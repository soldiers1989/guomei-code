/**
 * 
 */
package com.sunline.ccs.ui.client.pages.htrecord;

import com.google.gwt.i18n.client.Constants;

/**
 * 未出账单交易查询页面字符常量
* @author fanghj
 *
 */
public interface HistoryTradingRecordConstants extends Constants {
	public static final String CHANNEL_ALL = "ALL";
	public static final String CHANNEL_CUP = "CUP";
	public static final String CHANNEL_BANK = "BANK";
	public static final String CHANNEL_VISA = "VISA";
	public static final String CHANNEL_MC = "MC";
	public static final String CHANNEL_AMEX = "AMEX";
	public static final String CHANNEL_ICL = "ICL";
	public static final String CHANNEL_JCB = "JCB";
	
	String pageTitle();
	String cardNoTitle();
	String selectItemChannel();
	String channelAll();
	String channelBank();
	String channelCup();
	String channelVisa();
	String channelMc();
	String channelAmex();
	String channelIcl();
	String channelJcb();
}
