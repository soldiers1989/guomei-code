package com.sunline.ccs.service.api;

/**
 * 定义一些常用参数
 * 
* @author fanghj
 * 
 */
public class Constants {
	
	public static final String TXN_CODE_T840="T840";

	public static final String OPT_ZERO = "0";
	public static final String OPT_ONE = "1";
	public static final String OPT_TWO = "2";
	public static final String OPT_THREE = "3";
	
	public static final String CURR_CD_156= "156";
	public static final String CURR_CD_840= "840";
	public static final String DEFAULT_CURR_CD= "000";
	
	public static final String DEFAULT_STMT_DATE= "000000";
	
	public static final String ERR_MES_CUSTNOTNULL = "查询不到对应客户信息";
	public static final String ERR_MES_CUSTLIMITNOTNULL = "查询不到对应客户额度信息";
	public static final String ERR_MES_ACCTNOTNULL = "查询不到对应账户信息";
	public static final String ERR_MES_CARDNOTNULL = "查询不到对应卡片信息";
	
	/**
	 * 批量生成数据用户
	 */
	public static final String OP_USER_BATCH = "batch";
	
	/**
	 * 马上批量短信模板参数分隔符
	 */
	public static final String BATCH_SMS_SEPARATOR = "{}";
	/**
	 * 马上联机短信模板参数分隔符
	 */
	public static final String ONLINE_SMS_SEPARATOR = "|";
	/**
	 * 还款提醒提前天数参数分隔符
	 */
	public static final String REMINDER_SMS_ADVANCE_DAYS_SEPARATOR = ",";
	/**
	 * 来源系统-短信
	 */
	public static final String SOURCE_BIZ_SYSTEM = "ccs";
	/**
	 * 马上-短信业务类型-提现发放(成功/失败)
	 */
	public static final String WITHDRAW_CASH = "withdraw_cash";
	
	public static final String WITHDRAW_CASH_FAIL = "withdraw_cash_fail";
	/**
	 * 马上-短信业务类型-退货成功失败
	 */
	public static final String RETURN_GOODS = "return_goods";
	public static final String RETURN_FAIL = "return_fail";
	/**
	 * 马上-短信业务类型-手工调额（随借随还）
	 */
	public static final String MANUAL_ADJUSTMENT = "manual_adjustment";
	/**
	 * 马上-短信业务类型-批量调额（随借随还）
	 */
	public static final String BATCH_ADJUSTMENT = "batch_adjustment";
	/**
	 * 马上-短信业务类型-贷款发放
	 */
	public static final String LOAN_PAYMENT_SUCCESS = "loan_payment_success";
	/**
	 * 马上-短信业务类型-实时扣款失败
	 */
	public static final String REAL_TIME_FROM_FAILURE = "real_time_from_failure";
	/**
	 * 马上-短信业务类型-主动还款
	 */
	public static final String ACTIVE_REPAYMENT = "active_repayment";
	/**
	 * 马上-短信业务类型-还款提醒短信
	 */
	public static final String ACCOUNTING_REPAYMENT_REMINDER = "batch_repayment_reminder";
	/**
	 * 马上-短信业务类型-账单还款提醒短信
	 */
	public static final String BILL_ACCOUNTING_REPAYMENT_REMINDER = "bill_batch_repayment_reminder";
	/**
	 * 马上-短信业务类型-批量扣款失败（如余额不足、已挂失卡、无效卡号等）
	 */
	public static final String FROM_FAILURE = "from_failure";
	/**
	 * 马上-短信业务类型-提前还款提醒
	 */
	public static final String BATCH_MESSAGE_PREPAY_TIPS = "prepayment_reminder";
	/**
	 * 马上-短信业务类型-预约提前还款短信通知
	 */
	public static final String PREPAYMENT_BOOKING_SUCCESS = "prepayment";
	/**
	 * 马上-短信业务类型-退货欠款短信通知
	 */
	public static final String BATCH_RETURN_REMINDER = "batch_return_reminder";
	/**
	 * 马上-短信业务类型-批量扣款成功通知客户短信
	 */
	public static final String BATCH_CUT_SUCCESS_REMINDER = "from_success";
	/**
	 * 溢缴款转出成功
	 */
	public static final String OVERFLOW_PAYMENT_SUCCESS = "overflow_payment_success";
	/**
	 * 溢缴款转出失败
	 */
	public static final String OVERFLOW_PAYMENT_FAILURE = "overflow_payment_failure";
	/**
	 * 挂失类型
	 */
	public static final String S14050_R = "R";
	
	
	//查询类Q；客户类100，账户类200，卡片类300

	
	// 查询类Q，操作类O，积分类P，分期类L ,S 系统异常
	
	
	public static final String ERRS001_CODE ="S001";
	public static final String ERRS001_MES ="系统处理异常";
	
	public static final String ERRS002_CODE ="S002";
	public static final String ERRS002_MES ="未知的服务码";
	
	public static final String ERRS003_CODE ="S003";
	public static final String ERRS003_MES ="非法的请求字段";
	
	public static final String ERRS004_CODE ="S004";
	public static final String ERRS004_MES ="无效操作类型";
	
	public static final String ERRS006_CODE = "S006";
	public static final String ERRS006_MES = "报文验证错误";
	
	public static final String ERRS007_CODE ="S007";
	public static final String ERRS007_MES ="无效的日期类型";
	
	public static final String ERRS008_CODE ="S008";
	public static final String ERRS008_MES ="无效的翻页数据";
	
	public static final String ERRS009_CODE ="S009";
	public static final String ERRS009_MES ="翻页的起始位置不能大于等于结束位置";
	
	public static final String ERRS010_CODE ="S010";
	public static final String ERRS010_MES ="翻页查询记录数超过设置的最大值";
	
	public static final String ERRS011_CODE ="S011";
	public static final String ERRS011_MES ="翻页的结束位置lastrow必须大于0";
	
