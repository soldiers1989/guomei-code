package com.sunline.ccs.param.ui.server;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sunline.ccs.param.def.MerchantTxnCrtl;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.kylin.web.core.client.exception.FlatException;
import com.sunline.pcm.facility.ParameterFacility;
import com.sunline.pcm.facility.ParameterFetchResponseFacility;

@Controller
@RequestMapping(value = {"merchantTxnCrtlServer ", "/merchantTxnCrtlServer " })
public class MerchantTxnCrtlServer {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private ParameterFetchResponseFacility responseFacility;

	@Autowired
	private ParameterFacility parameterFacility;

	@ResponseBody
	@RequestMapping(value = "/updateMerchantTxnCrtl", method = {RequestMethod.POST })
	public void updateMerchantTxnCrtl(@RequestBody MerchantTxnCrtl merchantTxnCrtl) throws FlatException {
		try {
			parameterFacility.updateParameterObject(merchantTxnCrtl.merchantId, merchantTxnCrtl);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new FlatException("更新指定商户交易授权控制失败", e);
		}
	}

	@ResponseBody
	@RequestMapping(value = "/getMerchantTxnCrtl", method = {RequestMethod.POST })
	public MerchantTxnCrtl getMerchantTxnCrtl(@RequestBody String merchantTxnCrtl) throws FlatException {
		try {
			return parameterFacility.getParameterObject(merchantTxnCrtl, MerchantTxnCrtl.class);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new FlatException("获取指定商户交易授权控制详细信息失败", e);
		}
	}

	@ResponseBody
	@RequestMapping(value = "/addMerchantTxnCrtl", method = {RequestMethod.POST })
	public void addMerchantTxnCrtl(@RequestBody MerchantTxnCrtl merchantTxnCrtl) throws FlatException {
		try {
			parameterFacility.addNewParameter(merchantTxnCrtl.merchantId, merchantTxnCrtl);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new FlatException("添加指定商户交易授权控制控制失败", e);
		}
	}

	@ResponseBody
	@RequestMapping(value = "/getMerchantTxnCrtlList", method = {RequestMethod.POST })
	public FetchResponse getMerchantTxnCrtlList(@RequestBody(required = false) FetchRequest request) {
		try {
			return responseFacility.getFetchResponse(request, MerchantTxnCrtl.class);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new FlatException("获取指定商户交易授权控制控制失败", e);
		}
	}

	@ResponseBody
	@RequestMapping(value = "/deleteMerchantTxnCrtl", method = {RequestMethod.POST })
	public void deleteMerchantTxnCrtl(@RequestBody List<String> keys) throws FlatException {
		try {
			for(String key : keys) {
				parameterFacility.deleteParameterObject(key, MerchantTxnCrtl.class);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new FlatException("删除指定商户交易授权控制控制失败", e);
		}
	}
}
