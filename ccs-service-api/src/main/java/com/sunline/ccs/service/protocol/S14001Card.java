package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sunline.ppy.dictionary.enums.BscSuppIndicator;
import com.sunline.ppy.dictionary.enums.Indicator;

public class S14001Card implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String P_Cardno = "cardNbr";
    public static final String P_ProductCd = "productCode";
    public static final String P_ExpiryDate = "origExpiryDate";
    public static final String P_ActiveInd = "activeInd";
    public static final String P_ActivateDate = "activateDate";
    public static final String P_BlockCd = "blockCode";
    public static final String P_FirstCardno = "firstCardNbr";
    public static final String P_LastExpiryDate = "oldExpiryDate";

    /**
     * 附卡卡号
     */
    public String supp_card_no;
    /**
     * 持卡人姓名
     */
    public String cardholder_name;
    /**
     * 卡产品名称
     */
    public String product_name;
    /**
     * 主副卡标志
     */
    public BscSuppIndicator bsc_supp_ind;
    /**
     * 卡片有效日期
     */
    public Date card_expire_date;
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
     * 是否发新卡
     */
    public Indicator new_card_issue_ind;
    /**
     * 是否存在查询密码
     */
    public Indicator q_pin_exist_ind;
    /**
     * 是否存在交易密码
     */
    public Indicator p_pin_exist_ind;

    public String getSupp_card_no() {
	return supp_card_no;
    }

    public void setSupp_card_no(String supp_card_no) {
	this.supp_card_no = supp_card_no;
    }

    public String getCardholder_name() {
	return cardholder_name;
    }

    public void setCardholder_name(String cardholder_name) {
	this.cardholder_name = cardholder_name;
    }

    public String getProduct_name() {
	return product_name;
    }

    public void setProduct_name(String product_name) {
	this.product_name = product_name;
    }

    public BscSuppIndicator getBsc_supp_ind() {
	return bsc_supp_ind;
    }

    public void setBsc_supp_ind(BscSuppIndicator bsc_supp_ind) {
	this.bsc_supp_ind = bsc_supp_ind;
    }

    public Date getCard_expire_date() {
	return card_expire_date;
    }

    public void setCard_expire_date(Date card_expire_date) {
	this.card_expire_date = card_expire_date;
    }

    public Indicator getActivate_ind() {
	return activate_ind;
    }

    public void setActivate_ind(Indicator activate_ind) {
	this.activate_ind = activate_ind;
    }

    public Date getActivate_date() {
	return activate_date;
    }

    public void setActivate_date(Date activate_date) {
	this.activate_date = activate_date;
    }

    public String getBlock_code() {
	return block_code;
    }

    public void setBlock_code(String block_code) {
	this.block_code = block_code;
    }

    public Indicator getNew_card_issue_ind() {
	return new_card_issue_ind;
    }

    public void setNew_card_issue_ind(Indicator new_card_issue_ind) {
	this.new_card_issue_ind = new_card_issue_ind;
    }

    public Indicator getQ_pin_exist_ind() {
	return q_pin_exist_ind;
    }

    public void setQ_pin_exist_ind(Indicator q_pin_exist_ind) {
	this.q_pin_exist_ind = q_pin_exist_ind;
    }

    public Indicator getP_pin_exist_ind() {
	return p_pin_exist_ind;
    }

    public void setP_pin_exist_ind(Indicator p_pin_exist_ind) {
	this.p_pin_exist_ind = p_pin_exist_ind;
    }

    public String toString() {
	return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
