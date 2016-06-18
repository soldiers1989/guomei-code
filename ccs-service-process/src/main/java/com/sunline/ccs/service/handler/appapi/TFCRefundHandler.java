package com.sunline.ccs.service.handler.appapi;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.service.context.TxnContext;
import com.sunline.ccs.service.context.TxnInfo;
import com.sunline.ccs.service.handler.AppapiCommService;
import com.sunline.ccs.service.handler.RefundCommService;
import com.sunline.ccs.service.handler.WithdrawVoidCommService;
import com.sunline.ccs.service.msentity.MsRequestInfo;
import com.sunline.ccs.service.msentity.MsResponseInfo;
import com.sunline.ccs.service.msentity.TFCRefundReq;
import com.sunline.ccs.service.msentity.TFCRefundResp;
import com.sunline.ccs.service.msentity.TFDWithDrawVoidReq;
import com.sunline.ccs.service.noticetemplate.SMSLoanDeductions;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.MsRespCode;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * 商品贷退货接口
 * 
 * @author wangz
 * 
 */
@Service
public class TFCRefundHandler extends AbstractHandler {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private AppapiCommService appapiCommService;
	@Autowired
	private RefundCommService refundCommService;
	@Autowired
	private WithdrawVoidCommService voidAndRefundCommService;
	@Autowired
	private SMSLoanDeductions sMSLoanDeductions;
	@Override
	public MsResponseInfo execute(MsRequestInfo msRequestInfo) {

		LogTools.printLogger(logger, "TFCRefundReq", "商品贷退货", msRequestInfo,
				true);
		TFCRefundReq req = (TFCRefundReq) msRequestInfo;
		LogTools.printObj(logger, req, "请求参数TFCRefundReq");
		TFCRefundResp resp = new TFCRefundResp();

		TxnContext context = new TxnContext();
		TxnInfo txnInfo = new TxnInfo();
		context.setTxnInfo(txnInfo);
		context.setMsRequestInfo(req);

		try {
			//初始化查找原交易需要的txnInfo信息
			this.initTxnInfo(context, req);

			// 重复交易
			appapiCommService.checkRepeatTxn(context);
			appapiCommService.findOrigOrder(context);
			
			//当天当做撤销处理
			if(context.getOrigOrder()!=null 
			&& req.getBizDate().compareTo(context.getOrigOrder().getBusinessDate())==0){
				TFDWithDrawVoidReq withDrawReq = new TFDWithDrawVoidReq();
				withDrawReq.setBizDate(req.getBizDate());
				withDrawReq.setRequestTime(req.getRequestTime());
				withDrawReq.setServiceSn(req.getServiceSn());
				withDrawReq.setOrg(req.getOrg());
				withDrawReq.setOpId(req.getOpId());
				withDrawReq.setInputSource(req.getInputSource());
				withDrawReq.setSubTerminalType(req.getSubTerminalType());
				withDrawReq.setAcqId(req.getAcqId());
				withDrawReq.setServiceId(req.getServiceId());
				withDrawReq.setContractNo(req.getContractNo());
				withDrawReq.setAmount(req.getAmount());
				withDrawReq.setOrigServiceId(req.getOrigServiceId());
				withDrawReq.setOrigServiceSn(req.getOrigServiceSn());
				withDrawReq.setOrigAcqId(req.getOrigAcqId());				
				context.setMsRequestInfo(withDrawReq);

				voidAndRefundCommService.WithDrawVoidProcess(context,withDrawReq,Indicator.N);
			}
			else{//隔天退货处理
				//判断是否还需要找原交易
				Indicator refundFindOrigOrderInd = Indicator.Y;
				if(context.getOrigOrderHst() ==null && context.getOrigOrder() !=null){
					refundFindOrigOrderInd = Indicator.N;
				}
				refundCommService.RefundProcess(context, req, refundFindOrigOrderInd);
			}
			//向通知平台发送短信通知
			sMSLoanDeductions.sendSMS(context);							
		} catch (ProcessException pe) {
			if (logger.isErrorEnabled())
				logger.error(pe.getMessage(), pe);

			appapiCommService.preException(pe, pe, txnInfo);
			
			appapiCommService.saveErrorOrder(context);
			
			sMSLoanDeductions.sendSMS(context);

		} catch (Exception e) {
			if (logger.isErrorEnabled())
				logger.error(e.getMessage(), e);

			ProcessException pe = new ProcessException();
			appapiCommService.preException(pe, pe, txnInfo);
			
			appapiCommService.saveErrorOrder(context);

		} finally {
			LogTools.printLogger(logger, "TFCRefundResp", "商品贷退货", resp, false);
		}

		this.setResponse(resp, req, txnInfo);
		LogTools.printObj(logger, resp, "响应参数TFCRefundResp");
		return resp;

	}
	
	/**
	 * 初始化txnInfo
	 * 
	 * @param context
	 * @param req
	 */
	private void initTxnInfo(TxnContext context, TFCRefundReq req) {
		TxnInfo txnInfo = context.getTxnInfo();
				
		txnInfo.setServiceSn(req.getServiceSn());
		txnInfo.setAcqId(req.getAcqId());
		txnInfo.setOrg(req.getOrg());
		txnInfo.setServiceId(req.getServiceId());
		
		txnInfo.setContrNo(req.contractNo);
		txnInfo.setTransAmt(req.amount);
		txnInfo.setOrigAcqId(req.origAcqId);
		txnInfo.setOrigServiceSn(req.origServiceSn);
		txnInfo.setOrigServiceId(req.origServiceId);

	}

	/**
	 * 响应
	 * @param resp
	 * @param req
	 * @param txnInfo
	 */
	private void setResponse(TFCRefundResp resp, TFCRefundReq req,
			TxnInfo txnInfo) {
		resp.setContractNo(txnInfo.getContrNo());
		resp.setErrorCode(txnInfo.getResponsCode());
		resp.setErrorMessage(txnInfo.getResponsDesc());
		if (StringUtils.equals(MsRespCode.E_0000.getCode(),
				txnInfo.getResponsCode())) {
			resp.setStatus("S");// 交易成功
		} else {
			resp.setStatus("F");// 交易失败
		}
	}
}
