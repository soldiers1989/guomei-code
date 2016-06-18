package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class S15010Req implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
	 * 卡号
	 */
	public String card_no;
	/**
	 * 币种
	 */
	public String curr_cd;
	/**
	 * 功能码
	 */
	public String opt;
	/**
	 * 临时额度
	 */
	public BigDecimal temp_limit;
	/**
	 * 临时额度开始日期
	 */
	public Date temp_limit_begin_date;
	/**
	 * 临时额度结束日期
	 */
	public Date temp_limit_end_date;
	
	
	public String getCard_no() {
		return card_no;
	}
	public String getCurr_cd() {
		return curr_cd;
	}
	public String getOpt() {
		return opt;
	}
	public BigDecimal getTemp_limit() {
		return temp_limit;
	}
	public Date getTemp_limit_begin_date() {
		return temp_limit_begin_date;
	}
	public Date getTemp_limit_end_date() {
		return temp_limit_end_date;
	}
	public void setCard_no(String card_no) {
		this.card_no = card_no;
	}
	public void setCurr_cd(String curr_cd) {
		this.curr_cd = curr_cd;
	}
	public void setOpt(String opt) {
		this.opt = opt;
	}
	public void setTemp_limit(BigDecimal temp_limit) {
		this.temp_limit = temp_limit;
	}
	public void setTemp_limit_begin_date(Date temp_limit_begin_date) {
		this.temp_limit_begin_date = temp_limit_begin_date;
	}
	public void setTemp_limit_end_date(Date temp_limit_end_date) {
		this.temp_limit_end_date = temp_limit_end_date;
	}
	
	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
