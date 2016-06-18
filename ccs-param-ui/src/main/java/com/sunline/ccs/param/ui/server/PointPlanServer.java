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

import com.sunline.ccs.param.def.PointPlan;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.kylin.web.core.client.exception.FlatException;
import com.sunline.pcm.facility.ParameterFacility;
import com.sunline.pcm.facility.ParameterFetchResponseFacility;


@Controller
/*@Transactional*/

@RequestMapping(value={"/pointPlanServer" , "pointPlanServer"})
public class PointPlanServer {
	
    Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    private ParameterFetchResponseFacility responseFacility;
    
    @Autowired
    private ParameterFacility parameterFacility;
    
    @ResponseBody
    @RequestMapping(value = "/getPointPlanList", method = {RequestMethod.POST})
    public FetchResponse getpointPlanList(@RequestBody FetchRequest request)
    {
        try
        {
            return responseFacility.getFetchResponse(request, PointPlan.class);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new FlatException("获取积分计划列表失败", e);
        }
    }
    
    @ResponseBody
    @RequestMapping(value = "/addPointPlan", method = {RequestMethod.POST})
    public void addPointPlan(@RequestBody PointPlan plan) throws FlatException
    {
        try
        {

    		parameterFacility.addNewParameter(plan.planNbr, plan);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new FlatException("添加积分计划失败", e);
        }
    }
    
    @ResponseBody
    @RequestMapping(value = "/updatePointPlan", method = {RequestMethod.POST})
    public void updatePointPlan(@RequestBody PointPlan plan) throws FlatException
    {
        try
        {
            parameterFacility.updateParameterObject(plan.planNbr, plan);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new FlatException("更新积分计划失败", e);
        }
    }
    
    @ResponseBody
    @RequestMapping(value = "/getPointPlan", method = {RequestMethod.POST})
    public PointPlan getPointPlan(@RequestBody String plan) throws FlatException
    {
        try
        {
            return parameterFacility.getParameterObject(plan, PointPlan.class);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new FlatException("获取积分计划详细信息失败", e);
        }
    }
    
    @ResponseBody
    @RequestMapping(value = "/deletePointPlan", method = {RequestMethod.POST})
    public void deletePointPlan(@RequestBody List<String> keys) throws FlatException
    {
        try
        {
            for (String key : keys)
            {
                parameterFacility.deleteParameterObject(key, PointPlan.class);
            }
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new FlatException("删除积分计划失败", e);
        }
    }
    
}
/*public class PointPlanServer  extends GWTServer implements PointPlanInter {

	@Autowired
	private ParameterFetchResponseFacility fetchResponseFacility;

	@Autowired
	private ParameterFacility parameterFacility;

	@Override
	public FetchResponse getPointPlanList(FetchRequest request) {
		return fetchResponseFacility.getFetchResponse(request, PointPlan.class);
	}

	@Override
	public void addPointPlan(PointPlan plan) throws ProcessException {
		parameterFacility.addNewParameter(plan.planNbr, plan);
	}

	@Override
	public void updatePointPlan(PointPlan plan) throws ProcessException {
		parameterFacility.updateParameterObject(plan.planNbr, plan);
	}

	@Override
	public void deletePointPlan(List<String> keys) throws ProcessException {
		for(String key : keys) {
			parameterFacility.deleteParameterObject(key, PointPlan.class);
		}
	}

	@Override
	public PointPlan getPointPlan(String key) {
		return parameterFacility.getParameterObject(key, PointPlan.class);
	}
}*/
