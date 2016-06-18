package com.sunline.ccs.batch.cc1500;

import com.sunline.ppy.dictionary.exchange.MSLoanMsgItem;
import com.sunline.ppy.dictionary.report.ccs.AcctCrlmtAdjRptItem;


/**
 * @see 类名：S8502AcctCrlmtAdjRpt
 * @see 描述： 额度调整报表输出对象
 *
 * @see 创建日期： 2015-11-12
 * @author mengxiang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class S1502AcctCrlmtAdjRpt {

	/**
	 * 调额成功报表文件
	 */
	private AcctCrlmtAdjRptItem acctCrlmtAdjSuccessRpt;
	
	/**
	 * 调额失败报表文件
	 */
	private AcctCrlmtAdjRptItem acctCrlmtAdjFailRpt;
	
	/**
	 * 调额短信文件
	 */
	private MSLoanMsgItem acctCrlmtAdjMsg;

	
	public AcctCrlmtAdjRptItem getAcctCrlmtAdjSuccessRpt() {
		return acctCrlmtAdjSuccessRpt;
	}

	
	public void setAcctCrlmtAdjSuccessRpt(AcctCrlmtAdjRptItem acctCrlmtAdjSuccessRpt) {
		this.acctCrlmtAdjSuccessRpt = acctCrlmtAdjSuccessRpt;
	}

	
	public AcctCrlmtAdjRptItem getAcctCrlmtAdjFailRpt() {
		return acctCrlmtAdjFailRpt;
	}

	
	public void setAcctCrlmtAdjFailRpt(AcctCrlmtAdjRptItem acctCrlmtAdjFailRpt) {
		this.acctCrlmtAdjFailRpt = acctCrlmtAdjFailRpt;
	}


	public MSLoanMsgItem getAcctCrlmtAdjMsg() {
		return acctCrlmtAdjMsg;
	}


	public void setAcctCrlmtAdjMsg(MSLoanMsgItem acctCrlmtAdjMsg) {
		this.acctCrlmtAdjMsg = acctCrlmtAdjMsg;
	}
	
}
