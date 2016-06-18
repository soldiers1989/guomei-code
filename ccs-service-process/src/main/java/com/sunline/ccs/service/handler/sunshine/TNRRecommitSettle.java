package com.sunline.ccs.service.handler.sunshine;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunline.acm.service.api.GlobalManagementService;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.facility.TxnUtils;
import com.sunline.ccs.facility.order.OrderFacility;
import com.sunline.ccs.facility.order.PaymentFacility;
import com.sunline.ccs.infrastructure.server.repos.RCcsOrder;
import com.sunline.ccs.infrastructure.shared.model.CcsOrder;
import com.sunline.ccs.param.def.enums.AuthTransDirection;
import com.sunline.ccs.param.def.enums.TransAmtDirection;
import com.sunline.ccs.service.context.LoanInfo;
import com.sunline.ccs.service.context.TxnContext;
import com.sunline.ccs.service.context.TxnInfo;
import com.sunline.ccs.service.entity.S30004RecommitSettleReq;
import com.sunline.ccs.service.entity.S30004RecommitSettleResp;
import com.sunline.ccs.service.handler.SunshineCommService;
import com.sunline.ppy.dictionary.enums.AuthTransStatus;
import com.sunline.ppy.dictionary.enums.AuthTransType;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanUsage;
import com.sunline.ppy.dictionary.enums.MsRespCode;
import com.sunline.ppy.dictionary.enums.OrderStatus;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * 结算重提
 * @author wangz
 *
 */
@Service
public class TNRRecommitSettle {
	private Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private SunshineCommService sunshineCommService;
	@Autowired
	private PaymentFacility paymentFacility;
	@Autowired
	private RCcsOrder rCcsOrder;
	@Autowired
	private OrderFacility orderFacility;
	@Autowired
	private GlobalManagementService globalManageService;
	@Autowired
	TxnUtils txnUtils;
	
