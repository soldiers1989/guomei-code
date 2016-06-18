package com.sunline.ccs.service.msdentity;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ccs.param.def.enums.PaymentIntervalUnit;
import com.sunline.ccs.service.entity.SunshineResponseInfo;

/**
 * 放款申请返回报文
 * @author jjb
 *
 */
public class testSTNQPLPAmtRangeResp extends SunshineResponseInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 * 保单号
	 */
	@JsonProperty(value="LOAN_CODE")
	private String loanCode;

	public PaymentIntervalUnit getPaymentUnit() {
		return paymentUnit;
	}

	public void setPaymentUnit(PaymentIntervalUnit paymentUnit) {
		this.paymentUnit = paymentUnit;
	}

	@JsonProperty(value="PROD_TERM_LIST")
	private List<STNQPLPAmtRangeRespSubTermAmtRange> prodTermList;

	/*
	*还款间隔单位
	*/
	@JsonProperty(value="PAYMENT_UNIT")
	public PaymentIntervalUnit  paymentUnit; 


	public String getLoanCode() {
		return loanCode;
	}

	public void setLoanCode(String loanCode) {
		this.loanCode = loanCode;
	}

	public List<STNQPLPAmtRangeRespSubTermAmtRange> getProdTermList() {
		return prodTermList;
	}

	public void setProdTermList(
			List<STNQPLPAmtRangeRespSubTermAmtRange> prodTermList) {
		this.prodTermList = prodTermList;
	}



}
