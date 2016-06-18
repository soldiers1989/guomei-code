package com.sunline.ccs.service.handler;

import java.math.BigDecimal;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sunline.ark.support.utils.DateUtils;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.facility.TxnUtils;
import com.sunline.ccs.facility.order.OrderFacility;
import com.sunline.ccs.infrastructure.server.repos.RCcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsMerchandiseOrder;
import com.sunline.ccs.param.def.enums.AuthTransDirection;
import com.sunline.ccs.param.def.enums.TransAmtDirection;
import com.sunline.ccs.service.context.TxnContext;
import com.sunline.ccs.service.context.TxnInfo;
import com.sunline.ccs.service.msentity.TFCRefundReq;
import com.sunline.ccs.service.payEntity.CommResp;
import com.sunline.ppy.dictionary.enums.AuthTransStatus;
import com.sunline.ppy.dictionary.enums.AuthTransType;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanAction;
import com.sunline.ppy.dictionary.enums.LoanRegStatus;
import com.sunline.ppy.dictionary.enums.LoanUsage;
import com.sunline.ppy.dictionary.enums.MsPayfrontError;
import com.sunline.ppy.dictionary.enums.MsRespCode;
import com.sunline.ppy.dictionary.enums.OrderStatus;
import com.sunline.ppy.dictionary.exception.ProcessException;


/**
 * @author wangz
 * 
 */
@Service
public class RefundCommService {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private AppapiCommService appapiCommService;
	@Autowired
	private TxnUtils txnUtils;
	@Autowired
	private OrderFacility orderFacility;
	@Autowired
	private RCcsLoanReg rCcsLoanReg;
	@Autowired
	private QueryCommService queryCommService;
	
	public void RefundProcess(TxnContext context,TFCRefundReq req,Indicator findOrigOrderInd){
		this.initTxnInfo(context, req);

		this.checkReq(context);
		// 是否查找原交易 并检查重复交易		
		if(findOrigOrderInd == Indicator.Y){
			appapiCommService.checkRepeatTxn(context);
			appapiCommService.findOrigOrder(context);
		}

		appapiCommService.loadAcctByContrNo(context, true);
		appapiCommService.loadAcctOByAcct(context, true);
		appapiCommService.loadCardOByAcct(context, true);
		appapiCommService.loadCustomerByCustId(context, true);
		appapiCommService.loadLoanByDueBillNo(context, null, true);
		appapiCommService.loadLoanRegByDueBillNo(context,null, LoanRegStatus.S, LoanAction.A,false);
		//查找商品订单表
		CcsMerchandiseOrder merchan= appapiCommService.loadMerchandiseOrder(context);

		// 检查在途订单 暂不拦截--暂不拦截
		//appapiCommService.checkWaitCreditOrder(context);

		// 检查锁定码
		appapiCommService.checkBlockCode(context);

		//校验交易
		this.valiBiz(context,merchan,findOrigOrderInd);

		// 业务处理，单独业务处理，保证业务数据完整
		this.bizProc(context);

	}
	
	@Transactional
	private void bizProc(TxnContext context) {
		if (logger.isDebugEnabled())
			logger.debug("预约结清申请处理--bookingApply");
		TxnInfo txnInfo = context.getTxnInfo();
		CcsLoanReg refundLoanReg = null;

		// 查询贷款信息注册表 有则更新 无则新建
		try {
			CommResp mainResp = new CommResp();
			mainResp.setCode(MsPayfrontError.S_0.getCode());
			mainResp.setMessage(MsPayfrontError.S_0.getCode());
			MsPayfrontError msPayfrontError = appapiCommService.getErrorEnum(mainResp.getCode(), MsPayfrontError.class);
			txnInfo.setPayRespCode(mainResp.getCode());
			txnInfo.setPayRespMessage(mainResp.getMessage());
			txnInfo.setResponsCode(msPayfrontError.getRespCode());
			txnInfo.setResponsDesc(msPayfrontError.getDesc());
			
			// 初始化预约注册信息
			refundLoanReg = this.saveCcsLoanReg(context,refundLoanReg);
			
			appapiCommService.installOrder(context);
			appapiCommService.saveCcsAuthmemoO(context);
			context.getOrder().setTxnType(txnInfo.getTransType());
			orderFacility.updateOrder(context.getOrder(), mainResp, "SUCCESS",OrderStatus.S,txnInfo.getLogKv(),txnInfo.getBizDate(),MsRespCode.E_0000);
			appapiCommService.updateAcctO(context);
			appapiCommService.updateAcctoMemoAmt(context.getAccounto(), txnInfo,true);
			appapiCommService.updateAuthmemo(context, AuthTransStatus.N);//交易流水
			
			appapiCommService.savePosting(txnInfo);//入账记录
			
			appapiCommService.mergeProc(context);
		} catch (ProcessException e) {
			throw new ProcessException(MsRespCode.E_1044.getCode(),MsRespCode.E_1044.getMessage());
		}
	}

