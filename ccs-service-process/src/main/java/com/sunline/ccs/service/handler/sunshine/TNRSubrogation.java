package com.sunline.ccs.service.handler.sunshine;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.facility.TxnUtils;
import com.sunline.ccs.facility.order.OrderFacility;
import com.sunline.ccs.facility.order.PaymentFacility;
import com.sunline.ccs.infrastructure.server.repos.RCcsOffSheetLog;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsOffSheetLog;
import com.sunline.ccs.infrastructure.shared.model.CcsSettleClaim;
import com.sunline.ccs.param.def.enums.AuthTransDirection;
import com.sunline.ccs.param.def.enums.LoanTerminateReason;
import com.sunline.ccs.param.def.enums.TransAmtDirection;
import com.sunline.ccs.service.context.TxnContext;
import com.sunline.ccs.service.context.TxnInfo;
import com.sunline.ccs.service.entity.S32001SubrogationReq;
import com.sunline.ccs.service.entity.S32001SubrogationResp;
import com.sunline.ccs.service.handler.SunshineCommService;
import com.sunline.ppy.dictionary.enums.AuthTransType;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanUsage;
import com.sunline.ppy.dictionary.enums.MsRespCode;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * 代位追偿
 * @author wangz
 *
 */
@Service
public class TNRSubrogation {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private SunshineCommService sunshineCommService;
	@Autowired
	private PaymentFacility paymentFacility;
	@Autowired
	private RCcsOffSheetLog rCcsOffSheetLog;
	@Autowired
	private OrderFacility orderFacility;
	@Autowired
	TxnUtils txnUtils;
	public S32001SubrogationResp handler(S32001SubrogationReq req) throws ProcessException {
		LogTools.printLogger(logger, "S32001SubrogationReq", "代位追偿", req, true);
		LogTools.printObj(logger, req, "请求参数S32001SubrogationReq");
		S32001SubrogationResp resp = new S32001SubrogationResp();		
		String payJson ="";
		
		TxnContext context = new TxnContext();
		TxnInfo txnInfo = new TxnInfo();
		context.setTxnInfo(txnInfo);
		context.setSunshineRequestInfo(req);
		try{
			this.initTxnInfo(context,req);
			//重复交易
			sunshineCommService.checkReqAndRepeatTxn(context);
	
			//查询贷款信息
			sunshineCommService.loadLoan(context, true);
			//加载核心账户信息
			sunshineCommService.loadAcct(context, true);
			//加载客户信息
			sunshineCommService.loadCustomer(context, true);
			sunshineCommService.loadClaim(context, true);
			//校验数据
			this.check(context);	
			
			//业务处理，单独业务处理，保证业务数据完整
			payJson = this.bizProc(context);

//			//发送支付指令
//			retJson = bankServiceForApsImpl.sendMsPayFront(payJson, txnInfo.getLoanUsage());
			//处理结果
			sunshineCommService.payResultProc(context,payJson);
			
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
			this.saveOffSheetLog(txnInfo);
			LogTools.printLogger(logger, "S32001SubrogationReq", "代位追偿", resp, false);
		}
		
		this.setResponse(resp,req,txnInfo);
		LogTools.printObj(logger, resp, "响应参数S32001SubrogationResp");
		return resp;
	}

	/**
	 * 组装响应报文
	 * @param resp
	 * @param req
	 * @param txnInfo
	 */
	private void setResponse(S32001SubrogationResp resp, S32001SubrogationReq req, TxnInfo txnInfo) {
		resp.setMoney(req.getBusinesssum());
		resp.setGuarantyid(req.getGuarantyid());
		resp.setErrorCode(txnInfo.getResponsCode());
		resp.setErrorMessage(txnInfo.getResponsDesc());
		if(StringUtils.equals(MsRespCode.E_0000.getCode(), txnInfo.getResponsCode())){
			resp.setStatus("S");//交易成功
		}else{
			resp.setStatus("F");//交易失败
		}
	}
	

