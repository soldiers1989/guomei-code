package com.sunline.ccs.batch.cc9200;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ppy.dictionary.exchange.SynchroCardAcctInfoItem;

/**
 * @see 类名：P9203SynchroInfo
 * @see 描述：征审同步卡账状态
 *
 * @see 创建日期：   2015-6-24下午4:59:48
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class P9203SynchroInfo implements ItemProcessor<S9201MasterData, S9201MasterData> {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
    private BatchStatusFacility batchFacility;
	
	@Override
	public S9201MasterData process(S9201MasterData item) throws Exception {
		
		try
		{
			for (CcsCard tmCard : item.getListCard()) {
				SynchroCardAcctInfoItem apsAccountCard = new SynchroCardAcctInfoItem();
				
				apsAccountCard.org = item.getAccount().getOrg();
				apsAccountCard.acctType = item.getAccount().getAcctType();
				apsAccountCard.acctNo = item.getAccount().getAcctNbr().toString();
				apsAccountCard.acctBlockCode = item.getAccount().getBlockCode();
				apsAccountCard.logicalCardNo = tmCard.getLogicCardNbr();
				apsAccountCard.cardBlockCode = tmCard.getBlockCode();
				apsAccountCard.setupDate = batchFacility.getBatchDate();
				
				item.getApsAccountCard().add(apsAccountCard);
			}
		}catch (Exception e) {
			logger.error("征审同步卡账文件生成异常, 账号{}, 账户类型{}", item.getAccount().getAcctNbr(), item.getAccount().getAcctType());
			throw e;
		}

		return item;
	}
}
