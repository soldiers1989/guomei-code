package com.sunline.ccs.batch.cc3000.cancle;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ccs.batch.cc3000.U3001LoanPrepare;
import com.sunline.ccs.batch.common.DateUtils;
import com.sunline.ccs.facility.BlockCodeUtils;
import com.sunline.ccs.facility.Card2ProdctAcctFacility;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.MicroCreditRescheduleUtils;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsRepaySchedule;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnHst;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnUnstatement;
import com.sunline.ccs.infrastructure.shared.model.QCcsTxnHst;
import com.sunline.ccs.infrastructure.shared.model.QCcsTxnUnstatement;
import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.ccs.param.def.enums.LoanTerminateReason;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanStatus;

@Component
public class AutoCancleLoanP implements AutoCancle {
	
	@PersistenceContext
	private EntityManager em;
	@Autowired
	private BatchStatusFacility batchFacility;
	@Autowired
	private CustAcctCardFacility queryFacility;
	@Autowired
	private MicroCreditRescheduleUtils rescheduleUtils;
	@Autowired
	private U3001LoanPrepare loanPrepare;
	@Autowired
	private Card2ProdctAcctFacility productFacility;
	@Autowired
	private BlockCodeUtils blockcodeUtils;
	
	@Override
	public void cancle(CcsLoan loan) {
		// 获取账户
		CcsAcct acct = queryFacility.getAcctByAcctNbr(loan.getAcctType(), loan.getAcctNbr());
		// 原schedule
		List<CcsRepaySchedule> origSchedules = rescheduleUtils.getRepayScheduleListByLoanId(loan.getLoanId());
		// 对origSchedules按期数升序排序
		Collections.sort(origSchedules, new Comparator<CcsRepaySchedule>(){
			@Override
			public int compare(CcsRepaySchedule o1, CcsRepaySchedule o2) {
				return o1.getCurrTerm() - o2.getCurrTerm();
			}});
		// 失效schedule
		List<CcsRepaySchedule> cancledSchedules = new ArrayList<CcsRepaySchedule>();
		
		// 将原schedule搬到scheduleHst, 删除未到期的schedule进行重新分配
		for(CcsRepaySchedule schedule : origSchedules){
			// 未到期分配计划删除重新分配(到期日>=强制取消日)
			if(DateUtils.truncatedCompareTo(schedule.getLoanPmtDueDate(), batchFacility.getBatchDate(), Calendar.DATE)>=0){// 将原schedule搬到scheduleHst
				em.persist(loanPrepare.generateRepayScheduleHst(schedule, loan.getRegisterId()));
				em.remove(schedule);
				cancledSchedules.add(schedule);
			}
		}
		
		// 贷款逾期自动中止
		loan.setLastLoanStatus(loan.getLoanStatus());
		loan.setLoanStatus(LoanStatus.T);
		loan.setTerminalReasonCd(LoanTerminateReason.D);
		loan.setTerminalDate(batchFacility.getBatchDate());
		loan.setRemainTerm(1);
		loan.setLoanExpireDate(batchFacility.getBatchDate());
		loan.setLastActionDate(batchFacility.getBatchDate());
		
		if(cancledSchedules.size()==0){
			// 已过贷款到期日，什么也不做
		}else{
			// 生成新schedule
			em.persist(genRepaySchedule(loan, origSchedules, cancledSchedules));
			
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
		}
		acct.setBlockCode(blockcodeUtils.addBlockCode(acct.getBlockCode(), I_CODE));
	}
	
	/**
	 * 重新生成schedule
	 * @param loan
	 * @param origSchedules
	 * @param cancledSchedules
	 * @return
	 */
	private CcsRepaySchedule genRepaySchedule(CcsLoan loan, List<CcsRepaySchedule> origSchedules, List<CcsRepaySchedule> cancledSchedules){
		CcsRepaySchedule schedule = new CcsRepaySchedule();

		// 上一期
		CcsRepaySchedule lastSchedule = origSchedules.get(origSchedules.size()-cancledSchedules.size()-1);
		// 下一期
		CcsRepaySchedule nextSchedule = cancledSchedules.get(0);
		
		//剩余利息重算，还款计划中的利息*天数/30
		int calcuteDate = DateUtils.getIntervalDays(DateUtils.addDays(lastSchedule.getLoanPmtDueDate(), 1), batchFacility.getBatchDate());
		BigDecimal interest = BigDecimal.ZERO;
		if(calcuteDate >= 30){
			interest = nextSchedule.getLoanTermInt();
		}else{
			interest = nextSchedule.getLoanTermInt().multiply(new BigDecimal(calcuteDate)).divide(new BigDecimal(30), 2, RoundingMode.HALF_UP);
		}
		schedule.setOrg(loan.getOrg());
		schedule.setLoanId(loan.getLoanId());
		schedule.setAcctNbr(loan.getAcctNbr());
		schedule.setAcctType(loan.getAcctType());
		schedule.setLogicCardNbr(loan.getLogicCardNbr());
		schedule.setCardNbr(loan.getCardNbr());
		schedule.setLoanInitPrin(loan.getLoanInitPrin());
		schedule.setLoanInitTerm(loan.getLoanInitTerm());
		schedule.setCurrTerm(nextSchedule.getCurrTerm());
		schedule.setLoanTermPrin(loan.getUnstmtPrin());
		schedule.setLoanTermInt(interest);
		schedule.setLoanPmtDueDate(batchFacility.getBatchDate());
		schedule.setLoanInsuranceAmt(BigDecimal.ZERO);
		// 服务费 寿险计划包费 印花税
		schedule.setLoanTermFee(nextSchedule.getLoanTermFee());
		schedule.setLoanSvcFee(nextSchedule.getLoanSvcFee());
		schedule.setLoanLifeInsuAmt(nextSchedule.getLoanLifeInsuAmt());
		schedule.setLoanStampdutyAmt(BigDecimal.ZERO);
		
		if(loan.getStampCustomInd().equals(Indicator.Y)){
			schedule.setLoanStampdutyAmt(loan.getUnstmtStampdutyAmt());
		}
		
		// 代收服务费
		schedule.setLoanReplaceSvcFee(nextSchedule.getLoanReplaceSvcFee());
		
		schedule.setLoanPrepayPkgAmt(nextSchedule.getLoanPrepayPkgAmt());
		
		AccountAttribute acctAttr = productFacility.CardNoTOAccountAttribute(loan.getCardNbr(), loan.getAcctType().getCurrencyCode());
		schedule.setLoanGraceDate(DateUtils.addDays(batchFacility.getBatchDate(), acctAttr.pmtGracePrd));
		
		return schedule;
	}

}
