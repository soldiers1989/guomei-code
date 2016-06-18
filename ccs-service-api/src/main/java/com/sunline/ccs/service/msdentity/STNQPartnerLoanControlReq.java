package com.sunline.ccs.service.msdentity;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ccs.service.msentity.MsRequestInfo;
import com.sunline.ppy.dictionary.entity.Check;

@SuppressWarnings("serial")
public class STNQPartnerLoanControlReq extends MsRequestInfo implements Serializable{
	/**
	 * 合作方ID
	 */
	@Check(lengths=8,notEmpty=true)
	@JsonProperty(value="ACQ_ID")
	
	private String asqId;

	public String getAsqId() {
		return asqId;
	}

	public void setAsqId(String asqId) {
		this.asqId = asqId;
	}

	
	
	
}
