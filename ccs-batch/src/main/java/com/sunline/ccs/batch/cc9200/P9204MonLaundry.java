package com.sunline.ccs.batch.cc9200;


import java.util.List;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.infrastructure.shared.model.CcsPostingTmp;
import com.sunline.ccs.param.def.TxnCd;
import com.sunline.ccs.param.def.enums.TxnType;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.exchange.CPSExportMonLaundYXItem;

/**
 * @see 类名：P9204MonLaundry
 * @see 描述：生成反洗钱接口文件(玉溪)
 *
 * @see 创建日期：   2015-6-24下午5:07:02
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class P9204MonLaundry implements ItemProcessor<S9201MasterData, S9201MasterData> {
	
	/**
	 * 获取参数类
	 */
	@Autowired
	private UnifiedParameterFacility parameterFacility;
	@Override
	public S9201MasterData process(S9201MasterData item) throws Exception {
		OrganizationContextHolder.setCurrentOrg(item.getAccount().getOrg());
		/**
		 * 流水表+4个参数
		 * 1.客户号
		 * 2.入账科目
		 * 3.现转标志
		 * 4.冲销标志
		 */
		List<CcsPostingTmp> ttTxnPostList = item.getListTxnPost();
		for(CcsPostingTmp tmTxnPost : ttTxnPostList ){
			CPSExportMonLaundYXItem monLaund = new CPSExportMonLaundYXItem();
			makeMonLaund(tmTxnPost, monLaund);
			monLaund.custId = item.getCustomer().getInternalCustomerId();
			monLaund.postSubject=" ";
			TxnCd txnCd = parameterFacility.retrieveParameterObject(tmTxnPost.getTxnCode(), TxnCd.class);
			if(TxnType.T04.equals(txnCd.txnType)){
				monLaund.cashTrans="1";
			}else if(TxnType.T06.equals(txnCd.txnType)){
				monLaund.cashTrans="2";
			}else{
				monLaund.cashTrans="9";
			}
			
			monLaund.hierInd="0";
			item.getYxMonLaund().add(monLaund);
		}
		
		
		return item;
	}
        /**
         * @see 方法名：makeMonLaund 
         * @see 描述：TODO 方法描述
         * @see 创建日期：2015-6-24下午5:10:42
         * @author ChengChun
         *  
         * @param ttTxnPost
         * @param monLaund
         * 
         * @see 修改记录： 
         * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
         */
	private void makeMonLaund(CcsPostingTmp ttTxnPost, CPSExportMonLaundYXItem monLaund) {
		monLaund.org = ttTxnPost.getOrg();
		monLaund.txnSeq = ttTxnPost.getTxnSeq();
		monLaund.acctNo = ttTxnPost.getAcctNbr();
		monLaund.acctType = ttTxnPost.getAcctType();
		monLaund.cardNo = ttTxnPost.getCardNbr();
		monLaund.logicalCardNo = ttTxnPost.getLogicCardNbr();
		monLaund.bscLogiccardNo = ttTxnPost.getCardBasicNbr();
		monLaund.productCd = ttTxnPost.getProductCd();
		monLaund.txnDate = ttTxnPost.getTxnDate();
		monLaund.txnTime = ttTxnPost.getTxnTime();
		monLaund.postTxnType = ttTxnPost.getPostTxnType();
		monLaund.txnCode = ttTxnPost.getTxnCode();
		monLaund.dbCrInd = ttTxnPost.getDbCrInd();
		monLaund.txnAmt = ttTxnPost.getTxnAmt();
		monLaund.postAmt = ttTxnPost.getPostAmt();
		monLaund.postDate = ttTxnPost.getPostDate();
		monLaund.authCode = ttTxnPost.getAuthCode();
		monLaund.cardBlockCode = ttTxnPost.getCardBlockCode();
		monLaund.txnCurrCd = ttTxnPost.getTxnCurrency();
		monLaund.postCurrCd = ttTxnPost.getPostCurrency();
		monLaund.origTransDate = ttTxnPost.getOrigTransDate();
		monLaund.planNbr = ttTxnPost.getPlanNbr();
		monLaund.refNbr = ttTxnPost.getRefNbr();
		monLaund.txnDesc = ttTxnPost.getTxnDesc();
		monLaund.txnShortDesc = ttTxnPost.getTxnShortDesc();
		monLaund.point = ttTxnPost.getPoints();
		monLaund.postingFlag = ttTxnPost.getPostingFlag();
		monLaund.prePostingFlag = ttTxnPost.getPrePostingFlag();
		monLaund.relPmtAmt = ttTxnPost.getRelPmtAmt();
		monLaund.origPmtAmt = ttTxnPost.getOrigPmtAmt();
		monLaund.acqBranchId = ttTxnPost.getAcqBranchIq();
		monLaund.acqTerminalId = ttTxnPost.getAcqTerminalId();
		monLaund.acqAcceptorId = ttTxnPost.getAcqAcceptorId();
		monLaund.acqorderAddr = ttTxnPost.getAcqAddress();
		monLaund.mcc = ttTxnPost.getMcc();
		monLaund.inputTxnCode = ttTxnPost.getOrigTxnCode();
		monLaund.inputTxnAmt = ttTxnPost.getOrigTxnAmt();
		monLaund.inputSettAmt = ttTxnPost.getOrigSettAmt();
		monLaund.interchangeFee = ttTxnPost.getInterchangeFee();
		monLaund.feePayout = ttTxnPost.getFeePayout();
		monLaund.feeProfit = ttTxnPost.getFeeProfit();
		monLaund.loanIssueProfit = ttTxnPost.getLoanIssueProfit();
		monLaund.stmtDate = ttTxnPost.getStmtDate();
		monLaund.voucherNo = ttTxnPost.getVoucherNo();
		monLaund.jpaVersion = ttTxnPost.getJpaVersion();
	}
}
