package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * 贷款展期
* @author fanghj
 * @time 2014-3-26 下午2:25:32
 */
public class S20080Resp implements Serializable {

	private static final long serialVersionUID = 1349097071805415109L;

	/**
     * 卡号
     */
    public String card_no;

    /**
     * 贷款借据号
     */
    public String loan_receipt_nbr;

    /**
     * 展期期数
     */
    public Integer reschedule_term;

    /**
     * 展期生效日期
     */
    public Date reschedule_vdate;

    /**
     * 贷款总本金
     */
    public BigDecimal loan_init_prin;

    /**
     * 贷款总期数
     */
    public Integer loan_init_term;

    /**
     * 展期总本金
     */
    public BigDecimal reschedule_prin;

    public ArrayList<S20080Schedule> schedules;

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

	public BigDecimal getReschedule_prin() {
		return reschedule_prin;
	}

	public void setReschedule_prin(BigDecimal reschedule_prin) {
		this.reschedule_prin = reschedule_prin;
	}

	public ArrayList<S20080Schedule> getSchedules() {
		return schedules;
	}

	public void setSchedules(ArrayList<S20080Schedule> schedules) {
		this.schedules = schedules;
	}

	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}

