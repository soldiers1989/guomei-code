package com.sunline.ccs.batch.cc5000;

import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;

/**
 * @see 类名：U5001AcctGroup
 * @see 描述：销卡销户及关闭账户报表
 *
 * @see 创建日期：   2015-6-23下午7:53:22
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class U5001AcctGroup {

	/**
	 * 账户
	 */
	private CcsAcct acct;
	
	/**
	 * 联机账户 
	 */
	private CcsAcctO acctO;
	
	

	public CcsAcct getAcct() {
		return acct;
	}

	public CcsAcctO getAcctO() {
		return acctO;
	}

	public void setAcct(CcsAcct acct) {
		this.acct = acct;
	}

	public void setAcctO(CcsAcctO acctO) {
		this.acctO = acctO;
	}

}
