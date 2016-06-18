package com.sunline.ccs.service.handler.appapi;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.facility.TxnUtils;
import com.sunline.ccs.facility.order.OrderFacility;
import com.sunline.ccs.infrastructure.server.repos.RCcsOrder;
import com.sunline.ccs.infrastructure.shared.model.CcsOrder;
import com.sunline.ccs.param.def.enums.AuthTransDirection;
import com.sunline.ccs.param.def.enums.TransAmtDirection;
import com.sunline.ccs.service.context.TxnContext;
import com.sunline.ccs.service.context.TxnInfo;
import com.sunline.ccs.service.handler.AppapiCommService;
import com.sunline.ccs.service.msentity.MsRequestInfo;
import com.sunline.ccs.service.msentity.MsResponseInfo;
import com.sunline.ccs.service.msentity.TFCCouponRepayReq;
import com.sunline.ccs.service.msentity.TFCCouponRepayResp;
import com.sunline.ccs.service.payEntity.CommResp;
import com.sunline.ppy.dictionary.enums.AuthTransType;
import com.sunline.ppy.dictionary.enums.LoanUsage;
import com.sunline.ppy.dictionary.enums.MsPayfrontError;
import com.sunline.ppy.dictionary.enums.MsRespCode;
import com.sunline.ppy.dictionary.enums.OrderStatus;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * 优惠卷还款接口
 * @author zhengjf
 *
 */
@Service
public class TFCCouponRepay extends AbstractHandler {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private AppapiCommService appapiCommService;
	@Autowired
	private TxnUtils txnUtils;
	@Autowired
	private OrderFacility orderFacility;
	@Autowired
	private RCcsOrder rCcsOrder;
	

	@Override
	public MsResponseInfo execute(MsRequestInfo msRequestInfo) {

		TFCCouponRepayReq req = (TFCCouponRepayReq) msRequestInfo;
		LogTools.printLogger(logger, "TFCCouponRepayReq", "优惠卷还款接口", req, true);
		LogTools.printObj(logger, req, "请求参数TFCCouponRepayReq");
		TFCCouponRepayResp resp = new TFCCouponRepayResp();
		
		TxnContext context = new TxnContext();
		TxnInfo txnInfo = new TxnInfo();
		context.setTxnInfo(txnInfo);
		
		try {
			this.initTxnInfo(context, req);
			
			//是否检查报文体字段
			this.checkReq(context);
		
			// 重复交易
			appapiCommService.checkRepeatTxn(context);
			
			appapiCommService.loadAcctByContrNo(context, true);
			appapiCommService.loadAcctOByAcct(context, true);
			appapiCommService.loadCardOByAcct(context, true);
			appapiCommService.loadCustomerByCustId(context, true);
			
			//处理结果，单独事务处理
			this.payResultProc(context,req);
			
			//向通知平台发送通知
//			smsLoanDeductions.sendSMS(context);	
			
			//向催收平台发送通知
//			collectionLogic.sendCSPlatform(context);

		} catch (ProcessException pe) {
			if (logger.isErrorEnabled())
				logger.error(pe.getMessage(), pe);

			appapiCommService.preException(pe, pe, txnInfo);
			appapiCommService.saveErrorOrder(context);

		} catch (Exception e) {
			if (logger.isErrorEnabled())
				logger.error(e.getMessage(), e);

			ProcessException pe = new ProcessException();
			appapiCommService.preException(pe, pe, txnInfo);
			appapiCommService.saveErrorOrder(context);

		} finally {
			LogTools.printLogger(logger, "TFCCouponRepayResp", "优惠卷还款接口", resp, false);
		}

		this.setResponse(resp, req, txnInfo);
		
		return resp;
	}

