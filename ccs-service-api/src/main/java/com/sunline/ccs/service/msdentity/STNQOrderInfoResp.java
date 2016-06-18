package com.sunline.ccs.service.msdentity;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ccs.service.msentity.MsResponseInfo;
import com.sunline.ppy.dictionary.entity.Check;
@SuppressWarnings("serial")
public class STNQOrderInfoResp extends MsResponseInfo implements Serializable {
	/**
	 * 合同号
	 */
	@Check(lengths=22,notEmpty=true)
	@JsonProperty(value="CONTR_NBR")
	private String contractNo;
	/**
	 * 页大小
	 */
	@Check(lengths=10,notEmpty=true,isNumber=true)
	@JsonProperty(value="PAGE_SIZE")
	private int pagesize;
	/**
	 * 交易总条数
	 */
	@Check(lengths=20,isNumber=true)
	@JsonProperty(value="TXN_COUNT")
	private long txncount;
	
	/**
	 * TXN_LIST订单信息
	 */
	@JsonProperty(value="TXN_LIST")
	private List<STNQOrderInfoRespInfo> txnlist;

	public String getContractNo() {
		return contractNo;
	}

	public void setContractNo(String contractNo) {
		this.contractNo = contractNo;
	}

	public int getPagesize() {
		return pagesize;
	}

	public void setPagesize(int pagesize) {
		this.pagesize = pagesize;
	}

	public long getTxncount() {
		return txncount;
	}

	public void setTxncount(long txncount) {
		this.txncount = txncount;
	}

	public List<STNQOrderInfoRespInfo> getTxnlist() {
		return txnlist;
	}

	public void setTxnlist(List<STNQOrderInfoRespInfo> txnlist) {
		this.txnlist = txnlist;
	}
	
	
}
