package com.sunline.ccs.service.msdentity;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ccs.service.msentity.MsRequestInfo;
import com.sunline.ppy.dictionary.entity.Check;
@SuppressWarnings("serial")
public class STNQPrepayPkgQueryReq extends MsRequestInfo implements Serializable {
	
	/**
	 * 合同号
	 */
	@Check(lengths=22,notEmpty=true)
	@JsonProperty(value="CONTR_NBR")
	private String contractNo;

	public String getContractNo() {
		return contractNo;
	}

	public void setContractNo(String contractNo) {
		this.contractNo = contractNo;
	}

}
