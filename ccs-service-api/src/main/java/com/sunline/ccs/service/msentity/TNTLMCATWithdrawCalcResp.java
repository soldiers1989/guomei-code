package com.sunline.ccs.service.msentity;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TNTLMCATWithdrawCalcResp extends MsResponseInfo {
	private static final long serialVersionUID = 4240770335652266689L;
	/**
	 * 合同号	VARCHAR2(32)	Y
	 */
	@JsonProperty(value="CONTR_NBR")
	public String contrNbr;
	/**
	 * 合同额度	DECIMAL(15,2)   Y
	 */
	@JsonProperty(value="CONTR_LMT")
	public BigDecimal contrLmt;
	
	/**
	 * 可用额度	DECIMAL(15,2)	Y
	 */
	@JsonProperty(value="CONTRA_REMAIN")
	public BigDecimal contraRemain;
	
	/**
	 * 还款日期	VARCHAR2(8)	Y
	 */
	@JsonProperty(value="PMT_DUE_DATE")
	public String pmtDueDate;
	
	/**
	 * 提现手续费	DECIMAL(15,2)	Y
	 */
	@JsonProperty(value="CASH_CHARGE")
	public BigDecimal cashCharge;
	/**
	 * 该笔提现在下一账单日的应还款额	VARCHAR2(32)	Y
	 */
	@JsonProperty(value="WITHDRAW_QUAL_GRACE_BAL")
	public BigDecimal withdrawQualGraceBal;
	
	public String getContrNbr() {
		return contrNbr;
	}
	public void setContrNbr(String contrNbr) {
		this.contrNbr = contrNbr;
	}
	public BigDecimal getWithdrawQualGraceBal() {
		return withdrawQualGraceBal;
	}
	public void setWithdrawQualGraceBal(BigDecimal withdrawQualGraceBal) {
		this.withdrawQualGraceBal = withdrawQualGraceBal;
	}
	/**
	 * @return the contraRemain
	 */
	public BigDecimal getContraRemain() {
		return contraRemain;
	}
	/**
	 * @return the contrLmt
	 */
	public BigDecimal getContrLmt() {
		return contrLmt;
	}
	/**
	 * @param contrLmt the contrLmt to set
	 */
	public void setContrLmt(BigDecimal contrLmt) {
		this.contrLmt = contrLmt;
	}
	/**
	 * @param contraRemain the contraRemain to set
	 */
	public void setContraRemain(BigDecimal contraRemain) {
		this.contraRemain = contraRemain;
	}
	/**
	 * @return the pmtDueDate
	 */
	public String getPmtDueDate() {
		return pmtDueDate;
	}
	/**
	 * @param pmtDueDate the pmtDueDate to set
	 */
	public void setPmtDueDate(String pmtDueDate) {
		this.pmtDueDate = pmtDueDate;
	}
	/**
	 * @return the cashCharge
	 */
	public BigDecimal getCashCharge() {
		return cashCharge;
	}
	/**
	 * @param cashCharge the cashCharge to set
	 */
	public void setCashCharge(BigDecimal cashCharge) {
		this.cashCharge = cashCharge;
	}

	
}
