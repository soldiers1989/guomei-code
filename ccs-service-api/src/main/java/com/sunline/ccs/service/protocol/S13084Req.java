package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.math.BigDecimal;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sunline.ppy.dictionary.enums.LoanFeeMethod;

/**
 * 现金分期请求接口
* @author fanghj
 *
 */
public class S13084Req implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6653248938516672853L;
	/**
	 * 卡号
	 */
	public String card_no;
	/**
	 * 币种
	 */
	public String curr_cd;
	/**
	 * 分期额度
	 */
	public BigDecimal cash_amt;
	/**
	 * 分期期数
	 */
	public Integer loan_init_term;
	/**
	 * 分期手续费收取方式
	 */
	public LoanFeeMethod loan_fee_method;
	/**
	 * 操作类型 0-试算 1-申请
	 */
	public String opt;
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
	public BigDecimal getCash_amt() {
		return cash_amt;
	}
	public void setCash_amt(BigDecimal cash_amt) {
		this.cash_amt = cash_amt;
	}
	public Integer getLoan_init_term() {
		return loan_init_term;
	}
	public void setLoan_init_term(Integer loan_init_term) {
		this.loan_init_term = loan_init_term;
	}
	public LoanFeeMethod getLoan_fee_method() {
		return loan_fee_method;
	}
	public void setLoan_fee_method(LoanFeeMethod loan_fee_method) {
		this.loan_fee_method = loan_fee_method;
	}
	public String getOpt() {
		return opt;
	}
	public void setOpt(String opt) {
		this.opt = opt;
	}
	
	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
