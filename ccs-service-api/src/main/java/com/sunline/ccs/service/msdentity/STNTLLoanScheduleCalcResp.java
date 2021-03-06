package com.sunline.ccs.service.msdentity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ccs.service.msentity.MsResponseInfo;

/**
 * 计算产品还款详情接口返回报文
 * @author zqx
 *
 */
public class STNTLLoanScheduleCalcResp extends MsResponseInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/*
	*贷款期数
	*/
	@JsonProperty(value="LOAN_TERM")
	public Integer  loanTerm; 

	/*
	*贷款总本金
	*/
	@JsonProperty(value="LOAN_INIT_PRIN")
	public BigDecimal  loanInitPrin; 

	/*
	*贷款总利息
	*/
	@JsonProperty(value="LOAN_INT_SUM")
	public BigDecimal  loanIntSum; 

	/*
	*贷款总费用
	*/
	@JsonProperty(value="LOAN_FEE_SUM")
	public BigDecimal  loanFeeSum; 

//	/**
//	 * 首个还款日
//	 */
//	@JsonProperty(value="FIRST_PMT_DUE_DATE")
//	public String firstPmtDueDate;
	/*
	*还款计划列表
	*/
	@JsonProperty(value="SCHEDULES")
	public List<STNTLLoanScheduleCalcRespSubSchedule>  subScheduleList = new ArrayList<STNTLLoanScheduleCalcRespSubSchedule>();

//	public String getFirstPmtDueDate() {
//		return firstPmtDueDate;
//	}
//
//	public void setFirstPmtDueDate(String firstPmtDueDate) {
//		this.firstPmtDueDate = firstPmtDueDate;
//	}

	public Integer getLoanTerm() {
		return loanTerm;
	}

	public void setLoanTerm(Integer loanTerm) {
		this.loanTerm = loanTerm;
	}

	public BigDecimal getLoanInitPrin() {
		return loanInitPrin;
	}

	public void setLoanInitPrin(BigDecimal loanInitPrin) {
		this.loanInitPrin = loanInitPrin;
	}

	public BigDecimal getLoanIntSum() {
		return loanIntSum;
	}

	public void setLoanIntSum(BigDecimal loanIntSum) {
		this.loanIntSum = loanIntSum;
	}

	public BigDecimal getLoanFeeSum() {
		return loanFeeSum;
	}

	public void setLoanFeeSum(BigDecimal loanFeeSum) {
		this.loanFeeSum = loanFeeSum;
	}

	public List<STNTLLoanScheduleCalcRespSubSchedule> getSubScheduleList() {
		return subScheduleList;
	}

	public void setSubScheduleList(
			List<STNTLLoanScheduleCalcRespSubSchedule> subScheduleList) {
		this.subScheduleList = subScheduleList;
	}
	
}
