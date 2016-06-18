package com.sunline.ccs.service.msdentity;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ccs.service.msentity.MsResponseInfo;
/**
 * 放款申请返回报文
 * @author zqx
 *
 */
public class STNQPLPAmtRangeResp extends MsResponseInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 * 保单号
	 */
	@JsonProperty(value="LOAN_CODE")
	private String loanCode;

	@JsonProperty(value="PROD_TERM_LIST")
	private List<STNQPLPAmtRangeRespSubTermAmtRange> prodTermList;

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
