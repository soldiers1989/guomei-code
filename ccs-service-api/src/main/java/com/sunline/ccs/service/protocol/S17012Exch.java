package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sunline.ppy.dictionary.enums.AddressType;
import com.sunline.ccs.param.def.enums.ExchType;
import com.sunline.ccs.param.def.enums.SendInd;

public class S17012Exch implements Serializable {
	/**  
	 * serialVersionUID:TODO（用一句话描述这个变量表示什么）  
	 * @since 1.0.0  
	*/  
	    
	private static final long serialVersionUID = 5424008111933983859L;

	/**
	 * 兑换申请编号
	 */
	public String card_no;

	/**
	 * 礼品编号
	 */
	public String item_id;
	
	/**
	 * 礼品名称
	 */
	public String item_name;
	
	/**
	 * 兑换日期
	 */
	public Date exch_apply_date;
	
	
	/**
	 * 兑换礼品数量
	 */
	public Integer item_cnt;
	
	/**
	 * 兑换积分
	 */
	public Integer exch_bonus;
	
	/**
	 * 礼品单价
	 */
	public BigDecimal item_price;
	
	/**
	 * 兑换方式
	 */
	public ExchType exch_type;
	
	/**
	 * 寄送地址方式
	 */
	public SendInd send_ind;

	/**
	 * 寄送地址类型
	 */
	public AddressType addr_type;
	
	 /**
     * 收件人姓名
     */
    public String receive_name;
    /**
     * 收件人手机
     */
    public String receive_mobile;
    /**
     * 收件人座机
     */
    public String receive_phone;
    /**
     * 收件人地址
     */
    public String receive_address;    
	
    
		
	
	public SendInd getSend_ind() {
		return send_ind;
	}



	public void setSend_ind(SendInd send_ind) {
		this.send_ind = send_ind;
	}



	public String getCard_no() {
		return card_no;
	}



	public void setCard_no(String card_no) {
		this.card_no = card_no;
	}



	public String getItem_id() {
		return item_id;
	}



	public void setItem_id(String item_id) {
		this.item_id = item_id;
	}



	public String getItem_name() {
		return item_name;
	}



	public void setItem_name(String item_name) {
		this.item_name = item_name;
	}



	public Date getExch_apply_date() {
		return exch_apply_date;
	}



	public void setExch_apply_date(Date exch_apply_date) {
		this.exch_apply_date = exch_apply_date;
	}



	public Integer getItem_cnt() {
		return item_cnt;
	}



	public void setItem_cnt(Integer item_cnt) {
		this.item_cnt = item_cnt;
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



	public ExchType getExch_type() {
		return exch_type;
	}



	public void setExch_type(ExchType exch_type) {
		this.exch_type = exch_type;
	}



	public AddressType getAddr_type() {
		return addr_type;
	}



	public void setAddr_type(AddressType addr_type) {
		this.addr_type = addr_type;
	}



	public String getReceive_name() {
		return receive_name;
	}



	public void setReceive_name(String receive_name) {
		this.receive_name = receive_name;
	}



	public String getReceive_mobile() {
		return receive_mobile;
	}



	public void setReceive_mobile(String receive_mobile) {
		this.receive_mobile = receive_mobile;
	}



	public String getReceive_phone() {
		return receive_phone;
	}



	public void setReceive_phone(String receive_phone) {
		this.receive_phone = receive_phone;
	}



	public String getReceive_address() {
		return receive_address;
	}



	public void setReceive_address(String receive_address) {
		this.receive_address = receive_address;
	}



	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
