//package com.sunline.ccs.service.noticetemplate;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import com.sunline.ccs.facility.SMSService.MsgFacility;
//import com.sunline.ccs.service.context.TxnContext;
//import com.sunline.ccs.service.msdentity.SMSentitySendReq;
//import com.sunline.ppy.dictionary.enums.LoanUsage;
//import com.sunline.ppy.dictionary.enums.MsPayfrontError;
///**
// * 商品贷发送放款成功，退货成功/失败短信
// * @author Mr.L
// *
// */
//@Service
//public class SMSLoanCommodity {
//	private Logger logger = LoggerFactory.getLogger(getClass());
//	@Autowired
//	MsgFacility msgFacility;
//	public void sendSMS(TxnContext context){
//		
////		//当LoanCode为null的时候，查询数据库
////		if(context.getTxnInfo().getLoanCode()==null){
////			msgFacility.getLoanCode(context);
////		}
//		//判断通知平台返回交易码是否为成功
//		//发送通知
//		try{
//		Map<Integer, Object> params=new HashMap<Integer, Object>();
//		
//		if(context.getTxnInfo().getLoanUsage() != LoanUsage.R){
//			params = assemble(context);
//		}else {
//			params = debit(context);
//		}
//		
//		if(logger.isDebugEnabled())
//			logger.debug("短信参数大小：" + params.size());
//		
//		if (params.size()>1) {
//			SMSentitySendReq req = new SMSentitySendReq();
//			String mobileNo=null;
//			//如果交易信息不存在手机号，则从客户表里面取
//			if (context.getTxnInfo().getMobile()==null) {
//				mobileNo=context.getCustomer().getMobileNo();
//			}else {
//				mobileNo=context.getTxnInfo().getMobile();
//			}
//			//组装报文
//			req.setMobileNumber(mobileNo);
//			//组装短信模版，业务类型
//			req=msgFacility.getSMSInfo(req,params);
//			
//			if(logger.isDebugEnabled())
//				logger.debug("请求信息：" + req);
//			
//		
//				msgFacility.sendSingleSms(context.getTxnInfo().getOrg(),req);
//			}
//			}catch(Exception e){
//				//只打印日志，再往前抛异常，保证交易正常结束
//				logger.error("通知平台短信发送异常",e);
//		}
//	}
//	/**
//	 * 组装商品贷放款成功报文及业务类型
//	 * @param context
//	 */
//	private Map<Integer, Object> assemble(TxnContext context){
//		Map<Integer, Object> params = new HashMap<Integer, Object>();
//
//		if(logger.isDebugEnabled())
//			logger.debug("responsCode：" + context.getTxnInfo().getResponsCode() + "," + "productCd：" + context.getAccount().getProductCd());
//		
//		//交易成功
//		if(MsPayfrontError.S_0.getRespCode().equals(context.getTxnInfo().getResponsCode())) {
//				
//				//业务类型
//				String sourceBizType="ccs"+"-"+context.getAccount().getAcqId()+"-"+context.getTxnInfo().getLoanCode()+"-"+"loan_payment_success";
//				params.put(0,sourceBizType);
//				//短信模版
//				params.put(1,context.getAccount().getName());
//			}
//	
//		return params;
//	}
//	/**
//	 * 组装商品贷退货成功/失败业务类型及报文
//	 * @return
//	 */
//	private Map<Integer, Object> debit(TxnContext context) {
//		
//		Map<Integer, Object> params = new HashMap<Integer, Object>();
//		//获取产品code
//		String loancode=null;
//		if (context.getLoan()!=null) {
//			loancode=context.getLoan().getLoanCode();
//		}else {
//			loancode=context.getTxnInfo().getLoanCode();
//		}
//		//退货成功
//		if(MsPayfrontError.S_0.getRespCode().equals(context.getTxnInfo().getResponsCode())){
//			
//			String sourceBizType="ccs"+"-"+context.getAccount().getAcqId()+"-"+loancode+"-"+"return_goods";	
//			params.put(0, sourceBizType);
//			params.put(1, context.getAccount().getName());
//			params.put(2, context.getAccount().getApplicationNo());
//			
//		}else{
//	
//		//失败把业务类型组装进去
//		String sourceBizType="ccs"+"-"+context.getAccount().getAcqId()+"-"+loancode+"-"+"return_fail";
//		params.put(0, sourceBizType);
//		params.put(1, context.getAccount().getName());
//		params.put(2, context.getAccount().getApplicationNo());
//		}
//		
//		return params;
//	}
//}