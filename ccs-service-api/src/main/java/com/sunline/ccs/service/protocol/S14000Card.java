package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sunline.ppy.dictionary.enums.BscSuppIndicator;
import com.sunline.ppy.dictionary.enums.Indicator;

public class S14000Card implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public static final String P_Cardno = "cardNbr";
	public static final String p_EmbName = "embName";
	public static final String P_ProductCd = "productCode";
	public static final String P_ExpiryDate = "origExpiryDate";
	public static final String P_ActiveInd = "activeInd";
	public static final String P_ActivateDate = "activateDate";
	public static final String P_BlockCd = "blockCode";
	public static final String P_PIN_EXIST_IND = "pPinExistInd";
	public static final String Q_PIN_EXIST_IND = "qPinExistInd";
	public static final String NEW_CARD_ISSUE_IND = "newCardIssueInd";
	public static final String P_FirstCardno = "firstCardNbr";
	public static final String P_LastExpiryDate = "oldExpiryDate";
	
	/**
	 * 卡号
	 */
	public String card_no;
	/**
	 * 持卡人姓名
	 */
	public String cardholder_name;
	/**
	 * 卡产品名称
	 */
	public String product_name;
	/**
	 * 卡片有效日期
	 */
	public Date card_expire_date;
	/**
	 * 主副卡标志
	 */
	public BscSuppIndicator bsc_supp_ind;
	/**
	 * 是否已激活
	 */
	public Indicator activate_ind;
	/**
	 * 激活日期
	 */
	public Date activate_date;
	/**
	 * 锁定码
	 */
	public String block_code;

	/**
	 * 是否设置交易密码
	 */
	public Indicator p_pin_exist_ind;
	/**
	 * 是否设置查询密码
	 */
	public Indicator q_pin_exist_ind;
	/**
	 * 是否新换卡
	 */
	public Indicator new_card_issue_ind;

	public String getCard_no() {
		return card_no;
	}
	public String getProduct_name() {
		return product_name;
	}
	public Date getCard_expire_date() {
		return card_expire_date;
	}
	public BscSuppIndicator getBsc_supp_ind() {
		return bsc_supp_ind;
	}
	public Indicator getActivate_ind() {
		return activate_ind;
	}
	public Date getActivate_date() {
		return activate_date;
	}
	public String getBlock_code() {
		return block_code;
	}
	public void setCard_no(String card_no) {
		this.card_no = card_no;
	}
	public void setProduct_name(String product_name) {
		this.product_name = product_name;
	}
	public void setCard_expire_date(Date card_expire_date) {
		this.card_expire_date = card_expire_date;
	}
	public void setBsc_supp_ind(BscSuppIndicator bsc_supp_ind) {
		this.bsc_supp_ind = bsc_supp_ind;
	}
	public void setActivate_ind(Indicator activate_ind) {
		this.activate_ind = activate_ind;
	}
	public void setActivate_date(Date activate_date) {
		this.activate_date = activate_date;
	}
	public void setBlock_code(String block_code) {
		this.block_code = block_code;
	}
	
	public Indicator getP_pin_exist_ind() {
		return p_pin_exist_ind;
	}
	public void setP_pin_exist_ind(Indicator p_pin_exist_ind) {
		this.p_pin_exist_ind = p_pin_exist_ind;
	}
	public Indicator getQ_pin_exist_ind() {
		return q_pin_exist_ind;
	}
	public void setQ_pin_exist_ind(Indicator q_pin_exist_ind) {
		this.q_pin_exist_ind = q_pin_exist_ind;
	}
	public Indicator getNew_card_issue_ind() {
		return new_card_issue_ind;
	}
	public void setNew_card_issue_ind(Indicator new_card_issue_ind) {
		this.new_card_issue_ind = new_card_issue_ind;
	}
	
	public String getCardholder_name() {
		return cardholder_name;
	}
	public void setCardholder_name(String cardholder_name) {
		this.cardholder_name = cardholder_name;
	}
	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
