package com.sunline.ccs.service.msentity;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ppy.dictionary.entity.Check;
/**
 * 商品贷提现
 * @author zhengjf
 *
 */
public class TFDCommodyLoanWithDrawReq extends MsRequestInfo  {
	private static final long serialVersionUID = 1L;

	/**
	 * 合同号
	 */
	@Check(lengths=22,notEmpty=true)
	@JsonProperty(value="CONTR_NBR")
	public String contractNo;

	/**
	 * 放款金额（贷款金额）
	 */
	@JsonProperty(value="AMOUNT")
	public BigDecimal amount;
	
	/**
	 * 商户id
	 */
	@Check(lengths=32,notEmpty=true)
	@JsonProperty(value="MER_ID")
	public String merId;
	
	/**
	 * 终端设备号
	 */
	@Check(lengths=32)
	@JsonProperty(value="AUTH_TXN_TERMINAL")
	public String authTxnTerminal;
	
	/**
	 * 商品总金额
	 */
	@JsonProperty(value="MERCHANDISE_AMT")
	public BigDecimal merchandiseAmt;
	
	/**
	 * 销售人员编号
	 */
	@Check(lengths=32)
	@JsonProperty(value="RA_ID")
	public String raId;
	
	/**
	 * 手机号
	 */
	@Check(lengths=11,notEmpty=true,fixed=true,isNumber=true)
	@JsonProperty(value="MOBILE")
	public String mobile;
	
	/**
	 * 商品贷款订单号
	 */
	@Check(lengths=32,notEmpty=true)
	@JsonProperty(value="MERCHANDISE_ORDER")
	public String merchandiseOrder;

	/**
	 * 首付金额
	 * 
	 */
	@Check(lengths=12,notEmpty=false,regular="^(([1-9]\\d*)|0)(\\.(\\d){1,8})?$")
	@JsonProperty(value="DOWN_PAYMENT_AMT")
	public BigDecimal downPaymentAmt;
	
	public String getContractNo() {
		return contractNo;
	}

	public void setContractNo(String contractNo) {
		this.contractNo = contractNo;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
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

	public BigDecimal getMerchandiseAmt() {
		return merchandiseAmt;
	}

	public void setMerchandiseAmt(BigDecimal merchandiseAmt) {
		this.merchandiseAmt = merchandiseAmt;
	}

	public String getRaId() {
		return raId;
	}

	public void setRaId(String raId) {
		this.raId = raId;
	}

	public String getMerchandiseOrder() {
		return merchandiseOrder;
	}

	public void setMerchandiseOrder(String merchandiseOrder) {
		this.merchandiseOrder = merchandiseOrder;
	}

	public BigDecimal getDownPaymentAmt() {
		return downPaymentAmt;
	}

	public void setDownPaymentAmt(BigDecimal downPaymentAmt) {
		this.downPaymentAmt = downPaymentAmt;
	}

}
