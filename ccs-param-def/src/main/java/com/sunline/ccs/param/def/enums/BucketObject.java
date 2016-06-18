package com.sunline.ccs.param.def.enums;

import com.sunline.ppy.dictionary.enums.BucketType;
import com.sunline.ark.support.meta.EnumInfo;

/**
 * Plan中所有余额成份类型
* @author fanghj
 *
 */
/**
 * @author lin
 *
 */
@EnumInfo({
	"ctdPrincipal|本账期本金",
	"ctdInterest|本账期利息",
	"ctdCardFee|本账期年费",
	"ctdOvrlmtFee|本账期超限费",
	"ctdLpc|本账期滞纳金",
	"ctdNsfFee|本账期资金不足罚金",
	"ctdSvcFee|本账期服务费",
	"ctdTxnFee|本账期交易费",
	"ctdIns|本账期保险",
	"ctdLifeInsuFee|本账期寿险计划包费",
	"ctdStampduty|本账期印花税",
	"ctdMulct|本账期罚金",
	"ctdPenalty|本期罚息",
	"ctdCompound|本期复利",
	"ctdReplaceSvcFee|当期代收付服务费",
	"ctdUserFee1|本账期自定义费用1",
	"ctdUserFee2|本账期自定义费用2",
	"ctdUserFee3|本账期自定义费用3",
	"ctdUserFee4|本账期自定义费用4",
	"ctdUserFee5|本账期自定义费用5",
	"ctdUserFee6|本账期自定义费用6",
	"pastPrincipal|已出账单本金",
	"pastInterest|已出账单利息",
	"pastCardFee|已出账单年费",
	"pastOvrlmtFee|已出账单超限费",
	"pastLpc|已出账单滞纳金",
	"pastNsfFee|已出账单资金不足罚金",
	"pastTxnFee|已出账单交易费",
	"pastSvcFee|已出账单服务费",
	"pastIns|已出账单保险",
	"pastLifeInsuFee|已出账单寿险计划包费",
	"pastStampduty|已出账单印花税",
	"pastMulct|已出账单罚金",
	"pastPenalty|已出账单罚息",
	"pastCompound|已出账单复利",
	"pastReplaceSvcFee|往期代收付服务费",
	"pastUserFee1|已出账单自定义费用1",
	"pastUserFee2|已出账单自定义费用2",
	"pastUserFee3|已出账单自定义费用3",
	"pastUserFee4|已出账单自定义费用4",
	"pastUserFee5|已出账单自定义费用5",
	"pastUserFee6|已出账单自定义费用6",
	"pastReplacePenalty|已出账单代收罚息",
	"pastReplaceMulct|已出账单代收罚金",
	"pastReplaceLpc|已出账单代收滞纳金",
	"pastReplaceTxnFee|已出账单代收交易费",
	"ctdReplacePenalty|未出账单代收罚息",
	"ctdReplaceMulct|未出账单代收罚金",
	"ctdReplaceLpc|未出账单代收滞纳金",
	"ctdReplaceTxnFee|未出账单代收交易费",
	"ctdPrepayPkg|未出账单灵活还款计划包",
	"pastPrepayPkg|已出账单灵活还款计划包"
	
})

