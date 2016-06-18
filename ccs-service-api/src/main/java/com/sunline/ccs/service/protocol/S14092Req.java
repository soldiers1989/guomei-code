package com.sunline.ccs.service.protocol;

import java.io.Serializable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sunline.ppy.dictionary.enums.UnlockPwdType;

/**
 * 客服/内管解锁服务
 * 
* @author fanghj
 *
 */
public class S14092Req implements Serializable {


	/**
	 * 
	 */
	private static final long serialVersionUID = 1264365592056832939L;

	/**
	 * 卡号
	 */
	public String card_no;

	/**
	 * 有效期
	 */
	public String expire_date;
	
	/**
	 * 密码类型<br>
	 * Q：查询密码<br>
	 * P：交易密码
	 * CVV2
	 * CVV
	 * ICVV
	 */
	
	public UnlockPwdType error_type;

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

	public UnlockPwdType getError_type() {
		return error_type;
	}

	public void setError_type(UnlockPwdType error_type) {
		this.error_type = error_type;
	}

	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
