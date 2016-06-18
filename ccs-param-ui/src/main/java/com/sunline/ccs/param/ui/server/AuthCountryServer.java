package com.sunline.ccs.param.ui.server;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sunline.ccs.param.def.CountryCtrl;
import com.sunline.kylin.web.ark.client.data.SelectOptionEntry;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.kylin.web.core.client.exception.FlatException;
import com.sunline.pcm.facility.ParameterFacility;
import com.sunline.pcm.facility.ParameterFetchResponseFacility;
import com.sunline.pcm.param.def.CountryCd;

@Controller
@RequestMapping(value = {"authCountryServer", "/authCountryServer"})
public class AuthCountryServer  {
	
	
	 Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private ParameterFetchResponseFacility responseFacility;

	@Autowired
	private ParameterFacility parameterFacility;

	 @ResponseBody
	    @RequestMapping(value = "/updateAuthCountry", method = {RequestMethod.POST})
	 public void updateCountryCtrl(@RequestBody CountryCtrl countryCtrl) throws FlatException
	    {
	        try
	        {
	            parameterFacility.updateParameterObject(countryCtrl.countryCode, countryCtrl);
	        }
	        catch (Exception e)
	        {
	            logger.error(e.getMessage(), e);
	            throw new FlatException("更新国家代码控制失败", e);
	        }
	    }

	 @ResponseBody
	    @RequestMapping(value = "/getAuthCountry", method = {RequestMethod.POST})
	 public CountryCtrl getCountryCtrl(@RequestBody String countryCtrl) throws FlatException
	    {
	        try
	        {
	            return parameterFacility.getParameterObject(countryCtrl, CountryCtrl.class);
	        }
	        catch (Exception e)
	        {
	            logger.error(e.getMessage(), e);
	            throw new FlatException("获取国家代码控制详细信息失败", e);
	        }
	    }

	 @ResponseBody
	    @RequestMapping(value = "/addAuthCountry", method = {RequestMethod.POST})

	 public void addAuthCountry(@RequestBody CountryCtrl countryCtrl) throws FlatException
	    {
	        try
	        {
	            parameterFacility.addNewParameter(countryCtrl.countryCode, countryCtrl);
	        }
	        catch (Exception e)
	        {
	            logger.error(e.getMessage(), e);
	            throw new FlatException("添加国家代码控制失败", e);
	        }
	    }


	 @ResponseBody
	    @RequestMapping(value = "/getAuthCountryList", method = {RequestMethod.POST})
	 public FetchResponse getAuthCountryCdList(@RequestBody(required = false) FetchRequest request)
	    {
	        try
	        {
	            return responseFacility.getFetchResponse(request, CountryCtrl.class);
	        }
	        catch (Exception e)
	        {
	            logger.error(e.getMessage(), e);
	            throw new FlatException("获取国家代码授权失败", e);
	        }
	    }
	 
	@ResponseBody
	@RequestMapping(value = "/deleteAuthCountry", method = {RequestMethod.POST })
	public void deleteCountryCd(@RequestBody List<String> keys) throws FlatException {
		try {
			for(String key : keys) {
				parameterFacility.deleteParameterObject(key, CountryCtrl.class);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new FlatException("删除国家代码控制失败", e);
		}
	}
	
	@ResponseBody
	@RequestMapping(value = "/getCountryCd", method = {RequestMethod.POST })
	public List<SelectOptionEntry> getCountryCd() {
		List<CountryCd> countryCdList = parameterFacility.getParameterObject(CountryCd.class);
		List<SelectOptionEntry> uiShowData = new ArrayList<SelectOptionEntry>();
		if(countryCdList != null && !countryCdList.isEmpty()) {
			for(CountryCd countryCd : countryCdList) {
				SelectOptionEntry entry = new SelectOptionEntry(countryCd.countryCd,countryCd.description );
				uiShowData.add(entry);
			}
		}
		return uiShowData;
	}
}

