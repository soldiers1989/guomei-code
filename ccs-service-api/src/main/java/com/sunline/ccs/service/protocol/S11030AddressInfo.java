package com.sunline.ccs.service.protocol;

import java.io.Serializable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sunline.ppy.dictionary.enums.AddressType;

public class S11030AddressInfo implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
	 * 地址类型
	 */
	public AddressType addr_type;
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
	
	
	public AddressType getAddr_type() {
		return addr_type;
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
	public void setAddr_type(AddressType addr_type) {
		this.addr_type = addr_type;
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
	
	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
