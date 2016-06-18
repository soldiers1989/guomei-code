package com.sunline.ccs.service.auth.common;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sunline.ccs.infrastructure.server.repos.RCcsAcctO;
import com.sunline.ccs.infrastructure.server.repos.RCcsCardO;
import com.sunline.ccs.infrastructure.server.repos.RCcsCardSpecbizCtrl;
import com.sunline.ccs.infrastructure.server.repos.RCcsCardThresholdCtrl;
import com.sunline.ccs.infrastructure.server.repos.RCcsCustomer;
import com.sunline.ccs.infrastructure.server.repos.RCcsCustomerCrlmt;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctOKey;
import com.sunline.ccs.infrastructure.shared.model.CcsCardO;
import com.sunline.ccs.infrastructure.shared.model.CcsCardSpecbizCtrl;
import com.sunline.ccs.infrastructure.shared.model.CcsCardSpecbizCtrlKey;
import com.sunline.ccs.infrastructure.shared.model.CcsCardThresholdCtrl;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomerCrlmt;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcctO;
import com.sunline.ccs.otb.CommProvide;
import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.ccs.param.def.AuthTemplateCode;
import com.sunline.ccs.param.def.AuthTxnCode;
import com.sunline.ccs.param.def.AuthVipChecklistTemplate;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.enums.AuthAction;
import com.sunline.ccs.param.def.enums.AuthCheckPoint;
import com.sunline.ccs.param.def.enums.AuthReason;
import com.sunline.ccs.param.def.enums.AuthTransDirection;
import com.sunline.ccs.param.def.enums.SpecTxnType;
import com.sunline.ccs.service.auth.context.CupMsg;
import com.sunline.ccs.service.auth.context.TxnInfo;
import com.sunline.ccs.service.auth.frame.AuthException;
import com.sunline.pcm.param.def.CurrencyCd;
import com.sunline.pcm.param.def.Mcc;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.api.MediumInfo;
import com.sunline.ppy.dictionary.enums.AuthTransType;
import com.sunline.ppy.dictionary.enums.InputSource;

