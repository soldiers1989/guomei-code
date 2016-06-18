package com.sunline.ccs.service.msentity;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ppy.dictionary.enums.AuthTransStatus;
import com.sunline.ppy.dictionary.enums.Indicator;

public class TNQContractTxnQueryRespInfo implements Serializable {
	private static final long serialVersionUID = 5239620304362438905L;
	
	/**
	 * 交易金额	DECIMAL(15,2)	Y
	 */
	@JsonProperty(value="TXN_AMT")
	public String txnAmt;
	/**
	 * 入账日期	VARCHAR2(8)	N
	 */
	@JsonProperty(value="PROC_DATE")
	public String procDate;
	/**
	 * 交易时间	VARCHAR2(14)	N
	 */
	@JsonProperty(value="TXN_TIME")
	public String txnTime;
	/**
	 * 交易描述	VARCHAR2(80)	Y
	 */
	@JsonProperty(value="TXN_DESC")
	public String txnDesc;
	/**
	 * 是否已入账标志	char(1)	Y 
	 */
	@JsonProperty(value="MATCH_IND")
	public Indicator matchInd;
	/**
	 * 未入账交易交易状态	char(1)	N 
	 */
	@JsonProperty(value="AUTH_TRANS_STATUS")
	public AuthTransStatus authTransStatus;
	/**
	 * 交易码
	 */
	@JsonProperty(value="POST_TXN_CD")
	public String txnCode;
	
	
	public String getTxnAmt() {
		return txnAmt;
	}
	public void setTxnAmt(String txnAmt) {
		this.txnAmt = txnAmt;
	}
	public String getProcDate() {
		return procDate;
	}
	public void setProcDate(String procDate) {
		this.procDate = procDate;
	}
	public String getTxnTime() {
		return txnTime;
	}
	public void setTxnTime(String txnTime) {
		this.txnTime = txnTime;
	}
	public String getTxnDesc() {
		return txnDesc;
	}
	public void setTxnDesc(String txnDesc) {
		this.txnDesc = txnDesc;
	}
	public Indicator getMatchInd() {
		return matchInd;
	}
	public void setMatchInd(Indicator matchInd) {
		this.matchInd = matchInd;
	}
	public AuthTransStatus getAuthTransStatus() {
		return authTransStatus;
	}
	public void setAuthTransStatus(AuthTransStatus authTransStatus) {
		this.authTransStatus = authTransStatus;
	}
	public String getTxnCode() {
		return txnCode;
	}
	public void setTxnCode(String txnCode) {
		this.txnCode = txnCode;
	}
}
