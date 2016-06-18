package com.sunline.ccs.batch.cca100;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.sunline.acm.service.api.GlobalManagementService;
import com.sunline.ark.batch.KeyBasedReader;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.pcm.param.def.FinancialOrg;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;

/**
 * 读取机构下所有金融机构
 * @author zhangqiang
 *
 */
public class RA100Settle extends KeyBasedReader<String, FinancialOrg> {
	
	private static final Logger logger = LoggerFactory.getLogger(RA100Settle.class);
	
	@Autowired
	private UnifiedParameterFacility unifiedParameterFacility;
	
    private final String systemType = "CCS";

    @Value("#{env.instanceName}")
    private String instanceName;
    @Autowired
    private GlobalManagementService globalManagementService;
	
	// 注入机构号
//	private String org = "000000000001";
	
	@Override
	protected List<String> loadKeys() {
		String org = null;
		List<String> orgs = globalManagementService.getServeOrg(systemType, instanceName);
		for(String oOrg :orgs){
			if(!"root".equals(org)){
				org = oOrg;
				break;
			}
		}
		
		if(logger.isDebugEnabled())
			logger.debug("结算机构号,Org:[{}]", org);
		
		OrganizationContextHolder.setCurrentOrg(org);
		Map<String, FinancialOrg> finanicalOrgNos = unifiedParameterFacility.retrieveParameterObject(FinancialOrg.class);
		return new ArrayList<String>(finanicalOrgNos.keySet());
	}

	@Override
	protected FinancialOrg loadItemByKey(String key) {
		return unifiedParameterFacility.loadParameter(key, FinancialOrg.class);
	}


	
}
