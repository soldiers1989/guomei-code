package com.sunline.ccs.service.msdentity;

import java.io.Serializable;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ccs.service.msentity.MsRequestInfo;
import com.sunline.ppy.dictionary.entity.Check;
import com.sunline.ppy.dictionary.enums.Indicator;

/**
 * 计算产品还款详情接口申请报文
 * @author zqx
 *
 */
public class STNTLLoanScheduleCalcReq extends MsRequestInfo implements Serializable {
	public static final long serialVersionUID = 1L;

	
	/*
	*贷款产品代码
	*/
	@Check(lengths=4,notEmpty=true)
	@JsonProperty(value="LOAN_CODE")
	public String  loanCode; 

	/*
	*贷款金额
	*/
	@Check(lengths=15,notEmpty=true)
	@JsonProperty(value="LOAN_AMT")
	public BigDecimal  loanAmt; 

	/*
	*贷款期数
	*/
	@Check(lengths=2)
	@JsonProperty(value="LOAN_TERM")
	public Integer  loanTerm; 

//	/*
//	*协议利率
//	*/
//	@Check(lengths=12,notEmpty=false)
//	@JsonProperty(value="AGREEMENT_RATE")
//	public BigDecimal  agreementRate; 

	/*
	*是否加入寿险计划
	*/
	@Check(lengths=1,notEmpty=true)
	@JsonProperty(value="LIFE_INSURANCE_IND")
	public Indicator  lifeInsuranceInd; 

	/**
	 * 是否使用协议费率
	 * 
	 */
	@Check(lengths=1,notEmpty=true,fixed=true)
	@JsonProperty(value="AGREEMENT_RATE_IND")
	public Indicator agreeRateInd;
	
	/**
	 * 分期手续费率
	 * 
	 */
	@Check(lengths=12,notEmpty=false,regular="^(([1-9]\\d*)|0)(\\.(\\d){1,8})?$")
	@JsonProperty(value="INSTALLMENT_FEE_RATE")
	public BigDecimal feeRate;
	
	/**
	 * 分期手续费固定金额
	 * 
	 */
	@Check(lengths=18,notEmpty=false,regular="^(([1-9]\\d*)|0)(\\.(\\d){1,2})?$")
	@JsonProperty(value="INSTALLMENT_FEE_AMT")
	public BigDecimal feeAmount;

	/**
	 * 贷款服务费率
	 * 
	 */
	@Check(lengths=12,notEmpty=false,regular="^(([1-9]\\d*)|0)(\\.(\\d){1,8})?$")
	@JsonProperty(value="FEE_RATE")
	public BigDecimal installmentFeeRate;
	
	/**
	 * 贷款服务费固定金额
	 * 
	 */
	@Check(lengths=18,notEmpty=false,regular="^(([1-9]\\d*)|0)(\\.(\\d){1,2})?$")
	@JsonProperty(value="FEE_AMOUNT")
	public BigDecimal installmentFeeAmt;
	/**
	 * 寿险费率
	 * 
	 */
	@Check(lengths=12,notEmpty=false,regular="^(([1-9]\\d*)|0)(\\.(\\d){1,8})?$")
	@JsonProperty(value="LIFE_INSU_FEE_RATE")
	public BigDecimal lifeInsuFeeRate;
	
	/**
	 * 寿险固定金额
	 * 
	 */
	@Check(lengths=18,notEmpty=false,regular="^(([1-9]\\d*)|0)(\\.(\\d){1,2})?$")
	@JsonProperty(value="LIFE_INSU_FEE_AMT")
	public BigDecimal lifeInsuFeeAmt;
	
	/**
	 * 保费月费率
	 * 
	 */
	@Check(lengths=12,notEmpty=false,regular="^(([1-9]\\d*)|0)(\\.(\\d){1,8})?$")
	@JsonProperty(value="INS_RATE")
	public BigDecimal insRate;
	
