package com.sunline.ccs.service.msentity;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ppy.dictionary.entity.Check;

/**
 * 查询产品需上传电子资料清单请求报文
 * @author yuemk
 *
 */
@SuppressWarnings("serial")
public class TNQPLoanPlanAPPLeDataReq extends MsRequestInfo {
	/**
	 * 贷款产品代码
	 */
	@Check(lengths=4,notEmpty=true)
	@JsonProperty(value="LOAN_CODE")
	public String loanCode;
	/**
	 * 贷款金额
	 */
	@Check(lengths=18,notEmpty=true)
	@JsonProperty(value="AMOUNT")
	public BigDecimal loanAmt;
	/**
	 * 贷款期数
	 */
	@Check(lengths=2,notEmpty=true,isNumber=true)
	@JsonProperty(value="LOAN_TERMS")
	public String loanTerm;
	
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
	public String getLoanTerm() {
		return loanTerm;
	}
	public void setLoanTerm(String loanTerm) {
		this.loanTerm = loanTerm;
	}
}
