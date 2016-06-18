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
import com.sunline.ccs.param.def.NfControlField;
import com.sunline.ccs.param.def.Program;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.kylin.web.core.client.exception.FlatException;
import com.sunline.pcm.facility.ParameterFacility;
import com.sunline.pcm.facility.ParameterFetchResponseFacility;

/**
 * 非金融控制字段定义服务
 * 
 * @author fanghj
 *
 */

@Controller
@RequestMapping(value = {"nfControlFieldServer", "/nfControlFieldServer"})
public class NfControlFieldServer {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ParameterFetchResponseFacility responseFacility;

    @Autowired
    private ParameterFacility parameterFacility;

    @ResponseBody
    @RequestMapping(value = "/getNfControlFieldList", method = {RequestMethod.POST})
    public FetchResponse getNfControlFieldList(@RequestBody(required = false) FetchRequest request) {
	try {
	    return responseFacility.getFetchResponse(request, NfControlField.class);
	} catch (Exception e) {
	    logger.error(e.getMessage(), e);
	    throw new FlatException("获取非金融控制字段定义列表失败", e);
	}
    }

    @ResponseBody
    @RequestMapping(value = "/addNewNfControlField", method = {RequestMethod.POST})
    public void addNewNfControlField(@RequestBody NfControlField nfControlField) throws FlatException
    {
        try
        {
            parameterFacility.addNewParameter(nfControlField.fieldCode, nfControlField);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new FlatException("添加非金融字段定义失败", e);
        }
    }
   
    @ResponseBody()
    @RequestMapping(value = "/updateNfControlField", method = {RequestMethod.POST})
    public void updateNfControlField(@RequestBody NfControlField nfControlField) throws FlatException {
	try {
	    parameterFacility.updateParameterObject(nfControlField.fieldCode, nfControlField);
	} catch (Exception e) {
	    logger.error(e.getMessage(), e);
	    throw new FlatException("更新非金融控制字段失败", e);
	}

    }

    @ResponseBody()
    @RequestMapping(value = "/getNfControlField", method = {RequestMethod.POST})
    public NfControlField getNfControlField(@RequestBody String key) throws FlatException {

	try {
	    return parameterFacility.getParameterObject(key, NfControlField.class);
	} catch (Exception e) {
	    logger.error(e.getMessage(), e);
	    throw new FlatException("获取非金融字段定义参数详细信息失败", e);
	}
    }

    @ResponseBody()
    @RequestMapping(value = "/deleteNfControlField", method = {RequestMethod.POST})
    public void deleteNfControlField(@RequestBody ArrayList<String> keys) throws FlatException {
	try {
	    for (String key : keys) {
		parameterFacility.deleteParameterObject(key, NfControlField.class);
	    }
	} catch (Exception e) {
	    logger.error(e.getMessage(), e);
	    throw new FlatException("删除非金融字段定义失败", e);
	}
    }
}
