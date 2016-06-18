package com.sunline.ccs.service.protocol;

import java.io.Serializable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * 卡片冻结/解冻
 * 
* @author fanghj
 * 
 */
public class S14180Req implements Serializable  {

	/**
	 * 
	 */
	private static final long serialVersionUID = -389724758874682776L;

	/**
	 * 卡号
	 */
	public String card_no;

	/**
	 * 功能码<br>
	 * 0 : 冻结<br>
	 * 1 : 解冻<br>
	 */
	public String opt;

	public String getCard_no() {
		return card_no;
	}

	public void setCard_no(String card_no) {
		this.card_no = card_no;
	}

	/**
	 * 功能码<br>
	 * 0 : 冻结<br>
	 * 1 : 解冻<br>
	 */
	public String getOpt() {
		return opt;
	}

	/**
	 * 功能码<br>
	 * 0 : 冻结<br>
	 * 1 : 解冻<br>
	 */
	public void setOpt(String opt) {
		this.opt = opt;
	}
	
	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
