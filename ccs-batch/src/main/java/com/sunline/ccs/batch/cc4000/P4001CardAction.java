package com.sunline.ccs.batch.cc4000;

import java.util.Calendar;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.batch.LineItem;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ark.support.utils.CodeMarkUtils;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsCardLmMapping;
import com.sunline.ccs.infrastructure.shared.model.CcsCssfeeReg;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.exchange.CardActItem;

/**
 * @see 类名：P4001CardAction
 * @see 描述：挂失换卡、损坏换卡、损坏转到期批处理作业
 *
 * @see 创建日期：   2015-6-23下午7:49:04
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class P4001CardAction implements ItemProcessor<LineItem<CardActItem>, S4001CardAction> {

	/**
	 * 系统日志
	 */
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private CustAcctCardFacility queryFacility;
	@Autowired
	private UnifiedParameterFacility parameterFacility;
	@Autowired
	private BatchStatusFacility batchFacility;
	@PersistenceContext
	private EntityManager em;
	
	@Override
	public S4001CardAction process(LineItem<CardActItem> item) throws Exception {
		
		try 
		{
			S4001CardAction out = new S4001CardAction();
			CardActItem cardAct = item.getLineObject();
			OrganizationContextHolder.setCurrentOrg(cardAct.org);
			
			CcsCard card = queryFacility.getCardByLogicCardNbr(cardAct.logicCard);
			if(card == null)
			{
				//找不到逻辑卡,终止程序,手工处理
				throw new RuntimeException("找不到逻辑卡，旧介质卡号{}"+cardAct.cardNo);
			}
			
			if (logger.isDebugEnabled()) {
				logger.debug("newCardNo["+CodeMarkUtils.subCreditCard(cardAct.newCardNo)
						+"],Org["+card.getOrg()
						+"],logicCard["+cardAct.logicCard
						+"]");
			}
			
			switch (cardAct.actionCd){
			//挂失补卡
			case LI:
				generateCardLmMapping(cardAct, card);
				break;
				
			//损坏换卡
			case DI:
				
			//损坏转到期
			case DR:
				if(StringUtils.isNotBlank(cardAct.newCardNo) && !cardAct.cardNo.equals(cardAct.newCardNo))
				{
					generateCardLmMapping(cardAct, card);
				}
				card.setCardExpireDate(cardAct.expiryDate);
				break;
				
			//临时挂失
			case TL:
				ProductCredit productCr = parameterFacility.loadParameter(card.getProductCd(), ProductCredit.class);
				if(DateUtils.truncatedEquals(batchFacility.getBatchDate(), DateUtils.addDays(cardAct.actionDate, productCr.lostToSendMessageDays), Calendar.DATE)){
					// 转正式挂失短信
					//makeTempLostWarningMsg(out, cardAct, productCr, cust);
					
				}else if(DateUtils.truncatedEquals(batchFacility.getBatchDate(), DateUtils.addDays(cardAct.actionDate, productCr.lostToChangeCardDays), Calendar.DATE)){
					// 转正式挂失换卡短信
					//makeTempLostChangeMsg(out, cardAct, productCr, cust);
					// 挂失费
					generateCssFeeReg(cardAct, "S14050");
					// 换卡费
					generateCssFeeReg(cardAct, "S14070");
					// 更新卡号
					generateCardLmMapping(cardAct, card);
				}
				break;
				
			default: throw new IllegalArgumentException("不支持的卡操作:" + cardAct.actionCd);
			}

			return out;
			
		} catch (Exception e) {
			logger.error("换卡批处理作业异常, 换卡方式{}, 逻辑卡号{}", item.getLineObject().actionCd, item.getLineObject().logicCard);
			throw e;
		}
	}


	/**
	 * @see 方法名：generateCssFeeReg 
	 * @see 描述：生成一笔客服费用注册信息
	 * @see 创建日期：2015-6-23下午7:50:04
	 * @author ChengChun
	 *  
	 * @param cardAct
	 * @param serviceNbr
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private void generateCssFeeReg(CardActItem cardAct, String serviceNbr) {
		CcsCssfeeReg reg = new CcsCssfeeReg();
		
		reg.setOrg(cardAct.org);
		reg.setServiceNbr(serviceNbr);
		reg.setCardNbr(cardAct.cardNo);
		reg.setTxnDate(batchFacility.getSystemStatus().getBusinessDate());
		reg.setRequestTime(new Date());
		reg.setRegId(null);
		
		em.persist(reg);
	}


	/**
	 * @see 方法名：makeTempLostChangeMsg 
	 * @see 描述：创建转正式挂失换卡短信提醒
	 * @see 创建日期：2015-6-23下午7:51:16
	 * @author ChengChun
	 *  
	 * @param out
	 * @param cardAct
	 * @param productCr
	 * @param cust
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
/*	private void makeTempLostChangeMsg(S4001CardAction out, CardActItem cardAct, ProductCredit productCr, CcsCustomer cust) {
		TempLostToChangeCard tempLostChange = new TempLostToChangeCard();
		
		tempLostChange.org = cardAct.org;
		tempLostChange.cardNo = cardAct.cardNo;
		tempLostChange.custName = cust.getName();
		tempLostChange.gender = cust.getGender();
		tempLostChange.mobileNo = cust.getMobileNo();
		tempLostChange.msgCd = fetchMsgCdService.fetchMsgCd(productCr.productCd, CPSMessageCategory.CPS059);
		tempLostChange.tempLostDate = cardAct.actionDate;
		
		out.setTempLostChange(tempLostChange);
	}*/


	/**
	 * @see 方法名：makeTempLostWarningMsg 
	 * @see 描述：创建转正式挂失换卡短信提醒
	 * @see 创建日期：2015-6-23下午7:51:56
	 * @author ChengChun
	 *  
	 * @param out
	 * @param cardAct
	 * @param productCr
	 * @param cust
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
/*	private void makeTempLostWarningMsg(S4001CardAction out, CardActItem cardAct, ProductCredit productCr, CcsCustomer cust) {
		TempLostToChangeCardWarning tempLostWarning = new TempLostToChangeCardWarning();
		
		tempLostWarning.org = cardAct.org;
		tempLostWarning.cardNo = cardAct.cardNo;
		tempLostWarning.custName = cust.getName();
		tempLostWarning.gender = cust.getGender();
		tempLostWarning.mobileNo = cust.getMobileNo();
		tempLostWarning.msgCd = fetchMsgCdService.fetchMsgCd(productCr.productCd, CPSMessageCategory.CPS058);
		tempLostWarning.tempLostDate = cardAct.actionDate;
		tempLostWarning.expireDate = DateUtils.addDays(cardAct.actionDate, productCr.lostToChangeCardDays);
		
		out.setTempLostWarning(tempLostWarning);
	}*/

	
	/**
	 * @see 方法名：generateCardLmMapping 
	 * @see 描述：维护介质卡-逻辑卡映射表，更新卡档中的介质卡号
	 * @see 创建日期：2015-6-23下午7:52:53
	 * @author ChengChun
	 *  
	 * @param cardAct
	 * @param card
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private CcsCardLmMapping generateCardLmMapping(CardActItem cardAct, CcsCard card) {
		
		CcsCardLmMapping map = new CcsCardLmMapping();
		map.setOrg(card.getOrg());
		map.setCardNbr(cardAct.newCardNo);
		map.setLogicCardNbr(cardAct.logicCard);
		
		//更新卡档最新卡号
		card.setLastestMediumCardNbr(cardAct.newCardNo);
		
		em.persist(map);
		return map;
	}
}

