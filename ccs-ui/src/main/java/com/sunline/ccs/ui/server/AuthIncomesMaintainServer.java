/**
 * 
 */
package com.sunline.ccs.ui.server;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.expr.BooleanExpression;
import com.sunline.acm.service.api.GlobalManagementService;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ark.support.service.YakMessage;
import com.sunline.ccs.facility.MsgUtils;
import com.sunline.ccs.infrastructure.server.repos.RCcsAuthmemoO;
import com.sunline.ccs.infrastructure.shared.model.QCcsAuthmemoO;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsCardLmMapping;
import com.sunline.ccs.infrastructure.shared.model.CcsAuthmemoO;
import com.sunline.ccs.ui.server.commons.CPSBusProvide;
import com.sunline.ccs.ui.server.commons.CheckUtil;
import com.sunline.ccs.ui.server.commons.DateTools;
import com.sunline.kylin.web.ark.client.JPAQueryFetchResponseBuilder;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.kylin.web.core.client.exception.FlatException;
import com.sunline.ppy.api.AuthorizationService;
import com.sunline.ppy.api.CustomAttributesKey;
import com.sunline.ppy.dictionary.enums.InputSource;
import com.sunline.ppy.dictionary.enums.ManualAuthFlag;

/**
 * 授权未入账交易维护
* @author dch
 *
 */
@Controller
@RequestMapping(value="/t3402Server")
public class AuthIncomesMaintainServer {
	
	// A表示查卡片所属账户
	private static final String SEARCH_TRANSTYPE_A = "A";
	// C表示查本卡片
	private static final String SEARCH_TRANSTYPE_C = "C";
	
	private static final String CHANNEL_CUP = "CUP";
	private static final String CHANNEL_BANK = "BANK";
//	private static final String CHANNEL_ALL = "ALL";
	private static final String CHANNEL_VISA = "VISA";
	private static final String CHANNEL_MC = "MC";
	private static final String CHANNEL_AMEX = "AMEX";
	private static final String CHANNEL_ICL = "ICL";
	private static final String CHANNEL_JCB = "JCB";
	
	private static final String TPS_CUST_KEY_OPERATOR = "css.operatorID";
	private static final String TPS_CUST_KEY_TERMID = "css.termid";
//	private static final String TPS_CUST_KEY_ADJFLAG = "css.adjustflag";
	
	private static final String TPS_CUST_VAL_TERMID = "cps-app";
//	private static final String TPS_CUST_VAL_ADJFLAG = "auth";
//	private static final String TPS_INPUT_SOURCE = "BANK";
	
	private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMddHHmmss");
	private String dateTimeStr = "";
	private String traceNbrStr = "";
	private String amountStr = "";
	
	@Resource(name="authorizationService")
	private AuthorizationService authorizationService;

	@Autowired
	private OpeLogUtil opeLogUtil;

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	
	@PersistenceContext
	private EntityManager em;
	
	@Autowired
	private GlobalManagementService globalManagementService;

	@Autowired
	private CPSBusProvide cpsBusProvide;
	
	private QCcsAuthmemoO qTmUnmatchO = QCcsAuthmemoO.ccsAuthmemoO;
	
	@Autowired
	private RCcsAuthmemoO rTmUnmatchO;
	
