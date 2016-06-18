package com.sunline.ccs.service.protocol;

import java.io.Serializable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class S11050Resp implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
	 * 卡号
	 */
	public String card_no;
	/**
	 * 预留问题
	 */
	public String obligate_question;
	/**
	 * 预留答案
	 */
	public String obligate_answer;
	
	
	public String getCard_no() {
		return card_no;
	}
	public String getObligate_question() {
		return obligate_question;
	}
	public String getObligate_answer() {
		return obligate_answer;
	}
	public void setCard_no(String card_no) {
		this.card_no = card_no;
	}
	public void setObligate_question(String obligate_question) {
		this.obligate_question = obligate_question;
	}
	public void setObligate_answer(String obligate_answer) {
		this.obligate_answer = obligate_answer;
	}
	
	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
	
}
