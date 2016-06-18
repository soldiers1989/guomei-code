package com.sunline.ccs.service.payEntity;


/**
 * 
  * @author  jjb
  * @date 创建时间：2015年8月15日 下午2:30:15 
  * @see 单笔代扣接口 发送信息
 */
public class PayCutPaymentReq extends MainReq{

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
	 * 开户行号
	 */
	private String openBankId;
	/**
	 * 卡折标志
	 */
	private String cardType;
	/**
	 * 卡号/折号
	 */
	private String cardNo;
	/**
	 * 持卡人姓名
	 */
	private String usrName;
	/**
	 * 证件类型
	 */
	private String certType;
	/**
	 * 证件号
	 */
	private String certId;
	/**
	 * 币种
	 */
	private String curyId;
	/**
	 * 用途
	 */
	private String purpose;
	/**
	 * 私有域
	 */
	private String priv1;
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
	public String getOpenBankId() {
		return openBankId;
	}
	public void setOpenBankId(String openBankId) {
		this.openBankId = openBankId;
	}
	public String getCardType() {
		return cardType;
	}
	public void setCardType(String cardType) {
		this.cardType = cardType;
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
	public String getCertType() {
		return certType;
	}
	public void setCertType(String certType) {
		this.certType = certType;
	}
	public String getCertId() {
		return certId;
	}
	public void setCertId(String certId) {
		this.certId = certId;
	}
	public String getCuryId() {
		return curyId;
	}
	public void setCuryId(String curyId) {
		this.curyId = curyId;
	}
	public String getPurpose() {
		return purpose;
	}
	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}
	public String getPriv1() {
		return priv1;
	}
	public void setPriv1(String priv1) {
		this.priv1 = priv1;
	}
}
