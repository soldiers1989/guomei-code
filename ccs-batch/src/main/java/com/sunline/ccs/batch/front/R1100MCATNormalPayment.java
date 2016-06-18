package com.sunline.ccs.batch.front;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.batch.KeyBasedStreamReader;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ppy.dictionary.enums.LoanType;

public class R1100MCATNormalPayment extends KeyBasedStreamReader<Long, CcsLoan> {
	
	@PersistenceContext
	private EntityManager em;
	
	@Autowired
	private BatchStatusFacility batchFacility;
	
	QCcsAcct qAcct = QCcsAcct.ccsAcct;
	QCcsLoan qLoan = QCcsLoan.ccsLoan;
	
	@Override
	protected List<Long> loadKeys() {
		// 约定还款日当天 && 随借随还 && 贷款未还清
		return new JPAQuery(em).from(qAcct, qLoan).where(qAcct.org.eq(qLoan.org)
					.and(qAcct.acctNbr.eq(qLoan.acctNbr)).and(qAcct.acctType.eq(qLoan.acctType))
					.and(qAcct.ddDate.eq(batchFacility.getSystemStatus().getBusinessDate()))
					.and(qLoan.loanType.eq(LoanType.MCAT))
					.and(qLoan.paidOutDate.isNull()))
				.list(qLoan.loanId);
	}

	@Override
	protected CcsLoan loadItemByKey(Long key) {
		return em.find(CcsLoan.class, key);
	}
	
}
