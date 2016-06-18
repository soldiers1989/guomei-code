package com.sunline.ccs.service.handler.appapi;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.facility.TxnUtils;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.facility.order.PaymentFacility;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.enums.AuthTransDirection;
import com.sunline.ccs.param.def.enums.TransAmtDirection;
import com.sunline.ccs.service.context.TxnContext;
import com.sunline.ccs.service.context.TxnInfo;
import com.sunline.ccs.service.handler.AppapiCommService;
import com.sunline.ccs.service.handler.OpenAcctCommService;
import com.sunline.ccs.service.msentity.MsRequestInfo;
import com.sunline.ccs.service.msentity.MsResponseInfo;
import com.sunline.ccs.service.msentity.TFDWithDrawReq;
import com.sunline.ccs.service.msentity.TFDWithDrawResp;
import com.sunline.ccs.service.noticetemplate.SMSLoanDeductions;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.AuthTransStatus;
import com.sunline.ppy.dictionary.enums.AuthTransType;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanMold;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.LoanUsage;
import com.sunline.ppy.dictionary.enums.MsPayfrontError;
import com.sunline.ppy.dictionary.enums.MsRespCode;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * 循环现金贷提款(随借随换)
 * @author wangz
 *
 */
@Service
public class TFDWithDrawHandler  extends AbstractHandler{
	private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    CustAcctCardFacility queryFacility;
	@Autowired
	private AppapiCommService appapiCommService;
	@Autowired
	private OpenAcctCommService openAcctCommService;
	@Autowired
	private PaymentFacility paymentFacility;
	@Autowired
	public UnifiedParamFacilityProvide unifiedParamFacilityProvide;
	@Autowired
	public UnifiedParameterFacility unifiedParameterFacility;
	@Autowired
	private SMSLoanDeductions smsLoanDeductions;
	@Autowired
	TxnUtils txnUtils;
	
	public MsResponseInfo execute(MsRequestInfo msRequestInfo) throws ProcessException {
		LogTools.printLogger(logger, "TFDWithDrawReq", "循环现金贷提款", msRequestInfo, true);
		TFDWithDrawReq req = (TFDWithDrawReq) msRequestInfo;
		LogTools.printObj(logger, req, "TFDWithDrawReq请求参数");
		TFDWithDrawResp resp = new TFDWithDrawResp();
		TxnContext context = new TxnContext();
		TxnInfo txnInfo = new TxnInfo();
		String payJson = "";
		
		context.setTxnInfo(txnInfo);
		context.setMsRequestInfo(msRequestInfo);
		
		try {
			this.initTxnInfo(req, txnInfo);
			
			this.checkReq(req, txnInfo);
			
			//重复交易
			appapiCommService.checkRepeatTxn(context);
			
			appapiCommService.loadAcctByContrNo(context, true);
			appapiCommService.loadAcctOByAcct(context, true);
			appapiCommService.loadCardOByAcct(context, true);
			appapiCommService.loadLoanByContrNo(context,null, false);
			appapiCommService.loadLoanRegList(context,LoanType.MCAT);
			appapiCommService.loadCustomerByCustId(context, true);
			
			// 确定产品参数
			openAcctCommService.getProdParam(context);
			
			this.validateForApply(context);
			
			//锁定码
			appapiCommService.checkBlockCode(context);
			
			//检查otb
			appapiCommService.valiLoanOtb(context);
			
			this.bizProc(context,req);

			//处理结果，单独事务处理
			this.payResultProc(context,payJson);
			
			//向通知平台发送通知		
			smsLoanDeductions.sendSMS(context);	
			
		}catch (ProcessException pe){
			if(logger.isErrorEnabled())
				logger.error(pe.getMessage(), pe);
			
			appapiCommService.preException(pe, pe, txnInfo);
			appapiCommService.saveErrorOrder(context);
			//向通知平台发送通知		
			smsLoanDeductions.sendSMS(context);
		}catch (Exception e) {
			if(logger.isErrorEnabled())
				logger.error(e.getMessage(), e);
			ProcessException pe = new ProcessException();//e不是pe，处理逻辑与没有code的pe一样
			appapiCommService.preException(pe, pe, txnInfo);
			appapiCommService.saveErrorOrder(context);
		}finally{
			LogTools.printLogger(logger, "TFDWithDrawResp", "循环现金贷提款", resp, false);
		}
		
		this.setResponse(resp,req,txnInfo);
		LogTools.printObj(logger, resp, "TFDWithDrawResp响应参数");
		return resp;
		
	}

