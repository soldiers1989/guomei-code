package com.sunline.ccs.batch.cc2200;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ark.support.utils.DateUtils;
import com.sunline.ccs.batch.common.TxnPrepare;
import com.sunline.ccs.facility.BlockCodeUtils;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctKey;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsPostingTmp;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.SysTxnCdMapping;
import com.sunline.ccs.param.def.TxnCd;
import com.sunline.ccs.param.def.enums.SysTxnCd;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.PostTxnType;
import com.sunline.ppy.dictionary.enums.PostingFlag;
import com.sunline.ppy.dictionary.report.ccs.CardFeeRptItem;

/**
 * @see 类名：P2201CardFee
 * @see 描述：年费交易生成,下次年费日期更新
 *
 * @see 创建日期：   2015-6-23下午7:30:48
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class P2201CardFee implements ItemProcessor<CcsCard, CardFeeRptItem> {
	private Logger logger = LoggerFactory.getLogger(getClass());
	/**
	 * 获取参数工具类
	 */
	@Autowired
	private UnifiedParameterFacility parameterFacility;

	/**
	 * 锁定码处理组件
	 */
	@Autowired
	private BlockCodeUtils blockCodeUtils;

	@Autowired
	private RCcsAcct rAcct;
	
	@PersistenceContext
	private EntityManager em;

	/**
	 * 批量日期
	 */
	@Autowired
	private BatchStatusFacility batchStatusFacility;

	@Autowired
	private TxnPrepare txnPostPreEdit;
	
	@Override
	public CardFeeRptItem process(CcsCard card) throws Exception {
		
		OrganizationContextHolder.setCurrentOrg(card.getOrg());

		ProductCredit productCredit = parameterFacility.loadParameter(card.getProductCd(), ProductCredit.class);
		
		AccountAttribute accountAttr = parameterFacility.loadParameter(productCredit.accountAttributeId.toString(), AccountAttribute.class);
		/*
		 * 收取年费
		 */
		boolean cardFeeWaiveInd = false; // 默认不免年费
		CcsAcctKey accountKey = new CcsAcctKey();
		accountKey.setAcctNbr(card.getAcctNbr());
		accountKey.setAcctType(accountAttr.accountType);
		CcsAcct localAccount = rAcct.findOne(accountKey); // 本币账号
		logger.debug("收取年费，卡号=["+card.getLogicCardNbr()+"],账户编号=["+accountKey.getAcctNbr()+"],账户类型=["+accountKey.getAcctType()+"]");
		// 判断该卡是否有免收年费锁定码（BLOCK_CD中的cardFeeWaiveInd为Y表示该锁定码免除年费）
		if (blockCodeUtils.getMergedCardFeeWaiveInd(card.getBlockCode())) {
			cardFeeWaiveInd = true;
		}
		
		// 判断卡表TM_CARD记录中的免年费标志是否为免年费（WAIVE_CARDFEE_IND为Y表示免除年费）
		if (card.getWaiveCardfeeInd() == Indicator.Y) {
			cardFeeWaiveInd = true;
		}
		// 判断该卡的本币账户是否免年费
		// 判断该卡的本币账户是否有免收年费锁定码（BLOCK_CD中的cardFeeWaiveInd为Y表示该锁定码免除年费）
		if (blockCodeUtils.getMergedCardFeeWaiveInd(localAccount.getBlockCode())) {
			cardFeeWaiveInd = true;
		}
		// 判断该卡的本币账户的免年费标志是否为免年费（WAIVE_CARDFEE_IND为Y表示免除年费）
		if (localAccount.getWaiveCardfeeInd() == Indicator.Y) {
			cardFeeWaiveInd = true;
		}

		// 判断该卡的有效期（EXPIRE_DATE）是否小于等于批量日期（如果小于等于则表示已经过期，不再受年费）
		if (!card.getCardExpireDate().after(batchStatusFacility.getBatchDate())) {
			cardFeeWaiveInd = true;
		}
		// 首次年费
		if (card.getFirstCardFeeDate() == null) {
			// 判断首年是否免年费
			if (productCredit.fee.firstCardFeeWaiveInd) {
				cardFeeWaiveInd = true; 
			}
			
			card.setFirstCardFeeDate(batchStatusFacility.getBatchDate());
		}
		//如果是贷款账户，需要判断loan的状态
//		if(localAccount.getAcctType() == AccountType.E){
//			QCcsLoan qTmLoan = QCcsLoan.ccsLoan;
//			Long loanSize = new JPAQuery(em).from(qTmLoan)
//					.where(qTmLoan.logicCardNbr.eq(card.getLogicCardNbr())
//							.and(qTmLoan.acctNbr.eq(localAccount.getAcctNbr()))
//							.and(qTmLoan.acctType.eq(localAccount.getAcctType()))
//							.and(qTmLoan.loanType.eq(LoanType.MCAT)))
//							.singleResult(qTmLoan.count());
//			if(loanSize !=null && loanSize > 0){
//				 //存在随借随还，不收取年费
//			}else{
//				//不存在随借随还，不收取年费
//				cardFeeWaiveInd = true;
//			}
//		}

		CcsPostingTmp ttTxnPost = new CcsPostingTmp();
		if (!cardFeeWaiveInd) { // 不免年费
			// 年费金融交易生成
			ttTxnPost.setOrg(card.getOrg());
			ttTxnPost.setAcctNbr(card.getAcctNbr());
			ttTxnPost.setAcctType(accountAttr.accountType);
			ttTxnPost.setCardNbr(card.getLastestMediumCardNbr());
			ttTxnPost.setLogicCardNbr(card.getLogicCardNbr());
			ttTxnPost.setCardBasicNbr(null);
			ttTxnPost.setProductCd(null);
			ttTxnPost.setTxnDate(batchStatusFacility.getBatchDate());
			ttTxnPost.setTxnTime(new Date());
			ttTxnPost.setPostTxnType(PostTxnType.M);
			SysTxnCdMapping cardFeeSysTxnCdMapping = parameterFacility.loadParameter(SysTxnCd.S17.toString(), SysTxnCdMapping.class);
			TxnCd cardFeeTxnCd = parameterFacility.loadParameter(cardFeeSysTxnCdMapping.txnCd, TxnCd.class);
			ttTxnPost.setTxnCode(cardFeeSysTxnCdMapping.txnCd);
			ttTxnPost.setOrigTxnCode(cardFeeSysTxnCdMapping.txnCd);
			ttTxnPost.setDbCrInd(cardFeeTxnCd.logicMod.getDbCrInd());
			
			switch (card.getBscSuppInd()) {
			case B:
				ttTxnPost.setTxnAmt(productCredit.fee.primCardFee);
				ttTxnPost.setOrigSettAmt(productCredit.fee.primCardFee);
				ttTxnPost.setOrigTxnAmt(productCredit.fee.primCardFee);
				//	TODO 根据交易币种和入账币种进行金额转换，一期保持相同
				ttTxnPost.setPostAmt(productCredit.fee.primCardFee);
				break;
			case S:
				ttTxnPost.setTxnAmt(productCredit.fee.suppCardFee);
				ttTxnPost.setOrigSettAmt(productCredit.fee.suppCardFee);
				ttTxnPost.setOrigTxnAmt(productCredit.fee.suppCardFee);
				//	TODO 根据交易币种和入账币种进行金额转换，一期保持相同
				ttTxnPost.setPostAmt(productCredit.fee.suppCardFee);				
				break;
			default:
				break;
			}
			ttTxnPost.setPostDate(null);
			ttTxnPost.setAuthCode(null);
			ttTxnPost.setCardBlockCode(card.getBlockCode());
			ttTxnPost.setTxnCurrency(accountAttr.accountType.getCurrencyCode());
			ttTxnPost.setPostCurrency(accountAttr.accountType.getCurrencyCode());
			ttTxnPost.setOrigTransDate(batchStatusFacility.getBatchDate());
			ttTxnPost.setRefNbr(null);
			ttTxnPost.setTxnDesc(cardFeeTxnCd.description);
			ttTxnPost.setTxnShortDesc(cardFeeTxnCd.shortDesc);
			ttTxnPost.setPoints(BigDecimal.ZERO);
			ttTxnPost.setPostingFlag(PostingFlag.F00);
			ttTxnPost.setPrePostingFlag(PostingFlag.F00);
			ttTxnPost.setRelPmtAmt(BigDecimal.ZERO);
			ttTxnPost.setOrigPmtAmt(BigDecimal.ZERO);
			ttTxnPost.setAcqBranchIq(null);
			ttTxnPost.setAcqTerminalId(null);
			ttTxnPost.setAcqAddress(null);
			ttTxnPost.setAcqAcceptorId(null);
			ttTxnPost.setMcc(null);
			ttTxnPost.setFeePayout(BigDecimal.ZERO);
			ttTxnPost.setInterchangeFee(BigDecimal.ZERO);
			ttTxnPost.setFeeProfit(BigDecimal.ZERO);
			ttTxnPost.setLoanIssueProfit(BigDecimal.ZERO);
			ttTxnPost.setStmtDate(null);
			ttTxnPost.setVoucherNo(null);
			ttTxnPost.setOrigTxnCode(ttTxnPost.getTxnCode());
			CcsLoan loan = this.getMCATLoan(localAccount);
			if(loan != null){
				logger.debug("收取年费，获取到loan,账户编号=["+accountKey.getAcctNbr()+"],账户类型=["+accountKey.getAcctType()+"],loan类型=["+loan.getLoanType()+"],refnbr=["+loan.getRefNbr()+"]");
				ttTxnPost.setRefNbr(loan.getRefNbr());
			}else{
				logger.debug("收取年费，未获取到loan,账户编号=["+accountKey.getAcctNbr()+"],账户类型=["+accountKey.getAcctType()+"]");
			}
			
			txnPostPreEdit.txnPrepare(ttTxnPost, null);
		}

		// 置下次年费收取时间
		card.setNextCardFeeDate(DateUtils.addYears(card.getNextCardFeeDate(), 1));

		// 年费报表接口生成
		CardFeeRptItem rptItem = new CardFeeRptItem();
		rptItem.procDate = batchStatusFacility.getBatchDate();
		rptItem.org = card.getOrg();
		rptItem.logicCardNo = card.getLogicCardNbr();
		rptItem.bseSuppInd = card.getBscSuppInd();
		rptItem.acctNo = card.getAcctNbr();
		rptItem.firstCardFeeDate = card.getFirstCardFeeDate();
		rptItem.lastCardFeeDate = batchStatusFacility.getBatchDate();
		rptItem.nextCardFeeDate = card.getNextCardFeeDate();
		if (cardFeeWaiveInd) {
			rptItem.cardFeeAmt = BigDecimal.ZERO;
		} else {
			rptItem.cardFeeAmt = ttTxnPost.getTxnAmt();
		}
		rptItem.cardBlockCode = card.getBlockCode();
		rptItem.acctBlockCode = localAccount.getBlockCode();
		rptItem.cardCardFeeInd = card.getWaiveCardfeeInd();
		rptItem.acctCardFeeInd = localAccount.getWaiveCardfeeInd();

		return rptItem;
	}	
	private CcsLoan getMCATLoan(CcsAcct acct) {
		JPAQuery query = new JPAQuery(em);
		QCcsLoan qCcsLoan = QCcsLoan.ccsLoan;
		CcsLoan loan = query.from(qCcsLoan)
				.where(qCcsLoan.acctNbr.eq(acct.getAcctNbr())
						.and(qCcsLoan.acctType.eq(acct.getAcctType())
                        .and(qCcsLoan.loanType.eq(LoanType.MCAT))))
				.singleResult(qCcsLoan);
		return loan;
	}
}
