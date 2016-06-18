package com.sunline.ccs.service.msdentity;

import java.io.Serializable;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ccs.service.msentity.MsRequestInfo;
import com.sunline.ppy.dictionary.entity.Check;

/**
 * 计算随借随还详情接口申请报文
 * @author zqx
 *
 */
public class STNTLMactLoanCalcReq extends MsRequestInfo implements Serializable {
	public static final long serialVersionUID = 1L;

	
	/*
	*贷款产品代码
	*/
	@Check(lengths=4,notEmpty=true)
	@JsonProperty(value="LOAN_CODE")
	public String  loanCode; 

	/*
	*贷款金额
	*/
	@Check(lengths=15,notEmpty=true)
	@JsonProperty(value="LOAN_AMT")
	public BigDecimal  loanAmt;

	public String getLoanCode() {
		return loanCode;
	}

	public void setLoanCode(String loanCode) {
		this.loanCode = loanCode;
	}

	public BigDecimal getLoanAmt() {
		return loanAmt;
	}

	public void setLoanAmt(BigDecimal loanAmt) {
		this.loanAmt = loanAmt;
	} 

	
}
