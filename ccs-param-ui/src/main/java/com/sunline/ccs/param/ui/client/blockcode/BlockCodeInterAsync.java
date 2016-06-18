package com.sunline.ccs.param.ui.client.blockcode;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sunline.ccs.param.def.BlockCode;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
/*
import com.sunline.ccs.param.def.BlockCode;
import com.sunline.ark.gwt.shared.datasource.FetchRequest;
import com.sunline.ark.gwt.shared.datasource.FetchResponse;
import com.google.gwt.user.client.rpc.AsyncCallback;
*/
public interface BlockCodeInterAsync {

	void getBlockCodeList(FetchRequest request, AsyncCallback<FetchResponse> callback);

	void addBlockCode(Map<String, Serializable> map, AsyncCallback<Void> callback);

	void getBlockCode(String key, AsyncCallback<BlockCode> callback);

	void updateBlockCode(Map<String, Serializable> map, AsyncCallback<Void> callback);

	void deleteBlockCode(List<String> keys, AsyncCallback<Void> callback);

}
