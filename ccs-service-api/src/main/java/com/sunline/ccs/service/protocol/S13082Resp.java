package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.math.BigDecimal;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sunline.ppy.dictionary.enums.LoanFeeMethod;

public class S13082Resp implements Serializable {

	private static final long serialVersionUID = -815934596697418879L;

	/**
     * 卡号
     */
    public String card_no;
    /**
     * 币种
     */
    public String curr_cd;
    /**
     * 分期申请顺序号
     */
    public Long register_id;
    /**
     * 分期总期数
     */
    public Integer loan_init_term;
    /**
     * 分期手续费收取方式
     */
    public LoanFeeMethod loan_fee_method;
    /**
     * 分期总本金
     */
    public BigDecimal loan_init_prin;
    /**
     * 分期每期应还本金
     */
    public BigDecimal loan_fixed_pmt_prin;
    /**
     * 分期首期应还本金
     */
    public BigDecimal loan_first_term_prin;
    /**
     * 分期末期应还本金
     */
    public BigDecimal loan_final_term_prin;
    /**
     * 分期总手续费
     */
    public BigDecimal loan_init_fee1;
    /**
     * 分期每期手续费
     */
    public BigDecimal loan_fixed_fee1;
    /**
     * 分期首期手续费
     */
    public BigDecimal loan_first_term_fee1;
    /**
     * 分期末期手续费
     */
    public BigDecimal loan_final_term_fee1;
    
    
	public String getCard_no() {
		return card_no;
	}
	public String getCurr_cd() {
		return curr_cd;
	}
	public Integer getLoan_init_term() {
		return loan_init_term;
	}
	public LoanFeeMethod getLoan_fee_method() {
		return loan_fee_method;
	}
	public BigDecimal getLoan_init_prin() {
		return loan_init_prin;
	}
	public BigDecimal getLoan_fixed_pmt_prin() {
		return loan_fixed_pmt_prin;
	}
	public BigDecimal getLoan_first_term_prin() {
		return loan_first_term_prin;
	}
	public BigDecimal getLoan_final_term_prin() {
		return loan_final_term_prin;
	}
	public BigDecimal getLoan_init_fee1() {
		return loan_init_fee1;
	}
	public BigDecimal getLoan_fixed_fee1() {
		return loan_fixed_fee1;
	}
	public BigDecimal getLoan_first_term_fee1() {
		return loan_first_term_fee1;
	}
	public BigDecimal getLoan_final_term_fee1() {
		return loan_final_term_fee1;
	}
	public void setCard_no(String card_no) {
		this.card_no = card_no;
	}
	public void setCurr_cd(String curr_cd) {
		this.curr_cd = curr_cd;
	}
	public void setLoan_init_term(Integer loan_init_term) {
		this.loan_init_term = loan_init_term;
	}
	public void setLoan_fee_method(LoanFeeMethod loan_fee_method) {
		this.loan_fee_method = loan_fee_method;
	}
	public void setLoan_init_prin(BigDecimal loan_init_prin) {
		this.loan_init_prin = loan_init_prin;
	}
	public void setLoan_fixed_pmt_prin(BigDecimal loan_fixed_pmt_prin) {
		this.loan_fixed_pmt_prin = loan_fixed_pmt_prin;
	}
	public void setLoan_first_term_prin(BigDecimal loan_first_term_prin) {
		this.loan_first_term_prin = loan_first_term_prin;
	}
	public void setLoan_final_term_prin(BigDecimal loan_final_term_prin) {
		this.loan_final_term_prin = loan_final_term_prin;
	}
	public void setLoan_init_fee1(BigDecimal loan_init_fee1) {
		this.loan_init_fee1 = loan_init_fee1;
	}
	public void setLoan_fixed_fee1(BigDecimal loan_fixed_fee1) {
		this.loan_fixed_fee1 = loan_fixed_fee1;
	}
	public void setLoan_first_term_fee1(BigDecimal loan_first_term_fee1) {
		this.loan_first_term_fee1 = loan_first_term_fee1;
	}
	public void setLoan_final_term_fee1(BigDecimal loan_final_term_fee1) {
		this.loan_final_term_fee1 = loan_final_term_fee1;
	}
	public Long getRegister_id() {
		return register_id;
	}
	public void setRegister_id(Long register_id) {
		this.register_id = register_id;
	}
	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}

