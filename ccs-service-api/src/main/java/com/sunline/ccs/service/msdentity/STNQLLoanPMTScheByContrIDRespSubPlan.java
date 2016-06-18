package com.sunline.ccs.service.msdentity;

import java.io.Serializable;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ccs.param.def.enums.TermAmtPaidOutInd;

/**
 *  客户合同详情查询接口返回报文
 *  信用计划列表-信用计划
 * @author zqx
 *
 */
public class STNQLLoanPMTScheByContrIDRespSubPlan implements Serializable{
	private static final long serialVersionUID = 1L;

	/*
	*当前期数
	*/
	@JsonProperty(value="PLAN_CURR_TERM")
	public Integer  planCurrTerm; 

	/*
	*总欠款
	*/
	@JsonProperty(value="PLAN_CURR_BAL")
	public BigDecimal  planCurrBal; 

	/*
	*还清日期
	*/
	@JsonProperty(value="PLAN_PAID_OUT_DATE")
	public String  planPaidOutDate; 

	/*
	*应收本金
	*/
	@JsonProperty(value="LOAN_TERM_PRIN")
	public BigDecimal  loanTermPrin; 

	/*
	*应收利息
	*/
	@JsonProperty(value="AR_INT")
	public BigDecimal  arInt; 

	/*
	*应收年费
	*/
	@JsonProperty(value="AR_ANNUAL_FEE")
	public BigDecimal  arAnnualFee; 

	/*
	*应收服务费
	*/
	@JsonProperty(value="AR_SVC_FEE")
	public BigDecimal  arSvcFee; 

	/*
	*应收违约金
	*/
	@JsonProperty(value="AR_DUE_PENALTY")
	public BigDecimal  arDuePenalty; 

	/*
	*应收交易费
	*/
	@JsonProperty(value="AR_TXN_FEE")
	public BigDecimal  arTxnFee; 

	/*
	*应收印花税
	*/
	@JsonProperty(value="AR_STAMP")
	public BigDecimal  arStamp; 

	/*
	*应收寿险费
	*/
	@JsonProperty(value="AR_LIFE_INSU_FEE")
	public BigDecimal  arLifeInsuFee; 
	
	/*
	*应收寿险费
	*/
	@JsonProperty(value="AR_PREPAY_PKG_FEE")
	public BigDecimal  arPrepayPkgFee; 

	/*
	*应收罚金
	*/
	@JsonProperty(value="AR_MULCT_FEE")
	public BigDecimal  arMulctFee; 

	/*
	*应收罚息
	*/
	@JsonProperty(value="AR_PENALTY_INT")
	public BigDecimal  arPenaltyInt; 

	/*
	*应收复利
	*/
	@JsonProperty(value="AR_COMPOUND_INT")
	public BigDecimal  arCompoundInt; 

	/*
	*应计利息
	*/
	@JsonProperty(value="ACRU_INT")
	public BigDecimal  acruInt; 

	/*
	*应计罚息
	*/
	@JsonProperty(value="ACRU_PENALTY_INT")
	public BigDecimal  acruPenaltyInt; 
	
	/*
	*客户应计罚息
	*/
	@JsonProperty(value="COOPER_ACRU_PENALTY_INT")
	public BigDecimal  cooperAcruPenaltyInt; 

	/*
	*应计复利
	*/
	@JsonProperty(value="ACRU_COMPOUND_INT")
	public BigDecimal  acruCompoundInt;

	/*
	*到期还款日期
	*/
	@JsonProperty(value="LOAN_PMT_DUE_DATE")
	public String  loanPmtBueDate;
	/*
	 * 
	 * AR_AGENT_FEE 应收代收服务费
	 */
	@JsonProperty(value="AR_AGENT_FEE")
	public BigDecimal  arAgentFee;
	
	/*
	 * 当期总欠款
	 */
	@JsonProperty(value="SCHEDULE_CURR_BAL")
	public BigDecimal  scheduleCurrBal;
	
	/*
	 * 客户方应收罚金
	 */
	@JsonProperty(value="COOPER_AR_MULCT_FEE")
	public BigDecimal  cooperArMulctFee;
	
	/*
	 * 客户方应收罚息
	 */
	@JsonProperty(value="COOPER_AR_PENALTY_INT")
	public BigDecimal  cooperArPenaltyInt;
	
	/*
	 * 客户方应收违约金
	 */
	@JsonProperty(value="COOPER_AR_DUE_PENALTY")
	public BigDecimal  cooperArDuePenalty;	
	
	/*
	 * 客户方提前还款手续费
	 */
	@JsonProperty(value="COOPER_PREPAYMENT_FEE")
	public BigDecimal  cooperPrepaymentFee;
	
	/*
	 * 还款状态
	 */
	@JsonProperty(value="PLAN_STATUS")
	public TermAmtPaidOutInd  planStatus;
	
	public TermAmtPaidOutInd getPlanStatus() {
		return planStatus;
	}

	public void setPlanStatus(TermAmtPaidOutInd planStatus) {
		this.planStatus = planStatus;
	}

	public BigDecimal getCooperArMulctFee() {
		return cooperArMulctFee;
	}

	public void setCooperArMulctFee(BigDecimal cooperArMulctFee) {
		this.cooperArMulctFee = cooperArMulctFee;
	}

	public BigDecimal getCooperArPenaltyInt() {
		return cooperArPenaltyInt;
	}

