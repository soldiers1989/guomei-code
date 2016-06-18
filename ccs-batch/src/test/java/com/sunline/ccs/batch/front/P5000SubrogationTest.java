package com.sunline.ccs.batch.front;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.GlobalManagementServiceMock;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsOrder;
import com.sunline.ccs.infrastructure.shared.model.QCcsOrder;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.pcm.param.def.FinancialOrg;
import com.sunline.pcm.param.def.Split;
import com.sunline.pcm.service.sdk.ParameterServiceMock;
import com.sunline.ppy.dictionary.enums.OrderStatus;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/test-context-front.xml")
@Transactional
public class P5000SubrogationTest {
	
	private static final Logger logger = LoggerFactory.getLogger(P5000SubrogationTest.class);
	
	@Autowired
	private P5000Subrogation testProcessor;
	
	@Autowired
	private ParameterServiceMock parameterMock;
	
	@Autowired
	private GlobalManagementServiceMock managementMock;
	
	@PersistenceContext
	private EntityManager em;
	
	ProductCredit pc = null;
	FinancialOrg finanicalOrg = null;
	Split split = null;
	
	@Before
	public void setup(){
		// 产品参数
		pc = FrontBatchParameter.genProductCredit();
		parameterMock.putParameter(pc.productCd, pc);
		// 金融机构参数
		finanicalOrg = FrontBatchParameter.genFinancialOrg();
		parameterMock.putParameter(finanicalOrg.financialOrgNO, finanicalOrg);
		// 拆分参数
		split = FrontBatchParameter.genSplit();
		parameterMock.putParameter(split.splitTableId, split);
		// 业务日期
		managementMock.setupBusinessDate(new Date());
	}
	
	@Test
	public void testCase() throws Exception{
		logger.info("开始测试----------------------------------");
		
		SFrontInfo info = new SFrontInfo();
		CcsAcct acct = FrontBatchData.genAcct();
		info.setAcct(acct);
		CcsLoan loan = FrontBatchData.genCcsLoan1();
		info.setLoan(loan);
		CcsOrder order = FrontBatchData.genSubrogationOrder();
		em.persist(order);
		info.setrOrder(order);
		
		logger.info("数据准备完毕-------------------------------");
		
		testProcessor.process(info);
		
		logger.info("数据校验----------------------------------");
		
		QCcsOrder q = QCcsOrder.ccsOrder;
		List<CcsOrder> orders = new JPAQuery(em).from(q).orderBy(q.orderId.asc()).list(q);
		if(orders.size()==0){
			Assert.fail("未生成订单!");
		}
		CcsOrder olOrder = null;
		CcsOrder origOrder = null;
		BigDecimal splitAmt = BigDecimal.ZERO;
		for(CcsOrder subOrder : orders){
			if(subOrder.getOriOrderId()==null){
				olOrder = subOrder;
			}else if(subOrder.getOriOrderId()==1){
				origOrder = subOrder;
			}else{
				splitAmt = splitAmt.add(subOrder.getTxnAmt());
			}
		}
		orders.remove(olOrder);
		orders.remove(origOrder);
		Assert.assertNotNull("原订单不存在", origOrder);
		Assert.assertEquals("原订单未设置为失效", OrderStatus.V, olOrder.getOrderStatus());
		logger.info("原订单------------");
		logger.info("订单编号:"+origOrder.getOrderId());
		logger.info("订单金额:"+origOrder.getTxnAmt());
		Assert.assertEquals("拆分后订单交易金额之和不等于原订单交易金额", origOrder.getTxnAmt().setScale(2, BigDecimal.ROUND_UP), splitAmt.setScale(2, BigDecimal.ROUND_UP));
		for(int i=0;i<orders.size();i++){
			logger.info("拆分后订单"+(i+1)+"------------");
			logger.info("订单编号:"+orders.get(i).getOrderId());
			logger.info("订单金额:"+orders.get(i).getTxnAmt());
		}
	}
	
}