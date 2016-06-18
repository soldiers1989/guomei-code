
package com.sunline.ccs.service.protocol;

import java.io.Serializable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * 账单分期参数查询请求接口
* @author fanghj
 *2013-11-20上午10:18:10
 *version 1.0
 */
public class S13001Req implements Serializable{

	private static final long serialVersionUID = -8272540938441251088L;
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
