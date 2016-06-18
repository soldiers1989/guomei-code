package com.sunline.ccs.service.protocol;

import java.io.Serializable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sunline.ppy.dictionary.enums.AddressType;

public class S14170Resp implements Serializable {

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
	 * 卡片寄送地址标志
	 */
	public AddressType card_mailer_ind;
	/**
	 * 国家代码
	 */
	public String country_cd;
	/**
	 * 省份
	 */
	public String state;
	/**
	 * 城市
	 */
	public String city;
	/**
	 * 区县
	 */
	public String district;
	/**
	 * 邮政编码
	 */
	public String zip;
	/**
	 * 电话号码
	 */
	public String phone;
	/**
	 * 地址
	 */
	public String address;
	
	/**
	 * 领取方式
	 */
	public String card_fetch_method;
	
	public String getCard_no() {
		return card_no;
	}
	public String getCurr_cd() {
		return curr_cd;
	}
	public AddressType getCard_mailer_ind() {
		return card_mailer_ind;
	}
	public String getCountry_cd() {
		return country_cd;
	}
	public String getState() {
		return state;
	}
	public String getCity() {
		return city;
	}
	public String getDistrict() {
		return district;
	}
	public String getZip() {
		return zip;
	}
	public String getPhone() {
		return phone;
	}
	public String getAddress() {
		return address;
	}
	public void setCard_no(String card_no) {
		this.card_no = card_no;
	}
	public void setCurr_cd(String curr_cd) {
		this.curr_cd = curr_cd;
	}
	public void setCard_mailer_ind(AddressType card_mailer_ind) {
		this.card_mailer_ind = card_mailer_ind;
	}
	public void setCountry_cd(String country_cd) {
		this.country_cd = country_cd;
	}
	public void setState(String state) {
		this.state = state;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public void setDistrict(String district) {
		this.district = district;
	}
	public void setZip(String zip) {
		this.zip = zip;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public void setAddress(String address) {
		this.address = address;
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
