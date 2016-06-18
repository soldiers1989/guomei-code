package com.sunline.ccs.service.msdentity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ccs.param.def.enums.LoanTerminateReason;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanStatus;
import com.sunline.ppy.dictionary.enums.LoanType;

/**
 *  客户合同详情查询接口返回报文
 *  贷款列表-贷款
 * @author zqx
 *
 */
public class STNQLLoanPMTScheByContrIDRespSubLoan implements Serializable {
	private static final long serialVersionUID = 1L;

	/*
	*借据号
	*/
	@JsonProperty(value="DUE_BILL_NO")
	public String  dueBillNo; 
	/*
	*贷款代码
	*/
	@JsonProperty(value="LOAN_CODE")
	public String  loanCode; 
	/*
	*贷款类型
	*/
	@JsonProperty(value="LOAN_TYPE")
	public LoanType  loanType; 
	/*
	*激活日期
	*/
	@JsonProperty(value="LOAN_ACTIVE_DATE")
	public String  loanActiveDate; 
	/*
	*还清日期
	*/
	@JsonProperty(value="LOAN_PAID_OUT_DATE")
	public String  loanPaidOutDate; 

	/*
	*提前终止日期
	*/
	@JsonProperty(value="LOAN_TERMINAL_DATE")
	public String  loanTerminalDate; 

	/*
	*借据终止原因
	*/
	@JsonProperty(value="TERMINAL_REASON_CD")
	public LoanTerminateReason  terminalReasonCd; 

	/*
	*借据状态
	*/
	@JsonProperty(value="LOAN_STATUS")
	public LoanStatus  loanStatus; 

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
	*贷款总手续费
	*/
	@JsonProperty(value="LOAN_INIT_FEE")
	public BigDecimal  loanInitFee; 

	/*
	*总印花税
	*/
	@JsonProperty(value="LOAN_STAMPDUTY_AMT")
	public BigDecimal  loanStampdutyAmt; 

	/*
	*总寿险费用
	*/
	@JsonProperty(value="LOAN_LIFEINSU_AMT")
	public BigDecimal  loanLifeinsuAmt; 
	
	/*
	*总总灵活还款服务包费
	*/
	@JsonProperty(value="LOAN_PREPAY_PKG_AMT")
	public BigDecimal  loanPrepayPkgAmt; 

	/*
	*已偿还本金
	*/
	@JsonProperty(value="LOAN_PAID_PRIN")
	public BigDecimal  loanPaidPrin; 

	/*
	*已偿还利息
	*/
	@JsonProperty(value="LOAN_PAID_INT")
	public BigDecimal  loanPaidInt; 

	/*
	*已偿还费用
	*/
	@JsonProperty(value="LOAN_PAID_FEE")
	public BigDecimal  loanPaidFee; 

	/*
	*已偿还印花税
	*/
	@JsonProperty(value="LOAN_PAID_STMP")
	public BigDecimal  loanPaidStmp; 

	/*
	*已偿还寿险费用
	*/
	@JsonProperty(value="LOAN_PAID_LIFEINSU_FEE")
	public BigDecimal  loanPaidLifeinsuFee; 

	/*
	*当前总欠款
	*/
	@JsonProperty(value="LOAN_CURR_BAL")
	public BigDecimal  loanCurrBal; 

	/*
	*未到期总欠款
	*/
	@JsonProperty(value="LOAN_REMAIN_AMT")
	public BigDecimal  loanRemainAmt; 
	 
	/*
	*未到期本金
	*/
	@JsonProperty(value="LOAN_REMAIN_PRIN")
	public BigDecimal  loanRemainPrin; 
	 
	/*
	*未出账本金
	*/
	@JsonProperty(value="LOAN_UNSETTLE_PRIN")
	public BigDecimal  loanUnsettlePrin; 
	
	/*
	*基础利率
	*/
	@JsonProperty(value="LOAN_INT_RATE")
	public BigDecimal  loanIntRate; 

	/*
	*罚息利率
	*/
	@JsonProperty(value="LOAN_PENALTY_RATE")
	public BigDecimal  loanPenaltyRate; 

	/*
	*复利利率
	*/
	@JsonProperty(value="LOAN_COMP_RATE")
	public BigDecimal  loanCompRate; 

	/*
	*浮动比例
	*/
	@JsonProperty(value="LOAN_FLOAT_RATE")
	public BigDecimal  loanFloatRate; 

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
	*贷款逾期最大期数
	*/
	@JsonProperty(value="LOAN_AGE_CODE")
	public String  loanAgeCode; 
	
	/*
	*逾期欠款总额
	*/
	@JsonProperty(value="OVERDUE_AMT")
	public BigDecimal  overdueAmt; 
	
	/*
	*下一期期款
	*/
	@JsonProperty(value="NEXT_TERM_AMT")
	public BigDecimal  nextTermAmt; 
	
	/*
	*DPD最大值
	*/
	@JsonProperty(value="LOAN_MAX_DPD")
	public Integer  loanMaxDpd; 

