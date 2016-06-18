package com.sunline.ccs.service.auth.utils;

import org.slf4j.Logger;

import com.sunline.ark.support.service.YakMessage;
import com.sunline.ccs.service.auth.context.CupMsg;
import com.sunline.ccs.service.auth.context.TxnInfo;
import com.sunline.ppy.api.CustomAttributesKey;

/**
 * 
 * @see 类名：AuthLogUtils
 * @see 描述：AuthServiceImpl中对象日志输出
 *
 * @see 创建日期：   2015年6月25日上午10:58:18
 * @author liruilin
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class AuthServiceLogUtils {
	
	/**
	 * 日志打印yakMessage的内容
	 * 
	 * @param message
	 */
	public static void printYakMessage(YakMessage message, Logger logger) {
		logger.debug("inputsouse  ------------------ " + message.getCustomAttributes().get(CustomAttributesKey.INPUT_SOURCE));
		logger.debug("bizdate  --------------------- " + message.getCustomAttributes().get(CustomAttributesKey.BUSINESS_DATE_KEY_NAME));
		logger.debug("mti -------------------------- " + message.getCustomAttributes().get(CustomAttributesKey.MTI));
		for (int i = 0; i <= 128; i++) {
			if (message.getBody(i) != null) {
				if(i==35||i==2||i==61||i==36 || i==55 || i==102 || i==103 || i==48 || i==45 || i==57){ //#5112: PCI版本CCS日志处理遗漏问题
					logger.debug("message[field(" + String.format("%03d", i) + ")] ---------- " + message.getBody(i));
				}else{
					logger.info("message[field(" + String.format("%03d", i) + ")] ---------- " + message.getBody(i));
				}
			}
		}
	}
	
	public static void logCupMsg(CupMsg message, Logger logger){
		logger.debug("# 输出message");
		logger.debug("# F48 [ASIA]:" + message.f48IAVerify());
		logger.debug("# 61.4 [61.4]" + message.getF061_4_2_Cvv2());
		logger.debug("# 61.6_NM用法姓名验证[F61_6_NM_ChbName1]: " + message.getF061_6_NM_ChbName1());
		logger.debug("# 61.6_AM用法[CVV2]:" + message.getF061_6_AM_Cvv2CheckFlag());
	}
	
	public static void logTxnInfo(TxnInfo txnInfo, Logger logger) {
		if(logger.isDebugEnabled()){
			logger.debug("# 输出txnInfo");
			
			logger.debug("# 交易分类[processId] ---------- " + txnInfo.getProcessId());
			logger.debug("# 联机业务处理日期[bizDate] ------ " + txnInfo.getBizDate());
			logger.debug("# 接入卡组织[InputSource] ------- " + txnInfo.getInputSource());
			logger.debug("# 交易终端[transTerminal] ------- " + txnInfo.getTransTerminal());
			logger.debug("# 交易类型 [transType] ----------- " + txnInfo.getTransType());
			logger.debug("# 交易方向[transDirection] ------ " + txnInfo.getTransDirection());
			logger.debug("# 交易密码输入能力[pinEntryMode] - " + txnInfo.getPinEntryMode());
			logger.debug("# 自助类型 [autoType] ------------ " + txnInfo.getAutoType());
			logger.debug("# 交易介质[transMedium] --------- " + txnInfo.getTransMedium());
			logger.debug("# 交易发起方式[TransFrom] -------- " + txnInfo.getTransFrom());
			logger.debug("# 是否电子类交易[electronTrans] -- " + txnInfo.isElectronTrans());
			logger.debug("# 是否远程交易[remoteTrans] ------ " + txnInfo.isRemoteTrans());
			logger.debug("# 是否境外免验[abroadNoVerify] --- " + txnInfo.isAbroadNoVerify());
			logger.debug("# 是否银联境外交易[cupXborder] --- " + txnInfo.isCupXborder());
			logger.debug("# 是否网上交易[InternetTrans] ---- " + txnInfo.isInternetTrans());
			logger.debug("# 密码强制验证标识[mustCheckPwd] - " + txnInfo.isMustCheckPwd());
			logger.debug("# 是否无卡自助开通[SpecTranNoCardSelfSvc] ---- " + txnInfo.isSpecTranNoCardSelfSvc());
			logger.debug("# 原始交易金额[transAmt] = " + txnInfo.getTransAmt());
			logger.debug("# 原始交易币种[transCurr] = " + txnInfo.getTransCurr());
			logger.debug("# 入账交易金额[chbTransAmt] = " + txnInfo.getChbTransAmt());
			logger.debug("# 入账交易币种[chbCurr] = " + txnInfo.getChbCurr());
			

			logger.debug("# 61.6_AM用法姓名验证[ChbNameAMVerifyResult]: " + txnInfo.getChbNameAMVerifyResult());
			logger.debug("# 61.6_NM用法姓名验证[ChbNameNMVerifyResult]: " + txnInfo.getChbNameNMVerifyResult());

			logger.debug("# 61.4 [61.4]:" + txnInfo.isVerifCvv2());
		}
	}

	public static void stepLog(String stepNum, String stepDesc, Logger logger) {
		logger.debug("######### 第{}步, {} ##########", stepNum, stepDesc);
	}
}
