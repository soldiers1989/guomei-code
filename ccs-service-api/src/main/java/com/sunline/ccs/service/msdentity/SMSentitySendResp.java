package com.sunline.ccs.service.msdentity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 短信平台返回信息
 * @author zhengjf
 *
 */
public class SMSentitySendResp {
	
	@JsonProperty(value="code")
	private String code;
	@JsonProperty(value="message")
	private String message;
	@JsonProperty(value="data")
	private String data;
	
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	
}