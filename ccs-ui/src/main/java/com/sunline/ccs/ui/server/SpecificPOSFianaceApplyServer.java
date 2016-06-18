package com.sunline.ccs.ui.server;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.expr.BooleanExpression;
import com.sunline.acm.service.api.GlobalManagementService;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ark.support.service.YakMessage;
import com.sunline.ark.support.utils.CodeMarkUtils;
import com.sunline.ccs.facility.TxnUtils;
import com.sunline.ccs.infrastructure.server.repos.RCcsAuthmemoO;
import com.sunline.ccs.infrastructure.server.repos.RCcsTxnAdjLog;
import com.sunline.ccs.infrastructure.shared.model.CcsAuthmemoO;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnAdjLog;
import com.sunline.ccs.infrastructure.shared.model.QCcsAuthmemoO;
import com.sunline.ccs.infrastructure.shared.model.QCcsTxnAdjLog;
import com.sunline.ccs.param.def.LoanMerchant;
import com.sunline.ccs.param.def.Program;
import com.sunline.ccs.param.def.TxnCd;
import com.sunline.ccs.param.def.enums.AuthTransTerminal;
import com.sunline.ccs.ui.server.commons.CPSBusProvide;
import com.sunline.ccs.ui.server.commons.CheckUtil;
import com.sunline.ccs.ui.shared.PublicConst;
import com.sunline.kylin.web.ark.client.JPAQueryFetchResponseBuilder;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.kylin.web.core.client.exception.FlatException;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.api.AuthorizationService;
import com.sunline.ppy.api.CustomAttributesKey;
import com.sunline.ppy.dictionary.enums.AuthTransType;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.InputSource;
import com.sunline.ppy.dictionary.enums.LoanFeeMethod;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.ManualAuthFlag;

/**
 * 特定POS分期申请
 * 
 * @author linxc 2015年6月22日
 *
 */
@Controller
public class SpecificPOSFianaceApplyServer {

	private static final long serialVersionUID = 1L;

	private static final String TPS_CUST_KEY_OPERATOR = "css.operatorID";

	private static final String TPS_CUST_KEY_TERMID = "css.termid";

	private static final String TPS_CUST_KEY_TXNCODE = "css.cpstxncode";

	private static final String TPS_CUST_KEY_TXNREFNO = "css.cpstxnrefnbr";

	private static final String TPS_CUST_VAL_TERMID = "cps-app";

	private static final String TPS_VAL_INST_ACQUIR_ID = "99999999";

	private static final String TPS_VAL_INST_FORWARD_ID = "99999999";

	private static final String TPS_VAL_MERCHANT_ID = "999999999999999";

	private static final String TPS_VAL_MTI = "0200";

	private static final String REMARK = "内管特定POS分期";

	private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMddHHmmss");

	private String dateTimeStr = "";

	private String traceNbrStr = "";

	private String amountStr = "";

	private String transCode = "T881";// 分期交易码，写死

	private String b022 = "012";// 满足分期验密规则，写死

	Logger log = LoggerFactory.getLogger(Logger.class);

	@PersistenceContext
	private EntityManager em;

	@Autowired
	private UnifiedParameterFacility facility;

	@Resource(name = "authorizationService")
	private AuthorizationService authorizationService;

	@Autowired
	private GlobalManagementService globalManagementService;

	@Autowired
	private TxnUtils txnUtils;

	@Autowired
	private CPSBusProvide cpsBusProvide;

	@Autowired
	private OperatorAuthUtil operatorAuthUtil;

	@Autowired
	private OpeLogUtil opeLogUtil;

	@Autowired
	private RCcsAuthmemoO uCcsAuthmemoO;

	@Autowired
	private RCcsTxnAdjLog rCcsTxnAdjLog;

	private QCcsAuthmemoO qCcsAuthmemoO = QCcsAuthmemoO.ccsAuthmemoO;

