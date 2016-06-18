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
import com.sunline.ccs.infrastructure.server.repos.RCcsAuthmemoO;
import com.sunline.ccs.infrastructure.server.repos.RCcsTxnPost;
import com.sunline.ccs.infrastructure.shared.model.CcsAuthmemoO;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsMerchandiseOrder;
import com.sunline.ccs.infrastructure.shared.model.CcsOrder;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnPost;
import com.sunline.ccs.param.def.enums.AuthTransDirection;
import com.sunline.ccs.param.def.enums.TransAmtDirection;
import com.sunline.ccs.service.context.TxnContext;
import com.sunline.ccs.service.context.TxnInfo;
import com.sunline.ccs.service.handler.appapi.TFCTerminalRepayHandler;
import com.sunline.ccs.service.msentity.TFCTerminalRepayReq;
import com.sunline.ccs.service.msentity.TFDWithDrawVoidReq;
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
public class WithdrawVoidCommService {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private AppapiCommService appapiCommService;
	@Autowired
	private TxnUtils txnUtils;
	@Autowired
	private OrderFacility orderFacility;
	@Autowired
	private RCcsAuthmemoO rCcsAuthmemoO;
	@Autowired
	private RCcsTxnPost rCcsTxnPost;
	@Autowired
	private RepayCommService transactionHandle;
	@Autowired
	TFCTerminalRepayHandler tfcTerminalRepayHandler;
	
	public void WithDrawVoidProcess(TxnContext context,TFDWithDrawVoidReq req,Indicator findOrigOrderInd){
		
		TxnContext repayContext = new TxnContext();
		TxnInfo txnInfo = new TxnInfo();
		repayContext.setTxnInfo(txnInfo);
		this.initTxnInfo(context, req);
		tfcTerminalRepayHandler.initTxnInfo(repayContext,dataExchange(req));
		this.checkReq(context);
		// 是否查找原交易 并检查重复交易		
		if(findOrigOrderInd == Indicator.Y){
			appapiCommService.checkRepeatTxn(context);
			appapiCommService.findVoidOrigOrder(context);			
		}
		appapiCommService.loadAcctByContrNo(context, true);
		appapiCommService.loadAcctOByAcct(context, true);
		appapiCommService.loadCardOByAcct(context, true);
		appapiCommService.loadCustomerByCustId(context, true);
		appapiCommService.loadLoanByDueBillNo(context, null, false);
		appapiCommService.loadLoanRegByDueBillNo(context,context.getOrigOrder().getRefNbr(), null, LoanAction.A,false);//不支持多个loan
		//查找商品订单表
		CcsMerchandiseOrder merchan= appapiCommService.loadMerchandiseOrder(context);
		
		// 检查锁定码
		appapiCommService.checkBlockCode(context);

		//
		this.valiBiz(context);

		// 业务处理，单独业务处理，保证业务数据完整
		this.bizProc(context,merchan,repayContext);

	}
	
