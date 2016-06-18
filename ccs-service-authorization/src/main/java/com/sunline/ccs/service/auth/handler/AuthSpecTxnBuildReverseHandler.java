package com.sunline.ccs.service.auth.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ccs.infrastructure.shared.model.CcsCardSpecbizCtrl;
import com.sunline.ccs.param.def.enums.AuthReason;
import com.sunline.ccs.service.auth.context.AuthContext;
import com.sunline.ccs.service.auth.frame.AuthException;
import com.sunline.ccs.service.auth.frame.HandlerCommService;

/**
 * 
 * @see 类名：AuthSpecTxnBuildReverseHandler
 * @see 描述：建立授权特殊业务的[冲正]流程
 *
 * @see 创建日期：   2015年6月24日下午3:32:05
 * @author liruilin
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class AuthSpecTxnBuildReverseHandler extends AuthSpecTxnReverseHandler {
	@Autowired
	private HandlerCommService pcs;
	
	@Override
	protected void updateStatistics(AuthContext context, CcsCardSpecbizCtrl cardSpectxnSupportO) throws AuthException {
		if (null == cardSpectxnSupportO) {
			// 没有找到特殊业务，冲正失败
			throw new AuthException(AuthReason.R007, pcs.getAuthDefaultAction(AuthReason.R007));
		} else {
			// 更新特殊业务
			if (cardSpectxnSupportO.getTxnSupportIndicator() == Indicator.Y) {
				// 如果状态为[已开通]，则更改为[未开通]
				cardSpectxnSupportO.setTxnSupportIndicator(Indicator.N);
				cardSpectxnSupportO.setLastUpdateBizDate(context.getTxnInfo().getBizDate());
			} else {
				// TODO 状态为[已开通]报错
				throw new AuthException(AuthReason.R008, pcs.getAuthDefaultAction(AuthReason.R008));
			}
		}
	}
}
