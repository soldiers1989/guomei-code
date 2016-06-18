package com.sunline.ccs.batch.rpt.cca230;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.query.Tuple;
import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ccs.batch.rpt.cca230.items.YZFTransactionFlowFileItem;
import com.sunline.ccs.batch.rpt.common.RptParamFacility;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.QCcsOrder;
import com.sunline.ccs.infrastructure.shared.model.QCcsOrderHst;
import com.sunline.ppy.dictionary.enums.LoanUsage;
import com.sunline.ppy.dictionary.enums.OrderStatus;

public class PA232YZFTransactionFlowFile implements ItemProcessor<RA232YZFKey, YZFTransactionFlowFileItem>{
	private static final Logger logger = LoggerFactory.getLogger(RA232YZFTransactionFlowFile.class);
	@PersistenceContext
    private EntityManager em;
	@Autowired
	BatchStatusFacility batchStatusFacility;
	@Autowired
	private RptParamFacility codeProv;

	private QCcsOrderHst ccsOrderHst = QCcsOrderHst.ccsOrderHst;
	private QCcsOrder ccsOrder = QCcsOrder.ccsOrder;
	private QCcsAcct ccsAcct = QCcsAcct.ccsAcct;
	private QCcsCustomer qCcsCustomer = QCcsCustomer.ccsCustomer;
	
	
	@Override
	public YZFTransactionFlowFileItem process(RA232YZFKey ccsOrderHstitem) throws Exception {
		YZFTransactionFlowFileItem yZFTransactionFlowFileItem = new YZFTransactionFlowFileItem();
		//如果当前参数是订单表的orderid
		if(ccsOrderHstitem.getType()!=null && ("O").equals(ccsOrderHstitem.getType())){
			//1、业务查询(ccsOrderHst、ccsAcct、qCcsCustomer三张表联表查询出所需要的数据)
			List<Tuple> acctTuple = new JPAQuery(em).from(ccsOrder,ccsAcct,qCcsCustomer)
			.where(ccsOrder.orderId.eq(ccsOrderHstitem.getOrderId())
					.and(ccsAcct.acctNbr.eq(ccsOrder.acctNbr))
					.and(qCcsCustomer.custId.eq(ccsAcct.custId)))
			.list(ccsOrder.acqId,ccsOrder.servicesn,ccsOrder.orderId,qCcsCustomer.internalCustomerId,ccsOrder.orderTime,ccsOrder.orderStatus,ccsOrder.contrNbr,ccsOrder.txnAmt,ccsOrder.loanUsage,ccsOrder.message);
			if(acctTuple.size() <=0 ){
				logger.info("订单[{}]对应的账户不存在", ccsOrderHstitem.getOrderId());
				return null;
			}
			//2、获得数据后，给返回类赋值
			Tuple tuple = acctTuple.get(0);
			//马上合作方ID
			yZFTransactionFlowFileItem.acqId = tuple.get(ccsOrder.acqId);
			//翼支付推送的接口CallID
			yZFTransactionFlowFileItem.servicesn = tuple.get(ccsOrder.servicesn);
			//马上账务系统交易授权号
			yZFTransactionFlowFileItem.orderId = tuple.get(ccsOrder.orderId).toString();
			//马上客户号码
			yZFTransactionFlowFileItem.internalCustomerId = tuple.get(qCcsCustomer.internalCustomerId);
			//申请日期
			yZFTransactionFlowFileItem.orderTime = tuple.get(ccsOrder.orderTime);
			//状态
			if(tuple.get(ccsOrder.orderStatus).equals(OrderStatus.C) || tuple.get(ccsOrder.orderStatus).equals(OrderStatus.P) || tuple.get(ccsOrder.orderStatus).equals(OrderStatus.Q)  || tuple.get(ccsOrder.orderStatus).equals(OrderStatus.W) || tuple.get(ccsOrder.orderStatus).equals(OrderStatus.G)){
				//处理中
				yZFTransactionFlowFileItem.orderStatus = "1";
			}else if(tuple.get(ccsOrder.orderStatus).equals(OrderStatus.S) || tuple.get(ccsOrder.orderStatus).equals(OrderStatus.D)){
				//成功
				yZFTransactionFlowFileItem.orderStatus = "0";
			}else if(tuple.get(ccsOrder.orderStatus).equals(OrderStatus.V) || tuple.get(ccsOrder.orderStatus).equals(OrderStatus.E) || tuple.get(ccsOrder.orderStatus).equals(OrderStatus.T) || tuple.get(ccsOrder.orderStatus).equals(OrderStatus.R)){
				//失败
				yZFTransactionFlowFileItem.orderStatus = "2";
			}
			
			//马上账务合同号
			yZFTransactionFlowFileItem.contrNbr = tuple.get(ccsOrder.contrNbr);
			//金额
			yZFTransactionFlowFileItem.txnAmt = tuple.get(ccsOrder.txnAmt);
			//交易方向
			if(LoanUsage.L.equals(tuple.get(ccsOrder.loanUsage)) || LoanUsage.A.equals(tuple.get(ccsOrder.loanUsage)) || LoanUsage.E.equals(tuple.get(ccsOrder.loanUsage))){
				//提现
				yZFTransactionFlowFileItem.loanUsage = "L";
			}else if(LoanUsage.M.equals(tuple.get(ccsOrder.loanUsage)) || LoanUsage.N.equals(tuple.get(ccsOrder.loanUsage)) || LoanUsage.O.equals(tuple.get(ccsOrder.loanUsage)) || LoanUsage.F.equals(tuple.get(ccsOrder.loanUsage))){
				//还款
				yZFTransactionFlowFileItem.loanUsage = "P";
			}
			//失败原因
			yZFTransactionFlowFileItem.message = tuple.get(ccsOrder.message);
			
			//3、返回结果类
			return yZFTransactionFlowFileItem;
		}
		//如果当前参数是订单历史表的orderid
		if(ccsOrderHstitem.getType()!=null && ("H").equals(ccsOrderHstitem.getType())){
			//1、业务查询(ccsOrderHst、ccsAcct、qCcsCustomer三张表联表查询出所需要的数据)
			List<Tuple> acctTuplehst = new JPAQuery(em).from(ccsOrderHst,ccsAcct,qCcsCustomer)
			.where(ccsOrderHst.orderId.eq(ccsOrderHstitem.getOrderId())
					.and(ccsAcct.acctNbr.eq(ccsOrderHst.acctNbr))
					.and(qCcsCustomer.custId.eq(ccsAcct.custId)))
			.list(ccsOrderHst.acqId,ccsOrderHst.servicesn,ccsOrderHst.orderId,qCcsCustomer.internalCustomerId,ccsOrderHst.orderTime,ccsOrderHst.orderStatus,ccsOrderHst.contrNbr,ccsOrderHst.txnAmt,ccsOrderHst.loanUsage,ccsOrderHst.message);
			
			//2、获得数据后，给返回类赋值
			Tuple tupleHST = acctTuplehst.get(0);
			//马上合作方ID
			yZFTransactionFlowFileItem.acqId = tupleHST.get(ccsOrderHst.acqId);
			//翼支付推送的接口CallID
			yZFTransactionFlowFileItem.servicesn = tupleHST.get(ccsOrderHst.servicesn);
			//马上账务系统交易授权号
			yZFTransactionFlowFileItem.orderId = tupleHST.get(ccsOrderHst.orderId).toString();
			//马上客户号码
			yZFTransactionFlowFileItem.internalCustomerId = tupleHST.get(qCcsCustomer.internalCustomerId);
			//申请日期
			yZFTransactionFlowFileItem.orderTime = tupleHST.get(ccsOrderHst.orderTime);
			//状态
			if(tupleHST.get(ccsOrderHst.orderStatus).equals(OrderStatus.C) || tupleHST.get(ccsOrderHst.orderStatus).equals(OrderStatus.P) || tupleHST.get(ccsOrderHst.orderStatus).equals(OrderStatus.Q)  || tupleHST.get(ccsOrderHst.orderStatus).equals(OrderStatus.W) || tupleHST.get(ccsOrderHst.orderStatus).equals(OrderStatus.G)){
				//处理中
				yZFTransactionFlowFileItem.orderStatus = "1";
			}else if(tupleHST.get(ccsOrderHst.orderStatus).equals(OrderStatus.S) || tupleHST.get(ccsOrderHst.orderStatus).equals(OrderStatus.D)){
				//成功
				yZFTransactionFlowFileItem.orderStatus = "0";
			}else if(tupleHST.get(ccsOrderHst.orderStatus).equals(OrderStatus.V) || tupleHST.get(ccsOrderHst.orderStatus).equals(OrderStatus.E) || tupleHST.get(ccsOrderHst.orderStatus).equals(OrderStatus.T) || tupleHST.get(ccsOrderHst.orderStatus).equals(OrderStatus.R)){
				//失败
				yZFTransactionFlowFileItem.orderStatus = "2";
			}
			
			//马上账务合同号
			yZFTransactionFlowFileItem.contrNbr = tupleHST.get(ccsOrderHst.contrNbr);
			//金额
			yZFTransactionFlowFileItem.txnAmt = tupleHST.get(ccsOrderHst.txnAmt);
			//交易方向
			if(LoanUsage.L.equals(tupleHST.get(ccsOrderHst.loanUsage)) || LoanUsage.A.equals(tupleHST.get(ccsOrderHst.loanUsage)) || LoanUsage.E.equals(tupleHST.get(ccsOrderHst.loanUsage))){
				//提现
				yZFTransactionFlowFileItem.loanUsage = "L";
			}else if(LoanUsage.M.equals(tupleHST.get(ccsOrderHst.loanUsage)) || LoanUsage.N.equals(tupleHST.get(ccsOrderHst.loanUsage)) || LoanUsage.O.equals(tupleHST.get(ccsOrderHst.loanUsage)) || LoanUsage.F.equals(tupleHST.get(ccsOrderHst.loanUsage))){
				//还款
				yZFTransactionFlowFileItem.loanUsage = "P";
			}
				yZFTransactionFlowFileItem.message = tupleHST.get(ccsOrderHst.message);
			//3、返回结果类
			return yZFTransactionFlowFileItem;
		}
		return yZFTransactionFlowFileItem;
	}
	
}