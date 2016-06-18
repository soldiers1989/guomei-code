package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sunline.ppy.dictionary.enums.Indicator;

public class S15010Resp implements Serializable {

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
	/**
	 * 是否存在外币标识
	 */
	public Indicator dual_curr_ind;
	/**
	 * 外币币种
	 */
	public String dual_curr_cd;
	
	/**
	 * 最大可调临时额度
	 * 
	 */
	public BigDecimal max_temp_limit;
	
	/**
	 * 最小可调临时额度
	 * 
	 */
	public BigDecimal min_temp_limit;
	/**
	 * 临额最大有效月数
	 */
	public Integer temp_limit_max_mths;
	
	public String getCard_no() {
		return card_no;
	}
	public String getCurr_cd() {
		return curr_cd;
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
	public Indicator getDual_curr_ind() {
		return dual_curr_ind;
	}
	public String getDual_curr_cd() {
		return dual_curr_cd;
	}
	public void setCard_no(String card_no) {
		this.card_no = card_no;
	}
	public void setCurr_cd(String curr_cd) {
		this.curr_cd = curr_cd;
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
	public void setDual_curr_ind(Indicator dual_curr_ind) {
		this.dual_curr_ind = dual_curr_ind;
	}
	public void setDual_curr_cd(String dual_curr_cd) {
		this.dual_curr_cd = dual_curr_cd;
	}
	
	public BigDecimal getMax_temp_limit() {
		return max_temp_limit;
	}
	public void setMax_temp_limit(BigDecimal max_temp_limit) {
		this.max_temp_limit = max_temp_limit;
	}
	public BigDecimal getMin_temp_limit() {
		return min_temp_limit;
	}
	public void setMin_temp_limit(BigDecimal min_temp_limit) {
		this.min_temp_limit = min_temp_limit;
	}
	
	public Integer getTemp_limit_max_mths() {
		return temp_limit_max_mths;
	}
	public void setTemp_limit_max_mths(Integer temp_limit_max_mths) {
		this.temp_limit_max_mths = temp_limit_max_mths;
	}
	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