	public static final String ERRS012_CODE ="S012";
	public static final String ERRS012_MES ="服务调用控制不通过，原因";
	
	
	
	/**
	 * 卡片信息不存在
	 */
	public static final String ERRB001_CODE = "B001";
	public static final String ERRB001_MES = "卡片信息不存在";
	
	
	public static final String ERRB002_CODE = "B002";
	public static final String ERRB002_MES = "无效的卡片";
	
	
	public static final String ERRB003_CODE = "B003";
	public static final String ERRB003_MES = "无效的币种";
	
	
	public static final String ERRB004_CODE = "B004";
	public static final String ERRB004_MES = "起始日期大于截至日期";

	/**
	 * 设置失败：凭密设置授权未通过
	 */
	public static final String ERRB006_CODE = "B006";
	public static final String ERRB006_MES = "凭密设置授权未通过";

	/**
	 * 设置失败：当前状态与设置值一致，无需再设
	 */
	public static final String ERRB007_CODE = "B007";
	public static final String ERRB007_MES = "当前状态与设置值一致";

	/**
	 * 销卡失败：卡片存在欠款
	 */
	public static final String ERRB008_CODE = "B008";
	public static final String ERRB008_MES = "销卡失败,存在欠款或授权未入账金额";

	/**
	 * 主卡为非销卡状态，无需撤销操作
	 */
	public static final String ERRB009_CODE = "B009";
	public static final String ERRB009_MES = "主卡为非销卡状态，不能做撤销操作";

	/**
	 * 账户已经关闭，无法对卡片做销卡操作
	 */
	public static final String ERRB010_CODE = "B010";
	public static final String ERRB010_MES = "账户已经关闭，无法对卡片做销卡操作";

	/**
	 * 主卡为销卡状态，不可为附卡进行销卡撤销操作
	 */
	public static final String ERRB011_CODE = "B011";
	public static final String ERRB011_MES = "主卡为销卡状态，不可为附卡进行销卡撤销操作";
	
	/**
	 * 卡片已经冻结,无需在设
	 */
	public static final String ERRB012_CODE = "B012";
	public static final String ERRB012_MES = "卡片已经冻结,无需在设";
	
	/**
	 * 卡片非冻结状态，无需解冻
	 */
	public static final String ERRB013_CODE = "B013";
	public static final String ERRB013_MES = "卡片非冻结状态，无需解冻";
	
	/**
	 * 换卡暂时仅支持[损坏,挂失]类型
	 */
	public static final String ERRB014_CODE = "B014";
	public static final String ERRB014_MES = "换卡暂时仅支持[损坏,挂失]类型";
	
	/**
	 * 客户信息不存在
	 */
	public static final String ERRB015_CODE = "B015";
	public static final String ERRB015_MES = "客户信息不存在";
	
	/**
	 * 账户信息验证失败
	 */
	public static final String ERRB016_CODE = "B016";
	public static final String ERRB016_MES = "账户信息验证失败";
	
	/**
	 * 密码类型不存在
	 */
	public static final String ERRB017_CODE = "B017";
	public static final String ERRB017_MES = "密码类型不存在";
	
	
	/**
	 * 需发送电子账单，而电子邮箱没设置
	 */
	public static final String ERRB018_CODE = "B018";
	public static final String ERRB018_MES = "需发送电子账单，而电子邮箱没设置";
	
	/**
	 * 需发送电子账单，而电子邮箱没设置
	 */
	public static final String ERRB019_CODE = "B019";
	public static final String ERRB019_MES = "无效的电子邮箱";
	
	/**
	 * 卡号、证件均无效
	 */
	public static final String ERRB020_CODE = "B020";
	public static final String ERRB020_MES = "卡号或证件无效";
	
	/**
	 * 无效证件
	 */
	public static final String ERRB021_CODE = "B021";
	public static final String ERRB021_MES = "无效证件";
	
	
	/**
	 * 地址类型不能为空
	 */
	public static final String ERRB023_CODE = "B023";
	public static final String ERRB023_MES = "地址类型不能为空";
	
	/**
	 * 与持卡人关系不能为空
	 */
	public static final String ERRB024_CODE = "B024";
	public static final String ERRB024_MES = "与持卡人关系不能为空";
	
	/**
	 * 联系人证件类型不能为空
	 */
	public static final String ERRB025_CODE = "B025";
	public static final String ERRB025_MES = "联系人证件类型不能为空";

	/**
	 * 无效的邮政编码
	 */
	public static final String ERRB026_CODE = "B026";
	public static final String ERRB026_MES = "无效的邮政编码";
	
	/**
	 * 无效的电话号码，只能输入0XX-XXXXXXXX;XXXXXXXX;0XXXXXXXXXX或手机号
	 */
	public static final String ERRB027_CODE = "B027";
	public static final String ERRB027_MES = "无效的电话号码，只能输入0XX-XXXXXXXX;XXXXXXXX;0XXXXXXXXXXX或手机号";
	
	/**
	 * 无效的移动电话
	 */
	public static final String ERRB028_CODE = "B028";
	public static final String ERRB028_MES = "无效的移动电话";
	
	/**
	 * 生日与身份证不匹配
	 */
	public static final String ERRB029_CODE = "B029";
	public static final String ERRB029_MES = "生日与身份证不匹配";
	
	/**
	 * 寄送地址与原地址相同
	 */
	public static final String ERRB030_CODE = "B030";
	public static final String ERRB030_MES = "寄送地址与原地址相同";
	
	/**
	 * 寄送地址不能为空
	 */
	public static final String ERRB031_CODE = "B031";
	public static final String ERRB031_MES = "寄送地址不能为空";
	
	/**
	 * 账单介质类型不能为空
	 */
	public static final String ERRB032_CODE = "B032";
	public static final String ERRB032_MES = "账单介质类型不能为空";
	
	/**
	 * 账单介质与原账单介质相同
	 */
	public static final String ERRB034_CODE = "B034";
	public static final String ERRB034_MES = "账单介质与原账单介质相同";
	
