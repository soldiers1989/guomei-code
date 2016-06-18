package com.sunline.ccs.batch.rpt.cca000;

import java.util.Date;
import java.util.List;

import com.sunline.ccs.infrastructure.shared.model.CcsOrder;
import com.sunline.ccs.infrastructure.shared.model.CcsOrderHst;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.CcsRepayHst;
import com.sunline.ccs.param.def.enums.LoanTerminateReason;

public class SA003YGRpyDetailInfo {
	private Boolean isRecovery = false;
	/* 代位追偿 */
	private CcsOrderHst orderHst;
	private CcsOrder order;
	/* 账户还款 */
	private List<CcsRepayHst> repays;
	private CcsPlan plan;
	private String ddBankAcctNo;
	private Date termPmtDueDate;
	private Date batchDate;
	private String dueBillNo;
	private Integer currTerm;
	private List<Long> orderIds;
	private Boolean isLoanTerminated = false;
	private LoanTerminateReason terminalReason;
	
	
	public CcsOrder getOrder() {
		return order;
	}
	public void setOrder(CcsOrder order) {
		this.order = order;
	}
	public LoanTerminateReason getTerminalReason() {
		return terminalReason;
	}
	public void setTerminalReason(LoanTerminateReason terminalReason) {
		this.terminalReason = terminalReason;
	}
	public Boolean getIsLoanTerminated() {
		return isLoanTerminated;
	}
	public void setIsLoanTerminated(Boolean isLoanTerminated) {
		this.isLoanTerminated = isLoanTerminated;
	}
	public Boolean getIsRecovery() {
		return isRecovery;
	}
	public void setIsRecovery(Boolean isRecovery) {
		this.isRecovery = isRecovery;
	}
	public CcsOrderHst getOrderHst() {
		return orderHst;
	}
	public void setOrderHst(CcsOrderHst orderHst) {
		this.orderHst = orderHst;
	}
	public List<Long> getOrderIds() {
		return orderIds;
	}
	public void setOrderIds(List<Long> orderIds) {
		this.orderIds = orderIds;
	}
	public String getDueBillNo() {
		return dueBillNo;
	}
	public void setDueBillNo(String dueBillNo) {
		this.dueBillNo = dueBillNo;
	}
	public Integer getCurrTerm() {
		return currTerm;
	}
	public void setCurrTerm(Integer currTerm) {
		this.currTerm = currTerm;
	}
	public Date getBatchDate() {
		return batchDate;
	}
	public void setBatchDate(Date batchDate) {
		this.batchDate = batchDate;
	}
	public Date getTermPmtDueDate() {
		return termPmtDueDate;
	}
	public void setTermPmtDueDate(Date termPmtDueDate) {
		this.termPmtDueDate = termPmtDueDate;
	}
	public String getDdBankAcctNo() {
		return ddBankAcctNo;
	}
	public void setDdBankAcctNo(String ddBankAcctNo) {
		this.ddBankAcctNo = ddBankAcctNo;
	}
	public List<CcsRepayHst> getRepays() {
		return repays;
	}
	public void setRepays(List<CcsRepayHst> repays) {
		this.repays = repays;
	}
	public CcsPlan getPlan() {
		return plan;
	}
	public void setPlan(CcsPlan plan) {
		this.plan = plan;
	}
}
