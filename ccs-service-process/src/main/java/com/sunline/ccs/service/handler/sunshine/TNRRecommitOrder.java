package com.sunline.ccs.service.handler.sunshine;

import java.text.ParseException;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.api.GlobalManagementService;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.facility.TxnUtils;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.facility.order.OrderFacility;
import com.sunline.ccs.facility.order.PaymentFacility;
import com.sunline.ccs.infrastructure.server.repos.RCcsLoanReg;
import com.sunline.ccs.infrastructure.server.repos.RCcsOrder;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsOrder;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoanReg;
import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.enums.AuthTransDirection;
import com.sunline.ccs.param.def.enums.TransAmtDirection;
import com.sunline.ccs.service.context.LoanInfo;
import com.sunline.ccs.service.context.TxnContext;
import com.sunline.ccs.service.context.TxnInfo;
import com.sunline.ccs.service.entity.S30003RecommitOrderReq;
import com.sunline.ccs.service.entity.S30003RecommitOrderResp;
import com.sunline.ccs.service.handler.SunshineCommService;
import com.sunline.ccs.service.noticetemplate.SMSLoanDeductions;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.pcm.param.def.Product;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.AuthTransStatus;
import com.sunline.ppy.dictionary.enums.AuthTransType;
import com.sunline.ppy.dictionary.enums.InputSource;
import com.sunline.ppy.dictionary.enums.LoanAction;
import com.sunline.ppy.dictionary.enums.LoanMold;
import com.sunline.ppy.dictionary.enums.LoanRegStatus;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.LoanUsage;
import com.sunline.ppy.dictionary.enums.MsRespCode;
import com.sunline.ppy.dictionary.enums.OrderStatus;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * 贷款重提
 * @author wangz
 *
 */
@Service
public class TNRRecommitOrder {
	private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    CustAcctCardFacility queryFacility;
	@Autowired
	private SunshineCommService sunshineCommService;
	@Autowired
	private UnifiedParamFacilityProvide unifiedParamFacilityProvide;
	@PersistenceContext
	private EntityManager em;
	@Autowired
	private UnifiedParamFacilityProvide unifiedParameterFacilityProvide;
	@Autowired
	private UnifiedParameterFacility unifiedParameterFacility;
	@Autowired
	private RCcsLoanReg rCcsLoanReg;
	@Autowired
	private RCcsOrder rCcsOrder;
	@Autowired
	private PaymentFacility paymentFacility;
	@Autowired
	private OrderFacility orderFacility;
	@Autowired
	private GlobalManagementService globalManageService;
	@Autowired
	SMSLoanDeductions smsLoanDeductions;
	@Autowired 
	TxnUtils txnUtils;
	
	public S30003RecommitOrderResp handler(S30003RecommitOrderReq req) throws ProcessException {
		LogTools.printLogger(logger, "S30003RecommitOrderReq", "放款订单重提", req, true);
		LogTools.printObj(logger, req, "请求报文S30003RecommitOrderReq");
		S30003RecommitOrderResp resp = new S30003RecommitOrderResp();
		TxnContext context = new TxnContext();
		TxnInfo txnInfo = new TxnInfo();
		LoanInfo loanInfo = new LoanInfo();
		String payJson = "";
		
		context.setTxnInfo(txnInfo);
		context.setLoanInfo(loanInfo);
		context.setSunshineRequestInfo(req);
		
		try {
			if(null != req) {
				// 获取原订单信息,授权流水信息
				context.setOrigOrder(orderFacility.findById(req.getOrderId(), true));
				
				this.initTxnInfo(req,context.getOrigOrder(),txnInfo);
				
				//重复交易
				sunshineCommService.checkReqAndRepeatTxn(context);
				// 获取账户信息
//				sunshineCommService.loadLoanReg(context, LoanRegStatus.F,null, true);
				sunshineCommService.loadLoanReg(context,context.getOrigOrder().getRefNbr(), LoanRegStatus.F,null, false);
				sunshineCommService.loadLoan(context, false);
				
				this.checkLoanStatus(context);
				
				sunshineCommService.loadAcct(context, true);
				sunshineCommService.loadAcctO(context, true);
				sunshineCommService.loadCard(context, true);
				// 获取客户信息
				sunshineCommService.loadCustomer(context, true);
				
				//获取参数信息
				getProdParam(context);
				//获取贷款金额
				txnInfo.setDueBillNo(context.getLoanReg().getDueBillNo());
				txnInfo.setTransAmt(context.getLoanReg().getLoanInitPrin());
			    
			    
			    //业务处理 单独事务
			    payJson = this.bizProc(context);
				
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
				throw new ProcessException(MsRespCode.E_1008.getCode(),MsRespCode.E_1008.getMessage());
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
			LogTools.printLogger(logger, "S30003RecommitOrderResp", "放款订单重提", resp, false);
		}
		
		this.setResponse(resp,req,txnInfo);
		LogTools.printObj(logger, resp, "响应报文S30003RecommitOrderResp");
		return resp;
	}
	
	/**
	 * 组装响应报文
	 * @param resp
	 * @param req
	 * @param txnInfo
	 */
	private void setResponse(S30003RecommitOrderResp resp, S30003RecommitOrderReq req, TxnInfo txnInfo) {
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
		}
			
		if(StringUtils.equals(MsRespCode.E_0000.getCode(), txnInfo.getResponsCode())){
			resp.setStatus("S");//交易成功
		}else{
			resp.setStatus("F");//交易失败
		}
	}
//	/**
//	 * 组装返回response
//	 * @param context
//	 * @param resp
//	 */
//	private void setResponse(TxnContext context ,S30003RecommitOrderResp resp){
//		MsPayfrontError msPayfrontError;
//		
//		resp.setOrderid(context.getOrder().getOrderId());
//		resp.setErrorCode(context.getTxnInfo().getResponsCode());
//		resp.setErrorMessage(context.getTxnInfo().getResponsDesc());
//		
//		msPayfrontError = sunshineCommService.getErrorEnum(context.getTxnInfo().getPayRespCode(), MsPayfrontError.class);
//
//		resp.setStatus(msPayfrontError.getDecision());
//	}
	/**
	 * 业务处理
	 * @param context
	 * @param payJson
	 * @return
	 */
	@Transactional
	private String bizProc(TxnContext context) {
		if(logger.isDebugEnabled())
			logger.debug("独立事务，业务处理--bizProc");
		String payJson ="";
		// 检查放款参数
		this.validateForApply(context);
		
		//检查otb
		sunshineCommService.valiLoanOtb(context);
		
		//新增订单信息
	    sunshineCommService.saveCcsAuthmemoO(context);
		sunshineCommService.installOrder(context);
		//订单表记录原订单表订单号
		context.getOrder().setOriOrderId(context.getOrigOrder().getOrderId());
		//原订单状态改为失效
		context.getOrigOrder().setOrderStatus(OrderStatus.R);		
		context.getOrigOrder().setOptDatetime(context.getTxnInfo().getBizDate());
		rCcsOrder.save(context.getOrigOrder());

		this.updateLoanReg(context);

		//组装支付指令
		payJson = paymentFacility.installPaySinPaymentCommand(context.getOrder());
		return payJson;
	}