	/**
	 * 无效的约定还款账号
	 */
	public static final String ERRB035_CODE = "B035";
	public static final String ERRB035_MES = "无效的约定还款账号";
	
	/**
	 * 约定还款设置失败:持卡人姓名与约定还款账号用户姓名不一致
	 */
	public static final String ERRB036_CODE = "B036";
	public static final String ERRB036_MES = "持卡人姓名与约定还款账号用户姓名不一致";
	
	/**
	 * 临额设置失败：临时额度不能为空
	 */
	public static final String ERRB037_CODE = "B037";
	public static final String ERRB037_MES = "临时额度不能为空或大于信用额度";
	
	/**
	 * 临额设置失败：额度开始日期不能小于当前日期
	 */
	public static final String ERRB038_CODE = "B038";
	public static final String ERRB038_MES = "额度开始日期不能小于当前日期";
	
	/**
	 * 临额设置失败：额度开始日期不能大于结束日期
	 */
	public static final String ERRB039_CODE = "B039";
	public static final String ERRB039_MES = "额度开始日期不能大于结束日期";
	
	/**
	 * 临额设置失败：临额有效期不大于临额最大有效月数
	 */
	public static final String ERRB040_CODE = "B040";
	public static final String ERRB040_MES = "临时有效期不能大于临额最大有效月数 ";
	
	/**
	 * 临额设置失败：临时额度不能大于最大信用额度
	 */
	public static final String ERRB041_CODE = "B041";
	public static final String ERRB041_MES = "临时额度不在可调整区间";
	
	/**
	 * 取现额度比例设定失败：取现额度比例不能为空
	 */
	public static final String ERRB042_CODE = "B042";
	public static final String ERRB042_MES = "取现额度比例不能为空";
	
	/**
	 * 取现额度比例设定失败：取现比例不能大于100%
	 */
	public static final String ERRB043_CODE = "B043";
	public static final String ERRB043_MES = "取现比例不能大于100%";
	
	/**
	 * 分期额度比例设定失败：分期额度比例不能为空
	 */
	public static final String ERRB044_CODE = "B044";
	public static final String ERRB044_MES = "分期额度比例不能为空";
	
	/**
	 * 分期额度比例设定失败：分期比例不能大于100%
	 */
	public static final String ERRB045_CODE = "B045";
	public static final String ERRB045_MES = "分期比例不能大于100%";
	
	/**
	 * 账单周期调整失败：超过本年调整最大次数
	 */
	public static final String ERRB046_CODE = "B046";
	public static final String ERRB046_MES = "超过本年调整最大次数";
	
	/**
	 * 账单周期调整失败：账单日与原账单日相同
	 */
	public static final String ERRB047_CODE = "B047";
	public static final String ERRB047_MES = "账单日与原账单日相同";
	
	/**
	 * 账单周期调整失败：无效的账单周期
	 */
	public static final String ERRB048_CODE = "B048";
	public static final String ERRB048_MES = "无效的账单周期";
	/**
	 * 不能重复减免年费
	 */
	public static final String ERRB049_CODE = "B049";
	public static final String ERRB049_MES = "不能重复减免年费";
	
	/**
	 * 未签约约定还款
	 */
	public static final String ERRB050_CODE = "B050";
	public static final String ERRB050_MES = "未签约约定还款";
	
	/**
	 * 临额设置失败：额度开始日期为空，额度结束日期不空
	 */
	public static final String ERRB051_CODE = "B051";
	public static final String ERRB051_MES = "额度开始日期为空，额度结束日期不能为空";
	
	
	/**
	 * 卡片非销卡状态，无法进行销卡撤销
	 */
	public static final String ERRB052_CODE = "B052";
	public static final String ERRB052_MES = "卡片非销卡状态，无法进行销卡撤销";
	
	/**
	 * 已为销卡状态，无需再设
	 */
	public static final String ERRB053_CODE = "B053";
	public static final String ERRB053_MES = "已为销卡状态，无需再设";
	
	
	/**
	 * 设置的email地址与原地址相同
	 */
	public static final String ERRB054_CODE = "B054";
	public static final String ERRB054_MES = "设置的email地址与原地址相同";
	
	
	/**
	 * 账户信息验证错误原因：证件类型不匹配
	 */
	public static final String ERRB055_CODE = "B055";
	public static final String ERRB055_MES = "账户信息验证错误原因：证件类型不匹配";
	/**
	 * 账户信息验证错误原因：证件号码不匹配
	 */
	public static final String ERRB056_CODE = "B056";
	public static final String ERRB056_MES = "账户信息验证错误原因：证件号码不匹配";
	/**
	 * 账户信息验证错误原因：移动电话不匹配
	 */
	public static final String ERRB057_CODE = "B057";
	public static final String ERRB057_MES = "账户信息验证错误原因：移动电话不匹配";
	/**
	 * 账户信息验证错误原因：家庭电话不匹配
	 */
	public static final String ERRB058_CODE = "B058";
	public static final String ERRB058_MES = "账户信息验证错误原因：家庭电话不匹配";
	/**
	 * 账户信息验证错误原因：姓名不匹配
	 */
	public static final String ERRB059_CODE = "B059";
	public static final String ERRB059_MES = "账户信息验证错误原因：姓名不匹配";
	/**
	 * 账户信息验证错误原因：生日不匹配
	 */
	public static final String ERRB060_CODE = "B060";
	public static final String ERRB060_MES = "账户信息验证错误原因：生日不匹配";
	/**
	 * 账户信息验证错误原因：公司电话不匹配
	 */
	public static final String ERRB061_CODE = "B061";
	public static final String ERRB061_MES = "账户信息验证错误原因：单位电话不匹配";
	/**
	 * 账户信息验证错误原因：查询密码不匹配
	 */
	public static final String ERRB062_CODE = "B062";
	public static final String ERRB062_MES = "账户信息验证错误原因：查询密码不匹配";
	/**
	 * 账户信息验证错误原因：交易密码不匹配
	 */
	public static final String ERRB063_CODE = "B063";
	public static final String ERRB063_MES = "账户信息验证错误原因：交易密码不匹配";
	/**
	 * 账户信息验证错误原因：卡片有效期不匹配
	 */
	public static final String ERRB064_CODE = "B064";
	public static final String ERRB064_MES = "账户信息验证错误原因：卡片有效期不匹配";
	/**
	 * 账户信息验证错误原因：CVV2不匹配
	 */
	public static final String ERRB065_CODE = "B065";
	public static final String ERRB065_MES = "账户信息验证错误原因：CVV2不匹配";
	/**
	 * 账户信息验证错误原因：直属联系人姓名
	 */
	public static final String ERRB066_CODE = "B066";
	public static final String ERRB066_MES = "账户信息验证错误原因：直属联系人姓名不匹配";
	/**
	 * 账户信息验证错误原因：直属联系人电话
	 */
	public static final String ERRB067_CODE = "B067";
	public static final String ERRB067_MES = "账户信息验证错误原因：直属联系人电话不匹配";
			
		
	/**
	 * 查询不到对应账单信息
	 */
	public static final String ERRB068_CODE = "B068";
	public static final String ERRB068_MES = "查询不到对应账单信息";
	
	
	/**
	 * 此卡为附卡
	 */
	public static final String ERRB069_CODE = "B069";
	public static final String ERRB069_MES = "附卡不能执行此交易";
	