public enum BucketObject {
	/**
	 * 本账期本金
	 */
	ctdPrincipal(BucketType.Pricinpal),
	/**
	 * 本账期利息
	 */
	ctdInterest(BucketType.Interest),
	/**
	 * 本账期年费
	 */
	ctdCardFee(BucketType.CardFee),
	/**
	 * 本账期超限费
	 */
	ctdOvrlmtFee(BucketType.OverLimitFee),
	/**
	 * 本账期滞纳金
	 */
	ctdLpc(BucketType.LatePaymentCharge),
	/**
	 * 本账期资金不足罚金
	 */
	ctdNsfFee(BucketType.NSFCharge),
	/**
	 * 本账期服务费
	 */
	ctdSvcFee(BucketType.SVCFee),
	/**
	 * 本账期交易费
	 */
	ctdTxnFee(BucketType.TXNFee),
	/**
	 * 本账期保险
	 */
	ctdIns(BucketType.InsuranceFee),
	/**
	 * 本账期印花税
	 */
	ctdStampduty(BucketType.StampDuty),
	/**
	 * 本账期寿险计划包费
	 */
	ctdLifeInsuFee(BucketType.LifeInsuFee),
	/**
	 * 本账期罚金
	 */
	ctdMulct(BucketType.Mulct),
	/**
	 * 本期罚息
	 */
	ctdPenalty(BucketType.Interest),
	/**
	 * 本期复利
	 */
	ctdCompound(BucketType.Interest),
	/**
	 * 当期代收付服务费
	 */
	ctdReplaceSvcFee(BucketType.ReplaceSvcFee),
	/**
	 * 本账期自定义费用1
	 */
	ctdUserFee1(BucketType.UserFee1),
	/**
	 * 本账期自定义费用2
	 */
	ctdUserFee2(BucketType.UserFee2),
	/**
	 * 本账期自定义费用3
	 */
	ctdUserFee3(BucketType.UserFee3),
	/**
	 * 本账期自定义费用4
	 */
	ctdUserFee4(BucketType.UserFee4),
	/**
	 * 本账期自定义费用5
	 */
	ctdUserFee5(BucketType.UserFee5),
	/**
	 * 本账期自定义费用6
	 */
	ctdUserFee6(BucketType.UserFee6),
	/**
	 * 已出账单本金
	 */
	pastPrincipal(BucketType.Pricinpal),
	/**
	 * 已出账单利息
	 */
	pastInterest(BucketType.Interest),
	/**
	 * 已出账单年费
	 */
	pastCardFee(BucketType.CardFee),
	/**
	 * 已出账单超限费
	 */
	pastOvrlmtFee(BucketType.OverLimitFee),
	/**
	 * 已出账单滞纳金
	 */
	pastLpc(BucketType.LatePaymentCharge),
	/**
	 * 已出账单资金不足罚金
	 */
	pastNsfFee(BucketType.NSFCharge),
	/**
	 * 已出账单交易费
	 */
	pastTxnFee(BucketType.TXNFee),
	/**
	 * 已出账单服务费
	 */
	pastSvcFee(BucketType.SVCFee),
	/**
	 * 已出账单保险
	 */
	pastIns(BucketType.InsuranceFee),
	/**
	 * 已出账单印花税
	 */
	pastStampduty(BucketType.StampDuty),
	/**
	 * 已出账单寿险计划包费
	 */
	pastLifeInsuFee(BucketType.LifeInsuFee),
	/**
	 * 已出账单罚金
	 */
	pastMulct(BucketType.Mulct),
	/**
	 * 已出账单罚息
	 */
	pastPenalty(BucketType.Interest),
	/**
	 * 往期代收付服务费
	 */
	pastReplaceSvcFee(BucketType.ReplaceSvcFee),
	/**
	 * 已出账单复利
	 */
	pastCompound(BucketType.Interest),
	/**
	 * 已出账单自定义费用1
	 */
	pastUserFee1(BucketType.UserFee1),
	/**
	 * 已出账单自定义费用2
	 */
	pastUserFee2(BucketType.UserFee2),
	/**
	 * 已出账单自定义费用3
	 */
	pastUserFee3(BucketType.UserFee3),
	/**
	 * 已出账单自定义费用4
	 */
	pastUserFee4(BucketType.UserFee4),
	/**
	 * 已出账单自定义费用5
	 */
	pastUserFee5(BucketType.UserFee5),
	/**
	 * 已出账单自定义费用6
	 */
	pastUserFee6(BucketType.UserFee6),
	
	//兜底产品
	
	/**
	 * 已出账单代收罚息
	 */
	pastReplacePenalty(BucketType.ReplacePenalty),
	/**
	 * 未出账单代收罚息
	 */
	ctdReplacePenalty(BucketType.ReplacePenalty),
	/**
	 * 已出账单代收罚金
	 */
	pastReplaceMulct(BucketType.ReplaceMulct),
	/**
	 * 未出账单代收罚金
	 */
	ctdReplaceMulct(BucketType.ReplaceMulct),
	/**
	 * 已出账单代收滞纳金
	 */
	pastReplaceLpc(BucketType.ReplaceLatePaymentCharge),
	/**
	 * 未出账单代收滞纳金
	 */
	ctdReplaceLpc(BucketType.ReplaceLatePaymentCharge),
	/**
	 * 已出账单代收交易费
	 */
	pastReplaceTxnFee(BucketType.ReplaceTxnFee),
	/**
	 * 未出账单代收交易费
	 */
	ctdReplaceTxnFee(BucketType.ReplaceTxnFee),
	/**
	 * 未出账单灵活还款计划包
	 */
	ctdPrepayPkg(BucketType.PrepayPkg),
	/**
	 * 已出账单灵活还款计划包
	 */
	pastPrepayPkg(BucketType.PrepayPkg)
	;
	
	private BucketType bucketType;
	
	private BucketObject(BucketType bucketType) {
		this.bucketType = bucketType;
	}

	public BucketType getBucketType() {
		return bucketType;
	}

	public void setBucketType(BucketType bucketType) {
		this.bucketType = bucketType;
	}
}
