package com.sunline.ccs.ui.server;

import java.io.Serializable;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ark.support.utils.CodeMarkUtils;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcct;
import com.sunline.ccs.infrastructure.server.repos.RCcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomerCrlmt;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsCustomer;
import com.sunline.ccs.ui.server.commons.CPSBusProvide;
import com.sunline.ccs.ui.server.commons.CheckUtil;
import com.sunline.kylin.web.core.client.exception.FlatException;
import com.sunline.ppy.dictionary.enums.IdType;

/**
 * 提供客户信息服务
 * 
 * @author fanghj
 *
 */
@SuppressWarnings("all")
@Controller
@RequestMapping(value = "/custServer")
public class CustServer {

	@Autowired
	private CPSBusProvide cpsBusProvide;

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private QCcsCustomer qTmCustomer = QCcsCustomer.ccsCustomer;

	@Autowired
	private RCcsCustomer rCustomer;
	
	@Autowired
	private RCcsAcct rCcsAcct;

	public Map<String, Serializable> getCustInfo(Integer custId) throws FlatException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * 合同号获取客户信息
	 * @return
	 * @throws FlatException
	 */
	@ResponseBody()
	@RequestMapping(value = "/getCustByContrNbr", method = {RequestMethod.POST })
	public Map<String, Serializable> getCustByContrNbr(@RequestBody String contrNbr) throws FlatException {
		if(contrNbr==null||contrNbr.length()==0)
			throw new FlatException("合同号不允许为空");
		QCcsAcct qCcsAcct = QCcsAcct.ccsAcct;
		CcsAcct acct = rCcsAcct.findOne(qCcsAcct.contrNbr.eq(contrNbr));
		Long custId = null;
		if(acct!=null){
			custId = acct.getCustId();
		}else{
			throw new FlatException("查询不到客户信息，合同号["+contrNbr+"]");
		}
		CcsCustomer tmCustomer = rCustomer.findOne(custId);
		return genTmCustomer(tmCustomer);
	}
	
	@ResponseBody()
	@RequestMapping(value = "/getCustInfo", method = {RequestMethod.POST })
	public Map<String, Serializable> getCustInfo(@RequestBody IdType idType, @RequestBody String idNo)
			throws FlatException {
		logger.info("getCustInfo:证件类型[" + idType + "],证件号码[" + CodeMarkUtils.markIDCard(idNo) + "]");
		CheckUtil.checkId(idType, idNo);
		// 根据证件类型，证件号码查询TM_CUSTOMER主表
		CcsCustomer tmCustomer = rCustomer.findOne(qTmCustomer.idType.eq(idType).and(
				qTmCustomer.idNo.eq(idNo).and(qTmCustomer.org.eq(OrganizationContextHolder.getCurrentOrg()))));
		CheckUtil.rejectNull(tmCustomer, "证件类型[" + idType + "],证件号码[" + idNo + "]查询不到对应的客户信息");
		return genTmCustomer(tmCustomer);
	}

	/**
	 * 客户信息=CcsCustomer+tmCustLimt，组合返回的数据
	 * 
	 * @param tmCustomer 客户表 客户限额表
	 * @return
	 */
	private Map<String, Serializable> mergeMap(CcsCustomer tmCustomer, CcsCustomerCrlmt tmCustLimtO) {
		Map<String, Serializable> mapTmCustor = tmCustomer.convertToMap();
		if(!CheckUtil.isEmpty(tmCustLimtO)) {
			mapTmCustor.putAll(tmCustLimtO.convertToMap());
		}
		cpsBusProvide.setOTB(mapTmCustor);
		return mapTmCustor;
	}

	/**
	 * 生成 客户信息=CcsCustomer+tmCustLimtO
	 * 
	 * @param tmCustomer
	 * @return
	 */
	private Map<String, Serializable> genTmCustomer(CcsCustomer tmCustomer) {
		if(tmCustomer.getCustLmtId() == null) {
			return tmCustomer.convertToMap();
		} else {
			return mergeMap(tmCustomer, cpsBusProvide.getTmCustLimitOToCustLimitId(tmCustomer.getCustLmtId()));
		}
	}

	@ResponseBody()
	@RequestMapping(value = "/getCustInfoByCardNo", method = {RequestMethod.POST })
	public Map<String, Serializable> getCustInfoByCardNo(@RequestBody String cardNo) throws FlatException {
		logger.info("getCustInfoByCardNo:卡号后四位[" + CodeMarkUtils.subCreditCard(cardNo) + "]");
		CheckUtil.checkCardNo(cardNo);
		CcsCustomer tmCustomer = cpsBusProvide.getTmCustomerToCard(cardNo);
		CheckUtil.rejectNull(tmCustomer, "卡号[" + cardNo + "]查询不到对应的客户信息");
		return genTmCustomer(tmCustomer);
	}
}
