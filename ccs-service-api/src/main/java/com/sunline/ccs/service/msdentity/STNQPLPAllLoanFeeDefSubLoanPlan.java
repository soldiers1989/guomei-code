package com.sunline.ccs.service.msdentity;

import java.io.Serializable;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ccs.param.def.enums.LoanPlanStatus;
import com.sunline.ppy.dictionary.entity.Check;
import com.sunline.ppy.dictionary.enums.LoanType;

public class STNQPLPAllLoanFeeDefSubLoanPlan implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
	 * 产品ID
	 */
	@Check(lengths=4)
	@JsonProperty(value="LOAN_CODE")
	public String  loanCode;
	/**
	 * 产品描述
	 */
	@JsonProperty(value="DESC")
	private String desc;
	/**
	 * 贷款类型
	 */
	@JsonProperty(value="LOAN_TYPE")
	private LoanType loantype;
	/**
	 * 贷款产品使用状态
	 */
	@JsonProperty(value="LOAN_PLAN_STATUS")
	private LoanPlanStatus loanPlanStatus;
	/**
	 * 贷款产品有效期
	 */
	@JsonProperty(value="LOAN_PLAN_VALIDITY")
	private String loanPlanValidity;
	
	/**
	 * 产品金额范围列表
	 */
	@JsonProperty(value="LOAN_FEE_DEF_LIST")
	public List<STNQPLPAllLoanFeeDefSubLoanFeeDef> loanFeeDefList;

	
	public String getLoanCode() {
		return loanCode;
	}
	public void setLoanCode(String loanCode) {
		this.loanCode = loanCode;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public LoanType getLoantype() {
		return loantype;
	}
	public void setLoantype(LoanType loantype) {
		this.loantype = loantype;
	}
	public LoanPlanStatus getLoanPlanStatus() {
		return loanPlanStatus;
	}
	public void setLoanPlanStatus(LoanPlanStatus loanPlanStatus) {
		this.loanPlanStatus = loanPlanStatus;
	}
	public String getLoanPlanValidity() {
		return loanPlanValidity;
	}
	public void setLoanPlanValidity(String loanPlanValidity) {
		this.loanPlanValidity = loanPlanValidity;
	}
	public List<STNQPLPAllLoanFeeDefSubLoanFeeDef> getLoanFeeDefList() {
		return loanFeeDefList;
	}
	public void setLoanFeeDefList(
			List<STNQPLPAllLoanFeeDefSubLoanFeeDef> loanFeeDefList) {
		this.loanFeeDefList = loanFeeDefList;
	}
	
}