	@ResponseBody()
	@RequestMapping(value="/getAuthUnTranList",method={RequestMethod.POST})
	public FetchResponse getAuthUnTranList(@RequestBody FetchRequest request) throws FlatException {
		JPAQuery query = new JPAQuery(em).from(qTmUnmatchO);
		try {
			// 卡号
			Long acctNbr = request.getParameter("acctNbr") == null ? null : Long.parseLong(request.getParameter("acctNbr").toString());
			// 开始日期
			Date startDate = request.getParameter("bDate") == null ? null : new Date(Long.parseLong(request.getParameter("bDate").toString()));
			// 结束日期
			Date endDate = request.getParameter("eDate") == null ? null : new Date(Long.parseLong(request.getParameter("eDate").toString()));
			// 卡账标识
			//String cardAccountIndicator = request.getParameter("cardAccountIndicator") == null ? null : request.getParameter("cardAccountIndicator").toString();
			
			String inputSource = request.getParameter("inputSource") == null ? null : request.getParameter("inputSource").toString();
			
			/*logger.info("getAuthUnTranList:卡号后四位[" + CodeMarkUtils.subCreditCard(cardNo)
					+ "],开始日期[" + df.format(startDate) + "],结束日期[" + df.format(endDate) + "]");*/
//			if(cardNo != null){
//				CheckUtil.checkCardNo(cardNo);
//			}
//			CheckUtil.rejectNull(cardAccountIndicator, "卡帐指示标志不能为空");
			
			//查找卡片记录
//			CcsCard tmCard = cpsBusProvide.getTmCardTocardNbr(cardNo);
			
//			if (SEARCH_TRANSTYPE_A.equals(cardAccountIndicator)) {
				BooleanExpression expr = qTmUnmatchO.org.eq(OrganizationContextHolder.getCurrentOrg())
						.and(qTmUnmatchO.acctNbr.eq(acctNbr));
				
				if(CHANNEL_BANK.equals(inputSource)){
					expr = expr.and(qTmUnmatchO.inputSource.eq(InputSource.valueOf(CHANNEL_BANK)));
				}
				if(CHANNEL_CUP.equals(inputSource)){
					expr = expr.and(qTmUnmatchO.inputSource.eq(InputSource.valueOf(CHANNEL_CUP)));
				}
				if(CHANNEL_VISA.equals(inputSource)){
					expr = expr.and(qTmUnmatchO.inputSource.eq(InputSource.valueOf(CHANNEL_VISA)));
				}
				if(CHANNEL_MC.equals(inputSource)){
					expr = expr.and(qTmUnmatchO.inputSource.eq(InputSource.valueOf(CHANNEL_MC)));
				}
				if(CHANNEL_ICL.equals(inputSource)){
					expr = expr.and(qTmUnmatchO.inputSource.eq(InputSource.valueOf(CHANNEL_ICL)));
				}
				if(CHANNEL_JCB.equals(inputSource)){
					expr = expr.and(qTmUnmatchO.inputSource.eq(InputSource.valueOf(CHANNEL_JCB)));
				}
				if(CHANNEL_AMEX.equals(inputSource)){
					expr = expr.and(qTmUnmatchO.inputSource.eq(InputSource.valueOf(CHANNEL_AMEX)));
				}
				
				if(null != startDate) {
					expr = expr.and(qTmUnmatchO.logOlTime.goe(DateTools.startDateStamp(startDate)));
				}
				
				if(null != endDate) {
					expr = expr.and(qTmUnmatchO.logOlTime.loe(DateTools.endDateStamp(endDate)));
				}
				query = query.where(expr);
//			}
			/*弃用卡片层查询
			else if (cardAccountIndicator.equals(SEARCH_TRANSTYPE_C)) {
				BooleanExpression expr = qTmUnmatchO.org.eq(OrganizationContextHolder.getCurrentOrg())
						.and(qTmUnmatchO.logicCardNbr.eq(tmCard.getLogicCardNbr()));
				
				if(CHANNEL_BANK.equals(inputSource)){
					expr = expr.and(qTmUnmatchO.inputSource.eq(InputSource.valueOf(CHANNEL_BANK)));
				}
				if(CHANNEL_CUP.equals(inputSource)){
					expr = expr.and(qTmUnmatchO.inputSource.eq(InputSource.valueOf(CHANNEL_CUP)));
				}
				if(CHANNEL_VISA.equals(inputSource)){
					expr = expr.and(qTmUnmatchO.inputSource.eq(InputSource.valueOf(CHANNEL_VISA)));
				}
				if(CHANNEL_MC.equals(inputSource)){
					expr = expr.and(qTmUnmatchO.inputSource.eq(InputSource.valueOf(CHANNEL_MC)));
				}
				if(CHANNEL_ICL.equals(inputSource)){
					expr = expr.and(qTmUnmatchO.inputSource.eq(InputSource.valueOf(CHANNEL_ICL)));
				}
				if(CHANNEL_JCB.equals(inputSource)){
					expr = expr.and(qTmUnmatchO.inputSource.eq(InputSource.valueOf(CHANNEL_JCB)));
				}
				if(CHANNEL_AMEX.equals(inputSource)){
					expr = expr.and(qTmUnmatchO.inputSource.eq(InputSource.valueOf(CHANNEL_AMEX)));
				}
				if(null != startDate) {
					expr = expr.and(qTmUnmatchO.logOlTime.goe(DateTools.startDateStamp(startDate)));
				}
				if(null != endDate) {
					expr = expr.and(qTmUnmatchO.logOlTime.loe(DateTools.endDateStamp(endDate)));
				}
				query = query.where(expr);
			} else{
				throw new FlatException("无效的卡帐指示标志");
			}
			*/
			query.orderBy(qTmUnmatchO.origTransDate.desc());
		} catch (Exception e) {
			logger.debug(""+e);
			throw new FlatException("贷款审核信息查询报错："+e.getMessage());
		}
		
		return new JPAQueryFetchResponseBuilder(request, query)
		.addFieldMapping(CcsAuthmemoO.P_GuarantyId,qTmUnmatchO.guarantyId)
		.addFieldMapping(CcsAuthmemoO.P_LogKv, qTmUnmatchO.logKv)
		.addFieldMapping(CcsAuthmemoO.P_B002CardNbr, qTmUnmatchO.b002CardNbr)
		.addFieldMapping(CcsAuthmemoO.P_Org, qTmUnmatchO.org)
		.addFieldMapping(CcsAuthmemoO.P_AcctNbr, qTmUnmatchO.acctNbr)
		.addFieldMapping(CcsAuthmemoO.P_LogicCardNbr, qTmUnmatchO.logicCardNbr)
		.addFieldMapping(CcsAuthmemoO.P_LogOlTime, qTmUnmatchO.logOlTime)
		.addFieldMapping(CcsAuthmemoO.P_AcctType,qTmUnmatchO.acctType)
		.addFieldMapping(CcsAuthmemoO.P_TransportTime,qTmUnmatchO.transportTime)
		.addFieldMapping(CcsAuthmemoO.P_TxnCurrency,qTmUnmatchO.txnCurrency)
		.addFieldMapping(CcsAuthmemoO.P_AcqAddress, qTmUnmatchO.acqAddress)
		.addFieldMapping(CcsAuthmemoO.P_TxnAmt,qTmUnmatchO.txnAmt)
		.addFieldMapping(CcsAuthmemoO.P_AuthCode,qTmUnmatchO.authCode)
		.addFieldMapping(CcsAuthmemoO.P_Mcc,qTmUnmatchO.mcc)
		.addFieldMapping(CcsAuthmemoO.P_ChbCurrency,qTmUnmatchO.chbCurrency)
		.addFieldMapping(CcsAuthmemoO.P_ChbTxnAmt,qTmUnmatchO.chbTxnAmt)
		.addFieldMapping(CcsAuthmemoO.P_AuthTxnStatus,qTmUnmatchO.authTxnStatus)
		.addFieldMapping(CcsAuthmemoO.P_TxnType,qTmUnmatchO.txnType)
		.addFieldMapping(CcsAuthmemoO.P_InputSource,qTmUnmatchO.inputSource)
		.build();
	}

