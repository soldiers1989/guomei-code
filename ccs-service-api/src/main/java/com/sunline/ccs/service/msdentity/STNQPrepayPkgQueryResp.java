package com.sunline.ccs.service.msdentity;

import java.io.Serializable;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ccs.service.msentity.MsResponseInfo;
import com.sunline.ppy.dictionary.enums.Indicator;
@SuppressWarnings("serial")
public class STNQPrepayPkgQueryResp extends MsResponseInfo implements Serializable {
	
	
	/**
	 * 是否使用灵活还款计划包
	 */
	@JsonProperty(value="PREPAY_PKG_IND")
	public Indicator prepayPkgInd;
	
	/**
	*灵活还款计划包比例
	*/
	@JsonProperty(value="PREPAY_PKG_FEE_AMOUNT_RATE")
	public BigDecimal  prepayPkgFeeAmountRate; 

	/**
	*灵活还款计划包固定金额
	*/
	@JsonProperty(value="PREPAY_PKG_FEE_AMOUNT")
	public BigDecimal  prepayPkgFeeAmount; 
	
	/**
	 * 延期还款申请距离还款日提前天数
	 * delayApplyAdvDays
	 */
	@JsonProperty(value="DELAY_APPLY_ADV_DAYS")
	public Integer delayApplyAdvDays;
	/**
	 * 延期还款申请最大次数
	 * delayApplyMax
	 */
	@JsonProperty(value="DELAY_APPLY_MAX")
	public Integer delayApplyMax;
	/**
	 *  延期还款每次延期最大期数
	 *  delayMaxTerm
	 */
	@JsonProperty(value="DELAY_MAX_TERM")
	public Integer delayMaxTerm;
	
	/**
	 * 延期还款累计延期最大期数
	 * delayAccuMaxTerm 
	 */
	@JsonProperty(value="DELAY_ACCU_MAX_TERM")
	public Integer delayAccuMaxTerm;
	/**
	 *  延期还款首次申请足额还款期数
	 *  delayFristApplyTerm
	 */
	@JsonProperty(value="DELAY_FRIST_APPLY_TERM")
	public Integer delayFristApplyTerm;
	/**
	 *  延期还款再次申请距离上次足额偿还期数
	 *  delayApplyAgainTerm
	 */
	@JsonProperty(value="DELAY_APPLY_AGAIN_TERM")
	public Integer delayApplyAgainTerm;
	/**
	 *  变更还款日次月生效申请提前天数
	 *  payDateExpireAdvDays
	 */
	@JsonProperty(value="PAY_DATE_EXPIRE_ADV_DAYS")
	public Integer payDateExpireAdvDays;
	/**
	 *  变更还款日首次申请足额还款期数
	 *  payDateFirstApplyTerm
	 */
	@JsonProperty(value="PAY_DATE_FIRST_APPLY_TERM")
	public Integer payDateFirstApplyTerm;
	/**
	 *  变更还款日再次申请距离上次足额偿还期数
	 *  payDateApplyAgainTerm
	 */
	@JsonProperty(value="PAY_DATE_APPLY_AGAIN_TERM")
	public Integer payDateApplyAgainTerm;
	/**
	 *  变更还款日累计变更最大次数
	 *  payDateAccuMax
	 */
	@JsonProperty(value="PAY_DATE_ACCU_MAX")
	public Integer payDateAccuMax;
	/**
	 *  优惠提前还款申请足额还款期数
	 *  disPrepaymentApplyTerm
	 */
	@JsonProperty(value="DIS_PREPAYMENT_APPLY_TERM")
	public Integer disPrepaymentApplyTerm;
	/**
	 * 是否免收宽限期利息
	 */
	@JsonProperty(value="WAVIE_GRACE_INT_IND")
	public Indicator wavieGraceIntInd;
	
	
	public Integer getDelayApplyAdvDays() {
		return delayApplyAdvDays;
	}
	public void setDelayApplyAdvDays(Integer delayApplyAdvDays) {
		this.delayApplyAdvDays = delayApplyAdvDays;
	}
	public Integer getDelayApplyMax() {
		return delayApplyMax;
	}
	public void setDelayApplyMax(Integer delayApplyMax) {
		this.delayApplyMax = delayApplyMax;
	}
	public Integer getDelayMaxTerm() {
		return delayMaxTerm;
	}
	public void setDelayMaxTerm(Integer delayMaxTerm) {
		this.delayMaxTerm = delayMaxTerm;
	}
	public Integer getDelayAccuMaxTerm() {
		return delayAccuMaxTerm;
	}
	public void setDelayAccuMaxTerm(Integer delayAccuMaxTerm) {
		this.delayAccuMaxTerm = delayAccuMaxTerm;
	}
	public Integer getDelayFristApplyTerm() {
		return delayFristApplyTerm;
	}
	public void setDelayFristApplyTerm(Integer delayFristApplyTerm) {
		this.delayFristApplyTerm = delayFristApplyTerm;
	}
	public Integer getDelayApplyAgainTerm() {
		return delayApplyAgainTerm;
	}
	public void setDelayApplyAgainTerm(Integer delayApplyAgainTerm) {
		this.delayApplyAgainTerm = delayApplyAgainTerm;
	}
	public Integer getPayDateExpireAdvDays() {
		return payDateExpireAdvDays;
	}
	public void setPayDateExpireAdvDays(Integer payDateExpireAdvDays) {
		this.payDateExpireAdvDays = payDateExpireAdvDays;
	}
	public Integer getPayDateFirstApplyTerm() {
		return payDateFirstApplyTerm;
	}
	public void setPayDateFirstApplyTerm(Integer payDateFirstApplyTerm) {
		this.payDateFirstApplyTerm = payDateFirstApplyTerm;
	}
	public Integer getPayDateApplyAgainTerm() {
		return payDateApplyAgainTerm;
	}
	public void setPayDateApplyAgainTerm(Integer payDateApplyAgainTerm) {
		this.payDateApplyAgainTerm = payDateApplyAgainTerm;
	}
	public Integer getPayDateAccuMax() {
		return payDateAccuMax;
	}
	public void setPayDateAccuMax(Integer payDateAccuMax) {
		this.payDateAccuMax = payDateAccuMax;
	}
	public Integer getDisPrepaymentApplyTerm() {
		return disPrepaymentApplyTerm;
	}
	public void setDisPrepaymentApplyTerm(Integer disPrepaymentApplyTerm) {
		this.disPrepaymentApplyTerm = disPrepaymentApplyTerm;
	}
	public Indicator getWavieGraceIntInd() {
		return wavieGraceIntInd;
	}
	public void setWavieGraceIntInd(Indicator wavieGraceIntInd) {
		this.wavieGraceIntInd = wavieGraceIntInd;
	}
	public Indicator getPrepayPkgInd() {
		return prepayPkgInd;
	}
	public void setPrepayPkgInd(Indicator prepayPkgInd) {
		this.prepayPkgInd = prepayPkgInd;
	}
	public BigDecimal getPrepayPkgFeeAmountRate() {
		return prepayPkgFeeAmountRate;
	}
	public void setPrepayPkgFeeAmountRate(BigDecimal prepayPkgFeeAmountRate) {
		this.prepayPkgFeeAmountRate = prepayPkgFeeAmountRate;
	}
	public BigDecimal getPrepayPkgFeeAmount() {
		return prepayPkgFeeAmount;
	}
	public void setPrepayPkgFeeAmount(BigDecimal prepayPkgFeeAmount) {
		this.prepayPkgFeeAmount = prepayPkgFeeAmount;
	}

}
