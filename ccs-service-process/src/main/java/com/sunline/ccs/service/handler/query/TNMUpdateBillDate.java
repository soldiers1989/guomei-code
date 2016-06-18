package com.sunline.ccs.service.handler.query;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunline.ccs.facility.DateUtils;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.facility.MicroCreditRescheduleUtils;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcct;
import com.sunline.ccs.infrastructure.server.repos.RCcsLoan;
import com.sunline.ccs.infrastructure.server.repos.RCcsLoanReg;
import com.sunline.ccs.infrastructure.server.repos.RCcsRepaySchedule;
import com.sunline.ccs.infrastructure.server.repos.RCcsRepayScheduleHst;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsRepaySchedule;
import com.sunline.ccs.infrastructure.shared.model.CcsRepayScheduleHst;
import com.sunline.ccs.infrastructure.shared.model.QCcsRepaySchedule;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ccs.service.context.TxnContext;
import com.sunline.ccs.service.context.TxnInfo;
import com.sunline.ccs.service.handler.AppapiCommService;
import com.sunline.ccs.service.handler.OpenAcctCommService;
import com.sunline.ccs.service.handler.QueryCommService;
import com.sunline.ccs.service.msentity.TNMUpdateBillDateReq;
import com.sunline.ccs.service.msentity.TNMUpdateBillDateResp;
import com.sunline.ccs.service.util.DateFormatUtil;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanAction;
import com.sunline.ppy.dictionary.enums.LoanRegStatus;
import com.sunline.ppy.dictionary.enums.LoanStatus;
import com.sunline.ppy.dictionary.enums.MsRespCode;
import com.sunline.ppy.dictionary.exception.ProcessException;


/**
 * 变更换款日
 * @author zhengjf
 *
 */
@Service
public class TNMUpdateBillDate {
	
	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private AppapiCommService appapiCommService;
	@Autowired
	private OpenAcctCommService openAcctCommService;
	@Autowired
	private QueryCommService queryCommService;
	@Autowired
	private RCcsLoan rCcsLoan;
	@Autowired
	private RCcsLoanReg rCcsLoanReg;
	@Autowired
	private RCcsRepayScheduleHst rScheduleHst;
	@Autowired
	private RCcsRepaySchedule rSchedule;
	@Autowired
	private RCcsAcct rCcsAcct;
	@Autowired
	public MicroCreditRescheduleUtils microCreditRescheduleUtils;
	@Value("#{env.respDateFormat}")
	private String respDateFormat;
	
	
	public TNMUpdateBillDateResp handler(TNMUpdateBillDateReq req) throws ProcessException{
	
		LogTools.printLogger(log, "TNMUpdateBillDateReq", "变更换款日", req, true);
		LogTools.printObj(log, req, "请求参数TNMUpdateBillDateReq");
		TNMUpdateBillDateResp resp = new TNMUpdateBillDateResp();
		TxnContext context = new TxnContext();
		TxnInfo txnInfo = new TxnInfo();
		context.setTxnInfo(txnInfo);
		try{
			
			this.initTxnInfo(req, txnInfo);
			
			this.checkReq(txnInfo);
			
			appapiCommService.loadAcctByContrNo(context, true);
			appapiCommService.loadAcctOByAcct(context, true);
			appapiCommService.loadLoanByContrNo(context,LoanStatus.A, true);
			appapiCommService.loadLoanRegByDueBillNo(context,null,LoanRegStatus.A,null, false);
//			appapiCommService.loadLoanRegHstByDueBillNo(context,null,null,null,context.getLoan().getPayDateAccu(),true);
			openAcctCommService.getProdParam(context);
			
			this.validateForApply(context);
			
			//业务处理
			this.bizProc(context);
			
			//组装响应报文
			this.setResponse(resp, context);
			
		}
		catch(ProcessException pe){
			if(log.isErrorEnabled())
				log.error(pe.getMessage(),pe);
			appapiCommService.preException(pe, pe, txnInfo);
		}
		catch(Exception e){
			if(log.isErrorEnabled())
				log.error(e.getMessage(),e);
			appapiCommService.preException(e, null, txnInfo);				
		}finally{
			LogTools.printLogger(log, "TNMUpdateBillDateResp", "变更换款日", resp, false);
		}
		
		//组装响应码
		setResponse(resp,  req, txnInfo);
		
		return resp;
	}
	
	
	/**
	 * 初始化交易中间信息
	 * @param req
	 * @param txnInfo
	 */
	private void initTxnInfo(TNMUpdateBillDateReq req, TxnInfo txnInfo) {
		
		txnInfo.setBizDate(req.getBizDate());
		txnInfo.setCycleDay(req.payStmtDate);
		txnInfo.setContrNo(req.contrNbr);
		
	}
	
