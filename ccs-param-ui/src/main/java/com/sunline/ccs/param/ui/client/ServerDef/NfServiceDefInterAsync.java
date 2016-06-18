package com.sunline.ccs.param.ui.client.ServerDef;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sunline.ccs.param.def.NfServiceDef;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
/*
import com.sunline.ccs.param.def.NfServiceDef;
import com.sunline.ark.gwt.shared.datasource.FetchRequest;
import com.sunline.ark.gwt.shared.datasource.FetchResponse;
import com.google.gwt.user.client.rpc.AsyncCallback;
*/
public interface NfServiceDefInterAsync {

	void getNfServiceDefList(FetchRequest fetchRequest,
			AsyncCallback<FetchResponse> callback);

	void addNfServiceDef(NfServiceDef nfServiceDef, AsyncCallback<Void> callback);

	void getNfServiceDef(String svrCode, AsyncCallback<NfServiceDef> callback);

	void updateNfServiceDef(NfServiceDef nfServiceDef,
			AsyncCallback<Void> callback);

	void deleteNfServiceDef(List<String> keys, AsyncCallback<Void> callback);

}
