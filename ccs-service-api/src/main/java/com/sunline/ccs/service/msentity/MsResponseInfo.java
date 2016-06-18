package com.sunline.ccs.service.msentity;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 交易响应体
 * @author wangz
 *
 */
@SuppressWarnings("serial")
public abstract class MsResponseInfo implements Serializable{

	/**
	 * 支付状态
	 */
	@JsonIgnore
	private String status;
	/**
	 * 平台应答码
	 */
	@JsonIgnore
	private String errorCode;
	/**
	 * 平台应答信息
	 */
	@JsonIgnore
	private String errorMessage;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

}