/**
 * 
 * @see 类名：AuthLoadService
 * @see 描述：授权加载对象服务
 *
 * @see 创建日期：   2015年6月26日上午10:16:14
 * @author liruilin
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class AuthLoadService {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	// 服务
	@Autowired
	private UnifiedParameterFacility unifiedParameterFacility;
	
	@Autowired
	private CommProvide commonProvide;
	
	// dao
	@Autowired
	private RCcsCardO rCcsCardO;
	
	@Autowired
	private RCcsAcctO rCcsAcctO;

	@Autowired
	private RCcsCustomer rCcsCustomer;

	@Autowired
	private RCcsCardThresholdCtrl rCcsCardThresholdCtrl;
	
	@Autowired
	private RCcsCustomerCrlmt rCcsCustomerCrlmt;

	@Autowired
	private RCcsCardSpecbizCtrl rCcsCardSpecbizCtrl;

	QCcsAcctO qAcctO = QCcsAcctO.ccsAcctO;
	
	/**
	 * 
	 * @see 方法名：loadCheckList 
	 * @see 描述：取授权交易检查列表
	 * @see 创建日期：2015年6月22日下午4:44:35
	 * @author liruilin
	 *  
	 * @param txnCode
	 * @param inputSource
	 * @return
	 * @throws AuthException
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public List<String> loadCheckList(String txnCode, InputSource inputSource) throws AuthException {
		List<String> checkList = new ArrayList<String>();
		// 根据交易码去数据库取模板码
		AuthTxnCode authTxnCode = unifiedParameterFacility.loadParameter(AuthTxnCode.assemblingKey(txnCode, inputSource), AuthTxnCode.class);
		logger.debug("---------交易模板:{},{},{}", authTxnCode.txnCode, authTxnCode.templateCode, authTxnCode.description);
		// 根据模板码去数据库取检查项集合
		AuthTemplateCode authTemplateCode = unifiedParameterFacility.loadParameter(authTxnCode.templateCode, AuthTemplateCode.class);

		if (authTemplateCode.authCheckPointEnabled != null) {
			// 取出参数集合,将需要的检查项加入到列表中;
			Set<Map.Entry<AuthCheckPoint, Boolean>> entryseSet = authTemplateCode.authCheckPointEnabled.entrySet();
			for (Map.Entry<AuthCheckPoint, Boolean> entry : entryseSet) {
				if (entry.getValue() == Boolean.TRUE)
					checkList.add(entry.getKey().toString());
			}
		}
		return checkList;
	}
	
	/**
	 * 
	 * @see 方法名：loadCard 
	 * @see 描述：加载逻辑卡信息
	 * @see 创建日期：2015年6月22日下午6:02:55
	 * @author liruilin
	 *  
	 * @param mediumInfo
	 * @return
	 * @throws AuthException
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public CcsCardO loadCard(MediumInfo mediumInfo) throws AuthException {
		logger.debug("---------loadCard -----------" + mediumInfo.getLogicalCardNo());
		CcsCardO card = rCcsCardO.findOne(mediumInfo.getLogicalCardNo());
		if (card == null) {
			logger.debug("未找到卡片信息");
			throw new AuthException(AuthReason.R002, AuthAction.D);
		}
		return card;
	}
	
	/**
	 * 
	 * @see 方法名：loadVipExemptedFromInspectionCheckList 
	 * @see 描述：获取VIP需要免验的检查列表
	 * @see 创建日期：2015年6月22日下午4:51:08
	 * @author liruilin
	 *  
	 * @param card
	 * @return
	 * @throws AuthException
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public List<String> loadVipExemptedFromInspectionCheckList(CcsCardO card) throws AuthException {

		List<String> unCheckList = new ArrayList<String>();
		/**
		 * 获取卡片层BlockCode
		 */
		if (StringUtils.isNotEmpty(card.getBlockCode()) && card.getBlockCode().indexOf("V") > -1) {
			AuthVipChecklistTemplate authVipTemplate = unifiedParameterFacility.loadParameter("V", AuthVipChecklistTemplate.class);
			if (authVipTemplate.authCheckPointEnabled != null) {
				Set<Map.Entry<AuthCheckPoint, Boolean>> entryseSet = authVipTemplate.authCheckPointEnabled.entrySet();
				for (Map.Entry<AuthCheckPoint, Boolean> entry : entryseSet) {
					if (entry.getValue() == Boolean.TRUE)
						unCheckList.add(entry.getKey().toString());
				}
			}
		}
		return unCheckList;
	}

	/**
	 * 
	 * @see 方法名：loadAccount 
	 * @see 描述：根据本外币账户类型，确定交易币种及入账币种；并且根据本外币标识，取出对应的账户信息。
	 * 			51域(持卡人账户货币代码)未上送：交易币种和入账币种为49域(交易货币代码)；交易金额和入账金额为4域(交易金额)；
	 * 			51域上送：交易币种为49域，交易金额为4域；入账币种为51域；入账金额为6域(持卡人扣账金额)；
	 * @see 创建日期：2015年6月22日下午6:03:24
	 * @author liruilin
	 *  
	 * @param message
	 * @param productCredit
	 * @param txnInfo
	 * @param acctNbr
	 * @return
	 * @throws AuthException
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public CcsAcctO loadAccount(CupMsg message, ProductCredit productCredit, TxnInfo txnInfo, Long acctNbr) throws AuthException {
		logger.debug("---------loadAccount --------" + acctNbr);
		// 根据本币账户属性引用ID获取本币产品账户信息
		AccountAttribute accountAttribute = unifiedParameterFacility.loadParameter(productCredit.accountAttributeId, AccountAttribute.class);

		AccountAttribute dualAccountAttribute = null;
		if (productCredit.dualAccountAttributeId != null)
			// 根据外币账户属性引用ID获取外币产品账户信息
			dualAccountAttribute = unifiedParameterFacility.loadParameter(productCredit.dualAccountAttributeId, AccountAttribute.class);

		// 如果币种未上送,则默认取人民币币种
		/*
		 * TODO 银联撤销即使原交易是双币种，撤销也不送入账币种，当前写死为156
		 * AIC发行双币种卡时，需要改造匹配类交易逻辑，先取原交易币种再操作账户
		 */
		if (StringUtils.isEmpty(message.field(49)) 
				|| (message.getReqInputSource() == InputSource.CUP && txnInfo.getTransType() == AuthTransType.Inq)
				|| (message.getReqInputSource() == InputSource.CUP && txnInfo.getTransDirection() == AuthTransDirection.Revocation)
				|| (message.getReqInputSource() == InputSource.CUP && txnInfo.getTransDirection() == AuthTransDirection.RevocationReversal)
				|| (message.getReqInputSource() == InputSource.CUP && txnInfo.getTransDirection() == AuthTransDirection.Reversal)) {
			txnInfo.setTransCurr("156");
		} else {
			txnInfo.setTransCurr(message.field(49));
		}

		// 交易币种为报文49域(交易货币代码)
		// txnInfo.setTransCurr( txnInfo.getCurrencyCd() );
		// 交易金额为报文4域(交易金额)
		txnInfo.setTransAmt(message.getF004_Amount());
		// 51域(持卡人账户货币代码)存在，入账币种为报文51域，否则入账币种为报文49域
		txnInfo.setChbCurr(StringUtils.isBlank(message.field(51)) ? txnInfo.getTransCurr() : message.field(51));
		// 51域(持卡人账户货币代码)存在，入账金额为报文6域(持卡人扣账金额)，否则入账币种为报文4域
		// 账户验证，建立委托，解除委托，查询交易的chbtransamt强制设为0
		if (txnInfo.getTransType() == AuthTransType.ContractBuildUp 
				|| txnInfo.getTransType() == AuthTransType.ContractTermination
				|| txnInfo.getTransType() == AuthTransType.AcctVerfication){
			txnInfo.setChbTransAmt(BigDecimal.ZERO);
		}else{
			txnInfo.setChbTransAmt(StringUtils.isBlank(message.field(51)) ? message.getF004_Amount() : message.getF006_Amount());
		}

		// 根据币种控制表中本币的小数精度，设置交易金额的小数位
		txnInfo.setTransAmt(txnInfo.getTransAmt().movePointLeft(unifiedParameterFacility.loadParameter(txnInfo.getTransCurr(), CurrencyCd.class).exponent));
		// 根据币种控制表中本币的小数精度，设置交易金额的小数位
		txnInfo.setChbTransAmt(txnInfo.getChbTransAmt().movePointLeft(unifiedParameterFacility.loadParameter(txnInfo.getChbCurr(), CurrencyCd.class).exponent));

		// 本币标识：本币账户币种等于入账币种为true；否则为false。
		txnInfo.setBaseCurrency(accountAttribute.accountType.getCurrencyCode().equals(txnInfo.getChbCurr()));

		// 非本币处理
		if (!txnInfo.isBaseCurrency()) {
			// 单币账户,认为上送币种错误
			if (dualAccountAttribute == null)
				throw new AuthException(AuthReason.TU03, AuthAction.D);

			// 外币账户的货币代码不等于交易报文中的入账币种
			if (!dualAccountAttribute.accountType.getCurrencyCode().equals(txnInfo.getChbCurr()))
				throw new AuthException(AuthReason.TU03, AuthAction.D);
		}

		// 加载账户数据
		CcsAcctOKey key = new CcsAcctOKey();
		key.setAcctNbr(acctNbr);
		// 如果是本币币种，取本币账户，反之取外币账户
		key.setAcctType(txnInfo.isBaseCurrency() ? accountAttribute.accountType : dualAccountAttribute.accountType);

		CcsAcctO account = rCcsAcctO.findOne(key);
		if (account == null)
			throw new AuthException(AuthReason.R001, AuthAction.D);
		return account;
	}
	
	/**
	 * 
	 * @see 方法名：loadCardSpectxnSupportO 
	 * @see 描述：根据逻辑卡号+SpecTxnType.A02 查询无卡自助
	 * @see 创建日期：2015年6月22日下午5:10:55
	 * @author liruilin
	 *  
	 * @param card
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public CcsCardSpecbizCtrl loadCardSpectxnSupportO(CcsCardO card) {
		return rCcsCardSpecbizCtrl.findOne(new CcsCardSpecbizCtrlKey(card.getLogicCardNbr(), SpecTxnType.A02));
	}
	
	/**
	 * 
	 * @see 方法名：loadCustomer 
	 * @see 描述：加载客户信息
	 * @see 创建日期：2015年6月22日下午5:12:05
	 * @author liruilin
	 *  
	 * @param card
	 * @return
	 * @throws AuthException
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public CcsCustomer loadCustomer(Long custId) throws AuthException {
		logger.debug("---------loadCustomer -------" + custId);
		CcsCustomer customer = rCcsCustomer.findOne(custId);
		if (customer == null) {
			logger.debug("未找到客户信息");
			throw new AuthException(AuthReason.R004, AuthAction.D);
		}

		return customer;
	}
	
	/**
	 * 
	 * @see 方法名：loadCustLimit 
	 * @see 描述：加载客户额度信息
	 * @see 创建日期：2015年6月22日下午5:12:22
	 * @author liruilin
	 *  
	 * @param account
	 * @return
	 * @throws AuthException
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public CcsCustomerCrlmt loadCustLimit(CcsAcctO account) throws AuthException {
		logger.debug("---------loadCustLimit ------ custLmtId:" + account.getCustLmtId());
		CcsCustomerCrlmt customerl = rCcsCustomerCrlmt.findOne(account.getCustLmtId());
		if (customerl == null) {
			logger.debug("未找到客户limit信息");
			throw new AuthException(AuthReason.R004, AuthAction.D);
		}
		return customerl;
	}

	/**
	 * 
	 * @see 方法名：loadAccounts 
	 * @see 描述：加载账户列表
	 * @see 创建日期：2015年6月22日下午5:12:43
	 * @author liruilin
	 *  
	 * @param custLmtId
	 * @return
	 * @throws AuthException
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public Iterable<CcsAcctO> loadAccounts(Long custLmtId) throws AuthException {
		logger.debug("---------loadAccounts --------" + custLmtId);
		Iterable<CcsAcctO> ts = rCcsAcctO.findAll(qAcctO.custLmtId.eq(custLmtId));
		if (ts == null) {
			logger.debug("未找到客户账户列表信息");
			throw new AuthException(AuthReason.R004, AuthAction.D);
		}

		return ts;
	}

	/**
	 * 
	 * @see 方法名：loadCardLimitOverrideO 
	 * @see 描述：加载卡片限额，要求如果没有也不要报错
	 * @see 创建日期：2015年6月22日下午5:13:24
	 * @author liruilin
	 *  
	 * @param cardNbr
	 * @return
	 * @throws AuthException
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public CcsCardThresholdCtrl loadCardLimitOverrideO(String cardNbr) throws AuthException {
		logger.debug("---------loadCardLimitOverrideO ------ " + cardNbr);
		return rCcsCardThresholdCtrl.findOne(cardNbr);
	}
	
	/**
	 * 
	 * @see 方法名：loadMcc 
	 * @see 描述：获取Mcc
	 * @see 创建日期：2015年6月22日下午5:15:36
	 * @author liruilin
	 *  
	 * @param message
	 * @param txnInfo
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public Mcc loadMcc(CupMsg message, TxnInfo txnInfo) {
		try {
			return unifiedParameterFacility.loadParameter(Mcc.assemblingMccKey(message.field(18), txnInfo.getInputSource()), Mcc.class);
		} catch (Exception e) {
			// Mcc未获取
			logger.debug("# Mcc获取失败[mcc:{}, input-source:{}]", message.field(18), txnInfo.getInputSource());
			return null;
		}
	}
}
