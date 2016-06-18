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
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.facility.TxnUtils;
import com.sunline.ccs.facility.order.OrderFacility;
import com.sunline.ccs.facility.order.PaymentFacility;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsOrder;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.param.def.enums.AuthTransDirection;
import com.sunline.ccs.param.def.enums.TransAmtDirection;
import com.sunline.ccs.service.context.TxnContext;
import com.sunline.ccs.service.context.TxnInfo;
import com.sunline.ccs.service.handler.AppapiCommService;
import com.sunline.ccs.service.handler.OpenAcctCommService;
import com.sunline.ccs.service.msentity.MsRequestInfo;
import com.sunline.ccs.service.msentity.MsResponseInfo;
import com.sunline.ccs.service.msentity.TFDCommodyLoanWithDrawReq;
import com.sunline.ccs.service.msentity.TFDCommodyLoanWithDrawResp;
import com.sunline.ccs.service.noticetemplate.SMSLoanDeductions;
import com.sunline.ccs.service.payEntity.CommResp;
import com.sunline.ccs.service.payEntity.PaySinPaymentResp;
import com.sunline.ppy.dictionary.enums.AuthTransStatus;
import com.sunline.ppy.dictionary.enums.AuthTransType;
import com.sunline.ppy.dictionary.enums.LoanMold;
import com.sunline.ppy.dictionary.enums.LoanRegStatus;
import com.sunline.ppy.dictionary.enums.LoanUsage;
import com.sunline.ppy.dictionary.enums.MsPayfrontError;
import com.sunline.ppy.dictionary.enums.MsRespCode;
import com.sunline.ppy.dictionary.enums.OrderStatus;
import com.sunline.ppy.dictionary.exception.ProcessException;
/**
 * 商品贷提现
 * @author zhengjf
 *
 */
@Service
public class TFDCommodyLoanWithDraw extends AbstractHandler {

	private Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	TxnUtils txnUtils;
	@Autowired
	private AppapiCommService appapiCommService;
	@Autowired
	private SMSLoanDeductions sMSLoanDeductions;
	@Autowired
	private OpenAcctCommService openAcctCommService;
	@Autowired
	private PaymentFacility paymentFacility;
	@Autowired
	private OrderFacility orderFacility;
	
	
	@Override
	public MsResponseInfo execute(MsRequestInfo msRequestInfo) {
		LogTools.printLogger(logger, "TFDWithDrawReq", "循环现金贷提款", msRequestInfo, true);
		TFDCommodyLoanWithDrawReq req = (TFDCommodyLoanWithDrawReq) msRequestInfo;
		LogTools.printObj(logger, req, "TFDWithDrawReq请求参数");
		TFDCommodyLoanWithDrawResp resp = new TFDCommodyLoanWithDrawResp();
		TxnContext context = new TxnContext();
		TxnInfo txnInfo = new TxnInfo();
		
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
			appapiCommService.loadLoanRegList(context,null);
			appapiCommService.loadCustomerByCustId(context, true);
			
			// 确定产品参数
			openAcctCommService.getProdParam(context);
			
			this.validateForApply(context);
			
			//锁定码
			appapiCommService.checkBlockCode(context);
			
			//检查otb
			appapiCommService.valiLoanOtb(context);
			
			this.bizProc(context);

			//向通知平台发送通知		
			sMSLoanDeductions.sendSMS(context);
			
		}catch (ProcessException pe){
			if(logger.isErrorEnabled())
				logger.error(pe.getMessage(), pe);
			
			appapiCommService.preException(pe, pe, txnInfo);
			appapiCommService.saveErrorOrder(context);
		}catch (Exception e) {
			if(logger.isErrorEnabled())
				logger.error(e.getMessage(), e);
			ProcessException pe = new ProcessException();//e不是pe，处理逻辑与没有code的pe一样
			appapiCommService.preException(pe, pe, txnInfo);
			appapiCommService.saveErrorOrder(context);
		}finally{
			LogTools.printLogger(logger, "TFDCommodyLoanWithDrawResp", "循环现金贷提款", resp, false);
		}
		
