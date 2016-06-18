package com.sunline.ccs.service.handler.query;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mysema.query.types.expr.BooleanExpression;
import com.sunline.ccs.facility.DateUtils;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.facility.TxnUtils;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.facility.SMSService.MsgFacility;
import com.sunline.ccs.infrastructure.server.repos.RCcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoanReg;
import com.sunline.ccs.loan.McLoanProvideImpl;
import com.sunline.ccs.loan.TrialResp;
import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.enums.AuthTransTerminal;
import com.sunline.ccs.param.def.enums.PrepaymentCalMethod;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.context.TxnContext;
import com.sunline.ccs.service.context.TxnInfo;
import com.sunline.ccs.service.handler.AppapiCommService;
import com.sunline.ccs.service.handler.appapi.AbstractHandler;
import com.sunline.ccs.service.msdentity.SMSentitySendReq;
import com.sunline.ccs.service.msentity.MsRequestInfo;
import com.sunline.ccs.service.msentity.MsResponseInfo;
import com.sunline.ccs.service.msentity.TNMLBookingReq;
import com.sunline.ccs.service.msentity.TNMLBookingResp;
import com.sunline.pcm.param.def.Mulct;
import com.sunline.pcm.param.def.Product;
import com.sunline.pcm.param.def.enums.MulctMethod;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.InputSource;
import com.sunline.ppy.dictionary.enums.LoanAction;
import com.sunline.ppy.dictionary.enums.LoanRegStatus;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.LoanUsage;
import com.sunline.ppy.dictionary.enums.MsRespCode;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * 提前还款试算/预约
 * @author wangz
 *
 */
@Service
public class TNMLBookingHandler extends AbstractHandler{
	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	//预约类型 1-试算  2-申请
	private static final String BOOKING_TYPE_1 ="1";
	
	private static final String BOOKING_TYPE_2 ="2";
	@Autowired
	private AppapiCommService appapiCommService;
	@Autowired
	private RCcsLoanReg rCcsLoanReg;
	@Autowired
	private UnifiedParamFacilityProvide unifiedParamFacilityProvide;
	@Autowired
	private McLoanProvideImpl loanProvideImpl;
	@Autowired
	private UnifiedParameterFacility unifiedParameterFacility;
	@Autowired
	private TxnUtils txnUtils;
	@Autowired
	private MsgFacility msgFacility;
	
	public MsResponseInfo execute(MsRequestInfo msRequestInfo) {
		LogTools.printLogger(log, "TNMLBookingReq", "提前还款试算/预约", msRequestInfo, true);
		TNMLBookingReq req = (TNMLBookingReq) msRequestInfo;
		LogTools.printObj(log, msRequestInfo, "请求参数TNMLBookingReq");
		if(log.isDebugEnabled())
			log.debug("[TNMLBooking]:借据号[{}],合同号[{}],操作类型[{}],预约还款日期[{}]",req.getDueBillNo(),req.getContrNbr(),req.getType(),req.getCaldate());
		
		TNMLBookingResp resp = new TNMLBookingResp();
		TxnContext context = new TxnContext();
		TxnInfo txnInfo = new TxnInfo();
		TrialResp trialResp = null;
		
		context.setTxnInfo(txnInfo);
		context.setMsRequestInfo(req);
		
		try{
			this.initTxnInfo(context,req);
			appapiCommService.loadLoanByDueBillNo(context, null, true);
			appapiCommService.loadLoanRegByDueBillNo(context,null,LoanRegStatus.A,LoanAction.O, false);
			appapiCommService.loadAcct(context, txnInfo.getAcctNbr(), txnInfo.getAcctType(), true);
			appapiCommService.loadAcctOByAcct(context, true);
			
			//检查贷款
			appapiCommService.checkLoan(context);
			
			this.getProdParam(context);
			
			//检查业务逻辑
			this.check(context);
			
			//试算预约结清金额
			trialResp = this.computBookingAmt(context);
			
			//预约结清申请
			this.bookingApply(context, trialResp);
			
			//预约成功发送通知短信
			if(BOOKING_TYPE_2.equals(txnInfo.getBookingType()))
				this.sendSms(msRequestInfo.getOpId(),context.getAccount(),context.getLoan(),context.getLoanReg().getPreAdDate());
			
		}catch (ProcessException pe){
			if(log.isErrorEnabled())
				log.error(pe.getMessage(), pe);
			
			appapiCommService.preException(pe, pe, txnInfo);
		}catch (Exception e) {
			if(log.isErrorEnabled())
				log.error(e.getMessage(), e);
			ProcessException pe = new ProcessException();
			
			appapiCommService.preException(pe, pe, txnInfo);
		}finally{
			LogTools.printLogger(log, "S11001BookingResp", "提前还款试算/预约", resp, false);
		}
		
		this.setResponse(resp,req,txnInfo, trialResp);
		//如果SubTerminalType为host证明为页面发起交易
		if (AuthTransTerminal.HOST.toString().equals(req.getSubTerminalType())==false) {
			this.setResponse(resp,trialResp);
		}
		LogTools.printObj(log, resp, "响应报文S11001BookingReq");
		return resp;
	}
	
