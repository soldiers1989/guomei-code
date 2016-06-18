package com.sunline.ccs.service.protocol;

import java.io.Serializable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sunline.ppy.dictionary.enums.Indicator;

/**
 * 消费凭密设定
* @author fanghj
 *
 */
public class S14080Req implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 644356405678291508L;

	/**
	 * 卡号
	 */
	public String card_no;
	
	/**
	 * 功能码	
	 */
	public String opt;
	
	/**
	 * 是否消费凭密
	 */
	public Indicator pos_pin_verify_ind;

	public String getCard_no() {
		return card_no;
	}

	public void setCard_no(String card_no) {
		this.card_no = card_no;
	}

	public String getOpt() {
		return opt;
	}

	public void setOpt(String opt) {
		this.opt = opt;
	}

	public Indicator getPos_pin_verify_ind() {
		return pos_pin_verify_ind;
	}

	public void setPos_pin_verify_ind(Indicator pos_pin_verify_ind) {
		this.pos_pin_verify_ind = pos_pin_verify_ind;
	}
	
	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
