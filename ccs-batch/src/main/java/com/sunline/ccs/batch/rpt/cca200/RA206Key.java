package com.sunline.ccs.batch.rpt.cca200;

import java.io.Serializable;
import java.math.BigDecimal;

import com.sunline.ppy.dictionary.enums.AccountType;

public class RA206Key implements Serializable {
	private static final long serialVersionUID = 1L;
	private BigDecimal repayPrincipal = new BigDecimal(0);
	private BigDecimal repayInterest = new BigDecimal(0);
	private BigDecimal repayFineFee = new BigDecimal(0);
	private BigDecimal repayIns = new BigDecimal(0);
	private BigDecimal repayMulct = new BigDecimal(0);
	private String refNbr;
	private Long acctNbr;
	private AccountType acctType;
	
	
	public Long getAcctNbr() {
		return acctNbr;
	}
	public void setAcctNbr(Long acctNbr) {
		this.acctNbr = acctNbr;
	}
	public AccountType getAcctType() {
		return acctType;
	}
	public void setAcctType(AccountType acctType) {
		this.acctType = acctType;
	}
	public String getRefNbr() {
		return refNbr;
	}
	public void setRefNbr(String refNbr) {
		this.refNbr = refNbr;
	}
	public BigDecimal getRepayPrincipal() {
		return repayPrincipal;
	}
	public void setRepayPrincipal(BigDecimal repayPrincipal) {
		this.repayPrincipal = repayPrincipal;
	}
	public BigDecimal getRepayInterest() {
		return repayInterest;
	}
	public void setRepayInterest(BigDecimal repayInterest) {
		this.repayInterest = repayInterest;
	}
	public BigDecimal getRepayFineFee() {
		return repayFineFee;
	}
	public void setRepayFineFee(BigDecimal repayFineFee) {
		this.repayFineFee = repayFineFee;
	}
	public BigDecimal getRepayIns() {
		return repayIns;
	}
	public void setRepayIns(BigDecimal repayIns) {
		this.repayIns = repayIns;
	}
	public BigDecimal getRepayMulct() {
		return repayMulct;
	}
	public void setRepayMulct(BigDecimal repayMulct) {
		this.repayMulct = repayMulct;
	}
	

}
