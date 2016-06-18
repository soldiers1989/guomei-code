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

import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.kylin.web.core.client.exception.FlatException;
import com.sunline.pcm.facility.ParameterFacility;
import com.sunline.pcm.facility.ParameterFetchResponseFacility;

@Controller
@RequestMapping(value = {"accountAttributeServer", "/accountAttributeServer"})
public class AccountAtrributeServer {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ParameterFetchResponseFacility responseFacility;

    @Autowired
    private ParameterFacility parameterFacility;

    @ResponseBody
    @RequestMapping(value = "/getAccountAttrList", method = {RequestMethod.POST})
    public FetchResponse getAccountAttrList(@RequestBody(required = false) FetchRequest request) throws FlatException {
	try {
	    return responseFacility.getFetchResponse(request, AccountAttribute.class);
	} catch (Exception e) {
	    logger.error(e.getMessage(), e);
	    throw new FlatException("获取账户参数失败", e);
	}
    }

    @ResponseBody
    @RequestMapping(value = "/addAccountAttr", method = {RequestMethod.POST})
    public void addAccountAttr(@RequestBody AccountAttribute accountAttribute) throws FlatException {
	try {
	    parameterFacility.addNewParameter(accountAttribute.accountAttributeId.toString(), accountAttribute);
	} catch (Exception e) {
	    logger.error(e.getMessage(), e);
	    throw new FlatException("添加账户参数失败", e);
	}
    }

    @ResponseBody
    @RequestMapping(value = "/getAccountAttr", method = {RequestMethod.POST})
    public AccountAttribute getAccountAttr(@RequestBody String key) throws FlatException {
	try {
	    return parameterFacility.getParameterObject(key, AccountAttribute.class);
	} catch (Exception e) {
	    logger.error(e.getMessage(), e);
	    throw new FlatException("获取账户参数失败", e);
	}
    }

    @ResponseBody
    @RequestMapping(value = "/updateAccountAttr", method = {RequestMethod.POST})
    public void updateAccountAttr(@RequestBody AccountAttribute accountAttribute) throws FlatException {
	try {
	    parameterFacility.updateParameterObject(accountAttribute.accountAttributeId.toString(), accountAttribute);
	} catch (Exception e) {
	    logger.error(e.getMessage(), e);
	    throw new FlatException("更新账户参数失败", e);
	}
    }

    @ResponseBody
    @RequestMapping(value = "/deleteAccountAttr", method = {RequestMethod.POST})
    public void deleteAccountAttr(@RequestBody List<String> keys) throws FlatException {
	try {
	    for (String key : keys) {
		parameterFacility.deleteParameterObject(key, AccountAttribute.class);
	    }
	} catch (Exception e) {
	    logger.error(e.getMessage(), e);
	    throw new FlatException("删除账户参数失败", e);
	}
    }

}
