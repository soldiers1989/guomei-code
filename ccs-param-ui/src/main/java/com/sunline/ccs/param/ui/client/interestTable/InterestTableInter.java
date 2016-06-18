package com.sunline.ccs.param.ui.client.interestTable;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.sunline.ccs.param.def.InterestTable;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.ppy.dictionary.exception.ProcessException;
/*
import com.sunline.ppy.dictionary.exception.ProcessException;
import com.sunline.ccs.param.def.InterestTable;
import com.sunline.ark.gwt.shared.datasource.FetchRequest;
import com.sunline.ark.gwt.shared.datasource.FetchResponse;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
*/
@RemoteServiceRelativePath("rpc/interestTableServer")
public interface InterestTableInter extends RemoteService {
	FetchResponse getInterestTableList(FetchRequest request);

	void addInterestTable(InterestTable interest) throws ProcessException;

	void updateInterestTable(InterestTable interest) throws ProcessException;

	void deleteInterestTable(List<String> keys) throws ProcessException;
	
	InterestTable getInterestTable(String key);
}
