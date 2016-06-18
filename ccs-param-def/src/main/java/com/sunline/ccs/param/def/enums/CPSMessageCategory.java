package com.sunline.ccs.param.def.enums;

import com.sunline.ark.support.meta.EnumInfo;

/**
 * 命名规则：
 * CPS发短信，以CPS开头，CPS001开始，依次增加
 */
@EnumInfo({
	"CPS001|预留电话变更提醒",
	"CPS002|预留地址变更提醒",
	"CPS003|预留问题答案变更提醒",
	"CPS004|支付密码错误超限提醒",
//	"CPS005|支付密码成功修改提醒",
//	"CPS006|查询密码重置提醒",
	"CPS007|凭密标志变更提醒",
	"CPS008|支付密码解锁提醒",
	"CPS009|卡片冻结解除提醒",
//	"CPS010|重打密码函提醒",
	"CPS011|卡片寄送地址变更提醒",
	"CPS012|账单寄送地址变更提醒",
	"CPS013|预留联系人信息变更提醒",
	"CPS014|账单介质类型变更提醒",
	"CPS015|取现额度设置提醒",
	"CPS016|分期额度设置提醒",
	"CPS017|激活成功",
	"CPS018|查询密码锁定重置提醒",
//	"CPS019|查询密码修改提醒",
//	"CPS020|支付密码重置提醒",
	"CPS021|Email变更提醒",
	"CPS022|账单日变更提醒",
	"CPS023|联机正向交易通知",
	"CPS024|商户分期付款（暂不支持）",
	"CPS025|联机撤销交易通知",
	"CPS026|联机冲正交易通知",
	"CPS027|联机撤销的冲正交易通知",
	"CPS028|临时额度调整成功",
	"CPS029|永久额度调整成功",
	"CPS030|额度调整拒绝",
	"CPS031|卡片限额调整成功",
	"CPS032|分期付款申请成功",
	"CPS033|分期付款展期成功",
	"CPS034|分期付款取消",
	"CPS035|分期付款中止",
	"CPS036|分期展期撤销成功",
	"CPS037|账单提醒",
	"CPS038|绑定约定还款",
	"CPS039|拖欠催款",
	"CPS040|到期换卡",
	"CPS041|约定还款失败",
	"CPS042|约定还款成功",
	"CPS043|建客建账建卡成功短信",
	"CPS044|现金分期放款成功短信(一次性收取手续费)",
	"CPS045|现金分期放款成功短信(分期收取手续费)",
	"CPS046|预销户成功短信",
	"CPS047|余额查询",
	"CPS048|额度查询",
	"CPS049|账单查询",
	"CPS050|积分查询",
	"CPS051|指定借据还款成功",
	"CPS052|小额贷款展期成功",
	"CPS053|小额贷款缩期成功",
	"CPS054|卡片冻结提醒",
	"CPS055|卡片挂失提醒",
	"CPS056|卡片解挂提醒",
	"CPS057|约定还款取消提醒",
	"CPS058|转正式挂失短信提醒",
	"CPS059|转正式挂失换卡短信提醒",
	"CPS060|约定还款成功--扣款金额小于约定还款金额",
	"CPS061|到期还款提前短信提醒",
	"CPS062|临时额度取消短信",
	"CPS063|现金分期放款失败",
	"CPS064|有卡自助消费业务密码失败提醒",//AIC2.7银联升级
	"CPS065|建立委托",
	"CPS066|余额查询",
	"CPS067|账户验证",
	"CPS068|解除委托",
	"CPS069|建立委托冲正",
	"CPS070|解除委托冲正"
})
public enum CPSMessageCategory
{
	/**
	 * 预留电话变更提醒
	 */
	CPS001("phoneType|电话号码类型", "oldPhone|旧电话号码", "newPhone|新电话号码"),
	
	/**
	 * 预留地址变更提醒
	 */
	CPS002("addressType|地址类型", "newAddress|新地址"),
	
	/**
	 * 预留问题答案变更提醒
	 */
	CPS003,
	
	/**
	 * 支付密码错误超限提醒
	 */
	CPS004,
	
	/**
	 * 支付密码成功修改提醒
	 */
//	CPS005,
	
	/**
	 * 查询密码重置提醒 
	 */
//	CPS006,
	
	/**
	 * 凭密标志变更提醒
	 */
	CPS007,
	
	/**
	 * 支付密码解锁提醒
	 */
	CPS008,
	
	/**
	 * 卡片冻结解除提醒
	 */
	CPS009,
	