	/**
	 * 处理支付结果
	 * 
	 * @param context
	 * @param payJson
	 * @param resp
	 */
	@Transactional
	private void payResultProc(TxnContext context, TFCCouponRepayReq req) {
		//新建订单
		appapiCommService.installOrder(context);
		
		CcsOrder order = context.getOrder();
		TxnInfo txnInfo = context.getTxnInfo();
		CommResp mainResp = new CommResp();
		
		mainResp.setCode(null);
		mainResp.setMessage(null);
		txnInfo.setPayRespCode(mainResp.getCode());
		txnInfo.setPayRespMessage(mainResp.getMessage());
		txnInfo.setResponsCode(MsPayfrontError.S_0.getRespCode());
		txnInfo.setResponsDesc(MsPayfrontError.S_0.getDesc());
		
		context.getOrder().setTxnType(txnInfo.getTransType());
		orderFacility.updateOrder(context.getOrder(), mainResp, "SUCCESS",OrderStatus.S,txnInfo.getLogKv(),txnInfo.getBizDate(),MsRespCode.E_0000);
		order.setTerm(txnInfo.getCdTerms());
		order.setOffsetType(req.offsetType);
		order.setCouponId(req.offsetNO);
		
		rCcsOrder.save(order);
	}

	/**
	 * 字段校验
	 * @param context
	 */
	private void checkReq(TxnContext context) {
		TxnInfo txnInfo = context.getTxnInfo();
		if(txnInfo.getTransAmt().compareTo(BigDecimal.ZERO)<=0){
			throw new ProcessException(MsRespCode.E_1043.getCode(),MsRespCode.E_1043 .getMessage()+ ",字段名称{AMOUNT},必须大于0");
		}
	}


	/**
	 * 初始化中间交易信息
	 * 
	 * @param context
	 */
	private void initTxnInfo(TxnContext context, TFCCouponRepayReq req) {
		TxnInfo txnInfo = context.getTxnInfo();
		txnInfo.setBizDate(req.getBizDate());
		txnInfo.setTransDirection(AuthTransDirection.Normal);
		txnInfo.setTransType(AuthTransType.AgentCredit);
		txnInfo.setSysOlTime(new Date());
		txnInfo.setRequestTime(req.getRequestTime());
		txnInfo.setServiceSn(req.getServiceSn());
		txnInfo.setMti("9900");
		txnInfo.setProcCode("900001");
		txnInfo.setInputSource(req.getInputSource());
		txnInfo.setAcqId(req.getAcqId());
		txnInfo.setSubTerminalType(req.getSubTerminalType());
		txnInfo.setDirection(TransAmtDirection.C);
		txnInfo.setContrNo(req.contrNbr);//TODO 修改请求字段
		txnInfo.setTransAmt(req.amount);//TODO 修改请求字段
		txnInfo.setCdTerms(req.offsetTerm);
		txnInfo.setLoanUsage(LoanUsage.Q);
		txnInfo.setServiceId(req.getServiceId());
//		txnInfo.setRefNbr(req.getServiceSn());
		txnInfo.setRefNbr(txnUtils.genRefnbr(txnInfo.getBizDate(), txnInfo.getServiceSn()));
		txnInfo.setOrg(req.getOrg());

		if (logger.isDebugEnabled())
			logger.debug("业务日期：[{}]", txnInfo.getBizDate());
		LogTools.printObj(logger, txnInfo, "交易中间信息txnInfo");
		
	}
		
	/**
	 * 组装响应报文
	 * 
	 * @param resp
	 * @param req
	 * @param txnInfo
	 */
	private void setResponse(TFCCouponRepayResp resp, TFCCouponRepayReq req, TxnInfo txnInfo) {
		resp.setContractNo(txnInfo.getContrNo());
		resp.setErrorCode(txnInfo.getResponsCode());
		resp.setErrorMessage(txnInfo.getResponsDesc());
		if (StringUtils.equals(MsRespCode.E_0000.getCode(),
				txnInfo.getResponsCode())) {
			resp.setStatus("S");// 交易成功
			resp.setAmount(req.amount.setScale(2, RoundingMode.HALF_UP));
			resp.setContractNo(req.contrNbr);
		} else {
			resp.setStatus("F");// 交易失败
		}
	}
}