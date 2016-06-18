package com.sunline.ccs.param.ui.server;

import java.util.ArrayList;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import com.sunline.ark.support.meta.MapUtils;
import com.sunline.ccs.param.def.AuthReasonMapping;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.kylin.web.core.client.exception.FlatException;
import com.sunline.pcm.facility.ParameterFacility;
import com.sunline.pcm.facility.ParameterFetchResponseFacility;

@Controller
@RequestMapping(value= { "authReasonMappingServer", "/authReasonMappingServer"})
public class AuthReasonMappingServer
{
    Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    private ParameterFetchResponseFacility responseFacility;
    
    @Autowired
    private ParameterFacility parameterFacility;
    
    @ResponseBody
    @RequestMapping(value = "/getAuthReasonMappingList", method = {RequestMethod.POST})
    public FetchResponse getAuthReasonMappingList(@RequestBody FetchRequest request)
    {
    	try
        {
    		return responseFacility.getFetchResponse(request, AuthReasonMapping.class);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new FlatException("获取授权原因码列表失败", e);
        }
    }
    
    @ResponseBody
    @RequestMapping(value = "/addNewAuthReasonMapping", method = {RequestMethod.POST})
    public void addNewAuthReasonMapping(@RequestBody AuthReasonMapping mapping) throws FlatException
    {
        try
        {
    		parameterFacility.addNewParameter(mapping.inputSource.toString() + "|" + mapping.reason.toString(), mapping);
        	
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new FlatException("添加授权原因码失败", e);
        }
    }
    
    @ResponseBody()
	@RequestMapping(value="/updateAuthReasonMapping",method={RequestMethod.POST})
	public void updateAuthReasonMapping(@RequestBody AuthReasonMapping mapping) throws FlatException  {

		try{
			parameterFacility.updateParameterObject(mapping.inputSource.toString() + "|" + mapping.reason.toString(),mapping);
		} 
		catch (Exception e) 
		{
			logger.error(e.getMessage(), e);
			throw new FlatException("更新授权原因码失败", e);
		}
	}
    
	@ResponseBody()
	@RequestMapping(value="/getAuthReasonMapping",method={RequestMethod.POST})
	public AuthReasonMapping getAuthReasonMapping(@RequestBody String key) throws FlatException {

		try{
			return parameterFacility.getParameterObject(key, AuthReasonMapping.class);
		} 
		catch (Exception e) 
		{
			logger.error(e.getMessage(), e);
			throw new FlatException("获取授权原因码参数详细信息失败", e);
		}
	}

	@ResponseBody()
	@RequestMapping(value="/deleteAuthReasonMapping",method={RequestMethod.POST})
	public void deleteAuthReasonMapping(@RequestBody ArrayList<String> keys) throws FlatException  {
		try{
			for (String key : keys) {
				parameterFacility.deleteParameterObject(key, AuthReasonMapping.class);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new FlatException("删除授权原因码失败", e);
		}
	}
	}

