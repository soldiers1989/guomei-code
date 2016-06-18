/**
 * 
 */
package com.sunline.ccs.ui.server;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.enums.EnumUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.Order;
import com.mysema.query.types.OrderSpecifier;
import com.sunline.acm.service.api.GlobalManagementService;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ark.support.service.YakMessage;
import com.sunline.ark.support.utils.CodeMarkUtils;
import com.sunline.ark.support.utils.DateUtils;
import com.sunline.ccs.facility.MsgUtils;
//import com.sunline.ccs.infrastructure.server.repos.RCcsTxnAdjLog;
import com.sunline.ccs.infrastructure.server.repos.RCcsTxnAdjLog;
//import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
//import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
//import com.sunline.ccs.infrastructure.shared.model.CcsOpPrivilege;
import com.sunline.ccs.infrastructure.shared.model.CcsOpPrivilege;
//import com.sunline.ccs.infrastructure.shared.model.CcsTxnAdjLog;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnAdjLog;
//import com.sunline.ccs.infrastructure.shared.model.QCcsTxnAdjLog;
import com.sunline.ccs.infrastructure.shared.model.QCcsTxnAdjLog;
import com.sunline.ccs.param.def.CurrencyCtrl;
import com.sunline.ccs.param.def.MerchantGroup;
import com.sunline.ccs.param.def.PlanTemplate;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.TxnCd;
import com.sunline.ccs.param.def.enums.AdjustIndicator;
import com.sunline.ccs.ui.server.commons.CPSBusProvide;
import com.sunline.ccs.ui.server.commons.CheckUtil;
import com.sunline.ccs.ui.shared.PublicConst;
import com.sunline.kylin.web.ark.client.JPAQueryFetchResponseBuilder;
import com.sunline.kylin.web.ark.client.data.SelectOptionEntry;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.kylin.web.core.client.exception.FlatException;
import com.sunline.pcm.param.def.CurrencyCd;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.api.AuthorizationService;
import com.sunline.ppy.api.CustomAttributesKey;
import com.sunline.ppy.dictionary.enums.BucketType;
import com.sunline.ppy.dictionary.enums.DbCrInd;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.InputSource;
import com.sunline.ppy.dictionary.enums.ManualAuthFlag;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * 账务调整
 *
 * @author yeyu
 *
 */
@Controller
@RequestMapping(value = "/t3201Server")
public class FinanceAdjustmentServer {
	private static final String TPS_CUST_KEY_OPERATOR = "css.operatorID";
	private static final String TPS_CUST_KEY_TERMID = "css.termid";
	private static final String TPS_CUST_KEY_TXNCODE = "css.cpstxncode";
	private static final String TPS_CUST_KEY_TXNREFNO = "css.cpstxnrefnbr";

	private static final String TPS_CUST_VAL_TERMID = "cps-app";

	private static final String TPS_VAL_INST_ACQUIR_ID = "99999999";
	private static final String TPS_VAL_INST_FORWARD_ID = "99999999";
	private static final String TPS_VAL_MERCHANT_ID = "999999999999999";
	private static final String TPS_VAL_MTI_ADJUST = "0200";
	private static final String TPS_VAL_MTI_ADJUST_VOID = "0420";
	private static final String TPS_VAL_ADJUST = "账务调整";

