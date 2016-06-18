package com.sunline.ccs.service.handler.sunshine;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.facility.TxnUtils;
import com.sunline.ccs.facility.order.PaymentFacility;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.CcsRepaySchedule;
import com.sunline.ccs.loan.McLoanProvideImpl;
import com.sunline.ccs.loan.TrialResp;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.enums.AuthTransDirection;
import com.sunline.ccs.param.def.enums.TransAmtDirection;
import com.sunline.ccs.service.context.LoanInfo;
import com.sunline.ccs.service.context.TxnContext;
import com.sunline.ccs.service.context.TxnInfo;
import com.sunline.ccs.service.entity.S31001PaymentReq;
import com.sunline.ccs.service.entity.S31001PaymentResp;
import com.sunline.ccs.service.handler.SunshineCommService;
import com.sunline.ccs.service.util.DateTools;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.AuthTransType;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanAction;
import com.sunline.ppy.dictionary.enums.LoanRegStatus;
import com.sunline.ppy.dictionary.enums.LoanUsage;
import com.sunline.ppy.dictionary.enums.MsRespCode;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * 实时代扣
 * @author wangz
 *
 */
@Service
public class TNRPayment {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private SunshineCommService sunshineCommService;
	@Autowired
	private UnifiedParameterFacility unifiedParameterFacility;
	@Autowired
	private PaymentFacility paymentFacility;
	@Autowired
	McLoanProvideImpl mcLoanProvideImpl;
	@Autowired
	TxnUtils txnUtils;
	
	public S31001PaymentResp handler(S31001PaymentReq req) throws ProcessException {
		LogTools.printLogger(logger, "S31001PaymentReq", "实时代扣", req, true);
		LogTools.printObj(logger, req, "请求参数S31001PaymentReq");
		if(logger.isDebugEnabled())
			logger.debug("实时代扣-开始 流水号[{}]  保单号[{}]", req.getServiceSn(),req.getGuarantyid());
		S31001PaymentResp resp = new S31001PaymentResp();
		String payJson = "";
		
		TxnContext context = new TxnContext();
		TxnInfo txnInfo = new TxnInfo();
		LoanInfo loanInfo = new LoanInfo();
		context.setTxnInfo(txnInfo);
		context.setLoanInfo(loanInfo);
		context.setSunshineRequestInfo(req);
		
		try{
			this.initTxnInfo(context,req);
			//重复交易
			sunshineCommService.checkReqAndRepeatTxn(context);
			sunshineCommService.loadLoan(context, true);
			sunshineCommService.loadLoanReg(context,null, LoanRegStatus.A,LoanAction.O,false);//存在预约，且预约还款日到达，做预约实时代扣
			sunshineCommService.loadAcct(context, true);
			sunshineCommService.loadAcctO(context, true);
			sunshineCommService.loadCard(context, true);
			sunshineCommService.loadCustomer(context, true);
			
			//检查在途订单
			sunshineCommService.checkWaitCreditOrder(context);
			
			//检查锁定码
			sunshineCommService.checkBlockCode(context);
			
			//检查借据状态
			sunshineCommService.checkLoan(context);
			
			//计算扣款金额
			this.computPaymentAmt(context);
			
			//业务处理，单独业务处理，保证业务数据完整
			payJson = this.bizProc(context);

//			//发送支付指令
//			retJson = bankServiceForApsImpl.sendMsPayFront(payJson, txnInfo.getLoanUsage());
			
			//处理结果，单独事务处理
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
			LogTools.printLogger(logger, "S31001PaymentResp", "实时代扣", resp, false);
		}
		
		this.setResponse(resp,req,txnInfo);
		LogTools.printObj(logger, resp, "响应参数S31001PaymentResp");
		return resp;
	}
	
