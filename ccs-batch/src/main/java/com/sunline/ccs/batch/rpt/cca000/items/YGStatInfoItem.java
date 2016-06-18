package com.sunline.ccs.batch.rpt.cca000.items;

import java.math.BigDecimal;
import java.util.Date;

import com.sunline.ark.support.cstruct.CChar;

/**
 * YGStatInfo
 * @author wanghl
 *
 */
public class YGStatInfoItem {

	/**
	 * 前一天放款总笔数
 	 */
	@CChar( value = 12, order = 100 )
	public Long loanCount;

	/**
	 * 前一天放款总金额
 	 */
	@CChar( value = 15, pointSupported=true, order = 200 )
	public BigDecimal loanAmtSum;

	/**
	 * 当天总剩余贷款笔数
 	 */
	@CChar( value = 12, order = 300 )
	public Long leftLoanCount;

	/**
	 * 当天总剩余本金
 	 */
	@CChar( value = 15, pointSupported=true, order = 400 )
	public BigDecimal leftPrinSum;

	/**
	 * 当天理赔记录总笔数
 	 */
	@CChar( value = 12, order = 500 )
	public Long claimCount;

	/**
	 * 当天理赔记录总金额
 	 */
	@CChar( value = 15, pointSupported=true, order = 600 )
	public BigDecimal claimAmtSum;

	/**
	 * 前一天追偿代扣总笔数
 	 */
	@CChar( value = 12, order = 700 )
	public Long subrogationCount;

	/**
	 * 前一天追偿代扣总金额
 	 */
	@CChar( value = 15, pointSupported=true, order = 800 )
	public BigDecimal subrogationAmtSum;

	/**
	 * 入库日期
 	 */
	@CChar( value = 8, datePattern="yyyyMMdd", order = 900 )
	public Date inputDate;

	/**
	 * 修改日期
 	 */
	@CChar( value = 8, datePattern="yyyyMMdd", order = 1000 )
	public Date updateTime;

	/**
	 * 前一天还贷款笔数
 	 */
	@CChar( value = 12, order = 1100 )
	public Long repayLoanCount;

	/**
	 * 前一天还贷款金额
 	 */
	@CChar( value = 15, pointSupported=true, order = 1200 )
	public BigDecimal repayLoanSum;

	/**
	 * 前一天还保费笔数
 	 */
	@CChar( value = 12, order = 1300 )
	public Long repayCorpCount;

	/**
	 * 前一天还保费金额
 	 */
	@CChar( value = 15, pointSupported=true, order = 1400 )
	public BigDecimal repayCorpSum;
}
