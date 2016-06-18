package com.sunline.ccs.service.auth.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ccs.infrastructure.server.repos.RCcsCardSpecbizCtrl;
import com.sunline.ccs.infrastructure.shared.model.CcsCardO;
import com.sunline.ccs.infrastructure.shared.model.CcsCardSpecbizCtrl;
import com.sunline.ccs.param.def.enums.SpecTxnType;
import com.sunline.ccs.service.auth.context.AuthContext;
import com.sunline.ccs.service.auth.frame.AuthException;
import com.sunline.ccs.service.auth.frame.HandlerCommService;

/**
 * 
 * @see 类名：AuthSpecTxnBuildHandler
 * @see 描述：建立授权特殊业务流程
 *
 * @see 创建日期：   2015年6月24日下午3:31:55
 * @author liruilin
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class AuthSpecTxnBuildHandler extends AuthSpecTxnHandler {
	@Autowired
	private RCcsCardSpecbizCtrl rCcsCardSpecbizCtrl;

	@Autowired
	private HandlerCommService pcs;

	@Override
	protected void updateStatistics(AuthContext context, CcsCardSpecbizCtrl cardSpectxnSupportO) throws AuthException {
		if (null == cardSpectxnSupportO) {
			// 没有找到特殊业务则新建业务
			CcsCardO cardo = context.getCard();
			cardSpectxnSupportO = new CcsCardSpecbizCtrl();
			cardSpectxnSupportO.setLogicCardNbr(cardo.getLogicCardNbr());
			cardSpectxnSupportO.setSpecTxnType(SpecTxnType.A02);
			cardSpectxnSupportO.setTxnSupportIndicator(Indicator.Y);
			cardSpectxnSupportO.setBindDeviceNbr(context.getCustomer().getMobileNo());
			cardSpectxnSupportO.setLastUpdateBizDate(context.getTxnInfo().getBizDate());
			rCcsCardSpecbizCtrl.save(cardSpectxnSupportO);
		} else {
			// 更新特殊业务
			if (cardSpectxnSupportO.getTxnSupportIndicator() == Indicator.Y) {
				// TODO 状态为[已开通]报错 - 2013-11-25：银联离线测试要求，已开通的状态也可以通过
				//throw new AuthException(AuthReason.R008, pcs.getAuthDefaultAction(AuthReason.R008));
			} else {
				// 如果状态为[未开通]，则更改为[已开通]
				cardSpectxnSupportO.setTxnSupportIndicator(Indicator.Y);
				cardSpectxnSupportO.setLastUpdateBizDate(context.getTxnInfo().getBizDate());
			}
		}
	}
}
