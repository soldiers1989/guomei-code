package com.sunline.ccs.param.ui.client.accountattr;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;

public interface AccountAtrributeServerInterAsync {

	void addAccountAttr(Map<String, Serializable> accountAttrMap, AsyncCallback<Void> callback);

	void deleteAccountAttr(List<String> accountAttrList, AsyncCallback<Void> callback);

	void getAccountAttr(String key, AsyncCallback<AccountAttribute> callback);

	void getAccountAttrList(FetchRequest request, AsyncCallback<FetchResponse> callback);

	void updateAccountAttr(String key, Map<String, Serializable> accountAttrMap, AsyncCallback<Void> callback);

}
