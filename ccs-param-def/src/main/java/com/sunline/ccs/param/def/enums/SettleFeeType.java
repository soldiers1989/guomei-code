package com.sunline.ccs.param.def.enums;

import com.sunline.ark.support.meta.EnumInfo;

@EnumInfo({
	"CollectionPremiumFee|代收趸交费",
	"CollectionSvcFee|代收服务费",
	"CollectionPenaltyInt|代收罚息",
	"CollectionMulctAmt|代收罚金",
	"CollectionLateFee|代收滞纳金",
	"CollePrepayTxnFee|代收提前结清手续费（违约金）",
	"CompensateAmt|代偿款",
	"ClaimAmt|理赔款",
	"PrepayHesitationAmt|犹豫期内提前结清款"

})
public enum SettleFeeType {
	/**
	 * 代收趸交费 
	 */
	CollectionPremiumFee,
	/**
	 * 代收服务费 
	 */
	CollectionSvcFee,
	/**
	 * 代收罚息
	 */
	CollectionPenaltyInt,
	/**
	 * 代收罚金
	 */
	CollectionMulctAmt,
	/**
	 * 代收滞纳金
	 */
	CollectionLateFee,
	/**
	 * 代收提前结清手续费（违约金）
	 */
	CollePrepayTxnFee,
	/**
	 * 代偿款
	 */
	CompensateAmt,
	/**
	 * 理赔款
	 */
	ClaimAmt,
	/**
	 * 犹豫期内提前结清款
	 */
	PrepayHesitationAmt

}
