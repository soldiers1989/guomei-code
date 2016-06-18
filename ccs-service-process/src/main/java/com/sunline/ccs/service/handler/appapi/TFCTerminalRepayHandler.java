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
import com.sunline.ccs.infrastructure.shared.model.CcsOrder;
import com.sunline.ccs.loan.McLoanProvideImpl;
import com.sunline.ccs.param.def.enums.AuthTransDirection;
import com.sunline.ccs.param.def.enums.TransAmtDirection;
import com.sunline.ccs.service.context.LoanInfo;
import com.sunline.ccs.service.context.TxnContext;
import com.sunline.ccs.service.context.TxnInfo;
import com.sunline.ccs.service.handler.AppapiCommService;
import com.sunline.ccs.service.handler.RepayCommService;
import com.sunline.ccs.service.handler.collection.CollectionLogic;
import com.sunline.ccs.service.msentity.MsRequestInfo;
import com.sunline.ccs.service.msentity.MsResponseInfo;
import com.sunline.ccs.service.msentity.TFCTerminalRepayReq;
import com.sunline.ccs.service.msentity.TFCTerminalRepayResp;
import com.sunline.ccs.service.noticetemplate.SMSLoanDeductions;
import com.sunline.ppy.dictionary.enums.AuthTransType;
import com.sunline.ppy.dictionary.enums.LoanUsage;
import com.sunline.ppy.dictionary.enums.MsPayfrontError;
import com.sunline.ppy.dictionary.enums.MsRespCode;
import com.sunline.ppy.dictionary.enums.OrderStatus;
import com.sunline.ppy.dictionary.exception.ProcessException;
/**
 * 不调用支付的随借随还主动还款
 *
 */
@Service
public class TFCTerminalRepayHandler extends AbstractHandler {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private AppapiCommService appapiCommService;
	@Autowired
	McLoanProvideImpl mcLoanProvideImpl;
	@Autowired
	private SMSLoanDeductions smsLoanDeductions; 
	@Autowired
	private OrderFacility orderFacility;
	@Autowired
	private CollectionLogic collectionLogic;
	@Autowired
	private TxnUtils txnUtils;
	@Autowired
	private RepayCommService transactionHandle;
	
	
	public MsResponseInfo execute(MsRequestInfo msRequestInfo) throws ProcessException {
		TFCTerminalRepayReq req = (TFCTerminalRepayReq) msRequestInfo;
		LogTools.printLogger(logger, "TFCTerminalRepay", "不调用支付的主动还款", req, true);
		LogTools.printObj(logger, req, "请求参数TFCTerminalRepayReq");
		TFCTerminalRepayResp resp = new TFCTerminalRepayResp();

		TxnContext context = new TxnContext();
		TxnInfo txnInfo = new TxnInfo();
		LoanInfo loanInfo = new LoanInfo();
		context.setTxnInfo(txnInfo);
		context.setLoanInfo(loanInfo);
		context.setMsRequestInfo(req);

		try {
			this.initTxnInfo(context, req);
			
			this.checkReq(context);//TODO 是否检查报文体字段
			// 重复交易
//			appapiCommService.checkRepeatTxn(context);// TODO 重复交易应该要检查
			
			appapiCommService.loadAcctByContrNo(context, true);
			appapiCommService.loadAcctOByAcct(context, true);
			appapiCommService.loadCardOByAcct(context, true);
			appapiCommService.loadCustomerByCustId(context, true);
			
			//处理结果，单独事务处理
			this.payResultProc(context,req);
			
			//向通知平台发送通知
			smsLoanDeductions.sendSMS(context);	
			
			//向催收平台发送通知
			collectionLogic.sendCSPlatform(context);

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
			LogTools.printLogger(logger, "TFCTerminalRepay", " 不调用支付的随借随还主动还款", resp, false);
		}

		this.setResponse(resp, req, txnInfo);
		return resp;
	}

	/**
	 * @param context
	 */
	private void checkReq(TxnContext context) {
		TFCTerminalRepayReq req = (TFCTerminalRepayReq) context.getMsRequestInfo();
		
		if(req.amount.compareTo(BigDecimal.ZERO)<=0){
			throw new ProcessException(MsRespCode.E_1043.getCode(),MsRespCode.E_1043 .getMessage()+ ",字段名称{AMOUNT},必须大于0");
		}
	}


	/**
	 * 组装响应报文
	 * 
	 * @param resp
	 * @param req
	 * @param txnInfo
	 */
	private void setResponse(TFCTerminalRepayResp resp, TFCTerminalRepayReq req, TxnInfo txnInfo) {
		resp.setContractNo(txnInfo.getContrNo());
		resp.setErrorCode(txnInfo.getResponsCode());
		resp.setErrorMessage(txnInfo.getResponsDesc());
		if (StringUtils.equals(MsRespCode.E_0000.getCode(),
				txnInfo.getResponsCode())) {
			resp.setStatus("S");// 交易成功
			resp.setAmount(req.amount.setScale(2, RoundingMode.HALF_UP));
			resp.setContractNo(req.contractNo);
		} else {
			resp.setStatus("F");// 交易失败
		}
	}


	/**
	 * 初始化中间交易信息
	 * 
	 * @param context
	 */
	public void initTxnInfo(TxnContext context, TFCTerminalRepayReq req) {
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
		txnInfo.setContrNo(req.contractNo);//TODO 修改请求字段
		txnInfo.setTransAmt(req.amount);//TODO 修改请求字段
		txnInfo.setMobile(req.mobile);//TODO 修改请求字段
		txnInfo.setLoanUsage(LoanUsage.N);
		txnInfo.setServiceId(req.getServiceId());
//		txnInfo.setRefNbr(req.getServiceSn());
		txnInfo.setRefNbr(txnUtils.genRefnbr(txnInfo.getBizDate(), txnInfo.getServiceSn()));
		txnInfo.setOrg(req.getOrg());

		if (logger.isDebugEnabled())
			logger.debug("业务日期：[{}]", txnInfo.getBizDate());
		LogTools.printObj(logger, txnInfo, "交易中间信息txnInfo");
	}
	
	/**
	 * 处理支付结果
	 * 
	 * @param context
	 * @param payJson
	 * @param resp
	 */
	@Transactional
	public void payResultProc(TxnContext context,TFCTerminalRepayReq req) {
		
		CcsOrder order = orderFacility.findOrderBySersn(req.getServiceSn(),req.getAcqId());
		
		if (order != null) {
			OrderStatus status = order.getOrderStatus();
			context.setOrder(order);
			//订单状态为成功
			if (status.equals(OrderStatus.S)) {
				
				context.getTxnInfo().setResponsCode(MsPayfrontError.S_0.getRespCode());
				context.getTxnInfo().setResponsDesc(MsPayfrontError.S_0.getDesc());	

			}else {
				transactionHandle.bisProcess(context);
			}
		}else {
			appapiCommService.installOrder(context);
			transactionHandle.bisProcess(context);
		}
	}
}
