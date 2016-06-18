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

import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.facility.TxnUtils;
import com.sunline.ccs.facility.order.PaymentFacility;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.loan.McLoanProvideImpl;
import com.sunline.ccs.param.def.enums.AuthTransDirection;
import com.sunline.ccs.param.def.enums.TransAmtDirection;
import com.sunline.ccs.service.context.LoanInfo;
import com.sunline.ccs.service.context.TxnContext;
import com.sunline.ccs.service.context.TxnInfo;
import com.sunline.ccs.service.handler.AppapiCommService;
import com.sunline.ccs.service.handler.collection.CollectionLogic;
import com.sunline.ccs.service.msentity.MsRequestInfo;
import com.sunline.ccs.service.msentity.MsResponseInfo;
import com.sunline.ccs.service.msentity.TFCRepayReq;
import com.sunline.ccs.service.msentity.TFCRepayResp;
import com.sunline.ccs.service.noticetemplate.SMSLoanDeductions;
import com.sunline.ppy.dictionary.enums.AuthTransType;
import com.sunline.ppy.dictionary.enums.LoanRegStatus;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.LoanUsage;
import com.sunline.ppy.dictionary.enums.MsRespCode;
import com.sunline.ppy.dictionary.exception.ProcessException;
/**
 * 随借随还主动还款
 * @author wangz
 *
 */
@Service
public class TFCRepayHandler extends AbstractHandler {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private AppapiCommService appapiCommService;
	@Autowired
	McLoanProvideImpl mcLoanProvideImpl;
	@Autowired
	private PaymentFacility paymentFacility;
	@Autowired
	SMSLoanDeductions smsLoanDeductions; 
	@Autowired
	CollectionLogic collectionLogic;
	@Autowired
	TxnUtils txnUtils;
	
	public MsResponseInfo execute(MsRequestInfo msRequestInfo) throws ProcessException {
		LogTools.printLogger(logger, "TFCRepayReq", "实时代扣", msRequestInfo, true);
		TFCRepayReq req = (TFCRepayReq) msRequestInfo;
		LogTools.printObj(logger, req, "请求参数TFCRepayReq");
		TFCRepayResp resp = new TFCRepayResp();

		TxnContext context = new TxnContext();
		TxnInfo txnInfo = new TxnInfo();
		LoanInfo loanInfo = new LoanInfo();
		context.setTxnInfo(txnInfo);
		context.setLoanInfo(loanInfo);
		context.setMsRequestInfo(req);

		try {
			this.initTxnInfo(context, req);
			
			this.checkReq(context);
			// 重复交易
			appapiCommService.checkRepeatTxn(context);
			
			appapiCommService.loadAcctByContrNo(context, true);
			appapiCommService.loadAcctOByAcct(context, true);
			appapiCommService.loadCardOByAcct(context, true);
			appapiCommService.loadCustomerByCustId(context, true);
			appapiCommService.loadLoanByContrNo(context,null, false);
			appapiCommService.loadLoanRegList(context,LoanType.MCAT);
			

			// 检查在途订单  暂不拦截
			appapiCommService.checkWaitCreditOrder(context);

			// 检查锁定码
			appapiCommService.checkBlockCode(context);
			
			// 
			this.valiBiz(context);

			// 业务处理，单独业务处理，保证业务数据完整
			this.bizProc(context);
			
			//处理结果，单独事务处理
			this.payResultProc(context);
			
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
			LogTools.printLogger(logger, "TFCRepayResp", "实时代扣", resp, false);
		}

		this.setResponse(resp, req, txnInfo);
		LogTools.printObj(logger, resp, "响应参数TFCRepayResp");
		return resp;
	}

	/**
	 * @param context
	 */
	private void checkReq(TxnContext context) {
		TFCRepayReq req = (TFCRepayReq) context.getMsRequestInfo();
		
		if(req.amount.compareTo(BigDecimal.ZERO)<=0){
			throw new ProcessException(MsRespCode.E_1043.getCode(),MsRespCode.E_1043 .getMessage()+ ",字段名称{AMOUNT},必须大于0");
		}
	}

