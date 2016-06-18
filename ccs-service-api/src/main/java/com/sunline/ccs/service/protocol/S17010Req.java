package com.sunline.ccs.service.protocol;

import java.io.Serializable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * 积分查询
 * 
* @author fanghj
 * @date 2013-4-20 上午10:56:42
 * @version 1.0
 */
public class S17010Req implements Serializable {

	private static final long serialVersionUID = -8798799317216621757L;
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
