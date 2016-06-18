package com.sunline.ccs.batch.front;

import java.util.Date;

import junit.framework.Assert;

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

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/test-context-front.xml")
public class S9100TransferFileTest {
	
	private static final Logger logger = LoggerFactory.getLogger(S9100TransferFileTest.class);
	
	@Autowired
	private JobLauncherTestUtils jobUtils;
	
	@Autowired
	private GlobalManagementServiceMock managementMock;
	
	@Before
	public void setup(){
		// 业务日期
		managementMock.setupBusinessDate(new Date());
	}
	
	@Test
	public void testCase(){
		logger.info("开始测试----------------------------------");
		
		logger.info("数据准备完毕-------------------------------");
		
		JobExecution jobExecution = jobUtils.launchStep("s9100-transfer-file");
		
		logger.info("数据校验----------------------------------");
		Assert.assertEquals("跑批失败", ExitStatus.COMPLETED, jobExecution.getExitStatus());
	}
	
}