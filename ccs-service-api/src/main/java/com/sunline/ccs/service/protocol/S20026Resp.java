package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.util.ArrayList;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * 当日贷款变更申请
* @author fanghj
 * @time 2014-3-31 下午11:38:25
 */
public class S20026Resp implements Serializable {

	private static final long serialVersionUID = 4418618255389833544L;

	/**
     * 卡号
     */
    public String card_no;

    /**
     * 借据号
     */
    public String loan_receipt_nbr;

    public ArrayList<S20026LoanReg> loanRegs;
    
	public String getCard_no() {
		return card_no;
	}

	public void setCard_no(String card_no) {
		this.card_no = card_no;
	}

	public String getLoan_receipt_nbr() {
		return loan_receipt_nbr;
	}

	public void setLoan_receipt_nbr(String loan_receipt_nbr) {
		this.loan_receipt_nbr = loan_receipt_nbr;
	}

	public ArrayList<S20026LoanReg> getLoanRegs() {
		return loanRegs;
	}

	public void setLoanRegs(ArrayList<S20026LoanReg> loanRegs) {
		this.loanRegs = loanRegs;
	}

	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}

