package com.sunline.ccs.batch.cc5000;

import com.sunline.ppy.dictionary.report.ccs.AcctCloseRptItem;
import com.sunline.ppy.dictionary.report.ccs.CancelRptItem;

/**
 * @see 类名：S5001AcctClose
 * @see 描述：销卡销户及关闭账户报表
 *
 * @see 创建日期：   2015-6-23下午7:53:38
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class S5001AcctClose {
	
	/**
	 * 销卡销户送报表接口
	 */
	private CancelRptItem cancelRptItem;
	
	/**
	 * 关闭账户送报表接口 
	 */
	private AcctCloseRptItem acctCloseRptItem;
	
	
	
	public CancelRptItem getCancelRptItem() {
		return cancelRptItem;
	}

	public AcctCloseRptItem getAcctCloseRptItem() {
		return acctCloseRptItem;
	}

	public void setCancelRptItem(CancelRptItem cancelRptItem) {
		this.cancelRptItem = cancelRptItem;
	}

	public void setAcctCloseRptItem(AcctCloseRptItem acctCloseRptItem) {
		this.acctCloseRptItem = acctCloseRptItem;
	}
	
}
