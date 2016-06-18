
package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * 授权交易历史查询
* @author fanghj
 *2013-11-27上午11:11:34
 *version 1.0
 */
public class S13020Req implements Serializable{

	private static final long serialVersionUID = -7678595703504259315L;

	/**
	 * 卡号
	 */
	public String card_no;
	/**
	 * 币种
	 */
	public String curr_cd;
	/**
	 * 起始日期
	 */
	public Date start_date;
	/**
	 * 截止日期
	 */
	public Date end_date;
	/**
	 * 开始位置
	 */
	public Integer firstrow;
	/**
	 * 结束位置
	 */
	public Integer lastrow;
	
	
	public String getCard_no() {
		return card_no;
	}
	public String getCurr_cd() {
		return curr_cd;
	}
	public Date getStart_date() {
		return start_date;
	}
	public Date getEnd_date() {
		return end_date;
	}
	public Integer getFirstrow() {
		return firstrow;
	}
	public Integer getLastrow() {
		return lastrow;
	}
	public void setCard_no(String card_no) {
		this.card_no = card_no;
	}
	public void setCurr_cd(String curr_cd) {
		this.curr_cd = curr_cd;
	}
	public void setStart_date(Date start_date) {
		this.start_date = start_date;
	}
	public void setEnd_date(Date end_date) {
		this.end_date = end_date;
	}
	public void setFirstrow(Integer firstrow) {
		this.firstrow = firstrow;
	}
	public void setLastrow(Integer lastrow) {
		this.lastrow = lastrow;
	}
	
	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