	/**
	 * 检查报文
	 * @param req
	 */
	private void checkReq(TFDWithDrawReq req,TxnInfo txnInfo) {
		
		if(req.amount.compareTo(BigDecimal.ZERO)<=0){
			throw new ProcessException(MsRespCode.E_1043.getCode(),MsRespCode.E_1043 .getMessage()+ ",字段名称{AMOUNT},必须大于0");
		}
		
	}
	
	/**
	 * 业务检查
	 * @param context
	 */
	private void validateForApply(TxnContext context) {
		CcsAcct acct = context.getAccount();
		TxnInfo txnInfo = context.getTxnInfo();
		LoanPlan loanPlan = context.getLoanPlan();
//		LoanFeeDef loanFeeDef = context.getLoanFeeDef();
		
		if(loanPlan.loanType != LoanType.MCAT){
			throw new ProcessException(MsRespCode.E_1052.getCode(),MsRespCode.E_1052.getMessage()+"，非随借随还贷款产品");
		}
		
		if(loanPlan.loanMold != LoanMold.C){
			throw new ProcessException(MsRespCode.E_1052.getCode(),MsRespCode.E_1052.getMessage()+"，非随借随还贷款产品");
		}
		
//		if(txnInfo.getTransAmt().compareTo(loanFeeDef.maxAmount)>0 ){
//			throw new ProcessException(MsRespCode.E_1036.getCode(),MsRespCode.E_1036.getMessage());
//		}
//		if(txnInfo.getTransAmt().compareTo(loanFeeDef.minAmount)<0){
//			throw new ProcessException(MsRespCode.E_1037.getCode(),MsRespCode.E_1037.getMessage());
//		}
		//开户时检查-提款不检查
//		if(loanPlan.loanValidity.before(txnInfo.getBizDate())){
//			throw new ProcessException(MsRespCode.E_1038.getCode(), MsRespCode.E_1038.getMessage());
//		}
//		if(loanPlan.loanStaus != LoanPlanStatus.A){
//			throw new ProcessException(MsRespCode.E_1039.getCode(),MsRespCode.E_1039.getMessage());
//		}
		
		if(logger.isDebugEnabled())
			logger.debug("合同放款有效期:[{}],业务日期：[{}]",acct.getAcctExpireDate(),txnInfo.getBizDate());
		if(acct.getAcctExpireDate().compareTo(txnInfo.getBizDate())<0){
			throw new ProcessException(MsRespCode.E_1056.getCode(), MsRespCode.E_1056.getMessage());
		}
		
		appapiCommService.checkLoan(context);
		
	}

	/**
	 * 业务处理
	 * @param context
	 */
	@Transactional
	private void bizProc(TxnContext context,TFDWithDrawReq req) {
		TxnInfo txnInfo = context.getTxnInfo();
		CcsLoan ccsLoan = context.getLoan();
		List<CcsLoanReg> regList = context.getRegList();
		
		//设置借据号,取不到则后续业务处理使用新建的reg的借据号
		if(null != ccsLoan){
			txnInfo.setDueBillNo(ccsLoan.getDueBillNo());
		}else if(null != regList && regList.size()>0){
			txnInfo.setDueBillNo(regList.get(0).getDueBillNo());
		}
		
		//建立放款注册
		openAcctCommService.mergeLoanReg(context);
		//创建AuthMemoO
		appapiCommService.saveCcsAuthmemoO(context);
		//初始化订单
		appapiCommService.installOrder(context);
		//判断是否传入卡号等
		this.setOrderTxninfo(context, req);
	}
	
