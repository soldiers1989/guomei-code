package com.sunline.ccs.batch.rpt.cca240.item;

import java.math.BigDecimal;
import java.util.Date;

import com.sunline.ark.support.cstruct.CChar;
import com.sunline.ccs.param.def.enums.OffsetType;

/**
 * 优惠券入账报表
 * @author wanghl
 *
 */
public class CouponRepayRptItem {
	/**
	 * 合同号	CONTR_NBR
	 */
	@CChar ( value = 32, order = 100)
	public String contrNbr;
	/**
	 * 优惠券面值金额	AMOUNT
	 */
	@CChar ( value = 15, pointSupported = true, order = 200)
	public BigDecimal amount;
	/**
	 * 优惠券编码	OFFSET_NO
	 */
	@CChar ( value = 32, order = 300)
	public String offsetNo;
	/**
	 * 优惠券使用期数	OFFSET_TERM
	 */
	@CChar ( value = 32, order = 400)
	public Integer offsetTerm;
	/**
	 * 优惠券类型	OFFSET_TYPE
	 */
	@CChar ( value = 32, order = 500)
	public OffsetType offsetType;
	/**
	 * 优惠券实际入账金额	POST_AMT
	 */
	@CChar ( value = 15, pointSupported = true, order = 600)
	public BigDecimal postAmt;
	
	/**
	 * 优惠券还款日期
	 */
	@CChar( value = 8, datePattern="yyyyMMdd", order = 700 )
	public Date txnDate;
	
	/**
	 * 优惠券入账日期
	 */
	@CChar( value = 8, datePattern="yyyyMMdd", order = 800 )
	public Date postDate;

}
