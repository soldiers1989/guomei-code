package com.sunline.ccs.param.ui.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sunline.ccs.param.def.MerchantGroup;
import com.sunline.ccs.param.ui.client.merchantGroup.MerchantGroupInter;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.pcm.facility.ParameterFacility;
import com.sunline.pcm.facility.ParameterFetchResponseFacility;
import com.sunline.ppy.dictionary.exception.ProcessException;

@SuppressWarnings("serial")
@Controller
@RequestMapping(value = {"merchantGroupServer", "/merchantGroupServer"})
@Transactional
public class MerchantGroupServer /* extends GWTServer */implements MerchantGroupInter {

	@Autowired
	private ParameterFetchResponseFacility parameterFetchResponseFacility;

	@Autowired
	private ParameterFacility parameterFacility;

	@Override
	@ResponseBody
    @RequestMapping(value = "/fetchMerchantGroupList", method = {RequestMethod.POST})
	public FetchResponse fetchMerchantGroupList(@RequestBody FetchRequest request) {
		return parameterFetchResponseFacility.getFetchResponse(request, MerchantGroup.class);
	}

	@Override
	@ResponseBody
    @RequestMapping(value = "/addMerchantGroup", method = {RequestMethod.POST})
	public void addMerchantGroup(@RequestBody MerchantGroup merchantGroup) throws ProcessException {
		parameterFacility.addNewParameter(merchantGroup.merGroupId, merchantGroup);
	}

	@Override
	@ResponseBody
    @RequestMapping(value = "/updateMerchantGroup", method = {RequestMethod.POST})
	public void updateMerchantGroup(@RequestBody MerchantGroup merchantGroup) throws ProcessException {
		parameterFacility.updateParameterObject(merchantGroup.merGroupId, merchantGroup);
	}

	@Override
	@ResponseBody
    @RequestMapping(value = "/deleteMerchantGroup", method = {RequestMethod.POST})
	public void deleteMerchantGroup(@RequestBody List<String> keys) throws ProcessException {
		if(keys != null) {
			for(String key : keys) {
				parameterFacility.deleteParameterObject(key, MerchantGroup.class);
			}
		}
	}

	@Override
	@ResponseBody
    @RequestMapping(value = "/getMerchantGroup", method = {RequestMethod.POST})
	public MerchantGroup getMerchantGroup(@RequestBody String merGroupId) {
		return parameterFacility.getParameterObject(merGroupId, MerchantGroup.class);
	}
}
