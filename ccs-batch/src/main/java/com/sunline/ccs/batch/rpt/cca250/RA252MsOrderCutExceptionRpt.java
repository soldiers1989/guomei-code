package com.sunline.ccs.batch.rpt.cca250;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.query.Tuple;
import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.batch.KeyBasedStreamReader;
import com.sunline.ccs.batch.rpt.cca250.items.MsOrderCutExceptionRptItem;
import com.sunline.ccs.batch.rpt.common.RptParamFacility;
import com.sunline.ccs.infrastructure.shared.model.CcsOrder;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsOrder;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.pcm.param.def.Product;
import com.sunline.ppy.dictionary.enums.CommandType;
import com.sunline.ppy.dictionary.enums.LoanUsage;
import com.sunline.ppy.dictionary.enums.OrderStatus;

/**
 * 扣款异常报表
 * @author wanghl
 *
 */
public class RA252MsOrderCutExceptionRpt extends 
		KeyBasedStreamReader<Long, MsOrderCutExceptionRptItem> {
	Logger  logger = LoggerFactory.getLogger(this.getClass());
	
	@PersistenceContext
	private EntityManager em;
	@Autowired
	private RptParamFacility codeProv;
	@Autowired
	private BatchStatusFacility batchStatusFacility;
	
	private QCcsOrder qOrder = QCcsOrder.ccsOrder;
	private QCcsAcct qAcct = QCcsAcct.ccsAcct;
	private QCcsCustomer qCust = QCcsCustomer.ccsCustomer;
	private QCcsLoan qLoan = QCcsLoan.ccsLoan;
	
	@Override
	protected List<Long> loadKeys() {

		return new JPAQuery(em).from(qOrder)
				.where(qOrder.commandType.in(CommandType.SDB, CommandType.BDB)
						.and(qOrder.orderStatus.in(OrderStatus.W )))
				.list(qOrder.orderId);
	}

	@Override
	protected MsOrderCutExceptionRptItem loadItemByKey(Long key) {
		MsOrderCutExceptionRptItem item = new MsOrderCutExceptionRptItem();
		Date batchDate = batchStatusFacility.getBatchDate();
		
		CcsOrder order = em.find(CcsOrder.class, key);
		logger.info("订单[{}]用途[{}]账户[{}][{}]", order.getOrderId(), order.getLoanUsage(), order.getAcctNbr(), order.getAcctType());
	
		if(LoanUsage.C.equals(order.getLoanUsage()) ){
			logger.info("===理赔订单[{}]原订单Id[{}]===", order.getOrderId(), order.getOriOrderId());
			if(order.getOriOrderId() == null){
				logger.info("汇总理赔订单,调用支付");
				item.name = order.getUsrName();
				item.idNo = order.getAcqId();
			}else{
				logger.info("记录账户理赔记录的子订单,不调用支付");
				return null;
			}
		}else{
			logger.info("===客户还款[{}]===", order.getOrderId());
			Tuple acct = new JPAQuery(em).from(qAcct)
					.where(qAcct.acctNbr.eq(order.getAcctNbr()).and(qAcct.acctType.eq(order.getAcctType())))
					.singleResult(qAcct.custId, qAcct.contrNbr, qAcct.productCd, qAcct.org);
			if(acct != null)
				logger.debug("ProductCd[{}]客户号[{}]Org[{}]", 
						acct.get(qAcct.productCd), acct.get(qAcct.custId), acct.get(qAcct.org));
			else{
				logger.debug("账户不存在");
				return null;
			}
			Tuple cust = new JPAQuery(em).from(qCust)
					.where(qCust.custId.eq(acct.get(qAcct.custId)))
					.singleResult(qCust.name, qCust.idNo, qCust.idType);
			
			String loanCode = new JPAQuery(em).from(qLoan)
					.where(qLoan.acctNbr.eq(order.getAcctNbr()).and(qLoan.acctType.eq(order.getAcctType())))
					.singleResult(qLoan.loanCode);
			
			Product product = codeProv.loadParameter(acct.get(qAcct.productCd), Product.class, acct.get(qAcct.org));
			if(StringUtils.isBlank(loanCode)){
				ProductCredit pc = codeProv.loadParameter(acct.get(qAcct.productCd), ProductCredit.class, acct.get(qAcct.org));
				loanCode = pc.loanPlansMap.get(pc.defaultLoanType);
			}
			LoanPlan loanPlan = codeProv.loadParameter(loanCode, LoanPlan.class, acct.get(qAcct.org));
			logger.debug("贷款产品编码LoanCode[{}]", loanCode);
			
			item.productCd = product.productCode;
			item.productDesc = product.description;
			item.loanCode = loanPlan.loanCode;
			item.loanDesc = loanPlan.description;
			item.contrNbr = acct.get(qAcct.contrNbr);
			item.name = cust.get(qCust.name);
			item.idNo = cust.get(qCust.idNo);
		}
		item.searchDate = batchDate;
		item.cutAmt = order.getTxnAmt();
		item.cutStatus = order.getOrderStatus().name();
		return item;
	}

}
