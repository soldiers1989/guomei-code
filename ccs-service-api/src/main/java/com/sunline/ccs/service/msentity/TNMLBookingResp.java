package com.sunline.ccs.service.msentity;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ppy.dictionary.entity.Check;
/**
 * 预约提前还款返回报文
 * @author jjb
 *
 */
public class TNMLBookingResp extends MsResponseInfo implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * 借据号
	 */
	@Check(lengths=32,notEmpty=true)
	@JsonProperty(value="DUEBILLNO")
	public String dueBillNo;
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
	/**
	 * 寿险计划包
	 */
	@JsonProperty(value="LIFEINSU")
	private String lifeInsu;
	/**
	 * 印花税
	 */
	@JsonProperty(value="STAMP")
	private String stamp;
	/**
	 * 代收服务费
	 */
	@JsonProperty(value="REPLACESVC")
	private String replaceSvc;
	/**
	 * 贷款服务费
	 */
	@JsonProperty(value="LOANTERMFEE")
	private String loanTermFee;
	/**
	 * 分期手续费
	 */
	@JsonProperty(value="LOANTERMSVC")
	private String loanTermSvc;
	
	/**
	 * 趸交费
	 */
	@JsonProperty(value="PREMIUMAMT")
	private String premiumAmt;
	
	/**
	 * 未匹配金额
	 */
	@JsonProperty(value="MEMOAMT")
	private String memoAmt;
	/**
	 * 代收提前还款手续费
	 */
	@JsonProperty(value="REPLACEPREPAYFEE")
	private String replacePrepayFee;
	/**
	 * 代收罚金
	 */
	@JsonProperty(value="REPLACEMULCT")
	private String replaceMulct;
	/**
	 * 代收罚息
	 */
	@JsonProperty(value="REPLACEPENALTY")
	private String replacePenalty;
	/**
	 * 溢缴款
	 */
	@JsonProperty(value="DEPOSIT")
	private String deposit;
	/**
	 * 代收滞纳金
	 */
	@JsonProperty(value="REPLACELPC")
	private String replaceLpc;
	
	public String getReplaceLpc() {
		return replaceLpc;
	}
	public void setReplaceLpc(String replaceLpc) {
		this.replaceLpc = replaceLpc;
	}
	public String getLoanTermSvc() {
		return loanTermSvc;
	}
	public void setLoanTermSvc(String loanTermSvc) {
		this.loanTermSvc = loanTermSvc;
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
	public String getDueBillNo() {
		return dueBillNo;
	}
	public void setDueBillNo(String dueBillNo) {
		this.dueBillNo = dueBillNo;
	}
	public String getLifeInsu() {
		return lifeInsu;
	}
	public void setLifeInsu(String lifeInsu) {
		this.lifeInsu = lifeInsu;
	}
	public String getStamp() {
		return stamp;
	}
	public void setStamp(String stamp) {
		this.stamp = stamp;
	}
	public String getReplaceSvc() {
		return replaceSvc;
	}
	public void setReplaceSvc(String replaceSvc) {
		this.replaceSvc = replaceSvc;
	}
	public String getLoanTermFee() {
		return loanTermFee;
	}
	public void setLoanTermFee(String loanTermFee) {
		this.loanTermFee = loanTermFee;
	}
	public String getPremiumAmt() {
		return premiumAmt;
	}
	public void setPremiumAmt(String premiumAmt) {
		this.premiumAmt = premiumAmt;
	}
	public String getMemoAmt() {
		return memoAmt;
	}
	public void setMemoAmt(String memoAmt) {
		this.memoAmt = memoAmt;
	}
	public String getReplacePrepayFee() {
		return replacePrepayFee;
	}
	public void setReplacePrepayFee(String replacePrepayFee) {
		this.replacePrepayFee = replacePrepayFee;
	}
	public String getReplaceMulct() {
		return replaceMulct;
	}
	public void setReplaceMulct(String replaceMulct) {
		this.replaceMulct = replaceMulct;
	}
	public String getReplacePenalty() {
		return replacePenalty;
	}
	public void setReplacePenalty(String replacePenalty) {
		this.replacePenalty = replacePenalty;
	}
	public String getDeposit() {
		return deposit;
	}
	public void setDeposit(String deposit) {
		this.deposit = deposit;
	}
}
