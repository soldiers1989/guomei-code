package com.sunline.ccs.service.auth.frame;

import static com.sunline.ccs.facility.AuthCommUtils.checkDateValid;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mysema.query.NonUniqueResultException;
import com.sunline.ark.support.service.YakMessage;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsAuthmemoO;
import com.sunline.ccs.infrastructure.shared.model.CcsCardO;
import com.sunline.ccs.infrastructure.shared.model.CcsCardSpecbizCtrl;
import com.sunline.ccs.infrastructure.shared.model.CcsCardThresholdCtrl;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomerCrlmt;
import com.sunline.ccs.otb.AcctOTB;
import com.sunline.ccs.otb.CommProvide;
import com.sunline.ccs.otb.CustOTB;
import com.sunline.ccs.param.def.AuthProduct;
import com.sunline.ccs.param.def.AuthReasonMapping;
import com.sunline.ccs.param.def.Organization;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.enums.AuthAction;
import com.sunline.ccs.param.def.enums.AuthReason;
import com.sunline.ccs.param.def.enums.AuthTransDirection;
import com.sunline.ccs.param.def.enums.AuthTransTerminal;
import com.sunline.ccs.param.def.enums.AutoType;
import com.sunline.ccs.service.auth.common.AuthCheckService;
import com.sunline.ccs.service.auth.common.AuthCommService;
import com.sunline.ccs.service.auth.common.AuthDroolService;
import com.sunline.ccs.service.auth.common.AuthLoadService;
import com.sunline.ccs.service.auth.common.AuthLoanService;
import com.sunline.ccs.service.auth.common.AuthMicroCreditCommService;
import com.sunline.ccs.service.auth.common.AuthRdsService;
//import com.sunline.ccs.service.auth.common.AuthSmsService;
import com.sunline.ccs.service.auth.context.AuthContext;
import com.sunline.ccs.service.auth.context.CupMsg;
import com.sunline.ccs.service.auth.context.RespInfo;
import com.sunline.ccs.service.auth.context.TxnInfo;
import com.sunline.ccs.service.auth.utils.AuthServiceLogUtils;
import com.sunline.pcm.param.def.Mcc;
import com.sunline.pcm.param.def.Product;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.api.AuthorizationService;
import com.sunline.ppy.api.CustomAttributesKey;
import com.sunline.ppy.api.MediumInfo;
import com.sunline.ppy.dictionary.enums.AuthTransType;
import com.sunline.ppy.dictionary.enums.InputSource;
import com.sunline.ppy.dictionary.enums.LimitCategory;
import com.sunline.ppy.dictionary.enums.ManualAuthFlag;
import com.sunline.ppy.dictionary.enums.PasswordType;
import com.sunline.ppy.dictionary.enums.PasswordVerifyResult;

