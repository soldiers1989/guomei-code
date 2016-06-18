package com.sunline.ccs.batch.rpt.cca250;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.query.Tuple;
import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.batch.KeyBasedStreamReader;
import com.sunline.ccs.batch.rpt.cca250.items.MsOrderPayExceptionRptItem;
import com.sunline.ccs.batch.rpt.common.RptParamFacility;
import com.sunline.ccs.infrastructure.shared.model.CcsOrder;
import com.sunline.ccs.infrastructure.shared.model.CcsOrderHst;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoanRegHst;
import com.sunline.ccs.infrastructure.shared.model.QCcsOrder;
import com.sunline.ccs.infrastructure.shared.model.QCcsOrderHst;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.pcm.param.def.Product;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.CommandType;
import com.sunline.ppy.dictionary.enums.LoanUsage;
import com.sunline.ppy.dictionary.enums.OrderStatus;

/**
 * 付款异常报表
 * @author wanghl
 *
 */
public class RA253MsOrderPayExceptionRpt extends
		KeyBasedStreamReader<RA253Key, MsOrderPayExceptionRptItem> {
	Logger  logger = LoggerFactory.getLogger(this.getClass());
	
	@PersistenceContext
	private EntityManager em;
	@Autowired
	private RptParamFacility codeProv;
	@Autowired
	private BatchStatusFacility batchStatusFacility;
	
	private QCcsOrder qOrder = QCcsOrder.ccsOrder;
	private QCcsOrderHst qOrderHst = QCcsOrderHst.ccsOrderHst;
	private QCcsAcct qAcct = QCcsAcct.ccsAcct;
	private QCcsCustomer qCust = QCcsCustomer.ccsCustomer;
	private QCcsLoanReg qReg = QCcsLoanReg.ccsLoanReg;
	private QCcsLoanRegHst qRegHst = QCcsLoanRegHst.ccsLoanRegHst;

	@Override
	protected List<RA253Key> loadKeys() {
		 List<RA253Key> keyList = new ArrayList<RA253Key>();
		 
		 //只取出支付返回码code字段有值的订单，无值说明没有调用支付
		 List<Long> orderIdList = new JPAQuery(em).from(qOrder)
				.where(qOrder.commandType.in(CommandType.SPA, CommandType.BDA)
						.and(qOrder.code.isNotNull())
						.and(qOrder.orderStatus.in(OrderStatus.W, OrderStatus.E)))
				.list(qOrder.orderId);
		 //随借随还代付失败订单当天对账后会转移历史
		 List<Long> orderHstIdList = new JPAQuery(em).from(qOrderHst)
					.where(qOrderHst.commandType.in(CommandType.SPA, CommandType.BDA)
							.and(qOrderHst.code.isNotNull())
							.and(qOrderHst.orderStatus.in(OrderStatus.W, OrderStatus.E)))
					.list(qOrderHst.orderId);
		 for(Long id : orderIdList){
			 RA253Key key = new RA253Key();
			 key.setIsOrderHst(false);
			 key.setOrderId(id);
			 keyList.add(key);
		 }
		 for(Long id : orderHstIdList){
			 RA253Key key = new RA253Key();
			 key.setIsOrderHst(true);
			 key.setOrderId(id);
			 keyList.add(key);
		 }
		return keyList;
	}

	@Override
	protected MsOrderPayExceptionRptItem loadItemByKey(RA253Key key) {
		logger.info("订单ID[{}]", key.getOrderId());
		
		MsOrderPayExceptionRptItem item = new MsOrderPayExceptionRptItem();
		Date batchDate = batchStatusFacility.getBatchDate();
		
		OrderInfo orderInfo = initOrderInfo(key);
		
		if(OrderStatus.E.equals(orderInfo.orderStatus)){
			if(!DateUtils.truncatedEquals(batchDate, orderInfo.optDateTime , Calendar.DATE)){
				logger.info("订单状态为失败且今日无更新");
				return null;
			}
		}
		
		if(LoanUsage.B.equals(orderInfo.loanUsage )){
			logger.info("===结算订单[{}]===", key.getOrderId());
			item.name = orderInfo.usrName;
			item.idNo = orderInfo.acqId;
		}else{
			logger.info("===代付订单[{}]===", key.getOrderId());
			//代付订单应有账户
			if(orderInfo.acctNbr == null) return null;
			
			Tuple acct = new JPAQuery(em).from(qAcct)
					.where(qAcct.acctNbr.eq(orderInfo.acctNbr).and(qAcct.acctType.eq(orderInfo.acctType)))
					.singleResult(qAcct.productCd, qAcct.custId, qAcct.org, qAcct.contrNbr);
			if(acct != null)
				logger.info("马上贷账户[{}][{}]ProductCd[{}]客户号[{}]Org[{}]", 
						orderInfo.acctNbr, orderInfo.acctType, acct.get(qAcct.productCd), acct.get(qAcct.custId), acct.get(qAcct.org));
			else{
				logger.info("账户[{}][{}]不存在", orderInfo.acctNbr, orderInfo.acctType);
				return null;
			}
			Tuple cust = new JPAQuery(em).from(qCust)
					.where(qCust.custId.eq(acct.get(qAcct.custId)))
					.singleResult(qCust.name, qCust.idNo, qCust.idType);
			
			String loanCode = new JPAQuery(em).from(qReg)
					.where(qReg.acctNbr.eq(orderInfo.acctNbr).and(qReg.acctType.eq(orderInfo.acctType)))
					.singleResult(qReg.loanCode);
			logger.debug("CcsLoanReg.LoanCode[{}]", loanCode);
			if(loanCode == null){
				loanCode = new JPAQuery(em).from(qRegHst)
						.where(qRegHst.acctNbr.eq(orderInfo.acctNbr).and(qRegHst.acctType.eq(orderInfo.acctType)))
						.singleResult(qRegHst.loanCode);
				logger.debug("CcsLoanRegHst.LoanCode[{}]LoanId[{}]", loanCode);
			}
			
			Product product = codeProv.loadParameter(acct.get(qAcct.productCd), Product.class, acct.get(qAcct.org));
			if(loanCode != null){
				LoanPlan loanPlan = codeProv.loadParameter(loanCode, LoanPlan.class, acct.get(qAcct.org));
				item.loanCode = loanPlan.loanCode;
				item.loanDesc = loanPlan.description;
			}
			item.productCd = product.productCode;
			item.productDesc = product.description;
			item.contrNbr = acct.get(qAcct.contrNbr);
			item.name = cust.get(qCust.name);
			item.idNo = cust.get(qCust.idNo);
		}
		item.searchDate = batchDate;
		item.payAmt = orderInfo.orderAmt ;
		item.payStatus = orderInfo.orderStatus.name();
		return item;
	}

	private OrderInfo initOrderInfo(RA253Key key) {
		OrderInfo orderInfo = new OrderInfo();
		if(key.getIsOrderHst()){
			CcsOrderHst orderHst = em.find(CcsOrderHst.class, key.getOrderId());
			orderInfo.acctNbr = orderHst.getAcctNbr();
			orderInfo.acctType = orderHst.getAcctType();
			orderInfo.orderStatus = orderHst.getOrderStatus();
			orderInfo.orderAmt = orderHst.getTxnAmt();
			orderInfo.loanUsage = orderHst.getLoanUsage();
			orderInfo.optDateTime = orderHst.getOptDatetime();
			
		}else{
			CcsOrder order = em.find(CcsOrder.class, key.getOrderId());
			orderInfo.acctNbr = order.getAcctNbr();
			orderInfo.acctType = order.getAcctType();
			orderInfo.orderStatus = order.getOrderStatus();
			orderInfo.orderAmt = order.getTxnAmt();
			orderInfo.loanUsage = order.getLoanUsage();
			orderInfo.optDateTime = order.getOptDatetime();
		}
		return orderInfo;
	}
	
	public class OrderInfo{
		public String acqId;
		public Long acctNbr;
		public AccountType acctType;
		public OrderStatus orderStatus;
		public BigDecimal orderAmt;
		public LoanUsage loanUsage;
		public Date optDateTime;
		public String usrName;
		
	}
	
	public static void main(String[] args) {
		try {
			Date date = new SimpleDateFormat("yyyyMMddHHmmss").parse("20151208125959");
			System.out.println(date);
			System.out.println(DateUtils.truncatedEquals(date , 
					new Date(), 
					Calendar.DATE));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
