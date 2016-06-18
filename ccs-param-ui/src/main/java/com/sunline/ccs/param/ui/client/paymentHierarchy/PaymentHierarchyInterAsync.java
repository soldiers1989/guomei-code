package com.sunline.ccs.param.ui.client.paymentHierarchy;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sunline.ccs.param.def.PaymentHierarchy;
import com.sunline.ccs.param.def.enums.BucketObject;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
/*
import com.sunline.ccs.param.def.PaymentHierarchy;
import com.sunline.ccs.param.def.enums.BucketObject;
import com.sunline.ark.gwt.shared.datasource.FetchRequest;
import com.sunline.ark.gwt.shared.datasource.FetchResponse;
import com.google.gwt.user.client.rpc.AsyncCallback;
*/
public interface PaymentHierarchyInterAsync {

	void addPaymentHierarchy(Map<String, Serializable> map, List<BucketObject> objList, AsyncCallback<Void> callback);

	void getPaymentHierarchy(String key, AsyncCallback<PaymentHierarchy> callback);

	void getPaymentHierarchyList(FetchRequest request, AsyncCallback<FetchResponse> callback);

	void updatePaymentHierarchy(Map<String, Serializable> map, List<BucketObject> objList, AsyncCallback<Void> callback);

	void deletePaymentHierarchy(List<String> keys, AsyncCallback<Void> callback);

}
