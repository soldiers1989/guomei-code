package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.util.ArrayList;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * 贷款产品定价信息查询
* @author fanghj
 * @time 2014-3-24 下午4:27:43
 */
public class S20010Resp implements Serializable {

	private static final long serialVersionUID = 8753387429323554565L;

	/**
     * 贷款产品编号
     */
    public String loan_code;
    
    public ArrayList<S20010LoanFee>loanFees;

	public String getLoan_code() {
		return loan_code;
	}

	public void setLoan_code(String loan_code) {
		this.loan_code = loan_code;
	}

	public ArrayList<S20010LoanFee> getLoanFees() {
		return loanFees;
	}

	public void setLoanFees(ArrayList<S20010LoanFee> loanFees) {
		this.loanFees = loanFees;
	}
    
	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}

