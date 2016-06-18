package com.sunline.ccs.service.protocol;

import java.io.Serializable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class S12040Req implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 卡号
	 */
	public String card_no;
	/**
	 * 减免年数
	 */
	public Integer wave_years;

	public String getCard_no() {
		return card_no;
	}

	public void setCard_no(String card_no) {
		this.card_no = card_no;
	}

	public Integer getWave_years() {
		return wave_years;
	}

	public void setWave_years(Integer wave_years) {
		this.wave_years = wave_years;
	}
	
	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