	/**
	 * 组装响应报文
	 * @param resp
	 * @param req
	 * @param txnInfo
	 */
	private void setResponse(S31001PaymentResp resp, S31001PaymentReq req, TxnInfo txnInfo) {
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
	 * 单独事务处理业务逻辑，保证订单等基础信息能保存完整
	 * @param context
	 * @param payJson
	 * @return
	 */
	@Transactional
	private String bizProc(TxnContext context) {
		if(logger.isDebugEnabled())
			logger.debug("单独事务处理业务逻辑--bizProc");
		String payJson = "";
		
		//调用支付
		sunshineCommService.installOrder(context);
		
		//组装支付指令
		payJson = paymentFacility.installPayCutPaymentCommand(context.getOrder());
		return payJson;
	}

	/**
	 * 计算扣款金额  暂时没有考虑在途
	 * 业务处理
	 * @param context
	 */
	private void computPaymentAmt(TxnContext context) {
		if(logger.isDebugEnabled())
			logger.debug("计算扣款金额--computPaymentAmt");
		CcsLoan loan = context.getLoan();
		Date overdueDate = loan.getOverdueDate();//逾期起始日期
		
		LogTools.printObj(logger, overdueDate, "逾期起始日期");
		
		//非逾期时，业务日期==还款日期,可以做实时代扣
		if(null == overdueDate){
			beforeDueDate(context);
		}else{
			overdueDate(context);
		}
		LogTools.printObj(logger, context.getTxnInfo().getTransAmt(), "扣款金额：");
		if(BigDecimal.ZERO.compareTo(context.getTxnInfo().getTransAmt()) >= 0){
			throw new ProcessException(MsRespCode.E_1005.getCode(),MsRespCode.E_1005.getMessage());
		}
	}

	/**
	 * 逾期处理
	 * @param context
	 */
	private void overdueDate(TxnContext context) {
		CcsLoan loan = context.getLoan();
		CcsAcct acct = context.getAccount();
		CcsAcctO accto = context.getAccounto();
		TxnInfo txnInfo = context.getTxnInfo();
		ProductCredit productCredit = unifiedParameterFacility.
				loadParameter(acct.getProductCd(), ProductCredit.class);
		Date overdueDate = loan.getOverdueDate();//逾期起始日期
		CcsRepaySchedule ccsRepaySchedule = null;
		BigDecimal paymentAmt = BigDecimal.ZERO;
		txnInfo.setLoanUsage(LoanUsage.O);
		
		if(logger.isDebugEnabled())
			logger.debug("实时代扣   业务日期：[{}] ---- 下一账单日期：[{}]---- 下一还款日",txnInfo.getBizDate(),acct.getNextStmtDate(),acct.getPmtDueDate());
		
		//逾期时,业务日期-loan逾期起始日期<理赔天数,可以实时代扣
		if(DateTools.daysBetween(txnInfo.getBizDate(), overdueDate) < productCredit.claimsDays){
			//逾期，还款日当天，期款+逾期金额
			if(DateTools.dateCompare(txnInfo.getBizDate(), acct.getNextStmtDate()) == 0){
				LogTools.printObj(logger, acct.getNextStmtDate(), "逾期处理 -- 到期还款日还款-下一账单日期：");
				if(logger.isDebugEnabled())
					logger.debug("逾期，还款日当天，业务日期[{}],逾期日期[{}],下一账单日[{}]",
							txnInfo.getBizDate(),overdueDate,acct.getNextStmtDate());
				ccsRepaySchedule = sunshineCommService.loadSchedule(context, true);
				paymentAmt = paymentAmt.add(ccsRepaySchedule.getLoanTermPrin()).//应还本金
						add(ccsRepaySchedule.getLoanInsuranceAmt()).//应还保险费
						add(ccsRepaySchedule.getLoanStampdutyAmt()).//应还印花税
						add(ccsRepaySchedule.getLoanTermFee()).//应还费用
						add(ccsRepaySchedule.getLoanTermInt());//应还利息
						
				if(logger.isDebugEnabled())
					logger.debug("逾期，试算还款金额-期款部分金额：[{}]",paymentAmt);
						
				paymentAmt = paymentAmt.add(mcLoanProvideImpl.genLoanBal(loan));//
			}else{
				if(logger.isDebugEnabled())
					logger.debug("逾期，非还款日当天，业务日期[{}],逾期日期[{}],下一账单日[{}]",
							txnInfo.getBizDate(),overdueDate,acct.getNextStmtDate());
				//逾期，非还款日当天，逾期金额
				paymentAmt = paymentAmt.add(mcLoanProvideImpl.genLoanBal(loan));//
			}
			
			if(logger.isDebugEnabled())
				logger.debug("逾期，试算还款金额：[{}]",paymentAmt);
			
			if(paymentAmt.compareTo(BigDecimal.ZERO)<=0){
				throw new ProcessException(MsRespCode.E_1049.getCode(),MsRespCode.E_1049.getMessage());
			}
			
			paymentAmt = paymentAmt.subtract(accto.getMemoCr());
			if(paymentAmt.compareTo(BigDecimal.ZERO)<=0){
				throw new ProcessException(MsRespCode.E_1049.getCode(),MsRespCode.E_1049.getMessage());
			}
		}else{
			throw new ProcessException(MsRespCode.E_1020.getCode(),MsRespCode.E_1020.getMessage()+productCredit.claimsDays);
		}
		
		txnInfo.setTransAmt(paymentAmt);
	}

	/**
	 * 非逾期处理
	 * @param context
	 */
	private void beforeDueDate(TxnContext context) {
		
		CcsLoanReg loanReg = context.getLoanReg();
		CcsLoan loan = context.getLoan();
		CcsAcct acct = context.getAccount();
		CcsAcctO accto = context.getAccounto();
		TxnInfo txnInfo = context.getTxnInfo();
		BigDecimal paymentAmt = BigDecimal.ZERO;
		TrialResp trialResp = new TrialResp();
		CcsRepaySchedule ccsRepaySchedule;
		List<CcsPlan> plans = sunshineCommService.loadPlans(context, true);
		
		if(logger.isDebugEnabled())
			logger.debug("实时代扣   业务日期：[{}] ---- 下一账单日期：[{}]---- 下一还款日",txnInfo.getBizDate(),acct.getNextStmtDate(),acct.getPmtDueDate());
		
		if(DateTools.dateCompare(txnInfo.getBizDate(), acct.getNextStmtDate()) == 0){
			LogTools.printObj(logger, acct.getNextStmtDate(), "非逾期处理-正常扣款，下一账单日");
			
			txnInfo.setLoanUsage(LoanUsage.N);
			ccsRepaySchedule = sunshineCommService.loadSchedule(context, true);
			paymentAmt = paymentAmt.add(ccsRepaySchedule.getLoanTermPrin()).//应还本金
					add(ccsRepaySchedule.getLoanInsuranceAmt()).//应还保险费
//					add(ccsRepaySchedule.getLoanStampdutyAmt()).//应还印花税 不收印花税
					add(ccsRepaySchedule.getLoanTermFee()).//应还费用
					add(ccsRepaySchedule.getLoanTermInt());//应还利息
			
			if(logger.isDebugEnabled())
				logger.debug("正常扣款，试算还款金额：[{}]",paymentAmt);
			
			if(paymentAmt.compareTo(BigDecimal.ZERO)<=0){
				throw new ProcessException(MsRespCode.E_1050.getCode(),MsRespCode.E_1050.getMessage());
			}
			
			paymentAmt = paymentAmt.subtract(accto.getMemoCr());
			if(paymentAmt.compareTo(BigDecimal.ZERO)<=0){
				throw new ProcessException(MsRespCode.E_1050.getCode(),MsRespCode.E_1050.getMessage());
			}
			
		}else if(null != loanReg && DateTools.dateCompare(txnInfo.getBizDate(), loanReg.getPreAdDate()) == 0){
			LogTools.printObj(logger, loanReg.getPreAdDate(), "非逾期处理-预约提前结清扣款 ，预约还款日期：");
			
			txnInfo.setLoanUsage(LoanUsage.M);
			//提前还款预约日,可以做实时代扣
			txnInfo.setBookingDate(true);
			
			//已经做过提前预约结清
			if(null != loanReg.getDdRspFlag() && loanReg.getDdRspFlag() == Indicator.Y){
				throw new ProcessException(MsRespCode.E_1051.getCode(),MsRespCode.E_1051.getMessage());
			}
			
			try{
				trialResp = mcLoanProvideImpl.mCLoanTodaySettlement(loan,loanReg.getRegisterDate(), txnInfo.getBizDate(), LoanUsage.M, trialResp, plans,null);
			}catch(Exception e){
				logger.error(e.getMessage(), e);
				throw new ProcessException(MsRespCode.E_1026.getCode(),MsRespCode.E_1026.getMessage());
			}
			paymentAmt = trialResp.getTotalAMT();
			
			if(logger.isDebugEnabled())
				logger.debug("提前结清，试算还款金额：[{}]",paymentAmt);
			
			if(paymentAmt.compareTo(BigDecimal.ZERO)<=0){
				throw new ProcessException(MsRespCode.E_1051.getCode(),MsRespCode.E_1051.getMessage());
			}
			
			paymentAmt = paymentAmt.subtract(accto.getMemoCr());
			if(paymentAmt.compareTo(BigDecimal.ZERO)<=0){
				throw new ProcessException(MsRespCode.E_1051.getCode(),MsRespCode.E_1051.getMessage());
			}
		}else {
			throw new ProcessException(MsRespCode.E_1024.getCode(),MsRespCode.E_1024.getMessage());
		}
		
		txnInfo.setTransAmt(paymentAmt);
	}
	
	/**
	 * 初始化中间交易信息
	 * @param context
	 */
	private void initTxnInfo(TxnContext context,S31001PaymentReq req){
		TxnInfo txnInfo = context.getTxnInfo();
		txnInfo.setBizDate(req.getBizDate());
		txnInfo.setTransDirection(AuthTransDirection.Normal);
		txnInfo.setTransType(AuthTransType.AgentCredit);
		txnInfo.setSysOlTime(new Date());
		txnInfo.setRequestTime(req.getRequestTime());
		txnInfo.setServiceSn(req.getServiceSn());
		txnInfo.setMti("9900");
		txnInfo.setProcCode("900001");
		txnInfo.setGuarantyid(req.getGuarantyid());
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
}
