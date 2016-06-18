package com.sunline.ccs.batch.cc5500;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ark.support.utils.CodeMarkUtils;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcct;
import com.sunline.ccs.infrastructure.server.repos.RCcsAddress;
import com.sunline.ccs.infrastructure.server.repos.RCcsCard;
import com.sunline.ccs.infrastructure.server.repos.RCcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctKey;
import com.sunline.ccs.infrastructure.shared.model.CcsAddress;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsCardExpList;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.QCcsAddress;
import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.pcm.param.def.Product;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.CardFetchMethod;
import com.sunline.ppy.dictionary.enums.RenewInd;
import com.sunline.ppy.dictionary.enums.RenewResult;
import com.sunline.ppy.dictionary.exchange.ExpiryChangeFileItem;
import com.sunline.ppy.dictionary.report.ccs.RenewRptItem;
//import com.sunline.smsd.service.sdk.RenewMsgItem;


/**
 * @see 类名：P5511ManualRenewal
 * @see 描述： 审核通过的换卡记录处理
 *
 * @see 创建日期：   2015-6-23下午7:58:43
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class P5511ManualRenewal implements ItemProcessor<CcsCardExpList, S5500Renewal> {

	/**
	 * 系统日志
	 */
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	
	/**
	 * 卡档
	 */
	@Autowired
	RCcsCard rTmCard;
	
	/**
	 * 账户档
	 */
	@Autowired
	RCcsAcct rTmAccount;
	
	/**
	 * 客户档
	 */
	@Autowired
	RCcsCustomer rTmCustomer;
	
	/**
	 * 名址表
	 */
	@Autowired
	RCcsAddress rTmAddress;
	
	/**
	 * 获取参数类
	 */
	@Autowired
	private UnifiedParameterFacility parameterFacility;
	
	@Autowired
	private BatchStatusFacility batchFacility;
	
	@Override
	public S5500Renewal process(CcsCardExpList item) throws Exception {
		
		S5500Renewal info = new S5500Renewal();
		
		try {
			//范型
			
			//获取卡片
			CcsCard card = rTmCard.findOne(item.getLogicCardNbr());
			if(card == null)
			{
				info.setRenewRptItem(makeRenewRptItem(item, RenewResult.R01, RenewInd.W, null, null, null, null));
				return info;
			}
			
			//取机构号并设置上下文
			OrganizationContextHolder.setCurrentOrg(card.getOrg());
			
			Product product = parameterFacility.loadParameter(card.getProductCd(), Product.class);
			
			ProductCredit productCredit = parameterFacility.loadParameter(card.getProductCd(), ProductCredit.class);
			AccountAttribute acctAttr = parameterFacility.loadParameter(String.valueOf(productCredit.accountAttributeId), AccountAttribute.class);
			AccountAttribute dualAcctAttr = null;
			if (productCredit.dualAccountAttributeId != null)
			{
				dualAcctAttr = parameterFacility.loadParameter(String.valueOf(productCredit.dualAccountAttributeId), AccountAttribute.class);
			}
			
			//获取本币账户
			CcsAcctKey key = new CcsAcctKey();
			key.setAcctNbr(card.getAcctNbr());
			key.setAcctType(acctAttr.accountType);
			CcsAcct acct = rTmAccount.findOne(key);
			if(acct == null)
			{
				throw new RuntimeException(RenewResult.R02.getDesc());
			}
			//外币账户
			CcsAcct dualAcct = null;
			if (dualAcctAttr != null)
			{
				CcsAcctKey dualKey = new CcsAcctKey();
				dualKey.setAcctNbr(card.getAcctNbr());
				dualKey.setAcctType(dualAcctAttr.accountType);
				
				dualAcct = rTmAccount.findOne(dualKey);
			}
			
			
			//审核
			RenewResult result = item.getRenewRejectCd();
			if(result != RenewResult.R00){
				card.setRenewInd(RenewInd.W);
				card.setRenewRejectCd(result);
				info.setRenewRptItem(makeRenewRptItem(item, result, RenewInd.W, product, card, acct, dualAcct));
				return info;
			}
			
			
			//获取客户
			CcsCustomer cust = rTmCustomer.findOne(acct.getCustId());
			if(cust == null)
			{
				throw new RuntimeException(RenewResult.R03.getDesc());
			}
			
			//获取名址
			CcsAddress addr = null;
			if(card.getCardDeliverMethod() == CardFetchMethod.A){
				QCcsAddress qTmAddress = QCcsAddress.ccsAddress;
				addr = rTmAddress.findOne(qTmAddress.org.eq(cust.getOrg()).and(qTmAddress.custId.eq(cust.getCustId())).and(qTmAddress.addrType.eq(card.getCardDeliverAddrFlag())));
				if(addr == null)
				{
					throw new RuntimeException(RenewResult.R04.getDesc());
				}
			}
			
			
			//更新逻辑卡有效期
			card.setCardExpireDate(item.getCardExpireDate());
			card.setRenewInd(RenewInd.P);
			card.setRenewRejectCd(RenewResult.R00);
			card.setLastRenewcardDate(batchFacility.getBatchDate());
			
			//续卡成功回盘文件
			info.setExpiryChangeFileItem(makeExpiryChangeFileItem(item, card, acct, dualAcct, cust, addr, product, productCredit));
			//到期换卡通知短信
//			info.setRenewMsgItem(makeRenewMsgItem(item.getCardExpireDate(), card, cust));
			//到期续卡报表
			info.setRenewRptItem(makeRenewRptItem(item, result, RenewInd.P, product, card, acct, dualAcct));
			
			
			
			return info;
			
		} catch (Exception e) {
			logger.error("到期换卡批处理异常,卡号{}",CodeMarkUtils.subCreditCard(item.getCardNbr()));
			throw e;
		}
		
	}


	/**
	 * @see 方法名：makeRenewMsgItem 
	 * @see 描述：创建到期换卡接口对象
	 * @see 创建日期：2015-6-23下午8:00:13
	 * @author ChengChun
	 *  
	 * @param expiryDate
	 * @param card
	 * @param cust
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
/*	private RenewMsgItem makeRenewMsgItem(Date expiryDate, CcsCard card, CcsCustomer cust) {
		RenewMsgItem item = new RenewMsgItem();
		
		item.org = card.getOrg();
		item.custName = cust.getName();
		item.gender = cust.getGender();
		item.cardNo = card.getLastestMediumCardNbr(); // 不受卡号发送方式约束
		item.mobileNo = cust.getMobileNo();
		item.msgCd = fetchMsgCdService.fetchMsgCd(card.getProductCd(), CPSMessageCategory.CPS040);
		item.bsFlag = card.getBscSuppInd();
		item.cardFetchType = card.getCardDeliverMethod();
		item.expiryDate = expiryDate;
		
		return item;
	}*/


	/**
	 * @see 方法名：makeRenewRptItem 
	 * @see 描述：生成到期续卡报表
	 * @see 创建日期：2015-6-23下午8:00:57
	 * @author ChengChun
	 *  
	 * @param in
	 * @param result
	 * @param renewInd
	 * @param product
	 * @param card
	 * @param acct
	 * @param dualAcct
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private RenewRptItem makeRenewRptItem(CcsCardExpList in, RenewResult result, 
			RenewInd renewInd, Product product, CcsCard card, CcsAcct acct, CcsAcct dualAcct) {
		
		RenewRptItem rpt = new RenewRptItem();
		
		rpt.org = in.getOrg();
		rpt.cardNo = in.getCardNbr();
		rpt.logicalCardNo = in.getLogicCardNbr();
		rpt.newExpDate = in.getCardExpireDate();
		rpt.renewResult = result;
		rpt.renewInd = renewInd;
		if(product!=null){
			rpt.productCd = product.productCode;
			rpt.productDesc = product.description;
		}
		if(card!=null){
			rpt.cardBlockCode = card.getBlockCode();
		}
		if(acct!=null){
			rpt.acctBlockCode = acct.getBlockCode();
		}
		if(dualAcct!=null){
			rpt.dualAcctBlockCode = dualAcct.getBlockCode();
		}
		
		return rpt;
	}


	
	/**
	 * @see 方法名：makeExpiryChangeFileItem 
	 * @see 描述：生成续卡回盘文件
	 * @see 创建日期：2015-6-23下午8:01:40
	 * @author ChengChun
	 *  
	 * @param in
	 * @param card
	 * @param acct
	 * @param dualAcct
	 * @param cust
	 * @param addr
	 * @param product
	 * @param productCredit
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private ExpiryChangeFileItem makeExpiryChangeFileItem(CcsCardExpList in, CcsCard card, 
			CcsAcct acct, CcsAcct dualAcct, CcsCustomer cust, CcsAddress addr, Product product, ProductCredit productCredit) {
		
		ExpiryChangeFileItem item = new ExpiryChangeFileItem();
		
		item.org = in.getOrg();
		item.logicCard = in.getLogicCardNbr();
		item.cardNo = in.getCardNbr();
		item.expiryDate = in.getCardExpireDate();
		item.bsFlag = card.getBscSuppInd();
		item.idType = cust.getIdType();
		item.certId = cust.getIdNo();
		item.cnName = cust.getName();
		item.companyName = cust.getCorpName();
		item.cardFetchType = card.getCardDeliverMethod();
		if(addr != null){
			item.address = addr.getAddress();
			item.district = addr.getDistrict();
			item.city = addr.getCity();
			item.state = addr.getState();
			item.countryCd = addr.getCountryCode();
			item.zip = addr.getPostcode();
		}
		item.brand = product.brand;
		item.name = cust.getOncardName();
		item.cardClass = product.cardClass;
		item.gender = cust.getGender();
		item.mobile = cust.getMobileNo();
		item.phone = cust.getHomePhone();
		item.owningBranch = card.getOwningBranch();
		item.billingDay = acct.getCycleDay();
		item.currency = acct.getAcctType().getCurrencyCode();
		item.creditLimit = acct.getCreditLmt();
		if(dualAcct != null)
		{
			item.foreignCurrency = dualAcct.getAcctType().getCurrencyCode();
			item.dualCreditLimit = dualAcct.getCreditLmt();
		}
		
		return item;
	}
	
}
