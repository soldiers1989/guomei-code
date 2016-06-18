package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sunline.ccs.param.def.enums.TxnType;

public class S13060Req implements Serializable {

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
	 * 起始日期
	 */
	public Date start_date;

	/**
	 * 交易类型
	 */
	public TxnType sett_txn_type;
	/**
	 * 截止日期
	 */
	public Date end_date;
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
	public Date getStart_date() {
		return start_date;
	}
	public Date getEnd_date() {
		return end_date;
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
	public void setStart_date(Date start_date) {
		this.start_date = start_date;
	}
	public void setEnd_date(Date end_date) {
		this.end_date = end_date;
	}
	public void setFirstrow(Integer firstrow) {
		this.firstrow = firstrow;
	}
	public void setLastrow(Integer lastrow) {
		this.lastrow = lastrow;
	}
	public TxnType getSett_txn_type() {
		return sett_txn_type;
	}
	public void setSett_txn_type(TxnType sett_txn_type) {
		this.sett_txn_type = sett_txn_type;
	}
	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