	/**
	 * 礼品编号不能为空
	 */
	public static final String ERRB070_CODE = "B070";
	public static final String ERRB070_MES = "礼品编号不能为空";
	
	/**
	 * 积分余额不足
	 */
	public static final String ERRB071_CODE = "B071";
	public static final String ERRB071_MES = "积分余额不足";
	
	/**
	 * 需确定兑换数量
	 */
	public static final String ERRB072_CODE = "B072";
	public static final String ERRB072_MES = "需确定兑换数量";
	
	/**
	 * 礼品非可兑换状态
	 */
	public static final String ERRB073_CODE = "B073";
	public static final String ERRB073_MES = "礼品非可兑换状态";
	
	/**
	 * 超出兑换日期范围
	 */
	public static final String ERRB074_CODE = "B074";
	public static final String ERRB074_MES = "超出兑换日期范围";
	
	public static final String ERRB075_CODE = "B075";
	public static final String ERRB075_MES = "剩余礼品不足";
	
	public static final String ERRB076_CODE = "B076";
	public static final String ERRB076_MES = "超过单笔最大可兑换数量";
	
	public static final String ERRB077_CODE = "B077";
	public static final String ERRB077_MES = "上送的地址类型无对应的地址";
	
	public static final String ERRB078_CODE = "B078";
	public static final String ERRB078_MES = "账单年月不能为空";
	
	public static final String ERRB079_CODE = "B079";
	public static final String ERRB079_MES = "无效的账单年月";
	
	
	
	public static final String ERRB080_CODE = "B080";
	public static final String ERRB080_MES = "已激活，不能做重复激活";
	
	
	public static final String ERRB081_CODE = "B081";
	public static final String ERRB081_MES = "无账单，无法补打账单";
	
	
	public static final String ERRB082_CODE = "B082";
	public static final String ERRB082_MES = "查询不到礼品";
	
	public static final String ERRB083_CODE = "B083";
	public static final String ERRB083_MES = "兑换积分下限不能大于上限";
	
	public static final String ERRB084_CODE = "B084";
	public static final String ERRB084_MES = "卡片限额设定的金额不能小于零";
	
	public static final String ERRB085_CODE = "B085";
	public static final String ERRB085_MES = "找不到联系人信息";
	
	public static final String ERRB086_CODE = "B086";
	public static final String ERRB086_MES = "限额不能为空或负值";
	
	public static final String ERRB087_CODE = "B087";
	public static final String ERRB087_MES = "寄送地址方式为C-客户自定义地址，收件人信息不能为空";
	
	public static final String ERRB088_CODE = "B088";
	public static final String ERRB088_MES = "无效挂失原因代码";
	
	public static final String ERRB089_CODE = "B089";
	public static final String ERRB089_MES = "未收取年费，无需减免";
	
	public static final String ERRB090_CODE = "B090";
	public static final String ERRB090_MES = "授权处理超时,无法做年费减免";
	
	public static final String ERRB091_CODE = "B091";
	public static final String ERRB091_MES = "年费减免金额大于已收年费金额";
	
	public static final String ERRB092_CODE = "B092";
	public static final String ERRB092_MES = "金额不能为空或负值";
	
	public static final String ERRB093_CODE = "B093";
	public static final String ERRB093_MES = "短信发送标志不合法，只能为：Y、N、C";
	
	public static final String ERRB094_CODE = "B094";
	public static final String ERRB094_MES = "等待调额复核结果，不能重复申请";
	
	public static final String ERRB095_CODE = "B095";
	public static final String ERRB095_MES = "授权处理拒绝,年费减免失败";
	
	/**
	 * 存在待销卡销户锁定码C,不能挂失
	 */
	public static final String ERRB096_CODE = "B096";
	public static final String ERRB096_MES = "账户已关闭，不能执行挂失操作";
	
	/**
	 * 查询密码错误次数超过产品最大查询密码错误次数
	 */
	public static final String ERRB097_CODE = "B097";
	public static final String ERRB097_MES = "查询密码错误次数超过最大次数";
	
	/**
	 * 交易密码错误次数超过产品最大交易密码错误次数
	 */
	public static final String ERRB098_CODE = "B098";
	public static final String ERRB098_MES = "交易密码错误次数超过最大次数";
	
	/**
	 * 未找到对应的地址信息
	 */
	public static final String ERRB099_CODE = "B099";
	public static final String ERRB099_MES = "未找到对应的地址信息";
	
	/**
	 * 卡片有效期不能为空
	 */
	public static final String ERRB100_CODE = "B100";
	public static final String ERRB100_MES = "卡片有效期不能为空";
	
