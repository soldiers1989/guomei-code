package com.sunline.ccs.facility;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sunline.ark.support.service.YakMessage;
import com.sunline.ccs.facility.AuthCommUtils.MsgParameter;
import com.sunline.ccs.infrastructure.server.repos.RCcsCashloanDepoLog;
import com.sunline.ccs.infrastructure.server.repos.RCcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsCashloanDepoLog;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.enums.CPSMessageCategory;
import com.sunline.ccs.param.def.enums.CashLoanLimitType;
import com.sunline.pcm.param.def.Organization;
import com.sunline.ppy.api.AuthorizationService;
import com.sunline.ppy.api.BankClientService;
import com.sunline.ppy.api.CustomAttributesKey;
import com.sunline.ppy.dictionary.enums.LoanFeeMethod;
import com.sunline.ppy.dictionary.enums.LoanRegStatus;
import com.sunline.ppy.dictionary.enums.UnmatchStatus;
import com.sunline.ppy.dictionary.exception.ProcessException;

@Service
public class CashLendingFacility {

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private UnifiedParamFacilityProvide unifiedParamFacilityProvide;
	
	@Autowired
	private FetchSmsNbrFacility fetchSmsNbrFacility;
	
	@Autowired
	private RCcsLoanReg rCcsLoanReg;
	
	@Autowired
	private RCcsCashloanDepoLog rCcsCashloanDepoLog;
	
	public static final String TPS_VAL_MTI_ADJUST = "0200";
	
	public static final String TPS_VAL_MTI_ADJUST_VOID = "0420";
	
	public static final String TPS_CUST_KEY_OPERATOR = "cps.operatorID";
	
	public static final String TPS_CUST_KEY_TERMID = "cps.termid";
	
	public static final String TPS_CUST_KEY_TXNREFNO = "cps.cpstxnrefnbr";
	
	public static final String CONSUME_CODE = "000000";
	
	/**
	 * 生成现金分期放款响应参数
* @author fanghj
	 *
	 */
	public static class CashLendingMsg {
		
		/**
		 * 放款是否成功
		 */
		private boolean isSuccess;
		
		/**
		 * 39域响应码
		 */
		private String b039;
		
		private String errorMsg;

		public CashLendingMsg(boolean isSuccess) {
			this.isSuccess = isSuccess;
		}
		
		public boolean isSuccess() {
			return isSuccess;
		}

		public void setSuccess(boolean isSuccess) {
			this.isSuccess = isSuccess;
		}

		public String getB039() {
			return b039;
		}

		public void setB039(String b039) {
			this.b039 = b039;
		}

		public String getErrorMsg() {
			return errorMsg;
		}

		public void setErrorMsg(String errorMsg) {
			this.errorMsg = errorMsg;
		}
		
	}
	
