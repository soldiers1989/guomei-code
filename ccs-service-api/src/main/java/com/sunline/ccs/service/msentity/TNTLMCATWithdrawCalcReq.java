package com.sunline.ccs.service.msentity;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ppy.dictionary.entity.Check;


public class TNTLMCATWithdrawCalcReq extends MsRequestInfo  {
	private static final long serialVersionUID = -396726759534247732L;
	/**
	 * 合同号	VARCHAR2(32)	Y
	 */
	@Check(lengths=22,notEmpty=true)
	@JsonProperty(value="CONTR_NBR")
	public String contrNbr;
	/**
	 * 提现金额	DECIMAL(15,2)	Y
	 */
	@Check(lengths=18,notEmpty=true,regular="^(([1-9]\\d*)|0)(\\.(\\d){1,2})?$")
	@JsonProperty(value="AMOUNT")
	private BigDecimal amount;

	public String getContrNbr() {
		return contrNbr;
	}
	public void setContrNbr(String contrNbr) {
		this.contrNbr = contrNbr;
	}
	public BigDecimal getAmount() {
		return amount;
	}
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TNTLMCATWithdrawCalcReq [contrNbr=").append(contrNbr)
				.append(", amount=").append(amount).append("]");
		return builder.toString();
	}
}
