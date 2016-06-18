package com.sunline.ccs.batch.rpt.cca210.item;

import java.math.BigDecimal;
import java.util.Date;

import com.sunline.ark.support.cstruct.CChar;

/**
 * 马上贷贷款余额报表
 * @author wanghl
 *
 */
public class MsLoanBalanceRptItem {
	/**
	 * 产品代码
	 */
	@CChar ( value = 6, order = 20)
	public String productCd;
	/**
	 * 产品描述
	 */
	@CChar ( value = 200, order = 30 )
	public String productDesc;
	/**
	 * 分期产品代码
	 */
	@CChar ( value = 4, order = 40)
	public String loanCode;
	/**
	 * 分期产品描述
	 */
	@CChar ( value = 200, order = 50 )
	public String loanDesc;
	/**
	 * 贷款合同编号
	 */
	@CChar ( value = 20, order = 100 )
	public String contrNbr;
	/**
	 * 客户姓名
	 */
	@CChar ( value = 80, order = 200 )
	public String name;
	/**
	 * 身份号码
	 */
	@CChar ( value = 30, order = 300)
	public String idNo;
	/**
	 * 查询日期
	 */
	@CChar ( value = 8, datePattern = "yyyyMMdd", order = 400 )
	public Date searchDate;
	/**
	 * 放款日期
	 */
	@CChar ( value = 8, datePattern = "yyyyMMdd", order = 500 )
	public Date activeDate;
	/**
	 * 合同金额
	 */
	@CChar( value = 15, pointSupported=true,  order = 600 )
	public BigDecimal loanAmt;
	/**
	 * 合同期数
	 */
	@CChar ( value = 13, order = 700 )
	public Integer loanTerm;
	/**
	 * 贷款余额
	 */
	@CChar ( value = 15, pointSupported=true, order = 750 )
	public BigDecimal balance;
	/**
	 * 逾期天数
	 */
	@CChar( value = 10, order = 800)
	public Integer overDueDayCount;
	/**
	 * 入催天数
	 */
	@CChar( value = 10, order = 850)
	public Integer CPDDayCount;
	/**
	 * 应预提利息
	 */
	@CChar( value = 15, pointSupported=true, order = 900)
	public BigDecimal trialInterest;
	/**
	 * 应预提服务费
	 */
	@CChar( value = 15, pointSupported=true, order = 1000)
	public BigDecimal trialSvcFee;
	/**
	 * 应预提寿险计划包费
	 */
	@CChar( value = 15, pointSupported=true, order = 1050)
	public BigDecimal trialLifeInsFee;
	/**
	 * 应预提提前还款包
	 */
	@CChar( value = 15, pointSupported=true, order = 1070)
	public BigDecimal trialPrepayPKGAmt;
	
	/**
	 * 应预提罚金
	 */
	@CChar( value = 15, pointSupported=true, order = 1100)
	public BigDecimal trialMulct;
	
	/**
	 * 应预提罚息
	 */
	@CChar( value = 15, pointSupported = true, order = 1200)
	public BigDecimal trialPenalty;
	
	/**
	 * 应预提滞纳金
	 */
	@CChar( value = 15, pointSupported=true,  order = 1300)
	public BigDecimal trialLpc;
}
