package com.sunline.ccs.param.ui.server;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sunline.ccs.param.def.LoanMerchant;
import com.sunline.ccs.param.def.MerchantGroup;
import com.sunline.kylin.web.ark.client.data.SelectOptionEntry;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.kylin.web.ark.client.validator.ChineseAddressHelper;
import com.sunline.kylin.web.core.client.exception.FlatException;
import com.sunline.pcm.facility.ParameterFacility;
import com.sunline.pcm.facility.ParameterFetchResponseFacility;
import com.sunline.pcm.service.sdk.AddressHelperFacility;

/**
 * 分组商户维护管理Server页面
 * 
 * @author lisy
 * @version [版本号, Jun 23, 2015]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
@Controller
@RequestMapping(value = {"loanMerchantServer", "/loanMerchantServer" })
public class LoanMerchantServer/*
								 * extends GWTServer implements
								 * LoanMerchantInter
								 */{

	Logger logger = LoggerFactory.getLogger(this.getClass());
	private ChineseAddressHelper chineseAddressHelper;
	
	@Autowired
	private ParameterFetchResponseFacility responseFacility;
	@Autowired
	private AddressHelperFacility addressHelperFacility;

	@Autowired
	private ParameterFacility parameterFacility;

	@ResponseBody
	@RequestMapping(value = "/getLoanMerchantList", method = {RequestMethod.POST })
	public FetchResponse getLoanMerchantList(@RequestBody(required = false) FetchRequest request) {
		try {
			return responseFacility.getFetchResponse(request, LoanMerchant.class);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new FlatException("获取国家代码失败", e);
		}
	}

	@ResponseBody
    @RequestMapping(value = "/addLoanMerchant", method = {RequestMethod.POST})
    public void addLoanMerchant(@RequestBody LoanMerchant loanMerchant) throws FlatException
    {
        try
        {
            parameterFacility.addNewParameter(loanMerchant.merId, loanMerchant);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new FlatException("添加分期商户失败", e);
        }
    }

	@ResponseBody
    @RequestMapping(value = "/updateLoanMerchant", method = {RequestMethod.POST})
    public void updateLoanMerchant(@RequestBody LoanMerchant loanMerchant) throws FlatException
    {
        try
        {
            parameterFacility.updateParameterObject(loanMerchant.merId, loanMerchant);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new FlatException("更新分期商户失败", e);
        }
    }

	@ResponseBody
	@RequestMapping(value = "/deleteLoanMerchant", method = {RequestMethod.POST })
	public void deleteLoanMerchant(@RequestBody List<String> keys) throws FlatException {
		try {
			for(String key : keys) {
				parameterFacility.deleteParameterObject(key, LoanMerchant.class);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new FlatException("删除分期商户失败", e);
		}
	}

	@ResponseBody
    @RequestMapping(value = "/getLoanMerchant", method = {RequestMethod.POST})
    public LoanMerchant getLoanMerchant(@RequestBody String merId) throws FlatException
    {
        try
        {
            return parameterFacility.getParameterObject(merId, LoanMerchant.class);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new FlatException("获取分期商户详细信息失败", e);
        }
    }

	@ResponseBody()
	@RequestMapping(value="/getProvince",method={RequestMethod.POST})
	public List<SelectOptionEntry> getProvince() throws FlatException {
		try{
			chineseAddressHelper=new ChineseAddressHelper(addressHelperFacility.loadChineseAddress());
				return chineseAddressHelper.getProvinceSelectOptions();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new FlatException("获得省市区信息失败", e);
		}
	}
	/**
	 * 获得省市区信息
	 */
	@ResponseBody
    @RequestMapping(value = "/loadAddressData", method = {RequestMethod.POST})
	public TreeMap<String, String> loadAddressData() {
		return addressHelperFacility.loadChineseAddress();
	}
	
	@ResponseBody
    @RequestMapping(value = "/getMerGroup", method = {RequestMethod.POST})
	public List<SelectOptionEntry> getMerGroup() {
		List<MerchantGroup> merchantGroupList = parameterFacility.getParameterObject(MerchantGroup.class);
		List<SelectOptionEntry> uiShowData = new ArrayList<SelectOptionEntry>();
		if(merchantGroupList != null && !merchantGroupList.isEmpty()) {
			for(MerchantGroup merchantGroup : merchantGroupList) {
				SelectOptionEntry entry = new SelectOptionEntry(merchantGroup.merGroupId,merchantGroup.merGroupName );
				uiShowData.add(entry);
			}
		}
		return uiShowData;
	}

	@ResponseBody()
	@RequestMapping(value="/getCities",method={RequestMethod.POST})
	public List<SelectOptionEntry> getCities(@RequestBody String province) throws FlatException {
		try{
			return chineseAddressHelper.getCitySelectOptions(province);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new FlatException("获得省市区信息失败", e);
		}
	}
}