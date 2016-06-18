package com.sunline.ccs.service.handler.query;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunline.ccs.facility.DateUtils;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.facility.TxnUtils;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.facility.order.PaymentFacility;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.CcsRepaySchedule;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.param.def.enums.AuthTransDirection;
import com.sunline.ccs.param.def.enums.TransAmtDirection;
import com.sunline.ccs.service.context.TxnContext;
import com.sunline.ccs.service.context.TxnInfo;
import com.sunline.ccs.service.handler.AppapiCommService;
import com.sunline.ccs.service.handler.QueryCommService;
import com.sunline.ccs.service.msdentity.STFDRemainderTransferReq;
import com.sunline.ccs.service.msdentity.STFDRemainderTransferResp;
import com.sunline.ccs.service.noticetemplate.SMSLoanDeductions;
import com.sunline.ppy.dictionary.enums.AuthTransType;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.LoanUsage;
import com.sunline.ppy.dictionary.enums.MsRespCode;
import com.sunline.ppy.dictionary.enums.PlanType;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * 兜底 溢缴款转出计划
 * @author zhengjf
 *
 */
@Service
public class TFDRemainderTransfer {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	TxnUtils txnUtils;
	@Autowired
	private AppapiCommService appapiCommService;
	@Autowired
    private QueryCommService queryCommService;
	@Autowired
	private PaymentFacility paymentFacility;
	@Autowired
	public UnifiedParamFacilityProvide unifiedParamFacilityProvide;
	@Autowired
	private SMSLoanDeductions smsLoanDeductions;
	
	//预约类型 1-试算  2-申请
	private static final String BOOKING_TYPE_1 ="1";
		
	private static final String BOOKING_TYPE_2 ="2";
	
