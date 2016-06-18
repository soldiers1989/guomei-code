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
import com.sunline.ccs.param.def.AuthMccStateCurrXVerify;
import com.sunline.ccs.param.def.AuthReasonMapping;
import com.sunline.ccs.param.def.InterestTable;
import com.sunline.ccs.param.ui.client.authMccStateCurrXVerify.AuthMccStateCurrXVerifyInter;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.kylin.web.core.client.exception.FlatException;
import com.sunline.pcm.facility.ParameterFacility;
import com.sunline.pcm.facility.ParameterFetchResponseFacility;
import com.sunline.ppy.dictionary.exception.ProcessException;

@SuppressWarnings("serial")
@Controller
@RequestMapping(value = {"authMccStateCurrXVerifyServer", "/authMccStateCurrXVerifyServer"})
@Transactional
public class AuthMccStateCurrXVerifyServer /* extends GWTServer */implements AuthMccStateCurrXVerifyInter {

	@Autowired
	private ParameterFetchResponseFacility parameterFetchResponseFacility;

	@Autowired
	private ParameterFacility parameterFacility;

	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/getAuthMccStateCurrXVerifyList", method = {RequestMethod.POST})
	public FetchResponse getAuthMccStateCurrXVerifyList(@RequestBody(required = false) FetchRequest request) throws   ProcessException {
		try{
		    return parameterFetchResponseFacility.getFetchResponse(request, AuthMccStateCurrXVerify.class);
		}
		catch(Exception e){
			logger.error(e.getMessage(), e);
			throw  new ProcessException("获取国家币种商户交叉控制代码失败", e);
		}
	}

	
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/addAuthMccStateCurrXVerify", method = {RequestMethod.POST})
	public void addAuthMccStateCurrXVerify(@RequestBody AuthMccStateCurrXVerify verify ) throws ProcessException {
		
		 try
	        {
			 parameterFacility.addNewParameter(verify.inputSource.toString() + "|" + verify.mccCode + "|"
						+ verify.countryCode + "|" + verify.transCurrencyCode, verify);
	        }
	        catch (Exception e)
	        {
	            logger.error(e.getMessage(), e);
	            throw  new ProcessException("添加国家币种商户交叉控制代码失败", e);
	        }
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/updateAuthMccStateCurrXVerify", method = {RequestMethod.POST})
	public void updateAuthMccStateCurrXVerify(@RequestBody AuthMccStateCurrXVerify verify) throws ProcessException {
		try
        {
			parameterFacility.updateParameterObject(verify.inputSource.toString() + "|" + verify.mccCode + "|"
					+ verify.countryCode + "|" + verify.transCurrencyCode, verify);			
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
           
            throw  new ProcessException("修改国家币种商户交叉控制代码失败", e);
        }
		
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/getAuthMccStateCurrXVerify", method = {RequestMethod.POST})
	public AuthMccStateCurrXVerify getAuthMccStateCurrXVerify(@RequestBody String key) {
	    try{
		AuthMccStateCurrXVerify authMccStateCurrXVerify = parameterFacility.getParameterObject(key, AuthMccStateCurrXVerify.class);
		
		if(authMccStateCurrXVerify == null)
		{
		    authMccStateCurrXVerify = new AuthMccStateCurrXVerify();
		}
		
		return authMccStateCurrXVerify;
	} 
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw  new ProcessException("获取国家币种商户交叉控制详细信息代码失败", e);
        }
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/deleteAuthMccStateCurrXVerify", method = {RequestMethod.POST})
	public void deleteAuthMccStateCurrXVerify(@RequestBody List<String> keys) throws ProcessException {
		try{
		for(String key : keys) {
			parameterFacility.deleteParameterObject(key, AuthMccStateCurrXVerify.class);
		}
		}catch(Exception e){
			logger.error(e.getMessage(), e);
            throw  new ProcessException("删除国家币种商户交叉控制代码失败", e);
		}
	}
}
