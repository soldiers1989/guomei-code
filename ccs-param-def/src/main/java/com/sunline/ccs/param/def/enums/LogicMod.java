package com.sunline.ccs.param.def.enums;

import com.sunline.ppy.dictionary.enums.BucketType;
import com.sunline.ppy.dictionary.enums.DbCrInd;
import com.sunline.ark.support.meta.EnumInfo;

/**
 * 入账逻辑模块
 */
@EnumInfo({
	"L01|01：消费",
	"L02|02：退货",
	"L03|03：本金借记（停用）",
	"L04|04：本金贷记（停用）",
	"L05|05：利息入账",
	"L06|06：利息退还",
	"L07|07：服务费入账",
	"L08|08：服务费退还",
	"L09|09：滞纳金入账",
	"L10|10：滞纳金退还",
	"L11|11：年费入账",
	"L12|12：年费退还",
	"L13|13：超限费入账",
	"L14|14：超限费退还",
	"L15|15：交易费入账",
	"L16|16：交易费退还",
	"L23|23：本金强制借记（停用）",
	"L24|24：本金强制贷记（停用）",
	"L26|26：账单分期本金贷记（停用）",
	"L30|30：还款",
	"L31|31：还款撤销",
	"L32|指定借据还款",
	"L51|51：自定义费1借记",
	"L52|52：自定义费1贷记",
	"L61|61：积分增加",
	"L62|62：积分减少",
	"L63|63：积分兑换",
	"L80|80：争议解决(有利于公司，收费)",
	"L96|96：争议提出",
	"L97|97：争议解决(有利于持卡人)",
	"L98|98：争议解决(有利于公司，不收费)",
	"L99|99：备忘交易",
	"A03|03：本金借记调整",
	"A04|04：本金贷记调整",
	"A05|05：利息借记调整",
	"A06|06：利息贷记调整",
	"A07|07：服务费借记调整",
	"A08|08：服务费贷记调整",
	"A09|09：滞纳金借记调整",
	"A10|10：滞纳金贷记调整",
	"A11|11：年费借记调整",
	"A12|12：年费贷记调整",
	"A13|13：超限费借记调整",
	"A14|14：超限费贷记调整",
	"A15|15：交易费借记调整",
	"A16|16：交易费贷记调整",
	"A23|23：本金强制借记调整",
	"A24|24：本金强制贷记调整",
	"A26|26：账单分期本金贷记调整",
	"A51|51：自定义费1借记调整",
	"A52|52：自定义费1贷记调整",
	"L27|27：罚息利息入账",
	"A72|72：罚息利息贷调",
	"L29|29：复利利息入账",
	"A92|92：复利利息贷调",
	"L40|40：罚金入账",
	"L41|41：罚金回溯",
	"L42|42：保险费入账",
	"L43|43：保费费贷调",
	"L44|44：印花税入账",
	"L45|45：寿险计划包费入账",
	"L46|46：寿险计划包费贷调",
	"L47|47：代收服务费入账",
	"L48|48：代收服务费贷调",
	"L49|49：趸交费入账",
	"L53|53：代收罚息入账",
	"L54|54：代收罚息退还",
	"L55|55：代收罚金入账",
	"L56|56：代收罚金贷调",
	"L57|57：代收滞纳金入账",
	"L58|58：代收滞纳金贷调",
	"L59|59: 代收手续费入账",
	"L60|60: 代收手续费贷调",
	"L64|64：灵活还款计划包入账",
	"L65|65：灵活还款计划包回溯",
	"L67|67：优惠券抵扣利息"
})
public enum LogicMod {
	/**
	 * 消费
	 * 借记交易
	 */
	L01(DbCrInd.D, BucketType.Pricinpal), 
	/**
	 * 退货
	 * 贷记交易
	 */
	L02(DbCrInd.C, BucketType.Pricinpal), 
	L03(DbCrInd.D,BucketType.Pricinpal),
	L04(DbCrInd.C,BucketType.Pricinpal),
	/**
	 * 利息入账
	 * 借记交易
	 */
	L05(DbCrInd.D, BucketType.Interest), 
	/**
	 * 利息退还
	 * 贷记交易
	 */
	L06(DbCrInd.C, BucketType.Interest), 
	/**
	 * 服务费入账
	 * 借记交易
	 */
	L07(DbCrInd.D, BucketType.SVCFee), 
	/**
	 * 服务费退还
	 * 贷记交易
	 */
	L08(DbCrInd.C, BucketType.SVCFee), 
	/**
	 * 滞纳金入账
	 * 借记交易
	 */
	L09(DbCrInd.D, BucketType.LatePaymentCharge), 
	/**
	 * 滞纳金退还
	 * 贷记交易
	 */
	L10(DbCrInd.C, BucketType.LatePaymentCharge), 
	/**
	 * 年费入账
	 * 借记交易
	 */
	L11(DbCrInd.D, BucketType.CardFee), 
	/**
	 * 年费退还
	 * 贷记交易
	 */
	L12(DbCrInd.C, BucketType.CardFee), 
	/**
	 * 超限费入账
	 * 借记交易
	 */
	L13(DbCrInd.D, BucketType.OverLimitFee), 
	/**
	 * 超限费退还
	 * 贷记交易
	 */
	L14(DbCrInd.C, BucketType.OverLimitFee), 
	/**
	 * 交易费入账
	 * 借记交易
	 */
	L15(DbCrInd.D, BucketType.TXNFee), 
	/**
	 * 交易费退还
	 * 贷记交易
	 */
	L16(DbCrInd.C, BucketType.TXNFee), 
	/**
	 * 还款
	 * 贷记交易
	 */
	L30(DbCrInd.C, BucketType.Pricinpal), 
	/**
	 * 还款撤销
	 * 借记交易
	 */
	L31(DbCrInd.D, BucketType.Pricinpal), 
	/**
	 * 指定借据号还款
	 * 贷记交易
	 */
	L32(DbCrInd.C, BucketType.Pricinpal), 
	/**
	 * 自定义费1借记
	 * 借记交易
	 */
	L51(DbCrInd.D, BucketType.UserFee1), 
	/**
	 * 自定义费1贷记
	 * 贷记交易
	 */
	L52(DbCrInd.C, BucketType.UserFee1), 
	/**
	 * 积分增加
	 * 贷记交易
	 */
	L61(DbCrInd.C, BucketType.Pricinpal), 
	/**
	 * 积分减少
	 * 借记交易
	 */
	L62(DbCrInd.D, BucketType.Pricinpal), 
	/**
	 * 积分兑换
	 * 借记交易
	 */
	L63(DbCrInd.D, BucketType.Pricinpal), 
	/**
	 * 争议解决(有利于公司，收费)
	 * Memo交易
	 */
	L80(DbCrInd.M, BucketType.Pricinpal),
	/**
	 * 争议提出
	 * Memo交易
	 */
	L96(DbCrInd.M, BucketType.Pricinpal), 
	/**
	 * 争议解决(有利于持卡人)
	 * 贷记交易
	 */
	L97(DbCrInd.C, BucketType.Pricinpal), 
	/**
	 * 争议解决(有利于公司，不收费)
	 * Memo交易
	 */
	L98(DbCrInd.M, BucketType.Pricinpal),
	/**
	 * 备忘交易
	 * Memo交易
	 */
	L99(DbCrInd.M, BucketType.Pricinpal),
	/**
	 * 本金借记调整
	 * 借记交易
	 */
	A03(DbCrInd.D, BucketType.Pricinpal), 
	/**
	 * 本金贷记调整
	 * 贷记交易
	 */
	A04(DbCrInd.C, BucketType.Pricinpal), 
	/**
	 * 利息借记调整
	 * 借记交易
	 */
	A05(DbCrInd.D, BucketType.Interest), 
	/**
	 * 利息贷记调整
	 * 贷记交易
	 */
	A06(DbCrInd.C, BucketType.Interest), 
	/**
	 * 服务费借记调整
	 * 借记交易
	 */
	A07(DbCrInd.D, BucketType.SVCFee), 
	/**
	 * 服务费贷记调整
	 * 贷记交易
	 */
	A08(DbCrInd.C, BucketType.SVCFee), 
	/**
	 * 滞纳金借记调整
	 * 借记交易
	 */
	A09(DbCrInd.D, BucketType.LatePaymentCharge), 
	/**
	 * 滞纳金贷记调整
	 * 贷记交易
	 */
	A10(DbCrInd.C, BucketType.LatePaymentCharge), 
	/**
	 * 年费借记调整
	 * 借记交易
	 */
	A11(DbCrInd.D, BucketType.CardFee), 
	/**
	 * 年费贷记调整
	 * 贷记交易
	 */
	A12(DbCrInd.C, BucketType.CardFee), 
	/**
	 * 超限费借记调整
	 * 借记交易
	 */
	A13(DbCrInd.D, BucketType.OverLimitFee), 
	/**
	 * 超限费贷记调整
	 * 贷记交易
	 */
	A14(DbCrInd.C, BucketType.OverLimitFee), 
	/**
	 * 交易费借记调整
	 * 借记交易
	 */
	A15(DbCrInd.D, BucketType.TXNFee), 
	/**
	 * 交易费贷记调整
	 * 贷记交易
	 */
	A16(DbCrInd.C, BucketType.TXNFee), 
	/**
	 * 本金强制借记调整
	 * 借记交易
	 */
	A23(DbCrInd.D, BucketType.Pricinpal), 
	/**
	 * 本金强制贷记调整
	 * 贷记交易
	 */
	A24(DbCrInd.C, BucketType.Pricinpal), 
	/**
	 * 账单分期本金贷记调整
	 * 贷记交易
	 */
	A26(DbCrInd.C, BucketType.Pricinpal),
	