	/**
	 * 用来现金分期实时放款
	 * @param loanReg
	 * @param acct
	 * @param tmCardO
	 * @param remark
	 * @return
	 */
	@SuppressWarnings("finally")
	public CashLendingMsg lending(BankClientService bankClientService, AuthorizationService authorizationService, CcsLoanReg loanReg, CcsAcct acct, String productCd, String remark) {
		
		YakMessage requestMessage = createLendingMessage(loanReg, acct);
		CcsCashloanDepoLog depoLog = getTmLendingLogByYakMessage(requestMessage, loanReg, acct);
		
		//用来判断最终放款结果是成功还是失败，默认是失败
		CashLendingMsg returnMsg = new CashLendingMsg(false);
		YakMessage responseMessage = null;
		
		if(logger.isDebugEnabled())
			logger.debug("发送现金分期放款报文，报文体:{}", requestMessage.getBodyAttributes());
		
		try {
			//调用行内借记卡核心放款
			responseMessage = bankClientService.process(requestMessage);
		} catch (Exception e) {
			//如果调用前置出现任何非业务异常则表明放款成功
			if(logger.isDebugEnabled())
				logger.debug("调用前置出现非业务异常，我方认为放款成功");
			
			loanReg.setLoanRegStatus(LoanRegStatus.S);
			depoLog.setLoanRegStatus(LoanRegStatus.S);
			loanReg.setRemark(MsgUtils.substring(RPC_EXCEPTION, 40));
			returnMsg.isSuccess = true;
		} finally {
			//初始化抛出异常信息
			String errorMassage = RPC_EXCEPTION;
			if (null != responseMessage) {
				
				if(logger.isDebugEnabled())
					logger.debug("接受到前置响应的报文，报文体:{}", responseMessage.getBodyAttributes());
				
				depoLog.setAuthCode(responseMessage.getBodyAttributes().get(38));
				
				if (StringUtils.isNotBlank(responseMessage.getBodyAttributes().get(39)) && !RESP_SCUS_CODE_TIMEOUT.equals(responseMessage.getBodyAttributes().get(39)))
					depoLog.setB039RtnCode(responseMessage.getBodyAttributes().get(39));
				
				//根据响应报文判断是否放款调用成功
				if (RESP_SCUS_CODE_00.equals(responseMessage.getBodyAttributes().get(39)) ||
						RESP_SCUS_CODE_11.equals(responseMessage.getBodyAttributes().get(39)) ||
							RESP_SCUS_CODE_A0.equals(responseMessage.getBodyAttributes().get(39)) ||
							 RESP_SCUS_CODE_TIMEOUT.equals(responseMessage.getBodyAttributes().get(39))) {
					loanReg.setLoanRegStatus(LoanRegStatus.S);
					depoLog.setLoanRegStatus(LoanRegStatus.S);
					loanReg.setRemark(remark);
					returnMsg.isSuccess = true;
				} else {
					//只要不是bank给的运行时异常，则全部设置成放款失败
					if (!returnMsg.isSuccess) {
						loanReg.setLoanRegStatus(LoanRegStatus.F);
						depoLog.setLoanRegStatus(LoanRegStatus.F);
						returnMsg.b039 = responseMessage.getBodyAttributes().get(39);
					}
				}
			} else {
				
				if(logger.isDebugEnabled())
					logger.debug("responseMessage为空，未接收到响应报文。错误提示:{}", errorMassage);
				
				loanReg.setLoanRegStatus(LoanRegStatus.S);
				depoLog.setLoanRegStatus(LoanRegStatus.S);
				loanReg.setRemark(MsgUtils.substring(errorMassage, 40));
				returnMsg.isSuccess = true;
			}
			
			rCcsLoanReg.save(loanReg);
			rCcsCashloanDepoLog.save(depoLog);
			
			//放款失败，异常在这最后抛出
			if (!returnMsg.isSuccess) {
				returnMsg.errorMsg = sendRestoreLines(authorizationService, productCd, loanReg);
			}
			return returnMsg;
		}
	}
	
	/**
	 * <p>发送金融交易恢复额度</p>
	 * <p>当现金分期实时放款失败，调用此方法发送金融交易恢复额度</p>
	 * @param productCd 产品参数
	 * @param loanReg
	 */
	private String sendRestoreLines(AuthorizationService authorizationService, String productCd, CcsLoanReg loanReg) {
		
		//如果恢复额度失败或者超时则返回对应话术
		String returnString = "";
		
		ProductCredit productCredit = unifiedParamFacilityProvide.productCredit(productCd);
		
		YakMessage response = authorizationService.authorize(AuthCommUtils.makeCashLoanReverseRequestMsg(makeMsgParam(loanReg, getTransType(productCredit), generateFlowNo())));
		
		if(response != null){
			String respCode = response.getBody(39);
			if(!StringUtils.equals(respCode, RESP_SCUS_CODE_00) && !StringUtils.equals(respCode, RESP_SCUS_CODE_11)){
				returnString = "现金分期放款失败恢复额度失败";
			}
		}else{
			returnString = "现金分期放款失败调用授权释放额度处理超时";
		}
		
		return returnString;
	}
	
