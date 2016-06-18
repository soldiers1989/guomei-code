package com.sunline.ccs.param.ui.client.interestTable;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sunline.ccs.param.def.InterestTable;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
/*
import com.sunline.ccs.param.def.InterestTable;
import com.sunline.ark.gwt.shared.datasource.FetchRequest;
import com.sunline.ark.gwt.shared.datasource.FetchResponse;
import com.google.gwt.user.client.rpc.AsyncCallback;
*/
public interface InterestTableInterAsync {

	void getInterestTableList(FetchRequest request,
			AsyncCallback<FetchResponse> callback);

	void addInterestTable(InterestTable interest,
			AsyncCallback<Void> callback);

	void updateInterestTable(InterestTable interest,
			AsyncCallback<Void> callback);

	void getInterestTable(String key, AsyncCallback<InterestTable> callback);

	void deleteInterestTable(List<String> keys, AsyncCallback<Void> callback);
	
}
