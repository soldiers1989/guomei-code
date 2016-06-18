package com.sunline.ccs.service.payEntity;

/**
 * 
  * @author  zqx
  * @date 创建时间：2015年8月15日 下午4:05:23 
  * @see 单笔代付查询接口发送信息
 */
public class PaySinPaymentQueryReq extends MainReq{
	
	private static final long serialVersionUID = 1L;

	/**
	 * 渠道流水
	 */
	private String channelSerial;
	/**
	 * 渠道日期
	 */
	private String channelDate;
	
	public String getChannelSerial() {
		return channelSerial;
	}
	public void setChannelSerial(String channelSerial) {
		this.channelSerial = channelSerial;
	}
	public String getChannelDate() {
		return channelDate;
	}
	public void setChannelDate(String channelDate) {
		this.channelDate = channelDate;
	}
}
