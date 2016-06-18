package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.math.BigDecimal;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sunline.ppy.dictionary.enums.DdIndicator;
import com.sunline.ppy.dictionary.enums.LoanFeeMethod;

/**
 * 现金分期返回接口
* @author fanghj
 *
 */
public class S13084Resp implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2533898671880893663L;
	/**
	 * 卡号
	 */
	public String card_no;
	/**
	 * 币种
	 */
	public String curr_cd;
	/**
	 * 分期申请顺序号
	 */
	public Integer register_id;
	/**
	 * 分期总期数
	 */
	public Integer loan_init_term;
	/**
	 * 分期手续费收取方式
	 */
	public LoanFeeMethod loan_fee_method;
	/**
	 * 分期总本金
	 */
	public BigDecimal loan_init_prin;
	/**
	 * 分期每期应还本金
	 */
	public BigDecimal loan_fixed_pmt_prin;
	/**
	 * 分期首期应还本金
	 */
	public BigDecimal loan_first_term_prin;
	/**
	 * 分期末期应还本金
	 */
	public BigDecimal loan_final_term_prin;
	/**
	 * 分期总手续费
	 */
	public BigDecimal loan_init_fee1;
	/**
	 * 分期每期手续费
	 */
	public BigDecimal loan_fixed_fee1;
	/**
	 * 分期首期手续费
	 */
	public BigDecimal loan_first_term_fee1;
	/**
	 * 分期末期手续费
	 */
	public BigDecimal loan_final_term_fee1;
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
	public void setCard_no(String card_no) {
		this.card_no = card_no;
	}
	public String getCurr_cd() {
		return curr_cd;
	}
	public void setCurr_cd(String curr_cd) {
		this.curr_cd = curr_cd;
	}
	public Integer getRegister_id() {
		return register_id;
	}
	public void setRegister_id(Integer register_id) {
		this.register_id = register_id;
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
	public BigDecimal getLoan_init_prin() {
		return loan_init_prin;
	}
	public void setLoan_init_prin(BigDecimal loan_init_prin) {
		this.loan_init_prin = loan_init_prin;
	}
	public BigDecimal getLoan_fixed_pmt_prin() {
		return loan_fixed_pmt_prin;
	}
	public void setLoan_fixed_pmt_prin(BigDecimal loan_fixed_pmt_prin) {
		this.loan_fixed_pmt_prin = loan_fixed_pmt_prin;
	}
	public BigDecimal getLoan_first_term_prin() {
		return loan_first_term_prin;
	}
	public void setLoan_first_term_prin(BigDecimal loan_first_term_prin) {
		this.loan_first_term_prin = loan_first_term_prin;
	}
	public BigDecimal getLoan_final_term_prin() {
		return loan_final_term_prin;
	}
	public void setLoan_final_term_prin(BigDecimal loan_final_term_prin) {
		this.loan_final_term_prin = loan_final_term_prin;
	}
	public BigDecimal getLoan_init_fee1() {
		return loan_init_fee1;
	}
	public void setLoan_init_fee1(BigDecimal loan_init_fee1) {
		this.loan_init_fee1 = loan_init_fee1;
	}
	public BigDecimal getLoan_fixed_fee1() {
		return loan_fixed_fee1;
	}
	public void setLoan_fixed_fee1(BigDecimal loan_fixed_fee1) {
		this.loan_fixed_fee1 = loan_fixed_fee1;
	}
	public BigDecimal getLoan_first_term_fee1() {
		return loan_first_term_fee1;
	}
	public void setLoan_first_term_fee1(BigDecimal loan_first_term_fee1) {
		this.loan_first_term_fee1 = loan_first_term_fee1;
	}
	public BigDecimal getLoan_final_term_fee1() {
		return loan_final_term_fee1;
	}
	public void setLoan_final_term_fee1(BigDecimal loan_final_term_fee1) {
		this.loan_final_term_fee1 = loan_final_term_fee1;
	}
	public DdIndicator getDd_ind() {
		return dd_ind;
	}
	public void setDd_ind(DdIndicator dd_ind) {
		this.dd_ind = dd_ind;
	}
	public String getDd_bank_name() {
		return dd_bank_name;
	}
	public void setDd_bank_name(String dd_bank_name) {
		this.dd_bank_name = dd_bank_name;
	}
	public String getDd_bank_branch() {
		return dd_bank_branch;
	}
	public void setDd_bank_branch(String dd_bank_branch) {
		this.dd_bank_branch = dd_bank_branch;
	}
	public String getDd_bank_acct_no() {
		return dd_bank_acct_no;
	}
	public void setDd_bank_acct_no(String dd_bank_acct_no) {
		this.dd_bank_acct_no = dd_bank_acct_no;
	}
	public String getDd_bank_acct_name() {
		return dd_bank_acct_name;
	}
	public void setDd_bank_acct_name(String dd_bank_acct_name) {
		this.dd_bank_acct_name = dd_bank_acct_name;
	}
	
	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
