package com.sunline.ccs.service.auth.handler;

import org.springframework.stereotype.Service;

import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.service.auth.context.AuthContext;

 
/**
 * 
 * @see 类名：ManualForceAuthCashHandler
 * @see 描述：人工授权借记调整取现类
 *
 * @see 创建日期：   2015年6月24日下午3:43:30
 * @author liruilin
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class ManualForceAuthCashHandler extends AuthHandler {
 

	@Override
	protected void updateUnmatchAmt(AuthContext context) {
		CcsAcctO account =context.getAccount();
		//增加未匹配取现金额
		account.setMemoCash(account.getMemoCash().add(context.getTxnInfo().getChbTransAmt()));
		//增加未匹配借记总金额
		account.setMemoDb(account.getMemoDb().add(context.getTxnInfo().getChbTransAmt()));
	
	}
	
	@Override
	protected void updateStatistics(AuthContext context)
	{
		 
		 
	}
 

}