	/**
	 * 初始化交易中间信息
	 * @param req
	 * @param txnInfo
	 */
	private void initTxnInfo(S30003RecommitOrderReq req, CcsOrder origOrder,TxnInfo txnInfo) {
		txnInfo.setBizDate(globalManageService.getSystemStatus().getBusinessDate());
		txnInfo.setTransDirection(AuthTransDirection.Normal);
		txnInfo.setTransType(AuthTransType.AgentDebit);
		txnInfo.setGuarantyid(req.getGuarantyid());
		txnInfo.setDueBillNo(req.getDuebillno());
		txnInfo.setSysOlTime(new Date());
		txnInfo.setRequestTime(req.getRequestTime());
		txnInfo.setServiceSn(req.getServiceSn());
		txnInfo.setRefNbr(txnUtils.genRefnbr(req.getBizDate(), req.getServiceSn()));
		txnInfo.setMti("0208");
		txnInfo.setProcCode("480001");
		txnInfo.setLoanUsage(LoanUsage.A);
		txnInfo.setInputSource(req.getInputSource());
		txnInfo.setAcqId(req.getAcqId());
		txnInfo.setSubTerminalType(req.getSubTerminalType());
		txnInfo.setOrg(OrganizationContextHolder.getCurrentOrg());		
		txnInfo.setAuthTransStatus(AuthTransStatus.N);
		txnInfo.setDirection(TransAmtDirection.D);
		txnInfo.setMobile(origOrder.getMobileNumber());
		txnInfo.setServiceId(origOrder.getServiceId());
		txnInfo.setLoanAmt(origOrder.getLoanAmt());
		txnInfo.setPremiumAmt(origOrder.getPremiumAmt());
		txnInfo.setPremiumInd(origOrder.getPremiumInd());

		if(logger.isDebugEnabled())
			logger.debug("业务日期：[{}]",txnInfo.getBizDate());
		LogTools.printObj(logger, txnInfo, "交易中间信息txnInfo");
	}
	
	/**
	 * 更新贷款注册信息
	 * @param req
	 * @return
	 * @author lizz
	 */
	private void updateLoanReg(TxnContext context) {
		TxnInfo txnInfo = context.getTxnInfo();
		CcsLoanReg loanReg = context.getLoanReg();
		
		loanReg.setLoanRegStatus(LoanRegStatus.C);
		loanReg.setValidDate(txnInfo.getBizDate());
		loanReg.setRequestTime(txnInfo.getSysOlTime());
		loanReg.setRefNbr(txnInfo.getRefNbr());
		
		rCcsLoanReg.save(loanReg);
	}
	
