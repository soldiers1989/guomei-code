package com.sunline.ccs.service.auth.handler;

import java.util.Iterator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sunline.ppy.dictionary.enums.AuthTransStatus;
import com.sunline.ppy.dictionary.enums.AuthTransType;
import com.sunline.ccs.infrastructure.server.repos.RCcsAuthmemoO;
import com.sunline.ccs.infrastructure.shared.model.QCcsAuthmemoO;
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
 * @see 类名：PreAuthVoidedReverseHandler
 * @see 描述：[预授权或预授权完成]的撤销的冲正
 *
 * @see 创建日期：   2015年6月24日下午3:44:19
 * @author liruilin
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class PreAuthVoidedReverseHandler implements Handler {

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
		// 撤销交易
		CcsAuthmemoO origVoid = checkOrigStatistics(request, context);
		// 根据撤销交易的主键，查询原预授权或预授权完成交易
		CcsAuthmemoO origAuth = rCcsAuthmemoO.findOne(unmo.logKv.eq(origVoid.getOrigLogKv()==null ? 0 :origVoid.getOrigLogKv()));
		// 原交易
		CcsAuthmemoO orig = null;
		
		// 原[预授权]或[预授权完成]交易是否为空
		if (null == origAuth) {
			throw new AuthException(AuthReason.R009, pcs.getAuthDefaultAction(AuthReason.R009));
		}
		// 判断是[预授权]交易
		if (origAuth.getTxnType() == AuthTransType.PreAuth) {
			context.getTxnInfo().setTransType(AuthTransType.PreAuth);
			// 将原交易的状态从已撤销改为normal;
			origAuth.setAuthTxnStatus(AuthTransStatus.N);
			// 更新撤销交易状态
			origVoid.setAuthTxnStatus(AuthTransStatus.R);
			// 加未匹配借记金额
			context.getAccount().setMemoDb(context.getAccount().getMemoDb().add(context.getTxnInfo().getChbTransAmt()));
		}
		// 判断是[预授权完成]交易
		else if (origAuth.getTxnType() == AuthTransType.PAComp) {
			context.getTxnInfo().setTransType(AuthTransType.PAComp);
			orig = rCcsAuthmemoO.findOne(unmo.logKv.eq(origAuth.getOrigLogKv()==null ? 0 :origAuth.getOrigLogKv()));
			if (null == orig) {
				throw new AuthException(AuthReason.R010, pcs.getAuthDefaultAction(AuthReason.R010));
			}
			// 原预授权-已完成
			orig.setAuthTxnStatus(AuthTransStatus.F);
			// 原预授权完成-正常
			origAuth.setAuthTxnStatus(AuthTransStatus.N);
			// 原预授权完成的撤销-冲正
			origVoid.setAuthTxnStatus(AuthTransStatus.R);
			//减预授权金额加预授权完成金额
			context.getAccount().setMemoDb(context.getAccount().getMemoDb().add(context.getTxnInfo().getChbTransAmt()).subtract(orig.getChbTxnAmt()));
		} else {
			throw new AuthException(AuthReason.R008, pcs.getAuthDefaultAction(AuthReason.R008));
		}
		// 撤销的冲正时间
		origVoid.setLastReversalDate(context.getTxnInfo().getBizDate());

		// 存放撤销交易相关信息
		responseInfo = authCommonService.updateResponseInfo(origVoid);

		// final_upd_amt =完成交易.入账币种金额 CHB_txn_AMT - 预授权交易. CHB_txn_AMT
		//无论是预授权撤销冲正，还是完成的撤销冲正，都取撤销这笔的ChbTxnAmt做最终值
		responseInfo.setFinalUpdAmt(origVoid.getChbTxnAmt().negate());

		responseInfo.setFinalUpdDirection(TransAmtDirection.D.toString());

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
		// 1、查询原交易
		return rCcsAuthmemoO.findAll(
							unmo.b032AcqInst.eq(request.getBody(32).trim())
							.and(unmo.b033FwdIns.eq(request.getBody(33).trim()))
							// B090OrigData拆分
							.and(unmo.b007TxnTime.eq(msg.checkDummyStr(msg.getF090_3_OrigSysDateTime())))
							.and(unmo.b011Trace.eq(msg.checkDummyStr(msg.getF090_2_OrigSysTraceId())))
							.and(unmo.b032AcqInst.eq(msg.checkDummyStr(msg.getF090_4_OrigAcqInstId())))
							.and(unmo.b033FwdIns.eq(msg.checkDummyStr(msg.getF090_5_OrigFwdInstId())))
							.and(unmo.b003ProcCode.eq(msg.checkDummyStr(request.getBody(3))))
							.and(unmo.logBizDate.eq(context.getTxnInfo().getBizDate()))
						    // 新增
							.and(unmo.b002CardNbr.eq(request.getBody(2)))
							.and(unmo.txnDirection.eq(AuthTransDirection.Revocation))
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
