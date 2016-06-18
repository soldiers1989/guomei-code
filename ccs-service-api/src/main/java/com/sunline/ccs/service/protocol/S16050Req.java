package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sunline.ppy.dictionary.enums.IdType;

/**
 *第三方快捷支付开通验证
 *
* @author fanghj
 *@time 2014-4-28 上午10:04:49
 */
public class S16050Req implements Serializable{

	public static final String P_BlockCd = "blockCode";
    public static final String P_ActivateInd = "activeInd";
    public static final String P_ExpiryDate = "expiryDate";
	public static final String CURR_CD = "156";
	public static final String BLOCKCODE_C = "C";
	public static final String BLOCKCODE_T = "T";
	public static final String BLOCKCODE_P = "P";
	public static final String BLOCKCODE_R = "R";
	public static final String BLOCKCODE_S = "S";
	public static final String BLOCKCODE_L = "L";
	
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
	 * 客户姓名
	 */
	public String cust_name;
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
	public String getCust_name() {
		return cust_name;
	}
	public void setCust_name(String cust_name) {
		this.cust_name = cust_name;
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
