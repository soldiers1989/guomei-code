package com.sunline.ccs.batch.rpt.cca000;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.query.Tuple;
import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ccs.batch.rpt.cca000.items.YGStatInfoItem;
import com.sunline.ccs.batch.rpt.common.RptParamFacility;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsPlan;
import com.sunline.ccs.infrastructure.shared.model.QCcsRepayHst;
import com.sunline.ccs.infrastructure.shared.model.QCcsSettleClaim;
import com.sunline.ccs.param.def.enums.BucketObject;
import com.sunline.ccs.param.def.enums.LoanTerminateReason;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanStatus;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.Ownership;
import com.sunline.ppy.dictionary.enums.PlanType;

/**
 * 阳光对账
 * @author wanghl
 *
 */
public class PA009YGStatInfo implements ItemProcessor<Integer, YGStatInfoItem> {
	Logger logger = LoggerFactory.getLogger(this.getClass());
	@PersistenceContext
    private EntityManager em;
	@Autowired
	private BatchStatusFacility batchStatusFacility;
	@Autowired
	private RptParamFacility codeProv;

	private QCcsLoan qLoan = QCcsLoan.ccsLoan;
	private QCcsPlan qPlan = QCcsPlan.ccsPlan;
	private QCcsSettleClaim qClaim = QCcsSettleClaim.ccsSettleClaim;
	private QCcsRepayHst qRepay = QCcsRepayHst.ccsRepayHst;

	@Override
	public YGStatInfoItem process(Integer info) throws Exception {
		Date batchDate = batchStatusFacility.getBatchDate();
		List<String> loanCdList = codeProv.loadLoanCdList(Ownership.P, LoanType.MCEI, codeProv.YG_ACQ_ACCEPTOR_ID);
		if(loanCdList == null || loanCdList.size() <= 0){
			return null;
		}
		Tuple newLoanStats = new JPAQuery(em).from(qLoan)
				.where(qLoan.activeDate.eq(batchDate)
						.and(qLoan.loanCode.in(loanCdList)))
				.singleResult(qLoan.loanId.count(), qLoan.loanInitPrin.sum());
		
		//未还清且未强制理赔的借据
		List<Tuple> activeloans = new JPAQuery(em).from(qLoan)
				.where(qLoan.loanCode.in(loanCdList)
						.and(qLoan.loanStatus.in(LoanStatus.A, LoanStatus.O)
								.or(qLoan.loanStatus.eq(LoanStatus.F).and(qLoan.paidOutDate.isNull().and(qLoan.terminalDate.isNull())))
								.or(qLoan.loanStatus.eq(LoanStatus.T).and(qLoan.paidOutDate.isNull()))))
				.list(qLoan.loanId, qLoan.unstmtPrin);
		logger.info("未结清的贷款[{}]", activeloans.size());
		
		BigDecimal leftPrinSum = BigDecimal.ZERO.setScale(2);
		for(Tuple loan : activeloans){
			logger.info("loanId[{}]",loan.get(qLoan.loanId));
			List<Tuple> plans = new JPAQuery(em).from(qLoan, qPlan)
				.where(qLoan.loanId.eq(loan.get(qLoan.loanId))
						.and(qLoan.refNbr.eq(qPlan.refNbr))
						.and(qPlan.planType.in(PlanType.I, PlanType.Q))
						.and(qPlan.term.isNotNull()))
				.list(qPlan.planId, qPlan.ctdPrincipal, qPlan.pastPrincipal);
			for(Tuple p : plans ){
				logger.info("planId[{}]",p.get(qPlan.planId));
				leftPrinSum = leftPrinSum.add(p.get(qPlan.ctdPrincipal)).add(p.get(qPlan.pastPrincipal));
			}
			
			leftPrinSum = leftPrinSum.add(loan.get(qLoan.unstmtPrin));
		}
		
		Tuple claimStats = new JPAQuery(em).from(qClaim)
				.where(qClaim.settleSucDate.eq(batchDate)
						.and(qClaim.settleFlag.eq(Indicator.Y)))
				.singleResult(qClaim.loanId.count(), qClaim.settleAmt.sum());
		
		Tuple repayPrince = new JPAQuery(em).from(qRepay, qLoan, qPlan)
				.where(qLoan.loanCode.in(loanCdList)
						.and(qPlan.planId.eq(qRepay.planId))
						.and(qLoan.refNbr.eq(qPlan.refNbr))
						.and(qLoan.terminalReasonCd.isNull().or(qLoan.terminalReasonCd.notIn(LoanTerminateReason.C)))
						.and(qRepay.batchDate.eq(batchDate).and(qRepay.bnpType.in(BucketObject.ctdPrincipal, BucketObject.pastPrincipal))))
				.singleResult(qRepay.repayAmt.sum(), qRepay.paymentId.count());
		
		Tuple repayInsur = new JPAQuery(em).from(qRepay, qLoan, qPlan)
				.where(qLoan.loanCode.in(loanCdList)
						.and(qPlan.planId.eq(qRepay.planId))
						.and(qLoan.refNbr.eq(qPlan.refNbr))
						.and(qLoan.terminalReasonCd.isNull().or(qLoan.terminalReasonCd.notIn(LoanTerminateReason.C)))
						.and(qRepay.batchDate.eq(batchDate).and(qRepay.bnpType.in(BucketObject.ctdIns, BucketObject.pastIns))))
				.singleResult(qRepay.repayAmt.sum(), qRepay.paymentId.count());
		
		YGStatInfoItem item = new YGStatInfoItem();
		
		Long loanCount = newLoanStats.get(qLoan.loanId.count());
		item.loanCount = loanCount == null? 0L : loanCount;
		
		BigDecimal loanAmtSum = newLoanStats.get(qLoan.loanInitPrin.sum());
		item.loanAmtSum = loanAmtSum == null? BigDecimal.ZERO.setScale(2) : loanAmtSum.setScale(2, RoundingMode.HALF_UP);
		
		item.leftLoanCount = new Long(activeloans.size());
		
		item.leftPrinSum = leftPrinSum == null?BigDecimal.ZERO.setScale(2):leftPrinSum.setScale(2, RoundingMode.HALF_UP);
		
		Long claimCount = claimStats.get(qClaim.loanId.count());
		item.claimCount = claimCount == null?0L:claimCount;
		
		BigDecimal claimAmtSum = claimStats.get(qClaim.settleAmt.sum());
		item.claimAmtSum = claimAmtSum == null?BigDecimal.ZERO.setScale(2):claimAmtSum.setScale(2, RoundingMode.HALF_UP);
		
		item.subrogationCount = null;//传空
		item.subrogationAmtSum = null;//传空
		item.inputDate = batchDate;
		item.updateTime = batchDate;
		
		Long repayLoanCount = repayPrince.get(qRepay.paymentId.count());
		item.repayLoanCount = repayLoanCount == null?0L:repayLoanCount;
		
		BigDecimal repayLoanSum = repayPrince.get(qRepay.repayAmt.sum());
		item.repayLoanSum = repayLoanSum == null?BigDecimal.ZERO.setScale(2):repayLoanSum.setScale(2, RoundingMode.HALF_UP);
		
		Long repayCorpCount = repayInsur.get(qRepay.paymentId.count());
		item.repayCorpCount = repayCorpCount == null?0L:repayCorpCount;
		
		BigDecimal repayCorpSum = repayInsur.get(qRepay.repayAmt.sum());
		item.repayCorpSum = repayCorpSum == null?BigDecimal.ZERO.setScale(2):repayCorpSum.setScale(2, RoundingMode.HALF_UP);
		
		return item;
	}

}
