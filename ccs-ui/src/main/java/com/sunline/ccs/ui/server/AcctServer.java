package com.sunline.ccs.ui.server;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
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

import com.sunline.ark.support.utils.CodeMarkUtils;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcct;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcctO;
import com.sunline.ccs.ui.server.commons.CPSBusProvide;
import com.sunline.ccs.ui.server.commons.CheckUtil;
import com.sunline.kylin.web.core.client.exception.FlatException;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * 提供账户服务
 * 
 * @author linxc 2015年6月22日
 *
 */
@Controller
@RequestMapping(value="/acctServer")
public class AcctServer {

	@Autowired
	private RCcsAcct rCcsAcct;
	
	@Autowired
	private RCcsAcctO rCcsAcctO;
	
	@Autowired
	private CPSBusProvide cpsBusProvide;

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@ResponseBody()
	@RequestMapping(value="/getAcctList",method={RequestMethod.POST})
	public List<Map<String, Serializable>> getAcctList(@RequestBody String cardNo) throws FlatException {
		logger.info("NF2101:卡号后四位[" + CodeMarkUtils.subCreditCard(cardNo) + "]");
		CheckUtil.checkCardNo(cardNo);
		List<CcsAcct> ccsAcctList = cpsBusProvide.getTmAccountTocardNbr(cardNo);
		List<CcsAcctO> ccsAcctOList = cpsBusProvide.getTmAccountOTocardNbr(cardNo);
		return mergeMaps(ccsAcctList, ccsAcctOList);
	}
	
	@ResponseBody()
	@RequestMapping(value="/getAcctInfo",method={RequestMethod.POST})
	public Map<String, Serializable> getAcctInfo(@RequestBody AccountType acctType, @RequestBody String cardNo) throws ProcessException {
		logger.info("getAcctInfo:账户类型[" + acctType + "],卡号后四位[" + CodeMarkUtils.subCreditCard(cardNo) + "]");
		CcsAcct ccsAcct = cpsBusProvide.getTmAccountTocardNbr(cardNo, acctType);
		CcsAcctO ccsAcctO = cpsBusProvide.getTmAccountOTocardNbr(cardNo, acctType);
		return mergeMap(ccsAcct, ccsAcctO);
	}
	
	@ResponseBody()
	@RequestMapping(value="/getAcctInfoByContrNbr",method={RequestMethod.POST})
	public Map<String, Serializable> getAcctInfoByContrNbr(@RequestBody String contrNbr) throws ProcessException {
		QCcsAcct qCcsAcct = QCcsAcct.ccsAcct;
		QCcsAcctO qCcsAcctO = QCcsAcctO.ccsAcctO;

		CcsAcct ccsAcct = rCcsAcct.findOne(qCcsAcct.contrNbr.eq(contrNbr));
		CcsAcctO ccsAcctO = rCcsAcctO.findOne(qCcsAcctO.contrNbr.eq(contrNbr));
		
		if(ccsAcct==null||ccsAcctO==null) return null;
		return mergeMap(ccsAcct, ccsAcctO);
	}
	/**
	 * 账户信息=CcsAcct+CcsAcctO，组合返回的数据
	 * 
	 * 账户表
	 * 
	 * @param CcsAcctO
	 * @return
	 */
	private Map<String, Serializable> mergeMap(CcsAcct ccsAcct, CcsAcctO ccsAcctO) {
		Map<String, Serializable> mapCcsAcct = ccsAcct.convertToMap();
		mapCcsAcct.putAll(ccsAcctO.convertToMap());
		cpsBusProvide.setOTB(mapCcsAcct);
		return mapCcsAcct;
	}

	/**
	 * 账户信息=CcsAcct+CcsAcctO，组合返回列表
	 * 
	 * @param ccsAcctList
	 * @param ccsAcctOList
	 * @return
	 */
	private List<Map<String, Serializable>> mergeMaps(List<CcsAcct> ccsAcctList, List<CcsAcctO> ccsAcctOList) {
		ArrayList<Map<String, Serializable>> ccsAcctMaps = new ArrayList<Map<String, Serializable>>();
		for(int i = 0; i < ccsAcctList.size(); i++) {
			ccsAcctMaps.add(mergeMap(ccsAcctList.get(i), ccsAcctOList.get(i)));
		}
		return ccsAcctMaps;
	}
}

