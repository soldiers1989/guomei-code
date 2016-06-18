package com.sunline.ccs.batch.cc9100;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctKey;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.batch.KeyBasedStreamReader;
import com.mysema.query.Tuple;
import com.mysema.query.jpa.impl.JPAQuery;

/**
 * @see 类名：R9101PBOC
 * @see 描述： 人行征信报送文件
 *
 * @see 创建日期：   2015-6-24下午2:47:45
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class R9101PBOC extends KeyBasedStreamReader<CcsAcctKey, CcsAcct> {
	@PersistenceContext
	protected EntityManager em;
	@Autowired
	private BatchStatusFacility batchStatusFacility;

	@Override
	protected List<CcsAcctKey> loadKeys() {
		QCcsAcct q = QCcsAcct.ccsAcct;
		List<CcsAcctKey> keys = new ArrayList<CcsAcctKey>();

		// 参见人行T+1报送规范
		JPAQuery query = new JPAQuery(em).from(q)
											.where(q.acctType.in(AccountType.A,AccountType.B,AccountType.C,AccountType.D)
													.and(q.setupDate.eq(batchStatusFacility.getBatchDate())
															.or(q.lastStmtDate.eq(batchStatusFacility.getBatchDate()).and(q.blockCode.notLike("%P%").or(q.blockCode.isNull())))
															.or(q.closedDate.eq(batchStatusFacility.getBatchDate()))));
		for (Tuple objs : query.list(q.acctNbr, q.acctType))
			keys.add(new CcsAcctKey(objs.get(q.acctNbr), objs.get(q.acctType)));
		
		return keys;
	}

	@Override
	protected CcsAcct loadItemByKey(CcsAcctKey key) {
		return em.find(CcsAcct.class, key);
	}

}
