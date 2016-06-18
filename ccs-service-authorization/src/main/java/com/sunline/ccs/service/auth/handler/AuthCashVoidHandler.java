package com.sunline.ccs.service.auth.handler;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sunline.ppy.api.CustomAttributesKey;
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
 * @see 类名：AuthCashVoidHandler
 * @see 描述： 借记[取现]撤销 (单信息100，双信息200)
 *
 * @see 创建日期：   2015年6月24日下午3:28:27
 * @author liruilin
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class AuthCashVoidHandler extends AuthVoidReverseHandler {
	@Autowired
	private RCcsAuthmemoO rCcsAuthmemoO;
	
	@Override
	protected Iterator<CcsAuthmemoO> findOriginalTrans(YakMessage request, AuthContext context) {
		TxnInfo txnInfo = context.getTxnInfo(); 
		QCcsAuthmemoO unmo = QCcsAuthmemoO.ccsAuthmemoO;
		CupMsg msg = new CupMsg(request);
		Iterator<CcsAuthmemoO> it = null;
		if(request.getCustomAttributes().get(CustomAttributesKey.MTI).toString().substring(0,2).equals("01")){
			//单信息
			it = rCcsAuthmemoO.findAll(
					 unmo.b002CardNbr.eq(msg.checkDummyStr(request.getBody(2)))
					.and(unmo.authCode.eq(msg.checkDummyStr(request.getBody(38))))
					.and(unmo.b042MerId.eq(msg.checkDummyStr(request.getBody(42))))
					.and(unmo.logBizDate.eq(txnInfo.getBizDate()))
					// 新增
					.and(unmo.txnDirection.eq(AuthTransDirection.Normal))
					// 移除
					//.and(unmo.b004Amt.eq(msg.getF04_Amount()))
					//.and(unmo.authTxnStatus.eq(AuthTransStatus.N))
					//.and(unmo.finalAction.eq(AuthAction.A))
					).iterator();
		}
		if(request.getCustomAttributes().get(CustomAttributesKey.MTI).toString().substring(0,2).equals("02")){
					//双信息
			it = rCcsAuthmemoO.findAll(
			 			 unmo.b032AcqInst.eq(request.getBody(32).trim())
						.and(unmo.b033FwdIns.eq(request.getBody(33).trim()))
						//B090OrigData拆分
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
		}
		return it;
	}

	@Override
	protected void updateUnmatchAmt(AuthContext context) {
		CcsAcctO account =context.getAccount();
	 	
		//减去未匹配取现金额
		account.setMemoCash(account.getMemoCash().subtract(context.getTxnInfo().getChbTransAmt()));
		//减去未匹配借记总金额
		account.setMemoDb(account.getMemoDb().subtract(context.getTxnInfo().getChbTransAmt()));

	}
	
	@Override
	protected void updateStatistics(AuthContext context) {
		CcsCardO card = context.getCard();
		TxnInfo txnInfo = context.getTxnInfo();
		//统计值更新
		//减去 当期消费取现总金额  CTD_USED_AMT(废弃) 
		//card.setCtdUsedAmt(card.getCtdUsedAmt().subtract(txnInfo.getChbTransAmt()));

		//交易类型专门统计更新
		//减去单日取现金额DAY_USED_CASH_AMT
		card.setDayUsedCashAmt(card.getDayUsedCashAmt().subtract(txnInfo.getChbTransAmt()));
		//减去单日取现笔数DAY_USED_CASH_NBR 
		card.setDayUsedCashNbr(card.getDayUsedCashNbr()-1);
		//减去当期取现总金额CTD_CASH_AMT 
		card.setCtdCashAmt(card.getCtdCashAmt().subtract(txnInfo.getChbTransAmt()));
		
		if(txnInfo.getTransTerminal() == AuthTransTerminal.ATM){
			card.setDayUsedAtmAmt(card.getDayUsedAtmAmt().subtract(txnInfo.getChbTransAmt()));
			card.setDayUsedAtmNbr(card.getDayUsedAtmNbr() - 1 );
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
	}
 
	@Override
	protected void updateOriginalInfo(CcsAuthmemoO unmatch, AuthContext context) {
		unmatch.setAuthTxnStatus(AuthTransStatus.V);
		if(unmatch.getVoidCnt() == null){
			unmatch.setVoidCnt( 1);
		}else {
			unmatch.setVoidCnt(unmatch.getVoidCnt() + 1);
		}
		unmatch.setLastReversalDate(context.getTxnInfo().getBizDate());
	}
}
