package com.sunline.ccs.service.protocol;

import java.io.Serializable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 *积分兑换(礼品信息在行内时，用此接口)
* @author fanghj
 *@time 2014-11-25 上午10:33:46
 */
public class S17021Resp implements Serializable{

	private static final long serialVersionUID = 1L;
	
	/**
     * 卡号
     */
    public String card_no ;

    /**
	 * 币种
	 */
	public String curr_cd;
	
	/**
	 * 兑换积分
	 */
	public Integer exch_bonus;
	
	/**
	 * 交易参考号
	 */
	public String  ref_nbr;
	
    /**
     * 当期兑换积分
     */
    public Integer curr_exch_point;
    
    /**
     * 积分余额
     */
    public Integer point_bal;

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

	public Integer getExch_bonus() {
		return exch_bonus;
	}

	public void setExch_bonus(Integer exch_bonus) {
		this.exch_bonus = exch_bonus;
	}

	public String getRef_nbr() {
		return ref_nbr;
	}

	public void setRef_nbr(String ref_nbr) {
		this.ref_nbr = ref_nbr;
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
    
	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
