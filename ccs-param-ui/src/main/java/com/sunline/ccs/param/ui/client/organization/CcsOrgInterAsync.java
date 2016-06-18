package com.sunline.ccs.param.ui.client.organization;

import java.util.LinkedHashMap;
import java.util.Map;

import com.sunline.ccs.param.def.Organization;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface CcsOrgInterAsync {

	void getDetailCpsOrg(AsyncCallback<Organization> callback);

	void getMessageValueMaps(
			AsyncCallback<Map<String, LinkedHashMap<String, String>>> callback);

}
