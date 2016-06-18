package com.sunline.ccs.batch.cca900;

import java.util.Random;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.sunline.acm.service.sdk.GlobalManagementServiceMock;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.batch.cc6000.BatchParameter;
import com.sunline.ccs.batch.cca000.SA000ParamDataUtil;
import com.sunline.ccs.batch.sdk.BatchDateUtil;
import com.sunline.ccs.batch.utils.JUnit4ClassRunner;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.pcm.service.sdk.ParameterServiceMock;
@RunWith(JUnit4ClassRunner.class)
@ContextConfiguration("/test-context-cca900.xml")
public class Cca900SmsTest {
	@Autowired 
	JobLauncherTestUtils jobLauncherTestUtils;
	@Autowired
	BatchDateUtil batchDateUtil;
	@Autowired
	SA000ParamDataUtil paramUtil;
	@Autowired
	private GlobalManagementServiceMock managementMock;
//	@Resource(name="parameterServiceMock")
	@Autowired
	private ParameterServiceMock parameterMock;
	@Autowired
	private BatchParameter batchParameter;
	QCcsLoan qCcsLoan = QCcsLoan.ccsLoan;

	@Before
	public void init() throws Exception{
		batchDateUtil.setBatchDate("2018-08-15");
		OrganizationContextHolder.setCurrentOrg("000000000001");
		ProductCredit pc=batchParameter.genMulProductCredit();
		batchParameter.genMulLoanPlan();
//		parameterMock.putParameter(pc.productCd, pc);
//		AccountAttribute aa=BatchParameter.genAccountAttribute();
//		parameterMock.putParameter(pc.accountAttributeId+"", aa);
//		LoanPlan loanPlan = BatchParameter.genLoanPlan();
//		parameterMock.putParameter(loanPlan.loanCode, loanPlan);
		
	}
	
	@Test
	public void doTest() throws Exception{
		JobParametersBuilder jobParamBuilder = new JobParametersBuilder();
		jobParamBuilder.addString("testId", String.valueOf(new Random().nextInt()));
		jobParamBuilder.addDate("batchDate", batchDateUtil.getBatchDate());
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParamBuilder.toJobParameters());
		Assert.assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());
	}
	
	

}
