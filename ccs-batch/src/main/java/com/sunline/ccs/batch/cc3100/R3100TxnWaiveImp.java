package com.sunline.ccs.batch.cc3100;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.batch.KeyBasedStreamReader;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnWaiveLog;
import com.sunline.ccs.infrastructure.shared.model.QCcsTxnWaiveLog;

/**
 * 
 * @see 类名：R2102TxnWaiveImp
 * @see 描述：CCS_TXN_WAIVE_LOG
 *
 * @see 创建日期：   2015-11-13
 * @author liuqi
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class R3100TxnWaiveImp extends KeyBasedStreamReader<Long, CcsTxnWaiveLog> {
	
	@PersistenceContext
	private EntityManager em;
	
	@Autowired
	private BatchStatusFacility batchFacility;

	@Override
	protected List<Long> loadKeys() {
		QCcsTxnWaiveLog q = QCcsTxnWaiveLog.ccsTxnWaiveLog;
		return new JPAQuery(em).from(q)
				.where(q.logBizDate.loe(batchFacility.getBatchDate()))
				.list(q.opSeq);
	}

	@Override
	protected CcsTxnWaiveLog loadItemByKey(Long key) {
		return em.find(CcsTxnWaiveLog.class, key);
	}
}
