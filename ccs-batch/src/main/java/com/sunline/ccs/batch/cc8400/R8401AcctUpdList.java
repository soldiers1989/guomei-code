package com.sunline.ccs.batch.cc8400;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.mysema.query.Tuple;
import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.ark.batch.KeyBasedStreamReader;
import com.sunline.ccs.infrastructure.shared.model.CcsXsyncAcctTmp;
import com.sunline.ccs.infrastructure.shared.model.CcsXsyncAcctTmpKey;
import com.sunline.ccs.infrastructure.shared.model.QCcsXsyncAcctTmp;
/**
 * @see 类名：R8401AcctUpdList
 * @see 描述：TODO 中文描述
 *
 * @see 创建日期：   2015-6-24下午2:29:56
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class R8401AcctUpdList extends KeyBasedStreamReader<CcsXsyncAcctTmpKey, CcsXsyncAcctTmp> {
	@PersistenceContext
	private EntityManager em;

	@Override
	protected List<CcsXsyncAcctTmpKey> loadKeys() {
		QCcsXsyncAcctTmp q = QCcsXsyncAcctTmp.ccsXsyncAcctTmp;
		List<CcsXsyncAcctTmpKey> keys = new ArrayList<CcsXsyncAcctTmpKey>();
		for (Tuple objs : new JPAQuery(em).from(q).list(q.acctNbr, q.acctType))
			keys.add(new CcsXsyncAcctTmpKey(objs.get(q.acctNbr), objs.get(q.acctType)));
		return keys;
	}

	@Override
	protected CcsXsyncAcctTmp loadItemByKey(CcsXsyncAcctTmpKey key) {
		return em.find(CcsXsyncAcctTmp.class, key);
	}

}