	/**
	 * 本金强制借记调整
	 * 借记交易
	 */
	L23(DbCrInd.D, BucketType.Pricinpal), 
	/**
	 * 本金强制贷记调整
	 * 贷记交易
	 */
	L24(DbCrInd.C, BucketType.Pricinpal), 
	/**
	 * 账单分期本金贷记调整
	 * 贷记交易
	 */
	L26(DbCrInd.C, BucketType.Pricinpal),
	/**
	 * 自定义费1借记调整
	 * 借记交易
	 */
	A51(DbCrInd.D, BucketType.UserFee1), 
	/**
	 * 自定义费1贷记调整
	 * 贷记交易
	 */
	A52(DbCrInd.C, BucketType.UserFee1),
	/**
	 * 罚息利息入账
	 * 借记交易
	 */
	L27(DbCrInd.D, BucketType.Interest),
	/**
	 * 罚息利息贷调
	 * 贷记交易
	 */
	A72(DbCrInd.C, BucketType.Interest),
	/**
	 * 复利利息入账
	 * 贷记交易
	 */
	L29(DbCrInd.D, BucketType.Interest),
	/**
	 * 复利利息贷调
	 * 贷记交易
	 */
	A92(DbCrInd.C, BucketType.Interest),
	/**
	 * 罚金入账
	 * 借记交易
	 */
	L40(DbCrInd.D, BucketType.Mulct),
	/**
	 * 罚金贷调
	 * 贷记交易
	 */
	L41(DbCrInd.C, BucketType.Mulct),
	/**
	 * 保险费入账
	 * 借记交易
	 */
	L42(DbCrInd.D, BucketType.InsuranceFee),
	/**
	 * 保险费贷调
	 * 贷记交易
	 */
	L43(DbCrInd.C, BucketType.InsuranceFee),
	/**
	 * 印花税入账
	 * 借记交易
	 */
	L44(DbCrInd.D, BucketType.StampDuty),
	/**
	 * 寿险计划包费入账
	 * 借记交易
	 */
	L45(DbCrInd.D, BucketType.LifeInsuFee),
	/**
	 * 寿险计划包费贷调
	 * 借记交易
	 */
	L46(DbCrInd.C, BucketType.LifeInsuFee),
	
