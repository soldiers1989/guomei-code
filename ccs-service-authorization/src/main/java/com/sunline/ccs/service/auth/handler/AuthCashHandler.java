package com.sunline.ccs.service.auth.handler;

import org.springframework.stereotype.Service;

import com.sunline.ppy.dictionary.enums.InputSource;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsCardO;
import com.sunline.ccs.param.def.enums.AuthTransTerminal;
import com.sunline.ccs.service.auth.context.AuthContext;
import com.sunline.ccs.service.auth.context.TxnInfo;

/**
 * 
 * @see 类名：AuthCashHandler
 * @see 描述：借记[取现]处理
 *
 * @see 创建日期：   2015年6月24日下午3:27:19
 * @author liruilin
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class AuthCashHandler extends AuthHandler {
	
	@Override
	protected void updateUnmatchAmt(AuthContext context) {
		CcsAcctO account =context.getAccount();
		//增加未匹配取现金额
		account.setMemoCash(account.getMemoCash().add(context.getTxnInfo().getChbTransAmt()));
		//增加未匹配借记总金额
		account.setMemoDb(account.getMemoDb().add(context.getTxnInfo().getChbTransAmt()));
	}
	
	@Override
	protected void updateStatistics(AuthContext context) {
		CcsCardO card = context.getCard();
		TxnInfo txnInfo = context.getTxnInfo();
		//统计值更新
		//增加 当期消费取现总金额  CTD_USED_AMT (消费取现分开来记，这里不再累计总的值了)
		//card.setCtdUsedAmt(card.getCtdUsedAmt().add(context.getTransInfo().getChbTransAmt()));

		//交易类型专门统计更新
		//增加单日取现金额DAY_USED_CASH_AMT
		card.setDayUsedCashAmt(card.getDayUsedCashAmt().add(txnInfo.getChbTransAmt()));
		//增加单日取现笔数DAY_USED_CASH_NBR 
		card.setDayUsedCashNbr(card.getDayUsedCashNbr()+1);
		//增加当期取现总金额CTD_CASH_AMT 
		card.setCtdCashAmt(card.getCtdCashAmt().add(txnInfo.getChbTransAmt()));
		
		if(txnInfo.getTransTerminal() == AuthTransTerminal.ATM){
			card.setDayUsedAtmAmt(card.getDayUsedAtmAmt().add(txnInfo.getChbTransAmt()));
			card.setDayUsedAtmNbr(card.getDayUsedAtmNbr() + 1 );
		}

		// 更新银联境外atm取现单日累计
		if(txnInfo.getInputSource() == InputSource.CUP
				&& txnInfo.getTransTerminal() == AuthTransTerminal.ATM
				&& txnInfo.isCupXborder()){
			// 银联境外atm取现单日累计 +=  chbtxnamt
			card.setDayUsedAtmCupxbAmt(card.getDayUsedAtmCupxbAmt().add(txnInfo.getChbTransAmt()));
		}
	}
}
