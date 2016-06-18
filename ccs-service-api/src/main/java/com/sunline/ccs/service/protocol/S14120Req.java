package com.sunline.ccs.service.protocol;

import java.io.Serializable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * 销卡/销卡撤销
* @author fanghj
 *
 */
public class S14120Req implements Serializable  {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8845988879802249051L;

	/**
	 * 卡号
	 */
	public String card_no;
	
	/**
	 * 操作码<br>
	 * 0：销卡 <br>
	 * 1：销卡撤销
	 * 
	 * @return
	 */
	public String opt;

	public String getCard_no() {
		return card_no;
	}

	public void setCard_no(String card_no) {
		this.card_no = card_no;
	}
	/**
	 * 操作码<br>
	 * 0：销卡 <br>
	 * 1：销卡撤销
	 * 
	 * @return
	 */
	public String getOpt() {
		return opt;
	}
	
	/**
	 * 操作码<br>
	 * 0：销卡 <br>
	 * 1：销卡撤销
	 * 
	 * @return
	 */
	public void setOpt(String opt) {
		this.opt = opt;
	}
	
	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
