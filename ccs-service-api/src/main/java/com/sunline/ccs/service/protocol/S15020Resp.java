package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sunline.ppy.dictionary.enums.Indicator;

public class S15020Resp implements Serializable {
	
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
	 * 客户层信用额度
	 */
	public BigDecimal cust_limit;
	/**
	 * 客户层可用额度
	 */
	public BigDecimal cust_otb;
	/**
	 * 客户层取现额度
	 */
	public BigDecimal cust_cash_limit;
	/**
	 * 客户层可用取现额度
	 */
	public BigDecimal cust_cash_otb;
	/**
	 * 账户层信用额度
	 */
	public BigDecimal acct_limit;
	/**
	 * 账户层可用额度
	 */
	public BigDecimal acct_otb;
	/**
	 * 账户层取现额度
	 */
	public BigDecimal acct_cash_limit;
	/**
	 * 账户层可用取现额度
	 */
	public BigDecimal acct_cash_otb;
	/**
	 * 账户层分期额度
	 */
	public BigDecimal acct_loan_limit;
	/**
	 * 账户层分期可用额度
	 */
	public BigDecimal acct_loan_otb;
	/**
	 * 账户层临时额度
	 */
	public BigDecimal acct_temp_limit;
	/**
	 * 账户层临时额度失效期
	 */
	public Date acct_temp_limit_end_date;
	/**
	 * 综合可用额度
	 */
	public BigDecimal available_otb;
	/**
	 * 是否存在双币标识
	 */
	public Indicator dual_curr_ind;
	/**
	 * 外币币种
	 */
	public String dual_curr_cd;
	
	
	public String getCard_no() {
		return card_no;
	}
	public String getCurr_cd() {
		return curr_cd;
	}
	public BigDecimal getCust_limit() {
		return cust_limit;
	}
	public BigDecimal getCust_otb() {
		return cust_otb;
	}
	public BigDecimal getCust_cash_limit() {
		return cust_cash_limit;
	}
	public BigDecimal getCust_cash_otb() {
		return cust_cash_otb;
	}
	public BigDecimal getAcct_limit() {
		return acct_limit;
	}
	public BigDecimal getAcct_otb() {
		return acct_otb;
	}
	public BigDecimal getAcct_cash_limit() {
		return acct_cash_limit;
	}
	public BigDecimal getAcct_cash_otb() {
		return acct_cash_otb;
	}
	public BigDecimal getAcct_loan_limit() {
		return acct_loan_limit;
	}
	public BigDecimal getAcct_loan_otb() {
		return acct_loan_otb;
	}
	public BigDecimal getAcct_temp_limit() {
		return acct_temp_limit;
	}
	public Date getAcct_temp_limit_end_date() {
		return acct_temp_limit_end_date;
	}
	public BigDecimal getAvailable_otb() {
		return available_otb;
	}
	public Indicator getDual_curr_ind() {
		return dual_curr_ind;
	}
	public String getDual_curr_cd() {
		return dual_curr_cd;
	}
	public void setCard_no(String card_no) {
		this.card_no = card_no;
	}
	public void setCurr_cd(String curr_cd) {
		this.curr_cd = curr_cd;
	}
	public void setCust_limit(BigDecimal cust_limit) {
		this.cust_limit = cust_limit;
	}
	public void setCust_otb(BigDecimal cust_otb) {
		this.cust_otb = cust_otb;
	}
	public void setCust_cash_limit(BigDecimal cust_cash_limit) {
		this.cust_cash_limit = cust_cash_limit;
	}
	public void setCust_cash_otb(BigDecimal cust_cash_otb) {
		this.cust_cash_otb = cust_cash_otb;
	}
	public void setAcct_limit(BigDecimal acct_limit) {
		this.acct_limit = acct_limit;
	}
	public void setAcct_otb(BigDecimal acct_otb) {
		this.acct_otb = acct_otb;
	}
	public void setAcct_cash_limit(BigDecimal acct_cash_limit) {
		this.acct_cash_limit = acct_cash_limit;
	}
	public void setAcct_cash_otb(BigDecimal acct_cash_otb) {
		this.acct_cash_otb = acct_cash_otb;
	}
	public void setAcct_loan_limit(BigDecimal acct_loan_limit) {
		this.acct_loan_limit = acct_loan_limit;
	}
	public void setAcct_loan_otb(BigDecimal acct_loan_otb) {
		this.acct_loan_otb = acct_loan_otb;
	}
	public void setAcct_temp_limit(BigDecimal acct_temp_limit) {
		this.acct_temp_limit = acct_temp_limit;
	}
	public void setAcct_temp_limit_end_date(Date acct_temp_limit_end_date) {
		this.acct_temp_limit_end_date = acct_temp_limit_end_date;
	}
	public void setAvailable_otb(BigDecimal available_otb) {
		this.available_otb = available_otb;
	}
	public void setDual_curr_ind(Indicator dual_curr_ind) {
		this.dual_curr_ind = dual_curr_ind;
	}
	public void setDual_curr_cd(String dual_curr_cd) {
		this.dual_curr_cd = dual_curr_cd;
	}

	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
