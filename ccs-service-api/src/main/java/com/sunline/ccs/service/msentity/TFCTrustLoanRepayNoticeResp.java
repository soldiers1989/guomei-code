package com.sunline.ccs.service.msentity;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ppy.dictionary.entity.Check;

/**
 * 还款结果通知接口（拿去花）
 * @author qinshanbin
 *
 */
//@SuppressWarnings("serial")
public class TFCTrustLoanRepayNoticeResp extends MsResponseInfo {
	private static final long serialVersionUID = 1L;
	/**
	 * 合同号
	 */
	@Check(lengths=22,notEmpty=true)
	@JsonProperty(value="CONTR_NBR")
	public String contractNo;
//	/**
//	 * 还款金额
//	 */
//	@Check(lengths=18,notEmpty=true)
//	@JsonProperty(value="AMOUNT")
//	public BigDecimal amount;
	public String getContractNo() {
		return contractNo;
	}
	public void setContractNo(String contractNo) {
		this.contractNo = contractNo;
	}
//	public BigDecimal getAmount() {
//		return amount;
//	}
//	public void setAmount(BigDecimal amount) {
//		this.amount = amount;
//	}
	
}