	/**
	 * 检查报文
	 * @param req
	 */
	private void checkReq(TxnInfo txnInfo) {
		
		int day = Integer.parseInt(txnInfo.getCycleDay());
		if(day <= 0 || day >= 29){
			
			throw new ProcessException(MsRespCode.E_1080.getCode(),MsRespCode.E_1080.getMessage());
		}
		
	}
	
	/**
	 * 业务检查
	 * @param context
	 */
	private void validateForApply(TxnContext context) {
		
		TxnInfo txnInfo = context.getTxnInfo();
		CcsAcct acct = context.getAccount();
		CcsAcctO accto = context.getAccounto();
		CcsLoan loan = context.getLoan();
		LoanFeeDef loanFeeDef = context.getLoanFeeDef();
		
		// 批量时间检查
		queryCommService.batchProcessingCheck(acct, accto);
		
		//变更还款日次数
		int count = loan.getPayDateAccu() == null ? 0 : loan.getPayDateAccu();
		
		if(log.isDebugEnabled())
			log.debug("绑定灵活还款服务标示:[{}],已变更次数:[{}],产品可变更次数：[{}]",acct.getPrepayPkgInd(),count,loanFeeDef.payDateAccuMax);
		if(log.isDebugEnabled())
			log.debug("变更还款日首次申请足额还款期数:[{}],变更还款日再次申请距离上次足额偿还期数：[{}]",loanFeeDef.payDateFirstApplyTerm,loanFeeDef.payDateApplyAgainTerm);
		
		//检查是否绑定了灵活的还款服务
		if (acct.getPrepayPkgInd() != Indicator.Y) {
			throw new ProcessException(MsRespCode.E_1081.getCode(),MsRespCode.E_1081.getMessage());
		}
		//账单日当天不允许做更换账单日交易
		if (DateUtils.getIntervalDays(txnInfo.getBizDate(),acct.getNextStmtDate()) == 0) {
			throw new ProcessException(MsRespCode.E_1004.getCode(),MsRespCode.E_1004.getMessage()+"账单日当天不允许做更换账单日交易");
		}
		//检查是否超过了变更次数
		if (count >= (loanFeeDef.payDateAccuMax == null ? 0 : loanFeeDef.payDateAccuMax)) {
			throw new ProcessException(MsRespCode.E_1082.getCode(),MsRespCode.E_1082.getMessage());
		}
		//判断是否逾期
		if (loan.getOverdueDate() != null || loan.getCpdBeginDate() != null) {
			throw new ProcessException(MsRespCode.E_1025.getCode(),"逾期不能修改账单日");
		}
		//如果是第一次更改账单日 必须还满N期才能更改
		if (count <= 0 && loan.getCurrTerm() < (loanFeeDef.payDateFirstApplyTerm == null ? 0 : loanFeeDef.payDateFirstApplyTerm)) {
			throw new ProcessException(MsRespCode.E_1083.getCode(),MsRespCode.E_1083.getMessage().replace("N", loanFeeDef.payDateFirstApplyTerm.toString()));
		}
		//如果不是第一次更改账单日 则中间必须有N期还款
		if (count > 0 && loan.getCurrTerm() - loan.getPayDateTerm() < (loanFeeDef.payDateApplyAgainTerm == null ? 0 : loanFeeDef.payDateApplyAgainTerm)) {
			throw new ProcessException(MsRespCode.E_1083.getCode(),MsRespCode.E_1083.getMessage().replace("N", loanFeeDef.payDateApplyAgainTerm.toString()));
		}
		//该合同有预约提前还款时，不能发起更换账单日。
		if (context.getLoanReg() != null) {
			if(LoanAction.O.equals(context.getLoanReg().getLoanAction())){
				throw new ProcessException(MsRespCode.E_1073.getCode(),MsRespCode.E_1073.getMessage());
			}
		}
		
	}
	
