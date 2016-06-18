package com.sunline.ccs.service.msdentity;

import java.io.Serializable;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanStatus;
import com.sunline.ppy.dictionary.enums.LoanType;

/**
 * 客户合同列表查询接口返回报文 借据列表-单个贷款
 * 
 * @author zqx
 * 
 */
public class STNQAAcctsbyCustUUIDRESPSubLoan implements Serializable {

	private static final long serialVersionUID = 1L;
	/*
	*借据号
	*/
	@JsonProperty(value="DUE_BILL_NO")
	public String  dueBillNo; 

	/*
	*贷款产品代码
	*/
	@JsonProperty(value="LOAN_CODE")
	public String  loanCode; 
	/*
	*贷款类型
	*/
	@JsonProperty(value="LOAN_TYPE")
	public LoanType  loanType; 
	/*
	*借据状态
	*/
	@JsonProperty(value="LOAN_STATUS")
	public LoanStatus  loanStatus; 

	/*
	*激活日期
	*/
	@JsonProperty(value="ACTIVE_DATE")
	public String  activeDate; 

	/*
	*贷款总期数
	*/
	@JsonProperty(value="LOAN_INIT_TERM")
	public Integer  loanInitTerm; 

	/*
	*当前期数
	*/
	@JsonProperty(value="LOAN_CURR_TERM")
	public Integer  loanCurrTerm; 

	/*
	*剩余期数
	*/
	@JsonProperty(value="LOAN_REMAIN_TERM")
	public Integer  loanRemainTerm; 

	/*
	*贷款总本金
	*/
	@JsonProperty(value="LOAN_INIT_PRIN")
	public BigDecimal  loanInitPrin; 

	/*
	*贷款到期日期
	*/
	@JsonProperty(value="LOAN_EXPIRE_DATE")
	public String  loanExpireDate; 

	/*
	*逾期起始日期
	*/
	@JsonProperty(value="LOAN_OVERDUE_DATE")
	public String  loanOverdueDate; 

	/*
	*逾期欠款总额
	*/
	@JsonProperty(value="OVERDUE_AMT")
	public BigDecimal  overdueAmt; 

	/*
	*下期期款
	*/
	@JsonProperty(value="NEXT_TERM_AMT")
	public BigDecimal  nextTermAmt; 
	
	/*
	 * 是否使用协议费率
	 */
	@JsonProperty(value="AGREEMENT_RATE_IND")
	public Indicator  agreementRateInd;
	
	/*
	 * 分期手续费率
	 */
	@JsonProperty(value="INSTALLMENT_FEE_RATE")
	public BigDecimal  feeRate;
	/*
	 * 分期手续费固定金额
	 */
	@JsonProperty(value="INSTALLMENT_FEE_AMT")
	public BigDecimal  feeAmt;
	/*
	 * 贷款服务费率
	 */
	@JsonProperty(value="FEE_RATE")
	public BigDecimal  installmentFeeRate;
	
	/*
	 * 贷款服务费固定金额
	 */
	@JsonProperty(value="FEE_AMOUNT")
	public BigDecimal  installmentFeeAmt;
	/*
	 * 寿险费率
	 */
	@JsonProperty(value="LIFE_INSU_FEE_RATE")
	public BigDecimal  lifeInsuFeeRate;
	
	/*
	 * 寿险固定金额
	 */
	@JsonProperty(value="LIFE_INSU_FEE_AMT")
	public BigDecimal  lifeInsuFeeAmt;
	
	/*
	 * 保费月费率
	 */
	@JsonProperty(value="INS_RATE")
	public BigDecimal  insuranceRate;
	
	/*
	 * 保费月固定金额
	 */
	@JsonProperty(value="INS_AMT")
	public BigDecimal  insAmt;
	
	/*
	 * 提前还款包费率
	 */
	@JsonProperty(value="PREPAYMENT_FEE_AMOUNT_RATE")
	public BigDecimal  prepayPkgFeeRate;
	/*
	 * 提前还款包固定金额
	 */
	@JsonProperty(value="PREPAYMENT_FEE_AMOUNT")
	public BigDecimal  prepayPkgFeeAmt;
	
	/*
	 * 罚息利率
	 */
	@JsonProperty(value="PENALTY_RATE")
	public BigDecimal  penaltyRate;
		
	/*
	 * 复利利率
	 */
	@JsonProperty(value="COMPOUND_RATE")
	public BigDecimal  compoundRate;
		
	/*
	 * 基础利率
	 */
	@JsonProperty(value="INTEREST_RATE")
	public BigDecimal  interestRate;
		
	/*
	 * 印花税固定金额
	 */
	@JsonProperty(value="STAMP_AMT")
	public BigDecimal  stampAmt;
	
	/*
	 * 印花税费率
	 */
	@JsonProperty(value="STAMPDUTY_RATE")
	public BigDecimal  stampdutyRate;
	
	/*
	 * 代收服务费率
	 */
	@JsonProperty(value="AGENT_FEE_RATE")
	public BigDecimal  agentFeeRate;
	
	/*
	 * 代收服务费固定金额
	 */
	@JsonProperty(value="AGENT_FEE_AMOUNT")
	public BigDecimal  agentFeeMount;
	
