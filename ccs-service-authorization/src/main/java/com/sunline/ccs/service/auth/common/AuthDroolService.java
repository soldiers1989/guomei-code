package com.sunline.ccs.service.auth.common;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.drools.KnowledgeBase;
import org.drools.logger.KnowledgeRuntimeLogger;
import org.drools.logger.KnowledgeRuntimeLoggerFactory;
import org.drools.runtime.StatelessKnowledgeSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.sunline.ccs.otb.CommProvide;
import com.sunline.ccs.param.def.AuthMccStateCurrXVerify;
import com.sunline.ccs.param.def.AuthReasonMapping;
import com.sunline.ccs.param.def.CountryCtrl;
import com.sunline.ccs.param.def.CurrencyCtrl;
import com.sunline.ccs.param.def.MccCtrl;
import com.sunline.ccs.param.def.MerchantTxnCrtl;
import com.sunline.ccs.param.def.enums.AuthAction;
import com.sunline.ccs.param.def.enums.AuthReason;
import com.sunline.ccs.service.auth.context.AuthContext;
import com.sunline.ccs.service.auth.context.CupMsg;
import com.sunline.ccs.service.auth.context.TxnInfo;
import com.sunline.ccs.service.auth.frame.AuthException;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.api.MediumInfo;
import com.sunline.ppy.dictionary.enums.InputSource;

