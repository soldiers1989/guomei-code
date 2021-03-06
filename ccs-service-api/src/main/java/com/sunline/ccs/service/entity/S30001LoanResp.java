package com.sunline.ccs.service.entity;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 放款申请返回报文
 * @author jjb
 *
 */
public class S30001LoanResp extends SunshineResponseInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 * 保单号
	 */
	@JsonProperty(value="GUARANTYID")
	private String guarantyid;

	public String getGuarantyid() {
		return guarantyid;
	}

	public void setGuarantyid(String guarantyid) {
		this.guarantyid = guarantyid;
	}

}