	/**
	 * 组装响应报文
	 * @param resp
	 * @param req
	 * @param txnInfo
	 */
	private void setResponse(TNMLBookingResp resp, TNMLBookingReq req, TxnInfo txnInfo,TrialResp trialResp) {
		
		if(null != trialResp){
			resp.setAmount(String.valueOf(trialResp.getTotalAMT()));//总金额 totalAMT
			resp.setInterest(String.valueOf(trialResp.getCtdInterestAMT()));//利息 CtdInterestAMT
			resp.setPoundage(String.valueOf(trialResp.getCtdInitFee()));//提前还款手续费 initFee
			resp.setPremium(String.valueOf(trialResp.getCtdInsuranceAMT()));//保费 insuranceAMT
			resp.setPrincipal(String.valueOf(trialResp.getCtdPricinpalAMT()));//本金  CrdPricinpalAMT
			resp.setStamp(String.valueOf(trialResp.getCtdStampdutyAMT()));//印花税
			resp.setLifeInsu(String.valueOf(trialResp.getCtdLifeInsuFeeAMT()));//寿险计划包
			resp.setReplaceSvc(String.valueOf(trialResp.getCtdReplaceSvcAMT()));//代收服务费
			resp.setLoanTermSvc(String.valueOf(trialResp.getCtdLoanTermSvc()));//分期手续费
			resp.setLoanTermFee(String.valueOf(trialResp.getCtdLoanTermFeeAMT()));//贷款服务费
			
			resp.setDeposit(String.valueOf(trialResp.getDeposit()));
			resp.setMemoAmt(String.valueOf(trialResp.getMemoAmt()));
			resp.setReplaceMulct(String.valueOf(trialResp.getReplaceMulct()));
			resp.setReplacePrepayFee(String.valueOf(trialResp.getReplacePrepayFee()));
			resp.setReplacePenalty(String.valueOf(trialResp.getReplacePenalty()));
			resp.setPremiumAmt(String.valueOf(trialResp.getPremiumAmt()));
			resp.setReplaceLpc(String.valueOf(trialResp.getReplaceLpc()));
		}
		resp.setDueBillNo(txnInfo.getDueBillNo());
		resp.setErrorCode(txnInfo.getResponsCode());
		resp.setErrorMessage(txnInfo.getResponsDesc());
		if(StringUtils.isBlank(txnInfo.getResponsCode())){
			resp.setErrorCode(MsRespCode.E_0000.getCode());
			resp.setErrorMessage(MsRespCode.E_0000.getMessage());
		}
		if(StringUtils.equals(MsRespCode.E_0000.getCode(), txnInfo.getResponsCode())){
			resp.setStatus("S");//交易成功
		}else{
			resp.setStatus("F");//交易失败
		}
	}
	
