package com.sunline.ccs.param.ui.client.authReasonMapping;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.sunline.ccs.param.def.AuthReasonMapping;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.ppy.dictionary.exception.ProcessException;
/*
import com.sunline.ppy.dictionary.exception.ProcessException;
import com.sunline.ccs.param.def.AuthReasonMapping;
import com.sunline.ark.gwt.shared.datasource.FetchRequest;
import com.sunline.ark.gwt.shared.datasource.FetchResponse;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
*/
@RemoteServiceRelativePath("rpc/authReasonMappingServer")
public interface AuthReasonMappingInter extends RemoteService {
	FetchResponse getAuthReasonMappingList(FetchRequest request);

	void addNewAuthReasonMapping(Map<String, Serializable> map) throws ProcessException;

	void updateAuthReasonMapping(Map<String, Serializable> map) throws ProcessException;
	
	 void deleteAuthReasonMapping(List<String> keys) throws ProcessException;

	AuthReasonMapping getAuthReasonMapping(String key);
}