	/**
	 * 重打密码函提醒
	 */
//	CPS010,
	
	/**
	 * 卡片寄送地址变更提醒
	 */
	CPS011("oldAddressType|原地址类型", "newAddressType|新地址类型"),
	
	/**
	 * 账单寄送地址变更提醒
	 */
	CPS012("oldAddressType|原地址类型", "newAddressType|新地址类型"),
	
	/**
	 * 预留联系人信息变更提醒
	 */
	CPS013("contactName|联系人姓名", "relationship|与联系人关系"),
	
	/**
	 * 账单介质类型变更提醒
	 */
	CPS014("oldMediaType|原账单介质类型", "newMediaType|新账单介质类型"),
	
	/**
	 * 取现额度设置提醒
	 */
	CPS015("cashLimitRt|取现额度比率", "currencyCd|货币代码"),
	
	/**
	 * 分期额度设置提醒
	 */
	CPS016("loanLimitRt|分期额度比率", "currencyCd|货币代码"),
	
	/**
	 * 激活成功
	 */
	CPS017("posPinVerifyInd|消费凭密标识"),
	
	/**
	 * 查询密码锁定重置提醒
	 */
	CPS018,
	/**
	 * 查询密码修改提醒
	 */
//	CPS019,
	/**
	 * 支付密码重置提醒
	 */
//	CPS020,
	/**
	 * Email变更提醒
	 */
	CPS021("oldEmail|原Email地址", "newEmail|新Email地址"),
	
	/**
	 * 账单日变更提醒
	 */
	CPS022("oldBillingCycle|原账单日", "newBillingCycle|新账单日"),
	
	/**
	 * 联机正向交易通知
	 */
	CPS023("currencyCd|货币代码", "transType|交易类别", "amount|交易金额", "otb|可用余额", "otbCash|可用取现余额"),
	
	/**
	 * 商户分期付款（暂不支持）
	 */
	CPS024,
	
	/**
	 * 联机撤销交易通知
	 */
	CPS025("currencyCd|货币代码", "transType|交易类别", "amount|交易金额", "otb|可用余额", "otbCash|可用取现余额"),
	
	/**
	 * 联机冲正交易通知
	 */
	CPS026("currencyCd|货币代码", "transType|交易类别", "amount|交易金额", "otb|可用余额", "otbCash|可用取现余额"),
	
	 /**
     * 联机撤销的冲正交易通知
     */
	CPS027("currencyCd|货币代码", "transType|交易类别", "amount|交易金额", "otb|可用余额", "otbCash|可用取现余额"),
	
	/**
	 * 临时额度调整成功
	 */
	CPS028("currencyCd|货币代码", "creditLimit|调整后信用额度", "expireDate|临额到期日"),
	
	/**
	 * 永久额度调整成功
	 */
	CPS029("currencyCd|货币代码", "creditLimit|调整后信用额度"),
	
	/**
	 * 额度调整拒绝
	 */
	CPS030("currencyCd|货币代码", "creditLimit|当前信用额度"),
	
	/**
	 * 卡片限额调整成功
	 */
	CPS031("localCycleLimit|本币限额", "dualCycleLimit|外币限额"),
	
	/**
	 * 分期付款申请成功
	 */
	CPS032("loanType|分期类型", "amt|分期金额", "term|分期期数", "loanFee|总手续费", "nextPayment|下期还款金额", "loanFixedFee1|每期手续费"),
	
	/**
	 * 分期付款展期成功
	 */
	CPS033("loanType|分期类型", "amt|分期金额", "term|分期期数", "loanFee|总手续费", "nextPayment|下期还款金额", "loanFixedFee1|每期手续费"),
	
	/**
	 * 分期付款取消
	 */
	CPS034("loanType|分期类型", "amt|分期金额", "loanFee|手续费"),
	
	/**
	 * 分期付款中止
	 */
	CPS035("loanType|分期类型", "amt|分期金额", "loanFee|手续费"),
	
	/**
	 * 分期展期撤销成功
	 */
	CPS036("loanType|分期类型", "amt|分期金额", "term|分期期数", "loanFee|总手续费", "nextPayment|下期还款金额", "loanFixedFee1|每期手续费"),
	
	/**
	 * 账单提醒
	 */
	CPS037("currencyCd|货币代码", "stmtDate|账单日期", "graceBalance|全额应还款额", "due|最小还款额", "paymentDate|到期还款日"),
	
	/**
	 * 绑定约定还款
	 */
	CPS038("ddInd|约定还款类型", "nextStmtDate|下次账单日"),
	
