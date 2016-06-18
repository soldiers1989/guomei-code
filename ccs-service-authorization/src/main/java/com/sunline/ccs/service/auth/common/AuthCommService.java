package com.sunline.ccs.service.auth.common;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ark.support.service.YakMessage;
import com.sunline.ccs.facility.TxnUtils;
import com.sunline.ccs.infrastructure.server.repos.RCcsAuthmemoInqLog;
import com.sunline.ccs.infrastructure.server.repos.RCcsAuthmemoO;
import com.sunline.ccs.infrastructure.server.repos.RCcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsAuthmemoInqLog;
import com.sunline.ccs.infrastructure.shared.model.CcsAuthmemoO;
import com.sunline.ccs.infrastructure.shared.model.CcsCardO;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.QCcsAuthmemoO;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoanReg;
import com.sunline.ccs.param.def.AuthReasonMapping;
import com.sunline.ccs.param.def.enums.AuthAction;
import com.sunline.ccs.param.def.enums.AuthReason;
import com.sunline.ccs.param.def.enums.AuthTransDirection;
import com.sunline.ccs.param.def.enums.AuthTransTerminal;
import com.sunline.ccs.param.def.enums.PinBlockResetInd;
import com.sunline.ccs.service.auth.context.AuthBaseMsg;
import com.sunline.ccs.service.auth.context.AuthContext;
import com.sunline.ccs.service.auth.context.CupMsg;
import com.sunline.ccs.service.auth.context.LoanInfo;
import com.sunline.ccs.service.auth.context.RespInfo;
import com.sunline.ccs.service.auth.context.TxnInfo;
import com.sunline.ccs.service.auth.frame.AuthException;
import com.sunline.ccs.service.auth.frame.HandlerCommService;
import com.sunline.ccs.service.auth.utils.Cup55Utils;
import com.sunline.ccs.service.auth.utils.TLVEntity;
import com.sunline.pcm.param.def.Product;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.api.CustomAttributesKey;
import com.sunline.ppy.api.MediumInfo;
import com.sunline.ppy.dictionary.enums.AuthTransStatus;
import com.sunline.ppy.dictionary.enums.AuthTransType;
import com.sunline.ppy.dictionary.enums.IdType;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.InputSource;
import com.sunline.ppy.dictionary.enums.LimitCategory;
import com.sunline.ppy.dictionary.enums.LoanFeeMethod;
import com.sunline.ppy.dictionary.enums.LoanRegStatus;
import com.sunline.ppy.dictionary.enums.LoanStatus;
import com.sunline.ppy.dictionary.enums.ManualAuthFlag;

