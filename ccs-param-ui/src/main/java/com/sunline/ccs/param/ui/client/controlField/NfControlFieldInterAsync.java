package com.sunline.ccs.param.ui.client.controlField;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sunline.ccs.param.def.NfControlField;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
/*
import com.sunline.ccs.param.def.NfControlField;
import com.sunline.ark.gwt.shared.datasource.FetchRequest;
import com.sunline.ark.gwt.shared.datasource.FetchResponse;
import com.google.gwt.user.client.rpc.AsyncCallback;
*/
public interface NfControlFieldInterAsync {

	void getNfControlFieldList(FetchRequest fetchRequest,
			AsyncCallback<FetchResponse> callback);

	void deleteNfControlField(List<String> keys, AsyncCallback<Void> callback);

	void addNfServiceDef(NfControlField nfControlField,
			AsyncCallback<Void> callback);

	void getNfControlField(String fieldCode,
			AsyncCallback<NfControlField> callback);

	void updateNfServiceDef(NfControlField nfControlField,
			AsyncCallback<Void> callback);

}