/**
 * 
 * @see 类名：AuthServiceImpl
 * @see 描述：授权服务入口
 * 0、初始化对象－allResult、TxnInfo、CupMsg、AuthContext
 * 1、交易预处理，对报文初步解析，通过规则引擎给TxnInfo部分属性赋值,校验mediumInfo
 * 2、校验并获取介质卡信息
 * 3、获取检查项，并处理VIP免检项
 * 4、交叉检查
 * 5、加载参数
 * 6、常规校验
 * 7、计算otb
 * 8、特殊交易处理
 * 9、综合校验
 * 10、锁定码校验
 * 11、处理校验结构
 * 12、处理业务
 * 13、重新计算otb
 * 14、设置54和61域
 * 15、记入日志，
 * 16、更新联机账户表最近业务日期
 * 17、生成响应报文
 * 18、发短信
 * 19、反欺诈验证
 *
 * @see 创建日期：   2015年6月24日下午3:23:57
 * @author liruilin
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class AuthServiceImpl implements AuthorizationService {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private Map<String, Handler> bizProcessors;
	
	//服务
	@Autowired
	private AuthCommService authCommonService;

	@Autowired
	private AuthMicroCreditCommService authMicroCreditService;
	
	@Autowired
	private AuthLoanService authLoanService;
	
	@Autowired
	private AuthCheckService authCheckService;
	
	@Autowired
	private AuthDroolService authDroolService;
	
/*	@Autowired
	private AuthSmsService authSmsService;
*/	
//	@Autowired
//	private AuthRdsService authRdsService;
	
	@Autowired
	private AuthLoadService authLoadService;
	
	@Autowired
	private UnifiedParameterFacility unifiedParameterFacility;
	
	@Autowired
	private CustAcctCardFacility custAcctCardQueryFacility;

	// OTB
	@Autowired
	private AcctOTB accountOTB;

	@Autowired
	private CustOTB customerOTB;
	
	@Autowired
	private CommProvide commonProvide;

	
	//dao层

	
	/* (non-Javadoc)
	 * @see com.sunline.ppy.api.AuthorizationService#authorize(com.sunline.ark.support.service.YakMessage)
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public YakMessage authorize(YakMessage requestMessage) {
		logger.debug("#########授权服务Start#########");
		
		// 日志打印YakMessage的内容
		AuthServiceLogUtils.printYakMessage(requestMessage, logger);
		
		AuthServiceLogUtils.stepLog("0", "创建context对象", logger);
		// 合并所有命中规则,最后统一校验用
		Map<AuthReason, AuthAction> allResult = new HashMap<AuthReason, AuthAction>();
		
		// 建立TransInfo，存放预处理信息
		TxnInfo txnInfo = new TxnInfo();
		
		// 暂定全部是银联
		CupMsg cupMsg = new CupMsg(requestMessage);
		
		// 存放txnInfo信息，txnInfo里有responseCode的初始信息
		AuthContext context = new AuthContext();
		context.setMessage(cupMsg);//AIC2.7 银联升级
		
		// 客户属性
		Map<String, Serializable> custAttr = requestMessage.getCustomAttributes();
		// 业务日期
		Date bizDate = (Date) custAttr.get(CustomAttributesKey.BUSINESS_DATE_KEY_NAME);
		// 交易渠道
		InputSource inputSource = (InputSource) custAttr.get(CustomAttributesKey.INPUT_SOURCE);
		// 人工授权标志
		ManualAuthFlag manualAuthFlag = (ManualAuthFlag) custAttr.get(CustomAttributesKey.MANUAL_AUTH_FLAG);

		txnInfo.setBizDate(bizDate);
		txnInfo.setSysOlTime(new Date());
		txnInfo.setInputSource(inputSource);
		if (manualAuthFlag != null){
			txnInfo.setManualAuthFlag(manualAuthFlag);
		}
		
		MediumInfo mediumInfo = null;
		try {
			
			AuthServiceLogUtils.stepLog("1", "交易预处理", logger);
			// 第一步，交易预处理，负责对报文进行初步判断和分类
			authDroolService.preProcess(txnInfo, cupMsg, mediumInfo);
			// 执行完交易预处理后，如果没有找到对应的交易，抛出不支持此交易异常。
			if (txnInfo.getTransType() == null) {
				logger.debug("# 没有匹配的交易类型");
				throw new AuthException(AuthReason.TS03, AuthAction.D);
			}
			context.setTxnInfo(txnInfo);
			
			AuthServiceLogUtils.stepLog("2", "校验并获取介质卡信息", logger);
			// 交易介质 - ic
			custAttr.put(CustomAttributesKey.TRAN_MEDIUM_TYPE, txnInfo.getTransMedium());
			
			// 请求mps服务校验介质卡，并获取介质卡信息  《马上不用介质卡》
//			mediumInfo = authCheckService.checkMediumInfo(requestMessage);
			mediumInfo = new MediumInfo();
			mediumInfo.setLogicalCardNo(requestMessage.getBody(2));
			context.setMediumInfo(mediumInfo);
			
			// 设置有效期
			setExpiryDate(txnInfo, cupMsg, mediumInfo);
			
			AuthServiceLogUtils.stepLog("3", "获取检查项，并处理VIP免检项", logger);
			// 根据规则表cup-preprocess.xls取出的检查模板号,从数据库中取出检查项,送入集合中，用于后续的规则校验
			if (!StringUtils.isBlank(txnInfo.getCheckItem()))
				txnInfo.setCheckList(authLoadService.loadCheckList(txnInfo.getCheckItem(), inputSource));
			
			// 通过介质卡获取卡信息
			CcsCardO card = authLoadService.loadCard(mediumInfo);
			// 需要先检查卡表的blockcode是否是vip用，户将vip客户需要免检的项,从checklist中删除
			txnInfo.getCheckList().removeAll(authLoadService.loadVipExemptedFromInspectionCheckList(card));

			AuthServiceLogUtils.stepLog("4", "交叉检查", logger);
			// 交叉检查的fact取交易报文和交易预处理信息
			allResult.putAll(authDroolService.crossCheck(txnInfo, cupMsg));
			
			AuthServiceLogUtils.stepLog("5", "加载参数", logger);
			String productCode = card.getProductCd();
			// 卡产品贷记属性
			ProductCredit productCredit = commonProvide.retrieveProductCredit(productCode);
			// 产品
			Product product = commonProvide.getProduct(productCode);
			// 获取org层控制参数，贷记撤销或冲正时，借记卡负OTB控制，20130906
			Organization organization = commonProvide.getOrganiztion();
			CcsAcctO account = authLoadService.loadAccount(cupMsg, productCredit, txnInfo, card.getAcctNbr());
			
			// 处理预授权完成
			dealPAComp(requestMessage, txnInfo, context);
			
			// 获取无卡自助数据
			CcsCardSpecbizCtrl cardSpectxnSupportO = authLoadService.loadCardSpectxnSupportO(card);
			// 设置无卡自助开通标识、无卡自助手机号码
			if (null != cardSpectxnSupportO) {
				txnInfo.setNoCardPrzTxnSupportInd(cardSpectxnSupportO.getTxnSupportIndicator());
				txnInfo.setNoCardPrzTxnMobileNbr(cardSpectxnSupportO.getBindDeviceNbr());
			}
			
			// 客户
			CcsCustomer customer = authLoadService.loadCustomer(card.getCustId());
			// 客户自定义额度
			CcsCustomerCrlmt custLimit = authLoadService.loadCustLimit(account);
			// 客户账户列表
			Iterable<CcsAcctO> accounts = authLoadService.loadAccounts(account.getCustLmtId());
			
			// 加载卡片限额
			CcsCardThresholdCtrl cardLimitOverrideO = authLoadService.loadCardLimitOverrideO(card.getLogicCardNbr());
			setCardLimit(txnInfo, productCredit, cardLimitOverrideO);
			// 如果18域为上送，取不到Mcc对应的参数，返回null，由规则返回未匹配交易。
			Mcc mcc = authLoadService.loadMcc(cupMsg, txnInfo);
			
			context.setCard(card);
			context.setAccount(account);
			context.setCustomer(customer);
			context.setProductCredit(productCredit);
			context.setProduct(product);
			context.setAuthProduct(unifiedParameterFacility.loadParameter(productCode, AuthProduct.class));
			context.setMcc(mcc);
			context.setCustLimitO(custLimit);
			
			/** 检查是否存在密码 **/
			txnInfo.setExistPassword(authCheckService.checkSerPassword(cupMsg.field(2), mediumInfo.getPwdType()));
			
			// 设置姓名和身份验证和手机号码验证
			txnInfo.setChbNameAMVerifyResult(authCheckService.CHBNameAMVerifyResult(customer, cupMsg));
			// 61域NM用法持卡人姓名
			txnInfo.setChbNameNMVerifyResult(authCheckService.CHBNameNMVerifyResult(customer, cupMsg));
			// 行内还款收款人姓名验证结果缓存
			txnInfo.setChbReceiverNameFromBankVerifyResult(authCheckService.CHBReceiverNameFromBankVerifyResult(context, customer, cupMsg));
			// 61域NM用法收款人姓名验证
			authCheckService.CHBNameNMVerifyResult2(context, customer, cupMsg);
			
			txnInfo.setIdNbrVerifyResult(authCheckService.IdNbrVerifyResult(customer, cupMsg));
			txnInfo.setMobiNbrVerifyResult(authCheckService.MobiNbrVerifyResult(customer, cupMsg));
			txnInfo.setTransferDeditOverdrawValid(productCredit.transferDeditOverdrawValid != null ? productCredit.transferDeditOverdrawValid : false);
			txnInfo.setCreditVoidOtbCtrlInd(organization.creditVoidOtbCtrlInd != null ? organization.creditVoidOtbCtrlInd : false);
			txnInfo.setCreditReverseOtbCtrlInd(organization.creditReverseOtbCtrlInd != null ? organization.creditReverseOtbCtrlInd : false);
			txnInfo.setCrRevFloorLimit(organization.crRevFloorLimit != null ? organization.crRevFloorLimit : BigDecimal.ZERO);

			//设置判断cvv2标识  AIC2.7 银联升级
			setVerifCvv2(txnInfo, cupMsg);
			
			// 联机逻辑卡表自动更新累计额
			authCommonService.autoUpdAccumulativeCardO(context);
			// 密码正确在限值以内清空累计次数,此处必须更新，不考虑交易成功与否
			resetPasswordCount(card, productCredit, mediumInfo, txnInfo);

			// 设置国家码
			if (StringUtils.isEmpty(cupMsg.field(19))) {
				txnInfo.setCountryCd("156");
			} else {
				txnInfo.setCountryCd(cupMsg.field(19));
			}
			
			AuthServiceLogUtils.stepLog("6", "常规校验", logger);
			allResult.putAll(authDroolService.generalCheck(context));

			AuthServiceLogUtils.stepLog("7", "otb额度计算", logger);
			calcOTB(txnInfo, bizDate, card, productCredit, account);

			AuthServiceLogUtils.stepLog("8", "特殊交易处理", logger);
			// 特殊交易一：分期交易
			authLoanService.loanProcessor(context);
			// 特殊交易二：小额贷交易
			authMicroCreditService.microCreditProcessor(context);

			AuthServiceLogUtils.stepLog("9", "综合校验", logger);
			// 第8步，综合校验
			allResult.putAll(authDroolService.integratedCheck(context));
			
			AuthServiceLogUtils.stepLog("10", "blockcode校验", logger);
			// blockcode 校验放在最后，以确保安全认证不因内部锁定而跳过累加
			if (txnInfo.getTransDirection() == AuthTransDirection.Normal && txnInfo.getTransType() != AuthTransType.PAComp) {
				allResult.putAll(authCheckService.checkBlockCode(card, account, mediumInfo, txnInfo));
			}

			AuthServiceLogUtils.stepLog("11", "处理校验结果集", logger);
			if (!allResult.isEmpty()) {
				txnInfo.setReasons(allResult);
				authDroolService.checkResult(allResult, context);
			}
			
			AuthServiceLogUtils.stepLog("12", "处理业务", logger);
			String processorId = txnInfo.getProcessId();
			logger.debug("processorId =" + processorId);
			if (!bizProcessors.containsKey(processorId)) {
				logger.debug("未在交易路由中找到对应processorId" + processorId);
				throw new AuthException(AuthReason.S009, AuthAction.D);
			}

			context.setTxnInfo(txnInfo);
			AuthServiceLogUtils.logTxnInfo(txnInfo, logger);
			AuthServiceLogUtils.logCupMsg(cupMsg, logger);
			
			RespInfo responseInfo = null;
			try {
				responseInfo = bizProcessors.get(processorId).handle(requestMessage, context);
			} catch (NonUniqueResultException e) {
				// 反向交易匹配时查询到多条原记录
				logger.warn("# 交易[processorId] = " + processorId + " 反向交易匹配时查询到多条原记录");
				throw new AuthException(AuthReason.R008, AuthAction.D);
			}

			AuthServiceLogUtils.stepLog("13", "重算otb", logger);
			reCaclOTB(txnInfo, bizDate, productCredit, account, custLimit, accounts);

			AuthServiceLogUtils.stepLog("14", "设置54和61域", logger);
			if (txnInfo.getTransTerminal() == AuthTransTerminal.ATM 
					|| txnInfo.getTransTerminal() == AuthTransTerminal.OTC 
					|| txnInfo.getTransType() == AuthTransType.Inq
					|| (product.productType.getLimitCategory() == LimitCategory.MicroCreditLimit 
						&& txnInfo.getTransType() == AuthTransType.TransferDeditDepos 
						&& context.getTxnInfo().getTransDirection() == AuthTransDirection.Normal)) {
				authCommonService.setB054RespAndB061CustInfo(context, responseInfo, cupMsg);
			}

			AuthServiceLogUtils.stepLog("15", "记入日志, 保存对象", logger);
			// 记录授权日志
			authCommonService.writeAuthLog(cupMsg, context, responseInfo);

			AuthServiceLogUtils.stepLog("16", "更新联机账户表最近业务日期", logger);
			authCommonService.updateAcctODate(context, responseInfo);
			
			AuthServiceLogUtils.stepLog("17", "组装响应报文", logger);
			// 组装返回报文
			YakMessage responseMessage = authCommonService.createResponseMessage(requestMessage, context, responseInfo);
			
			AuthServiceLogUtils.stepLog("18", "发送短信", logger);
			// 发送短信
