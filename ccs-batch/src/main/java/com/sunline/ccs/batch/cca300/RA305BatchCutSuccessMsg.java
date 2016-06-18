package com.sunline.ccs.batch.cca300;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.batch.KeyBasedStreamReader;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctKey;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsOrder;
import com.sunline.ccs.infrastructure.shared.model.CcsOrderHst;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsOrder;
import com.sunline.ccs.infrastructure.shared.model.QCcsOrderHst;
import com.sunline.ppy.dictionary.enums.CommandType;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanUsage;
import com.sunline.ppy.dictionary.enums.OrderStatus;

/**
 * 批扣成功通知客户短信--读取订单
 *  1.正常扣款和提前结清扣款成功，没有扣款拆分按正常发送一笔扣款成功短信。
 *  2.逾期后扣款成功处理：逾期用户扣款，如果是拆分多笔批量扣款，需程序处理为最终扣款成功叠加总额，发送一条短信。
 * 
 * @author lizz
 * 
 */
public class RA305BatchCutSuccessMsg extends KeyBasedStreamReader<Long, BatchCutSmsInfo> {

	@Autowired
	private BatchStatusFacility batchStatusFacility;

	@PersistenceContext
	private EntityManager em;

	@Override
	protected BatchCutSmsInfo loadItemByKey(Long key) {
		CcsOrder order = em.find(CcsOrder.class, key);
		CcsOrderHst orderHst = em.find(CcsOrderHst.class, key);
		BatchCutSmsInfo info = new BatchCutSmsInfo();
		CcsAcct acct = null;
		CcsLoan loan = null;
		if(null != order) {
			acct = em.find(CcsAcct.class, new CcsAcctKey(order.getAcctNbr(), order.getAcctType()));
			
			QCcsLoan l = QCcsLoan.ccsLoan;
			loan = new JPAQuery(em).from(l).where(l.acctNbr.eq(order.getAcctNbr()).and(l.acctType.eq(order.getAcctType()))
					.and(l.dueBillNo.eq(order.getDueBillNo())))
					.singleResult(l);
		} else if(null != orderHst) {
			acct = em.find(CcsAcct.class, new CcsAcctKey(orderHst.getAcctNbr(), orderHst.getAcctType()));
			
			QCcsLoan l = QCcsLoan.ccsLoan;
			loan = new JPAQuery(em).from(l).where(l.acctNbr.eq(orderHst.getAcctNbr()).and(l.acctType.eq(orderHst.getAcctType()))
					.and(l.dueBillNo.eq(orderHst.getDueBillNo())))
					.singleResult(l);
		}
		
		info.setAcct(acct);
		info.setLoan(loan);
		info.setOrder(order);
		info.setOrderHst(orderHst);
		
		return info;
	}

	@Override
	protected List<Long> loadKeys() {
		QCcsOrderHst oHst = QCcsOrderHst.ccsOrderHst;
		QCcsOrder o = QCcsOrder.ccsOrder;
		List<Long> orderIdList = new ArrayList<Long>();
		List<Long> orderIds = null;
		List<Long> orderHstIds = null;
		// 小批量生成的当日处理中订单
		orderIds = new JPAQuery(em).from(o)
				.where(o.optDatetime.eq(batchStatusFacility.getBatchDate())
						.and(o.onlineFlag.eq(Indicator.N)).and(o.commandType.eq(CommandType.BDB))
						.and(o.loanUsage.in(LoanUsage.M,LoanUsage.N,LoanUsage.O))
						.and(o.orderStatus.in(OrderStatus.S,OrderStatus.D))
						.and(o.oriOrderId.isNull()))
						.orderBy(o.orderId.asc())
						.list(o.orderId);
		orderHstIds = new JPAQuery(em).from(oHst)
				.where(oHst.optDatetime.eq(batchStatusFacility.getBatchDate())
						.and(oHst.onlineFlag.eq(Indicator.N)).and(oHst.commandType.eq(CommandType.BDB))
						.and(oHst.loanUsage.in(LoanUsage.M,LoanUsage.N,LoanUsage.O))
						.and(oHst.orderStatus.in(OrderStatus.S,OrderStatus.D))
						.and(oHst.oriOrderId.isNull()))
				.orderBy(oHst.orderId.asc())
				.list(oHst.orderId);
		if(null != orderIds && orderIds.size() > 0) 
			orderIdList.addAll(orderIds);
		if(null != orderHstIds && orderHstIds.size() > 0) 
			orderIdList.addAll(orderHstIds);
		return orderIdList;
	}
	
}
