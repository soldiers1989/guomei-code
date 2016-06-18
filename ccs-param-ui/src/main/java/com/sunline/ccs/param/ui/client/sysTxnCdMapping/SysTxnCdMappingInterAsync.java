package com.sunline.ccs.param.ui.client.sysTxnCdMapping;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sunline.ccs.param.def.SysTxnCdMapping;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
/*
import com.sunline.ccs.param.def.SysTxnCdMapping;
import com.sunline.ark.gwt.shared.datasource.FetchRequest;
import com.sunline.ark.gwt.shared.datasource.FetchResponse;
import com.google.gwt.user.client.rpc.AsyncCallback;
*/
public interface SysTxnCdMappingInterAsync {

	void addSysTxnCdMapping(Map<String, Serializable> map, AsyncCallback<Void> callback);

	void getSysTxnCdMapping(String key, AsyncCallback<SysTxnCdMapping> callback);

	void getSysTxnCdMappingList(FetchRequest request, AsyncCallback<FetchResponse> callback);

	void updateSysTxnCdMapping(Map<String, Serializable> map, AsyncCallback<Void> callback);

	void deleteSysTxnCdMapping(List<String> keys, AsyncCallback<Void> callback);

}
