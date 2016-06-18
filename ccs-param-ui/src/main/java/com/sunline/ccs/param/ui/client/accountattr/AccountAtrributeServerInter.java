package com.sunline.ccs.param.ui.client.accountattr;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.ppy.dictionary.exception.ProcessException;

@RemoteServiceRelativePath("rpc/accountAtrributeServer")
public interface AccountAtrributeServerInter extends RemoteService {

	FetchResponse getAccountAttrList(FetchRequest request);

	void addAccountAttr(Map<String, Serializable> accountAttrMap) throws ProcessException;

	void updateAccountAttr(String key, Map<String, Serializable> accountAttrMap) throws ProcessException;

	AccountAttribute getAccountAttr(String key);

	void deleteAccountAttr(List<String> accountAttrList) throws ProcessException;;
}