	private void setResponse(TNMLBookingResp resp,TrialResp trialResp){
		
		if(null != trialResp){
			//对于联机接口的交易  手续为总手续费
			resp.setPoundage(String.valueOf(trialResp.getCtdInitFee().add(trialResp.getReplacePrepayFee())));
			resp.setLifeInsu(null);
			resp.setStamp(null);
			resp.setReplaceSvc(null);
			resp.setLoanTermFee(null);
			resp.setLoanTermSvc(null);
			resp.setPremiumAmt(null);
			resp.setMemoAmt(null);
			resp.setReplacePrepayFee(null);
			resp.setReplaceMulct(null);
			resp.setReplacePenalty(null);
			resp.setDeposit(null);
			resp.setReplaceLpc(null);
		}
	}

	/**
	 * 预约结清申请处理
	 * @param context
	 * @param trialResp
	 */
	@Transactional
	private void bookingApply(TxnContext context,TrialResp trialResp) {
		if(log.isDebugEnabled())
			log.debug("预约结清申请处理--bookingApply");
		TxnInfo txnInfo = context.getTxnInfo();
		CcsLoan loan = context.getLoan();
		CcsLoanReg ccsLoanReg = context.getLoanReg();
		
		if(BOOKING_TYPE_2.equals(txnInfo.getBookingType())){
			//按账单日计算
			Date preAdDate = null;
			if(PrepaymentCalMethod.B.equals(context.getLoanFeeDef().prepaymentCalMethod)){
				//实际还款日为下一账单日-预约还款扣款提前天数
				preAdDate = DateUtils.addDays(context.getAccount().getNextStmtDate(),-context.getLoanFeeDef().prepayApplyCutDay);
			}
			//按预约日计算
			else if(PrepaymentCalMethod.A.equals(context.getLoanFeeDef().prepaymentCalMethod)){
				//实际还款日为预约日
				preAdDate = txnInfo.getBookingDate();
			}
			
			//查询贷款信息注册表  有则更新 无则新建
			if(ccsLoanReg==null){
				try {
					//初始化预约注册信息
					ccsLoanReg=appapiCommService.genLoanReg(0,BigDecimal.ZERO, 
							loan.getRefNbr(), loan.getCardNbr(), 
							loan.getCardNbr(), loan.getAcctNbr(), loan.getAcctType(),
							null,txnInfo.getBizDate());
					ccsLoanReg.setPreAdDate(preAdDate);
					ccsLoanReg.setPreAdAmt(trialResp.getTotalAMT());
					//赋值账户信息	
					setCcsLoanReg(ccsLoanReg,context.getLoan());
					//保存信息
				} catch (ProcessException e) {
					throw new ProcessException(MsRespCode.E_1044.getCode(), MsRespCode.E_1044.getMessage());
				}
			}else{
				ccsLoanReg.setRegisterDate(txnInfo.getBizDate());
				ccsLoanReg.setPreAdDate(preAdDate);
				ccsLoanReg.setPreAdAmt(trialResp.getTotalAMT());
			}
			context.setLoanReg(ccsLoanReg);
			rCcsLoanReg.save(ccsLoanReg);
		}
		//试算添加MemoCr,MemoDb的计算
		else if(BOOKING_TYPE_1.equals(txnInfo.getBookingType())){
			context.setLoanReg(new CcsLoanReg());
		}
	}
	/**
	 * 计算预约结清金额
	 * @param context
	 * @return
	 */
	private TrialResp computBookingAmt(TxnContext context) {
		if(log.isDebugEnabled())
			log.debug("计算预约结清金额--computBookingAmt");
		TxnInfo txnInfo = context.getTxnInfo();
		
		//查询信用计划列表
		List<CcsPlan> ccsPlans = appapiCommService.loadPlansByAcct(context, true);
		//调用试算方法得到试算金额
		TrialResp trialResp = new TrialResp();
		try {
			Date preAdDate = null;
			if(PrepaymentCalMethod.B.equals(context.getLoanFeeDef().prepaymentCalMethod)){
				//实际还款日为下一账单日-预约还款扣款提前天数
				preAdDate = DateUtils.addDays(context.getAccount().getNextStmtDate(),-context.getLoanFeeDef().prepayApplyCutDay);
			}
			//按预约日计算
			else if(PrepaymentCalMethod.A.equals(context.getLoanFeeDef().prepaymentCalMethod)){
				//实际还款日为预约日
				preAdDate = txnInfo.getBookingDate();
			}
			trialResp=loanProvideImpl.mCLoanTodaySettlement(context.getLoan(), txnInfo.getBizDate(), preAdDate, LoanUsage.M, trialResp,ccsPlans,context.getLoanReg());
		} catch (Exception e) {
			if(log.isErrorEnabled())
				log.error(e.getMessage(), e);
			throw new ProcessException(MsRespCode.E_1026.getCode(), MsRespCode.E_1026.getMessage());
		}
		
		LogTools.printObj(log, trialResp, "试算金额trialResp");
		//提前还款金额不能<=0
//		if(trialResp.getTotalAMT().compareTo(BigDecimal.ZERO)<=0){
//			throw new ProcessException(MsRespCode.E_1026.getCode(), MsRespCode.E_1026.getMessage());
//		}
		return trialResp;
	}

