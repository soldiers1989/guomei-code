package com.sunline.ccs.service.handler.sunshine;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunline.acm.service.api.GlobalManagementService;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.facility.TxnUtils;
import com.sunline.ccs.facility.order.OrderFacility;
import com.sunline.ccs.facility.order.PaymentFacility;
import com.sunline.ccs.infrastructure.shared.model.CcsOrder;
import com.sunline.ccs.param.def.enums.AuthTransDirection;
import com.sunline.ccs.param.def.enums.TransAmtDirection;
import com.sunline.ccs.service.context.LoanInfo;
import com.sunline.ccs.service.context.TxnContext;
import com.sunline.ccs.service.context.TxnInfo;
import com.sunline.ccs.service.entity.S39002PayCutPaymentQueryReq;
import com.sunline.ccs.service.entity.S39002PayCutPaymentQueryResp;
import com.sunline.ccs.service.handler.SunshineCommService;
import com.sunline.ccs.service.handler.collection.CollectionLogic;
import com.sunline.ccs.service.noticetemplate.SMSLoanDeductions;
import com.sunline.ppy.dictionary.enums.AuthTransStatus;
import com.sunline.ppy.dictionary.enums.AuthTransType;
import com.sunline.ppy.dictionary.enums.InputSource;
import com.sunline.ppy.dictionary.enums.LoanAction;
import com.sunline.ppy.dictionary.enums.LoanRegStatus;
import com.sunline.ppy.dictionary.enums.LoanUsage;
import com.sunline.ppy.dictionary.enums.MsPayfrontError;
import com.sunline.ppy.dictionary.enums.MsRespCode;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * 代扣确认交易
 * @author zqx
 *
 */
@Service
public class TNRPayCutPaymentQuery {
	private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    CustAcctCardFacility queryFacility;
	@Autowired
	private SunshineCommService sunshineCommService;
	@Autowired
	private PaymentFacility paymentFacility;
	@Autowired
	private OrderFacility orderFacility;
	@Autowired
	private GlobalManagementService globalManageService;
	@Autowired
	private SMSLoanDeductions smsLoanDeductions;
	@Autowired
	private CollectionLogic collectionLogic;
	@Autowired
	private TxnUtils txnUtils;
	
	public S39002PayCutPaymentQueryResp handler(S39002PayCutPaymentQueryReq req) throws ProcessException {
		LogTools.printLogger(logger, "S39002PayCutPaymentQueryReq", "代扣查询", req, true);
		S39002PayCutPaymentQueryResp resp = new S39002PayCutPaymentQueryResp();
		LogTools.printObj(logger, req, "S39002PayCutPaymentQueryReq请求参数");
		TxnContext context = new TxnContext();
		TxnInfo txnInfo = new TxnInfo();
		LoanInfo loanInfo = new LoanInfo();
		String payJson = "";
		
		context.setTxnInfo(txnInfo);
		context.setLoanInfo(loanInfo);
		context.setSunshineRequestInfo(req);
		
		try {
			if(null != req && null != req.getOrderid()) {
				
				// 获取原订单信息
				context.setOrigOrder(orderFacility.findById(req.getOrderid(), true));

				//初始化交易相关数据
				initTxnInfo(req,context.getOrigOrder(),txnInfo);
				sunshineCommService.loadAcct(context,context.getOrigOrder().getAcctNbr(),context.getOrigOrder().getAcctType(), true);
				sunshineCommService.loadAcctO(context,context.getOrigOrder().getAcctNbr(),context.getOrigOrder().getAcctType(), true);
				sunshineCommService.loadCard(context, true);
				sunshineCommService.loadCustomer(context, true);
				sunshineCommService.loadLoanReg(context,null, LoanRegStatus.A,LoanAction.O,false);//存在预约，且预约还款日到达，做预约实时代扣
				
				context.getTxnInfo().setDueBillNo(context.getOrigOrder().getDueBillNo());
				context.getTxnInfo().setTransAmt(context.getOrigOrder().getTxnAmt());
			    //业务处理 单独事务
				payJson = this.bizProc(context,payJson);
				
				//处理结果，单独事务处理
				sunshineCommService.payResultProc(context,payJson);
				
				//向通知平台发送通知	非催收平台发起的
				if (!context.getOrigOrder().getSubTerminalType().equals("CLS")) {
					smsLoanDeductions.sendSMS(context);	
				}else {
					if(logger.isDebugEnabled())
						logger.debug("SubTerminalType():[{}],催收平台发起不支持发送还款通知",context.getOrigOrder().getSubTerminalType());
				}
				
				//向催收平台发送通知	
				if (context.getOrigOrder().getChannelId() != InputSource.SUNS) {
					collectionLogic.sendCSPlatform(context);
				}else {
					if(logger.isDebugEnabled())
						logger.debug("channel_id:[{}],不支持发送还款通知",txnInfo.getInputSource());
				}	
				

			}else {
				throw new ProcessException(MsRespCode.E_1034.getCode(),MsRespCode.E_1034.getMessage());
			}

		}catch (ProcessException pe){
			if(logger.isErrorEnabled())
				logger.error(pe.getMessage(), pe);
			
			sunshineCommService.preException(pe, pe, txnInfo);
//			this.exceptionProc(context, pe);
			
		}catch (Exception e) {
			if(logger.isErrorEnabled())
				logger.error(e.getMessage(), e);
			ProcessException pe = new ProcessException();
			
			sunshineCommService.preException(pe, pe, txnInfo);
//			this.exceptionProc(context, pe);
			
		}finally{
			LogTools.printLogger(logger, "S39002PayCutPaymentQueryReq", "代扣查询", req, false);
		}
		
		this.setResponse(resp,req,txnInfo);
		return resp;
	}
	
