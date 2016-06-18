package com.sunline.ccs.service.protocol;

import java.io.Serializable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sunline.ppy.dictionary.enums.StmtMediaType;

public class S12021Resp implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
	 * 卡号
	 */
	public String card_no;
	/**
	 * 账单介质类型
	 */
	public StmtMediaType stmt_media_type;
	/**
	 * 电子邮箱
	 */
	public String email;
	
	
	public String getCard_no() {
		return card_no;
	}
	public StmtMediaType getStmt_media_type() {
		return stmt_media_type;
	}
	public String getEmail() {
		return email;
	}
	public void setCard_no(String card_no) {
		this.card_no = card_no;
	}
	public void setStmt_media_type(StmtMediaType stmt_media_type) {
		this.stmt_media_type = stmt_media_type;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	
	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
	
}
