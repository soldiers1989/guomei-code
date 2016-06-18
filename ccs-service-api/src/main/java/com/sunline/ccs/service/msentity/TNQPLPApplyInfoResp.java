package com.sunline.ccs.service.msentity;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ccs.param.def.enums.CalcMethod;
import com.sunline.ccs.param.def.enums.LoanFeeDefStatus;
import com.sunline.ccs.param.def.enums.PrepaymentFeeMethod;
import com.sunline.ppy.dictionary.entity.Check;
import com.sunline.ppy.dictionary.enums.LoanFeeMethod;
import com.sunline.ppy.dictionary.enums.LoanType;

/**
 * 产品申请参数查询返回报文(审批系统查询调用)
 * @author yuemk
 *
 */
@SuppressWarnings("serial")
public class TNQPLPApplyInfoResp extends MsResponseInfo {
	/**
	 * 贷款产品代码
	 */
	@Check(lengths=4,notEmpty=false)
	@JsonProperty(value="LOAN_CODE")
	public String loanCode;
	/**
	 * 贷款子产品代码
	 */
	@Check(lengths=8,notEmpty=false)
	@JsonProperty(value="LOAN_FEE_DEF_ID")
	public String loanFeeDefId;
	/**
	 * 贷款子产品代状态
	 */
	@Check(lengths=1,notEmpty=false)
	@JsonProperty(value="LOAN_FEE_DEF_STATUS")
	public LoanFeeDefStatus loanFeeDefStatus;
	/**
	 * 最小允许贷款金额
	 */
	@Check(lengths=18,notEmpty=true)
	@JsonProperty(value="MIN_AMT")
	public BigDecimal minAmt;
	/**
	 * 最大允许贷款金额
	 */
	@Check(lengths=18,notEmpty=true)
	@JsonProperty(value="MAX_AMT")
	public BigDecimal maxAmt;
	/**
	 * 贷款期数
	 */
	@Check(lengths=2,notEmpty=true,isNumber=true)
	@JsonProperty(value="LOAN_TERM")
	public Integer loanTerm;
	/**
	 * 月利率
	 */
	@Check(lengths=18,notEmpty=false)
	@JsonProperty(value="MONTHLY_INT_RATE")
	public BigDecimal monthlyIntRate;
	/**
	 * 产品有效年利率
	 */
	@Check(lengths=18,notEmpty=false)
	@JsonProperty(value="ANNUAL_INT_RATE")
	public BigDecimal annualIntRate;
	/**
	 * 分期手续费收取方式
	 */
	@Check(lengths=1,notEmpty=false)
	@JsonProperty(value="INSTLMT_FEE_CHARGE_MTD")
	public LoanFeeMethod svcFeeChargeMtd;
	/**
	 * 分期手续费计算方式
	 */
	@Check(lengths=1,notEmpty=false)
	@JsonProperty(value="INSTLMT_FEE_CAL_MTD")
	public CalcMethod svcFeeCalMtd;
	/**
	 * 分期手续费金额
	 */
	@Check(lengths=18,notEmpty=true)
	@JsonProperty(value="MONTHLY_INSTLMT_FEE_AMT")
	public BigDecimal monthlySvcFee;
	/**
	 * 分期手续费费率
	 */
	@Check(lengths=18,notEmpty=false)
	@JsonProperty(value="MONTHLY_INSTLMT_FEE_RATE")
	public BigDecimal monthlySvcFeeRate;

	/**
	 * 贷款服务费收取方式
	 */
	@Check(lengths=1,notEmpty=false)
	@JsonProperty(value="SVC_FEE_CHARGE_MTD")
	public LoanFeeMethod instlmtFeeChargeMtd;

	/**
	 * 贷款服务费计算方式
	 */
	@Check(lengths=1,notEmpty=false)
	@JsonProperty(value="SVC_FEE_CAL_MTD")
	public PrepaymentFeeMethod instlmtFeeCalMtd;

