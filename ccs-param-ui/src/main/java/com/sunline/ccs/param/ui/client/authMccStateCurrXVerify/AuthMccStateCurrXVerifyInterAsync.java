package com.sunline.ccs.param.ui.client.authMccStateCurrXVerify;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sunline.ccs.param.def.AuthMccStateCurrXVerify;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
/*
import com.sunline.ccs.param.def.AuthMccStateCurrXVerify;
import com.sunline.ark.gwt.shared.datasource.FetchRequest;
import com.sunline.ark.gwt.shared.datasource.FetchResponse;
import com.google.gwt.user.client.rpc.AsyncCallback;*/
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;

public interface AuthMccStateCurrXVerifyInterAsync {

	void getAuthMccStateCurrXVerifyList(FetchRequest request, AsyncCallback<FetchResponse> callback);

	void addAuthMccStateCurrXVerify(Map<String, Serializable> map, AsyncCallback<Void> callback);

	void getAuthMccStateCurrXVerify(String key, AsyncCallback<AuthMccStateCurrXVerify> callback);

	void updateAuthMccStateCurrXVerify(Map<String, Serializable> map, AsyncCallback<Void> callback);

	void deleteAuthMccStateCurrXVerify(List<String> keys,
			AsyncCallback<Void> callback);

}
