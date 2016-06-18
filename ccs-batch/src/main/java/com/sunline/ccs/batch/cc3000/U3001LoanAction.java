package com.sunline.ccs.batch.cc3000;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ccs.batch.cc3000.cancle.AutoCancle;
import com.sunline.ccs.batch.common.DateUtils;
import com.sunline.ccs.batch.common.EnumUtils;
import com.sunline.ccs.batch.common.SettleRecordUtil;
import com.sunline.ccs.batch.common.TxnPrepare;
import com.sunline.ccs.facility.BlockCodeUtils;
import com.sunline.ccs.facility.Card2ProdctAcctFacility;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.MicroCreditRescheduleUtils;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.CcsPostingTmp;
import com.sunline.ccs.infrastructure.shared.model.CcsRepaySchedule;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnHst;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnUnstatement;
import com.sunline.ccs.infrastructure.shared.model.QCcsPlan;
import com.sunline.ccs.infrastructure.shared.model.QCcsTxnHst;
import com.sunline.ccs.infrastructure.shared.model.QCcsTxnUnstatement;
import com.sunline.ccs.loan.McLoanProvideImpl;
import com.sunline.ccs.loan.McLoanProvideImpl.EarlySettleMethodimple;
import com.sunline.ccs.loan.McLoanProvideImpl.ReplacePrepayMethodimple;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.SysTxnCdMapping;
import com.sunline.ccs.param.def.enums.LoanTerminateReason;
import com.sunline.ccs.param.def.enums.SysTxnCd;
import com.sunline.pcm.param.def.FinancialOrg;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.DbCrInd;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanAction;
import com.sunline.ppy.dictionary.enums.LoanStatus;
import com.sunline.ppy.dictionary.enums.PlanType;

@Component
public class U3001LoanAction {
	
	private static final String ERR_LOANSTATUS_NOT_T_F_R = "展期状态不正确";
	
	@Autowired
	private CustAcctCardFacility queryFacility;
	@Autowired
	private BatchStatusFacility batchFacility;
	@Autowired
	private TxnPrepare txnPrepare;
	@Autowired
	private U3001LoanPrepare loanPrepare;
	@Autowired
	private MicroCreditRescheduleUtils rescheduleUtils;
	@PersistenceContext
	private EntityManager em;
	@Autowired
	private Card2ProdctAcctFacility cardProctFacility;
	@Autowired
    private UnifiedParameterFacility parameterFacility;
	@Autowired
	private BlockCodeUtils blockcodeUtils;
	@Autowired
	private McLoanProvideImpl mcLoanProvideImpl;
	@Autowired
	private SettleRecordUtil settleRecUtil;
	/**
	 * 普通分期展期
	 * 
	 * @param loanReg
	 * @param origLoan
	 * @throws Exception
	 */
	public void generalReschedule(CcsLoanReg loanReg, CcsLoan origLoan) throws Exception{
		
		if (!EnumUtils.in(origLoan.getLoanStatus(), LoanStatus.T,LoanStatus.F,LoanStatus.R)){
			// 展期
			origLoan.setExtendDate(batchFacility.getBatchDate());
			origLoan.setBefExtendInitTerm(origLoan.getLoanInitTerm());
			origLoan.setExtendInitPrin(origLoan.getUnstmtPrin());
			// 展期前本金
			origLoan.setBefExtendFirstTermPrin(origLoan.getLoanFirstTermPrin());
			origLoan.setBefExtendFixedPmtPrin(origLoan.getLoanFixedPmtPrin());
			origLoan.setBefExtendFinalTermPrin(origLoan.getLoanFinalTermPrin());
			// 展期前费用
			origLoan.setBefExtendInitFee(origLoan.getLoanInitFee());
			origLoan.setBefExtendFirstTermFee(origLoan.getLoanFirstTermFee());
			origLoan.setBefExtendFixedFee(origLoan.getLoanFixedFee());
			origLoan.setBefExtendFinalTermFee(origLoan.getLoanFinalTermFee());
			// 展期后本金(loanFirstTermFee1不更新)
			origLoan.setLoanFixedPmtPrin(loanReg.getLoanFixedPmtPrin());
			origLoan.setLoanFinalTermPrin(loanReg.getLoanFinalTermPrin());
			// 展期后费用(loanFirstTermPrin不更新)
			origLoan.setExtendFirstTermFee(loanReg.getLoanFirstTermFee());
			origLoan.setLoanInitFee(origLoan.getLoanFeeXfrin().add(loanReg.getLoanInitFee()));
			origLoan.setLoanFixedFee(loanReg.getLoanFixedFee());
			origLoan.setLoanFinalTermFee(loanReg.getLoanFinalTermFee());
			origLoan.setUnstmtFee(loanReg.getLoanInitFee());
			// 展期后
			origLoan.setLoanInitTerm(loanReg.getLoanInitTerm());
			origLoan.setRemainTerm(origLoan.getLoanInitTerm() - origLoan.getCurrTerm());
			origLoan.setLoanFeeXfrout(loanReg.getLoanInitFee());
			origLoan.setLoanFeeMethod(loanReg.getLoanFeeMethod());
			origLoan.setLoanStatus(LoanStatus.R);
			
			em.persist(origLoan);
			
			// 展期手续费
			CcsPostingTmp feeTxn = loanPrepare.generatePostingTmp(loanReg, SysTxnCd.S38, loanReg.getCardNbr(), loanReg.getLogicCardNbr(), null, null, loanReg.getLoanSvcFee(), null);
			txnPrepare.txnPrepare(feeTxn, null);
		}else{
			loanReg.setRemark(ERR_LOANSTATUS_NOT_T_F_R);
		}
		
	}
	
	
	/**
	 * 普通分期提前还款(全额)
	 * 
	 * @param loanReg
	 * @throws Exception
	 */
	public void generalPrepayment(CcsLoanReg loanReg) throws Exception{
		// 金额不为0的应收手续费、应退手续费
		if(loanReg.getLoanSvcFee().compareTo(BigDecimal.ZERO) >0){
			CcsPostingTmp feeTxn = loanPrepare.generatePostingTmp(loanReg, SysTxnCd.S37, loanReg.getCardNbr(), loanReg.getLogicCardNbr(), null, null, loanReg.getLoanSvcFee(), null);
			txnPrepare.txnPrepare(feeTxn, null);
		}
		if(loanReg.getLoanSvcFeeReturn().compareTo(BigDecimal.ZERO) >0){
			CcsPostingTmp returnFeeTxn = loanPrepare.generatePostingTmp(loanReg, SysTxnCd.S35, loanReg.getCardNbr(), loanReg.getLogicCardNbr(), null, null, loanReg.getLoanSvcFeeReturn(), null);
			txnPrepare.txnPrepare(returnFeeTxn, null);
		}
	}
	