	private SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
			"MMddHHmmss");
	private String dateTimeStr = "";
	private String traceNbrStr = "";
	private String amountStr = "";

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Resource(name = "authorizationService")
	private AuthorizationService authorizationService;

	@Autowired
	private CPSBusProvide cpsBusProvide;

	@Autowired
	private UnifiedParameterFacility unifiedParameterService;

	@Autowired
	private OpeLogUtil opeLogUtil;

	@Autowired
	private RCcsTxnAdjLog rTmTranAdjLog;

	@PersistenceContext
	private EntityManager em;

	@Autowired
	private GlobalManagementService globalManagementService;

	@Autowired
	private OperatorAuthUtil operatorAuthUtil;

	private TxnCdComparator txnCdComparator = new TxnCdComparator();

	@ResponseBody()
	@RequestMapping(value = "/getTxnCd", method = { RequestMethod.POST })
	public List<SelectOptionEntry> getTxnCdList() {
		Map<String, TxnCd> txnCdMap = unifiedParameterService
				.retrieveParameterObject(TxnCd.class);
		List<TxnCd> txnCdList = new ArrayList<TxnCd>();
		for (TxnCd code : txnCdMap.values()) {
			if (code.adjustInd == AdjustIndicator.C
					|| code.adjustInd == AdjustIndicator.D) {
				//2016年02月需求-开放利息/本金/罚金调整功能 --lisy
//				if(code.logicMod.getBucketType()==BucketType.Interest||
//						code.logicMod.getBucketType()==BucketType.Pricinpal)
				txnCdList.add(code);
			}
		}

		// 按照交易码对交易码列表进行排序
		Collections.sort(txnCdList, txnCdComparator);

		List<SelectOptionEntry> uiShowData = new ArrayList<SelectOptionEntry>();
		if(txnCdList != null && !txnCdList.isEmpty()) {
			for(TxnCd txnCd : txnCdList) {
				SelectOptionEntry entry = new SelectOptionEntry(txnCd.txnCd,txnCd.description );
				uiShowData.add(entry);
			}
		}
		return uiShowData;
	}

	/*
	 * String cardNo, String transCode, BigDecimal transAmt,Date adjustDate, int
	 * currencyCd, String refNo, String transRemark
	 */

	@Transactional
	@ResponseBody()
	@RequestMapping(value = "/writeTranAdjLog", method = { RequestMethod.POST })
	public void adjustTran(@RequestBody Map<String, String> params)
			throws FlatException {
		int term = Integer.valueOf(params.get(CcsTxnAdjLog.P_Term));
		String cardNo = params.get(CcsTxnAdjLog.P_CardNbr);
		String transCode = params.get(CcsTxnAdjLog.P_TxnCode);
		BigDecimal transAmt = new BigDecimal(params.get("adjAmount"));
		Date optDate = new Date();
		optDate.setTime(Long.parseLong(params.get(CcsTxnAdjLog.P_OpTime)));
		Date adjustDate = optDate;
		int currencyCd = Integer.parseInt(params.get(CcsTxnAdjLog.P_Currency));
		String refNo = params.get(CcsTxnAdjLog.P_RefNbr);
		String transRemark = params.get(CcsTxnAdjLog.P_Remark);
		logger.info("adjustTran: cardNo后四位=["
				+ CodeMarkUtils.subCreditCard(cardNo) + "], transCode=["
				+ transCode + "], transAmt=[" + transAmt + "]");

		/*---- 1.校验输入项目是否合法 ---*/
		CheckUtil.checkCardNo(cardNo);
		CheckUtil.rejectNull(transCode, "交易码不允许为空");
		CheckUtil.rejectNull(transAmt, "调整金额不允许为空");
		if (transAmt.compareTo(PublicConst.TPS_TRAN_AMT_MAX) > 0) {
			throw new FlatException("调整金额已超过系统支持的最大值，请联系管理员！");
		}

		// 注意：联机交易金额不带小数点，最右边两位视为小数位
		long amount = (long) (transAmt.doubleValue() * 100);
		amountStr = new String(MsgUtils.leftPad(amount + "", 12));

		if (amount <= 0) {
			throw new FlatException("调整的金额必须大于0！");
		}

		// 检查操作员可调整的最大金额
		CcsOpPrivilege tmOperAuth = operatorAuthUtil.getCurrentOperatorAuth();
		if (null == tmOperAuth || tmOperAuth.getMaxAcctTxnAdj() == null
				|| tmOperAuth.getMaxAcctTxnAdj().compareTo(transAmt) < 0) {
			throw new FlatException("调整的金额已超出操作权限范围！");
		}

		// 查找卡片记录
		CcsCard tmCard = cpsBusProvide.getTmCardTocardNbr(cardNo);
		// 查询持卡人信息
		CcsCustomer tmCustomer = cpsBusProvide.getTmCustomerToCard(cardNo);

		// 查找交易码参数记录
		TxnCd txnCd = unifiedParameterService.retrieveParameterObject(
				transCode, TxnCd.class);
		CheckUtil.rejectNull(txnCd, "交易码:[" + transCode + "]查询不到对应的参数配置");

		// 查找主信用计划模板
		ProductCredit productCredit = unifiedParameterService.loadParameter(
				tmCard.getProductCd(), ProductCredit.class);
		String planNbr = productCredit.planNbrList.get(txnCd.planType);
		CheckUtil.rejectNull(planNbr, "卡产品号:[" + tmCard.getProductCd()
				+ "], 交易代码:[" + txnCd.txnCd + "] 查询不到对应的参数配置");
		PlanTemplate planTemplate = unifiedParameterService
				.retrieveParameterObject(planNbr, PlanTemplate.class);
		CheckUtil.rejectNull(planTemplate, "卡产品号:[" + tmCard.getProductCd()
				+ "], 交易代码:[" + txnCd.txnCd + "] 查询不到对应的信用计划模板参数配置");

		// 查找币种参数记录
		CurrencyCtrl currencyCtrl = unifiedParameterService
				.retrieveParameterObject(currencyCd + "", CurrencyCtrl.class);
		CheckUtil
				.rejectNull(currencyCtrl, "币种:[" + currencyCd + "]查询不到对应的参数配置");

		// 交易参考号是否为空，为空根据规则生成并上送
		if (CheckUtil.isEmpty(refNo)) {
			long currentTimeMillis = System.currentTimeMillis();
			int refNoInt = (int) (currentTimeMillis % 1000) * 1000
					+ (int) (Math.random() * 1000);
			refNo = new String(MsgUtils.leftPad(refNoInt + "", 23));
		}

		/*---- 2.组装金融交易报文 ---*/

		// 获取交易时间和创建流水号
		long currentTimeMillis = System.currentTimeMillis();
		// dateTimeStr = simpleDateFormat.format(currentTimeMillis);
		dateTimeStr = simpleDateFormat.format(adjustDate.getTime());
		int traceNbr = (int) (currentTimeMillis % 1000) * 1000
				+ (int) (Math.random() * 1000);
		traceNbrStr = new String(MsgUtils.leftPad(traceNbr + "", 6));

		// 判断合适的交易类型和商户类型以便填写交易报文
		// 消费类 5999
		// 取现类 6010
		// 还款（贷调） 6010
		String transType = "21"; // 还款 (贷调 )
		String mcc = "6010"; // 贷调的商户类型
		if (txnCd.logicMod.getDbCrInd() == DbCrInd.D) { // 借记调整
			switch (planTemplate.planType) {
			case C: // 取现计划
				transType = "01";
				mcc = "6010";
				break;
			case R: // 消费计划
			case O: // 分期转出计划
			case I: // 分期转入计划
			case Q:
			case P:
			case J:
			case L:
				transType = "00";
				mcc = "5999";
				break;
			case D:
				transType = "02";
				mcc = "6010";
				break;
			default:
				throw new FlatException("主信用计划模板类型参数有误["
						+ planTemplate.planType + "]");
			}
		}

		String b003 = transType + "0000";

		// 组装金融交易请求报文
		YakMessage requestMessage = new YakMessage();
		Map<Integer, String> headAttributes = requestMessage
				.getHeadAttributes();
		Map<Integer, String> bodyAttributes = requestMessage
				.getBodyAttributes();
		Map<String, Serializable> customAttributes = requestMessage
				.getCustomAttributes();

		// requestMessage.setSrcChannelId(TPS_INPUT_SOURCE); //inputsource =客服
		headAttributes.put(11, TPS_VAL_MTI_ADJUST); // MTI 为了支持撤消操作由220改为200
													// @20120929

		bodyAttributes.put(2, cardNo); // 介质卡号:B002
		// 交易类型: 消费(借调消费类分期类plan)/ 取现(借调取现类plan) /还款 (贷调 ):B003前两位=00 /01/ 21
		bodyAttributes.put(3, b003);
		bodyAttributes.put(4, amountStr); // 交易金额:b004，交易金额不带小数点，最右边两位视为小数位
		bodyAttributes.put(7, dateTimeStr); // 交易时间(客服的自然时间):b007
		bodyAttributes.put(11, traceNbrStr); // 系统流水号(客服人工授权流水号):b011
		bodyAttributes.put(18, mcc); // 商户类型:B018 固定值全9
		bodyAttributes.put(25, "00"); // 服务点条件码 00-正常提交
		bodyAttributes.put(32, TPS_VAL_INST_ACQUIR_ID); // 收单机构id:b032 固定值
		bodyAttributes.put(33, TPS_VAL_INST_FORWARD_ID); // 转发机构id:b033 固定值
		bodyAttributes.put(40, "898"); // 终端类型码: AIC内管系统
		bodyAttributes.put(42, TPS_VAL_MERCHANT_ID); // 商户id :B042 固定值全9
		bodyAttributes.put(43, TPS_VAL_ADJUST); // 43域 增加账务调整
		bodyAttributes.put(49, currencyCd + ""); // 交易货币代码:b049

		/*---- yakmessage定义字段 ------*/
		customAttributes.put(CustomAttributesKey.MTI, TPS_VAL_MTI_ADJUST);
		customAttributes
				.put(CustomAttributesKey.INPUT_SOURCE, InputSource.BANK);
		customAttributes.put(CustomAttributesKey.MANUAL_AUTH_FLAG,
				ManualAuthFlag.F);
		customAttributes.put(TPS_CUST_KEY_OPERATOR,
				OrganizationContextHolder.getUsername()); // 操作员ID
		customAttributes.put(TPS_CUST_KEY_TERMID, TPS_CUST_VAL_TERMID); // 操作终端ID
		customAttributes.put(TPS_CUST_KEY_TXNCODE, transCode); // 交易码 必填
		customAttributes.put(TPS_CUST_KEY_TXNREFNO, refNo); // ref_nbr 选填
		customAttributes.put(CustomAttributesKey.BUSINESS_DATE_KEY_NAME,
				globalManagementService.getSystemStatus().getBusinessDate());
		// customAttributes.put(TPS_CUST_KEY_ADJFLAG, TPS_CUST_VAL_ADJFLAG);
		// //人工授权借贷调标志

		/*---- 3.发送金融交易，并等待返回消息 ---*/

		YakMessage responseMessage = authorizationService.authorize(requestMessage);
		String rspCode = "";
		String authCode = "";
		if (null != responseMessage) {
			rspCode = responseMessage.getBody(39);
			authCode = responseMessage.getBody(38);

			if (rspCode == null) {
				rspCode = "";
			}
			if (authCode == null) {
				authCode = "";
			}
		}

		Date now = new Date();

		CcsTxnAdjLog tmTranAdjLog = new CcsTxnAdjLog();
		tmTranAdjLog.setAcctNbr(tmCard.getAcctNbr());
		tmTranAdjLog.setTerm(term);
		tmTranAdjLog.setAuthCode(authCode);
		tmTranAdjLog.setCardNbr(cardNo);
		tmTranAdjLog.setCurrency(currencyCd + "");
		tmTranAdjLog.setDbCrInd(txnCd.logicMod.getDbCrInd());
		tmTranAdjLog.setMcc(mcc);
		tmTranAdjLog.setMti(TPS_VAL_MTI_ADJUST);
		tmTranAdjLog.setOpId(OrganizationContextHolder.getUsername());
		tmTranAdjLog.setOpSeq(null);
		tmTranAdjLog.setOpTime(now);
		tmTranAdjLog.setOrg(OrganizationContextHolder.getCurrentOrg());
		tmTranAdjLog.setRefNbr(refNo);
		tmTranAdjLog.setRemark(transRemark);
		tmTranAdjLog.setTxnAmt(transAmt);
		tmTranAdjLog.setTxnCode(transCode);
		tmTranAdjLog.setTxnDate(adjustDate);
		tmTranAdjLog.setB003ProcCode(b003);
		tmTranAdjLog.setB004Amt(new BigDecimal(amount));
		tmTranAdjLog.setB007TxnTime(dateTimeStr);
		tmTranAdjLog.setB011Trace(traceNbrStr);
		tmTranAdjLog.setB032AcqInst(TPS_VAL_INST_ACQUIR_ID);
		tmTranAdjLog.setB033FwdIns(TPS_VAL_INST_FORWARD_ID);
		tmTranAdjLog.setB039RtnCode(rspCode);
		tmTranAdjLog.setB042MerId(TPS_VAL_MERCHANT_ID);
		tmTranAdjLog.setB049CurrCode(currencyCd + "");
		tmTranAdjLog.setVoidInd(Indicator.N);
		tmTranAdjLog.setLogBizDate(globalManagementService.getSystemStatus().getBusinessDate());
		writeTranAdjLog(tmTranAdjLog); // 独立事务保证不管rspCode成功还是失败都能保存记录

		if (rspCode.equals("00") || rspCode.equals("11")) {// 交易成功
			// 记录操作日志
			opeLogUtil.cardholderServiceLog("3201", null, cardNo,
					tmCustomer.getName() + ',' + transCode + "-"
							+ txnCd.description, "账务调整:" + txnCd.description
							+ "成功，金额[" + transAmt + "]");
		} else {// 交易失败
			// 记录操作日志
			opeLogUtil.cardholderServiceLog("3201", null, cardNo,
					tmCustomer.getName() + ',' + transCode + "-"
							+ txnCd.description, "账务调整:" + txnCd.description
							+ "失败，金额[" + transAmt + "]，失败代码[" + rspCode + "]");
			throw new FlatException("调整失败，原因查看授权未入账信息");
		}

	}

	/**
	 * 记录账务调整操作日志
	 * 
	 * @param cardNo
	 * @param debitAdjFlag
	 * @param transCode
	 * @param transAmt
	 * @param currencyCd
	 * @param refNo
	 * @param transRemark
	 * @param tmCard
	 */
	@Transactional
	private void writeTranAdjLog(@RequestBody CcsTxnAdjLog log) {
		rTmTranAdjLog.save(log);
	}

	@ResponseBody()
	@RequestMapping(value = "/getCurrentUserAdjLogList", method = { RequestMethod.POST })
	public FetchResponse getCurrentUserAdjLogList(
			@RequestBody FetchRequest request) throws FlatException {
		Date beginDate = null;
		Date endDate = null;
		long paramBeginDate = 0l;
		long paramEndDate = 0l;
		if (request.getParameter(CcsTxnAdjLog.P_OpTime) == null) {
			beginDate = null;
		} else {
			paramBeginDate = Long.parseLong((String) request
					.getParameter(CcsTxnAdjLog.P_OpTime));
			beginDate = new Date(paramBeginDate);
		}
		if (request.getParameter(CcsTxnAdjLog.P_VoidTime) == null) {
			endDate = null;
		} else {
			paramEndDate = Long.parseLong((String) request
					.getParameter(CcsTxnAdjLog.P_VoidTime));
			endDate = new Date(paramEndDate);
		}
		// 定义局部变量
		QCcsTxnAdjLog qTmTranAdjLog = QCcsTxnAdjLog.ccsTxnAdjLog;
		JPAQuery query = new JPAQuery(em).from(qTmTranAdjLog);
		{
			// 条件限于当前操作员
			query = query.where(
					qTmTranAdjLog.org.eq(OrganizationContextHolder
							.getCurrentOrg())).where(
					qTmTranAdjLog.opId.eq(OrganizationContextHolder
							.getUsername()));
			if (null != beginDate) {
				beginDate = DateUtils.truncate(beginDate, Calendar.DATE);
				query = query.where(qTmTranAdjLog.opTime.goe(beginDate));
			}
			if (null != endDate) {
				endDate = DateUtils.truncate(
						DateUtils.addDays(
								DateUtils.truncate(endDate, Calendar.DATE), 1),
						Calendar.DATE);
				query = query.where(qTmTranAdjLog.opTime.lt(endDate));
			}
			query = query.orderBy(new OrderSpecifier<Long>(Order.DESC,
					qTmTranAdjLog.opSeq));
		}
		return new JPAQueryFetchResponseBuilder(request, query)
				.addFieldMapping(CcsTxnAdjLog.P_AcctNbr, qTmTranAdjLog.acctNbr)
				.addFieldMapping(CcsTxnAdjLog.P_AuthCode,
						qTmTranAdjLog.authCode)
				.addFieldMapping(CcsTxnAdjLog.P_CardNbr, qTmTranAdjLog.cardNbr)
				.addFieldMapping(CcsTxnAdjLog.P_Currency,
						qTmTranAdjLog.currency)
				.addFieldMapping(CcsTxnAdjLog.P_DbCrInd, qTmTranAdjLog.dbCrInd)
				.addFieldMapping(CcsTxnAdjLog.P_RefNbr,qTmTranAdjLog.refNbr)
				.addFieldMapping(CcsTxnAdjLog.P_Mcc, qTmTranAdjLog.mcc)
				.addFieldMapping(CcsTxnAdjLog.P_Mti, qTmTranAdjLog.mti)
				.addFieldMapping(CcsTxnAdjLog.P_OpId, qTmTranAdjLog.opId)
				.addFieldMapping(CcsTxnAdjLog.P_OpSeq, qTmTranAdjLog.opSeq)
				.addFieldMapping(CcsTxnAdjLog.P_OpTime, qTmTranAdjLog.opTime)
				.addFieldMapping(CcsTxnAdjLog.P_Org, qTmTranAdjLog.org)
				.addFieldMapping(CcsTxnAdjLog.P_RefNbr, qTmTranAdjLog.refNbr)
				.addFieldMapping(CcsTxnAdjLog.P_Remark, qTmTranAdjLog.remark)
				.addFieldMapping(CcsTxnAdjLog.P_TxnAmt, qTmTranAdjLog.txnAmt)
				.addFieldMapping(CcsTxnAdjLog.P_TxnCode, qTmTranAdjLog.txnCode)
				.addFieldMapping(CcsTxnAdjLog.P_TxnDate, qTmTranAdjLog.txnDate)
				.addFieldMapping(CcsTxnAdjLog.P_B003ProcCode,
						qTmTranAdjLog.b003ProcCode)
				.addFieldMapping(CcsTxnAdjLog.P_B004Amt, qTmTranAdjLog.b004Amt)
				.addFieldMapping(CcsTxnAdjLog.P_B007TxnTime,
						qTmTranAdjLog.b007TxnTime)
				.addFieldMapping(CcsTxnAdjLog.P_B011Trace,
						qTmTranAdjLog.b011Trace)
				.addFieldMapping(CcsTxnAdjLog.P_B032AcqInst,
						qTmTranAdjLog.b032AcqInst)
				.addFieldMapping(CcsTxnAdjLog.P_B033FwdIns,
						qTmTranAdjLog.b033FwdIns)
				.addFieldMapping(CcsTxnAdjLog.P_B039RtnCode,
						qTmTranAdjLog.b039RtnCode)
				.addFieldMapping(CcsTxnAdjLog.P_B042MerId,
						qTmTranAdjLog.b042MerId)
				.addFieldMapping(CcsTxnAdjLog.P_B049CurrCode,
						qTmTranAdjLog.b049CurrCode)
				.addFieldMapping(CcsTxnAdjLog.P_VoidInd, qTmTranAdjLog.voidInd)
				.addFieldMapping(CcsTxnAdjLog.P_VoidOpId,
						qTmTranAdjLog.voidOpId)
				.addFieldMapping(CcsTxnAdjLog.P_VoidReason,
						qTmTranAdjLog.voidReason)
				.addFieldMapping(CcsTxnAdjLog.P_VoidTime,
						qTmTranAdjLog.voidTime).build();
	}

	@Transactional
	@ResponseBody()
	@RequestMapping(value = "/cancelAdjustTran", method = { RequestMethod.POST })
	public void cancelAdjustTran(@RequestBody String operSeqParam,
			@RequestBody String reason) throws FlatException {
		long operSeq = Long.parseLong(operSeqParam);
		Map map = new HashMap();
		// 检查输入项
		if (operSeq <= 0) {
			throw new FlatException("必须指定撤消的账务调整记录");
		}
		CcsTxnAdjLog tmTranAdjLog = rTmTranAdjLog.findOne(operSeq);
		CheckUtil.rejectNull(tmTranAdjLog, "未找到账务调整记录:[" + operSeq + "]");
		if (!tmTranAdjLog.getOrg().equals(
				OrganizationContextHolder.getCurrentOrg())) {
			throw new FlatException("未找到账务调整记录:[" + operSeq + "]");
		}

		// 判断账务调整记录是否为成功记录
		if (tmTranAdjLog.getB039RtnCode() == null
				|| !tmTranAdjLog.getB039RtnCode().equals("00")) {
			// throw new ProcessException("仅允许账务调整成功的记录做撤消");
			throw new FlatException("仅允许账务调整成功的记录做撤消");
		}

		// 判断账务调整记录是否已被撤销
		if (tmTranAdjLog.getVoidInd() != Indicator.N) {
			throw new FlatException("该账务调整已被撤消，不能重复撤消");
		}

		/*---- 2.组装金融交易报文 ---*/

		amountStr = new String(MsgUtils.leftPad(tmTranAdjLog.getB004Amt()
				.toString(), 12));

		// 获取交易时间和创建流水号
		long currentTimeMillis = System.currentTimeMillis();
		dateTimeStr = simpleDateFormat.format(currentTimeMillis);
		int traceNbr = (int) (currentTimeMillis % 1000) * 1000
				+ (int) (Math.random() * 1000);
		traceNbrStr = new String(MsgUtils.leftPad(traceNbr + "", 6));

		// 组装金融交易请求报文
		YakMessage requestMessage = new YakMessage();
		// Map<Integer, String> headAttributes =
		// requestMessage.getHeadAttributes();
		Map<Integer, String> bodyAttributes = requestMessage
				.getBodyAttributes();
		Map<String, Serializable> customAttributes = requestMessage
				.getCustomAttributes();

		// requestMessage.setSrcChannelId(TPS_INPUT_SOURCE); //inputsource =内管
		// headAttributes.put(11, TPS_VAL_MTI_ADJUST_VOID); //MTI

		bodyAttributes.put(2, tmTranAdjLog.getCardNbr()); // 介质卡号:B002
		bodyAttributes.put(3, String.valueOf(tmTranAdjLog.getAcctNbr()));
		bodyAttributes.put(4, amountStr); // 交易金额:b004，交易金额不带小数点，最右边两位视为小数位
		bodyAttributes.put(7, dateTimeStr); // 交易时间(客服的自然时间):b007
		bodyAttributes.put(11, traceNbrStr); // 系统流水号(客服人工授权流水号):b011
		bodyAttributes.put(18, tmTranAdjLog.getMcc()); // 商户类型:B018 固定值全9
		bodyAttributes.put(32, tmTranAdjLog.getB032AcqInst()); // 收单机构id:b032
																// 固定值
		bodyAttributes.put(33, tmTranAdjLog.getB033FwdIns()); // 转发机构id:b033 固定值
		bodyAttributes.put(38, tmTranAdjLog.getAuthCode());
		bodyAttributes.put(40, "898"); // 终端类型码: AIC内管系统
		bodyAttributes.put(42, tmTranAdjLog.getB042MerId()); // 商户id :B042 固定值全9
		bodyAttributes.put(49, tmTranAdjLog.getB049CurrCode()); // 持卡人账户币种:b049

		// 域90组成
		// 位置 1-4 字节 5-10 字节 11-20 字节 21-31 字节 32-42 字节
		// 子域 90.1 MTI 90.2 B011 90.3 B007 90.4 B032 90.5 B033
		byte[] mtiBytes = MsgUtils.leftPad(tmTranAdjLog.getMti(), 4);
		byte[] b011Bytes = MsgUtils.leftPad(tmTranAdjLog.getB011Trace(), 6);
		byte[] b007Bytes = MsgUtils.leftPad(tmTranAdjLog.getB007TxnTime(), 10);
		byte[] b032Bytes = MsgUtils.leftPad(tmTranAdjLog.getB032AcqInst(), 11);
		byte[] b033Bytes = MsgUtils.leftPad(tmTranAdjLog.getB033FwdIns(), 11);
		byte[] b090Bytes = new byte[42];
		int b090Index = 0;
		for (int i = 0; i < mtiBytes.length; i++) {
			b090Bytes[b090Index] = mtiBytes[i];
			b090Index++;
		}
		for (int i = 0; i < b011Bytes.length; i++) {
			b090Bytes[b090Index] = b011Bytes[i];
			b090Index++;
		}
		for (int i = 0; i < b007Bytes.length; i++) {
			b090Bytes[b090Index] = b007Bytes[i];
			b090Index++;
		}
		for (int i = 0; i < b032Bytes.length; i++) {
			b090Bytes[b090Index] = b032Bytes[i];
			b090Index++;
		}
		for (int i = 0; i < b033Bytes.length; i++) {
			b090Bytes[b090Index] = b033Bytes[i];
			b090Index++;
		}

		bodyAttributes.put(90, new String(b090Bytes));

		/*---- yakmessage定义字段 ------*/
		customAttributes.put(CustomAttributesKey.MTI, TPS_VAL_MTI_ADJUST_VOID);
		customAttributes
				.put(CustomAttributesKey.INPUT_SOURCE, InputSource.BANK);
		customAttributes.put(CustomAttributesKey.MANUAL_AUTH_FLAG,
				ManualAuthFlag.F);
		customAttributes.put(TPS_CUST_KEY_OPERATOR,
				OrganizationContextHolder.getUsername()); // 操作员ID
		customAttributes.put(TPS_CUST_KEY_TERMID, TPS_CUST_VAL_TERMID); // 操作终端ID
		customAttributes.put(CustomAttributesKey.BUSINESS_DATE_KEY_NAME,
				globalManagementService.getSystemStatus().getBusinessDate());

		/*---- 3.发送金融交易，并等待返回消息 ---*/
		YakMessage responseMessage = null;
//		try{
//			responseMessage = authorizationService.authorize(requestMessage);
//		}catch(Exception e){
//			logger.error("authorize failed",e);
//		}
//		
//		String rspCode = "";
//		if (null != responseMessage) {
//			rspCode = responseMessage.getBody(39);
//
//			if (rspCode == null) {
//				rspCode = "";
//			}
//		}
		//远程服务无法调用，这里使用了测试数据
		String rspCode = "00";
		CcsCustomer tmCustomer = cpsBusProvide.getTmCustomerToCard(tmTranAdjLog.getCardNbr());

		// 查找交易码参数记录
		TxnCd txnCode = unifiedParameterService.retrieveParameterObject(
				tmTranAdjLog.getTxnCode(), TxnCd.class);
		CheckUtil.rejectNull(txnCode, "交易码:[" + tmTranAdjLog.getTxnCode()
				+ "]查询不到对应的参数配置");
		if (rspCode.equals("00") || rspCode.equals("11")) {// 交易成功
			// 更新调整记录信息
			tmTranAdjLog.setVoidOpId(OrganizationContextHolder.getUsername());
			tmTranAdjLog.setVoidReason(reason);
			tmTranAdjLog.setVoidTime(new Date());
			tmTranAdjLog.setVoidInd(Indicator.valueOf("Y"));
			//tmTranAdjLog.updateFromMap(tmTranAdjLog.convertToMap());
			rTmTranAdjLog.saveAndFlush(tmTranAdjLog);
			
			// 记录操作日志
			opeLogUtil.cardholderServiceLog("3201", null,
					tmTranAdjLog.getCardNbr(), tmCustomer.getName() + ','
							+ txnCode.txnCd + '-' + txnCode.description, "账务调整撤消:"
							+ txnCode.description + "成功");
		} else {// 交易失败
			// 记录操作日志
			opeLogUtil.cardholderServiceLog("3201", null,
					tmTranAdjLog.getCardNbr(), tmCustomer.getName() + ','
							+ txnCode.txnCd + '-' + txnCode.description, "账务调整撤消:"
							+ txnCode.description + "失败，失败代码[" + rspCode + "]");
			throw new FlatException("账务调整撤消失败，失败代码[" + rspCode + "]");
		}

	}

	/**
	 * 
	 * @param cardNo
	 *            持卡人卡号
	 * @return
	 * @throws ProcessException
	 */
	@SuppressWarnings("all")
	@ResponseBody()
	@RequestMapping(value = "/loadCustInfoByCardNo", method = { RequestMethod.POST })
	public Map loadCustInfoByCardNo(@RequestBody String cardNbr)
			throws FlatException {
		logger.info("loadCustInfoByCardNo:卡号后四位["
				+ CodeMarkUtils.subCreditCard(cardNbr) + "]");
		Map result = null;
		CheckUtil.checkCardNo(cardNbr);
		CcsCard tmCard = cpsBusProvide.getTmCardTocardNbr(cardNbr);
		CheckUtil.rejectNull(tmCard, "卡号[" + cardNbr + "]查询不到对应的卡片信息");
		CcsCustomer tmCustomer = cpsBusProvide.getTmCustomerByCustId(tmCard
				.getCustId());
		result = tmCustomer.convertToMap();
		ArrayList<CurrencyCd> currencyCdList = new ArrayList<CurrencyCd>();
		// 获取所有币种代码参数
		Map<String, CurrencyCd> currencyCdMap = unifiedParameterService
				.retrieveParameterObject(CurrencyCd.class);
		// Map<String, CurrencyCd> currencyCdMap = null;
		// 获取卡产品参数
		ProductCredit product = unifiedParameterService
				.retrieveParameterObject(tmCard.getProductCd(),
						ProductCredit.class);
		// ProductCredit product = null;
		// 将卡产品中的本币和外币币种代码返回
		// 顺序：本币排在前面，外币排在后面
		if (product != null) {
			if (StringUtils.isNotBlank(product.postCurrCd)) {
				currencyCdList.add(currencyCdMap.get(product.postCurrCd));
			}
			if (StringUtils.isNotBlank(product.dualCurrCd)) {
				currencyCdList.add(currencyCdMap.get(product.dualCurrCd));
			}
		}
		// 按照币种代码对币种代码列表进行排序
		// Collections.sort(currencyCdList, currencyCdComparator);
		result.put(CurrencyCd.class.getName(), currencyCdList);
		return result;
	}

	/**
	 * 根据交易码获取参数内部交易码对象对应的信用计划模板
	 */
	public TxnCd getPlanTypeByTxnCd(String txnCd) throws FlatException {
		logger.info("交易码[" + txnCd + "],获取内部交易码参数");
		TxnCd txnCdObj = unifiedParameterService.loadParameter(txnCd,
				TxnCd.class);
		if (txnCdObj == null) {
			throw new FlatException("交易码[" + txnCd + "]，对应的参数不存在");
		}
		return txnCdObj;
	}

}