	/**
	 * 每月服务费
	 */
	@Check(lengths=18,notEmpty=true)
	@JsonProperty(value="MONTHLY_SVC_FEE")
	public BigDecimal monthlyInstlmtFeeAmt;

	/**
	 * 每月服务费费率
	 */
	@Check(lengths=18,notEmpty=true)
	@JsonProperty(value="MONTHLY_SVC_FEE_RATE")
	public BigDecimal monthlyInstlmtFeeRate;
	/**
	 * 寿险包收取方式
	 */
	@Check(lengths=1,notEmpty=false)
	@JsonProperty(value="LIFEINSU_FEE_CHARGE_MTD")
	public LoanFeeMethod lifeinsuFeeChargeMtd;
	/**
	 * 寿险包计算方式
	 */
	@Check(lengths=1,notEmpty=false)
	@JsonProperty(value="LIFEINSU_FEE_CAL_MTD")
	public PrepaymentFeeMethod lifeinsuFeeCalMtd;
	/**
	 * 每月寿险计划包费
	 */
	@Check(lengths=18,notEmpty=true)
	@JsonProperty(value="MONTHLY_LIFE_INSU_FEE")
	public BigDecimal monthlyLifeInsuFee;
	/**
	 * 每月寿险计划包费费率
	 */
	@Check(lengths=18,notEmpty=false)
	@JsonProperty(value="MONTHLY_LIFE_INSU_RATE")
	public BigDecimal monthlyLifeInsuRate;

	/**
	 * 保险手续费收取方式
	 */
	@Check(lengths=1,notEmpty=true)
	@JsonProperty(value="INS_COLL_METHOD")
	public LoanFeeMethod insCollMethod;
	/**
	 * 保险手续费计算方式
	 */
	@Check(lengths=1,notEmpty=true)
	@JsonProperty(value="INS_CALC_METHOD")
	public PrepaymentFeeMethod insCalcMethod;
	/**
	 * 保费金额
	 */
	@Check(lengths=18,notEmpty=true)
	@JsonProperty(value="MONTHLY_INS_FEE_AMT")
	public BigDecimal monthlyInsFeeAmt;
	/**
	 * 保费费率
	 */
	@Check(lengths=18,notEmpty=true)
	@JsonProperty(value="MONTHLY_INS_RATE")
	public BigDecimal monthlyInsRate;
	/**
	 * 贷款金额
	 */
	@Check(lengths=18,notEmpty=true)
	@JsonProperty(value="LOAN_AMT")
	public BigDecimal loanAmt;
	/**
	 * 每月应还总额
	 */
	@Check(lengths=18,notEmpty=true)
	@JsonProperty(value="LOAN_TERM_AMT")
	public BigDecimal loanTermAmt;
	/**
	 * 产品有效期
	 */
	@Check(lengths=8)
	@JsonProperty(value="LOAN_VALIDITY")
	public String loanValidity;
	/**
	 * 
	 * 贷款类型
	 */
	@Check(lengths=4,notEmpty=true)
	@JsonProperty(value="LOAN_TYPE")
	public LoanType loanType;
	/**
	 *  
	 * 待收服务费收取方式
	 */
	@Check(lengths=1,notEmpty=true)
	@JsonProperty(value="AGENT_FEE_CHARGE_MTD")
	public LoanFeeMethod agentFeeChargeMtd;
	/**
	 * 
	 * 代收服务费计算方式
	 */
	@Check(lengths=1,notEmpty=true)
	@JsonProperty(value="AGENT_FEE_CAL_MTD")
	public CalcMethod agentFeeCalMtd;
	
	/**
	 * 代收服务费金额
	 * @return
	 */
	@Check(lengths=17,notEmpty=true)
	@JsonProperty(value="MONTHLY_AGENT_FEE_AMT")
	public BigDecimal monthlyAgentFeeAmt;
	/**
	 * 代收服务费费率
	 * @return
	 */
	@Check(lengths=18,notEmpty=true)
	@JsonProperty(value="MONTHLY_AGENT_FEE_RATE")
	public BigDecimal monthlyAgentFeeRate;
	