	/**
	 * 卡片Cvv2不能为空
	 */
	public static final String ERRB104_CODE = "B104";
	public static final String ERRB104_MES = "卡片Cvv2不能为空";
	
	
	public static final String ERRB200_CODE = "B200";
	public static final String ERRB200_MES = "没有直属联系人姓名/直属联系人电话";
	
	/**
	 * 已存在此类型地址
	 */
	public static final String ERRB101_CODE = "B101";
	public static final String ERRB101_MES = "已存在此类型地址，不能再添加";
	/**
	 * 已存在此类型联系人
	 */
	public static final String ERRB102_CODE = "B102";
	public static final String ERRB102_MES = "已存在此类型联系人，不能再添加";
	/**
	 * 此类型地址为账单地址时不能删除
	 */
	public static final String ERRB103_CODE = "B103";
	public static final String ERRB103_MES = "此类型地址为账单地址时不能删除";
	/**
	 * 只存在一个联系人时不能删除
	 */
	public static final String ERRB105_CODE = "B105";
	public static final String ERRB105_MES = "只存在一个联系人时不能删除";
	
	
	/**
	 * 已做了挂失换卡的卡片当天或者隔天都不能再做解挂
	 */
	public static final String ERRB106_CODE = "B106";
	public static final String ERRB106_MES = "已做了挂失换卡的卡片当天或者隔天都不能再做解挂";
	
	
	
	
	
	
	/**
	 * 限笔不能为负
	 */
	public static final String ERRB107_CODE = "B107";
	public static final String ERRB107_MES = "限笔不能为空或负值";
	
	/**
	 * 卡号长度必须为16位或19位
	 */
	public static final String ERRB108_CODE="B108";
	public static final String ERRB108_MES="卡号长度有误";
	
	public static final String ERRB109_CODE="B109";
	public static final String ERRB109_MES="卡片未激活";
	
	public static final String ERRB110_CODE="B110";
	public static final String ERRB110_MES="卡片已过期";
	
	public static final String ERRB111_CODE="B111";
	public static final String ERRB111_MES="已销户";
	
	public static final String ERRB112_CODE="B112";
	public static final String ERRB112_MES="已冻结";
	
	public static final String ERRB113_CODE="B113";
	public static final String ERRB113_MES="账户已关闭";
	
	public static final String ERRB114_CODE="B114";
	public static final String ERRB114_MES="卡片已挂失";
	
	public static final String ERRB115_CODE="B115";
	public static final String ERRB115_MES="账户止付";
	
	public static final String ERRB116_CODE="B116";
	public static final String ERRB116_MES="签约时约定还款类型不能上送为N|未设置";
	
	/**
	 * 未找到地址信息
	 */
	public static final String ERRB117_CODE = "B117";
	public static final String ERRB117_MES = "未找到上送的类型地址，不能更新";
	
	public static final String ERRB118_CODE = "B118";
	public static final String ERRB118_MES = "未找到上送的类型地址，不能删除";
	
	public static final String ERRB119_CODE = "B119";
	public static final String ERRB119_MES = "地址不能为空";
	
	public static final String ERRB120_CODE = "B120";
	public static final String ERRB120_MES = "国家代码不能为空";
	
	public static final String ERRB121_CODE = "B121";
	public static final String ERRB121_MES = "电话号码不能为空";
	
	public static final String ERRB122_CODE = "B122";
	public static final String ERRB122_MES = "只存在一个地址信息不能删除";
	
	public static final String ERRB123_CODE = "B123";
	public static final String ERRB123_MES = "交易参考号不能为空";
	
	public static final String ERRB124_CODE = "B124";
	public static final String ERRB124_MES = "卡号和交易参考号查询不到当天积分兑换信息";
	
	public static final String ERRB125_CODE = "B125";
	public static final String ERRB125_MES = "当天交易参考号不能重复";
	
	public static final String ERRB126_CODE = "B126";
	public static final String ERRB126_MES = "交易参考号超长";
	
	public static final String ERRB127_CODE = "B127";
	public static final String ERRB127_MES = "兑换积分不能为空且大于0";
	
	public static final String ERRB128_CODE = "B128";
	public static final String ERRB128_MES = "临额已经失效";
	
	public static final String ERRB129_CODE = "B129";
	public static final String ERRB129_MES = "临额未设置";
	
	public static final String ERRB130_CODE = "B130";
	public static final String ERRB130_MES = "cvv2次数超限";
	
	public static final String ERRB131_CODE = "B131";
	public static final String ERRB131_MES = "用户近期有过额度调整，不能重复申请";
	
	
	//分期类L
	/**
	 * 原交易日期不能为空
	 */
	public static final String ERRL001_CODE = "L001";
	public static final String ERRL001_MES = "原交易日期不能为空";
	
	/**
	 * 原交易金额不能为空
	 */
	public static final String ERRL002_CODE = "L002";
	public static final String ERRL002_MES = "原交易金额不能为空";
	
	/**
	 * 分期总期数不能为空
	 */
	public static final String ERRL003_CODE = "L003";
	public static final String ERRL003_MES = "分期总期数不能为空";
	
	/**
	 * 分期手续费收取方式不能为空
	 */
	public static final String ERRL004_CODE = "L004";
	public static final String ERRL004_MES = "分期手续费收取方式不能为空";
	
	/**
	 * 分期申请失败：找不到原消费交易
	 */
	public static final String ERRL005_CODE = "L005";
	public static final String ERRL005_MES = "找不到原消费交易";
	
	/**
	 * 分期申请失败：非可转分期交易类型
	 */
	public static final String ERRL006_CODE = "L006";
	public static final String ERRL006_MES = "非可转分期交易类型";
	
	/**
	 * 分期申请失败：该笔交易已经转分期申请，不可重复申请
	 */
	public static final String ERRL007_CODE = "L007";
	public static final String ERRL007_MES = "该笔交易已经转分期申请，不可重复申请";
	
