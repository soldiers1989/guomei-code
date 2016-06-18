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
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.DateUtils;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.facility.TxnUtils;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.facility.order.OrderFacility;
import com.sunline.ccs.facility.order.PaymentFacility;
import com.sunline.ccs.infrastructure.server.repos.RCcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
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
import com.sunline.ccs.service.entity.S30002RecommitReq;
import com.sunline.ccs.service.entity.S30002RecommitResp;
import com.sunline.ccs.service.handler.SunshineCommService;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.pcm.param.def.Product;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.AuthTransType;
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
public class TNRRecommit {
	private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    CustAcctCardFacility queryFacility;
	@Autowired
	private SunshineCommService sunshineCommService;
	@Autowired
	private UnifiedParamFacilityProvide unifiedParamFacilityProvide;
	@Autowired
	private UnifiedParameterFacility unifiedParameterFacility;
	@PersistenceContext
	private EntityManager em;
	@Autowired
	private UnifiedParamFacilityProvide unifiedParameterFacilityProvide;
	@Autowired
	private RCcsLoanReg rCcsLoanReg;
	@Autowired
	private PaymentFacility paymentFacility;
	@Autowired
	private OrderFacility orderFacility;
	@Autowired
	TxnUtils txnUtils;

	public S30002RecommitResp handler(S30002RecommitReq req) throws ProcessException {
		LogTools.printLogger(logger, "S30002RecommitReq", "放款重提", req, true);
		LogTools.printObj(logger, req, "请求报文S30002RecommitReq");
		S30002RecommitResp resp = new S30002RecommitResp();
		TxnContext context = new TxnContext();
		TxnInfo txnInfo = new TxnInfo();
		LoanInfo loanInfo = new LoanInfo();
		String payJson = "";
		
		context.setTxnInfo(txnInfo);
		context.setLoanInfo(loanInfo);
		context.setSunshineRequestInfo(req);
		
		try {
			if(null != req && null != req.getGuarantyid()) {
				
				this.initTxnInfo(req, txnInfo);
				
				//重复交易
				sunshineCommService.checkReqAndRepeatTxn(context);
				
				// 获取贷款注册信息  找到失败的loadReg
				sunshineCommService.loadLoanReg(context,null, null,null, false);
				sunshineCommService.loadLoan(context, false);
				this.checkLoanStatus(context);
				// 获取账户信息
				sunshineCommService.loadAcct(context, true);
				sunshineCommService.loadAcctO(context, true);
				// 确定产品参数
				this.getProdParam(context);
				
				sunshineCommService.loadCard(context, true);
				
				// 获取客户信息
				sunshineCommService.loadCustomer(context, true);
				
				//获取借据号贷款金额
				txnInfo.setDueBillNo(context.getLoanReg().getDueBillNo());
				txnInfo.setTransAmt(context.getLoanReg().getLoanInitPrin());
			    //业务处理 单独事务
			    payJson = this.bizProc(context);
				
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
//			this.exceptionProc(context, pe);
			
		}catch (Exception e) {
			if(logger.isErrorEnabled())
				logger.error(e.getMessage(), e);
			
			ProcessException pe = new ProcessException();
			sunshineCommService.preException(pe, pe, txnInfo);
//			this.exceptionProc(context, pe);
			
		}finally{
			LogTools.printLogger(logger, "S30002RecommitResp", "放款重提", resp, false);
		}
		
		this.setResponse(resp,req,txnInfo);
		LogTools.printObj(logger, resp, "响应报文S30002RecommitResp");
		return resp;
	}
	
	/**
	 * 组装响应报文
	 * @param resp
	 * @param req
	 * @param txnInfo
	 */
	private void setResponse(S30002RecommitResp resp, S30002RecommitReq req, TxnInfo txnInfo) {
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
	 * 获取产品参数
	 * @param context
	 */
	private void getProdParam(TxnContext context){
		if(logger.isDebugEnabled())
			logger.debug("获取产品参数--getProdParam");
		CcsAcct ccsAcct = context.getAccount();
		CcsLoanReg ccsLoanReg = context.getLoanReg();
		try{
			
			ProductCredit productCredit = unifiedParameterFacility.loadParameter(ccsAcct.getProductCd(), ProductCredit.class);
		    Product product = unifiedParameterFacility.loadParameter(ccsAcct.getProductCd(), Product.class);
		    AccountAttribute acctAttr = unifiedParameterFacility.loadParameter(
		    		productCredit.accountAttributeId, AccountAttribute.class);
		    LoanPlan loanPlan = unifiedParamFacilityProvide.loanPlan(ccsAcct.getProductCd(), LoanType.MCEI);
		    LoanFeeDef loanFeeDef = new LoanFeeDef();
		    if(null != ccsLoanReg) {
		    	loanFeeDef = unifiedParamFacilityProvide.loanFeeDef(ccsAcct.getProductCd(), LoanType.MCEI, Integer.valueOf(ccsLoanReg.getLoanInitTerm()));
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
				logger.error("获取产品参数异常,产品号["+ccsAcct.getProductCd()+"]",e);
			throw new ProcessException(MsRespCode.E_1052.getCode(), MsRespCode.E_1052.getMessage());
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
		LogTools.printObj(logger, "", "独立事务，处理bizProc");
		
		String payJson ="";
		// 检查放款参数
		this.validateForApply(context);
		
		//检查otb
		sunshineCommService.valiLoanOtb(context);
		
		sunshineCommService.saveCcsAuthmemoO(context);
		
		//调用支付
		sunshineCommService.installOrder(context);
		
		//组装支付指令
		payJson = paymentFacility.installPaySinPaymentCommand(context.getOrder());
		
		this.updateOrders(context);
		this.updateLoanReg(context);
		return payJson;
	}

	/**
	 * 更新失败订单为已重提
	 * @param context
	 */
	private void updateOrders(TxnContext context) {
		CcsLoanReg loanReg = context.getLoanReg();
		TxnInfo txnInfo = context.getTxnInfo();
		Iterable<CcsOrder> orders = orderFacility.findByDueBillNo(loanReg.getDueBillNo(), OrderStatus.E, 
				txnInfo.getTransType(), loanReg.getAcctNbr(), loanReg.getAcctType());
		if(null != orders){
			for (CcsOrder ccsOrder : orders) {
				ccsOrder.setOrderStatus(OrderStatus.R);
				em.merge(ccsOrder);
			}
		}
	}

	/**
	 * 初始化交易中间信息
	 * @param req
	 * @param txnInfo
	 */
	private void initTxnInfo(S30002RecommitReq req, TxnInfo txnInfo) {
		txnInfo.setBizDate(req.getBizDate());
		txnInfo.setTransDirection(AuthTransDirection.Normal);
		txnInfo.setTransType(AuthTransType.AgentDebit);
		txnInfo.setGuarantyid(req.getGuarantyid());
		txnInfo.setSysOlTime(new Date());
		txnInfo.setRequestTime(req.getRequestTime());
		txnInfo.setServiceSn(req.getServiceSn());
		txnInfo.setRefNbr(txnUtils.genRefnbr(txnInfo.getBizDate(), txnInfo.getServiceSn()));
		txnInfo.setMti("0208");
		txnInfo.setProcCode("480001");
		txnInfo.setLoanUsage(LoanUsage.A);
		txnInfo.setInputSource(req.getInputSource());
		txnInfo.setAcqId(req.getAcqId());
		txnInfo.setSubTerminalType(req.getSubTerminalType());
		txnInfo.setOrg(req.getOrg());
		txnInfo.setDirection(TransAmtDirection.D);
		txnInfo.setServiceId(req.getServiceId());
		
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
		CcsCard card = context.getCard();
		CcsLoanReg loanReg = context.getLoanReg();
		TxnInfo txnInfo = context.getTxnInfo();
		
		LoanPlan loanPlan = unifiedParamFacilityProvide.loanPlan(card.getProductCd(), LoanType.MCEI);
		LoanFeeDef loanFeeDef = unifiedParamFacilityProvide.loanFeeDef(card.getProductCd(), LoanType.MCEI, Integer.valueOf(loanReg.getLoanInitTerm()));
		
//		LogTools.printObj(logger, loanPlan, "贷款产品loanPlan");
//		LogTools.printObj(logger, loanFeeDef, "贷款产品定价loanFeeDef");
		// 判断账户上是否有锁定码存在，存在则无法分期
		sunshineCommService.checkBlockCode(context);
		//判断已有现金分期笔数是否超限
		QCcsLoanReg qCcsLoanReg = QCcsLoanReg.ccsLoanReg;
		QCcsLoan qCcsLoan = QCcsLoan.ccsLoan;
		JPAQuery query = new JPAQuery(em);
		JPAQuery queryLoan = new JPAQuery(em);
		long cnt = 0;
		cnt+=query.from(qCcsLoanReg).where(qCcsLoanReg.guarantyId.eq(txnInfo.getGuarantyid()).
				and(qCcsLoanReg.org.eq(CheckUtil.addCharForNum(txnInfo.getOrg(), 12, " "))).
				and(qCcsLoanReg.loanType.eq(LoanType.MCEI)).
				and(qCcsLoanReg.loanAction.in(LoanAction.A)).
				and(qCcsLoanReg.loanRegStatus.in(LoanRegStatus.S,LoanRegStatus.C))).count();
		
		LogTools.printObj(logger, cnt, "贷款注册CcsLoanReg的条数");
		
		cnt+=queryLoan.from(qCcsLoan).where(qCcsLoan.guarantyId.eq(txnInfo.getGuarantyid()).
				and(qCcsLoan.org.eq(CheckUtil.addCharForNum(txnInfo.getOrg(), 12, " ")))).count();
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
		if(loanReg.getLoanInitPrin().compareTo(loanFeeDef.maxAmount)>0 ){
			throw new ProcessException(MsRespCode.E_1036.getCode(),MsRespCode.E_1036.getMessage());
		}
		if(loanReg.getLoanInitPrin().compareTo(loanFeeDef.minAmount)<0){
			throw new ProcessException(MsRespCode.E_1037.getCode(),MsRespCode.E_1037.getMessage());
		}
		if(loanPlan.loanValidity.before(unifiedParameterFacilityProvide.BusinessDate())){
			throw new ProcessException(MsRespCode.E_1038.getCode(),MsRespCode.E_1038.getMessage());
		}
		if(loanPlan.loanStaus != com.sunline.ccs.param.def.enums.LoanPlanStatus.A){
			throw new ProcessException(MsRespCode.E_1039.getCode(),MsRespCode.E_1039.getMessage());
		}
		context.setLoanPlan(loanPlan);
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
		
		if(null != loan){
			throw new ProcessException(MsRespCode.E_1045.getCode(),MsRespCode.E_1045.getMessage());
		}
		
		if(null == loanReg ){
			throw new ProcessException(MsRespCode.E_1011.getCode(),MsRespCode.E_1011.getMessage());
		}
		
		if(DateUtils.isSameDay(loanReg.getRegisterDate(), txnInfo.getBizDate())){
			throw new ProcessException(MsRespCode.E_1048.getCode(),MsRespCode.E_1048.getMessage());
		}
		
		//当天不能做重提，重提必须是失败的借据
		if( !loanReg.getLoanRegStatus().equals(LoanRegStatus.F)) {
			throw new ProcessException(MsRespCode.E_1045.getCode(),MsRespCode.E_1045.getMessage());
		}
		
	}


}
