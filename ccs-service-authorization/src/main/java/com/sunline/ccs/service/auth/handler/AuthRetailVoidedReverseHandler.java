package com.sunline.ccs.service.auth.handler;

import java.util.Date;
import java.util.Iterator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sunline.ppy.dictionary.enums.AuthTransStatus;
import com.sunline.ccs.infrastructure.server.repos.RCcsAuthmemoO;
import com.sunline.ccs.infrastructure.shared.model.QCcsAuthmemoO;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsCardO;
import com.sunline.ccs.infrastructure.shared.model.CcsAuthmemoO;
import com.sunline.ccs.param.def.enums.AuthTransDirection;
import com.sunline.ccs.service.auth.context.AuthContext;
import com.sunline.ccs.service.auth.context.CupMsg;
import com.sunline.ccs.service.auth.context.TxnInfo;
import com.sunline.ark.support.service.YakMessage;

/**
 * 
 * @see 类名：AuthRetailVoidedReverseHandler
 * @see 描述：借记[消费]撤销的冲正
 *
 * @see 创建日期：   2015年6月24日下午3:31:32
 * @author liruilin
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class AuthRetailVoidedReverseHandler extends AuthVoidedReverseHandler {
	@Autowired
	private RCcsAuthmemoO rCcsAuthmemoO;
	
	@Override
	protected Iterator<CcsAuthmemoO> findOriginalTrans(YakMessage request, AuthContext context) {
		TxnInfo txnInfo = context.getTxnInfo(); 
		QCcsAuthmemoO unmo = QCcsAuthmemoO.ccsAuthmemoO;
		CupMsg msg = new CupMsg(request);
		// 撤销交易
		return rCcsAuthmemoO.findAll( 
						 	 unmo.b032AcqInst.eq(request.getBody(32).trim())
							.and(unmo.b033FwdIns.eq(request.getBody(33).trim()))
							// B090OrigData拆分
							.and(unmo.b007TxnTime.eq(msg.checkDummyStr(msg.getF090_3_OrigSysDateTime())))
							.and(unmo.b011Trace.eq(msg.checkDummyStr(msg.getF090_2_OrigSysTraceId())))
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
		//加上未匹配借记金额
		account.setMemoDb(account.getMemoDb().add( context.getTxnInfo().getChbTransAmt()));
	}
	
	@Override
	protected void updateStatistics(AuthContext context) {
		CcsCardO card = context.getCard();
		//加上单日消费金额 DAY_USED_RETAIL_AMT 
		card.setDayUsedRetailAmt(card.getDayUsedRetailAmt().add(context.getTxnInfo().getChbTransAmt()));
		//加上单日消费笔数 DAY_USED_RETAIL_NBR
		card.setDayUsedRetailNbr(card.getDayUsedRetailNbr()+1);
		//加上当期消费金额 CTD_USED_AMT 
		card.setCtdUsedAmt(card.getCtdUsedAmt().add(context.getTxnInfo().getChbTransAmt()));
		
		if(context.getTxnInfo().isInternetTrans()){
			card.setCtdNetAmt(card.getCtdNetAmt().add(context.getTxnInfo().getChbTransAmt()))	;
		}
	}

	@Override
	protected void updateOriginalInfo(CcsAuthmemoO orig, CcsAuthmemoO origVoid, AuthContext context) {
		// 将原交易的状态从已完成改为normal;
		orig.setAuthTxnStatus(AuthTransStatus.N);
		// 更新撤销交易状态
		origVoid.setAuthTxnStatus(AuthTransStatus.R);
		origVoid.setLastReversalDate(context.getTxnInfo().getBizDate());
	}
}
