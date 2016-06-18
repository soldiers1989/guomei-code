package com.sunline.ccs.service.protocol;

import java.io.Serializable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sunline.ccs.param.def.enums.TxnType;

public class S12011Req implements Serializable {

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
	
	/**
	 * 交易类型
	 */
	public TxnType sett_txn_type;
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
	public String getStmt_date() {
		return stmt_date;
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
	public void setStmt_date(String stmt_date) {
		this.stmt_date = stmt_date;
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
