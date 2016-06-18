package com.sunline.ccs.service.protocol;

import java.io.Serializable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sunline.ppy.dictionary.enums.AddressType;

public class S12020Req implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
	 * 操作类型
	 */
	public String opt;
	/**
	 * 卡号
	 */
	public String card_no;
	/**
	 * 地址类型
	 */
	public AddressType addr_type;
	
	
	public String getOpt() {
		return opt;
	}
	public void setOpt(String opt) {
		this.opt = opt;
	}
	public String getCard_no() {
		return card_no;
	}
	public AddressType getAddr_type() {
		return addr_type;
	}
	public void setCard_no(String card_no) {
		this.card_no = card_no;
	}
	public void setAddr_type(AddressType addr_type) {
		this.addr_type = addr_type;
	}
	
	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
	
}
