package com.sunline.ccs.service.protocol;

import java.io.Serializable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sunline.ppy.dictionary.enums.PasswordType;

/**
 * 密码设定
 * 
* @author fanghj
 * 
 */
public class S14090Req implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5395145254251806757L;
	/**
	 * 卡号
	 */
	public String card_no;
	/**
	 * 密码类型
	 */
	public PasswordType passwdtype;
	/**
	 * 新密码密文
	 */
	public String newpin;
	public String getCard_no() {
		return card_no;
	}
	public void setCard_no(String card_no) {
		this.card_no = card_no;
	}
	public PasswordType getPasswdtype() {
		return passwdtype;
	}
	public void setPasswdtype(PasswordType passwdtype) {
		this.passwdtype = passwdtype;
	}
	public String getNewpin() {
		return newpin;
	}
	public void setNewpin(String newpin) {
		this.newpin = newpin;
	}
	
	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
