package com.sunline.ccs.batch.cc3000;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.ark.batch.KeyBasedStreamReader;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ppy.dictionary.enums.LoanStatus;

public class R3010AutoCancleLoan extends KeyBasedStreamReader<Long, CcsLoan> {
	
	@PersistenceContext
	private EntityManager em;
	
	@Override
	protected CcsLoan loadItemByKey(Long key) {
		return em.find(CcsLoan.class, key);
	}

	@Override
	protected List<Long> loadKeys() {
		QCcsLoan q = QCcsLoan.ccsLoan;
		return new JPAQuery(em).from(q)
				.where(q.cpdBeginDate.isNotNull()
						.and(q.loanStatus.ne(LoanStatus.T)))
				.list(q.loanId);
	}

}
