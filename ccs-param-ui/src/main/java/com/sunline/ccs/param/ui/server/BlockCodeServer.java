package com.sunline.ccs.param.ui.server;

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
import com.sunline.ccs.param.def.BlockCode;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.kylin.web.core.client.exception.FlatException;
import com.sunline.pcm.facility.ParameterFacility;
import com.sunline.pcm.facility.ParameterFetchResponseFacility;


@Controller


@RequestMapping(value={"/blockCodeServer" , "blcokCodeServer"})
@Transactional
public class BlockCodeServer 
{
    Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    private ParameterFetchResponseFacility responseFacility;
    
    @Autowired
    private ParameterFacility parameterFacility;
    
    @ResponseBody
    @RequestMapping(value = "/getBlockCodeList", method = {RequestMethod.POST})
    public FetchResponse getBlockCodeList(@RequestBody FetchRequest request)
    {
        try
        {
            return responseFacility.getFetchResponse(request, BlockCode.class);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new FlatException("获取锁定码列表失败", e);
        }
    }
    
    @ResponseBody
    @RequestMapping(value = "/addBlockCode", method = {RequestMethod.POST})
    public void addBlcokCode(@RequestBody BlockCode blockCode) throws FlatException
    {
        try
        {
//        	BlockCode blockCode = new BlockCode();
//    		MapUtils.updateFieldFromMap(blockCode, map);
    		parameterFacility.addNewParameter(blockCode.blockCode, blockCode);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new FlatException("添加锁定码失败", e);
        }
    }
    
    @ResponseBody
    @RequestMapping(value = "/updateBlockCode", method = {RequestMethod.POST})
    public void updateBlockCode(@RequestBody BlockCode blcokcode) throws FlatException
    {
        try
        {
            parameterFacility.updateParameterObject(blcokcode.blockCode, blcokcode);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new FlatException("更新锁定码失败", e);
        }
    }
    
    @ResponseBody
    @RequestMapping(value = "/getBlockCode", method = {RequestMethod.POST})
    public BlockCode getBlockCode(@RequestBody String blockcode) throws FlatException
    {
        try
        {
            return parameterFacility.getParameterObject(blockcode, BlockCode.class);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new FlatException("获取锁定码详细信息失败", e);
        }
    }
    
    @ResponseBody
    @RequestMapping(value = "/deleteBlockCode", method = {RequestMethod.POST})
    public void deleteBlockCode(@RequestBody List<String> keys) throws FlatException
    {
        try
        {
            for (String key : keys)
            {
                parameterFacility.deleteParameterObject(key, BlockCode.class);
            }
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new FlatException("删除锁定码失败", e);
        }
    }
    
}

