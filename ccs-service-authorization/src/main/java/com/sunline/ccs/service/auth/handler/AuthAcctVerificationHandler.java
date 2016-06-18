package com.sunline.ccs.service.auth.handler;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ccs.infrastructure.server.repos.RCcsCardSpecbizCtrl;
import com.sunline.ccs.infrastructure.shared.model.CcsCardSpecbizCtrl;
import com.sunline.ccs.infrastructure.shared.model.CcsCardSpecbizCtrlKey;
import com.sunline.ccs.param.def.enums.SpecTxnType;
import com.sunline.ccs.param.def.enums.TransAmtDirection;
import com.sunline.ccs.service.auth.context.AuthContext;
import com.sunline.ccs.service.auth.context.CupMsg;
import com.sunline.ccs.service.auth.context.RespInfo;
import com.sunline.ccs.service.auth.frame.AuthException;
import com.sunline.ccs.service.auth.frame.Handler;
import com.sunline.ark.support.service.YakMessage;

/**
 * 
 * @see 类名：AuthAcctVerificationHandler
 * @see 描述：账户验证、生成动态验证码
 *		1.对于返回姓名的交易，生成姓名;<br>
 *		2.对于生成验证码的交易，触发外部接口生成验证码及发送(暂不实现)
 *
 * @see 创建日期：   2015年6月24日下午3:26:47
 * @author liruilin
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class AuthAcctVerificationHandler implements Handler {
	
	@Autowired
	private RCcsCardSpecbizCtrl rCcsCardSpecbizCtrl;
	
	@Override
	public RespInfo handle(YakMessage request, AuthContext context) throws AuthException {
		RespInfo responseInfo = new RespInfo();
		// 2.1、对于生成验证码的交易，触发外部接口生成验证码及发送(暂不实现)
		if (context.getTxnInfo().isDynamicCodeResp()) {
			getRespDynCode();
		} 
		// 2.2、对于返回姓名的交易，生成姓名
		if(context.getTxnInfo().isNameResp()){
			context.getTxnInfo().setAcctVerifyName(getRespChnName(context.getCustomer().getName()));
		}
		//2.3、用于查询是否开通无卡自助
		if(context.getTxnInfo().isqSpecTranNoCardSvc()){
			responseInfo.setOpenNoCardSelf(findSpecialTrans(request, context));
		}
		// 交易方向
		responseInfo.setFinalUpdDirection(TransAmtDirection.D.toString());
		return responseInfo;
	}

	/**
	 * 是否生成动态验证码(暂不使用)
	 * 
	 * @param f48
	 * @return 判断48域AO用法的值是否为[12/13];是则返回true[生成动态验证码],否则返回false[账户验证返回姓名]
	 */
	protected boolean isDynCode(String f48) throws AuthException {
		CupMsg message = new CupMsg();
		Map<String, Map<String, String>> f48Map = message.formatAddDataPriv(f48, 48);
		if (null != f48Map) {
			Map<String, String> f48AO = f48Map.get("AO");
			// TODO 生成动态验证码暂不支持
			if (null != f48AO 
					&& StringUtils.isNotBlank(f48AO.get(CupMsg.VAL)) 
					&& (f48AO.get(CupMsg.VAL).equals("12") || f48AO.get(CupMsg.VAL).equals("13"))) {
				return true;
			}
		}
		// 判断是否为账户验证条件
		return false;
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

	/**
	 * 动态验证码(暂不实现)
	 * 
	 * @about 对于生成验证码的交易，触发外部接口生成验证码及发送
	 */
	public void getRespDynCode() {
		// TODO 触发外部接口生成验证码及发送
	}
	
	
	/**
	 * 匹配特殊交易,为了查询是否开通无卡自助交易
	 * 
	 * @param request
	 * @param context
	 * @return
	 */
	protected Indicator findSpecialTrans(YakMessage request, AuthContext context) {
		CcsCardSpecbizCtrlKey cardSpectxnSupportOKey = new CcsCardSpecbizCtrlKey();
		cardSpectxnSupportOKey.setLogicCardNbr(context.getCard().getLogicCardNbr());
		cardSpectxnSupportOKey.setSpecTxnType(SpecTxnType.A02);
		CcsCardSpecbizCtrl cardSpectxnSupportO = rCcsCardSpecbizCtrl.findOne(cardSpectxnSupportOKey);
		Indicator indicator = Indicator.N;
		if(cardSpectxnSupportO != null){
			 indicator = cardSpectxnSupportO.getTxnSupportIndicator();
		}
		return indicator;
	}

}
