package com.sunline.ccs.param.ui.client.currencyCtrl;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.sunline.ccs.param.def.CurrencyCtrl;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.ppy.dictionary.exception.ProcessException;
/*
import com.sunline.ppy.dictionary.exception.ProcessException;
import com.sunline.ccs.param.def.CurrencyCtrl;
import com.sunline.ark.gwt.shared.datasource.FetchRequest;
import com.sunline.ark.gwt.shared.datasource.FetchResponse;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
*/
@RemoteServiceRelativePath("rpc/currencyCtrlServer")
public interface CurrencyCtrlInter extends RemoteService {
	FetchResponse getCurrencyCdList(FetchRequest request);

	void addCurrencyCd(Map<String, Serializable> map) throws ProcessException;

	void updateCurrencyCd(Map<String, Serializable> map) throws ProcessException;
	
	void deleteCurrencyCd(List<String> keys) throws ProcessException;

	CurrencyCtrl getCurrencyCd(String key);
}
