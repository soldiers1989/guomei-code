package com.sunline.ccs.batch.rpt.cca000.items;

import java.math.BigDecimal;
import java.util.Date;

import com.sunline.ark.support.cstruct.CChar;

/**
 * 保费期供信息
 * @author wanghl
 *
 */
public class YGInsuredAmtStatusItem {

	/**
	 * 借据编号
 	 */
	@CChar( value = 32, order = 100 )
	public String putOutNo;

	/**
	 * 本期期数
 	 */
	@CChar( value = 12, order = 200 )
	public Integer loanTerm;

	/**
	 * 应缴日期
 	 */
	@CChar( value = 8, datePattern="yyyyMMdd", order = 300 )
	public Date endDate;

	/**
	 * 应缴未缴保费金额
 	 */
	@CChar( value = 15,pointSupported=true, order = 400 )
	public BigDecimal payCorp;

	/**
	 * 正常应缴保费金额
 	 */
	@CChar( value = 15,pointSupported=true, order = 500 )
	public BigDecimal normalCorp;

	/**
	 * 缴纳状态
 	 */
	@CChar( value = 3, order = 600 )
	public Integer curtermStatus;

	/**
	 * 逾期标志(YesNo)
 	 */
	@CChar( value = 3, order = 700 )
	public String overDueFlag;

	/**
	 * 录入日期
 	 */
	@CChar( value = 8, datePattern="yyyyMMdd", order = 800 )
	public Date inputDate;

	/**
	 * 更新日期
 	 */
	@CChar( value = 8, datePattern="yyyyMMdd", order = 900 )
	public Date updateDate;
}
