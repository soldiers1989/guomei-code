package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.math.BigDecimal;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class S17030Item implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -859645005652043743L;

	/**
     * 礼品编号
     */
    public String item_id ;

    /**
     * 礼品品牌
     */
    public String item_brand ;

    /**
     * 品牌说明
     */
    public String item_brand_desc ;

    /**
     * 礼品名称
     */
    public String item_name ;

    /**
     * 礼品说明
     */
    public String item_desc ;

    /**
     * 兑换积分
     */
    public Integer exch_bonus ;

    /**
     * 礼品总价
     */
    public BigDecimal item_price ;

	public String getItem_id() {
		return item_id;
	}

	public void setItem_id(String item_id) {
		this.item_id = item_id;
	}

	public String getItem_brand() {
		return item_brand;
	}

	public void setItem_brand(String item_brand) {
		this.item_brand = item_brand;
	}

	public String getItem_brand_desc() {
		return item_brand_desc;
	}

	public void setItem_brand_desc(String item_brand_desc) {
		this.item_brand_desc = item_brand_desc;
	}

	public String getItem_name() {
		return item_name;
	}

	public void setItem_name(String item_name) {
		this.item_name = item_name;
	}

	public String getItem_desc() {
		return item_desc;
	}

	public void setItem_desc(String item_desc) {
		this.item_desc = item_desc;
	}

	public Integer getExch_bonus() {
		return exch_bonus;
	}

	public void setExch_bonus(Integer exch_bonus) {
		this.exch_bonus = exch_bonus;
	}

	public BigDecimal getItem_price() {
		return item_price;
	}

	public void setItem_price(BigDecimal item_price) {
		this.item_price = item_price;
	}
    
	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
