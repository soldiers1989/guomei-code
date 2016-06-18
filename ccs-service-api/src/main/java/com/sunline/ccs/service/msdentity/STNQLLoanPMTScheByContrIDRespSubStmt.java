package com.sunline.ccs.service.msdentity;

import java.io.Serializable;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ccs.param.def.enums.TermAmtPaidOutInd;

/**
 *  客户合同详情查询接口返回报文
 *  偿清计划列表
 * @author zqx
 *
 */
public class STNQLLoanPMTScheByContrIDRespSubStmt implements Serializable{
	private static final long serialVersionUID = 1L;

	/*
	*贷款分期计划号
	*/
	@JsonProperty(value="STMT_ID")
	public String  stmtId; 

	/*
	*到期还款日
	*/
	@JsonProperty(value="PMT_DUE_DATE")
	public String  pmtDueDate; 

	/*
	*期款金额
	*/
	@JsonProperty(value="TERM_AMT")
	public BigDecimal  termAmt; 

	/*
	*本金
	*/
	@JsonProperty(value="PRINCIPLE")
	public BigDecimal  principle; 

	/*
	*本金匹配状态
	*/
	@JsonProperty(value="PRINCIPLE_MATCH_IND")
	public TermAmtPaidOutInd  principleMatchInd; 

	/*
	*利息
	*/
	@JsonProperty(value="INTEREST")
	public BigDecimal  interest; 

	/*
	*利息匹配状态
	*/
	@JsonProperty(value="INTEREST_MATCH_IND")
	public TermAmtPaidOutInd  interestMatchInd; 

	/*
	*服务费
	*/
	@JsonProperty(value="SVC_FEE")
	public BigDecimal  svcFee; 

	/*
	*服务费匹配状态
	*/
	@JsonProperty(value="SVC_FEE_MATCH_IND")
	public TermAmtPaidOutInd  svcFeeMatchInd; 

	/*
	*寿险服务费
	*/
	@JsonProperty(value="LIFE_INSU_FEE")
	public BigDecimal  lifeInsuFee; 

	/*
	*寿险服务费匹配状态
	*/
	@JsonProperty(value="LIFE_INSU_FEE_MATCH_IND")
	public TermAmtPaidOutInd  lifeInsuFeeMatchInd; 

	/*
	*提前还款服务费
	*/
	@JsonProperty(value="PREPAYMENT_FEE")
	public BigDecimal  prepaymentFee; 

	/*
	*提前还款服务费匹配状态
	*/
	@JsonProperty(value="PREPAYMENT_FEE_MATCH_IND")
	public TermAmtPaidOutInd  prepaymentFeeMatchInd; 

	/*
	*罚金
	*/
	@JsonProperty(value="MULCT_FEE")
	public BigDecimal  mulctFee; 

	/*
	*罚金匹配状态
	*/
	@JsonProperty(value="MULCT_FEE_MATCH_IND")
	public TermAmtPaidOutInd  mulctFeeMatchInd; 

	/*
	*期款状态
	*/
	@JsonProperty(value="TERM_AMT_MATCH_IND")
	public String  termAmtMatchInd; 

	/*
	*期款匹配完成日期
	*/
	@JsonProperty(value="TERM_AMT_PAID_OUT_DATE")
	public String  termAmtPaidOutDate;
	/*
	 * 代收服务费
	 * 
	 */
	@JsonProperty(value="AGENT_FEE")
	public BigDecimal  agentFee; 
	
	/*
	*代收服务费匹配状态
	*/
	@JsonProperty(value="AGENT_FEE_MATCH_IND")
	public TermAmtPaidOutInd  agentFeeMatchInd; 
	
	/*
	 * AR_AGENT_FEE 应收代收服务费
	 */
	@JsonProperty(value="SCHEDULE_CURR_BAL")
	public BigDecimal  scheduleCurrBal;
	
	/*
	 * 客户方应收罚金
	 */
	@JsonProperty(value="COOPER_AR_MULCT_FEE")
	public BigDecimal  cooperArMulctFee;
	
	/*
	 * 客户方应收罚金匹配状态
	 */
	@JsonProperty(value="COOPER_AR_MULCT_FEE_MATCH_IND")
	public TermAmtPaidOutInd  cooperArMulctFeeMatchInd;
	
