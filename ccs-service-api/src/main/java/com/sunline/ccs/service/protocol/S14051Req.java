package com.sunline.ccs.service.protocol;

import java.io.Serializable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sunline.ppy.dictionary.enums.Indicator;

/**
 * 卡片挂失/解挂
 * 
* @author fanghj
 * 
 */
public class S14051Req implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6420869883116930806L;

	/**
	 * 卡号
	 */
	public String card_no;

	/**
	 * 操作方向
	 */
	public String opt;

	/**
	 * 原因
	 */
	public String lost_reason;
	
	/**
	 * 是否加急
	 * @return
	 */
	public Indicator fee_ind;

	public Indicator getFee_ind() {
		return fee_ind;
	}

	public void setFee_ind(Indicator fee_ind) {
		this.fee_ind = fee_ind;
	}

	public String getCard_no() {
		return card_no;
	}

	public void setCard_no(String card_no) {
		this.card_no = card_no;
	}

	/**
	 * 操作方向<br>
	 * 0：挂失 <br>
	 * 1：解挂
	 * 
	 * @return
	 */
	public String getOpt() {
		return opt;
	}

	/**
	 * 操作方向<br>
	 * 0：挂失 <br>
	 * 1：解挂
	 * 
	 * @return
	 */
	public void setOpt(String opt) {
		this.opt = opt;
	}

	/**
	 * 原因 <br>
	 * 0：被盗挂失 <br>
	 * 1：丢失挂失
	 * 
	 * @return
	 */
	public String getLost_reason() {
		return lost_reason;
	}
	/**
	 * 原因 <br>
	 * 0：被盗挂失 <br>
	 * 1：丢失挂失
	 * 
	 * @return
	 */
	public void setLost_reason(String lost_reason) {
		this.lost_reason = lost_reason;
	}
	
	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
