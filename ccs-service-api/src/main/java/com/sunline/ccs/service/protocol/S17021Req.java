package com.sunline.ccs.service.protocol;

import java.io.Serializable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 *积分兑换(礼品信息在行内时，用此接口)
* @author fanghj
 *@time 2014-11-25 上午10:24:15
 */
public class S17021Req implements Serializable{

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
	 * 操作码
	 */
	public String opt;
	
	/**
	 * 兑换积分
	 */
	public Integer exch_bonus;
	
	/**
	 * 交易参考号
	 */
	public String  ref_nbr;

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

	public String getOpt() {
		return opt;
	}

	public void setOpt(String opt) {
		this.opt = opt;
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
	
	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
