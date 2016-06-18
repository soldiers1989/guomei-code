package com.sunline.ccs.param.ui.client.paymentHierarchy;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.sunline.ccs.param.def.PaymentHierarchy;
import com.sunline.ccs.param.def.enums.BucketObject;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.ppy.dictionary.exception.ProcessException;
/*
import com.sunline.ppy.dictionary.exception.ProcessException;
import com.sunline.ccs.param.def.PaymentHierarchy;
import com.sunline.ccs.param.def.enums.BucketObject;
import com.sunline.ark.gwt.shared.datasource.FetchRequest;
import com.sunline.ark.gwt.shared.datasource.FetchResponse;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
*/
@RemoteServiceRelativePath("rpc/paymentHierarchyServer")
public interface PaymentHierarchyInter extends RemoteService {
	FetchResponse getPaymentHierarchyList(FetchRequest request);

	void addPaymentHierarchy(Map<String, Serializable> map, List<BucketObject> objList) throws ProcessException;

	void updatePaymentHierarchy(Map<String, Serializable> map, List<BucketObject> objList) throws ProcessException;
	
	void deletePaymentHierarchy(List<String> keys) throws ProcessException;
	
	PaymentHierarchy getPaymentHierarchy(String key);
}
