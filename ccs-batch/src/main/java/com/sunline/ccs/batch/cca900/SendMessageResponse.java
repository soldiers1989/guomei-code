package com.sunline.ccs.batch.cca900;

import java.io.Serializable;

@SuppressWarnings("serial")
public class SendMessageResponse implements Serializable{
	
	/**
	 * 处理状态
	 */
	private String code;
	
	/**
	 * 状态描述
	 */
	private String message;
	
	/**
	 * 结果明细
	 */
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