	/**
	 * 小额贷款展期
	 * 
	 * @param output
	 * @param loanReg
	 * @param loan
	 * @param nextStmtDate
	 * @param loanFeeDef
	 * @param reSchedules
	 * @throws Exception
	 */
	public void microCreditReschedule(S3001LoanHandler output, CcsLoanReg loanReg, CcsLoan loan, 
			Date nextStmtDate, List<CcsRepaySchedule> origSchedules, List<CcsRepaySchedule> reSchedules, LoanFeeDef loanFeeDef) throws Exception {
		// 将原schedule搬到scheduleHst, 删除未到期的schedule进行重新分配
		for(CcsRepaySchedule schedule : origSchedules){
			// 将原schedule搬到scheduleHst
			em.persist(loanPrepare.generateRepayScheduleHst(schedule, loan.getRegisterId()));
			
			// 未到期分配计划, 删除, 需重新分配
			if(schedule.getCurrTerm() > loan.getCurrTerm()){
				em.remove(schedule);
			}
		}
		
		for(CcsRepaySchedule schedule : reSchedules){
			em.persist(schedule);
		}
		
		
		// 更新Loan信息
		loan.setLastLoanStatus(loan.getLoanStatus());
		loan.setLoanStatus(LoanStatus.R);
		loan.setLoanInitTerm(loanReg.getExtendTerm());
		loan.setRemainTerm(loanReg.getExtendTerm()-loan.getCurrTerm());
		loan.setRegisterId(loanReg.getRegisterId());
		loan.setLoanExpireDate(rescheduleUtils.getLoanPmtDueDate(nextStmtDate, loanFeeDef, loanReg.getExtendTerm()-loan.getCurrTerm()));
		loan.setPastExtendCnt(loan.getPastExtendCnt() +1);
		loan.setLastActionDate(batchFacility.getBatchDate());
		loan.setLastActionType(LoanAction.R);
		loan.setInterestRate(loanReg.getInterestRate());
		loan.setPenaltyRate(loanReg.getPenaltyRate());
		loan.setCompoundRate(loanReg.getCompoundRate());
		//TODO 行动手续费在loan上增加统计字段
		
		// 生成展期手续费
		CcsPostingTmp feeTxn = loanPrepare.generatePostingTmp(loanReg, SysTxnCd.S55, loanReg.getCardNbr(), loanReg.getLogicCardNbr(), null, null, loanReg.getLoanSvcFee(), loan.getCurrTerm()+1);
		txnPrepare.txnPrepare(feeTxn, null);
		
		// 短信
//		output.setRescheduleMsg(loanPrepare.makeRescheduleMsgItem(loanReg, loanReg.getExtendTerm()-loan.getCurrTerm(), loan.getUnstmtPrin(), loanReg.getLoanSvcFee()));
	}
	
	
	/**
	 * 小额贷款缩期
	 * 
	 * @param output
	 * @param loanReg
	 * @param loan
	 * @param nextStmtDate
	 * @param loanFeeDef
	 * @param reSchedules
	 * @throws Exception
	 */
	public void microCreditShorted(S3001LoanHandler output, CcsLoanReg loanReg, CcsLoan loan, 
			Date nextStmtDate, List<CcsRepaySchedule> origSchedules, List<CcsRepaySchedule> reSchedules, LoanFeeDef loanFeeDef) throws Exception {
		// 将原schedule搬到scheduleHst, 删除未到期的schedule进行重新分配
		for(CcsRepaySchedule schedule : origSchedules){
			// 将原schedule搬到scheduleHst
			em.persist(loanPrepare.generateRepayScheduleHst(schedule, loan.getRegisterId()));
			
			// 未到期分配计划, 删除, 需重新分配
			if(schedule.getCurrTerm() > loan.getCurrTerm()){
				em.remove(schedule);
			}
		}
		
		for(CcsRepaySchedule schedule : reSchedules){
			em.persist(schedule);
		}
		
		
		// 更新Loan信息
		loan.setLastLoanStatus(loan.getLoanStatus());
		loan.setLoanStatus(LoanStatus.S);
		loan.setLoanInitTerm(loanReg.getShortedTerm());
		loan.setRemainTerm(loanReg.getShortedTerm()-loan.getCurrTerm());
		loan.setRegisterId(loanReg.getRegisterId());
		loan.setLoanExpireDate(rescheduleUtils.getLoanPmtDueDate(nextStmtDate, loanFeeDef, loanReg.getShortedTerm()-loan.getCurrTerm()));
		loan.setPastShortenCnt(loan.getPastShortenCnt() +1);
		loan.setLastActionDate(batchFacility.getBatchDate());
		loan.setLastActionType(LoanAction.S);
		loan.setInterestRate(loanReg.getInterestRate());
		loan.setPenaltyRate(loanReg.getPenaltyRate());
		loan.setCompoundRate(loanReg.getCompoundRate());
		
		// 生成缩期手续费
		CcsPostingTmp feeTxn = loanPrepare.generatePostingTmp(loanReg, SysTxnCd.S56, loanReg.getCardNbr(), loanReg.getLogicCardNbr(), null, null, loanReg.getLoanSvcFee(), loan.getCurrTerm()+1);
		txnPrepare.txnPrepare(feeTxn, null);
		
		// 短信
//		output.setRescheduleMsg(loanPrepare.makeRescheduleMsgItem(loanReg, loanReg.getShortedTerm()-loan.getCurrTerm(), loan.getUnstmtPrin().subtract(loanReg.getShortedPmtDue()), loanReg.getLoanSvcFee()));
	}
	
	
	/**
	 * 小额贷款提前还款(全额)
	 * 
	 * @param output
	 * @param loanReg
	 * @throws Exception
	 */
	public void microCreditPrepayment(S3001LoanHandler output, CcsLoanReg loanReg) throws Exception{
		CcsLoan loan = loanPrepare.matchCcsLoan(loanReg);
		if(loan!=null){
			// 获取账户
			CcsAcct acct = queryFacility.getAcctByAcctNbr(loanReg.getAcctType(), loanReg.getAcctNbr());
			
			List<CcsRepaySchedule> origSchedules = rescheduleUtils.getRepayScheduleListByLoanId(loan.getLoanId());
			// 获取当期还款计划
			CcsRepaySchedule origNextSchedule = null;
			for (CcsRepaySchedule s : origSchedules) {
				if(s.getCurrTerm().intValue() == loan.getCurrTerm().intValue()+1){
					origNextSchedule = s;
					break;
				}
			}
			// 将原schedule搬到scheduleHst, 删除未到期的schedule进行重新分配
			for(CcsRepaySchedule schedule : origSchedules){
				// 将原schedule搬到scheduleHst
				em.persist(loanPrepare.generateRepayScheduleHst(schedule, loan.getRegisterId()));
				
				// 未到期分配计划, 删除, 需重新分配
				if(schedule.getCurrTerm() > loan.getCurrTerm()){
				    em.remove(schedule);
				}
			}
			
			// 重新进行分配
			em.persist(rescheduleUtils.genPrepayment(loanReg, loan, acct.getNextStmtDate(), origNextSchedule));
			
			// 更新Loan信息
			loan.setLastLoanStatus(loan.getLoanStatus());
			loan.setLoanInitTerm(loan.getCurrTerm()+1);
			loan.setRemainTerm(1);
			loan.setRegisterId(loanReg.getRegisterId());
			loan.setLoanExpireDate(acct.getNextStmtDate());
			loan.setLastActionDate(batchFacility.getBatchDate());
			loan.setLastActionType(LoanAction.P);
			loan.setInterestRate(loanReg.getInterestRate());
			loan.setPenaltyRate(loanReg.getPenaltyRate());
			loan.setCompoundRate(loanReg.getCompoundRate());
			loan.setAdvPmtAmt(loanReg.getAdvPmtAmt());
			
			// 手续费
			CcsPostingTmp feeTxn = loanPrepare.generatePostingTmp(loanReg, SysTxnCd.S67, loanReg.getCardNbr(), loanReg.getLogicCardNbr(), null, null, loanReg.getLoanSvcFee(), loan.getCurrTerm()+1);
			txnPrepare.txnPrepare(feeTxn, null);
			
			// 短信
//			output.setRescheduleMsg(loanPrepare.makeRescheduleMsgItem(loanReg, 1, loan.getUnstmtPrin().subtract(loanReg.getAdvPmtAmt()), loanReg.getLoanSvcFee()));
		}
	}