	/*
	 *客户方应收罚息 
	 */
	@JsonProperty(value="COOPER_AR_PENALTY_INT")
	public BigDecimal  cooperArPenaltyInt;
	
	/* 
	 * 客户方应收罚息匹配状态
	 */
	@JsonProperty(value="COOPER_AR_PENALTY_INT_MATCH_IND")
	public TermAmtPaidOutInd  cooperArPenaltyIntMatchInd;
	
	/*
	 * 客户方应收违约金
	 */
	@JsonProperty(value="COOPER_AR_DUE_PENALTY")
	public BigDecimal  cooperArDuePenalty;
	
	/*
	 * 客户方应收违约金匹配状态
	 */
	@JsonProperty(value="COOPER_AR_DUE_PENALTY_MATCH_IND")
	public TermAmtPaidOutInd  cooperArDuePenaltyMatchInd;
	
	/*
	 * 客户方提前还款手续费
	 */
	@JsonProperty(value="COOPER_PREPAYMENT_FEE")
	public BigDecimal  cooperPrepaymentFee;
	
	/*
	 * 客户方提前还款手续费匹配状态
	 */
	@JsonProperty(value="COOPER_PREPAYMENT_FEE_MATCH_IND")
	public TermAmtPaidOutInd  cooperPrepaymentFeeMatchInd;
	
	public BigDecimal getCooperArMulctFee() {
		return cooperArMulctFee;
	}

	public void setCooperArMulctFee(BigDecimal cooperArMulctFee) {
		this.cooperArMulctFee = cooperArMulctFee;
	}

	public TermAmtPaidOutInd getCooperArMulctFeeMatchInd() {
		return cooperArMulctFeeMatchInd;
	}

	public void setCooperArMulctFeeMatchInd(TermAmtPaidOutInd cooperArMulctFeeMatchInd) {
		this.cooperArMulctFeeMatchInd = cooperArMulctFeeMatchInd;
	}

	public BigDecimal getCooperArPenaltyInt() {
		return cooperArPenaltyInt;
	}

	public void setCooperArPenaltyInt(BigDecimal cooperArPenaltyInt) {
		this.cooperArPenaltyInt = cooperArPenaltyInt;
	}

	public TermAmtPaidOutInd getCooperArPenaltyIntMatchInd() {
		return cooperArPenaltyIntMatchInd;
	}

	public void setCooperArPenaltyIntMatchInd(TermAmtPaidOutInd cooperArPenaltyIntMatchInd) {
		this.cooperArPenaltyIntMatchInd = cooperArPenaltyIntMatchInd;
	}

	public BigDecimal getCooperArDuePenalty() {
		return cooperArDuePenalty;
	}

	public void setCooperArDuePenalty(BigDecimal cooperArDuePenalty) {
		this.cooperArDuePenalty = cooperArDuePenalty;
	}


	public TermAmtPaidOutInd getCooperArDuePenaltyMatchInd() {
		return cooperArDuePenaltyMatchInd;
	}

	public void setCooperArDuePenaltyMatchInd(
			TermAmtPaidOutInd cooperArDuePenaltyMatchInd) {
		this.cooperArDuePenaltyMatchInd = cooperArDuePenaltyMatchInd;
	}

	public BigDecimal getCooperPrepaymentFee() {
		return cooperPrepaymentFee;
	}

	public void setCooperPrepaymentFee(BigDecimal cooperPrepaymentFee) {
		this.cooperPrepaymentFee = cooperPrepaymentFee;
	}

	public TermAmtPaidOutInd getCooperPrepaymentFeeMatchInd() {
		return cooperPrepaymentFeeMatchInd;
	}

	public void setCooperPrepaymentFeeMatchInd(
			TermAmtPaidOutInd cooperPrepaymentFeeMatchInd) {
		this.cooperPrepaymentFeeMatchInd = cooperPrepaymentFeeMatchInd;
	}

	
	
	public BigDecimal getAgentFee(BigDecimal bigDecimal) {
		return agentFee;
	}

	public void setAgentFee(BigDecimal agentFee) {
		this.agentFee = agentFee;
	}

	public String getStmtId() {
		return stmtId;
	}

	public void setStmtId(String stmtId) {
		this.stmtId = stmtId;
	}

	public String getPmtDueDate() {
		return pmtDueDate;
	}

