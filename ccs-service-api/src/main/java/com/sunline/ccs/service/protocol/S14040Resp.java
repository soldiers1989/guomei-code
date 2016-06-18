package com.sunline.ccs.service.protocol;

import java.io.Serializable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sunline.ppy.dictionary.enums.Indicator;

/**
 * 卡片激活
* @author fanghj
 *
 */
public class S14040Resp implements Serializable {
	
    public static final String P_ActivateInd = "activeInd";
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7000769108969790603L;

	/**
	 * 卡号
	 */
	public String card_no;
	
	/**
	 * 是否新发卡
	 */
	public Indicator new_card_issue_ind;
	
	/**
	 * 是否存在查询密码
	 */
	public Indicator q_pin_exist_ind;
	
	/**
	 * 是否存在交易密码
	 */
	public Indicator p_pin_exist_ind;
	
	/**
	 * 是否消费凭密
	 */
	public Indicator pos_pin_verify_ind;

	public Indicator getPos_pin_verify_ind() {
		return pos_pin_verify_ind;
	}

	public void setPos_pin_verify_ind(Indicator pos_pin_verify_ind) {
		this.pos_pin_verify_ind = pos_pin_verify_ind;
	}

	public String getCard_no() {
		return card_no;
	}

	public void setCard_no(String card_no) {
		this.card_no = card_no;
	}

	public Indicator getNew_card_issue_ind() {
		return new_card_issue_ind;
	}

	public void setNew_card_issue_ind(Indicator new_card_issue_ind) {
		this.new_card_issue_ind = new_card_issue_ind;
	}

	public Indicator getQ_pin_exist_ind() {
		return q_pin_exist_ind;
	}

	public void setQ_pin_exist_ind(Indicator q_pin_exist_ind) {
		this.q_pin_exist_ind = q_pin_exist_ind;
	}

	public Indicator getP_pin_exist_ind() {
		return p_pin_exist_ind;
	}

	public void setP_pin_exist_ind(Indicator p_pin_exist_ind) {
		this.p_pin_exist_ind = p_pin_exist_ind;
	}
	
	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
