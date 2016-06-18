package com.sunline.ccs.service.payEntity;

/**
 * 
  * @author  jjb
  * @date 创建时间：2015年8月15日 下午2:27:40 
  * @see 单笔代扣接口返回信息
 */
public class PayCutPaymentResp extends MainResp {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * 渠道流水
	 */
	private String channelSerial;
	/**
	 * 渠道日期
	 * YYYYMMDD
	 */
	private String channelDate;
	/**
	 * 交易金额
	 */
	private String transAmt;
	/**
	 * 支付状态
	 */
	private String status;
	/**
	 * 平台应答码
	 */
	private String errorCode;
	/**
	 * 平台应答信息
	 */
	private String errorMessage;
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
	public String getTransAmt() {
		return transAmt;
	}
	public void setTransAmt(String transAmt) {
		this.transAmt = transAmt;
	}
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