	/**
	 * <p>根据页面选中的TM_LOAN_REQ组装成银联8583请求报文</p>
	 * <p>请求报文内容为：请求行内借记卡系统为现金分期实时放款</p>
	 * @param loanReg
	 * @return YakMessage
	 */
	private YakMessage createLendingMessage(CcsLoanReg loanReg, CcsAcct acct) {
		YakMessage requestMessage = new YakMessage();
		
		String businessDate = new SimpleDateFormat(DATE_FORMAT).format(unifiedParamFacilityProvide.BusinessDate());
		
		requestMessage.getCustomAttributes().put(CustomAttributesKey.MTI, TPS_VAL_MTI_ADJUST);
		requestMessage.getBodyAttributes().put(2, acct.getDdBankAcctNbr());
		requestMessage.getBodyAttributes().put(3, PROCESSING_CODE);
		
		//4域不能有小数，最右边两位默认为小数位
		if (null != loanReg.getLoanInitPrin())
			requestMessage.getBodyAttributes().put(4, new String(AuthCommUtils.leftPad(AuthCommUtils.getAuthAmt(loanReg.getLoanInitPrin()), 12)));
		
		String b007 = new SimpleDateFormat("MMddHHmmss").format(System.currentTimeMillis());
		requestMessage.getBodyAttributes().put(7, b007);
		
		//生成11域当日不重复跟踪审计号码
		int traceNbr = (int)(System.currentTimeMillis() % 1000) * 1000 + (int)(Math.random() * 1000);
		requestMessage.getBodyAttributes().put(11, new String(AuthCommUtils.leftPad(String.valueOf(traceNbr), 6)));
		
		requestMessage.getBodyAttributes().put(12, b007.substring(4));
		requestMessage.getBodyAttributes().put(13, b007.substring(0, 4));
		requestMessage.getBodyAttributes().put(15, businessDate.substring(4));
		requestMessage.getBodyAttributes().put(18, MERCHANTS_TYPE);
		requestMessage.getBodyAttributes().put(22, ENTRY_MODE);
		requestMessage.getBodyAttributes().put(25, POINT_CODE);
		requestMessage.getBodyAttributes().put(28, new StringBuffer(B028_DEBIT).append(TRANSACTION_FEE).toString());
		
		//加载机构层参数
		Organization organization = unifiedParamFacilityProvide.organizationBmp();
		requestMessage.getBodyAttributes().put(32, organization.CUP_CLEARING_ORG1);
		requestMessage.getBodyAttributes().put(33, organization.CUP_CLEARING_ORG1);
		requestMessage.getBodyAttributes().put(37, AuthCommUtils.makeRandomData(12));
		requestMessage.getBodyAttributes().put(41, TERMINAL_ID);
		requestMessage.getBodyAttributes().put(42, CARD_ACCEPT_ID);
		requestMessage.getBodyAttributes().put(43, Hex.encodeHexString(CARD_ACCEPT_NAME.getBytes()));
		
		//根据约定48域特殊域存放：CL+接受放款行名称+行号+账户户名
		StringBuffer b048 = new StringBuffer();
		b048.append(ADD_DATA_CL).append(MsgUtils.rightPadToString(acct.getDdBankName(), 80))
			.append(MsgUtils.rightPadToString(acct.getDdBankBranch(), 11))
			.append(MsgUtils.rightPadToString(acct.getDdBankAcctName(), 80))
			.append(businessDate);
				
		requestMessage.getBodyAttributes().put(48, Hex.encodeHexString(b048.toString().getBytes()));
		requestMessage.getBodyAttributes().put(49, acct.getAcctType().getCurrencyCode());
		requestMessage.getBodyAttributes().put(100, organization.CUP_CLEARING_ORG1);
		requestMessage.getBodyAttributes().put(102, loanReg.getCardNbr());
		requestMessage.getBodyAttributes().put(103, StringUtils.isBlank(acct.getName()) ? "" : acct.getName());
		return requestMessage;
	}
	
	/**
	 * <p>根据请求报文的YakMessage返回一个设置好值得TmLendingLog</p>
	 * @param requestMessage 请求报文
	 * @return
	 */
	private CcsCashloanDepoLog getTmLendingLogByYakMessage(YakMessage requestMessage, CcsLoanReg loanReg, CcsAcct acct) {
		CcsCashloanDepoLog depoLog = new CcsCashloanDepoLog();
		depoLog.setOrg(loanReg.getOrg());
		depoLog.setMti(requestMessage.getCustomAttributes().get(CustomAttributesKey.MTI).toString());
		depoLog.setAcctNbr(loanReg.getAcctNbr());
		depoLog.setAcctType(loanReg.getAcctType());
		depoLog.setRegisterId(loanReg.getRegisterId());
		depoLog.setB003ProcCode(requestMessage.getBodyAttributes().get(3));
		depoLog.setB032AcqInst(requestMessage.getBodyAttributes().get(32));
		depoLog.setB033FwdIns(requestMessage.getBodyAttributes().get(33));
		depoLog.setB011Trace(requestMessage.getBodyAttributes().get(11));
		depoLog.setSettleDate(requestMessage.getBodyAttributes().get(15));
		depoLog.setB002CardNbr(requestMessage.getBodyAttributes().get(2));
		depoLog.setB004Amt(loanReg.getLoanInitPrin());
		depoLog.setB049CurrCode(requestMessage.getBodyAttributes().get(49));
		depoLog.setSettleCode(requestMessage.getBodyAttributes().get(49));
		depoLog.setB007TxnTime(requestMessage.getBodyAttributes().get(7));
		depoLog.setMcc(requestMessage.getBodyAttributes().get(18));
		depoLog.setRlBankAcctName(acct.getDdBankAcctName());
		depoLog.setRlBankBranch(acct.getDdBankBranch());
		depoLog.setRlBankName(acct.getDdBankName());
		depoLog.setB102AcctIdent1(requestMessage.getBodyAttributes().get(102));
		depoLog.setB103AcctIdent2(acct.getName());
		depoLog.setB090OrigData(requestMessage.getBodyAttributes().get(90));
		depoLog.setRefNbr(loanReg.getRefNbr());
		depoLog.setBusinessDate(unifiedParamFacilityProvide.BusinessDate());
		depoLog.setSystemDate(new Date());
		depoLog.setMemoStatus(UnmatchStatus.U);
		return depoLog;
	}
	
