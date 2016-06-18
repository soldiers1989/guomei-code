package com.sunline.ccs.service.msentity;

import java.io.Serializable;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ppy.dictionary.entity.Check;

/**
 *  去哪儿还款计划结果通知
 *  还款计划明细
 *  
 * @author Mr.L
 *
 */

public class TNMTrustLoanSchedReqSubPlan implements Serializable{
	
	private static final long serialVersionUID = 1L;

	/**
	 * 分期数
	 */
	@Check(lengths=32,notEmpty=true)
	@JsonProperty(value="TERM_NO")
	public String termNo;
	

	/**
	 * 最后还款日期
	 */
	@Check(lengths=14,notEmpty=true)
	@JsonProperty(value="DUE_DATE")
	public String dueDate;
	
	/**
	 * 单期本金
	 * 
	 */
	@Check(lengths=18,notEmpty=true)
	@JsonProperty(value="CAPITAL_AMOUNT")
	public BigDecimal capitalAmount;
	
	/**
	 * 单期手续费
	 * 
	 */
	@Check(lengths=18,notEmpty=true)
	@JsonProperty(value="FEE_AMOUNT")
	public BigDecimal feeAmount;
	
	/**
	 *单期逾期罚金
	 * 
	 */
	@Check(lengths=18,notEmpty=true)
	@JsonProperty(value="OVERDUE_FINE")
	public BigDecimal overdueFine;

	/**
	 *是否逾期
	 * 
	 */
	@Check(lengths=2,notEmpty=true)
	@JsonProperty(value="IS_OVERDUE")
	public String isOverdue;
	
	/**
	 *单期已付本金
	 * 
	 */
	@Check(lengths=18,notEmpty=true)
	@JsonProperty(value="PAID_CAPITAL")
	public BigDecimal paidCapital;
	
	/**
	 *单期已付手续费
	 * 
	 */
	@Check(lengths=18,notEmpty=true)
	@JsonProperty(value="PAID_FEE")
	public BigDecimal paidFee;
	
	
	/**
	 *单期已付逾期罚金
	 * 
	 */
	@Check(lengths=18,notEmpty=true)
	@JsonProperty(value="PAID_FINE")
	public BigDecimal paidFine;
	
	/**
	 * 最近付款日期
	 */
	@Check(lengths=14,notEmpty=false)
	@JsonProperty(value="PAID_DATE")
	public String paidDate;
	/**
	 * 还款状态
	 */
	@Check(lengths=8,notEmpty=true)
	@JsonProperty(value="STATUS")
	public String status;
	public String getTermNo() {
		return termNo;
	}
	public void setTermNo(String termNo) {
		this.termNo = termNo;
	}
	public String getDueDate() {
		return dueDate;
	}
	public void setDueDate(String dueDate) {
		this.dueDate = dueDate;
	}
	public BigDecimal getCapitalAmount() {
		return capitalAmount;
	}
	public void setCapitalAmount(BigDecimal capitalAmount) {
		this.capitalAmount = capitalAmount;
	}
	public BigDecimal getFeeAmount() {
		return feeAmount;
	}
	public void setFeeAmount(BigDecimal feeAmount) {
		this.feeAmount = feeAmount;
	}
	public BigDecimal getOverdueFine() {
		return overdueFine;
	}
	public void setOverdueFine(BigDecimal overdueFine) {
		this.overdueFine = overdueFine;
	}
	public String getIsOverdue() {
		return isOverdue;
	}
	public void setIsOverdue(String isOverdue) {
		this.isOverdue = isOverdue;
	}
	public BigDecimal getPaidCapital() {
		return paidCapital;
	}
	public void setPaidCapital(BigDecimal paidCapital) {
		this.paidCapital = paidCapital;
	}
	public BigDecimal getPaidFee() {
		return paidFee;
	}
	public void setPaidFee(BigDecimal paidFee) {
		this.paidFee = paidFee;
	}
	public BigDecimal getPaidFine() {
		return paidFine;
	}
	public void setPaidFine(BigDecimal paidFine) {
		this.paidFine = paidFine;
	}
	public String getPaidDate() {
		return paidDate;
	}
	public void setPaidDate(String paidDate) {
		this.paidDate = paidDate;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
	
	
}
