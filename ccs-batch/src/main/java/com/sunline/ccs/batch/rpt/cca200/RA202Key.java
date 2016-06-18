package com.sunline.ccs.batch.rpt.cca200;

import java.io.Serializable;

public class RA202Key implements Serializable{
	private static final long serialVersionUID = 1L;
	private Long loanId;
	private Boolean isClaimSettled;
	
	public Long getLoanId() {
		return loanId;
	}
	public void setLoanId(Long loanId) {
		this.loanId = loanId;
	}
	public Boolean getIsClaimSettled() {
		return isClaimSettled;
	}
	public void setIsClaimSettled(Boolean isClaimSettled) {
		this.isClaimSettled = isClaimSettled;
	}

}
