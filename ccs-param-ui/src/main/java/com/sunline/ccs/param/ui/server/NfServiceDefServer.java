package com.sunline.ccs.param.ui.server;

import java.util.ArrayList;
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
import com.sunline.ccs.param.def.NfContrlServMapping;
import com.sunline.ccs.param.def.NfControlField;
import com.sunline.ccs.param.def.NfServiceDef;
import com.sunline.ccs.param.ui.client.ServerDef.NfServiceDefInter;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.kylin.web.core.client.exception.FlatException;
import com.sunline.pcm.facility.ParameterFacility;
import com.sunline.pcm.facility.ParameterFetchResponseFacility;
import com.sunline.ppy.dictionary.exception.ProcessException;


@Controller
@RequestMapping(value= { "nfServiceDefServer", "/nfServiceDefServer"})
public class NfServiceDefServer {
	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
    private ParameterFetchResponseFacility responseFacility;
    
    @Autowired
    private ParameterFacility parameterFacility;
    
    @ResponseBody
    @RequestMapping(value = "/getNfServiceDefList", method = {RequestMethod.POST}) 
    public FetchResponse getNfServiceDefList(@RequestBody(required = false) FetchRequest request)
    {
    	try
        {
    		return responseFacility.getFetchResponse(request, NfServiceDef.class);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new FlatException("获取非金融服务定义列表失败", e);
        }
    }
    @ResponseBody
    @RequestMapping(value = "/addNewNfServiceDef", method = {RequestMethod.POST})
    public void addNewNfServiceDef(@RequestBody NfServiceDef mapping) throws FlatException
    {
        try
        {
//        	NfServiceDef mapping = new NfServiceDef();
//    		MapUtils.updateFieldFromMap(mapping, map);
    		parameterFacility.addNewParameter(mapping.servCode , mapping);
        	
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new FlatException("添加非金融服务定义失败", e);
        }
    }
    
    @ResponseBody()
	@RequestMapping(value="/updateNfServiceDef",method={RequestMethod.POST})
	public void updateNfServiceDef(@RequestBody(required=false)Map<String, String> map) throws FlatException  {

		try{
			NfServiceDef mapping = new NfServiceDef();
			MapUtils.updateFieldFromMap(mapping, map);
			parameterFacility.updateParameterObject(mapping.servCode , mapping);
		} 
		catch (Exception e) 
		{
			logger.error(e.getMessage(), e);
			throw new FlatException("更新非金融服务定义失败", e);
		}
	}
    
	@ResponseBody()
	@RequestMapping(value="/getNfServiceDef",method={RequestMethod.POST})
	public NfServiceDef getNfServceDef(@RequestBody String key) throws FlatException {

		try{
			return parameterFacility.getParameterObject(key, NfServiceDef.class);
		} 
		catch (Exception e) 
		{
			logger.error(e.getMessage(), e);
			throw new FlatException("获取非金融服务定义详细信息失败", e);
		}
	}

	@ResponseBody()
	@RequestMapping(value="/deleteNfServiceDef",method={RequestMethod.POST})
	public void deleteNfServceDef(@RequestBody ArrayList<String> keys) throws FlatException  {
		try{
			for (String key : keys) {
				NfContrlServMapping contrlSvr = parameterFacility.getParameterObject(key, NfContrlServMapping.class);
				if(contrlSvr != null) {
					throw new ProcessException("服务定义[" + contrlSvr.servCode + "]存在于非金融服务控制中，不能删除！请先清理非金融服务控制中的数据");
				} else {
					parameterFacility.deleteParameterObject(key, NfServiceDef.class);
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new FlatException("删除非金融字段定义失败", e);
		}
	}    
}


