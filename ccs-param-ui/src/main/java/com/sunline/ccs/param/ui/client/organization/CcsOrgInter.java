package com.sunline.ccs.param.ui.client.organization;

import java.util.LinkedHashMap;
import java.util.Map;

import com.sunline.ccs.param.def.Organization;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("rpc/cpsOrgServer")
public interface CcsOrgInter extends RemoteService {

	Organization getDetailCpsOrg();

	Map<String, LinkedHashMap<String, String>> getMessageValueMaps();
}
