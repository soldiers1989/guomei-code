package com.sunline.ccs.service.entity;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
/**
 * 预约提前还款返回报文
 * @author jjb
 *
 */
public class S11001BookingResp extends SunshineResponseInfo implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * 保单号
	 */
	@JsonProperty(value="GUARANTYID")
	private String guarantyid;
	/**
	 * 总金额
	 */
	@JsonProperty(value="AMOUNT")
	private String amount;
	/**
	 * 保费
	 */
	@JsonProperty(value="PREMIUM")
	private String premium;
	/**
	 * 手续费
	 */
	@JsonProperty(value="POUNDAGE")
	private String poundage;
	/**
	 * 利息
	 */
	@JsonProperty(value="INTEREST")
	private String interest;
	/**
	 * 本金
	 */
	@JsonProperty(value="PRINCIPAL")
	private String principal;
	
	public String getGuarantyid() {
		return guarantyid;
	}
	public void setGuarantyid(String guarantyid) {
		this.guarantyid = guarantyid;
	}
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public String getPremium() {
		return premium;
	}
	public void setPremium(String premium) {
		this.premium = premium;
	}
	public String getPoundage() {
		return poundage;
	}
	public void setPoundage(String poundage) {
		this.poundage = poundage;
	}
	public String getInterest() {
		return interest;
	}
	public void setInterest(String interest) {
		this.interest = interest;
	}
	public String getPrincipal() {
		return principal;
	}
	public void setPrincipal(String principal) {
		this.principal = principal;
	}
	
}
