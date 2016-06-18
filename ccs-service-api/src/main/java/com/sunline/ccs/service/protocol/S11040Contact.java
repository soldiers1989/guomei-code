package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sunline.ppy.dictionary.enums.EmpPositionAttrType;
import com.sunline.ppy.dictionary.enums.Gender;
import com.sunline.ppy.dictionary.enums.IdType;
import com.sunline.ppy.dictionary.enums.Relationship;

public class S11040Contact implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * 与持卡人关系
	 */
	public Relationship relationship;
	/**
	 * 证件类型
	 */
	public IdType contact_id_type;
	/**
	 * 证件号码
	 */
	public String contact_id_no;
	/**
	 * 姓名
	 */
	public String name;
	/**
	 * 性别
	 */
	public Gender gender;
	/**
	 * 移动电话
	 */
	public String mobile_no;
	/**
	 * 生日
	 */
	public Date birthday;
	/**
	 * 公司名称
	 */
	public String corp_name;
	/**
	 * 证件类型
	 */
	public IdType id_type;
	/**
	 * 证件号码
	 */
	public String id_no;
	/**
	 * 公司电话
	 */
	public String corp_phone;
	/**
	 * 公司传真
	 */
	public String corp_fax;
	/**
	 * 公司职务
	 */
	public EmpPositionAttrType corp_post;
	
	
	public Relationship getRelationship() {
		return relationship;
	}
	public String getName() {
		return name;
	}
	public Gender getGender() {
		return gender;
	}
	public String getMobile_no() {
		return mobile_no;
	}
	public Date getBirthday() {
		return birthday;
	}
	public String getCorp_name() {
		return corp_name;
	}
	public IdType getId_type() {
		return id_type;
	}
	public String getId_no() {
		return id_no;
	}
	public String getCorp_phone() {
		return corp_phone;
	}
	public String getCorp_fax() {
		return corp_fax;
	}
	public EmpPositionAttrType getCorp_post() {
		return corp_post;
	}
	public void setRelationship(Relationship relationship) {
		this.relationship = relationship;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setGender(Gender gender) {
		this.gender = gender;
	}
	public void setMobile_no(String mobile_no) {
		this.mobile_no = mobile_no;
	}
	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}
	public void setCorp_name(String corp_name) {
		this.corp_name = corp_name;
	}
	public void setId_type(IdType id_type) {
		this.id_type = id_type;
	}
	public void setId_no(String id_no) {
		this.id_no = id_no;
	}
	public void setCorp_phone(String corp_phone) {
		this.corp_phone = corp_phone;
	}
	public void setCorp_fax(String corp_fax) {
		this.corp_fax = corp_fax;
	}
	public void setCorp_post(EmpPositionAttrType corp_post) {
		this.corp_post = corp_post;
	}
	public IdType getContact_id_type() {
		return contact_id_type;
	}
	public String getContact_id_no() {
		return contact_id_no;
	}
	public void setContact_id_type(IdType contact_id_type) {
		this.contact_id_type = contact_id_type;
	}
	public void setContact_id_no(String contact_id_no) {
		this.contact_id_no = contact_id_no;
	}
	
	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