	/**
	 * 根据放款成功与否获取短信模板号
	 * @param productCd 产品参数
	 * @param loanReg
	 * @param isLendingTrue
	 * @return
	 */
	public String getMsgCd(String productCd, CcsLoanReg loanReg, boolean isLendingTrue) {
		String msgCd = "";
		if (isLendingTrue) {
			//判断手续费收取方式
			if(loanReg.getLoanFeeMethod() == LoanFeeMethod.F){
				msgCd = fetchSmsNbrFacility.fetchMsgCd(productCd, CPSMessageCategory.CPS044);
			}else if(loanReg.getLoanFeeMethod() == LoanFeeMethod.E){
				msgCd = fetchSmsNbrFacility.fetchMsgCd(productCd, CPSMessageCategory.CPS045);
			}
		} else {
			msgCd = fetchSmsNbrFacility.fetchMsgCd(productCd, CPSMessageCategory.CPS063);
		}
		return msgCd;
	}
	
	/**
	 * <p>根据放款的结果调用短信接口发送短信</p>
	 * @param loanReg 现金分期记录
	 * @param isLendingTrue 是否放款成功。true表示放款成功
	 */
	public Map<String, Object> getSendMessage(CcsLoanReg loanReg, boolean isLendingTrue) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put(LOANINITPRIN, loanReg.getLoanInitPrin());
		if (isLendingTrue) {
			params.put(LOANINITTERM, loanReg.getLoanInitTerm());
			params.put(LOANFIXEDPMTPRIN, loanReg.getLoanFixedPmtPrin());
			
			//判断手续费收取方式
			if(loanReg.getLoanFeeMethod() == LoanFeeMethod.F){
				params.put(LOANINITFEE1, loanReg.getLoanInitFee());
			}else if(loanReg.getLoanFeeMethod() == LoanFeeMethod.E){
				params.put(LOANFIXEDFEE1, loanReg.getLoanFixedFee());
			}
		}
		
