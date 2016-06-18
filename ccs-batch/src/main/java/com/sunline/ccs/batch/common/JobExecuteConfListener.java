package com.sunline.ccs.batch.common;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.sunline.acm.service.api.GlobalManagementService;
import com.sunline.ark.support.OrganizationContextHolder;

public class JobExecuteConfListener implements JobExecutionListener {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
    private GlobalManagementService globalManagementService;
	
	private final String systemType = "CCS";
	@Value("#{env['isAcmMock']?:false}")
	private Boolean isAcmMock;
	@Value("#{env.instanceName}")
	private String instanceName;
	@Override
	public void beforeJob(JobExecution jobExecution) {
		logger.info("JobExecuteConfListener.beforJob Executing...");
		if(isAcmMock){
			return ;
		}
		String currOrg = OrganizationContextHolder.getCurrentOrg();
		logger.info("设置机构号instanceName[{}]systemType[{}]isAcmMock[{}]currentOrg", instanceName, systemType, isAcmMock, currOrg);
		if(StringUtils.isNotBlank(currOrg)){
			logger.info("机构号已存在---不更新");
			return ;
		}
		//取机构号并设置上下文
		List<String> orgs = globalManagementService.getServeOrg(systemType, instanceName);
		for(String org :orgs){
			logger.debug("机构号=["+org+"]");
			if(!"root".equals(org)){
				OrganizationContextHolder.setCurrentOrg(org);
				logger.debug("找到机构号=["+org+"]");
				break;
			}
		}
		String currOrgNo = OrganizationContextHolder.getCurrentOrg();
		if(StringUtils.isBlank(currOrgNo)){
			OrganizationContextHolder.setCurrentOrg("000000000001");;
		}
	}

	@Override
	public void afterJob(JobExecution jobExecution) {
		logger.info("JobExecuteConfListener.afterJob Executing...");
	}

}
