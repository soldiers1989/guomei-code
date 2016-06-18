package com.sunline.ccs.service.auth.handler;

import org.springframework.stereotype.Service;

import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.service.auth.context.AuthContext;

/**
 * 
 * @see 类名：ManualForceAuthRetailHandler
 * @see 描述：借记[消费]处理包括：代收、普通消费、代授权通知、预授权消费、圈存
 *
 * @see 创建日期：   2015年6月24日下午3:43:44
 * @author liruilin
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class ManualForceAuthRetailHandler extends AuthHandler {

	@Override
	protected void updateUnmatchAmt(AuthContext context) {
		CcsAcctO account =context.getAccount();
		//增加未匹配借记金额
		account.setMemoDb(account.getMemoDb().add(context.getTxnInfo().getChbTransAmt()));

		
	}
	
	
	@Override
	protected void updateStatistics(AuthContext context)
	{
		 		 
	}

 

}
