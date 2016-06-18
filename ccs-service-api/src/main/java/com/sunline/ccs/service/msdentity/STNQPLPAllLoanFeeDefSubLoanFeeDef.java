package com.sunline.ccs.service.msdentity;

import java.io.Serializable;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ark.support.meta.PropertyInfo;
import com.sunline.ccs.param.def.enums.LoanFeeDefStatus;

public class STNQPLPAllLoanFeeDefSubLoanFeeDef implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
	 * 分期子产品编号
	 */
	@PropertyInfo(name="子产品编号",length=8)
	@JsonProperty(value="LOAN_FEE_DEF_ID")
	public Integer loanFeeDefId;
	/**
	 * 分期期数
	 */
	@PropertyInfo(name="分期期数", length=2)
	@JsonProperty(value="LOAN_TERM")
	public Integer initTerm;

	/**
	 * 最小分期金额
	 */
	@PropertyInfo(name="最小允许分期金额", length=15, precision=2)
	@JsonProperty(value="MIN_AMT")
	public BigDecimal minAmount;
	
	/**
	 * 最大允许分期金额
	 */
	@PropertyInfo(name="最大允许分期金额", length=15, precision=2)
	@JsonProperty(value="MAX_AMT")
	public BigDecimal maxAmount;
	/**
     * 分期子产品状态
     */
	@PropertyInfo(name = "分期子产品状态", length = 1)
	@JsonProperty(value="LOAN_FEE_DEF_STATUS")
	public LoanFeeDefStatus loanFeeDefStatus;
	public Integer getLoanFeeDefId() {
		return loanFeeDefId;
	}
	public void setLoanFeeDefId(Integer loanFeeDefId) {
		this.loanFeeDefId = loanFeeDefId;
	}
	public Integer getInitTerm() {
		return initTerm;
	}
	public void setInitTerm(Integer initTerm) {
		this.initTerm = initTerm;
	}
	public BigDecimal getMinAmount() {
		return minAmount;
	}
	public void setMinAmount(BigDecimal minAmount) {
		this.minAmount = minAmount;
	}
	public BigDecimal getMaxAmount() {
		return maxAmount;
	}
	public void setMaxAmount(BigDecimal maxAmount) {
		this.maxAmount = maxAmount;
	}
	public LoanFeeDefStatus getLoanFeeDefStatus() {
		return loanFeeDefStatus;
	}
	public void setLoanFeeDefStatus(LoanFeeDefStatus loanFeeDefStatus) {
		this.loanFeeDefStatus = loanFeeDefStatus;
	}
	
}
