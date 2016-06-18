package com.sunline.ccs.batch.rpt.cca000.items;

import java.math.BigDecimal;

import com.sunline.ark.support.cstruct.CChar;

/**
 * YGPenalInfo
 * @author wanghl
 *
 */
public class YGPenalInfoItem {

	/**
	 * 借据编号
 	 */
	@CChar( value = 32, order = 100 )
	public String putOutNo;

	/**
	 * 保单号
 	 */
	@CChar( value = 20, order = 200 )
	public String guarantyID;

	/**
	 * 违约金付款帐号
 	 */
	@CChar( value = 32, order = 300 )
	public String fbPaymentAcctNo;

	/**
	 * 违约金金额（保险公司）
 	 */
	@CChar( value = 15, pointSupported=true, order = 400 )
	public BigDecimal penalsum;

	/**
	 * 分行名称
 	 */
	@CChar( value = 32, order = 500 )
	public String branchBank;

	/**
	 * 支行名称
 	 */
	@CChar( value = 32, order = 600 )
	public String subBranchBank;
}
