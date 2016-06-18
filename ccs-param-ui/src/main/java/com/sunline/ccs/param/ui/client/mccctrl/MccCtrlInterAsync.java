package com.sunline.ccs.param.ui.client.mccctrl;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sunline.ccs.param.def.MccCtrl;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
/*
import com.sunline.ccs.param.def.MccCtrl;
import com.sunline.ark.gwt.shared.datasource.FetchRequest;
import com.sunline.ark.gwt.shared.datasource.FetchResponse;
import com.google.gwt.user.client.rpc.AsyncCallback;
*/
public interface MccCtrlInterAsync {

	void addMccCtrl(Map<String, Serializable> map, AsyncCallback<Void> callback);

	void getMccCtrl(String key, AsyncCallback<MccCtrl> callback);

	void getMccCtrlList(FetchRequest request, AsyncCallback<FetchResponse> callback);

	void updateMccCtrl(Map<String, Serializable> map, AsyncCallback<Void> callback);

	void deleteMccCtrl(List<String> keys, AsyncCallback<Void> callback);

}
