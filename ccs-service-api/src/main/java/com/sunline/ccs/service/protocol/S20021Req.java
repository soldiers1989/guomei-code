package com.sunline.ccs.service.protocol;

import java.io.Serializable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * 据贷款借据号查询贷款信息
* @author fanghj
 * @time 2014-4-1 下午8:11:57
 */
public class S20021Req implements Serializable {

	private static final long serialVersionUID = 449628064509533143L;
	/**
     * 借据号
     */
    public String loan_receipt_nbr;

	public String getLoan_receipt_nbr() {
		return loan_receipt_nbr;
	}

	public void setLoan_receipt_nbr(String loan_receipt_nbr) {
		this.loan_receipt_nbr = loan_receipt_nbr;
	}

	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}