	/*
	*最大DPD日期
	*/
	@JsonProperty(value="MAX_DPD_DATE")
	public String  maxDpdDate; 

	/*
	*CPD最大值
	*/
	@JsonProperty(value="LOAN_MAX_CPD")
	public Integer  loanMaxCpd; 

	/*
	*最大CPD日期
	*/
	@JsonProperty(value="MAX_CPD_DATE")
	public String  maxCpdDate; 
	
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
	 * 灵活还款服务包费率
	 */
	@JsonProperty(value="PREPAYMENT_FEE_AMOUNT_RATE")
	public BigDecimal  prepayPkgFeeRate;
	/*
	 * 灵活还款服务包固定金额
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
	 * 总代收服务费
	 * 
	 */
	@JsonProperty(value="LOAN_AGENT_FEE")
	public BigDecimal  loanAgentFee; 
	/*
	 * 
	 * 已偿还代收服务费
	 */
	@JsonProperty(value="LOAN_PAID_AGENT_FEE")
	public BigDecimal  loanPaidAgentFee; 
	/*
	 * 
	 * AGENT_FEE_RATE 代收服务费率
	 */
	@JsonProperty(value="AGENT_FEE_RATE")
	public BigDecimal  agentFeeRate; 
	/*
	 * AGENT_FEE_AMOUNT
	 * 代收服务费固定金额
	 */
	@JsonProperty(value="AGENT_FEE_AMOUNT")
	public BigDecimal  agentFeeAmount; 
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
	
	/*
	 * 客户方代收罚息利率
	 */
	@JsonProperty(value="COOPER_PENALTY_RATE")
    public BigDecimal cooperPenaltyRate;
	
	/*
	 * 还款计划/分期计划 列表
	 */
	@JsonProperty(value="PLAN_LIST")
    public List<STNQLLoanPMTScheByContrIDRespSubPlan> subPlanList;

	/*
	 * 偿清列表
	 */
	@JsonProperty(value="STMT_LIST")
    public List<STNQLLoanPMTScheByContrIDRespSubStmt> subStmtList;
	
	
	public BigDecimal getCooperPenaltyRate() {
		return cooperPenaltyRate;
	}

	public void setCooperPenaltyRate(BigDecimal cooperPenaltyRate) {
		this.cooperPenaltyRate = cooperPenaltyRate;
	}

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

	
	public BigDecimal getLoanPaidAgentFee() {
		return loanPaidAgentFee;
	}

	public void setLoanPaidAgentFee(BigDecimal loanPaidAgentFee) {
		this.loanPaidAgentFee = loanPaidAgentFee;
	}

	public BigDecimal getLoanAgentFee() {
		return loanAgentFee;
	}

	public void setLoanAgentFee(BigDecimal loanAgentFee) {
		this.loanAgentFee = loanAgentFee;
	}

	public String getDueBillNo() {
		return dueBillNo;
	}

	public void setDueBillNo(String dueBillNo) {
		this.dueBillNo = dueBillNo;
	}

	public String getLoanActiveDate() {
		return loanActiveDate;
	}

	public void setLoanActiveDate(String loanActiveDate) {
		this.loanActiveDate = loanActiveDate;
	}

	public String getLoanPaidOutDate() {
		return loanPaidOutDate;
	}

	public void setLoanPaidOutDate(String loanPaidOutDate) {
		this.loanPaidOutDate = loanPaidOutDate;
	}

	public String getLoanTerminalDate() {
		return loanTerminalDate;
	}

	public void setLoanTerminalDate(String loanTerminalDate) {
		this.loanTerminalDate = loanTerminalDate;
	}

	public LoanTerminateReason getTerminalReasonCd() {
		return terminalReasonCd;
	}

	public void setTerminalReasonCd(LoanTerminateReason terminalReasonCd) {
		this.terminalReasonCd = terminalReasonCd;
	}

	public LoanStatus getLoanStatus() {
		return loanStatus;
	}

