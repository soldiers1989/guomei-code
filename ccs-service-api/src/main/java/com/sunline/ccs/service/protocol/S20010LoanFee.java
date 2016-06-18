package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.math.BigDecimal;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanFeeMethod;
import com.sunline.ccs.param.def.enums.CalcBaseInd;
import com.sunline.ccs.param.def.enums.CalcMethod;
import com.sunline.ccs.param.def.enums.InterestAcruMethod;
import com.sunline.ccs.param.def.enums.InterestAdjMethod;
import com.sunline.ccs.param.def.enums.PrepaymentFeeMethod;
import com.sunline.ccs.param.def.enums.TierInd;

/**
 *
* @author fanghj
 *@time 2014-3-24 下午4:28:09
 */
public class S20010LoanFee implements Serializable{

	private static final long serialVersionUID = 8907864596422625042L;

	   /**
     * 贷款期数
     */
    public Integer loan_term;

    /**
     * 贷款手续费收取方式
     */
    public LoanFeeMethod loan_fee_method;

    /**
     * 贷款手续费计算方式
     */
    public CalcMethod loan_fee_calc_method;

    /**
     * 贷款手续费金额
     */
    public BigDecimal fee_amount;

    /**
     * 贷款手续费比例
     */
    public BigDecimal fee_rate;

    /**
     * 是否允许展期
     */
    public Boolean reschedule_ind;

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

    /**
     * 是否允许缩期
     */
    public Boolean systolicphase_ind;

    /**
     * 缩期手续费计算方式
     */
    public CalcMethod systolicphase_fee_method;

    /**
     * 缩期手续费金额
     */
    public BigDecimal systolicphase_fee_amount;

    /**
     * 缩期手续费比例
     */
    public BigDecimal systolicphase_fee_amount_rate;
    
    /**
     * 基准利率
     */
    public BigDecimal interest_rate;
    
    /**
     * 复利利率
     */
    public BigDecimal compoundinttableid;

    /**
     * 罚息利率
     */
    public BigDecimal penaltyinttableid;

    /**
     * 计息方式
     */
    public InterestAcruMethod interestacrumethod;

    /**
     * 利率调整方式
     */
    public InterestAdjMethod interestadjmethod;

    /**
     * 滞纳金收取标志
     */
    public Indicator latefeecharge;

    /**
     * 滞纳金触发最小拖欠期数
     */
    public String minagecd;

    public BigDecimal getInterest_rate() {
		return interest_rate;
	}

	public void setInterest_rate(BigDecimal interest_rate) {
		this.interest_rate = interest_rate;
	}

	/**
     * 滞纳金免收最小金额
     */
    public BigDecimal mincharge;

    /**
     * 滞纳金单笔最大金额
     */
    public BigDecimal maxcharge;

    /**
     * 滞纳金年累计最大金额
     */
    public BigDecimal yearmaxcharge;

    /**
     * 滞纳今年累计最大次数
     */
    public Integer yearmaxcnt;

    /**
     * 计算基准金额
     */
    public CalcBaseInd calcbaseind;

    /**
     * 计算方式
     */
    public TierInd tierind;

	public Integer getLoan_term() {
		return loan_term;
	}

	public void setLoan_term(Integer loan_term) {
		this.loan_term = loan_term;
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

	public Boolean getReschedule_ind() {
		return reschedule_ind;
	}

	public void setReschedule_ind(Boolean reschedule_ind) {
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

	public Boolean getSystolicphase_ind() {
		return systolicphase_ind;
	}

	public void setSystolicphase_ind(Boolean systolicphase_ind) {
		this.systolicphase_ind = systolicphase_ind;
	}

	public CalcMethod getSystolicphase_fee_method() {
		return systolicphase_fee_method;
	}

	public void setSystolicphase_fee_method(CalcMethod systolicphase_fee_method) {
		this.systolicphase_fee_method = systolicphase_fee_method;
	}

	public BigDecimal getSystolicphase_fee_amount() {
		return systolicphase_fee_amount;
	}

	public void setSystolicphase_fee_amount(BigDecimal systolicphase_fee_amount) {
		this.systolicphase_fee_amount = systolicphase_fee_amount;
	}

	public BigDecimal getSystolicphase_fee_amount_rate() {
		return systolicphase_fee_amount_rate;
	}

	public void setSystolicphase_fee_amount_rate(
			BigDecimal systolicphase_fee_amount_rate) {
		this.systolicphase_fee_amount_rate = systolicphase_fee_amount_rate;
	}

	public BigDecimal getCompoundinttableid() {
		return compoundinttableid;
	}

	public void setCompoundinttableid(BigDecimal compoundinttableid) {
		this.compoundinttableid = compoundinttableid;
	}

	public BigDecimal getPenaltyinttableid() {
		return penaltyinttableid;
	}

	public void setPenaltyinttableid(BigDecimal penaltyinttableid) {
		this.penaltyinttableid = penaltyinttableid;
	}

	public InterestAcruMethod getInterestacrumethod() {
		return interestacrumethod;
	}

	public void setInterestacrumethod(InterestAcruMethod interestacrumethod) {
		this.interestacrumethod = interestacrumethod;
	}

	public InterestAdjMethod getInterestadjmethod() {
		return interestadjmethod;
	}

	public void setInterestadjmethod(InterestAdjMethod interestadjmethod) {
		this.interestadjmethod = interestadjmethod;
	}

	public Indicator getLatefeecharge() {
		return latefeecharge;
	}

	public void setLatefeecharge(Indicator latefeecharge) {
		this.latefeecharge = latefeecharge;
	}

	public String getMinagecd() {
		return minagecd;
	}

	public void setMinagecd(String minagecd) {
		this.minagecd = minagecd;
	}

	public BigDecimal getMincharge() {
		return mincharge;
	}

	public void setMincharge(BigDecimal mincharge) {
		this.mincharge = mincharge;
	}

	public BigDecimal getMaxcharge() {
		return maxcharge;
	}

	public void setMaxcharge(BigDecimal maxcharge) {
		this.maxcharge = maxcharge;
	}

	public BigDecimal getYearmaxcharge() {
		return yearmaxcharge;
	}

	public void setYearmaxcharge(BigDecimal yearmaxcharge) {
		this.yearmaxcharge = yearmaxcharge;
	}

	public Integer getYearmaxcnt() {
		return yearmaxcnt;
	}

	public void setYearmaxcnt(Integer yearmaxcnt) {
		this.yearmaxcnt = yearmaxcnt;
	}

	public CalcBaseInd getCalcbaseind() {
		return calcbaseind;
	}

	public void setCalcbaseind(CalcBaseInd calcbaseind) {
		this.calcbaseind = calcbaseind;
	}

	public TierInd getTierind() {
		return tierind;
	}

	public void setTierind(TierInd tierind) {
		this.tierind = tierind;
	}

	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
