package com.sunline.ccs.param.ui.server;

import java.io.Serializable;
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
import com.sunline.ccs.param.def.MccCtrl;
import com.sunline.ccs.param.ui.client.mccctrl.MccCtrlInter;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.pcm.facility.ParameterFacility;
import com.sunline.pcm.facility.ParameterFetchResponseFacility;
import com.sunline.ppy.dictionary.exception.ProcessException;

@SuppressWarnings("serial")
@Controller
@RequestMapping(value={"mccCtrlServer","/mccCtrlServer"})
@Transactional
public class MccCtrlServer implements MccCtrlInter {

	@Autowired
	private ParameterFetchResponseFacility parameterFetchResponseFacility;

	@Autowired
	private ParameterFacility parameterFacility;
    
	@Override
	@ResponseBody
	@RequestMapping(value="/getMccCtrlList",method={RequestMethod.POST})
	public FetchResponse getMccCtrlList(@RequestBody  FetchRequest request) {
		return parameterFetchResponseFacility.getFetchResponse(request, MccCtrl.class);
	}

	@Override
	@ResponseBody
	@RequestMapping(value="/addMccCtrl",method={RequestMethod.POST})
	public void addMccCtrl(@RequestBody MccCtrl mccCtrl) throws ProcessException {
		parameterFacility.addNewParameter(mccCtrl.mcc + "|" + mccCtrl.inputSource.toString(), mccCtrl);
	}

	@Override
	@ResponseBody
	@RequestMapping(value="/updateMccCtrl",method={RequestMethod.POST})
	public void updateMccCtrl(@RequestBody MccCtrl mccCtrl) throws ProcessException {
		parameterFacility.updateParameterObject(mccCtrl.mcc + "|" + mccCtrl.inputSource.toString(), mccCtrl);
	}

	@Override
	@ResponseBody
	@RequestMapping(value="/getMccCtrl",method={RequestMethod.POST})
	public MccCtrl getMccCtrl(@RequestBody String key) {
		return parameterFacility.getParameterObject(key, MccCtrl.class);
	}

	@Override
	@ResponseBody
	@RequestMapping(value="/deleteMccCtrl",method={RequestMethod.POST})
	public void deleteMccCtrl(@RequestBody List<String> keys) throws ProcessException {
		for(String key : keys) {
			parameterFacility.deleteParameterObject(key, MccCtrl.class);
		}
	}
}