	/**
	 * 保存退货注册信息
	 * @param txnInfo
	 * @param refundLoanReg
	 * @param loan
	 * @param ccsLoanReg
	 * @return
	 */
	private CcsLoanReg saveCcsLoanReg(TxnContext context,CcsLoanReg refundLoanReg) {
		TxnInfo txnInfo = context.getTxnInfo();
		refundLoanReg = appapiCommService.genLoanReg(0, BigDecimal.ZERO,
				context.getLoan().getRefNbr(), txnInfo.getCardNo(), txnInfo.getCardNo(),
				txnInfo.getAcctNbr(), txnInfo.getAcctType(), null,
				txnInfo.getBizDate());
		
		refundLoanReg.setOrg(txnInfo.getOrg());
		refundLoanReg.setAcctNbr(txnInfo.getAcctNbr());
		refundLoanReg.setAcctType(txnInfo.getAcctType());
		refundLoanReg.setRequestTime(new Date());
		refundLoanReg.setLogicCardNbr(txnInfo.getCardNo());// LoanRegStatus
		refundLoanReg.setCardNbr(txnInfo.getCardNo());
		refundLoanReg.setLoanRegStatus(LoanRegStatus.A);
		refundLoanReg.setLoanAction(LoanAction.T);// GUARANTY_ID
		refundLoanReg.setDueBillNo(txnInfo.getDueBillNo());
		refundLoanReg.setContrNbr(txnInfo.getContrNo());
		refundLoanReg.setGuarantyId(txnInfo.getGuarantyid());
		refundLoanReg.setLoanCode(txnInfo.getLoanCode());
		
		refundLoanReg.setAdDebitAmt(txnInfo.getTransAmt());// 将退货金额放在提前还款实扣款成功金额中，3000里面重算还款计划时做比较
		refundLoanReg.setDdRspFlag(Indicator.Y);// 预约注册-扣款成功
		refundLoanReg.setValidDate(txnInfo.getBizDate());
		
		refundLoanReg = rCcsLoanReg.save(refundLoanReg);
		return refundLoanReg;
	}

