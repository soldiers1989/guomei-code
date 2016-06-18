package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.math.BigDecimal;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class S15050Resp implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
	 * 主卡卡号
	*/
	public String card_no;
	/**
	 * 币种
	 */
	public String curr_cd;
	/**
	 * 取现额度比例
	 */
	public BigDecimal cash_limit_rt;
	
	
	public String getCard_no() {
		return card_no;
	}
	public String getCurr_cd() {
		return curr_cd;
	}
	public BigDecimal getCash_limit_rt() {
		return cash_limit_rt;
	}
	public void setCard_no(String card_no) {
		this.card_no = card_no;
	}
	public void setCurr_cd(String curr_cd) {
		this.curr_cd = curr_cd;
	}
	public void setCash_limit_rt(BigDecimal cash_limit_rt) {
		this.cash_limit_rt = cash_limit_rt;
	}
	
	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
