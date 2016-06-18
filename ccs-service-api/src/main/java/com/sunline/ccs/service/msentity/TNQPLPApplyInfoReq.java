package com.sunline.ccs.service.msentity;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ppy.dictionary.entity.Check;

/**
 * 产品申请参数查询请求报文(审批系统查询调用)
 * @author yuemk
 *
 */
@SuppressWarnings("serial")
public class TNQPLPApplyInfoReq extends MsRequestInfo {
	/**
	 * 贷款产品代码
	 */
	@Check(lengths=4,notEmpty=true)
	@JsonProperty(value="LOAN_CODE")
	public String loanCode;
	/**
	 * 贷款金额
	 */
	@Check(lengths=18)
	@JsonProperty(value="LOAN_AMT")
	public BigDecimal loanAmt;
	/**
	 * 贷款期数
	 */
	@Check(lengths=2,notEmpty=false,isNumber=true)
	@JsonProperty(value="LOAN_TERM")
	public String loanTerm;

	/**
	 * 贷款子产品代码
	 * @return
	 */
	@Check(lengths=8)
	@JsonProperty(value="LOAN_FEE_DEF_ID")
	public Integer loanFeeDefId;
	
	
	public Integer getLoanFeeDefId() {
		return loanFeeDefId;
	}
	public void setLoanFeeDefId(Integer loanFeeDefId) {
		this.loanFeeDefId = loanFeeDefId;
	}
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
