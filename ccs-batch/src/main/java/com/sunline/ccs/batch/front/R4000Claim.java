package com.sunline.ccs.batch.front;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.ark.batch.KeyBasedStreamReader;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;

/**
 * 理赔代扣
 * @author zhangqiang
 *
 */
public class R4000Claim extends KeyBasedStreamReader<Long, CcsLoan> {
	
	
	@PersistenceContext
	private EntityManager em;
	
	
	@Override
	protected CcsLoan loadItemByKey(Long key) {
		return em.find(CcsLoan.class, key);
	}

	@Override
	protected List<Long> loadKeys() {
		QCcsLoan l = QCcsLoan.ccsLoan;
		// 所有逾期loan
		return new JPAQuery(em).from(l).where(l.overdueDate.isNotNull()).list(l.loanId);
	}
	
}
