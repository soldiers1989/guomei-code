package com.sunline.ccs.service.protocol;

import java.io.Serializable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sunline.ppy.dictionary.enums.AddressType;

public class S14170Req implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
	 * 卡号
	 */
	public String card_no;
	/**
	 * 功能码
	 */
	public String opt;
	/**
	 * 卡片寄送地址标志
	 */
	public AddressType card_mailer_ind;
	
	/**
	 * 领取方式
	 */
	public String card_fetch_method;
	
	
	public String getCard_no() {
		return card_no;
	}
	public String getOpt() {
		return opt;
	}
	public AddressType getCard_mailer_ind() {
		return card_mailer_ind;
	}
	public void setCard_no(String card_no) {
		this.card_no = card_no;
	}
	public void setOpt(String opt) {
		this.opt = opt;
	}
	public void setCard_mailer_ind(AddressType card_mailer_ind) {
		this.card_mailer_ind = card_mailer_ind;
	}
	
	public String getCard_fetch_method() {
		return card_fetch_method;
	}
	public void setCard_fetch_method(String card_fetch_method) {
		this.card_fetch_method = card_fetch_method;
	}
	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
	
}
