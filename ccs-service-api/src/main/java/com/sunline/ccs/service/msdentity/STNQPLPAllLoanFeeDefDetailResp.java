package com.sunline.ccs.service.msdentity;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ccs.service.msentity.MsResponseInfo;

public class STNQPLPAllLoanFeeDefDetailResp extends MsResponseInfo implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	/*
	 * 贷款参数代码列表
	 */
	@JsonProperty(value="LOAN_FEE_DEF_LIST")
	public List<STNQPLPAllLoanFeeDefSubLoanFeeDefList> loanFeeDefList;

	public List<STNQPLPAllLoanFeeDefSubLoanFeeDefList> getLoanFeeDefList() {
		return loanFeeDefList;
	}

	public void setLoanFeeDefList(
			List<STNQPLPAllLoanFeeDefSubLoanFeeDefList> loanFeeDefList) {
		this.loanFeeDefList = loanFeeDefList;
	}
	
	
}