	public STFDRemainderTransferResp handle(STFDRemainderTransferReq req) {
		LogTools.printLogger(log, "STFDRemainderTransferReq", "兜底溢缴款转出计划接口", req, true);
		LogTools.printObj(log, req, "请求参数STFDRemainderTransferReq");
		
		STFDRemainderTransferResp resp = new STFDRemainderTransferResp();
		
		TxnContext context = new TxnContext();
		TxnInfo txnInfo = new TxnInfo();
		context.setTxnInfo(txnInfo);
		try {
			this.initTxnInfo(context, req);
			appapiCommService.checkRepeatTxn(context);
			
			appapiCommService.loadAcctByContrNo(context, true);
			appapiCommService.loadAcctOByAcct(context, true);
			appapiCommService.loadCustomerByCustId(context, true);
			appapiCommService.loadLoanByContrNo(context,null, false);
			appapiCommService.loadCardOByAcct(context, true);
			
			// 检查在途订单
			appapiCommService.checkWaitCreditOrder(context);
			// 检查锁定码
			appapiCommService.checkBlockCode(context);
			
			// 校验
			this.valiBiz(context);
			
			//试算最大可转出溢缴款金额
			this.maximumAmt(context);
			
			//如果交易类型是BOOKING_TYPE_1，为2为转出
			if (txnInfo.getBookingType().equals(BOOKING_TYPE_2)) {
				
				//业务处理，单独业务处理，保证业务数据完整
				this.bizProc(context);
				
				//处理结果，单独事务处理
				this.payResultProc(context);
				
				//向通知平台发送通知		
				smsLoanDeductions.sendSMS(context);	
				
			}else {
				txnInfo.setResponsCode(MsRespCode.E_0000.getCode());
				txnInfo.setResponsDesc("交易成功");
				resp.setTransferAmt(txnInfo.getDepositeCashOTB());
			}
			
		} catch (ProcessException pe) {
			if (log.isErrorEnabled())
				log.error(pe.getMessage(), pe);

			appapiCommService.preException(pe, pe, txnInfo);
			appapiCommService.saveErrorOrder(context);

		} catch (Exception e) {
			if (log.isErrorEnabled())
				log.error(e.getMessage(), e);

			ProcessException pe = new ProcessException();
			appapiCommService.preException(pe, pe, txnInfo);
			appapiCommService.saveErrorOrder(context);

		} finally {
			LogTools.printLogger(log, "STFDRemainderTransferResp", "兜底溢缴款转出计划接口", resp, false);
		}

		this.setResponse(resp, req, txnInfo);
		LogTools.printObj(log, resp, "响应参数STFDRemainderTransferResp");
		return resp;
	}

	
	/**
	 * 初始化中间交易信息
	 * 
	 * @param context
	 */
	private void initTxnInfo(TxnContext context, STFDRemainderTransferReq req) {
		TxnInfo txnInfo = context.getTxnInfo();
		txnInfo.setBizDate(req.getBizDate());
		txnInfo.setTransDirection(AuthTransDirection.Normal);
		txnInfo.setTransType(AuthTransType.AgentDebit);
		txnInfo.setSysOlTime(new Date());
		txnInfo.setRequestTime(req.getRequestTime());
		txnInfo.setServiceSn(req.getServiceSn());
		txnInfo.setMti("0208");
		txnInfo.setProcCode("490001");
		txnInfo.setInputSource(req.getInputSource());
		txnInfo.setAcqId(req.getAcqId());
		txnInfo.setSubTerminalType(req.getSubTerminalType());
		txnInfo.setDirection(TransAmtDirection.D);
		txnInfo.setContrNo(req.getContrNo());
		txnInfo.setTransAmt(req.getTransferLmt());
		txnInfo.setLoanUsage(LoanUsage.D);
		txnInfo.setRefNbr(txnUtils.genRefnbr(txnInfo.getBizDate(), txnInfo.getServiceSn()));
		txnInfo.setServiceId(req.getServiceId());
		txnInfo.setBookingType(req.getType());//预约类型 1-试算 2-转出
		
		if(!StringUtils.equals(BOOKING_TYPE_1, req.getType()) 
				&& !StringUtils.equals(BOOKING_TYPE_2, req.getType())){
			throw new ProcessException(MsRespCode.E_1043.getCode(),MsRespCode.E_1043 .getMessage()+ ",字段名称{" +req.getType()+ "} 1-试算  2-申请");
		}

		if (log.isDebugEnabled())
			log.debug("业务日期：[{}]", txnInfo.getBizDate());
		LogTools.printObj(log, txnInfo, "交易中间信息txnInfo");
	}
	/**
	 * @param context
	 */
	private void valiBiz(TxnContext context) {
		CcsAcctO accto = context.getAccounto();
		CcsAcct acct = context.getAccount();
		// 获取分期计划参数
		LoanPlan loanPlan;
		loanPlan = unifiedParamFacilityProvide.loanPlanProduct(context.getAccount().getProductCd());
		
		//批量时间检查
		queryCommService.batchProcessingCheck(acct, accto);
		
		LogTools.printObj(log, accto.getBlockCode(), "账户锁定码");
		// 账户锁定码包含"P"
		if (StringUtils.isNotEmpty(accto.getBlockCode()) && accto.getBlockCode().toUpperCase().indexOf("P") > -1) {
			throw new ProcessException(MsRespCode.E_1014.getCode(), MsRespCode.E_1014.getMessage());
		}
		
		// 溢缴款转出距离账单日提前天数 默认为5
		if (loanPlan.depositEarlyDays == null){
			loanPlan.depositEarlyDays = 0;
		}
		LogTools.printObj(log, loanPlan.depositEarlyDays, "溢缴款转出距离账单日提前天数");
		
		
		//如果溢缴款转出距离账单日提前天数小于0
		if(loanPlan.depositEarlyDays < 0) {
			throw new ProcessException(MsRespCode.E_1070.getCode(), "溢缴款转出距离账单日允许提前的天数为空或者小于零");
		}

		context.setLoanPlan(loanPlan);
	}
	/**
	 * 计算最大转出金额
	 * @param context
	 * @return
	 */
	private void maximumAmt(TxnContext context) {

		TxnInfo txnInfo = context.getTxnInfo();
		CcsAcctO accto = context.getAccounto();
		CcsLoan loan = context.getLoan();
		CcsAcct acct = context.getAccount();
		LoanPlan loanPlan = context.getLoanPlan();
//		int term = 0;
		
		BigDecimal payment = new BigDecimal(0);//累积还款金额
		BigDecimal succsAmt = new BigDecimal(0);//总欠款
	
		List<CcsPlan> planList = appapiCommService.loadPlansByAcct(context, true);
		
		//计算溢缴款跟逾期总欠款，key为1，value为溢缴款，key为2，value为转入计划总欠款，key为3，value为转出计划总欠款
		Map<Integer,BigDecimal> computBookingAmt = computBookingAmt(context, planList);
		
		//累积还款金额=当前贷款上的溢缴款+当天还款多笔还款总额（没跑批的）-当天已转出总金额（没跑批的）
		payment =payment.add(computBookingAmt.get(1)).multiply(new BigDecimal(-1))
				.add(accto.getMemoCr()).subtract(accto.getMemoDb());
		succsAmt = computBookingAmt.get(2);
		
		//累积还款金额-总欠款
		BigDecimal transfer = payment.subtract(succsAmt);
		
		//loan不为空 并 状态为非完成 并 下个账单日距离当期日期小于等于五天 则
		//转出金额<=累积还款金额-总欠款-本期期款
		//loan不为空 并 状态为非完成 并 下个账单日距离当期日期小于等于五天 则
		//转出金额<=累积还款金额-总欠款-本期期款
	 	int day = DateUtils.getIntervalDays(txnInfo.getBizDate(),acct.getNextStmtDate());
		if(log.isDebugEnabled())
			log.debug("溢缴款转出距离账单日提前天数[{}],距离下个账单日天数[{}]",loanPlan.depositEarlyDays,day);
		if(null != loan && loan.getCurrTerm() != loan.getLoanInitTerm()
				&& day<=loanPlan.depositEarlyDays){
			
			//如果是随借随还  减去最小还款额并减去转出计划总欠款
			if (loan.getLoanType() == LoanType.MCAT) {
				transfer=transfer.subtract(acct.getTotDueAmt()).subtract(computBookingAmt.get(3));
			}
			//如果是等额本息  减去减本期期款
			if (loan.getLoanType() == LoanType.MCEI) {
				
				//获取schedule
				List<CcsRepaySchedule> scheduleList = queryCommService.getSchedulelistByLoan(loan.getLoanId());
			
				//把schedule通过map集合保存  key为期数  value为当期总欠款
				Map<Integer,BigDecimal> tmpTotAmteMap = tmpTotAmteMap(scheduleList);
			
				//减本期期款
				transfer = transfer.subtract(tmpTotAmteMap.get(loan.getCurrTerm()+1));
			}
		}
		//如果最大可转移溢缴款为负数，则返回零
		transfer = (transfer.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : transfer);
		
		//账号最大可转移溢缴款额度
		context.getTxnInfo().setDepositeCashOTB(transfer);
		
	}
	
	
	