	/**
	 * 小额贷款提前还款(全额)
	 * 当天结清，保费，利息，印花税重新计算，按天计算到当天
	 * @param output
	 * @param loanReg
	 * @throws Exception
	 */
	public void MCLoanTodaySettlement(S3001LoanHandler output, CcsLoanReg loanReg) throws Exception{
		
		CcsLoan loan = loanPrepare.matchCcsLoan(loanReg);
		// 获取账户
		CcsAcct acct = queryFacility.getAcctByAcctNbr(loanReg.getAcctType(), loanReg.getAcctNbr());
		
		List<CcsRepaySchedule> origSchedules = rescheduleUtils.getRepayScheduleListByLoanId(loan.getLoanId());
		
		// 将原schedule搬到scheduleHst, 删除未到期的schedule进行重新分配
		for(CcsRepaySchedule schedule : origSchedules){
			// 未到期分配计划, 删除, 需重新分配
			if(schedule.getCurrTerm() > loan.getCurrTerm()){
				// 将原schedule搬到scheduleHst
				em.persist(loanPrepare.generateRepayScheduleHst(schedule, loan.getRegisterId()));
			    em.remove(schedule);
			}
		}
		CcsRepaySchedule newRepaySchedule = new CcsRepaySchedule();
		List<CcsRepaySchedule> newRefundScheduleList = new ArrayList<CcsRepaySchedule>();
		if(loanReg.getLoanAction().equals(LoanAction.C)){
			loan.setTerminalReasonCd(LoanTerminateReason.C);
			newRepaySchedule = this.loanSettlementC(loan, loanReg, origSchedules);
			acct.setBlockCode(blockcodeUtils.addBlockCode(acct.getBlockCode(), AutoCancle.I_CODE));
		}else if(loanReg.getLoanAction().equals(LoanAction.O)){
			loan.setTerminalReasonCd(LoanTerminateReason.P);
			newRepaySchedule = this.loanSettlementO(loan, loanReg, origSchedules);
		}else if (LoanAction.T.equals(loanReg.getLoanAction())) {
			loan.setTerminalReasonCd(LoanTerminateReason.T);
			newRefundScheduleList = this.loanSettlementT(acct, loan, loanReg, origSchedules);
		}
		
		//当日批量日期
		Date batDate = batchFacility.getBatchDate();
		//r如果新的还款计划时空的，说明贷款已经终止，不需要更新loan的状态--退货修改为不改账单日,单独处理
		if(LoanAction.T.equals(loanReg.getLoanAction())){
			// 找到最后的还款计划,重置loan的到期日期
			Date lastSchedulePmtDay = batDate;
			if(newRefundScheduleList!=null && newRefundScheduleList.size() > 0){
				for(CcsRepaySchedule refundSchedule : newRefundScheduleList){
					if(refundSchedule.getLoanPmtDueDate().compareTo(lastSchedulePmtDay) > 0){
						lastSchedulePmtDay = refundSchedule.getLoanPmtDueDate();
					}
					refundSchedule.setLoanInitPrin(loan.getLoanInitPrin());
					refundSchedule.setLoanInitTerm(loan.getLoanInitTerm());
					em.persist(refundSchedule);
				}
			}
			// 更新Loan信息
			loan.setLastLoanStatus(loan.getLoanStatus());
			loan.setRemainTerm(newRefundScheduleList.size());
			loan.setLoanExpireDate(lastSchedulePmtDay);
			loan.setLastActionDate(batDate);
			loan.setLastActionType(loanReg.getLoanAction());
		}else{
			// 非退货情况处理
			if(newRepaySchedule!=null){
				newRepaySchedule.setLoanInitPrin(loan.getLoanInitPrin());
				newRepaySchedule.setLoanInitTerm(loan.getLoanInitTerm());
				em.persist(newRepaySchedule);
				
				//设置交易账单为旧批量日期的改为当日
				QCcsTxnUnstatement q = QCcsTxnUnstatement.ccsTxnUnstatement;
				QCcsTxnHst qtxn = QCcsTxnHst.ccsTxnHst;
				List<CcsTxnUnstatement> txnUnstatements = new JPAQuery(em)
					.from(q).where(q.acctNbr.eq(acct.getAcctNbr())
						.and(q.acctType.eq(acct.getAcctType()))
						.and(q.stmtDate.eq(acct.getNextStmtDate()))
						).orderBy(q.txnSeq.asc()).list(q);
				List<CcsTxnHst> ccsTxnHsts = new JPAQuery(em)
				.from(qtxn).where(qtxn.acctNbr.eq(acct.getAcctNbr())
					.and(qtxn.acctType.eq(acct.getAcctType()))
					.and(qtxn.stmtDate.eq(acct.getNextStmtDate()))
					).orderBy(qtxn.txnSeq.asc()).list(qtxn);
				if(txnUnstatements.size()>0){
					for(CcsTxnUnstatement ccsTxnUnstatement:txnUnstatements){
						ccsTxnUnstatement.setStmtDate(batchFacility.getBatchDate());
					}
				}
				if(ccsTxnHsts.size()>0){
					for(CcsTxnHst ccsTxnHst:ccsTxnHsts){
						ccsTxnHst.setStmtDate(batchFacility.getBatchDate());
					}
				}
				//设置下一账单日为当前批量日
				acct.setNextStmtDate(batchFacility.getBatchDate());
				
				// 更新Loan信息
				loan.setLastLoanStatus(loan.getLoanStatus());
				loan.setRemainTerm(1);
				loan.setLoanExpireDate(batDate);
				loan.setLastActionDate(batDate);
				loan.setLastActionType(loanReg.getLoanAction());
				loan.setAdvPmtAmt(loanReg.getAdvPmtAmt());
			}
		}
		
		//设置贷款终止
		loan.setLoanStatus(LoanStatus.T);
			
		// 短信
//			output.setRescheduleMsg(loanPrepare.makeRescheduleMsgItem(loanReg, 1, loan.getUnstmtPrin().subtract(loanReg.getAdvPmtAmt()), loanReg.getLoanSvcFee()));
	}
	/**
	 * 预约提前结清的获取当期的还款计划
	 * @param loan
	 * @param loanReg
	 * @param origSchedules
	 * @return
	 * @throws Exception
	 */
	private CcsRepaySchedule loanSettlementO(CcsLoan loan,CcsLoanReg loanReg,List<CcsRepaySchedule> origSchedules) throws Exception{
		//预约日期，预约日期不能大于贷款结束日期
		Date batDate;
		if(loanReg.getPreAdDate() != null){
			batDate = loanReg.getPreAdDate();
		}else{
			batDate = batchFacility.getBatchDate();
		}
		loan.setTerminalDate(batDate);
		//获取下一期的还款计划和预约日期所在的期数
		int currTerm = 0;
		CcsRepaySchedule ccsRepaySchedule = null;
		for (CcsRepaySchedule s : origSchedules) {
			if(s.getLoanPmtDueDate().compareTo(batDate)>0){
				ccsRepaySchedule= s;
				currTerm=s.getCurrTerm()-1;
				break;
			}
		}
		//如果下一期是空的，说明贷款已经终止
		if(ccsRepaySchedule ==null){
			return null;
		}
		// 获取上一期还款计划
		CcsRepaySchedule origSchedule = null;
		BigDecimal unstmtPrin = BigDecimal.ZERO;
		for (CcsRepaySchedule s : origSchedules) {
			if(s.getCurrTerm().intValue() == currTerm){
				origSchedule= s;
			}
			if(s.getCurrTerm().intValue() > currTerm){
				unstmtPrin = unstmtPrin.add(s.getLoanTermPrin());
			}
		}
		//获取预约的当期的起始日期
		Date beginDate = null;
		if(origSchedule == null){
			beginDate = loan.getActiveDate();
		}else{
			beginDate = DateUtils.addDays(origSchedule.getLoanPmtDueDate(), 1);
		}
		LoanFeeDef loanFeeDef = rescheduleUtils.getLoanFeeDef(loan.getLoanCode(), loan.getLoanInitTerm(), 
				loan.getLoanInitPrin(),loan.getLoanFeeDefId());
		// 判断是否犹豫期内
		boolean isHesitation = false;
		if(LoanAction.O == loanReg.getLoanAction()){
			if(loanFeeDef.hesitationDays!=null){
				Date hesitationDate = DateUtils.addDays(loan.getActiveDate(), loanFeeDef.hesitationDays);
				if(DateUtils.truncatedCompareTo(loan.getActiveDate(), loanReg.getRegisterDate(), Calendar.DATE)<0
						&& DateUtils.truncatedCompareTo(hesitationDate, loanReg.getRegisterDate(), Calendar.DATE)>=0)
					isHesitation= true;
			}
		}
		//犹豫期不收手续费
		if(isHesitation){
			//若代收了趸交费，且扣款成功
			if(Indicator.Y==loanFeeDef.premiumReturnInd&&Indicator.Y==loan.getPremiumInd()&&Indicator.Y==loanReg.getDdRspFlag()){
				CcsPostingTmp premiumFee = loanPrepare.generatePostingTmp(loanReg, SysTxnCd.D11, loanReg.getCardNbr(), loanReg.getLogicCardNbr(),null,null, loan.getPremiumAmt(),loan.getCurrTerm()+1);
				txnPrepare.txnPrepare(premiumFee, null);
			}
			// 犹豫期结算平台记录
			settleRecUtil.savePrepayRec( loan, loanReg);
		}else{
			// 手续费金额是否重算
			// 手续费金额重算
			BigDecimal initFee = BigDecimal.ZERO;
			//如果购买了灵活还款计划包，则不收取提前还款手续费
			if(loanFeeDef.disPrepaymentApplyTerm!=null){
				if(loan.getPrepayPkgInd() == Indicator.Y && loan.getCurrTerm() >= loanFeeDef.disPrepaymentApplyTerm){
					initFee = BigDecimal.ZERO;
				}
			}else{
				initFee = EarlySettleMethodimple.valueOf(
						loanFeeDef.prepaymentFeeMethod.toString()).calcuteEarlySettle(
								unstmtPrin, loanFeeDef,currTerm+1);
				CcsPostingTmp feeTxn = loanPrepare.generatePostingTmp(loanReg, SysTxnCd.S67, loanReg.getCardNbr(), loanReg.getLogicCardNbr(), null, null, initFee, currTerm+1);
				txnPrepare.txnPrepare(feeTxn, null);
			}
			
			if(loanFeeDef.replacePrepaymentFeeMethod != null){
				BigDecimal initReplaceFee = BigDecimal.ZERO;
				//如果购买了灵活还款计划包，则不收取提前还款手续费
				if(loanFeeDef.disPrepaymentApplyTerm!=null){
					if(loan.getPrepayPkgInd() == Indicator.Y && loan.getCurrTerm() >= loanFeeDef.disPrepaymentApplyTerm){
						initReplaceFee = BigDecimal.ZERO;
					}
				}else{
					initReplaceFee = ReplacePrepayMethodimple.valueOf(
						loanFeeDef.replacePrepaymentFeeMethod.toString()).calcuteEarlySettle(
								unstmtPrin, loanFeeDef,currTerm+1);
				
					CcsPostingTmp replaceFeeTxn = loanPrepare.generatePostingTmp(loanReg, SysTxnCd.D15, loanReg.getCardNbr(), loanReg.getLogicCardNbr(), null, null, initReplaceFee, currTerm+1);
					txnPrepare.txnPrepare(replaceFeeTxn, null);
				}
			}
			
			// 获取卡产品信息
			ProductCredit productCr = cardProctFacility.CardNoToProductCr(loanReg.getCardNbr());
			FinancialOrg financialOrg = parameterFacility.loadParameter(productCr.financeOrgNo, FinancialOrg.class);
			if(financialOrg.adFeeScale != null){
				BigDecimal divideFee = initFee.multiply(financialOrg.adFeeScale).setScale(2, RoundingMode.HALF_UP);
				CcsPostingTmp divideFeeTxn = loanPrepare.generatePostingTmp(loanReg, SysTxnCd.S77, loanReg.getCardNbr(), loanReg.getLogicCardNbr(), null, null, divideFee, currTerm+1);
				txnPrepare.txnPrepare(divideFeeTxn, null);	
			}
			
		}
		//计算预约当期的利息天数
		int calcuteDate =0;
		if(beginDate.compareTo(batDate)<0){
			calcuteDate = DateUtils.getIntervalDays(beginDate,batDate);
		}
		//重算下一期的还款计划
		CcsRepaySchedule newRepaySchedule = rescheduleUtils.genPrepaymentRetry(loanReg, loan, calcuteDate, batchFacility.getBatchDate(), isHesitation, ccsRepaySchedule,loanFeeDef);
		
		//如果预约日期所在的起始跟loan所在期数不同，就说明跨了账单日，需要回溯，这里不会发生currtrem》loan中的currtrem值
		if(currTerm < loan.getCurrTerm()){
			List<CcsRepaySchedule> schedules = new ArrayList<CcsRepaySchedule>();
			//取得预约日期后已经出单的schedule
			for (CcsRepaySchedule s : origSchedules) {
				if(s.getCurrTerm().intValue() > currTerm && s.getCurrTerm().intValue() <= loan.getCurrTerm()){
					schedules.add(s);
				}
			}
			//回溯
			for(CcsRepaySchedule s : schedules){
				//回溯利息--->回溯利息放到后面从txnhst中贷调,否则会造成利息贷调两次,放到后面贷调可以将收罚息时结出的正常息部分也贷调掉--ymk 20150114
				//CcsPostingTmp recallTxnInt = loanPrepare.generatePostingTmp(loanReg, SysTxnCd.S96, loanReg.getCardNbr(), loanReg.getLogicCardNbr(), null, null, s.getLoanTermInt(), currTerm+1);
				//txnPrepare.txnPrepare(recallTxnInt, null);
				//回溯保费
				CcsPostingTmp recallTxnIns = loanPrepare.generatePostingTmp(loanReg, SysTxnCd.S75, loanReg.getCardNbr(), loanReg.getLogicCardNbr(), null, null, s.getLoanInsuranceAmt(), currTerm+1);
				txnPrepare.txnPrepare(recallTxnIns, null);	
				//回溯贷款手续费
				CcsPostingTmp recallTxnFee = loanPrepare.generatePostingTmp(loanReg, SysTxnCd.S78, loanReg.getCardNbr(), loanReg.getLogicCardNbr(), null, null, s.getLoanTermFee(), currTerm+1);
				txnPrepare.txnPrepare(recallTxnFee, null);
				//回溯贷款手续费
				CcsPostingTmp recallSvcFee = loanPrepare.generatePostingTmp(loanReg, SysTxnCd.S78, loanReg.getCardNbr(), loanReg.getLogicCardNbr(), null, null, s.getLoanSvcFee(), currTerm+1);
				txnPrepare.txnPrepare(recallSvcFee, null);
				//回溯寿险费
				CcsPostingTmp recallTxnLifeInsu = loanPrepare.generatePostingTmp(loanReg, SysTxnCd.S76, loanReg.getCardNbr(), loanReg.getLogicCardNbr(), null, null, s.getLoanLifeInsuAmt(), currTerm+1);
				txnPrepare.txnPrepare(recallTxnLifeInsu, null);
				//回溯代收服务费
				CcsPostingTmp recallReplaceSvcFee = loanPrepare.generatePostingTmp(loanReg, SysTxnCd.C09, loanReg.getCardNbr(), loanReg.getLogicCardNbr(), null, null, s.getLoanReplaceSvcFee(), currTerm+1);
				txnPrepare.txnPrepare(recallReplaceSvcFee, null);
				//回溯灵活还款计划包 
				CcsPostingTmp recallPrepayPkg = loanPrepare.generatePostingTmp(loanReg, SysTxnCd.C17, loanReg.getCardNbr(), loanReg.getLogicCardNbr(), null,null, s.getLoanPrepayPkgAmt(),currTerm+1);
				txnPrepare.txnPrepare(recallPrepayPkg, null);
			}
			//回溯利息/罚息/罚金/复利
			QCcsTxnHst qccsTxnHst = QCcsTxnHst.ccsTxnHst;
			List<CcsTxnHst> txnHstList = new ArrayList<CcsTxnHst>();
			//从提前还款日到批量日间发生的交易
			txnHstList = new JPAQuery(em).from(qccsTxnHst)
				.where(qccsTxnHst.postDate.goe(batDate)
						//借记交易
						.and(qccsTxnHst.dbCrInd.eq(DbCrInd.D))
						.and(qccsTxnHst.acctNbr.eq(loan.getAcctNbr()).and(qccsTxnHst.acctType.eq(loan.getAcctType())))
						//入账成功
						).list(qccsTxnHst);
			//须被回溯的交易类型
			Map<String,SysTxnCd> txnCdMap = new HashMap<String,SysTxnCd>();
			txnCdMap.put(parameterFacility.loadParameter(SysTxnCd.S43,SysTxnCdMapping.class).txnCd,SysTxnCd.S43);
			txnCdMap.put(parameterFacility.loadParameter(SysTxnCd.S44,SysTxnCdMapping.class).txnCd,SysTxnCd.S44);
			txnCdMap.put(parameterFacility.loadParameter(SysTxnCd.S46,SysTxnCdMapping.class).txnCd,SysTxnCd.S46);
			txnCdMap.put(parameterFacility.loadParameter(SysTxnCd.D13,SysTxnCdMapping.class).txnCd,SysTxnCd.D13);
			for(CcsTxnHst txn:txnHstList){
				if(txn.getPostAmt().compareTo(BigDecimal.ZERO)>0){
					if(txnCdMap.containsKey(txn.getTxnCode())){
						switch(txnCdMap.get(txn.getTxnCode())){
							case S44:
							case S46:
								//回溯利息复利
								CcsPostingTmp recallTxnIntPently = loanPrepare.generatePostingTmp(loanReg, SysTxnCd.C03, loanReg.getCardNbr(), loanReg.getLogicCardNbr(), null, null,txn.getPostAmt(), currTerm+1);
								txnPrepare.txnPrepare(recallTxnIntPently, null);
								break;
							case S43:
								//回溯罚息
								CcsPostingTmp recallTxnCompound = loanPrepare.generatePostingTmp(loanReg, SysTxnCd.C05, loanReg.getCardNbr(), loanReg.getLogicCardNbr(), null, null,txn.getPostAmt(), currTerm+1);
								txnPrepare.txnPrepare(recallTxnCompound, null);
								break;
							case D13:
								CcsPostingTmp recallTxnRPenalty = loanPrepare.generatePostingTmp(loanReg, SysTxnCd.C13, loanReg.getCardNbr(), loanReg.getLogicCardNbr(), null, null, txn.getPostAmt(), currTerm+1);
								txnPrepare.txnPrepare(recallTxnRPenalty, null);
								break;
							default:break;
						}
					}
				}
			}
			//免掉plan上面累计的非延迟利息/罚息累计/复利累计/代收罚息累计
			QCcsPlan qPlan = QCcsPlan.ccsPlan;
			List<CcsPlan> planList = new JPAQuery(em).from(qPlan).where(qPlan.acctNbr.eq(loan.getAcctNbr()).and(qPlan.acctType.eq(loan.getAcctType())).and(qPlan.refNbr.eq(loan.getRefNbr()))).list(qPlan);
			for (CcsPlan plan : planList) {
				if (PlanType.Q.equals(plan.getPlanType())) {
					plan.setNodefbnpIntAcru(BigDecimal.ZERO);
					plan.setPenaltyAcru(BigDecimal.ZERO);
					plan.setCompoundAcru(BigDecimal.ZERO);
					plan.setReplacePenaltyAcru(BigDecimal.ZERO);
				}
			}
		}
		// 重新进行分配
		return newRepaySchedule;
	}
	
