package com.sunline.ccs.service.auth.handler;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sunline.ccs.infrastructure.server.repos.RCcsCardSpecbizCtrl;
import com.sunline.ccs.infrastructure.server.repos.RCcsAuthmemoO;
import com.sunline.ccs.infrastructure.shared.model.QCcsAuthmemoO;
import com.sunline.ccs.infrastructure.shared.model.CcsCardSpecbizCtrl;
import com.sunline.ccs.infrastructure.shared.model.CcsCardSpecbizCtrlKey;
import com.sunline.ccs.infrastructure.shared.model.CcsAuthmemoO;
//import com.sunline.ccs.param.def.enums.AuthReason;
import com.sunline.ccs.param.def.enums.AuthTransDirection;
import com.sunline.ccs.param.def.enums.SpecTxnType;
import com.sunline.ccs.service.auth.common.AuthCommService;
import com.sunline.ccs.service.auth.context.AuthContext;
import com.sunline.ccs.service.auth.context.CupMsg;
import com.sunline.ccs.service.auth.context.RespInfo;
import com.sunline.ccs.service.auth.context.TxnInfo;
import com.sunline.ccs.service.auth.frame.AuthException;
import com.sunline.ccs.service.auth.frame.Handler;
import com.sunline.ccs.service.auth.frame.HandlerCommService;
import com.sunline.ark.support.service.YakMessage;

/**
 * 
 * @see 类名：AuthSpecTxnReverseHandler
 * @see 描述：授权特殊业务的[冲正]总流程
 *
 * @see 创建日期：   2015年6月24日下午3:32:26
 * @author liruilin
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public abstract class AuthSpecTxnReverseHandler implements Handler {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private RCcsCardSpecbizCtrl rCcsCardSpecbizCtrl;

	@Autowired
	private RCcsAuthmemoO rCcsAuthmemoO;

	@Autowired
	private HandlerCommService pcs;
	
	@Autowired
	private AuthCommService authCommonService;

	@Override
	public RespInfo handle(YakMessage request, AuthContext context) throws AuthException {
		RespInfo responseInfo = new RespInfo();

		// 1、检查并返回原交易信息
		CcsAuthmemoO orig = checkOrigStatistics(request , context);
		
		// 1.5 校验是否无卡自助开通(冲正交易无法获取48域，取原交易的48域匹配)
		f48AOVerify(context, orig.getB048Add());
		// 2、判断原特殊交易状态
		if (context.getTxnInfo().isSpecTranNoCardSelfSvc()) {
			// 3、查询特殊业务
			CcsCardSpecbizCtrl cardSpectxnSupportO = findSpecialTrans(request, context);
			// 4、更新特殊业务信息
			updateStatistics(context, cardSpectxnSupportO);
		} else {
			// TODO 暂不支持其他特殊交易
			logger.debug("# {}","暂不支持其他特殊交易");
			//throw new AuthException(AuthReason.TS03, pcs.getAuthDefaultAction(AuthReason.TS03));
		}
		return responseInfo;
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
	 * 匹配原交易
	 * 
	 * @param request
	 * @param context
	 * @return
	 */
	protected Iterator<CcsAuthmemoO> findOriginalTrans(YakMessage request, AuthContext context) {
		TxnInfo txnInfo = context.getTxnInfo();
		QCcsAuthmemoO unmo = QCcsAuthmemoO.ccsAuthmemoO;
		CupMsg msg = new CupMsg(request);
		// 原交易
		return rCcsAuthmemoO.findAll(
					 unmo.b032AcqInst.eq(request.getBody(32).trim())
				.and(unmo.b033FwdIns.eq(request.getBody(33).trim()))
				// B090OrigData拆分
				.and(unmo.b007TxnTime.eq(msg.checkDummyStr(msg.getF090_3_OrigSysDateTime())))
				.and(unmo.b011Trace.eq(msg.checkDummyStr(msg.getF090_2_OrigSysTraceId())))
				.and(unmo.b032AcqInst.eq(msg.checkDummyStr(msg.getF090_4_OrigAcqInstId())))
				.and(unmo.b033FwdIns.eq(msg.checkDummyStr(msg.getF090_5_OrigFwdInstId())))
				.and(unmo.logBizDate.eq(txnInfo.getBizDate()))
				// 新增
				.and(unmo.b002CardNbr.eq(request.getBody(2)))
				.and(unmo.txnDirection.eq(AuthTransDirection.Normal))
				// 移除
				//.and(unmo.authTxnStatus.eq(AuthTransStatus.N))
				//.and(unmo.finalAction.eq(AuthAction.A))
				).iterator();
	}

	/**
	 * 更新特殊业务信息
	 * 
	 * @param context
	 * @param cardSpectxnSupportO
	 * @throws AuthException
	 */
	protected abstract void updateStatistics(AuthContext context, CcsCardSpecbizCtrl cardSpectxnSupportO) throws AuthException;

	/**
	 * 48域校验是否无卡自助开通
	 * 
	 * @about 取48域AO用法的值
	 * @param context
	 * @param f48
	 */
	protected void f48AOVerify(AuthContext context, String f48) {
		try {
			CupMsg message = new CupMsg();
			String f48AO = message.formatAddDataPriv(f48, 48).get("AO").get(CupMsg.VAL);
			context.getTxnInfo().setSpecTranNoCardSelfSvc(f48AO.equals("14") ? true : false);
		} catch (Exception e) {
			context.getTxnInfo().setSpecTranNoCardSelfSvc(false);
		}
	}
	
	/**
	 * 检查并返回原交易信息
	 * @param request
	 * @param context
	 * @return		
	 * @throws AuthException
	 */
	protected CcsAuthmemoO checkOrigStatistics(YakMessage request , AuthContext context) throws AuthException{
		// 1.0 匹配原交易
		Iterator<CcsAuthmemoO> it = findOriginalTrans(request, context);
		// 1.1 检查步骤一：原交易集合为空；则返回-找不到原交易（25）
		CcsAuthmemoO orig = authCommonService.checkOrigListIsNotNull(it, false);
		/**
		 * 1.2检查步骤二：
		 * 1)-原交易被拒绝（FinalAction != A ）且 状态为（AuthTxnStatus == E）；则返回 - 无效交易(12)
		 * 2)-原交易状态为（AuthTxnStatus != N）；则返回 - 故障交易(22)
		 */
		authCommonService.checkOrigStatusIsInvalid(orig);
		// 1.3 检查步骤（委托业务或开通无卡自助）：原交易状态正常（AuthTxnStatus != N ）；则返回 无效交易(12)
		authCommonService.checkOrigStatusIsNotNormal(orig);
		return orig;
	}
}
