package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sunline.ppy.dictionary.enums.LoanFeeMethod;

public class S13082Req implements Serializable {

	private static final long serialVersionUID = -6294066256886443457L;

	/**
     * 卡号
     */
    public String card_no ;
    /**
     * 币种
     */
    public String curr_cd ;
    /**
     * 原交易日期
     */
    public Date txn_date ;
    /**
     * 原交易金额
     */
    public BigDecimal txn_amt ;
    /**
     * 分期总期数
     */
    public Integer loan_init_term ;
    /**
     * 分期手续费收取方式
     */
    public LoanFeeMethod loan_fee_method ;
    /**
     * 操作类型
     */
    public String opt ;
    
    
	public String getCard_no() {
		return card_no;
	}
	public String getCurr_cd() {
		return curr_cd;
	}
	public Date getTxn_date() {
		return txn_date;
	}
	public BigDecimal getTxn_amt() {
		return txn_amt;
	}
	public Integer getLoan_init_term() {
		return loan_init_term;
	}
	public LoanFeeMethod getLoan_fee_method() {
		return loan_fee_method;
	}
	public String getOpt() {
		return opt;
	}
	public void setCard_no(String card_no) {
		this.card_no = card_no;
	}
	public void setCurr_cd(String curr_cd) {
		this.curr_cd = curr_cd;
	}
	public void setTxn_date(Date txn_date) {
		this.txn_date = txn_date;
	}
	public void setTxn_amt(BigDecimal txn_amt) {
		this.txn_amt = txn_amt;
	}
	public void setLoan_init_term(Integer loan_init_term) {
		this.loan_init_term = loan_init_term;
	}
	public void setLoan_fee_method(LoanFeeMethod loan_fee_method) {
		this.loan_fee_method = loan_fee_method;
	}
	public void setOpt(String opt) {
		this.opt = opt;
	}

	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}