	/**
	 * 检查业务逻辑
	 * @param context
	 */
	private void check(TxnContext context) {
		if(log.isDebugEnabled())
			log.debug("检查业务逻辑--check");
		TxnInfo txnInfo = context.getTxnInfo();
		CcsAcctO accounto = context.getAccounto();
		CcsAcct ccsAcct =context.getAccount();
		CcsLoan loan = context.getLoan();
		LoanFeeDef lfd = context.getLoanFeeDef();
		Mulct mulct = unifiedParameterFacility.retrieveParameterObject(lfd.mulctTableId, Mulct.class);
		
//		AccountAttribute acctAttr = context.getAccAttr();
		
		LogTools.printObj(log, accounto.getBlockCode(), "账户锁定码");
		// 账户锁定码包含"P"
		if (StringUtils.isNotEmpty(accounto.getBlockCode()) && accounto.getBlockCode().toUpperCase().indexOf("P") > -1) {
			throw new ProcessException(MsRespCode.E_1014.getCode(), MsRespCode.E_1014.getMessage());
		}
		//该产品不支持提前还款
		if(!Indicator.Y.equals(lfd.appointEarlySettleEnable)){
			 throw new ProcessException(MsRespCode.E_1069.getCode(), MsRespCode.E_1069.getMessage());
		}
		if(log.isDebugEnabled())
			log.debug("当前日期为[{}]，预约还款日[{}],预约提前结清提前天数[{}]",txnInfo.getBizDate(),txnInfo.getBookingDate(),lfd.appointEarlySettleDate.intValue());
        
		//判断是否逾期
		if(log.isDebugEnabled())
			log.debug("逾期开始日期为[{}]",loan.getOverdueDate());
			log.debug("CPD起始日期为[{}]",loan.getCpdBeginDate());
		if(mulct!=null){
			if(mulct.mulctMethod == MulctMethod.DPD && null != loan.getOverdueDate()){
				throw new ProcessException(MsRespCode.E_1025.getCode(), MsRespCode.E_1025.getMessage());
			}
			else if(mulct.mulctMethod == MulctMethod.CPD && null != loan.getCpdBeginDate()){
				throw new ProcessException(MsRespCode.E_1025.getCode(), MsRespCode.E_1025.getMessage());
			}
		}
		//预约还款日<下一账单日
		if(log.isDebugEnabled())
			log.debug("下一账单日期[{}]",ccsAcct.getNextStmtDate());
		//提前还款计算方式未配置
		if(null==lfd.prepaymentCalMethod){
			throw new ProcessException(MsRespCode.E_1070.getCode(),MsRespCode.E_1070.getMessage().replace("N","提前还款计算方式"));
		}
		//按账单日计算：预约还款日<账单日-预约还款提前扣款天数
		//--按账单日计算无须考虑预约日--lsy20160112
//		if(PrepaymentCalMethod.B.equals(lfd.prepaymentCalMethod)&&DateUtils.getIntervalDays(txnInfo.getBookingDate(),ccsAcct.getNextStmtDate())<lfd.prepayApplyCutDay){
//			throw new ProcessException(MsRespCode.E_1067.getCode(),MsRespCode.E_1067.getMessage().replace("N",lfd.prepayApplyCutDay.intValue()+""));
//		}
		//未配置预约日
		if (PrepaymentCalMethod.A.equals(lfd.prepaymentCalMethod)&&null==txnInfo.getBookingDate()){
			throw new ProcessException(MsRespCode.E_1078.getCode(), MsRespCode.E_1078.getMessage());
		}
		//按预约日计算：账单日<预约日+预约还款提前天数
//		if (PrepaymentCalMethod.A.equals(lfd.prepaymentCalMethod)
//				&& DateUtils.getIntervalDays(txnInfo.getBookingDate(), ccsAcct.getNextStmtDate()) < lfd.appointEarlySettleDate) {
//			throw new ProcessException(MsRespCode.E_1067.getCode(), MsRespCode.E_1067.getMessage()
//					.replace("N",lfd.appointEarlySettleDate.intValue() + ""));
//		}
		//预约类型=账单日
		if(PrepaymentCalMethod.B.equals(lfd.prepaymentCalMethod)){
			//预约日=账单日时
			if(lfd.prepayApplyCutDay.intValue()==0){
				throw new ProcessException(MsRespCode.E_1070.getCode(),MsRespCode.E_1070.getMessage());				
			}
			//账单日>=当前日期+预约还款提前天数
			if(DateUtils.getIntervalDays(txnInfo.getBizDate(), ccsAcct.getNextStmtDate())<lfd.appointEarlySettleDate.intValue()){
				throw new ProcessException(MsRespCode.E_1067.getCode(), MsRespCode.E_1067.getMessage().replace("N",lfd.appointEarlySettleDate.intValue()+""));				
			}
			//账单日>=当前日期+提前还款申请扣款提前天数-->防止参数配置错误：预约还款提前天数应该大于扣款提前天数
			if(DateUtils.getIntervalDays(txnInfo.getBizDate(), ccsAcct.getNextStmtDate())<lfd.prepayApplyCutDay.intValue()){
				throw new ProcessException(MsRespCode.E_1070.getCode(), MsRespCode.E_1070.getMessage());				
			}
		}
		if (PrepaymentCalMethod.A.equals(lfd.prepaymentCalMethod)){
			//预约还款日>今天
			if(DateUtils.getIntervalDays(txnInfo.getBizDate(),txnInfo.getBookingDate())<1){
				 throw new ProcessException(MsRespCode.E_1021.getCode(), MsRespCode.E_1021.getMessage().replace("N",1+""));
			}
			if(DateUtils.getIntervalDays(txnInfo.getBookingDate(),ccsAcct.getNextStmtDate()) <= 0){
				 throw new ProcessException(MsRespCode.E_1022.getCode(),MsRespCode.E_1022.getMessage()+ccsAcct.getNextStmtDate());
			}
			//申请日须在账单日前N天 
			if(DateUtils.getIntervalDays(txnInfo.getBizDate(),ccsAcct.getNextStmtDate())<lfd.appointEarlySettleDate.intValue()){
				throw new ProcessException(MsRespCode.E_1067.getCode(), MsRespCode.E_1067.getMessage().replace("N",lfd.appointEarlySettleDate.intValue()+""));
			}
		}
		if(this.existLoanRegOfDueBillNo(txnInfo, LoanAction.T)){
			throw new ProcessException(MsRespCode.E_1087.getCode(),MsRespCode.E_1087.getMessage());
		}
		appapiCommService.checkWaitCreditOrder(context);
	}
	
