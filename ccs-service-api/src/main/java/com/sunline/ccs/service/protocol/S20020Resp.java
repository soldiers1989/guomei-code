package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.util.ArrayList;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class S20020Resp implements Serializable {

	private static final long serialVersionUID = 8039777886028780983L;

	/**
     * 贷款卡号
     */
    public String card_no;

    public ArrayList<S20020Loan> loans;

	public String getCard_no() {
		return card_no;
	}

	public void setCard_no(String card_no) {
		this.card_no = card_no;
	}

	public ArrayList<S20020Loan> getLoans() {
		return loans;
	}

	public void setLoans(ArrayList<S20020Loan> loans) {
		this.loans = loans;
	}
    
	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}

