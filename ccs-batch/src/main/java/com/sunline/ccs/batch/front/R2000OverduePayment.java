package com.sunline.ccs.batch.front;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.ark.batch.KeyBasedStreamReader;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ppy.dictionary.enums.LoanStatus;
import com.sunline.ppy.dictionary.enums.LoanType;

/**
 * 逾期抵扣
 * 按loan判断是否逾期
 * @author zhangqiang
 *
 */
public class R2000OverduePayment extends KeyBasedStreamReader<Long, CcsLoan> {

	@PersistenceContext
	private EntityManager em;
	
	@Override
	protected CcsLoan loadItemByKey(Long key) {
		CcsLoan loan = em.find(CcsLoan.class, key);
		return loan;
	}
	@Override
	protected List<Long> loadKeys() {
		
		QCcsLoan l = QCcsLoan.ccsLoan;
		List<Long> loanIds = new JPAQuery(em).from(l)
				.where((l.overdueDate.isNotNull().or(l.cpdBeginDate.isNotNull()))
						.and(l.loanStatus.in(LoanStatus.F, LoanStatus.T, LoanStatus.A))
						.and(l.paidOutDate.isNull())
						.and(l.loanType.eq(LoanType.MCEI)))
				.list(l.loanId);
		
		return loanIds;
	}
	
}
