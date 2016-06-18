package com.sunline.ccs.service.protocol;

import java.io.Serializable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * 账单日列表查询请求
 * 
* @author fanghj
 * @date 2013-6-4  上午11:04:53
 * @version 1.0
 */
public class S12111Req implements Serializable{

	private static final long serialVersionUID = -6545960808913317058L;

	/**
	 * 卡号
	 */
	public String card_no;

	public String getCard_no() {
		return card_no;
	}

	public void setCard_no(String card_no) {
		this.card_no = card_no;
	}
	
	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
