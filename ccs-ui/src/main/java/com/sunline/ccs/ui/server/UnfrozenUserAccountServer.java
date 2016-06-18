package com.sunline.ccs.ui.server;


import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.Order;
import com.mysema.query.types.OrderSpecifier;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ark.support.utils.CodeMarkUtils;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsOpOperateLog;
import com.sunline.ccs.infrastructure.shared.model.QCcsOpOperateLog;
import com.sunline.ccs.ui.server.commons.BlockCodeUtil;
import com.sunline.ccs.ui.server.commons.CPSBusProvide;
import com.sunline.ccs.ui.server.commons.CPSConstants;
import com.sunline.ccs.ui.server.commons.CheckUtil;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * 
 */
/**
 * 
 * @see 类名：UnfrozenUserAccountServer
 * @see 描述：账户解除冻结
 *
 * @see 创建日期：   Jun 30, 20157:34:56 PM
 * @author yeyu
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Controller
@RequestMapping(value="/unfrozenUserAccountServer")
public class UnfrozenUserAccountServer {

	private static final long serialVersionUID = 1L;
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private OpeLogUtil opeLogUtil;

	@Autowired
	private CPSBusProvide cpsBusProvide;
	
	@PersistenceContext
	private EntityManager em;
	
	QCcsOpOperateLog qTmOperLog = QCcsOpOperateLog.ccsOpOperateLog;
	
	@Transactional
	@ResponseBody()
	@RequestMapping(value="/cancelPersonAccountFrozen",method={RequestMethod.POST})
	public void cancelPersonAccountFrozen(@RequestBody String cardNo,@RequestBody String accountType,@RequestBody String remark) {
		logger.info("cancelPersonAccountFrozen: cardNo后四位=["+CodeMarkUtils.subCreditCard(cardNo)+"],["+accountType+"]]"+ "remark:[" + remark + "]");
		unlockAccount(cardNo,AccountType.valueOf(accountType));

		CcsCustomer tmCustomer = cpsBusProvide.getTmCustomerToCard(cardNo);

		//记录操作日志
		opeLogUtil.cardholderServiceLog("3302", tmCustomer.getCustId(), cardNo, tmCustomer.getName()+','+accountType, "个人账户解冻,原因："+remark);
	}
  
	/**
	 * 账户解除冻结
	 * @param cardNo
	 * @param acctType
	 * @throws ProcessException
	 */
	@Transactional
	public void unlockAccount(String cardNo, AccountType acctType) {
		logger.info("unlockAccount 卡号后四位:[" + CodeMarkUtils.subCreditCard(cardNo) + "]+账户类型:[" + acctType + "]");
		CheckUtil.checkCardNo(cardNo);
		// 判断TmAccount和TmAccountO表中记录是否有冻结码，如果有清除该冻结码，没有就返回账户非冻结状态，无需解冻。
		CcsAcct tmAccount = cpsBusProvide.getTmAccountTocardNbr(cardNo, acctType);
		CcsAcctO tmAccountO = cpsBusProvide.getTmAccountOTocardNbr(cardNo, acctType);

		if (!BlockCodeUtil.hasBlockCode(tmAccount.getBlockCode(), CPSConstants.BLOCKCODE_T)) {
			logger.error("卡号后四位:[" + CodeMarkUtils.subCreditCard(cardNo) + "]+账户类型:[" + acctType + "]账户非冻结状态，无需解冻。");
			throw new ProcessException("卡号:[" + cardNo + "]+账户类型:[" + acctType + "]账户非冻结状态，无需解冻。");
		} else {
			tmAccount.setBlockCode(BlockCodeUtil
					.removeBlockCode(tmAccount.getBlockCode(), CPSConstants.BLOCKCODE_T));
		}
		if (!BlockCodeUtil.hasBlockCode(tmAccountO.getBlockCode(), CPSConstants.BLOCKCODE_T)) {
			logger.error("卡号后四位:[" + CodeMarkUtils.subCreditCard(cardNo) + "]+账户类型:[" + acctType + "]账户非冻结状态，无需解冻。");
			throw new ProcessException("卡号:[" + cardNo + "]+账户类型:[" + acctType + "]账户非冻结状态，无需解冻。");
		} else {
			tmAccountO.setBlockCode(BlockCodeUtil
					.removeBlockCode(tmAccountO.getBlockCode(), CPSConstants.BLOCKCODE_T));
		}
	}
	
	/**
	 * 
	 * @see 方法名：getOperAuthAndRemark 
	 * @see 描述：根据卡号获取账户冻结和解冻原因
	 * @see 创建日期：Jun 30, 20157:37:46 PM
	 * @author yeyu
	 *  
	 * @param cardNo
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public Map<String, Serializable> getOperAuthAndRemark(@RequestBody String cardNo) {
		JPAQuery query = new JPAQuery(em);
		//根据卡号从日志表中取最新的解冻原因
		List<CcsOpOperateLog> operLogList = query.from(qTmOperLog).where(qTmOperLog.cardNbr.eq(cardNo).
				and(qTmOperLog.serviceCode.eq("3301")).
				and(qTmOperLog.org.eq(OrganizationContextHolder.getCurrentOrg()))).
				orderBy(new OrderSpecifier<Date>(Order.DESC, qTmOperLog.opTime))
				.list(qTmOperLog);
//		if(operLogList.isEmpty()){
//			throw new ProcessException("个人账户冻结，卡号["+cardNo+"]未进行冻结操作");
//		}
		
		JPAQuery query1 = new JPAQuery(em);
		//根据卡号从日志表中取最新的解冻原因
		List<CcsOpOperateLog> thawedList = query1.from(qTmOperLog).where(qTmOperLog.cardNbr.eq(cardNo).
				and(qTmOperLog.serviceCode.eq("3302")).
				and(qTmOperLog.org.eq(OrganizationContextHolder.getCurrentOrg()))).
				orderBy(new OrderSpecifier<Date>(Order.DESC, qTmOperLog.opTime))
				.list(qTmOperLog);
		
		Map<String, Serializable> map = new LinkedHashMap<String, Serializable>();
		
		String[] str = operLogList.get(0).getRelatedDesc().split("：");
		map.put("blockedReason", str[1].toString());
		if(thawedList.isEmpty()){
			map.put("remark", null);
		}else{
			String [] thawed = thawedList.get(0).getRelatedDesc().split("：");
			map.put("remark", thawed[1].toString());
		}
		return map;
	}
	
}
