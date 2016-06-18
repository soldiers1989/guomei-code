package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class S12110Resp implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
	 * 主卡卡号
	 */
	public String card_no;
	/**
	 * 新账单周期
	 */
	public Integer billing_cycle;
	/**
	 * 调整前上个账单日期
	 */
	public Date last_stmt_date;
	/**
	 * 调整后下个账单日期
	 */
	public Date next_stmt_date;
	/**
	 * 到期还款日期
	 */
	public Date pmt_due_date;
	/**
	 * 约定还款日期
	 */
	public Date dd_date;
	/**
	 * 账单日已调次数 
	 */
	public Integer used_cnt;
	/**
	 * 账单日剩余可调次数 
	 */
	public Integer aval_cnt;
	
	
	public String getCard_no() {
		return card_no;
	}
	public Integer getBilling_cycle() {
		return billing_cycle;
	}
	public Date getLast_stmt_date() {
		return last_stmt_date;
	}
	public Date getNext_stmt_date() {
		return next_stmt_date;
	}
	public Date getPmt_due_date() {
		return pmt_due_date;
	}
	public Date getDd_date() {
		return dd_date;
	}
	public Integer getUsed_cnt() {
		return used_cnt;
	}
	public Integer getAval_cnt() {
		return aval_cnt;
	}
	public void setCard_no(String card_no) {
		this.card_no = card_no;
	}
	public void setBilling_cycle(Integer billing_cycle) {
		this.billing_cycle = billing_cycle;
	}
	public void setLast_stmt_date(Date last_stmt_date) {
		this.last_stmt_date = last_stmt_date;
	}
	public void setNext_stmt_date(Date next_stmt_date) {
		this.next_stmt_date = next_stmt_date;
	}
	public void setPmt_due_date(Date pmt_due_date) {
		this.pmt_due_date = pmt_due_date;
	}
	public void setDd_date(Date dd_date) {
		this.dd_date = dd_date;
	}
	public void setUsed_cnt(Integer used_cnt) {
		this.used_cnt = used_cnt;
	}
	public void setAval_cnt(Integer aval_cnt) {
		this.aval_cnt = aval_cnt;
	}
	
	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
