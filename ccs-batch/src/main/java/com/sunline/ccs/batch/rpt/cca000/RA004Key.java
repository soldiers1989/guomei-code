package com.sunline.ccs.batch.rpt.cca000;

import java.io.Serializable;

public class RA004Key implements Serializable{
	
	private static final long serialVersionUID = 1;
	
	private Long scheduleId;
	private Long loanId;
	private Long planId;
	private Integer termStatus;
	
	public Long getScheduleId() {
		return scheduleId;
	}
	public void setScheduleId(Long scheduleId) {
		this.scheduleId = scheduleId;
	}
	public Long getLoanId() {
		return loanId;
	}
	public void setLoanId(Long loanId) {
		this.loanId = loanId;
	}
	public Long getPlanId() {
		return planId;
	}
	public void setPlanId(Long planId) {
		this.planId = planId;
	}

	public Integer getTermStatus() {
		return termStatus;
	}
	public void setTermStatus(Integer termStatus) {
		this.termStatus = termStatus;
	}

}
