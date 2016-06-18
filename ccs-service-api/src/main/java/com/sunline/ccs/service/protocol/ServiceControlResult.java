package com.sunline.ccs.service.protocol;

import java.io.Serializable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * 服务控制返回结果
 * 
* @author fanghj
 * 
 */
public class ServiceControlResult implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// true 为可以通过   false 为不通过
	private boolean pass = true;
	
	private String returnMessage;
	private String returnCode;

	public boolean isPass() {
		return pass;
	}

	public boolean isNotPass() {
		return !pass;
	}

	public void setPass(boolean pass) {
		this.pass = pass;
	}

	public String getReturnMessage() {
		return returnMessage;
	}

	public void setReturnMessage(String returnMessage) {
		this.returnMessage = returnMessage;
	}

	public String getReturnCode() {
		return returnCode;
	}

	public void setReturnCode(String returnCode) {
		this.returnCode = returnCode;
	}

	public String toString() {
		return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
