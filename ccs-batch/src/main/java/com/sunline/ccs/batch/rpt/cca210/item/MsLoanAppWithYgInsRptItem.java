package com.sunline.ccs.batch.rpt.cca210.item;

import java.math.BigDecimal;
import java.util.Date;

import com.sunline.ark.support.cstruct.CChar;
import com.sunline.ppy.dictionary.enums.Gender;
import com.sunline.ppy.dictionary.enums.IdType;

/**
 * 寿险客户统计报表
 * @author wanghl
 *
 */
public class MsLoanAppWithYgInsRptItem {

	/**
	 * 客户姓名
	 */
	@CChar ( value = 80, order = 100 )
	public String name;
	/**
	 * 客户性别
	 */
	@CChar ( value = 1, order = 200)
	public Gender gender;
	
	/**
	 * 身份证类型
	 */
	@CChar ( value = 1, order = 300)
	public IdType idType;
	/**
	 * 身份证号
	 */
	@CChar ( value = 30, order = 400)
	public String idNo;
	/**
	 * 贷款本金/投保额度
	 */
	@CChar( value = 15, order = 500 )
	public BigDecimal loanAmt;
	
	/**
	 * 寿险生效日
	 */
	@CChar( value = 8, datePattern = "yyyyMMdd", order = 600)
	public Date lifeInsValidDate;
	
	/**
	 * 寿险终止日
	 */
	@CChar( value = 8, datePattern = "yyyyMMdd", order = 700)
	public Date lifeInsEndDate;
	
}