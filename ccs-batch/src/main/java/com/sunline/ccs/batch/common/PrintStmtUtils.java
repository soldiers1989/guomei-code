package com.sunline.ccs.batch.common;

import org.springframework.stereotype.Service;

import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsStatement;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnHst;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnUnstatement;
import com.sunline.ppy.dictionary.exchange.StmtInterfaceItem;
import com.sunline.ppy.dictionary.exchange.StmttxnInterfaceItem;

/**
 * @see 类名：PrintStmtUtils
 * @see 描述：账单工具类
 *
 * @see 创建日期：   2015-6-24下午5:36:08
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class PrintStmtUtils {

	/**
	 * @see 方法名：makeStmtItem 
	 * @see 描述：创建账单汇总信息接口文件项
	 * @see 创建日期：2015-6-24下午5:36:35
	 * @author ChengChun
	 *  
	 * @param stmtHst
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public StmtInterfaceItem makeStmtItem(CcsStatement stmtHst) {
		if (stmtHst == null) {
			throw new IllegalArgumentException("输入null");
		}

		StmtInterfaceItem item = new StmtInterfaceItem();
		item.org                 = stmtHst.getOrg();
		item.acctNo              = stmtHst.getAcctNbr();
		item.stmtDate            = stmtHst.getStmtDate();
		item.name                = stmtHst.getName();
		item.gender              = stmtHst.getGender();
		item.stmtMediaType       = stmtHst.getStmtMediaType();
		item.stmtZip             = stmtHst.getStmtPostcode();
		item.stmtAddress         = stmtHst.getStmtAddress();
		item.stmtDistrict        = stmtHst.getStmtDistrict();
		item.stmtCity            = stmtHst.getStmtCity();
		item.stmtState           = stmtHst.getStmtState();
		item.stmtCountryCd       = stmtHst.getStmtCountryCode();
		item.defaultLogicalCardNo = stmtHst.getDefaultLogicCardNbr();
		item.pmtDueDate          = stmtHst.getPmtDueDate();
		item.currCd              = stmtHst.getCurrency();
		item.creditLimit         = stmtHst.getCreditLmt();
		item.tempLimit           = stmtHst.getTempLmt();
		item.tempLimitBeginDate  = stmtHst.getTempLmtBegDate();
		item.tempLimitEndDate    = stmtHst.getTempLmtEndDate();
		item.stmtBegBal          = stmtHst.getLastStmtBal();
		item.cashLimitRt         = stmtHst.getCashLmtRate();
		item.loanLimitRt         = stmtHst.getLoanLmtRate();
		item.ctdCashAmt          = stmtHst.getCtdCashAmt();
		item.ctdRetailAmt        = stmtHst.getCtdRetailAmt();
		item.ctdAmtDb            = stmtHst.getCtdAmtDb();
		item.ctdAmtCr	         = stmtHst.getCtdAmtCr();
		item.ctdFeeAmt           = stmtHst.getCtdFeeAmt();
		item.ctdInterestAmt      = stmtHst.getCtdInterestAmt();
		item.ctdInterestCnt      = stmtHst.getCtdInterestCnt();
		item.ctdPaymentAmt       = stmtHst.getCtdRepayAmt();
		item.qualGraceBal        = stmtHst.getQualGraceBal();
		item.stmtCurrBal         = stmtHst.getCtdStmtBal();
		item.totDueAmt           = stmtHst.getTotDueAmt();
		item.pointBeginBal       = stmtHst.getPointsBegBal();
		item.ctdEarnedPoints     = stmtHst.getCtdPoints();
		item.ctdAdjPoints        = stmtHst.getCtdAdjPoints();
		item.ctdDisbPoints       = stmtHst.getCtdSpendPoints();
		item.pointBal            = stmtHst.getPointsBal();
		item.ageCd               = stmtHst.getAgeCode();
		item.mobileNo            = stmtHst.getMobileNo();
		item.email               = stmtHst.getEmail();
		item.dualBillingFlag     = stmtHst.getDualBillingFlag();

		return item;
	}
	
	/**
	 * 
	 * @see 方法名：makeStmtItem 
	 * @see 描述： 补打账单信息生成，获取账单历史统计信息后,替换账单地址信息
	 * @see 创建日期：2015-6-24下午5:37:08
	 * @author ChengChun
	 *  
	 * @param stmtHst
	 * @param account
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public StmtInterfaceItem makeStmtItem(CcsStatement stmtHst, CcsAcct account) {
		if (stmtHst == null || account == null) {
			throw new IllegalArgumentException("输入null");
		}

		StmtInterfaceItem item = makeStmtItem(stmtHst);
		item.defaultLogicalCardNo = account.getDefaultLogicCardNbr();
		item.email = account.getEmail();
		item.gender = account.getGender();
		item.mobileNo = account.getMobileNo();
		item.stmtAddress = account.getStmtAddress();
		item.stmtCity = account.getStmtCity();
		item.stmtCountryCd = account.getStmtCountryCode();
		item.stmtDistrict = account.getStmtDistrict();
		item.stmtMediaType = account.getStmtMediaType();
		item.stmtState = account.getStmtState();
		item.stmtZip = account.getStmtPostcode();

		return item;
	}

	/**
	 * @see 方法名：makeStmttxnItem 
	 * @see 描述：创建账单交易明细接口文件项
	 * @see 创建日期：2015-6-24下午5:37:33
	 * @author ChengChun
	 *  
	 * @param txnHst
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public StmttxnInterfaceItem makeStmttxnItem(CcsTxnHst txnHst) {
		if (txnHst == null) {
			throw new IllegalArgumentException("输入null");
		}

		StmttxnInterfaceItem item = new StmttxnInterfaceItem();
		item.acctNo = txnHst.getAcctNbr();
		item.acctType = txnHst.getAcctType();
		item.acqBranchId = txnHst.getAcqBranchIq();
		item.acqAcceptorId = txnHst.getAcqAcceptorId();
		item.acqNameAddr = txnHst.getAcqAddress();
		item.acqTerminalId = txnHst.getAcqTerminalId();
		item.authCode = txnHst.getAuthCode();
		item.bscLogiccardNo = txnHst.getCardBasicNbr();
		item.cardBlockCode = txnHst.getCardBlockCode();
		item.cardNo = txnHst.getCardNbr();
		item.dbCrInd = txnHst.getDbCrInd();
		item.feeProfit = txnHst.getFeeProfit();
		item.inputSettAmt = txnHst.getOrigSettAmt();
		item.inputTxnCode = txnHst.getOrigTxnCode();
		item.inputTxnAmt = txnHst.getOrigTxnAmt();
		item.inputTxnFee = txnHst.getFeePayout();
		item.interchangeFee = txnHst.getInterchangeFee();
		item.loanIssueProfit = txnHst.getLoanIssueProfit();
		item.logicalCardNo = txnHst.getLogicCardNbr();
		item.mcc = txnHst.getMcc();
		item.org = txnHst.getOrg();
		item.origPmtAmt = txnHst.getOrigPmtAmt();
		item.origTransDate = txnHst.getOrigTransDate();
		item.planNbr = txnHst.getPlanNbr();
		item.point = txnHst.getPoints();
		item.postAmt = txnHst.getPostAmt();
		item.postCurrCd = txnHst.getPostCurrency();
		item.postDate = txnHst.getPostDate();
		item.postingFlag = txnHst.getPostingFlag();
		item.prePostingFlag = txnHst.getPrePostingFlag();
		item.productCd = txnHst.getProductCd();
		item.postTxnType = txnHst.getPostTxnType();
		item.refNbr = txnHst.getRefNbr();
		item.relPmtAmt = txnHst.getRelPmtAmt();
		item.stmtDate = txnHst.getStmtDate();
		item.txnSeq = txnHst.getTxnSeq();
		item.txnAmt = txnHst.getTxnAmt();
		item.txnCode = txnHst.getTxnCode();
		item.txnCurrCd = txnHst.getTxnCurrency();
		item.txnDate = txnHst.getTxnDate();
		item.txnDesc = txnHst.getTxnDesc();
		item.txnTime = txnHst.getTxnTime();
		item.voucherNo = txnHst.getVoucherNo();

		return item;
	}

	/**
	 * @see 方法名：makeStmttxnItem 
	 * @see 描述：创建账单交易明细接口文件项
	 * @see 创建日期：2015-6-24下午5:38:00
	 * @author ChengChun
	 *  
	 * @param txnUnstmt
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public StmttxnInterfaceItem makeStmttxnItem(CcsTxnUnstatement txnUnstmt) {
		if (txnUnstmt == null) {
			throw new IllegalArgumentException("输入null");
		}

		StmttxnInterfaceItem item = new StmttxnInterfaceItem();
		item.acctNo = txnUnstmt.getAcctNbr();
		item.acctType = txnUnstmt.getAcctType();
		item.acqBranchId = txnUnstmt.getAcqBranchIq();
		item.acqAcceptorId = txnUnstmt.getAcqAcceptorId();
		item.acqNameAddr = txnUnstmt.getAcqAddress();
		item.acqTerminalId = txnUnstmt.getAcqTerminalId();
		item.authCode = txnUnstmt.getAuthCode();
		item.bscLogiccardNo = txnUnstmt.getCardBasicNbr();
		item.cardBlockCode = txnUnstmt.getCardBlockCode();
		item.cardNo = txnUnstmt.getCardNbr();
		item.dbCrInd = txnUnstmt.getDbCrInd();
		item.feeProfit = txnUnstmt.getFeeProfit();
		item.inputSettAmt = txnUnstmt.getOrigSettAmt();
		item.inputTxnCode = txnUnstmt.getOrigTxnCode();
		item.inputTxnAmt = txnUnstmt.getOrigTxnAmt();
		item.inputTxnFee = txnUnstmt.getFeePayout();
		item.interchangeFee = txnUnstmt.getInterchangeFee();
		item.loanIssueProfit = txnUnstmt.getLoanIssueProfit();
		item.logicalCardNo = txnUnstmt.getLogicCardNbr();
		item.mcc = txnUnstmt.getMcc();
		item.org = txnUnstmt.getOrg();
		item.origPmtAmt = txnUnstmt.getOrigPmtAmt();
		item.origTransDate = txnUnstmt.getOrigTransDate();
		item.planNbr = txnUnstmt.getPlanNbr();
		item.point = txnUnstmt.getPoints();
		item.postAmt = txnUnstmt.getPostAmt();
		item.postCurrCd = txnUnstmt.getPostCurrency();
		item.postDate = txnUnstmt.getPostDate();
		item.postingFlag = txnUnstmt.getPostingFlag();
		item.postTxnType = txnUnstmt.getPostTxnType();
		item.prePostingFlag = txnUnstmt.getPrePostingFlag();
		item.productCd = txnUnstmt.getProductCd();
		item.refNbr = txnUnstmt.getRefNbr();
		item.relPmtAmt = txnUnstmt.getRelPmtAmt();
		item.stmtDate = txnUnstmt.getStmtDate();
		item.txnSeq = txnUnstmt.getTxnSeq();
		item.txnAmt = txnUnstmt.getTxnAmt();
		item.txnCode = txnUnstmt.getTxnCode();
		item.txnCurrCd = txnUnstmt.getTxnCurrency();
		item.txnDate = txnUnstmt.getTxnDate();
		item.txnDesc = txnUnstmt.getTxnDesc();
		item.txnTime = txnUnstmt.getTxnTime();
		item.voucherNo = txnUnstmt.getVoucherNo();
		return item;
	}

}
