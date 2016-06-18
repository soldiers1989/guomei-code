package com.sunline.ccs.batch.cc1400;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.batch.KeyBasedStreamReader;
import com.sunline.ccs.infrastructure.shared.model.CcsOrder;
import com.sunline.ccs.infrastructure.shared.model.QCcsOrder;

/**
 * 马上对账文件-订单处理
 * @author liuq
 *
 */
public class R1404MsxfOrder extends KeyBasedStreamReader<Long, CcsOrder> {
	@PersistenceContext
    private EntityManager em;
	@Autowired
	private BatchStatusFacility batchFacility;
	
	@Override
	protected List<Long> loadKeys() {
		QCcsOrder qOrder = QCcsOrder.ccsOrder;
		return new JPAQuery(em).from(qOrder)
				.where(qOrder.businessDate.loe(batchFacility.getBatchDate()))
				.list(qOrder.orderId);
	}
	@Override
	protected CcsOrder loadItemByKey(Long key) {
		return em.find(CcsOrder.class, key);
	}

}
