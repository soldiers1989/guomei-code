package com.sunline.ccs.batch.cc6100;

import java.util.ArrayList;
import java.util.List;

import com.sunline.ppy.dictionary.exchange.StmtInterfaceItem;
import com.sunline.ppy.dictionary.exchange.StmttxnInterfaceItem;
//import com.sunline.smsd.service.sdk.StmtMsgInterfaceItem;

/**
 * @see 类名：S6101Statement
 * @see 描述：账单功能输出项
 *
 * @see 创建日期：   2015-6-24下午2:05:42
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class S6101Statement {
	/**
	 * 实体帐单交易接口文件内容列表
	 */
	private List<StmttxnInterfaceItem> stmttxnInterfaceItems = new ArrayList<StmttxnInterfaceItem>();

	/**
	 * 实体账单汇总接口文件内容
	 */
	private List<StmtInterfaceItem> stmtInterfaceItems = new ArrayList<StmtInterfaceItem>();
	
	/**
	 * 账单提醒短信
	 */
//	private List<StmtMsgInterfaceItem> stmtMsgInterfaceItems = new ArrayList<StmtMsgInterfaceItem>();

	public List<StmttxnInterfaceItem> getStmttxnInterfaceItems() {
		return stmttxnInterfaceItems;
	}

	public void setStmttxnInterfaceItems(List<StmttxnInterfaceItem> stmttxnInterfaceItems) {
		this.stmttxnInterfaceItems = stmttxnInterfaceItems;
	}

	public List<StmtInterfaceItem> getStmtInterfaceItems() {
		return stmtInterfaceItems;
	}

	public void setStmtInterfaceItems(List<StmtInterfaceItem> stmtInterfaceItems) {
		this.stmtInterfaceItems = stmtInterfaceItems;
	}

/*	public List<StmtMsgInterfaceItem> getStmtMsgInterfaceItems() {
		return stmtMsgInterfaceItems;
	}

	public void setStmtMsgInterfaceItems(List<StmtMsgInterfaceItem> stmtMsgInterfaceItems) {
		this.stmtMsgInterfaceItems = stmtMsgInterfaceItems;
	}*/

}
