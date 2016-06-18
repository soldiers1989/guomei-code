package com.sunline.ccs.batch.rpt.cca000.items;

import java.math.BigDecimal;
import java.util.Date;

import com.sunline.ark.support.cstruct.CChar;

/**
 * YGClaimInfo
 * @author wanghl
 *
 */
public class YGClaimInfoItem {

	/**
	 * 借据编号
 	 */
	@CChar( value = 32, order = 100 )
	public String putOutNo;

	/**
	 * 逾期总天数
 	 */
	@CChar( value = 12, order = 200 )
	public Integer overDueDays;

	/**
	 * 欠金
 	 */
	@CChar( value = 15, pointSupported=true, order = 300 )
	public BigDecimal overDueBalance;

	/**
	 * 欠利息
 	 */
	@CChar( value = 15, pointSupported=true, order = 400 )
	public BigDecimal overDueInte;

	/**
	 * 罚金
 	 */
	@CChar( value = 15, pointSupported=true, order = 500 )
	public BigDecimal overDueInfine;

	/**
	 * 未记利息
 	 */
	@CChar( value = 15, pointSupported=true, order = 600 )
	public BigDecimal nint;

	/**
	 * 当前余额
 	 */
	@CChar( value = 15, pointSupported=true, order = 700 )
	public BigDecimal balance;

	/**
	 * 理陪金额
 	 */
	@CChar( value = 15, pointSupported=true, order = 800 )
	public BigDecimal lpamt;

	/**
	 * 理赔文本信息
 	 */
	@CChar( value = 1200, order = 900 )
	public String lptxt;

	/**
	 * 预计理赔日期
 	 */
	@CChar( value = 8,datePattern="yyyyMMdd", order = 1000 )
	public Date yjlprq;
}
