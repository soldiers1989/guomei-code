package com.sunline.ccs.batch.rpt.cca000.items;

import java.math.BigDecimal;
import java.util.Date;

import com.sunline.ark.support.cstruct.CChar;

/**
 * YGInsuredAmtRpyInfo
 * @author wanghl
 *
 */
public class YGInsuredAmtRpyInfoItem {

	/**
	 * 记录号
 	 */
	@CChar( value = 32, order = 100 )
	public Long seqNo;

	/**
	 * 借据编号
 	 */
	@CChar( value = 32, order = 200 )
	public String putOutNo;

	/**
	 * 还款期数
 	 */
	@CChar( value = 12, order = 300 )
	public Integer loanTerm;

	/**
	 * 还款交易类型
 	 */
	@CChar( value = 3, order = 400 )
	public String payMentKind;

	/**
	 * 应还款日期
 	 */
	@CChar( value = 8, datePattern="yyyyMMdd", order = 500 )
	public Date payDate;

	/**
	 * 实还款日期
 	 */
	@CChar( value = 8, datePattern="yyyyMMdd", order = 600 )
	public Date actualPayDate;

	/**
	 * 实缴保费金额
 	 */
	@CChar( value = 15, pointSupported=true, order = 700 )
	public BigDecimal actualPayCorp;

	/**
	 * 应缴未缴保费金额
 	 */
	@CChar( value = 15, pointSupported=true,  order = 800 )
	public BigDecimal payCorp;

	/**
	 * 录入日期
 	 */
	@CChar( value = 8,datePattern="yyyyMMdd", order = 900 )
	public Date inputDate;
}
