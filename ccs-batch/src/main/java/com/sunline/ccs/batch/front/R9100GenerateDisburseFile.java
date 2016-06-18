package com.sunline.ccs.batch.front;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.batch.KeyBasedReader;
import com.sunline.ccs.infrastructure.shared.model.CcsOrder;
import com.sunline.ccs.infrastructure.shared.model.QCcsOrder;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanUsage;
import com.sunline.ppy.dictionary.enums.OrderStatus;

/**
 * 读取溢缴款转出的订单
 * 
 * @author MengXiang
 * 
 */
public class R9100GenerateDisburseFile extends KeyBasedReader<Long, CcsOrder> {

	@Autowired
	private BatchStatusFacility batchStatusFacility;

	@PersistenceContext
	private EntityManager em;

	@Override
	protected CcsOrder loadItemByKey(Long key) {
		return em.find(CcsOrder.class, key);
	}

	@Override
	protected List<Long> loadKeys() {
		QCcsOrder o = QCcsOrder.ccsOrder;
		// 当日处理中溢缴款订单
		return new JPAQuery(em).from(o)
				.where(o.businessDate.eq(batchStatusFacility.getSystemStatus().getBusinessDate())
						.and(o.onlineFlag.eq(Indicator.N))
						.and(o.orderStatus.eq(OrderStatus.W))
						.and(o.loanUsage.eq(LoanUsage.D)))
				.orderBy(o.orderId.asc())
				.list(o.orderId);
	}
	
}
