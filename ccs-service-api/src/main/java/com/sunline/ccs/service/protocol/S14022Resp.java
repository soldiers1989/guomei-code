package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.math.BigDecimal;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class S14022Resp implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * 贷款卡号
	 */
	public String card_no;
	/**
	 * 币种
	 */
	public String curr_cd;
	/**
	 * 全部应还款额
	 */
	public BigDecimal qual_grace_bal;
	
	/**
	 * 已入账还款欠款
	 */
	public BigDecimal curr_bal;

	/**
	 * 授权未入账借记
	 */
	public BigDecimal unmatch_db;
	
	/**
	 * 授权未入账贷记
	 */
	public BigDecimal unmatch_cr;
	
	/**
	 * 未入账利息
	 * @return
	 */
	public BigDecimal unpost_interest;

	public String getCard_no() {
		return card_no;
	}




	public void setCard_no(String card_no) {
		this.card_no = card_no;
	}




	public String getCurr_cd() {
		return curr_cd;
	}




	public void setCurr_cd(String curr_cd) {
		this.curr_cd = curr_cd;
	}




	public BigDecimal getQual_grace_bal() {
		return qual_grace_bal;
	}




	public void setQual_grace_bal(BigDecimal qual_grace_bal) {
		this.qual_grace_bal = qual_grace_bal;
	}




	public BigDecimal getCurr_bal() {
		return curr_bal;
	}




	public void setCurr_bal(BigDecimal curr_bal) {
		this.curr_bal = curr_bal;
	}




	public BigDecimal getUnmatch_db() {
		return unmatch_db;
	}




	public void setUnmatch_db(BigDecimal unmatch_db) {
		this.unmatch_db = unmatch_db;
	}




	public BigDecimal getUnmatch_cr() {
		return unmatch_cr;
	}




	public void setUnmatch_cr(BigDecimal unmatch_cr) {
		this.unmatch_cr = unmatch_cr;
	}




	public BigDecimal getUnpost_interest() {
		return unpost_interest;
	}




	public void setUnpost_interest(BigDecimal unpost_interest) {
		this.unpost_interest = unpost_interest;
	}




	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
