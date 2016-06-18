package com.sunline.ccs.batch.cc2100;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.batch.KeyBasedStreamReader;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnAdjLog;
import com.sunline.ccs.infrastructure.shared.model.QCcsTxnAdjLog;

/**
 * 
 * @see 类名：R2101TxnAdjImp
 * @see 描述：CcsTxnAdjLog
 *
 * @see 创建日期：   2015-6-23下午7:28:35
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class R2101TxnAdjImp extends KeyBasedStreamReader<Long, CcsTxnAdjLog> {
	
	@PersistenceContext
	private EntityManager em;
	
	@Autowired
	private BatchStatusFacility batchFacility;

	@Override
	protected List<Long> loadKeys() {
		QCcsTxnAdjLog q = QCcsTxnAdjLog.ccsTxnAdjLog;
		return new JPAQuery(em).from(q)
				.where(q.logBizDate.loe(batchFacility.getBatchDate()))
				.list(q.opSeq);
	}

	@Override
	protected CcsTxnAdjLog loadItemByKey(Long key) {
		return em.find(CcsTxnAdjLog.class, key);
	}
}
