package com.sunline.ccs.service.protocol;

import java.io.Serializable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class S12050Req implements Serializable {

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
	 * 账单起始年月
	 */
	public String stmt_start_date;
	/**
	 * 账单截止年月
	 */
	public String stmt_end_date;
	/**
	 * 开始位置
	 */
	public Integer firstrow;
	/**
	 * 结束位置
	 */
	public Integer lastrow;
	
	
	public String getCard_no() {
		return card_no;
	}
	public String getCurr_cd() {
		return curr_cd;
	}

	public Integer getFirstrow() {
		return firstrow;
	}
	public Integer getLastrow() {
		return lastrow;
	}
	public void setCard_no(String card_no) {
		this.card_no = card_no;
	}
	public void setCurr_cd(String curr_cd) {
		this.curr_cd = curr_cd;
	}
	
	public void setFirstrow(Integer firstrow) {
		this.firstrow = firstrow;
	}
	public void setLastrow(Integer lastrow) {
		this.lastrow = lastrow;
	}
	public String getStmt_start_date() {
		return stmt_start_date;
	}
	public void setStmt_start_date(String stmt_start_date) {
		this.stmt_start_date = stmt_start_date;
	}
	public String getStmt_end_date() {
		return stmt_end_date;
	}
	public void setStmt_end_date(String stmt_end_date) {
		this.stmt_end_date = stmt_end_date;
	}
	
	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
