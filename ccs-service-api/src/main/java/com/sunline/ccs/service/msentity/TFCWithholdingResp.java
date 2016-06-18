package com.sunline.ccs.service.msentity;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ppy.dictionary.entity.Check;

@SuppressWarnings("serial")
public class TFCWithholdingResp extends MsResponseInfo{
	
	/**
	 * 合同号
	 */
	@Check(lengths=22,notEmpty=true)
	@JsonProperty(value="CONTR_NBR")
	public String contractNo;
	/**
	 * 还款金额
	 */
	@Check(lengths=18,notEmpty=true)
	@JsonProperty(value="AMOUNT")
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
