package com.sunline.ccs.batch.cc1400;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.batch.KeyBasedStreamReader;
import com.sunline.ccs.infrastructure.shared.model.CcsOutsideDdTxn;
import com.sunline.ccs.infrastructure.shared.model.QCcsOutsideDdTxn;

/**
 * 马上对账文件-外部扣款流水处理
 * @author liuq
 *
 */
public class R1405OutsideDDTxn extends KeyBasedStreamReader<Long, CcsOutsideDdTxn> {
	@PersistenceContext
    private EntityManager em;
	@Autowired
	private BatchStatusFacility batchFacility;
	
	@Override
	protected List<Long> loadKeys() {
		QCcsOutsideDdTxn qCcsOutsideDdTxn = QCcsOutsideDdTxn.ccsOutsideDdTxn;
		return new JPAQuery(em).from(qCcsOutsideDdTxn)
				.where(qCcsOutsideDdTxn.businessDate.loe(batchFacility.getBatchDate()))
				.list(qCcsOutsideDdTxn.txnId);
	}
	@Override
	protected CcsOutsideDdTxn loadItemByKey(Long key) {
		return em.find(CcsOutsideDdTxn.class, key);
	}

}
