package com.sunline.ccs.param.ui.client.merchantGroup;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.sunline.ccs.param.def.MerchantGroup;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.ppy.dictionary.exception.ProcessException;
/*
import com.sunline.ppy.dictionary.exception.ProcessException;
import com.sunline.ccs.param.def.MerchantGroup;
import com.sunline.ark.gwt.shared.datasource.FetchRequest;
import com.sunline.ark.gwt.shared.datasource.FetchResponse;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
*/
@RemoteServiceRelativePath("rpc/merchantGroupServer")
public interface MerchantGroupInter extends RemoteService{
	
	FetchResponse fetchMerchantGroupList(FetchRequest request);
	
	MerchantGroup getMerchantGroup(String merGroupId);
	
	void addMerchantGroup(MerchantGroup merchantGroup) throws ProcessException;
	
	void updateMerchantGroup(MerchantGroup merchantGroup) throws ProcessException;
	
	void deleteMerchantGroup(List<String> keys) throws ProcessException;

}
