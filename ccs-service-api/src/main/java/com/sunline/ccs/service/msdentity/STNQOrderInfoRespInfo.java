package com.sunline.ccs.service.msdentity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ppy.dictionary.entity.Check;
public class STNQOrderInfoRespInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 交易金额
	 */
	@Check(lengths=22,notEmpty=true)
	@JsonProperty(value="TXN_AMT")
	private BigDecimal amt;
	/**
	 * 交易状态
	 */
	@Check(lengths=1,notEmpty=true)
	@JsonProperty(value="TXN_STATUS")
	private String status;
	/**
	 * 失败原因
	 */
	@Check(lengths=64)
	@JsonProperty(value="FAILURE_MESSAGE")
	private String message;
	/**
	 * 交易时间
	 */
	@JsonProperty(value="TXN_TIME")
//	@JsonFormat(pattern ="yyyyMMddHHmmss")
	private String time;
	/**
	 * 支付方式
	 */
	@Check(lengths=1)
	@JsonProperty(value="PURPOSE")
	private String purpose;
	/**
	 * 银行名称
	 */
	@Check(lengths=32)
	@JsonProperty(value="BANK_NAME")
	private String bankName;
	
	/**
	 * 银行卡号
	 */
	@Check(lengths=32)
	@JsonProperty(value="BANK_CARD_NBR")
	private String bankCardNbr;
	
	
	public BigDecimal getAmt() {
		return amt;
	}
	public void setAmt(BigDecimal bigDecimal) {
		this.amt = bigDecimal;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getPurpose() {
		return purpose;
	}
	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}
	public String getBankName() {
		return bankName;
	}
	public void setBankName(String bankName) {
		this.bankName = bankName;
	}
	public String getBankCardNbr() {
		return bankCardNbr;
	}
	public void setBankCardNbr(String bankCardNbr) {
		this.bankCardNbr = bankCardNbr;
	}
	
}
