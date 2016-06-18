package com.sunline.ccs.batch.rpt.cca230.items;

import java.math.BigDecimal;
import java.util.Date;

import com.sunline.ark.support.cstruct.CChar;

public class YZFTransactionFlowFileItem {
	/**
	 * 马上合作方ID
	 */
	@CChar ( value = 100, order = 1)
	public String acqId;
	/**
	 * 翼支付推送的接口CallID
	 */
	@CChar ( value = 200, order = 2 )
	public String servicesn;
	/**
	 * 交易授权号
	 */
	@CChar ( value = 100, order = 3)
	public String orderId;
	/**
	 * 马上客户号码
	 */
	@CChar ( value = 200, order = 4 )
	public String internalCustomerId;
	/**
	 * 申请日期
	 */
	@CChar ( value = 8, datePattern = "yyyyMMdd", order = 5 )
	public Date orderTime;
	/**
	 * 状态
	 */
	@CChar ( value = 100, order = 6 )
	public String orderStatus;
	
	/**
	 * 马上账务合同号
	 */
	@CChar ( value = 100, order = 7 )
	public String contrNbr;
	/**
	 * 金额
	 */
	@CChar ( value = 15, order = 8 )
	public BigDecimal txnAmt;
	/**
	 * 交易方向(提现/还款)
	 */
	@CChar( value = 100, order = 9 )
	public String loanUsage;
	/**
	 * 失败原因
	 */
	@CChar( value = 100, order = 10 )
	public String message;
}
