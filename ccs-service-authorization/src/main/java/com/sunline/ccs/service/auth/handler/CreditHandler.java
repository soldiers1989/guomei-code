package com.sunline.ccs.service.auth.handler;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import com.sunline.ppy.dictionary.enums.AuthTransType;
import com.sunline.ppy.dictionary.enums.InputSource;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.param.def.enums.TransAmtDirection;
import com.sunline.ccs.service.auth.context.AuthContext;
import com.sunline.ccs.service.auth.context.RespInfo;
import com.sunline.ccs.service.auth.frame.AuthException;
import com.sunline.ccs.service.auth.frame.Handler;
import com.sunline.ark.support.service.YakMessage;

 
/**
 * 
 * @see 类名：CreditHandler
 * @see 描述：正向贷记流程
 *
 * @see 创建日期：   2015年6月24日下午3:42:49
 * @author liruilin
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public  class CreditHandler  implements Handler {

	@Override
	public RespInfo handle(YakMessage request, AuthContext context)
			throws AuthException {
		RespInfo responseInfo = new RespInfo();
		CcsAcctO account =context.getAccount();
		// 使用[*]号替换姓名中的第一个字
		if (context.getTxnInfo().getInputSource() == InputSource.CUP 
				&& context.getTxnInfo().getTransType() == AuthTransType.TransferCredit) {
			context.getTxnInfo().setAcctVerifyName(getRespChnName(context.getCustomer().getName()));
		}
		// 增加未匹配贷记金额
		account.setMemoCr(account.getMemoCr().add(context.getTxnInfo().getChbTransAmt()));
		responseInfo.setFinalUpdDirection(TransAmtDirection.C.toString()); 
		responseInfo.setFinalUpdAmt(context.getTxnInfo().getChbTransAmt());
//		responseInfo.setB038_AuthCode(authCommonService.generateAuthCode(context));
		return responseInfo;
	}
	
	/**
	 * 使用[*]号替换姓名中的第一个字
	 * 
	 * @param name
	 * @return 李四 -> *四
	 */
	public String getRespChnName(String name) {
		return StringUtils.isNotBlank(name) ? "*" + name.trim().substring(1) : name;
	}
}