	/**
	 * 退货重算还款计划
	 * @param acct
	 * @param loan
	 * @param loanReg
	 * @param origSchedules
	 * @return
	 * @throws Exception
	 */
	private List<CcsRepaySchedule> loanSettlementT(CcsAcct acct, CcsLoan loan,CcsLoanReg loanReg,List<CcsRepaySchedule> origSchedules) throws Exception{

		Date batDate = batchFacility.getBatchDate();
		loan.setTerminalDate(batDate);
		
		CcsRepaySchedule origSchedule = null;
		CcsRepaySchedule schedule = null;
		for (CcsRepaySchedule s : origSchedules) {
			
			// 上期还款计划
			if(s.getCurrTerm().intValue() == loan.getCurrTerm()) {
				origSchedule = s;
			}
			
			// 当期还款计划
			if(s.getCurrTerm().intValue() == (loan.getCurrTerm()+1)) {
				schedule = s;
			}
		}
		
		//如果下一期是空的，说明贷款已经完成所有期数，直接返回
		if(schedule ==null){
			return null;
		}
		
		//当期的起始日期
		Date beginDate = (origSchedule == null) ? loan.getActiveDate() : DateUtils.addDays(origSchedule.getLoanPmtDueDate(), 1);
		
		LoanFeeDef loanFeeDef = rescheduleUtils.getLoanFeeDef(loan.getLoanCode(), loan.getLoanInitTerm(), 
				loan.getLoanInitPrin(),loan.getLoanFeeDefId());
		
		// 判断是否免费退货
		boolean isFree = false;
		if (loanFeeDef.returnMaxDays != null) {
			Date freeDate = DateUtils.addDays(loan.getActiveDate(), loanFeeDef.returnMaxDays);
			if (DateUtils.truncatedCompareTo(freeDate, batDate, Calendar.DATE) >= 0) {
				isFree= true;
			}
		}
		
		//计算预约当期的利息天数
		int calcuteDate =0;
		if (beginDate.compareTo(batDate) < 0){
			calcuteDate = DateUtils.getIntervalDays(beginDate,batDate);
		}
		
		//免费期，按只收本金重算当期的还款计划
		List<CcsRepaySchedule> newRepayScheduleList = rescheduleUtils.genRefundRetry(acct, loanReg, loan, calcuteDate, batchFacility.getBatchDate(), isFree, schedule,loanFeeDef);
		
		// 免费期退货并且存在上期还款计划，跨账单日，需要回溯已收费用
		if(isFree && origSchedule != null){
		
			List<CcsRepaySchedule> schedules = new ArrayList<CcsRepaySchedule>();
			
			// 取已经出单的schedule
			for (CcsRepaySchedule s : origSchedules) {
				if(s.getCurrTerm().intValue() < schedule.getCurrTerm().intValue()){
					schedules.add(s);
				}
			}
			
			//回溯
			for(CcsRepaySchedule s : schedules){
				//回溯利息
				CcsPostingTmp recallTxnInt = loanPrepare.generatePostingTmp(loanReg, SysTxnCd.S96, loanReg.getCardNbr(), loanReg.getLogicCardNbr(), null, null, s.getLoanTermInt(), loan.getCurrTerm());
				txnPrepare.txnPrepare(recallTxnInt, null);
				//回溯保费
				CcsPostingTmp recallTxnIns = loanPrepare.generatePostingTmp(loanReg, SysTxnCd.S75, loanReg.getCardNbr(), loanReg.getLogicCardNbr(), null, null, s.getLoanInsuranceAmt(), loan.getCurrTerm());
				txnPrepare.txnPrepare(recallTxnIns, null);	
				//回溯贷款服务费
				CcsPostingTmp recallTxnFee = loanPrepare.generatePostingTmp(loanReg, SysTxnCd.S78, loanReg.getCardNbr(), loanReg.getLogicCardNbr(), null, null, s.getLoanTermFee(), loan.getCurrTerm());
				txnPrepare.txnPrepare(recallTxnFee, null);
				//回溯贷款分期手续费
				CcsPostingTmp recallSvcFee = loanPrepare.generatePostingTmp(loanReg, SysTxnCd.S78, loanReg.getCardNbr(), loanReg.getLogicCardNbr(), null, null, s.getLoanSvcFee(), loan.getCurrTerm());
				txnPrepare.txnPrepare(recallSvcFee, null);
				//回溯寿险费
				CcsPostingTmp recallTxnLifeInsu = loanPrepare.generatePostingTmp(loanReg, SysTxnCd.S76, loanReg.getCardNbr(), loanReg.getLogicCardNbr(), null, null, s.getLoanLifeInsuAmt(), loan.getCurrTerm());
				txnPrepare.txnPrepare(recallTxnLifeInsu, null);
				//回溯代收服务费
				CcsPostingTmp recallReplaceSvcFee = loanPrepare.generatePostingTmp(loanReg, SysTxnCd.C09, loanReg.getCardNbr(), loanReg.getLogicCardNbr(), null, null, s.getLoanReplaceSvcFee(), loan.getCurrTerm());
				txnPrepare.txnPrepare(recallReplaceSvcFee, null);
				//回溯灵活还款计划包 
				CcsPostingTmp recallPrepayPkg = loanPrepare.generatePostingTmp(loanReg, SysTxnCd.C17, loanReg.getCardNbr(), loanReg.getLogicCardNbr(), null,null, s.getLoanPrepayPkgAmt(),loan.getCurrTerm());
				txnPrepare.txnPrepare(recallPrepayPkg, null);
			}
		}

		return newRepayScheduleList;
	}
	
