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
import com.sunline.ppy.dictionary.enums.ManualAuthFlag;
import com.sunline.ccs.infrastructure.server.repos.RCcsAuthmemoO;
import com.sunline.ccs.infrastructure.shared.model.QCcsAuthmemoO;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsCardO;
import com.sunline.ccs.infrastructure.shared.model.CcsAuthmemoO;
import com.sunline.ccs.param.def.enums.AuthTransDirection;
import com.sunline.ccs.param.def.enums.AuthTransTerminal;
import com.sunline.ccs.service.auth.context.AuthContext;
import com.sunline.ccs.service.auth.context.CupMsg;
import com.sunline.ccs.service.auth.context.TxnInfo;
import com.sunline.ark.support.service.YakMessage;

/**
 * 
 * @see 类名：AuthXFRoutDepositeReverseHandler
 * @see 描述：借记[消费]冲正
 *
 * @see 创建日期：   2015年6月24日下午3:42:01
 * @author liruilin
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class AuthXFRoutDepositeReverseHandler extends AuthVoidReverseHandler {
	@Autowired
	private RCcsAuthmemoO rCcsAuthmemoO;
	
	private Logger log = LoggerFactory.getLogger(getClass());
	
	@Override
	protected Iterator<CcsAuthmemoO> findOriginalTrans(YakMessage request,
			AuthContext context) {
		TxnInfo txnInfo = context.getTxnInfo();
		QCcsAuthmemoO unmo = QCcsAuthmemoO.ccsAuthmemoO;
		CupMsg msg = new CupMsg(request);
		Iterator<CcsAuthmemoO> it;
		if (context.getTxnInfo().getInputSource() == InputSource.CUP || (context
				.getTxnInfo().getInputSource() == InputSource.BANK
				&& context.getTxnInfo().getManualAuthFlag() == ManualAuthFlag.N)) {
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
		account.setMemoCash(account.getMemoCash().subtract(context.getTxnInfo().getChbTransAmt()));
		account.setMemoDb(account.getMemoDb().subtract(context.getTxnInfo().getChbTransAmt()));
	}

	@Override
	protected void updateStatistics(AuthContext context) {
		try {
			CcsCardO card = context.getCard();
			TxnInfo txnInfo = context.getTxnInfo();
			//原交易业务日期
			Date origBizDate = txnInfo.getOrigBizDate();
			//账单周期
			int cycle = Integer.valueOf(context.getAccount().getCycleDay().trim());
			//当前业务日期
			Date bizDate = txnInfo.getBizDate();
			//当月账单日
			Date cycleDate =DateUtils.setDays(bizDate, cycle);
			//上月账单日
			Date prevCycleDate=DateUtils.addMonths(DateUtils.setDays(bizDate, cycle), -1);
			// 判断原交易业务日期是否为空
			if(null == origBizDate){
				return;
			}
			if(origBizDate.compareTo(txnInfo.getBizDate()) == 0){
				card.setDayUsedCashAmt(card.getDayUsedCashAmt().subtract(txnInfo.getChbTransAmt()));
				// 减去单日取现笔数DAY_USED_CASH_NBR
				card.setDayUsedCashNbr(card.getDayUsedCashNbr() - 1);
				
				if(txnInfo.getTransTerminal() == AuthTransTerminal.ATM){
					card.setDayUsedAtmAmt(card.getDayUsedAtmAmt().subtract(txnInfo.getChbTransAmt()));
					card.setDayUsedAtmNbr(card.getDayUsedAtmNbr() - 1 );
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
				card.setCtdCashAmt(card.getCtdCashAmt().subtract(context.getTxnInfo().getChbTransAmt()));
			}
			
			// 更新银联境外atm取现单日累计
			if(txnInfo.getInputSource() == InputSource.CUP
					&& txnInfo.getTransTerminal() == AuthTransTerminal.ATM
					&& txnInfo.isCupXborder()){
				// 银联境外atm取现单日累计 -=  chbtxnamt
				card.setDayUsedAtmCupxbAmt(card.getDayUsedAtmCupxbAmt().subtract(txnInfo.getChbTransAmt()));
				//if(银联境外atm取现单日累计 <0) {
				//	银联境外atm取现单日累计 = 0
				//}
				if(card.getDayUsedAtmCupxbAmt().compareTo(BigDecimal.ZERO) < 0  ){
					card.setDayUsedAtmCupxbAmt(BigDecimal.ZERO);
				}
			}
			
			// //统计值更新
			// //减去 当期消费取现总金额 CTD_USED_AMT(废弃)
			// //card.setCtdUsedAmt(card.getCtdUsedAmt().subtract(context.getTransInfo().getChbTransAmt()));
			// //交易类型专门统计更新
			// //减去单日取现金额DAY_USED_CASH_AMT
			// card.setDayUsedCashAmt(card.getDayUsedCashAmt().subtract(context.getTransInfo().getChbTransAmt()));
			// //减去单日取现笔数DAY_USED_CASH_NBR
			// card.setDayUsedCashNbr(card.getDayUsedCashNbr() - 1);
			// //减去当期取现总金额CTD_CASH_AMT
			// card.setCtdCashAmt(card.getCtdCashAmt().subtract(context.getTransInfo().getChbTransAmt()));
			// if(context.getTransInfo().getTransTerminal() == AuthTransTerminal.ATM){
			//		card.setDayUsedAtmAmt(card.getDayUsedAtmAmt().subtract(context.getTransInfo().getChbTransAmt()));
			//		card.setDayUsedAtmNbr(card.getDayUsedAtmNbr() - 1 );
			// }
		} catch (Exception e) {
			// 更新账户统计值时转换异常
			log.error("更新账户统计值时转换异常", e);
			return ;
		}
	}

	@Override
	protected void updateOriginalInfo(CcsAuthmemoO unmatch , AuthContext context) {
		// 更新原交易记录
		unmatch.setAuthTxnStatus(AuthTransStatus.R);
		unmatch.setLastReversalDate(context.getTxnInfo().getBizDate());
	}
}
