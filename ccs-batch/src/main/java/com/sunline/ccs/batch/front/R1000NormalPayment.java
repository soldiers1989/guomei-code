package com.sunline.ccs.batch.front;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.batch.KeyBasedStreamReader;
import com.sunline.ccs.infrastructure.shared.model.CcsRepaySchedule;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsRepaySchedule;
import com.sunline.ccs.param.def.enums.LoanTerminateReason;
import com.sunline.ppy.dictionary.enums.LoanStatus;

/**
 * 正常代扣
 * 
 * @author zhangqiang
 *
 */
public class R1000NormalPayment extends KeyBasedStreamReader<Long, CcsRepaySchedule> {

	@Autowired
	private BatchStatusFacility batchStatusFacility;

	@PersistenceContext
	private EntityManager em;

	QCcsLoan qLoan = QCcsLoan.ccsLoan;
	QCcsRepaySchedule qRepaySchedule = QCcsRepaySchedule.ccsRepaySchedule;

	@Override
	protected CcsRepaySchedule loadItemByKey(Long key) {
		return em.find(CcsRepaySchedule.class, key);
	}

	@Override
	protected List<Long> loadKeys() {
		// 期款日的活动贷款
		return new JPAQuery(em)
				.from(qLoan, qRepaySchedule)
				.where(qLoan.loanId.eq(qRepaySchedule.loanId).and(qLoan.loanStatus.eq(LoanStatus.A))
						//或退货中的贷款
						.or(qLoan.loanStatus.eq(LoanStatus.T).and(qLoan.terminalReasonCd.eq(LoanTerminateReason.T)))
						.and(qRepaySchedule.loanPmtDueDate.eq(batchStatusFacility.getSystemStatus().getBusinessDate())))
				.list(qRepaySchedule.scheduleId);
	}
}