	/**
	 * 代收服务入账
	 * 借记交易
	 */
	L47(DbCrInd.D, BucketType.ReplaceSvcFee),
	/**
	 * 代收服务贷调
	 * 借记交易
	 */
	L48(DbCrInd.C, BucketType.ReplaceSvcFee),
	/**
	 * 趸交费入账
	 * Memo交易
	 */
	L49(DbCrInd.M,BucketType.Pricinpal),
	/**
	 * 代收罚息入账
	 * 借记交易
	 */
	L53(DbCrInd.D,BucketType.ReplacePenalty),
	/**
	 * 代收罚息退还
	 * 贷记交易
	 */
	L54(DbCrInd.C,BucketType.ReplacePenalty),
	/**
	 * 代收罚金入账
	 * 借记交易
	 */
	L55(DbCrInd.D,BucketType.ReplaceMulct),
	/**
	 * 代收罚金贷调
	 * 贷记交易
	 */
	L56(DbCrInd.C,BucketType.ReplaceMulct),
	/**
	 * 代收滞纳金入账
	 * 借记交易
	 */
	L57(DbCrInd.D,BucketType.ReplaceLatePaymentCharge),
	/**
	 * 代收滞纳金贷调
	 * 贷记交易
	 */
	L58(DbCrInd.C,BucketType.ReplaceLatePaymentCharge),
	/**
	 * 代收交易费入账
	 * 借记交易
	 */
	L59(DbCrInd.D,BucketType.ReplaceTxnFee),
	/**
	 * 代收交易费贷调
	 * 贷记交易
	 */
	L60(DbCrInd.C,BucketType.ReplaceTxnFee),
	/**
	 * 灵活还款计划包入账
	 * 借记交易
	 */
	L64(DbCrInd.D,BucketType.PrepayPkg),
	/**
	 * 灵活还款计划包贷调
	 * 贷记交易
	 */
	L65(DbCrInd.C,BucketType.PrepayPkg),
	/**
	 * 优惠券抵扣利息
	 * 贷记交易
	 */
	L67(DbCrInd.C,BucketType.Interest);
	
	private DbCrInd dbCrInd;
	private BucketType bucketType;
	
	public DbCrInd getDbCrInd() {
		return dbCrInd;
	}
	
	public BucketType getBucketType() {
		return bucketType;
	}

	private LogicMod(DbCrInd dbCrInd, BucketType bucketType){
		this.dbCrInd = dbCrInd;
		this.bucketType = bucketType;
	}
}
