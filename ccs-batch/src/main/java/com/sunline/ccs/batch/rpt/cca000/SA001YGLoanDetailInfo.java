package com.sunline.ccs.batch.rpt.cca000;

import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanRegHst;

public class SA001YGLoanDetailInfo {
	
	private Boolean isLoanEstablished;
	private CcsLoanRegHst loanRegHst;
	private CcsLoanReg loanReg;
	private CcsLoan loan;
	private String ddBankAcctNbr;
	private CcsCustomer customer;
	
	public Boolean getIsLoanEstablished() {
		return isLoanEstablished;
	}
	public void setIsLoanEstablished(Boolean isLoanEstablished) {
		this.isLoanEstablished = isLoanEstablished;
	}
	public CcsLoanRegHst getLoanRegHst() {
		return loanRegHst;
	}
	public CcsLoanReg getLoanReg() {
		return loanReg;
	}
	public void setLoanReg(CcsLoanReg loanReg) {
		this.loanReg = loanReg;
	}
	public void setLoanRegHst(CcsLoanRegHst loanRegHst) {
		this.loanRegHst = loanRegHst;
	}
	public void setLoanReg(CcsLoanRegHst loanRegHst) {
		this.loanRegHst = loanRegHst;
	}
	public CcsLoan getLoan() {
		return loan;
	}
	public void setLoan(CcsLoan loan) {
		this.loan = loan;
	}
	public String getDdBankAcctNbr() {
		return ddBankAcctNbr;
	}
	public void setDdBankAcctNbr(String ddBankAcctNbr) {
		this.ddBankAcctNbr = ddBankAcctNbr;
	}
	public CcsCustomer getCustomer() {
		return customer;
	}
	public void setCustomer(CcsCustomer customer) {
		this.customer = customer;
	}

}
