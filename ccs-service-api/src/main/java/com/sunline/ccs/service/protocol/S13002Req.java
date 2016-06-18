package com.sunline.ccs.service.protocol;

import java.io.Serializable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 *现金分期参数查询请求接口
* @author fanghj
 *@time 2014-6-4 下午9:40:07
 */
public class S13002Req implements Serializable{

	private static final long serialVersionUID = 6506966195696572893L;
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
