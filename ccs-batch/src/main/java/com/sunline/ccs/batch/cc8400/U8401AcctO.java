package com.sunline.ccs.batch.cc8400;

import java.math.BigDecimal;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ark.support.cstruct.CChar;

/**
 * @see 类名：U8401AcctO
 * @see 描述：账户更新临时表
 *
 * @see 创建日期：   2015-6-24下午2:29:41
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class U8401AcctO {
	@CChar( value = 12, order = 100 )
	public String org;

	@CChar( value = 20, zeroPadding = true, order = 200 )
	public Long acctNo;

	@CChar( value = 1, order = 300 )
	public AccountType acctType;

	@CChar( value = 15, precision = 2, order = 400 )
	public BigDecimal currBal;

	@CChar( value = 15, precision = 2, order = 500 )
	public BigDecimal cashBal;

    @CChar( value = 15, precision = 2, order = 600 )
    public BigDecimal loanBal;

    @CChar( value = 15, precision = 2, order = 700 )
    public BigDecimal disputeAmt;

	@CChar( value = 15, precision = 2, order = 800 )
	public BigDecimal unmatchCash;

	@CChar( value = 15, precision = 2, order = 900 )
	public BigDecimal unmatchCr;

	@CChar( value = 15, precision = 2, order = 1000 )
	public BigDecimal unmatchDb;

	@CChar( value = 27, order = 1100 )
	public String blockCode;

	@CChar( value = 15, order = 1200 )
	public String ltdLoanAmt;
	
	/**
	 * 数据处理标识
	 * Y = after , N = before;
	 */
	@CChar( value = 1, order = 1300 )
	public Indicator batchFlag;
	
	/**
	 * 批量期间隔日类交易数量
	 */
	@CChar( value = 9, order = 1400 )
	public Integer otherDayTxnCount;

}