	public void setPmtDueDate(String pmtDueDate) {
		this.pmtDueDate = pmtDueDate;
	}

	public BigDecimal getTermAmt() {
		return termAmt;
	}

	public void setTermAmt(BigDecimal termAmt) {
		this.termAmt = termAmt;
	}

	public BigDecimal getPrinciple() {
		return principle;
	}

	public void setPrinciple(BigDecimal principle) {
		this.principle = principle;
	}

	public TermAmtPaidOutInd getPrincipleMatchInd() {
		return principleMatchInd;
	}

	public void setPrincipleMatchInd(TermAmtPaidOutInd principleMatchInd) {
		this.principleMatchInd = principleMatchInd;
	}

	public BigDecimal getInterest() {
		return interest;
	}

	public void setInterest(BigDecimal interest) {
		this.interest = interest;
	}

	public TermAmtPaidOutInd getInterestMatchInd() {
		return interestMatchInd;
	}

	public void setInterestMatchInd(TermAmtPaidOutInd interestMatchInd) {
		this.interestMatchInd = interestMatchInd;
	}

	public BigDecimal getSvcFee() {
		return svcFee;
	}

	public void setSvcFee(BigDecimal svcFee) {
		this.svcFee = svcFee;
	}

	public TermAmtPaidOutInd getSvcFeeMatchInd() {
		return svcFeeMatchInd;
	}

	public void setSvcFeeMatchInd(TermAmtPaidOutInd svcFeeMatchInd) {
		this.svcFeeMatchInd = svcFeeMatchInd;
	}

	public BigDecimal getLifeInsuFee() {
		return lifeInsuFee;
	}

	public void setLifeInsuFee(BigDecimal lifeInsuFee) {
		this.lifeInsuFee = lifeInsuFee;
	}

	public TermAmtPaidOutInd getLifeInsuFeeMatchInd() {
		return lifeInsuFeeMatchInd;
	}

	public void setLifeInsuFeeMatchInd(TermAmtPaidOutInd lifeInsuFeeMatchInd) {
		this.lifeInsuFeeMatchInd = lifeInsuFeeMatchInd;
	}

	public BigDecimal getPrepaymentFee() {
		return prepaymentFee;
	}

	public void setPrepaymentFee(BigDecimal prepaymentFee) {
		this.prepaymentFee = prepaymentFee;
	}

	public TermAmtPaidOutInd getPrepaymentFeeMatchInd() {
		return prepaymentFeeMatchInd;
	}

	public void setPrepaymentFeeMatchInd(TermAmtPaidOutInd prepaymentFeeMatchInd) {
		this.prepaymentFeeMatchInd = prepaymentFeeMatchInd;
	}

	public BigDecimal getMulctFee() {
		return mulctFee;
	}

	public void setMulctFee(BigDecimal mulctFee) {
		this.mulctFee = mulctFee;
	}

	public TermAmtPaidOutInd getMulctFeeMatchInd() {
		return mulctFeeMatchInd;
	}

	public void setMulctFeeMatchInd(TermAmtPaidOutInd mulctFeeMatchInd) {
		this.mulctFeeMatchInd = mulctFeeMatchInd;
	}

	public String getTermAmtMatchInd() {
		return termAmtMatchInd;
	}

	public void setTermAmtMatchInd(String termAmtMatchInd) {
		this.termAmtMatchInd = termAmtMatchInd;
	}

	public String getTermAmtPaidOutDate() {
		return termAmtPaidOutDate;
	}

	public void setTermAmtPaidOutDate(String termAmtPaidOutDate) {
		this.termAmtPaidOutDate = termAmtPaidOutDate;
	}

	public TermAmtPaidOutInd getAgentFeeMatchInd() {
		return agentFeeMatchInd;
	}

	public void setAgentFeeMatchInd(TermAmtPaidOutInd agentFeeMatchInd) {
		this.agentFeeMatchInd = agentFeeMatchInd;
	}

	public BigDecimal getAgentFee() {
		return agentFee;
	}

	public BigDecimal getScheduleCurrBal() {
		return scheduleCurrBal;
	}

	public void setScheduleCurrBal(BigDecimal scheduleCurrBal) {
		this.scheduleCurrBal = scheduleCurrBal;
	} 
	
}