		return params;
	}
	
	/**
	 * 根据使用额度类型获得交易类型
	 * @param tmCard 
	 * @return
	 * @throws ProcessException
	 */
	private String getTransType(ProductCredit productCredit) throws ProcessException {
		String transType = "010000";//取现 
		if(productCredit.cashLoanLimitType == CashLoanLimitType.L){
			transType="000000";//消费
		}
		return transType;
	}
	
	/**
	 * <p>生成额度恢复接口37域随机码</p>
	 * @return
	 */
	private String generateFlowNo(){
		DateFormat df = new SimpleDateFormat("yyDS");
		Calendar calendar = Calendar.getInstance();
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		int second = calendar.get(Calendar.SECOND);
		StringBuffer sb = new StringBuffer(df.format(calendar.getTime()));
		sb.append(hour * 60 * 60 + minute * 60 + second);
		return sb.substring(1);
	}
	
	/**
	 * <p>生成YakMessage参数对象</p>
	 * @param loanReg
	 * @param mcc
	 * @param b003
	 * @return
	 */
	private MsgParameter makeMsgParam(CcsLoanReg loanReg, String b003, String b037) {
		MsgParameter msgParameter = new MsgParameter();
		msgParameter.setB003(b003);
		msgParameter.setB025(b003 == "000000" ? "00" : "64");
		msgParameter.setBusiDate(unifiedParamFacilityProvide.BusinessDate());
		msgParameter.setCardNo(loanReg.getCardNbr());
		msgParameter.setCurrCd(loanReg.getAcctType().getCurrencyCode());
		msgParameter.setMcc(RESTORE_LINES_MCC);
		msgParameter.setTxnAmt(AuthCommUtils.getAuthAmt(loanReg.getLoanInitPrin()));
		msgParameter.setB037(b037);
		msgParameter.setB038(loanReg.getOrigAuthCode());
		msgParameter.setB090(AuthCommUtils.makeFeild90(TPS_VAL_MTI_ADJUST, loanReg.getB011Trace(), loanReg.getB007TxnTime(), loanReg.getB032AcqInst(), loanReg.getB033FwdIns()));
		return msgParameter;
	}
	
	/**
	 * <p>3域，处理代码：写死470000</p>
	 */
	private static final String PROCESSING_CODE = "470000";
	
	/**
	 * <p>18域，商户类型：写死6010</p>
	 */
	private static final String MERCHANTS_TYPE = "6010";
	
	/**
	 * <p>22域，服务点输入模式：写死012</p>
	 */
	private static final String ENTRY_MODE = "012";
	
	/**
	 * <p>25域，POS代码：写死00</p>
	 */
	private static final String POINT_CODE = "00";
	
	/**
	 * <p>28域，交易费：写死8个0</p>
	 */
	private static final String TRANSACTION_FEE = "00000000";
	
	/**
	 * <p>41域，终端标识：写死8个0</p>
	 */
	private static final String TERMINAL_ID = "00000000";
	
	/**
	 * <p>42域，收卡商户识别：写死15个0</p>
	 */
	private static final String CARD_ACCEPT_ID = "000000000000000";
	
	/**
	 * <p>43域，收卡商户名称及位置：写死现金分期放款</p>
	 */
	private static final String CARD_ACCEPT_NAME = "现金分期放款";
	
	/**
	 * <p>48域，格式符：写死CL</p>
	 */
	private static final String ADD_DATA_CL = "CL";
	
	/**
	 * <p>行方放款成功响应码：00</p>
	 */
	private static final String RESP_SCUS_CODE_00 = "00";
	
	/**
	 * <p>行方放款成功响应码：11</p>
	 */
	private static final String RESP_SCUS_CODE_11 = "11";
	
	/**
	 * <p>MAC验证失败,我方认为成功响应码：A0</p>
	 */
	private static final String RESP_SCUS_CODE_A0 = "A0";
	
	/**
	 * <p>ffs-client超时,我方认为成功响应码：@@</p>
	 */
	private static final String RESP_SCUS_CODE_TIMEOUT = "@@";
	
	/**
	 * <p>28域交易方向：借记交易</p>
	 */
	private static final String B028_DEBIT = "D";
	
	/**
	 * <p>API调用BankClientService出现异常一律记录此信息，但放款成功</p>
	 */
	private static final String RPC_EXCEPTION = "调用BankClientService出现异常";
	
	/**
	 * <p>放款成功，弹窗标识</p>
	 */
	public static final String RPC_SUCCESS = "TRUE";
	
	/**
	 * <p>短信模板中分期总金额</p>
	 */
	private static final String LOANINITPRIN = "loanInitPrin";
	
	/**
	 * <p>短信模板中分期总期数</p>
	 */
	private static final String LOANINITTERM = "loanInitTerm";
	
	/**
	 * <p>短信模板中每期应还金额</p>
	 */
	private static final String LOANFIXEDPMTPRIN = "loanFixedPmtPrin";
	
	/**
	 * <p>短信模板中手续费</p>
	 * <p>一次性收取手续费此值为总手续费</p>
	 */
	private static final String LOANINITFEE1 = "loanInitFee1";
	
	/**
	 * <p>短信模板中手续费</p>
	 * <p>分期收取手续费此值为每期手续费</p>
	 */
	private static final String LOANFIXEDFEE1 = "loanFixedFee1";
	
	/**
	 * <p>固定日期格式，用来对businessDate格式化</p>
	 */
	private static final String DATE_FORMAT = "yyyyMMdd";
	
	/**
	 * <p>放款失败恢复额度mcc</p>
	 */
	private static final String RESTORE_LINES_MCC = "5999"; 
	
}
