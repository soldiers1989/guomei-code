package com.sunline.ccs.service.payEntity;

import java.io.Serializable;

@SuppressWarnings("serial")
public abstract class MainReq implements Serializable{

	/**
	 * 渠道标识
	 * 接入系统ID 主业务：0 长亮核算：1
	 */
	private String channelId;
	/**
	 * 渠道业务标识
	 * 应答描述
	 * 接入系统业务编码 实时或批量代扣：0  实时或批量代付：1
	 */
	private String channelBizCode;
	public String getChannelId() {
		return channelId;
	}
	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}
	public String getChannelBizCode() {
		return channelBizCode;
	}
	public void setChannelBizCode(String channelBizCode) {
		this.channelBizCode = channelBizCode;
	}
	
}
