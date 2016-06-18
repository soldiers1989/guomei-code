package com.sunline.ccs.param.ui.client.merchantTxnCrtl;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.sunline.ccs.param.def.MerchantTxnCrtl;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.ppy.dictionary.exception.ProcessException;
/*
import com.sunline.ppy.dictionary.exception.ProcessException;
import com.sunline.ccs.param.def.MerchantTxnCrtl;
import com.sunline.ark.gwt.shared.datasource.FetchRequest;
import com.sunline.ark.gwt.shared.datasource.FetchResponse;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
*/
@RemoteServiceRelativePath("rpc/merchantTxnCrtlServer")
public interface MerchantTxnCrtlInter extends RemoteService {
	FetchResponse getMerchantTxnCrtlList(FetchRequest request);

	void addMerchantTxnCrtl(Map<String, Serializable> map) throws ProcessException;

	void updateMerchantTxnCrtl(Map<String, Serializable> map) throws ProcessException;
	
	 void deleteMerchantTxnCrtl(List<String> keys) throws ProcessException;

	MerchantTxnCrtl getMerchantTxnCrtl(String key);
}
