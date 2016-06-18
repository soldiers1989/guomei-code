package com.sunline.ccs.service.msdentity;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ppy.dictionary.enums.LoanType;

public class STNQPLPAllLoanPlanRespSubTermAmtRange implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 贷款产品代码
	 */
	@JsonProperty(value="LOAN_CODE")
	private String loanCode;
	/**
	 * 贷款类型
	 */
	@JsonProperty(value="LOAN_TYPE")
	private LoanType loantype;
	/**
	 * 产品描述
	 */
	@JsonProperty(value="DESC")
	private String desc;
	
	@JsonProperty(value="PROD_TERM_LIST")
	private List<STNQPLPAmtRangeRespSubTermAmtRange> prodTermList;

	public String getLoanCode() {
		return loanCode;
	}

	public void setLoanCode(String loanCode) {
		this.loanCode = loanCode;
	}

	public List<STNQPLPAmtRangeRespSubTermAmtRange> getProdTermList() {
		return prodTermList;
	}

	public void setProdTermList(
			List<STNQPLPAmtRangeRespSubTermAmtRange> prodTermList) {
		this.prodTermList = prodTermList;
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
	
}
