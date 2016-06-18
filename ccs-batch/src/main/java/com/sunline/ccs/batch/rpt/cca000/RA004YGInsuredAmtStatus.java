package com.sunline.ccs.batch.rpt.cca000;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.query.Tuple;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.expr.BooleanExpression;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.batch.KeyBasedStreamReader;
import com.sunline.ccs.batch.rpt.cca000.items.YGInsuredAmtStatusItem;
import com.sunline.ccs.batch.rpt.common.RptParamFacility;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.CcsRepaySchedule;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsPlan;
import com.sunline.ccs.infrastructure.shared.model.QCcsRepayHst;
import com.sunline.ccs.infrastructure.shared.model.QCcsRepaySchedule;
import com.sunline.ccs.param.def.enums.BucketObject;
import com.sunline.ppy.dictionary.enums.LoanStatus;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.Ownership;
import com.sunline.ppy.dictionary.enums.PlanType;

/**
 * 保费期供信息
 * @author wanghl
 *
 */
public class RA004YGInsuredAmtStatus extends KeyBasedStreamReader<RA004Key, YGInsuredAmtStatusItem> {
	Logger logger = LoggerFactory.getLogger(this.getClass());
	@PersistenceContext
    private EntityManager em;	
	@Autowired
	private RptParamFacility codeProv;

	@Autowired
	BatchStatusFacility batchStatusFacility;
	private QCcsLoan qCcsLoan = QCcsLoan.ccsLoan;
	private QCcsPlan qCcsPlan = QCcsPlan.ccsPlan;
	private QCcsRepayHst qRepay = QCcsRepayHst.ccsRepayHst;
	private QCcsRepaySchedule qSchedule = QCcsRepaySchedule.ccsRepaySchedule;
	@Override
	protected List<RA004Key> loadKeys() {
		Date batchDate = batchStatusFacility.getBatchDate();
		List<RA004Key> keys = new ArrayList<RA004Key>();
		
		List<String> loanCdList = codeProv.loadLoanCdList(Ownership.P, LoanType.MCEI, codeProv.YG_ACQ_ACCEPTOR_ID);
		if(loanCdList == null || loanCdList.size() <= 0) return keys;
		
		BooleanExpression exp = 
				(qCcsLoan.loanCode.in(loanCdList)
						.and(qCcsLoan.loanStatus.eq(LoanStatus.A)
								.or(qCcsLoan.loanStatus.eq(LoanStatus.F).and(qCcsLoan.paidOutDate.isNull().and(qCcsLoan.terminalDate.isNull())))
								.or(qCcsLoan.loanStatus.eq(LoanStatus.F).and(qCcsLoan.paidOutDate.eq(batchDate)))))
				.and(qSchedule.loanId.eq(qCcsLoan.loanId));
		
		/*
		 * 新增当期：
		 * 1. 贷款放款成功建立Loan，出一期新增期供
		 * 2. 某期期到期，若有下期，出下期新增期供
		 */
		//一期新增
		BooleanExpression expFirstPlan = exp
				.and(qCcsLoan.activeDate.eq(batchDate).and(qSchedule.currTerm.eq(1)));
		
		JPAQuery queryFirstPlan = new JPAQuery(em).from(qCcsLoan,qSchedule).where(expFirstPlan);
		keys.addAll(getKeysFromQuery(queryFirstPlan, 0));
		
		/* 下期新增 */
		//找到到期schedule，获取下期期数的schedule。无下期则查询无记录
		List<Tuple> dueDayPlans = new JPAQuery(em).from(qCcsLoan, qSchedule)
				.where(exp.and(qSchedule.loanPmtDueDate.eq(batchDate)))
				.list(qSchedule.currTerm, qCcsLoan.loanId);
		
		for(Tuple dueDayPlan : dueDayPlans){

			BooleanExpression expNextPlan = exp
					.and(qCcsLoan.loanId.eq(dueDayPlan.get(qCcsLoan.loanId))
							.and(qSchedule.currTerm.eq(dueDayPlan.get(qSchedule.currTerm ) + 1 )));
			
			JPAQuery queryNextPlan = new JPAQuery(em).from(qCcsLoan,qSchedule).where(expNextPlan);
			keys.addAll(getKeysFromQuery(queryNextPlan, 0));
		
		}
			
		//往期结清
		BooleanExpression expPreFinishedPlan = exp
				.and(qCcsPlan.refNbr.eq(qCcsLoan.refNbr))
				.and(qCcsPlan.planType.in(PlanType.I, PlanType.Q))
				.and(qCcsPlan.term.isNotNull())
				.and(qCcsPlan.ctdInsurance.add(qCcsPlan.pastInsurance).eq(new BigDecimal(0)))
				.and(qRepay.planId.eq(qCcsPlan.planId))
				.and(qRepay.batchDate.eq(batchDate).and(qRepay.bnpType.in(BucketObject.ctdIns, BucketObject.pastIns)))
				.and(qSchedule.currTerm.eq(qCcsPlan.term));
		
		JPAQuery queryPreFinishedPlan = new JPAQuery(em).from(qRepay, qCcsPlan,qCcsLoan,qSchedule).where(expPreFinishedPlan);
		keys.addAll(getKeysFromQuery(queryPreFinishedPlan, 1));
		
		//逾期
		BooleanExpression expOverDuePlan = exp
				.and(qCcsPlan.refNbr.eq(qCcsLoan.refNbr))
				.and(qCcsPlan.planType.in(PlanType.I, PlanType.Q))
				.and(qCcsPlan.term.isNotNull())
				.and(qCcsPlan.ctdInsurance.add(qCcsPlan.pastInsurance).gt(new BigDecimal(0)))
				.and(qCcsPlan.term.loe(qCcsLoan.currTerm))
				.and(qSchedule.currTerm.eq(qCcsPlan.term));
		JPAQuery queryOverDuePlan = new JPAQuery(em).from( qCcsPlan,qCcsLoan,qSchedule).where(expOverDuePlan);
		keys.addAll(getKeysFromQuery(queryOverDuePlan, 2));
		
		return keys;
	}

