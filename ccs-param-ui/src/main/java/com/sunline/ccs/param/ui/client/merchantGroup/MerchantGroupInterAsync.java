package com.sunline.ccs.param.ui.client.merchantGroup;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sunline.ccs.param.def.MerchantGroup;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
/*
import com.sunline.ccs.param.def.MerchantGroup;
import com.sunline.ark.gwt.shared.datasource.FetchRequest;
import com.sunline.ark.gwt.shared.datasource.FetchResponse;
import com.google.gwt.user.client.rpc.AsyncCallback;
*/
public interface MerchantGroupInterAsync {

	void fetchMerchantGroupList(FetchRequest request,
			AsyncCallback<FetchResponse> callback);

	void getMerchantGroup(String merGroupId, AsyncCallback<MerchantGroup> callback);

	void addMerchantGroup(MerchantGroup downMsgTemplate,
			AsyncCallback<Void> callback);

	void updateMerchantGroup(MerchantGroup downMsgTemplate,
			AsyncCallback<Void> callback);

	void deleteMerchantGroup(List<String> keys, AsyncCallback<Void> callback);
}
