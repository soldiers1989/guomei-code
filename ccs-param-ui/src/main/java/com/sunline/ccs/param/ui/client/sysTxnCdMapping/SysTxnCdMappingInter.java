package com.sunline.ccs.param.ui.client.sysTxnCdMapping;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.sunline.ccs.param.def.SysTxnCdMapping;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.ppy.dictionary.exception.ProcessException;
/*
import com.sunline.ppy.dictionary.exception.ProcessException;
import com.sunline.ccs.param.def.SysTxnCdMapping;
import com.sunline.ark.gwt.shared.datasource.FetchRequest;
import com.sunline.ark.gwt.shared.datasource.FetchResponse;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
*/
@RemoteServiceRelativePath("rpc/sysTxnCdMappingServer")
public interface SysTxnCdMappingInter extends RemoteService {

	FetchResponse getSysTxnCdMappingList(FetchRequest request);

	void addSysTxnCdMapping(Map<String, Serializable> map) throws ProcessException;

	void updateSysTxnCdMapping(Map<String, Serializable> map) throws ProcessException;
	
	 void deleteSysTxnCdMapping(List<String> keys) throws ProcessException;

	SysTxnCdMapping getSysTxnCdMapping(String key);
}
