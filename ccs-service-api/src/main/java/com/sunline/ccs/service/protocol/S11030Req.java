package com.sunline.ccs.service.protocol;

import java.io.Serializable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sunline.ppy.dictionary.enums.AddressType;
import com.sunline.ppy.dictionary.enums.IdType;

public class S11030Req implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
	 * 证件类型
	 */
	public IdType id_type;
	/**
	 * 证件号码
	 */
	public String id_no;
	/**
	 * 操作行动码
	 */
	public String opt;
	/**
	 * 开始位置
	 */
	public Integer firstrow;
	/**
	 * 结束位置
	 */
	public Integer lastrow;
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
	
	
	public IdType getId_type() {
		return id_type;
	}
	public String getId_no() {
		return id_no;
	}
	public String getOpt() {
		return opt;
	}
	public Integer getFirstrow() {
		return firstrow;
	}
	public Integer getLastrow() {
		return lastrow;
	}
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
	public void setId_type(IdType id_type) {
		this.id_type = id_type;
	}
	public void setId_no(String id_no) {
		this.id_no = id_no;
	}
	public void setOpt(String opt) {
		this.opt = opt;
	}
	public void setFirstrow(Integer firstrow) {
		this.firstrow = firstrow;
	}
	public void setLastrow(Integer lastrow) {
		this.lastrow = lastrow;
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
