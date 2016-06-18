package com.sunline.ccs.service.noticetemplate;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.api.GlobalManagementService;
import com.sunline.ccs.facility.SMSService.MsgFacility;
import com.sunline.ccs.facility.contract.AcctOTBCal;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.context.TxnContext;
import com.sunline.ccs.service.handler.QueryCommService;
import com.sunline.ccs.service.msdentity.SMSentitySendReq;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.LoanUsage;
import com.sunline.ppy.dictionary.enums.MsPayfrontError;
import com.sunline.ppy.dictionary.enums.MsRespCode;
import com.sunline.ppy.dictionary.exception.ProcessException;
/**
 * 发送放款成功，扣款成功/失败短信
 * @author zhengjf
 *
 */
@Service
public class SMSLoanDeductions {
	private Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	MsgFacility msgFacility;
    @Autowired
    GlobalManagementService globalManagementService;
    @Autowired
    private QueryCommService queryCommService;
    @PersistenceContext
	private EntityManager em;
    @Autowired
    private AcctOTBCal acctOTBCal;
    @Autowired
    private UnifiedParameterFacility unifiedParameterFacility;
    
	public void sendSMS(TxnContext context){
		
//		//当LoanCode为null的时候，查询数据库
//		if(context.getTxnInfo().getLoanCode()== null){
//			getLoanCode(context);
//		}
		try{
			//判断通知平台返回交易码是否为成功
			Map<Integer, Object> params=new HashMap<Integer, Object>();
			
			if (context.getTxnInfo().getLoanUsage()==LoanUsage.N || context.getTxnInfo().getLoanUsage()==LoanUsage.F) {
				//代扣短信模版
				params = cutPayment(context);
			}else if (context.getTxnInfo().getLoanUsage()==LoanUsage.L
					|| context.getTxnInfo().getLoanUsage()==LoanUsage.A
					|| context.getTxnInfo().getLoanUsage()==LoanUsage.E) {
				
				//如果原订单不对空并loanuasge为D 则为溢缴款转出计划
				if (context.getOrigOrder() !=null && 
						context.getOrigOrder().getLoanUsage() == LoanUsage.D) {
					
					this.transfer(context);
					
				}else {
					
					//代付短信模版
					params = this.sinPayment(context);
					
				}
				
			}else  if (context.getTxnInfo().getLoanUsage()==LoanUsage.R || context.getTxnInfo().getLoanUsage()==LoanUsage.V ) {
				//退货短信模版
				params = this.refund(context);
			}else if (context.getTxnInfo().getLoanUsage()==LoanUsage.D) {
				params = this.transfer(context);
			}
			if(logger.isDebugEnabled())
				logger.debug("短信参数大小：" + params.size());
			
			if (params.size()>1) {
				SMSentitySendReq req = new SMSentitySendReq();
				String mobileNo=null;
				//如果交易信息不存在手机号，则从客户表里面取
				if (context.getTxnInfo().getMobile()==null) {
					mobileNo=context.getCustomer().getMobileNo();
				}else {
					mobileNo=context.getTxnInfo().getMobile();
				}
				//组装报文
				req.setMobileNumber(mobileNo);
				//组装短信模版，业务类型
				req=msgFacility.getSMSInfo(req,params);
				
				if(logger.isDebugEnabled())
					logger.debug("请求信息：" + req);
				
				//发送通知
				msgFacility.sendSingleSms(context.getTxnInfo().getOrg(),req);
			}
		}catch(Exception e){
			//只打印日志，再往前抛异常，保证交易正常结束
			logger.error("通知平台短信发送异常",e);
		}
	}
	/**
	 * 组装报文及判断业务类型
	 * @param context
	 */
	private Map<Integer, Object> sinPayment(TxnContext context){
		Map<Integer, Object> params = new HashMap<Integer, Object>();

		if(logger.isDebugEnabled())
			logger.debug("responsCode：" + context.getTxnInfo().getResponsCode() + "," + "productCd：" + context.getAccount().getProductCd());
		//交易成功
		if(MsPayfrontError.S_0.getRespCode().equals(context.getTxnInfo().getResponsCode())) {
			
			//获取产品code
			String loancode1=null;
			if (null!=context.getLoan()) {
				loancode1=context.getLoan().getLoanCode();
			}else if (null != context.getLoanReg()&& null != context.getLoanReg().getLoanCode()){
				loancode1=context.getLoanReg().getLoanCode();
			}else if(null != context.getTxnInfo().getLoanCode()) {
				loancode1=context.getTxnInfo().getLoanCode();
			}else {
				 ProductCredit productCredit = unifiedParameterFacility.loadParameter(context.getAccount().getProductCd(), ProductCredit.class);
				 context.getTxnInfo().setLoanCode(productCredit.loanPlansMap.get(productCredit.defaultLoanType));
				 loancode1 = context.getTxnInfo().getLoanCode();
			}
			//马上贷
			if(LoanType.MCEI.equals(context.getLoanReg().getLoanType())){
				//业务类型
				String sourceBizType="ccs"+"-"+context.getAccount().getAcqId()+"-"+loancode1+"-"+Constants.LOAN_PAYMENT_SUCCESS;
				params.put(0,sourceBizType);
				//短信模版
				params.put(1,context.getAccount().getName());
			}
			
			//白名单-随借随还
			if(LoanType.MCAT.equals(context.getLoanReg().getLoanType())){
				//获取可用金额
				BigDecimal acctOTB=getAcctOTB(context);
				//业务类型
				String sourceBizType="ccs"+"-"+context.getAccount().getAcqId()+"-"+loancode1+"-"+Constants.WITHDRAW_CASH;
				params.put(0,sourceBizType);
				//短信模版
				params.put(1,context.getAccount().getName());
				params.put(2, acctOTB);
			}
		}else {
			 ProductCredit productCredit = unifiedParameterFacility.loadParameter(context.getAccount().getProductCd(), ProductCredit.class);
			 context.getTxnInfo().setLoanCode(productCredit.loanPlansMap.get(productCredit.defaultLoanType));
			 context.setProductCredit(productCredit);
			if(LoanType.MCAT.equals(productCredit.defaultLoanType)){
				//获取可用金额
				BigDecimal acctOTB=getAcctOTB(context);
				//业务类型
				String sourceBizType="ccs"+"-"+context.getAccount().getAcqId()+"-"+context.getTxnInfo().getLoanCode()+"-"+Constants.WITHDRAW_CASH_FAIL;
				params.put(0,sourceBizType);
				//短信模版
				params.put(1,context.getAccount().getName());
				params.put(2, acctOTB);
			}
		}
		return params;
	}
	/**
	 * 组装实时扣款失败报文
	 * @return
	 */
	private Map<Integer, Object> cutPayment(TxnContext context) {
		
		Map<Integer, Object> params = new HashMap<Integer, Object>();
		//获取产品code
		String loancode=null;
		if (null!=context.getLoan()) {
			loancode=context.getLoan().getLoanCode();
		}else if (null != context.getLoanReg()&& null != context.getLoanReg().getLoanCode()){
			loancode=context.getLoanReg().getLoanCode();
		}else if(null != context.getTxnInfo().getLoanCode()) {
			loancode=context.getTxnInfo().getLoanCode();
		}else {
			 ProductCredit productCredit = unifiedParameterFacility.loadParameter(context.getAccount().getProductCd(), ProductCredit.class);
			 context.getTxnInfo().setLoanCode(productCredit.loanPlansMap.get(productCredit.defaultLoanType));
			 loancode = context.getTxnInfo().getLoanCode();
		}
		//代扣成功
		if(MsPayfrontError.S_0.getRespCode().equals(context.getTxnInfo().getResponsCode())){
				Date bizDate = globalManagementService.getSystemStatus().getBusinessDate();
				String time=new SimpleDateFormat("yyyyMMdd").format(bizDate);
				String time1=time.substring(0, 4);
				String time2=time.substring(4, 6);
				String time3=time.substring(6, 8);
				
				
				String sourceBizType="ccs"+"-"+context.getAccount().getAcqId()+"-"+loancode+"-"+Constants.ACTIVE_REPAYMENT;	
				params.put(0, sourceBizType);
				params.put(1, context.getAccount().getName());
				params.put(2, time1);
				params.put(3, time2);
				params.put(4, time3);
				params.put(5, context.getTxnInfo().getTransAmt());
		}else{
		//判断失败原因是否为以下几种
		if (MsPayfrontError.E_00102.getCode().equals(context.getTxnInfo().getPayOthRespCode())
				|| MsPayfrontError.E_00103.getCode().equals(context.getTxnInfo().getPayOthRespCode())
				|| MsPayfrontError.E_00104.getCode().equals(context.getTxnInfo().getPayOthRespCode())){
			params.put(1, context.getTxnInfo().getPayOthRespMessage());
		}
		if (MsPayfrontError.E_00102.getCode().equals(context.getTxnInfo().getPayRespCode())
				|| MsPayfrontError.E_00103.getCode().equals(context.getTxnInfo().getPayRespCode())
				|| MsPayfrontError.E_00104.getCode().equals(context.getTxnInfo().getPayRespCode())) {
				params.put(1, context.getTxnInfo().getPayRespMessage());
		}
	
		//把业务类型组装进去
		String sourceBizType="ccs"+"-"+context.getAccount().getAcqId()+"-"+loancode+"-"+Constants.REAL_TIME_FROM_FAILURE;
		params.put(0, sourceBizType);
		}
			return params;
	}
	/**
	 * 计算可用金额
	 * @param context
	 * @return
	 */
	public BigDecimal getAcctOTB(TxnContext context) {
		QCcsAcct qCcsAcct = QCcsAcct.ccsAcct;
		
		CcsAcct acct = new JPAQuery(em).from(qCcsAcct).where(qCcsAcct.contrNbr.eq(context.getTxnInfo().getContrNo()))
				.singleResult(qCcsAcct);			
		if(null == acct){
			throw new ProcessException(MsRespCode.E_1003.getCode(),MsRespCode.E_1003.getMessage());
		}
		CcsAcctO acctO = queryCommService.loadAcctO(acct.getAcctNbr(), acct.getAcctType(), true);
		Date bizDate = globalManagementService.getSystemStatus().getBusinessDate();
		BigDecimal acctOTB = acctOTBCal.getAcctOTB(null, acct, acctO, bizDate );
		return acctOTB;
	}
	/**
	 * 组装商品贷退货成功/失败业务类型及报文
	 * @return
	 */
	private Map<Integer, Object> refund(TxnContext context) {
		
		Map<Integer, Object> params = new HashMap<Integer, Object>();
		//获取产品code
		String loancode=null;
		if (null!=context.getLoan()) {
			loancode=context.getLoan().getLoanCode();
		}else if (null != context.getLoanReg()&& null != context.getLoanReg().getLoanCode()){
			loancode=context.getLoanReg().getLoanCode();
		}else if(null != context.getTxnInfo().getLoanCode()) {
			loancode=context.getTxnInfo().getLoanCode();
		}else {
			 ProductCredit productCredit = unifiedParameterFacility.loadParameter(context.getAccount().getProductCd(), ProductCredit.class);
			 context.getTxnInfo().setLoanCode(productCredit.loanPlansMap.get(productCredit.defaultLoanType));
			 loancode = context.getTxnInfo().getLoanCode();
		}
		//退货成功
		if(MsPayfrontError.S_0.getRespCode().equals(context.getTxnInfo().getResponsCode())){
			
			String sourceBizType="ccs"+"-"+context.getAccount().getAcqId()+"-"+loancode+"-"+Constants.RETURN_GOODS;	
			params.put(0, sourceBizType);
			params.put(1, context.getAccount().getName());
			params.put(2, context.getAccount().getApplicationNo());
			
		}else{
	
		//失败把业务类型组装进去
		String sourceBizType="ccs"+"-"+context.getAccount().getAcqId()+"-"+loancode+"-"+Constants.RETURN_FAIL;
		params.put(0, sourceBizType);
		params.put(1, context.getAccount().getName());
		params.put(2, context.getAccount().getApplicationNo());
		}
		
		return params;
	}
	
