package com.sunline.ccs.service.auth.common;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ark.support.service.YakMessage;
import com.sunline.ark.support.utils.CodeMarkUtils;
import com.sunline.ccs.otb.CommProvide;
import com.sunline.ccs.service.auth.context.AuthContext;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.api.CustomAttributesKey;
import com.sunline.ppy.api.RdsTransactionDetectService;
import com.sunline.ppy.api.RdsTransactionInfo;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.InputSource;

/**
 * 
 * @see 类名：AuthRdsService
 * @see 描述：授权风险调用
 *
 * @see 创建日期：   2015年6月25日下午3:39:25
 * @author liruilin
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class AuthRdsService {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private UnifiedParameterFacility unifiedParameterFacility;

	@Autowired
	private CommProvide commonProvide;
	
	@Autowired
	private RdsTransactionDetectService rdsTransService;
	
	/**
	 * 
	 * @see 方法名：callRds 
	 * @see 描述：联机调用RDS
	 * @see 创建日期：2015年6月22日下午6:01:14
	 * @author liruilin
	 *  
	 * @param context
	 * @param request
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public void callRds(AuthContext context, YakMessage request) {
		final RdsTransactionInfo r = new RdsTransactionInfo();
		r.org = OrganizationContextHolder.getCurrentOrg();
		r.latestCardNo = request.getBody(2);
		r.cardSeqNo = request.getBody(32);
		r.b007 = request.getBody(7);
		r.b013 = request.getBody(13);
		r.b012 = request.getBody(12);
		r.b037 = request.getBody(37);
		r.transActionId = request.getBody(7) + request.getBody(11) + request.getBody(32) + request.getBody(33);
		r.b018 = request.getBody(18);
		r.b011 = request.getBody(11);
		r.b032 = request.getBody(32);
		r.b038 = request.getBody(38);
		r.b039 = request.getBody(39);
		r.b041 = request.getBody(41);
		r.b042 = request.getBody(42);
		r.b043 = request.getBody(43);
		r.b019 = request.getBody(19);
		r.b022 = request.getBody(22);
		r.mti = (String) request.getCustomAttributes().get(CustomAttributesKey.MTI);
		r.inputSource = (InputSource) request.getCustomAttributes().get(CustomAttributesKey.INPUT_SOURCE);
		if (request.getBody(4) != null) {
			r.b004 = new BigDecimal(request.getBody(4));
		}
		r.b049 = request.getBody(49);
		if (request.getBody(6) != null) {
			r.b006= new BigDecimal(request.getBody(6));
		}
		r.b051 = request.getBody(51);
		r.logOlTime = context.getTxnInfo().getSysOlTime();
		
		if(context !=null){
			if(context.getAccount() != null){
				r.acctId = String.valueOf( context.getAccount().getAcctNbr() );
				r.acctType = context.getAccount().getAcctType();
				r.balance = context.getAccount().getCurrBal();
				r.cashBalance = context.getAccount().getCashBal();
				r.accountBlockCode = context.getAccount().getBlockCode();
				r.unmatchDb = context.getAccount().getMemoDb();
				r.unmatchCr = context.getAccount().getMemoCr();
			}
			if(context.getCustomer() != null){
				r.custId =  String.valueOf( context.getCustomer().getCustId() );
			}
			if(context.getCustLimitO() != null) {
				/**
				 * 增加信用额度和取现信用额度（2014-3-13反欺诈接口新增字段）
				 */
				BigDecimal cashLimitRt = commonProvide.getCashLmtRate(context.getAccount(), context.getProductCredit(), context.getTxnInfo().getBizDate());
				r.creditLimit = context.getCustLimitO().getCreditLmt();
				r.cashLimit =  context.getCustLimitO().getCreditLmt().multiply(cashLimitRt);
			}
			if (context.getCard() != null) {
				r.logicalCardNo = context.getCard().getLogicCardNbr();
				r.pinTries = context.getCard().getPinTries();
				r.cardBlockCode = context.getCard().getBlockCode();
				r.lastOlProcessDate = context.getCard().getLastUpdateBizDate();
				r.posPinVerifyInd = context.getCard().getPosPinVerifyInd();
				r.productCode = context.getCard().getProductCd();
			}
			if (context.getTxnInfo() != null) {
				r.logOlTime = context.getTxnInfo().getSysOlTime();
				r.txnDirection = null == context.getTxnInfo().getTransDirection() ?
						"":context.getTxnInfo().getTransDirection().toString();
				r.expireDate = context.getTxnInfo().getExpiryDate();
				r.transType = context.getTxnInfo().getTransType();
				r.txnAmt = context.getTxnInfo().getTransAmt();
				r.txnCurrency = context.getTxnInfo().getTransCurr();
				r.chbAmt = context.getTxnInfo().getChbTransAmt();
				r.chbCurrency = context.getTxnInfo().getChbCurr();
				r.transTerminalType = null == context.getTxnInfo().getTransTerminal() ?
						"":context.getTxnInfo().getTransTerminal().toString();
				r.authAction = null == context.getTxnInfo().getAuthAction() ?
						"":context.getTxnInfo().getAuthAction().toString();
				r.remoteTrans = context.getTxnInfo().isRemoteTrans() ? Indicator.Y : Indicator.N;
				r.abroadNoVerify = context.getTxnInfo().isAbroadNoVerify() ? Indicator.Y : Indicator.N;
				r.billingCurrencyCode = context.getTxnInfo().getChbCurr();
				r.otb = context.getTxnInfo().getAccountOTB();
				r.cashOtb = context.getTxnInfo().getCashOTB();
				r.custOtb = context.getTxnInfo().getCustomerOTB();
				r.authReason = null == context.getTxnInfo().getAuthReason() ?
						"":context.getTxnInfo().getAuthReason().toString();
				r.acqCountryCode = context.getTxnInfo().getCountryCd();
			}
			if (context.getMediumInfo() != null) {
				r.icInd = context.getMediumInfo().getIcInd();
				r.merchantBlackList = context.getMediumInfo().getMerchantBlackFlag();
				r.cardBlackList = context.getMediumInfo().getCardBlackFlag();
				r.passwordVerifyResult = context.getMediumInfo().getPasswordVerifyResult();
				r.arqcVerifyResult = context.getMediumInfo().getArqcVerifyResult();
				r.cavvVerifyResult = context.getMediumInfo().getCavvVerifyResult();
				r.cvvVerifyResult = context.getMediumInfo().getCvvVerifyResult();
				r.cvv2VerifyResult = context.getMediumInfo().getCvv2VerifyResult();
				r.mpsCardBlockCode = context.getMediumInfo().getBlockCodes();
			}
		}
		
		logger.info("调用反欺诈系统开始" + r.mti);
		if (logger.isDebugEnabled()) {
			logger.debug("异常账户状态：Org[{}],AcctType[{}],CradNo[{}],AcctNbr[{}],AcctBlockCode[{}],TxnAmt[{}],TxnCurrency[{}],CreditLmt[{}],CashLimit[{}]", 
					r.org, r.acctType, CodeMarkUtils.subCreditCard(r.cardNo), 
					r.acctId, r.accountBlockCode, r.txnAmt, 
					r.txnCurrency, r.creditLimit, r.cashLimit);
		}

		rdsTransService.doRiskDetect(r);
	}
	
}
