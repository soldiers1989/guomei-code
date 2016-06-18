package com.sunline.ccs.service.protocol;

import java.io.Serializable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * 卡片激活
* @author fanghj
 *
 */
public class S14040Req implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8235725514556615274L;

	/**
	 * 卡号
	 */
	public String card_no;
	
	/**
	 * 有效期
	 */
	public String expire_date;
	
	/**
	 * cvv2
	 */
	public String cvv2;

	public String getCard_no() {
		return card_no;
	}

	public void setCard_no(String card_no) {
		this.card_no = card_no;
	}

	public String getExpire_date() {
		return expire_date;
	}

	public void setExpire_date(String expire_date) {
		this.expire_date = expire_date;
	}

	public String getCvv2() {
		return cvv2;
	}

	public void setCvv2(String cvv2) {
		this.cvv2 = cvv2;
	}
	
	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
