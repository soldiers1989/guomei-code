package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class S20090Req implements Serializable {

	private static final long serialVersionUID = 4693576650805407271L;
	
	/**
	 * 每月还款额不变，期数减少
	 */
	public static final String SHORTEDTYPE_T = "T";
	/**
	 * 期数不变，每月还款额降低
	 */
	public static final String SHORTEDTYPE_A = "A";
	/**
	 * 指定缩期后期数，调整每月还款额
	 */
	public static final String SHORTEDTYPE_S = "S";
	/**
	 * 指定每月还款额，调整期数（未实现）
	 */
	public static final String SHORTEDTYPE_P = "P";

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
     * 缩期方式
     */
    public String shorted_resc_type;

    /**
     * 还款金额
     */
    public BigDecimal loan_pmt_due;

    /**
     * 缩期期数
     */
    public Integer shorted_resc_term;

    /**
     * 缩期生效日期
     */
    public Date shorted_resc_vdate;

    /**
     * 缩期理由
     */
    public String shorted_resc_reson;

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

	public String getShorted_resc_type() {
		return shorted_resc_type;
	}

	public void setShorted_resc_type(String shorted_resc_type) {
		this.shorted_resc_type = shorted_resc_type;
	}

	public BigDecimal getLoan_pmt_due() {
		return loan_pmt_due;
	}

	public void setLoan_pmt_due(BigDecimal loan_pmt_due) {
		this.loan_pmt_due = loan_pmt_due;
	}

	public Date getShorted_resc_vdate() {
		return shorted_resc_vdate;
	}

	public void setShorted_resc_vdate(Date shorted_resc_vdate) {
		this.shorted_resc_vdate = shorted_resc_vdate;
	}

	public Integer getShorted_resc_term() {
		return shorted_resc_term;
	}

	public void setShorted_resc_term(Integer shorted_resc_term) {
		this.shorted_resc_term = shorted_resc_term;
	}

	public String getShorted_resc_reson() {
		return shorted_resc_reson;
	}

	public void setShorted_resc_reson(String shorted_resc_reson) {
		this.shorted_resc_reson = shorted_resc_reson;
	}

	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}

