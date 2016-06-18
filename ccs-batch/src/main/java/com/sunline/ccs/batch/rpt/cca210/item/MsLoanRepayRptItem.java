package com.sunline.ccs.batch.rpt.cca210.item;

import java.math.BigDecimal;
import java.util.Date;

import com.sunline.ark.support.cstruct.CChar;

/**
 * 马上贷还款结果查询表
 * @author wanghl
 *
 */
public class MsLoanRepayRptItem {
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
	 * 放款日期
	 */
	@CChar ( value = 8, datePattern = "yyyyMMdd", order = 400 )
	public Date activeDate;
	/**
	 * 合同金额
	 */
	@CChar( value = 15, order = 500 )
	public BigDecimal loanAmt;
	/**
	 * 还款金额
	 */
	@CChar( value = 15, order = 600)
	public BigDecimal repayAmt;
	/**
	 * 实还本金
	 */
	@CChar( value = 15, order = 700)
	public BigDecimal actualRepayPrincipal;
	/**
	 * 实还利息
	 */
	@CChar( value = 15, order = 800)
	public BigDecimal actualRepayInterest;
	/**
	 * 实还罚金
	 */
	@CChar( value = 15, pointSupported=true, order = 900)
	public BigDecimal actualRepayMulct;
	
	/**
	 * 实还服务费
	 */
	@CChar( value = 15, pointSupported=true, order = 1000)
	public BigDecimal actualRepaySvcFee;
	/**
	 * 实还可选服务费
	 */
	@CChar( value = 15, pointSupported=true, order = 1100)
	public BigDecimal actualRepayLifeInsFee;
	/**
	 * 实还交易费
	 */
	@CChar( value = 15, pointSupported=true, order = 1200)
	public BigDecimal actualRepayTxnFee; 
	/**
	 * 实还提前还款包
	 */
	@CChar( value = 15, pointSupported=true, order = 1250)
	public BigDecimal actualRepayPrePayPKGAmt;
	/**
	 * 实还罚息
	 */
	@CChar( value = 15, pointSupported=true, order = 1300)
	public BigDecimal actualRepayPenalty;
	/**
	 * 实还滞纳金
	 */
	@CChar( value = 15, pointSupported=true, order = 1400)
	public BigDecimal actualRepayLpc;
	/**
	 * 实还代收服务费
	 */
	@CChar( value = 15, pointSupported=true, order = 1500)
	public BigDecimal actualRepayRepalaceSvcFee;
	/**
	 * 实还代收罚息
	 */
	@CChar( value = 15, pointSupported=true, order = 1600)
	public BigDecimal actualRepayReplacePenalty;
	/**
	 * 实还代收滞纳金
	 */
	@CChar( value = 15, pointSupported=true, order = 1700)
	public BigDecimal actualRepayReplaceLateFee;
	/**
	 * 实还代收罚金
	 */
	@CChar( value = 15, pointSupported=true, order = 1800)
	public BigDecimal actualRepayReplaceMulctAmt;
	/**
	 * 实还代收提前还款违约金
	 */
	@CChar( value = 15, pointSupported=true, order = 1900)
	public BigDecimal actualRepayReplaceTxnFee;
	/**
	 * 还款日期
	 */
	@CChar( value = 8, datePattern="yyyyMMdd", order = 2000)
	public Date repayDate; 
}
