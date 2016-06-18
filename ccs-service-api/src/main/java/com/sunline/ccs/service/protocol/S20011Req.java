package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.math.BigDecimal;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * 提前还款
* @author fanghj
 * @time 2014-3-31 下午11:42:06
 */
public class S20011Req implements Serializable {

	private static final long serialVersionUID = -6587992655129502548L;

	/**
     * 卡号
     */
    public String card_no;

    /**
     * 贷款借据号
     */
    public String loan_receipt_nbr;

    /**
     * 功能码
     */
    public String opt;

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

	public String getOpt() {
		return opt;
	}

	public void setOpt(String opt) {
		this.opt = opt;
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

