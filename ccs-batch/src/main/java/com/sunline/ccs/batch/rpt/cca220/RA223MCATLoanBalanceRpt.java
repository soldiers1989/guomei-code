package com.sunline.ccs.batch.rpt.cca220;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.query.Tuple;
import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.ark.batch.KeyBasedStreamReader;
import com.sunline.ccs.batch.rpt.common.RptParamFacility;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctKey;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.Ownership;

/**
 *	随借随还贷款余额报表
 */
public class RA223MCATLoanBalanceRpt extends KeyBasedStreamReader<CcsAcctKey, CcsAcct> {
	@PersistenceContext
    private EntityManager em;
	@Autowired
	private RptParamFacility codeProv;
	@Override
	protected List<CcsAcctKey> loadKeys() {
		List<CcsAcctKey> acctKeyList = new ArrayList<CcsAcctKey>();
		
		List<String> productCdList = codeProv.loadProductCdList(Ownership.O, LoanType.MCAT, null);
		if(productCdList == null || productCdList.size() <=0 ) return acctKeyList;
		
		
		QCcsAcct qAcct = QCcsAcct.ccsAcct;
		List<Tuple> keyTupleList = new JPAQuery(em).from(qAcct)
				.where(qAcct.productCd.in(productCdList))
				.list(qAcct.acctNbr, qAcct.acctType);
		
		for(Tuple t : keyTupleList){
			acctKeyList.add(new CcsAcctKey(t.get(qAcct.acctNbr), t.get(qAcct.acctType)));
		}
		return acctKeyList;
	}
	@Override
	protected CcsAcct loadItemByKey(CcsAcctKey key) {
		return em.find(CcsAcct.class, key);
	}

}