	/**
	 * 分期申请失败：存在不能分期的锁定码
	 */
	public static final String ERRL008_CODE = "L008";
	public static final String ERRL008_MES = "存在不能分期的锁定码";
	
	/**
	 * 分期申请失败：查询不到对应的消费计划
	 */
	public static final String ERRL009_CODE = "L009";
	public static final String ERRL009_MES = "查询不到对应的消费计划";
	
	/**
	 * 分期申请失败：分期金额小于最小可分期金额
	 */
	public static final String ERRL010_CODE = "L010";
	public static final String ERRL010_MES = "分期金额小于最小可分期金额";
	
	/**
	 * 分期申请失败：卡片已经失效
	 */
	public static final String ERRL011_CODE = "L011";
	public static final String ERRL011_MES = "卡片已经失效";
	
	/**
	 * 本金分配方式的参数为空
	 */
	public static final String ERRL012_CODE = "L012";
	public static final String ERRL012_MES = "本金分配方式的参数为空";
	
	/**
	 * 分期总本金不能为空
	 */
	public static final String ERRL013_CODE = "L013";
	public static final String ERRL013_MES = "分期总本金不能为空";
	
	/**
	 * 分期申请失败：找不到可分期的账单
	 */
	public static final String ERRL014_CODE = "L014";
	public static final String ERRL014_MES = "找不到可分期的账单";
	
	/**
	 * 分期申请失败：该笔账单已做过账单分期
	 */
	public static final String ERRL015_CODE = "L015";
	public static final String ERRL015_MES = "该笔账单已做过账单分期";
	
	/**
	 * 分期申请失败：申请日期不在规定日期之内
	 */
	public static final String ERRL016_CODE = "L016";
	public static final String ERRL016_MES = "申请日期不在规定日期之内";
	
	/**
	 * 分期申请失败：申请金额大于允许的分期比例
	 */
	public static final String ERRL017_CODE = "L017";
	public static final String ERRL017_MES = "申请金额大于允许的分期比例";
	
	/**
	 * 分期申请失败：申请金额大于最大允许分期金额
	 */
	public static final String ERRL018_CODE = "L018";
	public static final String ERRL018_MES = "申请金额大于最大允许分期金额";
	
	/**
	 * 分期申请失败：申请金额大于信用额度
	 */
	public static final String ERRL019_CODE = "L019";
	public static final String ERRL019_MES = "申请金额大于信用额度";
	
	/**
	 * 分期申请顺序号不能为空
	 */
	public static final String ERRL020_CODE = "L020";
	public static final String ERRL020_MES = "分期申请顺序号不能为空";
	
	/**
	 * 分期交易不存在
	 */
	public static final String ERRL021_CODE = "L021";
	public static final String ERRL021_MES = "分期交易不存在";
	
	/**
	 * 分期终止失败：该分期已为终止状态
	 */
	public static final String ERRL022_CODE = "L022";
	public static final String ERRL022_MES = "该分期已为终止状态";
	
	/**
	 * 分期终止撤销失败：分期不是终止状态
	 */
	public static final String ERRL023_CODE = "L023";
	public static final String ERRL023_MES = "分期不是终止状态";
	
	/**
	 * 分期终止撤销失败：分期不是持卡人发起的终止
	 */
	public static final String ERRL024_CODE = "L024";
	public static final String ERRL024_MES = "分期不是持卡人发起的终止";
	
	/**
	 * 分期终止失败：非当日分期终止
	 */
	public static final String ERRL025_CODE = "L025";
	public static final String ERRL025_MES = "非当日分期终止";
	
	public static final String ERRL026_CODE = "L026";
	public static final String ERRL026_MES = "分期不是活动状态，无法提前还款";
	
	public static final String ERRL027_COED = "L027";
	public static final String ERRL027_MES = "当日已经申请展期，不可重复申请";
	
	public static final String ERRL028_CODE = "L028";
	public static final String ERRL028_MES = "分期不为展期状态，不能展期撤销";
	
	public static final String ERRL029_CODE = "L029";
	public static final String ERRL029_MES = "分期展期不是当日申请,不能撤销";
	
	public static final String ERRL030_CODE = "L030";
	public static final String ERRL030_MES = "分期不是活动状态，无法做分期展期";
	
	public static final String ERRL031_CODE = "L031";
	public static final String ERRL031_MES = "分期已是展期状态，每笔分期只能做一次展期";
	
	public static final String ERRL032_CODE = "L032";
	public static final String ERRL032_MES = "分期展期期数应大于原分期期数";
	
	public static final String ERRL033_CODE = "L033";
	public static final String ERRL033_MES = "授权处理超时,无法做现金分期";
	
	public static final String ERRL034_CODE = "L034";
	public static final String ERRL034_MES = "授权拒绝,无法做现金分期";
	
	public static final String ERRL035_CODE = "L035";
	public static final String ERRL035_MES = "分期手续费收取方式不能为空";
	
	public static final String ERRL036_CODE = "L036";
	public static final String ERRL036_MES = "该申请已经被处理，不能撤销";
	
	public static final String ERRL037_CODE = "L037";
	public static final String ERRL037_MES = "超过最大允许的分期笔数";
	
	public static final String ERRL038_CODE = "L038";
	public static final String ERRL038_MES = "不允许多笔分期申请";
	
	public static final String ERRL039_CODE = "L039";
	public static final String ERRL039_MES = "该分期不允许提前还款";
	
	public static final String ERRL040_CODE = "L040";
	public static final String ERRL040_MES = "分期参数中不允许展期";
	
	public static final String ERRL041_CODE = "L041";
	public static final String ERRL041_MES = "申请现金分期金额大于允许分期的额度";
	
	public static final String ERRL042_CODE = "L042";
	public static final String ERRL042_MES = "非现金分期申请，不可通过此途径撤销";
	
	public static final String ERRL043_CODE = "L043";
	public static final String ERRL043_MES = "未开通约定还款，不允许做现金分期";
	
