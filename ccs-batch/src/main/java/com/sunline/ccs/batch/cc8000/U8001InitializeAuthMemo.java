package com.sunline.ccs.batch.cc8000;

import java.io.Serializable;
import java.util.Map;

import com.sunline.ccs.param.def.enums.AuthAction;
import com.sunline.ccs.param.def.enums.AuthReason;
import com.sunline.ccs.param.def.enums.AuthTransDirection;
import com.sunline.kylin.web.ark.client.utils.DataTypeUtils;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.AuthTransStatus;
import com.sunline.ppy.dictionary.enums.AuthTransType;
import com.sunline.ppy.dictionary.enums.BlackWhiteCode;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.InputSource;
import com.sunline.ppy.dictionary.enums.ManualAuthFlag;
import com.sunline.ppy.dictionary.enums.PasswordType;
import com.sunline.ppy.dictionary.enums.PasswordVerifyResult;
import com.sunline.ppy.dictionary.enums.TrackVerifyResult;

/**
 * @see 类名：U8001InitializeAuthMemo
 * @see 描述：TODO 中文描述
 *
 * @see 创建日期：   2015-6-24下午2:24:48
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class U8001InitializeAuthMemo {
	/**
	 * 输出对象赋值
	 * 
	 * @param map
	 * @return
	 */
	public static U8001AuthMemo setItemFromMap(Map<String, Serializable> map) {
		U8001AuthMemo umitem = new U8001AuthMemo();
		if (map.containsKey("org"))
			umitem.org = DataTypeUtils.getStringValue(map.get("org"));
		if (map.containsKey("logKv"))
			umitem.logKv = DataTypeUtils.getIntegerValue(map.get("logKv"));
		if (map.containsKey("acctNo"))
			umitem.acctNo = DataTypeUtils.getLongValue(map.get("acctNo"));
		if (map.containsKey("acctType"))
			umitem.acctType = DataTypeUtils.getEnumValue(map.get("acctType"), AccountType.class);
		if (map.containsKey("logicCardNbr"))
			umitem.logicCardNbr = DataTypeUtils.getStringValue(map.get("logicCardNbr"));
		if (map.containsKey("acqRefNo"))
			umitem.acqRefNo = DataTypeUtils.getIntegerValue(map.get("acqRefNo"));
		if (map.containsKey("txnAmt"))
			umitem.txnAmt = DataTypeUtils.getBigDecimalValue(map.get("txnAmt"));
		if (map.containsKey("txnCurrCd"))
			umitem.txnCurrCd = DataTypeUtils.getStringValue(map.get("txnCurrCd"));
		if (map.containsKey("authCode"))
			umitem.authCode = DataTypeUtils.getStringValue(map.get("authCode"));
		if (map.containsKey("acqNameAddr"))
			umitem.acqNameAddr = DataTypeUtils.getStringValue(map.get("acqNameAddr"));
		if (map.containsKey("chbTxnAmt"))
			umitem.chbTxnAmt = DataTypeUtils.getBigDecimalValue(map.get("chbTxnAmt"));
		if (map.containsKey("chbCurrCd"))
			umitem.chbCurrCd = DataTypeUtils.getStringValue(map.get("chbCurrCd"));
		if (map.containsKey("channel"))
			umitem.channel = DataTypeUtils.getEnumValue(map.get("channel"), InputSource.class);
		if (map.containsKey("mcc"))
			umitem.mcc = DataTypeUtils.getStringValue(map.get("mcc"));
		if (map.containsKey("acqBranchId"))
			umitem.acqBranchId = DataTypeUtils.getStringValue(map.get("acqBranchId"));
		if (map.containsKey("fwdInstId"))
			umitem.fwdInstId = DataTypeUtils.getStringValue(map.get("fwdInstId"));
		if (map.containsKey("transmissionTimestamp"))
			umitem.transmissionTimestamp = DataTypeUtils.getStringValue(map.get("transmissionTimestamp"));
		if (map.containsKey("settleDate"))
			umitem.settleDate = DataTypeUtils.getStringValue(map.get("settleDate"));
		if (map.containsKey("txnDirection"))
			umitem.txnDirection = DataTypeUtils.getEnumValue(map.get("txnDirection"), AuthTransDirection.class);
		if (map.containsKey("txnStatus"))
			umitem.txnStatus = DataTypeUtils.getEnumValue(map.get("txnStatus"), AuthTransStatus.class);
		if (map.containsKey("txnType"))
			umitem.txnType = DataTypeUtils.getEnumValue(map.get("txnType"), AuthTransType.class);
		if (map.containsKey("logOlTime"))
			umitem.logOlTime = DataTypeUtils.getDateValue(map.get("logOlTime"));
		if (map.containsKey("logBizDate"))
			umitem.logBizDate = DataTypeUtils.getDateValue(map.get("logBizDate"));
		if (map.containsKey("mti"))
			umitem.mti = DataTypeUtils.getStringValue(map.get("mti"));
		if (map.containsKey("origTxnType"))
			umitem.origTxnType = DataTypeUtils.getEnumValue(map.get("origTxnType"), AuthTransType.class);
		if (map.containsKey("origFwdInstId"))
			umitem.origFwdInstId = DataTypeUtils.getStringValue(map.get("origFwdInstId"));
		if (map.containsKey("origAcqInstId"))
			umitem.origAcqInstId = DataTypeUtils.getStringValue(map.get("origAcqInstId"));
		if (map.containsKey("origTxnMti"))
			umitem.origTxnMti = DataTypeUtils.getStringValue(map.get("origTxnMti"));
		if (map.containsKey("origTransDate"))
			umitem.origTransDate = DataTypeUtils.getDateValue(map.get("origTransDate"));
		if (map.containsKey("origTraceNo"))
			umitem.origTraceNo = DataTypeUtils.getIntegerValue(map.get("origTraceNo"));
		if (map.containsKey("origTxnProc"))
			umitem.origTxnProc = DataTypeUtils.getStringValue(map.get("origTxnProc"));
		if (map.containsKey("origTxnAmt"))
			umitem.origTxnAmt = DataTypeUtils.getBigDecimalValue(map.get("origTxnAmt"));
		if (map.containsKey("origLogKv"))
			umitem.origLogKv = DataTypeUtils.getIntegerValue(map.get("origLogKv"));
		if (map.containsKey("origTxnVal1"))
			umitem.origTxnVal1 = DataTypeUtils.getStringValue(map.get("origTxnVal1"));
		if (map.containsKey("origTxnVal2"))
			umitem.origTxnVal2 = DataTypeUtils.getStringValue(map.get("origTxnVal2"));
		if (map.containsKey("origChbTxnAmt"))
			umitem.origChbTxnAmt = DataTypeUtils.getBigDecimalValue(map.get("origChbTxnAmt"));
		if (map.containsKey("origBizDate"))
			umitem.origBizDate = DataTypeUtils.getDateValue(map.get("origBizDate"));
		if (map.containsKey("lastReversalDate"))
			umitem.lastReversalDate = DataTypeUtils.getDateValue(map.get("lastReversalDate"));
		if (map.containsKey("voidCount"))
			umitem.voidCount = DataTypeUtils.getIntegerValue(map.get("voidCount"));
		if (map.containsKey("manualAuthFlag"))
			umitem.manualAuthFlag = DataTypeUtils.getEnumValue(map.get("manualAuthFlag"), ManualAuthFlag.class);
		if (map.containsKey("operaId"))
			umitem.operaId = DataTypeUtils.getStringValue(map.get("operaId"));
		if (map.containsKey("brand"))
			umitem.brand = DataTypeUtils.getStringValue(map.get("brand"));
		if (map.containsKey("productCd"))
			umitem.productCd = DataTypeUtils.getStringValue(map.get("productCd"));
		if (map.containsKey("mccType"))
			umitem.mccType = DataTypeUtils.getStringValue(map.get("mccType"));
		if (map.containsKey("finalReason"))
			umitem.finalReason = DataTypeUtils.getEnumValue(map.get("finalReason"), AuthReason.class);
		if (map.containsKey("finalAction"))
			umitem.finalAction = DataTypeUtils.getEnumValue(map.get("finalAction"), AuthAction.class);
		if (map.containsKey("compAmt"))
			umitem.compAmt = DataTypeUtils.getBigDecimalValue(map.get("compAmt"));
		if (map.containsKey("finalUpdDirection"))
			umitem.finalUpdDirection = DataTypeUtils.getStringValue(map.get("finalUpdDirection"));
		if (map.containsKey("finalUpdAmt"))
			umitem.finalUpdAmt = DataTypeUtils.getBigDecimalValue(map.get("finalUpdAmt"));
		if (map.containsKey("icInd"))
			umitem.icInd = DataTypeUtils.getEnumValue(map.get("icInd"), Indicator.class);
		if (map.containsKey("the3dsecureType"))
			umitem.the3dsecureType = DataTypeUtils.getEnumValue(map.get("the3dsecureType"), Indicator.class);
		if (map.containsKey("vipStatus"))
			umitem.vipStatus = DataTypeUtils.getEnumValue(map.get("vipStatus"), Indicator.class);
		if (map.containsKey("currBal"))
			umitem.currBal = DataTypeUtils.getBigDecimalValue(map.get("currBal"));
		if (map.containsKey("cashAmt"))
			umitem.cashAmt = DataTypeUtils.getBigDecimalValue(map.get("cashAmt"));
		if (map.containsKey("otb"))
			umitem.otb = DataTypeUtils.getBigDecimalValue(map.get("otb"));
		if (map.containsKey("cashOtb"))
			umitem.cashOtb = DataTypeUtils.getBigDecimalValue(map.get("cashOtb"));
		if (map.containsKey("custOtb"))
			umitem.custOtb = DataTypeUtils.getBigDecimalValue(map.get("custOtb"));
		if (map.containsKey("cardBlackFlag"))
			umitem.cardBlackFlag = DataTypeUtils.getEnumValue(map.get("cardBlackFlag"), BlackWhiteCode.class);
		if (map.containsKey("merchantBlackFlag"))
			umitem.merchantBlackFlag = DataTypeUtils.getEnumValue(map.get("merchantBlackFlag"), BlackWhiteCode.class);
		if (map.containsKey("expireDate"))
			umitem.expireDate = DataTypeUtils.getStringValue(map.get("expireDate"));
		if (map.containsKey("trackOneResult"))
			umitem.trackOneResult = DataTypeUtils.getEnumValue(map.get("trackOneResult"), TrackVerifyResult.class);
		if (map.containsKey("trackTwoResult"))
			umitem.trackTwoResult = DataTypeUtils.getEnumValue(map.get("trackTwoResult"), TrackVerifyResult.class);
		if (map.containsKey("trackThreeResult"))
			umitem.trackThreeResult = DataTypeUtils.getEnumValue(map.get("trackThreeResult"), TrackVerifyResult.class);
		if (map.containsKey("pwdType"))
			umitem.pwdType = DataTypeUtils.getEnumValue(map.get("pwdType"), PasswordType.class);
		if (map.containsKey("checkPwdResult"))
			umitem.checkPwdResult = DataTypeUtils.getEnumValue(map.get("checkPwdResult"), PasswordVerifyResult.class);
		if (map.containsKey("payPwdErrNum"))
			umitem.payPwdErrNum = DataTypeUtils.getIntegerValue(map.get("payPwdErrNum"));
		if (map.containsKey("checkCvvResult"))
			umitem.checkCvvResult = DataTypeUtils.getEnumValue(map.get("checkCvvResult"), PasswordVerifyResult.class);
		if (map.containsKey("checkCvv2Result"))
			umitem.checkCvv2Result = DataTypeUtils.getEnumValue(map.get("checkCvv2Result"), PasswordVerifyResult.class);
		if (map.containsKey("checkIcvnResult"))
			umitem.checkIcvnResult = DataTypeUtils.getEnumValue(map.get("checkIcvnResult"), PasswordVerifyResult.class);
		if (map.containsKey("checkArqcResult"))
			umitem.checkArqcResult = DataTypeUtils.getEnumValue(map.get("checkArqcResult"), PasswordVerifyResult.class);
		if (map.containsKey("checkAtcResult"))
			umitem.checkAtcResult = DataTypeUtils.getEnumValue(map.get("checkAtcResult"), PasswordVerifyResult.class);
		if (map.containsKey("checkCvrResult"))
			umitem.checkCvrResult = DataTypeUtils.getEnumValue(map.get("checkCvrResult"), PasswordVerifyResult.class);
		if (map.containsKey("checkTvrResult"))
			umitem.checkTvrResult = DataTypeUtils.getEnumValue(map.get("checkTvrResult"), PasswordVerifyResult.class);
		if (map.containsKey("rejReason"))
			umitem.rejReason = DataTypeUtils.getStringValue(map.get("rejReason"));
		if (map.containsKey("unmatchCr"))
			umitem.unmatchCr = DataTypeUtils.getBigDecimalValue(map.get("unmatchCr"));
		if (map.containsKey("unmatchDb"))
			umitem.unmatchDb = DataTypeUtils.getBigDecimalValue(map.get("unmatchDb"));
		if (map.containsKey("b002"))
			umitem.b002 = DataTypeUtils.getStringValue(map.get("b002"));
		if (map.containsKey("b003"))
			umitem.b003 = DataTypeUtils.getStringValue(map.get("b003"));
		if (map.containsKey("b007"))
			umitem.b007 = DataTypeUtils.getStringValue(map.get("b007"));
		if (map.containsKey("b011"))
			umitem.b011 = DataTypeUtils.getStringValue(map.get("b011"));
		if (map.containsKey("b022"))
			umitem.b022 = DataTypeUtils.getStringValue(map.get("b022"));
		if (map.containsKey("b025"))
			umitem.b025 = DataTypeUtils.getStringValue(map.get("b025"));
		if (map.containsKey("b032"))
			umitem.b032 = DataTypeUtils.getStringValue(map.get("b032"));
		if (map.containsKey("b033"))
			umitem.b033 = DataTypeUtils.getStringValue(map.get("b033"));
		if (map.containsKey("b039"))
			umitem.b039 = DataTypeUtils.getStringValue(map.get("b039"));
		if (map.containsKey("b042"))
			umitem.b042 = DataTypeUtils.getStringValue(map.get("b042"));
		if (map.containsKey("b060"))
			umitem.b060 = DataTypeUtils.getStringValue(map.get("b060"));
		if (map.containsKey("b061"))
			umitem.b061 = DataTypeUtils.getStringValue(map.get("b061"));
		if (map.containsKey("b090"))
			umitem.b090 = DataTypeUtils.getStringValue(map.get("b090"));
		if (map.containsKey("operaTermId"))
			umitem.operaTermId = DataTypeUtils.getStringValue(map.get("operaTermId"));
		if (map.containsKey("jpaVersion"))
			umitem.jpaVersion = DataTypeUtils.getIntegerValue(map.get("jpaVersion"));
		if (map.containsKey("b004"))
			umitem.b004 = DataTypeUtils.getBigDecimalValue(map.get("b004"));
		if (map.containsKey("b006"))
			umitem.b006 = DataTypeUtils.getBigDecimalValue(map.get("b006"));
		if (map.containsKey("b049"))
			umitem.b049 = DataTypeUtils.getStringValue(map.get("b049"));
		if (map.containsKey("b051"))
			umitem.b051 = DataTypeUtils.getStringValue(map.get("b051"));
		if (map.containsKey("b037"))
			umitem.b037 = DataTypeUtils.getStringValue(map.get("b037"));
		if (map.containsKey("b028"))
			umitem.b028 = DataTypeUtils.getBigDecimalValue(map.get("b028"));
		if (map.containsKey("b048"))
			umitem.b048 = DataTypeUtils.getStringValue(map.get("b048"));
		if (map.containsKey("b054"))
			umitem.b054 = DataTypeUtils.getStringValue(map.get("b054"));
		if (map.containsKey("accountBlockCode"))
			umitem.accountBlockCode = DataTypeUtils.getStringValue(map.get("accountBlockCode"));
		if (map.containsKey("cardBlockCode"))
			umitem.cardBlockCode = DataTypeUtils.getStringValue(map.get("cardBlockCode"));
		if (map.containsKey("mediumBlockCode"))
			umitem.mediumBlockCode = DataTypeUtils.getStringValue(map.get("mediumBlockCode"));
		return umitem;
	}
}
