package com.sunline.ccs.service.msentity;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ppy.dictionary.entity.Check;

public class TNQLoanSetupQueryResp extends MsResponseInfo implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * 合同号
	 */
	@Check(lengths=32,notEmpty=true)
	@JsonProperty(value="CONTR_NBR")
	public String contractNo;
	
	/**
	 * 借据号
	 */
	@Check(lengths=22,notEmpty=true)
	@JsonProperty(value="DUE_BILL_NO")
	public String dueBillNo;

	public String getContractNo() {
		return contractNo;
	}

	public void setContractNo(String contractNo) {
		this.contractNo = contractNo;
	}

	public String getDueBillNo() {
		return dueBillNo;
	}

	public void setDueBillNo(String dueBillNo) {
		this.dueBillNo = dueBillNo;
	}

}
