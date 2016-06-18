package com.sunline.ccs.service.msdentity;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ccs.service.msentity.MsRequestInfo;
import com.sunline.ppy.dictionary.entity.Check;
/**
 * 所有产品获取请求报文
 * @author zhengjf
 *
 */
public class STNQPLPAllLoanPlanReq  extends MsRequestInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 产品ID
	 */
	@Check(lengths=4)
	@JsonProperty(value="LOAN_CODE")
	public String  loanCode;

	public String getLoanCode() {
		return loanCode;
	}

	public void setLoanCode(String loanCode) {
		this.loanCode = loanCode;
	}
}