	/**
	 * 业务检查
	 * 
	 * @param context
	 */
	private void valiBiz(TxnContext context,CcsMerchandiseOrder merchan,Indicator findOrigOrderInd) {
		// 获取放款申请信息
		BigDecimal amt=(findOrigOrderInd == Indicator.N ? context.getOrigOrder().getTxnAmt():context.getOrigOrderHst().getTxnAmt());
		
		CcsLoanReg loanReg = context.getLoanReg();
		CcsLoanReg preLoanReg = appapiCommService.loadLoanRegByDueBillNo(
				context, null, LoanRegStatus.A, LoanAction.O);
		CcsLoanReg refundLoanReg = appapiCommService.loadLoanRegByDueBillNo(
				context, null, LoanRegStatus.A, LoanAction.T);
		if (null == context.getLoan() && null == loanReg) {
			throw new ProcessException(MsRespCode.E_1015.getCode(),
					MsRespCode.E_1015.getMessage());
		}
		if (context.getTxnInfo().getTransAmt().compareTo(
				amt.add(merchan.getDownPayment()==null?BigDecimal.ZERO:merchan.getDownPayment())) > 0
						|| context.getTxnInfo().getTransAmt().compareTo(amt)<0){
			throw new ProcessException(MsRespCode.E_1005.getCode(),MsRespCode.E_1005.getMessage());
		}

		// loan已经结清/终止，则拒绝交易
		appapiCommService.checkLoan(context);
		if(null != loanReg){
			if (LoanRegStatus.V == loanReg.getLoanRegStatus()) {
				throw new ProcessException(MsRespCode.E_1015.getCode(), MsRespCode.E_1015.getMessage()+",已撤销");
			}
			
			if (LoanRegStatus.F == loanReg.getLoanRegStatus()) {
				throw new ProcessException(MsRespCode.E_1015.getCode(), MsRespCode.E_1015.getMessage()+",失败贷款注册信息不能退货");
			}
			
			if (LoanRegStatus.S != loanReg.getLoanRegStatus()) {
				throw new ProcessException(MsRespCode.E_1015.getCode(), MsRespCode.E_1015.getMessage()+",未完成贷款注册信息不能退货");
			}
		}

		// 已申请结清
		if (null != preLoanReg) {
			throw new ProcessException(MsRespCode.E_1073.getCode(),
					MsRespCode.E_1073.getMessage());
		}

		// 已退货
		if (null != refundLoanReg) {
			throw new ProcessException(MsRespCode.E_1074.getCode(),
					MsRespCode.E_1074.getMessage());
		}
		
		//检查是否是当天的交易记录（业务日期是否为当天）-- 是否判断大于等于当天更合理
		if(context.getOrigOrder() != null && 
				DateUtils.isSameDay(context.getOrigOrder().getBusinessDate(), context.getTxnInfo().getBizDate())){
			throw new ProcessException(MsRespCode.E_1015.getCode(), MsRespCode.E_1015.getMessage()+",交易当天不能退货");
		}
		//做一个批量校验
		queryCommService.batchProcessingCheck(context.getAccount(), context.getAccounto());
	}

	/**
	 * 初始化txnInfo
	 * 
	 * @param context
	 * @param req
	 */
	private void initTxnInfo(TxnContext context, TFCRefundReq req) {
		TxnInfo txnInfo = context.getTxnInfo();
		txnInfo.setTransDirection(AuthTransDirection.Normal);
		txnInfo.setTransType(AuthTransType.AgentCredit);
		txnInfo.setMti("0220");
		txnInfo.setProcCode("200001");
		txnInfo.setDirection(TransAmtDirection.C);
		txnInfo.setLoanUsage(LoanUsage.R);
		txnInfo.setRefNbr(txnUtils.genRefnbr(txnInfo.getBizDate(),
				txnInfo.getServiceSn()));
		txnInfo.setSysOlTime(new Date());
		txnInfo.setCountryCd("156");
		
		txnInfo.setBizDate(req.getBizDate());
		txnInfo.setRequestTime(req.getRequestTime());
		txnInfo.setServiceSn(req.getServiceSn());
		txnInfo.setInputSource(req.getInputSource());
		txnInfo.setAcqId(req.getAcqId());
		txnInfo.setSubTerminalType(req.getSubTerminalType());
		txnInfo.setOrg(req.getOrg());
		txnInfo.setServiceId(req.getServiceId());

		txnInfo.setContrNo(req.contractNo);
		txnInfo.setTransAmt(req.amount);
//		txnInfo.setOrigTransAmt(req.amount);
		txnInfo.setOrigAcqId(req.origAcqId);
		txnInfo.setOrigServiceSn(req.origServiceSn);
		txnInfo.setOrigServiceId(req.origServiceId);


		if (logger.isDebugEnabled())
			logger.debug("业务日期：[{}]", txnInfo.getBizDate());
		LogTools.printObj(logger, txnInfo, "交易中间信息txnInfo");
	}

	/**
	 * @param context
	 */
	private void checkReq(TxnContext context) {
		TFCRefundReq req = (TFCRefundReq) context.getMsRequestInfo();

		if (req.amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new ProcessException(MsRespCode.E_1043.getCode(),
					MsRespCode.E_1043.getMessage() + ",字段名称{AMOUNT},必须大于0");
		}
	}

}
