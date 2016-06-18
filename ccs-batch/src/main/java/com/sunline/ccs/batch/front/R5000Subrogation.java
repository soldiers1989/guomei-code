package com.sunline.ccs.batch.front;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.batch.KeyBasedStreamReader;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctKey;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsOrder;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsOrder;
import com.sunline.ccs.param.def.enums.LoanTerminateReason;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanUsage;
import com.sunline.ppy.dictionary.enums.OrderStatus;

/**
 * 追偿拆分代扣
 * @author zhangqiang
 *
 */
public class R5000Subrogation extends KeyBasedStreamReader<Long, SFrontInfo> {
	
	@Autowired
	private BatchStatusFacility batchStatusFacility;
	
	@PersistenceContext
	private EntityManager em;
	
	@Override
	protected SFrontInfo loadItemByKey(Long key) {
		SFrontInfo info = new SFrontInfo();
		
		CcsOrder order = em.find(CcsOrder.class, key);
		
		CcsAcctKey acctKey = new CcsAcctKey();
		acctKey.setAcctNbr(order.getAcctNbr());
		acctKey.setAcctType(order.getAcctType());
		CcsAcct acct = em.find(CcsAcct.class, acctKey);
		
		CcsCustomer cust = em.find(CcsCustomer.class, acct.getCustId());
		
		QCcsLoan l = QCcsLoan.ccsLoan;
		CcsLoan loan = new JPAQuery(em).from(l)
				.where(l.dueBillNo.eq(order.getDueBillNo())
						.and(l.terminalReasonCd.eq(LoanTerminateReason.C)))
				.singleResult(l);
		
		info.setAcct(acct);
		info.setCust(cust);
		info.setLoan(loan);
		info.setrOrder(order);
		
		return info;
	}

	@Override
	protected List<Long> loadKeys() {
		// 当日 && 联机追偿 && 失败  订单
		QCcsOrder o = QCcsOrder.ccsOrder;
		return new JPAQuery(em).from(o).where(o.businessDate.eq(batchStatusFacility.getSystemStatus().getBusinessDate())
				.and(o.loanUsage.eq(LoanUsage.S))
				.and(o.onlineFlag.eq(Indicator.Y))
				.and(o.orderStatus.eq(OrderStatus.E)))
			.list(o.orderId);
	}
	
}