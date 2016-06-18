package com.sunline.ccs.param.ui.server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sunline.ccs.param.def.BlockCode;
import com.sunline.ccs.param.def.NfContrlServMapping;
import com.sunline.ccs.param.def.NfControlField;
import com.sunline.ccs.param.def.NfServiceDef;
import com.sunline.kylin.web.ark.client.data.SelectOptionEntry;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.kylin.web.core.client.exception.FlatException;
import com.sunline.pcm.facility.ParameterFacility;
import com.sunline.pcm.facility.ParameterFetchResponseFacility;

/**
 * 非金融服务控制约束服务
 * 
 * @author guxiaoyu
 *
 */
@Controller
@RequestMapping(value = {"nfContrlSvrMappingServer ", "/nfContrlSvrMappingServer "})
public class NfContrlSvrMappingServer
{

	Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private ParameterFetchResponseFacility responseFacility;

	@Autowired
	private ParameterFacility parameterFacility;

	@ResponseBody
	@RequestMapping(value = "/updateNfContrlSvrMapping", method = {RequestMethod.POST})
	public void updateNfContrlServMapping(@RequestBody NfContrlServMapping nfContrlSvrMapping) throws FlatException
	{
		try
		{
			parameterFacility.updateParameterObject(nfContrlSvrMapping.servCode, nfContrlSvrMapping);

		} catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			throw new FlatException("更新非金融服务控制约束失败", e);
		}
	}

	@ResponseBody
	@RequestMapping(value = "/getNfContrlSvrMapping", method = {RequestMethod.POST})
	public NfContrlServMapping getNfContrlSvrMapping(@RequestBody String nfContrlSvrMapping) throws FlatException
	{
		try
		{
			return parameterFacility.getParameterObject(nfContrlSvrMapping, NfContrlServMapping.class);

		} catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			throw new FlatException("获取非金融服务控制约束失败", e);
		}
	}

	// 获取描述
	@ResponseBody
	@RequestMapping(value = "/getNfContrlSvrMappingDec", method = {RequestMethod.POST})
	public String getNfContrlSvrMappingDec(@RequestBody String key) throws FlatException
	{
		try
		{
			List<NfServiceDef> nfServiceDef = parameterFacility.getParameterObject(NfServiceDef.class);

			for (int i = 0; i < nfServiceDef.size(); i++)
			{

				if (nfServiceDef.get(i).servCode.equals(key))
				{
					return nfServiceDef.get(i).servCode + "-" + nfServiceDef.get(i).servDesc;
				}
			}
		} catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			throw new FlatException("获取描述失败", e);
		}
		return "";
	}

	@ResponseBody
	@RequestMapping(value = "/addNfContrlSvrMapping", method = {RequestMethod.POST})
	public void addNfContrlServMapping(@RequestBody NfContrlServMapping nfContrlServMapping) throws FlatException
	{
		try
		{
			parameterFacility.addNewParameter(nfContrlServMapping.servCode, nfContrlServMapping);
		} catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			throw new FlatException("添加非金融服务控制约束失败", e);
		}
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@ResponseBody
	@RequestMapping(value = "/getNfContrlSvrMappingList", method = {RequestMethod.POST})
	public FetchResponse getNfContrlSvrMappingList(@RequestBody(required = false) FetchRequest request)
	{
		FetchResponse response;
		List<Map<String, Serializable>> list = new ArrayList<Map<String, Serializable>>();

		try
		{
			response = responseFacility.getFetchResponse(request, NfContrlServMapping.class);
			List<NfContrlServMapping> contrlSvrMapping = response.getRows();
			for (NfContrlServMapping nS : contrlSvrMapping)
			{
				NfServiceDef serviceDef = parameterFacility.getParameterObject(nS.servCode, NfServiceDef.class);
				if (serviceDef != null)
				{
					Map<String, Serializable> returnMap = new HashMap<String, Serializable>();
					returnMap.put("servCode", serviceDef.servCode);
					returnMap.put("servDesc", serviceDef.servDesc);
					returnMap.put("memo", nS.memo);
					list.add(returnMap);
				}
			}
			response.setRows(list);
			return response;
		} catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			throw new FlatException("获取非金融服务控制约束失败", e);
		}
	}

	@ResponseBody
	@RequestMapping(value = "/deleteNfContrlSvrMapping", method = {RequestMethod.POST})
	public void deleteCurrencyCtrl(@RequestBody List<String> keys) throws FlatException
	{
		try
		{
			for (String key : keys)
			{
				parameterFacility.deleteParameterObject(key, NfContrlServMapping.class);
			}
		} catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			throw new FlatException("删除非金融服务控制约束失败", e);
		}
	}

	@ResponseBody
	@RequestMapping(value = "/getServCode", method = {RequestMethod.POST})
	public List<SelectOptionEntry> getNfServiceDef()
	{
		List<NfServiceDef> nfServiceDefList = parameterFacility.getParameterObject(NfServiceDef.class);
		List<NfContrlServMapping> nfContrlServList = parameterFacility.getParameterObject(NfContrlServMapping.class);

		List<String> servCodeList = new ArrayList<String>();

		if (nfContrlServList != null && !nfContrlServList.isEmpty())
		{
			for (NfContrlServMapping nfContrlServMapping : nfContrlServList)
			{
				servCodeList.add(nfContrlServMapping.servCode);
			}
		}

		List<SelectOptionEntry> uiShowData = new ArrayList<SelectOptionEntry>();

		if (nfServiceDefList != null && !nfServiceDefList.isEmpty())
		{
			for (NfServiceDef nfServiceDef : nfServiceDefList)
			{
				if (!servCodeList.contains(nfServiceDef.servCode))
				{
					SelectOptionEntry entry = new SelectOptionEntry(nfServiceDef.servCode, nfServiceDef.servDesc);
					uiShowData.add(entry);
				}
			}
		}

		return uiShowData;
	}

	// 获取锁定码数据
	@ResponseBody
	@RequestMapping(value = "/getBlockCode", method = {RequestMethod.POST})
	public LinkedHashMap<String, String> getBlockCode()
	{
		List<BlockCode> blockCodeList = parameterFacility.getParameterObject(BlockCode.class);
		LinkedHashMap<String, String> blockCodeMap = new LinkedHashMap<String, String>();
		if (blockCodeList != null && !blockCodeList.isEmpty())
		{
			for (BlockCode blockCode : blockCodeList)
			{
				blockCodeMap.put(blockCode.blockCode, blockCode.blockCode + " - " + blockCode.description);
			}
		}
		return blockCodeMap;
	}

	// 获取其他状态数据
	@ResponseBody
	@RequestMapping(value = "/getNfControlField", method = {RequestMethod.POST})
	public LinkedHashMap<String, String> getNfControlField()
	{
		List<NfControlField> nfControlFieldList = parameterFacility.getParameterObject(NfControlField.class);
		LinkedHashMap<String, String> nfControlFieldMap = new LinkedHashMap<String, String>();
		if (nfControlFieldList != null && !nfControlFieldList.isEmpty())
		{
			for (NfControlField nfControlField : nfControlFieldList)
			{
				nfControlFieldMap.put(nfControlField.fieldCode, nfControlField.fieldCode + " - "
						+ nfControlField.fieldName);
			}
		}
		return nfControlFieldMap;
	}
}
