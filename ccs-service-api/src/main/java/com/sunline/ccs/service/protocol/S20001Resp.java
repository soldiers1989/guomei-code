package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.util.ArrayList;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * 贷款产品信息查询
* @author fanghj
 * @time 2014-3-24 下午4:26:58
 */
public class S20001Resp implements Serializable {

	private static final long serialVersionUID = 6367835799004438208L;

	/**
     * 贷款卡号
     */
    public String card_no;
    
    public ArrayList<S20001Loan> loans;

	public String getCard_no() {
		return card_no;
	}

	public void setCard_no(String card_no) {
		this.card_no = card_no;
	}

	public ArrayList<S20001Loan> getLoans() {
		return loans;
	}

	public void setLoans(ArrayList<S20001Loan> loans) {
		this.loans = loans;
	}
    
	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}

}

