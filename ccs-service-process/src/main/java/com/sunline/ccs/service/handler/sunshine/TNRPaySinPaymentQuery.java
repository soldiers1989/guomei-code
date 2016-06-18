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
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.facility.order.OrderFacility;
import com.sunline.ccs.facility.order.PaymentFacility;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsOrder;
import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.enums.AuthTransDirection;
import com.sunline.ccs.param.def.enums.TransAmtDirection;
import com.sunline.ccs.service.context.LoanInfo;
import com.sunline.ccs.service.context.TxnContext;
import com.sunline.ccs.service.context.TxnInfo;
import com.sunline.ccs.service.entity.S39001PaySinPaymentQueryReq;
import com.sunline.ccs.service.entity.S39001PaySinPaymentQueryResp;
import com.sunline.ccs.service.handler.SunshineCommService;
import com.sunline.ccs.service.handler.collection.CollectionLogic;
import com.sunline.ccs.service.noticetemplate.SMSLoanDeductions;
import com.sunline.pcm.param.def.Product;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.AuthTransStatus;
import com.sunline.ppy.dictionary.enums.AuthTransType;
import com.sunline.ppy.dictionary.enums.InputSource;
import com.sunline.ppy.dictionary.enums.LoanRegStatus;
import com.sunline.ppy.dictionary.enums.LoanType;
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
public class TNRPaySinPaymentQuery {
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
	private UnifiedParameterFacility unifiedParameterFacility;
	@Autowired
	private UnifiedParamFacilityProvide unifiedParamFacilityProvide;
	@Autowired
	CollectionLogic collectionLogic;
	@Autowired
	TxnUtils txnUtils;
	