	/**
	 * 判断账户锁定码
	 * @param loanReg 
	 * @param request
	 */
	public void validateForApply(TxnContext context) throws ProcessException{
		if(logger.isDebugEnabled())
			logger.debug("判断账户锁定码--validateForApply");
		CcsLoanReg loanReg = context.getLoanReg();
		TxnInfo txnInfo = context.getTxnInfo();
		
		LoanPlan loanPlan = context.getLoanPlan();
		LoanFeeDef loanFeeDef = context.getLoanFeeDef();
		// 判断账户上是否有锁定码存在，存在则无法分期
		sunshineCommService.checkBlockCode(context);
		//判断已有现金分期笔数是否超限
		QCcsLoanReg qCcsLoanReg = QCcsLoanReg.ccsLoanReg;
		QCcsLoan qCcsLoan = QCcsLoan.ccsLoan;
		JPAQuery query = new JPAQuery(em);
		JPAQuery queryLoan = new JPAQuery(em);
		long cnt = 0;
		cnt+=query.from(qCcsLoanReg).where(qCcsLoanReg.dueBillNo.eq(txnInfo.getDueBillNo()).
				and(qCcsLoanReg.org.eq(CheckUtil.addCharForNum(txnInfo.getOrg(), 12, " "))).
				and(qCcsLoanReg.loanType.eq(LoanType.MCEI)).
				and(qCcsLoanReg.loanAction.in(LoanAction.A)).
				and(qCcsLoanReg.loanRegStatus.in(LoanRegStatus.S,LoanRegStatus.C)).
				and(qCcsLoanReg.contrNbr.eq(context.getAccount().getContrNbr()))).count();
		
		LogTools.printObj(logger, cnt, "贷款注册CcsLoanReg的条数");
		
		cnt+=queryLoan.from(qCcsLoan).where(qCcsLoan.dueBillNo.eq(txnInfo.getDueBillNo()).
				and(qCcsLoan.org.eq(CheckUtil.addCharForNum(txnInfo.getOrg(), 12, " "))).
				and(qCcsLoan.contrNbr.eq(context.getAccount().getContrNbr()))).count();
//				and(qCcsLoan.loanType.eq(LoanType.MCEI))).
//				and(qCcsLoan.loanStatus.notIn(LoanStatus.T,LoanStatus.F))).count();
		
		LogTools.printObj(logger, cnt, "贷款CcsLoan的条数");
		
		if(null == loanPlan.loanMold)
			throw new ProcessException(MsRespCode.E_1052.getCode(),MsRespCode.E_1052.getMessage());
		
		if(loanPlan.loanMold == LoanMold.S){
			if(cnt>0){
				throw new ProcessException(MsRespCode.E_1013.getCode(),MsRespCode.E_1013.getMessage());
			}
		}
		//20151127
		if(loanReg.getLoanInitPrin().compareTo(loanFeeDef.maxAmount)>0 &&
				InputSource.SUNS.equals(context.getSunshineRequestInfo().getInputSource())){
			throw new ProcessException(MsRespCode.E_1036.getCode(),MsRespCode.E_1036.getMessage());
		}
		//20151127
		if(loanReg.getLoanInitPrin().compareTo(loanFeeDef.minAmount)<0 &&
				InputSource.SUNS.equals(context.getSunshineRequestInfo().getInputSource())){
			throw new ProcessException(MsRespCode.E_1037.getCode(),MsRespCode.E_1037.getMessage());
		}
		if(loanPlan.loanValidity.before(unifiedParameterFacilityProvide.BusinessDate())){
			throw new ProcessException(MsRespCode.E_1038.getCode(),MsRespCode.E_1038.getMessage());
		}
		if(loanPlan.loanStaus != com.sunline.ccs.param.def.enums.LoanPlanStatus.A){
			throw new ProcessException(MsRespCode.E_1039.getCode(),MsRespCode.E_1039.getMessage());
		}
	}
	
	/**
	 * 校验放款状态
	 * @param req
	 * @return
	 * @author lizz
	 * @throws ParseException 
	 */
	private void checkLoanStatus(TxnContext context) throws ParseException {
		if(logger.isDebugEnabled())
			logger.debug("校验放款状态--checkLoanStatus");
		CcsLoanReg loanReg = context.getLoanReg();
		CcsLoan loan = context.getLoan();
		TxnInfo txnInfo = context.getTxnInfo();
		
		if(null == loanReg ){
			throw new ProcessException(MsRespCode.E_1011.getCode(),MsRespCode.E_1011.getMessage());
		}
		
		
		if(null != loan && loanReg.getLoanType() != LoanType.MCAT){
			throw new ProcessException(MsRespCode.E_1045.getCode(),MsRespCode.E_1045.getMessage());
		}
		
//		if(DateUtils.isSameDay(loanReg.getRegisterDate(), txnInfo.getBizDate())){
//			throw new ProcessException(MsRespCode.E_1048.getCode(),MsRespCode.E_1048.getMessage());
//		}
		
		//当天不能做重提，重提必须是失败的借据
		if( !loanReg.getLoanRegStatus().equals(LoanRegStatus.F)) {
			throw new ProcessException(MsRespCode.E_1045.getCode(),MsRespCode.E_1045.getMessage());
		}
		
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

}
