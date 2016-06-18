package com.sunline.ccs.service.protocol;

import java.io.Serializable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class S11020Req implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 手机号
	 */
	public String mobileno;
	/**
	 * 卡号后四位
	 */
	public String card_no_lastfour;

	public String getMobileno() {
		return mobileno;
	}

	public void setMobileno(String mobileno) {
		this.mobileno = mobileno;
	}

	public String getCard_no_lastfour() {
		return card_no_lastfour;
	}

	public void setCard_no_lastfour(String card_no_lastfour) {
		this.card_no_lastfour = card_no_lastfour;
	}
	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