	/**
	 * 保费月固定金额
	 * 
	 */
	@Check(lengths=18,notEmpty=false,regular="^(([1-9]\\d*)|0)(\\.(\\d){1,2})?$")
	@JsonProperty(value="INS_AMT")
	public BigDecimal insAmt;
	

	
	/**
	 * 提前还款包费率
	 * 
	 */
	@Check(lengths=12,notEmpty=false,regular="^(([1-9]\\d*)|0)(\\.(\\d){1,8})?$")
	@JsonProperty(value="PREPAYMENT_FEE_AMOUNT_RATE")
	public BigDecimal prepaymentFeeRate;
	
	/**
	 * 提前还款包固定金额
	 * 
	 */
	@Check(lengths=18,notEmpty=false,regular="^(([1-9]\\d*)|0)(\\.(\\d){1,2})?$")
	@JsonProperty(value="PREPAYMENT_FEE_AMOUNT")
	public BigDecimal prepaymentFeeAmt;
	
	/**
	 * 罚息利率
	 * 
	 */
	@Check(lengths=12,notEmpty=false,regular="^(([1-9]\\d*)|0)(\\.(\\d){1,8})?$")
//	@JsonProperty(value="PENALTY_RATE")
	@JsonIgnore
	public BigDecimal penaltyRate;
	
	/**
	 * 复利利率
	 * 
	 */
	@Check(lengths=12,notEmpty=false,regular="^(([1-9]\\d*)|0)(\\.(\\d){1,8})?$")
//	@JsonProperty(value="COMPOUND_RATE")
	@JsonIgnore
	public BigDecimal compoundRate;
	
	/**
	 * 基础利率
	 * 
	 */
	@Check(lengths=12,notEmpty=false,regular="^(([1-9]\\d*)|0)(\\.(\\d){1,8})?$")
	@JsonProperty(value="INTEREST_RATE")
	public BigDecimal interestRate;
	
	/**
	 * 印花税费率
	 * 
	 */
	@Check(lengths=12,notEmpty=false,regular="^(([1-9]\\d*)|0)(\\.(\\d){1,8})?$")
//	@JsonProperty(value="STAMPDUTY_RATE")
	@JsonIgnore
	public BigDecimal stampRate;
	
	/**
	 * 印花税固定金额
	 * 
	 */
	@Check(lengths=18,notEmpty=false,regular="^(([1-9]\\d*)|0)(\\.(\\d){1,2})?$")
//	@JsonProperty(value="STAMP_AMT")
	@JsonIgnore
	public BigDecimal stampAmt;
	
	/**
	 * 
	 * 代收服务费费率
	 */
	@Check(lengths=12,notEmpty=false,regular="^(([1-9]\\d*)|0)(\\.(\\d){1,8})?$")
	@JsonProperty(value="AGENT_FEE_RATE")
	public BigDecimal agentFeeRate;
	
	/**
	 * 
	 * 代收服务费固定金额
	 */
	@Check(lengths=18,notEmpty=false,regular="^(([1-9]\\d*)|0)(\\.(\\d){1,2})?$")
	@JsonProperty(value="AGENT_FEE_AMOUNT")
	public BigDecimal agentFeeAmount;

	/**
	 * 是否购买灵活还款服务包
	 * 
	 */
	@Check(lengths=1,notEmpty=false,fixed=true)
	@JsonProperty(value="PREPAY_PKG_IND")
	public Indicator prepayPkgInd;
	
	public String getLoanCode() {
		return loanCode;
	}

	public void setLoanCode(String loanCode) {
		this.loanCode = loanCode;
	}

	public BigDecimal getLoanAmt() {
		return loanAmt;
	}

	public void setLoanAmt(BigDecimal loanAmt) {
		this.loanAmt = loanAmt;
	}

	public Integer getLoanTerm() {
		return loanTerm;
	}

	public void setLoanTerm(Integer loanTerm) {
		this.loanTerm = loanTerm;
	}



	public Indicator getLifeInsuranceInd() {
		return lifeInsuranceInd;
	}

	public void setLifeInsuranceInd(Indicator lifeInsuranceInd) {
		this.lifeInsuranceInd = lifeInsuranceInd;
	}

