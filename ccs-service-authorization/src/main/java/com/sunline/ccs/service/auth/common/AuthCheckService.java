package com.sunline.ccs.service.auth.common;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sunline.ark.support.service.YakMessage;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsCardO;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.param.def.BlockCode;
import com.sunline.ccs.param.def.enums.AuthAction;
import com.sunline.ccs.param.def.enums.AuthReason;
import com.sunline.ccs.param.def.enums.AuthTransTerminal;
import com.sunline.ccs.service.auth.context.AuthContext;
import com.sunline.ccs.service.auth.context.CupMsg;
import com.sunline.ccs.service.auth.context.TxnInfo;
import com.sunline.ccs.service.auth.frame.AuthException;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.api.MediumInfo;
import com.sunline.ppy.api.MediumService;
import com.sunline.ppy.api.MmCardService;
import com.sunline.ppy.dictionary.enums.AuthTransType;
import com.sunline.ppy.dictionary.enums.InputSource;
import com.sunline.ppy.dictionary.enums.ManualAuthFlag;
import com.sunline.ppy.dictionary.enums.PasswordType;
import com.sunline.ppy.dictionary.enums.PasswordVerifyResult;
import com.sunline.ppy.dictionary.enums.VerifyResult;

/**
 * 
 * @see 类名：AuthCheckService
 * @see 描述：授权校验服务
 *
 * @see 创建日期：   2015年6月25日上午11:05:03
 * @author liruilin
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class AuthCheckService {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private AuthCommService authCommonService;
	
	@Autowired
	private UnifiedParameterFacility unifiedParameterFacility;
	
//	@Autowired 《马上不用介质卡》
//	private MediumService mediumService;
//	
//	@Autowired  《马上不用介质卡》
//	private MmCardService mmCardService;

	//《马上不用介质卡》
	public MediumInfo checkMediumInfo(YakMessage requestMessage) {
		MediumInfo mediumInfo = new MediumInfo();
		mediumInfo.setLogicalCardNo(requestMessage.getBody(2));
//		MediumInfo mediumInfo = mediumService.verifyAuthMedium(requestMessage);
		
//		if (StringUtils.isBlank(mediumInfo.getLogicalCardNo())) {
//			logger.debug("未找到逻辑卡号");
//			throw new AuthException(AuthReason.R002, AuthAction.D);
//		}
//		
//		//TODO 先移动，后期再判断是否应该移动位置
//		if (!mediumInfo.isValidCardNo()) {
//			logger.debug("卡管验证卡号不存在");
//			throw new AuthException(AuthReason.V022, AuthAction.D);
//		}
		return mediumInfo;
	}
	
	/**
	 * 检查密码是否存在   《马上不用介质卡》
	 * <p>
	 * true 有密码 false 无密码
	 * <p>
	 * 未上送密码返回false
	 * 
	 * @param cardNbr
	 * @param pwdType
	 * @return
	 */
	public PasswordVerifyResult checkSerPassword(String cardNbr, PasswordType pwdType) {
//		if (pwdType == null) {
			return PasswordVerifyResult.NoThisField;
//		}
//		switch (pwdType) {
//		case P:
//			return mmCardService.MS3502(cardNbr) ? PasswordVerifyResult.Approve : PasswordVerifyResult.Decline;
//		case Q:
//			return mmCardService.MS3501(cardNbr) ? PasswordVerifyResult.Approve : PasswordVerifyResult.Decline;
//		default:
//			logger.error("检查密码是否存在时：密码类型错误");
//			return PasswordVerifyResult.HsmFail;
//		}
	}
	
	/**
	 * 验证交易中的中文姓名或英文姓名（AM用法）
	 * 
	 * @param customer
	 * @param message
	 * @throws AuthException
	 * @throws DecoderException
	 * @throws UnsupportedEncodingException
	 */
	public VerifyResult CHBNameAMVerifyResult(CcsCustomer customer, CupMsg message) {
		try {
			// String name = new
			// String(Hex.decodeHex(message.getF61_6_AM_Name().toCharArray()),
			// "GBK");
			String name = message.getF061_6_AM_Name();

			if (!customer.getName().equals(name) && !customer.getOncardName().equals(name.toUpperCase()))
				return VerifyResult.Decline;

		} catch (Exception e) {
			return VerifyResult.Decline;
		}

		return VerifyResult.Approve;

	}
	
	/**
	 * 验证交易中的持卡人姓名（NM用法）
	 * 
	 * @param customer
	 * @param message
	 * @throws AuthException
	 * @throws DecoderException
	 * @throws UnsupportedEncodingException
	 */
	public VerifyResult CHBNameNMVerifyResult(CcsCustomer customer, CupMsg message) {
		try {
			String name = message.getF061_6_NM_ChbName1();
			if (name != null) {
				if (!customer.getName().equals(name) && !customer.getOncardName().equals(name.toUpperCase()))
					return VerifyResult.Decline;
			} else {
				return VerifyResult.NoThisField;
			}

		} catch (Exception e) {
			return VerifyResult.Decline;
		}

		return VerifyResult.Approve;

	}
	
	/**
	 * 验证行内存款、代付、转入交易检查48域上送的收款人姓名
	 * @param context
	 * @param customer
	 * @param message
	 * @return Decline 拒绝（不相符或获取姓名出现异常） NoThisField 无内容（未上送姓名）  Approve 通过（相符）
	 */
	public VerifyResult CHBReceiverNameFromBankVerifyResult(AuthContext context, CcsCustomer customer, CupMsg message){
		TxnInfo txnInfo = context.getTxnInfo();
		AuthTransType transType = txnInfo.getTransType();
		InputSource inputSource = txnInfo.getInputSource();
		if ( inputSource == InputSource.BANK &&
				(transType == AuthTransType.Credit || 
					transType == AuthTransType.AgentCredit || 
						transType == AuthTransType.TransferCredit)
		){
			try {
				String name = message.getF048_ReceiverName_Bank();
				if (StringUtils.isNotBlank(name)) {
					if (!StringUtils.equals(customer.getName(), name) && !StringUtils.equalsIgnoreCase(customer.getOncardName(), name)){
						return VerifyResult.Decline;
					}
				}else{
					return VerifyResult.NoThisField;
				}
			} catch (Exception e) {
				return VerifyResult.Decline;
			}
		}
		return VerifyResult.Approve;
	}
	
	/**
	 * 验证交易中的收款人姓名（NM用法）
	 * @param context 
	 * 
	 * @param customer
	 * @param message
	 * @throws AuthException
	 * @throws DecoderException
	 * @throws UnsupportedEncodingException
	 */
	public void CHBNameNMVerifyResult2(AuthContext context, CcsCustomer customer, CupMsg message) throws AuthException {
		String name = null;
		AuthTransType transType = context.getTxnInfo().getTransType();
		if (transType == AuthTransType.Credit || transType == AuthTransType.AgentCredit || transType == AuthTransType.TransferCredit) {
			try {
				name = message.getF061_6_NM_ChbName2();
			} catch (Exception e) {
				authCommonService.throwAuthException(AuthReason.TF02, "# 61NM用法格式错误:" + message.getF061_6_SecuData());
			}
			if (StringUtils.isNotBlank(name)) {
				if (!customer.getName().equals(name) && !customer.getOncardName().equals(name))
					authCommonService.throwAuthException(AuthReason.V041, "# 61NM用法[收款人]姓名不正确");
			}
		}
	}
	
	/**
	 * 验证交易中的身份证
	 * 
	 * @param customer
	 * @param message
	 * @return 补足20位的身份证号
	 * @throws AuthException
	 */
	public VerifyResult IdNbrVerifyResult(CcsCustomer customer, CupMsg message) {
		try {
			// 去除身份证号中的非法字符
			String messageIdNbr = message.getF061_1_IdNbr().replaceAll("[^0-9]", "");
			// 截取证件号后6位及补足6位的数字,并左补3个0
			messageIdNbr = "000" + (messageIdNbr.length() >= 6 ? messageIdNbr.substring(messageIdNbr.length() - 6) : messageIdNbr);

			// 去除数据库身份证号中的非法字符
			String dbIdNbr = customer.getIdNo().replaceAll("[^0-9]", "");
			// 截取证件号后6位及补足6位的数字,并左补3个0
			dbIdNbr = "000" + (dbIdNbr.length() >= 6 ? dbIdNbr.substring(dbIdNbr.length() - 6) : dbIdNbr);

			/**
			 * 1. 需要对身份证进行验证 2. 报文中的身份证后6位与数据库中的后6位进行判断
			 */
			if (message.getF061_6_AM_IDCheckFlag() && messageIdNbr.equals(dbIdNbr))
				return VerifyResult.Approve;
			// 报文61.6中身份证与数据库中的身份证相同，返回通过
			if (messageIdNbr.equals(dbIdNbr)) {
				return VerifyResult.Approve;
			}
		} catch (Exception e) {
			return VerifyResult.Decline;
		}
		// 右补20个空格并返回填入子域
		// return String.format("%-20s", messageIdNbr);
		return VerifyResult.Decline;
	}
	
	/**
	 * 验证交易中的手机号码
	 * 
	 * @param customer
	 * @param message
	 * @return 
	 *         上送的手机号码不为空且不等于持卡人手机号码返回[VerifyResult.Decline],否则返回[VerifyResult.Approve
	 *         ]
	 */
	public VerifyResult MobiNbrVerifyResult(CcsCustomer customer, CupMsg message) {
		try {
			// 从61AM用法中获得手机号码
			String mobiNbr = message.getF061_6_AM_TlvMap().get(CupMsg.MOBI_NBR).get(CupMsg.VAL);
			// 手机号码不为空且上送的手机号码不等于持卡人手机号码
			if (StringUtils.isNotBlank(mobiNbr) && !mobiNbr.equals(customer.getMobileNo())) {
				return VerifyResult.Decline;
			}
		} catch (Exception e) {
			return VerifyResult.Decline;
		}
		return VerifyResult.Approve;
	}
	
	/**
	 * 
	 * @see 方法名：checkBlockCode 
	 * @see 描述：获取卡管、逻辑卡和账户上BlockCode对应的ReasonCode及Action
	 * @see 创建日期：2015年6月22日下午6:03:47
	 * @author liruilin
	 *  
	 * @param card
	 * @param account
	 * @param mediumInfo
	 * @param txnInfo
	 * @return
	 * @throws AuthException
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public Map<AuthReason, AuthAction> checkBlockCode(CcsCardO card, CcsAcctO account, MediumInfo mediumInfo, TxnInfo txnInfo) throws AuthException {
		logger.debug("---------checkBlockCode ------ ");

		Map<AuthReason, AuthAction> reasonMap = new HashMap<AuthReason, AuthAction>();

		BlockCode blockCode = null;

		if (txnInfo.getTransType() == AuthTransType.Credit || txnInfo.getTransType() == AuthTransType.AgentCredit || txnInfo.getTransType() == AuthTransType.TransferCredit) {
			// 账户锁定码包含"P"
			if (StringUtils.isNotEmpty(account.getBlockCode()) && account.getBlockCode().toUpperCase().indexOf("P") > -1) {
				blockCode = unifiedParameterFacility.loadParameter("P", BlockCode.class);
				reasonMap.put(blockCode.authReason, AuthAction.D);
			}
		} else {

			/**
			 * 获取卡管信息中每一位BlockCode(BlockCode存储格式为ABCD...)
			 */
			if (StringUtils.isNotEmpty(mediumInfo.getBlockCodes())) {
				setActionFromBlockCode(txnInfo, reasonMap, mediumInfo.getBlockCodes().toCharArray());
			}

			/**
			 * 获取卡片层每一位BlockCode(BlockCode存储格式为ABCD...)
			 */
			if (StringUtils.isNotEmpty(card.getBlockCode())) {
				setActionFromBlockCode(txnInfo, reasonMap, card.getBlockCode().toCharArray());
			}
			/**
			 * 获取账户层每一位BlockCode(BlockCode存储格式为ABCD...)
			 */
			if (StringUtils.isNotEmpty(account.getBlockCode())) {
				setActionFromBlockCode(txnInfo, reasonMap, account.getBlockCode().toCharArray());
			}

		}
		/**
		 * 销户结清移除AuthReason.L002
		 */
		if (txnInfo.isCloseSettleAcct()) {
			logger.debug("# 移除销户结清原因码：{},{},{}", "L002:待销卡或待销户", "L011:被盗止付", "L012:丢失止付");
			reasonMap.remove(AuthReason.L002);
			reasonMap.remove(AuthReason.L011);
			reasonMap.remove(AuthReason.L012);
		}

		return reasonMap;
	}
	
	/**
	 * 
	 * @see 方法名：setActionFromBlockCode 
	 * @see 描述：根据交易类型和交易终端，获取对应的行动码。
	 * @see 创建日期：2015年6月22日下午6:04:02
	 * @author liruilin
	 *  
	 * @param txnInfo
	 * @param reasonMap
	 * @param BlockCodeArray
	 * @throws AuthException
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public void setActionFromBlockCode(TxnInfo txnInfo, Map<AuthReason, AuthAction> reasonMap, char... BlockCodeArray) throws AuthException {
		BlockCode blockCode = null;
		AuthAction action = null;

		for (char bc : BlockCodeArray) {
			blockCode = unifiedParameterFacility.loadParameter(bc, BlockCode.class);

			/**
			 * 交易类型中，人工授权优先级最高； 当人工授权标志=强制调整，并且交易类型=消费/取现/查询...，都按人工授权的行动码为准；
			 * 人工授权标志 == 强制调整
			 */
			if (txnInfo.getManualAuthFlag() == ManualAuthFlag.F) {
				// 根据交易类型获取对应的action
				action = blockCode.debitAdjustAction;
			}
			/**
			 * 交易类型 = 取现 行动码 = BlockCode参数中的取现行动码
			 */
			else if (txnInfo.getTransType() == AuthTransType.Cash || txnInfo.getTransType() == AuthTransType.TransferDeditDepos || txnInfo.isCloseSettleAcct()) {
				// 根据交易类型获取对应的action
				action = blockCode.cashAction;
			}
			/**
			 * 交易类型 = 代付 行动码 = BlockCode参数中的代付行动码
			 */
			else if (txnInfo.getTransType() == AuthTransType.AgentCredit) {
				// 根据交易类型获取对应的action
				action = blockCode.agentAction;
			}
			/**
			 * 交易类型 = 查询 行动码 = BlockCode参数中的查询行动码
			 */
			else if (txnInfo.getTransType() == AuthTransType.Inq) {
				action = blockCode.inquireAction;
			}
			/**
			 * 交易类型 = 消费|普通(额度内)分期|预授权|圈存
			 */
			else {
				if (txnInfo.getTransTerminal() == AuthTransTerminal.PHT) {
					// Moto手工消费对应的action
					action = blockCode.motoRetailAction;
				} else if (txnInfo.getTransTerminal() == AuthTransTerminal.PHE) {
					// Moto电子消费对应的action
					action = blockCode.motoElecAction;
				} else {
					// 非moto普通消费对应的action
					action = blockCode.nonMotoRetailAction;
				}
			}
			// 设置非"A|批准"的Reason Code和Reason Action
			reasonMap.put(blockCode.authReason, action);
		}

	}
	
	
	public static void main(String[] args) throws DecoderException {
		
		YakMessage message = new YakMessage();
		// NoThisField
		//message.getBodyAttributes().put(48, "    ");
		// Approve
		message.getBodyAttributes().put(48, "2020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020d3f7b7e5");
		// Decline
		
		CupMsg cupMessage = new CupMsg(message);
		
		CcsCustomer customer = new CcsCustomer();
		customer.setName("喻峰");
		//customer.setOncardName("laozhang");
		
		AuthContext context = new AuthContext();
		TxnInfo txnInfo = new TxnInfo();
		txnInfo.getCheckList().add("Cust_001");
		txnInfo.setInputSource(InputSource.BANK);
		txnInfo.setTransType(AuthTransType.Credit);
		context.setTxnInfo(txnInfo);
		
		AuthCheckService service = new AuthCheckService();
		VerifyResult verifyResult = service.CHBReceiverNameFromBankVerifyResult(context, customer, cupMessage);
		System.out.println(verifyResult);
		
	}
}
