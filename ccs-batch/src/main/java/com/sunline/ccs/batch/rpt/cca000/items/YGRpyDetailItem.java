package com.sunline.ccs.batch.rpt.cca000.items;

import java.math.BigDecimal;
import java.util.Date;

import com.sunline.ark.support.cstruct.CChar;

/**
 * YGRpyDetail
 * @author wanghl
 *
 */
public class YGRpyDetailItem {

	/**
	 * 交易流水号
 	 */
	@CChar( value = 30, order = 100 )
	public Long seqNo;

	/**
	 * 还款日期
 	 */
	@CChar( value = 8,datePattern="yyyyMMdd", order = 200 )
	public Date repayDate;

	/**
	 * 借据编号
 	 */
	@CChar( value = 32, order = 300 )
	public String putOutNo;

	/**
	 * 还款期数
 	 */
	@CChar( value = 12, order = 400 )
	public Integer loanTerm;

	/**
	 * 还款交易类型
 	 */
	@CChar( value = 1, order = 500 )
	public String paymentKind  ;

	/**
	 * 还款账号
 	 */
	@CChar( value = 32, order = 600 )
	public String paymentAcctNo ;

	/**
	 * 本金
 	 */
	@CChar( value = 15,pointSupported=true, order = 700 )
	public BigDecimal balance;

	/**
	 * 利息
 	 */
	@CChar( value = 15, pointSupported=true, order = 800 )
	public BigDecimal inte;

	/**
	 * 罚金
 	 */
	@CChar( value = 15, pointSupported=true, order = 900 )
	public BigDecimal infine;
}
