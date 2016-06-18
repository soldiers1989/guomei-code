package com.sunline.ccs.batch.rpt.cca220.item;

import java.math.BigDecimal;
import java.util.Date;

import com.sunline.ark.support.cstruct.CChar;

/**
 * 随借随还还款结果查询报表
 * @author wanghl
 *
 */
public class MCATLoanRepayRptItem {
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
	 * 合同号
	 */
	@CChar ( value = 20, zeroPadding = true, order = 100 )
	public String contrNbr;
	/**
	 * 客户姓名
	 */
	@CChar ( value = 80, order = 340 )
	public String name;
	
	/**
	 * 身份证号
	 */
	@CChar ( value = 30, order = 360)
	public String idNo;
	/**
	 * 还款金额
	 */
	@CChar( value = 15, pointSupported=true, order = 600)
	public BigDecimal repayAmt;
	/**
	 * 实还本金
	 */
	@CChar( value = 15, pointSupported=true, order = 700)
	public BigDecimal actualRepayPrincipal;
	/**
	 * 实还利息
	 */
	@CChar( value = 15, pointSupported=true, order = 800)
	public BigDecimal actualRepayInterest;
	/**
	 * 实还服务费
	 */
	@CChar( value = 15, pointSupported=true, order = 850)
	public BigDecimal actualRepaySvcFee;
	/**
	 * 实还滞纳金
	 */
	@CChar( value = 15, pointSupported=true, order = 900)
	public BigDecimal actualRepayLpc;
	
	/**
	 * 实还交易费
	 */
	@CChar( value = 15, pointSupported=true, order = 1000)
	public BigDecimal actualRepayTxnFee;
	/**
	 * 实还年费
	 */
	@CChar( value = 15, pointSupported=true, order = 1100)
	public BigDecimal actualRepayAnnualFee;
	/**
	 * 实还罚息
	 */
	@CChar( value = 15, pointSupported=true, order = 1150)
	public BigDecimal actualRepayPenalty;
	/**
	 * 实还寿险计划包
	 */
	@CChar( value = 15, pointSupported=true, order = 1170)
	public BigDecimal actualRepayLifeInsFee;

	/**
	 * 还款日期
	 */
	@CChar( value = 8, datePattern="yyyyMMdd", order = 1200)
	public Date repayDate; 
}
