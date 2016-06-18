package com.sunline.ccs.service.auth.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsCardO;
import com.sunline.ccs.service.auth.context.AuthContext;
import com.sunline.ccs.service.auth.frame.AuthException;
 
/**
 * 
 * @see 类名：AuthLoanHandler
 * @see 描述：借记[分期]处理
 *
 * @see 创建日期：   2015年6月24日下午3:29:08
 * @author liruilin
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class AuthLoanHandler extends AuthHandler {
	
	@Autowired
	private UnifiedParamFacilityProvide unifieldParServer;
	
	@Autowired
	private RCcsAcct rCcsAcct; 
	
	@Override
	protected void updateUnmatchAmt(AuthContext context) {
		CcsAcctO account =context.getAccount();
		//增加未匹配借记金额
		account.setMemoDb(account.getMemoDb().add(context.getTxnInfo().getChbTransAmt()));
	}
	@Override
	protected void updateStatistics(AuthContext context) throws AuthException {
		CcsCardO card = context.getCard();
		//增加单日消费金额 DAY_USED_RETAIL_AMT 
		card.setDayUsedRetailAmt(card.getDayUsedRetailAmt().add(context.getTxnInfo().getChbTransAmt()));
		//增加单日消费笔数 DAY_USED_RETAIL_NBR
		card.setDayUsedRetailNbr(card.getDayUsedRetailNbr()+1);
		//增加当期消费金额 CTD_USED_AMT 
		card.setCtdUsedAmt(card.getCtdUsedAmt().add(context.getTxnInfo().getChbTransAmt()));
	}
}
