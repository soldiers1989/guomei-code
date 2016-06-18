package com.sunline.ccs.service.auth.handler;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sunline.ppy.dictionary.enums.AuthTransStatus;
import com.sunline.ppy.dictionary.enums.AuthTransType;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ccs.param.def.enums.TransAmtDirection;
import com.sunline.ccs.infrastructure.server.repos.RCcsAuthmemoO;
import com.sunline.ccs.infrastructure.shared.model.QCcsAuthmemoO;
import com.sunline.ccs.infrastructure.shared.model.CcsAuthmemoO;
import com.sunline.ccs.param.def.enums.AuthAction;
import com.sunline.ccs.param.def.enums.AuthReason;
import com.sunline.ccs.param.def.enums.AuthTransDirection;
import com.sunline.ccs.service.auth.common.AuthCommService;
import com.sunline.ccs.service.auth.context.AuthContext;
import com.sunline.ccs.service.auth.context.CupMsg;
import com.sunline.ccs.service.auth.context.RespInfo;
import com.sunline.ccs.service.auth.frame.AuthException;
import com.sunline.ccs.service.auth.frame.Handler;
import com.sunline.ark.support.service.YakMessage;

/**
 * 
 * @see 类名：CreditConfirmHandler
 * @see 描述：L200-confirm-credit 贷记确认
 *
 * @see 创建日期：   2015年6月24日下午3:42:37
 * @author liruilin
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class CreditConfirmHandler implements Handler {
	private Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private RCcsAuthmemoO rCcsAuthmemoO;
	@Autowired
	private AuthCommService authCommonService;

	@Override
	public RespInfo handle(YakMessage request, AuthContext context) throws AuthException {
		// 1、查询原存款交易
		CcsAuthmemoO orig = checkOrigStatistics(request, context);
		
		// 2、判断原存款交易是否空
		if (null == orig) {
			// 3、原存款交易为空则判断是否支持孤立确认
			return isolatedConfirm(request, context);
		} else {
			// 4、正常处理
			if (orig.getAuthTxnStatus().equals(AuthTransStatus.N)) {
				orig.setAuthTxnStatus(AuthTransStatus.D);
			}
			RespInfo responseInfo = authCommonService.updateResponseInfo(orig);
			responseInfo.setFinalUpdDirection(TransAmtDirection.C.toString());
			return responseInfo;
		}
	}
	
	/**
	 * 孤立确认处理流程
	 * @param request
	 * @param context
	 * @param msg
	 * @param unmo
	 * @return
	 * @throws AuthException
	 */
	public RespInfo isolatedConfirm(YakMessage request, AuthContext context) throws AuthException{
		// 1、判断是否支持孤立确认
		if(Indicator.Y != context.getAuthProduct().isolatedConfirm){
			authCommonService.throwAuthException(AuthReason.R014, "当前存款确认交易不支持孤立确认，交易拒绝：没有找到原交易");
		}
		
		// 2、查询确认交易
		CupMsg msg = new CupMsg(request);
		QCcsAuthmemoO unmo = QCcsAuthmemoO.ccsAuthmemoO;
		CcsAuthmemoO creditConfirm = rCcsAuthmemoO.findOne(
				     unmo.txnDirection.eq(AuthTransDirection.Confirm)
					.and(unmo.b032AcqInst.eq(request.getBody(32).trim()))
					.and(unmo.b033FwdIns.eq(request.getBody(33).trim()))
					.and(unmo.b090OrigData.eq(msg.checkDummyStr(request.getBody(90))))
					.and(unmo.logBizDate.eq(context.getTxnInfo().getBizDate()))
					.and(unmo.b004Amt.eq(msg.getF004_Amount()))
					.and(unmo.finalAction.eq(AuthAction.A)));
		// 3、判断是否确认；如果找到确认交易则返回已确认
		if( null != creditConfirm){
			authCommonService.throwAuthException(AuthReason.R012, "原交易已确认");
		}
		// 4、未匹配到则首次孤立确认,执行贷记交易处理内容 返回正常即可
		RespInfo responseInfo = new RespInfo();
		// 5、增加未匹配贷记金额
		context.getAccount().setMemoCr(context.getAccount().getMemoCr().add(context.getTxnInfo().getChbTransAmt()));
		responseInfo.setFinalUpdDirection(TransAmtDirection.C.toString());
		responseInfo.setFinalUpdAmt(context.getTxnInfo().getChbTransAmt());
		// 6、增加孤立确认标识，记录日志时判断
		responseInfo.setIsolatedConfirm(true);
		
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
				     unmo.txnType.in(AuthTransType.Credit , AuthTransType.AgentCredit , AuthTransType.TransferCredit)
					.and(unmo.b032AcqInst.eq(request.getBody(32).trim()))
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
					//.and(unmo.b004Amt.eq(msg.getF04_Amount()))
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
		CcsAuthmemoO orig = authCommonService.checkOrigListIsNotNull(it, true);
		// 原交易为空时，判断孤立确认
		if(orig == null){
			return null;
		}
		// 原交易为存款确认且交易时间不等于当前时间
		if(orig.getLogBizDate().compareTo(context.getTxnInfo().getBizDate()) != 0){
			authCommonService.throwAuthException(AuthReason.R013, "隔日确认交易不支持");
		}
		// 原交易是否为已确认交易
		if (orig.getAuthTxnStatus() == AuthTransStatus.D) {
			authCommonService.throwAuthException(AuthReason.R012, "原交易已确认");
		}
		try {
			/**
			 * 1.2检查步骤二：
			 * 1)-原交易被拒绝（FinalAction != A ）且 状态为（AuthTxnStatus == E）；则返回 - 无效交易(12)
			 * 2)-原交易状态为（AuthTxnStatus != N）；则返回 - 故障交易(22)
			 */
			authCommonService.checkOrigStatusIsInvalid(orig);
			// 1.3 检查步骤三：原交易状态正常（AuthTxnStatus=N）且交易金额不等；则返回原是交易金额不匹配（64）
			authCommonService.checkOrigTxnAmtIsNotEqual(orig, context.getTxnInfo());
		} catch (AuthException e) {
			logger.debug("######### 当前存款确认交易的原交易状态异常，默认：交易通过");
		}
		return orig;
	}
}
