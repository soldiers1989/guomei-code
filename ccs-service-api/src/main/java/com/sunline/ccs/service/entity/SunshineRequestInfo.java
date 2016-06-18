package com.sunline.ccs.service.entity;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ppy.dictionary.entity.Check;
import com.sunline.ppy.dictionary.enums.InputSource;

/**
 * 阳光交易接口
 * @author wangz
 *
 */
@SuppressWarnings("serial")
public abstract class SunshineRequestInfo implements Serializable{

	private String org;
	
	private String opId;
	
	private InputSource inputSource;
	
	private Date bizDate;
	
	private String requestTime;
	
	private String serviceSn;
	
	private String subTerminalType;
	
	private String acqId;

	/**
	 * 交易服务码
	 */
	@Check(lengths=32,notEmpty=true)
	@JsonProperty(value="SERVICE_ID")
	private String serviceId;
	
	public Date getBizDate() {
		return bizDate;
	}

	public void setBizDate(Date bizDate) {
		this.bizDate = bizDate;
	}

	public String getRequestTime() {
		return requestTime;
	}

	public void setRequestTime(String requestTime) {
		this.requestTime = requestTime;
	}

	public String getServiceSn() {
		return serviceSn;
	}

	public void setServiceSn(String serviceSn) {
		this.serviceSn = serviceSn;
	}

	public String getOrg() {
		return org;
	}

	public void setOrg(String org) {
		this.org = org;
	}

	public String getOpId() {
		return opId;
	}

	public void setOpId(String opId) {
		this.opId = opId;
	}

	public InputSource getInputSource() {
		return inputSource;
	}

	public void setInputSource(InputSource inputSource) {
		this.inputSource = inputSource;
	}



	public String getSubTerminalType() {
		return subTerminalType;
	}

	public void setSubTerminalType(String subTerminalType) {
		this.subTerminalType = subTerminalType;
	}

	public String getAcqId() {
		return acqId;
	}

	public void setAcqId(String acqId) {
		this.acqId = acqId;
	}

	public String getServiceId() {
		return serviceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}
	
}
