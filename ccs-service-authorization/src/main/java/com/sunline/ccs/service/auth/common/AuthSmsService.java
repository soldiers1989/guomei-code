package com.sunline.ccs.service.auth.common;

import java.math.BigDecimal;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sunline.ark.support.service.YakMessage;
import com.sunline.ark.support.utils.MapBuilder;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.FetchSmsNbrFacility;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsCardO;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.enums.AuthReason;
import com.sunline.ccs.param.def.enums.AuthTransDirection;
import com.sunline.ccs.param.def.enums.AutoType;
import com.sunline.ccs.param.def.enums.CPSMessageCategory;
import com.sunline.ccs.param.def.enums.SendMessageCardType;
import com.sunline.ccs.service.auth.context.AuthContext;
import com.sunline.ccs.service.auth.context.CupMsg;
import com.sunline.ccs.service.auth.context.RespInfo;
import com.sunline.ccs.service.auth.context.TxnInfo;
import com.sunline.ccs.service.auth.frame.AuthException;
import com.sunline.pcm.param.def.CurrencyCd;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.api.CustomAttributesKey;
import com.sunline.ppy.dictionary.enums.AuthTransType;
import com.sunline.ppy.dictionary.enums.DdIndicator;
import com.sunline.ppy.dictionary.enums.Gender;
import com.sunline.ppy.dictionary.enums.ManualAuthFlag;
import com.sunline.ppy.dictionary.enums.SmsInd;
//import com.sunline.smsd.service.sdk.DownMsgFacility;

