package com.sunline.ccs.service.auth.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsCardO;
import com.sunline.ccs.service.auth.context.AuthContext;

/**
 * 
 * @see 类名：AuthRetailHandler
 * @see 描述：借记[消费]处理包括：代收、普通消费、代授权通知、预授权消费、圈存
 *
 * @see 创建日期：   2015年6月24日下午3:31:10
 * @author liruilin
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class AuthRetailHandler extends AuthHandler {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	protected void updateUnmatchAmt(AuthContext context) {
		CcsAcctO account =context.getAccount();
		//增加未匹配借记金额
		account.setMemoDb(account.getMemoDb().add(context.getTxnInfo().getChbTransAmt()));

		
	}
	
	
	@Override
	protected void updateStatistics(AuthContext context)
	{
		CcsCardO card = context.getCard();
		//增加单日消费金额 DAY_USED_RETAIL_AMT 
		 
		logger.info("getDayUsedRetailAmt add begin == "+card.getDayUsedRetailAmt());
		card.setDayUsedRetailAmt(card.getDayUsedRetailAmt().add(context.getTxnInfo().getChbTransAmt()));
		logger.info("getDayUsedRetailAmt add end  == "+card.getDayUsedRetailAmt());
		
		//增加单日消费笔数 DAY_USED_RETAIL_NBR
		 
		logger.info("getDayUsedRetailNbr add begin== "+ card.getDayUsedRetailNbr());
		card.setDayUsedRetailNbr(card.getDayUsedRetailNbr()+1);
		logger.info("getDayUsedRetailNbr add end  == "+ card.getDayUsedRetailNbr());
		 
		
		//增加当期消费金额 CTD_USED_AMT 
	 
		logger.info("getCtdUsedAmt add begin == "+card.getCtdUsedAmt());
		card.setCtdUsedAmt(card.getCtdUsedAmt().add(context.getTxnInfo().getChbTransAmt()));
		logger.info("getCtdUsedAmt add end  == "+card.getCtdUsedAmt());
		 
		
		if(context.getTxnInfo().isInternetTrans()){
			card.setCtdNetAmt(card.getCtdNetAmt().add(context.getTxnInfo().getChbTransAmt()))	;
		}
		 
	}


}
