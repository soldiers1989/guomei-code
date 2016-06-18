package com.sunline.ccs.batch.rpt.cca200;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.query.Tuple;
import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.batch.KeyBasedStreamReader;
import com.sunline.ccs.batch.rpt.cca200.items.LoanRepayRptItem;
import com.sunline.ccs.batch.rpt.common.RptParamFacility;
import com.sunline.ccs.infrastructure.shared.model.CcsRepayHst;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsOrder;
import com.sunline.ccs.infrastructure.shared.model.QCcsOrderHst;
import com.sunline.ccs.infrastructure.shared.model.QCcsPlan;
import com.sunline.ccs.infrastructure.shared.model.QCcsRepayHst;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.pcm.param.def.Product;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.LoanUsage;
import com.sunline.ppy.dictionary.enums.OrderStatus;
import com.sunline.ppy.dictionary.enums.Ownership;
import com.sunline.ppy.dictionary.enums.PlanType;

/**
 * 还款结果查询报表文件
 * @author wanghl
 *
 */
public class RA206LoanRepayRpt extends KeyBasedStreamReader<RA206Key,LoanRepayRptItem>{
	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@PersistenceContext
    private EntityManager em;
	@Autowired
	BatchStatusFacility batchStatusFacility;
	@Autowired
	private RptParamFacility codeProv;
	
	private Map<String,RA206Key> keymap = new HashMap<String,RA206Key>();
	private QCcsRepayHst qRepay = QCcsRepayHst.ccsRepayHst;
	private QCcsLoan qLoan = QCcsLoan.ccsLoan;
	private QCcsPlan qPlan = QCcsPlan.ccsPlan;
	private QCcsAcct qAcct = QCcsAcct.ccsAcct;
	private QCcsOrderHst qOrderHst = QCcsOrderHst.ccsOrderHst;
	private QCcsOrder qOrder = QCcsOrder.ccsOrder;
	
	@Override
	protected List<RA206Key> loadKeys() {
		
		Date batchDate = batchStatusFacility.getBatchDate();
		List<RA206Key> keyList = new ArrayList<RA206Key>();
		List<String> productCdList = codeProv.loadProductCdList(Ownership.P, LoanType.MCEI, codeProv.YG_ACQ_ACCEPTOR_ID);
		if(productCdList == null || productCdList.size() <=0 ) return keyList;
		
		//获取当日所有还款分配信息，关联Plan表取得交易参考号
		List<Tuple> repayExList = new JPAQuery(em).from(qRepay, qPlan, qAcct)
				.where(qRepay.acctNbr.eq(qAcct.acctNbr)
						.and(qRepay.acctType.eq(qAcct.acctType))
						.and(qAcct.productCd.in(productCdList))
						.and(qRepay.planId.eq(qPlan.planId))
						.and(qRepay.batchDate.eq(batchDate))
						.and(qPlan.planType.in(PlanType.I, PlanType.Q))
						.and(qPlan.term.isNotNull()))
				.list(qRepay, qPlan.refNbr);
		
		logger.info("还款分配信息[{}]条",repayExList.size());
		//还款分配按交易参考号分组，汇总相同余额成分的分配金额
		for(Tuple t : repayExList){
			CcsRepayHst repay = t.get(qRepay);
			if(keymap.containsKey(t.get(qPlan.refNbr))){
				RA206Key key = keymap.get(t.get(qPlan.refNbr));
				addBnpAmt(key, repay);
			}else{
				RA206Key key = new RA206Key();
				key.setRefNbr(t.get(qPlan.refNbr));
				key.setAcctNbr(repay.getAcctNbr());
				key.setAcctType(repay.getAcctType());
				addBnpAmt(key, repay);
				keymap.put(key.getRefNbr(), key);
			}
		}
		Iterator<RA206Key> keyIt = keymap.values().iterator();
		while(keyIt.hasNext()){
			keyList.add(keyIt.next());
		}
		return keyList;
	}

	/**
	 * 累加同一Loan同余额成分的金额
	 */
	private void addBnpAmt(RA206Key key, CcsRepayHst repay) {
		logger.info("RefNbr[{}]repayId[{}]bnpType[{}]repayAmt[{}]",key.getRefNbr(),repay.getPaymentId(),repay.getBnpType(),repay.getRepayAmt());
		switch (repay.getBnpType()) {
		case pastPrincipal:
		case ctdPrincipal: key.setRepayPrincipal(key.getRepayPrincipal().add(repay.getRepayAmt())); break;
		case pastInterest:
		case ctdInterest: key.setRepayInterest(key.getRepayInterest().add(repay.getRepayAmt())); break;
		case ctdTxnFee: 
		case pastTxnFee: key.setRepayFineFee(key.getRepayFineFee().add(repay.getRepayAmt())); break;
		case pastIns:
		case ctdIns: 	key.setRepayIns(key.getRepayIns().add(repay.getRepayAmt())); break;
		case pastMulct:
		case ctdMulct: key.setRepayMulct(key.getRepayMulct().add(repay.getRepayAmt())); break;
		default: break;
		}
	}

