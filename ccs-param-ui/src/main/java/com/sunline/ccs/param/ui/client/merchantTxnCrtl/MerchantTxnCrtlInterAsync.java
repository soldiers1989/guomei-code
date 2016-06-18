package com.sunline.ccs.param.ui.client.merchantTxnCrtl;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sunline.ccs.param.def.MerchantTxnCrtl;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
/*
import com.sunline.ccs.param.def.MerchantTxnCrtl;
import com.sunline.ark.gwt.shared.datasource.FetchRequest;
import com.sunline.ark.gwt.shared.datasource.FetchResponse;
import com.google.gwt.user.client.rpc.AsyncCallback;
*/
public interface MerchantTxnCrtlInterAsync {

	void addMerchantTxnCrtl(Map<String, Serializable> map, AsyncCallback<Void> callback);

	void getMerchantTxnCrtl(String key, AsyncCallback<MerchantTxnCrtl> callback);

	void getMerchantTxnCrtlList(FetchRequest request, AsyncCallback<FetchResponse> callback);

	void updateMerchantTxnCrtl(Map<String, Serializable> map, AsyncCallback<Void> callback);

	void deleteMerchantTxnCrtl(List<String> keys, AsyncCallback<Void> callback);

}
