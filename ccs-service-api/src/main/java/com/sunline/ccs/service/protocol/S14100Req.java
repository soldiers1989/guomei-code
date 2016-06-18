package com.sunline.ccs.service.protocol;

import java.io.Serializable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sunline.ppy.dictionary.enums.PasswordType;

/**
 * 卡片交易/查询密码锁定解除
 * 
* @author fanghj
 * 
 */
public class S14100Req implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3724448776161510760L;

	/**
	 * 卡号
	 */
	public String card_no;

	/**
	 * 密码类型<br>
	 * Q：查询密码<br>
	 * P：交易密码
	 */
	public PasswordType passwdtype;

	public String getCard_no() {
		return card_no;
	}

	public void setCard_no(String card_no) {
		this.card_no = card_no;
	}

	/**
	 * 密码类型<br>
	 * Q：查询密码<br>
	 * P：交易密码
	 */
	public PasswordType getPasswdtype() {
		return passwdtype;
	}

	/**
	 * 密码类型<br>
	 * Q：查询密码<br>
	 * P：交易密码
	 */
	public void setPasswdtype(PasswordType passwdtype) {
		this.passwdtype = passwdtype;
	}
	
	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
