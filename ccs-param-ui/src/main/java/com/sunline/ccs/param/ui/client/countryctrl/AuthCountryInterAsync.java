package com.sunline.ccs.param.ui.client.countryctrl;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sunline.ccs.param.def.CountryCtrl;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
/*
import com.sunline.ccs.param.def.CountryCtrl;
import com.sunline.ark.gwt.shared.datasource.FetchRequest;
import com.sunline.ark.gwt.shared.datasource.FetchResponse;
import com.google.gwt.user.client.rpc.AsyncCallback;
*/
public interface AuthCountryInterAsync {

	void addNewAuthCountry(Map<String, Serializable> map, AsyncCallback<Void> callback);

	void getAuthCountry(String key, AsyncCallback<CountryCtrl> callback);

	void deleteAuthCountry(List<String> keys, AsyncCallback<Void> callback);

	void getAuthCountryList(FetchRequest request, AsyncCallback<FetchResponse> callback);

	void updateAuthCountry(Map<String, Serializable> map, AsyncCallback<Void> callback);

}
