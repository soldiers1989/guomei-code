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
import com.sunline.ccs.batch.rpt.cca200.items.LoanRecoveryRptItem;
import com.sunline.ccs.batch.rpt.common.RptParamFacility;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsOrder;
import com.sunline.ccs.infrastructure.shared.model.QCcsOrderHst;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.pcm.param.def.Product;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.LoanUsage;
import com.sunline.ppy.dictionary.enums.OrderStatus;
import com.sunline.ppy.dictionary.enums.Ownership;

/**
 *	代位追偿报表
 */
public class RA203LoanRecoveryRpt extends KeyBasedStreamReader<RA203Key, LoanRecoveryRptItem> {
	@PersistenceContext
    private EntityManager em;
	@Autowired
	BatchStatusFacility batchStatusFacility;
	@Autowired
	private RptParamFacility codeProv;
	
	private QCcsOrderHst qOrderHst = QCcsOrderHst.ccsOrderHst;
	private QCcsOrder qOrder = QCcsOrder.ccsOrder;
	private QCcsAcct qAcct = QCcsAcct.ccsAcct;
	private QCcsLoan qLoan = QCcsLoan.ccsLoan;
	
	@Override
	protected List<RA203Key> loadKeys() {
		
		List<RA203Key> keys = new ArrayList<RA203Key>();
		Date batchDate = batchStatusFacility.getBatchDate();
		List<String> productCdList = codeProv.loadProductCdList(Ownership.P, LoanType.MCEI, codeProv.YG_ACQ_ACCEPTOR_ID);
		if(productCdList == null || productCdList.size() <=0 ) return keys;
		
		JPAQuery queryHst = new JPAQuery(em).from(qOrderHst, qAcct)
			.where(qOrderHst.acctNbr.eq(qAcct.acctNbr).and(qOrderHst.acctType.eq(qAcct.acctType))
					.and(qOrderHst.optDatetime.eq(batchDate))
					.and(qAcct.productCd.in(productCdList))
					.and(qOrderHst.loanUsage.eq(LoanUsage.S))
					.and(qOrderHst.onlineFlag.eq(Indicator.Y).and(qOrderHst.orderStatus.eq(OrderStatus.S))
							.or(qOrderHst.onlineFlag.eq(Indicator.N).and(qOrderHst.orderStatus.eq(OrderStatus.D))))
					);
		JPAQuery query = new JPAQuery(em).from(qOrder, qAcct)
				.where(qOrder.acctNbr.eq(qAcct.acctNbr).and(qOrder.acctType.eq(qAcct.acctType))
						.and(qOrder.optDatetime.eq(batchDate))
						.and(qAcct.productCd.in(productCdList))
						.and(qOrder.loanUsage.eq(LoanUsage.S))
						.and(qOrder.onlineFlag.eq(Indicator.Y).and(qOrder.orderStatus.eq(OrderStatus.S))
								.or(qOrder.onlineFlag.eq(Indicator.N).and(qOrder.orderStatus.eq(OrderStatus.D))))
						);
		for(Long orderHstid : queryHst.list(qOrderHst.orderId)){
			RA203Key key = new RA203Key();
			key.setId(orderHstid);
			key.setIsOrderHst(true);
			keys.add(key);
		}
		for(Long orderId : query.list(qOrder.orderId)){
			RA203Key key = new RA203Key();
			key.setId(orderId);
			key.setIsOrderHst(false);
			keys.add(key);
		}
		
		return keys;
	}

	@Override
	protected LoanRecoveryRptItem loadItemByKey(RA203Key  key) {
		LoanRecoveryRptItem item = new LoanRecoveryRptItem();
		String dueBillNo = null;
		if(key.getIsOrderHst()){
			Tuple orderHst = new JPAQuery(em).from(qOrderHst)
					.where(qOrderHst.orderId.eq(key.getId()))
					.singleResult(qOrderHst.dueBillNo, qOrderHst.usrName, qOrderHst.certId, 
							qOrderHst.successAmt, qOrderHst.businessDate);
			dueBillNo = orderHst.get(qOrderHst.dueBillNo);

			item.recoveryAmt = orderHst.get(qOrderHst.successAmt).setScale(2, RoundingMode.HALF_UP);
			item.recoveryDate = orderHst.get(qOrderHst.businessDate);
			
		}else{
			Tuple order = new JPAQuery(em).from(qOrder)
					.where(qOrder.orderId.eq(key.getId()))
					.singleResult(qOrder.dueBillNo, qOrder.usrName, qOrder.certId, 
							qOrder.successAmt, qOrder.businessDate);
			dueBillNo = order.get(qOrder.dueBillNo);
			item.recoveryAmt = order.get(qOrder.successAmt).setScale(2, RoundingMode.HALF_UP);
			item.recoveryDate = order.get(qOrder.businessDate);
		}
		
		Tuple loanAcct = new JPAQuery(em).from(qLoan, qAcct)
				.where(qLoan.dueBillNo.eq(dueBillNo)
						.and(qLoan.acctNbr.eq(qAcct.acctNbr))
						.and(qLoan.acctType.eq(qAcct.acctType)))
				.singleResult(qLoan.contrNbr,qLoan.activeDate,qLoan.loanInitPrin, 
						qLoan.loanCode, qAcct.productCd, qAcct.name, qAcct.custId);
		
		item.contrNbr = loanAcct.get(qLoan.contrNbr);
		item.activeDate = loanAcct.get(qLoan.activeDate);
		item.loanAmt = loanAcct.get(qLoan.loanInitPrin).setScale(2, RoundingMode.HALF_UP);
		item.name = loanAcct.get(qAcct.name);
		item.idNo = loanAcct.get(qAcct.custId).toString();
		
		Product product = codeProv.loadProduct(loanAcct.get(qAcct.productCd));
		LoanPlan loanPlan = codeProv.loadLoanPlan(loanAcct.get(qLoan.loanCode));
		item.productCd = product.productCode;
		item.productDesc = product.description;
		item.loanCode = loanPlan.loanCode;
		item.loanDesc = loanPlan.description;

		return item;
	}

}
