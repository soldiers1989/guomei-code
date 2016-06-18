package com.sunline.ccs.param.ui.server;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sunline.ccs.param.def.Organization;
import com.sunline.ccs.param.ui.client.organization.CcsOrgInter;
import com.sunline.pcm.facility.ParameterFacility;
import com.sunline.pcm.param.def.BMPMessageTemplate;

@SuppressWarnings("serial")
@Controller
@Transactional
public class CcsOrgInterImpl implements CcsOrgInter {

	@Autowired
	private ParameterFacility parameterFacility;

	@Override
	public Organization getDetailCpsOrg() {
		return parameterFacility.getParameterObject(null, com.sunline.ccs.param.def.Organization.class);
	}

	@Override
	public Map<String, LinkedHashMap<String, String>> getMessageValueMaps() {
		Map<String, LinkedHashMap<String, String>> result = new HashMap<String, LinkedHashMap<String, String>>();
		
		List<BMPMessageTemplate> list = parameterFacility.getParameterObject(BMPMessageTemplate.class);
		
		for(BMPMessageTemplate mt : list) {
			if(!result.containsKey(mt.msgCategory)) {
				result.put(mt.msgCategory, new LinkedHashMap<String, String>());
			}
			result.get(mt.msgCategory).put(mt.code, mt.code + "-" + mt.desc);
		}
		return result;
	}
}
