package com.sunline.ccs.param.ui.client.program;

import com.sunline.ccs.param.def.ProgramFeeDef;

public class ProgramFeeInfo {
	private Integer loanNum;
	
	private ProgramFeeDef programFeeDef;

	public ProgramFeeDef getProgramFeeDef() {
		return programFeeDef;
	}

	public void setProgramFeeDef(ProgramFeeDef programFeeDef) {
		this.programFeeDef = programFeeDef;
	}

	public Integer getLoanNum() {
		return loanNum;
	}

	public void setLoanNum(Integer loanNum) {
		this.loanNum = loanNum;
	}

	
}
