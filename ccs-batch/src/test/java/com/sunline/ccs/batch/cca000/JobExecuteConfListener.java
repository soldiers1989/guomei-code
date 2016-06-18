package com.sunline.ccs.batch.cca000;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

import com.sunline.ark.support.OrganizationContextHolder;

public class JobExecuteConfListener implements JobExecutionListener {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	@Override
	public void beforeJob(JobExecution jobExecution) {
		logger.info("JobExecuteConfListener.beforJob执行");
		OrganizationContextHolder.setCurrentOrg("000000000001");
	}

	@Override
	public void afterJob(JobExecution jobExecution) {
		logger.info("JobExecuteConfListener.afterJob执行");
	}

}
