package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sunline.ppy.dictionary.enums.LoanFeeMethod;
import com.sunline.ppy.dictionary.enums.LoanStatus;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ccs.param.def.enums.LoanTerminateReason;

public class S13080Loan implements Serializable {

    /**  
	 * serialVersionUID:TODO（用一句话描述这个变量表示什么）  
	 * @since 1.0.0  
	*/  
	    
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
     * 分期类型
     */
    public LoanType loan_type ;

    /**
     * 分期状态
     */
    public LoanStatus loan_status ;
    
    /**
     * 分期终止原因
     */
    public LoanTerminateReason terminate_reason_cd;

    /**
     * 分期总期数
     */
    public Integer loan_init_term ;

    /**
     * 当前期数
     */
    public Integer curr_term ;

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
     * 激活日期
     */
    public Date activate_date ;

    /**
     * 已偿还本金
     */
    public BigDecimal prin_paid ;

    /**
     * 已偿还利息
     */
    public BigDecimal int_paid ;

    /**
     * 已偿还费用
     */
    public BigDecimal fee_paid ;

    /**
     * 分期当前总余额
     */
    public BigDecimal loan_curr_bal ;

    /**
     * 分期未到期余额
     */
    public BigDecimal loan_bal_xfrout ;

    /**
     * 分期已出账单余额
     */
    public BigDecimal loan_bal_xfrin ;

    /**
     * 分期未到期本金
     */
    public BigDecimal loan_prin_xfrout ;

    /**
     * 分期已出账单本金
     */
    public BigDecimal loan_prin_xfrin ;

    /**
     * 分期未到期手续费
     */
    public BigDecimal loan_fee1_xfrout ;

    /**
     * 分期已出账单手续费
     */
    public BigDecimal loan_fee1_xfrin ;

    /**
     * 分期计划代码
     */
    public String loan_code ;

    /**
     * 分期手续费收取方式
     */
    public LoanFeeMethod loan_fee_method ;
    
    /**
     * 账单年月
     */
    public Date stmt_date;
    
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

	public Long getRegister_id() {
		return register_id;
	}
	public void setRegister_id(Long register_id) {
		this.register_id = register_id;
	}
	public Date getRegister_date() {
		return register_date;
	}

	public void setRegister_date(Date register_date) {
		this.register_date = register_date;
	}

	public LoanType getLoan_type() {
		return loan_type;
	}

	public void setLoan_type(LoanType loan_type) {
		this.loan_type = loan_type;
	}

	public LoanStatus getLoan_status() {
		return loan_status;
	}

	public void setLoan_status(LoanStatus loan_status) {
		this.loan_status = loan_status;
	}

	public Integer getLoan_init_term() {
		return loan_init_term;
	}

	public void setLoan_init_term(Integer loan_init_term) {
		this.loan_init_term = loan_init_term;
	}

	public Integer getCurr_term() {
		return curr_term;
	}

	public void setCurr_term(Integer curr_term) {
		this.curr_term = curr_term;
	}

	public BigDecimal getLoan_init_prin() {
		return loan_init_prin;
	}

	public void setLoan_init_prin(BigDecimal loan_init_prin) {
		this.loan_init_prin = loan_init_prin;
	}

	public BigDecimal getLoan_fixed_pmt_prin() {
		return loan_fixed_pmt_prin;
	}

	public void setLoan_fixed_pmt_prin(BigDecimal loan_fixed_pmt_prin) {
		this.loan_fixed_pmt_prin = loan_fixed_pmt_prin;
	}

	public BigDecimal getLoan_first_term_prin() {
		return loan_first_term_prin;
	}

	public void setLoan_first_term_prin(BigDecimal loan_first_term_prin) {
		this.loan_first_term_prin = loan_first_term_prin;
	}

	public BigDecimal getLoan_final_term_prin() {
		return loan_final_term_prin;
	}

	public void setLoan_final_term_prin(BigDecimal loan_final_term_prin) {
		this.loan_final_term_prin = loan_final_term_prin;
	}

	public BigDecimal getLoan_init_fee1() {
		return loan_init_fee1;
	}

	public void setLoan_init_fee1(BigDecimal loan_init_fee1) {
		this.loan_init_fee1 = loan_init_fee1;
	}

