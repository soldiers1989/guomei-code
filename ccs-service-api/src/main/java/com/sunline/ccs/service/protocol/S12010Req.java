package com.sunline.ccs.service.protocol;

import java.io.Serializable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class S12010Req implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
	 * 卡号
	 */
	public String card_no;
	
	/**
	 * 币种
	 */
	public String curr_cd;
	
	/**
	 * 账单年月
	 */
	public String stmt_date;


	public String getCard_no() {
		return card_no;
	}

	public String getCurr_cd() {
		return curr_cd;
	}

	public String getStmt_date() {
		return stmt_date;
	}

	public void setCard_no(String card_no) {
		this.card_no = card_no;
	}

	public void setCurr_cd(String curr_cd) {
		this.curr_cd = curr_cd;
	}

	public void setStmt_date(String stmt_date) {
		this.stmt_date = stmt_date;
	}
	
	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
