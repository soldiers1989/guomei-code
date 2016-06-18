package com.sunline.ccs.service.handler.sub;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.api.GlobalManagementService;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ark.support.service.YakMessage;
import com.sunline.ccs.facility.AuthCommUtils;
import com.sunline.ccs.facility.AuthCommUtils.MsgParameter;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.infrastructure.server.repos.RCcsTxnAdjLog;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnAdjLog;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnHst;
import com.sunline.ccs.infrastructure.shared.model.QCcsTxnHst;
import com.sunline.ccs.param.def.SysTxnCdMapping;
import com.sunline.ccs.param.def.TxnCd;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S12041Req;
import com.sunline.ccs.service.protocol.S12041Resp;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.api.AuthorizationService;
import com.sunline.ppy.api.CustomAttributesKey;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.ManualAuthFlag;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：TNRCardFeeReturn
 * @see 描述： 已收年费减免
 *
 * @see 创建日期： 2015年06月25日下午 03:00:33
 * @author yanjingfeng
 *
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNRCardFeeReturn {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private AuthorizationService authorizationService;
	@Autowired
	private UnifiedParameterFacility unifiedParameterFacility;
	@Autowired
	private GlobalManagementService globalManagementService;
	@Autowired
	private CustAcctCardFacility queryFacility;
	@Autowired
	private UnifiedParamFacilityProvide unifiedParaFacilityProvide;

	@Autowired
	private RCcsTxnAdjLog rCcsTxnAdjLog;

	@PersistenceContext
	private EntityManager em;
	private QCcsTxnHst qCcsTxnHst = QCcsTxnHst.ccsTxnHst;

	/**
	 * @see 方法名：handler
	 * @see 描述：已收年费减免handler
	 * @see 创建日期：2015年6月25日下午6:18:23
	 * @author yanjingfeng
	 * 
	 * @param req
	 * @return
	 * @throws ProcessException
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	@Transactional
	public S12041Resp handler(S12041Req req) throws ProcessException {

		LogTools.printLogger(logger, "S12041", "已收年费减免", req, true);

		// 校验卡号的合法性
		CheckUtil.checkCardNo(req.getCard_no());

		// 获取卡片信息
		CcsCard CcsCard = queryFacility.getCardByCardNbr(req.getCard_no());
		CheckUtil.rejectNull(CcsCard, Constants.ERRB001_CODE, Constants.ERRB001_MES);

		// 获取年费交易码
		SysTxnCdMapping sysTxnCdMapping = unifiedParaFacilityProvide.cardFee();

		// 获取年费减免交易码
		SysTxnCdMapping sysTxnCdMappingDerateCardFee = unifiedParaFacilityProvide.derateCardFee();

		// 根据卡号和年费交易码查询年费收取记录
		JPAQuery query = new JPAQuery(em);
		List<CcsTxnHst> tmTxnHsts = query.from(qCcsTxnHst).where(qCcsTxnHst.cardNbr.eq(req.getCard_no()).and(qCcsTxnHst.txnCode.eq(sysTxnCdMapping.txnCd)))
				.orderBy(qCcsTxnHst.txnTime.desc()).list(qCcsTxnHst);
		// 当有多笔年费时，取最近一笔
		CcsTxnHst tmTxnHst = tmTxnHsts.get(0);
		CheckUtil.rejectNull(tmTxnHst, Constants.ERRB089_CODE, Constants.ERRB089_MES);

		// 创建返回对象
		S12041Resp resp = new S12041Resp();

		// 同时满足收取年费和客服费通知表中无年费减免记录时进行年费减免
		if (tmTxnHst != null) {
			BigDecimal cardFee = req.getDerate_card_fee_amt() == null ? tmTxnHst.getPostAmt() : req.getDerate_card_fee_amt();
			// 如果年费的入账金额小于减免金额则抛出异常
			if (tmTxnHst.getPostAmt().compareTo(cardFee) >= 0) {

				String transType = "21";
				String mcc = "6010";
				String b003 = transType + "0000";
				DecimalFormat df = new DecimalFormat("#0");
				BigDecimal txnAmt = cardFee;
				MsgParameter m = new MsgParameter();
				m.setB003(b003);
				m.setB025("00");
				m.setBusiDate(unifiedParaFacilityProvide.BusinessDate());
				m.setCardNo(tmTxnHst.getCardNbr());
				m.setCurrCd(tmTxnHst.getAcctType().getCurrencyCode());
				m.setMcc(mcc);
				m.setTxnAmt(df.format(txnAmt.doubleValue() * 100));
				// 组装金融交易报文
				YakMessage request = AuthCommUtils.makeCashLoanRequestMsg(m);
				request.getCustomAttributes().put(CustomAttributesKey.MANUAL_AUTH_FLAG, ManualAuthFlag.F);

				YakMessage responseMessage = authorizationService.authorize(request);

				if (responseMessage == null) {
					throw new ProcessException(Constants.ERRB090_CODE, Constants.ERRB090_MES);
				}

				String respCode = responseMessage.getBody(39);
				if (!StringUtils.equals(respCode, "00") && !StringUtils.equals(respCode, "11")) {
					throw new ProcessException(Constants.ERRB095_CODE, Constants.ERRB095_MES);
				}
				resp.setCard_no(req.getCard_no());
				resp.setCard_fee_date(tmTxnHst.getPostDate());
				resp.setCard_fee_amt(tmTxnHst.getPostAmt());
				resp.setDerate_card_fee_amt(cardFee);
				resp.setNext_card_fee_date(CcsCard.getNextCardFeeDate());
				resp.setDerate_reason(req.getDerate_reason());// TODO 减免原因没有处理

				// 记录账务调整操作日志 送给批量处理
				saveTmTranAdjLog(request, responseMessage, sysTxnCdMappingDerateCardFee, CcsCard);
			} else {
				throw new ProcessException(Constants.ERRB091_CODE, Constants.ERRB091_MES);
			}
		}

		LogTools.printLogger(logger, "S12041", "已收年费减免", resp, false);

		return resp;
	}

	/**
	 * @see 方法名：saveTmTranAdjLog
	 * @see 描述：记录账务调整操作日志 送给批量处理
	 * @see 创建日期：2015年6月25日下午6:19:12
	 * @author yanjingfeng
	 * 
	 * @param request
	 * @param response
	 * @param sysTxnCdMapping
	 * @param card
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public void saveTmTranAdjLog(YakMessage request, YakMessage response, SysTxnCdMapping sysTxnCdMapping, CcsCard card) {

		// 查找交易码参数记录
		TxnCd txnCd = unifiedParameterFacility.retrieveParameterObject(sysTxnCdMapping.txnCd, TxnCd.class);

		CcsTxnAdjLog tmTranAdjLog = new CcsTxnAdjLog();

		tmTranAdjLog.setOrg(OrganizationContextHolder.getCurrentOrg());
		tmTranAdjLog.setAcctNbr(card.getAcctNbr());
		tmTranAdjLog.setB011Trace(request.getBody(11));
		tmTranAdjLog.setB003ProcCode(request.getBody(3));
		tmTranAdjLog.setB004Amt(BigDecimal.valueOf(Double.valueOf(request.getBody(4))));
		tmTranAdjLog.setB007TxnTime(request.getBody(7));
		tmTranAdjLog.setB011Trace(request.getBody(11));
		tmTranAdjLog.setB032AcqInst(request.getBody(32));
		tmTranAdjLog.setB033FwdIns(request.getBody(33));
		tmTranAdjLog.setAuthCode(response.getBody(38));
		tmTranAdjLog.setB039RtnCode(response.getBody(39));
		tmTranAdjLog.setB042MerId(request.getBody(42));
		tmTranAdjLog.setB049CurrCode(request.getBody(49) + "");
		tmTranAdjLog.setCardNbr(request.getBody(2));
		tmTranAdjLog.setCurrency(request.getBody(49) + "");
		tmTranAdjLog.setDbCrInd(txnCd.logicMod.getDbCrInd());
		tmTranAdjLog.setMcc(request.getBody(18));
		tmTranAdjLog.setMti(AuthCommUtils.TPS_VAL_MTI_ADJUST);
		tmTranAdjLog.setOpId(AuthCommUtils.TPS_CUST_KEY_OPERATOR);
		tmTranAdjLog.setOpSeq(null);
		tmTranAdjLog.setOpTime(new Date());
		tmTranAdjLog.setRefNbr(AuthCommUtils.TPS_CUST_KEY_TXNREFNO);
		tmTranAdjLog.setTxnAmt(BigDecimal.valueOf(Double.valueOf(request.getBody(4)) / 100));
		tmTranAdjLog.setTxnCode(sysTxnCdMapping.txnCd);
		tmTranAdjLog.setRemark("");// TODO 是否应填写年费减免原因？
		tmTranAdjLog.setTxnDate(new Date());
		tmTranAdjLog.setVoidInd(Indicator.N);
		tmTranAdjLog.setLogBizDate(globalManagementService.getSystemStatus().getBusinessDate());

		rCcsTxnAdjLog.save(tmTranAdjLog);
	}

}
