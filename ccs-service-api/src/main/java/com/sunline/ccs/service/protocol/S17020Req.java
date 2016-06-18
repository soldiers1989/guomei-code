package com.sunline.ccs.service.protocol;

import java.io.Serializable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sunline.ppy.dictionary.enums.AddressType;
import com.sunline.ccs.param.def.enums.ExchType;
import com.sunline.ccs.param.def.enums.SendInd;

/**
 * 积分兑换
 * 
* @author fanghj
 * @date 2013-4-20  上午11:22:23
 * @version 1.0
 */
public class S17020Req implements Serializable {

	private static final long serialVersionUID = -6172593179676802969L;

	/**
     * 卡号
     */
    public String card_no ;

    /**
	 * 币种
	 */
	public String curr_cd;
    
    /**
     * 礼品编号
     */
    public String gift_nbr ;

    /**
     * 兑换方式
     */
    public ExchType exch_type ;
    
    /**
     * 兑换礼品数量
     */
    public Integer item_cnt;
    
    /**
     * 寄送地址方式
     */
    public SendInd send_ind;
    
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
    
	/**
     * 寄送地址类型
     */
    public AddressType addr_type ;

    public String getCard_no() {
		return card_no;
	}

	public void setCard_no(String card_no) {
		this.card_no = card_no;
	}

	public String getCurr_cd() {
		return curr_cd;
	}

	public void setCurr_cd(String curr_cd) {
		this.curr_cd = curr_cd;
	}
	
	public String getGift_nbr() {
		return gift_nbr;
	}

	public void setGift_nbr(String gift_nbr) {
		this.gift_nbr = gift_nbr;
	}

	public AddressType getAddr_type() {
		return addr_type;
	}

	public void setAddr_type(AddressType addr_type) {
		this.addr_type = addr_type;
	}

	public Integer getItem_cnt() {
		return item_cnt;
	}

	public void setItem_cnt(Integer item_cnt) {
		this.item_cnt = item_cnt;
	}


	public ExchType getExch_type() {
		return exch_type;
	}

	public void setExch_type(ExchType exch_type) {
		this.exch_type = exch_type;
	}

	public SendInd getSend_ind() {
		return send_ind;
	}

	public void setSend_ind(SendInd send_ind) {
		this.send_ind = send_ind;
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