	@Override
	protected LoanRepayRptItem loadItemByKey(RA206Key key) {
		LoanRepayRptItem item = new LoanRepayRptItem();
		Date batchDate = batchStatusFacility.getBatchDate();

		Tuple loan = new JPAQuery(em).from(qLoan)
				.where(qLoan.refNbr.eq(key.getRefNbr()))
				.singleResult(qLoan.loanInitPrin, qLoan.contrNbr, qLoan.activeDate, qLoan.dueBillNo, qLoan.loanCode);
		logger.debug("借据号[{}]合同号[{}]",loan.get(qLoan.dueBillNo),loan.get(qLoan.contrNbr));
		
		item.contrNbr = loan.get(qLoan.contrNbr);
		item.activeDate = loan.get(qLoan.activeDate);
		item.loanAmt = loan.get(qLoan.loanInitPrin);
		
		//获取当日完成的原始订单历史的金额
		BigDecimal repayAmt = null;
		
		Tuple orderHst = new JPAQuery(em).from(qOrderHst)
				.where(qOrderHst.optDatetime.eq(batchDate)
						.and(qOrderHst.dueBillNo.eq(loan.get(qLoan.dueBillNo)))
						.and((qOrderHst.loanUsage.eq(LoanUsage.O)
									.and(qOrderHst.onlineFlag.eq(Indicator.Y).and(qOrderHst.orderStatus.eq(OrderStatus.S))
										.or(qOrderHst.onlineFlag.eq(Indicator.N).and(qOrderHst.orderStatus.eq(OrderStatus.D)).and(qOrderHst.oriOrderId.isNull()))
									)
								)
								.or(qOrderHst.loanUsage.in(LoanUsage.M, LoanUsage.N).and(qOrderHst.orderStatus.eq(OrderStatus.S)))
							)
						)
				.singleResult(qOrderHst.orderId, qOrderHst.txnAmt, qOrderHst.orderStatus, qOrderHst.successAmt);
		if(orderHst != null ){
			logger.debug("原始订单号[{}]",orderHst==null?null:orderHst.get(qOrderHst.orderId));
			
			repayAmt = orderHst.get(qOrderHst.successAmt);
		}else{//订单历史没有记录就从订单表找
			
			Tuple order = new JPAQuery(em).from(qOrder)
					.where(qOrder.optDatetime.eq(batchDate)
							.and(qOrder.dueBillNo.eq(loan.get(qLoan.dueBillNo)))
							.and((qOrder.loanUsage.eq(LoanUsage.O)
										.and(qOrder.onlineFlag.eq(Indicator.Y).and(qOrder.orderStatus.eq(OrderStatus.S))
											.or(qOrder.onlineFlag.eq(Indicator.N).and(qOrder.orderStatus.eq(OrderStatus.D)).and(qOrder.oriOrderId.isNull()))
										)
									)
									.or(qOrder.loanUsage.in(LoanUsage.M, LoanUsage.N).and(qOrder.orderStatus.eq(OrderStatus.S)))
								)
							)
					.singleResult(qOrder.orderId, qOrder.txnAmt, qOrder.orderStatus, qOrder.successAmt);
			
			logger.debug("原始订单号[{}]",order==null?null:order.get(qOrder.orderId));
			if(order != null){
				repayAmt = order.get(qOrder.successAmt);
			}
		}
		
		item.repayAmt = repayAmt;
			
		Tuple custAcct = new JPAQuery(em).from( qAcct)
				.where( qAcct.acctNbr.eq(key.getAcctNbr()) 
						.and(qAcct.acctType.eq(key.getAcctType())))
				.singleResult(qAcct.productCd, qAcct.name, qAcct.custId);
		
		item.name = custAcct.get(qAcct.name);
		item.idNo = custAcct.get(qAcct.custId).toString();
		
		LoanPlan loanPlan = codeProv.loadLoanPlan(loan.get(qLoan.loanCode));
		Product product = codeProv.loadProduct(custAcct.get(qAcct.productCd));
		item.productCd = product.productCode;
		item.productDesc = product.description;
		item.loanCode = loanPlan.loanCode;
		item.loanDesc = loanPlan.description;

		item.actualRepayPrincipal = key.getRepayPrincipal().setScale(2, RoundingMode.HALF_UP);
		item.actualRepayInterest = key.getRepayInterest().setScale(2, RoundingMode.HALF_UP);
		item.actualRepayMulct = key.getRepayMulct().setScale(2, RoundingMode.HALF_UP);
		item.actualRepayIns = key.getRepayIns().setScale(2, RoundingMode.HALF_UP);
		item.actualRepayFineFee = key.getRepayFineFee().setScale(2, RoundingMode.HALF_UP);
		item.repayDate = batchDate;
		return item;
	}

}
