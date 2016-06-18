package com.sunline.ccs.service.protocol;

import java.io.Serializable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * 账单日天数
 * 
* @author fanghj
 * @date 2013-6-4  上午11:11:32
 * @version 1.0
 */
public class S12111Item implements Serializable{

	private static final long serialVersionUID = -884332725897934888L;

	/*
	 * 账单日天数
	 */
	public Integer cycle_day;

	public Integer getCycle_day() {
		return cycle_day;
	}

	public void setCycle_day(Integer cycle_day) {
		this.cycle_day = cycle_day;
	}



	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
