package com.sunline.ccs.batch.rpt.cca200;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.query.Tuple;
import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.batch.KeyBasedStreamReader;
import com.sunline.ccs.batch.rpt.cca200.items.LoanRecoveryPayRptItem;
import com.sunline.ccs.batch.rpt.common.RptParamFacility;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsOrder;
import com.sunline.ccs.infrastructure.shared.model.CcsOrderHst;
import com.sunline.ccs.infrastructure.shared.model.CcsSettleLoanHst;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsOrder;
import com.sunline.ccs.infrastructure.shared.model.QCcsOrderHst;
import com.sunline.ccs.infrastructure.shared.model.QCcsSettleLoanHst;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.pcm.param.def.Product;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanUsage;
import com.sunline.ppy.dictionary.enums.OrderStatus;

/**
 * 代位追偿付款报表
 * @author wanghl
 *
 */
public class RA207LoanRecoveryPayRpt extends KeyBasedStreamReader<RA207Key, LoanRecoveryPayRptItem> {
	@PersistenceContext
    private EntityManager em;
	@Autowired
	BatchStatusFacility batchStatusFacility;
	@Autowired
	private RptParamFacility codeProv;

	private QCcsSettleLoanHst qSettleLoanHst = QCcsSettleLoanHst.ccsSettleLoanHst;
	private QCcsOrderHst qOrderHst = QCcsOrderHst.ccsOrderHst;
	private QCcsOrder qOrder = QCcsOrder.ccsOrder;
	private QCcsLoan qLoan = QCcsLoan.ccsLoan;
	private QCcsAcct qAcct = QCcsAcct.ccsAcct;
	
	@Override
	protected List<RA207Key> loadKeys() {
		Date batchDate = batchStatusFacility.getBatchDate();
		List<RA207Key> keys = new ArrayList<RA207Key>();
		
		CcsSettleLoanHst settle = new JPAQuery(em).from(qSettleLoanHst)
				.where(qSettleLoanHst.settleDate.eq(batchDate))
				.singleResult(qSettleLoanHst);
		
		if(settle == null){
			return keys;
		}
		
		List<Long> recoverOrderHstIds = new JPAQuery(em).from(qOrderHst)
				.where(qOrderHst.loanUsage.eq(LoanUsage.S)
						.and(qOrderHst.onlineFlag.eq(Indicator.Y).and(qOrderHst.orderStatus.eq(OrderStatus.S))
								.or(qOrderHst.onlineFlag.eq(Indicator.N).and(qOrderHst.orderStatus.eq(OrderStatus.D)))
							)
						.and(qOrderHst.optDatetime.between(settle.getBeginDate(), settle.getEndDate())))
				.list(qOrderHst.orderId);
		if(recoverOrderHstIds.size() >0 ){
			for(Long orderId : recoverOrderHstIds){
				RA207Key key = new RA207Key();
				key.setIsHstOrder(true);
				key.setOrderId(orderId);
				keys.add(key);
			}
		}
		List<Long> recoverOrderIds = new JPAQuery(em).from(qOrder)
				.where(qOrder.loanUsage.eq(LoanUsage.S)
						.and(qOrder.onlineFlag.eq(Indicator.Y).and(qOrder.orderStatus.eq(OrderStatus.S))
								.or(qOrder.onlineFlag.eq(Indicator.N).and(qOrder.orderStatus.eq(OrderStatus.D)))
							)
						.and(qOrder.optDatetime.between(settle.getBeginDate(), settle.getEndDate())))
				.list(qOrder.orderId);
		if(recoverOrderIds.size() > 0){
			for(Long orderId : recoverOrderHstIds){
				RA207Key key = new RA207Key();
				key.setIsHstOrder(false);
				key.setOrderId(orderId);
				keys.add(key);
			}
		}
		return keys;
	}

	@Override
	protected LoanRecoveryPayRptItem loadItemByKey(RA207Key key) {
		Date batchDate = batchStatusFacility.getBatchDate();
		LoanRecoveryPayRptItem item = new LoanRecoveryPayRptItem();
		String dueBillNo = null;
		Long acctNbr = null;
		AccountType acctType = null;
		
		if(key.getIsHstOrder()){
			CcsOrderHst orderHst = em.find(CcsOrderHst.class, key.getOrderId());
			dueBillNo = orderHst.getDueBillNo();
			acctNbr = orderHst.getAcctNbr();
			acctType = orderHst.getAcctType();
			
			item.recoveryAmt = orderHst.getSuccessAmt().setScale(2, RoundingMode.HALF_UP);
			item.recoveryDate = batchDate;
		}else{
			item = new LoanRecoveryPayRptItem();
			CcsOrder order = em.find(CcsOrder.class, key.getOrderId());
			dueBillNo = order.getDueBillNo();
			acctNbr = order.getAcctNbr();
			acctType = order.getAcctType();
			
			item.recoveryAmt = order.getSuccessAmt().setScale(2, RoundingMode.HALF_UP);
			item.recoveryDate = batchDate;
		}
		
		CcsLoan loan = new JPAQuery(em).from(qLoan)
				.where(qLoan.dueBillNo.eq(dueBillNo ))
				.singleResult(qLoan);
		
		Tuple acct = new JPAQuery(em).from(qAcct)
				.where(qAcct.acctNbr.eq(acctNbr).and(qAcct.acctType.eq(acctType)))
				.singleResult(qAcct.productCd, qAcct.custId, qAcct.name);
		
		LoanPlan loanPlan = codeProv.loadLoanPlan(loan.getLoanCode());
		Product product = codeProv.loadProduct(acct.get(qAcct.productCd));
		item.productCd = product.productCode;
		item.productDesc = product.description;
		item.loanCode = loanPlan.loanCode;
		item.loanDesc = loanPlan.description;

		item.name = acct.get(qAcct.name);
		item.idNo = acct.get(qAcct.custId).toString();
		
		item.contrNbr = loan.getContrNbr();
		item.activeDate = loan.getActiveDate();
		item.loanAmt = loan.getLoanInitPrin().setScale(2, RoundingMode.HALF_UP);

		return item;
		
	}

}