	private Map<Integer, Object> transfer(TxnContext context) {
		Map<Integer, Object> params = new HashMap<Integer, Object>();
		
		//获取产品code
		String loancode=null;
		if (null!=context.getLoan()) {
			loancode=context.getLoan().getLoanCode();
		}else if (null != context.getLoanReg()&& null != context.getLoanReg().getLoanCode()){
			loancode=context.getLoanReg().getLoanCode();
		}else if(null != context.getTxnInfo().getLoanCode()) {
			loancode=context.getTxnInfo().getLoanCode();
		}else {
			 ProductCredit productCredit = unifiedParameterFacility.loadParameter(context.getAccount().getProductCd(), ProductCredit.class);
			 context.getTxnInfo().setLoanCode(productCredit.loanPlansMap.get(productCredit.defaultLoanType));
			 loancode = context.getTxnInfo().getLoanCode();
		}
		
		//退货成功
		if(MsPayfrontError.S_0.getRespCode().equals(context.getTxnInfo().getResponsCode())){
			//获取日期
			Date bizDate = globalManagementService.getSystemStatus().getBusinessDate();
			String time=new SimpleDateFormat("yyyyMMdd").format(bizDate);
			String time1=time.substring(0, 4);
			String time2=time.substring(4, 6);
			String time3=time.substring(6, 8);
			
			String card = context.getTxnInfo().getCardNo();
			int size = card.length();
			
			String cardAfter = card.substring(size-4, size);
			
			//组装业务类型
			String sourceBizType="ccs"+"-"+context.getAccount().getAcqId()+"-"+loancode+"-"+Constants.OVERFLOW_PAYMENT_SUCCESS;	
			params.put(0, sourceBizType);
			params.put(1, context.getAccount().getName());
			params.put(2, time1);
			params.put(3, time2);
			params.put(4, time3);
			params.put(5, context.getTxnInfo().getTransAmt());
			params.put(6, cardAfter);
			
		}else if (MsPayfrontError.E_00206.getCode().equals(context.getTxnInfo().getPayOthRespCode())
					|| MsPayfrontError.E_00206.getCode().equals(context.getTxnInfo().getPayRespCode())){
		
				String message = context.getTxnInfo().getPayOthRespMessage()== null?
						context.getTxnInfo().getPayRespMessage() : context.getTxnInfo().getPayOthRespMessage();
				
				params.put(1, context.getAccount().getName());
				params.put(2, message);
			
				//失败把业务类型组装进去
				String sourceBizType="ccs"+"-"+context.getAccount().getAcqId()+"-"+loancode+"-"+Constants.OVERFLOW_PAYMENT_FAILURE;
				params.put(0, sourceBizType);
			
		}
		
		return params;
	}

}