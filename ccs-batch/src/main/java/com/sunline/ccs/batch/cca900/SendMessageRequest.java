package com.sunline.ccs.batch.cca900;

import java.io.Serializable;

@SuppressWarnings(value="serial")
public class SendMessageRequest implements Serializable{
	
	/**
	 * 业务系统代码
	 */
	private String businessId;
	
	/**
	 * 库名
	 */
//	private String dbName;
	
	
	public SendMessageRequest(){
		this.businessId = "CCS_AAS";
//		this.dbName = "ccs";
	}

	public String getBusinessId() {
		return businessId;
	}

	public void setBusinessId(String businessId) {
		this.businessId = businessId;
	}

}