	public LoanFeeMethod getAgentFeeChargeMtd() {
		return agentFeeChargeMtd;
	}
	public void setAgentFeeChargeMtd(LoanFeeMethod agentFeeChargeMtd) {
		this.agentFeeChargeMtd = agentFeeChargeMtd;
	}
	public CalcMethod getAgentFeeCalMtd() {
		return agentFeeCalMtd;
	}
	public void setAgentFeeCalMtd(CalcMethod agentFeeCalMtd) {
		this.agentFeeCalMtd = agentFeeCalMtd;
	}
	public BigDecimal getMonthlyAgentFeeAmt() {
		return monthlyAgentFeeAmt;
	}
	public void setMonthlyAgentFeeAmt(BigDecimal replaceFeeAmt) {
		this.monthlyAgentFeeAmt = replaceFeeAmt;
	}
	public BigDecimal getMonthlyAgentFeeRate() {
		return monthlyAgentFeeRate;
	}
	public void setMonthlyAgentFeeRate(BigDecimal replaceFeeRate) {
		this.monthlyAgentFeeRate = replaceFeeRate;
	}
	public LoanType getLoanType() {
		return loanType;
	}
	public void setLoanType(LoanType loanType) {
		this.loanType = loanType;
	}
	public String getLoanCode() {
		return loanCode;
	}
	public void setLoanCode(String loanCode) {
		this.loanCode = loanCode;
	}
	public BigDecimal getMinAmt() {
		return minAmt;
	}
	public void setMinAmt(BigDecimal minAmt) {
		this.minAmt = minAmt;
	}
	public BigDecimal getMaxAmt() {
		return maxAmt;
	}
	public void setMaxAmt(BigDecimal maxAmt) {
		this.maxAmt = maxAmt;
	}
	public Integer getLoanTerm() {
		return loanTerm;
	}
	public void setLoanTerm(Integer loanTerm) {
		this.loanTerm = loanTerm;
	}
	public BigDecimal getMonthlyIntRate() {
		return monthlyIntRate;
	}
	public void setMonthlyIntRate(BigDecimal monthlyIntRate) {
		this.monthlyIntRate = monthlyIntRate;
	}
	public BigDecimal getAnnualIntRate() {
		return annualIntRate;
	}
	public void setAnnualIntRate(BigDecimal annualIntRate) {
		this.annualIntRate = annualIntRate;
	}
	public BigDecimal getMonthlySvcFee() {
		return monthlySvcFee;
	}
	public void setMonthlySvcFee(BigDecimal monthlySvcFee) {
		this.monthlySvcFee = monthlySvcFee;
	}
	public BigDecimal getMonthlySvcFeeRate() {
		return monthlySvcFeeRate;
	}
	public void setMonthlySvcFeeRate(BigDecimal monthlySvcFeeRate) {
		this.monthlySvcFeeRate = monthlySvcFeeRate;
	}
	public BigDecimal getMonthlyLifeInsuFee() {
		return monthlyLifeInsuFee;
	}
	public void setMonthlyLifeInsuFee(BigDecimal monthlyLifeInsuFee) {
		this.monthlyLifeInsuFee = monthlyLifeInsuFee;
	}
	public BigDecimal getMonthlyLifeInsuRate() {
		return monthlyLifeInsuRate;
	}
	public void setMonthlyLifeInsuRate(BigDecimal monthlyLifeInsuRate) {
		this.monthlyLifeInsuRate = monthlyLifeInsuRate;
	}
	public BigDecimal getLoanAmt() {
		return loanAmt;
	}
	public void setLoanAmt(BigDecimal loanAmt) {
		this.loanAmt = loanAmt;
	}
	public BigDecimal getLoanTermAmt() {
		return loanTermAmt;
	}
	public void setLoanTermAmt(BigDecimal loanTermAmt) {
		this.loanTermAmt = loanTermAmt;
	}
	public String getLoanValidity() {
		return loanValidity;
	}
	public void setLoanValidity(String loanValidity) {
		this.loanValidity = loanValidity;
	}
	public String getLoanFeeDefId() {
		return loanFeeDefId;
	}
	public void setLoanFeeDefId(String loanFeeDefId) {
		this.loanFeeDefId = loanFeeDefId;
	}
	public LoanFeeMethod getSvcFeeChargeMtd() {
		return svcFeeChargeMtd;
	}
	public void setSvcFeeChargeMtd(LoanFeeMethod svcFeeChargeMtd) {
		this.svcFeeChargeMtd = svcFeeChargeMtd;
	}
	public CalcMethod getSvcFeeCalMtd() {
		return svcFeeCalMtd;
	}
	public void setSvcFeeCalMtd(CalcMethod svcFeeCalMtd) {
		this.svcFeeCalMtd = svcFeeCalMtd;
	}
	public LoanFeeMethod getLifeinsuFeeChargeMtd() {
		return lifeinsuFeeChargeMtd;
	}
	public void setLifeinsuFeeChargeMtd(LoanFeeMethod lifeinsuFeeChargeMtd) {
		this.lifeinsuFeeChargeMtd = lifeinsuFeeChargeMtd;
	}
	public PrepaymentFeeMethod getLifeinsuFeeCalMtd() {
		return lifeinsuFeeCalMtd;
	}
	public void setLifeinsuFeeCalMtd(PrepaymentFeeMethod lifeinsuFeeCalMtd) {
		this.lifeinsuFeeCalMtd = lifeinsuFeeCalMtd;
	}
	public LoanFeeMethod getInstlmtFeeChargeMtd() {
		return instlmtFeeChargeMtd;
	}
	public void setInstlmtFeeChargeMtd(LoanFeeMethod instlmtFeeChargeMtd) {
		this.instlmtFeeChargeMtd = instlmtFeeChargeMtd;
	}
	public PrepaymentFeeMethod getInstlmtFeeCalMtd() {
		return instlmtFeeCalMtd;
	}
	public void setInstlmtFeeCalMtd(PrepaymentFeeMethod instlmtFeeCalMtd) {
		this.instlmtFeeCalMtd = instlmtFeeCalMtd;
	}
	public BigDecimal getMonthlyInstlmtFeeAmt() {
		return monthlyInstlmtFeeAmt;
	}
	public void setMonthlyInstlmtFeeAmt(BigDecimal monthlyInstlmtFeeAmt) {
		this.monthlyInstlmtFeeAmt = monthlyInstlmtFeeAmt;
	}
	public BigDecimal getMonthlyInstlmtFeeRate() {
		return monthlyInstlmtFeeRate;
	}
	public void setMonthlyInstlmtFeeRate(BigDecimal monthlyInstlmtFeeRate) {
		this.monthlyInstlmtFeeRate = monthlyInstlmtFeeRate;
	}
	public LoanFeeMethod getInsCollMethod() {
		return insCollMethod;
	}
	public void setInsCollMethod(LoanFeeMethod insCollMethod) {
		this.insCollMethod = insCollMethod;
	}
	public PrepaymentFeeMethod getInsCalcMethod() {
		return insCalcMethod;
	}
	public void setInsCalcMethod(PrepaymentFeeMethod insCalcMethod) {
		this.insCalcMethod = insCalcMethod;
	}
	public BigDecimal getMonthlyInsFeeAmt() {
		return monthlyInsFeeAmt;
	}
	public void setMonthlyInsFeeAmt(BigDecimal monthlyInsFeeAmt) {
		this.monthlyInsFeeAmt = monthlyInsFeeAmt;
	}
	public BigDecimal getMonthlyInsRate() {
		return monthlyInsRate;
	}
	public void setMonthlyInsRate(BigDecimal monthlyInsRate) {
		this.monthlyInsRate = monthlyInsRate;
	}
	public LoanFeeDefStatus getLoanFeeDefStatus() {
		return loanFeeDefStatus;
	}
	public void setLoanFeeDefStatus(LoanFeeDefStatus loanFeeDefStatus) {
		this.loanFeeDefStatus = loanFeeDefStatus;
	}
	
}
