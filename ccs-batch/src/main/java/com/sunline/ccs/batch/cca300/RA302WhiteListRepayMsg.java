package com.sunline.ccs.batch.cca300;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.ark.batch.KeyBasedStreamReader;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;

public class RA302WhiteListRepayMsg extends KeyBasedStreamReader<Long, CcsLoan> {
	
	@PersistenceContext
	private EntityManager em;
	
	QCcsAcct a = QCcsAcct.ccsAcct;
	QCcsLoan l = QCcsLoan.ccsLoan;
	
	@Override
	protected List<Long> loadKeys() {
		
		return new JPAQuery(em).from(a, l)
				.where(l.acctNbr.eq(a.acctNbr).and(l.acctType.eq(a.acctType))
						.and(l.paidOutDate.isNull()))
				.list(l.loanId);
	}

	@Override
	protected CcsLoan loadItemByKey(Long key) {
		return em.find(CcsLoan.class, key);
	}
	
}
