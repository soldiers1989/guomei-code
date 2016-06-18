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
/**
 * 
 * @see 类名：FrozenUserAccountServer
 * @see 描述：个人账户冻结
 *
 * @see 创建日期：   Jun 23, 201510:53:55 AM
 * @author yeyu
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Controller
@RequestMapping(value="/frozenUserAccountServer")
public class FrozenUserAccountServer {


	private static final long serialVersionUID = 1L;
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private OpeLogUtil opeLogUtil;

	@Autowired
	private CPSBusProvide cpsBusProvide;
	
	@PersistenceContext
	private EntityManager em;
	
	/**
	 * 
	 * @see 方法名：personAccountFrozen 
	 * @see 描述：用户账户冻结
	 * @see 创建日期：Jun 23, 201511:03:24 AM
	 * @author Liming.Feng
	 *  
	 * @param cardNo
	 * @param accountType
	 * @param remark
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	@Transactional
	@ResponseBody()
	@RequestMapping(value="/personAccountFrozen",method={RequestMethod.POST})
	public void personAccountFrozen(@RequestBody String cardNo,@RequestBody String accountType, @RequestBody String remark) {
		
		logger.info("personAccountFrozen: cardNo后四位=["+CodeMarkUtils.subCreditCard(cardNo)+"],["+accountType+"]," + "remark:[" + remark + "]");
		
		lockAccount(cardNo, AccountType.valueOf(accountType));
		
		CcsCustomer tmCustomer = cpsBusProvide.getTmCustomerToCard(cardNo);

		//记录操作日志
		opeLogUtil.cardholderServiceLog("3301", tmCustomer.getCustId(), cardNo, tmCustomer.getName()+','+accountType, "个人账户冻结,原因："+remark);
	}

	/**
	 * 
	 * @see 方法名：lockAccount 
	 * @see 描述：账户冻结
	 * @see 创建日期：Jun 23, 201511:03:02 AM
	 * @author Liming.Feng
	 *  
	 * @param cardNo
	 * @param acctType
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	@Transactional
	private void lockAccount(String cardNo, AccountType acctType)  {
		logger.info("lockAccount 卡号后四位:[" + CodeMarkUtils.subCreditCard(cardNo) + "]+账户类型:[" + acctType + "]");
		CheckUtil.checkCardNo(cardNo);
		// 判断TmAccount 和TmAccountO表是否有冻结码，如果有返回账户已冻结，如果没有则设置冻结锁定码
		CcsAcct tmAccount = cpsBusProvide.getTmAccountTocardNbr(cardNo, acctType);
		CcsAcctO tmAccountO = cpsBusProvide.getTmAccountOTocardNbr(cardNo, acctType);
		tmAccount.setBlockCode(BlockCodeUtil.addBlockCode(tmAccount.getBlockCode(), CPSConstants.BLOCKCODE_T));
		tmAccountO.setBlockCode(BlockCodeUtil
					.addBlockCode(tmAccountO.getBlockCode(), CPSConstants.BLOCKCODE_T));
		/*if (BlockCodeUtil.hasBlockCode(tmAccount.getBlockCode(), CPSConstants.BLOCKCODE_T)) {
			logger.error("卡号后四位:[" + CodeMarkUtils.subCreditCard(cardNo) + "]+账户类型:[" + acctType + "]账户已经冻结");
			
		} else {
			tmAccount.setBlockCode(BlockCodeUtil.addBlockCode(tmAccount.getBlockCode(), CPSConstants.BLOCKCODE_T));
		}
		if (BlockCodeUtil.hasBlockCode(tmAccountO.getBlockCode(), CPSConstants.BLOCKCODE_T)) {
			logger.error("卡号后四位:[" + CodeMarkUtils.subCreditCard(cardNo) + "]+账户类型:[" + acctType + "]账户已经冻结");
			
		} else {
			tmAccountO.setBlockCode(BlockCodeUtil
					.addBlockCode(tmAccountO.getBlockCode(), CPSConstants.BLOCKCODE_T));
		}*/
		
	}
	
	/**
	 * 
	 * @see 方法名：getOpeLogAndRemark 
	 * @see 描述：操作日志表中查询冻结原因
	 * @see 创建日期：Jun 23, 201511:02:49 AM
	 * @author Liming.Feng
	 *  
	 * @param cardNo
	 * @param servCode
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	@ResponseBody()
	@RequestMapping(value="/getOpeLogAndRemark",method={RequestMethod.POST})
	public Map getOpeLogAndRemark(@RequestBody String cardNo,@RequestBody String servCode)  {
		QCcsOpOperateLog qTmOperLog = QCcsOpOperateLog.ccsOpOperateLog;
		JPAQuery query = new JPAQuery(em);
		List<CcsOpOperateLog> operLogList = query.from(qTmOperLog).where(qTmOperLog.cardNbr.eq(cardNo).
				and(qTmOperLog.serviceCode.eq(servCode)).
				and(qTmOperLog.org.eq(OrganizationContextHolder.getCurrentOrg()))).
				orderBy(new OrderSpecifier<Date>(Order.DESC, qTmOperLog.opTime))
				.list(qTmOperLog);
		Map<String, Serializable> map = new LinkedHashMap<String, Serializable>();
		if(operLogList.isEmpty()){
			map.put("remark", null);
		}else{
			String[] str = operLogList.get(0).getRelatedDesc().split("：");
			map.put("remark", str[1].toString());
		}
		return map;
	}
	
}
