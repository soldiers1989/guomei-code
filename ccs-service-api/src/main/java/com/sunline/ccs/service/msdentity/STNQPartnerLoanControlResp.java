package com.sunline.ccs.service.msdentity;

import java.io.Serializable;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ccs.service.msentity.MsResponseInfo;

@SuppressWarnings("serial")
public class STNQPartnerLoanControlResp extends MsResponseInfo implements Serializable{
	/**
	 * 合作方ID
	 */
	@JsonProperty(value="ACQ_ID")
	private String acqId;
	
	/**
	 * 担保方贷款总余额 
	 * @return
	 */
	@JsonProperty(value="TOTAL_BAL")
	private BigDecimal totalBal;
	
	public BigDecimal getTotalBal() {
		return totalBal;
	}

	public void setTotalBal(BigDecimal totalBal) {
		this.totalBal = totalBal;
	}
	
	public String getAcqId() {
		return acqId;
	}

	public void setAcqId(String acqId) {
		this.acqId = acqId;
	}
	
	
}
