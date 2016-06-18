package com.sunline.ccs.param.ui.server;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sunline.ark.support.meta.MapUtils;
import com.sunline.ccs.param.def.NfControlField;
import com.sunline.ccs.param.def.TxnCd;
import com.sunline.kylin.web.ark.client.data.SelectOptionEntry;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.kylin.web.core.client.exception.FlatException;
import com.sunline.pcm.facility.ParameterFacility;
import com.sunline.pcm.facility.ParameterFetchResponseFacility;
import com.sunline.pcm.param.def.CurrencyCd;
import com.sunline.ppy.dictionary.exception.ProcessException;

@SuppressWarnings("serial")
@Controller
@RequestMapping(value = "/txnCdServer")
@Transactional
public class TxnCdServer{
    	Logger logger = LoggerFactory.getLogger(this.getClass());
    
	@Autowired
	private ParameterFetchResponseFacility parameterFetchResponseFacility;

	@Autowired
	private ParameterFacility parameterFacility;
	@ResponseBody
	@RequestMapping(value = "/getTxnCdList", method = {RequestMethod.POST})
	public FetchResponse getTxnCdList(@RequestBody(required=false) FetchRequest request) {
	    try {
		    logger.info("》》》》》》》》》》》》》》》获取内部交易码列表");
		    return parameterFetchResponseFacility.getFetchResponse(request, TxnCd.class);
		} catch (Exception e) {
		    logger.error(e.getMessage(), e);
		    throw new FlatException("获取内部交易码列表失败", e);
		}
		/*if(request == null || CollectionUtils.isEmpty(request.getFields())) {
			//request = new FetchRequest();
			request.setFields(new ArrayList<String>());
		}
		return parameterFetchResponseFacility.getFetchResponse(request, TxnCd.class);*/
	}
	
	@ResponseBody
	@RequestMapping(value = "/addTxnCd", method = {RequestMethod.POST})
	public void addTxnCd(@RequestBody TxnCd txnCd) throws ProcessException {
	    try
	        {
	            parameterFacility.addNewParameter(txnCd.txnCd, txnCd);
	        }
	        catch (Exception e)
	        {
	            throw new FlatException("添加内部交易码定义失败", e);
	        }
	}
	
	@ResponseBody
	@RequestMapping(value = "/updateTxnCd", method = {RequestMethod.POST})
	public void updateTxnCd(@RequestBody TxnCd txnCd) throws ProcessException {
	    try
	        {
	            parameterFacility.updateParameterObject(txnCd.txnCd, txnCd);
	        }
	        catch (Exception e)
	        {
	            throw new FlatException("更新内部交易码定义失败", e);
	        }
	}
	
	@ResponseBody
	@RequestMapping(value = "/getTxnCd", method = {RequestMethod.POST})
	public TxnCd getTxnCd(@RequestBody String key) {
		return parameterFacility.getParameterObject(key, TxnCd.class);
	}
	
	@ResponseBody
	@RequestMapping(value = "/deleteTxnCd", method = {RequestMethod.POST})
	public void deleteTxnCd(@RequestBody List<String> keys) throws ProcessException {
		for(String key : keys) {
			parameterFacility.deleteParameterObject(key, TxnCd.class);
		}
	}
	
	//获取入账交易代码
  	@ResponseBody
  	@RequestMapping(value = "/getAllTxnCd", method = {RequestMethod.POST })
  	public List<SelectOptionEntry> getAllTxnCd() {
  		List<TxnCd> txnCdList = parameterFacility.getParameterObject(TxnCd.class);
  		List<SelectOptionEntry> uiShowData = new ArrayList<SelectOptionEntry>();
  		if (txnCdList != null && !txnCdList.isEmpty()) {
  			for (TxnCd txnCd : txnCdList) {
  				SelectOptionEntry entry = new SelectOptionEntry(txnCd.txnCd,
  						txnCd.description);
  				uiShowData.add(entry);
  			}
  		}
  		return uiShowData;
  	}
}
