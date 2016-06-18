package com.sunline.ccs.param.ui.client.loanPlan;

import com.sunline.ccs.param.def.LoanFeeDef;

public class LoanFeeInfo {
	
	private Integer loanNum;
	
	private LoanFeeDef loanFeeDef;

	public Integer getLoanNum() {
		return loanNum;
	}

	public void setLoanNum(Integer loanNum) {
		this.loanNum = loanNum;
	}

	public LoanFeeDef getLoanFeeDef() {
		return loanFeeDef;
	}

	public void setLoanFeeDef(LoanFeeDef loanFeeDef) {
		this.loanFeeDef = loanFeeDef;
	}
	
}
