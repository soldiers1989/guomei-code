package com.sunline.ccs.service.protocol;

import java.io.Serializable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * 根据借据号查询还款计划表
* @author fanghj
 * @time 2014-3-31 下午11:18:55
 */
public class S20023Req implements Serializable {

	private static final long serialVersionUID = 7963134473947927175L;

	/**
     * 卡号
     */
    public String card_no;

    /**
     * 贷款借据号
     */
    public String loan_receipt_nbr;

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

	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}