	public BigDecimal getLoan_fixed_fee1() {
		return loan_fixed_fee1;
	}

	public void setLoan_fixed_fee1(BigDecimal loan_fixed_fee1) {
		this.loan_fixed_fee1 = loan_fixed_fee1;
	}

	public BigDecimal getLoan_first_term_fee1() {
		return loan_first_term_fee1;
	}

	public void setLoan_first_term_fee1(BigDecimal loan_first_term_fee1) {
		this.loan_first_term_fee1 = loan_first_term_fee1;
	}

	public BigDecimal getLoan_final_term_fee1() {
		return loan_final_term_fee1;
	}

	public void setLoan_final_term_fee1(BigDecimal loan_final_term_fee1) {
		this.loan_final_term_fee1 = loan_final_term_fee1;
	}

	public Date getActivate_date() {
		return activate_date;
	}

	public void setActivate_date(Date activate_date) {
		this.activate_date = activate_date;
	}

	public BigDecimal getPrin_paid() {
		return prin_paid;
	}

	public void setPrin_paid(BigDecimal prin_paid) {
		this.prin_paid = prin_paid;
	}

	public BigDecimal getInt_paid() {
		return int_paid;
	}

	public void setInt_paid(BigDecimal int_paid) {
		this.int_paid = int_paid;
	}

	public BigDecimal getFee_paid() {
		return fee_paid;
	}

	public void setFee_paid(BigDecimal fee_paid) {
		this.fee_paid = fee_paid;
	}

	public BigDecimal getLoan_curr_bal() {
		return loan_curr_bal;
	}

	public void setLoan_curr_bal(BigDecimal loan_curr_bal) {
		this.loan_curr_bal = loan_curr_bal;
	}

	public BigDecimal getLoan_bal_xfrout() {
		return loan_bal_xfrout;
	}

	public void setLoan_bal_xfrout(BigDecimal loan_bal_xfrout) {
		this.loan_bal_xfrout = loan_bal_xfrout;
	}

	public BigDecimal getLoan_bal_xfrin() {
		return loan_bal_xfrin;
	}

	public void setLoan_bal_xfrin(BigDecimal loan_bal_xfrin) {
		this.loan_bal_xfrin = loan_bal_xfrin;
	}

	public BigDecimal getLoan_prin_xfrout() {
		return loan_prin_xfrout;
	}

	public void setLoan_prin_xfrout(BigDecimal loan_prin_xfrout) {
		this.loan_prin_xfrout = loan_prin_xfrout;
	}

	public BigDecimal getLoan_prin_xfrin() {
		return loan_prin_xfrin;
	}

	public void setLoan_prin_xfrin(BigDecimal loan_prin_xfrin) {
		this.loan_prin_xfrin = loan_prin_xfrin;
	}

	public BigDecimal getLoan_fee1_xfrout() {
		return loan_fee1_xfrout;
	}

	public void setLoan_fee1_xfrout(BigDecimal loan_fee1_xfrout) {
		this.loan_fee1_xfrout = loan_fee1_xfrout;
	}

	public BigDecimal getLoan_fee1_xfrin() {
		return loan_fee1_xfrin;
	}

	public void setLoan_fee1_xfrin(BigDecimal loan_fee1_xfrin) {
		this.loan_fee1_xfrin = loan_fee1_xfrin;
	}

	public String getLoan_code() {
		return loan_code;
	}

	public void setLoan_code(String loan_code) {
		this.loan_code = loan_code;
	}


	public LoanFeeMethod getLoan_fee_method() {
		return loan_fee_method;
	}

	public void setLoan_fee_method(LoanFeeMethod loan_fee_method) {
		this.loan_fee_method = loan_fee_method;
	}

	public String getCard_no() {
		return card_no;
	}

	public void setCard_no(String card_no) {
		this.card_no = card_no;
	}

	public LoanTerminateReason getTerminate_reason_cd() {
		return terminate_reason_cd;
	}

	public void setTerminate_reason_cd(LoanTerminateReason terminate_reason_cd) {
		this.terminate_reason_cd = terminate_reason_cd;
	}

	public Date getStmt_date() {
		return stmt_date;
	}

	public void setStmt_date(Date stmt_date) {
		this.stmt_date = stmt_date;
	}

	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
    


}

