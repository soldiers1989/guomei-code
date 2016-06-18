package com.sunline.ccs.service.msentity;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ppy.dictionary.entity.Check;

public class TFDTrustLoanWithDrawNoticeReq extends MsRequestInfo {
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
	 * 放款金额（贷款金额）
	 */
	@Check(lengths=18,notEmpty=true,regular="^(([1-9]\\d*)|0)(\\.(\\d){1,2})?$")
	@JsonProperty(value="AMOUNT")
	public BigDecimal amount;
	
	/**
	 * 分期期数
	 */
	@Check(lengths=2,notEmpty=true,isNumber=true)
	@JsonProperty(value="TERM")
	public Integer term;
	
	/**
	 * 应还分期服务费
	 */
	@Check(lengths=18,notEmpty=true,regular="^(([1-9]\\d*)|0)(\\.(\\d){1,2})?$")
	@JsonProperty(value="FEE_AMOUNT")
	public BigDecimal feeAmount;
	
	/**
	 * 贷款来源
	 */
	@Check(lengths=32,notEmpty=false)
	@JsonProperty(value="LOAN_SOURCE")
	public String loanSource;
	
	/**
	 * 商户id
	 */
	@Check(lengths=32,notEmpty=false)
	@JsonProperty(value="MER_ID")
	public String merId;
	
	/**
	 * 终端设备号
	 */
	@Check(lengths=32)
	@JsonProperty(value="AUTH_TXN_TERMINAL")
	public String authTxnTerminal;
	
	/**
	 * 订单号
	 */
	@Check(lengths=32,notEmpty=true)
	@JsonProperty(value="MERCHANDISE_ORDER")
	public String merchandiseOrder;
	
	/**
	 * 商品总金额
	 */
	@Check(lengths=32,notEmpty=true,regular="^(([1-9]\\d*)|0)(\\.(\\d){1,2})?$")
	@JsonProperty(value="MERCHANDISE_AMT")
	public BigDecimal merchandiseAmt;
	
	/**
	 * 首付金额
	 * 
	 */
	@Check(lengths=12,notEmpty=false,regular="^(([1-9]\\d*)|0)(\\.(\\d){1,8})?$")
	@JsonProperty(value="DOWN_PAYMENT_AMT")
	public BigDecimal downPaymentAmt;
	
	/**
	 * 销售人员编号
	 */
	@Check(lengths=32,notEmpty=false)
	@JsonProperty(value="RA_ID")
	public String raId;
	
	/**
	 * 手机号
	 */
	@Check(lengths=11,isNumber=true)
	@JsonProperty(value="MOBILE")
	public String mobile;

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

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public Integer getTerm() {
		return term;
	}

	public void setTerm(Integer term) {
		this.term = term;
	}

	public BigDecimal getFeeAmount() {
		return feeAmount;
	}

	public void setFeeAmount(BigDecimal feeAmount) {
		this.feeAmount = feeAmount;
	}

	public String getLoanSource() {
		return loanSource;
	}

	public void setLoanSource(String loanSource) {
		this.loanSource = loanSource;
	}

	public String getMerId() {
		return merId;
	}

	public void setMerId(String merId) {
		this.merId = merId;
	}

	public String getAuthTxnTerminal() {
		return authTxnTerminal;
	}

	public void setAuthTxnTerminal(String authTxnTerminal) {
		this.authTxnTerminal = authTxnTerminal;
	}

	public String getMerchandiseOrder() {
		return merchandiseOrder;
	}

	public void setMerchandiseOrder(String merchandiseOrder) {
		this.merchandiseOrder = merchandiseOrder;
	}

	public BigDecimal getMerchandiseAmt() {
		return merchandiseAmt;
	}

	public void setMerchandiseAmt(BigDecimal merchandiseAmt) {
		this.merchandiseAmt = merchandiseAmt;
	}

	public BigDecimal getDownPaymentAmt() {
		return downPaymentAmt;
	}

	public void setDownPaymentAmt(BigDecimal downPaymentAmt) {
		this.downPaymentAmt = downPaymentAmt;
	}

	public String getRaId() {
		return raId;
	}

	public void setRaId(String raId) {
		this.raId = raId;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	
}
