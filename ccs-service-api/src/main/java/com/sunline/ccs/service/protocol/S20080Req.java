package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * 贷款展期
* @author fanghj
 * @time 2014-3-26 下午2:23:44
 */
public class S20080Req implements Serializable {

	private static final long serialVersionUID = 2508979896452026933L;

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
     * 展期期数
     */
    public Integer reschedule_term;

    /**
     * 展期生效日期
     */
    public Date reschedule_vdate;

    /**
     * 展期理由
     */
    public String reschedule_reson;

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

	public Integer getReschedule_term() {
		return reschedule_term;
	}

	public void setReschedule_term(Integer reschedule_term) {
		this.reschedule_term = reschedule_term;
	}

	public Date getReschedule_vdate() {
		return reschedule_vdate;
	}

	public void setReschedule_vdate(Date reschedule_vdate) {
		this.reschedule_vdate = reschedule_vdate;
	}

	public String getReschedule_reson() {
		return reschedule_reson;
	}

	public void setReschedule_reson(String reschedule_reson) {
		this.reschedule_reson = reschedule_reson;
	}

	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}

