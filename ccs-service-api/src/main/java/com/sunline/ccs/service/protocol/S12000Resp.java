package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sunline.ppy.dictionary.enums.AddressType;
import com.sunline.ppy.dictionary.enums.DualBillingInd;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.StmtMediaType;

public class S12000Resp implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * 卡号
	 */
	public String card_no;
	
	/**
	 * 姓名
	 */
	public String name;
	
	/**
	 * 币种
	 */
	public String curr_cd;
	/**
	 * 产品名称
	 */
	public String product_name;
	/**
	 * 信用额度
	 */
	public BigDecimal credit_limit;
	/**
	 * 临时额度
	 */
	public BigDecimal temp_limit;
	/**
	 * 临时额度开始日期
	 */
	public Date temp_limit_begin_date;
	/**
	 * 临时额度结束日期
	 */
	public Date temp_limit_end_date;
	/**
	 * 取现额度比例
	 */
	public BigDecimal cash_limit_rt;
	/**
	 * 授权超限比例
	 */
	public BigDecimal ovrlmt_rate;
	/**
	 * 额度内分期额度比例
	 */
	public BigDecimal loan_limit_rt;
	/**
	 * 当前余额
	 */
	public BigDecimal curr_bal;
	/**
	 * 取现余额
	 */
	public BigDecimal cash_bal;
	/**
	 * 本金余额
	 */
	public BigDecimal principal_bal;
	/**
	 * 额度内分期余额
	 */
	public BigDecimal loan_bal;
	/**
	 * 争议金额
	 */
	public BigDecimal dispute_amt;
	/**
	 * 期初余额
	 */
	public BigDecimal begin_bal;
	/**
	 * 到期还款日余额
	 */
	public BigDecimal pmt_due_day_bal;
	/**
	 * 全部应还款额
	 */
	public BigDecimal qual_grace_bal;
	/**
	 * 是否已全额还款
	 */
	public Indicator grace_days_full_ind;
	
	/**
	 * 综合可用额度
	 */
	public BigDecimal available_otb;

	/**
	 * 账户层可用取现额度
	 */
	public BigDecimal acct_cash_otb;
	/**
	 * 创建日期
	 */
	public Date setup_date;
	/**
	 * 发卡网点
	 */
	public String owning_branch;
	/**
	 * 账单周期
	 */
	public String billing_cycle;
	/**
	 * 账单标志
	 */
	public Indicator stmt_flag;
	/**
	 * 账单寄送地址标志
	 */
	public AddressType stmt_mail_addr_ind;
	/**
	 * 账单介质类型
	 */
	public StmtMediaType stmt_media_type;
	/**
	 * 锁定码
	 */
	public String block_code;
	/**
	 * 账龄
	 */
	public String age_cd;
	/**
	 * 未匹配借记金额
	 */
	public BigDecimal unmatch_db;
	/**
	 * 未匹配取现金额
	 */
	public BigDecimal unmatch_cash;
	/**
	 * 未匹配贷记金额
	 */
	public BigDecimal unmatch_cr;
	/**
	 * 本币溢缴款还外币指示
	 */
	public DualBillingInd dual_billing_flag;
	/**
	 * 上笔还款金额
	 */
	public BigDecimal last_pmt_amt;
	/**
	 * 上一还款日期
	 */
	public Date last_pmt_date;
	/**
	 * 上个账单日期
	 */
	public Date last_stmt_date;
	/**
	 * 下个账单日期
	 */
	public Date next_stmt_date;
	/**
	 * 到期还款日期
	 */
	public Date pmt_due_date;
	/**
	 * 约定还款日期
	 */
	public Date dd_date;
	/**
	 * 宽限日期
	 */
	public Date grace_date;
	/**
	 * 最终销户日期
	 */
	public Date closed_date;
	/**
	 * 首个账单日期
	 */
	public Date first_stmt_date;
	/**
	 * 销卡销户日期
	 */
	public Date cancel_date;
	/**
	 * 转呆账日期
	 */
	public Date charge_off_date;
	/**
	 * 首次消费日期
	 */
	public Date first_purchase_date;
	/**
	 * 首次消费金额
	 */
	public BigDecimal first_purchase_amt;
	/**
	 * 最小还款额
	 */
	public BigDecimal tot_due_amt;
	/**
	 * 是否存在双币标识
	 */
	public Indicator dual_curr_ind;
	/**
	 * 外币账户
	 */
	public String dual_curr_cd;
	/**
	 *  当期剩余应还款额 
	 */
	public BigDecimal curr_remain_tot_bal;
	public String getCard_no() {
		return card_no;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCurr_cd() {
		return curr_cd;
	}
	public String getProduct_name() {
		return product_name;
	}
	public BigDecimal getCredit_limit() {
		return credit_limit;
	}
	public BigDecimal getTemp_limit() {
		return temp_limit;
	}
	public Date getTemp_limit_begin_date() {
		return temp_limit_begin_date;
	}
	public Date getTemp_limit_end_date() {
		return temp_limit_end_date;
	}
	public BigDecimal getCash_limit_rt() {
		return cash_limit_rt;
	}
	public BigDecimal getOvrlmt_rate() {
		return ovrlmt_rate;
	}
	public BigDecimal getLoan_limit_rt() {
		return loan_limit_rt;
	}
	public BigDecimal getCurr_bal() {
		return curr_bal;
	}
	public BigDecimal getCash_bal() {
		return cash_bal;
	}
	public BigDecimal getPrincipal_bal() {
		return principal_bal;
	}
	public BigDecimal getLoan_bal() {
		return loan_bal;
	}
	public BigDecimal getDispute_amt() {
		return dispute_amt;
	}
	public BigDecimal getBegin_bal() {
		return begin_bal;
	}
	public BigDecimal getPmt_due_day_bal() {
		return pmt_due_day_bal;
	}
	public BigDecimal getQual_grace_bal() {
		return qual_grace_bal;
	}
	public Indicator getGrace_days_full_ind() {
		return grace_days_full_ind;
	}
	public Date getSetup_date() {
		return setup_date;
	}
	public String getOwning_branch() {
		return owning_branch;
	}
	public String getBilling_cycle() {
		return billing_cycle;
	}
	public Indicator getStmt_flag() {
		return stmt_flag;
	}
	public AddressType getStmt_mail_addr_ind() {
		return stmt_mail_addr_ind;
	}
	public StmtMediaType getStmt_media_type() {
		return stmt_media_type;
	}
	public String getBlock_code() {
		return block_code;
	}
	public String getAge_cd() {
		return age_cd;
	}
	public BigDecimal getUnmatch_db() {
		return unmatch_db;
	}
	public BigDecimal getUnmatch_cash() {
		return unmatch_cash;
	}
	public BigDecimal getUnmatch_cr() {
		return unmatch_cr;
	}
	public DualBillingInd getDual_billing_flag() {
		return dual_billing_flag;
	}
	public BigDecimal getLast_pmt_amt() {
		return last_pmt_amt;
	}
	public Date getLast_pmt_date() {
		return last_pmt_date;
	}
	public Date getLast_stmt_date() {
		return last_stmt_date;
	}
	public Date getNext_stmt_date() {
		return next_stmt_date;
	}
	public Date getPmt_due_date() {
		return pmt_due_date;
	}
	public Date getDd_date() {
		return dd_date;
	}
	public Date getGrace_date() {
		return grace_date;
	}
	public Date getClosed_date() {
		return closed_date;
	}
	public Date getFirst_stmt_date() {
		return first_stmt_date;
	}
	public Date getCancel_date() {
		return cancel_date;
	}
	public Date getCharge_off_date() {
		return charge_off_date;
	}
	public Date getFirst_purchase_date() {
		return first_purchase_date;
	}
	public BigDecimal getFirst_purchase_amt() {
		return first_purchase_amt;
	}
	public BigDecimal getTot_due_amt() {
		return tot_due_amt;
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
	public void setProduct_name(String product_name) {
		this.product_name = product_name;
	}
	public void setCredit_limit(BigDecimal credit_limit) {
		this.credit_limit = credit_limit;
	}
	public void setTemp_limit(BigDecimal temp_limit) {
		this.temp_limit = temp_limit;
	}
	public void setTemp_limit_begin_date(Date temp_limit_begin_date) {
		this.temp_limit_begin_date = temp_limit_begin_date;
	}
	public void setTemp_limit_end_date(Date temp_limit_end_date) {
		this.temp_limit_end_date = temp_limit_end_date;
	}
	public void setCash_limit_rt(BigDecimal cash_limit_rt) {
		this.cash_limit_rt = cash_limit_rt;
	}
	public void setOvrlmt_rate(BigDecimal ovrlmt_rate) {
		this.ovrlmt_rate = ovrlmt_rate;
	}
	public void setLoan_limit_rt(BigDecimal loan_limit_rt) {
		this.loan_limit_rt = loan_limit_rt;
	}
	public void setCurr_bal(BigDecimal curr_bal) {
		this.curr_bal = curr_bal;
	}
	public void setCash_bal(BigDecimal cash_bal) {
		this.cash_bal = cash_bal;
	}
	public void setPrincipal_bal(BigDecimal principal_bal) {
		this.principal_bal = principal_bal;
	}
	public void setLoan_bal(BigDecimal loan_bal) {
		this.loan_bal = loan_bal;
	}
	public void setDispute_amt(BigDecimal dispute_amt) {
		this.dispute_amt = dispute_amt;
	}
	public void setBegin_bal(BigDecimal begin_bal) {
		this.begin_bal = begin_bal;
	}
	public void setPmt_due_day_bal(BigDecimal pmt_due_day_bal) {
		this.pmt_due_day_bal = pmt_due_day_bal;
	}
	public void setQual_grace_bal(BigDecimal qual_grace_bal) {
		this.qual_grace_bal = qual_grace_bal;
	}
	public void setGrace_days_full_ind(Indicator grace_days_full_ind) {
		this.grace_days_full_ind = grace_days_full_ind;
	}
	public void setSetup_date(Date setup_date) {
		this.setup_date = setup_date;
	}
	public void setOwning_branch(String owning_branch) {
		this.owning_branch = owning_branch;
	}
	public void setBilling_cycle(String billing_cycle) {
		this.billing_cycle = billing_cycle;
	}
	public void setStmt_flag(Indicator stmt_flag) {
		this.stmt_flag = stmt_flag;
	}
	public void setStmt_mail_addr_ind(AddressType stmt_mail_addr_ind) {
		this.stmt_mail_addr_ind = stmt_mail_addr_ind;
	}
	public void setStmt_media_type(StmtMediaType stmt_media_type) {
		this.stmt_media_type = stmt_media_type;
	}
	public void setBlock_code(String block_code) {
		this.block_code = block_code;
	}
	public void setAge_cd(String age_cd) {
		this.age_cd = age_cd;
	}
	public void setUnmatch_db(BigDecimal unmatch_db) {
		this.unmatch_db = unmatch_db;
	}
	public void setUnmatch_cash(BigDecimal unmatch_cash) {
		this.unmatch_cash = unmatch_cash;
	}
	public void setUnmatch_cr(BigDecimal unmatch_cr) {
		this.unmatch_cr = unmatch_cr;
	}
	public void setDual_billing_flag(DualBillingInd dual_billing_flag) {
		this.dual_billing_flag = dual_billing_flag;
	}
	public void setLast_pmt_amt(BigDecimal last_pmt_amt) {
		this.last_pmt_amt = last_pmt_amt;
	}
	public void setLast_pmt_date(Date last_pmt_date) {
		this.last_pmt_date = last_pmt_date;
	}
	public void setLast_stmt_date(Date last_stmt_date) {
		this.last_stmt_date = last_stmt_date;
	}
	public void setNext_stmt_date(Date next_stmt_date) {
		this.next_stmt_date = next_stmt_date;
	}
	public void setPmt_due_date(Date pmt_due_date) {
		this.pmt_due_date = pmt_due_date;
	}
	public void setDd_date(Date dd_date) {
		this.dd_date = dd_date;
	}
	public void setGrace_date(Date grace_date) {
		this.grace_date = grace_date;
	}
	public void setClosed_date(Date closed_date) {
		this.closed_date = closed_date;
	}
	public void setFirst_stmt_date(Date first_stmt_date) {
		this.first_stmt_date = first_stmt_date;
	}
	public void setCancel_date(Date cancel_date) {
		this.cancel_date = cancel_date;
	}
	public void setCharge_off_date(Date charge_off_date) {
		this.charge_off_date = charge_off_date;
	}
	public void setFirst_purchase_date(Date first_purchase_date) {
		this.first_purchase_date = first_purchase_date;
	}
	public void setFirst_purchase_amt(BigDecimal first_purchase_amt) {
		this.first_purchase_amt = first_purchase_amt;
	}
	public void setTot_due_amt(BigDecimal tot_due_amt) {
		this.tot_due_amt = tot_due_amt;
	}
	public void setDual_curr_ind(Indicator dual_curr_ind) {
		this.dual_curr_ind = dual_curr_ind;
	}
	public void setDual_curr_cd(String dual_curr_cd) {
		this.dual_curr_cd = dual_curr_cd;
	}		

	public BigDecimal getAvailable_otb() {
		return available_otb;
	}

	public void setAvailable_otb(BigDecimal available_otb) {
		this.available_otb = available_otb;
	}

	public BigDecimal getAcct_cash_otb() {
		return acct_cash_otb;
	}

	public void setAcct_cash_otb(BigDecimal acct_cash_otb) {
		this.acct_cash_otb = acct_cash_otb;
	}

	public BigDecimal getCurr_remain_tot_bal() {
		return curr_remain_tot_bal;
	}

	public void setCurr_remain_tot_bal(BigDecimal curr_remain_tot_bal) {
		this.curr_remain_tot_bal = curr_remain_tot_bal;
	}

	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
	
}
