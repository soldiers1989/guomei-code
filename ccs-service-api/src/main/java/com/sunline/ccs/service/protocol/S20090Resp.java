package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class S20090Resp implements Serializable {

	private static final long serialVersionUID = 3294300503159151240L;

	/**
     * 卡号
     */
    public String card_no;

    /**
     * 贷款借据号
     */
    public String loan_receipt_nbr;

    /**
     * 缩期期数
     */
    public Integer shorted_resc_term;

    /**
     * 缩期生效日期
     */
    public Date shorted_resc_vdate;

    /**
     * 贷款总本金
     */
    public BigDecimal loan_init_prin;

    /**
     * 贷款总期数
     */
    public Integer loan_init_term;

    /**
     * 缩期总本金
     */
    public BigDecimal shorted_resc_prin;
    
    public ArrayList<S20090Schedule> schedules;

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

	public Date getShorted_resc_vdate() {
		return shorted_resc_vdate;
	}

	public void setShorted_resc_vdate(Date shorted_resc_vdate) {
		this.shorted_resc_vdate = shorted_resc_vdate;
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

	public BigDecimal getShorted_resc_prin() {
		return shorted_resc_prin;
	}

	public void setShorted_resc_prin(BigDecimal shorted_resc_prin) {
		this.shorted_resc_prin = shorted_resc_prin;
	}

	public ArrayList<S20090Schedule> getSchedules() {
		return schedules;
	}

	public void setSchedules(ArrayList<S20090Schedule> schedules) {
		this.schedules = schedules;
	}

	public Integer getShorted_resc_term() {
		return shorted_resc_term;
	}

	public void setShorted_resc_term(Integer shorted_resc_term) {
		this.shorted_resc_term = shorted_resc_term;
	}

	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}

