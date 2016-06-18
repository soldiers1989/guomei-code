package com.sunline.ccs.batch.rpt.cca000;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.expr.BooleanExpression;
import com.sunline.ark.batch.KeyBasedStreamReader;
import com.sunline.ccs.batch.rpt.common.RptParamFacility;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.Ownership;

public class RA006YGClaimInfo extends KeyBasedStreamReader<Long, CcsLoan>{

	private QCcsLoan qLoan = QCcsLoan.ccsLoan;
	
	@PersistenceContext
    private EntityManager em;
	@Autowired
	private RptParamFacility codeProv;
	
	@Override
	protected List<Long> loadKeys() {
		List<String> loanCdList = codeProv.loadLoanCdList(Ownership.P, LoanType.MCEI, codeProv.YG_ACQ_ACCEPTOR_ID);
		if(loanCdList == null || loanCdList.size() <= 0) return new ArrayList<Long>();

		BooleanExpression exp = 
				qLoan.overdueDate.isNotNull().and(qLoan.loanCode.in(loanCdList));
		return new JPAQuery(em).from(qLoan).where(exp).list(qLoan.loanId);
	}

	@Override
	protected CcsLoan loadItemByKey(Long key) {
		return em.find(CcsLoan.class, key);
	}
	
	
	
}
