package com.sunline.ccs.service.auth.handler;

import java.util.Iterator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sunline.ppy.dictionary.enums.AuthTransStatus;
import com.sunline.ccs.infrastructure.server.repos.RCcsAuthmemoO;
import com.sunline.ccs.infrastructure.shared.model.QCcsAuthmemoO;
import com.sunline.ccs.infrastructure.shared.model.CcsAuthmemoO;
import com.sunline.ccs.param.def.enums.AuthTransDirection;
import com.sunline.ccs.param.def.enums.TransAmtDirection;
import com.sunline.ccs.service.auth.common.AuthCommService;
import com.sunline.ccs.service.auth.context.AuthContext;
import com.sunline.ccs.service.auth.context.CupMsg;
import com.sunline.ccs.service.auth.context.RespInfo;
import com.sunline.ccs.service.auth.frame.AuthException;
import com.sunline.ccs.service.auth.frame.Handler;
import com.sunline.ark.support.service.YakMessage;

/**
 * 
 * @see 类名：CreditReverseHandler
 * @see 描述：贷记冲正 (存款)
 *
 * @see 创建日期：   2015年6月24日下午3:42:58
 * @author liruilin
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public  class CreditReverseHandler implements Handler {

	@Autowired
	private RCcsAuthmemoO rCcsAuthmemoO;

	@Autowired
	private AuthCommService authCommonService;

	@Override
	public RespInfo handle(YakMessage request, AuthContext context)
			throws AuthException {
		// 存款交易
		CcsAuthmemoO orig = checkOrigStatistics(request, context);
		// 更新原交易记录
		orig.setAuthTxnStatus(AuthTransStatus.R);
		orig.setLastReversalDate(context.getTxnInfo().getBizDate());
		// 减少未匹配贷记金额
		context.getAccount().setMemoCr(context.getAccount().getMemoCr().subtract((context.getTxnInfo().getChbTransAmt())));
		// 存放原交易记录相关信息
		RespInfo responseInfo = authCommonService.updateResponseInfo(orig);
		responseInfo.setFinalUpdAmt(context.getTxnInfo().getChbTransAmt().negate());
		responseInfo.setFinalUpdDirection(TransAmtDirection.C.toString());
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
					    // 新增
						.and(unmo.b002CardNbr.eq(request.getBody(2)))
						.and(unmo.txnDirection.eq(AuthTransDirection.Normal))
					    // 移除
						//.and(unmo.txnType.in(AuthTransType.Credit,AuthTransType.TransferCredit))
					    //.and(unmo.logBizDate.eq(txnInfo.getBizDate()))
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
		// 1.4 检查步骤三：原交易状态正常（AuthTxnStatus=N）且交易金额不等；则返回原是交易金额不匹配（64）
		authCommonService.checkOrigTxnAmtIsNotEqual(orig, context.getTxnInfo());
		
		return orig;
	}

}
