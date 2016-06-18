package com.sunline.ccs.service.msdentity;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ccs.service.msentity.MsResponseInfo;
/**
 * 返回所有参数loanplan集合
 * @author zhengjf
 *
 */
public class STNQPLPAllLoanPlanResp extends MsResponseInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 贷款参数代码列表
	 */
	@JsonProperty(value="LOAN_PLAN_LIST")
	private List<STNQPLPAllLoanPlanRespSubTermAmtRange> loanPlanList;

	public List<STNQPLPAllLoanPlanRespSubTermAmtRange> getLoanPlanList() {
		return loanPlanList;
	}

	public void setLoanPlanList(
			List<STNQPLPAllLoanPlanRespSubTermAmtRange> loanPlanList) {
		this.loanPlanList = loanPlanList;
	}
	
}
