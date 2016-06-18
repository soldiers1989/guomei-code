package com.sunline.ccs.service.msdentity;

import java.io.Serializable;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 计算产品还款详情接口返回报文
 * 还款计划
 * @author zqx
 *
 */
public class STNTLLoanScheduleCalcRespSubSchedule  implements Serializable  {
	private static final long serialVersionUID = 1L;

	
	/*
	*当前期数
	*/
	@JsonProperty(value="LOAN_CURR_TERM")
	public Integer  loanCurrTerm; 

	/*
	*应还总额
	*/
	@JsonProperty(value="LOAN_TERM_TOT_AMT")
	public BigDecimal  loanTermTotAmt; 
	
	/*
	*应还本金
	*/
	@JsonProperty(value="LOAN_TERM_PRIN")
	public BigDecimal  loanTermPrin; 

//	/*
//	*应还费用
//	*/
//	@JsonProperty(value="LOAN_TERM_FEE")
//	public BigDecimal  loanTermFee; 
	
	/*
	*应还手续费
	*/
	@JsonProperty(value="LOAN_TERM_INSTALL_FEE")
	public BigDecimal  loanTermInstallFee; 

	/*
	 *应还服务费
	 */
	@JsonProperty(value="LOAN_TERM_SVC_FEE")
	public BigDecimal  loanTermSvcFee; 
	
	/*
	*应还利息
	*/
	@JsonProperty(value="LOAN_TERM_INT")
	public BigDecimal  loanTermInt; 
	
	/*
	 *应还寿险费
	 */
	@JsonProperty(value="LOAN_LIFE_INSU_FEE")
	public BigDecimal  loanLifeInsuFee; 

	/*
	 *应还提前还款计划包费
	 */
	@JsonProperty(value="LOAN_PREPAY_PKG_FEE")
	public BigDecimal  loanPrepayPkgFee; 
	
	/*
	 *应还保费
	 */
	@JsonProperty(value="LOAN_INSURANCE_FEE")
	public BigDecimal  loanInsuranceFee; 

	/*
	 *应还代收服务费
	 */
	@JsonProperty(value="LOAN_AGENT_FEE")
	public BigDecimal  loanAgentFee; 
	
	/*
	 *到期还款日期
	 */
	@JsonProperty(value="LOAN_PMT_DUE_DATE")
	public String  loanPmtDueDate; 
	
	/*
	*应还印花税
	*/
	@JsonProperty(value="LOAN_STAMP_DUTY_AMT")
	public BigDecimal  loanStampDutyAmt; 
	
	public Integer getLoanCurrTerm() {
		return loanCurrTerm;
	}

	public void setLoanCurrTerm(Integer loanCurrTerm) {
		this.loanCurrTerm = loanCurrTerm;
	}

	public BigDecimal getLoanTermPrin() {
		return loanTermPrin;
	}

	public void setLoanTermPrin(BigDecimal loanTermPrin) {
		this.loanTermPrin = loanTermPrin;
	}

	public BigDecimal getLoanTermInt() {
		return loanTermInt;
	}

	public void setLoanTermInt(BigDecimal loanTermInt) {
		this.loanTermInt = loanTermInt;
	}

	public BigDecimal getLoanStampDutyAmt() {
		return loanStampDutyAmt;
	}

	public void setLoanStampDutyAmt(BigDecimal loanStampDutyAmt) {
		this.loanStampDutyAmt = loanStampDutyAmt;
	}

	public BigDecimal getLoanLifeInsuFee() {
		return loanLifeInsuFee;
	}

	public void setLoanLifeInsuFee(BigDecimal loanLifeInsuFee) {
		this.loanLifeInsuFee = loanLifeInsuFee;
	}

	public BigDecimal getLoanTermTotAmt() {
		return loanTermTotAmt;
	}

	public void setLoanTermTotAmt(BigDecimal loanTermTotAmt) {
		this.loanTermTotAmt = loanTermTotAmt;
	}

	public BigDecimal getLoanTermInstallFee() {
		return loanTermInstallFee;
	}

	public void setLoanTermInstallFee(BigDecimal loanTermInstallFee) {
		this.loanTermInstallFee = loanTermInstallFee;
	}

	public BigDecimal getLoanTermSvcFee() {
		return loanTermSvcFee;
	}

	public void setLoanTermSvcFee(BigDecimal loanTermSvcFee) {
		this.loanTermSvcFee = loanTermSvcFee;
	}

	public BigDecimal getLoanPrepayPkgFee() {
		return loanPrepayPkgFee;
	}

	public void setLoanPrepayPkgFee(BigDecimal loanPrepayPkgFee) {
		this.loanPrepayPkgFee = loanPrepayPkgFee;
	}

	public BigDecimal getLoanInsuranceFee() {
		return loanInsuranceFee;
	}

	public void setLoanInsuranceFee(BigDecimal loanInsuranceFee) {
		this.loanInsuranceFee = loanInsuranceFee;
	}

	public BigDecimal getLoanAgentFee() {
		return loanAgentFee;
	}

	public void setLoanAgentFee(BigDecimal loanAgentFee) {
		this.loanAgentFee = loanAgentFee;
	}

	public String getLoanPmtDueDate() {
		return loanPmtDueDate;
	}

	public void setLoanPmtDueDate(String loanPmtDueDate) {
		this.loanPmtDueDate = loanPmtDueDate;
	}
	
}
