package com.sunline.ccs.batch.cca000;

import java.math.BigDecimal;
import java.util.Date;

import junit.framework.Assert;

import org.apache.commons.lang.time.DateUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.sunline.acm.service.sdk.GlobalManagementServiceMock;
import com.sunline.ccs.batch.front.FrontBatchData;
import com.sunline.ccs.batch.front.FrontBatchParameter;
import com.sunline.ccs.infrastructure.server.repos.RCcsOrder;
import com.sunline.ccs.infrastructure.server.repos.RCcsOrderHst;
import com.sunline.ccs.infrastructure.server.repos.RCcsRepayHst;
import com.sunline.ccs.infrastructure.shared.model.CcsOrder;
import com.sunline.ccs.infrastructure.shared.model.CcsOrderHst;
import com.sunline.ccs.infrastructure.shared.model.CcsRepayHst;
import com.sunline.pcm.param.def.FinancialOrg;
import com.sunline.pcm.service.sdk.ParameterServiceMock;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/cca000/test-context-cca.xml")
public class SA100Test {
	
	private static final Logger logger = LoggerFactory.getLogger(SA100Test.class);
	
	@Autowired
	private JobLauncherTestUtils jobUtils;
	
	@Autowired
	private GlobalManagementServiceMock managementMock;
	
	@Autowired
	private ParameterServiceMock parameterMock;
	
	@Autowired
	private RCcsOrder r;
	
	@Autowired
	private RCcsRepayHst p;
	
	@Autowired
	private RCcsOrderHst d;
	
	FinancialOrg financialOrg = null;
	
	@Before
	public void setup(){
		financialOrg = FrontBatchParameter.genFinancialOrg();
		parameterMock.putParameter(financialOrg.financialOrgNO, financialOrg);
		// 批量日期
		managementMock.setupBatchDate(new Date(), DateUtils.addDays(new Date(), -1));
		managementMock.setupBusinessDate(DateUtils.addDays(new Date(), 1));
		managementMock.getInstanceRoute();
	}
	
	@Test
	public void testCase() throws Exception{
		logger.info("开始测试----------------------------------");
		// 结算保费
		CcsRepayHst repayHst1 = FrontBatchData.genCcsRepayHst1();
		// 结算提前结清手续费
		CcsRepayHst repayHst2 = FrontBatchData.genCcsRepayHst2();
		// 结算追偿费
		CcsOrderHst orderHst = FrontBatchData.genCcsOrderHst();
		FrontBatchData.setNotNullField(CcsRepayHst.class, repayHst1);
		FrontBatchData.setNotNullField(CcsRepayHst.class, repayHst2);
		FrontBatchData.setNotNullField(CcsOrderHst.class, orderHst);
		p.save(repayHst1);
		p.save(repayHst2);
		d.save(orderHst);
		
		logger.info("数据准备完毕-------------------------------");
		
		JobExecution jobExecution = jobUtils.launchStep("cca100Settle");
		
		logger.info("数据校验----------------------------------");
		
		Assert.assertEquals("跑批失败", ExitStatus.COMPLETED, jobExecution.getExitStatus());
		CcsOrder order = r.findAll().get(0);
		BigDecimal txnAmt = repayHst1.getRepayAmt().add(orderHst.getTxnAmt()).add(repayHst2.getRepayAmt().multiply(financialOrg.adFeeScale));
		Assert.assertEquals("金额不符", txnAmt.setScale(2, BigDecimal.ROUND_UP), order.getTxnAmt().setScale(2, BigDecimal.ROUND_UP));
	}
	
}