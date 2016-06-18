package com.sunline.ccs.service.msentity;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ppy.dictionary.entity.Check;

/**
 * 客户手机号修改接口接受报文
 * @author ymk
 *
 */
public class TNMCCustMobileReq extends MsRequestInfo implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * 客户号
	 */
	@Check(lengths=32,notEmpty=true)
	@JsonProperty(value="UUID")
	public String uuid;
	/**
	 * 手机号
	 */
	@Check(lengths=11,notEmpty=true)
	@JsonProperty(value="MOBILE")
	private String mobile;
	
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	
}
