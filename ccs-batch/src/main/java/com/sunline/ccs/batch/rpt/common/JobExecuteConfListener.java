package com.sunline.ccs.batch.rpt.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;

public class JobExecuteConfListener implements JobExecutionListener {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private RptBatchUtil rptBatchUtil;
	
	@Override
	public void beforeJob(JobExecution jobExecution) {
		logger.info("JobExecuteConfListener.beforJob Executing...");
//		OrganizationContextHolder.setCurrentOrg("000000000001");
		rptBatchUtil.setCurrOrgNoToContext();
	}

	@Override
	public void afterJob(JobExecution jobExecution) {
		logger.info("JobExecuteConfListener.afterJob Executing...");
	}

}
