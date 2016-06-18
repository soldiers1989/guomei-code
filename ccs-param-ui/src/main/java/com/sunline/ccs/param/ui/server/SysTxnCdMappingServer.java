package com.sunline.ccs.param.ui.server;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sunline.ark.support.meta.MapUtils;
import com.sunline.ccs.param.def.Program;
import com.sunline.ccs.param.def.SysTxnCdMapping;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.kylin.web.core.client.exception.FlatException;
import com.sunline.pcm.facility.ParameterFacility;
import com.sunline.pcm.facility.ParameterFetchResponseFacility;
import com.sunline.ppy.dictionary.exception.ProcessException;

@SuppressWarnings("serial")
@Controller
@RequestMapping(value = "/sysTxnCdServer")
@Transactional
public class SysTxnCdMappingServer  {

	@Autowired
	private ParameterFetchResponseFacility parameterFetchResponseFacility;

	@Autowired
	private ParameterFacility parameterFacility;

	@ResponseBody
	@RequestMapping(value = "/getSysTxnCdMappingList", method = {RequestMethod.POST})
	public FetchResponse getSysTxnCdMappingList(@RequestBody(required = false) FetchRequest request) {
//		FetchResponse re = new FetchResponse();
//		re= parameterFetchResponseFacility.getFetchResponse(request, SysTxnCdMapping.class);
//		return re;
		try
        {
            return parameterFetchResponseFacility.getFetchResponse(request,SysTxnCdMapping.class);
        }
        catch (Exception e)
        {
            throw new FlatException("获取系统内部交易码映射失败", e);
        }
	}

	@ResponseBody
	@RequestMapping(value = "/addSysTxnCdMapping", method = {RequestMethod.POST})
	public void addSysTxnCdMapping(@RequestBody SysTxnCdMapping mapping) throws ProcessException {
//		SysTxnCdMapping mapping = new SysTxnCdMapping();
//		MapUtils.updateFieldFromMap(mapping, map);
		parameterFacility.addNewParameter(mapping.sysTxnCd.toString(), mapping);
	}
	@ResponseBody
	@RequestMapping(value = "/updateSysTxnCdMapping", method = {RequestMethod.POST})
	public void updateSysTxnCdMapping(@RequestBody SysTxnCdMapping mapping) throws ProcessException {
//		SysTxnCdMapping mapping = new SysTxnCdMapping();
//		MapUtils.updateFieldFromMap(mapping, map);
		parameterFacility.updateParameterObject(mapping.sysTxnCd.toString(), mapping);
	}
	@ResponseBody
	@RequestMapping(value = "/getSysTxnCdMapping", method = {RequestMethod.POST})
	public SysTxnCdMapping getSysTxnCdMapping(@RequestBody String key) {
		return parameterFacility.getParameterObject(key, SysTxnCdMapping.class);
	}
	@ResponseBody
	@RequestMapping(value = "/deleteSysTxnCdMapping", method = {RequestMethod.POST})
	public void deleteSysTxnCdMapping(@RequestBody List<String> keys) throws ProcessException {
		for(String key : keys) {
			parameterFacility.deleteParameterObject(key, SysTxnCdMapping.class);
		}
	}
}
