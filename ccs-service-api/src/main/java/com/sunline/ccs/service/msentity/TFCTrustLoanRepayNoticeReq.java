package com.sunline.ccs.service.msentity;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ppy.dictionary.entity.Check;

/**
 * 还款结果通知接口（拿去花）
 * @author qinshanbin
 *
 */
public class TFCTrustLoanRepayNoticeReq extends MsRequestInfo {
	private static final long serialVersionUID = 1L;
	/**
	 * 合同号
	 */
	@Check(lengths=32,notEmpty=true)
	@JsonProperty(value="CONTR_NBR")
	public String contrNo;
	/**
	 * 贷款流水号
	 */
	@Check(lengths=64,notEmpty=true)
	@JsonProperty(value="LOAN_NO")
	public String loanNo;
	/**
	 * 还款流水号
	 */
	@Check(lengths=64,notEmpty=true)
	@JsonProperty(value="REPAYMENT_NO")
	public String reNo;
	/**
	 * 还款金额
	 */
	@Check(lengths=15,notEmpty=true,regular="^(([1-9]\\d*)|0)(\\.(\\d){1,2})?$")
	@JsonProperty(value="AMOUNT")
	public BigDecimal transAmt;
	/**
	 * 还款期数
	 */
	@Check(lengths=2,notEmpty=true)
	@JsonProperty(value="PAID_TERM")
	public Integer cdTerms;
	/**
	 * 已还本金
	 */
	@Check(lengths=15,notEmpty=true,regular="^(([1-9]\\d*)|0)(\\.(\\d){1,2})?$")
	@JsonProperty(value="PAID_PRINCIPAL")
	public BigDecimal principal;
	/**
	 * 已还分期服务费
	 */
	@Check(lengths=15,notEmpty=true,regular="^(([1-9]\\d*)|0)(\\.(\\d){1,2})?$")
	@JsonProperty(value="PAID_FEE_AMOUNT")
	public BigDecimal feeAmt;
	/**
	 * 已还罚金
	 */
	@Check(lengths=15,notEmpty=true,regular="^(([1-9]\\d*)|0)(\\.(\\d){1,2})?$")
	@JsonProperty(value="PAID_PENALTY")
	public BigDecimal penalty;
	/**
	 * 还款类型
	 */
	@Check(lengths=20,notEmpty=true)
	@JsonProperty(value="REPAY_TYPE")
	public String repayType;
	
	public String getContrNo() {
		return contrNo;
	}
	public void setContrNo(String contrNo) {
		this.contrNo = contrNo;
	}
	public String getLoanNo() {
		return loanNo;
	}
	public void setLoanNo(String loanNo) {
		this.loanNo = loanNo;
	}
	public String getReNo() {
		return reNo;
	}
	public void setReNo(String reNo) {
		this.reNo = reNo;
	}
	public BigDecimal getTransAmt() {
		return transAmt;
	}
	public void setTransAmt(BigDecimal transAmt) {
		this.transAmt = transAmt;
	}
	public Integer getCdTerms() {
		return cdTerms;
	}
	public void setCdTerms(Integer cdTerms) {
		this.cdTerms = cdTerms;
	}
	public BigDecimal getPrincipal() {
		return principal;
	}
	public void setPrincipal(BigDecimal principal) {
		this.principal = principal;
	}
	public BigDecimal getFeeAmt() {
		return feeAmt;
	}
	public void setFeeAmt(BigDecimal feeAmt) {
		this.feeAmt = feeAmt;
	}
	public BigDecimal getPenalty() {
		return penalty;
	}
	public void setPenalty(BigDecimal penalty) {
		this.penalty = penalty;
	}
	public String getRepayType() {
		return repayType;
	}
	public void setRepayType(String repayType) {
		this.repayType = repayType;
	}
}
