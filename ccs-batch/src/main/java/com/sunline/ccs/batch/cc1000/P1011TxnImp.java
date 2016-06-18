package com.sunline.ccs.batch.cc1000;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.sunline.ppy.dictionary.exchange.FmInterfaceItem;
import com.sunline.ccs.batch.common.TxnPrepare;
import com.sunline.ccs.batch.common.TxnPrepareSet;
import com.sunline.ccs.infrastructure.shared.model.CcsPostingTmp;
import com.sunline.ark.batch.LineItem;
import com.sunline.ark.support.utils.CodeMarkUtils;


/**
 * @see 类名：P1011TxnImp
 * @see 描述：TPS交易导入
 *
 * @see 创建日期：   2015-6-23下午7:19:49
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class P1011TxnImp implements ItemProcessor<LineItem<FmInterfaceItem>, TxnPrepareSet> {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private TxnPrepare txnPrepare;
	
	@Override
	public TxnPrepareSet process(LineItem<FmInterfaceItem> item) throws Exception {
		
		try
		{
			FmInterfaceItem trans = item.getLineObject();
			CcsPostingTmp post = new CcsPostingTmp();
			
			post.setOrg(trans.org);
			post.setCardNbr(trans.cardNo);
			post.setTxnDate(trans.txnDateTime);
			post.setTxnTime(trans.txnDateTime);
			post.setTxnCode(trans.inputTxnCode);//一期暂定
			post.setDbCrInd(trans.dbCrInd);
			post.setPlanNbr(trans.planNbr);
			post.setTxnAmt(trans.txnAmt);
			post.setAuthCode(trans.authCode);
			post.setTxnCurrency(trans.txnCurrCd);
			post.setPostCurrency(trans.postCurrCd);
			post.setPostAmt(trans.postAmt);
//			post.setOrigTransDate(tps.txnDateTime);
			post.setRefNbr(trans.refNbr);
			post.setAcqBranchIq(trans.acqBranchId);
			post.setAcqTerminalId(trans.acqTerminalId);
			post.setAcqAcceptorId(trans.acqAcceptorId);
			post.setAcqAddress(trans.acqNameAddr);
			post.setMcc(trans.mcc);
			post.setOrigTxnCode(trans.inputTxnCode);
			post.setOrigTxnAmt(trans.txnAmt);
			post.setOrigSettAmt(trans.txnAmt);
			post.setFeePayout(trans.feePayout);
			post.setFeeProfit(trans.feeProfit);
			post.setLoanIssueProfit(trans.loanIssueProfit);
			post.setVoucherNo(trans.voucherNo);
			post.setLoanCode(trans.loanCode);
			
			//交易预处理
			TxnPrepareSet output = txnPrepare.txnPrepare(post, trans.inputSource);
			
			return output;
			
		}catch (Exception e) {
			logger.error("交易导入异常, 卡号{}, 交易参考号{}",CodeMarkUtils.subCreditCard(item.getLineObject().cardNo), item.getLineObject().refNbr);
			throw e;
		}
	}
}