	/**
	 * 业务处理
	 * @param context
	 */
	@Transactional
	private void bizProc(TxnContext context) {
		
		CcsLoan loan = context.getLoan();
		CcsLoanReg loanReg = context.getLoanReg();

		// 获取loan下schedule
		List<CcsRepaySchedule> scheduleList = queryCommService.getSchedulelistByLoan(loan.getLoanId());

		
		//更新下个账单日 及 计算更改账单日后开始日期
		Date cycleDay = this.getNextDate(context);
		
		if (loanReg == null) {
			try {
				
				//新建loanreg
				loanReg = this.genLoanReg(context);
				
				context.setLoanReg(loanReg);
				
				rCcsLoanReg.save(loanReg);
				
			} catch (ProcessException e) {
				throw new ProcessException(MsRespCode.E_1044.getCode(), MsRespCode.E_1044.getMessage());
			}
		}
		
		//对loanreg loan 上面的灵活还款服务字段赋值
		this.updatLoanReg(context, cycleDay);
		//更新表
		this.update(context, cycleDay, scheduleList);
		
	}
	
	/**
	 * 计算更改后的账单日
	 * @param context
	 * @return
	 */
	public Date getNextDate(TxnContext context){
		
		QCcsRepaySchedule qSchedule = QCcsRepaySchedule.ccsRepaySchedule;
		TxnInfo txnInfo = context.getTxnInfo();
		CcsAcct acct = context.getAccount();
		CcsLoan loan = context.getLoan();
		LoanFeeDef loanFeeDef = context.getLoanFeeDef();
		Calendar c = Calendar.getInstance();
		Date cycleDay = txnInfo.getBizDate();
		txnInfo.setOrginCycleDay(context.getAccount().getCycleDay());
		int i;
		int date = -100;
		boolean mark = false;
		
		c.setTime(cycleDay);
		int month = c.get(Calendar.MONTH)+1;
		c.set(Calendar.DATE, Integer.parseInt(txnInfo.getCycleDay()));
		cycleDay = c.getTime();
		
		
		date = DateUtils.getIntervalDays(txnInfo.getBizDate(), cycleDay);
		
		CcsRepaySchedule schedule = rSchedule.findOne(qSchedule.loanPmtDueDate.month().eq(month)
				.and(qSchedule.acctNbr.eq(acct.getAcctNbr())));
		
		//如果当期日期大于账单日则从下个月开始
		if (schedule == null || DateUtils.getIntervalDays(schedule.getLoanPmtDueDate(), txnInfo.getBizDate()) > 0) {
			//当期日期距离设置账单日的天数
			date =loanFeeDef.payDateExpireAdvDays == null ? -1 : loanFeeDef.payDateExpireAdvDays -1;
			mark = true ;
		}
		
		//判断只要满足提前N天，就跳出
		for ( i = 0 ; date < (loanFeeDef.payDateExpireAdvDays == null ? 0 : loanFeeDef.payDateExpireAdvDays); i++) {
			
			c.add(Calendar.MONTH, 1);
			cycleDay = c.getTime();
			date = DateUtils.getIntervalDays(txnInfo.getBizDate(), cycleDay);
			
		}
		//如果过了账单日 减一期
		if (mark) {
			i = i - 1;
		}
		
		//如果变更后最近账单日为当月  或者  变更后最近账单日为下月 则跟新账单日
		if (i == 0) {
			
			acct.setNextStmtDate(cycleDay);
			acct.setCycleDay(txnInfo.getCycleDay());
//			//更新到期还款日
//			acct.setPmtDueDate(microCreditRescheduleUtils.getNextPaymentDay(
//					acct.getProductCd(), acct.getNextStmtDate()));
			
			//如果当期期数为零  则更新首个账单日
			if (loan.getCurrTerm() == 0) {
				acct.setFirstStmtDate(cycleDay);
			}
		}
		
		txnInfo.setVariable(i);
		
		if(log.isDebugEnabled())
			log.debug("计算后的账单日:[{}],变更后第几月生效:[{}],更改账单日提前提前天数：[{}],是否过账单日：[{}]"
					, cycleDay, txnInfo.getVariable(), loanFeeDef.payDateAccuMax, mark);
		
		return cycleDay;
	}
	
