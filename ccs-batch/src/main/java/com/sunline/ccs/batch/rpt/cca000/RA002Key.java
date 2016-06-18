package com.sunline.ccs.batch.rpt.cca000;

import java.io.Serializable;

public class RA002Key implements Serializable{
	
	private static final long serialVersionUID = 1;
	
	private Integer planStatus;
	private Long planId;
	private Long scheduleId;
	private Long loanId;
	
	public Integer getPlanStatus() {
		return planStatus;
	}
	public void setPlanStatus(Integer planStatus) {
		this.planStatus = planStatus;
	}
	public Long getPlanId() {
		return planId;
	}
	public void setPlanId(Long planId) {
		this.planId = planId;
	}
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
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
}
