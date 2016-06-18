//package com.sunline.ccs.batch.sdk;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import com.sunline.acm.service.sdk.GlobalManagementServiceMock;
//import com.sunline.ark.support.OrganizationContextHolder;
//
//public class CcsManagementServiceMock extends GlobalManagementServiceMock {
//
//	@Override
//	public Map<String, Map<String, String>> getInstanceRoute() {
//		Map<String, Map<String, String>> result = new HashMap<String, Map<String,String>>();
//		
//		String org = OrganizationContextHolder.getCurrentOrg();
//		
//		Map<String, String> route = new HashMap<String, String>();
//		route.put("ccs", "myms01");
//		route.put("nfs", "myms02");
//		
//		result.put(org, route);
//		
//		return result;
//	}
//	
//}