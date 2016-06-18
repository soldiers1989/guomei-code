package com.sunline.ccs.service.auth.handler;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sunline.ccs.infrastructure.server.repos.RCcsCardSpecbizCtrl;
import com.sunline.ccs.infrastructure.shared.model.CcsCardSpecbizCtrl;
import com.sunline.ccs.infrastructure.shared.model.CcsCardSpecbizCtrlKey;
import com.sunline.ccs.param.def.enums.AuthReason;
import com.sunline.ccs.param.def.enums.SpecTxnType;
import com.sunline.ccs.param.def.enums.TransAmtDirection;
import com.sunline.ccs.service.auth.context.AuthContext;
import com.sunline.ccs.service.auth.context.CupMsg;
import com.sunline.ccs.service.auth.context.RespInfo;
import com.sunline.ccs.service.auth.frame.AuthException;
import com.sunline.ccs.service.auth.frame.Handler;
import com.sunline.ccs.service.auth.frame.HandlerCommService;
import com.sunline.ark.support.service.YakMessage;

/**
 * 
 * @see 类名：AuthSpecTxnHandler
 * @see 描述：授权特殊业务总流程
 *
 * @see 创建日期：   2015年6月24日下午3:32:16
 * @author liruilin
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public abstract class AuthSpecTxnHandler implements Handler {

	@Autowired
	private RCcsCardSpecbizCtrl rCcsCardSpecbizCtrl;

	@Autowired
	private HandlerCommService pcs;


	@Override
	public RespInfo handle(YakMessage request, AuthContext context) throws AuthException {
		RespInfo responseInfo = new RespInfo();
		// 1、判断原特殊交易状态
		if (context.getTxnInfo().isSpecTranNoCardSelfSvc()) {
			// 2、查询特殊业务
			CcsCardSpecbizCtrl cardSpectxnSupportO = findSpecialTrans(request, context);
			// 3、更新特殊业务信息
			updateStatistics(context, cardSpectxnSupportO);
		} else {
			// 原-代码注释
			// logger.debug("# {}","暂不支持其他特殊交易");
			// throw new AuthException(AuthReason.TS03,
			// pcs.getAuthDefaultAction(AuthReason.TS03));
		}


		// 交易方向
		responseInfo.setFinalUpdDirection(TransAmtDirection.D.toString());
		return responseInfo;
	}

	/**
	 * 是否为特殊交易(暂不使用)
	 * 
	 * @param request
	 * @param context
	 * @return
	 */
	@Deprecated
	protected boolean isSpecialBusiness(String f48) throws AuthException {
		CupMsg message = new CupMsg();
		Map<String, Map<String, String>> f48Map = message.formatAddDataPriv(f48, 48);
		// TODO 没有找到48域
		if (null == f48Map) {
			throw new AuthException(AuthReason.TF02, pcs.getAuthDefaultAction(AuthReason.TF02));
		}
		Map<String, String> f48AO = f48Map.get("AO");
		// 判断开通特殊业务还是开通委托
		if (null != f48AO && StringUtils.isNotBlank(f48AO.get(CupMsg.VAL)) && f48AO.get(CupMsg.VAL).equals("14")) {
			return true;
		} else {
			// TODO 暂不支持其他特殊交易
			throw new AuthException(AuthReason.TS03, pcs.getAuthDefaultAction(AuthReason.TS03));
		}
	}

	/**
	 * 匹配特殊交易
	 * 
	 * @param request
	 * @param context
	 * @return
	 */
	protected CcsCardSpecbizCtrl findSpecialTrans(YakMessage request, AuthContext context) {
		CcsCardSpecbizCtrlKey cardSpectxnSupportOKey = new CcsCardSpecbizCtrlKey();
		cardSpectxnSupportOKey.setLogicCardNbr(context.getCard().getLogicCardNbr());
		cardSpectxnSupportOKey.setSpecTxnType(SpecTxnType.A02);
		CcsCardSpecbizCtrl cardSpectxnSupportO = rCcsCardSpecbizCtrl.findOne(cardSpectxnSupportOKey);
		return cardSpectxnSupportO;
	}

	/**
	 * 更新特殊业务信息
	 * 
	 * @param context
	 * @param cardSpectxnSupportO
	 * @throws AuthException
	 */
	protected abstract void updateStatistics(AuthContext context, CcsCardSpecbizCtrl cardSpectxnSupportO) throws AuthException;

	
}