	private QCcsTxnAdjLog qCcsTxnAdjLog = QCcsTxnAdjLog.ccsTxnAdjLog;

	
	public Map<String, LoanMerchant> getLoanMerchant() throws FlatException {
		Map<String, LoanMerchant> merchantMap = facility.retrieveParameterObject(LoanMerchant.class);
		return merchantMap;
	}

	
	public Map<String, Program> getProgram() throws FlatException {
		Map<String, Program> programMap = facility.retrieveParameterObject(Program.class);
		// 分期活动中的分期 类型不是POS分期的去掉
		for(String key : programMap.keySet()) {
			if(!programMap.get(key).loanType.equals(LoanType.P)) {
				programMap.remove(key);
			}
		}
		return programMap;
	}

	
//	public FetchResponse getAuthUnTranList(FetchRequest request, String cardNo) throws FlatException {
//		log.debug("3500查询当天申请的授权信息:getAuthUnTranList卡号[{}]", cardNo);
//		// 查找卡片记录
//		CcsCard tmCard = cpsBusProvide.getTmCardTocardNbr(cardNo);
//		BigDecimal maxLoanAmt = this.genOperAuth().getAdjPosLoanMax();
//		JPAQuery query = new JPAQuery(em).from(qCcsAuthmemoO);
//		{
//			// 查询授权为入账中终端为内管，交易类型为分期的交易,并且交易金额小于操作员权限
//			BooleanExpression expr = qCcsAuthmemoO.org.eq(OrganizationContextHolder.getCurrentOrg()).and(
//					qCcsAuthmemoO.acctNo.eq(tmCard.getAcctNo()).and(
//							qCcsAuthmemoO.logicalCardNo.eq(tmCard.getLogicalCardNo())
//									.and(qCcsAuthmemoO.txnTerminal.eq(AuthTransTerminal.HOST))
//									.and(qCcsAuthmemoO.txnType.eq(AuthTransType.Loan))
//									.and(qCcsAuthmemoO.txnAmt.loe(maxLoanAmt))));
//			query = query.where(expr);
//			query.orderBy(qCcsAuthmemoO.logBizDate.desc());
//		}
//		return new JPAQueryFetchResponseBuilder(request, query).addFieldMapping(qCcsAuthmemoO).build();
//	}
//
//	@Transactional
//	
//	public void cancelLoanTrans(int logKey) throws FlatException {
//		log.info("cancelTrans: logKey=[" + logKey + "]");
//		/*----查询授权信息时已经控制权限，此处无需控制
//		/*---- 1.校验输入项目是否合法 ---*/
//		CcsAuthmemoO ccsAuthmemoO = rCcsAuthmemoO.findOne(logKey);
//		CheckUtil.rejectNull(ccsAuthmemoO, "未找到原交易记录[" + logKey + "]");
//		/*---- 2.组装金融交易报文 ---*/
//		amountStr = new String(MsgUtil.leftPad(ccsAuthmemoO.getB004().toString(), 12));
//		// 获取交易时间和创建流水号
//		long currentTimeMillis = System.currentTimeMillis();
//		dateTimeStr = simpleDateFormat.format(currentTimeMillis);
//		int traceNbr = (int) (currentTimeMillis % 1000) * 1000 + (int) (Math.random() * 1000);
//		traceNbrStr = new String(MsgUtil.leftPad(traceNbr + "", 6));
//		// 组装金融交易请求报文
//		YakMessage requestMessage = new YakMessage();
//		// Map<Integer, String> headAttributes =
//		// requestMessage.getHeadAttributes();
//		Map<Integer, String> bodyAttributes = requestMessage.getBodyAttributes();
//		Map<String, Serializable> customAttributes = requestMessage.getCustomAttributes();
//		// requestMessage.setSrcChannelId(TPS_INPUT_SOURCE); //inputsource =内管
//		// headAttributes.put(11, "420"); //MTI
//		bodyAttributes.put(2, ccsAuthmemoO.getB002()); // 介质卡号:B002
//		bodyAttributes.put(3, "200000");// 写死
//		bodyAttributes.put(4, amountStr); // 交易金额:b004，交易金额不带小数点，最右边两位视为小数位
//		if(null != ccsAuthmemoO.getB006()) {
//			bodyAttributes.put(6, ccsAuthmemoO.getB006().toString()); // 持卡人扣账金额
//			bodyAttributes.put(51, ccsAuthmemoO.getB051()); // 持卡人账户货币代码
//		}
//		bodyAttributes.put(7, dateTimeStr); // 交易时间(客服的自然时间):b007
//		bodyAttributes.put(11, traceNbrStr); // 系统流水号(客服人工授权流水号):b011
//		bodyAttributes.put(14, ccsAuthmemoO.getExpireDate());
//		bodyAttributes.put(18, ccsAuthmemoO.getMcc()); // 商户类型:B018 固定值全9
//		bodyAttributes.put(22, ccsAuthmemoO.getB022());
//		bodyAttributes.put(25, ccsAuthmemoO.getB025());
//		bodyAttributes.put(32, ccsAuthmemoO.getB032()); // 收单机构id:b032 固定值
//		bodyAttributes.put(33, ccsAuthmemoO.getB033()); // 转发机构id:b033 固定值
//		bodyAttributes.put(37, ccsAuthmemoO.getB037());
//		bodyAttributes.put(38, ccsAuthmemoO.getAuthCode());
//		bodyAttributes.put(40, "898"); // 终端类型码: AIC内管系统
//		bodyAttributes.put(42, ccsAuthmemoO.getB042()); // 商户id :B042 固定值全9
//		bodyAttributes.put(48, ccsAuthmemoO.getB048());
//		bodyAttributes.put(49, ccsAuthmemoO.getB049()); // 持卡人账户币种:b049
//		// 域90组成
//		// 位置 1-4 字节 5-10 字节 11-20 字节 21-31 字节 32-42 字节
//		// 子域 90.1 MTI 90.2 B011 90.3 B007 90.4 B032 90.5 B033
//		byte[] mtiBytes = MsgUtil.leftPad(ccsAuthmemoO.getMti(), 4);
//		byte[] b011Bytes = MsgUtil.leftPad(ccsAuthmemoO.getB011(), 6);
//		byte[] b007Bytes = MsgUtil.leftPad(ccsAuthmemoO.getB007(), 10);
//		byte[] b032Bytes = MsgUtil.leftPad(ccsAuthmemoO.getB032(), 11);
//		byte[] b033Bytes = MsgUtil.leftPad(ccsAuthmemoO.getB033(), 11);
//		byte[] b090Bytes = new byte[42];
//		int b090Index = 0;
//		for(int i = 0; i < mtiBytes.length; i++) {
//			b090Bytes[b090Index] = mtiBytes[i];
//			b090Index++;
//		}
//		for(int i = 0; i < b011Bytes.length; i++) {
//			b090Bytes[b090Index] = b011Bytes[i];
//			b090Index++;
//		}
//		for(int i = 0; i < b007Bytes.length; i++) {
//			b090Bytes[b090Index] = b007Bytes[i];
//			b090Index++;
//		}
//		for(int i = 0; i < b032Bytes.length; i++) {
//			b090Bytes[b090Index] = b032Bytes[i];
//			b090Index++;
//		}
//		for(int i = 0; i < b033Bytes.length; i++) {
//			b090Bytes[b090Index] = b033Bytes[i];
//			b090Index++;
//		}
//		bodyAttributes.put(90, new String(b090Bytes));
//		/*---- yakmessage定义字段 ------*/
//		customAttributes.put(CustomAttributesKey.MTI, TPS_VAL_MTI);
//		customAttributes.put(CustomAttributesKey.INPUT_SOURCE, InputSource.BANK);
//		customAttributes.put(CustomAttributesKey.MANUAL_AUTH_FLAG, ManualAuthFlag.A);
//		customAttributes.put(TPS_CUST_KEY_OPERATOR, OrganizationContextHolder.getUsername()); // 操作员ID
//		customAttributes.put(TPS_CUST_KEY_TERMID, TPS_CUST_VAL_TERMID); // 操作终端ID
//		customAttributes.put(CustomAttributesKey.BUSINESS_DATE_KEY_NAME, globalManagementService.getSystemStatus()
//				.getBusinessDate());
//		// customAttributes.put(TPS_CUST_KEY_ADJFLAG, TPS_CUST_VAL_ADJFLAG);
//		// //人工授权借贷调标志
//		/*---- 3.发送金融交易，并等待返回消息 ---*/
//		YakMessage responseMessage = authorizationService.authorize(requestMessage);
//		String rspCode = "";
//		if(null != responseMessage) {
//			rspCode = responseMessage.getBody(39);
//			if(rspCode == null) {
//				rspCode = "";
//			}
//		}
//		// 查询持卡人信息
//		TmCustomer tmCustomer = cpsBusProvide.getTmCustomerToCard(ccsAuthmemoO.getB002());
//		if(rspCode.equals("00") || rspCode.equals("11")) {// 交易成功
//			CcsTxnAdjLog ccsTxnAdjLog = rCcsTxnAdjLog.findOne(qCcsTxnAdjLog.cardNo
//					.eq(ccsAuthmemoO.getB002())
//					.and(qCcsTxnAdjLog.authCode.eq(ccsAuthmemoO.getAuthCode()))
//					.and(qCcsTxnAdjLog.b007.eq(ccsAuthmemoO.getB007()).and(qCcsTxnAdjLog.b011.eq(ccsAuthmemoO.getB011()))
//							.and(qCcsTxnAdjLog.org.eq(ccsAuthmemoO.getOrg()))));
//			if(ccsTxnAdjLog != null) {
//				ccsTxnAdjLog.setVoidInd(Indicator.Y);
//				ccsTxnAdjLog.setVoidOperator(OrganizationContextHolder.getUsername());
//				ccsTxnAdjLog.setVoidReason("");
//				ccsTxnAdjLog.setVoidTime(new Date());
//			}
//			// 记录操作日志
//			opeLogUtil.cardholderServiceLog("3500", null, ccsAuthmemoO.getB002(), tmCustomer.getName() + "POS分期",
//					"POS分期撤销:成功，金额[{" + ccsAuthmemoO.getTxnAmt() + "}]");
//		} else {// 交易失败
//			// 记录操作日志
//			opeLogUtil.cardholderServiceLog("3500", null, ccsAuthmemoO.getB002(), tmCustomer.getName() + "POS分期",
//					"POS分期撤销:失败，失败代码[{" + rspCode + "}]");
//			throw new FlatException("POS分期撤消失败，失败代码[" + rspCode + "]");
//		}
//	}
//
//	
//	public FetchResponse getLoanRegList(FetchRequest request, String cardNo) throws FlatException {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Transactional
//	
//	public void regPosLoan(String cardNo, String merId, String mcc, BigDecimal loanAmt, LoanFeeMethod flag,
//			String address, int term, String programId) throws FlatException {
//		log.debug("3500POS分期申请regPosLoan：卡号【{}】，商户ID【{}】，MCC【{}】，申请分期金额【{}】，手续费收取方式【{}】，商品名址信息【{}】",
//				CodeMarkUtils.subCreditCard(cardNo), merId, mcc, loanAmt, flag, address);
//		/*---- 1.校验输入项目是否合法 ---*/
//		CheckUtil.checkCardNo(cardNo);
//		CheckUtil.rejectNull(loanAmt, "申请分期金额不允许为空");
//		/*
//		 * 权限检查
//		 */
//		this.operAuth(loanAmt);
//		// 注意：联机交易金额不带小数点，最右边两位视为小数位
//		long amount = (long) (loanAmt.doubleValue() * 100);
//		amountStr = new String(MsgUtil.leftPad(amount + "", 12));
//		if(amount <= 0) {
//			throw new FlatException("申请分期金额必须大于0！");
//		}
//		// 查找卡片记录
//		TmCard tmCard = cpsBusProvide.getTmCardToCardNo(cardNo);
//		// 查询持卡人信息
//		TmCustomer tmCustomer = cpsBusProvide.getTmCustomerToCard(cardNo);
//		if(flag.equals(LoanFeeMethod.C)) {
//			throw new FlatException("手续费收取方式自行自定暂不支持");
//		}
//		// 查找交易码参数记录
//		TxnCd txnCd = facility.retrieveParameterObject(transCode, TxnCd.class);
//		CheckUtil.rejectNull(txnCd, "交易码:[" + transCode + "]查询不到对应的参数配置");
//		/*---- 2.组装金融交易报文 ---*/
//		// 获取交易时间和创建流水号
//		long currentTimeMillis = System.currentTimeMillis();
//		// dateTimeStr = simpleDateFormat.format(currentTimeMillis);
//		dateTimeStr = simpleDateFormat.format((new Date()).getTime());
//		SimpleDateFormat sdf = new SimpleDateFormat("MMdd");
//		String md = sdf.format(globalManagementService.getSystemStatus().getBusinessDate());
//		dateTimeStr = md + dateTimeStr.substring(4);
//		int traceNbr = (int) (currentTimeMillis % 1000) * 1000 + (int) (Math.random() * 1000000000);
//		traceNbrStr = new String(MsgUtil.leftPad(traceNbr + dateTimeStr, 6));
//		String refNo = new String(MsgUtil.leftPad(traceNbr + dateTimeStr, 12));
//		String transType = "00";
//		String b003 = transType + "0000";
//		// 商户ID
//		String b042 = new String(MsgUtil.rightPad(merId, 15));
//		// 组装金融交易请求报文
//		YakMessage requestMessage = new YakMessage();
//		Map<Integer, String> bodyAttributes = requestMessage.getBodyAttributes();
//		Map<String, Serializable> customAttributes = requestMessage.getCustomAttributes();
//		bodyAttributes.put(2, cardNo); // 介质卡号:B002
//		bodyAttributes.put(3, b003);
//		bodyAttributes.put(4, amountStr); // 交易金额:b004，交易金额不带小数点，最右边两位视为小数位
//		bodyAttributes.put(7, dateTimeStr); // 交易时间(客服的自然时间):b007
//		bodyAttributes.put(11, traceNbrStr); // 系统流水号(客服人工授权流水号):b011
//		bodyAttributes.put(18, mcc); // 商户类型:B018 固定值全9
//		bodyAttributes.put(22, b022);
//		bodyAttributes.put(25, "64"); // 服务点条件码 64-正常提交
//		bodyAttributes.put(32, TPS_VAL_INST_ACQUIR_ID); // 收单机构id:b032 固定值
//		bodyAttributes.put(33, TPS_VAL_INST_FORWARD_ID); // 转发机构id:b033 固定值
//		bodyAttributes.put(37, refNo);
//		bodyAttributes.put(40, "898"); // 终端类型码: AIC内管系统
//		bodyAttributes.put(42, b042); // 商户id :B042 固定值全9
//		bodyAttributes.put(43, address); // 43域
//		bodyAttributes.put(48, new String(Hex.encodeHex(this.genB048(programId, flag, term).getBytes())));
//		bodyAttributes.put(49, "156"); // 交易货币代码:b049,此处156，写死
//		/*---- yakmessage定义字段 ------*/
//		customAttributes.put(CustomAttributesKey.MTI, TPS_VAL_MTI);
//		customAttributes.put(CustomAttributesKey.INPUT_SOURCE, InputSource.BANK);
//		customAttributes.put(CustomAttributesKey.MANUAL_AUTH_FLAG, ManualAuthFlag.A);
//		customAttributes.put(TPS_CUST_KEY_OPERATOR, OrganizationContextHolder.getUsername()); // 操作员ID
//		customAttributes.put(TPS_CUST_KEY_TERMID, TPS_CUST_VAL_TERMID); // 操作终端ID
//		customAttributes.put(TPS_CUST_KEY_TXNCODE, transCode); // 交易码 必填
//		customAttributes.put(TPS_CUST_KEY_TXNREFNO, refNo); // ref_nbr 选填
//		customAttributes.put(CustomAttributesKey.BUSINESS_DATE_KEY_NAME, globalManagementService.getSystemStatus()
//				.getBusinessDate());
//		/*---- 3.发送金融交易，并等待返回消息 ---*/
//		YakMessage responseMessage = authorizationService.authorize(requestMessage);
//		String rspCode = "";
//		String authCode = "";
//		if(null != responseMessage) {
//			rspCode = responseMessage.getBody(39);
//			authCode = responseMessage.getBody(38);
//			if(rspCode == null) {
//				rspCode = "";
//			}
//			if(authCode == null) {
//				authCode = "";
//			}
//		}
//		Date now = new Date();
//		CcsTxnAdjLog ccsTxnAdjLog = new CcsTxnAdjLog();
//		ccsTxnAdjLog.setAcctNo(tmCard.getAcctNo());
//		ccsTxnAdjLog.setAuthCode(authCode);
//		ccsTxnAdjLog.setCardNo(cardNo);
//		ccsTxnAdjLog.setCurrCd(156 + "");
//		ccsTxnAdjLog.setDbCrInd(txnCd.logicMod.getDbCrInd());
//		ccsTxnAdjLog.setMcc(mcc);
//		ccsTxnAdjLog.setMti(TPS_VAL_MTI);
//		ccsTxnAdjLog.setOperaId(OrganizationContextHolder.getUsername());
//		// ccsTxnAdjLog.setOperDate(now);
//		ccsTxnAdjLog.setOperSeq(null);
//		ccsTxnAdjLog.setOperTime(now);
//		ccsTxnAdjLog.setOrg(OrganizationContextHolder.getCurrentOrg());
//		ccsTxnAdjLog.setRefNbr(txnUtils.getRefnbr(dateTimeStr, authCode, refNo));
//		ccsTxnAdjLog.setRemark(REMARK);
//		ccsTxnAdjLog.setTxnAmt(loanAmt);
//		ccsTxnAdjLog.setTxnCode(transCode);
//		ccsTxnAdjLog.setTxnDate(globalManagementService.getSystemStatus().getBusinessDate());
//		ccsTxnAdjLog.setB003(b003);
//		ccsTxnAdjLog.setB004(new BigDecimal(amount));
//		ccsTxnAdjLog.setB007(dateTimeStr);
//		ccsTxnAdjLog.setB011(traceNbrStr);
//		ccsTxnAdjLog.setB032(TPS_VAL_INST_ACQUIR_ID);
//		ccsTxnAdjLog.setB033(TPS_VAL_INST_FORWARD_ID);
//		ccsTxnAdjLog.setB039(rspCode);
//		ccsTxnAdjLog.setB042(TPS_VAL_MERCHANT_ID);
//		ccsTxnAdjLog.setB049(156 + "");
//		ccsTxnAdjLog.setVoidInd(Indicator.N);
//		ccsTxnAdjLog.setLogBizDate(globalManagementService.getSystemStatus().getBusinessDate());
//		writeTranAdjLog(ccsTxnAdjLog); // 独立事务保证不管rspCode成功还是失败都能保存记录
//		if(rspCode.equals("00") || rspCode.equals("11")) {// 交易成功
//			// 记录操作日志
//			opeLogUtil.cardholderServiceLog("3500", null, cardNo, tmCustomer.getName() + "POS分期", "POS分期申请:成功，金额[{"
//					+ loanAmt + "}]");
//		} else {// 交易失败
//			// 记录操作日志
//			opeLogUtil.cardholderServiceLog("3201", null, cardNo, tmCustomer.getName() + "POS分期", "POS分期申请:失败，金额["
//					+ loanAmt + "],失败返回码[" + rspCode + "]");
//			throw new FlatException("分期申请失败，原因查看授权未入账信息,失败代码[{" + rspCode + "}]");
//		}
//	}
//
//	
//	public CcsAuthmemoO getCcsAuthmemoO(int logKey) throws FlatException {
//		return rCcsAuthmemoO.findOne(logKey);
//	}
//
//	/**
//	 * 记录账务调整操作日志
//	 * 
//	 * @param cardNo
//	 * @param debitAdjFlag
//	 * @param transCode
//	 * @param transAmt
//	 * @param currencyCd
//	 * @param refNo
//	 * @param transRemark
//	 * @param tmCard
//	 */
//	@Transactional
//	private void writeTranAdjLog(CcsTxnAdjLog ccsTxnAdjLog) {
//		rCcsTxnAdjLog.save(ccsTxnAdjLog);
//	}
//
//	/**
//	 * 组成分期交易的48域
//	 * 
//	 * @param merId
//	 * @param flag
//	 * @return
//	 */
//	private String genB048(String merId, LoanFeeMethod flag, Integer term) {
//		String b048 = "IP";
//		// 期数不足两位前补零
//		String a = term.toString();
//		if(term < 10) {
//			a = "0" + a;
//		}
//		String b = new String(MsgUtil.rightPad(merId, 31));
//		/*
//		 * 0－1次性支付手续费 1－按期支付手续费
//		 */
//		String c = flag == LoanFeeMethod.E ? "1" : "0";
//		c = new String(MsgUtil.rightPad(c, 29));
//		b048 = b048 + a + b + c;
//		return b048;
//	}
//
//	private void operAuth(BigDecimal loanAmt) {
//		if(loanAmt.compareTo(this.genOperAuth().getAdjPosLoanMax()) > 0) {
//			throw new FlatException("申请分期金额已超出操作权限范围！");
//		}
//		if(loanAmt.compareTo(PublicConst.TPS_TRAN_AMT_MAX) > 0) {
//			throw new FlatException("申请分期金额已超过系统支持的最大值，请联系管理员！");
//		}
//	}
//
//	/**
//	 * 获取操作员权限信息
//	 */
//	private TmOperAuth genOperAuth() {
//		TmOperAuth operAuth = operatorAuthUtil.getCurrentOperatorAuth();
//		CheckUtil.rejectNull(operAuth, "操作员无此权限");
//		CheckUtil.rejectNull(operAuth.getAdjPosLoanMax(), "操作员无此权限");
//		return operAuth;
//	}
	// public static void main(String[] args) throws
	// UnsupportedEncodingException, DecoderException{
	// T3500Server server = new T3500Server();
	// String b48 = server.genB048("0001", LoanFeeMethod.E, 9);
	// System.out.println(b48);
	// String a = new String(Hex.encodeHex(b48.getBytes()));
	// System.out.println(a);
	// String b = "";
	// try {
	// b = new String(Hex.decodeHex(a.toCharArray()),"GBK");
	// System.out.println(b);
	// } catch (Exception e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// String len = String.format("%03d", b.length() - 2);
	// String method = b.substring(0, 2);
	// String val = b.substring(2);
	// System.out.println(len);
	// System.out.println(method);
	// System.out.println(val);
	//
	// }
}
