package com.sunline.ccs.service.protocol;

import java.io.Serializable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sunline.ppy.dictionary.enums.AddressType;

public class S12020Resp implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * 卡号
	 */
	public String card_no;
	/**
	 * 地址类型
	 */
	public AddressType addr_type;
	/**
	 * 账单地址国家代码
	 */
	public String stmt_country_cd;
	/**
	 * 账单地址省份
	 */
	public String stmt_state;
	/**
	 * 账单地址城市
	 */
	public String stmt_city;
	/**
	 * 账单地址行政区
	 */
	public String stmt_district;
	/**
	 * 账单地址 
	 */
	public String stmt_address;
	/**
	 * 账单地址邮政编码 
	 */
	public String stmt_zip;
	
	
	public String getCard_no() {
		return card_no;
	}
	public AddressType getAddr_type() {
		return addr_type;
	}
	public String getStmt_country_cd() {
		return stmt_country_cd;
	}
	public String getStmt_state() {
		return stmt_state;
	}
	public String getStmt_city() {
		return stmt_city;
	}
	public String getStmt_district() {
		return stmt_district;
	}
	public String getStmt_address() {
		return stmt_address;
	}
	public String getStmt_zip() {
		return stmt_zip;
	}
	public void setCard_no(String card_no) {
		this.card_no = card_no;
	}
	public void setAddr_type(AddressType addr_type) {
		this.addr_type = addr_type;
	}
	public void setStmt_country_cd(String stmt_country_cd) {
		this.stmt_country_cd = stmt_country_cd;
	}
	public void setStmt_state(String stmt_state) {
		this.stmt_state = stmt_state;
	}
	public void setStmt_city(String stmt_city) {
		this.stmt_city = stmt_city;
	}
	public void setStmt_district(String stmt_district) {
		this.stmt_district = stmt_district;
	}
	public void setStmt_address(String stmt_address) {
		this.stmt_address = stmt_address;
	}
	public void setStmt_zip(String stmt_zip) {
		this.stmt_zip = stmt_zip;
	}
	
	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
