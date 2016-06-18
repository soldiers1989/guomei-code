package com.sunline.ccs.service.protocol;

import java.io.Serializable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * 固定费用查询请求接口
 * 
* @author fanghj
 * @date 2013-6-5  上午10:44:54
 * @version 1.0
 */
public class S14021Req implements Serializable {

	private static final long serialVersionUID = 4107450999992295359L;
	/**
     * 卡号
     */
    public String card_no ;
    
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

