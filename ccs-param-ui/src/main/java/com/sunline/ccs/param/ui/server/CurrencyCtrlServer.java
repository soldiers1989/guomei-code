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

import com.sunline.ccs.param.def.CurrencyCtrl;
import com.sunline.kylin.web.ark.client.data.SelectOptionEntry;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.kylin.web.core.client.exception.FlatException;
import com.sunline.pcm.facility.ParameterFacility;
import com.sunline.pcm.facility.ParameterFetchResponseFacility;
import com.sunline.pcm.param.def.CurrencyCd;

@Controller
@RequestMapping(value = {"currencyCtrlServer ", "/currencyCtrlServer "})
public class CurrencyCtrlServer  {
	
	
	 Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private ParameterFetchResponseFacility responseFacility;

	@Autowired
	private ParameterFacility parameterFacility;

	 @ResponseBody
	    @RequestMapping(value = "/updateCurrencyCtrl", method = {RequestMethod.POST})
	 public void updatecurrencyCtrl(@RequestBody CurrencyCtrl currencyCtrl) throws FlatException
	    {
	        try
	        {
	            parameterFacility.updateParameterObject(currencyCtrl.currencyCd, currencyCtrl);
	        }
	        catch (Exception e)
	        {
	            logger.error(e.getMessage(), e);
	            throw new FlatException("更新币种代码控制失败", e);
	        }
	    }

	 @ResponseBody
	    @RequestMapping(value = "/getCurrencyCtrl", method = {RequestMethod.POST})
	 public CurrencyCtrl getCurrencyCtrl(@RequestBody String currencyCtrl) throws FlatException
	    {
	        try
	        {
	            return parameterFacility.getParameterObject(currencyCtrl, CurrencyCtrl.class);
	        }
	        catch (Exception e)
	        {
	            logger.error(e.getMessage(), e);
	            throw new FlatException("获取币种代码控制详细信息失败", e);
	        }
	    }

	 @ResponseBody
	    @RequestMapping(value = "/addCurrencyCtrl", method = {RequestMethod.POST})

	 public void addCurrencyCtrl(@RequestBody CurrencyCtrl currencyCtrl) throws FlatException
	    {
	        try
	        {
	            parameterFacility.addNewParameter(currencyCtrl.currencyCd, currencyCtrl);
	        }
	        catch (Exception e)
	        {
	            logger.error(e.getMessage(), e);
	            throw new FlatException("添加币种代码控制失败", e);
	        }
	    }


	 @ResponseBody
	    @RequestMapping(value = "/getCurrencyCtrlList", method = {RequestMethod.POST})
	 public FetchResponse getCurrencyCtrlList(@RequestBody(required = false) FetchRequest request)
	    {
	        try
	        {
	            return responseFacility.getFetchResponse(request,CurrencyCtrl.class);
	        }
	        catch (Exception e)
	        {
	            logger.error(e.getMessage(), e);
	            throw new FlatException("获取币种代码授权失败", e);
	        }
	    }
	 
	@ResponseBody
	@RequestMapping(value = "/deleteCurrencyCtrl", method = {RequestMethod.POST })
	public void deleteCurrencyCtrl(@RequestBody List<String> keys) throws FlatException {
		try {
			for(String key : keys) {
				parameterFacility.deleteParameterObject(key, CurrencyCtrl.class);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new FlatException("删除币种代码控制失败", e);
		}
	}
	
	@ResponseBody
	@RequestMapping(value = "/getCurrencyCd", method = {RequestMethod.POST })
	public List<SelectOptionEntry> getCurrencyCd() {
		List<CurrencyCd> currencyCdList = parameterFacility.getParameterObject(CurrencyCd.class);
		List<SelectOptionEntry> uiShowData = new ArrayList<SelectOptionEntry>();
		if(currencyCdList != null && !currencyCdList.isEmpty()) {
			for(CurrencyCd currencyCd : currencyCdList) {
				SelectOptionEntry entry = new SelectOptionEntry(currencyCd.currencyCd,currencyCd.description );
				uiShowData.add(entry);
			}
		}
		return uiShowData;
	}
}

