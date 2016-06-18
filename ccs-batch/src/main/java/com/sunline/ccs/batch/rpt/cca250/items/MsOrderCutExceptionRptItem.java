package com.sunline.ccs.batch.rpt.cca250.items;

import java.math.BigDecimal;
import java.util.Date;

import com.sunline.ark.support.cstruct.CChar;

/**
 * 扣款异常报表
 * @author wanghl
 *
 */
public class MsOrderCutExceptionRptItem {
	/**
	 * 产品代码
	 */
	@CChar ( value = 6, order = 100)
	public String productCd;
	/**
	 * 产品描述
	 */
	@CChar ( value = 200, order = 200 )
	public String productDesc;
	/**
	 * 分期产品代码
	 */
	@CChar ( value = 4, order = 300)
	public String loanCode;
	/**
	 * 分期产品描述
	 */
	@CChar ( value = 200, order = 400 )
	public String loanDesc;
	/**
	 * 合同号
	 */
	@CChar ( value = 20, zeroPadding = true, order = 500 )
	public String contrNbr;
	/**
	 * 客户姓名
	 */
	@CChar ( value = 80, order = 600 )
	public String name;
	
	/**
	 * 身份证号
	 */
	@CChar ( value = 30, order = 700)
	public String idNo;
	/**
	 * 查询日期
	 */
	@CChar ( value = 8, datePattern = "yyyyMMdd", order = 800 )
	public Date searchDate;
	/**
	 * 扣款金额
	 */
	@CChar( value = 15, order = 1100 )
	public BigDecimal cutAmt;
	/**
	 * 扣款状态
	 */
	@CChar( value = 1, order = 1200 )
	public String cutStatus;
}
