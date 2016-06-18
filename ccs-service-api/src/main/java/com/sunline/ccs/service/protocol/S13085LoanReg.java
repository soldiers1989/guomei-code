package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>分期申请状态查询接口</p>
* @author fanghj
 *
 */
@SuppressWarnings("serial")
public class S13085LoanReg implements Serializable{
	
	/**
	 * 币种
	 */
	public String curr_cd;
	
	/**
	 * 分期申请顺序号
	 */
	public Long register_id;
	
	/**
	 * 交易参考号
	 */
	public String ref_nbr;
	
	/**
	 * 分期类型
	 */
	public String loan_type;
	
	/**
	 * 分期注册状态
	 */
	public String loan_reg_status;
	
	/**
	 * 分期总期数
	 */
	public Integer loan_init_term;
	
	/**
	 * 分期手续费收取方式
	 */
	public String loan_fee_method;
	
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
	 * 备注
	 */
	public String remark;
	
	/**
	 * 原始交易币种金额
	 */
	public BigDecimal orig_txn_amt;
	
	/**
	 * 原始交易日期
	 */
	public Date orig_trans_date;
	
	/**
	 * 原始交易授权码
	 */
	public String orig_auth_code;
	
	/**
	 * 分期计划代码
	 */
	public String loan_code;
	
	/**
	 * 分期交易行动码
	 */
	public String loan_action;

	public String getCurr_cd() {
		return curr_cd;
	}

	public void setCurr_cd(String curr_cd) {
		this.curr_cd = curr_cd;
	}

	public Long getRegister_id() {
		return register_id;
	}

	public void setRegister_id(Long register_id) {
		this.register_id = register_id;
	}

	public String getRef_nbr() {
		return ref_nbr;
	}

	public void setRef_nbr(String ref_nbr) {
		this.ref_nbr = ref_nbr;
	}

	public String getLoan_type() {
		return loan_type;
	}

	public void setLoan_type(String loan_type) {
		this.loan_type = loan_type;
	}

	public String getLoan_reg_status() {
		return loan_reg_status;
	}

	public void setLoan_reg_status(String loan_reg_status) {
		this.loan_reg_status = loan_reg_status;
	}

	public Integer getLoan_init_term() {
		return loan_init_term;
	}

	public void setLoan_init_term(Integer loan_init_term) {
		this.loan_init_term = loan_init_term;
	}

	public String getLoan_fee_method() {
		return loan_fee_method;
	}

	public void setLoan_fee_method(String loan_fee_method) {
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

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public BigDecimal getOrig_txn_amt() {
		return orig_txn_amt;
	}

	public void setOrig_txn_amt(BigDecimal orig_txn_amt) {
		this.orig_txn_amt = orig_txn_amt;
	}

	public Date getOrig_trans_date() {
		return orig_trans_date;
	}

	public void setOrig_trans_date(Date orig_trans_date) {
		this.orig_trans_date = orig_trans_date;
	}

	public String getOrig_auth_code() {
		return orig_auth_code;
	}

	public void setOrig_auth_code(String orig_auth_code) {
		this.orig_auth_code = orig_auth_code;
	}

	public String getLoan_code() {
		return loan_code;
	}

	public void setLoan_code(String loan_code) {
		this.loan_code = loan_code;
	}

	public String getLoan_action() {
		return loan_action;
	}

	public void setLoan_action(String loan_action) {
		this.loan_action = loan_action;
	}

}
