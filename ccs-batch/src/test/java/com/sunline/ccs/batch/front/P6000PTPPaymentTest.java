package com.sunline.ccs.batch.front;

import java.util.Random;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.sunline.ccs.batch.cc6000.BatchParameter;
import com.sunline.ccs.batch.sdk.BatchDateUtil;
import com.sunline.ccs.batch.utils.JUnit4ClassRunner;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcct;
import com.sunline.ccs.infrastructure.server.repos.RCcsCustomer;
import com.sunline.ccs.infrastructure.server.repos.RCcsLoan;
import com.sunline.ccs.infrastructure.server.repos.RCcsOrder;
import com.sunline.ccs.infrastructure.server.repos.RCcsOutsideDdTxn;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.pcm.param.def.FinancialOrg;
import com.sunline.pcm.service.sdk.ParameterServiceMock;

@RunWith(JUnit4ClassRunner.class)
@ContextConfiguration("/front/test-context-front.xml")
public class P6000PTPPaymentTest {
	
	private static final Logger logger = LoggerFactory.getLogger(P6000PTPPaymentTest.class);
	
	@Autowired
	private P6000PTPayment processor;
	@Autowired
	private RCcsAcct rCcsAcct;
	@Autowired
	private RCcsCustomer rCcsCust;
	@Autowired
	private RCcsLoan rCcsLoan;
	@Autowired
	private RCcsOrder r;
	@Autowired
	private RCcsOutsideDdTxn rCcsOutsideDdTxn;
	@Autowired
	private ParameterServiceMock parameterMock;
	FinancialOrg finanicalOrg = null;
	@PersistenceContext
	private EntityManager em;
	@Autowired
	BatchDateUtil batchDateUtil;
	@Autowired 
	JobLauncherTestUtils jobLauncherTestUtils;
	@Autowired
	private BatchParameter batchParameter;
	
	@Before
	public void init() throws Exception{
		batchDateUtil.setBatchDate("2018-08-15");
		ProductCredit pc=batchParameter.genMulProductCredit();
//		parameterMock.putParameter(pc.productCd, pc);
//		AccountAttribute aa=BatchParameter.genAccountAttribute();
//		parameterMock.putParameter(pc.accountAttributeId+"", aa);
		LoanPlan loanPlan = BatchParameter.genLoanPlan();
		parameterMock.putParameter(loanPlan.loanCode, loanPlan);
		// 金融机构参数
		finanicalOrg = FrontBatchParameter.genFinancialOrg();
		parameterMock.putParameter(finanicalOrg.financialOrgNO, finanicalOrg);
//		parameterMock.putParameter(FrontBatchParameter.genProductCredit().productCd, FrontBatchParameter.genProductCredit()); 
	}
	
	@Test
	public void testCase() throws Exception{
		logger.info("开始测试----------------------------------");
		
//		SPtpBatchCutFile info = new SPtpBatchCutFile();
//		CcsAcct acct = FrontBatchData.genAcct();
//		CcsLoan loan = FrontBatchData.genLoan();
//
//		CcsCustomer cust = FrontBatchData.genCustomer();
//		CcsOutsideDdTxn ccsOutsideDdTxn = FrontBatchData.genCcsOutsideDdTxn();
//		rCcsLoan.saveAndFlush(loan);
//		rCcsAcct.saveAndFlush(acct);
//		rCcsCust.saveAndFlush(cust);
//		rCcsOutsideDdTxn.saveAndFlush(ccsOutsideDdTxn);
//		logger.info("数据准备完毕-------------------------------");
//		info.contractCode="11111";
//		info.customerCode="";
//		info.loanInfoCode="1111111111111111111111";
//		info.paybackAmount=new BigDecimal(15000.01);
//		
////		SFrontInfo jobExecution = processor.process(info);
//		
//		logger.info("数据校验----------------------------------");
//		
//		CcsOrder orders = em.find(CcsOrder.class, 1L);
//		List<CcsOutsideDdTxn> txns = rCcsOutsideDdTxn.findAll();
//		Assert.assertEquals(1, txns.size());
		
		JobParametersBuilder jobParamBuilder = new JobParametersBuilder();
		jobParamBuilder.addString("testId", String.valueOf(new Random().nextInt()));
		jobParamBuilder.addDate("batchDate", batchDateUtil.getBatchDate());
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParamBuilder.toJobParameters());
		Assert.assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());
	}
}