	/**
	 * 
	 * 计算总欠款额，累积还款金额
	 * @param context
	 * @return
	 */
	private Map<Integer,BigDecimal> computBookingAmt(TxnContext context,List<CcsPlan> planList) {
		
		Map<Integer,BigDecimal> computBookingAmt = new HashMap<Integer, BigDecimal>();

		BigDecimal transfer  = BigDecimal.ZERO;		//溢缴款  

		BigDecimal sumOverdue = BigDecimal.ZERO;	//转入计划总欠款
		
		BigDecimal outPlan  = BigDecimal.ZERO;	//转出计划 总欠款
		
		for (CcsPlan ccsPlan : planList) {
			
			if (PlanType.D.equals(ccsPlan.getPlanType())) {
				 
					transfer=transfer.add(ccsPlan.getCurrBal());
			
			}else if(PlanType.Q.equals(ccsPlan.getPlanType()) || PlanType.L.equals(ccsPlan.getPlanType())){
				
				//总欠款为余额+累计利息+累计罚息+累计复利
				sumOverdue=sumOverdue.add(ccsPlan.getCurrBal()).add(ccsPlan.getNodefbnpIntAcru())
									   .add(ccsPlan.getPenaltyAcru()).add(ccsPlan.getCompoundAcru());
			
			}else if(PlanType.J.equals(ccsPlan.getPlanType()) || PlanType.P.equals(ccsPlan.getPlanType())){
				
				outPlan=outPlan.add(ccsPlan.getCurrBal()).add(ccsPlan.getNodefbnpIntAcru())
						   .add(ccsPlan.getPenaltyAcru()).add(ccsPlan.getCompoundAcru());
				
			}
		}
		
		computBookingAmt.put(1, transfer);
		computBookingAmt.put(2, sumOverdue);
		computBookingAmt.put(3, outPlan);
		
		return computBookingAmt;
	}
	