	/**
	 * 新建注册表
	 * @param context
	 * @param cycleDay
	 * @return
	 */
	public CcsLoanReg genLoanReg(TxnContext context) {
		
		CcsLoan loan = context.getLoan();
		TxnInfo txnInfo = context.getTxnInfo();
		
		CcsLoanReg loanReg=appapiCommService.genLoanReg(0,BigDecimal.ZERO, 
				loan.getRefNbr(), loan.getCardNbr(), 
				loan.getCardNbr(), loan.getAcctNbr(), loan.getAcctType(),
				null,txnInfo.getBizDate());
		
		//赋值账户信息	
		this.setLoanReg(loanReg,context.getLoan());

		return loanReg;
	}
	
	
	public void update(TxnContext context,Date cycleDay, List<CcsRepaySchedule> scheduleList) {
		
		Calendar c = Calendar.getInstance();
		TxnInfo txnInfo = context.getTxnInfo();
		CcsLoan loan = context.getLoan();
		CcsAcct acct = context.getAccount();
		CcsLoanReg loanReg = context.getLoanReg();
		
		//转化map
		Map<Integer, CcsRepaySchedule> scheduleMap = this.scheduleMap(scheduleList);
		
		for (int i = 1 ; i <= scheduleList.size() ; i++) {
			CcsRepaySchedule schedule = scheduleMap.get(i);
			//判断从第几期开始更改账单日
			if (schedule.getCurrTerm() > loan.getCurrTerm() + txnInfo.getVariable()) {
				CcsRepayScheduleHst scheduleHst = new CcsRepayScheduleHst();
				scheduleHst.updateFromMap(schedule.convertToMap());
				
				//更新未到期的schedule到期还款日
				schedule.setLoanPmtDueDate(microCreditRescheduleUtils.getNextPaymentDay(
						acct.getProductCd(), cycleDay));
				schedule.setLoanGraceDate(microCreditRescheduleUtils.getNextGraceDay(
						acct.getProductCd(), cycleDay));
				
				scheduleHst.setRegisterId(loanReg.getRegisterId());
				
				rScheduleHst.save(scheduleHst);
				rSchedule.save(schedule);
				//日期加一个月 算到期还款日
				c.setTime(cycleDay);
				c.add(Calendar.MONTH, 1);
				cycleDay = c.getTime();
			}
			
			//更新loan到期还款日acct账户有效期
			if (schedule.getCurrTerm() == loan.getLoanInitTerm()) {
				loan.setLoanExpireDate(schedule.getLoanPmtDueDate());
				acct.setAcctExpireDate(schedule.getLoanPmtDueDate());
			}
		}
		
		rCcsAcct.save(acct);
		rCcsLoan.save(loan);
		rCcsLoanReg.save(loanReg);
	}
	
	/**
	 * 设置更换账单日注册信息
	 * @param loanReg
	 * @param loan
	 */
	private void setLoanReg(CcsLoanReg loanReg, CcsLoan loan) {
		loanReg.setOrg(loan.getOrg());
		loanReg.setAcctNbr(loan.getAcctNbr());
		loanReg.setAcctType(loan.getAcctType());
		loanReg.setRequestTime(new Date());
		loanReg.setLogicCardNbr(loan.getLogicCardNbr());//LoanRegStatus
		loanReg.setCardNbr(loan.getCardNbr());
		loanReg.setLoanRegStatus(LoanRegStatus.A);
		loanReg.setLoanAction(LoanAction.U);//GUARANTY_ID
		loanReg.setDueBillNo(loan.getDueBillNo());
		loanReg.setContrNbr(loan.getContrNbr());
		loanReg.setGuarantyId(loan.getGuarantyId());		
		loanReg.setLoanCode(loan.getLoanCode());
		loanReg.setPremiumAmt(loan.getPremiumAmt());
		loanReg.setPremiumInd(loan.getPremiumInd());
		
	}

