package com.sunline.ccs.param.ui.client.util;

import java.util.LinkedHashMap;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface CcsSelectOptionInterAsync {

	void getTxnCd(AsyncCallback<LinkedHashMap<String, String>> callback);

	void getPlantemplateCd(AsyncCallback<LinkedHashMap<String, String>> callback);

	void getLoanPLan(AsyncCallback<LinkedHashMap<String, String>> callback);

	void getAcctAtrrId(AsyncCallback<LinkedHashMap<String, String>> callback);

	void getPaymentHierarchy(AsyncCallback<LinkedHashMap<String, String>> callback);

	void getInterestId(AsyncCallback<LinkedHashMap<String, String>> callback);

	void getBranchList(AsyncCallback<LinkedHashMap<String, String>> callback);
	
	void getProdcreditList(AsyncCallback<LinkedHashMap<String, String>> callback);
	
	void getLoanPLanForType(AsyncCallback<LinkedHashMap<String, String>> callback);
	
	void getMerList(AsyncCallback<LinkedHashMap<String, String>> callback);
	
//	void getMccList(AsyncCallback<LinkedHashMap<String, String>> callback);

}
