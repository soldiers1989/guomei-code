package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sunline.ppy.dictionary.enums.Indicator;

public class S12010Resp implements Serializable {
	
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
	 * 账单年月
	 */
	public Date stmt_date;
	/**
	 * 账单日期
	 */
	public Date billing_date;
	/**
	 * 姓名
	 */
	public String name;
	/**
	 * 到期还款日期
	 */
	public Date pmt_due_date;
	/**
	 * 信用额度
	 */
	public BigDecimal credit_limit;
	
	/**
	 * 取现额度
	 */
	public BigDecimal cash_limit;
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
	 * 上个账单日期
	 */
	public Date last_stmt_date;
	/**
	 * 上期账单余额
	 */
	public BigDecimal stmt_beg_bal;
	/**
	 * 当期账单余额
	 */
	public BigDecimal stmt_curr_bal;
	
	/**
	 * 当期取现金额
	 */
	public BigDecimal ctd_cash_amt;

	/**
	 * 全部应还款额
	 */
	public BigDecimal qual_grace_bal;
	/**
	 * 最小还款额
	 */
	public BigDecimal tot_due_amt;
	/**
	 * 当期借记金额
	 */
	public BigDecimal ctd_amt_db;
	/**
	 * 当期借记交易笔数
	 */
	public Integer ctd_nbr_db;
	/**
	 * 当期贷记金额
	 */
	public BigDecimal ctd_amt_cr;
	/**
	 * 当期贷记交易笔数
	 */
	public Integer ctd_nbr_cr;
	/**
	 * 账龄
	 */
	public String age_cd;
	/**
	 * 是否已全额还款
	 */
	public Indicator grace_days_full_ind;
	/**
	 * 期初积分余额
	 */
	public Integer point_begin_bal;
	/**
	 * 当期新增积分
	 */
	public Integer ctd_earned_points;
	/**
	 * 当期调整积分
	 */
	public Integer ctd_adj_points;
	/**
	 * 当期兑换积分
	 */
	public Integer ctd_disb_points;
	/**
	 * 积分余额
	 */
	public Integer point_bal;
	/**
	 * 是否存在双币标识
	 */
	public Indicator dual_curr_ind;
	/**
	 * 外币账户
	 */
	public String dual_curr_cd;
	/**
	 * 本期账单已还金额
	 */
	public BigDecimal ctd_pmt_yet_amt;
	/**	
	 * 本期账单剩余未还金额 	
	 */
	public BigDecimal ctd_pmt_not_amt;
	
	/**
	 * 当期费用金额
	 */
	public BigDecimal ctd_fee_amt;
	
	/**
	 * 当期费用笔数
	 */
	public Integer ctd_fee_cnt;
	
	/**
	 * 当期利息金额
	 */
	public BigDecimal ctd_interest_amt;
	
	/**
	 * 当期利息笔数
	 */
	public Integer ctd_interest_cnt;
	
	public String getCard_no() {
		return card_no;
	}
	public String getCurr_cd() {
		return curr_cd;
	}
	public Date getStmt_date() {
		return stmt_date;
	}
	public String getName() {
		return name;
	}
	public Date getPmt_due_date() {
		return pmt_due_date;
	}
	public BigDecimal getCredit_limit() {
		return credit_limit;
	}
	