/**
 * 
 * @see 类名：AuthDroolService
 * @see 描述：授权规则引擎相关服务
 *
 * @see 创建日期：   2015年6月25日上午11:26:49
 * @author liruilin
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class AuthDroolService {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	// 服务
	@Autowired
	private UnifiedParameterFacility unifiedParameterFacility;
	
	@Autowired
	private CommProvide commonProvide;

	@Autowired
	private AuthCommService authCommonService;
	
	// 规则
	@Value("#{env['drools.reason.tracer']}")
	private String reasonTracerFile;
	
	@Resource
	private KnowledgeBase cupPreProcessRule;

	@Resource
	private KnowledgeBase bankPreProcessRule;
	
	@Resource
	private KnowledgeBase thirdpartyPreProcessRule;

	@Resource
	private KnowledgeBase crossVerficationRule;

	@Resource
	private KnowledgeBase generalVerficationRule;

	@Resource
	private KnowledgeBase reasonRule;
	
	/**
	 * 
	 * @see 方法名：step1PreProcess 
	 * @see 描述：第一步，交易预处理，负责对报文进行初步判断和分类
	 * @see 创建日期：2015年6月22日下午2:59:56
	 * @author liruilin
	 *  
	 * @param txnInfo 交易信息
	 * @param message 请求信息
	 * @param mediumInfo 介质卡信息
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public void preProcess(TxnInfo txnInfo, CupMsg message, MediumInfo mediumInfo) {
		StatelessKnowledgeSession preProcessSession = null;
		switch (txnInfo.getInputSource()) {
			case CUP:
				preProcessSession = cupPreProcessRule.newStatelessKnowledgeSession();
				break;
			case BANK:
				preProcessSession = bankPreProcessRule.newStatelessKnowledgeSession();
				break;
			case THIR:
				preProcessSession = thirdpartyPreProcessRule.newStatelessKnowledgeSession();
			default:
				break;
		}
		preProcessSession.setGlobal("txnInfo", txnInfo);
		// 消息报文作为fact
		preProcessSession.execute(Arrays.asList(message, mediumInfo, txnInfo));
	}
	
	/**
	 * 
	 * @see 方法名：crossVerfication 
	 * @see 描述：交叉验证
	 * @see 创建日期：2015年6月22日下午5:58:28
	 * @author liruilin
	 *  
	 * @param txnInfo
	 * @param message
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public Map<AuthReason, AuthAction> crossCheck(TxnInfo txnInfo, CupMsg message) {
		StatelessKnowledgeSession crossVerficationSession = crossVerficationRule.newStatelessKnowledgeSession();
		Map<AuthReason, AuthAction> crossResult = new HashMap<AuthReason, AuthAction>();
		crossVerficationSession.setGlobal("result", crossResult);
		crossVerficationSession.execute(Arrays.asList(message, txnInfo, logger));
		return crossResult;
	}
	
	/**
	 * 
	 * @see 方法名：generalVerfication 
	 * @see 描述：卡管结果验证
	 * @see 创建日期：2015年6月22日下午5:58:24
	 * @author liruilin
	 *  
	 * @param context
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public Map<AuthReason, AuthAction> generalCheck(AuthContext context) {
		StatelessKnowledgeSession generalVerficationSession = generalVerficationRule.newStatelessKnowledgeSession();
		Map<AuthReason, AuthAction> generalResult = new HashMap<AuthReason, AuthAction>();
		generalVerficationSession.setGlobal("result", generalResult);
		generalVerficationSession.execute(Arrays.asList(context.getMessage(), context.getMediumInfo(), context.getTxnInfo(), context.getAuthProduct(), logger));
		return generalResult;
	}
	
	/**
	 * 
	 * @see 方法名：integratedCheck 
	 * @see 描述：综合验证
	 * @see 创建日期：2015年6月25日下午12:02:18
	 * @author liruilin
	 *  
	 * @param context
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public Map<AuthReason, AuthAction> integratedCheck(AuthContext context) {
		// 规则引擎日志对象
		KnowledgeRuntimeLogger reasonTracer = null;
		try{
			TxnInfo txnInfo = context.getTxnInfo();
			CupMsg cupMsg = context.getMessage();
			
			/** 在获取不到如下4类参数，交易正常通过 **/
			CountryCtrl countryCtrl = unifiedParameterFacility.retrieveParameterObject(txnInfo.getCountryCd(), CountryCtrl.class);
			
			// 授权系统Mcc控制表在未取得参数的情况下，默认没有控制；
			MccCtrl mccCtrl = unifiedParameterFacility.retrieveParameterObject(MccCtrl.assemblingMccCtrlKey(cupMsg.field(18), txnInfo.getInputSource()), MccCtrl.class);
			
			// 授权系统币种控制表在未取得参数的情况下，默认没有控制；
			CurrencyCtrl currencyCtrl = unifiedParameterFacility.retrieveParameterObject(txnInfo.getTransCurr(), CurrencyCtrl.class);
			
			// 获取币种控制，当未获取到时，兑换汇率=1，即表示本币
			txnInfo.setConversionRt(currencyCtrl == null ? BigDecimal.ONE : currencyCtrl.conversionRt);
			// 授权系统MCC交叉控制控制表在未取得参数的情况下，默认没有控制；
			AuthMccStateCurrXVerify authMccStateCurrXVerity = unifiedParameterFacility.retrieveParameterObject(
					
			AuthMccStateCurrXVerify.assemblingKey(txnInfo.getInputSource(), cupMsg.field(18), cupMsg.field(19), txnInfo.getTransCurr()), AuthMccStateCurrXVerify.class);
			
			logger.debug("-----------获取authMccStateCurrXVerity参数----------------");
			logger.debug("{},{},{},{},{}", authMccStateCurrXVerity, txnInfo.getInputSource(), cupMsg.field(18), cupMsg.field(19), txnInfo.getTransCurr());
			// 商户控制表在未取得参数的情况下，默认没有商户控制；
			MerchantTxnCrtl merchantTxnCrtl = unifiedParameterFacility.retrieveParameterObject(cupMsg.field(42), MerchantTxnCrtl.class);
			logger.debug("-----------卡管结果验证------------------");
			
			StatelessKnowledgeSession reasonSession = reasonRule.newStatelessKnowledgeSession();
	//		String reasonTracerFile = System.getProperty("drools.reason.tracer");
			if (StringUtils.isNotBlank(reasonTracerFile))
				reasonTracer = KnowledgeRuntimeLoggerFactory.newFileLogger(reasonSession, reasonTracerFile);
	
			// 这一步通过global返回一组结果
			Map<AuthReason, AuthAction> reasonResult = new HashMap<AuthReason, AuthAction>();
			reasonSession.setGlobal("result", reasonResult);
	
			reasonSession.execute(Arrays.asList(cupMsg, context.getMediumInfo(), // 请求报文
					txnInfo, // 中间结果
					context.getLoanInfo(), // 分期信息
					context.getCard(), context.getAccount(), context.getCustomer(), // 卡、账、客数据
					context.getProductCredit(), context.getAuthProduct(), 
					countryCtrl, mccCtrl, currencyCtrl, 
					authMccStateCurrXVerity, merchantTxnCrtl, commonProvide.getOrganiztion(), // 业务参数
					logger
					));
			return reasonResult;
		} finally {
			if (reasonTracer != null)
				reasonTracer.close();
		}
	}
	
	/**
	 * 
	 * @see 方法名：checkResult 
	 * @see 描述：确认规则引擎结果集，并把未通过的作为异常抛出
	 * @see 创建日期：2015年6月22日下午6:02:22
	 * @author liruilin
	 *  
	 * @param result
	 * @param context
	 * @throws AuthException
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public void checkResult(final Map<AuthReason, AuthAction> result, AuthContext context) throws AuthException {
		logger.debug("进入规则校验结果Reason排序CheckResult");
		List<AuthReasonMapping> mappings = new ArrayList<AuthReasonMapping>();
		// 取出命中的原因
		for (Entry<AuthReason, AuthAction> reasonEntry : result.entrySet()) {
			logger.debug("命中原因[{}]，对应行为[{}]", reasonEntry.getKey(), reasonEntry.getValue());
			// 银联和行内都用CUP的reasonmapping
			AuthReasonMapping mapping = unifiedParameterFacility.loadParameter(InputSource.CUP + "|" + reasonEntry.getKey(), AuthReasonMapping.class);
			if (reasonEntry.getValue() == null) {
				result.put(reasonEntry.getKey(), mapping.defaultAction);
			}
			mappings.add(mapping);
		}

		if (!mappings.isEmpty()) {
			// 按优先级排序
			Collections.sort(mappings, new Comparator<AuthReasonMapping>() {
				@Override
				public int compare(AuthReasonMapping o1, AuthReasonMapping o2) {
					// 这里是排序逻辑， 先按ADPC排序，再按优先级排列
					String a1;
					String a2;
					// 如果reason是null，则取reason默认的action

					a1 = result.get(o1.reason).getActionFlag();

					a2 = result.get(o2.reason).getActionFlag();

					if (a1.compareTo(a2) != 0)
						return a1.compareTo(a2);
					return o2.reasonCodeRank.compareTo(o1.reasonCodeRank);
				}
			});

			for (AuthReasonMapping rm : mappings) {
				logger.debug("排序后reasoncd =" + rm.reason + "优先级= " + rm.reasonCodeRank);
			}
			AuthReasonMapping resultMapping = mappings.get(0);
			AuthReason returnReason = resultMapping.reason;
			AuthAction returnAction = result.get(returnReason);
			// 如果是A，不需要拋異常
			if (returnAction != AuthAction.A) {
				throw new AuthException(returnReason, returnAction);
			} else if (returnReason != AuthReason.A000) {
				context.getTxnInfo().setAuthReason(returnReason);
				context.getTxnInfo().setAuthAction(returnAction);
				context.getTxnInfo().setResponsCode(authCommonService.getResponseCode(returnReason, returnAction));
			}
		}
	}
}