	public static final String ERRL044_CODE = "L044";
	public static final String ERRL044_MES = "该分期产品已过期，不能执行此操作";
	
	public static final String ERRL045_CODE = "L045";
	public static final String ERRL045_MES = "该分期产品非活动状态，不能执行此操作";
	
	public static final String ERRL046_CODE = "L046";
	public static final String ERRL046_MES = "该卡片对应的产品不允许分期";
	
	public static final String ERRL047_CODE = "L047";
	public static final String ERRL047_MES = "临额不能办理分期";
	
	public static final String ERRL048_CODE = "L048";
	public static final String ERRL048_MES = "该卡片最近有逾期不允许办理分期";
	
	public static final String ERRL049_CODE = "L049";
	public static final String ERRL049_MES = "无效的借据号";
	
	public static final String ERRL050_CODE = "L050";
	public static final String ERRL050_MES = "无效的贷款申请顺序号";
	
	public static final String ERRL051_CODE = "L051";
	public static final String ERRL051_MES = "分期类型不支持该渠道撤销";
	
	/**
	 * 现金分期实时放款失败
	 */
	public static final String ERRL052_CODE = "L052";
	public static final String ERRL052_MES = "现金分期实时放款失败";
	
	
	public static final String  ERRBLCA_CODE="BLCA";	//存在锁定码A
	public static final String  ERRBLCB_CODE="BLCB";	//存在锁定码B
	public static final String  ERRBLCC_CODE="BLCC";	//存在锁定码C
	public static final String  ERRBLCD_CODE="BLCD";	//存在锁定码D
	public static final String  ERRBLCE_CODE="BLCE";	//存在锁定码E
	public static final String  ERRBLCF_CODE="BLCF";	//存在锁定码F
	public static final String  ERRBLCG_CODE="BLCG";	//存在锁定码G
	public static final String  ERRBLCH_CODE="BLCH";	//存在锁定码H
	public static final String  ERRBLCI_CODE="BLCI";	//存在锁定码I
	public static final String  ERRBLCJ_CODE="BLCJ";	//存在锁定码J
	public static final String  ERRBLCK_CODE="BLCK";	//存在锁定码K
	public static final String  ERRBLCL_CODE="BLCL";	//存在锁定码L
	public static final String  ERRBLCM_CODE="BLCM";	//存在锁定码M
	public static final String  ERRBLCN_CODE="BLCN";	//存在锁定码N
	public static final String  ERRBLCO_CODE="BLCO";	//存在锁定码O
	public static final String  ERRBLCP_CODE="BLCP";	//存在锁定码P
	public static final String  ERRBLCQ_CODE="BLCQ";	//存在锁定码Q
	public static final String  ERRBLCR_CODE="BLCR";	//存在锁定码R
	public static final String  ERRBLCS_CODE="BLCS";	//存在锁定码S
	public static final String  ERRBLCT_CODE="BLCT";	//存在锁定码T
	public static final String  ERRBLCU_CODE="BLCU";	//存在锁定码U
	public static final String  ERRBLCV_CODE="BLCV";	//存在锁定码V
	public static final String  ERRBLCW_CODE="BLCW";	//存在锁定码W
	public static final String  ERRBLCX_CODE="BLCX";	//存在锁定码X
	public static final String  ERRBLCY_CODE="BLCY";	//存在锁定码Y
	public static final String  ERRBLCZ_CODE="BLCZ";	//存在锁定码Z
	public static final String  ERRBLC0_CODE="BLC0";	//存在锁定码0
	public static final String  ERRBLC1_CODE="BLC1";	//存在锁定码1
	public static final String  ERRBLC2_CODE="BLC2";	//存在锁定码2
	public static final String  ERRBLC3_CODE="BLC3";	//存在锁定码3
	public static final String  ERRBLC4_CODE="BLC4";	//存在锁定码4
	public static final String  ERRBLC5_CODE="BLC5";	//存在锁定码5
	public static final String  ERRBLC6_CODE="BLC6";	//存在锁定码6
	public static final String  ERRBLC7_CODE="BLC7";	//存在锁定码7
	public static final String  ERRBLC8_CODE="BLC8";	//存在锁定码8
	public static final String  ERRBLC9_CODE="BLC9";	//存在锁定码9
	public static final String  ERRNOAC_CODE="NAIC"	;   //未激活
	public static final String  ERRNEXP_CODE="NEXP"	;   //有效期之外
	public static final String  ERRNPIN_CODE="NPIN"	;   //无交易密码
	public static final String  ERRNQIN_CODE="NQIN"	;   //无查询密码
	public static final String  ERRLOIC_CODE="LOCQ"	;   //查询密码锁定
	public static final String  ERRLOCP_CODE="LOCP"	;   //交易密码锁定
	public static final String  ERRSUPP_CODE="SUPP"	;   //附卡

	//小额贷错误码C开头
	public static final String ERRC010_CODE="C010";
	public static final String ERRC010_MES="贷款产品编号无效或不能为空";
	
	public static final String ERRC011_CODE="C011";
	public static final String ERRC011_MES="没有对应的贷款定价产品";
	
	public static final String ERRC012_CODE="C012";
	public static final String ERRC012_MES="无贷款账户信息";
	
	public static final String ERRC013_CODE="C013";
	public static final String ERRC013_MES="无贷款产品信息";
	
	public static final String ERRC014_CODE="C014";
	public static final String ERRC014_MES="浮动比例不能为空";
	
	public static final String ERRC015_CODE="C015";
	public static final String ERRC015_MES="无还款计划";
	
	public static final String ERRC016_CODE="C016";
	public static final String ERRC016_MES="无贷款信息";

	public static final String ERRC017_CODE="C017";
	public static final String ERRC017_MES="没有找到贷款变更";
	
	public static final String ERRC018_CODE="C018";
	public static final String ERRC018_MES="无还款分配历史";
	
	public static final String ERRC019_CODE="C019";
	public static final String ERRC019_MES="展期后总期数不能为空";
	
