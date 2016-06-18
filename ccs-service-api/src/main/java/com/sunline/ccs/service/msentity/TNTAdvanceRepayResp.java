package com.sunline.ccs.service.msentity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ppy.dictionary.entity.Check;

/**
 * 
 * 兜底 提前还款试算/申请
 * @author zhengjf
 *
 */
public class TNTAdvanceRepayResp extends MsResponseInfo {

	private static final long serialVersionUID = 1L;
	
	/**
	 * 借据号
	 */
	@Check(lengths=32,notEmpty=true)
	@JsonProperty(value="DUE_BILL_NO")
	private String dueBillNo;
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
	
	public String getDueBillNo() {
		return dueBillNo;
	}
	public void setDueBillNo(String dueBillNo) {
		this.dueBillNo = dueBillNo;
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