	/**
	 * 处理支付结果
	 * 
	 * @param context
	 * @param payJson
	 * @param resp
	 */
	@Transactional
	public void payResultProc(TxnContext context,String payJson) {
		TxnInfo txnInfo = context.getTxnInfo();
		ProductCredit productCredit = context.getProductCredit();
		try{
			//组装支付指令
			payJson = paymentFacility.installPaySinPaymentCommand(context.getOrder());
			
			if(logger.isDebugEnabled())
				logger.debug("独立事务，处理支付结果--payResultProc,支付报文：[{}]",payJson);
			
			appapiCommService.loadAuthmemoLogKv(context, context.getTxnInfo().getLogKv(),false);
			
			if(logger.isDebugEnabled())
				logger.debug("自动放款阈值:[{}],贷款金额:[{}]",productCredit.autoDCAmtLimit,txnInfo.getTransAmt());
			if(productCredit.autoDCAmtLimit.compareTo(txnInfo.getTransAmt())>=0){
				appapiCommService.loanProc(context,payJson);
			}else{
				throw new ProcessException(MsPayfrontError.E_90004.getCode(), MsPayfrontError.E_90004.getDesc());
			}
			
		}catch (ProcessException pe){
			if(logger.isErrorEnabled())
				logger.error(pe.getMessage(), pe);
			ProcessException newPe = appapiCommService.preException(pe, pe, txnInfo);
			appapiCommService.exceptionProc(context, newPe);
			
		}catch (Exception e) {
			if(logger.isErrorEnabled())
				logger.error(e.getMessage(), e);
			
			ProcessException pe = new ProcessException();
			ProcessException newPe = appapiCommService.preException(e, pe, txnInfo);;//e不是pe，处理逻辑与没有code的pe一样
			appapiCommService.exceptionProc(context, newPe);
		}
		//保存交易数据
		appapiCommService.mergeProc(context);
	}

	/**
	 * 组装响应报文
	 * @param resp
	 * @param req
	 * @param txnInfo
	 */
	private void setResponse(TFDWithDrawResp resp, TFDWithDrawReq req, TxnInfo txnInfo) {
		resp.setErrorCode(txnInfo.getResponsCode());
		resp.setErrorMessage(txnInfo.getResponsDesc());
		if(StringUtils.equals(MsRespCode.E_0000.getCode(), txnInfo.getResponsCode())){
			resp.setContractNo(txnInfo.getContrNo());
			resp.setDueBillNo(txnInfo.getDueBillNo());
			resp.setStatus("S");//交易成功
		}else{
			resp.setStatus("F");//交易失败
		}
	}

	/**
	 * 初始化交易中间信息
	 * @param req
	 * @param txnInfo
	 */
	private void initTxnInfo(TFDWithDrawReq req, TxnInfo txnInfo) {
		txnInfo.setBizDate(req.getBizDate());
		txnInfo.setTransDirection(AuthTransDirection.Normal);
		txnInfo.setTransType(AuthTransType.AgentDebit);
		txnInfo.setSysOlTime(new Date());
		txnInfo.setRequestTime(req.getRequestTime());
		txnInfo.setServiceSn(req.getServiceSn());
		txnInfo.setMti("0208");
		txnInfo.setProcCode("480001");
		txnInfo.setLoanUsage(LoanUsage.L);
		txnInfo.setTransAmt(req.amount);
		txnInfo.setOrg(OrganizationContextHolder.getCurrentOrg());
		txnInfo.setInputSource(req.getInputSource());
		txnInfo.setAcqId(req.getAcqId());
		txnInfo.setSubTerminalType(req.getSubTerminalType());
		txnInfo.setAuthTransStatus(AuthTransStatus.N);
		txnInfo.setContrNo(req.contractNo);
		txnInfo.setDirection(TransAmtDirection.D);
		txnInfo.setCdTerms(req.getTerm());
		txnInfo.setLoanCode("");
		txnInfo.setMobile(req.mobile);
//		txnInfo.setRefNbr(req.getServiceSn());
		txnInfo.setRefNbr(txnUtils.genRefnbr(txnInfo.getBizDate(), txnInfo.getServiceSn()));
		txnInfo.setServiceId(req.getServiceId());
		txnInfo.setPrepayPkgInd(Indicator.N);
		if(logger.isDebugEnabled())
			logger.debug("业务日期：[{}]",txnInfo.getBizDate());
		LogTools.printObj(logger, txnInfo, "txnInfo交易中间信息");
		
	}
	/**
	 * 如果客户传入卡号等，则用传的卡号交易，否则用原卡号交易
	 * @param context
	 * @param req
	 */
	private void setOrderTxninfo(TxnContext context,TFDWithDrawReq req) {
		if (req.getBankCardNbr()!=null) {
			if(req.getBankCardName()==null
					|| req.getBankName()==null
					|| req.getBankProvince()==null
					|| req.getBankCity()==null){
			
				throw new ProcessException(MsRespCode.E_1079.getCode(),MsRespCode.E_1079.getMessage());
			}
			context.getOrder().setUsrName(req.getBankCardName());
			context.getOrder().setOpenBank(req.getBankName());
			context.getOrder().setState(req.getBankProvince());
			context.getOrder().setCity(req.getBankCity());
			context.getOrder().setCardNo(req.getBankCardNbr());
		}
	
	}
}