	/**
	 * 组装响应报文
	 * @param resp
	 * @param req
	 * @param txnInfo
	 */
	private void setResponse(TNMUpdateBillDateResp resp, TxnContext context) {
		
		TxnInfo txnInfo = context.getTxnInfo();
		
		resp.setContrNbr(txnInfo.getContrNo());
		resp.setPayStmtDate(txnInfo.getCycleDay());
		resp.setStmtDate(DateFormatUtil.format(
				context.getAccount().getNextStmtDate(), respDateFormat));
		
	}

	/**
	 * 组装响应报文
	 * @param resp
	 * @param req
	 * @param txnInfo
	 */
	private void setResponse(TNMUpdateBillDateResp resp,TNMUpdateBillDateReq req,  TxnInfo txnInfo) {
		
		resp.setErrorCode(txnInfo.getResponsCode());
		resp.setErrorMessage(txnInfo.getResponsDesc());
		if(StringUtils.equals(MsRespCode.E_0000.getCode(), txnInfo.getResponsCode())){
			resp.setStatus("S");//交易成功
		}else{
			resp.setStatus("F");//交易失败
		}
	}
	
	/**
	 * 对灵活还款服务字段赋值
	 * @param context
	 * @param cycleDay
	 */
	private void updatLoanReg(TxnContext context, Date cycleDay) {
		
		TxnInfo txnInfo = context.getTxnInfo();
		CcsLoan loan = context.getLoan();
		CcsLoanReg loanReg = context.getLoanReg();
		
		loanReg.setLoanAction(LoanAction.U);
		loanReg.setPayDateTerm(loan.getCurrTerm());
		loanReg.setPayDateBegDate(cycleDay);
		loanReg.setPayDateAfter(Integer.parseInt(txnInfo.getCycleDay()));
		loanReg.setPayDateOrigin(Integer.parseInt(txnInfo.getOrginCycleDay()));
		loanReg.setPayDateAccu(
				loan.getPayDateAccu() == null ? 1 : loan.getPayDateAccu()+1);
		//对loan进行赋值
		loan.setPayDateTerm(loanReg.getPayDateTerm());
		loan.setPayDateBegDate(loanReg.getPayDateBegDate());
		loan.setPayDateAccu(loanReg.getPayDateAccu());

	}
	
	/*
	 * 把schedule变为map key为schedule期数
	 */
	private Map<Integer, CcsRepaySchedule> scheduleMap(List<CcsRepaySchedule> scheduleList) {
		
		Map<Integer, CcsRepaySchedule> tmpTotAmteMap = new HashMap<Integer, CcsRepaySchedule>();
		for (CcsRepaySchedule schedule : scheduleList) {
			tmpTotAmteMap.put(schedule.getCurrTerm(), schedule);
		}
		return tmpTotAmteMap;
	}
	
//	/**
//	 * 获取自然月的schedule
//	 * @param scheduleList
//	 * @param month
//	 * @return
//	 */
//	private CcsRepaySchedule getSchedule(List<CcsRepaySchedule> scheduleList, int month) {
//		
//		Calendar c = Calendar.getInstance();
//		
//		CcsRepaySchedule schedule = new CcsRepaySchedule();
//		
//		for (CcsRepaySchedule ccsRepaySchedule : scheduleList) {
//			
//			c.setTime(ccsRepaySchedule.getLoanPmtDueDate());
//			
//			if (c.get(Calendar.MONTH) == month) {
//				return ccsRepaySchedule;
//			}
//			
//		}
//		
//		
//		return schedule;
//	}
}