	/**
	 * 理赔获取当期的还款计划
	 * @param loan
	 * @param loanReg
	 * @param origSchedules
	 * @return
	 * @throws Exception
	 */
	private CcsRepaySchedule loanSettlementC(CcsLoan loan,CcsLoanReg loanReg,List<CcsRepaySchedule> origSchedules) throws Exception{
		Date batDate = batchFacility.getBatchDate();
		// 理赔 计算到当日的逾期天数
		if(loan.getOverdueDate()==null){
			return null;
		}
		int days = DateUtils.getIntervalDays(loan.getOverdueDate(),batDate);
		
		//计算逾期80天所在的期数
		Date days80 = null;
		// 获取理赔天数参数
		ProductCredit productCr = cardProctFacility.CardNoToProductCr(loan.getCardNbr());
		int claimDays = productCr.claimsDays;
		if(days>claimDays){
			days80 = DateUtils.addDays(loan.getOverdueDate(),claimDays);
			origSchedules = this.sortSchedule(origSchedules);
		}else{
			days80 = batDate;
		}
		loan.setTerminalDate(days80);
		//获取下一期的还款计划和预约日期所在的期数
		int currTerm = 0;
		CcsRepaySchedule ccsRepaySchedule = null;
		for (CcsRepaySchedule s : origSchedules) {
			if(s.getLoanPmtDueDate().compareTo(days80)>=0){
				ccsRepaySchedule= s;
				currTerm=s.getCurrTerm()-1;
				break;
			}
		}
		//如果下一期是空的，说明贷款已经终止
		CcsRepaySchedule newRepaySchedule =null;
		if(ccsRepaySchedule ==null){
			currTerm = loan.getLoanInitTerm();
		}else{
			// 获取上一期还款计划
			CcsRepaySchedule origSchedule = null;
			for (CcsRepaySchedule s : origSchedules) {
				if(s.getCurrTerm().intValue() == currTerm){
					origSchedule= s;
					break;
				}
			}
			//获取理赔的当期的起始日期
			Date beginDate = null;
			if(origSchedule == null){
				beginDate = loan.getActiveDate();
			}else{
				beginDate = DateUtils.addDays(origSchedule.getLoanPmtDueDate(), 1);
			}
			//计算理赔当期的利息天数
			int calcuteDate =0;
			if(beginDate.compareTo(days80)<0){
				calcuteDate = DateUtils.getIntervalDays(beginDate,days80);
			}
			LoanFeeDef loanFeeDef = rescheduleUtils.getLoanFeeDef(loan.getLoanCode(), loan.getLoanInitTerm(), 
					loan.getLoanInitPrin(),loan.getLoanFeeDefId());
			//重算下一期的还款计划
			newRepaySchedule = rescheduleUtils.genPrepaymentRetry(loanReg, loan, calcuteDate,batchFacility.getBatchDate(),false,ccsRepaySchedule,loanFeeDef);
		}
		/**
		 * 如果理赔日期所在的起始跟loan所在期数不同，就说明跨了账单日，需要回溯
		 * currtrem>=loan中的currtrem值说明贷款已经终止，不需要回溯利息保费
		 * currtrem<loan中的currtrem值说明贷款理赔之后还有期数入账需要回溯
		 */
		if(currTerm < loan.getCurrTerm()){
			List<CcsRepaySchedule> schedules = new ArrayList<CcsRepaySchedule>();
			//取得预约日期后已经出单的schedule
			for (CcsRepaySchedule s : origSchedules) {
				if(s.getCurrTerm().intValue() > currTerm && s.getCurrTerm().intValue() <= loan.getCurrTerm()){
					schedules.add(s);
				}
			}
			//回溯利息和保费
			for(CcsRepaySchedule s : schedules){
				CcsPostingTmp recallTxnInt = loanPrepare.generatePostingTmp(loanReg, SysTxnCd.S96, loanReg.getCardNbr(), loanReg.getLogicCardNbr(), null, null, s.getLoanTermInt(), currTerm+1);
				txnPrepare.txnPrepare(recallTxnInt, null);
//				CcsPostingTmp recallTxnIns = loanPrepare.generatePostingTmp(loanReg, SysTxnCd.S75, loanReg.getCardNbr(), loanReg.getLogicCardNbr(), null, null, s.getLoanInsuranceAmt(), currTerm+1);
//				txnPrepare.txnPrepare(recallTxnIns, null);	
			}
		}
		//如果是理赔结清，对未还的保费进行贷调处理
		QCcsPlan qPlan = QCcsPlan.ccsPlan;
		List<CcsPlan> plans =new JPAQuery(em)
			.from(qPlan)
			.where(qPlan.acctNbr.eq(loan.getAcctNbr()).and(qPlan.acctType.eq(loan.getAcctType()))
					.and(qPlan.refNbr.eq(loan.getRefNbr()))
					.and(qPlan.planType.eq(PlanType.Q)))
			.list(qPlan);
		BigDecimal txnAmt = BigDecimal.ZERO;
		for(CcsPlan plan : plans){
			txnAmt = txnAmt.add(plan.getPastInsurance()).add(plan.getCtdInsurance());
		}
		//未还保费》0 生成保费的贷调交易
		if(txnAmt.compareTo(BigDecimal.ZERO)>0){
			CcsPostingTmp recallIns = loanPrepare.generatePostingTmp(loanReg, SysTxnCd.S75, loanReg.getCardNbr(), loanReg.getLogicCardNbr(), null, null, txnAmt, currTerm+1);
			txnPrepare.txnPrepare(recallIns, null);
		}
		
		// 重新进行分配
		return newRepaySchedule;
	}
	 /**
	 * 还款计划收取方式按照期数大小进行排序
	 * @param arr
	 * @return
	 */
   private List<CcsRepaySchedule> sortSchedule(List<CcsRepaySchedule> sches) { // 交换排序->冒泡排序
	   CcsRepaySchedule sche = null;
        boolean exchange = false;
        for (int i = 0; i < sches.size(); i++) {
            exchange = false;
            for (int j = sches.size() - 2; j >= i; j--) {
                if (sches.get(j + 1).getCurrTerm().compareTo(sches.get(j).getCurrTerm()) <= 0) {
                	sche = sches.get(j + 1);
                	sches.set(j + 1, sches.get(j));
                	sches.set(j, sche);
                    exchange = true;
                }
            }
            if (!exchange)
                break;
        }
        return sches;
    }

}