//			authSmsService.processMessage(context, responseMessage, responseInfo);
			
			// 调用反欺诈系统 
//			if (needCallRds()) {
//				AuthServiceLogUtils.stepLog("19", "反欺诈验证", logger);
//				authRdsService.callRds(context, requestMessage);<马上 不调用反欺诈>
//			}
			logger.debug("# F57 [57]" + responseMessage.getBody(57));
			logger.debug("-------- Sucess RsponseMessage [End OK] --------");
			return responseMessage;
		} catch (AuthException ae) {
			try {
				//对于cupmessag抛出异常不包含action的处理，填充默认action的处理
				if(ae.getFillDefaultAction()){
					AuthReasonMapping mapping = unifiedParameterFacility.retrieveParameterObject("CUP" + "|" + ae.getReason(), AuthReasonMapping.class);
					ae = new AuthException(ae.getReason(),mapping.defaultAction);
				}
				logger.debug("# 交易异常结束： 异常reason=" + ae.getReason() + "action=" + ae.getAction());
				/**
				 * 在规则交易抛出异常的情况下，不会执行355行密码累计清空resetPasswordCount函数；
				 * 因此在此处捕获规则异常处，需要对密码输入正确的卡，进行密码累计清空的处理。
				 */
				CcsCardO card = context.getCard();
				ProductCredit productCredit = context.getProductCredit();
				// 密码正确在限值以内清空累计次数
				if (card != null && productCredit != null)
					resetPasswordCount(card, productCredit, mediumInfo, txnInfo);

				context.getTxnInfo().setAuthReason(ae.getReason());
				context.getTxnInfo().setAuthAction(ae.getAction());
				context.getTxnInfo().setResponsCode(authCommonService.getResponseCode(ae.getReason(), ae.getAction()));
				// 记录授权日志
				authCommonService.writeAuthLog(cupMsg, context, null);
				authCommonService.updateAcctODate(context, null);
				// 组装返回报文
				// 处理异常响应
				YakMessage responseMessage = authCommonService.createResponseMessage(requestMessage, context, null);
				
				//发送短信
//				authSmsService.processReasonMsg(ae, context);
				
				// 调用反欺诈系统
//				if (needCallRds()) {
//					authRdsService.callRds(context, requestMessage);
//				}
				logger.debug("AuthException RsponseMessage [End OK]");
				return responseMessage;// response
			} catch (AuthException e) {
				// 如果这里再出错，那就没办法了，只能不响应了
				logger.error("无法响应请求", e);
				return null;
			}
		//TODO 2014-05-16：发生runtime异常时，事务回滚待优化(由TPS返回异常报文)
		} finally {
			logger.debug("#######授权服务end#######");
		}
	}

	
	
	/**
	 * 
	 * @see 方法名：reCaclOTB 
	 * @see 描述：重新计算可用额度
	 * @see 创建日期：2015年6月22日下午5:53:48
	 * @author liruilin
	 *  
	 * @param txnInfo
	 * @param bizDate
	 * @param productCredit
	 * @param account
	 * @param custLimit
	 * @param accounts
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private void reCaclOTB(TxnInfo txnInfo, Date bizDate, ProductCredit productCredit, CcsAcctO account,
			CcsCustomerCrlmt custLimit, Iterable<CcsAcctO> accounts) {
		// 账户可用额--2013-10-25：inqOTB不包含临时额度（由accountInqOTB改为accountOTB）
		txnInfo.setAccountOTB(accountOTB.acctOTB(account, productCredit, bizDate).setScale(2));
		logger.debug("# AccountOTB == " + txnInfo.getAccountOTB());
		// 取现可用额
		txnInfo.setCashOTB(accountOTB.acctCashInqOTB(account, productCredit, bizDate).setScale(2));
		logger.debug("# CashOTB == " + txnInfo.getCashOTB());
		// 账户溢缴款取现可用额度
		txnInfo.setDepositeCashOTB(accountOTB.acctDepositeCashOTB(account, productCredit, bizDate).setScale(2));
		logger.debug("# DepositeCashOTB == " + txnInfo.getDepositeCashOTB());
		// 客户可用额
		txnInfo.setCustomerOTB(customerOTB.customerInqOTB(custLimit, productCredit, accounts, bizDate).setScale(2));
		logger.debug("# CustomerOTB == " + txnInfo.getCustomerOTB());
	}

	/**
	 * 
	 * @see 方法名：计算可用额度
	 * @see 创建日期：2015年6月22日下午5:58:34
	 * @author liruilin
	 *  
	 * @param txnInfo
	 * @param bizDate
	 * @param card
	 * @param productCredit
	 * @param account
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private void calcOTB(TxnInfo txnInfo, Date bizDate, CcsCardO card, ProductCredit productCredit, CcsAcctO account) {
		// 账户可用额
		txnInfo.setAccountOTB(accountOTB.acctOTB(account, productCredit, bizDate).setScale(2));
		logger.debug("# AccountOTB == " + txnInfo.getAccountOTB());

		// 取现可用额
		txnInfo.setCashOTB(accountOTB.acctCashOTB(account, productCredit, bizDate).setScale(2));
		logger.debug("# CashOTB == " + txnInfo.getCashOTB());

		// 账户溢缴款取现可用额度
		txnInfo.setDepositeCashOTB(accountOTB.acctDepositeCashOTB(account, productCredit, bizDate).setScale(2));
		logger.debug("# DepositeCashOTB == " + txnInfo.getDepositeCashOTB());

		// 客户可用额
		txnInfo.setCustomerOTB(customerOTB.customerOTB(card.getLogicCardNbr(), bizDate).setScale(2));
		// 账户临时额度生效时，不控制客户额度
		if (account.getTempLmtBegDate() != null && account.getTempLmtEndDate() != null && bizDate.compareTo(account.getTempLmtBegDate()) < 1
				&& bizDate.compareTo(account.getTempLmtEndDate()) <= 0) {
			// 临时额度有效，当前客户额度 = 账户临时额度
			txnInfo.setCustomerOTB(txnInfo.getAccountOTB());
			logger.debug("# 账户临时额度生效时,不控制客户额度,使用账户临额覆盖客户额度");
		}
		logger.debug("# CustomerOTB == " + txnInfo.getCustomerOTB());
	}

	/**
	 * 
	 * @see 方法名：dealPAComp 
	 * @see 描述：预授权完成交易
	 * @see 创建日期：2015年6月22日下午5:07:29
	 * @author liruilin
	 *  
	 * @param requestMessage
	 * @param txnInfo
	 * @param context
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private void dealPAComp(YakMessage requestMessage, TxnInfo txnInfo, AuthContext context) {
		//如果是预授权完成撤销和预授权完成冲正交易，set原始预授权交易金额
		//预授权完成交易获取原始预授权交易金额，用于判断是否超限
		if(txnInfo.getTransType().equals(AuthTransType.PAComp)){
			CcsAuthmemoO orig;
			if(txnInfo.getTransDirection().equals(AuthTransDirection.Reversal)
					|| txnInfo.getTransDirection().equals(AuthTransDirection.Revocation)){
				orig = authCommonService.genOrgOrgTrans(requestMessage, context);
				txnInfo.setOrigOrigTransAmt(orig.getTxnAmt());
			}else if(txnInfo.getTransDirection().equals(AuthTransDirection.Normal)
					|| txnInfo.getTransDirection().equals(AuthTransDirection.Advice)){
				Iterator<CcsAuthmemoO> origs = authCommonService.findPAOriginalTrans(requestMessage, context);
				logger.debug("log键值：---------------------"+origs.next().getLogKv());
				orig = authCommonService.checkOrigListIsNotNull(origs, true);
				if (orig == null ) {
					// 对于预授权完成通知继续处理
					if (context.getTxnInfo().getTransDirection() != AuthTransDirection.Advice) {
						authCommonService.throwAuthException(AuthReason.R014, "预授权完成时，找不到原交易(25)");
					}
				}else{
					txnInfo.setOrigOrigTransAmt(orig.getTxnAmt());
				}
			}
		}
	}


	/**
	 * 
	 * @see 方法名：setExpiryDate 
	 * @see 描述：根据请求和卡信息设置有效期
	 * @see 创建日期：2015年6月22日下午4:43:33
	 * @author liruilin
	 *  
	 * @param txnInfo
	 * @param message
	 * @param mediumInfo
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private void setExpiryDate(TxnInfo txnInfo, CupMsg message, MediumInfo mediumInfo) {
		// 设置有效期
		if (StringUtils.isNotBlank(message.field(14))) {
			// 14域不为空，则取14域为有效期
			txnInfo.setExpiryDate(message.field(14));
		} else if (StringUtils.isNotBlank(message.field(35))) {
			// B014 为空且B035非空，则使用B035的17-21位替代
			txnInfo.setExpiryDate(message.getF035Track2Expiry());
		} else if (StringUtils.isNotBlank(mediumInfo.getExpiryDate())) {
			// B014 和B035 都为空时，使用MPS的数据
			txnInfo.setExpiryDate(mediumInfo.getExpiryDate());
		} else {
			// B014、B035和MPS数据为空，B014为空
			txnInfo.setExpiryDate("");
		}
	}

	/**
	 * 
	 * @see 方法名：setVerifCvv2 
	 * @see 描述：设置验证CVV2标识
	 * 银联的无卡自助的辅助/自主 或订购的消费/预授权交易，且F61.6AM标识不验cvv2，则赋值为false
	 * @see 创建日期：2015年6月22日下午6:00:58
	 * @author liruilin
	 *  
	 * @param txnInfo
	 * @param message
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private void setVerifCvv2(TxnInfo txnInfo, CupMsg message) {
		//银联 的 消费/预授权交易
		if(txnInfo.getInputSource() == InputSource.CUP 
				&& (txnInfo.getTransType() == AuthTransType.Auth || txnInfo.getTransType() == AuthTransType.PreAuth)) {
			//订购 交易
			if(txnInfo.getTransTerminal() == AuthTransTerminal.PHE || txnInfo.getTransTerminal() == AuthTransTerminal.PHT){
				//若F61.6AM标识不验cvv2
				txnInfo.setVerifCvv2(message.getF061_6_AM_Cvv2CheckFlag());
			//无卡自助的辅助/自主交易 
			} else if (txnInfo.getAutoType() == AutoType.NoCardSelfService 
					&& (txnInfo.getAuthVerifyType() == "09" || txnInfo.getAuthVerifyType() == "10")){
				//若F61.6AM标识不验cvv2
				txnInfo.setVerifCvv2(message.getF061_6_AM_Cvv2CheckFlag());
			}else {
				txnInfo.setVerifCvv2(true);
			}
		}else {
			txnInfo.setVerifCvv2(true);
		}
	}

	/**
	 * 
	 * @see 方法名：setCardLimit 
	 * @see 描述：设置卡片当日相关交易类型限额
	 * @see 创建日期：2015年6月22日下午5:14:50
	 * @author liruilin
	 *  
	 * @param txnInfo
	 * @param productCredit
	 * @param cardLimitOverrideO
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private void setCardLimit(TxnInfo txnInfo, ProductCredit productCredit, CcsCardThresholdCtrl cardLimitOverrideO) {
		logger.debug("---------setCardLimit ------ ");
		// 单日amt金额笔数限额
		txnInfo.setDayAtmLimit(productCredit.dayAtmLimit);
		txnInfo.setDayAtmNbr(productCredit.dayAtmNbr);
		// 单日取现金额笔数限额
		txnInfo.setDayCashAmtLmt(productCredit.dayCashAmtLimit);
		txnInfo.setDayCshNbrLimit(productCredit.dayCashNbrLimit);
		// 单日消费金额笔数限额
		txnInfo.setDayRetailAmtLmt(productCredit.dayRetailAmtLimit);
		txnInfo.setDayRetailNbrLmt(productCredit.dayRetailNbrLimit);
		// 单日转出金额笔数限额(2期)
		txnInfo.setDayXfroutAmtLmt(productCredit.dayXfroutAmtLimit);
		txnInfo.setDayXfroutNbrLmt(productCredit.dayXfroutNbrLimit);
		// 单日银联境外atm金额笔数限额
		txnInfo.setDayCupxbAtmLimit(productCredit.dayCupxbAtmLimit);

		// 逻辑卡限额覆盖表存在数据时，取逻辑卡参数
		if (null != cardLimitOverrideO) {
			// 单日amt金额笔数限额
			if (checkDateValid(cardLimitOverrideO.getDayAtmOvriInd(), txnInfo.getBizDate(), 
					cardLimitOverrideO.getDayAtmOvriBegDate(), cardLimitOverrideO.getDayAtmOvriEndDate())) {
				txnInfo.setDayAtmLimit(cardLimitOverrideO.getDayAtmAmtLmt());
				txnInfo.setDayAtmNbr(cardLimitOverrideO.getDayAtmNbrLmt());
			}
			// 单日取现金额笔数限额
			if (checkDateValid(cardLimitOverrideO.getDayCashOvriInd(), txnInfo.getBizDate(), 
					cardLimitOverrideO.getDayCashOvriBegDate(), cardLimitOverrideO.getDayCashOvriEndDate())) {
				txnInfo.setDayCashAmtLmt(cardLimitOverrideO.getDayCashAmtLmt());
				txnInfo.setDayCshNbrLimit(cardLimitOverrideO.getDayCashNbrLmt());
			}
			// 单日消费金额笔数限额
			if (checkDateValid(cardLimitOverrideO.getDayRetlOvriInd(), txnInfo.getBizDate(), 
					cardLimitOverrideO.getDayRetlOvriBegDate(), cardLimitOverrideO.getDayRetlOvriEndDate())) {
				txnInfo.setDayRetailAmtLmt(cardLimitOverrideO.getDayRetailAmtLmt());
				txnInfo.setDayRetailNbrLmt(cardLimitOverrideO.getDayRetailNbrLmt());
			}
			// 单日转出金额笔数限额(2期)
			if (checkDateValid(cardLimitOverrideO.getDayXfroutOvriInd(), txnInfo.getBizDate(), 
					cardLimitOverrideO.getDayXfroutOvriBegDate(), cardLimitOverrideO.getDayXfroutOvriEndDate())) {
				txnInfo.setDayXfroutAmtLmt(cardLimitOverrideO.getDayXfroutAmtLmt());
				txnInfo.setDayXfroutNbrLmt(cardLimitOverrideO.getDayXfroutNbrLmt());
			}
			// 单日银联境外atm金额笔数限额
			if (checkDateValid(cardLimitOverrideO.getDayCupxbAtmOvriInd(), txnInfo.getBizDate(), 
					cardLimitOverrideO.getDayCupxbAtmOvriBegDate(), cardLimitOverrideO.getDayCupxbAtmOvriEndDate())) {
				txnInfo.setDayCupxbAtmLimit(cardLimitOverrideO.getDayCupxbAtmAmtLmt());
			}
		}
	}

	/**
	 * 
	 * @see 方法名：resetPasswordCount 
	 * @see 描述：密码正确在限值以内清空累计次数
	 * @see 创建日期：2015年6月22日下午5:21:48
	 * @author liruilin
	 *  
	 * @param card
	 * @param productCredit
	 * @param mediumInfo
	 * @param txnInfo
	 * @throws AuthException
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public void resetPasswordCount(CcsCardO card, ProductCredit productCredit, MediumInfo mediumInfo, TxnInfo txnInfo) throws AuthException {
		logger.debug("---------resetPasswordCount ------ ");
		// 支付密码正确在限值以内清空累计次数
		if (productCredit.pinTry > card.getPinTries() && mediumInfo.getPasswordVerifyResult() == PasswordVerifyResult.Approve && mediumInfo.getPwdType() == PasswordType.P) {
			card.setPinTries(0);
			card.setLastUpdateBizDate(txnInfo.getBizDate());
		}

		// 查询密码正确在限值以内清空累计次数
		if (productCredit.maxInqPinTry > card.getInqPinTries() && mediumInfo.getPasswordVerifyResult() == PasswordVerifyResult.Approve && mediumInfo.getPwdType() == PasswordType.Q) {
			card.setInqPinTries(0);
			card.setLastUpdateBizDate(txnInfo.getBizDate());
		}

	}
	
	
	/**
	 * 
	 * @see 方法名：needCallRds 
	 * @see 描述：判断本机构是否需要调用反欺诈
	 * @see 创建日期：2015年6月22日下午6:01:35
	 * @author liruilin
	 *  
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private boolean needCallRds() {
		Organization org = unifiedParameterFacility.retrieveParameterObject(null, Organization.class);
		return org.needRds;
	}

}