	/**
	 * 拖欠催款
	 */
	CPS039("currencyCd|货币代码", "stmtDate|账单日期", "graceBalance|全额应还款额", "due|最小还款额", "paymentDate|到期还款日"),
	
	/**
	 * 到期换卡
	 */
	CPS040("expiryDate|新卡有效期", "bsFlag|主附卡标识", "cardFetchType|领卡方式"),
	
	/**
	 * 约定还款失败
	 */
	CPS041("ddReturnCode|还款失败原因"),
	
	/**
	 * 约定还款成功
	 */
	CPS042("txnAmt|约定还款金额", "txnDate|约定还款日期"),
	
	/**
	 * 建客建账建卡成功
	 */
	CPS043("creditLimit|信用额度", "dualCreditLimit|外币信用额度"),
	
	/**
	 * 现金分期放款成功(一次性收取手续费)
	 */
	CPS044("loanInitPrin|分期金额", "loanInitTerm|分期期数", "loanFixedPmtPrin|每期还款金额", "loanInitFee1|总手续费"),
	
	CPS045("loanInitPrin|分期金额", "loanInitTerm|分期期数", "loanFixedPmtPrin|每期还款金额", "loanFixedFee1|每期手续费"),
	
	/**
	 * 预销户成功短信
	 */
	CPS046,
	
	/**
	 * 余额查询
	 */
	CPS047("ACCT_CASH_OTB|账户层取现额度","CURR_BAL|当前余额"),
	
	/**
	 * 额度查询
	 */
	CPS048("ACCT_LIMIT|账户层信用额度"),
	
	/**
	 * 账单查询
	 */
	CPS049("PMT_DUE_DATE|到期还款日期", "QUAL_GRACE_BAL|全部应还款额", "TOT_DUE_AMT|最小还款额"),
	
	/**
	 * 积分查询
	 */
	CPS050("POINT_BAL|积分余额"),
	
	/**
	 * 指定借据还款成功
	 */
	CPS051("loanReceiptNbr|借据号", "txnAmt|约定还款金额", "txnDate|约定还款日期"),
	
	/**
	 * 小额贷款展期缩期成功
	 */
	CPS052("loanReceiptNbr|借据号", "loanAction|行动类型", "loanActionDate|行动日期"),
	
	/**
	 * 卡片冻结提醒
	 */
	CPS054,
	
	/**
	 * 卡片挂失提醒
	 */
	CPS055,
	
	/**
	 * 卡片解挂提醒
	 */
	CPS056,
	
	/**
	 * 约定还款取消提醒
	 */
	CPS057,

	/**
	 * 转正式挂失短信提醒
	 */
	CPS058("tempLostDate|临时挂失日期"),
	
	/**
	 * 转正式挂失换卡短信提醒
	 */
	CPS059("tempLostDate|临时挂失日期", "expireDate|转正式换卡到期日期"),
	
	/**
	 * 约定还款成功--扣款金额小于约定还款金额
	 */
	CPS060("txnAmt|约定还款金额", "txnDate|约定还款日期", "payAmt|实际扣款金额", "notPayAmt|剩余未还金额"),
	
	/**
	 * 到期还款提前X天短信提醒
	 */
	CPS061("currencyCd|货币代码", "stmtDate|账单日期", "graceBalance|全额应还款额", "due|最小还款额", "paymentDate|到期还款日","currHavePay|当期已还款额","needToPay|剩余应还款额","minNeedToPay|剩余最小还款额"),
	/**
	 * 临时额度取消
	 */
	CPS062("currencyCd|货币代码", "creditLimit|取消后信用额度"),
	
	/**
	 * 现金分期放款失败
	 */
	CPS063("loanInitPrin|分期金额"),
	/**
	 * 有卡自助消费业务密码失败提醒
	 * AIC2.7银联升级
	 */
	CPS064,
	/**
	 * 建立委托
	 */
	CPS065,
	
	/**
	 * 余额查询
	 */
	CPS066,
	
	/**
	 * 账户验证
	 */
	CPS067,
	
	/**
	 * 解除委托
	 */
	CPS068,
	
	/**
	 * 建立委托冲正
	 */
	CPS069,
	
	/**
	 * 解除委托冲正
	 */
	CPS070
	;
	
	private String variables[];

	/**
	 * @param variables 以"|"分隔的一组字符串，写明该类型支持/需要提供的变量列表
	 */
	private CPSMessageCategory(String ... variables){
		this.variables = variables;
	}

	public String[] getVariables() {
		return variables;
	}
}
