package com.sunline.ccs.batch.cc6000;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.support.utils.DateUtils;
import com.sunline.ccs.batch.common.TxnPrepare;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsPostingTmp;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.SysTxnCdMapping;
import com.sunline.ccs.param.def.TxnCd;
import com.sunline.ccs.param.def.enums.PrepaymentFeeMethod;
import com.sunline.ccs.param.def.enums.SysTxnCd;
import com.sunline.pcm.param.def.FinancialOrg;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.PostTxnType;
import com.sunline.ppy.dictionary.enums.PostingFlag;


/**
 * @see 类名：P6004InitializeAcctUsage
 * @see 描述： 年初对TM_ACCOUNT账户清理,在做年初判断和清零时；需要同时做一个月初的判断和清零；
 *
 * @see 创建日期：   2015-6-24上午10:01:49
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Component
public class P6004InitializeAcctUsage implements ItemProcessor<S6000AcctInfo, S6000AcctInfo> {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
    private BatchStatusFacility batchFacility;
	
	@Autowired
	private UnifiedParameterFacility parameterFacility;
	
	@Autowired
	private UnifiedParamFacilityProvide unifieldParServer;
	
	@Autowired
	private TxnPrepare txnPrepare;

	@Override
	public S6000AcctInfo process(S6000AcctInfo item) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("账户清理：Org["+item.getAccount().getOrg()
					+"],AcctType["+item.getAccount().getAcctType()
					+"],AcctNo["+item.getAccount().getAcctNbr()
					+"]");
		}
		// 如果处理日期是年初
		if (DateUtils.isFirstDayOfYear(batchFacility.getBatchDate())) {
			// 本年消费金额	YTD_RETAIL_AMT
			item.getAccount().setYtdRetailAmt(BigDecimal.ZERO);
			// 本年消费笔数	YTD_RETAIL_CNT
			item.getAccount().setYtdRetailCnt(0);
			// 本年取现金额	YTD_CASH_AMT
			item.getAccount().setYtdCashAmt(BigDecimal.ZERO);
			// 本年取现笔数	YTD_CASH_CNT
			item.getAccount().setYtdCashCnt(0);
			// 本年退货金额	YTD_REFUND_AMT
			item.getAccount().setYtdRefundAmt(BigDecimal.ZERO);
			// 本年退货笔数	YTD_REFUND_CNT
			item.getAccount().setYtdRefundCnt(0);
			// 本年超限费收取金额
			item.getAccount().setYtdOvrlmtFeeAmt(BigDecimal.ZERO);
			// 本年超限费收取笔数
			item.getAccount().setYtdOvrlmtFeeCnt(0);
			// 本年滞纳金收取金额
			item.getAccount().setYtdLateFeeAmt(BigDecimal.ZERO);
			// 本年滞纳金收取笔数
			item.getAccount().setYtdLateFeeCnt(0);
			// 本年账单日调整次数清零
			item.getAccount().setYtdCycleChagCnt(0);
			
		}
		// 如果处理日期是月初
		if (DateUtils.isFirstDayOfMonth(batchFacility.getBatchDate())) {
			// 本月消费金额	MTD_RETAIL_AMT
			item.getAccount().setMtdRetailAmt(BigDecimal.ZERO);
			// 本月消费笔数	MTD_RETAIL_CNT
			item.getAccount().setMtdRetailCnt(0);
			// 本月取现金额	MTD_CASH_AMT
			item.getAccount().setMtdCashAmt(BigDecimal.ZERO);
			// 本月取现笔数	MTD_CASH_CNT
			item.getAccount().setMtdCashCnt(0);
			// 本月退货金额	MTD_REFUND_AMT
			item.getAccount().setMtdRefundAmt(BigDecimal.ZERO);
			// 本月退货笔数	MTD_REFUND_CNT
			item.getAccount().setMtdRefundCnt(0);
		}
		
		if (batchFacility.getBatchDate().compareTo(item.getAccount().getSetupDate()) == 0) {
			
			ProductCredit productCredit = parameterFacility.loadParameter(item.getAccount().getProductCd(), ProductCredit.class);
			
			// 随借随还开卡收取印花税
			if (LoanType.MCAT.equals(productCredit.defaultLoanType)) {
				String loanCode = productCredit.loanPlansMap.get(productCredit.defaultLoanType);
				LoanFeeDef loanFeeDef = unifieldParServer.loanFeeDefMCAT(loanCode);			
				
				BigDecimal totalStampTAX = BigDecimal.ZERO;
				
				if (PrepaymentFeeMethod.A.equals(loanFeeDef.stampCalcMethod)) {
					totalStampTAX = loanFeeDef.stampAMT.setScale(1, BigDecimal.ROUND_HALF_UP);
				} else {
					totalStampTAX = item.getAccount().getCreditLmt().multiply(loanFeeDef.stampRate).setScale(1, BigDecimal.ROUND_HALF_UP);
				}
				
				//如果贷款印花税不计入客户账，并且未出账单的印花税大于0,生成转入交易
				if(totalStampTAX.compareTo(BigDecimal.ZERO) >0 ){
					CcsPostingTmp stampAMT = null;
					if(Indicator.N.equals(loanFeeDef.stampCustomInd)){
						stampAMT = createCcsPostingTmp( item.getAccount(),totalStampTAX,SysTxnCd.S97);
						if(loanFeeDef.isOffsetRate.equals(Indicator.Y)){
							
							//冲减利息
							CcsPostingTmp stampdutyTxnXfrInt = createCcsPostingTmp( item.getAccount(),totalStampTAX,SysTxnCd.S98);
							stampdutyTxnXfrInt.setStmtDate(item.getAccount().getNextStmtDate());
							txnPrepare.txnPrepare(stampdutyTxnXfrInt, null);
							item.getTxnPosts().add(stampdutyTxnXfrInt);
						}
					}else{
						stampAMT = createCcsPostingTmp( item.getAccount(),totalStampTAX,SysTxnCd.S71);
					}
					stampAMT.setStmtDate(item.getAccount().getNextStmtDate());
					txnPrepare.txnPrepare(stampAMT, null);	
					item.getTxnPosts().add(stampAMT);
				}
				
				
			}
		}

		return item;
	}
	
	private CcsPostingTmp createCcsPostingTmp(CcsAcct acct,BigDecimal txnAmt,SysTxnCd sysTxnCd){
		
		ProductCredit productCredit = parameterFacility.loadParameter(acct.getProductCd(), ProductCredit.class);
		FinancialOrg financialOrg = parameterFacility.loadParameter(productCredit.financeOrgNo, FinancialOrg.class);
		CcsPostingTmp postingTmp = new CcsPostingTmp();
		
		postingTmp.setOrg(acct.getOrg());
		postingTmp.setAcctNbr(acct.getAcctNbr());
		postingTmp.setAcctType(acct.getAcctType());
		postingTmp.setCardNbr(acct.getDefaultLogicCardNbr());
		postingTmp.setLogicCardNbr(acct.getDefaultLogicCardNbr());
		postingTmp.setCardBasicNbr(acct.getDefaultLogicCardNbr());
		postingTmp.setProductCd(acct.getProductCd());
		postingTmp.setTxnTime(acct.getSetupDate());
		postingTmp.setTxnCurrency(acct.getAcctType().getCurrencyCode());
		postingTmp.setTxnAmt(txnAmt);
		postingTmp.setPostDate(batchFacility.getBatchDate());
		postingTmp.setPostCurrency(acct.getAcctType().getCurrencyCode());
		postingTmp.setPostAmt(txnAmt);
		postingTmp.setPostTxnType(PostTxnType.M);
		
		SysTxnCdMapping sysTxnCdMapping = parameterFacility.loadParameter(sysTxnCd.toString(), SysTxnCdMapping.class);
		TxnCd txnCd = parameterFacility.loadParameter(sysTxnCdMapping.txnCd, TxnCd.class);
		postingTmp.setTxnCode(txnCd.txnCd);
		postingTmp.setOrigTxnCode(txnCd.txnCd);
		postingTmp.setDbCrInd(txnCd.logicMod.getDbCrInd());
		postingTmp.setTxnDesc(txnCd.description);
		postingTmp.setTxnShortDesc(txnCd.shortDesc);
		postingTmp.setOrigTxnAmt(txnAmt);
		postingTmp.setOrigSettAmt(txnAmt);
		postingTmp.setMcc(null);
		postingTmp.setAuthCode(null);
		postingTmp.setTxnDate(acct.getSetupDate());
		postingTmp.setRefNbr(null);
		postingTmp.setVoucherNo(null);
		postingTmp.setCardBlockCode(null);
		postingTmp.setStmtDate(null);
		postingTmp.setOrigTransDate(acct.getSetupDate());
		postingTmp.setOrigPmtAmt(BigDecimal.ZERO);
		postingTmp.setPoints(BigDecimal.ZERO);
		postingTmp.setPrePostingFlag(PostingFlag.F00);
		postingTmp.setPostingFlag(PostingFlag.F00);
		postingTmp.setAcqBranchIq(null);
		postingTmp.setAcqTerminalId(null);
		postingTmp.setAcqAcceptorId(financialOrg.acqAcceptorId);
		postingTmp.setAcqAddress(null);
		postingTmp.setRelPmtAmt(BigDecimal.ZERO);
		postingTmp.setInterchangeFee(BigDecimal.ZERO);
		postingTmp.setFeePayout(BigDecimal.ZERO);
		postingTmp.setFeeProfit(BigDecimal.ZERO);
		postingTmp.setLoanIssueProfit(BigDecimal.ZERO);
		postingTmp.setLoanCode(productCredit.loanPlansMap.get(productCredit.defaultLoanType));
		
		return postingTmp;
	}

}
