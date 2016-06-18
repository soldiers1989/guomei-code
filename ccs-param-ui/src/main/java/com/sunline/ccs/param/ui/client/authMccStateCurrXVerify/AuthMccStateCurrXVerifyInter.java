package com.sunline.ccs.param.ui.client.authMccStateCurrXVerify;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.sunline.ccs.param.def.AuthMccStateCurrXVerify;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.ppy.dictionary.exception.ProcessException;
/*
import com.sunline.ppy.dictionary.exception.ProcessException;
import com.sunline.ccs.param.def.AuthMccStateCurrXVerify;
import com.sunline.ark.gwt.shared.datasource.FetchRequest;
import com.sunline.ark.gwt.shared.datasource.FetchResponse;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
*/
@RemoteServiceRelativePath("rpc/authMccStateCurrXVerifyServer")
public interface AuthMccStateCurrXVerifyInter extends RemoteService {
	FetchResponse getAuthMccStateCurrXVerifyList(FetchRequest request);

	void addAuthMccStateCurrXVerify(AuthMccStateCurrXVerify verify) throws ProcessException;

	void updateAuthMccStateCurrXVerify(AuthMccStateCurrXVerify verify) throws ProcessException;
	
	void deleteAuthMccStateCurrXVerify(List<String> keys) throws ProcessException;

	AuthMccStateCurrXVerify getAuthMccStateCurrXVerify(String key);
}
