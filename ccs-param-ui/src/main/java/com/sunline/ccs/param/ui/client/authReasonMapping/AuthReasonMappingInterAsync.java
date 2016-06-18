package com.sunline.ccs.param.ui.client.authReasonMapping;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sunline.ccs.param.def.AuthReasonMapping;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
/*
import com.sunline.ccs.param.def.AuthReasonMapping;
import com.sunline.ark.gwt.shared.datasource.FetchRequest;
import com.sunline.ark.gwt.shared.datasource.FetchResponse;
import com.google.gwt.user.client.rpc.AsyncCallback;
*/
public interface AuthReasonMappingInterAsync {

	void addNewAuthReasonMapping(Map<String, Serializable> map, AsyncCallback<Void> callback);

	void getAuthReasonMappingList(FetchRequest request, AsyncCallback<FetchResponse> callback);

	void updateAuthReasonMapping(Map<String, Serializable> map, AsyncCallback<Void> callback);

	void getAuthReasonMapping(String key, AsyncCallback<AuthReasonMapping> callback);

	void deleteAuthReasonMapping(List<String> keys, AsyncCallback<Void> callback);

}
