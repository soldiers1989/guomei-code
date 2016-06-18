package com.sunline.ccs.batch.cca300;

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
@ContextConfiguration("/cca300/test-context-cca300.xml")
public class SA300JobTest{
	@Autowired 
	private JobLauncherTestUtils jobLauncherTestUtils;
	@Autowired
	private BatchDateUtil batchDateUtil;
	@Autowired
	private SA000ParamDataUtil prepare;
	private JobParametersBuilder jobParamBuilder = new JobParametersBuilder();
	
	private final static String BATCH_DATE_STR = "2016-05-05";
	private final static String TEST_ID = "1000161";
	
	@Before
	public void init() throws Exception{
		
		batchDateUtil.setBatchDate(BATCH_DATE_STR);
		jobParamBuilder.addString("testId", TEST_ID);
		jobParamBuilder.addDate("batchDate", batchDateUtil.getBatchDate());
		prepare.loadParamFromDir();
//		prepare.prepareData(jobLauncherTestUtils.getJob().getName());
	}
	
	@Test
	public void doTest() throws Exception{
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParamBuilder.toJobParameters());
		Assert.assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());
	}

}