	/**
	 * 获取产品参数
	 * @param context
	 */
	private void getProdParam(TxnContext context){
		if(log.isDebugEnabled())
			log.debug("获取产品参数--getProdParam");
		String productCd = context.getAccount().getProductCd();
		CcsLoan loan = context.getLoan();
		try{
			ProductCredit productCredit = unifiedParameterFacility.loadParameter(productCd, ProductCredit.class);
		    Product product = unifiedParameterFacility.loadParameter(productCd, Product.class);
		    AccountAttribute acctAttr = unifiedParameterFacility.loadParameter(
		    		productCredit.accountAttributeId, AccountAttribute.class);
		    LoanPlan loanPlan = null;
		    LoanFeeDef loanFeeDef= null;
		    //阳光产品
		    if(InputSource.SUNS.equals(context.getMsRequestInfo().getInputSource())){
//		    	loanPlan = unifiedParamFacilityProvide.loanPlan(productCd, LoanType.MCEI);
//			    loanFeeDef = unifiedParamFacilityProvide.loanFeeDef(productCd, LoanType.MCEI, Integer.valueOf(loan.getLoanInitTerm()));
		    	throw new ProcessException(MsRespCode.E_1004.getCode(), MsRespCode.E_1004.getMessage());
		    } else if(loan.getLoanType()==LoanType.MCAT){
		    	//
		    	throw new ProcessException(MsRespCode.E_1004.getCode(), MsRespCode.E_1004.getMessage());
//		    	loanPlan = unifiedParamFacilityProvide.loanPlan(productCredit.loanPlansMap.get(productCredit.defaultLoanType));
//		    	loanFeeDef = unifiedParamFacilityProvide.loanFeeDefMCAT(loanPlan.loanCode);
		    }else{
		    	//马上贷产品  20151127
		    	loanPlan = unifiedParamFacilityProvide.loanPlan(productCredit.loanPlansMap.get(productCredit.defaultLoanType));
			    if(StringUtils.isNotBlank(loan.getLoanFeeDefId())){
			    	loanFeeDef = unifiedParamFacilityProvide.loanFeeDefByKey(productCd,loan.getLoanType() , Integer.valueOf(loan.getLoanFeeDefId()));
			    }else{
			    	loanFeeDef = unifiedParamFacilityProvide.loanFeeDef(loanPlan.loanCode,Integer.valueOf(loan.getLoanInitTerm()),loan.getLoanInitPrin());
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
		    log.debug("accountAttributeId --------- " + productCredit.accountAttributeId);
		}catch(Exception e){
			if(log.isErrorEnabled())
				log.error("获取产品参数异常,产品号[{"+productCd+"}]",e);
			throw new ProcessException(MsRespCode.E_1052.getCode(), MsRespCode.E_1052.getMessage());
		}

	}
	
	/**
	 * 初始化交易中间信息
	 * @param context
	 * @param req
	 */
	public void initTxnInfo(TxnContext context,TNMLBookingReq req){
		TxnInfo txnInfo = context.getTxnInfo();
		txnInfo.setBizDate(req.getBizDate());
		txnInfo.setSysOlTime(new Date());
		txnInfo.setRequestTime(req.getRequestTime());
		txnInfo.setServiceSn(req.getServiceSn());
		txnInfo.setRefNbr(txnUtils.genRefnbr(req.getBizDate(), req.getServiceSn()));
		txnInfo.setBookingDate(req.getCaldate());//预约日期
		txnInfo.setBookingType(req.getType());//预约类型 1-试算 2-申请
		txnInfo.setInputSource(req.getInputSource());
		txnInfo.setAcqId(req.getAcqId());
		txnInfo.setSubTerminalType(req.getSubTerminalType());
		txnInfo.setDueBillNo(req.dueBillNo);
		txnInfo.setContrNo(req.getContrNbr());
		
		if(log.isDebugEnabled())
			log.debug("业务日期：[{}]",txnInfo.getBizDate());
		LogTools.printObj(log, txnInfo, "交易中间信息txnInfo");
		
		if(!StringUtils.equals(BOOKING_TYPE_1, req.getType()) 
				&& !StringUtils.equals(BOOKING_TYPE_2, req.getType())){
			throw new ProcessException(MsRespCode.E_1043.getCode(),MsRespCode.E_1043 .getMessage()+ ",字段名称{" +req.getType()+ "} 1-试算  2-申请");
		}
	}
	
	/**
	 * 设置预约注册信息
	 * @param ccsloanReg
	 * @param loan
	 */
	public void setCcsLoanReg(CcsLoanReg ccsloanReg,CcsLoan loan){
		ccsloanReg.setOrg(loan.getOrg());
		ccsloanReg.setAcctNbr(loan.getAcctNbr());
		ccsloanReg.setAcctType(loan.getAcctType());
		ccsloanReg.setRequestTime(new Date());
		ccsloanReg.setLogicCardNbr(loan.getLogicCardNbr());//LoanRegStatus
		ccsloanReg.setCardNbr(loan.getCardNbr());
		ccsloanReg.setLoanRegStatus(LoanRegStatus.A);
		ccsloanReg.setLoanAction(LoanAction.O);//GUARANTY_ID
		ccsloanReg.setDueBillNo(loan.getDueBillNo());
		ccsloanReg.setContrNbr(loan.getContrNbr());
		ccsloanReg.setGuarantyId(loan.getGuarantyId());		
		ccsloanReg.setLoanCode(loan.getLoanCode());
		ccsloanReg.setPremiumAmt(loan.getPremiumAmt());
		ccsloanReg.setPremiumInd(loan.getPremiumInd());
	}

	/**
	 * 发送通知短信
	 */
	public void sendSms(String org,CcsAcct acct,CcsLoan loan,Date date){
		if(log.isDebugEnabled())
			log.debug("发送短信通知...");
		try{
			SMSentitySendReq req = new SMSentitySendReq();
			
			req.setMobileNumber(acct.getMobileNo());
			req.setSourceBizSystem(Constants.SOURCE_BIZ_SYSTEM);
			req.setSourceBizType(Constants.SOURCE_BIZ_SYSTEM + "-" + 
					acct.getAcqId() + "-" +
					loan.getLoanCode() + "-" +
					Constants.PREPAYMENT_BOOKING_SUCCESS);
			
			SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd");
			String dateStr = dateFormatter.format(new Date());
			String acctNbrStr = String.format("%016d", acct.getAcctNbr());
			String randomStr = String.format("%08d", new Random().nextInt(99999999));
			req.setSourceSerialNumber(dateStr + acctNbrStr + randomStr);
			
			//短信参数内容
			//尊敬的{}，您的提前还款申请成功，温馨提醒您的最晚还款日期为{}年{}月{}日
			Calendar calendar = GregorianCalendar.getInstance();
			calendar.setTime(date);
			req.setMsgParam(new StringBuffer().append(acct.getName())
					.append(Constants.ONLINE_SMS_SEPARATOR)
					.append(calendar.get(Calendar.YEAR))
					.append(Constants.ONLINE_SMS_SEPARATOR)
					.append(calendar.get(Calendar.MONTH)+1)
					.append(Constants.ONLINE_SMS_SEPARATOR)
					.append(calendar.get(Calendar.DAY_OF_MONTH)).toString());
	
			msgFacility.sendSingleSms(org, req);
		}catch(Exception e){
			if(log.isDebugEnabled())
				log.debug("发送短信异常，跳过处理");
		}
	}
	
	/**
	 * 获取贷款注册信息
	 * @param txnInfo
	 * @param loanAction
	 * @return
	 */
	private Boolean existLoanRegOfDueBillNo(TxnInfo txnInfo,LoanAction... loanAction) {
		if(log.isDebugEnabled())
			log.debug("加载贷款注册信息--内部loadLoanReg");
		try {
			QCcsLoanReg qCcsLoanReg = QCcsLoanReg.ccsLoanReg;
			CcsLoanReg loanReg = null;
			BooleanExpression expression = qCcsLoanReg.dueBillNo.eq(txnInfo.getDueBillNo());
			if(null != loanAction){
				expression = expression.and(qCcsLoanReg.loanAction.in(loanAction));
			}
			if(null != txnInfo.getContrNo()){
				expression = expression.and(qCcsLoanReg.contrNbr.eq(txnInfo.getContrNo()));
			}
			
			loanReg = rCcsLoanReg.findOne(expression);
			if(loanReg!=null){
				return true;
			}else return false;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new ProcessException(MsRespCode.E_1011.getCode(), MsRespCode.E_1011.getMessage());
		}

	}
}
