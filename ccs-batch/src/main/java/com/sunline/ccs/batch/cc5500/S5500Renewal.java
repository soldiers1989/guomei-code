package com.sunline.ccs.batch.cc5500;

import com.sunline.ppy.dictionary.exchange.ExpiryChangeFileItem;
import com.sunline.ppy.dictionary.report.ccs.RenewRptItem;
//import com.sunline.smsd.service.sdk.RenewMsgItem;


/**
 * @see 类名：S5500Renewal
 * @see 描述：到期换卡输出对象
 *
 * @see 创建日期：   2015-6-23下午7:57:32
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class S5500Renewal {

	/**
	 * 送报表接口
	 */
	private RenewRptItem renewRptItem;
	
	/**
	 * 换卡回盘接口
	 */
	private ExpiryChangeFileItem expiryChangeFileItem;
	
	/**
	 * 到期换卡通知短信接口
	 */
//	private RenewMsgItem renewMsgItem;
	
	public RenewRptItem getRenewRptItem() {
		return renewRptItem;
	}
	public ExpiryChangeFileItem getExpiryChangeFileItem() {
		return expiryChangeFileItem;
	}
	public void setRenewRptItem(RenewRptItem renewRptItem) {
		this.renewRptItem = renewRptItem;
	}
	public void setExpiryChangeFileItem(ExpiryChangeFileItem expiryChangeFileItem) {
		this.expiryChangeFileItem = expiryChangeFileItem;
	}
/*	public RenewMsgItem getRenewMsgItem() {
		return renewMsgItem;
	}
	public void setRenewMsgItem(RenewMsgItem renewMsgItem) {
		this.renewMsgItem = renewMsgItem;
	}*/

}
