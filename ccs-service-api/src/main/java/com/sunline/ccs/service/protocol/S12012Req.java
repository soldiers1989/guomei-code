package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.math.BigDecimal;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sunline.ppy.dictionary.enums.LoanFeeMethod;

public class S12012Req implements Serializable {

	private static final long serialVersionUID = 5247009503445644101L;

	/**
	 * 卡号
	 */
	public String card_no;
	/**
	 * 币种
	 */
	public String curr_cd;
	/**
	 * 分期总期数
	 */
	public Integer loan_init_term;
	/**
	 * 分期总本金
	 */
	public BigDecimal loan_init_prin;
	/**
	 * 分期手续费收取方式
	 */
	public LoanFeeMethod loan_fee_method;
	/**
	 * 操作类型
	 */
	public String opt;
	

	public String getCard_no() {
		return card_no;
	}

	public void setCard_no(String card_no) {
		this.card_no = card_no;
	}

	public String getCurr_cd() {
		return curr_cd;
	}

	public void setCurr_cd(String curr_cd) {
		this.curr_cd = curr_cd;
	}

	public Integer getLoan_init_term() {
		return loan_init_term;
	}

	public void setLoan_init_term(Integer loan_init_term) {
		this.loan_init_term = loan_init_term;
	}

	public BigDecimal getLoan_init_prin() {
		return loan_init_prin;
	}

	public void setLoan_init_prin(BigDecimal loan_init_prin) {
		this.loan_init_prin = loan_init_prin;
	}

	public LoanFeeMethod getLoan_fee_method() {
		return loan_fee_method;
	}

	public void setLoan_fee_method(LoanFeeMethod loan_fee_method) {
		this.loan_fee_method = loan_fee_method;
	}

	public String getOpt() {
		return opt;
	}

	public void setOpt(String opt) {
		this.opt = opt;
	}
	
	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
