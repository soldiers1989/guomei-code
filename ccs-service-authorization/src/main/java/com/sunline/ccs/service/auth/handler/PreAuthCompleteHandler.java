package com.sunline.ccs.service.auth.handler;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;

import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sunline.ppy.dictionary.enums.AuthTransStatus;
import com.sunline.ccs.infrastructure.server.repos.RCcsAuthmemoO;
import com.sunline.ccs.infrastructure.shared.model.QCcsAuthmemoO;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsCardO;
import com.sunline.ccs.infrastructure.shared.model.CcsAuthmemoO;
import com.sunline.ppy.dictionary.enums.AuthTransType;
import com.sunline.ccs.param.def.enums.AuthReason;
import com.sunline.ccs.param.def.enums.AuthTransDirection;
import com.sunline.ccs.param.def.enums.TransAmtDirection;
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
 * @see 类名：PreAuthCompleteHandler
 * @see 描述：预授权完成
 *
 * @see 创建日期：   2015年6月24日下午3:43:56
 * @author liruilin
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class PreAuthCompleteHandler implements Handler {
	private Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private RCcsAuthmemoO rCcsAuthmemoO;
	@Autowired
	private HandlerCommService pcs;
	@Autowired
	private AuthCommService authCommonService;

	@Override
	public RespInfo handle(YakMessage request, AuthContext context) throws AuthException {
		TxnInfo txnInfo = context.getTxnInfo();
		RespInfo responseInfo;
		//查询原交易
		CcsAuthmemoO orig = checkOrigStatistics(request, context);
		// 原交易为空或者不为空且方向为通知时，按照通知继续处理
		if (orig == null ) {
			// 对于预授权完成通知继续处理
			if (context.getTxnInfo().getTransDirection() == AuthTransDirection.Advice) {
				return origTxnIsPAcompAdvice(request, context);
			} else {
				authCommonService.throwAuthException(AuthReason.R014, "预授权完成时，找不到原交易(25)");
			}
		}
		if(orig != null && orig.getAuthTxnStatus() != AuthTransStatus.N){
			if(context.getTxnInfo().getTransDirection() == AuthTransDirection.Advice){
				return origTxnIsPAcompAdvice(request, context);
			}
			if(context.getTxnInfo().getTransDirection() == AuthTransDirection.Normal){
				authCommonService.throwAuthException(AuthReason.R015, "预授权完成时，故障交易(22)");
			}
		}
		
		//MTI =200 是预授权完成，需检查额度控制，=220 是预授权完成通知，不需要检查
		if(context.getTxnInfo().getTransDirection() == AuthTransDirection.Normal &&
				//	FIXME 明确是否应该使用origChbTxnAmt
				txnInfo.getChbTransAmt().compareTo(orig.getChbTxnAmt().add(orig.getChbTxnAmt().multiply(context.getProductCredit().preathCompTolRt)).setScale(2, BigDecimal.ROUND_HALF_UP)) > 0){
			//TODO 修改reason和acton，现在还没有对应reason
			throw new AuthException(AuthReason.B010, pcs.getAuthDefaultAction(AuthReason.B010));

		}
		// 更新原交易记录
		orig.setAuthTxnStatus(AuthTransStatus.F);
		orig.setFinalAuthAmt(txnInfo.getChbTransAmt());
		// 存放预授权交易相关信息
		responseInfo = authCommonService.updateResponseInfo(orig);
		
		CcsAcctO account =context.getAccount();
		//增加未匹配借记金额 -历史交易金额
		if(orig.getChbTxnAmt() == null){
			orig.setChbTxnAmt(new BigDecimal(0));
		}
			
		account.setMemoDb(account.getMemoDb().add(context.getTxnInfo().getChbTransAmt()).subtract(orig.getChbTxnAmt()));

		//更新累计值
		updateStatistics(context,orig);
		
		responseInfo.setFinalUpdDirection(TransAmtDirection.D.toString());
		//final_upd_amt = 匹配交易.入账币种金额 CHB_txn_AMT  - 预授权交易. CHB_txn_AMT
		responseInfo.setFinalUpdAmt(context.getTxnInfo().getChbTransAmt().subtract(orig.getChbTxnAmt()));
		responseInfo.setB038_AuthCode(request.getBody(38));
		
		return responseInfo;
	}
	
	private void updateStatistics(AuthContext context,CcsAuthmemoO orig){
		try {
			CcsCardO card = context.getCard();
			//原交易业务日期
			Date origBizDate = context.getTxnInfo().getOrigBizDate();
			//账单周期
			int cycle = Integer.valueOf(context.getAccount().getCycleDay().trim());
			//当前业务日期
			Date bizDate = context.getTxnInfo().getBizDate();
			//当月账单日
			Date cycleDate =DateUtils.setDays(bizDate, cycle);
			//上月账单日
			Date prevCycleDate=DateUtils.addMonths(DateUtils.setDays(bizDate, cycle), -1);
			// 判断原交易业务日期是否为空
			if(null == origBizDate){
				return;
			}
			if(origBizDate.compareTo(context.getTxnInfo().getBizDate()) == 0){
				//增加单日消费金额 DAY_USED_RETAIL_AMT减去原交易金额OrigAmt
				card.setDayUsedRetailAmt(card.getDayUsedRetailAmt().add(context.getTxnInfo().getChbTransAmt()).subtract(orig.getOrigChbTxnAmt()));
			}
			
			/* 
			 * 交易大于上一账单周期日 
			 * 业务日期大于当月账单日,且原交易日期大于当月账单日;
			 * 或者
			 * 业务日期小于等于当月账单日,且原交易日期大于上月账单日;
			 */
			if((bizDate.compareTo(cycleDate) > 0 && origBizDate.compareTo(cycleDate) > 0)
			 ||(bizDate.compareTo(cycleDate) <= 0 && origBizDate.compareTo(prevCycleDate) > 0)){
				//增加当期消费金额 CTD_USED_AMT减去原交易金额OrigAmt
				card.setCtdUsedAmt(card.getCtdUsedAmt().add(context.getTxnInfo().getChbTransAmt()).subtract(orig.getOrigChbTxnAmt()));
			}
		} catch (Exception e) {
			// 更新账户统计值时转换异常
			logger.info("更新账户统计值时转换异常");
			return ;
		}
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
									 unmo.b002CardNbr.eq(msg.checkDummyStr(request.getBody(2)))
								.and(unmo.txnType.eq(AuthTransType.PreAuth))
								.and(unmo.authCode.eq(msg.checkDummyStr(request.getBody(38))))
								.and(unmo.b042MerId.eq(msg.checkDummyStr(request.getBody(42))))
								.and(unmo.logBizDate.gt(DateUtils.addDays(context.getTxnInfo().getBizDate(), -45)))
								.and(unmo.txnDirection.eq(AuthTransDirection.Normal))
							    // 新增
								.and(unmo.b002CardNbr.eq(request.getBody(2)))
							    // 移除
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
		CcsAuthmemoO orig = authCommonService.checkOrigListIsNotNull(it, true);
		// 如果原交易为空，返回Null；用于判断预授权完成通知的处理流程
		if(orig == null) return null;
		/**
		 * 1.2检查步骤二：
		 * 1)-原交易被拒绝（FinalAction != A ）且 状态为（AuthTxnStatus == E）；则返回 - 无效交易(12)
		 * 2)-原交易状态为（AuthTxnStatus != N）；则返回 - 故障交易(22)
		 */
		authCommonService.checkOrigStatusIsInvalid(orig);
		
		return orig;
	}
	
	/**
	 * 通知类，按照通知继续处理
	 * @param request
	 * @param context
	 * @return
	 * @throws AuthException
	 */
	protected RespInfo origTxnIsPAcompAdvice(YakMessage request, AuthContext context) throws AuthException {
			context.getAccount().setMemoDb(context.getAccount().getMemoDb().add(context.getTxnInfo().getChbTransAmt()));

			CcsCardO card = context.getCard();
			card.setDayUsedRetailAmt(card.getDayUsedRetailAmt().add(context.getTxnInfo().getChbTransAmt()));
			card.setCtdUsedAmt(card.getCtdUsedAmt().add(context.getTxnInfo().getChbTransAmt()));

			RespInfo responseInfo = new RespInfo();
			responseInfo.setFinalUpdDirection(TransAmtDirection.D.toString());
			// final_upd_amt = 匹配交易.入账币种金额 CHB_txn_AMT
			responseInfo.setFinalUpdAmt(context.getTxnInfo().getChbTransAmt());
			responseInfo.setB038_AuthCode(request.getBody(38));

			return responseInfo;
	}
}
