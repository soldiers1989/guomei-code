package com.sunline.ccs.service.auth.handler;

import java.util.Date;
import java.util.Iterator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sunline.ppy.api.CustomAttributesKey;
import com.sunline.ppy.dictionary.enums.AuthTransStatus;
import com.sunline.ppy.dictionary.enums.AuthTransType;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ccs.infrastructure.server.repos.RCcsAuthmemoO;
import com.sunline.ccs.infrastructure.shared.model.QCcsAuthmemoO;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsCardO;
import com.sunline.ccs.infrastructure.shared.model.CcsAuthmemoO;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.enums.AuthTransDirection;
import com.sunline.ccs.service.auth.context.AuthContext;
import com.sunline.ccs.service.auth.context.CupMsg;
import com.sunline.ccs.service.auth.context.TxnInfo;
import com.sunline.ark.support.service.YakMessage;

 
/**
 * 
 * @see 类名：AuthRetailVoidHandler
 * @see 描述：借记[消费]撤销 (单信息100，双信息200)
 *
 * @see 创建日期：   2015年6月24日下午3:31:44
 * @author liruilin
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class AuthRetailVoidHandler extends AuthVoidReverseHandler {
	@Autowired
	private RCcsAuthmemoO rCcsAuthmemoO;
	
	@Override
	protected Iterator<CcsAuthmemoO> findOriginalTrans(YakMessage request,
			AuthContext context) {
		TxnInfo txnInfo = context.getTxnInfo(); 
		ProductCredit productCredit= context.getProductCredit();
		QCcsAuthmemoO unmo = QCcsAuthmemoO.ccsAuthmemoO;
		CupMsg msg = new CupMsg(request);
		Iterator<CcsAuthmemoO> it = null;
		if(request.getCustomAttributes().get(CustomAttributesKey.MTI).toString().substring(0,2).equals("01")){
			//单信息
			if(productCredit.oldPreAuthVoidSuppInd == Indicator.Y && txnInfo.getTransType() == AuthTransType.PreAuth){
				it = rCcsAuthmemoO.findAll(
						 unmo.b002CardNbr.eq(msg.checkDummyStr(request.getBody(2)))
						.and(unmo.authCode.eq(msg.checkDummyStr(request.getBody(38))))
						.and(unmo.b042MerId.eq(msg.checkDummyStr(request.getBody(42))))
						.and(unmo.txnType.ne(AuthTransType.PAComp))
						// 新增
						.and(unmo.txnDirection.eq(AuthTransDirection.Normal))
						// 移除
						//.and(unmo.b004Amt.eq(msg.getF04_Amount()))
						//.and(unmo.authTxnStatus.eq(AuthTransStatus.N))
						//.and(unmo.finalAction.eq(AuthAction.A))
					).iterator();
			}else{
				it = rCcsAuthmemoO.findAll(
						 unmo.b002CardNbr.eq(msg.checkDummyStr(request.getBody(2)))
						.and(unmo.authCode.eq(msg.checkDummyStr(request.getBody(38))))
						.and(unmo.b042MerId.eq(msg.checkDummyStr(request.getBody(42))))
						.and(unmo.logBizDate.eq(txnInfo.getBizDate()))
						.and(unmo.txnType.ne(AuthTransType.PAComp))
						// 新增
						.and(unmo.txnDirection.eq(AuthTransDirection.Normal))
						// 移除
						//.and(unmo.b004Amt.eq(msg.getF04_Amount()))
						//.and(unmo.authTxnStatus.eq(AuthTransStatus.N))
						//.and(unmo.finalAction.eq(AuthAction.A))
					).iterator();
			}
			
		}
		if(request.getCustomAttributes().get(CustomAttributesKey.MTI).toString().substring(0,2).equals("02")){
			//双信息
			if(productCredit.oldPreAuthVoidSuppInd == Indicator.Y && txnInfo.getTransType() == AuthTransType.PreAuth){
				it = rCcsAuthmemoO.findAll(
			 			 unmo.b032AcqInst.eq(request.getBody(32).trim()) 
						.and(unmo.b033FwdIns.eq(request.getBody(33).trim()))
						//B090OrigData拆分
						.and(unmo.b007TxnTime.eq(msg.checkDummyStr(msg.getF090_3_OrigSysDateTime())))
						.and(unmo.b011Trace.eq(msg.checkDummyStr(msg.getF090_2_OrigSysTraceId())))
						.and(unmo.b032AcqInst.eq(msg.checkDummyStr(msg.getF090_4_OrigAcqInstId())))
						.and(unmo.b033FwdIns.eq(msg.checkDummyStr(msg.getF090_5_OrigFwdInstId())))
						.and(unmo.txnDirection.eq(AuthTransDirection.Normal))
						// 新增
						.and(unmo.b002CardNbr.eq(request.getBody(2)))
						// 移除
						//.and(unmo.b004Amt.eq(msg.getF04_Amount()))
						//.and(unmo.authTxnStatus.eq(AuthTransStatus.N))
						//.and(unmo.finalAction.eq(AuthAction.A))
					).iterator();
			}else{
				it = rCcsAuthmemoO.findAll(
			 			 unmo.b032AcqInst.eq(request.getBody(32).trim()) 
						.and(unmo.b033FwdIns.eq(request.getBody(33).trim()))
						//B090OrigData拆分
						.and(unmo.b007TxnTime.eq(msg.checkDummyStr(msg.getF090_3_OrigSysDateTime())))
						.and(unmo.b011Trace.eq(msg.checkDummyStr(msg.getF090_2_OrigSysTraceId())))
						.and(unmo.b032AcqInst.eq(msg.checkDummyStr(msg.getF090_4_OrigAcqInstId())))
						.and(unmo.b033FwdIns.eq(msg.checkDummyStr(msg.getF090_5_OrigFwdInstId())))
						.and(unmo.logBizDate.eq(txnInfo.getBizDate()))
						.and(unmo.txnDirection.eq(AuthTransDirection.Normal))
						// 新增
						.and(unmo.b002CardNbr.eq(request.getBody(2)))
						// 移除
						//.and(unmo.b004Amt.eq(msg.getF04_Amount()))
						//.and(unmo.authTxnStatus.eq(AuthTransStatus.N))
						//.and(unmo.finalAction.eq(AuthAction.A))
					).iterator();
			}
	 		
		}
		return it;
	}
	
	@Override
	protected void updateUnmatchAmt(AuthContext context) {
		CcsAcctO account =context.getAccount();
		//减去未匹配借记金额
		account.setMemoDb(account.getMemoDb().subtract( context.getTxnInfo().getChbTransAmt()));
	}
	
	
	@Override
	protected void updateStatistics(AuthContext context)
	{
		CcsCardO card = context.getCard();
		//减去单日消费金额 DAY_USED_RETAIL_AMT 
		card.setDayUsedRetailAmt(card.getDayUsedRetailAmt().subtract(context.getTxnInfo().getChbTransAmt()));
		//减去单日消费笔数 DAY_USED_RETAIL_NBR
		card.setDayUsedRetailNbr(card.getDayUsedRetailNbr()-1);
		//减去当期消费金额 CTD_USED_AMT 
		card.setCtdUsedAmt(card.getCtdUsedAmt().subtract(context.getTxnInfo().getChbTransAmt()));
		
		if(context.getTxnInfo().isInternetTrans()){
			card.setCtdNetAmt(card.getCtdNetAmt().subtract(context.getTxnInfo().getChbTransAmt()))	;
		}

	}

 
	@Override
	protected void updateOriginalInfo(CcsAuthmemoO unmatch , AuthContext context) {
		unmatch.setAuthTxnStatus(AuthTransStatus.V);
		if(unmatch.getVoidCnt() == null){
			unmatch.setVoidCnt( 1);
		}else {
			unmatch.setVoidCnt(unmatch.getVoidCnt() + 1);
		}
		unmatch.setLastReversalDate(context.getTxnInfo().getBizDate());
	}
}
