package com.sunline.ccs.batch.rpt.cca210;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.ark.batch.KeyBasedStreamReader;
import com.sunline.ccs.batch.rpt.common.RptParamFacility;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.Ownership;

/**
 *	马上贷贷款余额报表
 */
public class RA213MsLoanBalanceRpt extends KeyBasedStreamReader<Long, CcsLoan> {
	@PersistenceContext
    private EntityManager em;
	@Autowired
	private RptParamFacility codeProv;
	@Override
	protected List<Long> loadKeys() {
		
		List<String> loanCdList = codeProv.loadLoanCdList(Ownership.O, LoanType.MCEI, null);
		if(loanCdList == null || loanCdList.size() <= 0) return new ArrayList<Long>();
		
		QCcsLoan qCcsLoan = QCcsLoan.ccsLoan;
		return new JPAQuery(em).from(qCcsLoan)
				.where(qCcsLoan.paidOutDate.isNull()
						.and(qCcsLoan.loanCode.in(loanCdList))
						)
				.list(qCcsLoan.loanId);
	}
	@Override
	protected CcsLoan loadItemByKey(Long key) {
		return em.find(CcsLoan.class, key);
	}

}
