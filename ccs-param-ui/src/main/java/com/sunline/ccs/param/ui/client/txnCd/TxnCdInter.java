package com.sunline.ccs.param.ui.client.txnCd;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.sunline.ccs.param.def.TxnCd;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.ppy.dictionary.exception.ProcessException;
/*
import com.sunline.ppy.dictionary.exception.ProcessException;
import com.sunline.ccs.param.def.TxnCd;
import com.sunline.ark.gwt.shared.datasource.FetchRequest;
import com.sunline.ark.gwt.shared.datasource.FetchResponse;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
*/
@RemoteServiceRelativePath("rpc/txnCdServer")
public interface TxnCdInter extends RemoteService {
	FetchResponse getTxnCdList(FetchRequest request);

	void addTxnCd(Map<String, Serializable> map) throws ProcessException;

	void updateTxnCd(Map<String, Serializable> map) throws ProcessException;
	
	 void deleteTxnCd(List<String> keys) throws ProcessException;

	TxnCd getTxnCd(String key);
}
