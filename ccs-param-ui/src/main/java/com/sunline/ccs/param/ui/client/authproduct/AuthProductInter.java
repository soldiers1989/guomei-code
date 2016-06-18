package com.sunline.ccs.param.ui.client.authproduct;

import java.io.Serializable;
import java.util.Map;

import com.sunline.ppy.dictionary.enums.AuthTransType;
import com.sunline.ppy.dictionary.exception.ProcessException;
import com.sunline.ccs.param.def.AuthProduct;
import com.sunline.ccs.param.def.enums.AuthTransTerminal;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("rpc/authProductServer")
public interface AuthProductInter extends RemoteService {
	AuthProduct getAuthProduct(String productCode);

	void saveOrUpdateAuthProduct(Map<String, Serializable> map, String productCd) throws ProcessException;

	void saveOrUpdateAuthProductReasonAction(Map<String, Serializable> map, String productCd) throws ProcessException;

	void saveOrUpdateAuthProductTransTypeTerminal(Map<AuthTransType, Map<AuthTransTerminal, Boolean>> map, String productCd) throws ProcessException;

	void saveOrUpdateAuthProductVerifyActions(Map<String, Serializable> map, String productCd) throws ProcessException;

	void saveOrUpdateAuthProductCheckEnabled(Map<String, Serializable> map, String productCd) throws ProcessException;

	void saveOrUpdateAuthProductTerminalEnabled(Map<String, Serializable> map, String productCd) throws ProcessException;
}