	private List<RA004Key> getKeysFromQuery(JPAQuery query, Integer planStatus) {
		List<RA004Key> keys = new ArrayList<RA004Key>();
		if(planStatus.equals(0)){
			List<Tuple> newPlan = query.list(
					qCcsLoan.loanId,
					qSchedule.scheduleId
					);
			for(Tuple t : newPlan){
				RA004Key key = new RA004Key();
				key.setTermStatus(planStatus);
				key.setLoanId(t.get(qCcsLoan.loanId));
				key.setScheduleId(t.get(qSchedule.scheduleId));
				keys.add(key);
			}
		}else{
			List<Tuple> plan = query.list(
					qCcsLoan.loanId,
					qSchedule.scheduleId,
					qCcsPlan.planId,
					qSchedule.loanPmtDueDate
					);
			for(Tuple t : plan){
				RA004Key key = new RA004Key();
				key.setTermStatus(planStatus);
				key.setLoanId(t.get(qCcsLoan.loanId));
				key.setScheduleId(t.get(qSchedule.scheduleId));
				key.setPlanId(t.get(qCcsPlan.planId));
				
				keys.add(key);
			}
		}
		return keys;
	}

	@Override
	protected YGInsuredAmtStatusItem loadItemByKey(RA004Key key) {
		logger.debug("key:Loanid[{}],planId[{}],scheduleId[{}],termstatus[{}]",
				key.getLoanId(),key.getPlanId(),key.getScheduleId(),key.getTermStatus());
		
		Date batchDate = batchStatusFacility.getBatchDate();
		CcsLoan loan = em.find(CcsLoan.class, key.getLoanId());
		CcsRepaySchedule schedule = em.find(CcsRepaySchedule.class, key.getScheduleId());
		
		YGInsuredAmtStatusItem item = new YGInsuredAmtStatusItem();
		if(key.getTermStatus() == 0){
			item.payCorp = schedule.getLoanInsuranceAmt().setScale(2, RoundingMode.HALF_UP);
			item.inputDate = batchDate;
			item.overDueFlag = "0";
		}else{
			CcsPlan plan = em.find(CcsPlan.class, key.getPlanId());
			item.payCorp = plan.getCtdInsurance().add(plan.getPastInsurance()).setScale(2, RoundingMode.HALF_UP);
			item.inputDate = plan.getPlanAddDate();
			
			if(key.getTermStatus().equals(2)){
				item.overDueFlag = "1";
			}else if(key.getTermStatus().equals(1) && schedule.getLoanPmtDueDate().before(batchDate)){
				item.overDueFlag = "1";
			}else{
				item.overDueFlag = "0";
			}
			
		}
		item.putOutNo = loan.getDueBillNo();
		item.loanTerm = schedule.getCurrTerm();
		item.endDate = schedule.getLoanPmtDueDate();
		item.normalCorp = schedule.getLoanInsuranceAmt().setScale(2, RoundingMode.HALF_UP);
		item.curtermStatus = key.getTermStatus();
		
		item.updateDate = batchDate;
		return item;
	}

}
