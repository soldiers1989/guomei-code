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
import com.sunline.ccs.service.context.LoanInfo;
import com.sunline.ccs.service.context.TxnContext;
import com.sunline.ccs.service.context.TxnInfo;
import com.sunline.ccs.service.entity.S39003SettleQueryReq;
import com.sunline.ccs.service.entity.S39003SettleQueryResp;
import com.sunline.ccs.service.handler.SunshineCommService;
import com.sunline.ppy.dictionary.enums.LoanUsage;
import com.sunline.ppy.dictionary.enums.MsPayfrontError;
import com.sunline.ppy.dictionary.enums.MsRespCode;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * 代付确认交易
 * @author zqx
 *
 */
@Service
public class TNRSettleQuery {
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
	private TxnUtils txnUtils;


	public S39003SettleQueryResp handler(S39003SettleQueryReq req) throws ProcessException {
		LogTools.printLogger(logger, "S39003SettleQueryReq", "结算查询", req, true);
		S39003SettleQueryResp resp = new S39003SettleQueryResp();
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

				initTxnInfo(req,context.getOrigOrder(),txnInfo);
			    
			    //业务处理 单独事务
				payJson = this.bizProc(context,payJson);
				
//				//发送支付指令
//				retJson = bankServiceForApsImpl.sendMsPayFront(payJson, txnInfo.getLoanUsage());
				
				//处理结果，单独事务处理
				sunshineCommService.payResultProc(context,payJson);

			}else {
				throw new ProcessException(MsRespCode.E_1034.getCode(),MsRespCode.E_1034.getMessage());
			}

		}catch (ProcessException pe){
			if(logger.isErrorEnabled())
				logger.error(pe.getMessage(), pe);
			sunshineCommService.preException(pe, pe, txnInfo);			
		}catch (Exception e) {
			
			if(logger.isErrorEnabled())
				logger.error(e.getMessage(), e);
			ProcessException pe = new ProcessException();
			sunshineCommService.preException(pe, pe, txnInfo);
		}finally{
			LogTools.printLogger(logger, "S39003SettleQueryReq", "结算查询", req, false);
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
	private void setResponse(S39003SettleQueryResp resp, S39003SettleQueryReq req, TxnInfo txnInfo) {
		MsPayfrontError msPayfrontError;
		resp.setOrderid(req.getOrderid());
		resp.setErrorCode(txnInfo.getResponsCode());
		
		//查询交易返回描述直接使用支付前置返回码描述
		if(txnInfo.getPayOthRespCode() != null){
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
		payJson = paymentFacility.installPaySinPaymentQueryCommand(context.getOrigOrder());
		return payJson;
	}
	
	/**
	 * 初始化交易中间信息
	 * @param req
	 * @param txnInfo
	 */
	private void initTxnInfo(S39003SettleQueryReq req, CcsOrder order, TxnInfo txnInfo) {
		txnInfo.setBizDate(globalManageService.getSystemStatus().getBusinessDate());
//		txnInfo.setTransDirection(AuthTransDirection.Normal);
//		txnInfo.setTransType(AuthTransType.AgentDebit);
		txnInfo.setSysOlTime(new Date());
		txnInfo.setRequestTime(req.getRequestTime());
		txnInfo.setServiceSn(req.getServiceSn());
		txnInfo.setRefNbr(txnUtils.genRefnbr(req.getBizDate(), req.getServiceSn()));
		txnInfo.setLoanUsage(LoanUsage.G);
		txnInfo.setInputSource(req.getInputSource());
		txnInfo.setAcqId(req.getAcqId());
		txnInfo.setSubTerminalType(req.getSubTerminalType());
//		txnInfo.setAuthTransStatus(AuthTransStatus.N);
//		txnInfo.setDirection(TransAmtDirection.D);
		txnInfo.setServiceId(req.getServiceId());

		if(logger.isDebugEnabled())
			logger.debug("业务日期：[{}]",txnInfo.getBizDate());
		LogTools.printObj(logger, txnInfo, "交易中间信息txnInfo");
	}
	

}
