package com.sunline.ccs.param.ui.client.txnCd;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sunline.ccs.param.def.TxnCd;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
/*
import com.sunline.ccs.param.def.TxnCd;
import com.sunline.ark.gwt.shared.datasource.FetchRequest;
import com.sunline.ark.gwt.shared.datasource.FetchResponse;
import com.google.gwt.user.client.rpc.AsyncCallback;
*/
public interface TxnCdInterAsync {

	void addTxnCd(Map<String, Serializable> map, AsyncCallback<Void> callback);

	void getTxnCd(String key, AsyncCallback<TxnCd> callback);

	void getTxnCdList(FetchRequest request, AsyncCallback<FetchResponse> callback);

	void updateTxnCd(Map<String, Serializable> map, AsyncCallback<Void> callback);

	void deleteTxnCd(List<String> keys, AsyncCallback<Void> callback);

}