/**
 * 
 * @see 类名：AuthCommService
 * @see 描述：授权公共业务处理
 *
 * @see 创建日期：   2015年6月18日下午3:22:42
 * @author liruilin
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class AuthCommService {
	private Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private UnifiedParameterFacility parameterFacility;
	@Autowired
	private HandlerCommService pcs;
	@Autowired
	private TxnUtils txnUtils;
	@Autowired
	private RCcsAuthmemoO rCcsAuthmemoO;
	@Autowired
	private RCcsAuthmemoInqLog rCcsAuthmemoInqLog;
	@Autowired
	private RCcsLoanReg rCcsLoanReg;
	@PersistenceContext
	private EntityManager em;
	// 空格
	private static final String SPACE = " ";

	/**
	 * 组装8583返回报文
	 * 
	 * @param formatMessage
	 * @return
	 * @throws AuthException
	 */
	public YakMessage createResponseMessage(YakMessage requestMessage, AuthContext context, RespInfo responseInfo) throws AuthException {
		// 返回MTI（这里MTI为空时不抛异常，因为总控catch异常交易时createResponseMessage；出现异常无法处理）
		if (null != requestMessage.getCustomAttributes().get(CustomAttributesKey.MTI) && StringUtils.isNotBlank(requestMessage.getCustomAttributes().get(CustomAttributesKey.MTI).toString())) {
			requestMessage.getCustomAttributes()
					.put(CustomAttributesKey.MTI, String.format("%04d", Integer.valueOf(requestMessage.getCustomAttributes().get(CustomAttributesKey.MTI).toString()) + 10));
		}
		if (context != null && context.getTxnInfo() != null) {
			requestMessage.getBodyAttributes().put(14, requestMessage.getBody(14) != null ? requestMessage.getBody(14) : context.getTxnInfo().getExpiryDate());
			requestMessage.getBodyAttributes().put(39, context.getTxnInfo().getResponsCode());
			logger.debug("Response Message : " + context.getTxnInfo().getResponsCode());
		}
		if (null != responseInfo) {
			requestMessage.getBodyAttributes().put(38, responseInfo.getB038_AuthCode());
			requestMessage.getBodyAttributes().put(44, responseInfo.getB044_AddRespData());
			requestMessage.getBodyAttributes().put(54, responseInfo.getB054Resp_AddAmt());
			requestMessage.getBodyAttributes().put(57, responseInfo.getB057_AddPriv());
			requestMessage.getBodyAttributes().put(123, responseInfo.getB123_IssInsReserved());
			/**
			 * 行内还款、转入、取现(包含销户结清)、转出(包含销户结清)交易 生成48域返回姓名
			 */
			if(context.getCustomer() != null){
				String respf048 = genBankB048Add(context);
				if (respf048 != null) {
					logger.debug("行内交易返回的48域：[{}]", respf048);
					requestMessage.getBodyAttributes().put(48, respf048);
				}
			}
			/**
			 * 分期交易封装57域并保存分期注册表信息
			 */
			if (context.getTxnInfo().getTransType() == AuthTransType.Loan && context.getTxnInfo().getTransDirection() == AuthTransDirection.Normal) {
				requestMessage.getBodyAttributes().put(57, generateB057Loan(context.getLoanInfo(), context.getTxnInfo()));
				// 保存分期注册表信息
				saveLoanReg(context, requestMessage, responseInfo);
			}

			/**
			 * 保存小额贷注册表信息
			 */
			if (context.getProduct().productType.getLimitCategory() == LimitCategory.MicroCreditLimit && context.getTxnInfo().getTransType() == AuthTransType.TransferDeditDepos
					&& context.getTxnInfo().getTransDirection() == AuthTransDirection.Normal) {
				logger.debug("# [小额贷第四步]保存小额贷注册表信息 #");
				String b057 = genMicroCredit57(responseInfo, context);
				requestMessage.getBodyAttributes().put(57, b057);
				saveLoanReg(context, requestMessage, responseInfo);
			}

			if (context.getTxnInfo().getTransType() == AuthTransType.AcctVerfication && context.getTxnInfo().getTransDirection() == AuthTransDirection.Normal) {
				String b057 = generateB057AcctVry(requestMessage);
				if (b057 != null) {
					requestMessage.getBodyAttributes().put(57, b057);
				}
			}

			if (context.getTxnInfo().getTransType() == AuthTransType.AcctVerfication && context.getTxnInfo().getTransDirection() == AuthTransDirection.Normal
					&& context.getTxnInfo().isqSpecTranNoCardSvc()) {
				String b057 = generateB057AcctVry(responseInfo, context);

				requestMessage.getBodyAttributes().put(57, b057);
			}

			if (StringUtils.isEmpty(responseInfo.getB061CustInfo_Cardholer())) {
				if (context.getTxnInfo().getTransType() == AuthTransType.TransferCredit && context.getTxnInfo().getTransDirection() == AuthTransDirection.Normal) {
					requestMessage.getBodyAttributes().put(61, requestMessage.getBody(61));
				} if (context.getTxnInfo().getTransType() == AuthTransType.TransferDeditDepos ){//AIC2.7 银联改造
					requestMessage.getBodyAttributes().put(61, getRespB061CustInfo_name2(context.getTxnInfo().getAcctVerifyName(), requestMessage.getBody(61)));
					logger.debug("## 转出交易61:" + requestMessage.getBodyAttributes().get(61));
				}else {
					requestMessage.getBodyAttributes().put(61, getRespB061CustInfoReset(context.getTxnInfo().getAcctVerifyName(), requestMessage.getBody(61)));
				}
			} else {
				requestMessage.getBodyAttributes().put(61, responseInfo.getB061CustInfo_Cardholer());
			}
		} else {
			if (StringUtils.isNotBlank(requestMessage.getBody(61))) {
				requestMessage.getBodyAttributes().put(61, getRespB061CustInfoReset(context.getTxnInfo().getAcctVerifyName(), requestMessage.getBody(61)));
			}
			if (context.getTxnInfo().getTransType() == AuthTransType.AcctVerfication && context.getTxnInfo().getTransDirection() == AuthTransDirection.Normal) {
				String b057 = generateB057AcctVry(requestMessage);
				if (b057 != null) {
					requestMessage.getBodyAttributes().put(57, b057);
				}
			}
		}

		requestMessage.getBodyAttributes().put(52, null);

		// 60域长度=27时.补三个零
		if (context != null && context.getTxnInfo() != null && (context.getTxnInfo().getInputSource() == InputSource.CUP || context.getTxnInfo().getInputSource() == InputSource.THIR) && StringUtils.isNotEmpty(requestMessage.getBody(60))
				&& requestMessage.getBody(60).length() == 27) {
			requestMessage.getBodyAttributes().put(60, requestMessage.getBody(60) + "000");
		}

		// IC交易返回ARPC
		if (context != null && context.getMediumInfo() != null ) {
			// context.getMediumInfo().getRespf55() 不能等于 null
			String f55 = generateArpc(requestMessage, context);
			if (StringUtils.isNotBlank(f55)) {
				requestMessage.getBodyAttributes().put(55, f55);
			}
		}
		
		// 49域为空时，默认返回156
		if(requestMessage != null && StringUtils.isBlank(requestMessage.getBody(49))){
			requestMessage.getBodyAttributes().put(49, "156");
		}

		/**
		 * [移除交易介质] 因为cup渠道不依赖aic项目,所以返回时,无法反射对应的枚举 所以在返回时移除掉
		 */
		requestMessage.getCustomAttributes().remove(CustomAttributesKey.TRAN_MEDIUM_TYPE);

		logger.info("返回值11域[系统跟踪号]=" + requestMessage.getCustomAttributes().get(CustomAttributesKey.MTI));
		logger.info("返回值39域[应答码]=" + requestMessage.getBodyAttributes().get(39));
		logger.info("返回值38域[授权标识应答码]=" + requestMessage.getBodyAttributes().get(38));

		return requestMessage;
	}

	/**
	 * 转出交易设置61
	 * AIC2.7 银联升级
	 * @param acctVerifyName
	 * @return
	 */
	private String getRespB061CustInfo_name2(String acctVerifyName, String f61) {
		StringBuffer respF61 = new StringBuffer();
		// 证件号改为22个零
		respF61.append("0000000000000000000000");
		// 修改[__CUP000__]（原数据为10个空格）
		respF61.append(SPACE + SPACE + "CUP000" + SPACE + SPACE);
		// 追加AM/NM
		respF61.append(appendRespB061CustInfoName2(acctVerifyName, f61));
		logger.debug("61域持卡人身份认证信息=" + respF61.toString());
		return respF61.toString();
	}
	
	/**
	 * 追加61.6_NM
	 * AIC2.7 银联改造
	 * @param name
	 * @param f61
	 * @return
	 */
	public String appendRespB061CustInfoName2(String name, String f61) {
		if (StringUtils.isNotBlank(name)) {
			// "CUPNM" + "name + SPACE补到60"
			return "CUPNM" + addSpace("", 30) + name + addSpace(name, 30);
		} else if (StringUtils.isNotBlank(f61)) {
			// 61.6原样返回
			return f61.length() > 32 ? f61.substring(32) : "";
		}
		return "";
	}

	/**
	 * 返回系统异常报文（原报文域）
	 * 
	 * @param requestMessage
	 * @return
	 */
	public YakMessage errorRespMessage(YakMessage requestMessage) {
		requestMessage.getCustomAttributes().put(CustomAttributesKey.MTI, String.format("%04d", Integer.valueOf(requestMessage.getCustomAttributes().get(CustomAttributesKey.MTI).toString()) + 10));
		requestMessage.getBodyAttributes().put(39, "98");
		logger.info("返回值39域[应答码]=" + requestMessage.getBodyAttributes().get(39));
		return requestMessage;
	}

	/**
	 * 记录授权日志
	 * 
	 * @param formatMessage
	 */
	public void writeAuthLog(AuthBaseMsg ym, AuthContext context, RespInfo responseInfo) {
		AuthTransType transType = context.getTxnInfo().getTransType();
		// 判断是否为查询类
		if (transType == AuthTransType.Inq) {
			CcsAuthmemoInqLog ail = saveCcsAuthmemoInqLog(ym, context, responseInfo);
			if (null != responseInfo) {
				// 设置授权码
				ail.setAuthCode(decideAuthCode(ym, context, responseInfo, ail.getLogKv()));
			}
		} else {
			CcsAuthmemoO umo = saveCcsAuthmemoO(ym, context, responseInfo);
			if (null != responseInfo) {
				umo.setAuthCode(decideAuthCode(ym, context, responseInfo, umo.getLogKv()));
			}
		}
	}

	/**
	 * 设置response授权码,并返回<br>
	 * 
	 * @param ym
	 * @param context
	 * @param responseInfo
	 * @param logKV
	 * @return
	 * 
	 *         针对通过的正向借贷记交易均生成新的授权码,反向交易不生成授权码,<br>
	 *         预授权完成和贷记确认使用原授权码<br>
	 *         孤立确认生成新的授权码<br>
	 *         如果上送请求已包含授权码则使用上送的授权码<br>
	 *         既非通过的正向交易也非上送授权码的交易,则授权码为空<br>
	 */
	public String decideAuthCode(AuthBaseMsg ym, AuthContext context, RespInfo responseInfo, Long logKV) {
		TxnInfo txnInfo = context.getTxnInfo();
		// 判断当前交易的授权码是否为空
		if (!StringUtils.isBlank(ym.field(38))) {
			// 更新日志授权码
			responseInfo.setB038_AuthCode(ym.field(38));
		}
		// 正向且不是预授权完成的交易
		if (null != txnInfo && txnInfo.getTransDirection() == AuthTransDirection.Normal && txnInfo.getTransType() != AuthTransType.PAComp && txnInfo.getAuthAction() == AuthAction.A
				&& (txnInfo.getResponsCode().equals("00") || txnInfo.getResponsCode().equals("11"))) {
			responseInfo.setB038_AuthCode(generateAuthCode(context, logKV));
		}

		// 判断是否为确认交易
		if (null != txnInfo && txnInfo.getTransDirection() == AuthTransDirection.Confirm && txnInfo.getAuthAction() == AuthAction.A
				&& (txnInfo.getResponsCode().equals("00") || txnInfo.getResponsCode().equals("11"))) {
			/*
			 * 判断当前交易的状态； 1、状态=O（孤立确认）获取新的授权码， 2、状态=N（成功的确认交易）获取原交易的授权码
			 */
			responseInfo.setB038_AuthCode(responseInfo.isIsolatedConfirm() ? generateAuthCode(context, logKV) : responseInfo.getOrigAuthCode());
		}

		return responseInfo.getB038_AuthCode();
	}

	/**
	 * 生成授权码
	 * 
	 * @return
	 */
	public String generateAuthCode(AuthContext context, Long logKV) {
		String str = logKV.toString();
		String ac = str.length() < 6 ? String.format("%06d", logKV) : StringUtils.right(str, 6);

		if (null != context.getAuthProduct() && null != context.getMcc() && null != context.getTxnInfo()) {
			String mccType = context.getMcc().mccType;
			if (context.getAuthProduct().authCodeIncMCCFlag
					&& !StringUtils.isEmpty(mccType)
					&& (context.getTxnInfo().getInputSource() == InputSource.MC || context.getTxnInfo().getInputSource() == InputSource.AMEX || context.getTxnInfo().getInputSource() == InputSource.JCB)) {
				return mccType.length() > 1 ? StringUtils.left(mccType, 1) : mccType + ac.substring(1);
			} else {
				return "0" + ac.substring(1);
			}
		}
		return ac;
	}

	/**
	 * 转换格式并保存
	 * 
	 * @return
	 */
	public CcsAuthmemoO saveCcsAuthmemoO(AuthBaseMsg ym, AuthContext context, RespInfo responseInfo) {
		CcsAuthmemoO um = new CcsAuthmemoO();
		um.setOrg(OrganizationContextHolder.getCurrentOrg());
		if (null != context.getAccount()) {
			CcsAcctO acct = context.getAccount();
			um.setAcctNbr(acct.getAcctNbr());
			um.setAcctType(acct.getAcctType());
			um.setCurrBal(acct.getCurrBal());
			um.setMemoCr(acct.getMemoCr());
			um.setMemoDb(acct.getMemoDb());

			um.setAcctBlockcode(acct.getBlockCode());
		}
		if (null != context.getCard()) {
			CcsCardO card = context.getCard();
			um.setPayPwdErrNum(card.getPinTries());
			um.setCardBlockCode(card.getBlockCode());
		}
		if (null != context.getMediumInfo()) {
			MediumInfo mps = context.getMediumInfo();
			um.setInputSource(context.getTxnInfo().getInputSource());
			um.setArqcResult(mps.getArqcVerifyResult());
			um.setAtcResult(mps.getAtcVerifyResult());
			um.setCvrResult(mps.getCvrVerifyResult());
			um.setCvv2Result(mps.getCvv2VerifyResult());
			um.setCvvResult(mps.getCvvVerifyResult());
			um.setIcvnResult(mps.getIcvnVerifyResult());
			um.setPwdResult(mps.getPasswordVerifyResult());
			um.setTvrResult(mps.getTvrVerifyResult());
			um.setIcInd(mps.getIcInd());
			um.setInBlacklistInd(mps.getCardBlackFlag());
			um.setMerInBlacklistInd(mps.getMerchantBlackFlag());
			um.setLogicCardNbr(mps.getLogicalCardNo());
			um.setMegt1Result(mps.getTrack1VerifyResult());
			um.setMegt2Result(mps.getTrack2VerifyResult());
			um.setMegt3Result(mps.getTrack3VerifyResult());
			um.setPwdType(mps.getPwdType());
			um.setOpId(mps.getOperaId());
			um.setOpTermId(mps.getOperaTermId());
			um.setMediumBlockCode(mps.getBlockCodes());
		}
		if (null != context.getProductCredit()) {
			Product product = context.getProduct();
			um.setCardAssociation(product.brand.toString());
			um.setProductCd(product.productCode);
		}
		if (null != context.getTxnInfo()) {
			TxnInfo txnInfo = context.getTxnInfo();
			um.setLogOlTime(txnInfo.getSysOlTime());
			um.setB039RtnCode(txnInfo.getResponsCode());
			um.setFinalAction(txnInfo.getAuthAction());
			um.setFinalReason(txnInfo.getAuthReason());
			um.setManualAuthFlag(txnInfo.getManualAuthFlag());
			um.setChbTxnAmt(txnInfo.getChbTransAmt());
			um.setChbCurrency(txnInfo.getChbCurr());
			um.setTxnAmt(txnInfo.getTransAmt());
			um.setTxnCurrency(txnInfo.getTransCurr());
			um.setTxnType(txnInfo.getTransType());
			um.setAuthTxnTerminal(txnInfo.getTransTerminal());
			um.setCustOtb(txnInfo.getCustomerOTB());
			um.setCashOtb(txnInfo.getCashOTB());
			um.setOtb(txnInfo.getAccountOTB());
			um.setLogBizDate(txnInfo.getBizDate());
			um.setTxnDirection(txnInfo.getTransDirection());
			um.setRejReason(rejectReason2String(txnInfo.getReasons()));
			/*
			 * 如果交易失败,finalamt=0,结束; 如果是撤销类交易,finalamt=0; 如果是冲正类交易 finalamt=0
			 * 如果当前是正向交易,finalamt= txnamt 如果是预授权完成,finalamt=txnamt;
			 */
			if (txnInfo.getAuthAction() == AuthAction.A && (txnInfo.getTransType() == AuthTransType.PAComp || txnInfo.getTransType() == AuthTransType.AdviceSettle)) {
				um.setFinalAuthAmt(txnInfo.getChbTransAmt());
			} else {
				um.setFinalAuthAmt(BigDecimal.ZERO);
			}
			/*
			 * 如果交易失败,finalamt=0,结束; 如果不是取现交易,finalamt=0,结束; 其他finalamt=txnamt
			 */
			if (txnInfo.getAuthAction() != AuthAction.A || (txnInfo.getTransType() != AuthTransType.Cash && txnInfo.getTransType() != AuthTransType.TransferDeditDepos)) {
				um.setCashAmt(BigDecimal.ZERO);
			} else {
				um.setCashAmt(txnInfo.getChbTransAmt());
			}
		}

		if (null != context.getMcc() && StringUtils.isNotBlank(context.getMcc().mccType)) {
			String mccType = context.getMcc().mccType;
			um.setMccType(mccType.length() > 1 ? mccType.substring(0, 1) : mccType);
		}

		if (null != responseInfo) {
			um.setOrigAcqInstId(responseInfo.getOrigAcqInstId());
			um.setOrigChbTxnAmt(responseInfo.getOrigChbTxnAmt());
			um.setOrigFwdInstId(responseInfo.getOrigFwdInstId());
			um.setOrigLogKv(responseInfo.getOrigLogKv());
			um.setOrigTraceNo(responseInfo.getOrigTraceNo());
			um.setOrigTransDate(responseInfo.getOrigTransDate());
			um.setOrigTxnAmt(responseInfo.getOrigTxnAmt());
			um.setOrigTxnProc(responseInfo.getOrigTxnProc());
			um.setOrigTxnType(responseInfo.getOrigTxnType());
			um.setOrigTxnVal1(responseInfo.getOrigTxnVal1());
			um.setOrigTxnVal2(responseInfo.getOrigTxnVal2());
			um.setOrigBizDate(responseInfo.getOrigBizDate());
			um.setFinalUpdAmt(responseInfo.getFinalUpdAmt());
			um.setFinalUpdDirection(responseInfo.getFinalUpdDirection());
			um.setOrigMti(responseInfo.getOrigMti());
			// 根据孤立确认标识，填交易态
			um.setAuthTxnStatus(responseInfo.isIsolatedConfirm() ? AuthTransStatus.O : AuthTransStatus.N);
		} else {
			// 未通过的交易状态
			um.setAuthTxnStatus(AuthTransStatus.E);
		}

		if (null != ym) {
			um.setAcqRefNbr(StringUtils.isBlank(ym.field(11)) ? null : Integer.valueOf(ym.field(11)));
			um.setExpireDate(ym.field(14));
			um.setFwdInstId(ym.field(33).trim());
			um.setMcc(ym.field(18));
			um.setAcqAddress(ym.getAcqAddress());
			um.setMti(ym.getMti());
			um.setThe3dsecureType(StringUtils.isBlank(ym.getThe3dsecureType()) ? null : ym.getThe3dsecureType().equals("00") ? Indicator.N : Indicator.Y);
			um.setSettleDate(ym.field(15));
			um.setTransportTime(ym.field(7));
			um.setB002CardNbr(ym.field(2));
			um.setB003ProcCode(ym.field(3));
			um.setB004Amt(StringUtils.isBlank(ym.field(4)) ? null : new BigDecimal(ym.field(4)));
			um.setB006ChbAmt(StringUtils.isBlank(ym.field(6)) ? null : new BigDecimal(ym.field(6)));
			um.setB007TxnTime(ym.field(7));
			um.setB011Trace(ym.field(11));
			um.setB022Entrymode(ym.field(22));
			um.setB025Entrycond(ym.field(25));
			um.setB028Fee(getB028Fee(ym.field(28)));
			um.setB032AcqInst(ym.field(32).trim());
			um.setB033FwdIns(ym.field(33).trim());
			um.setB037RefNbr(ym.field(37));
			um.setB042MerId(ym.field(42));
			um.setB048Add(ym.field(48));
			um.setB049CurrCode(ym.field(49));
			um.setB051ChbCurrCode(ym.field(51));
			um.setB060Reserved(ym.field(60));
			um.setB090OrigData(ym.field(90));
			um.setAcqBranchIq(StringUtils.isBlank(ym.field(48)) ? null : StringUtils.substring(ym.field(32), 4, 8));
		}

		if (null != responseInfo && null != ym) {
			um.setAuthCode(StringUtils.isBlank(responseInfo.getB038_AuthCode()) ? StringUtils.isBlank(ym.field(38)) ? null : ym.field(38) : responseInfo.getB038_AuthCode());
			um.setB054Resp(StringUtils.isBlank(responseInfo.getB054Resp_AddAmt()) ? StringUtils.isBlank(ym.field(54)) ? null : ym.field(54) : responseInfo.getB054Resp_AddAmt());
			um.setB061CustInfo(responseInfo.getB061CustInfo_Cardholer());
		}
		um.setVipStatus(null);

		return rCcsAuthmemoO.save(um);
	}

	/**
	 * 转换格式并保存
	 * 
	 * @param ym
	 * @param context
	 * @return
	 */
	public CcsAuthmemoInqLog saveCcsAuthmemoInqLog(AuthBaseMsg ym, AuthContext context, RespInfo responseInfo) {
		CcsAuthmemoInqLog ail = new CcsAuthmemoInqLog();
		ail.setOrg(OrganizationContextHolder.getCurrentOrg());
		if (null != context.getAccount()) {
			CcsAcctO acct = context.getAccount();
			ail.setAcctNbr(acct.getAcctNbr());
			ail.setAcctType(acct.getAcctType());
			ail.setAcctBlockcode(acct.getBlockCode());
			ail.setCurrBal(acct.getCurrBal());
			ail.setMemoCr(acct.getMemoCr());
			ail.setMemoDb(acct.getMemoDb());

		}
		if (null != context.getCard()) {
			CcsCardO card = context.getCard();
			ail.setPayPwdErrNum(card.getPinTries());
			ail.setCardBlockCode(card.getBlockCode());
		}
		if (null != context.getMediumInfo()) {
			MediumInfo mps = context.getMediumInfo();
			ail.setInputSource(context.getTxnInfo().getInputSource());
			ail.setArqcResult(mps.getArqcVerifyResult());
			ail.setAtcResult(mps.getAtcVerifyResult());
			ail.setCvrResult(mps.getCvrVerifyResult());
			ail.setCvv2Result(mps.getCvv2VerifyResult());
			ail.setCvvResult(mps.getCvvVerifyResult());
			ail.setIcvnResult(mps.getIcvnVerifyResult());
			ail.setPwdResult(mps.getPasswordVerifyResult());
			ail.setTvrResult(mps.getTvrVerifyResult());
			ail.setIcInd(mps.getIcInd());
			ail.setInBlacklistInd(mps.getCardBlackFlag());
			ail.setMerInBlacklistInd(mps.getMerchantBlackFlag());
			ail.setLogicCardNbr(mps.getLogicalCardNo());
			ail.setMegt1Result(mps.getTrack1VerifyResult());
			ail.setMegt2Result(mps.getTrack2VerifyResult());
			ail.setMegt3Result(mps.getTrack3VerifyResult());
			ail.setPwdType(mps.getPwdType());
			ail.setOpId(mps.getOperaId());
			ail.setOpTermId(mps.getOperaTermId());
			ail.setMediumBlockCode(mps.getBlockCodes());
		}
		if (null != context.getProductCredit()) {
			Product product = context.getProduct();
			ail.setCardAssociation(product.brand.toString());
			ail.setProductCd(product.productCode);
		}
		if (null != context.getTxnInfo()) {
			TxnInfo txnInfo = context.getTxnInfo();
			ail.setLogOlTime(txnInfo.getSysOlTime());
			ail.setB039RtnCode(txnInfo.getResponsCode());
			ail.setFinalAction(txnInfo.getAuthAction());
			ail.setFinalReason(txnInfo.getAuthReason());
			ail.setManualAuthFlag(txnInfo.getManualAuthFlag());
			ail.setChbTxnAmt(txnInfo.getChbTransAmt());
			ail.setChbCurrency(txnInfo.getChbCurr());
			ail.setTxnAmt(txnInfo.getTransAmt());
			ail.setTxnCurrency(txnInfo.getTransCurr());
			ail.setTxnType(txnInfo.getTransType());
			ail.setAuthTxnTerminal(txnInfo.getTransTerminal());
			ail.setCustOtb(txnInfo.getCustomerOTB());
			ail.setCashOtb(txnInfo.getCashOTB());
			ail.setOtb(txnInfo.getAccountOTB());
			ail.setLogBizDate(txnInfo.getBizDate());
			ail.setTxnDirection(txnInfo.getTransDirection());
			ail.setRejReason(rejectReason2String(txnInfo.getReasons()));
			// 查询交易取现金额、完成金额固定为零
			ail.setFinalAuthAmt(BigDecimal.ZERO);
			ail.setCashAmt(BigDecimal.ZERO);
		}

		if (null != context.getMcc() && StringUtils.isNotBlank(context.getMcc().mccType)) {
			String mccType = context.getMcc().mccType;
			ail.setMccType(mccType.length() > 1 ? mccType.substring(0, 1) : mccType);
		}

		if (null != responseInfo) {
			ail.setOrigAcqInstId(responseInfo.getOrigAcqInstId());
			ail.setOrigChbTxnAmt(responseInfo.getOrigChbTxnAmt());
			ail.setOrigFwdInstId(responseInfo.getOrigFwdInstId());
			ail.setOrigLogKv(responseInfo.getOrigLogKv());
			ail.setOrigTraceNo(responseInfo.getOrigTraceNo());
			ail.setOrigTransDate(responseInfo.getOrigTransDate());
			ail.setOrigTxnAmt(responseInfo.getOrigTxnAmt());
			ail.setOrigTxnProc(responseInfo.getOrigTxnProc());
			ail.setOrigTxnType(responseInfo.getOrigTxnType());
			ail.setOrigTxnVal1(responseInfo.getOrigTxnVal1());
			ail.setOrigTxnVal2(responseInfo.getOrigTxnVal2());
			ail.setOrigBizDate(responseInfo.getOrigBizDate());
			ail.setFinalUpdAmt(responseInfo.getFinalUpdAmt());
			ail.setFinalUpdDirection(responseInfo.getFinalUpdDirection());
			ail.setOrigMti(responseInfo.getOrigMti());
			// 根据孤立确认标识，填交易态
			ail.setAuthTxnStatus(responseInfo.isIsolatedConfirm() ? AuthTransStatus.O : AuthTransStatus.N);
		} else {
			// 未通过的交易状态
			ail.setAuthTxnStatus(AuthTransStatus.E);
		}

		if (null != ym) {
			ail.setAcqRefNbr(StringUtils.isBlank(ym.field(11)) ? null : Integer.valueOf(ym.field(11)));
			ail.setExpireDate(ym.field(14));
			ail.setFwdInstId(ym.field(33).trim());
			ail.setMcc(ym.field(18));
			ail.setAcqAddress(ym.getAcqAddress());
			ail.setMti(ym.getMti());
			ail.setThe3dsecureType(StringUtils.isBlank(ym.getThe3dsecureType()) ? null : ym.getThe3dsecureType().equals("00") ? Indicator.N : Indicator.Y);
			ail.setSettleDate(ym.field(15));
			ail.setTransportTime(ym.field(7));
			ail.setB002CardNbr(ym.field(2));
			ail.setB003ProcCode(ym.field(3));
			ail.setB004Amt(StringUtils.isBlank(ym.field(4)) ? null : new BigDecimal(ym.field(4)));
			ail.setB006ChbAmt(StringUtils.isBlank(ym.field(6)) ? null : new BigDecimal(ym.field(6)));
			ail.setB007TxnTime(ym.field(7));
			ail.setB011Trace(ym.field(11));
			ail.setB022Entrymode(ym.field(22));
			ail.setB025Entrycond(ym.field(25));
			ail.setB028Fee(getB028Fee(ym.field(28)));
			ail.setB032AcqInst(ym.field(32).trim());
			ail.setB033FwdIns(ym.field(33).trim());
			ail.setB037RefNbr(ym.field(37));
			ail.setB042MerId(ym.field(42));
			ail.setB048Add(ym.field(48));
			ail.setB049CurrCode(ym.field(49));
			ail.setB051ChbCurrCode(ym.field(51));
			ail.setB060Reserved(ym.field(60));
			ail.setB090OrigData(ym.field(90));
			ail.setAcqBranchIq(StringUtils.isBlank(ym.field(48)) ? null : StringUtils.substring(ym.field(32), 4, 8));
		}
		if (null != responseInfo && null != ym) {
			ail.setAuthCode(StringUtils.isBlank(responseInfo.getB038_AuthCode()) ? StringUtils.isBlank(ym.field(38)) ? null : ym.field(38) : responseInfo.getB038_AuthCode());
			ail.setB054Resp(StringUtils.isBlank(responseInfo.getB054Resp_AddAmt()) ? StringUtils.isBlank(ym.field(54)) ? null : ym.field(54) : responseInfo.getB054Resp_AddAmt());
			ail.setB061CustInfo(responseInfo.getB061CustInfo_Cardholer());
		}
		ail.setVipStatus(null);

		return rCcsAuthmemoInqLog.save(ail);
	}

	/**
	 * 处理28域首字母D/C（正负数）
	 * 
	 * @param b28
	 * @return
	 */
	public BigDecimal getB028Fee(String b28) {
		if (StringUtils.isNotBlank(b28) && b28.length() > 1) {
			if (StringUtils.left(b28, 1).equals("C")) {
				return new BigDecimal(b28.substring(1)).negate();
			} else {
				return new BigDecimal(b28.substring(1));
			}
		}
		return null;
	}

	/**
	 * 返回B061CustInfo域[持卡人身份认证信息]
	 * 
	 * @param context
	 * 
	 * @param pid
	 * @param name
	 * @return
	 */
	public String getRespB061CustInfo(AuthContext context, String f61) {
		IdType idType = context.getCustomer().getIdType();
		String idNo = context.getCustomer().getIdNo();
		// 返回姓名;[*]代替姓名首字符
		String name = context.getTxnInfo().getAcctVerifyName();

		StringBuffer respF61 = new StringBuffer();
		// 格式化B061CustInfo.1证件域[22位]
		respF61.append(formatB061CustInfo_1(idType, idNo));
		// 修改[__CUP000__]（原数据为10个空格）
		respF61.append(SPACE + SPACE + "CUP000" + SPACE + SPACE);
		// 追加AM/NM []
		respF61.append(appendRespB061CustInfo(name, f61));

		logger.debug("61域持卡人身份认证信息=" + respF61.toString());

		return respF61.toString();
	}

	/**
	 * 返回清空的B061CustInfo域[持卡人身份认证信息]
	 * 
	 * @param f61
	 * @return
	 */
	public String getRespB061CustInfoReset(String name, String f61) {
		StringBuffer respF61 = new StringBuffer();
		// 证件号改为22个零
		respF61.append("0000000000000000000000");
		// 修改[__CUP000__]（原数据为10个空格）
		respF61.append(SPACE + SPACE + "CUP000" + SPACE + SPACE);
		// 追加AM/NM
		respF61.append(appendRespB061CustInfo(name, f61));
		logger.debug("61域持卡人身份认证信息=" + respF61.toString());
		return respF61.toString();
	}

	/**
	 * 追加61.6_AM/NM
	 * 
	 * @param name
	 * @param f61
	 * @return
	 */
	public String appendRespB061CustInfo(String name, String f61) {
		if (StringUtils.isNotBlank(name)) {
			// "CUPNM" + "name + SPACE补到60"
			return "CUPNM" + name + addSpace(name, 60);
		} else if (StringUtils.isNotBlank(f61)) {
			// 61.6原样返回
			return f61.length() > 32 ? f61.substring(32) : "";
		}
		return "";
	}

	/**
	 * 返回证件类型对应的银链编号
	 * 
	 * @param idType
	 * @return
	 */
	public String getIdTypeVal(IdType idType) {
		for (IdType i : IdType.values()) {
			if (i == idType) {
				return i.getIdTypeVal();
			}
		}
		// 没有找到符合的证件类型
		return "99";
	}

	/**
	 * 格式化B061CustInfo.1证件域
	 * 
	 * @param idNo
	 * @return
	 */
	public String formatB061CustInfo_1(IdType idType, String idNo) {
		StringBuffer result = new StringBuffer();
		// 身份证类型
		if (idType == IdType.I) {
			result.append(String.format("%012d", 0) + StringUtils.right(idNo, 6));
		} else {
			result.append(idNo);
		}
		result.append(addSpace(idNo, 20));
		// 证件类型枚举转成数字
		return getIdTypeVal(idType) + result.toString();
	}

	/**
	 * 添加空格
	 * 
	 * @param s
	 * @param len
	 * @return SPACE.len = s.length + SPACE*n
	 */
	public String addSpace(String s, int len) {
		return String.format("%0" + (len - s.getBytes().length) + "d", 0).replace("0", SPACE);
	}

	/**
	 * 取现，转出，存款需返回的54域 （账户余额和可用余额）
	 * 
	 * @param context
	 * @return
	 */
	public String getB054RespAmt(AuthContext context) {
		StringBuffer b54 = new StringBuffer();

		BigDecimal balance = context.getAccount().getCurrBal();
		BigDecimal customOTB = context.getTxnInfo().getCustomerOTB();
		BigDecimal accountOTB = context.getTxnInfo().getAccountOTB();
		// 可用余额取账户和客户中小的值
		BigDecimal currOTB;

		if (customOTB.compareTo(accountOTB) > 0)
			currOTB = accountOTB;
		else
			currOTB = customOTB;
		// 账户余额符号判断 、余额补零

		// 域长度 账户类型 余额类型 货币代码 余额符号 余额 账户类型 余额类型 货币代码 余额符号 余额
		currOTB = currOTB.multiply(BigDecimal.valueOf(100));
		balance = balance.multiply(BigDecimal.valueOf(100));
		DecimalFormat df = new DecimalFormat("000000000000");
		// 余额 balance 是 小于0送C 大于等于0送D ,otb是 小于0送D 反之送C,cashotb和otb一样
		String balanceMark = "D";
		String otbMark = "C";
		if (balance.compareTo(BigDecimal.ZERO) < 0) {
			balanceMark = "C";
			balance = balance.abs();
		} else {
			balanceMark = "D";
		}

		if (currOTB.compareTo(BigDecimal.ZERO) < 0) {
			otbMark = "D";
			currOTB = BigDecimal.ZERO;
		} else {
			otbMark = "C";
		}
		// CCS 不做变长处理
		b54.append("3001").append(context.getTxnInfo().getChbCurr()).append(balanceMark).append(df.format(balance))
		// .append(StringUtils.leftPad(balance.toString(), 12, "0"))
				.append("3002").append(context.getTxnInfo().getChbCurr()).append(otbMark).append(df.format(currOTB));
		// .append(StringUtils.leftPad(currOTB.toString(), 12, "0"));

		logger.debug("54域账户余额和可用余额返回域=" + b54);

		return b54.toString();
	}

	/**
	 * 取现需返回的54域 （取现余额）
	 * 
	 * @param context
	 * @return
	 */
	public String getB054RespAmtWithCash(AuthContext context) {

		String b054Resp = getB054RespAmt(context);
		// 判断54为空则返回null
		if (StringUtils.isBlank(b054Resp)) {
			return null;
		}

		if (null != context.getTxnInfo() && context.getTxnInfo().getInputSource() == InputSource.BANK) {
			// 获取CASHOTB
			BigDecimal cashOTB = context.getTxnInfo().getCashOTB();
			BigDecimal custOtb = context.getTxnInfo().getCustomerOTB();
			/** 取现额度：与客户额度比较。取小 **/
			cashOTB = custOtb.compareTo(cashOTB) > 0 ? cashOTB : custOtb;
			cashOTB = cashOTB.multiply(BigDecimal.valueOf(100));

			DecimalFormat df = new DecimalFormat("000000000000");
			// 获得3001和3002
			StringBuffer b054RespBank = new StringBuffer(b054Resp);
			// 追加3003
			String cashMark = "C";
			if (cashOTB.compareTo(BigDecimal.ZERO) < 0) {
				cashMark = "D";
				cashOTB = BigDecimal.ZERO;
			} else {
				cashMark = "C";
			}
			b054RespBank.append("3003").append(context.getTxnInfo().getChbCurr()).append(cashMark).append(df.format(cashOTB));

			return b054RespBank.toString();
		}

		return b054Resp;
	}

	/**
	 * ATM交易返回b054Resp ，柜台交易 需返回 b061CustInfo 和b054Resp
	 * 
	 * @param context
	 * @param responseInfo
	 * @param message
	 */
	public void setB054RespAndB061CustInfo(AuthContext context, RespInfo responseInfo, CupMsg message) {

		switch (context.getTxnInfo().getInputSource()) {
		case BANK:
			responseInfo.setB054Resp_AddAmt(getB054RespAmtWithCash(context));
			// 行内的柜台交易，返回证件号
			if (context.getTxnInfo().getTransTerminal() == AuthTransTerminal.OTC) {
				responseInfo.setB061CustInfo_Cardholer(getRespB061CustInfo(context, message.field(61)));
			}
			break;
		case CUP:
			responseInfo.setB054Resp_AddAmt(getB054RespAmt(context));
			break;
		default:
			break;
		}
	}

	/**
	 * 联机逻辑卡表自动更新累计额
	 * 
	 * @param context
	 * @throws AuthException
	 */
	public void autoUpdAccumulativeCardO(AuthContext context) throws AuthException {
		logger.debug("---------AutoUpdAccumulativeCardO ------ ");
		// 卡
		CcsCardO card = context.getCard();
		Date bizDate = context.getTxnInfo().getBizDate();
		try {
			// 上次联机更新业务日期
			Date lastBizDate = card.getLastUpdateBizDate();

			if (null == lastBizDate) {
				card.setLastUpdateBizDate(bizDate);
			}
			// lastBizDate初始的时候是允许为空的，所以这边需加null判断
			if (null != card && null != lastBizDate) {
				// 账单日-day[2]
				int cycle = Integer.valueOf(context.getAccount().getCycleDay().trim());

				// 如果当前日期==上一次卡片更新日期,则跳出更新
				if (bizDate.compareTo(lastBizDate) == 0) {
					return;
				}
				// 如果当前bizDate > 逻辑卡lastBizDate
				if (bizDate.compareTo(lastBizDate) > 0) {
					// 每日累计额清零
					card.setDayUsedAtmAmt(BigDecimal.ZERO);
					card.setDayUsedAtmNbr(0);
					card.setDayUsedCashAmt(BigDecimal.ZERO);
					card.setDayUsedCashNbr(0);
					card.setDayUsedRetailAmt(BigDecimal.ZERO);
					card.setDayUsedRetailNbr(0);
					card.setDayUsedXfroutAmt(BigDecimal.ZERO);
					card.setDayUsedXfroutNbr(0);
					// 将cardo银联境外atm取现单日累计设为0
					card.setDayUsedAtmCupxbAmt(BigDecimal.ZERO);
					// 每日自动解锁密码次数
					if (null != context.getProductCredit()) {
						if (context.getProductCredit().pinBlockResetInd == PinBlockResetInd.D) {
							card.setPinTries(0);
						}
						if (context.getProductCredit().inqPinBlockResetInd == PinBlockResetInd.D) {
							card.setInqPinTries(0);
						}
					}
				}

				// 当月账单日
				Date cycleDate = DateUtils.setDays(bizDate, cycle);
				// 上月账单日
				Date prevCycleDate = DateUtils.addMonths(DateUtils.setDays(bizDate, cycle), -1);

				/*
				 * 业务日期大于当月账单日,且最后交易日期小于等于当月账单日; 或者
				 * 业务日期小于等于当月账单日,且最后交易日期小于等于上月账单日;
				 */
				if ((bizDate.compareTo(cycleDate) > 0 && lastBizDate.compareTo(cycleDate) <= 0) || (bizDate.compareTo(cycleDate) <= 0 && lastBizDate.compareTo(prevCycleDate) <= 0)) {
					// 账周累计额清零
					card.setCtdCashAmt(BigDecimal.ZERO);
					card.setCtdNetAmt(BigDecimal.ZERO);
					card.setCtdUsedAmt(BigDecimal.ZERO);
				}

				// 更新日期
				card.setLastUpdateBizDate(bizDate);
			}
		} catch (Exception e) {
			throwAuthException(AuthReason.S008, "自动更新累计时出现异常");
		}
	}

	public String getResponseCode(AuthReason authReason, AuthAction authAction) {
		String responseCode = "90";// 对于没配置参数或其他原因拿不到reasonMapping的reason，默认返回90做异常返回码
		AuthReasonMapping mapping = parameterFacility.retrieveParameterObject(InputSource.CUP + "|" + authReason, AuthReasonMapping.class);
		if (mapping != null) {
			// 根据Action来确定该取哪个返回码
			switch (authAction) {
			case D:
				responseCode = mapping.declineResponse;
				break;
			case P:
				responseCode = mapping.pickupResponse;
				break;
			case C:
				responseCode = mapping.callResponse;
				break;
			case A:
				// 对于approve的action，如果returnCode不是00，则取该approve下的returnCode值，实际上不用判断，直接取来覆盖就好
				responseCode = mapping.approveResponse;
				break;
			default:
				break;
			}
		}
		return responseCode;

	}

	/***
	 * 记录原交易信息
	 * 
	 * @param orig
	 * @return
	 */
	public RespInfo updateResponseInfo(CcsAuthmemoO orig) {

		RespInfo responseInfo = new RespInfo();

		if (orig == null)
			return responseInfo;

		// 原交易MTI
		responseInfo.setOrigMti(orig.getMti());
		// 原始受理机构号
		responseInfo.setOrigAcqInstId(orig.getOrigAcqInstId());
		// 原始授权金额
		responseInfo.setOrigChbTxnAmt(orig.getChbTxnAmt());
		// 原始转发机构号
		responseInfo.setOrigFwdInstId(orig.getFwdInstId());
		// 原交易LOG键值
		responseInfo.setOrigLogKv(orig.getLogKv());
		// 原始系统跟踪号
		if (orig.getB011Trace() != null) {
			responseInfo.setOrigTraceNo(Integer.valueOf(orig.getB011Trace()));
		}
		// 原始交易日期
		responseInfo.setOrigTransDate(orig.getLogOlTime());
		// 原始交易金额
		responseInfo.setOrigTxnAmt(orig.getTxnAmt());
		// 原交易处理码
		responseInfo.setOrigTxnProc(orig.getB003ProcCode());
		// 原交易类型
		responseInfo.setOrigTxnType(orig.getTxnType());
		// 原交易键值1
		responseInfo.setOrigTxnVal1(orig.getB007TxnTime() + orig.getB011Trace() + orig.getB032AcqInst() + orig.getB033FwdIns());
		// 原交易键值2
		responseInfo.setOrigTxnVal2(orig.getB002CardNbr() + orig.getAuthCode() + orig.getB042MerId());
		// 原贷记卡系统的处理日期
		responseInfo.setOrigBizDate(orig.getLogBizDate());
		// 原交易授权码
		responseInfo.setOrigAuthCode(orig.getAuthCode());

		return responseInfo;

	}

	/**
	 * 更新账户联机表中的最近维护日期
	 * 
	 * @param context
	 * @param responseInfo
	 */
	public void updateAcctODate(AuthContext context, RespInfo responseInfo) {
		if (null == context.getAccount() || null == responseInfo) {
			return;
		}
		CcsAcctO acct = context.getAccount();
		TxnInfo txnInfo = context.getTxnInfo();
		acct.setLastUpdateBizDate(txnInfo.getBizDate());
		// 判断origBizDate是否为空
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
		String bizDate = df.format(txnInfo.getBizDate());
		String origBizDate = responseInfo.getOrigBizDate() == null ? "0" : df.format(responseInfo.getOrigBizDate());

		// 跨日匹配是为24*7检查使用,目前只考虑成功的预授权完成 结算通知和隔日冲正,隔日多重撤冲和孤立确认不予考虑
		if (origBizDate == null || txnInfo.getAuthAction() != AuthAction.A || (!txnInfo.getResponsCode().equals("00") && !txnInfo.getResponsCode().equals("11"))) {
			return;
		}
		if ((txnInfo.getTransType() == AuthTransType.PAComp || txnInfo.getTransType() == AuthTransType.AdviceSettle || (txnInfo.getTransDirection() != AuthTransDirection.Normal && txnInfo
				.getTransDirection() != AuthTransDirection.Advice)) && bizDate.compareTo(origBizDate) > 0) {
			acct.setLastMatchedOvernightDate(txnInfo.getBizDate());
		}
	}

	/**
	 * reasonMap转成String
	 * 
	 * @param reasons
	 * @return
	 */
	public static String rejectReason2String(Map<AuthReason, AuthAction> reasons) {
		if (null == reasons || reasons.size() == 0) {
			return null;
		}
		StringBuffer result = new StringBuffer();
		for (Entry<AuthReason, AuthAction> entry : reasons.entrySet()) {
			result.append(entry.getKey());
			result.append((null == entry.getValue() || "".equals(entry.getValue())) ? SPACE : entry.getValue());
		}
		return result.toString();
	}

	/**
	 * 使用[*]号替换姓名中的第一个字
	 * 
	 * @param name
	 * @return
	 */
	public String getRespChnName(String name) {
		return StringUtils.isNotBlank(name) ? "*" + name.trim().substring(1) : name;
	}

	

	
	
	
	

	

	

	/**
	 * 检查步骤一：原交易集合为空,则返回找不到原交易25
	 * 
	 * @param iterable
	 *            原交易集合
	 * @param isReturnNull
	 *            没有找到原交易时：true == 返回null , false == [ AuthException -
	 *            找不到原交易(25) ]
	 * @return
	 * @throws AuthException
	 */
	public CcsAuthmemoO checkOrigListIsNotNull(Iterator<CcsAuthmemoO> it, boolean isReturnNull) throws AuthException {
		int count = 0;
		CcsAuthmemoO orig = null;
		// 记录条数大于一时，取第一条，记录logger为查询出现多条原交易
		while (it.hasNext()) {
			if (count == 0) {
				orig = it.next();
				logger.debug(count + " - 获取第一条原交易: " + "[ log_kv : " + orig.getLogKv() + " , direction : " + orig.getTxnDirection() + " , type : " + orig.getTxnType() + " , ststus : "
						+ orig.getAuthTxnStatus() + " ]");
			} else {
				CcsAuthmemoO other = it.next();
				logger.debug(count + " - 查询出多条原交易 : " + "[ log_kv : " + other.getLogKv() + " , direction : " + other.getTxnDirection() + " , type : " + other.getTxnType() + " , ststus : "
						+ other.getAuthTxnStatus() + " ]");
			}
			count++;
		}
		if (orig == null) {
			if (isReturnNull) {
				return null;
			} else {
				throwAuthException(AuthReason.R014, "找不到原交易(25) [orig:null]");
			}
		}
		return orig;
	}

	/**
	 * 检查步骤二： 1)-原交易被拒绝FinalAction != A 或 状态为AuthTxnStatus == E,则返回 - 无效交易(12)
	 * 2)-原交易状态为AuthTxnStatus != N,则返回 - 故障交易(22)
	 * 
	 * @param orig
	 * @throws AuthException
	 */
	public void checkOrigStatusIsInvalid(CcsAuthmemoO orig) throws AuthException {
		if (orig.getFinalAction() != AuthAction.A && orig.getAuthTxnStatus() == AuthTransStatus.E) {
			throwAuthException(AuthReason.R016, "无效交易(12) [orig_txn_status:" + orig.getAuthTxnStatus() + "]");
		}
		if (orig.getAuthTxnStatus() != AuthTransStatus.N && orig.getAuthTxnStatus() != AuthTransStatus.D && orig.getTxnDirection() != AuthTransDirection.Advice) {
			throwAuthException(AuthReason.R015, "故障交易(22) [orig_txn_status:" + orig.getAuthTxnStatus() + "]");
		}
	}

	/**
	 * 检查步骤三：原交易状态正常AuthTxnStatus=N且交易金额不等,则返回原始交易金额不匹配64
	 * 
	 * @param orig
	 * @param txnInfo
	 * @throws AuthException
	 */
	public void checkOrigTxnAmtIsNotEqual(CcsAuthmemoO orig, TxnInfo txnInfo) throws AuthException {
		if (orig.getAuthTxnStatus() == AuthTransStatus.N || orig.getAuthTxnStatus() == AuthTransStatus.D) {
			if (orig.getTxnAmt().compareTo(txnInfo.getTransAmt()) != 0) {
				throwAuthException(AuthReason.R017, "原始交易金额不匹配(64) [orig_txn_amt:" + orig.getTxnAmt() + "] , [curr_txn_amt:" + txnInfo.getTransAmt() + "]");
			}
		}
	}

	/**
	 * 检查步骤 [委托业务或开通无卡自助]：原交易状态正常AuthTxnStatus != N ;则返回 无效交易(12)
	 * 
	 * @param orig
	 * @param txnInfo
	 * @throws AuthException
	 */
	public void checkOrigStatusIsNotNormal(CcsAuthmemoO orig) throws AuthException {
		if (orig.getAuthTxnStatus() != AuthTransStatus.N && orig.getAuthTxnStatus() != AuthTransStatus.D) {
			throwAuthException(AuthReason.R016, "无效交易(12) [orig_txn_status:" + orig.getAuthTxnStatus() + "]");
		}
	}

	/**
	 * 根据reason 返回异常并记录日志
	 * 
	 * @param reason
	 * @param desc
	 * @throws AuthException
	 */
	public void throwAuthException(AuthReason reason, String desc) throws AuthException {
		if (logger.isDebugEnabled() && StringUtils.isNotEmpty(desc))
			logger.debug("# 异常交易: " + desc);
		throw new AuthException(reason, pcs.getAuthDefaultAction(reason));
	}

	/**
	 * 根据reason 返回异常并记录日志
	 * 
	 * @param reason
	 * @param desc
	 * @throws AuthException
	 */
	public void throwAuthException(Logger log, Exception e, AuthReason reason, String desc) throws AuthException {
		if (log.isDebugEnabled() && StringUtils.isNotEmpty(desc))
			log.debug("# 异常交易: " + desc, e);
		throw new AuthException(reason, pcs.getAuthDefaultAction(reason));
	}


	/**
	 * [分期第三步]返回57域（分期信息）
	 * 
	 * @param loanInfo
	 * @return
	 */
	public String generateB057Loan(LoanInfo loanInfo, TxnInfo txnInfo) {
		CcsLoanReg loanReg = loanInfo.getCcsLoanReg();
		logger.debug("# [分期第三步]获取分期注册表(TM_LOAN_REG):{}", loanReg.convertToMap());
		// 用法标志、n2
		StringBuffer b057 = new StringBuffer("IP");
		// 首期还款金额、n12
		b057.append(StringUtils.leftPad(loanReg.getLoanFirstTermPrin().setScale(2, BigDecimal.ROUND_HALF_UP).movePointRight(2).toString(), 12, "0"));
		// 还款币种、n3
		b057.append(txnInfo.getChbCurr());
		/**
		 * 持卡人分期手续费 n12，由发卡方填写，右对齐左补零。若无手续费则本字段以全零填充。 手续费币种由“还款币种”表示。
		 * 当为1次性支付手续费方式时代表持卡人1次性支付的手续费金额， 若为分期支付手续费方式时本字段以全零填充。
		 */
		b057.append(StringUtils.leftPad(loanInfo.getLoanFeeMethod() == LoanFeeMethod.F ? loanReg.getLoanInitFee().setScale(2, BigDecimal.ROUND_HALF_UP).movePointRight(2).toString() : "", 12, "0"));
		// 商户手续费率、n6、 值为扣率*10000 (ProgramFeeDef.merFeeRate)
		if(loanInfo.getProgramFeeDef().merFeeRate != null){
			b057.append(StringUtils.leftPad(loanInfo.getProgramFeeDef().merFeeRate.movePointRight(4).toString(), 6, "0"));
		}else{
			b057.append("000000");
		}

		/**
		 * 保留使用 ans50，保留给分期付款方式使用。 该域细分定义分期付款特色域，包含奖励积分，分期付款手续费支付方式，
		 * 当为分期支付手续费时的首期手续费和每期手续费。
		 */
		// 积分奖励、n12、12个零填充
		b057.append(StringUtils.leftPad("", 12, "0"));
		// 手续费支付方式[ 0 : F , 1 : E ]、an1
		b057.append(loanInfo.getLoanFeeMethod() == LoanFeeMethod.F ? "0" : "1");
		// 首期手续费、n12、当位置2取值为0 时以0 填充
		b057.append(StringUtils.leftPad(loanInfo.getLoanFeeMethod() == LoanFeeMethod.F ? "" : loanReg.getLoanFirstTermFee().setScale(2, BigDecimal.ROUND_HALF_UP).movePointRight(2).toString(), 12,
				"0"));
		// 每期手续费、n12、当位置2取值为0 时以0 填充
		b057.append(StringUtils.leftPad(loanInfo.getLoanFeeMethod() == LoanFeeMethod.F ? "" : loanReg.getLoanFixedFee().setScale(2, BigDecimal.ROUND_HALF_UP).movePointRight(2).toString(), 12, "0"));
		// 保留、n13、13个空格填充
		b057.append(StringUtils.leftPad("", 13, SPACE));
		logger.debug("# [分期第三步]返回分期信息(57域):[{}]", b057.toString());
		return b057.toString();
	}

	/**
	 * 48域AO为07、08或者15的账户验证交易，返回57域的AS+IA用法填入卡片所属地区
	 * 
	 * @param requestMessage
	 * @return
	 */
	public String generateB057AcctVry(YakMessage requestMessage) {
		CupMsg message = new CupMsg(requestMessage);
		// 得到48域AO用法的值
		String b048Add = message.f48AOVerify();
		String b048AddIA = message.f48IAVerify();//AIC2.7 银联升级
		if (b048Add.equals("07") || b048Add.equals("08") || b048Add.equals("15") || StringUtils.isNotBlank(b048AddIA)) {
			logger.debug("48域AO为07、08或者15的账户验证交易，返回57域的AS+IA用法填入卡片所属地区：{}", requestMessage.getBody(100));
			// AS用法为TLV的构造方式，value为8位定长
			// 用法标志Tag
			StringBuffer b057 = new StringBuffer("ASIA");
			// value的长度L
			b057.append("008");
			// value V为卡片所属地区，也是就报文中的100域
			String b100 = requestMessage.getBody(100);
			if (b100.length() >= 8) {
				b100 = StringUtils.right(b100, 8);
			} else {
				b100 = StringUtils.leftPad(b100, 8, "0");
			}
			b100 = StringUtils.left(b100, 4);
			String l = "0000";
			b057.append(b100).append(l);
			logger.debug("账户验证返回的57域为：{}", b057.toString());
			return b057.toString();
		} else {
			return null;
		}
	}

	/**
	 * 48域为16的账户验证，用于查询是否开通无卡自助，返回是否开通无卡自助和手机号码
	 * 
	 * @param requestMessage
	 * @param responseInfo
	 * @return
	 */
	public String generateB057AcctVry(RespInfo responseInfo, AuthContext context) {
		String b057 = responseInfo.getB057_AddPriv();
		StringBuffer sb057;
		if (b057 == null) {
			sb057 = new StringBuffer("ASSE");
		} else {
			sb057 = new StringBuffer(b057 + "SE");
		}
		// 长度
		String l = "023";
		String ind = responseInfo.getOpenNoCardSelf() == Indicator.N ? "000" : "101";
		String mobileNo = context.getCustomer().getMobileNo();
		if (mobileNo == null) {
			mobileNo = StringUtils.leftPad("", 11, "0");
		}
		mobileNo = StringUtils.right(mobileNo, 11);
		// 替换手机号码中间4个数字为*
		mobileNo = mobileNo.substring(0, mobileNo.length() - (mobileNo.substring(3)).length()) + "****" + mobileNo.substring(7);
		mobileNo = StringUtils.rightPad(mobileNo, 20, "");
		mobileNo = responseInfo.getOpenNoCardSelf() == Indicator.N ? addSpace("", 20) : mobileNo;
		sb057.append(l).append(ind).append(mobileNo);
		return sb057.toString();
	}

	/**
	 * 获取账户下分期交易未终结的所有交易的分期本金
	 * 
	 * @param account
	 * @return
	 */
	public BigDecimal sumloanInitPrin(CcsAcctO account) {
		QCcsLoan loan = QCcsLoan.ccsLoan;
		BigDecimal sum = new JPAQuery(em).from(loan)
				.where(loan.acctNbr.eq(account.getAcctNbr()).and(loan.acctType.eq(account.getAcctType())).and(loan.loanStatus.ne(LoanStatus.T)).and(loan.loanStatus.ne(LoanStatus.F)))
				.singleResult(loan.loanInitPrin.sum());
		return sum == null ? BigDecimal.ZERO : sum;
	}

	/**
	 * 获取账户下分期交易未达账的所有交易的分期交易金额
	 * 
	 * @param account
	 * @return
	 */
	public BigDecimal sumUnmatchLoanChbTxnAmt(CcsAcctO account) {
		QCcsAuthmemoO unmatch = QCcsAuthmemoO.ccsAuthmemoO;
		BigDecimal sum = new JPAQuery(em)
				.from(unmatch)
				.where(unmatch.acctNbr.eq(account.getAcctNbr()).and(unmatch.acctType.eq(account.getAcctType())).and(unmatch.txnType.eq(AuthTransType.Loan)).and(unmatch.finalAction.eq(AuthAction.A))
						.and(unmatch.authTxnStatus.eq(AuthTransStatus.N))).singleResult(unmatch.chbTxnAmt.sum());
		return sum == null ? BigDecimal.ZERO : sum;
	}

	/**
	 * 获取分期交易信息（ 条件:卡号,b007TxnTime,b011Trace,b032AcqInst,b033FwdIns,授权码）
	 * 
	 * @param unmatch
	 * @return
	 * @throws AuthException
	 */
	public CcsLoanReg findLoanRegWithB7B11B32B33(CcsAuthmemoO unmatch, LoanRegStatus loanRegStatus, ManualAuthFlag manualAuthFlag) throws AuthException {
		QCcsLoanReg qCcsLoanReg = QCcsLoanReg.ccsLoanReg;
		CcsLoanReg loanReg = new JPAQuery(em)
				.from(qCcsLoanReg)
				.where(qCcsLoanReg.cardNbr.eq(unmatch.getB002CardNbr()).and(qCcsLoanReg.b007TxnTime.eq(unmatch.getB007TxnTime())).and(qCcsLoanReg.b011Trace.eq(unmatch.getB011Trace())).and(qCcsLoanReg.b032AcqInst.eq(unmatch.getB032AcqInst()))
						.and(qCcsLoanReg.b033FwdIns.eq(unmatch.getB033FwdIns())).and(qCcsLoanReg.origAuthCode.eq(unmatch.getAuthCode())).and(qCcsLoanReg.loanRegStatus.eq(loanRegStatus))).singleResult(qCcsLoanReg);
		if (loanReg == null) {
			// 分期或小额贷款交易撤销和冲正时，如果是人工授权发起的，则无论有没有找到分期注册表信息，都允许授权人工释放-ymk20140507
			if (manualAuthFlag.equals(ManualAuthFlag.N)) {
				throwAuthException(AuthReason.R018, "没有找到分期注册表信息");
			}
		}
		return loanReg;
	}

	/**
	 * 保存分期注册表信息
	 * 
	 * @param context
	 * @param message
	 * @param responseInfo
	 */
	public void saveLoanReg(AuthContext context, YakMessage message, RespInfo responseInfo) {
		LoanInfo loanInfo = context.getLoanInfo();
		CcsLoanReg ccsLoanReg = loanInfo.getCcsLoanReg();
		ccsLoanReg.setB007TxnTime(message.getBody(7).trim());
		ccsLoanReg.setB011Trace(message.getBody(11).trim());
		ccsLoanReg.setB032AcqInst(message.getBody(32).trim());
		ccsLoanReg.setB033FwdIns(message.getBody(33).trim());
		ccsLoanReg.setOrigAuthCode(responseInfo.getB038_AuthCode());
		ccsLoanReg.setOrigTxnAmt(context.getTxnInfo().getChbTransAmt());
		
		// 解决refNbr重复问题
		ccsLoanReg.setRefNbr(txnUtils.getRefnbr(message.getBody(7).trim(), responseInfo.getB038_AuthCode(), ccsLoanReg.getRefNbr()));

		// b007TxnTime没有年，默认使用联机的年份
		Calendar c = Calendar.getInstance();
		c.set(Calendar.MONTH, Integer.valueOf(message.getBody(7).substring(0, 2)) - 1);
		c.set(Calendar.DAY_OF_MONTH, Integer.valueOf(message.getBody(7).substring(2, 4)));
		ccsLoanReg.setOrigTransDate(c.getTime());

		// 小额贷浮动比例
		ccsLoanReg.setFloatRate(loanInfo.getFloatRate());
		// 匹配入账交易标识
		ccsLoanReg.setMatched(Indicator.N);
		logger.debug("LoanReg:{}", ccsLoanReg.convertToMap());
		rCcsLoanReg.save(ccsLoanReg);
	}

	/**
	 * 封装ic交易返回时的ARPC(TLV格式)
	 * @param requestMessage 
	 * 
	 * @param context
	 * @return
	 * @throws AuthException 
	 */
	public String generateArpc(YakMessage requestMessage, AuthContext context) throws AuthException {
		
		// E3BCA8AD5725E3753030
		String arpc = context.getMediumInfo().getRespf55();
		StringBuffer arpcTLV = new StringBuffer();
		// ARPC
		if(StringUtils.isNotEmpty(arpc)){
			arpcTLV.append("91").append("0A").append(arpc);
		}
		
		// 银联查询、建立、解除委托、取现、消费、预授权返回9F36
		AuthTransType att = context.getTxnInfo().getTransType();
		String f55=requestMessage.getBody(55);
		if (StringUtils.isBlank(f55)) {
			return null;
		}
		Map<String, TLVEntity> f55Map = Cup55Utils.cup55ToMap(f55.toUpperCase());
		
		if (context.getTxnInfo().getInputSource() == InputSource.CUP 
				&& (att == AuthTransType.Inq || att == AuthTransType.ContractBuildUp || att == AuthTransType.ContractTermination || att == AuthTransType.Auth || att == AuthTransType.Cash || att == AuthTransType.PreAuth)){
			TLVEntity _9f36 = f55Map.get("9F36");
			arpcTLV.append(_9f36 != null ? _9f36.tlv() : "");
		}
		
		logger.debug("# ic交易成功, ARPC:{}", arpcTLV.toString());
		return arpcTLV.toString();
	}

	public String genMicroCredit57(RespInfo responseInfo, AuthContext context) {
		String b057 = responseInfo.getB057_AddPriv();
		// 贷款借据号
		String loanReceiptNbr = context.getLoanInfo().getCcsLoanReg().getDueBillNo();
		StringBuffer sb057;
		if (b057 == null) {
			sb057 = new StringBuffer("IP");
		} else {
			sb057 = new StringBuffer(b057 + "IP");
		}
		// 加IP一共85字节,右补空格
		loanReceiptNbr = StringUtils.rightPad(loanReceiptNbr, 83);
		sb057.append(loanReceiptNbr);
		return sb057.toString();
	}

	/**
	 * 行内还款、转入、取现(包含销户结清)、转出(包含销户结清)交易 生成48域返回姓名
	 * 
	 * @return HexB048Add
	 */
	public String genBankB048Add(AuthContext context) {
		TxnInfo txnInfo = context.getTxnInfo();
		String name = context.getCustomer().getName();
		String respf048 = null;
		// 行内正向交易
		if (txnInfo.getInputSource() == InputSource.BANK && txnInfo.getTransDirection() == AuthTransDirection.Normal) {
			// 小额贷转出交易48域不返回姓名
			if (context.getProduct().productType.getLimitCategory() == LimitCategory.MicroCreditLimit && txnInfo.getTransType() == AuthTransType.TransferDeditDepos && !txnInfo.isCloseSettleAcct() ) {
				//TODO 小额贷需要返回贷款信息，格式等于上送报文48域。暂时无添加
				respf048 = null;
			}
		
			// 柜台渠道的还款、转入、取现(包含销户结清)、转出(包含销户结清)交易
			if (txnInfo.getTransTerminal() == AuthTransTerminal.OTC
					&& (txnInfo.getTransType() == AuthTransType.Credit 
					|| txnInfo.getTransType() == AuthTransType.TransferCredit 
					|| txnInfo.getTransType() == AuthTransType.Cash 
					|| txnInfo.getTransType() == AuthTransType.TransferDeditDepos)) {
				// 返回姓名
				respf048 = name + addSpace(name, 40);
			}
			// ATM渠道的取现、销户结清转出交易
			if (txnInfo.getTransTerminal() == AuthTransTerminal.ATM
					&& (txnInfo.getTransType() == AuthTransType.Cash 
						|| (txnInfo.getTransType() == AuthTransType.TransferDeditDepos && txnInfo.isCloseSettleAcct()))
					) {
				// 返回姓名
				respf048 = name + addSpace(name, 40);
			}
			
			return respf048 != null ? Hex.encodeHexString(respf048.getBytes()) : null;
		} else {
			return null;
		}
	}
	
	/**
	 * 匹配原交易
	 * 
	 * @param request
	 * @param context
	 * @return
	 */
	public Iterator<CcsAuthmemoO> findOriginalTrans(YakMessage request, AuthContext context) {
		CupMsg msg = new CupMsg(request);
		QCcsAuthmemoO unmo = QCcsAuthmemoO.ccsAuthmemoO;
		// 1、查询原存款交易
		return rCcsAuthmemoO.findAll(
								 unmo.b032AcqInst.eq(request.getBody(32).trim())
							.and(unmo.b033FwdIns.eq(request.getBody(33).trim()))
							// B090OrigData拆分
							.and(unmo.b007TxnTime.eq(msg.checkDummyStr(msg.getF090_3_OrigSysDateTime())))
							.and(unmo.b011Trace.eq(msg.checkDummyStr(msg.getF090_2_OrigSysTraceId())))
							.and(unmo.b032AcqInst.eq(msg.checkDummyStr(msg.getF090_4_OrigAcqInstId())))
							.and(unmo.b033FwdIns.eq(msg.checkDummyStr(msg.getF090_5_OrigFwdInstId())))
							.and(unmo.logBizDate.eq(context.getTxnInfo().getBizDate()))
						    // 新增
							.and(unmo.b002CardNbr.eq(request.getBody(2)))
							.and(unmo.txnDirection.eq(AuthTransDirection.Normal))
						    // 移除
						    //.and(unmo.b004Amt.eq(msg.getF04_Amount()))
							//.and(unmo.authTxnStatus.eq(AuthTransStatus.N))
							//.and(unmo.finalAction.eq(AuthAction.A))
						).iterator();
	}
	
	/**
	 * 匹配原交易 用于预授权完成交易
	 * 
	 * @param request
	 * @param context
	 * @return
	 */
	public Iterator<CcsAuthmemoO> findPAOriginalTrans(YakMessage request, AuthContext context) {
		CupMsg msg = new CupMsg(request);
		QCcsAuthmemoO unmo = QCcsAuthmemoO.ccsAuthmemoO;
		return rCcsAuthmemoO.findAll(
				 unmo.b002CardNbr.eq(msg.checkDummyStr(request.getBody(2)))
			.and(unmo.txnType.eq(AuthTransType.PreAuth))
			.and(unmo.authCode.eq(msg.checkDummyStr(request.getBody(38))))
			.and(unmo.b042MerId.eq(msg.checkDummyStr(request.getBody(42))))
			.and(unmo.logBizDate.gt(DateUtils.addDays(context.getTxnInfo().getBizDate(), -45)))
			.and(unmo.txnDirection.eq(AuthTransDirection.Normal))
		    // 新增
			.and(unmo.b002CardNbr.eq(request.getBody(2)))
		    // 移除
			//.and(unmo.authTxnStatus.eq(AuthTransStatus.N))
			//.and(unmo.finalAction.eq(AuthAction.A))
		).iterator();
	}
	
	/**
	 * 检查并返回原交易信息
	 * @param request
	 * @param context
	 * @return
	 * @throws AuthException
	 */
	public CcsAuthmemoO checkOrigStatistics(YakMessage request , AuthContext context) throws AuthException{
		// 1.0 匹配原交易
		Iterator<CcsAuthmemoO> it = findOriginalTrans(request, context);
		//查出来的it只有一条数据，
		logger.debug("log键值————————————————————————————"+it.next().getLogKv());
		// 1.1 检查步骤一：原交易集合为空；则返回Null
		CcsAuthmemoO orig = this.checkOrigListIsNotNull(it, false);
		/**
		 * 1.2检查步骤二：
		 * 1)-原交易被拒绝（FinalAction != A ）且 状态为（AuthTxnStatus == E）；则返回 - 无效交易(12)
		 * 2)-原交易状态为（AuthTxnStatus != N）；则返回 - 故障交易(22)
		 */
		this.checkOrigStatusIsInvalid(orig);
		// 1.3 检查步骤三：原交易状态正常（AuthTxnStatus=N）且交易金额不等；则返回原是交易金额不匹配（64）
		this.checkOrigTxnAmtIsNotEqual(orig, context.getTxnInfo());
		context.getTxnInfo().setChbTransAmt(orig.getChbTxnAmt());
		context.getTxnInfo().setChbCurr(orig.getChbCurrency());
		return orig;
	}
	
	/**
	 * 预授权完成撤销、预授权完成冲正时，得到原预授权交易
	 * @param request
	 * @param context
	 * @return
	 * @throws AuthException
	 */
	public CcsAuthmemoO genOrgOrgTrans(YakMessage request, AuthContext context)throws AuthException{
		QCcsAuthmemoO unmo = QCcsAuthmemoO.ccsAuthmemoO;
		// 预授权完成交易
		CcsAuthmemoO origComplete = checkOrigStatistics(request, context);

		// 根据预授权完成的原交易键值，获取预授权交易
		CcsAuthmemoO orig = rCcsAuthmemoO.findOne(unmo.logKv.eq(origComplete.getOrigLogKv() == null ? 0 : origComplete.getOrigLogKv()));
		if (null == orig) {
			throw new AuthException(AuthReason.R009, pcs.getAuthDefaultAction(AuthReason.R009));
		}
		return orig;
	}
	
	
	/**
	 * 获取refNbr= b007TxnTime(后8位) +b038(后3位) +b037RefNbr(12位)
	 * 
	 * @param b007TxnTime
	 * @param b038
	 * @param b037RefNbr
	 * @return
	 */
	public String getRefnbr(String b007TxnTime, String b038, String b037RefNbr) {
		return formatStr(b007TxnTime, 8) + formatStr(b038, 3) + formatStr(b037RefNbr, 12);
	}
	
	
	/**
	 * 泛型字符串并截取x位
	 * 
	 * @param str
	 * @param i
	 * @return
	 */
	private String formatStr(String str, int i) {
		if(StringUtils.isNotBlank(str)){
			return str.substring(str.length()-i, str.length());
		}else{
			StringBuffer sb = new StringBuffer(i);
			for (int j = 0; j < i; j++) {
				sb.append("0");
			}
			return sb.toString();
		}
	}
	
	
	
}
