package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sunline.ppy.dictionary.enums.LoanAction;
import com.sunline.ppy.dictionary.enums.LoanFeeMethod;
import com.sunline.ppy.dictionary.enums.LoanRegStatus;
import com.sunline.ppy.dictionary.enums.LoanType;

public class S13083Loan implements Serializable {

	private static final long serialVersionUID = 4345964771499862067L;
	
	/**
	 * 卡号
	 */
	public String card_no;

	/**
     * 分期申请顺序号
     */
    public Long register_id ;
    /**
     * 分期注册日期
     */
    public Date register_date ;
    /**
     * 请求日期时间
     */
    public String request_time;
    /**
     * 分期类型
     */
    public LoanType loan_type ;
    /**
     * 分期状态
     */
    public LoanRegStatus loan_reg_status ;
    /**
     * 分期交易行动码
     */
    public LoanAction loan_action ;

	/**
     * 分期总期数
     */
    public Integer loan_init_term ;
    /**
     * 分期总本金
     */
    public BigDecimal loan_init_prin ;
    /**
     * 分期每期应还本金
     */
    public BigDecimal loan_fixed_pmt_prin ;
    /**
     * 分期首期应还本金
     */
    public BigDecimal loan_first_term_prin ;
    /**
     * 分期末期应还本金
     */
    public BigDecimal loan_final_term_prin ;
    /**
     * 分期总手续费
     */
    public BigDecimal loan_init_fee1 ;
    /**
     * 分期每期手续费
     */
    public BigDecimal loan_fixed_fee1 ;
    /**
     * 分期首期手续费
     */
    public BigDecimal loan_first_term_fee1 ;
    /**
     * 分期末期手续费
     */
    public BigDecimal loan_final_term_fee1 ;
    /**
     * 分期手续费收取方式
     */
    public LoanFeeMethod loan_fee_method ;
    /**
     * 分期手续费率
     * 
     */
    public BigDecimal interest_rate; 
    /**
     * 获取分期手续费率
     * @return 分期手续费率
     */
	public BigDecimal getInterest_rate() {
		return interest_rate;
	}
	/**
	 * 设置分期手续费率
	 * @param interest_rate 分期手续费率
	 */
	public void setInterest_rate(BigDecimal interest_rate) {
		this.interest_rate = interest_rate;
	}
	
	public String getCard_no() {
		return card_no;
	}
	public void setCard_no(String card_no) {
		this.card_no = card_no;
	}
	public Date getRegister_date() {
		return register_date;
	}
	public String getRequest_time() {
		return request_time;
	}
	public LoanType getLoan_type() {
		return loan_type;
	}
	public LoanAction getLoan_action() {
		return loan_action;
	}
	public void setLoan_action(LoanAction loan_action) {
		this.loan_action = loan_action;
	}
	public Integer getLoan_init_term() {
		return loan_init_term;
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
	public LoanFeeMethod getLoan_fee_method() {
		return loan_fee_method;
	}
	public void setRegister_date(Date register_date) {
		this.register_date = register_date;
	}
	public void setRequest_time(String request_time) {
		this.request_time = request_time;
	}
	public void setLoan_type(LoanType loan_type) {
		this.loan_type = loan_type;
	}
	
	public LoanRegStatus getLoan_reg_status() {
		return loan_reg_status;
	}
	public void setLoan_reg_status(LoanRegStatus loan_reg_status) {
		this.loan_reg_status = loan_reg_status;
	}
	public void setLoan_init_term(Integer loan_init_term) {
		this.loan_init_term = loan_init_term;
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
	public void setLoan_fee_method(LoanFeeMethod loan_fee_method) {
		this.loan_fee_method = loan_fee_method;
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

