package com.sunline.ccs.param.ui.client.blockcode;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.sunline.ccs.param.def.BlockCode;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.ppy.dictionary.exception.ProcessException;
/*
import com.sunline.ppy.dictionary.exception.ProcessException;
import com.sunline.ccs.param.def.BlockCode;
import com.sunline.ark.gwt.shared.datasource.FetchRequest;
import com.sunline.ark.gwt.shared.datasource.FetchResponse;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
*/
@RemoteServiceRelativePath("rpc/blockCodeServer")
public interface BlockCodeInter extends RemoteService {
	FetchResponse getBlockCodeList(FetchRequest request);

	void addBlockCode(Map<String, Serializable> map) throws ProcessException;

	void updateBlockCode(Map<String, Serializable> map) throws ProcessException;
	
	void deleteBlockCode(List<String> keys) throws ProcessException;

	BlockCode getBlockCode(String key);
}