	/**
	 * 记录代位追偿流水
	 * @param txnInfo
	 */
	@Transactional
	private void saveOffSheetLog(TxnInfo txnInfo) {
		CcsOffSheetLog offSheetLog = new CcsOffSheetLog();
		
		offSheetLog.setOrg(OrganizationContextHolder.getCurrentOrg());//当前机构
		offSheetLog.setServicesn(txnInfo.getServiceSn());//外部流水
		offSheetLog.setRequstTime(txnInfo.getRequestTime());//外部时间
		offSheetLog.setBusinessDate(txnInfo.getBizDate());//业务日期
		offSheetLog.setChannelId(txnInfo.getInputSource());//渠道编号
		offSheetLog.setGuarantyId(txnInfo.getGuarantyid());//保单号
		offSheetLog.setTransAmt(txnInfo.getTransAmt());//代偿金额
		offSheetLog.setCreateTime(txnInfo.getSysOlTime());//当前时间
		offSheetLog.setOptDatetime(txnInfo.getSysOlTime());//更新时间
		offSheetLog.setOpId(OrganizationContextHolder.getUsername());
		
		rCcsOffSheetLog.save(offSheetLog);
	}


	/**
	 * 单独事务处理业务逻辑，保证订单等基础信息能保存完整
	 * @param context
	 * @param payJson
	 * @return
	 */
	@Transactional
	private String bizProc(TxnContext context) {
		if(logger.isDebugEnabled())
			logger.debug("独立事务，业务处理--bizProc");
		String payJson = "";
		
		//调用支付
		sunshineCommService.installOrder(context);
		
		//组装支付指令
		payJson = paymentFacility.installPayCutPaymentCommand(context.getOrder());
		
		return payJson;
	}

	
	/**
	 * 初始化中间交易信息
	 * @param context
	 */
	private void initTxnInfo(TxnContext context,S32001SubrogationReq req){
		TxnInfo txnInfo = context.getTxnInfo();
		txnInfo.setGuarantyid(req.getGuarantyid());
		txnInfo.setBizDate(req.getBizDate());
		txnInfo.setLoanUsage(LoanUsage.S);
		txnInfo.setTransAmt(req.getBusinesssum());
		
		txnInfo.setTransDirection(AuthTransDirection.Normal);
		txnInfo.setTransType(AuthTransType.AgentCredit);
		txnInfo.setSysOlTime(new Date());
		txnInfo.setRequestTime(req.getRequestTime());
		txnInfo.setServiceSn(req.getServiceSn());
		txnInfo.setInputSource(req.getInputSource());
		txnInfo.setAcqId(req.getAcqId());
		txnInfo.setSubTerminalType(req.getSubTerminalType());
		txnInfo.setDirection(TransAmtDirection.C);
		txnInfo.setRefNbr(txnUtils.genRefnbr(txnInfo.getBizDate(), txnInfo.getServiceSn()));
		txnInfo.setServiceId(req.getServiceId());

		if(logger.isDebugEnabled())
			logger.debug("业务日期：[{}]",txnInfo.getBizDate());
		LogTools.printObj(logger, txnInfo, "交易中间信息txnInfo");
	}
	
	/**
	 * 校验数据
	 * @param context
	 */
	private void check(TxnContext context){
		//检查代位追偿的金额
		TxnInfo txnInfo = context.getTxnInfo();
		CcsLoan loan = context.getLoan();
		CcsSettleClaim claim = context.getClaim();
		
		if(txnInfo.getTransAmt().compareTo(BigDecimal.ZERO) <= 0){
			throw new ProcessException(MsRespCode.E_1005.getCode(),MsRespCode.E_1005.getMessage());
		}
		
		LogTools.printObj(logger,  loan.getTerminalReasonCd(), "分期终止原因代码LoanTerminateReason ");
		
		//非理赔终止的借据不能做代偿
		if(loan.getTerminalReasonCd()!=LoanTerminateReason.C){
			throw new ProcessException(MsRespCode.E_1016.getCode(),MsRespCode.E_1016.getMessage());
		}
		
		if(null == claim.getSettleFlag() || claim.getSettleFlag()!=Indicator.Y){
			throw new ProcessException(MsRespCode.E_1016.getCode(),MsRespCode.E_1016.getMessage()+",理赔处理中或理赔失败");
		}
		
	}
}
