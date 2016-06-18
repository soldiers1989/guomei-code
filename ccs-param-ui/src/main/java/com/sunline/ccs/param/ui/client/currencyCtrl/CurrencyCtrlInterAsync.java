package com.sunline.ccs.param.ui.client.currencyCtrl;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sunline.ccs.param.def.CurrencyCtrl;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
/*
import com.sunline.ccs.param.def.CurrencyCtrl;
import com.sunline.ark.gwt.shared.datasource.FetchRequest;
import com.sunline.ark.gwt.shared.datasource.FetchResponse;
import com.google.gwt.user.client.rpc.AsyncCallback;
*/
public interface CurrencyCtrlInterAsync {

	void getCurrencyCdList(FetchRequest request, AsyncCallback<FetchResponse> callback);

	void addCurrencyCd(Map<String, Serializable> map, AsyncCallback<Void> callback);

	void updateCurrencyCd(Map<String, Serializable> map, AsyncCallback<Void> callback);

	void getCurrencyCd(String key, AsyncCallback<CurrencyCtrl> callback);

	void deleteCurrencyCd(List<String> keys, AsyncCallback<Void> callback);

}
