package com.sunline.ccs.batch.rpt.cca200;

import java.util.Date;
import java.util.List;

import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.CcsRepaySchedule;
import com.sunline.ccs.infrastructure.shared.model.CcsSettleClaim;

public class PA202Info {
	
	private Boolean isClaimSettled;
	private CcsLoan loan;
	private CcsSettleClaim claim;
	private String customerName;
	private String idNo;
	private Date batchDate;
	
	/* 未结清贷款应设置以下属性 */
	private Date currTermStartDate;
	private List<CcsPlan> plans;
	private CcsRepaySchedule schedule;
	
	public Date getCurrTermStartDate() {
		return currTermStartDate;
	}
	public void setCurrTermStartDate(Date currTermStartDate) {
		this.currTermStartDate = currTermStartDate;
	}
	public CcsRepaySchedule getSchedule() {
		return schedule;
	}
	public void setSchedule(CcsRepaySchedule schedule) {
		this.schedule = schedule;
	}
	public Date getBatchDate() {
		return batchDate;
	}
	public void setBatchDate(Date batchDate) {
		this.batchDate = batchDate;
	}
	public String getCustomerName() {
		return customerName;
	}
	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	public String getIdNo() {
		return idNo;
	}
	public void setIdNo(String idNo) {
		this.idNo = idNo;
	}
	public Boolean getIsClaimSettled() {
		return isClaimSettled;
	}
	public void setIsClaimSettled(Boolean isClaimSettled) {
		this.isClaimSettled = isClaimSettled;
	}
	public CcsLoan getLoan() {
		return loan;
	}
	public void setLoan(CcsLoan loan) {
		this.loan = loan;
	}
	public CcsSettleClaim getClaim() {
		return claim;
	}
	public void setClaim(CcsSettleClaim claim) {
		this.claim = claim;
	}
	public List<CcsPlan> getPlans() {
		return plans;
	}
	public void setPlans(List<CcsPlan> plans) {
		this.plans = plans;
	}


}
