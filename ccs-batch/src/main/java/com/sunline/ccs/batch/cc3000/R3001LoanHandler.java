package com.sunline.ccs.batch.cc3000;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.query.Tuple;
import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.batch.KeyBasedStreamReader;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctKey;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoanReg;

/**
 * TmLoanReg对象读取
 * 
* @author liuqi
 */
public class R3001LoanHandler extends KeyBasedStreamReader<CcsAcctKey, CcsAcct> {
	@PersistenceContext
	protected EntityManager em;
	@Autowired
	private BatchStatusFacility batchFacility;

	@Override
	protected List<CcsAcctKey> loadKeys() {
		QCcsLoanReg q = QCcsLoanReg.ccsLoanReg;
		
		List<CcsAcctKey> keys = new ArrayList<CcsAcctKey>();
		JPAQuery query = new JPAQuery(em);
		for (Tuple objs : query.from(q).where(q.registerDate.loe(batchFacility.getBatchDate())).distinct().list(q.acctNbr, q.acctType))
			keys.add(new CcsAcctKey(objs.get(q.acctNbr), objs.get(q.acctType)));
		
		return keys;
	}

	@Override
	protected CcsAcct loadItemByKey(CcsAcctKey key) {
		return em.find(CcsAcct.class, key);
	}
}