	/*
	 * 把schedule总欠款计算出来
	 * key为schedule期数
	 */
	private Map<Integer,BigDecimal> tmpTotAmteMap(List<CcsRepaySchedule> scheduleList){
		 Map<Integer,BigDecimal> tmpTotAmteMap = new HashMap<Integer, BigDecimal>();
		 for(CcsRepaySchedule schedule : scheduleList){
			 BigDecimal tmpTotAmt= BigDecimal.ZERO;
			 //当期总欠款
			 tmpTotAmt = tmpTotAmt.add(schedule.getLoanTermPrin()==null?BigDecimal.ZERO:schedule.getLoanTermPrin()).
						add(schedule.getLoanTermFee()==null?BigDecimal.ZERO:schedule.getLoanTermFee()).
						add(schedule.getLoanTermInt()==null?BigDecimal.ZERO:schedule.getLoanTermInt()).
						add(schedule.getLoanStampdutyAmt()==null?BigDecimal.ZERO:schedule.getLoanStampdutyAmt()).
						add(schedule.getLoanLifeInsuAmt()==null?BigDecimal.ZERO:schedule.getLoanLifeInsuAmt()).
						add(schedule.getLoanSvcFee()==null?BigDecimal.ZERO:schedule.getLoanSvcFee()).
						add(schedule.getLoanReplaceSvcFee()==null?BigDecimal.ZERO:schedule.getLoanReplaceSvcFee()).
						add(schedule.getLoanInsuranceAmt()==null?BigDecimal.ZERO:schedule.getLoanInsuranceAmt());
			 tmpTotAmteMap.put(schedule.getCurrTerm(), tmpTotAmt);
		 }
		 return tmpTotAmteMap;
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
		TxnInfo txnInfo = context.getTxnInfo();
		if (log.isDebugEnabled())
			log.debug("最大可转出溢缴款金额--[{}],交易金额[{}]",txnInfo.getDepositeCashOTB(),txnInfo.getTransAmt());
			
		if(txnInfo.getTransAmt().compareTo(BigDecimal.ZERO)<=0
				|| txnInfo.getDepositeCashOTB().compareTo(txnInfo.getTransAmt())<0){
			throw new ProcessException(MsRespCode.E_1005.getCode(), "转出溢缴款金额必须大于零并小于等于最大转出金额");
		}
		
		if (log.isDebugEnabled())
			log.debug("单独事务处理业务逻辑--bizProc");
		// 生成订单
		appapiCommService.installOrder(context);
		
		appapiCommService.saveCcsAuthmemoO(context);
		
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
			String payJson = paymentFacility.installPaySinPaymentCommand(context.getOrder());
			
			if(log.isDebugEnabled())
				log.debug("独立事务，处理支付结果--payResultProc,支付报文：[{}]",payJson);
			
			appapiCommService.loadAuthmemoLogKv(context, context.getTxnInfo().getLogKv(),true);

			appapiCommService.loanProc(context,payJson);
			
			context.getOrder().setCardNo(context.getAccount().getDdBankAcctNbr());
		}catch (ProcessException pe){
			if(log.isErrorEnabled())
				log.error(pe.getMessage(), pe);
			
			ProcessException newPe = appapiCommService.preException(pe, pe, txnInfo);
			appapiCommService.exceptionProc(context, newPe);
		}catch (Exception e) {
			if(log.isErrorEnabled())
				log.error(e.getMessage(), e);
			
			ProcessException pe = new ProcessException();
			ProcessException newPe = appapiCommService.preException(e, pe, txnInfo);;//e不是pe，处理逻辑与没有code的pe一样
			appapiCommService.exceptionProc(context, newPe);
		}
		//保存交易数据
		appapiCommService.mergeProc(context);
	}
	
	/**
	 * 组装响应报文
	 * 
	 * @param resp
	 * @param req
	 * @param txnInfo
	 */
	private void setResponse(STFDRemainderTransferResp resp, STFDRemainderTransferReq req, TxnInfo txnInfo) {
		resp.setContrNo(txnInfo.getContrNo());
		resp.setErrorCode(txnInfo.getResponsCode());
		resp.setErrorMessage(txnInfo.getResponsDesc());
		if (StringUtils.equals(MsRespCode.E_0000.getCode(),
				txnInfo.getResponsCode())) {
			resp.setStatus("S");// 交易成功
		} else {
			resp.setStatus("F");// 交易失败
		}
	}
}
