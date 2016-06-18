package com.sunline.ccs.param.ui.client.loanMerchant;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sunline.ccs.param.def.LoanMerchant;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
/*
import com.sunline.ccs.param.def.LoanMerchant;
import com.sunline.ark.gwt.shared.datasource.FetchRequest;
import com.sunline.ark.gwt.shared.datasource.FetchResponse;
import com.google.gwt.user.client.rpc.AsyncCallback;
*/
public interface LoanMerchantInterAsync {

	void fetchLoanMerchantList(FetchRequest request,
			AsyncCallback<FetchResponse> callback);

	void getLoanMerchant(String merId, AsyncCallback<LoanMerchant> callback);

	void addLoanMerchant(LoanMerchant loanMerchant, AsyncCallback<Void> callback);

	void updateLoanMerchant(LoanMerchant loanMerchant,
			AsyncCallback<Void> callback);

	void deleteLoanMerchant(List<String> keys, AsyncCallback<Void> callback);

	void loadAddressData(AsyncCallback<TreeMap<String, String>> callback);

	void getMerGroup(boolean showLabel,
			AsyncCallback<LinkedHashMap<String, String>> callback);
}