	public S30004RecommitSettleResp handler(S30004RecommitSettleReq req) throws ProcessException {
		LogTools.printLogger(logger, "S30004RecommitSettleReq", "放款订单重提", req, true);
		S30004RecommitSettleResp resp = new S30004RecommitSettleResp();
		TxnContext context = new TxnContext();
		TxnInfo txnInfo = new TxnInfo();
		LoanInfo loanInfo = new LoanInfo();
		String payJson = "";
		
		context.setTxnInfo(txnInfo);
		context.setLoanInfo(loanInfo);
		context.setSunshineRequestInfo(req);
		
		try {
			if(null != req && null != req.getOrderId()) {
				
				this.initTxnInfo(req, txnInfo);
				
				//重复交易
				orderFacility.valiRepeatOrder(context.getTxnInfo().getServiceSn(),context.getTxnInfo().getAcqId());
				// 获取原订单信息
				context.setOrigOrder(orderFacility.findById(req.getOrderId(), true));
				//只能对原交易失败或状态为待处理的交易 进行结算重提
			    if(context.getOrigOrder().getOrderStatus()!=OrderStatus.E 
			    && context.getOrigOrder().getOrderStatus()!=OrderStatus.P ){
					throw new ProcessException(MsRespCode.E_1012.getCode(),MsRespCode.E_1012 .getMessage());
			    }
			
			    //业务处理 单独事务
			    payJson = this.bizProc(context);
				
			    //发送支付指令
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
//			this.exceptionProc(context, pe);
			
		}catch (Exception e) {
			if(logger.isErrorEnabled())
				logger.error(e.getMessage(), e);
			ProcessException pe = new ProcessException();
			sunshineCommService.preException(pe, pe, txnInfo);
//			this.exceptionProc(context, pe);
			
		}finally{
			LogTools.printLogger(logger, "S30004RecommitSettleReq", "放款订单重提", resp, false);
		}
		
		this.setResponse(resp,req,txnInfo);
		LogTools.printObj(logger,  resp, "响应报文 S30004RecommitSettleResp");
		return resp;
	}
	
	/**
	 * 组装响应报文
	 * @param resp
	 * @param req
	 * @param txnInfo
	 */
	private void setResponse(S30004RecommitSettleResp resp, S30004RecommitSettleReq req, TxnInfo txnInfo) {
		resp.setOrderid(req.getOrderId());
		resp.setErrorCode(txnInfo.getResponsCode());
		//返回描述优先使用支付的返回信息
		if(txnInfo.getPayOthRespMessage() != null ){
			resp.setErrorMessage(txnInfo.getPayOthRespMessage());			
		}
		else if(txnInfo.getPayRespCode() != null){
			resp.setErrorMessage(txnInfo.getPayRespMessage());
		}
		else{			
			resp.setErrorMessage(txnInfo.getResponsDesc());			
		}		if(StringUtils.equals(MsRespCode.E_0000.getCode(), txnInfo.getResponsCode())){
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
	private String bizProc(TxnContext context) {
		CcsOrder origOrder = context.getOrigOrder();
		TxnInfo txnInfo = context.getTxnInfo();
		if(logger.isDebugEnabled())
			logger.debug("独立事务，业务处理--bizProc");
		
		//新增订单信息
		CcsOrder order = new CcsOrder();
		Date currDate = new Date();
		//直接拷贝原订单信息
		order.updateFromMap(origOrder.convertToMap());
		//与原订单不同的信息
		order.setOrderId(null);
		order.setOriOrderId(origOrder.getOrderId());
		order.setJpaVersion(0);
		order.setOnlineFlag(Indicator.Y);
		order.setBusinessDate(txnInfo.getBizDate());
		order.setSetupDate(txnInfo.getBizDate());
		order.setOptDatetime(txnInfo.getBizDate());
		order.setSendTime(currDate);
		order.setOrderTime(currDate);
		order.setRequestTime(txnInfo.getRequestTime());
		order.setServicesn(txnInfo.getServiceSn());
		order.setRefNbr(txnUtils.genRefnbr(txnInfo.getBizDate(), txnInfo.getServiceSn()));
		order.setChannelId(txnInfo.getInputSource());
//		rCcsOrder.save(order);
		order = rCcsOrder.save(order);
		context.setOrder(order);


		//原订单状态改为已重提
		origOrder.setOrderStatus(OrderStatus.R);
		origOrder.setOptDatetime(txnInfo.getBizDate());
		//从原订单信息中获取省 市 ，收款人，开户行，付款标志
		origOrder = rCcsOrder.save(origOrder);
		context.setOrigOrder(origOrder);

		//新建订单后更新理赔结清表的订单号
		sunshineCommService.updateSettleLoanHst(context.getOrigOrder(),context.getOrder(), Indicator.N);
		//组装支付指令
		String payJson = paymentFacility.installPaySinPaymentCommand(context.getOrder());
		return payJson;
	}

	/**
	 * 初始化交易中间信息
	 * @param req
	 * @param txnInfo
	 */
	private void initTxnInfo(S30004RecommitSettleReq req, TxnInfo txnInfo) {
		txnInfo.setBizDate(globalManageService.getSystemStatus().getBusinessDate());
		txnInfo.setTransDirection(AuthTransDirection.Normal);
		txnInfo.setTransType(AuthTransType.AgentDebit);
		txnInfo.setSysOlTime(new Date());
		txnInfo.setRequestTime(req.getRequestTime());
		txnInfo.setServiceSn(req.getServiceSn());
		txnInfo.setRefNbr(txnUtils.genRefnbr(txnInfo.getBizDate(), txnInfo.getServiceSn()));
		txnInfo.setLoanUsage(LoanUsage.B);
		txnInfo.setInputSource(req.getInputSource());
		txnInfo.setAcqId(req.getAcqId());
		txnInfo.setSubTerminalType(req.getSubTerminalType());
		txnInfo.setOrg(OrganizationContextHolder.getCurrentOrg());
		txnInfo.setAuthTransStatus(AuthTransStatus.N);
		txnInfo.setDirection(TransAmtDirection.D);
		
		if(logger.isDebugEnabled())
			logger.debug("业务日期：[{}]",txnInfo.getBizDate());
		LogTools.printObj(logger, txnInfo, "交易中间信息txnInfo");
	}
}
