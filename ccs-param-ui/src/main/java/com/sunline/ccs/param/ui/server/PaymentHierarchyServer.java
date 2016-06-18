package com.sunline.ccs.param.ui.server;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sunline.ark.support.meta.MapUtils;
import com.sunline.ccs.infrastructure.shared.map.PaymentHierarchyMapHelper;
import com.sunline.ccs.param.def.PaymentHierarchy;
import com.sunline.ccs.param.def.Program;
import com.sunline.ccs.param.def.enums.BucketObject;
import com.sunline.ccs.param.ui.client.paymentHierarchy.PaymentHierarchyInter;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.kylin.web.core.client.exception.FlatException;
import com.sunline.pcm.facility.ParameterFacility;
import com.sunline.pcm.facility.ParameterFetchResponseFacility;
import com.sunline.ppy.dictionary.exception.ProcessException;

@Controller
@RequestMapping(value = {"paymentHierarchyServer", "/paymentHierarchyServer"})
public class PaymentHierarchyServer {

   Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    private ParameterFetchResponseFacility responseFacility;
    
    @Autowired
    private ParameterFacility parameterFacility;
    
    @ResponseBody
    @RequestMapping(value = "/getPaymentHierarchyList", method = {RequestMethod.POST})
    public FetchResponse getPaymentHierarchyList(@RequestBody(required = false) FetchRequest request) {
	try {
	    return responseFacility.getFetchResponse(request, PaymentHierarchy.class);
	} catch (Exception e) {
	     logger.error(e.getMessage(), e);
	    throw new FlatException("获取分期活动失败", e);
	}
    }

    @ResponseBody
    @RequestMapping(value = "/getPaymentHierarchy", method = {RequestMethod.POST})
    public PaymentHierarchy getPaymentHierarchy(@RequestBody String key) {
	return parameterFacility.getParameterObject(key, PaymentHierarchy.class);
    }

    @ResponseBody
    @RequestMapping(value = "/addPaymentHierarchy", method = {RequestMethod.POST})
    public void addPaymentHierarchy(@RequestBody Map<String, Object> map, @RequestBody List<BucketObject> objList)
	    throws ProcessException {
	PaymentHierarchy paymentHierarchy = new PaymentHierarchy();
	paymentHierarchy.pmtHierId = Integer.valueOf(String.valueOf(map.get("pmtHierId")));
	paymentHierarchy.description = String.valueOf(map.get("description"));
	paymentHierarchy.paymentHier = objList;
	parameterFacility.addNewParameter(paymentHierarchy.pmtHierId.toString(), paymentHierarchy);
    }

    @ResponseBody
    @RequestMapping(value = "/updatePaymentHierarchy", method = {RequestMethod.POST})
    public void updatePaymentHierarchy(@RequestBody Map<String, Object> map, @RequestBody List<BucketObject> objList)
	    throws ProcessException {

	PaymentHierarchy paymentHierarchy = new PaymentHierarchy();
	paymentHierarchy.pmtHierId = Integer.valueOf(String.valueOf(map.get("pmtHierId")));
	paymentHierarchy.description = String.valueOf(map.get("description"));
	paymentHierarchy.paymentHier = objList;
	parameterFacility.updateParameterObject(paymentHierarchy.pmtHierId.toString(), paymentHierarchy);

    }

    @ResponseBody
    @RequestMapping(value = "/deletePaymentHierarchy", method = {RequestMethod.POST})
    public void deletePaymentHierarchy(@RequestBody List<String> keys) throws ProcessException {
	for (String key : keys) {
	    parameterFacility.deleteParameterObject(key, PaymentHierarchy.class);
	}
    }
}
