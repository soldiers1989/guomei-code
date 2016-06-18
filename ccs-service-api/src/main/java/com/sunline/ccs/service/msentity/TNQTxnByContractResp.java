package com.sunline.ccs.service.msentity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TNQTxnByContractResp extends MsResponseInfo {
	private static final long serialVersionUID = 7762498374688856358L;
	/**
	 * 合同号	VARCHAR2(32)	Y
	 */
	@JsonProperty(value="CONTR_NBR")
	public String contrNbr;
	/**
	 * 页大小	VARCHAR2(10)	Y
	 */
	@JsonProperty(value="PAGE_SIZE")
	public Integer pageSize;
	/**
	 * 交易总条数	VARCHAR2(20)	N
	 */
	@JsonProperty(value="TXN_COUNT")
	public Long txnCount;
	/**
	 * TXN_LIST
	 */
	@JsonProperty(value="TXN_LIST")
	public List<TNQContractTxnQueryRespInfo> txnList;
	public String getContrNbr() {
		return contrNbr;
	}
	public void setContrNbr(String contrNbr) {
		this.contrNbr = contrNbr;
	}
	public Integer getPageSize() {
		return pageSize;
	}
	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}
	public Long getTxnCount() {
		return txnCount;
	}
	public void setTxnCount(Long txnCount) {
		this.txnCount = txnCount;
	}
	public List<TNQContractTxnQueryRespInfo> getTxnList() {
		return txnList;
	}
	public void setTxnList(List<TNQContractTxnQueryRespInfo> txnList) {
		this.txnList = txnList;
	}
	
	
}
