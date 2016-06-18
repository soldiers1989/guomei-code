package com.sunline.ccs.param.ui.client.countryctrl;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.sunline.ccs.param.def.CountryCtrl;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.ppy.dictionary.exception.ProcessException;
/*
import com.sunline.ppy.dictionary.exception.ProcessException;
import com.sunline.ccs.param.def.CountryCtrl;
import com.sunline.ark.gwt.shared.datasource.FetchRequest;
import com.sunline.ark.gwt.shared.datasource.FetchResponse;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
*/
@RemoteServiceRelativePath("rpc/authCountryServer")
public interface AuthCountryInter extends RemoteService {
	
	FetchResponse getAuthCountryList(FetchRequest request);

	void addNewAuthCountry(Map<String, Serializable> map) throws ProcessException;

	void updateAuthCountry(Map<String, Serializable> map) throws ProcessException;

	CountryCtrl getAuthCountry(String key) throws ProcessException;

	void deleteAuthCountry(List<String> keys) throws ProcessException;

}
