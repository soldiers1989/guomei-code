package com.sunline.ccs.batch.cc6000.transfer;

import java.util.Calendar;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.sunline.ccs.batch.cc3000.U3001LoanPrepare;
import com.sunline.ccs.batch.cc6000.S6000AcctInfo;
import com.sunline.ccs.facility.DateUtils;
import com.sunline.ccs.facility.MicroCreditRescheduleUtils;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.CcsRepaySchedule;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ccs.param.def.PlanTemplate;

public abstract class Transfer {
	@PersistenceContext
	private EntityManager em;
	@Autowired
	private U3001LoanPrepare loanPrepare;
	@Autowired
	private MicroCreditRescheduleUtils rescheduleUtils;
	
	//停止计息费锁定码
	public static final String I_CODE = "I";
	
	public abstract CcsPlan getXfrInPlan(S6000AcctInfo item,PlanTemplate xfrOut,PlanTemplate xfrIn,CcsPlan xfrOutPlan,CcsLoan loan);

	public abstract void xfr(S6000AcctInfo item, CcsPlan xfrOutPlan, CcsPlan xfrInPlan,CcsLoan tmLoan) throws Exception;

	/**
	 *  重新计算下一期的还款计划
	 * 生成还款计划为当日，保费，印花税，寿险计划包费，重新计算
	 * @param loan
	 * @param isHesitation
	 * @return
	 */
	public CcsRepaySchedule nextScheduletRetry(CcsLoan loan,S6000AcctInfo item,Date batchDate) {
		// 获取上一期还款计划
		CcsRepaySchedule origSchedule = null;
		// 获取当期还款计划
		CcsRepaySchedule ctdSchedule = null;
		for(CcsRepaySchedule schedule : item.getSchedules()){
			// 将原schedule搬到scheduleHst, 删除未到期的schedule进行重新分配
			if(schedule.getLoanId().intValue() == loan.getLoanId().intValue()){
				if(schedule.getCurrTerm().intValue() == loan.getCurrTerm()){
					origSchedule= schedule;
				}
				if(schedule.getCurrTerm().intValue() == loan.getCurrTerm()+1){
					ctdSchedule= schedule;
				}
				// 将原schedule搬到scheduleHst, 删除未到期的schedule进行重新分配
				// 未到期分配计划, 删除, 需重新分配
				if(schedule.getCurrTerm() > loan.getCurrTerm()){
					// 将原schedule搬到scheduleHst
					em.persist(loanPrepare.generateRepayScheduleHst(schedule, loan.getRegisterId()));
					em.remove(schedule);
				}
			}
		}
		// 判断是否犹豫期内
		boolean isHesitation = false;
		LoanFeeDef loanFeeDef = rescheduleUtils.getLoanFeeDef(loan.getLoanCode(), loan.getLoanInitTerm(), 
				loan.getLoanInitPrin(),loan.getLoanFeeDefId());
		if(loanFeeDef.hesitationDays!=null){
			Date hesitationDate = DateUtils.addDays(loan.getActiveDate(), loanFeeDef.hesitationDays);
			if(DateUtils.truncatedCompareTo(loan.getActiveDate(), batchDate, Calendar.DATE)<0
					&& DateUtils.truncatedCompareTo(hesitationDate, batchDate, Calendar.DATE)>=0)
				isHesitation= true;
		}
		//获取起始日期
		Date beginDate = null;
		if(origSchedule == null){
			beginDate = loan.getActiveDate();
		}else{
			beginDate = DateUtils.addDays(origSchedule.getLoanPmtDueDate(), 1);
		}
		
		int days = DateUtils.getIntervalDays(beginDate,batchDate);
		CcsRepaySchedule schedule = null;
		schedule = rescheduleUtils.nextScheduletRetry(loan, ctdSchedule, days, batchDate,isHesitation);
		if(schedule!=null){
			item.getSchedules().add(schedule);
			em.persist(schedule);
		}
		
		return schedule;
	}
	
}
