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

import com.sunline.ccs.param.def.Program;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.kylin.web.core.client.exception.FlatException;
import com.sunline.pcm.facility.ParameterFacility;
import com.sunline.pcm.facility.ParameterFetchResponseFacility;

@Controller
@RequestMapping(value = {"programServer", "/programServer"})
public class ProgramServer
{
    Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    private ParameterFetchResponseFacility responseFacility;
    
    @Autowired
    private ParameterFacility parameterFacility;
    
    @ResponseBody
    @RequestMapping(value = "/getProgramList", method = {RequestMethod.POST})
    public FetchResponse getProgramList(@RequestBody(required = false) FetchRequest request)
    {
        try
        {
            return responseFacility.getFetchResponse(request,Program.class);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new FlatException("获取分期活动失败", e);
        }
    }
    
    @ResponseBody
    @RequestMapping(value = "/addProgram", method = {RequestMethod.POST})
    public void addProgram(@RequestBody Program program) throws FlatException
    {
        try
        {
            parameterFacility.addNewParameter(program.programId, program);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new FlatException("添加分期活动失败", e);
        }
    }
    
    @ResponseBody
    @RequestMapping(value = "/updateProgram", method = {RequestMethod.POST})
    public void updateProgram(@RequestBody Program program) throws FlatException
    {
        try
        {
            parameterFacility.updateParameterObject(program.programId, program);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new FlatException("更新分期活动失败", e);
        }
    }
    
    @ResponseBody
    @RequestMapping(value = "/getProgram", method = {RequestMethod.POST})
    public Program getProgram(@RequestBody String programId) throws FlatException
    {
        try
        {
            return parameterFacility.getParameterObject(programId, Program.class);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new FlatException("获取分期活动详细信息失败", e);
        }
    }
    
    @ResponseBody
    @RequestMapping(value = "/deleteProgram", method = {RequestMethod.POST})
    public void deleteProgram(@RequestBody List<String> keys) throws FlatException
    {
        try
        {
            for (String key : keys)
            {
                parameterFacility.deleteParameterObject(key, Program.class);
            }
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new FlatException("删除分期活动失败", e);
        }
    }
   
}