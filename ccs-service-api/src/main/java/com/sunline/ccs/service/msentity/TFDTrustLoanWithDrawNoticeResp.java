package com.sunline.ccs.service.msentity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ppy.dictionary.entity.Check;

public class TFDTrustLoanWithDrawNoticeResp extends MsResponseInfo {
	private static final long serialVersionUID = 1L;
	
	/**
	 * 合同号
	 */
	@Check(lengths=32,notEmpty=true)
	@JsonProperty(value="CONTR_NBR")
	public String contractNo;

	public String getContractNo() {
		return contractNo;
	}

	public void setContractNo(String contractNo) {
		this.contractNo = contractNo;
	}
	
}
