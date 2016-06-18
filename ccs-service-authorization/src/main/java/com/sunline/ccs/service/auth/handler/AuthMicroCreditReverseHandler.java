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
import com.sunline.ppy.dictionary.enums.InputSource;
import com.sunline.ppy.dictionary.enums.LoanRegStatus;
import com.sunline.ppy.dictionary.enums.ManualAuthFlag;
import com.sunline.ccs.infrastructure.server.repos.RCcsAuthmemoO;
import com.sunline.ccs.infrastructure.shared.model.QCcsAuthmemoO;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsCardO;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsAuthmemoO;
import com.sunline.ccs.param.def.enums.AuthTransDirection;
import com.sunline.ccs.service.auth.common.AuthCommService;
import com.sunline.ccs.service.auth.context.AuthContext;
import com.sunline.ccs.service.auth.context.CupMsg;
import com.sunline.ccs.service.auth.context.TxnInfo;
import com.sunline.ccs.service.auth.frame.AuthException;
import com.sunline.ark.support.service.YakMessage;

/**
 * 
 * @see 类名：AuthMicroCreditReverseHandler
 * @see 描述：[分期]冲正
 *
 * @see 创建日期：   2015年6月24日下午3:30:08
 * @author liruilin
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class AuthMicroCreditReverseHandler extends AuthVoidReverseHandler {
	@Autowired
	private RCcsAuthmemoO rCcsAuthmemoO;
	@Autowired
	private AuthCommService authCommonService;
	
	private Logger log = LoggerFactory.getLogger(getClass());

	@Override
	protected Iterator<CcsAuthmemoO> findOriginalTrans(YakMessage request,
			AuthContext context) {
		TxnInfo txnInfo = context.getTxnInfo();
		QCcsAuthmemoO unmo = QCcsAuthmemoO.ccsAuthmemoO;
		CupMsg msg = new CupMsg(request);
		Iterator<CcsAuthmemoO> it;
		if (context.getTxnInfo().getInputSource() == InputSource.CUP 
				|| (context.getTxnInfo().getInputSource() == InputSource.BANK && context.getTxnInfo().getManualAuthFlag() == ManualAuthFlag.N)) {
			it = rCcsAuthmemoO.findAll( 
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
					//.and(unmo.b004Amt.eq(msg.getF04_Amount()))
					//.and(unmo.authTxnStatus.eq(AuthTransStatus.N))
					//.and(unmo.finalAction.eq(AuthAction.A))
					).iterator();
		} else {
			it = rCcsAuthmemoO.findAll( 
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
					//.and(unmo.b004Amt.eq(msg.getF04_Amount()))
					//.and(unmo.authTxnStatus.eq(AuthTransStatus.N))
					//.and(unmo.finalAction.eq(AuthAction.A))
					).iterator();
		}
		
		// 更新统计值时用于比较当前交易与原交易的业务日期差值
		// if(null != orig){
		// context.getTransInfo().setOrigBizDate(orig.getLogBizDate());
		// }
		
		return it;
	}

	@Override
	protected void updateUnmatchAmt(AuthContext context) {
		CcsAcctO account = context.getAccount();
		// 减去未匹配借记金额
		account.setMemoDb(account.getMemoDb().subtract(
				context.getTxnInfo().getChbTransAmt()));
		if(account.getMemoDb().compareTo(BigDecimal.ZERO) <0){
			log.debug("----账户[未匹配借记金额]经过处理流程后小于[零],此处将[未匹配借记金额]与[零]修正----");
			account.setMemoDb(BigDecimal.ZERO);
		}
	}

	@Override
	protected void updateStatistics(AuthContext context) {
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
				// 减去单日消费金额 DAY_USED_RETAIL_AMT
				card.setDayUsedRetailAmt(card.getDayUsedRetailAmt().subtract(context.getTxnInfo().getChbTransAmt()));
				// 减去单日消费笔数 DAY_USED_RETAIL_NBR
				card.setDayUsedRetailNbr(card.getDayUsedRetailNbr() - 1);
				
				if(context.getTxnInfo().isInternetTrans()){
			 		card.setCtdNetAmt(card.getCtdNetAmt().subtract(context.getTxnInfo().getChbTransAmt()));
				}
			}
			
			/* 
			 * 交易大于上一账单周期日 
			 * 业务日期大于当月账单日,且原交易日期大于当月账单日;
			 * 或者
			 * 业务日期小于等于当月账单日,且原交易日期大于上月账单日;
			 */
			if((bizDate.compareTo(cycleDate) > 0 && origBizDate.compareTo(cycleDate) > 0)
				||(bizDate.compareTo(cycleDate) <= 0 && origBizDate.compareTo(prevCycleDate) > 0)){
				// 减去当期取现总金额CTD_CASH_AMT
				card.setCtdUsedAmt(card.getCtdUsedAmt().subtract(context.getTxnInfo().getChbTransAmt()));
			}
			
			// // 减去单日消费金额 DAY_USED_RETAIL_AMT
			// card.setDayUsedRetailAmt(card.getDayUsedRetailAmt().subtract(context.getTransInfo().getChbTransAmt()));
			// // 减去单日消费笔数 DAY_USED_RETAIL_NBR
			// card.setDayUsedRetailNbr(card.getDayUsedRetailNbr() - 1);
			// // 减去当期消费金额 CTD_USED_AMT
			// card.setCtdUsedAmt(card.getCtdUsedAmt().subtract(context.getTransInfo().getChbTransAmt()));
			// if(context.getTransInfo().isInternetTrans()){
			// 		card.setCtdNetAmt(card.getCtdNetAmt().subtract(context.getTransInfo().getChbTransAmt()));
			// }
		} catch (Exception e) {
			// 更新账户统计值时转换异常
			log.info("更新账户统计值时转换异常");
			return ;
		}
	}

	@Override
	protected void updateOriginalInfo(CcsAuthmemoO unmatch , AuthContext context) throws AuthException {
		/**
		 * 分期处理：冲正交易改变LOAN_REG表中的分期注册状态为[V]
		 */
		CcsLoanReg loanReg = authCommonService.findLoanRegWithB7B11B32B33(unmatch,LoanRegStatus.A,context.getTxnInfo().getManualAuthFlag());
		if(loanReg != null){
			loanReg.setLoanRegStatus(LoanRegStatus.V);
		}
		
		// 更新原交易记录
		unmatch.setAuthTxnStatus(AuthTransStatus.R);
		unmatch.setLastReversalDate(context.getTxnInfo().getBizDate());
	}
}
