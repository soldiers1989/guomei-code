package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sunline.ppy.dictionary.enums.EducationType;
import com.sunline.ppy.dictionary.enums.EmpPositionAttrType;
import com.sunline.ppy.dictionary.enums.Gender;
import com.sunline.ppy.dictionary.enums.IdType;
import com.sunline.ppy.dictionary.enums.MaritalStatus;
import com.sunline.ppy.dictionary.enums.OccupationType;

public class S11010Resp implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * 证件号码
	 */
	public String id_no;
	/**
	 * 证件类型
	 */
	public IdType id_type;
	/**
	 * 称谓
	 */
	public EmpPositionAttrType title;
	/**
	 * 姓名
	 */
	public String name;
	/**
	 * 性别
	 */
	public Gender gender;
	/**
	 * 生日
	 */
	public Date birthday;
	/**
	 * 职业
	 */
	public OccupationType occupation;
	/**
	 * 本行员工号
	 */
	public String bankmember_no;
	/**
	 * 国籍
	 */
	public String nationality;
	/**
	 * 婚姻状况
	 */
	public MaritalStatus marital_status;
	/**
	 * 教育状况
	 */
	public EducationType qualification;
	/**
	 * 家庭电话
	 */
	public String home_phone;
	/**
	 * 移动电话
	 */
	public String mobile_no;
	/**
	 * 电子邮箱
	 */
	public String email;
	/**
	 * 创建日期
	 */
	public Date setup_date;
	/**
	 * 公司名称
	 */
	public String corp_name;
	/**
	 * 凸印姓名
	 */
	public String emb_name;
	
	/**
	 * 行内客户号
	 */
	public String bank_customer_id;
	
	public String getId_no() {
		return id_no;
	}
	public IdType getId_type() {
		return id_type;
	}
	public EmpPositionAttrType getTitle() {
		return title;
	}
	public String getName() {
		return name;
	}
	public Gender getGender() {
		return gender;
	}
	public Date getBirthday() {
		return birthday;
	}
	public OccupationType getOccupation() {
		return occupation;
	}
	public String getBankmember_no() {
		return bankmember_no;
	}
	public String getNationality() {
		return nationality;
	}
	public MaritalStatus getMarital_status() {
		return marital_status;
	}
	public EducationType getQualification() {
		return qualification;
	}
	public String getHome_phone() {
		return home_phone;
	}
	public String getMobile_no() {
		return mobile_no;
	}
	public String getEmail() {
		return email;
	}
	public Date getSetup_date() {
		return setup_date;
	}
	public String getCorp_name() {
		return corp_name;
	}
	public String getEmb_name() {
		return emb_name;
	}
	public void setId_no(String id_no) {
		this.id_no = id_no;
	}
	public void setId_type(IdType id_type) {
		this.id_type = id_type;
	}
	public void setTitle(EmpPositionAttrType title) {
		this.title = title;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setGender(Gender gender) {
		this.gender = gender;
	}
	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}
	public void setOccupation(OccupationType occupation) {
		this.occupation = occupation;
	}
	public void setBankmember_no(String bankmember_no) {
		this.bankmember_no = bankmember_no;
	}
	public void setNationality(String nationality) {
		this.nationality = nationality;
	}
	public void setMarital_status(MaritalStatus marital_status) {
		this.marital_status = marital_status;
	}
	public void setQualification(EducationType qualification) {
		this.qualification = qualification;
	}
	public void setHome_phone(String home_phone) {
		this.home_phone = home_phone;
	}
	public void setMobile_no(String mobile_no) {
		this.mobile_no = mobile_no;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public void setSetup_date(Date setup_date) {
		this.setup_date = setup_date;
	}
	public void setCorp_name(String corp_name) {
		this.corp_name = corp_name;
	}
	public void setEmb_name(String emb_name) {
		this.emb_name = emb_name;
	}
	public String getBank_customer_id() {
		return bank_customer_id;
	}
	public void setBank_customer_id(String bank_customer_id) {
		this.bank_customer_id = bank_customer_id;
	}
	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
	
}
