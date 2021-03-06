package com.sunline.ccs.service.auth.handler;


import java.util.Date;
import java.util.Iterator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sunline.ppy.dictionary.enums.AuthTransStatus;
import com.sunline.ppy.dictionary.enums.InputSource;
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
 * @see 类名：AuthXFRoutDepositeVoidedReverseHandler
 * @see 描述：借记[转出]撤销的冲正
 *
 * @see 创建日期：   2015年6月24日下午3:42:14
 * @author liruilin
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class AuthXFRoutDepositeVoidedReverseHandler extends AuthVoidedReverseHandler {
	@Autowired
	private RCcsAuthmemoO rCcsAuthmemoO;
	
	@Override
	protected Iterator<CcsAuthmemoO> findOriginalTrans(YakMessage request, AuthContext context) {
		TxnInfo txnInfo = context.getTxnInfo(); 
		QCcsAuthmemoO unmo = QCcsAuthmemoO.ccsAuthmemoO;
		CupMsg msg = new CupMsg(request);
		return rCcsAuthmemoO.findAll(
							 unmo.b032AcqInst.eq(request.getBody(32).trim())
						.and(unmo.b033FwdIns.eq(request.getBody(33).trim()))
						// B090OrigData拆分
						.and(unmo.b011Trace.eq(msg.checkDummyStr(msg.getF090_2_OrigSysTraceId())))
						.and(unmo.b007TxnTime.eq(msg.checkDummyStr(msg.getF090_3_OrigSysDateTime())))
						.and(unmo.b032AcqInst.eq(msg.checkDummyStr(msg.getF090_4_OrigAcqInstId())))
						.and(unmo.b033FwdIns.eq(msg.checkDummyStr(msg.getF090_5_OrigFwdInstId())))
						.and(unmo.logBizDate.eq(txnInfo.getBizDate()))
						.and(unmo.b003ProcCode.eq(msg.checkDummyStr(request.getBody(3))))
						// 新增
						.and(unmo.b002CardNbr.eq(request.getBody(2)))
						// 撤销的冲正，查询的原交易方向为撤销
						.and(unmo.txnDirection.eq(AuthTransDirection.Revocation))
						// 移除
						//.and(unmo.b004Amt.eq(msg.getF04_Amount()))
						//.and(unmo.authTxnStatus.eq(AuthTransStatus.N))
						//.and(unmo.finalAction.eq(AuthAction.A))
						).iterator();
	}
	
	@Override
	protected void updateUnmatchAmt(AuthContext context) {
		CcsAcctO account =context.getAccount();
		//增加未匹配取现金额
		account.setMemoCash(account.getMemoCash().add(context.getTxnInfo().getChbTransAmt()));
		//增加未匹配借记总金额
		account.setMemoDb(account.getMemoDb().add(context.getTxnInfo().getChbTransAmt()));
	}
	
	
	@Override
	protected void updateStatistics(AuthContext context) {
		CcsCardO card = context.getCard();
		TxnInfo txnInfo = context.getTxnInfo();
		//统计值更新
		//增加 当期消费取现总金额  CTD_USED_AMT(废弃) 
		// card.setCtdUsedAmt(card.getCtdUsedAmt().add(txnInfo.getChbTransAmt()));

		//交易类型专门统计更新
		//增加单日取现金额DAY_USED_CASH_AMT
		card.setDayUsedCashAmt(card.getDayUsedCashAmt().add(txnInfo.getChbTransAmt()));
		//增加单日取现笔数DAY_USED_CASH_NBR 
		card.setDayUsedCashNbr(card.getDayUsedCashNbr()+1);
		//增加当期取现总金额CTD_CASH_AMT 
		card.setCtdCashAmt(card.getCtdCashAmt().add(txnInfo.getChbTransAmt()));
		
		if(txnInfo.getTransTerminal() == AuthTransTerminal.ATM){
			card.setDayUsedAtmAmt(card.getDayUsedAtmAmt().add(txnInfo.getChbTransAmt()));
			card.setDayUsedAtmNbr(card.getDayUsedAtmNbr() + 1 );
		}
		
		// 更新银联境外atm取现单日累计
		if(txnInfo.getInputSource() == InputSource.CUP
				&& txnInfo.getTransTerminal() == AuthTransTerminal.ATM
				&& txnInfo.isCupXborder()){
			card.setDayUsedAtmCupxbAmt(card.getDayUsedAtmCupxbAmt().add(txnInfo.getChbTransAmt()));
		}
	}

	@Override
	protected void updateOriginalInfo(CcsAuthmemoO orig, CcsAuthmemoO origVoid , AuthContext context) {
		// 将原交易的状态从已完成改为normal;
		orig.setAuthTxnStatus(AuthTransStatus.N);
		// 更新撤销交易状态
		origVoid.setAuthTxnStatus(AuthTransStatus.R);
		origVoid.setLastReversalDate(context.getTxnInfo().getBizDate());
	}
}