	@Transactional
	private void bizProc(TxnContext context,CcsMerchandiseOrder merchan,TxnContext repayContext) {
		if (logger.isDebugEnabled())
			logger.debug("预约结清申请处理--bookingApply");
		TxnInfo txnInfo = context.getTxnInfo();
		CcsLoanReg loanReg = context.getLoanReg();
		CcsOrder origOrder = context.getOrigOrder();

		// 查询贷款信息注册表 有则更新 无则新建
		CommResp mainResp = new CommResp();
		mainResp.setCode(MsPayfrontError.S_0.getCode());
		mainResp.setMessage(MsPayfrontError.S_0.getCode());
		MsPayfrontError msPayfrontError = appapiCommService.getErrorEnum(mainResp.getCode(), MsPayfrontError.class);
		txnInfo.setPayRespCode(mainResp.getCode());
		txnInfo.setPayRespMessage(mainResp.getMessage());
		txnInfo.setResponsCode(msPayfrontError.getRespCode());
		txnInfo.setResponsDesc(msPayfrontError.getDesc());
		
		loanReg.setLoanRegStatus(LoanRegStatus.V);
		context.getOrigOrder().setOrderStatus(OrderStatus.B);
		
		this.updateOrigMemo(context, origOrder);
		this.updateOrigTxnPost(txnInfo, origOrder);
		
		appapiCommService.installOrder(context);
		appapiCommService.saveCcsAuthmemoO(context);
		orderFacility.updateOrder(context.getOrder(), mainResp, "SUCCESS",OrderStatus.S,txnInfo.getLogKv(),txnInfo.getBizDate(),MsRespCode.E_0000);
		appapiCommService.updateAcctO(context);
		appapiCommService.updateAcctoMemoAmt(context.getAccounto(), txnInfo,false);
		appapiCommService.updateAuthmemo(context, AuthTransStatus.N);//交易流水
		//判断是否是退货或者撤销交易
		if ("TFCRefund".equals(txnInfo.getServiceId())) {
			//如果退货，则对累积放款额度赋值并对多余的金额做一个入账
			context.getAccount().setLtdLoanAmt(context.getAccount().getCreditLmt());
			this.repayMent(context,merchan,repayContext);
		}else {
			if (context.getTxnInfo().getTransAmt().compareTo(context.getOrigOrder().getTxnAmt())!=0) {
				throw new ProcessException(MsRespCode.E_1005.getCode(), "当天退货金额必须等于放款金额");
			}
		}
		
		appapiCommService.mergeProc(context);
			
	}

	/**
	 * 更新原memo
	 * @param context
	 * @param origOrder
	 */
	private void updateOrigMemo(TxnContext context, CcsOrder origOrder) {
		CcsAuthmemoO origAuthmemo = rCcsAuthmemoO.findOne(origOrder.getLogKv());
		origAuthmemo.setAuthTxnStatus(AuthTransStatus.V);
		
		rCcsAuthmemoO.save(origAuthmemo);
	}

	/**
	 * 更新原txnPost
	 * @param txnInfo
	 * @param origOrder
	 */
	private void updateOrigTxnPost(TxnInfo txnInfo, CcsOrder origOrder) {
		CcsTxnPost origTxnPost = appapiCommService.findOrigTxnPost(txnInfo.getCardNo(), origOrder.getRefNbr());
		origTxnPost.setIsRevoke(Indicator.Y);
		
		rCcsTxnPost.save(origTxnPost);
	}

	/**
	 * 初始化txnInfo
	 * 
	 * @param context
	 * @param req
	 */
	private void initTxnInfo(TxnContext context, TFDWithDrawVoidReq req) {
		TxnInfo txnInfo = context.getTxnInfo();
		txnInfo.setTransDirection(AuthTransDirection.Revocation);
		txnInfo.setTransType(AuthTransType.AgentDebit);
		txnInfo.setMti("0420");
		txnInfo.setProcCode("480001");
		txnInfo.setDirection(TransAmtDirection.D);
		txnInfo.setLoanUsage(LoanUsage.V);
		txnInfo.setRefNbr(txnUtils.genRefnbr(txnInfo.getBizDate(),txnInfo.getServiceSn()));
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
		txnInfo.setOrigMerId(req.merId);
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
		TFDWithDrawVoidReq req = (TFDWithDrawVoidReq) context.getMsRequestInfo();

		if (req.amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new ProcessException(MsRespCode.E_1043.getCode(),
					MsRespCode.E_1043.getMessage() + ",字段名称{AMOUNT},必须大于0");
		}
	}
	
