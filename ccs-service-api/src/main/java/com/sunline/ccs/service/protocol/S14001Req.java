package com.sunline.ccs.service.protocol;

import java.io.Serializable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class S14001Req implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
	 * 卡号
	 */
	public String card_no;
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
	public Integer getFirstrow() {
		return firstrow;
	}
	public Integer getLastrow() {
		return lastrow;
	}
	public void setCard_no(String card_no) {
		this.card_no = card_no;
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
