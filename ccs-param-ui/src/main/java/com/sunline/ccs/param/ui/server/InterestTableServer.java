package com.sunline.ccs.param.ui.server;

import java.util.List;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sunline.ccs.param.def.InterestTable;
import com.sunline.ccs.param.ui.client.interestTable.InterestTableInter;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.kylin.web.core.client.exception.FlatException;
import com.sunline.pcm.facility.ParameterFacility;
import com.sunline.pcm.facility.ParameterFetchResponseFacility;
import com.sunline.ppy.dictionary.exception.ProcessException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 利率表数据服务
 * 
 * @author liuky
 * @version [版本号, Jun 18, 2015]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
@SuppressWarnings("serial")
@Controller
@RequestMapping(value = {"interestTableServer", "/interestTableServer"})
@Transactional
public class InterestTableServer /* extends GWTServer */implements InterestTableInter {

	 @Autowired
	    private ParameterFetchResponseFacility responseFacility;
	    
	    @Autowired
	    private ParameterFacility parameterFacility;

	 Logger logger = LoggerFactory.getLogger(this.getClass());
	
	 @Override
	@ResponseBody
    @RequestMapping(value = "/getInterestTableList", method = {RequestMethod.POST})
	public FetchResponse getInterestTableList(@RequestBody(required = false) FetchRequest request) throws FlatException{
		//return parameterFetchResponseFacility.getFetchResponse(request, InterestTable.class);
		    try
	        {
	            return responseFacility.getFetchResponse(request, InterestTable.class);
	        }
	        catch (Exception e)
	        {
	            logger.error(e.getMessage(), e);
	            throw new FlatException("获取利率表代码失败", e);
	        }
	}

	@Override
	@ResponseBody
    @RequestMapping(value = "/addInterestTable", method = {RequestMethod.POST})
	public void addInterestTable(@RequestBody InterestTable interest) throws FlatException {
		// ServerUtil.validateRateCeil(interest.rateCeils);
		//parameterFacility.addNewParameter(interest.intTableId.toString(), interest);
		 try
	        {
	            parameterFacility.addNewParameter(interest.intTableId.toString(), interest);
	        }
		 	catch (ProcessException e)
	        {
	            logger.error(e.getMessage(), e);
	            throw new FlatException(e.getMessage(), e);
	        }
	        catch (Exception e)
	        {
	            logger.error(e.getMessage(), e);
	            throw new FlatException("添加利率表代码失败", e);
	        }
	}

	@Override
	@ResponseBody
    @RequestMapping(value = "/updateInterestTable", method = {RequestMethod.POST})
	public void updateInterestTable(@RequestBody InterestTable interest) throws FlatException {
		
		try
        {
            parameterFacility.updateParameterObject(interest.intTableId.toString(), interest);        
        }
        catch (ProcessException e)
        {
            logger.error(e.getMessage(), e);
            throw new FlatException(e.getMessage(), e);
        }
		catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new FlatException("更新利率表代码失败", e);
        }
	}

	@Override
	@ResponseBody
    @RequestMapping(value = "/getInterestTable", method = {RequestMethod.POST})
	public InterestTable getInterestTable(@RequestBody String key) throws FlatException {
		
		 try
	        {
			    InterestTable inter = null;
			    inter = parameterFacility.getParameterObject(key, InterestTable.class);
	            return inter;
	        }
	        catch (Exception e)
	        {
	            logger.error(e.getMessage(), e);
	            throw new FlatException("获取利率表详细信息代码失败", e);
	        }
	}

	@Override
	@ResponseBody
    @RequestMapping(value = "/deleteInterestTable", method = {RequestMethod.POST})
	public void deleteInterestTable(@RequestBody List<String> keys) throws FlatException {
		 try
	        {
	            for (String key : keys)
	            {
	                parameterFacility.deleteParameterObject(key, InterestTable.class);
	            }
	        }
	        catch (Exception e)
	        {
	            logger.error(e.getMessage(), e);
	            throw new FlatException("删除利率表代码失败", e);
	        }
	    }
	}

