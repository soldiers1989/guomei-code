package com.sunline.ccs.param.ui.client.controlField;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.sunline.ccs.param.def.NfControlField;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.ppy.dictionary.exception.ProcessException;
/*
import com.sunline.ppy.dictionary.exception.ProcessException;
import com.sunline.ccs.param.def.NfControlField;
import com.sunline.ark.gwt.shared.datasource.FetchRequest;
import com.sunline.ark.gwt.shared.datasource.FetchResponse;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
*/
@RemoteServiceRelativePath("rpc/nfControlFieldServer")
public interface NfControlFieldInter extends RemoteService {
	
	FetchResponse getNfControlFieldList(FetchRequest fetchRequest);
	
	void deleteNfControlField(List<String> keys) throws ProcessException;

	void addNfServiceDef(NfControlField nfControlField) throws ProcessException;

	NfControlField getNfControlField(String fieldCode) throws ProcessException;

	void updateNfServiceDef(NfControlField nfControlField) throws ProcessException;

}
