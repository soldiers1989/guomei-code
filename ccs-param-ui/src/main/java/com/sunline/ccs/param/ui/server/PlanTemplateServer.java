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
import com.sunline.ccs.param.def.BucketDef;
import com.sunline.ccs.param.def.InterestTable;
import com.sunline.ccs.param.def.PlanTemplate;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.pcm.facility.ParameterFacility;
import com.sunline.pcm.facility.ParameterFetchResponseFacility;
import com.sunline.ppy.dictionary.enums.BucketType;
import com.sunline.ppy.dictionary.exception.ProcessException;

@Controller
@RequestMapping(value = "/planTemplateServer")
@Transactional
public class PlanTemplateServer
{
    
    @Autowired
    private ParameterFetchResponseFacility parameterFetchResponseFacility;
    
    @Autowired
    private ParameterFacility parameterFacility;
    
    @ResponseBody
    @RequestMapping(value = "/getPlantemplateList", method = {RequestMethod.POST})
    public FetchResponse getPlantemplateList(@RequestBody FetchRequest request)
    {
        return parameterFetchResponseFacility.getFetchResponse(request, PlanTemplate.class);
    }
    
    @ResponseBody
    @RequestMapping("/addPlantemplate")
    public void addPlantemplate(@RequestBody Map<String, Object> map,
        @RequestBody Map<BucketType, BucketDef> intParameterBuckets) throws ProcessException
    {
    	int i = Integer.parseInt((String) map.get("intTableId"));              //本金罚息利率表的id传过来是String,直接放进去会报错，单独拿出来传进去
    	map.remove("intTableId");
        PlanTemplate plan = new PlanTemplate();
        MapUtils.updateFieldFromMap(plan, map);
        plan.intTableId = i;
        plan.intParameterBuckets = intParameterBuckets;
        parameterFacility.addNewParameter(plan.planNbr, plan);
    }
    
    @ResponseBody
    @RequestMapping("/updatePlantemplate")
    public void updatePlantemplate(@RequestBody PlanTemplate plan,
        @RequestBody Map<BucketType, BucketDef> intParameterBuckets) throws ProcessException
    {
//        PlanTemplate plan = new PlanTemplate();
//        MapUtils.updateFieldFromMap(plan, map);
        plan.intParameterBuckets = intParameterBuckets;
        parameterFacility.updateParameterObject(plan.planNbr, plan);
    }
    
    @ResponseBody
    @RequestMapping(value = "/getPlantemplate", method = {RequestMethod.POST})
    public PlanTemplate getPlantemplate(@RequestBody String key)
    {
        return parameterFacility.getParameterObject(key, PlanTemplate.class);
    }
    
    public void addInterest(InterestTable interest) throws ProcessException
    {
        // ServerUtil.validateRateCeil(interest.rateCeils);
        parameterFacility.addNewParameter(interest.intTableId.toString(), interest);
    }
    
    @ResponseBody
    @RequestMapping(value = "/deletePlantemplate", method = {RequestMethod.POST})
    public void deletePlantemplate(@RequestBody List<String> keys) throws ProcessException
    {
        for (String key : keys)
        {
            parameterFacility.deleteParameterObject(key, PlanTemplate.class);
        }
    }
}