	public S39001PaySinPaymentQueryResp handler(S39001PaySinPaymentQueryReq req) throws ProcessException {
		LogTools.printLogger(logger, "S39001LoanQueryReq", "代付查询", req, true);
		S39001PaySinPaymentQueryResp resp = new S39001PaySinPaymentQueryResp();
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
				
				sunshineCommService.loadAcct(context,context.getOrigOrder().getAcctNbr(),context.getOrigOrder().getAcctType(), true);
				sunshineCommService.loadAcctO(context, context.getOrigOrder().getAcctNbr(),context.getOrigOrder().getAcctType(),true);
				sunshineCommService.loadAuthmemo(context, context.getOrigOrder().getLogKv(), true);
				sunshineCommService.loadCard(context, true);
				// 获取客户信息
				sunshineCommService.loadCustomer(context, true);
				
				//如果非溢缴款转出 代付确认，则走  handler(context);
				if (context.getOrigOrder().getLoanUsage() != LoanUsage.D) {
					this.handler(context);
				}else {
					//获取贷款金额
					context.getTxnInfo().setTransAmt(context.getOrigOrder().getTxnAmt());
					context.getTxnInfo().setProcCode("490001");
				}
				
				// 获取原订单信息
				txnInfo.setLogKv(context.getOrigOrder().getLogKv()); //用于更新新订单流水的logkv
				
			    //业务处理 单独事务
				payJson = this.bizProc(context,payJson);
				
//				//发送支付指令
//				retJson = bankServiceForApsImpl.sendMsPayFront(payJson, txnInfo.getLoanUsage());
				
				//处理结果，单独事务处理
				sunshineCommService.payResultProc(context,payJson);
				
				//向通知平台发送通知		
				if(txnInfo.getInputSource() != InputSource.SUNS){
					smsLoanDeductions.sendSMS(context);	
				}else {
					if(logger.isDebugEnabled())
						logger.debug("channel_id:[{}],不支持发送短信",txnInfo.getInputSource());
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
			LogTools.printLogger(logger, "S39001LoanQueryReq", "代付查询", req, false);
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
	private void setResponse(S39001PaySinPaymentQueryResp resp, S39001PaySinPaymentQueryReq req, TxnInfo txnInfo) {
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
		payJson = paymentFacility.installPaySinPaymentQueryCommand(context.getOrigOrder());
		
		//支持短信功能
		TxnInfo txnInfo=context.getTxnInfo();
		txnInfo.setProductCd(context.getAccount().getProductCd());
		context.setTxnInfo(txnInfo);
		
		return payJson;
	}
	
	/**
	 * 初始化交易中间信息
	 * @param req
	 * @param txnInfo
	 */
	private void initTxnInfo(S39001PaySinPaymentQueryReq req, CcsOrder order, TxnInfo txnInfo) {
		txnInfo.setBizDate(globalManageService.getSystemStatus().getBusinessDate());
		txnInfo.setTransDirection(AuthTransDirection.Normal);
		txnInfo.setTransType(AuthTransType.Inq);
		txnInfo.setGuarantyid(order.getGuarantyId());
		txnInfo.setDueBillNo(order.getDueBillNo());
		txnInfo.setSysOlTime(new Date());
		txnInfo.setRequestTime(req.getRequestTime());
		txnInfo.setServiceSn(req.getServiceSn());
		txnInfo.setRefNbr(txnUtils.genRefnbr(req.getBizDate(), req.getServiceSn()));
		txnInfo.setMti("0208");
		txnInfo.setProcCode("480001");
		txnInfo.setLoanUsage(LoanUsage.E);
		txnInfo.setInputSource(order.getChannelId());
		txnInfo.setAcqId(order.getAcqId());
		txnInfo.setSubTerminalType(order.getSubTerminalType());
		txnInfo.setAuthTransStatus(AuthTransStatus.N);
		txnInfo.setDirection(TransAmtDirection.D);
		txnInfo.setMobile(order.getMobileNumber());
		txnInfo.setServiceId(req.getServiceId());
		txnInfo.setLoanAmt(order.getLoanAmt());
		txnInfo.setPremiumAmt(order.getPremiumAmt());
		txnInfo.setPremiumInd(order.getPremiumInd());

		if(logger.isDebugEnabled())
			logger.debug("业务日期：[{}]",txnInfo.getBizDate());
		LogTools.printObj(logger, txnInfo, "交易中间信息txnInfo");
	}
	/**
	 * 获取产品参数
	 * @param context
	 */
	private void getProdParam(TxnContext context){
		if(logger.isDebugEnabled())
			logger.debug("获取产品参数--getProdParam");
		String productCd = context.getAccount().getProductCd();
		CcsLoanReg loanReg = context.getLoanReg();
		try{
			ProductCredit productCredit = unifiedParameterFacility.loadParameter(productCd, ProductCredit.class);
		    Product product = unifiedParameterFacility.loadParameter(productCd, Product.class);
		    AccountAttribute acctAttr = unifiedParameterFacility.loadParameter(
		    		productCredit.accountAttributeId, AccountAttribute.class);
		    LoanPlan loanPlan = null;
		    LoanFeeDef loanFeeDef= null;
		    //阳光产品
		    if(InputSource.SUNS.equals(context.getSunshineRequestInfo().getInputSource())){
		    	loanPlan = unifiedParamFacilityProvide.loanPlan(productCd, LoanType.MCEI);
			    loanFeeDef = unifiedParamFacilityProvide.loanFeeDef(productCd, LoanType.MCEI, Integer.valueOf(loanReg.getLoanInitTerm()));
		    } else{
		    	//20151127
		    	loanPlan = unifiedParamFacilityProvide.loanPlan(productCredit.loanPlansMap.get(productCredit.defaultLoanType));
		    	if(StringUtils.isNotBlank(loanReg.getLoanFeeDefId())){
		    		loanFeeDef =  unifiedParamFacilityProvide.loanFeeDefByKey(
			    			productCredit.productCd, loanReg.getLoanType(), Integer.valueOf(loanReg.getLoanFeeDefId()));
		    	}else{
				    if(loanReg.getLoanType()==LoanType.MCAT){
				    	loanFeeDef = unifiedParamFacilityProvide.loanFeeDefMCAT(loanPlan.loanCode);
				    }else{
				    	//马上贷产品
				    	loanFeeDef = unifiedParamFacilityProvide.loanFeeDef(loanPlan.loanCode,Integer.valueOf(loanReg.getLoanInitTerm()),loanReg.getLoanInitPrin());
				    }
		    	}
		    }
		    if(null == loanPlan || null == loanFeeDef){
		    	throw new ProcessException(MsRespCode.E_1052.getCode(), MsRespCode.E_1052.getMessage());
		    }
			
		    context.setProduct(product);
		    context.setProductCredit(productCredit);
		    context.setAccAttr(acctAttr);
		    context.setLoanPlan(loanPlan);
		    context.setLoanFeeDef(loanFeeDef);
		    logger.debug("accountAttributeId --------- " + productCredit.accountAttributeId);
		}catch(Exception e){
			if(logger.isErrorEnabled())
				logger.error("获取产品参数异常,产品号[{"+productCd+"}]",e);
			throw new ProcessException(MsRespCode.E_1052.getCode(), MsRespCode.E_1052.getMessage());
		}

	}
	
	/**
	 * 放款处理中逻辑(除了溢缴款转出)
	 * @param context
	 */
	private void handler(TxnContext context){
		TxnInfo txnInfo = context.getTxnInfo();
		
		sunshineCommService.loadLoanReg(context, context.getOrigOrder().getRefNbr(),LoanRegStatus.C,null, true);
		// 获取产品参数
		this.getProdParam(context);

		//获取贷款金额
		txnInfo.setTransAmt(context.getLoanReg().getLoanInitPrin());
	}
}
