package com.sunline.ccs.param.ui.client.contrlServMapping;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sunline.ccs.param.def.NfContrlServMapping;
/*
import com.sunline.ccs.param.def.NfContrlServMapping;
import com.sunline.ccs.param.def.NfServiceDef;
import com.sunline.ark.gwt.shared.datasource.FetchRequest;
import com.sunline.ark.gwt.shared.datasource.FetchResponse;
import com.google.gwt.user.client.rpc.AsyncCallback;
*/
/**
 * 非金融服务控制约束接口
* @author fanghj
 *
 */
public interface NfContrlSrvMappingInterAsync {

	void saveContrlSerrMapping(NfContrlServMapping contrlServMapping,
			AsyncCallback<Void> callback);
	
	void updateContrlSerrMapping(NfContrlServMapping contrlServMapping,
			AsyncCallback<Void> callback);

//	void getNfContrSrvMappingList(FetchRequest fetchRequest,
//			AsyncCallback<FetchResponse> callback);

	void getNfContrlSvrMapping(String srvCode,
			AsyncCallback<List> callback);

	void deleteNfContrlSrvMapping(List<String> keys,
			AsyncCallback<Void> callback);

	void getNfContrSrvMappingList(
			String servCode, AsyncCallback<Map<String, Serializable>> callback);

	

}
