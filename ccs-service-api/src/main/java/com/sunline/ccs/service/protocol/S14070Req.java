package com.sunline.ccs.service.protocol;

import java.io.Serializable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ccs.param.def.enums.ChangeReason;

/**
 * 挂失换卡/损坏换卡
 * 
* @author fanghj
 * 
 */
public class S14070Req implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7395379395970210426L;

	/**
	 * 卡号
	 */
	public String card_no;
	
	/**
	 * 换卡原因
	 */
	public ChangeReason change_reson;
	
	/**
	 * 是否加急
	 */
	public Indicator urgent_flg;
	
	/**
	 * 收费标志
	 */
	public Indicator fee_ind;

	public String getCard_no() {
		return card_no;
	}

	public void setCard_no(String card_no) {
		this.card_no = card_no;
	}

	/**
	 * 换卡原因<br>
	 */
	public ChangeReason getChange_reson() {
		return change_reson;
	}

	/**
	 * 换卡原因<br>
	 */
	public void setChange_reson(ChangeReason change_reson) {
		this.change_reson = change_reson;
	}

	public Indicator getFee_ind() {
		return fee_ind;
	}

	public void setFee_ind(Indicator fee_ind) {
		this.fee_ind = fee_ind;
	}

	public Indicator getUrgent_flg() {
		return urgent_flg;
	}

	public void setUrgent_flg(Indicator urgent_flg) {
		this.urgent_flg = urgent_flg;
	}

	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
