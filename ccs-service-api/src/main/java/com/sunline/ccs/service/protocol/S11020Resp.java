package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sunline.ppy.dictionary.enums.BscSuppIndicator;
import com.sunline.ppy.dictionary.enums.IdType;
import com.sunline.ppy.dictionary.enums.Indicator;

public class S11020Resp implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 手机号码
     */
    public String mobileno;
	/**
	 * 卡号后四位
	 */
    public String card_no_lastfour;
    /**
     * 证件类型
     */
    public IdType id_type;
    /**
     * 证件号码
     */
    public String id_no;
    /**
     * 卡号
     */
    public String card_no;
    /**
     * 卡产品名称
     */
    public String product_name;
    /**
     * 卡片有效日期
     */
    public Date card_expire_date;
    /**
     * 主附卡标志
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

	public String getMobileno() {
		return mobileno;
	}

	public void setMobileno(String mobileno) {
		this.mobileno = mobileno;
	}

	public String getCard_no_lastfour() {
		return card_no_lastfour;
	}

	public void setCard_no_lastfour(String card_no_lastfour) {
		this.card_no_lastfour = card_no_lastfour;
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

	public String getCard_no() {
		return card_no;
	}

	public void setCard_no(String card_no) {
		this.card_no = card_no;
	}

	public String getProduct_name() {
		return product_name;
	}

	public void setProduct_name(String product_name) {
		this.product_name = product_name;
	}

	public Date getCard_expire_date() {
		return card_expire_date;
	}

	public void setCard_expire_date(Date card_expire_date) {
		this.card_expire_date = card_expire_date;
	}

	public BscSuppIndicator getBsc_supp_ind() {
		return bsc_supp_ind;
	}

	public void setBsc_supp_ind(BscSuppIndicator bsc_supp_ind) {
		this.bsc_supp_ind = bsc_supp_ind;
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

	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
