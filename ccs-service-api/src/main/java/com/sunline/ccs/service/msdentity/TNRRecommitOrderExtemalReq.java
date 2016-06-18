package com.sunline.ccs.service.msdentity;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ccs.service.msentity.MsRequestInfo;
import com.sunline.ppy.dictionary.entity.Check;
@SuppressWarnings("serial")
public class TNRRecommitOrderExtemalReq extends MsRequestInfo implements Serializable{
	
	/**
	 * 合同号
	 */
	@Check(lengths=32,notEmpty=true)
	@JsonProperty(value="CONTR_NBR")
	private String contractNo;
	
	/**
	 * 借据号
	 * 
	 */
	@Check(lengths=32,notEmpty=true)
	@JsonProperty(value="DUE_BILL_NO")
	private String dueBillNo;

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
