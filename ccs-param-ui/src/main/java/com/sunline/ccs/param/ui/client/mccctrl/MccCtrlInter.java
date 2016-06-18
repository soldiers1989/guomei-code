package com.sunline.ccs.param.ui.client.mccctrl;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestBody;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.sunline.ccs.param.def.MccCtrl;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.ppy.dictionary.exception.ProcessException;
/*
import com.sunline.ppy.dictionary.exception.ProcessException;
import com.sunline.ccs.param.def.MccCtrl;
import com.sunline.ark.gwt.shared.datasource.FetchRequest;
import com.sunline.ark.gwt.shared.datasource.FetchResponse;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
*/
@RemoteServiceRelativePath("rpc/mccCtrlServer")
public interface MccCtrlInter extends RemoteService {

	FetchResponse getMccCtrlList(FetchRequest request);

	void addMccCtrl(MccCtrl mccCtrl) throws ProcessException;

	void updateMccCtrl(MccCtrl mccCtrl) throws ProcessException;
	
	void deleteMccCtrl(List<String> keys) throws ProcessException;

	MccCtrl getMccCtrl(String key);
}
