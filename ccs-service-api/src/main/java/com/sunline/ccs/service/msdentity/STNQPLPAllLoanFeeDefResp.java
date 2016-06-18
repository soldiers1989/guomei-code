package com.sunline.ccs.service.msdentity;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ccs.service.msentity.MsResponseInfo;

public class STNQPLPAllLoanFeeDefResp extends MsResponseInfo implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
	 * 贷款参数代码列表
	 */
	@JsonProperty(value="LOAN_PLAN_LIST")
	public List<STNQPLPAllLoanFeeDefSubLoanPlan> loanPlanList;

	public List<STNQPLPAllLoanFeeDefSubLoanPlan> getLoanPlanList() {
		return loanPlanList;
	}

	public void setLoanPlanList(List<STNQPLPAllLoanFeeDefSubLoanPlan> loanPlanList) {
		this.loanPlanList = loanPlanList;
	}
	
}
