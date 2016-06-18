package com.sunline.ccs.service.auth.handler;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sunline.ppy.dictionary.enums.AuthTransType;
import com.sunline.ppy.dictionary.enums.InputSource;
import com.sunline.ccs.param.def.enums.TransAmtDirection;
import com.sunline.ccs.service.auth.common.AuthCommService;
import com.sunline.ccs.service.auth.context.AuthContext;
import com.sunline.ccs.service.auth.context.RespInfo;
import com.sunline.ccs.service.auth.frame.AuthException;
import com.sunline.ccs.service.auth.frame.Handler;
import com.sunline.ark.support.service.YakMessage;

/**
 * 
 * @see 类名：AuthHandler
 * @see 描述：正向借记总流程
 *
 * @see 创建日期：   2015年6月24日下午3:28:38
 * @author liruilin
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public abstract class AuthHandler  implements Handler {
	
	@Autowired
	AuthCommService authCommonService ;

	@Override
	public RespInfo handle(YakMessage request, AuthContext context)
			throws AuthException {
		RespInfo responseInfo = new RespInfo();
		//更新账户表未匹配金额
		updateUnmatchAmt(context);
		//更新卡表累计信息
		updateStatistics(context);
		
		responseInfo.setFinalUpdDirection(TransAmtDirection.D.toString()); 
		responseInfo.setFinalUpdAmt(context.getTxnInfo().getChbTransAmt());
//		responseInfo.setB038_AuthCode(authCommonService.generateAuthCode(context));
		
		// 姓名  AIC2.7 银联改造
		if (context.getTxnInfo().getInputSource() == InputSource.CUP 
				&& context.getTxnInfo().getTransType() == AuthTransType.TransferDeditDepos) {
			context.getTxnInfo().setAcctVerifyName(context.getCustomer().getName());
		}
		return responseInfo;
	}
	
	/**
	 * 更新账户表未匹配金额
	 * @param context
	 */
	protected abstract void updateUnmatchAmt(AuthContext context) throws AuthException;
	
	/**
	 * 更新卡表累计信息
	 * @param context
	 * @throws AuthException 
	 */
	protected abstract void updateStatistics(AuthContext context) throws AuthException;
	
	 


}
