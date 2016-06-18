package com.sunline.ccs.service.auth.handler;

import java.util.Date;
import java.util.Iterator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sunline.ccs.infrastructure.server.repos.RCcsAuthmemoO;
import com.sunline.ccs.infrastructure.shared.model.CcsAuthmemoO;
import com.sunline.ccs.param.def.enums.AuthReason;
import com.sunline.ccs.param.def.enums.TransAmtDirection;
import com.sunline.ccs.service.auth.common.AuthCommService;
import com.sunline.ccs.service.auth.context.AuthContext;
import com.sunline.ccs.service.auth.context.RespInfo;
import com.sunline.ccs.service.auth.frame.AuthException;
import com.sunline.ccs.service.auth.frame.Handler;
import com.sunline.ccs.service.auth.frame.HandlerCommService;
import com.sunline.ark.support.service.YakMessage;

/**
 * 
 * @see 类名：AuthVoidedReverseHandler
 * @see 描述：借记撤销的冲正总流程
 *
 * @see 创建日期：   2015年6月24日下午3:37:10
 * @author liruilin
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public abstract class AuthVoidedReverseHandler implements Handler {

	@Autowired
	private HandlerCommService pcs;

	@Autowired
	private RCcsAuthmemoO rCcsAuthmemoO;

	@Autowired
	private AuthCommService authCommonService;

	@Override
	public RespInfo handle(YakMessage request, AuthContext context) throws AuthException {
		RespInfo responseInfo;
		
		// 1、检查并返回原交易信息
		CcsAuthmemoO origVoid = checkOrigStatistics(request , context);

		// 2、根据撤销交易的原交易键值，获取原交易
		CcsAuthmemoO orig = rCcsAuthmemoO.findOne(origVoid.getOrigLogKv()==null ? 0 :origVoid.getOrigLogKv());
		if (orig == null) {
			throw new AuthException(AuthReason.R014, pcs.getAuthDefaultAction(AuthReason.R014));
		} else {
			// 3、 更新撤销交易信息和撤销交易的原交易信息
			updateOriginalInfo(orig, origVoid, context);
			// 4、更新账户表未匹配金额
			updateUnmatchAmt(context);
			// 5、更新卡表累计信息
			updateStatistics(context);
			// 6、获取原交易记录相关信息返回供日志记录
			responseInfo = authCommonService.updateResponseInfo(origVoid);

			responseInfo.setFinalUpdDirection(TransAmtDirection.D.toString());
			responseInfo.setFinalUpdAmt(context.getTxnInfo().getChbTransAmt());
		}

		return responseInfo;
	}

	/**
	 * 匹配原交易
	 * 
	 * @param request
	 * @param context
	 * @return
	 */
	protected abstract Iterator<CcsAuthmemoO> findOriginalTrans(YakMessage request, AuthContext context);

	/**
	 * 更新撤销交易信息和撤销交易的原交易信息
	 * 
	 * @param context
	 * @throws AuthException 
	 */
	protected abstract void updateOriginalInfo(CcsAuthmemoO orig, CcsAuthmemoO origVoid, AuthContext context) throws AuthException;

	/**
	 * 更新账户表未匹配金额
	 * 
	 * @param context
	 */
	protected abstract void updateUnmatchAmt(AuthContext context)throws AuthException;

	/**
	 * 更新卡表累计信息
	 * 
	 * @param context
	 */
	protected abstract void updateStatistics(AuthContext context)throws AuthException;

	/**
	 * 检查并返回原交易信息
	 * @param request
	 * @param context
	 * @return
	 * @throws AuthException
	 */
	protected CcsAuthmemoO checkOrigStatistics(YakMessage request , AuthContext context) throws AuthException{
		// 1、匹配原交易
		Iterator<CcsAuthmemoO> it = findOriginalTrans(request, context);
		// 1.1 检查步骤一：原交易集合为空；则返回-找不到原交易（25）
		CcsAuthmemoO origVoid = authCommonService.checkOrigListIsNotNull(it, false);
		/**
		 * 1.2检查步骤二：
		 * 1)-原交易被拒绝（FinalAction != A ）且 状态为（AuthTxnStatus == E）；则返回 - 无效交易(12)
		 * 2)-原交易状态为（AuthTxnStatus != N）；则返回 - 故障交易(22)
		 */
		authCommonService.checkOrigStatusIsInvalid(origVoid);
		// 1.4 检查步骤四：原交易状态正常（AuthTxnStatus=N）且交易金额不等；则返回原是交易金额不匹配（64）
		authCommonService.checkOrigTxnAmtIsNotEqual(origVoid, context.getTxnInfo());
		// 1.5、更新统计值时用于比较当前交易与原交易的业务日期差值
		context.getTxnInfo().setOrigBizDate(origVoid.getLogBizDate());
		// 银联撤销不上送入账金额入账币种，使用原交易的金额币种
		context.getTxnInfo().setChbTransAmt(origVoid.getChbTxnAmt());
		context.getTxnInfo().setChbCurr(origVoid.getChbCurrency());
		return origVoid;
	}
}
