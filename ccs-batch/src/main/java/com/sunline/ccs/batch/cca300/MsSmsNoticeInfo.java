package com.sunline.ccs.batch.cca300;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MsSmsNoticeInfo implements Serializable{
	
	private static final long serialVersionUID = 1L;

	@JsonProperty(value="sourceBatchNumber")
	private String sourceBatchNumber;
	
//	@JsonProperty(value="sourceBizSystem")
//	private String sourceBizSystem;
//	
//	@JsonProperty(value="sourceBizType")
//	private String sourceBizType;
	
	@JsonProperty(value="sendFileId")
	private String sendFileId;
	
	public MsSmsNoticeInfo(){
//		this.sourceBizSystem = "accounting";
	}
	
	public String getSourceBatchNumber() {
		return sourceBatchNumber;
	}
	public void setSourceBatchNumber(String sourceBatchNumber) {
		this.sourceBatchNumber = sourceBatchNumber;
	}
//	public String getSourceBizSystem() {
//		return sourceBizSystem;
//	}
//	public void setSourceBizSystem(String sourceBizSystem) {
//		this.sourceBizSystem = sourceBizSystem;
//	}
//	public String getSourceBizType() {
//		return sourceBizType;
//	}
//	public void setSourceBizType(String sourceBizType) {
//		this.sourceBizType = sourceBizType;
//	}
	public String getSendFileId() {
		return sendFileId;
	}
	public void setSendFileId(String sendFileId) {
		this.sendFileId = sendFileId;
	}
	
}
