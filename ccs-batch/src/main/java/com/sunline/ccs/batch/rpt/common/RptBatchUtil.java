package com.sunline.ccs.batch.rpt.common;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.sunline.acm.service.api.GlobalManagementService;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ppy.dictionary.exception.ProcessException;

@Service
public class RptBatchUtil {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
    private GlobalManagementService globalManagementService;
	
	private final String systemType = "CCS";
	
	@Value("#{env.instanceName}")
	private String instanceName;
	
	@Value("#{env['isAcmMock']?:false}")
	private Boolean isAcmMock;
	
	public RptBatchUtil() {
		super();
	}
	/**
	 * 设置机构号到机构上下文
	 */
	public void setCurrOrgNoToContext(String org){
		OrganizationContextHolder.setCurrentOrg(org);
	}
	/**
	 * 找到当前机构机构号并设置<p>
	 * 若机构上下文已设机构号，默认不更新<p>
	 */
	public void setCurrOrgNoToContext(){
		setCurrOrgNoToContext(false);
	}
	/**
	 * 找到当前机构机构号并设置
	 * @param isUpdate 若机构上下文已设机构号是否更新
	 */
	public void setCurrOrgNoToContext(Boolean isUpdate){
		String currOrg = OrganizationContextHolder.getCurrentOrg();
		logger.info("设置机构号instanceName[{}]systemType[{}]isAcmMock[{}]currentOrg", instanceName, systemType, isAcmMock, currOrg);
		if(isAcmMock){
			return ;
		}
		if(currOrg != null && !isUpdate){
			logger.info("机构号已存在---不更新");
			return ;
		}
		//取机构号并设置上下文
		List<String> orgs = globalManagementService.getServeOrg(systemType, instanceName);
		if(orgs == null || orgs.size() == 0){
			throw new IllegalArgumentException("未取得机构号");
		}
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
			throw new ProcessException("无法获取机构号，systemType["+systemType+"]instanceName["+instanceName+"]");
		}
	}

	public void setIsAcmMock(Boolean isAcmMock) {
		this.isAcmMock = isAcmMock;
	}
	
	
}
