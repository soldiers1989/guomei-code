package com.sunline.ccs.service.msentity;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ppy.dictionary.entity.Check;

public class TFCTrustLoanRefundNoticeReq extends MsRequestInfo {
	private static final long serialVersionUID = 1L;
	
	/**
	 * 合同号
	 */
	@Check(lengths=32,notEmpty=true)
	@JsonProperty(value="CONTR_NBR")
	public String contractNo;
	
	/**
	 * 贷款流水号
	 */
	@Check(lengths=64,notEmpty=true)
	@JsonProperty(value="LOAN_NO")
	public String loanNo;
	
	/**
	 * 退款流水号
	 */
	@Check(lengths=64,notEmpty=true)
	@JsonProperty(value="REFUND_NO")
	public String refundNo;
	
	/**
	 * 退款金额
	 */
	@Check(lengths=18,notEmpty=true,regular="^(([1-9]\\d*)|0)(\\.(\\d){1,2})?$")
	@JsonProperty(value="REFUND_AMOUNT")
	public BigDecimal refundAmt;
	
	/**
	 * 已退本金
	 */
	@Check(lengths=18,notEmpty=true,regular="^(([1-9]\\d*)|0)(\\.(\\d){1,2})?$")
	@JsonProperty(value="PAID_PRINCIPAL")
	public BigDecimal paidPrincipal;
	
	/**
	 * 已退分期服务费
	 */
	@Check(lengths=18,notEmpty=true,regular="^(([1-9]\\d*)|0)(\\.(\\d){1,2})?$")
	@JsonProperty(value="PAID_FEE_AMOUNT")
	public BigDecimal paidFeeAmt;
	
	/**
	 * 已退罚金
	 */
	@Check(lengths=18,notEmpty=true,regular="^(([1-9]\\d*)|0)(\\.(\\d){1,2})?$")
	@JsonProperty(value="PAID_PENALTY")
	public BigDecimal paidPenalty;
	
	/**
	 * 原路返回金额
	 */
	@Check(lengths=18,notEmpty=true,regular="^(([1-9]\\d*)|0)(\\.(\\d){1,2})?$")
	@JsonProperty(value="RETURN_AMOUNT")
	public BigDecimal returnAmt;

	public String getContractNo() {
		return contractNo;
	}

	public void setContractNo(String contractNo) {
		this.contractNo = contractNo;
	}

	public String getLoanNo() {
		return loanNo;
	}

	public void setLoanNo(String loanNo) {
		this.loanNo = loanNo;
	}

	public String getRefundNo() {
		return refundNo;
	}

	public void setRefundNo(String refundNo) {
		this.refundNo = refundNo;
	}

	public BigDecimal getRefundAmt() {
		return refundAmt;
	}

	public void setRefundAmt(BigDecimal refundAmt) {
		this.refundAmt = refundAmt;
	}

	public BigDecimal getPaidPrincipal() {
		return paidPrincipal;
	}

	public void setPaidPrincipal(BigDecimal paidPrincipal) {
		this.paidPrincipal = paidPrincipal;
	}

	public BigDecimal getPaidFeeAmt() {
		return paidFeeAmt;
	}

	public void setPaidFeeAmt(BigDecimal paidFeeAmt) {
		this.paidFeeAmt = paidFeeAmt;
	}

	public BigDecimal getPaidPenalty() {
		return paidPenalty;
	}

	public void setPaidPenalty(BigDecimal paidPenalty) {
		this.paidPenalty = paidPenalty;
	}

	public BigDecimal getReturnAmt() {
		return returnAmt;
	}

	public void setReturnAmt(BigDecimal returnAmt) {
		this.returnAmt = returnAmt;
	}
	
}
