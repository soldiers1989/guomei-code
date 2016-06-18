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
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ccs.service.context.TxnContext;
import com.sunline.ccs.service.context.TxnInfo;
import com.sunline.ccs.service.handler.AppapiCommService;
import com.sunline.ccs.service.handler.OpenAcctCommService;
import com.sunline.ccs.service.handler.QueryCommService;
import com.sunline.ccs.service.msentity.TNMApplyDelayReq;
import com.sunline.ccs.service.msentity.TNMApplyDelayResp;
import com.sunline.ccs.service.util.DateFormatUtil;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanAction;
import com.sunline.ppy.dictionary.enums.LoanRegStatus;
import com.sunline.ppy.dictionary.enums.LoanStatus;
import com.sunline.ppy.dictionary.enums.MsRespCode;
import com.sunline.ppy.dictionary.exception.ProcessException;


/**
 * 延期申请
 * @author Mr.L
 *
 */
@Service
public class TNMApplyDelay {
	
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
	private MicroCreditRescheduleUtils rescheduleUtils;
	@Autowired
	public MicroCreditRescheduleUtils microCreditRescheduleUtils;
	@Value("#{env.respDateFormat}")
	private String respDateFormat;
	
	
	
	public TNMApplyDelayResp handler(TNMApplyDelayReq req) throws ProcessException{
	
		LogTools.printLogger(log, "TNMApplyDelayReq", "延期还款申请", req, true);
		LogTools.printObj(log, req, "请求参数TNMApplyDelayReq");
		TxnContext context = new TxnContext();
		TxnInfo txnInfo = new TxnInfo();
		context.setTxnInfo(txnInfo);
		TNMApplyDelayResp resp = new TNMApplyDelayResp();
		try{
			
			this.initTxnInfo(req, txnInfo);
			
			this.checkReq(txnInfo);
			
			appapiCommService.loadAcctByContrNo(context, true);
			appapiCommService.loadAcctOByAcct(context, true);
			appapiCommService.loadLoanByContrNo(context,LoanStatus.A, true);
			appapiCommService.loadLoanRegByDueBillNo(context,null,LoanRegStatus.A,null, false);
			openAcctCommService.getProdParam(context);
			
			//业务检查
			this.validateForApply(context);
			
			Date pmtDueDate = this.getNextDate(context, resp);
			
			//业务处理
			this.bizProc(context,resp,pmtDueDate);
			
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
			LogTools.printLogger(log, "TNMApplyDelayResp", "变更换款日", resp, false);
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
	private void initTxnInfo(TNMApplyDelayReq req, TxnInfo txnInfo) {
		
		txnInfo.setBizDate(req.getBizDate());
		txnInfo.setApplyDelayTerm(req.applyDelayTerm);
		txnInfo.setContrNo(req.contrNbr);
		
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
		
		//变更延期还款次数
		int count = loan.getAccuDelayCount() == null ? 0 : loan.getAccuDelayCount();
		
		//变更延期期数
		int term = loan.getAccuDelayCount() == null ? 0 : loan.getAccuDelayCount();
		
		if(log.isDebugEnabled())
			log.debug("绑定灵活还款服务标示:[{}],已变更次数:[{}],产品可变更次数：[{}]",acct.getPrepayPkgInd(),count,loanFeeDef.delayApplyMax);
		if(log.isDebugEnabled())
			log.debug("延期首次申请足额还款期数:[{}],延期再次申请距离上次足额偿还期数：[{}]",loanFeeDef.delayFristApplyTerm,loanFeeDef.delayApplyAgainTerm);
		
		//检查是否绑定了灵活的还款服务
		if (acct.getPrepayPkgInd() != Indicator.Y) {
			throw new ProcessException(MsRespCode.E_1081.getCode(),MsRespCode.E_1081.getMessage());
		}
		//检查是否超过单次变更期数
		if (txnInfo.getApplyDelayTerm() > (loanFeeDef.delayMaxTerm == null ? 0 : loanFeeDef.delayMaxTerm)) {
			throw new ProcessException(MsRespCode.E_1085.getCode(),MsRespCode.E_1085.getMessage().replace("N", (loanFeeDef.delayMaxTerm == null ? "0" : loanFeeDef.delayMaxTerm).toString()));
		}
		//检查是否超过了累计变更期数
		if (txnInfo.getApplyDelayTerm() + term > (loanFeeDef.delayAccuMaxTerm == null ? 0 : loanFeeDef.delayAccuMaxTerm)) {
			throw new ProcessException(MsRespCode.E_1086.getCode(),MsRespCode.E_1086.getMessage().replace("N", (loanFeeDef.delayAccuMaxTerm == null ? "0" : loanFeeDef.delayAccuMaxTerm).toString()));
		}
		//检查是否超过了变更次数
		if (count >= (loanFeeDef.delayApplyMax == null ? 0 : loanFeeDef.delayApplyMax)) {
			throw new ProcessException(MsRespCode.E_1082.getCode(),MsRespCode.E_1082.getMessage());
		}
		//判断是否逾期
		if (loan.getOverdueDate() != null || loan.getCpdBeginDate() != null) {
			throw new ProcessException(MsRespCode.E_1025.getCode(),"逾期不能修改账单日");
		}
		//如果是第一次延期 必须还满N期才能更改
		if (count <= 0 && loan.getCurrTerm() < (loanFeeDef.delayFristApplyTerm == null ? 0 : loanFeeDef.delayFristApplyTerm)) {
			throw new ProcessException(MsRespCode.E_1083.getCode(),MsRespCode.E_1083.getMessage().replace("N", loanFeeDef.delayFristApplyTerm.toString()));
		}
		//如果不是第一次延期 则中间必须有N期还款
		if (count > 0 && loan.getCurrTerm() - loan.getApplyTerm() < (loanFeeDef.delayApplyAgainTerm == null ? 0 : loanFeeDef.delayApplyAgainTerm)) {
			throw new ProcessException(MsRespCode.E_1083.getCode(),MsRespCode.E_1083.getMessage().replace("N", loanFeeDef.delayApplyAgainTerm.toString()));
		}
		//该合同有预约提前还款时，不能发起延期。
		if (null != context.getLoanReg()) {
			if(LoanAction.O.equals(context.getLoanReg().getLoanAction())){
				throw new ProcessException(MsRespCode.E_1073.getCode(),MsRespCode.E_1073.getMessage());
			}
		}
		//申请日限制至少要比下一期还款日提前N天
		if(DateUtils.getIntervalDays(txnInfo.getBizDate(), context.getAccount().getNextStmtDate()) < (loanFeeDef.delayApplyAdvDays==null ? 0 :loanFeeDef.delayApplyAdvDays)){
			throw new ProcessException(MsRespCode.E_1084.getCode(),MsRespCode.E_1084.getMessage().replace("N", loanFeeDef.delayApplyAdvDays.toString()));
		}
	}
	
	/**
	 * 业务处理
	 * @param context
	 */
	@Transactional
	private void bizProc(TxnContext context,TNMApplyDelayResp resp,Date pmtdueDate) {
		
		CcsLoan loan = context.getLoan();
		CcsLoanReg loanReg = context.getLoanReg();
	
		// 获取loan下schedule
		List<CcsRepaySchedule> scheduleList = queryCommService.getSchedulelistByLoan(loan.getLoanId());
		
		if (loanReg == null) {
			try {
				
				//新建loanreg
				loanReg = this.genLoanReg(context,resp);
				
				context.setLoanReg(loanReg);
				
				rCcsLoanReg.save(loanReg);
				
			} catch (ProcessException e) {
				throw new ProcessException(MsRespCode.E_1044.getCode(), MsRespCode.E_1044.getMessage());
			}
		}
		
		//对loanreg loan 上面的灵活还款服务字段赋值
		this.updatLoanReg(context);
		
		this.update(context, scheduleList,resp,pmtdueDate);
		
	}
	
	/**
	 * 计算更改后的到期还款日与账单日
	 * @param context
	 * @return
	 */
	public Date getNextDate(TxnContext context,TNMApplyDelayResp resp){
		
		CcsAcct acct = context.getAccount();
		TxnInfo txnInfo = context.getTxnInfo();
		CcsLoan loan = context.getLoan();
		
		//下一到期还款日
		Date nextPmtDueDate = rescheduleUtils.getNextPaymentDay(acct.getProductCd(), acct.getNextStmtDate());
		// 延期还款后的下一到期还款日期
		Date pmtDueDate = DateUtils.addMonths(nextPmtDueDate, txnInfo.getApplyDelayTerm());
		resp.setNextPmtDueDate(DateFormatUtil.format(pmtDueDate,respDateFormat)); 
		//下一个账单日
		Date stmtDate = DateUtils.addMonths(acct.getNextStmtDate(), txnInfo.getApplyDelayTerm());
		//
		acct.setAcctExpireDate(DateUtils.addMonths(acct.getAcctExpireDate(), txnInfo.getApplyDelayTerm()));
		loan.setLoanExpireDate(DateUtils.addMonths(loan.getLoanExpireDate(), txnInfo.getApplyDelayTerm()));
		//更新下一账单日
		acct.setNextStmtDate(stmtDate);
		//更新到期还款日
		acct.setPmtDueDate(pmtDueDate);
		return pmtDueDate;
	}
	
	/**
	 * 新建注册表
	 * @param context
	 * @param cycleDay
	 * @return
	 */
	public CcsLoanReg genLoanReg(TxnContext context,TNMApplyDelayResp resp) {
		
		CcsLoan loan = context.getLoan();
		TxnInfo txnInfo = context.getTxnInfo();
		CcsLoanReg loanReg=appapiCommService.genLoanReg(0,BigDecimal.ZERO, 
				loan.getRefNbr(), loan.getCardNbr(), 
				loan.getCardNbr(), loan.getAcctNbr(), loan.getAcctType(),
				null,txnInfo.getBizDate());
		
		//赋值loanreg信息	
		this.setLoanReg(loanReg,context.getLoan());

		return loanReg;
	}
	
	
	public void update(TxnContext context, List<CcsRepaySchedule> scheduleList,TNMApplyDelayResp resp,Date pmtdueDate) {
		
		Calendar c = Calendar.getInstance();
		CcsLoan loan = context.getLoan();
		CcsAcct acct = context.getAccount();
		CcsLoanReg loanReg = context.getLoanReg();
		
		//转化map
		Map<Integer, CcsRepaySchedule> scheduleMap = this.scheduleMap(scheduleList);
		
		for (int i = 1 ; i <= scheduleList.size() ; i++) {
			CcsRepaySchedule schedule = scheduleMap.get(i);
			
			//判断从第几期开始更改账单日
			if (schedule.getCurrTerm()> loan.getCurrTerm()) {
				
				CcsRepayScheduleHst scheduleHst = new CcsRepayScheduleHst();
				scheduleHst.updateFromMap(schedule.convertToMap());
				
				//更新未到期的schedule到期还款日
				schedule.setLoanPmtDueDate(microCreditRescheduleUtils.getNextPaymentDay(
						acct.getProductCd(), pmtdueDate));
				schedule.setLoanGraceDate(microCreditRescheduleUtils.getNextGraceDay(
						acct.getProductCd(), pmtdueDate));
				
				scheduleHst.setRegisterId(loanReg.getRegisterId());
				
				rScheduleHst.save(scheduleHst);
				rSchedule.save(schedule);
				
				//日期加一个月 算到期还款日
				c.setTime(pmtdueDate);
				c.add(Calendar.MONTH, 1);
				pmtdueDate = c.getTime();
	
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
	 * @param txnInfo 
	 */
	private void setLoanReg(CcsLoanReg loanReg, CcsLoan loan) {
		loanReg.setOrg(loan.getOrg());
		loanReg.setAcctNbr(loan.getAcctNbr());
		loanReg.setAcctType(loan.getAcctType());
		loanReg.setRequestTime(new Date());
		loanReg.setLogicCardNbr(loan.getLogicCardNbr());//LoanRegStatus
		loanReg.setCardNbr(loan.getCardNbr());
		loanReg.setLoanRegStatus(LoanRegStatus.A);
		loanReg.setLoanAction(LoanAction.D);//GUARANTY_ID
		loanReg.setDueBillNo(loan.getDueBillNo());
		loanReg.setContrNbr(loan.getContrNbr());
		loanReg.setGuarantyId(loan.getGuarantyId());		
		loanReg.setLoanCode(loan.getLoanCode());
		loanReg.setPremiumAmt(loan.getPremiumAmt());
		loanReg.setPremiumInd(loan.getPremiumInd());
		loanReg.setLoanInitPrin(loan.getLoanInitPrin());
		loanReg.setLoanInitTerm(loan.getLoanInitTerm());
	
		
	}
	
	/**
	 * 检查报文
	 * @param req
	 */
	private void checkReq(TxnInfo txnInfo) {
		
		if(txnInfo.getApplyDelayTerm() <= 0){
			
			throw new ProcessException(MsRespCode.E_1008.getCode(),MsRespCode.E_1008.getMessage());
		}
	}
	
	/**
	 * 对灵活还款服务字段赋值
	 * @param context
	 * @param cycleDay
	 */
	private void updatLoanReg(TxnContext context) {
		
		TxnInfo txnInfo = context.getTxnInfo();
		CcsLoan loan = context.getLoan();
		CcsLoanReg loanReg = context.getLoanReg();
		
		loanReg.setLoanAction(LoanAction.D);
		loanReg.setApplyTerm(loan.getCurrTerm());
		loanReg.setApplyDelayTerm(txnInfo.getApplyDelayTerm());
		//对loan进行赋值
		loan.setApplyDelayTerm(loanReg.getApplyDelayTerm());
		loan.setApplyTerm(loanReg.getApplyTerm());
		loan.setAccuApplyCount(loan.getAccuDelayCount() == null ? 1 : loan.getAccuDelayCount()+1);
		loan.setAccuDelayCount(loan.getAccuDelayCount() == null ? txnInfo.getApplyDelayTerm() : loan.getAccuDelayCount() + txnInfo.getApplyDelayTerm());
				
	}

	/**
	 * 组装响应报文
	 * @param resp
	 * @param req
	 * @param txnInfo
	 */
	private void setResponse(TNMApplyDelayResp resp,TNMApplyDelayReq req,  TxnInfo txnInfo) {
		
		resp.setErrorCode(txnInfo.getResponsCode());
		resp.setErrorMessage(txnInfo.getResponsDesc());
		resp.setApplyDelayTerm(txnInfo.getApplyDelayTerm());
		resp.setContrNbr(txnInfo.getContrNo());
		if(StringUtils.equals(MsRespCode.E_0000.getCode(), txnInfo.getResponsCode())){
			resp.setStatus("S");//交易成功
		}else{
			resp.setStatus("F");//交易失败
		}
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
}
