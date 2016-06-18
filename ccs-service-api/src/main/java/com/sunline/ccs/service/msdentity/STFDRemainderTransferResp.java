package com.sunline.ccs.service.msdentity;

import java.io.Serializable;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ccs.service.msentity.MsResponseInfo;
import com.sunline.ppy.dictionary.entity.Check;

public class STFDRemainderTransferResp extends MsResponseInfo implements Serializable {

private static final long serialVersionUID = 1L;
	
	/**
	 * 合同号
	 */
	@Check(lengths=22,notEmpty=true)
	@JsonProperty(value="CONTR_NBR")
	private String contrNo;
	
	/**
	 * 最大可转移额度
	 */
	@Check(lengths=15)
	@JsonProperty(value="TRANSFER_AMT")
	private BigDecimal  transferAmt;
	

	public String getContrNo() {
		return contrNo;
	}

	public void setContrNo(String contrNo) {
		this.contrNo = contrNo;
	}

	public BigDecimal getTransferAmt() {
		return transferAmt;
	}

	public void setTransferAmt(BigDecimal transferAmt) {
		this.transferAmt = transferAmt;
	}
	
}
