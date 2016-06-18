package com.sunline.ccs.batch.front;

import java.math.BigDecimal;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ccs.infrastructure.shared.model.CcsOrder;
import com.sunline.ccs.infrastructure.shared.model.QCcsOrder;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanUsage;
import com.sunline.ppy.dictionary.enums.OrderStatus;

public class GatherClaimOrderTasklet implements Tasklet {

	private static final Logger logger = LoggerFactory.getLogger(GatherClaimOrderTasklet.class);
	
	@PersistenceContext
	private EntityManager em;
	
	@Autowired
	private BatchStatusFacility batchStatusFacility;
	
	QCcsOrder o = QCcsOrder.ccsOrder;

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		
		if(logger.isDebugEnabled()){
			logger.debug("汇总理赔订单");
		}
		
		// 获取所有的acqId
		List<String> acqIds = new JPAQuery(em).from(o).distinct().list(o.acqId);
		// 为每一个acqId汇总出一条订单
		for(String acqId : acqIds){
			gatherOrder(acqId);
		}
		
		return RepeatStatus.FINISHED;
	}
	
	/**
	 * 根据acqId生成汇总的理赔订单
	 * @param acqId
	 */
	private void gatherOrder(String acqId){
		// 获取所有理赔订单
		List<CcsOrder> claimOrders = new JPAQuery(em).from(o)
				.where(o.acqId.eq(acqId)
						.and(o.businessDate.eq(batchStatusFacility.getSystemStatus().getBusinessDate()))
						.and(o.onlineFlag.eq(Indicator.N))
						.and(o.loanUsage.eq(LoanUsage.C))
						.and(o.orderStatus.eq(OrderStatus.W)))
						.orderBy(o.orderId.asc())
						.list(o);
		// 若该金融机构当天无理赔订单，则不进行汇总
		if(claimOrders.size()==0)
			return;
		if(logger.isDebugEnabled()){
			logger.debug("开始汇总理赔订单,acqId:[" + acqId + "]");
		}
		// 生成汇总订单
		BigDecimal txnAmt = BigDecimal.ZERO;
		CcsOrder gatheredOrder = genGatheredOrder(claimOrders.get(0));
		em.persist(gatheredOrder);
		// 汇总金额 设置origOrderId
		for(CcsOrder claimOrder : claimOrders){
			txnAmt = txnAmt.add(claimOrder.getTxnAmt());
			claimOrder.setOriOrderId(gatheredOrder.getOrderId());
		}
		gatheredOrder.setTxnAmt(txnAmt);
		
		if(logger.isDebugEnabled()){
			logger.debug("汇总订单,orderId:[" + gatheredOrder.getOrderId() 
							+ ",txnAmt:[" + gatheredOrder.getTxnAmt()
							+ "]");
		}
	}
	
	/**
	 * 用理赔订单生成汇总订单，清除某些字段
	 * @param order
	 * @return
	 */
	private CcsOrder genGatheredOrder(CcsOrder order){
		CcsOrder gatheredOrder = new CcsOrder();
		
		gatheredOrder.updateFromMap(order.convertToMap());
		gatheredOrder.setOrderId(null);

		gatheredOrder.setGuarantyId(null);
		gatheredOrder.setDueBillNo(null);
		gatheredOrder.setAcctNbr(null);
		gatheredOrder.setAcctType(null);
		
		return gatheredOrder;
	}
	
}
