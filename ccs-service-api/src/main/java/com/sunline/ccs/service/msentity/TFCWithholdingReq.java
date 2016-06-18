package com.sunline.ccs.service.msentity;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ppy.dictionary.entity.Check;

@SuppressWarnings("serial")
public class TFCWithholdingReq extends MsRequestInfo {
	
	/**
	 * 合同号
	 */
	@Check(lengths=22,notEmpty=true)
	@JsonProperty(value="contractId")
	public String contractNo;
	/**
	 * 还款金额
	 */
	@Check(lengths=18,notEmpty=true,regular="^(([1-9]\\d*)|0)(\\.(\\d){1,2})?$")
	@JsonProperty(value="money")
	public BigDecimal amount;
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
}
