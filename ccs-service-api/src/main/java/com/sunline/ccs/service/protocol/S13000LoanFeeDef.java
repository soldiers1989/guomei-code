
package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.math.BigDecimal;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanFeeMethod;
import com.sunline.ccs.param.def.enums.CalcMethod;
import com.sunline.ccs.param.def.enums.PrepaymentFeeMethod;

/**
* @author fanghj
 *2013-11-20上午9:42:00
 *version 1.0
 */
public class S13000LoanFeeDef implements Serializable{

	private static final long serialVersionUID = 4859592325014914846L;

	/**
	 * 分期期数
	 */
	public Integer loan_init_term;
	
	/**
	 * 最小分期金额
	 */
	public BigDecimal min_amount;
	
	/**
	 * 最大允许分期金额
	 */
	public BigDecimal max_amount;
	
	/**
	 * 分期手续费收取方式
	 */
	public LoanFeeMethod loan_fee_method;
	
	/**
	 * 分期手续费计算方式
	 */
	public CalcMethod loan_fee_calc_method;
	
	/**
	 * 分期手续费金额
	 */
	public BigDecimal fee_amount;
	
	/**
	 * 分期手续费比例
	 */
	public BigDecimal fee_rate;
	
	/**
	 * 是否允许展期
	 */
	public Indicator reschedule_ind;
	
	/**
	 * 展期手续费收取方式
	 */
	public LoanFeeMethod reschedule_fee_method;
	
	/**
	 * 展期手续费计算方式
	 */
	public CalcMethod reschedule_calc_method;
	
	/**
	 * 展期手续费金额
	 */
	public BigDecimal reschedule_fee_amount;
	
	/**
	 * 展期手续费比例
	 */
	public BigDecimal reschedule_fee_rate;
	
	/**
	 * 提前还款手续费计算方式
	 */
	public PrepaymentFeeMethod prepayment_fee_method;
	
	/**
	 * 提前还款手续费金额
	 */
	public BigDecimal prepayment_fee_amount;
	
	/**
	 * 提前还款手续费比例
	 */
	public BigDecimal prepayment_fee_amount_rate;

	public Integer getLoan_init_term() {
		return loan_init_term;
	}

	public void setLoan_init_term(Integer loan_init_term) {
		this.loan_init_term = loan_init_term;
	}

	public BigDecimal getMin_amount() {
		return min_amount;
	}

	public void setMin_amount(BigDecimal min_amount) {
		this.min_amount = min_amount;
	}

	public BigDecimal getMax_amount() {
		return max_amount;
	}

	public void setMax_amount(BigDecimal max_amount) {
		this.max_amount = max_amount;
	}

	public LoanFeeMethod getLoan_fee_method() {
		return loan_fee_method;
	}

	public void setLoan_fee_method(LoanFeeMethod loan_fee_method) {
		this.loan_fee_method = loan_fee_method;
	}

	public CalcMethod getLoan_fee_calc_method() {
		return loan_fee_calc_method;
	}

	public void setLoan_fee_calc_method(CalcMethod loan_fee_calc_method) {
		this.loan_fee_calc_method = loan_fee_calc_method;
	}

	public BigDecimal getFee_amount() {
		return fee_amount;
	}

	public void setFee_amount(BigDecimal fee_amount) {
		this.fee_amount = fee_amount;
	}

	public BigDecimal getFee_rate() {
		return fee_rate;
	}

	public void setFee_rate(BigDecimal fee_rate) {
		this.fee_rate = fee_rate;
	}

	public Indicator getReschedule_ind() {
		return reschedule_ind;
	}

	public void setReschedule_ind(Indicator reschedule_ind) {
		this.reschedule_ind = reschedule_ind;
	}

	public LoanFeeMethod getReschedule_fee_method() {
		return reschedule_fee_method;
	}

	public void setReschedule_fee_method(LoanFeeMethod reschedule_fee_method) {
		this.reschedule_fee_method = reschedule_fee_method;
	}

	public CalcMethod getReschedule_calc_method() {
		return reschedule_calc_method;
	}

	public void setReschedule_calc_method(CalcMethod reschedule_calc_method) {
		this.reschedule_calc_method = reschedule_calc_method;
	}

	public BigDecimal getReschedule_fee_amount() {
		return reschedule_fee_amount;
	}

	public void setReschedule_fee_amount(BigDecimal reschedule_fee_amount) {
		this.reschedule_fee_amount = reschedule_fee_amount;
	}

	public BigDecimal getReschedule_fee_rate() {
		return reschedule_fee_rate;
	}

	public void setReschedule_fee_rate(BigDecimal reschedule_fee_rate) {
		this.reschedule_fee_rate = reschedule_fee_rate;
	}

	public PrepaymentFeeMethod getPrepayment_fee_method() {
		return prepayment_fee_method;
	}

	public void setPrepayment_fee_method(PrepaymentFeeMethod prepayment_fee_method) {
		this.prepayment_fee_method = prepayment_fee_method;
	}

	public BigDecimal getPrepayment_fee_amount() {
		return prepayment_fee_amount;
	}

	public void setPrepayment_fee_amount(BigDecimal prepayment_fee_amount) {
		this.prepayment_fee_amount = prepayment_fee_amount;
	}

	public BigDecimal getPrepayment_fee_amount_rate() {
		return prepayment_fee_amount_rate;
	}

	public void setPrepayment_fee_amount_rate(BigDecimal prepayment_fee_amount_rate) {
		this.prepayment_fee_amount_rate = prepayment_fee_amount_rate;
	}
	
	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
	
}