/**
 * 
 * @see 类名：AuthSmsService
 * @see 描述：授权短信发送服务
 *
 * @see 创建日期：   2015年6月25日下午3:14:55
 * @author liruilin
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
/*@Service
public class AuthSmsService {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private UnifiedParameterFacility unifiedParameterFacility;

	@Autowired
	private CustAcctCardFacility custAcctCardQueryFacility;
	
	@Autowired
	private DownMsgFacility downMsgFacility;

	@Autowired
	private FetchSmsNbrFacility fetchMsgCdService;
	*//**
	 * 
	 * @see 方法名：processMessage 
	 * @see 描述：短信发送
	 * @see 创建日期：2015年6月22日下午6:01:51
	 * @author liruilin
	 *  
	 * @param context
	 * @param responseMessage
	 * @param responseInfo
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 *//*
	public void processMessage(final AuthContext context, YakMessage responseMessage, RespInfo responseInfo) {
		TxnInfo txnInfo = context.getTxnInfo();

		final ProductCredit productCredit = context.getProductCredit();
		
		// 是否发送短信标志
		boolean isSendSms = false;
		if (txnInfo.getTransType() == AuthTransType.AgentCredit || txnInfo.getTransType() == AuthTransType.TransferCredit || txnInfo.getTransType() == AuthTransType.Credit
				|| txnInfo.getTransType() == AuthTransType.AcctVerfication || txnInfo.getTransType() == AuthTransType.ContractBuildUp 
				|| txnInfo.getTransType() == AuthTransType.ContractTermination || txnInfo.getTransType() == AuthTransType.Inq) {
			// 发送短信
			isSendSms = true;
		} else {
			if (context.getAccount().getSmsInd() == SmsInd.C) {
				if (txnInfo.getChbTransAmt().compareTo(context.getAccount().getUserSmsAmt()) > -1)
					isSendSms = true;
			} else if (context.getAccount().getSmsInd() == SmsInd.Y) {
				if (txnInfo.getChbTransAmt().compareTo(productCredit.defaultSmsAmt) > -1)
					isSendSms = true;
			} else if (context.getAccount().getSmsInd() == SmsInd.N) {
				// 不发送短信
			}
		}

		// 人工授权不发短信
		ManualAuthFlag manualAuthFlag = (ManualAuthFlag) responseMessage.getCustomAttributes().get(CustomAttributesKey.MANUAL_AUTH_FLAG);
		if (manualAuthFlag == ManualAuthFlag.F || manualAuthFlag == ManualAuthFlag.A) {
			logger.debug("交易类型：[{}] , 交易方向：[{}] , 短信功能标识：[{}] , 人工授权[ManualAuthFlag.{}]不发短信： ", txnInfo.getTransType(), txnInfo.getTransDirection(), context.getAccount().getSmsInd(),
					manualAuthFlag);
			return;
		}

		logger.debug("交易类型：[{}] , 交易方向：[{}] , 短信功能标识：[{}] , 是否发送：[{}]", txnInfo.getTransType(), txnInfo.getTransDirection(), context.getAccount().getSmsInd(), isSendSms);
		// 不发送短信则退出
		if (!isSendSms)
			return;

		CupMsg message = new CupMsg(responseMessage);

		if (productCredit.messageTemplates != null) {
			CcsCustomer tmCustomer = context.getCustomer();
			CcsAcctO ccsAcctO = context.getAccount();
			SendMessageCardType sendMessage = productCredit.sendMessageCardType;
			CurrencyCd currCd = unifiedParameterFacility.loadParameter(context.getTxnInfo().getTransCurr(), CurrencyCd.class);
			String field_2 = message.field(2);// 2域，卡号
			switch (sendMessage) {
			case C://介质卡发送
				field_2 = message.field(2);
				break;
			case D://约定还款的借记号发送,如果约定还款类型为为设置，则默认使用介质卡号发送
				CcsAcct ccsAcct = custAcctCardQueryFacility.getAcctByAcctNbr(ccsAcctO.getAcctType(), ccsAcctO.getAcctNbr());
				field_2 = ccsAcct.getDdInd() == DdIndicator.N ? message.field(2) : ccsAcct.getDdBankAcctNbr();
				break;
			case L://逻辑卡发送
				CcsCard tmCard = custAcctCardQueryFacility.getCardByCardNbr(field_2);
				field_2 = tmCard.getLogicCardNbr();
				break;
			}
			String name = tmCustomer.getName();
			Gender gender = tmCustomer.getGender();
			String mobileNo = tmCustomer.getMobileNo();
			// 发送短信
			class SendMessage {
				private CurrencyCd currCd;
				private String field_2;
				private String name;
				private Gender gender;
				private String mobileNo;
				
				public SendMessage(CurrencyCd currCd, String field_2, String name, Gender gender, String mobileNo){
					this.currCd = currCd;
					this.field_2 = field_2;
					this.name = name;
					this.gender = gender;
					this.mobileNo = mobileNo;
				}
				public void sendMessage(CPSMessageCategory cpsParam) {
					switch (cpsParam) {
					case CPS023:
						downMsgFacility.sendMessage(
								fetchMsgCdService.fetchMsgCd(productCredit.productCd, cpsParam),
								field_2,
								name,
								gender,
								mobileNo,
								new Date(),
								new MapBuilder<String, Object>().add("currencyCd", currCd.description).add("transType", context.getTxnInfo().getTransType())
										.add("amount", context.getTxnInfo().getTransAmt()).add("otb", context.getTxnInfo().getAccountOTB()).add("otbCash", context.getTxnInfo().getCashOTB())
										.build());
						break;
					case CPS032:
						final CcsLoanReg loanReg = context.getLoanInfo().getCcsLoanReg();
						logger.debug("短信[分期信息]：{}", loanReg.convertToMap());
						downMsgFacility.sendMessage(
								fetchMsgCdService.fetchMsgCd(productCredit.productCd, cpsParam),
								field_2,
								name,
								gender,
								mobileNo,
								new Date(),
								new MapBuilder<String, Object>().add("loanType", loanReg.getLoanType()).add("amt", loanReg.getLoanInitPrin()).add("term", loanReg.getLoanInitTerm())
										.add("loanFee", loanReg.getLoanInitFee().setScale(2, BigDecimal.ROUND_HALF_UP))
										.add("nextPayment", loanReg.getLoanFirstTermPrin().add(loanReg.getLoanFirstTermFee()).setScale(2, BigDecimal.ROUND_HALF_UP))
										.add("loanFixedFee1", loanReg.getLoanFixedFee().setScale(2, BigDecimal.ROUND_HALF_UP)).build());
						break;
					case CPS025:
						downMsgFacility.sendMessage(
								fetchMsgCdService.fetchMsgCd(productCredit.productCd, cpsParam),
								field_2,
								name,
								gender,
								mobileNo,
								new Date(),
								new MapBuilder<String, Object>().add("currencyCd", currCd.description).add("transType", context.getTxnInfo().getTransType())
										.add("amount", context.getTxnInfo().getTransAmt()).add("otb", context.getTxnInfo().getAccountOTB()).add("otbCash", context.getTxnInfo().getCashOTB())
										.build());
						break;
					case CPS026:
						downMsgFacility.sendMessage(
								fetchMsgCdService.fetchMsgCd(productCredit.productCd, cpsParam),
								field_2,
								name,
								gender,
								mobileNo,
								new Date(),
								new MapBuilder<String, Object>().add("currencyCd", currCd.description).add("transType", context.getTxnInfo().getTransType())
										.add("amount", context.getTxnInfo().getTransAmt()).add("otb", context.getTxnInfo().getAccountOTB()).add("otbCash", context.getTxnInfo().getCashOTB())
										.build());
						break;
					case CPS027:
						downMsgFacility.sendMessage(
								fetchMsgCdService.fetchMsgCd(productCredit.productCd, cpsParam),
								field_2,
								name,
								gender,
								mobileNo,
								new Date(),
								new MapBuilder<String, Object>().add("currencyCd", currCd.description).add("transType", context.getTxnInfo().getTransType())
										.add("amount", context.getTxnInfo().getTransAmt()).add("otb", context.getTxnInfo().getAccountOTB()).add("otbCash", context.getTxnInfo().getCashOTB())
										.build());
						break;
					case CPS065:
						downMsgFacility.sendMessage(
								fetchMsgCdService.fetchMsgCd(productCredit.productCd, cpsParam),
								field_2,
								name,
								gender,
								mobileNo,
								new Date(),
								new MapBuilder<String, Object>().add("currencyCd", currCd.description).add("transType", context.getTxnInfo().getTransType())
										.build());
						break;
					case CPS066:
						downMsgFacility.sendMessage(
								fetchMsgCdService.fetchMsgCd(productCredit.productCd, cpsParam),
								field_2,
								name,
								gender,
								mobileNo,
								new Date(),
								new MapBuilder<String, Object>().add("currencyCd", currCd.description).add("transType", context.getTxnInfo().getTransType())
										.build());
						break;
					case CPS067:
						downMsgFacility.sendMessage(
								fetchMsgCdService.fetchMsgCd(productCredit.productCd, cpsParam),
								field_2,
								name,
								gender,
								mobileNo,
								new Date(),
								new MapBuilder<String, Object>().add("currencyCd", currCd.description).add("transType", context.getTxnInfo().getTransType())
										.build());
						break;
					case CPS068:
						downMsgFacility.sendMessage(
								fetchMsgCdService.fetchMsgCd(productCredit.productCd, cpsParam),
								field_2,
								name,
								gender,
								mobileNo,
								new Date(),
								new MapBuilder<String, Object>().add("currencyCd", currCd.description).add("transType", context.getTxnInfo().getTransType())
										.build());
						break;
					case CPS069:
						downMsgFacility.sendMessage(
								fetchMsgCdService.fetchMsgCd(productCredit.productCd, cpsParam),
								field_2,
								name,
								gender,
								mobileNo,
								new Date(),
								new MapBuilder<String, Object>().add("currencyCd", currCd.description).add("transType", context.getTxnInfo().getTransType())
										.build());
						break;
					case CPS070:
						downMsgFacility.sendMessage(
								fetchMsgCdService.fetchMsgCd(productCredit.productCd, cpsParam),
								field_2,
								name,
								gender,
								mobileNo,
								new Date(),
								new MapBuilder<String, Object>().add("currencyCd", currCd.description).add("transType", context.getTxnInfo().getTransType())
										.build());
						break;
					default:
						break;
					}
				}
			}
			SendMessage sendMessageObject = new SendMessage(currCd,field_2,name,gender,mobileNo);
			if (context.getTxnInfo().getTransDirection() == AuthTransDirection.Normal || context.getTxnInfo().getTransDirection() == AuthTransDirection.Advice || responseInfo.isIsolatedConfirm()) {
				if (context.getTxnInfo().getTransType() == AuthTransType.ContractBuildUp){
					sendMessageObject.sendMessage(CPSMessageCategory.CPS065);
				}else if (context.getTxnInfo().getTransType() == AuthTransType.Inq){
					sendMessageObject.sendMessage(CPSMessageCategory.CPS066);
				}else if (context.getTxnInfo().getTransType() == AuthTransType.AcctVerfication){
					sendMessageObject.sendMessage(CPSMessageCategory.CPS067);
				}else if (context.getTxnInfo().getTransType() == AuthTransType.ContractTermination){
					sendMessageObject.sendMessage(CPSMessageCategory.CPS068);
				}else if (context.getTxnInfo().getTransType() != AuthTransType.Loan) {
					// 正向交易、结算通知、孤立确认交易的短信通知
					sendMessageObject.sendMessage(CPSMessageCategory.CPS023);
				} else {
					// TODO 分期发短信
					sendMessageObject.sendMessage(CPSMessageCategory.CPS032);
				}
			} else if (context.getTxnInfo().getTransDirection() == AuthTransDirection.Revocation) {
				// 撤销交易的短信通知
				sendMessageObject.sendMessage(CPSMessageCategory.CPS025);
			} else if (context.getTxnInfo().getTransDirection() == AuthTransDirection.Reversal) {
				if (context.getTxnInfo().getTransType() == AuthTransType.ContractBuildUp){
					sendMessageObject.sendMessage(CPSMessageCategory.CPS069);
				}else if (context.getTxnInfo().getTransType() == AuthTransType.ContractTermination){
					sendMessageObject.sendMessage(CPSMessageCategory.CPS070);
				}else{
					// 冲正交易的短信通知
					sendMessageObject.sendMessage(CPSMessageCategory.CPS026);
				}
			} else if (context.getTxnInfo().getTransDirection() == AuthTransDirection.RevocationReversal) {
				// 撤销的冲正交易的短信通知
				sendMessageObject.sendMessage(CPSMessageCategory.CPS027);
			}
		}
	}
	
	public void processReasonMsg(AuthException ae, AuthContext context) {
		CupMsg cupMsg = context.getMessage();
		CcsCustomer cust = context.getCustomer();
		TxnInfo txnInfo = context.getTxnInfo();
		CcsCardO card = context.getCard();
		
		if (ae.getReason() == AuthReason.V005) { // 密码错且超限
			String msgCd = fetchMsgCdService.fetchMsgCd(context.getCard().getProductCd(), CPSMessageCategory.CPS004);
			downMsgFacility.sendMessage(msgCd, cupMsg.field(2), cust.getName(), cust.getGender(), cust.getMobileNo(), new Date(), new MapBuilder<String, Object>().build());
		}else if (ae.getReason() == AuthReason.V004 || ae.getReason() == AuthReason.V014 
				 	|| ae.getReason() == AuthReason.V033 || ae.getReason() == AuthReason.V049){
			//AIC2.7银联升级-有卡自助交易因密码问题而失败的交易，发送短信
			if(null != txnInfo && txnInfo.getAutoType() == AutoType.ACardSelfService
					&& (txnInfo.getTransType() == AuthTransType.Auth || txnInfo.getTransType() == AuthTransType.PreAuth)){
				String msgCd = fetchMsgCdService.fetchMsgCd(card.getProductCd(), CPSMessageCategory.CPS064);
				
				downMsgFacility.sendMessage(msgCd, cupMsg.field(2), cust.getName(),
						cust.getGender(), cust.getMobileNo(), new Date(),
						new MapBuilder<String, Object>().add("authReason", ae.getReason()).build());
			}
		}
	}
}*/
