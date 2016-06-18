package com.sunline.ccs.batch.cca300;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.ark.batch.KeyBasedStreamReader;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoanReg;
import com.sunline.ppy.dictionary.enums.InputSource;

public class RA303PrepaymentRemainMsg extends KeyBasedStreamReader<Long, CcsLoanReg>{

	@PersistenceContext
	private EntityManager em;
	QCcsLoanReg qLoanReg = QCcsLoanReg.ccsLoanReg;
	QCcsAcct qAcct = QCcsAcct.ccsAcct;
	
	@Override
	protected List<Long> loadKeys() {
		List<Long> keyList = new JPAQuery(em).from(qLoanReg,qAcct)
				.where(qLoanReg.preAdDate.isNotNull()
				.and(qLoanReg.preAdDate.isNotNull()
				.and(qLoanReg.acctNbr.eq(qAcct.acctNbr))
				.and(qLoanReg.acctType.eq(qAcct.acctType))
				.and(qAcct.custSource.notIn(InputSource.SUNS))
				)).list(qLoanReg.registerId);
		return keyList;
	}

	@Override
	protected CcsLoanReg loadItemByKey(Long key) {
		return em.find(CcsLoanReg.class,key);
	}

}