	public Indicator getAgreeRateInd() {
		return agreeRateInd;
	}

	public void setAgreeRateInd(Indicator agreeRateInd) {
		this.agreeRateInd = agreeRateInd;
	}

	public BigDecimal getFeeRate() {
		return feeRate;
	}

	public void setFeeRate(BigDecimal feeRate) {
		this.feeRate = feeRate;
	}

	public BigDecimal getFeeAmount() {
		return feeAmount;
	}

	public void setFeeAmount(BigDecimal feeAmount) {
		this.feeAmount = feeAmount;
	}

	public BigDecimal getLifeInsuFeeRate() {
		return lifeInsuFeeRate;
	}

	public void setLifeInsuFeeRate(BigDecimal lifeInsuFeeRate) {
		this.lifeInsuFeeRate = lifeInsuFeeRate;
	}

	public BigDecimal getLifeInsuFeeAmt() {
		return lifeInsuFeeAmt;
	}

	public void setLifeInsuFeeAmt(BigDecimal lifeInsuFeeAmt) {
		this.lifeInsuFeeAmt = lifeInsuFeeAmt;
	}

	public BigDecimal getInsRate() {
		return insRate;
	}

	public void setInsRate(BigDecimal insRate) {
		this.insRate = insRate;
	}

	public BigDecimal getInsAmt() {
		return insAmt;
	}

	public void setInsAmt(BigDecimal insAmt) {
		this.insAmt = insAmt;
	}

	public BigDecimal getInstallmentFeeRate() {
		return installmentFeeRate;
	}

	public void setInstallmentFeeRate(BigDecimal installmentFeeRate) {
		this.installmentFeeRate = installmentFeeRate;
	}

	public BigDecimal getInstallmentFeeAmt() {
		return installmentFeeAmt;
	}

	public void setInstallmentFeeAmt(BigDecimal installmentFeeAmt) {
		this.installmentFeeAmt = installmentFeeAmt;
	}

	public BigDecimal getPrepaymentFeeRate() {
		return prepaymentFeeRate;
	}

	public void setPrepaymentFeeRate(BigDecimal prepaymentFeeRate) {
		this.prepaymentFeeRate = prepaymentFeeRate;
	}

	public BigDecimal getPrepaymentFeeAmt() {
		return prepaymentFeeAmt;
	}

	public void setPrepaymentFeeAmt(BigDecimal prepaymentFeeAmt) {
		this.prepaymentFeeAmt = prepaymentFeeAmt;
	}

	public BigDecimal getPenaltyRate() {
		return penaltyRate;
	}

	public void setPenaltyRate(BigDecimal penaltyRate) {
		this.penaltyRate = penaltyRate;
	}

	public BigDecimal getCompoundRate() {
		return compoundRate;
	}

	public void setCompoundRate(BigDecimal compoundRate) {
		this.compoundRate = compoundRate;
	}

	public BigDecimal getInterestRate() {
		return interestRate;
	}

	public void setInterestRate(BigDecimal interestRate) {
		this.interestRate = interestRate;
	}

	public BigDecimal getStampRate() {
		return stampRate;
	}

	public void setStampRate(BigDecimal stampRate) {
		this.stampRate = stampRate;
	}

	public BigDecimal getStampAmt() {
		return stampAmt;
	}

	public void setStampAmt(BigDecimal stampAmt) {
		this.stampAmt = stampAmt;
	}

	public BigDecimal getAgentFeeRate() {
		return agentFeeRate;
	}

	public void setAgentFeeRate(BigDecimal agentFeeRate) {
		this.agentFeeRate = agentFeeRate;
	}

	public BigDecimal getAgentFeeAmount() {
		return agentFeeAmount;
	}

	public void setAgentFeeAmount(BigDecimal agentFeeAmount) {
		this.agentFeeAmount = agentFeeAmount;
	}

	public Indicator getPrepayPkgInd() {
		return prepayPkgInd;
	}

	public void setPrepayPkgInd(Indicator prepayPkgInd) {
		this.prepayPkgInd = prepayPkgInd;
	}

}
