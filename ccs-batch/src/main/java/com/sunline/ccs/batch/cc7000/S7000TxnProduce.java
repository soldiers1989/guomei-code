package com.sunline.ccs.batch.cc7000;

import com.sunline.ppy.dictionary.exchange.DdOnlineSuccIntefaceItem;
import com.sunline.ppy.dictionary.exchange.DdRequestInterfaceItem;
import com.sunline.ppy.dictionary.exchange.ExpiryChangeFileItem;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnHst;
//import com.sunline.smsd.service.sdk.CashLoanDirectCreditMsgItem;
//import com.sunline.smsd.service.sdk.DdSucessMessInterfaceItem;
//import com.sunline.smsd.service.sdk.LoanRepaymentMsgInterfaceItem;


/**
 * @see 类名：S7000TxnProduce
 * @see 描述：TODO 中文描述
 *
 * @see 创建日期：   2015-6-24下午2:07:49
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class S7000TxnProduce {

	private CcsTxnHst tmTxnHst;

	/**
	 * 约定还款成功短信
	 */
//	private DdSucessMessInterfaceItem ddSucessMess;
	
	/**
	 * 指定借据还款成功批量短信
	 */
//	private LoanRepaymentMsgInterfaceItem loanRepaymentMsg;
	
	/**
	 * 到期还款申请文件
	 */
	private ExpiryChangeFileItem expiryChangeFile;
	/**
	 * 现金分期批量放款文件
	 */
	private DdRequestInterfaceItem cashLoanDirectCreditFile;
	
	/**
	 * 现金分期实时放款成功文件
	 */
	private DdOnlineSuccIntefaceItem onlineSuccIntefaceFile;
	/**
	 * 现金分期放款短信
	 */
//	private CashLoanDirectCreditMsgItem cashLoanDirectCreditMsg;
	

	public CcsTxnHst getTmTxnHst() {
		return tmTxnHst;
	}

/*	public DdSucessMessInterfaceItem getDdSucessMess() {
		return ddSucessMess;
	}*/

	public ExpiryChangeFileItem getExpiryChangeFile() {
		return expiryChangeFile;
	}

	public void setExpiryChangeFile(ExpiryChangeFileItem expiryChangeFile) {
		this.expiryChangeFile = expiryChangeFile;
	}

	public DdRequestInterfaceItem getCashLoanDirectCreditFile() {
		return cashLoanDirectCreditFile;
	}

/*	public CashLoanDirectCreditMsgItem getCashLoanDirectCreditMsg() {
		return cashLoanDirectCreditMsg;
	}
*/
	public void setTmTxnHst(CcsTxnHst tmTxnHst) {
		this.tmTxnHst = tmTxnHst;
	}

/*	public void setDdSucessMess(DdSucessMessInterfaceItem ddSucessMess) {
		this.ddSucessMess = ddSucessMess;
	}
*/
	public void setCashLoanDirectCreditFile(DdRequestInterfaceItem cashLoanDirectCreditFile) {
		this.cashLoanDirectCreditFile = cashLoanDirectCreditFile;
	}

/*	public void setCashLoanDirectCreditMsg(CashLoanDirectCreditMsgItem cashLoanDirectCreditMsg) {
		this.cashLoanDirectCreditMsg = cashLoanDirectCreditMsg;
	}

	public LoanRepaymentMsgInterfaceItem getLoanRepaymentMsg() {
		return loanRepaymentMsg;
	}

	public void setLoanRepaymentMsg(LoanRepaymentMsgInterfaceItem loanRepaymentMsg) {
		this.loanRepaymentMsg = loanRepaymentMsg;
	}*/

	public DdOnlineSuccIntefaceItem getOnlineSuccIntefaceFile() {
		return onlineSuccIntefaceFile;
	}

	public void setOnlineSuccIntefaceFile(DdOnlineSuccIntefaceItem onlineSuccIntefaceFile) {
		this.onlineSuccIntefaceFile = onlineSuccIntefaceFile;
	}
	
}
