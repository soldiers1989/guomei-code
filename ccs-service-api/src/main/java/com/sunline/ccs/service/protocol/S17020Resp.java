package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.math.BigDecimal;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sunline.ppy.dictionary.enums.AddressType;
import com.sunline.ccs.param.def.enums.ExchType;
import com.sunline.ccs.param.def.enums.SendInd;

/**
 * 兑换积分
 * 
* @author fanghj
 * @date 2013-4-20  上午11:24:31
 * @version 1.0
 */
public class S17020Resp implements Serializable {

	private static final long serialVersionUID = -2708510549831554089L;

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
     * 兑换礼品数量
     */
    public Integer item_cnt;
    
    /**
     * 兑换方式
     */
    public ExchType exch_type ;
    
    
    /**
     * 寄送地址方式
     */
    public SendInd send_ind;
    
    /**
     * 寄送地址类型
     */
    public AddressType addr_type ;
    
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
     * 兑换积分
     */
    public Integer exch_bonus;
    
    /**
     * 当期兑换积分
     */
    public Integer curr_exch_point;
    
    /**
     * 积分余额
     */
    public Integer point_bal;
    
    /**
     * 商品总价
     */
    public BigDecimal item_price ;

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

	public BigDecimal getItem_price() {
		return item_price;
	}

	public void setItem_price(BigDecimal item_price) {
		this.item_price = item_price;
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

	

	public Integer getExch_bonus() {
		return exch_bonus;
	}

	public void setExch_bonus(Integer exch_bonus) {
		this.exch_bonus = exch_bonus;
	}

	public Integer getCurr_exch_point() {
		return curr_exch_point;
	}

	public void setCurr_exch_point(Integer curr_exch_point) {
		this.curr_exch_point = curr_exch_point;
	}

	public Integer getPoint_bal() {
		return point_bal;
	}

	public void setPoint_bal(Integer point_bal) {
		this.point_bal = point_bal;
	}
	
	

	public AddressType getAddr_type() {
		return addr_type;
	}

	public void setAddr_type(AddressType addr_type) {
		this.addr_type = addr_type;
	}

	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}


}

