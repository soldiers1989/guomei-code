package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class S12040Resp implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 卡号
	 */
	public String card_no;
	/**
	 * 调整前下次收年费日期
	 */
	public Date last_card_fee_date;

	/**
	 * 调整后下次收年费日期
	 */

	public Date next_card_fee_date;

	public String getCard_no() {
		return card_no;
	}

	public void setCard_no(String card_no) {
		this.card_no = card_no;
	}

	public Date getLast_card_fee_date() {
		return last_card_fee_date;
	}

	public void setLast_card_fee_date(Date last_card_fee_date) {
		this.last_card_fee_date = last_card_fee_date;
	}

	public Date getNext_card_fee_date() {
		return next_card_fee_date;
	}

	public void setNext_card_fee_date(Date next_card_fee_date) {
		this.next_card_fee_date = next_card_fee_date;
	}
	
	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