	/**
	 * 业务检查
	 * 
	 * @param context
	 */
	private void valiBiz(TxnContext context) {
		// 获取放款申请信息
		CcsLoanReg loanReg = context.getLoanReg();
		CcsLoanReg preLoanReg = appapiCommService.loadLoanRegByDueBillNo(context, null, LoanRegStatus.A, LoanAction.O);
		CcsLoanReg refundLoanReg = appapiCommService.loadLoanRegByDueBillNo(context, null, LoanRegStatus.A, LoanAction.T);
		
		if (null != context.getLoan()) {
			throw new ProcessException(MsRespCode.E_1015.getCode(), MsRespCode.E_1015.getMessage());
		}
		
		if (null == loanReg) {
			throw new ProcessException(MsRespCode.E_1015.getCode(), MsRespCode.E_1015.getMessage()+",未找到贷款注册信息");
		}
		
		if (LoanRegStatus.V == loanReg.getLoanRegStatus()) {
			throw new ProcessException(MsRespCode.E_1015.getCode(), MsRespCode.E_1015.getMessage()+",重复撤销");
		}
		
		if (LoanRegStatus.F == loanReg.getLoanRegStatus()) {
			throw new ProcessException(MsRespCode.E_1015.getCode(), MsRespCode.E_1015.getMessage()+",失败贷款注册信息不能撤销");
		}
		
		if (LoanRegStatus.S != loanReg.getLoanRegStatus()) {
			throw new ProcessException(MsRespCode.E_1015.getCode(), MsRespCode.E_1015.getMessage()+",未完成贷款注册信息不能撤销");
		}

		// loan已经结清/终止，则拒绝交易
		appapiCommService.checkLoan(context);

		// 已申请结清
		if (null != preLoanReg) {
			throw new ProcessException(MsRespCode.E_1073.getCode(), MsRespCode.E_1073.getMessage());
		}

		// 已退货
		if (null != refundLoanReg) {
			throw new ProcessException(MsRespCode.E_1074.getCode(), MsRespCode.E_1074.getMessage());
		}

		//检查是否是当天的交易记录（业务日期是否为当天）
		if(!DateUtils.isSameDay(context.getOrigOrder().getBusinessDate(), context.getTxnInfo().getBizDate())){
			throw new ProcessException(MsRespCode.E_1075.getCode(), MsRespCode.E_1075.getMessage());
		}
	}
	/**
	 * 对当天退货多余的金额做一个入账
	 * @param context
	 * @param merchan
	 * @param repayContext
	 */
	private void repayMent(TxnContext context,CcsMerchandiseOrder merchan,TxnContext repayContext) {
		
		//存入金额
		BigDecimal amt = context.getTxnInfo().getTransAmt().subtract(context.getAccount().getCreditLmt());
		if (amt.compareTo(BigDecimal.ZERO)!=0) {
			//判断退货金额大于信用额度加上首付金额并退货金额大于贷款金额
			if (context.getTxnInfo().getTransAmt().compareTo(
					context.getOrigOrder().getTxnAmt().add(
							merchan.getDownPayment()==null?BigDecimal.ZERO:merchan.getDownPayment())) > 0 
							|| context.getTxnInfo().getTransAmt().compareTo(context.getOrigOrder().getTxnAmt())<0){
				throw new ProcessException(MsRespCode.E_1005.getCode(),MsRespCode.E_1005.getMessage());
			}
		
			repayContext.getTxnInfo().setTransAmt(amt);
			appapiCommService.loadAcctByContrNo(repayContext, true);
			//对下面的主动还款的交易ccs_acct_o赋值初始化
			repayContext.setAccounto(context.getAccounto());
			appapiCommService.loadCardOByAcct(repayContext, true);
			appapiCommService.installOrder(repayContext);
			transactionHandle.bisProcess(repayContext);
			//不做对账
			repayContext.getOrder().setMatchInd(Indicator.N);

			//因为两个 TxnContext的accto为同一对象，提交会混乱，所有对context赋值
			context.setAccounto(repayContext.getAccounto());
			appapiCommService.mergeProc(repayContext);
		}
	}
	/**
	 * 对主动还款请求体
	 * @param req
	 * @return
	 */
	private TFCTerminalRepayReq dataExchange(TFDWithDrawVoidReq req) {
		
		TFCTerminalRepayReq repayReq = new TFCTerminalRepayReq();
		
		repayReq.setBizDate(req.getBizDate());
		repayReq.setRequestTime(req.getRequestTime());
		repayReq.setServiceSn(req.getServiceSn());
		repayReq.setInputSource(req.getInputSource());
		repayReq.setAcqId(req.getAcqId());
		repayReq.setSubTerminalType(req.getSubTerminalType());
		repayReq.setContractNo(req.contractNo);//TODO 修改请求字段
		repayReq.setAmount(req.amount);//TODO 修改请求字段
		repayReq.setServiceId(req.getServiceId());
		repayReq.setOrg(req.getOrg());
		
		return repayReq;
	}
}