	public BigDecimal getCash_limit() {
		return cash_limit;
	}
	public void setCash_limit(BigDecimal cash_limit) {
		this.cash_limit = cash_limit;
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
	public Date getLast_stmt_date() {
		return last_stmt_date;
	}
	public BigDecimal getStmt_beg_bal() {
		return stmt_beg_bal;
	}
	public BigDecimal getStmt_curr_bal() {
		return stmt_curr_bal;
	}
	public BigDecimal getQual_grace_bal() {
		return qual_grace_bal;
	}
	public BigDecimal getTot_due_amt() {
		return tot_due_amt;
	}
	public BigDecimal getCtd_amt_db() {
		return ctd_amt_db;
	}
	public Integer getCtd_nbr_db() {
		return ctd_nbr_db;
	}
	public BigDecimal getCtd_amt_cr() {
		return ctd_amt_cr;
	}
	public Integer getCtd_nbr_cr() {
		return ctd_nbr_cr;
	}
	public String getAge_cd() {
		return age_cd;
	}
	public Indicator getGrace_days_full_ind() {
		return grace_days_full_ind;
	}
	public Integer getPoint_begin_bal() {
		return point_begin_bal;
	}
	public Integer getCtd_earned_points() {
		return ctd_earned_points;
	}
	public Integer getCtd_adj_points() {
		return ctd_adj_points;
	}
	public Integer getCtd_disb_points() {
		return ctd_disb_points;
	}
	public Integer getPoint_bal() {
		return point_bal;
	}
	public void setCard_no(String card_no) {
		this.card_no = card_no;
	}
	public void setCurr_cd(String curr_cd) {
		this.curr_cd = curr_cd;
	}
	public void setStmt_date(Date stmt_date) {
		this.stmt_date = stmt_date;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setPmt_due_date(Date pmt_due_date) {
		this.pmt_due_date = pmt_due_date;
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
	public void setLast_stmt_date(Date last_stmt_date) {
		this.last_stmt_date = last_stmt_date;
	}
	public void setStmt_beg_bal(BigDecimal stmt_beg_bal) {
		this.stmt_beg_bal = stmt_beg_bal;
	}
	public void setStmt_curr_bal(BigDecimal stmt_curr_bal) {
		this.stmt_curr_bal = stmt_curr_bal;
	}
	public void setQual_grace_bal(BigDecimal qual_grace_bal) {
		this.qual_grace_bal = qual_grace_bal;
	}
	public BigDecimal getCtd_cash_amt() {
		return ctd_cash_amt;
	}
	public void setCtd_cash_amt(BigDecimal ctd_cash_amt) {
		this.ctd_cash_amt = ctd_cash_amt;
	}
	public void setTot_due_amt(BigDecimal tot_due_amt) {
		this.tot_due_amt = tot_due_amt;
	}
	public void setCtd_amt_db(BigDecimal ctd_amt_db) {
		this.ctd_amt_db = ctd_amt_db;
	}
	public void setCtd_nbr_db(Integer ctd_nbr_db) {
		this.ctd_nbr_db = ctd_nbr_db;
	}
	public void setCtd_amt_cr(BigDecimal ctd_amt_cr) {
		this.ctd_amt_cr = ctd_amt_cr;
	}
	public void setCtd_nbr_cr(Integer ctd_nbr_cr) {
		this.ctd_nbr_cr = ctd_nbr_cr;
	}
	public void setAge_cd(String age_cd) {
		this.age_cd = age_cd;
	}
	public void setGrace_days_full_ind(Indicator grace_days_full_ind) {
		this.grace_days_full_ind = grace_days_full_ind;
	}
	public void setPoint_begin_bal(Integer point_begin_bal) {
		this.point_begin_bal = point_begin_bal;
	}
	public void setCtd_earned_points(Integer ctd_earned_points) {
		this.ctd_earned_points = ctd_earned_points;
	}
	public void setCtd_adj_points(Integer ctd_adj_points) {
		this.ctd_adj_points = ctd_adj_points;
	}
	public void setCtd_disb_points(Integer ctd_disb_points) {
		this.ctd_disb_points = ctd_disb_points;
	}
	public void setPoint_bal(Integer point_bal) {
		this.point_bal = point_bal;
	}
	public Indicator getDual_curr_ind() {
		return dual_curr_ind;
	}
	public String getDual_curr_cd() {
		return dual_curr_cd;
	}
	public void setDual_curr_ind(Indicator dual_curr_ind) {
		this.dual_curr_ind = dual_curr_ind;
	}
	public void setDual_curr_cd(String dual_curr_cd) {
		this.dual_curr_cd = dual_curr_cd;
	}
	public Date getBilling_date() {
		return billing_date;
	}
	public void setBilling_date(Date billing_date) {
		this.billing_date = billing_date;
	}

	public BigDecimal getCtd_pmt_yet_amt() {
		return ctd_pmt_yet_amt;
	}
	public void setCtd_pmt_yet_amt(BigDecimal ctd_pmt_yet_amt) {
		this.ctd_pmt_yet_amt = ctd_pmt_yet_amt;
	}
	public BigDecimal getCtd_pmt_not_amt() {
		return ctd_pmt_not_amt;
	}
	public void setCtd_pmt_not_amt(BigDecimal ctd_pmt_not_amt) {
		this.ctd_pmt_not_amt = ctd_pmt_not_amt;
	}
	
	public BigDecimal getCtd_fee_amt() {
		return ctd_fee_amt;
	}
	public void setCtd_fee_amt(BigDecimal ctd_fee_amt) {
		this.ctd_fee_amt = ctd_fee_amt;
	}
	public Integer getCtd_fee_cnt() {
		return ctd_fee_cnt;
	}
	public void setCtd_fee_cnt(Integer ctd_fee_cnt) {
		this.ctd_fee_cnt = ctd_fee_cnt;
	}
	public BigDecimal getCtd_interest_amt() {
		return ctd_interest_amt;
	}
	public void setCtd_interest_amt(BigDecimal ctd_interest_amt) {
		this.ctd_interest_amt = ctd_interest_amt;
	}
	public Integer getCtd_interest_cnt() {
		return ctd_interest_cnt;
	}
	public void setCtd_interest_cnt(Integer ctd_interest_cnt) {
		this.ctd_interest_cnt = ctd_interest_cnt;
	}
	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
	
}