	public void setLoanStatus(LoanStatus loanStatus) {
		this.loanStatus = loanStatus;
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

	public BigDecimal getLoanInitFee() {
		return loanInitFee;
	}

	public void setLoanInitFee(BigDecimal loanInitFee) {
		this.loanInitFee = loanInitFee;
	}

	public BigDecimal getLoanStampdutyAmt() {
		return loanStampdutyAmt;
	}

	public void setLoanStampdutyAmt(BigDecimal loanStampdutyAmt) {
		this.loanStampdutyAmt = loanStampdutyAmt;
	}

	public BigDecimal getLoanLifeinsuAmt() {
		return loanLifeinsuAmt;
	}

	public void setLoanLifeinsuAmt(BigDecimal loanLifeinsuAmt) {
		this.loanLifeinsuAmt = loanLifeinsuAmt;
	}

	public BigDecimal getLoanPaidPrin() {
		return loanPaidPrin;
	}

	public void setLoanPaidPrin(BigDecimal loanPaidPrin) {
		this.loanPaidPrin = loanPaidPrin;
	}

	public BigDecimal getLoanPaidInt() {
		return loanPaidInt;
	}

	public void setLoanPaidInt(BigDecimal loanPaidInt) {
		this.loanPaidInt = loanPaidInt;
	}

	public BigDecimal getLoanPaidFee() {
		return loanPaidFee;
	}

	public void setLoanPaidFee(BigDecimal loanPaidFee) {
		this.loanPaidFee = loanPaidFee;
	}

	public BigDecimal getLoanPaidStmp() {
		return loanPaidStmp;
	}

	public void setLoanPaidStmp(BigDecimal loanPaidStmp) {
		this.loanPaidStmp = loanPaidStmp;
	}

	public BigDecimal getLoanPaidLifeinsuFee() {
		return loanPaidLifeinsuFee;
	}

	public void setLoanPaidLifeinsuFee(BigDecimal loanPaidLifeinsuFee) {
		this.loanPaidLifeinsuFee = loanPaidLifeinsuFee;
	}

	public BigDecimal getLoanCurrBal() {
		return loanCurrBal;
	}

	public void setLoanCurrBal(BigDecimal loanCurrBal) {
		this.loanCurrBal = loanCurrBal;
	}

	public BigDecimal getLoanRemainAmt() {
		return loanRemainAmt;
	}

	public void setLoanRemainAmt(BigDecimal loanRemainAmt) {
		this.loanRemainAmt = loanRemainAmt;
	}

	public BigDecimal getLoanIntRate() {
		return loanIntRate;
	}

	public void setLoanIntRate(BigDecimal loanIntRate) {
		this.loanIntRate = loanIntRate;
	}

	public BigDecimal getLoanPenaltyRate() {
		return loanPenaltyRate;
	}

	public void setLoanPenaltyRate(BigDecimal loanPenaltyRate) {
		this.loanPenaltyRate = loanPenaltyRate;
	}

	public BigDecimal getLoanCompRate() {
		return loanCompRate;
	}

	public void setLoanCompRate(BigDecimal loanCompRate) {
		this.loanCompRate = loanCompRate;
	}

	public BigDecimal getLoanFloatRate() {
		return loanFloatRate;
	}

	public void setLoanFloatRate(BigDecimal loanFloatRate) {
		this.loanFloatRate = loanFloatRate;
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

	public String getLoanAgeCode() {
		return loanAgeCode;
	}

	public void setLoanAgeCode(String loanAgeCode) {
		this.loanAgeCode = loanAgeCode;
	}

	public Integer getLoanMaxDpd() {
		return loanMaxDpd;
	}

	public void setLoanMaxDpd(Integer loanMaxDpd) {
		this.loanMaxDpd = loanMaxDpd;
	}

	public String getMaxDpdDate() {
		return maxDpdDate;
	}

	public void setMaxDpdDate(String maxDpdDate) {
		this.maxDpdDate = maxDpdDate;
	}

	public Integer getLoanMaxCpd() {
		return loanMaxCpd;
	}

	public void setLoanMaxCpd(Integer loanMaxCpd) {
		this.loanMaxCpd = loanMaxCpd;
	}

	public String getMaxCpdDate() {
		return maxCpdDate;
	}

	public void setMaxCpdDate(String maxCpdDate) {
		this.maxCpdDate = maxCpdDate;
	}

	public List<STNQLLoanPMTScheByContrIDRespSubPlan> getSubPlanList() {
		return subPlanList;
	}

	public void setSubPlanList(
			List<STNQLLoanPMTScheByContrIDRespSubPlan> subPlanList) {
		this.subPlanList = subPlanList;
	}
	
	public String getLoanCode() {
		return loanCode;
	}

	public void setLoanCode(String loanCode) {
		this.loanCode = loanCode;
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

	public List<STNQLLoanPMTScheByContrIDRespSubStmt> getSubStmtList() {
		return subStmtList;
	}

	public void setSubStmtList(
			List<STNQLLoanPMTScheByContrIDRespSubStmt> subStmtList) {
		this.subStmtList = subStmtList;
	}

	public LoanType getLoanType() {
		return loanType;
	}

	public void setLoanType(LoanType loanType) {
		this.loanType = loanType;
	}

	public BigDecimal getLoanRemainPrin() {
		return loanRemainPrin;
	}

	public void setLoanRemainPrin(BigDecimal loanRemainPrin) {
		this.loanRemainPrin = loanRemainPrin;
	}

	public BigDecimal getLoanUnsettlePrin() {
		return loanUnsettlePrin;
	}

	public void setLoanUnsettlePrin(BigDecimal loanUnsettlePrin) {
		this.loanUnsettlePrin = loanUnsettlePrin;
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

	public BigDecimal getLoanPrepayPkgAmt() {
		return loanPrepayPkgAmt;
	}

	public void setLoanPrepayPkgAmt(BigDecimal loanPrepayPkgAmt) {
		this.loanPrepayPkgAmt = loanPrepayPkgAmt;
	}


}
