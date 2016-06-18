package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sunline.ppy.dictionary.enums.IdType;

/**
 * 账户信息验证
* @author fanghj
 *
 */
public class S16040Req implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2489238918022497852L;
	/**
	 * 卡号
	 */
	public String card_no;
	/**
	 * 证件类型
	 */
	public IdType id_type;
	/**
	 * 证件号码
	 */
	public String id_no;
	/**
	 * 手机号码
	 */
	public String mobile_no;
	/**
	 * 家庭电话
	 */
	public String home_phone;
	/**
	 * 客户姓名
	 */
	public String cust_name;
	/**
	 * 出生年月
	 */
	public Date birthday;
	/**
	 * 单位电话
	 */
	public String corp_phone;
	/**
	 * 直属联系人姓名
	 */
	public String name;
	/**
	 * 直属联系人电话
	 */
	public String phone;
	/**
	 * 查询密码
	 */
	public String q_pin;
	/**
	 * 交易密码
	 */
	public String p_pin;
	/**
	 * 有效期
	 */
	public Date expire_date;
	/**
	 * CVV2
	 */
	public String cvv2;
	public String getCard_no() {
		return card_no;
	}
	public void setCard_no(String card_no) {
		this.card_no = card_no;
	}
	public IdType getId_type() {
		return id_type;
	}
	public void setId_type(IdType id_type) {
		this.id_type = id_type;
	}
	public String getId_no() {
		return id_no;
	}
	public void setId_no(String id_no) {
		this.id_no = id_no;
	}
	public String getMobile_no() {
		return mobile_no;
	}
	public void setMobile_no(String mobile_no) {
		this.mobile_no = mobile_no;
	}
	public String getHome_phone() {
		return home_phone;
	}
	public void setHome_phone(String home_phone) {
		this.home_phone = home_phone;
	}
	public String getCust_name() {
		return cust_name;
	}
	public void setCust_name(String cust_name) {
		this.cust_name = cust_name;
	}
	public Date getBirthday() {
		return birthday;
	}
	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}
	public String getCorp_phone() {
		return corp_phone;
	}
	public void setCorp_phone(String corp_phone) {
		this.corp_phone = corp_phone;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getQ_pin() {
		return q_pin;
	}
	public void setQ_pin(String q_pin) {
		this.q_pin = q_pin;
	}
	public String getP_pin() {
		return p_pin;
	}
	public void setP_pin(String p_pin) {
		this.p_pin = p_pin;
	}
	public Date getExpire_date() {
		return expire_date;
	}
	public void setExpire_date(Date expire_date) {
		this.expire_date = expire_date;
	}
	public String getCvv2() {
		return cvv2;
	}
	public void setCvv2(String cvv2) {
		this.cvv2 = cvv2;
	}
	
	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