	/*
	 * 趸交费固定金额
	 */
	@JsonProperty(value="PREMIUM_AMT")
	public BigDecimal  premiumAmt;
	/*
	 * 是否代收趸交费
	 */
	@JsonProperty(value="PREMIUM_IND")
	public Indicator  premiumInd;
	
	public BigDecimal getPremiumAmt() {
		return premiumAmt;
	}

	public void setPremiumAmt(BigDecimal premiumAmt) {
		this.premiumAmt = premiumAmt;
	}

	public Indicator getPremiumInd() {
		return premiumInd;
	}

	public void setPremiumInd(Indicator premiumInd) {
		this.premiumInd = premiumInd;
	}

	public String getDueBillNo() {
		return dueBillNo;
	}

	public void setDueBillNo(String dueBillNo) {
		this.dueBillNo = dueBillNo;
	}

	public String getLoanCode() {
		return loanCode;
	}

	public void setLoanCode(String loanCode) {
		this.loanCode = loanCode;
	}

	public LoanStatus getLoanStatus() {
		return loanStatus;
	}

	public void setLoanStatus(LoanStatus loanStatus) {
		this.loanStatus = loanStatus;
	}

	public String getActiveDate() {
		return activeDate;
	}

	public void setActiveDate(String activeDate) {
		this.activeDate = activeDate;
	}

	public Integer getLoanInitTerm() {
		return loanInitTerm;
	}

	public void setLoanInitTerm(Integer loanInitTerm) {
		this.loanInitTerm = loanInitTerm;
	}

	public Integer getLoanCurrTerm() {
		return loanCurrTerm;
	}

	public void setLoanCurrTerm(Integer loanCurrTerm) {
		this.loanCurrTerm = loanCurrTerm;
	}

	public Integer getLoanRemainTerm() {
		return loanRemainTerm;
	}

	public void setLoanRemainTerm(Integer loanRemainTerm) {
		this.loanRemainTerm = loanRemainTerm;
	}

	public BigDecimal getLoanInitPrin() {
		return loanInitPrin;
	}

	public void setLoanInitPrin(BigDecimal loanInitPrin) {
		this.loanInitPrin = loanInitPrin;
	}

	public String getLoanExpireDate() {
		return loanExpireDate;
	}

	public void setLoanExpireDate(String loanExpireDate) {
		this.loanExpireDate = loanExpireDate;
	}

	public String getLoanOverdueDate() {
		return loanOverdueDate;
	}

	public void setLoanOverdueDate(String loanOverdueDate) {
		this.loanOverdueDate = loanOverdueDate;
	}

	public BigDecimal getOverdueAmt() {
		return overdueAmt;
	}

	public void setOverdueAmt(BigDecimal overdueAmt) {
		this.overdueAmt = overdueAmt;
	}

	public BigDecimal getNextTermAmt() {
		return nextTermAmt;
	}

	public void setNextTermAmt(BigDecimal nextTermAmt) {
		this.nextTermAmt = nextTermAmt;
	}

	public LoanType getLoanType() {
		return loanType;
	}

	public void setLoanType(LoanType loanType) {
		this.loanType = loanType;
	}

	public Indicator getAgreementRateInd() {
		return agreementRateInd;
	}

	public void setAgreementRateInd(Indicator agreementRateInd) {
		this.agreementRateInd = agreementRateInd;
	}

	public BigDecimal getFeeRate() {
		return feeRate;
	}

	public void setFeeRate(BigDecimal feeRate) {
		this.feeRate = feeRate;
	}

	public BigDecimal getFeeAmt() {
		return feeAmt;
	}

	public void setFeeAmt(BigDecimal feeAmt) {
		this.feeAmt = feeAmt;
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

	public BigDecimal getInsuranceRate() {
		return insuranceRate;
	}

	public void setInsuranceRate(BigDecimal insuranceRate) {
		this.insuranceRate = insuranceRate;
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

	public BigDecimal getPrepayPkgFeeRate() {
		return prepayPkgFeeRate;
	}

	public void setPrepayPkgFeeRate(BigDecimal prepayPkgFeeRate) {
		this.prepayPkgFeeRate = prepayPkgFeeRate;
	}

	public BigDecimal getPrepayPkgFeeAmt() {
		return prepayPkgFeeAmt;
	}

	public void setPrepayPkgFeeAmt(BigDecimal prepayPkgFeeAmt) {
		this.prepayPkgFeeAmt = prepayPkgFeeAmt;
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

	public BigDecimal getStampAmt() {
		return stampAmt;
	}

	public void setStampAmt(BigDecimal stampAmt) {
		this.stampAmt = stampAmt;
	}

	public BigDecimal getStampdutyRate() {
		return stampdutyRate;
	}

	public void setStampdutyRate(BigDecimal stampdutyRate) {
		this.stampdutyRate = stampdutyRate;
	}

	public BigDecimal getAgentFeeRate() {
		return agentFeeRate;
	}

	public void setAgentFeeRate(BigDecimal agentFeeRate) {
		this.agentFeeRate = agentFeeRate;
	}

	public BigDecimal getAgentFeeMount() {
		return agentFeeMount;
	}

	public void setAgentFeeMount(BigDecimal agentFeeMount) {
		this.agentFeeMount = agentFeeMount;
	}
	
}
