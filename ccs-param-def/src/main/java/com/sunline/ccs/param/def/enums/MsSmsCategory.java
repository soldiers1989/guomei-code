package com.sunline.ccs.param.def.enums;

import com.sunline.ark.support.meta.EnumInfo;

/**
 * 马上短信种类配置
 * 命名规则：
 * CCS发短信，以CCS开头，CCS001开始，依次增加
 */
@EnumInfo({
	"CCS001|提现审核通过短信",
	"CCS002|调额审核通过短信",
	"CCS003|贷款发放成功短信",
	"CCS004|还款提醒短信（马上贷）",
	"CCS005|账单还款提醒短信（白名单）",
	"CCS006|提前还款申请成功短信",
	"CCS007|提前还款提醒短信",
	"CCS008|扣款失败短信（如余额不足、已挂失卡、无效卡号等）",
})
public enum MsSmsCategory
{
	/**
	 * 提现审核通过短信
	 */
	CCS001("custName|客户姓名", "otb|可用额度", "year|额度有效期年", "month|额度有效期月", "day|额度有效期日"),
	
	/**
	 * 调额审核通过短信
	 */
	CCS002("custName|客户姓名", "otb|可用额度", "year|额度有效期年", "month|额度有效期月", "day|额度有效期日"),
	
	/**
	 * 贷款发放成功短信
	 */
	CCS003("custName|客户姓名"),
	
	/**
	 * 还款提醒短信（马上贷）
	 */
	CCS004("custName|客户姓名", "year|到期还款日年", "month|到期还款日月", "day|到期还款日日", "due|到期还款金额"),
	
	/**
	 * 账单还款提醒短信（白名单）
	 */
	CCS005("custName|客户姓名", "year|到期还款日年", "month|到期还款日月", "day|到期还款日日", "due|到期还款金额"),
	
	/**
	 * 提前还款申请成功短信
	 */
	CCS006("custName|客户姓名", "year|最晚还款日年", "month|最晚还款日月", "day|最晚还款日日"),
	
	/**
	 * 提前还款提醒短信
	 */
	CCS007("custName|客户姓名", "year|最晚还款日年", "month|最晚还款日月", "day|最晚还款日日", "balance|提前还款应还款额"),
	
	/**
	 * 扣款失败短信（如余额不足、已挂失卡、无效卡号等）
	 */
	CCS008("reason|失败原因")
	;
	
	private String variables[];

	/**
	 * @param variables 以"|"分隔的一组字符串，组装短信时需要按照给定顺序传输对应变量，否则短信异常
	 */
	private MsSmsCategory(String ... variables){
		this.variables = variables;
	}

	public String[] getVariables() {
		return variables;
	}
}