	public void setCooperArPenaltyInt(BigDecimal cooperArPenaltyInt) {
		this.cooperArPenaltyInt = cooperArPenaltyInt;
	}

	public BigDecimal getCooperArDuePenalty() {
		return cooperArDuePenalty;
	}

	public void setCooperArDuePenalty(BigDecimal cooperArDuePenalty) {
		this.cooperArDuePenalty = cooperArDuePenalty;
	}

	public BigDecimal getCooperPrepaymentFee() {
		return cooperPrepaymentFee;
	}

	public void setCooperPrepaymentFee(BigDecimal cooperPrepaymentFee) {
		this.cooperPrepaymentFee = cooperPrepaymentFee;
	}

	public BigDecimal getArAgentFee() {
		return arAgentFee;
	}

	public void setArAgentFee(BigDecimal arAgentFee) {
		this.arAgentFee = arAgentFee;
	}

	public String getLoanPmtBueDate() {
		return loanPmtBueDate;
	}

	public void setLoanPmtBueDate(String loanPmtBueDate) {
		this.loanPmtBueDate = loanPmtBueDate;
	}

	public Integer getPlanCurrTerm() {
		return planCurrTerm;
	}

	public void setPlanCurrTerm(Integer planCurrTerm) {
		this.planCurrTerm = planCurrTerm;
	}

	public BigDecimal getPlanCurrBal() {
		return planCurrBal;
	}

	public void setPlanCurrBal(BigDecimal planCurrBal) {
		this.planCurrBal = planCurrBal;
	}

	public String getPlanPaidOutDate() {
		return planPaidOutDate;
	}

	public void setPlanPaidOutDate(String planPaidOutDate) {
		this.planPaidOutDate = planPaidOutDate;
	}

	public BigDecimal getLoanTermPrin() {
		return loanTermPrin;
	}

	public void setLoanTermPrin(BigDecimal loanTermPrin) {
		this.loanTermPrin = loanTermPrin;
	}

	public BigDecimal getArInt() {
		return arInt;
	}

	public void setArInt(BigDecimal arInt) {
		this.arInt = arInt;
	}

	public BigDecimal getArAnnualFee() {
		return arAnnualFee;
	}

	public void setArAnnualFee(BigDecimal arAnnualFee) {
		this.arAnnualFee = arAnnualFee;
	}

	public BigDecimal getArSvcFee() {
		return arSvcFee;
	}

	public void setArSvcFee(BigDecimal arSvcFee) {
		this.arSvcFee = arSvcFee;
	}

	public BigDecimal getArDuePenalty() {
		return arDuePenalty;
	}

	public void setArDuePenalty(BigDecimal arDuePenalty) {
		this.arDuePenalty = arDuePenalty;
	}

	public BigDecimal getArTxnFee() {
		return arTxnFee;
	}

	public void setArTxnFee(BigDecimal arTxnFee) {
		this.arTxnFee = arTxnFee;
	}

	public BigDecimal getArStamp() {
		return arStamp;
	}

	public void setArStamp(BigDecimal arStamp) {
		this.arStamp = arStamp;
	}

	public BigDecimal getArLifeInsuFee() {
		return arLifeInsuFee;
	}

	public void setArLifeInsuFee(BigDecimal arLifeInsuFee) {
		this.arLifeInsuFee = arLifeInsuFee;
	}

	public BigDecimal getArMulctFee() {
		return arMulctFee;
	}

	public void setArMulctFee(BigDecimal arMulctFee) {
		this.arMulctFee = arMulctFee;
	}

	public BigDecimal getArPenaltyInt() {
		return arPenaltyInt;
	}

	public void setArPenaltyInt(BigDecimal arPenaltyInt) {
		this.arPenaltyInt = arPenaltyInt;
	}

	public BigDecimal getArCompoundInt() {
		return arCompoundInt;
	}

	public void setArCompoundInt(BigDecimal arCompoundInt) {
		this.arCompoundInt = arCompoundInt;
	}

	public BigDecimal getAcruInt() {
		return acruInt;
	}

	public void setAcruInt(BigDecimal acruInt) {
		this.acruInt = acruInt;
	}

	public BigDecimal getAcruPenaltyInt() {
		return acruPenaltyInt;
	}

	public void setAcruPenaltyInt(BigDecimal acruPenaltyInt) {
		this.acruPenaltyInt = acruPenaltyInt;
	}

	public BigDecimal getAcruCompoundInt() {
		return acruCompoundInt;
	}

	public void setAcruCompoundInt(BigDecimal acruCompoundInt) {
		this.acruCompoundInt = acruCompoundInt;
	}

	public BigDecimal getScheduleCurrBal() {
		return scheduleCurrBal;
	}

	public void setScheduleCurrBal(BigDecimal scheduleCurrBal) {
		this.scheduleCurrBal = scheduleCurrBal;
	}

	public BigDecimal getCooperAcruPenaltyInt() {
		return cooperAcruPenaltyInt;
	}

	public void setCooperAcruPenaltyInt(BigDecimal cooperAcruPenaltyInt) {
		this.cooperAcruPenaltyInt = cooperAcruPenaltyInt;
	}

	public BigDecimal getArPrepayPkgFee() {
		return arPrepayPkgFee;
	}

	public void setArPrepayPkgFee(BigDecimal arPrepayPkgFee) {
		this.arPrepayPkgFee = arPrepayPkgFee;
	}

}
