package com.sunline.ccs.service.msdentity;

import java.io.Serializable;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ccs.service.msentity.MsRequestInfo;
import com.sunline.ppy.dictionary.entity.Check;

/**
 * 根据金额、期数获取产品信息接口申请报文
 * @author zqx
 *
 */
public class STNQPLSPbyAmtTermReq extends MsRequestInfo implements Serializable {
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
	@Check(lengths=15,notEmpty=false)
	@JsonProperty(value="LOAN_AMT")
	public BigDecimal  loanAmt; 

	/*
	*贷款期数
	*/
	@Check(lengths=2,notEmpty=false)
	@JsonProperty(value="LOAN_TERM")
	public Integer  loanTerm;

	/**
	 * 贷款子产品代码
	 * @return
	 */
	@Check(lengths=8,notEmpty=false)
	@JsonProperty(value="LOAN_FEE_DEF_ID")
	public Integer  loanFeeDefId;
	
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

	public Integer getLoanTerm() {
		return loanTerm;
	}

	public void setLoanTerm(Integer loanTerm) {
		this.loanTerm = loanTerm;
	} 
	
}
