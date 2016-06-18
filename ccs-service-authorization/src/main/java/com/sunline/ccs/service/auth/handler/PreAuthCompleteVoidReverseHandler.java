package com.sunline.ccs.service.auth.handler;


import java.util.Iterator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sunline.ppy.dictionary.enums.AuthTransStatus;
import com.sunline.ccs.infrastructure.server.repos.RCcsAuthmemoO;
import com.sunline.ccs.infrastructure.shared.model.QCcsAuthmemoO;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsAuthmemoO;
import com.sunline.ccs.param.def.enums.AuthReason;
import com.sunline.ccs.param.def.enums.AuthTransDirection;
import com.sunline.ccs.param.def.enums.TransAmtDirection;
import com.sunline.ccs.service.auth.common.AuthCommService;
import com.sunline.ccs.service.auth.context.AuthContext;
import com.sunline.ccs.service.auth.context.CupMsg;
import com.sunline.ccs.service.auth.context.RespInfo;
import com.sunline.ccs.service.auth.frame.AuthException;
import com.sunline.ccs.service.auth.frame.Handler;
import com.sunline.ccs.service.auth.frame.HandlerCommService;
import com.sunline.ark.support.service.YakMessage;
/**
 * 
 * @see 类名：PreAuthCompleteVoidReverseHandler
 * @see 描述：预授权完成撤销、预授权完成冲正
 *
 * @see 创建日期：   2015年6月24日下午3:44:06
 * @author liruilin
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class PreAuthCompleteVoidReverseHandler implements Handler {

	@Autowired
	private RCcsAuthmemoO rCcsAuthmemoO;
	@Autowired
	private HandlerCommService pcs;
	
	@Autowired
	private AuthCommService authCommonService;

	@Override
	public RespInfo handle(YakMessage request, AuthContext context) throws AuthException {
		RespInfo responseInfo = new RespInfo();
		QCcsAuthmemoO unmo = QCcsAuthmemoO.ccsAuthmemoO;
		// 预授权完成交易
		CcsAuthmemoO origComplete = checkOrigStatistics(request, context);

		// 根据预授权完成的原交易键值，获取预授权交易
		CcsAuthmemoO orig = rCcsAuthmemoO.findOne(unmo.logKv.eq(origComplete.getOrigLogKv() == null ? 0 : origComplete.getOrigLogKv()));
		if (null == orig) {
			throw new AuthException(AuthReason.R009, pcs.getAuthDefaultAction(AuthReason.R009));
		}
		// 将原预授权交易的状态从已完成改为Normal;
		orig.setAuthTxnStatus(AuthTransStatus.N);
		// 更新预授权完成交易的状态,如果是撤销则MTI=200 ，如果是冲正MTI=420
		if (context.getTxnInfo().getTransDirection() == AuthTransDirection.Revocation) {
			origComplete.setAuthTxnStatus(AuthTransStatus.V);
			if (origComplete.getVoidCnt() == null) {
				origComplete.setVoidCnt(1);
			} else {
				origComplete.setVoidCnt(origComplete.getVoidCnt() + 1);
			}
		} else {
			origComplete.setAuthTxnStatus(AuthTransStatus.R);
		}
		origComplete.setLastReversalDate(context.getTxnInfo().getBizDate());
		// 存放预授权完成交易相关信息
		responseInfo = authCommonService.updateResponseInfo(origComplete);

		responseInfo.setFinalUpdDirection(TransAmtDirection.D.toString());
		// final_upd_amt = -1 * (完成交易.入账币种金额 CHB_txn_AMT - 预授权交易. CHB_txn_AMT)
		responseInfo.setFinalUpdAmt(origComplete.getChbTxnAmt().subtract(orig.getChbTxnAmt()).negate());

		CcsAcctO account = context.getAccount();
		// 减未匹配借记金额 +历史交易金额
		account.setMemoDb(account.getMemoDb().subtract(context.getTxnInfo().getChbTransAmt()).add(orig.getChbTxnAmt()));

		//增加未匹累计金额,一期不考虑累计信息。
		//CcsCardO card = context.getCard();
		//减单日消费金额 DAY_USED_RETAIL_AMT加原交易金额OrigAmt
		//card.setDayUsedRetailAmt(card.getDayUsedRetailAmt().subtract(context.getTransInfo().getChbTransAmt()).add(orig.getChbTxnAmt()));
		//减当期消费金额 CTD_USED_AMT加原交易金额OrigAmt
		//card.setCtdUsedAmt(card.getCtdUsedAmt().subtract(context.getTransInfo().getChbTransAmt()).add(orig.getChbTxnAmt()));
		 
		return responseInfo;
	}
	
	/**
	 * 匹配原交易
	 * 
	 * @param request
	 * @param context
	 * @return
	 */
	protected Iterator<CcsAuthmemoO> findOriginalTrans(YakMessage request, AuthContext context) {
		CupMsg msg = new CupMsg(request);
		QCcsAuthmemoO unmo = QCcsAuthmemoO.ccsAuthmemoO;
		// 1、查询原存款交易
		return rCcsAuthmemoO.findAll(
								 unmo.b032AcqInst.eq(request.getBody(32).trim())
							.and(unmo.b033FwdIns.eq(request.getBody(33).trim()))
							// B090OrigData拆分
							.and(unmo.b007TxnTime.eq(msg.checkDummyStr(msg.getF090_3_OrigSysDateTime())))
							.and(unmo.b011Trace.eq(msg.checkDummyStr(msg.getF090_2_OrigSysTraceId())))
							.and(unmo.b032AcqInst.eq(msg.checkDummyStr(msg.getF090_4_OrigAcqInstId())))
							.and(unmo.b033FwdIns.eq(msg.checkDummyStr(msg.getF090_5_OrigFwdInstId())))
							.and(unmo.logBizDate.eq(context.getTxnInfo().getBizDate()))
						    // 新增
							.and(unmo.b002CardNbr.eq(request.getBody(2)))
							.and(unmo.txnDirection.eq(AuthTransDirection.Normal))
						    // 移除
						    //.and(unmo.b004Amt.eq(msg.getF04_Amount()))
							//.and(unmo.authTxnStatus.eq(AuthTransStatus.N))
							//.and(unmo.finalAction.eq(AuthAction.A))
						).iterator();
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
		// 1.1 检查步骤一：原交易集合为空；则返回Null
		CcsAuthmemoO orig = authCommonService.checkOrigListIsNotNull(it, false);
		/**
		 * 1.2检查步骤二：
		 * 1)-原交易被拒绝（FinalAction != A ）且 状态为（AuthTxnStatus == E）；则返回 - 无效交易(12)
		 * 2)-原交易状态为（AuthTxnStatus != N）；则返回 - 故障交易(22)
		 */
		authCommonService.checkOrigStatusIsInvalid(orig);
		// 1.3 检查步骤三：原交易状态正常（AuthTxnStatus=N）且交易金额不等；则返回原是交易金额不匹配（64）
		authCommonService.checkOrigTxnAmtIsNotEqual(orig, context.getTxnInfo());
		context.getTxnInfo().setChbTransAmt(orig.getChbTxnAmt());
		context.getTxnInfo().setChbCurr(orig.getChbCurrency());
		return orig;
	}
}
