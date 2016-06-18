package com.sunline.ccs.batch.rpt.cca200.items;

import java.math.BigDecimal;
import java.util.Date;

import com.sunline.ark.support.cstruct.CChar;

/**
 * 代位追偿送报表接口
 * 
 * @author liuq
 *
 */
public class LoanRecoveryRptItem {
	
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
	 * 追偿金额
	 */
	@CChar( value = 15, order = 1100 )
	public BigDecimal recoveryAmt;
	/**
	 * 追偿日期
	 */
	@CChar ( value = 8, datePattern = "yyyyMMdd", order = 1200 )
	public Date recoveryDate;
	
}
