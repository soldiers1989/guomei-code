package com.sunline.ccs.param.ui.client.authproduct;

import java.io.Serializable;
import java.util.Map;

import com.sunline.ppy.dictionary.enums.AuthTransType;
import com.sunline.ccs.param.def.AuthProduct;
import com.sunline.ccs.param.def.enums.AuthTransTerminal;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface AuthProductInterAsync {

	void saveOrUpdateAuthProductReasonAction(Map<String, Serializable> map, String productCd, AsyncCallback<Void> callback);

	void saveOrUpdateAuthProduct(Map<String, Serializable> map, String productCd, AsyncCallback<Void> callback);

	void getAuthProduct(String productCode, AsyncCallback<AuthProduct> callback);

	void saveOrUpdateAuthProductVerifyActions(Map<String, Serializable> map, String productCd, AsyncCallback<Void> callback);

	void saveOrUpdateAuthProductCheckEnabled(Map<String, Serializable> map, String productCd, AsyncCallback<Void> callback);

	void saveOrUpdateAuthProductTerminalEnabled(Map<String, Serializable> map, String productCd, AsyncCallback<Void> callback);

	void saveOrUpdateAuthProductTransTypeTerminal(Map<AuthTransType, Map<AuthTransTerminal, Boolean>> map, String productCd,
			AsyncCallback<Void> callback);
}
