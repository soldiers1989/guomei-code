package com.sunline.ccs.service.protocol;

import java.io.Serializable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sunline.ppy.dictionary.enums.Indicator;

/**
 * 补打账单
 * 
* @author fanghj
 * 
 */
public class S12030Req implements Serializable{

	private static final long serialVersionUID = 6465787969663874078L;

	/**
	 * 卡号
	 */
	public String card_no;

	/**
	 * 账单年月
	 */
	public String stmt_date;
	
	/**
	 * 是否加急
	 */
	public Indicator urgent_flg;
	
	/**
	 * 收费标志
	 */
	public Indicator fee_ind;

	public Indicator getFee_ind() {
		return fee_ind;
	}

	public void setFee_ind(Indicator fee_ind) {
		this.fee_ind = fee_ind;
	}

	public String getCard_no() {
		return card_no;
	}

	public void setCard_no(String card_no) {
		this.card_no = card_no;
	}

	public String getStmt_date() {
		return stmt_date;
	}

	public void setStmt_date(String stmt_date) {
		this.stmt_date = stmt_date;
	}
	
	public Indicator getUrgent_flg() {
		return urgent_flg;
	}

	public void setUrgent_flg(Indicator urgent_flg) {
		this.urgent_flg = urgent_flg;
	}
	
	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
