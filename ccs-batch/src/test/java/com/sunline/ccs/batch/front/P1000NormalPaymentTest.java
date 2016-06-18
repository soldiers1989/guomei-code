package com.sunline.ccs.batch.front;

import java.math.BigDecimal;
import java.util.List;

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

import com.sunline.ccs.infrastructure.server.repos.RCcsOrder;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsOrder;
import com.sunline.ccs.infrastructure.shared.model.CcsRepaySchedule;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.pcm.param.def.FinancialOrg;
import com.sunline.pcm.service.sdk.ParameterServiceMock;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/test-context-front.xml")
@Transactional
public class P1000NormalPaymentTest {
	
	private static final Logger logger = LoggerFactory.getLogger(P1000NormalPaymentTest.class);
	
	@Autowired
	private P1000NormalPayment testProcessor;
	
	@Autowired
	private RCcsOrder r;
	
	@Autowired
	private ParameterServiceMock parameterMock;
	
	ProductCredit pc = null;
	FinancialOrg finanicalOrg = null;
	
	@Before
	public void setup(){
		// 产品参数
		pc = FrontBatchParameter.genProductCredit();
		parameterMock.putParameter(pc.productCd, pc);
		// 金融机构参数
		finanicalOrg = FrontBatchParameter.genFinancialOrg();
		parameterMock.putParameter(finanicalOrg.financialOrgNO, finanicalOrg);
	}
	
	@Test
	public void testCase() throws Exception{
		logger.info("开始测试----------------------------------");
		
		SFrontInfo info = new SFrontInfo();
		CcsAcct acct = FrontBatchData.genAcct();
		info.setAcct(acct);
		CcsRepaySchedule schedule = FrontBatchData.genSchedule();
		info.setSchedule(schedule);
		
		logger.info("数据准备完毕-------------------------------");
		
//		testProcessor.process(info);
		
		logger.info("数据校验----------------------------------");
		
		List<CcsOrder> orders = r.findAll();
		Assert.assertEquals(1, orders.size());
		CcsOrder order = orders.get(0);
		BigDecimal respectAmt = schedule.getLoanTermPrin().add(schedule.getLoanTermInt()).add(schedule.getLoanTermFee()).add(schedule.getLoanStampdutyAmt()).add(schedule.getLoanInsuranceAmt()).add(schedule.getLoanLifeInsuAmt());
		Assert.assertEquals("生成的订单金额与期供中应还金额不符", respectAmt.setScale(2, BigDecimal.ROUND_UP), order.getTxnAmt().setScale(2, BigDecimal.ROUND_UP));
		Assert.assertEquals("生成的订单渠道号与参数不符", finanicalOrg.acqAcceptorId, order.getAcqId());
		
	}
	
}