	/**
	 * 获取授权未匹配记录
	 * @param logKey
	 * @return
	 * @throws FlatException
	 */
	@ResponseBody()
	@RequestMapping(value="/getTmUnmatchO",method={RequestMethod.POST})
	public CcsAuthmemoO getTmUnmatchO(@RequestBody Long logKey) throws FlatException {
		return rTmUnmatchO.findOne(logKey);
	}

	/**
	 * 撤销
	 * @param logKey
	 * @throws FlatException
	 */
	@ResponseBody()
	@RequestMapping(value="/cancelTrans",method={RequestMethod.POST})
	public void cancelTrans(@RequestBody Long logKey) throws FlatException {
		logger.info("cancelTrans: logKey=["+logKey+"]");
		
		/*---- 1.校验输入项目是否合法 ---*/
		CcsAuthmemoO tmUnmatchO = rTmUnmatchO.findOne(logKey);
		CheckUtil.rejectNull(tmUnmatchO, "未找到原交易记录["+logKey+"]");
		
		/*---- 2.组装金融交易报文 ---*/
		
		amountStr = new String(MsgUtils.leftPad(tmUnmatchO.getB004Amt().toString(), 12));
		
		//获取交易时间和创建流水号
		long currentTimeMillis = System.currentTimeMillis();
		dateTimeStr = simpleDateFormat.format(currentTimeMillis);
		int traceNbr = (int)(currentTimeMillis % 1000) * 1000 + (int)(Math.random() * 1000);
		traceNbrStr = new String(MsgUtils.leftPad(traceNbr+"", 6));
		
		//组装金融交易请求报文
		YakMessage requestMessage = new YakMessage();
//		Map<Integer, String> headAttributes = requestMessage.getHeadAttributes();
		Map<Integer, String> bodyAttributes = requestMessage.getBodyAttributes();
		Map<String, Serializable> customAttributes = requestMessage.getCustomAttributes();
		
//		requestMessage.setSrcChannelId(TPS_INPUT_SOURCE); //inputsource =内管
//		headAttributes.put(11, "420"); //MTI
		
		bodyAttributes.put(2, tmUnmatchO.getB002CardNbr()); //介质卡号:B002
		bodyAttributes.put(3, tmUnmatchO.getB003ProcCode());
		bodyAttributes.put(4, amountStr); //交易金额:b004，交易金额不带小数点，最右边两位视为小数位
		if(null != tmUnmatchO.getB006ChbAmt()) {
			bodyAttributes.put(6, tmUnmatchO.getB006ChbAmt().toString()); //持卡人扣账金额
			bodyAttributes.put(51, tmUnmatchO.getB051ChbCurrCode()); //持卡人账户货币代码
		}
		bodyAttributes.put(7, dateTimeStr); //交易时间(客服的自然时间):b007
		bodyAttributes.put(11, traceNbrStr); //系统流水号(客服人工授权流水号):b011
		bodyAttributes.put(14, tmUnmatchO.getExpireDate());
		bodyAttributes.put(18, tmUnmatchO.getMcc()); //商户类型:B018 固定值全9
		bodyAttributes.put(22, tmUnmatchO.getB022Entrymode()); 
		bodyAttributes.put(25, tmUnmatchO.getB025Entrycond()); 
		bodyAttributes.put(32, tmUnmatchO.getB032AcqInst()); //收单机构id:b032 固定值
		bodyAttributes.put(33, tmUnmatchO.getB033FwdIns()); //转发机构id:b033 固定值
		bodyAttributes.put(37, tmUnmatchO.getB037RefNbr());
		bodyAttributes.put(38, tmUnmatchO.getAuthCode());
		bodyAttributes.put(40, "898"); //终端类型码: AIC内管系统
		bodyAttributes.put(42, tmUnmatchO.getB042MerId()); //商户id :B042 固定值全9
//		bodyAttributes.put(49, tmUnmatchO.getB049CurrCode()); //持卡人账户币种:b049
		bodyAttributes.put(49, "156");//暂定为人民币
		
		//域90组成
		//位置  1-4 字节  5-10 字节  11-20 字节  21-31 字节  32-42 字节
		//子域  90.1 MTI 90.2 B011  90.3 B007  90.4 B032  90.5 B033
		byte[] mtiBytes = MsgUtils.leftPad(tmUnmatchO.getMti(), 4);
		byte[] b011Bytes = MsgUtils.leftPad(tmUnmatchO.getB011Trace(), 6);
		byte[] b007Bytes = MsgUtils.leftPad(tmUnmatchO.getB007TxnTime(), 10);
		byte[] b032Bytes = MsgUtils.leftPad(tmUnmatchO.getB032AcqInst(), 11);
		byte[] b033Bytes = MsgUtils.leftPad(tmUnmatchO.getB033FwdIns(), 11);
		byte[] b090Bytes = new byte[42];
		int b090Index = 0;
		for(int i = 0; i < mtiBytes.length; i++) {
			b090Bytes[b090Index] = mtiBytes[i];
			b090Index++;
		}
		for(int i = 0; i < b011Bytes.length; i++) {
			b090Bytes[b090Index] = b011Bytes[i];
			b090Index++;
		}
		for(int i = 0; i < b007Bytes.length; i++) {
			b090Bytes[b090Index] = b007Bytes[i];
			b090Index++;
		}
		for(int i = 0; i < b032Bytes.length; i++) {
			b090Bytes[b090Index] = b032Bytes[i];
			b090Index++;
		}
		for(int i = 0; i < b033Bytes.length; i++) {
			b090Bytes[b090Index] = b033Bytes[i];
			b090Index++;
		}
		
		bodyAttributes.put(90, new String(b090Bytes));
		

		/*---- yakmessage定义字段 ------*/
		customAttributes.put(CustomAttributesKey.MTI, "0420");
		customAttributes.put(CustomAttributesKey.INPUT_SOURCE, InputSource.BANK);
		customAttributes.put(CustomAttributesKey.MANUAL_AUTH_FLAG, ManualAuthFlag.A);
		customAttributes.put(TPS_CUST_KEY_OPERATOR, OrganizationContextHolder.getUsername()); //操作员ID
		customAttributes.put(TPS_CUST_KEY_TERMID, TPS_CUST_VAL_TERMID); //操作终端ID
		customAttributes.put(CustomAttributesKey.BUSINESS_DATE_KEY_NAME, globalManagementService.getSystemStatus().getBusinessDate());
//		customAttributes.put(TPS_CUST_KEY_ADJFLAG, TPS_CUST_VAL_ADJFLAG); //人工授权借贷调标志
		
		/*---- 3.发送金融交易，并等待返回消息 ---*/
		YakMessage responseMessage = authorizationService.authorize(requestMessage);
		String rspCode = "";
		if(null != responseMessage) {
			rspCode = responseMessage.getBody(39);
			
			if(rspCode == null) {
				rspCode = "";
			}
		}
		
		if(rspCode.equals("00")) {//交易成功
			//记录操作日志
	        opeLogUtil.cardholderServiceLog(
		    		"3402", null, tmUnmatchO.getB002CardNbr(), logKey+","+dateTimeStr+","+traceNbrStr, "授权交易人工撤消");
		} else {//交易失败
			//记录操作日志
	        opeLogUtil.cardholderServiceLog(
		    		"3402", null, tmUnmatchO.getB002CardNbr(), logKey+","+dateTimeStr+","+traceNbrStr, "授权交易人工撤消，失败代码["+rspCode+"]");
			throw new FlatException("授权交易人工撤消失败，失败代码["+rspCode+"]");
		}
		
	}
	
	public static void main(String[] args){
		String amount = "200000.34";
		System.out.println(amount.getBytes().length);
	}

}
