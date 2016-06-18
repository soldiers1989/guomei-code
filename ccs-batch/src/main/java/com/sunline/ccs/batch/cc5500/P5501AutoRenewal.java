package com.sunline.ccs.batch.cc5500;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.batch.LineItem;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ark.support.utils.CodeMarkUtils;
import com.sunline.ccs.facility.BlockCodeUtils;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcct;
import com.sunline.ccs.infrastructure.server.repos.RCcsAddress;
import com.sunline.ccs.infrastructure.server.repos.RCcsCard;
import com.sunline.ccs.infrastructure.server.repos.RCcsCardExpList;
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
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.RenewInd;
import com.sunline.ppy.dictionary.enums.RenewResult;
import com.sunline.ppy.dictionary.exchange.ExpiryChangeFileItem;
import com.sunline.ppy.dictionary.report.ccs.RenewRptItem;

/**
 * 到期换卡批处理作业
 * 
* @author fanghj
 *
 */
public class P5501AutoRenewal implements ItemProcessor<LineItem<ExpiryChangeFileItem>, S5500Renewal> {

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
	@Autowired
	private RCcsCardExpList rTmExpiryCheckList;
	/**
	 * BlockCode参数类
	 */
	@Autowired
	private BlockCodeUtils blockCodeUtils;
	
	/**
	 * 获取参数类
	 */
	@Autowired
	private UnifiedParameterFacility parameterFacility;
	
	@Autowired
	private BatchStatusFacility batchFacility;
	
	@Override
	public S5500Renewal process(LineItem<ExpiryChangeFileItem> item) throws Exception {
		
		S5500Renewal info = new S5500Renewal();
		
		try {
			//范型
			ExpiryChangeFileItem in = item.getLineObject();
			
			//获取卡片
			CcsCard card = rTmCard.findOne(in.logicCard);
			if(card == null)
			{
				info.setRenewRptItem(createRenewRptItem(in, RenewResult.R01, RenewInd.W, null, null, null, null));
				return info;
			}
			
			//取机构号并设置上下文
			OrganizationContextHolder.setCurrentOrg(card.getOrg());
			
			Product product = parameterFacility.loadParameter(card.getProductCd(), Product.class);
			ProductCredit productCredit = parameterFacility.loadParameter(card.getProductCd(), ProductCredit.class);
			//对于手工审核的记录，直接将数据放到到期换卡审核表
			if(!autoCheck(productCredit)){
				CcsCardExpList check = new CcsCardExpList();
				check.setOrg(card.getOrg());
				check.setLogicCardNbr(in.logicCard);
				check.setCardNbr(in.cardNo);
				check.setCardExpireDate(in.expiryDate);
				check.setBatchDate(batchFacility.getBatchDate());
				rTmExpiryCheckList.save(check);
				return info;
			}
			
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
			RenewResult result = check(card, acct, dualAcct);
			if(result != RenewResult.R00){
				card.setRenewInd(RenewInd.W);
				card.setRenewRejectCd(result);
				info.setRenewRptItem(createRenewRptItem(in, result, RenewInd.W, product, card, acct, dualAcct));
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
			card.setCardExpireDate(in.expiryDate);
			card.setRenewInd(RenewInd.P);
			card.setRenewRejectCd(RenewResult.R00);
			card.setLastRenewcardDate(batchFacility.getBatchDate());
			
			//续卡成功回盘文件
			info.setExpiryChangeFileItem(createExpiryChangeFileItem(in, card, acct, dualAcct, cust, addr, product, productCredit));
			
			//到期续卡报表
			info.setRenewRptItem(createRenewRptItem(in, result, RenewInd.P, product, card, acct, dualAcct));
			
			//到期换卡通知短信
//		info.setRenewMsgItem(createRenewMsgItem(in.expiryDate, card, cust));
			
			return info;
			
		} catch (Exception e) {
			logger.error("到期换卡批处理异常,卡号{}",CodeMarkUtils.subCreditCard(item.getLineObject().cardNo));
			throw e;
		}
		
	}


	/**
	 * 根据产品参数判断是否自动审核，如果否，则放入人工复核表
	 * @param product
	 * @return
	 */
	private boolean autoCheck(ProductCredit product) {
		return product.expiryAutoCheck == Indicator.N;
	}


	/**
	 * 创建到期换卡接口对象
	 * 
	 * @param expiryDate 新卡有效期
	 * @param card
	 * @param cust
	 * @return
	 */
/*	private RenewMsgItem createRenewMsgItem(Date expiryDate, CcsCard card, CcsCustomer cust) {
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
	 * 续卡审核
	 * 
	 * @param card
	 * @param acct
	 * @param dualAcct
	 * @return
	 */
	private RenewResult check(CcsCard card, CcsAcct acct, CcsAcct dualAcct) {
		
		//BlockCode审核
		if(!blockCodeUtils.getMergedRenewInd(acct.getBlockCode())){
			return RenewResult.R05;
		}
		if(dualAcct != null && !blockCodeUtils.getMergedRenewInd(dualAcct.getBlockCode())){
			return RenewResult.R05;
		}
		if(!blockCodeUtils.getMergedRenewInd(card.getBlockCode())){
			return RenewResult.R05;
		}
		
		//TODO 交易状况审核(二期)
		
		return RenewResult.R00;
	}


	/**
	 * 生成到期续卡报表
	 * 
	 * @param in
	 * @param result
	 * @param renewInd
	 * @param card
	 * @param acct
	 * @param dualAcct
	 * @return
	 */
	private RenewRptItem createRenewRptItem(ExpiryChangeFileItem in, RenewResult result, 
			RenewInd renewInd, Product product, CcsCard card, CcsAcct acct, CcsAcct dualAcct) {
		
		RenewRptItem rpt = new RenewRptItem();
		
		rpt.org = in.org;
		rpt.cardNo = in.cardNo;
		rpt.logicalCardNo = in.logicCard;
		rpt.newExpDate = in.expiryDate;
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
	 * 生成续卡回盘文件
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
	 */
	private ExpiryChangeFileItem createExpiryChangeFileItem(ExpiryChangeFileItem in, CcsCard card, 
			CcsAcct acct, CcsAcct dualAcct, CcsCustomer cust, CcsAddress addr, Product product, ProductCredit productCredit) {
		
		ExpiryChangeFileItem item = new ExpiryChangeFileItem();
		
		item.org = in.org;
		item.logicCard = in.logicCard;
		item.cardNo = in.cardNo;
		item.expiryDate = in.expiryDate;
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
