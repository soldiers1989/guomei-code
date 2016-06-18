package com.sunline.ccs.service.payEntity;

/**
 * 
  * @author  jjb
  * @date 创建时间：2015年8月15日 下午4:05:23 
  * @see 单笔代付接口发送信息
 */
public class PaySinPaymentReq extends MainReq{
	
	private static final long serialVersionUID = 1L;

	/**
	 * 渠道流水
	 */
	private String channelSerial;
	/**
	 * 渠道日期
	 */
	private String channelDate;
	/**
	 * 交易金额
	 */
	private String transAmt;
	/**
	 * 收款账号
	 */
	private String cardNo;
	/**
	 * 收款人姓名
	 */
	private String usrName;
	/**
	 * 开户银行
	 */
	private String openBank;
	/**
	 * 省份
	 */
	private String prov;
	/**
	 * 城市
	 */
	private String city;
	/**
	 * 用途
	 */
	private String purpose;
	/**
	 * 支行
	 */
	private String subBank;
	/**
	 * 付款标志
	 */
	private String flag;
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
	public String getCardNo() {
		return cardNo;
	}
	public void setCardNo(String cardNo) {
		this.cardNo = cardNo;
	}
	public String getUsrName() {
		return usrName;
	}
	public void setUsrName(String usrName) {
		this.usrName = usrName;
	}
	public String getOpenBank() {
		return openBank;
	}
	public void setOpenBank(String openBank) {
		this.openBank = openBank;
	}
	public String getProv() {
		return prov;
	}
	public void setProv(String prov) {
		this.prov = prov;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getPurpose() {
		return purpose;
	}
	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}
	public String getSubBank() {
		return subBank;
	}
	public void setSubBank(String subBank) {
		this.subBank = subBank;
	}
	public String getFlag() {
		return flag;
	}
	public void setFlag(String flag) {
		this.flag = flag;
	}
		
}
