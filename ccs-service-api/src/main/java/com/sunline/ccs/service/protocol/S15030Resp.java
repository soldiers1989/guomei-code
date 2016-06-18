package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sunline.ppy.dictionary.enums.Indicator;

/**
 * 卡片限额设定
 * 
* @author fanghj
 * 
 */
public class S15030Resp implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2264845340539683296L;
	/**
	 * 卡号
	 */
	public String card_no;
	/**
	 * 币种
	 */
	public String curr_cd;
	/**
	 * 消费周期限额
	 */
	public BigDecimal cycle_limit;
	/**
	 * 取现周期限额
	 */
	public BigDecimal cycle_cash_limit;
	/**
	 * 网上消费周期限额
	 */
	public BigDecimal cycle_net_limit;
	/**
	 * 消费单笔限额
	 */
	public BigDecimal txn_limit;

	/**
	 * 取现单笔限额
	 */
	public BigDecimal txn_cash_limit;

	/**
	 * 网上消费单笔限额
	 */
	public BigDecimal txn_net_limit;

	/**
	 * 是否存在外币标识
	 */
	public Indicator dual_curr_ind;

	/**
	 * 外币币种
	 */
	public String dual_curr_cd;

	/**
	 * 当天ATM取现交易笔数
	 */
	public Integer day_used_atm_nbr;

	/**
	 * 当天ATM取现金额
	 */
	public BigDecimal day_used_atm_amt;

	/**
	 * 当天消费笔数
	 */
	public Integer day_used_retail_nbr;

	/**
	 * 当天消费金额
	 */

	public BigDecimal day_used_retail_amt;

	/**
	 * 当天取现笔数
	 */
	public Integer day_used_cash_nbr;

	/**
	 * 当天取现金额
	 */
	public BigDecimal day_used_cash_amt;

	/**
	 * 当天转出笔数
	 */
	public Integer day_used_xfrout_nbr;
	/**
	 * 当日ATM银联境外取现金额
	 */
	public BigDecimal day_used_atm_cupxb_amt;
	
	/**
     * 当期消费总金额
     */
	 public BigDecimal  ctd_used_amt ;
    
	 /**
     * 当期取现金额
     */
	 public BigDecimal ctd_cash_amt;
	 
	 /**
	  * 网银当期交易金额
	  */
	 public BigDecimal ctd_net_retl_amt;

	/**
	 * 当天转出金额
	 */
	public BigDecimal day_used_xfrout_amt;

	/**
	 * ATM覆盖起始日期
	 */
	public Date day_atm_override_start;

	/**
	 * ATM覆盖终止日期
	 */
	public Date day_atm_override_end;

	/**
	 * 单日ATM取现限笔
	 */
	public Integer day_atm_nbr_limit;

	/**
	 * 单日ATM取现限额
	 */
	public BigDecimal day_atm_amt_limit;

	/**
	 * 消费覆盖起始日期
	 */
	public Date day_retail_override_start;

	/**
	 * 消费覆盖终止日期
	 */
	public Date day_retail_override_end;

	/**
	 * 单日消费限笔
	 */
	public Integer day_retail_nbr_limit;

	/**
	 * 单日消费限额
	 */
	public BigDecimal day_retail_amt_limit;

	/**
	 * 取现覆盖起始日期
	 */
	public Date day_cash_override_start;

	/**
	 * 取现覆盖终止日期
	 */
	public Date day_cash_override_end;

	/**
	 * 单日取现限笔
	 */
	public Integer day_cash_nbr_limit;
	/**
	 * 单日取现限额
	 */
	public BigDecimal day_cash_amt_limit;

	/**
	 * 转出覆盖起始日期
	 */
	public Date day_xfrout_override_start;

	/**
	 * 转出覆盖终止日期
	 */
	public Date day_xfrout_override_end;

	/**
	 * 单日转出限笔
	 */
	public Integer day_xfrout_nbr_limit;

	/**
	 * 单日转出限额
	 */
	public BigDecimal day_xfrout_amt_limit;

	/**
	 * 单日银联境外ATM限制覆盖起始日期
	 */

	public Date day_cupxb_atm_override_start;

	/**
	 * 单日银联境外ATM覆盖终止日期
	 */
	public Date day_cupxb_atm_override_end;

	/**
	 * 单日银联境外ATM取现限额
	 */
	public Integer day_cupxb_atm_amt_limit;
	
	
	 
	public Indicator getDual_curr_ind() {
		return dual_curr_ind;
	}

	public void setDual_curr_ind(Indicator dual_curr_ind) {
		this.dual_curr_ind = dual_curr_ind;
	}

	public String getDual_curr_cd() {
		return dual_curr_cd;
	}

	public void setDual_curr_cd(String dual_curr_cd) {
		this.dual_curr_cd = dual_curr_cd;
	}

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

	public BigDecimal getCycle_limit() {
		return cycle_limit;
	}

	public void setCycle_limit(BigDecimal cycle_limit) {
		this.cycle_limit = cycle_limit;
	}

	public BigDecimal getCycle_cash_limit() {
		return cycle_cash_limit;
	}

	public void setCycle_cash_limit(BigDecimal cycle_cash_limit) {
		this.cycle_cash_limit = cycle_cash_limit;
	}

	public BigDecimal getCycle_net_limit() {
		return cycle_net_limit;
	}

	public void setCycle_net_limit(BigDecimal cycle_net_limit) {
		this.cycle_net_limit = cycle_net_limit;
	}

	public BigDecimal getTxn_limit() {
		return txn_limit;
	}

	public void setTxn_limit(BigDecimal txn_limit) {
		this.txn_limit = txn_limit;
	}

	public BigDecimal getTxn_cash_limit() {
		return txn_cash_limit;
	}

	public void setTxn_cash_limit(BigDecimal txn_cash_limit) {
		this.txn_cash_limit = txn_cash_limit;
	}

	public BigDecimal getTxn_net_limit() {
		return txn_net_limit;
	}

	public void setTxn_net_limit(BigDecimal txn_net_limit) {
		this.txn_net_limit = txn_net_limit;
	}
	
	public Integer getDay_used_atm_nbr() {
		return day_used_atm_nbr;
	}

	public void setDay_used_atm_nbr(Integer day_used_atm_nbr) {
		this.day_used_atm_nbr = day_used_atm_nbr;
	}

	public BigDecimal getDay_used_atm_amt() {
		return day_used_atm_amt;
	}

	public void setDay_used_atm_amt(BigDecimal day_used_atm_amt) {
		this.day_used_atm_amt = day_used_atm_amt;
	}

	public Integer getDay_used_retail_nbr() {
		return day_used_retail_nbr;
	}

	public void setDay_used_retail_nbr(Integer day_used_retail_nbr) {
		this.day_used_retail_nbr = day_used_retail_nbr;
	}

	public BigDecimal getDay_used_retail_amt() {
		return day_used_retail_amt;
	}

	public void setDay_used_retail_amt(BigDecimal day_used_retail_amt) {
		this.day_used_retail_amt = day_used_retail_amt;
	}

	public Integer getDay_used_cash_nbr() {
		return day_used_cash_nbr;
	}

	public void setDay_used_cash_nbr(Integer day_used_cash_nbr) {
		this.day_used_cash_nbr = day_used_cash_nbr;
	}

	public BigDecimal getDay_used_cash_amt() {
		return day_used_cash_amt;
	}

	public void setDay_used_cash_amt(BigDecimal day_used_cash_amt) {
		this.day_used_cash_amt = day_used_cash_amt;
	}

	public Integer getDay_used_xfrout_nbr() {
		return day_used_xfrout_nbr;
	}

	public void setDay_used_xfrout_nbr(Integer day_used_xfrout_nbr) {
		this.day_used_xfrout_nbr = day_used_xfrout_nbr;
	}

	public BigDecimal getDay_used_xfrout_amt() {
		return day_used_xfrout_amt;
	}

	public void setDay_used_xfrout_amt(BigDecimal day_used_xfrout_amt) {
		this.day_used_xfrout_amt = day_used_xfrout_amt;
	}
    
	public Date getDay_atm_override_start() {
		return day_atm_override_start;
	}

	public void setDay_atm_override_start(Date day_atm_override_start) {
		this.day_atm_override_start = day_atm_override_start;
	}

	public Date getDay_atm_override_end() {
		return day_atm_override_end;
	}

	public void setDay_atm_override_end(Date day_atm_override_end) {
		this.day_atm_override_end = day_atm_override_end;
	}

	public Integer getDay_atm_nbr_limit() {
		return day_atm_nbr_limit;
	}

	public void setDay_atm_nbr_limit(Integer day_atm_nbr_limit) {
		this.day_atm_nbr_limit = day_atm_nbr_limit;
	}

	public BigDecimal getDay_atm_amt_limit() {
		return day_atm_amt_limit;
	}

	public void setDay_atm_amt_limit(BigDecimal day_atm_amt_limit) {
		this.day_atm_amt_limit = day_atm_amt_limit;
	}

	public Date getDay_retail_override_start() {
		return day_retail_override_start;
	}

	public void setDay_retail_override_start(Date day_retail_override_start) {
		this.day_retail_override_start = day_retail_override_start;
	}

	public Date getDay_retail_override_end() {
		return day_retail_override_end;
	}

	public void setDay_retail_override_end(Date day_retail_override_end) {
		this.day_retail_override_end = day_retail_override_end;
	}

	public Integer getDay_retail_nbr_limit() {
		return day_retail_nbr_limit;
	}

	public void setDay_retail_nbr_limit(Integer day_retail_nbr_limit) {
		this.day_retail_nbr_limit = day_retail_nbr_limit;
	}

	public BigDecimal getDay_retail_amt_limit() {
		return day_retail_amt_limit;
	}

	public void setDay_retail_amt_limit(BigDecimal day_retail_amt_limit) {
		this.day_retail_amt_limit = day_retail_amt_limit;
	}

	public Date getDay_cash_override_start() {
		return day_cash_override_start;
	}

	public void setDay_cash_override_start(Date day_cash_override_start) {
		this.day_cash_override_start = day_cash_override_start;
	}
	public Date getDay_cash_override_end() {
		return day_cash_override_end;
	}

	public void setDay_cash_override_end(Date day_cash_override_end) {
		this.day_cash_override_end = day_cash_override_end;
	}

	public Integer getDay_cash_nbr_limit() {
		return day_cash_nbr_limit;
	}

	public void setDay_cash_nbr_limit(Integer day_cash_nbr_limit) {
		this.day_cash_nbr_limit = day_cash_nbr_limit;
	}

	public BigDecimal getDay_cash_amt_limit() {
		return day_cash_amt_limit;
	}

	public void setDay_cash_amt_limit(BigDecimal day_cash_amt_limit) {
		this.day_cash_amt_limit = day_cash_amt_limit;
	}

	public Date getDay_xfrout_override_start() {
		return day_xfrout_override_start;
	}

	public void setDay_xfrout_override_start(Date day_xfrout_override_start) {
		this.day_xfrout_override_start = day_xfrout_override_start;
	}

	public Date getDay_xfrout_override_end() {
		return day_xfrout_override_end;
	}

	public void setDay_xfrout_override_end(Date day_xfrout_override_end) {
		this.day_xfrout_override_end = day_xfrout_override_end;
	}

	public Integer getDay_xfrout_nbr_limit() {
		return day_xfrout_nbr_limit;
	}

	public void setDay_xfrout_nbr_limit(Integer day_xfrout_nbr_limit) {
		this.day_xfrout_nbr_limit = day_xfrout_nbr_limit;
	}

	public BigDecimal getDay_xfrout_amt_limit() {
		return day_xfrout_amt_limit;
	}

	public void setDay_xfrout_amt_limit(BigDecimal day_xfrout_amt_limit) {
		this.day_xfrout_amt_limit = day_xfrout_amt_limit;
	}

	public Date getDay_cupxb_atm_override_start() {
		return day_cupxb_atm_override_start;
	}

	public void setDay_cupxb_atm_override_start(Date day_cupxb_atm_override_start) {
		this.day_cupxb_atm_override_start = day_cupxb_atm_override_start;
	}

	public Date getDay_cupxb_atm_override_end() {
		return day_cupxb_atm_override_end;
	}

	public void setDay_cupxb_atm_override_end(Date day_cupxb_atm_override_end) {
		this.day_cupxb_atm_override_end = day_cupxb_atm_override_end;
	}

	public Integer getDay_cupxb_atm_amt_limit() {
		return day_cupxb_atm_amt_limit;
	}

	public void setDay_cupxb_atm_amt_limit(Integer day_cupxb_atm_amt_limit) {
		this.day_cupxb_atm_amt_limit = day_cupxb_atm_amt_limit;
	}

	public BigDecimal getCtd_used_amt() {
		return ctd_used_amt;
	}

	public void setCtd_used_amt(BigDecimal ctd_used_amt) {
		this.ctd_used_amt = ctd_used_amt;
	}

	public BigDecimal getCtd_cash_amt() {
		return ctd_cash_amt;
	}

	public void setCtd_cash_amt(BigDecimal ctd_cash_amt) {
		this.ctd_cash_amt = ctd_cash_amt;
	}
	public BigDecimal getDay_used_atm_cupxb_amt() {
		return day_used_atm_cupxb_amt;
	}

	public void setDay_used_atm_cupxb_amt(BigDecimal day_used_atm_cupxb_amt) {
		this.day_used_atm_cupxb_amt = day_used_atm_cupxb_amt;
	}

	public BigDecimal getCtd_net_retl_amt() {
		return ctd_net_retl_amt;
	}

	public void setCtd_net_retl_amt(BigDecimal ctd_net_retl_amt) {
		this.ctd_net_retl_amt = ctd_net_retl_amt;
	}

	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
