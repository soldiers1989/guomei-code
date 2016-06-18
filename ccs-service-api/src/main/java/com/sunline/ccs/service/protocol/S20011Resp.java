package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.math.BigDecimal;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * 提前还款
* @author fanghj
 * @time 2014-3-31 下午11:43:32
 */
public class S20011Resp implements Serializable {

	private static final long serialVersionUID = 6705412694066805673L;

	/**
     * 卡号
     */
    public String card_no;

    /**
     * 贷款借据号
     */
    public String loan_receipt_nbr;

    /**
     * 贷款总本金
     */
    public BigDecimal loan_init_prin;

    /**
     * 贷款总期数
     */
    public Integer loan_init_term;

    /**
     * 还款金额
     */
    public BigDecimal loan_pmt_due;

	public String getCard_no() {
		return card_no;
	}

	public void setCard_no(String card_no) {
		this.card_no = card_no;
	}

	public String getLoan_receipt_nbr() {
		return loan_receipt_nbr;
	}

	public void setLoan_receipt_nbr(String loan_receipt_nbr) {
		this.loan_receipt_nbr = loan_receipt_nbr;
	}

	public BigDecimal getLoan_init_prin() {
		return loan_init_prin;
	}

	public void setLoan_init_prin(BigDecimal loan_init_prin) {
		this.loan_init_prin = loan_init_prin;
	}

	public Integer getLoan_init_term() {
		return loan_init_term;
	}

	public void setLoan_init_term(Integer loan_init_term) {
		this.loan_init_term = loan_init_term;
	}

	public BigDecimal getLoan_pmt_due() {
		return loan_pmt_due;
	}

	public void setLoan_pmt_due(BigDecimal loan_pmt_due) {
		this.loan_pmt_due = loan_pmt_due;
	}

	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}