	/**
	 * 组装响应报文
	 * @param resp
	 * @param req
	 * @param txnInfo
	 */
	private void setResponse(S39002PayCutPaymentQueryResp resp, S39002PayCutPaymentQueryReq req, TxnInfo txnInfo) {
		MsPayfrontError msPayfrontError;
		resp.setOrderid(req.getOrderid());
		resp.setErrorCode(txnInfo.getResponsCode());
		
		
		if( StringUtils.equals(txnInfo.getPayRespCode(), MsRespCode.E_9998.getCode())){//系统内部错误
			resp.setErrorMessage(txnInfo.getResponsDesc());
		}
		else if(txnInfo.getPayOthRespCode() != null){//查询交易返回描述直接使用支付前置返回码描述
			msPayfrontError = sunshineCommService.getErrorEnum(txnInfo.getPayOthRespCode(), MsPayfrontError.class);			
			resp.setErrorMessage(msPayfrontError.getDesc());
		}
		else if(txnInfo.getPayRespCode() != null) {
			msPayfrontError = sunshineCommService.getErrorEnum(txnInfo.getPayRespCode(), MsPayfrontError.class);			
			resp.setErrorMessage(msPayfrontError.getDesc());
		}
		else{
			resp.setErrorMessage(txnInfo.getResponsDesc());
		}
		
		if(StringUtils.equals(MsRespCode.E_0000.getCode(), txnInfo.getResponsCode())){
			resp.setStatus("S");//交易成功
		}else{
			resp.setStatus("F");//交易失败
		}
	}
	
	/**
	 * 业务处理
	 * @param context
	 * @param payJson
	 * @return
	 */
	@Transactional
	private String bizProc(TxnContext context,String payJson) {
		if(logger.isDebugEnabled())
			logger.debug("业务处理--bizProc");
		//调用支付
		sunshineCommService.installOrder(context);
		//组装支付指令
		payJson = paymentFacility.installPayCutPaymentQueryCommand(context.getOrigOrder());
		
		return payJson;
	}

	/**
	 * 初始化交易中间信息
	 * @param req
	 * @param txnInfo
	 */
	private void initTxnInfo(S39002PayCutPaymentQueryReq req, CcsOrder order, TxnInfo txnInfo) {
		txnInfo.setBizDate(globalManageService.getSystemStatus().getBusinessDate());
		txnInfo.setTransDirection(AuthTransDirection.Normal);
		txnInfo.setTransType(AuthTransType.Inq);
		txnInfo.setGuarantyid(order.getGuarantyId());
		txnInfo.setSysOlTime(new Date());
		txnInfo.setRequestTime(req.getRequestTime());
		txnInfo.setServiceSn(req.getServiceSn());
		txnInfo.setRefNbr(txnUtils.genRefnbr(req.getBizDate(), req.getServiceSn()));
		txnInfo.setMti("9900");
		txnInfo.setProcCode("900001");
		txnInfo.setLoanUsage(LoanUsage.F);
		txnInfo.setInputSource(order.getChannelId());
		txnInfo.setAcqId(order.getAcqId());
		txnInfo.setSubTerminalType(order.getSubTerminalType());
		txnInfo.setAuthTransStatus(AuthTransStatus.N);
		txnInfo.setDirection(TransAmtDirection.C);
		txnInfo.setDueBillNo(order.getDueBillNo());
		txnInfo.setServiceId(req.getServiceId());
		
		if(logger.isDebugEnabled())
			logger.debug("业务日期：[{}]",txnInfo.getBizDate());
		LogTools.printObj(logger, txnInfo, "交易中间信息txnInfo");
	}
	
}