	/**
	 * @param context
	 */
	private void valiBiz(TxnContext context) {
		TxnInfo txnInfo = context.getTxnInfo();
		CcsAcctO accto = context.getAccounto();
		CcsLoan loan = context.getLoan();
		List<CcsLoanReg> regList = context.getRegList();
//		List<CcsPlan> planList = appapiCommService.loadPlansByAcct(context, true);
		BigDecimal payment = new BigDecimal(0);//已账户余额
		BigDecimal succsAmt = new BigDecimal(0);//当天成功的放款金额
		int succsCount = 0;
		
		if(null != regList && regList.size() > 0){
			for (CcsLoanReg reg : regList) {
				if(reg.getLoanRegStatus() == LoanRegStatus.S){
					succsAmt = succsAmt.add(reg.getLoanInitPrin());
					succsCount++;
					txnInfo.setDueBillNo(reg.getDueBillNo());//设置借据号
				}
			}
		}
		
		//必须有loan或成功的loanReg
		if(null != loan){
			if(loan.getLoanType() != LoanType.MCAT){
				if (logger.isDebugEnabled())
					logger.debug("分期类型--[{}]",context.getLoan().getLoanType());
				throw new ProcessException(MsRespCode.E_1062.getCode(), MsRespCode.E_1062.getMessage());
			}
			
			//逾期扣款
//			if(null != loan.getOverdueDate()){
//				txnInfo.setLoanUsage(LoanUsage.O);
//			}
//			appapiCommService.checkLoan(context);  马上贷和随机随还不因借据状态改变而不能还款
		}else if(succsCount == 0){
			throw new ProcessException(MsRespCode.E_1015.getCode(), MsRespCode.E_1015.getMessage());
		}
		
//		if(null != planList && planList.size() > 0){
//			for (CcsPlan ccsPlan : planList) {
//				payment = payment.add(ccsPlan.getCurrBal());
//				if(ccsPlan.getPlanType() == PlanType.J){
//					payment.add(ccsPlan.getNodefbnpIntAcru());//非延迟利息
//				}
//			}
//		}
		
		//应还款金额=accto账户余额(不包含非延迟利息)-accto未入账贷记金额+loanReg所有放款成功的金额
		//问题1.没有包含非延迟利息
		//问题2.不包含挂账的借记交易金额
		//问题3.包含挂账的贷记交易金额
		payment = (accto.getCurrBal()==null?BigDecimal.ZERO:accto.getCurrBal())
				.subtract(accto.getMemoCr()==null?BigDecimal.ZERO:accto.getMemoCr())
				.add(succsAmt);//loanReg所有放款成功的金额
//				.add(accto.getMemoDb()==null?BigDecimal.ZERO:accto.getMemoDb());
		
		if (logger.isDebugEnabled())
			logger.debug("应还款金额--[{}],交易金额[{}]",payment,txnInfo.getTransAmt());
		
		if(payment.compareTo(txnInfo.getTransAmt())<0){
			throw new ProcessException(MsRespCode.E_1060.getCode(), MsRespCode.E_1060.getMessage());
		}
		
//		if(null != acct.getDdDate() && acct.getDdDate().compareTo(txnInfo.getBizDate())== 0){
//			throw new ProcessException(MsRespCode.E_1064.getCode(), MsRespCode.E_1064.getMessage());
//		}
		
	}

	/**
	 * 组装响应报文
	 * 
	 * @param resp
	 * @param req
	 * @param txnInfo
	 */
	private void setResponse(TFCRepayResp resp, TFCRepayReq req, TxnInfo txnInfo) {
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

	/**
	 * 单独事务处理业务逻辑，保证订单等基础信息能保存完整
	 * 
	 * @param context
	 * @param payJson
	 * @return
	 */
	@Transactional
	private void bizProc(TxnContext context) {
		if (logger.isDebugEnabled())
			logger.debug("单独事务处理业务逻辑--bizProc");
		// 调用支付
		appapiCommService.installOrder(context);
	}

	/**
	 * 初始化中间交易信息
	 * 
	 * @param context
	 */
	private void initTxnInfo(TxnContext context, TFCRepayReq req) {
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
		txnInfo.setContrNo(req.contractNo);
		txnInfo.setTransAmt(req.amount);
		txnInfo.setMobile(req.mobile);
		txnInfo.setLoanUsage(LoanUsage.N);
//		txnInfo.setRefNbr(req.getServiceSn());
		txnInfo.setRefNbr(txnUtils.genRefnbr(txnInfo.getBizDate(), txnInfo.getServiceSn()));
		txnInfo.setOrg(req.getOrg());
		txnInfo.setServiceId(req.getServiceId());

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
	public void payResultProc(TxnContext context) {
		TxnInfo txnInfo = context.getTxnInfo();
		try{
			//组装支付指令
			String payJson = paymentFacility.installPayCutPaymentCommand(context.getOrder());
			
			if(logger.isDebugEnabled())
				logger.debug("独立事务，处理支付结果--payResultProc,支付报文：[{}]",payJson);
			appapiCommService.loadAcctOByAcct(context, true);
			appapiCommService.loadAcctByContrNo(context, true);

			appapiCommService.paymentProc(context,payJson);
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

}
