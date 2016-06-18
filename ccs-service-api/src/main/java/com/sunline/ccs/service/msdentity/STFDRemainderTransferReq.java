package com.sunline.ccs.service.msdentity;

import java.io.Serializable;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ccs.service.msentity.MsRequestInfo;
import com.sunline.ppy.dictionary.entity.Check;

public class STFDRemainderTransferReq extends MsRequestInfo implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
	 * 合同号
	 */
	@Check(lengths=22,notEmpty=true)
	@JsonProperty(value="CONTR_NBR")
	private String contrNo;
	
	/**
	 * 转移额度
	 */
	@Check(lengths=15)
	@JsonProperty(value="TRANSFER_LMT")
	private BigDecimal  transferLmt;
	
	/**
	 * 交易类型
	 */
	@Check(lengths=1)
	@JsonProperty(value="TYPE")
	private String type;
	
	public String getContrNo() {
		return contrNo;
	}

	public void setContrNo(String contrNo) {
		this.contrNo = contrNo;
	}
	/**
	 * 转移额度
	 */
	public BigDecimal getTransferLmt() {
		return transferLmt;
	}
	/**
	 * 转移额度
	 */
	public void setTransferLmt(BigDecimal transferLmt) {
		this.transferLmt = transferLmt;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
}
