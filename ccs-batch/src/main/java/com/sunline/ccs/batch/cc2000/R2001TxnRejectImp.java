package com.sunline.ccs.batch.cc2000;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.ark.batch.KeyBasedStreamReader;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnReject;
import com.sunline.ccs.infrastructure.shared.model.QCcsTxnReject;

/**
 * 
 * @see 类名：R2001TxnRejectImp
 * @see 描述：CcsTxnReject对象读取，无状态Reader
 *
 * @see 创建日期：   2015-6-23下午7:27:08
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class R2001TxnRejectImp extends KeyBasedStreamReader<Long, CcsTxnReject> {
	
	@PersistenceContext
	private EntityManager em;
	
	@Override
	protected List<Long> loadKeys() {
		QCcsTxnReject q = QCcsTxnReject.ccsTxnReject;
		return new JPAQuery(em).from(q).list(q.txnSeq);
	}

	@Override
	protected CcsTxnReject loadItemByKey(Long key) {
		return em.find(CcsTxnReject.class, key);
	}

}
