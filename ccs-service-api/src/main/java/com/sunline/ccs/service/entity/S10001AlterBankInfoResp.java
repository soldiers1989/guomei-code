package com.sunline.ccs.service.entity;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 客户银行卡信息修改接口返回报文
 * @author jjb
 *
 */
public class S10001AlterBankInfoResp extends SunshineResponseInfo implements Serializable {
	
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
