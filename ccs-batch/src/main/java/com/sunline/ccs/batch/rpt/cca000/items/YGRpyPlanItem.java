package com.sunline.ccs.batch.rpt.cca000.items;

import java.math.BigDecimal;
import java.util.Date;

import com.sunline.ark.support.cstruct.CChar;

/**
 * YGRpyPlan
 * @author wanghl
 *
 */
public class YGRpyPlanItem {

	/**
	 * 借据号
 	 */
	@CChar( value = 32, order = 100 )
	public String putOutNo;

	/**
	 * 本期状态
 	 */
	@CChar( value = 1, order = 200 )
	public String status;

	/**
	 * 本期期数
 	 */
	@CChar( value = 12, order = 300 )
	public Integer loanTerm;

	/**
	 * 到期日
 	 */
	@CChar( value = 8, datePattern="yyyyMMdd", order = 400 )
	public Date maturityDate;

	/**
	 * 当前实际应还本金
 	 */
	@CChar( value = 15, pointSupported=true, order = 500 )
	public BigDecimal actualBalance;

	/**
	 * 当前实际应还利息
 	 */
	@CChar( value = 15, pointSupported=true, order = 600 )
	public BigDecimal actualInte;

	/**
	 * 正常应收本金
 	 */
	@CChar( value = 15, pointSupported=true, order = 700 )
	public BigDecimal balance;

	/**
	 * 正常应收利息
 	 */
	@CChar( value = 15, pointSupported=true, order = 800 )
	public BigDecimal inte;

	/**
	 * 罚金
 	 */
	@CChar( value = 15, pointSupported=true, order = 900 )
	public BigDecimal infine;
}
