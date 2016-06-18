package com.sunline.ccs.batch.front;

import java.math.BigDecimal;

import com.sunline.ark.support.cstruct.CChar;

public class SPtpBatchCutFile {
	/**
	 * 合同编号
	 */
	@CChar(value=20,order=100)
	public String contractCode;
	
	/**
	 * 客户编号
	 */
	@CChar(value=32,order=200)
	public String customerCode;
	
	/**
	 * 贷款编号
	 */
	@CChar(value=32,order=300)
	public String loanInfoCode;
	
	/**
	 * 扣款金额
	 */
	@CChar(value=15,order=400)
	public BigDecimal paybackAmount;
}
