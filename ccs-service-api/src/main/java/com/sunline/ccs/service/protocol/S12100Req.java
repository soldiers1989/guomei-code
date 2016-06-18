package com.sunline.ccs.service.protocol;

import java.io.Serializable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sunline.ppy.dictionary.enums.DdIndicator;

public class S12100Req implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
	 * 卡号
	 */
	public String card_no;
	/**
	 * 币种
	 */
	public String curr_cd;
	/**
	 * 功能码
	 */
	public String opt;
	/**
	 * 约定还款类型
	 */
	public DdIndicator dd_ind;
	/**
	 * 约定还款银行名称
	 */
	public String dd_bank_name;
	/**
	 * 约定还款开户行号
	 */
	public String dd_bank_branch;
	/**
	 * 约定还款扣款账号
	 */
	public String dd_bank_acct_no;
	/**
	 * 约定还款扣款账户姓名
	 */
	public String dd_bank_acct_name;
	
	
	public String getCard_no() {
		return card_no;
	}
	public String getCurr_cd() {
		return curr_cd;
	}
	public String getOpt() {
		return opt;
	}
	public DdIndicator getDd_ind() {
		return dd_ind;
	}
	public String getDd_bank_name() {
		return dd_bank_name;
	}
	public String getDd_bank_branch() {
		return dd_bank_branch;
	}
	public String getDd_bank_acct_no() {
		return dd_bank_acct_no;
	}
	public String getDd_bank_acct_name() {
		return dd_bank_acct_name;
	}
	public void setCard_no(String card_no) {
		this.card_no = card_no;
	}
	public void setCurr_cd(String curr_cd) {
		this.curr_cd = curr_cd;
	}
	public void setOpt(String opt) {
		this.opt = opt;
	}
	public void setDd_ind(DdIndicator dd_ind) {
		this.dd_ind = dd_ind;
	}
	public void setDd_bank_name(String dd_bank_name) {
		this.dd_bank_name = dd_bank_name;
	}
	public void setDd_bank_branch(String dd_bank_branch) {
		this.dd_bank_branch = dd_bank_branch;
	}
	public void setDd_bank_acct_no(String dd_bank_acct_no) {
		this.dd_bank_acct_no = dd_bank_acct_no;
	}
	public void setDd_bank_acct_name(String dd_bank_acct_name) {
		this.dd_bank_acct_name = dd_bank_acct_name;
	}
	
	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