	public static final String ERRC020_CODE="C020";
	public static final String ERRC020_MES="账单日不能做此交易";
	
	public static final String ERRC021_CODE="C021";
	public static final String ERRC021_MES="第一个账单之后才能做此交易";
	
	public static final String ERRC022_CODE="C022";
	public static final String ERRC022_MES="贷款不是活动状态，不能做此交易";
	
	public static final String ERRC023_CODE="C023";
	public static final String ERRC023_MES="展期期数应大于原期数";

	public static final String ERRC024_CODE="C024";
	public static final String ERRC024_MES="产品不支持贷款展期";
	
	public static final String ERRC025_CODE="C025";
	public static final String ERRC025_MES="没有找到贷款变更申请";
	
	public static final String ERRC026_CODE = "C026";
	public static final String ERRC026_MES = "查询不到对应账户授权信息";
	
	public static final String ERRC027_CODE="C027";
	public static final String ERRC027_MES="不支持的期数";
	
	public static final String ERRC028_CODE="C028";
	public static final String ERRC028_MES="贷款类型不支持展期、缩期或者提前还款";
	
	public static final String ERRC029_CODE="C029";
	public static final String ERRC029_MES="缩期后总期数不能为空";
	
	public static final String ERRC030_CODE="C030";
	public static final String ERRC030_MES="缩期方式不能为空";
	
	public static final String ERRC031_CODE="C031";
	public static final String ERRC031_MES="上送缩期后期数应小于原期数";
	
	public static final String ERRC032_CODE="C032";
	public static final String ERRC032_MES="缩期方式只支持T/A/S";
//	public static final String ERRC032_MES="缩期方式只支持T/A/S/P"; //TODO 追加P
	
	public static final String ERRC033_COED = "C033";
	public static final String ERRC033_MES = "当日已经申请贷款变更，不可重复申请";
	
	public static final String ERRC034_CODE="C034";
	public static final String ERRC034_MES="产品不支持贷款缩期";
	
	public static final String ERRC035_CODE="C035";
	public static final String ERRC035_MES="还款金额不能为0";
	
	public static final String ERRC036_CODE="C036";
	public static final String ERRC036_MES="贷款最后一期不支持此交易";
	
	public static final String ERRC037_CODE="C037";
	public static final String ERRC037_MES="申请顺序号不能为空";
	
	public static final String ERRC038_CODE="C038";
	public static final String ERRC038_MES="贷款变更已被处理";
	
	public static final String ERRC039_CODE="C039";
	public static final String ERRC039_MES="展期到期时间大于卡片有效期";
	
	public static final String ERRC040_CODE="C040";
	public static final String ERRC040_MES="展期到期时间大于贷款产品有效期";
	
	public static final String ERRC041_CODE="C041";
	public static final String ERRC041_MES="缩期还款金额小于最小还款金额";
	
	public static final String ERRC042_CODE="C042";
	public static final String ERRC042_MES="贷款的未还金额小于允许缩期最小金额";
	
	public static final String ERRC043_CODE="C043";
	public static final String ERRC043_MES="贷款未还款金额大于允许展期的最大金额";
	
	public static final String ERRC044_CODE="C044";
	public static final String ERRC044_MES="贷款未还款金额小于允许展期的最小金额";
	
	public static final String ERRC045_CODE="C045";
	public static final String ERRC045_MES="还款金额不能为空";
	
	public static final String ERRC046_CODE="C046";
	public static final String ERRC046_MES="还款金额与试算值不相等";
	
	public static final String ERRC047_CODE="C047";
	public static final String ERRC047_MES="剩余一期不能做此交易";
	
	public static final String ERRC048_CODE="C048";
	public static final String ERRC048_MES="上送缩期后期数应大于贷款下期期数";
	
	public static final String ERRC049_CODE="C049";
	public static final String ERRC049_MES="缩期还款金额大于贷款剩余本金";
	
	public static final String ERRC050_CODE="C050";
	public static final String ERRC050_MES="申请期数不能小于贷款最短周期";
	
	public static final String ERRC051_CODE="C051";
	public static final String ERRC051_MES="申请期数不能大于贷款最长周期";
	
	/**
	 * 查询不到对应客户信息
	 */
//	public static final String ERRQ101_CODE = "Q101";
//	public static final String ERRQ101_MES = "查询不到对应客户信息";
	

	/**
	 * 查询不到对应客户额度信息
	 */
//	public static final String ERRQ103_CODE = "Q103";
//	public static final String ERRQ103_MES = "查询不到对应客户额度信息";
	
	/**
	 * 查询不到对应账户信息
	 */
	public static final String ERRQ201_CODE = "Q201";
	public static final String ERRQ201_MES = "查询不到对应账户信息";
	
	/**
	 * 预约还款日期不符
	 */
	public static final String ERRQ202_CODE = "1021";
	public static final String ERRQ202_MES = "客户可以申请至少2天之后提前结清";
	
	/**
	 * 已经逾期不能,不能提前预约还款
	 */
	public static final String ERRQ203_CODE = "1025";
	public static final String ERRQ203_MES = "逾期预约";
	
	/**
	 * 预约还款日不能是账单日也不能夸账单日，
	 */
	public static final String ERRQ204_CODE = "1022";
	public static final String ERRQ204_MES = "不允许预约跨还款日";
	/**
	 * 预约还款试算金额失败，
	 */
	public static final String ERRQ205_CODE = "1005";
	public static final String ERRQ205_MES = "无效的金额";
	
	/**
	 * 预约还款试算金额失败，
	 */
	public static final String ERRQ206_CODE = "9998";
	public static final String ERRQ206_MES = "系统内部异常";
	
	//暂用
	//重庆百货大楼股份有限公司
	public static final String MERCHANT_ACQ_ID = "20000005";
	//马上消费金融股份有限公司
	public static final String MS_ACQ_ID = "10000000";
	//
	public static final String TX_ACQ_ID = "20000003";
}
