package com.sunline.ccs.service.payEntity;

import java.io.Serializable;

public class CommResp implements Serializable {
	
	private static final long serialVersionUID = 1L;
	/**
	 * 应答码
	 */
	private String code;
	/**
	 * 应答描述
	 */
	private String message;
	
	private String errors;
	/**
	 * 响应结果描述
	 */
	private Object data;
	
	public String getErrors() {
		return errors;
	}
	
	public void setErrors(String errors) {
		this.errors = errors;
	}

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
		
	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}

}
