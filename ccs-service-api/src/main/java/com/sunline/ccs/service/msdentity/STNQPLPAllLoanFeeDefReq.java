package com.sunline.ccs.service.msdentity;

import java.io.Serializable;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ccs.service.msentity.MsRequestInfo;
import com.sunline.ppy.dictionary.entity.Check;

public class STNQPLPAllLoanFeeDefReq extends MsRequestInfo implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
	 * 产品ID
	 */
	@Check(lengths=4)
	@JsonProperty(value="LOAN_CODE")
	public String  loanCode;
	
	/**
	 * 贷款金额
	 */
	@JsonProperty(value="LOAN_AMT")
	public BigDecimal  loanAmt;
	/**
	 * 合作机构编号
	 */
	@Check(lengths=8,notEmpty=false)
	@JsonProperty(value="ACQ_ID")
	public String  idAcq;

	public String getIdAcq() {
		return idAcq;
	}

	public void setIdAcq(String idAcq) {
		this.idAcq = idAcq;
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
	
}
