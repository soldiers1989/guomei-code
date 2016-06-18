package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 *
* @author fanghj
 *@time 2014-3-26 下午2:39:55
 */
public class S20080Schedule implements Serializable{

	private static final long serialVersionUID = -1933733740711472610L;
	   /**
     * 当前期数
     */
    public Integer curr_term;

    /**
     * 应还本金
     */
    public BigDecimal loan_term_prin;

    /**
     * 应还费用
     */
    public BigDecimal loan_term_fee1;

    /**
     * 应还利息
     */
    public BigDecimal loan_term_interest;

    /**
     * 贷款款到期还款日期
     */
    public Date loan_pmt_due_date;

    /**
     * 宽限日
     */
    public Date loan_grace_date;
    
	public Integer getCurr_term() {
		return curr_term;
	}

	public void setCurr_term(Integer curr_term) {
		this.curr_term = curr_term;
	}

	public BigDecimal getLoan_term_prin() {
		return loan_term_prin;
	}

	public void setLoan_term_prin(BigDecimal loan_term_prin) {
		this.loan_term_prin = loan_term_prin;
	}

	public BigDecimal getLoan_term_fee1() {
		return loan_term_fee1;
	}

	public void setLoan_term_fee1(BigDecimal loan_term_fee1) {
		this.loan_term_fee1 = loan_term_fee1;
	}

	public BigDecimal getLoan_term_interest() {
		return loan_term_interest;
	}

	public void setLoan_term_interest(BigDecimal loan_term_interest) {
		this.loan_term_interest = loan_term_interest;
	}

	public Date getLoan_pmt_due_date() {
		return loan_pmt_due_date;
	}

	public void setLoan_pmt_due_date(Date loan_pmt_due_date) {
		this.loan_pmt_due_date = loan_pmt_due_date;
	}

	public Date getLoan_grace_date() {
		return loan_grace_date;
	}

	public void setLoan_grace_date(Date loan_grace_date) {
		this.loan_grace_date = loan_grace_date;
	}

	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
