package com.sunline.ccs.service.msentity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ppy.dictionary.entity.Check;
/**
 * 去哪还款计划结果通知接口
 * @author  Mr.L
 *
 */
public class TNMTrustLoanSchedNoticeReq extends MsRequestInfo implements Serializable {

	private static final long serialVersionUID = 1L;
	
	
	/**
	 * 贷款总金额 
	 */
	@Check(lengths=18,notEmpty=true)
	@JsonProperty(value="AMOUNT")
	public BigDecimal amount;
	
	/**
	 * 贷款总期限 
	 */
	@Check(lengths=2,notEmpty=true)
	@JsonProperty(value="TERM")
	public Integer term;
	

	/**
	 * 合同号
	 */
	@Check(lengths=32,notEmpty=true)
	@JsonProperty(value="CONTR_NBR")
	public String contractNo;
	
	/**
	 * 贷款流水号
	 */
	@Check(lengths=64,notEmpty=true)
	@JsonProperty(value="LOAN_NO")
	public String loanNo;
	/**
	 * 更新时间
	 */
	@Check(lengths=20,notEmpty=true)
	@JsonProperty(value="CHANGE_TIME")
	public String changeTime;
	
	/**
	 * 还款计划明细 列表
	 */
	@JsonProperty(value="SCHEDULE_DETAILS")
    public List<TNMTrustLoanSchedReqSubPlan> scheduleDetails;


	public BigDecimal getAmount() {
		return amount;
	}


	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}


	public Integer getTerm() {
		return term;
	}


	public void setTerm(Integer term) {
		this.term = term;
	}


	public String getContractNo() {
		return contractNo;
	}


	public void setContractNo(String contractNo) {
		this.contractNo = contractNo;
	}


	public String getLoanNo() {
		return loanNo;
	}


	public void setLoanNo(String loanNo) {
		this.loanNo = loanNo;
	}


	public List<TNMTrustLoanSchedReqSubPlan> getScheduleDetails() {
		return scheduleDetails;
	}


	public void setScheduleDetails(List<TNMTrustLoanSchedReqSubPlan> scheduleDetails) {
		this.scheduleDetails = scheduleDetails;
	}


	public String getChangeTime() {
		return changeTime;
	}


	public void setChangeTime(String changeTime) {
		this.changeTime = changeTime;
	}
	
	
}
