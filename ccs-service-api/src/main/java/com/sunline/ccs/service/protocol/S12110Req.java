package com.sunline.ccs.service.protocol;

import java.io.Serializable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class S12110Req implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
	 * 主卡卡号
	 */
	public String card_no;
	/**
	 * 新账单周期
	 */
	public Integer billing_cycle;
	
	
	public String getCard_no() {
		return card_no;
	}
	public Integer getBilling_cycle() {
		return billing_cycle;
	}
	public void setCard_no(String card_no) {
		this.card_no = card_no;
	}
	public void setBilling_cycle(Integer billing_cycle) {
		this.billing_cycle = billing_cycle;
	}
	
	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
