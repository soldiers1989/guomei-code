package com.sunline.ccs.service.msdentity;

import java.io.Serializable;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SMSentitySendReq implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 来源业务流水号
	 */
	@JsonProperty(value="sourceSerialNumber")
	private String sourceSerialNumber;

	/**
	 * 来源业务系统
	 */
	@JsonProperty(value="sourceBizSystem")
	private String sourceBizSystem;
	/**
	 * 来源业务类型
	 */
	@JsonProperty(value="sourceBizType")
	private String sourceBizType;
	
	/**
	 * 手机号码
	 */
	@JsonProperty(value="mobileNumber")
	private String mobileNumber;
	
	/**
	 * 短信模版参数变量,竖线"|"分割
	 */
	@JsonProperty(value="msgParam")
	private String msgParam;

	SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddhhmmssSSS"); 
	
	/**
	 * @param sourceSerialNumber 系统时间+随机数生成23位流水号
	 * @param sourceBizSystem
	 * @param sourceBizType
	 */
	public SMSentitySendReq() {
		super();
		this.sourceSerialNumber = sdf.format(new Date()) + new SecureRandom().nextInt(999999);
		this.sourceBizSystem = "ccs";
//		this.sourceBizType = "Core";
	}

	public String getSourceSerialNumber() {
		return sourceSerialNumber;
	}

	public void setSourceSerialNumber(String sourceSerialNumber) {
		this.sourceSerialNumber = sourceSerialNumber;
	}

	public String getSourceBizSystem() {
		return sourceBizSystem;
	}

	public void setSourceBizSystem(String sourceBizSystem) {
		this.sourceBizSystem = sourceBizSystem;
	}

	public String getSourceBizType() {
		return sourceBizType;
	}

	public void setSourceBizType(String sourceBizType) {
		this.sourceBizType = sourceBizType;
	}

	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

	public String getMsgParam() {
		return msgParam;
	}

	public void setMsgParam(String msgParam) {
		this.msgParam = msgParam;
	}
	
}
