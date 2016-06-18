package com.sunline.ccs.batch.rpt.cca210.item;

import java.math.BigDecimal;
import java.util.Date;

import com.sunline.ark.support.cstruct.CChar;
import com.sunline.ppy.dictionary.enums.OrderStatus;

/**
 * 马上贷放款结果查询送报表接口
* @author wanghl
 *
 */
public class MsLoanRptItem {
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
	@CChar ( value = 20, order = 100 )
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
	public Date loanDate;

	/**
	 * 付款状态
	 */
	@CChar ( value = 8, datePattern = "yyyyMMdd", order = 600 )
	public OrderStatus orderStatus;

	/**
	 * 合同金额
	 */
	@CChar( value = 15, order = 700 )
	public BigDecimal loanAmt;
	
	/**
	 * 合同期数
	 */
	@CChar ( value = 15,order = 800 )
	public Integer loanTerm;

	/**
	 * 利率
	 */
	@CChar ( value = 12, order = 1000 )
	public String interestRate;
	
	/**
	 * 服务费率
	 */
	@CChar ( value = 12, order = 1200 )
	public String svcFeeRate;
	/**
	 * 可选服务费率
	 */
	@CChar ( value = 12, order = 1200 )
	public String optionalSvcFeeRate;
	/**
	 * 趸交费
	 */
	@CChar ( value = 15, pointSupported=true, order = 1300 )
	public BigDecimal premiumAmt;
	
}
