package com.sunline.ccs.param.ui.server;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.sunline.ark.support.meta.MapUtils;
import com.sunline.ccs.param.def.AuthProduct;
import com.sunline.ccs.param.def.enums.AuthAction;
import com.sunline.ccs.param.def.enums.AuthFlagAction;
import com.sunline.ccs.param.def.enums.AuthReason;
import com.sunline.ccs.param.def.enums.AuthTransTerminal;
import com.sunline.ccs.param.def.enums.AuthVerifyAction;
import com.sunline.ccs.param.def.enums.CheckType;
import com.sunline.ccs.param.def.enums.VerifyEnum;
import com.sunline.ccs.param.ui.client.authproduct.AuthProductInter;
import com.sunline.pcm.facility.ParameterFacility;
import com.sunline.ppy.dictionary.enums.AuthTransType;
import com.sunline.ppy.dictionary.exception.ProcessException;

@SuppressWarnings("serial")
// @Controller
@Transactional
public class AuthProductServer /* extends GWTServer */implements AuthProductInter {

	@Autowired
	private ParameterFacility parameterFacility;

	@Override
	public void saveOrUpdateAuthProductReasonAction(Map<String, Serializable> map, String productCd)
			throws ProcessException {
		Map<AuthReason, AuthAction> reasonActions = new HashMap<AuthReason, AuthAction>();
		AuthProduct authProduct = parameterFacility.getParameterObject(productCd, AuthProduct.class);
		for(Map.Entry<String, Serializable> entry : map.entrySet()) {
			String key = entry.getKey();
			if(entry.getValue() != null) {
				String value = entry.getValue().toString();
				reasonActions.put(AuthReason.valueOf(key), AuthAction.valueOf(value));
			}
		}
		if(authProduct == null) {
			authProduct = new AuthProduct();
			authProduct.reasonActions = reasonActions;
			parameterFacility.addNewParameter(productCd, authProduct);
		} else {
			authProduct.reasonActions = reasonActions;
			parameterFacility.updateParameterObject(productCd, authProduct);
		}
	}

	@Override
	public void saveOrUpdateAuthProduct(Map<String, Serializable> map, String productCd) throws ProcessException {
		AuthProduct authProduct = parameterFacility.getParameterObject(productCd, AuthProduct.class);
		if(authProduct == null) {
			authProduct = new AuthProduct();
			MapUtils.updateFieldFromMap(authProduct, map);
			parameterFacility.addNewParameter(productCd, authProduct);
		} else {
			MapUtils.updateFieldFromMap(authProduct, map);
			parameterFacility.updateParameterObject(productCd, authProduct);
		}
	}

	@Override
	public void saveOrUpdateAuthProductTransTypeTerminal(Map<AuthTransType, Map<AuthTransTerminal, Boolean>> map,
			String productCd) throws ProcessException {
		AuthProduct authProduct = parameterFacility.getParameterObject(productCd, AuthProduct.class);
		if(authProduct == null) {
			authProduct = new AuthProduct();
			authProduct.transTypeTerminalEnabled = map;
			parameterFacility.addNewParameter(productCd, authProduct);
		} else {
			authProduct.transTypeTerminalEnabled = map;
			parameterFacility.updateParameterObject(productCd, authProduct);
		}
	}

	@Override
	public AuthProduct getAuthProduct(String productCode) {
		return parameterFacility.getParameterObject(productCode, AuthProduct.class);
	}

	@Override
	public void saveOrUpdateAuthProductVerifyActions(Map<String, Serializable> map, String productCd)
			throws ProcessException {
		Map<VerifyEnum, AuthVerifyAction> verifyActions = new HashMap<VerifyEnum, AuthVerifyAction>();
		AuthProduct authProduct = parameterFacility.getParameterObject(productCd, AuthProduct.class);
		for(Map.Entry<String, Serializable> entry : map.entrySet()) {
			String key = entry.getKey();
			if(entry.getValue() != null) {
				String value = entry.getValue().toString();
				verifyActions.put(VerifyEnum.valueOf(key), AuthVerifyAction.valueOf(value));
			}
		}
		if(authProduct == null) {
			authProduct = new AuthProduct();
			authProduct.verifyActions = verifyActions;
			parameterFacility.addNewParameter(productCd, authProduct);
		} else {
			authProduct.verifyActions = verifyActions;
			parameterFacility.updateParameterObject(productCd, authProduct);
		}
	}

	@Override
	public void saveOrUpdateAuthProductCheckEnabled(Map<String, Serializable> map, String productCd)
			throws ProcessException {
		Map<CheckType, Boolean> checkEnabled = new HashMap<CheckType, Boolean>();
		AuthProduct authProduct = parameterFacility.getParameterObject(productCd, AuthProduct.class);
		for(Map.Entry<String, Serializable> entry : map.entrySet()) {
			String key = entry.getKey();
			if(entry.getValue() != null) {
				String value = entry.getValue().toString();
				checkEnabled.put(CheckType.valueOf(key), Boolean.valueOf(value));
			}
		}
		if(authProduct == null) {
			authProduct = new AuthProduct();
			authProduct.checkEnabled = checkEnabled;
			parameterFacility.addNewParameter(productCd, authProduct);
		} else {
			authProduct.checkEnabled = checkEnabled;
			parameterFacility.updateParameterObject(productCd, authProduct);
		}
	}

	@Override
	public void saveOrUpdateAuthProductTerminalEnabled(Map<String, Serializable> map, String productCd)
			throws ProcessException {
		Map<AuthTransTerminal, AuthFlagAction> terminalEnabled = new HashMap<AuthTransTerminal, AuthFlagAction>();
		AuthProduct authProduct = parameterFacility.getParameterObject(productCd, AuthProduct.class);
		for(Map.Entry<String, Serializable> entry : map.entrySet()) {
			String key = entry.getKey();
			if(entry.getValue() != null) {
				String value = entry.getValue().toString();
				terminalEnabled.put(AuthTransTerminal.valueOf(key), AuthFlagAction.valueOf(value));
			}
		}
		if(authProduct == null) {
			authProduct = new AuthProduct();
			authProduct.terminalEnabled = terminalEnabled;
			parameterFacility.addNewParameter(productCd, authProduct);
		} else {
			authProduct.terminalEnabled = terminalEnabled;
			parameterFacility.updateParameterObject(productCd, authProduct);
		}
	}
}