		this.setResponse(resp,req,txnInfo);
		LogTools.printObj(logger, resp, "TFDCommodyLoanWithDrawResp响应参数");
		return resp;
	}
	/**
	 * 初始化交易中间信息
	 * @param req
	 * @param txnInfo
	 */
	private void initTxnInfo(TFDCommodyLoanWithDrawReq req, TxnInfo txnInfo) {
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
		txnInfo.setLoanCode("");
		txnInfo.setMobile(req.mobile);
		txnInfo.setRefNbr(txnUtils.genRefnbr(txnInfo.getBizDate(), txnInfo.getServiceSn()));
		txnInfo.setServiceId(req.getServiceId());
		txnInfo.setMerId(req.getMerId());
		txnInfo.setAuthTxnTerminal(req.getAuthTxnTerminal());
		txnInfo.setRaId(req.getRaId());
		txnInfo.setMerchandiseAmt(req.getMerchandiseAmt());
		txnInfo.setMerchandiseOrder(req.getMerchandiseOrder());
		txnInfo.setDownPaymentAmt(req.downPaymentAmt);

		
		if(logger.isDebugEnabled())
			logger.debug("业务日期：[{}]",txnInfo.getBizDate());
		LogTools.printObj(logger, txnInfo, "txnInfo交易中间信息");
		
	}
	/**
	 * 检查报文
	 * @param req
	 */
	private void checkReq(TFDCommodyLoanWithDrawReq req,TxnInfo txnInfo) {
		
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
		if(logger.isDebugEnabled())
			logger.debug("合同放款有效期:[{}],业务日期：[{}]",acct.getAcctExpireDate(),txnInfo.getBizDate());
		if(acct.getAcctExpireDate().compareTo(txnInfo.getBizDate())<0){
			throw new ProcessException(MsRespCode.E_1056.getCode(), MsRespCode.E_1056.getMessage());
		}
		if(logger.isDebugEnabled())
			logger.debug("放款类型:[{}],信用额度：[{}]",loanPlan.loanMold,acct.getCreditLmt());
		if(loanPlan.loanMold == LoanMold.S && txnInfo.getTransAmt().compareTo(acct.getCreditLmt()) != 0){
			throw new ProcessException(MsRespCode.E_1005.getCode(),MsRespCode.E_1005.getMessage()+"，单次放款必须等于账户额度");
		}
		appapiCommService.checkLoan(context);
		
	}
	/**
	 * 业务处理
	 * @param context
	 */
	@Transactional
	private void bizProc(TxnContext context) {
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
		//初始化订单
		appapiCommService.installOrder(context);
		
		appapiCommService.saveCcsAuthmemoO(context);
		
		appapiCommService.loadAuthmemoLogKv(context, context.getTxnInfo().getLogKv(),false);
		
		appapiCommService.saveMerchandiseOrder(context);
		
		loanProcCommodity(context);
		
		//保存交易数据
		appapiCommService.mergeProc(context);
	}

	/**
	 * 组装响应报文
	 * @param resp
	 * @param req
	 * @param txnInfo
	 */
	private void setResponse(TFDCommodyLoanWithDrawResp resp, TFDCommodyLoanWithDrawReq req, TxnInfo txnInfo) {
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
	 * 更新联机账户信息
	 * @param context
	 */
	public void loanProcCommodity(TxnContext context){
		CcsOrder order = context.getOrder();
		TxnInfo txnInfo = context.getTxnInfo();
		CcsLoanReg loanReg = context.getLoanReg();
		CcsAcctO accto = context.getAccounto();
		PaySinPaymentResp data = new PaySinPaymentResp();
		CommResp mainResp = new CommResp();
		
		mainResp.setCode(MsPayfrontError.S_0.getRespCode());
		mainResp.setData(MsPayfrontError.S_0.getDesc());
		try {
			//公共报文响应码检查
			txnInfo.setResponsCode(MsPayfrontError.S_0.getRespCode());
			txnInfo.setResponsDesc(MsPayfrontError.S_0.getDesc());
			
			// 设置支付前置返回码，报文体响应码覆盖报文头响应码,用于设置订单状态
//			txnInfo.setPayRespCode(mainResp.getCode());
//			txnInfo.setPayRespMessage(mainResp.getData().toString());
//			txnInfo.setPayStatus(MsPayfrontError.S_0.get);
			
			orderFacility.updateOrder(order,mainResp, data.getStatus(),OrderStatus.S, txnInfo.getLogKv(),txnInfo.getBizDate(),MsRespCode.E_0000);
			//更新账单日
			appapiCommService.updateAcct(context);
			appapiCommService.updateAcctO(context);
			appapiCommService.updateLoanReg(context, loanReg,LoanRegStatus.S);
			appapiCommService.updateAcctoMemoAmt(accto, txnInfo,true);
			appapiCommService.updateAuthmemo(context, AuthTransStatus.N);
			appapiCommService.savePosting(txnInfo);// 入账记录
			
		} catch(ProcessException pe){
			if(logger.isErrorEnabled())
				logger.error(pe.getMessage(), pe);
			throw pe;
		}
	}
}
