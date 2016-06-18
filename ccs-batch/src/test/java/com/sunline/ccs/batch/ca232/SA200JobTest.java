package com.sunline.ccs.batch.ca232;

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

import com.sunline.ccs.batch.cca000.SA000ParamDataUtil;
import com.sunline.ccs.batch.sdk.BatchDateUtil;
import com.sunline.ccs.batch.utils.JUnit4ClassRunner;
@RunWith(JUnit4ClassRunner.class)
@ContextConfiguration("/test-context-yzf001.xml")
public class SA200JobTest {
	@Autowired 
	JobLauncherTestUtils jobLauncherTestUtils;
	@Autowired
	BatchDateUtil batchDateUtil;
	@Autowired
	SA000ParamDataUtil paramUtil;
	
	@Before
	public void init(){
		batchDateUtil.setBatchDate("2019-05-02");
//		paramUtil.prepareParam();
	}
	
	@Test
	public void doTest() throws Exception{
		JobParametersBuilder jobParamBuilder = new JobParametersBuilder();
		jobParamBuilder.addString("testId", "1241241219");
		jobParamBuilder.addDate("batchDate", batchDateUtil.getBatchDate());
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParamBuilder.toJobParameters());
		Assert.assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());
	}

}
