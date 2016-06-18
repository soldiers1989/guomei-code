//package com.sunline.ccs.batch.front;
//
//import java.math.BigDecimal;
//import java.util.Date;
//import java.util.List;
//
//import javax.persistence.EntityManager;
//import javax.persistence.PersistenceContext;
//
//import junit.framework.Assert;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//import org.springframework.transaction.annotation.Transactional;
//
//import com.mysema.query.jpa.impl.JPAQuery;
//import com.sunline.acm.service.sdk.GlobalManagementServiceMock;
//import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
//import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
//import com.sunline.ccs.infrastructure.shared.model.CcsOrder;
//import com.sunline.ccs.infrastructure.shared.model.CcsRepaySchedule;
//import com.sunline.ccs.infrastructure.shared.model.QCcsOrder;
//import com.sunline.ccs.param.def.ProductCredit;
//import com.sunline.pcm.param.def.FinancialOrg;
//import com.sunline.pcm.param.def.Split;
//import com.sunline.pcm.service.sdk.ParameterServiceMock;
//
//
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration("/test-context-front.xml")
//@Transactional
//public class P2000OverduePaymentTest {
//	
//	private static final Logger logger = LoggerFactory.getLogger(P2000OverduePaymentTest.class);
//	
//	@Autowired
//	private P2000OverduePayment testProcess;
//	
//	@Autowired
//	private ParameterServiceMock parameterMock;
//	
//	@Autowired
//	private GlobalManagementServiceMock managementMock;
//	
//	@PersistenceContext
//	private EntityManager em;
//	
//	ProductCredit pc = null;
//	FinancialOrg finanicalOrg = null;
//	Split split = null;
//	
//	@Before
//	public void setup(){
//		// 产品参数
//		pc = FrontBatchParameter.genProductCredit();
//		parameterMock.putParameter(pc.productCd, pc);
//		// 金融机构参数
//		finanicalOrg = FrontBatchParameter.genFinancialOrg();
//		parameterMock.putParameter(finanicalOrg.financialOrgNO, finanicalOrg);
//		// 拆分参数
//		split = FrontBatchParameter.genSplit();
//		parameterMock.putParameter(split.splitTableId, split);
//		// 业务日期
//		managementMock.setupBusinessDate(new Date());
//	}
//	
//	@Test
//	public void testCase1() throws Exception{
//		logger.info("开始测试案例1,无正常到期&&逾期未满80天----------------------------------");
//		
//		SFrontInfo info = new SFrontInfo();
//		CcsAcct acct = FrontBatchData.genAcct();
//		info.setAcct(acct);
//		CcsLoan loan = FrontBatchData.genCcsLoan1();
//		info.setLoan(loan);
//		
//		logger.info("数据准备完毕-------------------------------");
//		
//		testProcess.process(info);
//		
//		logger.info("数据校验----------------------------------");
//		
//		QCcsOrder q = QCcsOrder.ccsOrder;
//		List<CcsOrder> orders = new JPAQuery(em).from(q).orderBy(q.orderId.asc()).list(q);
//		if(orders.size()==0){
//			Assert.fail("未生成订单!");
//		}
//		CcsOrder origOrder = null;
//		BigDecimal splitAmt = BigDecimal.ZERO;
//		for(CcsOrder order : orders){
//			if(order.getOriOrderId()==null){
//				origOrder = order;
//			}else{
//				splitAmt = splitAmt.add(order.getTxnAmt());
//			}
//		}
//		orders.remove(origOrder);
//		Assert.assertNotNull("原订单不存在", origOrder);
//		logger.info("原订单------------");
//		logger.info("订单编号:"+origOrder.getOrderId());
//		logger.info("订单金额:"+origOrder.getTxnAmt());
//		Assert.assertEquals("原订单交易金额不等于应还金额", acct.getCurrBal().setScale(2, BigDecimal.ROUND_UP), origOrder.getTxnAmt().setScale(2, BigDecimal.ROUND_UP));
//		Assert.assertEquals("拆分后订单交易金额之和不等于原订单交易金额", origOrder.getTxnAmt().setScale(2, BigDecimal.ROUND_UP), splitAmt.setScale(2, BigDecimal.ROUND_UP));
//		for(int i=0;i<orders.size();i++){
//			logger.info("拆分后订单"+(i+1)+"------------");
//			logger.info("订单编号:"+orders.get(i).getOrderId());
//			logger.info("订单金额:"+orders.get(i).getTxnAmt());
//		}
//	}
//	
//	@Test
//	public void testCase2() throws Exception{
//		logger.info("开始测试案例2,还款日&&逾期未满80天----------------------------------");
//		
//		SFrontInfo info = new SFrontInfo();
//		CcsAcct acct = FrontBatchData.genAcct();
//		info.setAcct(acct);
//		CcsLoan loan = FrontBatchData.genCcsLoan1();
//		info.setLoan(loan);
//		CcsRepaySchedule schedule = FrontBatchData.genSchedule();
//		info.setSchedule(schedule);
//		
//		logger.info("数据准备完毕-------------------------------");
//		
//		testProcess.process(info);
//		
//		logger.info("数据校验----------------------------------");
//		
//		QCcsOrder q = QCcsOrder.ccsOrder;
//		List<CcsOrder> orders = new JPAQuery(em).from(q).orderBy(q.orderId.asc()).list(q);
//		if(orders.size()==0){
//			Assert.fail("未生成订单!");
//		}
//		CcsOrder origOrder = null;
//		BigDecimal splitAmt = BigDecimal.ZERO;
//		for(CcsOrder order : orders){
//			if(order.getOriOrderId()==null){
//				origOrder = order;
//			}else{
//				splitAmt = splitAmt.add(order.getTxnAmt());
//			}
//		}
//		orders.remove(origOrder);
//		Assert.assertNotNull("原订单不存在", origOrder);
//		logger.info("原订单------------");
//		logger.info("订单编号:"+origOrder.getOrderId());
//		logger.info("订单金额:"+origOrder.getTxnAmt());
//		BigDecimal scheduleAmt = schedule.getLoanTermPrin().add(schedule.getLoanTermInt()).add(schedule.getLoanTermFee()).add(schedule.getLoanStampdutyAmt()).add(schedule.getLoanInsuranceAmt()).add(schedule.getLoanLifeInsuAmt());
//		Assert.assertEquals("原订单交易金额不等于应还金额", acct.getCurrBal().add(scheduleAmt).setScale(2, BigDecimal.ROUND_UP), origOrder.getTxnAmt().setScale(2, BigDecimal.ROUND_UP));
//		Assert.assertEquals("拆分后订单交易金额之和不等于原订单交易金额", origOrder.getTxnAmt().setScale(2, BigDecimal.ROUND_UP), splitAmt.setScale(2, BigDecimal.ROUND_UP));
//		for(int i=0;i<orders.size();i++){
//			logger.info("拆分后订单"+(i+1)+"------------");
//			logger.info("订单编号:"+orders.get(i).getOrderId());
//			logger.info("订单金额:"+orders.get(i).getTxnAmt());
//		}
//	}
//	
//	@Test
//	public void testCase3() throws Exception{
//		logger.info("开始测试案例3,逾期满80天----------------------------------");
//		
//		SFrontInfo info = new SFrontInfo();
//		CcsAcct acct = FrontBatchData.genAcct();
//		info.setAcct(acct);
//		CcsLoan loan = FrontBatchData.genCcsLoan2();
//		info.setLoan(loan);
//		
//		logger.info("数据准备完毕-------------------------------");
//		
//		testProcess.process(info);
//		
//		logger.info("数据校验----------------------------------");
//		
//		QCcsOrder q = QCcsOrder.ccsOrder;
//		List<CcsOrder> orders = new JPAQuery(em).from(q).orderBy(q.orderId.asc()).list(q);
//		Assert.assertEquals("不应生成订单!", 0, orders.size());
//	}
//	
//}
