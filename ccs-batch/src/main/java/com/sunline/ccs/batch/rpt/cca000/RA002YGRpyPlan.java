package com.sunline.ccs.batch.rpt.cca000;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
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
import com.sunline.ccs.batch.rpt.cca000.items.YGRpyPlanItem;
import com.sunline.ccs.batch.rpt.common.RptParamFacility;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.CcsRepaySchedule;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsPlan;
import com.sunline.ccs.infrastructure.shared.model.QCcsRepaySchedule;
import com.sunline.ppy.dictionary.enums.LoanStatus;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.Ownership;
import com.sunline.ppy.dictionary.enums.PlanType;

/**
 * 贷款期供
 * @author wanghl
 *
 */
public class RA002YGRpyPlan extends KeyBasedStreamReader<RA002Key, YGRpyPlanItem> {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	@PersistenceContext
    private EntityManager em;
	@Autowired
	private RptParamFacility codeProv;

	@Autowired
	BatchStatusFacility batchStatusFacility;
	private QCcsLoan qCcsLoan = QCcsLoan.ccsLoan;
	private QCcsPlan qCcsPlan = QCcsPlan.ccsPlan;
	private QCcsRepaySchedule qSchedule = QCcsRepaySchedule.ccsRepaySchedule;
	
	@Override
	protected List<RA002Key> loadKeys() {
		Date batchDate = batchStatusFacility.getBatchDate();
		logger.debug("批量日期：["+new SimpleDateFormat("yyyyMMddHHmmSS").format(batchDate)+"]");
		List<RA002Key> keys = new ArrayList<RA002Key>();
		
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
			logger.debug("下期期供[{}]",dueDayPlan.get(qSchedule.currTerm ) + 1 );
			JPAQuery queryNextPlan = new JPAQuery(em).from(qCcsLoan,qSchedule).where(expNextPlan);
			keys.addAll(getKeysFromQuery(queryNextPlan, 0));
		}
		
		/* 往期结清(正常、逾期) */
		BooleanExpression expPreFinishedPlan = exp
				.and(qCcsLoan.refNbr.eq(qCcsPlan.refNbr))
				.and(qCcsPlan.planType.in(PlanType.I, PlanType.Q))
				.and(qCcsPlan.term.isNotNull())
				.and(qCcsPlan.paidOutDate.isNotNull().and(qCcsPlan.paidOutDate.eq(batchDate)))//还清日期
				.and(qSchedule.currTerm.eq(qCcsPlan.term));
		
		JPAQuery queryPreFinishedPlan = new JPAQuery(em).from(qCcsPlan,qCcsLoan,qSchedule).where(expPreFinishedPlan);
		keys.addAll(getKeysFromQuery(queryPreFinishedPlan, 1));
		
		/* 逾期  */
		BooleanExpression expOverDuePlan = exp
				.and(qCcsLoan.refNbr.eq(qCcsPlan.refNbr))
				.and(qCcsPlan.planType.in(PlanType.I, PlanType.Q))
				.and(qCcsPlan.term.isNotNull())
				.and(qCcsPlan.currBal.gt(new BigDecimal(0)))//当期余额
				.and(qCcsPlan.term.loe(qCcsLoan.currTerm))
				.and(qSchedule.currTerm.eq(qCcsPlan.term));
		
		JPAQuery queryOverDuePlan = new JPAQuery(em).from(qCcsPlan,qCcsLoan,qSchedule).where(expOverDuePlan);
		keys.addAll(getKeysFromQuery(queryOverDuePlan, 2));
		
		return keys;
	}

	/**
	 */
	private List<RA002Key> getKeysFromQuery(JPAQuery query, Integer planStatus) {
		List<RA002Key> keys = new ArrayList<RA002Key>();
		if(planStatus.equals(0)){
			List<Tuple> newPlan = query.list(
					qCcsLoan.loanId,
					qSchedule.scheduleId);
			for(Tuple t : newPlan){
				RA002Key key = new RA002Key();
				key.setPlanStatus(planStatus);
				key.setLoanId(t.get(qCcsLoan.loanId));
				key.setScheduleId(t.get(qSchedule.scheduleId));
				keys.add(key);
			}
		}else{
			List<Tuple> newPlan = query.list(
					qCcsPlan.planId,
					qCcsLoan.loanId,
					qSchedule.scheduleId);
			for(Tuple t : newPlan){
				RA002Key key = new RA002Key();
				key.setPlanStatus(planStatus);
				key.setPlanId(t.get(qCcsPlan.planId));
				key.setLoanId(t.get(qCcsLoan.loanId));
				key.setScheduleId(t.get(qSchedule.scheduleId));
				keys.add(key);
			}
		}
		return keys;
	}

	@Override
	protected YGRpyPlanItem loadItemByKey(RA002Key key) {
		logger.info("LoanId:["+key.getLoanId()+"], PlanId["+key.getPlanId()+"], scheduleId["+key.getScheduleId()+"], planStatus：["+key.getPlanStatus()+"]");
		
		CcsLoan loan = em.find(CcsLoan.class, key.getLoanId());
		logger.info("账户号：["+loan.getAcctNbr()+"]["+loan.getAcctType()+"], 借据号：["+loan.getDueBillNo()+"], "+"保单号：["+loan.getGuarantyId()+"]");
		
		CcsRepaySchedule schedule = em.find(CcsRepaySchedule.class, key.getScheduleId());
		logger.info("期数：["+schedule.getCurrTerm()+"]");
		
		CcsPlan plan = key.getPlanStatus()!=0?em.find(CcsPlan.class, key.getPlanId()):null;
		
		YGRpyPlanItem fileItem = new YGRpyPlanItem();
		if(key.getPlanStatus() == 0){
			fileItem.actualBalance = schedule.getLoanTermPrin().setScale(2);
			fileItem.actualInte = schedule.getLoanTermInt().setScale(2);
			fileItem.infine = BigDecimal.ZERO.setScale(2);	
		}else{
			fileItem.actualBalance = plan.getCtdPrincipal().add(plan.getPastPrincipal()).setScale(2, RoundingMode.HALF_UP);
			fileItem.actualInte = plan.getCtdInterest().add(plan.getPastInterest()).setScale(2, RoundingMode.HALF_UP);
			fileItem.infine = plan.getCtdMulctAmt().add(plan.getPastMulctAmt()).setScale(2, RoundingMode.HALF_UP);	
		}
		
		fileItem.putOutNo = loan.getDueBillNo();
		fileItem.status = key.getPlanStatus().toString();
		fileItem.loanTerm = schedule.getCurrTerm();
		fileItem.maturityDate = schedule.getLoanPmtDueDate();
		fileItem.balance = schedule.getLoanTermPrin().setScale(2, RoundingMode.HALF_UP);
		fileItem.inte = schedule.getLoanTermInt().setScale(2, RoundingMode.HALF_UP);
		return fileItem;
	}
	
}
