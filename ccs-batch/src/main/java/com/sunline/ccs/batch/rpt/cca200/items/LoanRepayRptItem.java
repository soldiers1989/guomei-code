package com.sunline.ccs.batch.rpt.cca200.items;

import java.math.BigDecimal;
import java.util.Date;

import com.sunline.ark.support.cstruct.CChar;

/**
 * 还款结果查询表
 * @author wanghl
 *
 */
public class LoanRepayRptItem {
	
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
	@CChar( value = 15, order = 900)
	public BigDecimal actualRepayMulct;
	
	/**
	 * 实还保费
	 */
	@CChar( value = 15, order = 1000)
	public BigDecimal actualRepayIns;
	/**
	 * 实还提前还款手续费
	 */
	@CChar( value = 15, order = 1100)
	public BigDecimal actualRepayFineFee;
	/**
	 * 还款日期
	 */
	@CChar( value = 8, datePattern="yyyyMMdd", order = 1200)
	public Date repayDate; 
}
