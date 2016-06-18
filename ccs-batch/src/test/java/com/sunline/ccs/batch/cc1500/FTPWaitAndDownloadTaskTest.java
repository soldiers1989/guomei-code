package com.sunline.ccs.batch.cc1500;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.sunline.ccs.batch.utils.JUnit4ClassRunner;

@RunWith(JUnit4ClassRunner.class)
@ContextConfiguration("/test-ftp.xml")
public class FTPWaitAndDownloadTaskTest {
	@Autowired 
	private JobLauncherTestUtils jobLauncherTestUtils;
	@Before
	public void init() {

	}

	@Test
	public void test() {
		JobExecution jobExecution = jobLauncherTestUtils.launchStep("resp-file-wait");
		Assert.assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());
		JobExecution jobExecution2 = jobLauncherTestUtils.launchStep("resp-file-down");
		Assert.assertEquals(ExitStatus.COMPLETED, jobExecution2.getExitStatus());
	}
}
