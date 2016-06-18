package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.math.BigDecimal;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class S13110Term implements Serializable{

	private static final long serialVersionUID = -7619553414302990200L;
	
	/**
	 * 分期期数
	 */
	public Integer loan_init_term;
	
	/**
	 * 可分期金额
	 */
	public BigDecimal loan_amt;

	public Integer getLoan_init_term() {
		return loan_init_term;
	}

	public void setLoan_init_term(Integer loan_init_term) {
		this.loan_init_term = loan_init_term;
	}

	public BigDecimal getLoan_amt() {
		return loan_amt;
	}

	public void setLoan_amt(BigDecimal loan_amt) {
		this.loan_amt = loan_amt;
	}
	
	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
