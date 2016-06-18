package com.sunline.ccs.batch.cc6000.common;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ccs.batch.cc6000.S6000AcctInfo;
import com.sunline.ccs.batch.common.BatchUtils;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.CcsPostingTmp;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnSeq;
import com.sunline.ccs.param.def.LatePaymentCharge;
import com.sunline.ccs.param.def.Organization;
import com.sunline.ccs.param.def.OverlimitCharge;
import com.sunline.ccs.param.def.PlanTemplate;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.SysTxnCdMapping;
import com.sunline.ccs.param.def.TxnCd;
import com.sunline.ccs.param.def.TxnFee;
import com.sunline.ccs.param.def.enums.LoanTerminateReason;
import com.sunline.ccs.param.def.enums.SysTxnCd;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.BucketType;
import com.sunline.ppy.dictionary.enums.LoanFeeMethod;
import com.sunline.ppy.dictionary.enums.LoanStatus;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.PlanType;
import com.sunline.ppy.dictionary.enums.PostTxnType;
import com.sunline.ppy.dictionary.enums.PostingFlag;


/**
* @author fanghj
 *

 */
/** 
 * @see 类名：GeneratorTransaction
 * @see 描述：内部交易生成
 * 			  交易费用（取现费/货币转换费）
 * 			  超限费
 * 			  滞纳金
 * 			  分期转移交易
 * 			  分期手续费
 * 			  利息交易
 *
 * @see 创建日期：   2015年6月25日 下午3:03:17
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Component
public class TransactionGenerator {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private UnifiedParameterFacility parameterFacility;
	@Autowired
	private TransactionPost transactionPost;
	@Autowired
	private Calculator calculator;
	@Autowired
	private PlanManager planManager;
	@Autowired
	private BatchUtils batchUtils;
	@Autowired
	private BatchStatusFacility batchStatusFacility;
	
	@PersistenceContext
	private EntityManager em;
	
	
	/**
	 * 生成交易费用（取现费/货币转换费），并入账
	 * @param item
	 * @param postPreAcctCurrBal 入账前账户的账户余额
	 * @param txnPost
	 * @param txnFee
	 * @param batchDate 批量日期
	 * @param productCode 计划的产品编码
	 * @param newTxnPosts 
	 * @return
	 */
	public CcsPostingTmp generateTransactionFee(S6000AcctInfo item, BigDecimal postPreAcctCurrBal, CcsPostingTmp txnPost, TxnFee txnFee, Date batchDate, String productCode, List<CcsPostingTmp> newTxnPosts) {
		logger.info("内部交易费生成");
		
		//生成内部交易费金额
		BigDecimal txnFeeAmt = BigDecimal.ZERO;
		BigDecimal chargeWaiveFee = BigDecimal.ZERO;
		BigDecimal despositWaiveFee = BigDecimal.ZERO;
		
		// 账户余额剔除额度内分期余额
		BigDecimal planLoanBal = BigDecimal.ZERO;
		for(CcsPlan plan : item.getPlans()){
			if (plan.getPlanType()== PlanType.O) {
				planLoanBal = planLoanBal.add(plan.getCurrBal());
			}
		}
		BigDecimal currBal = postPreAcctCurrBal.subtract(planLoanBal);
		
		//透支
		if (currBal.compareTo(BigDecimal.ZERO) >= 0) {
			//是否免除透支费用
			if(calculator.isWaive(txnFee.chargeWaiveInd, txnPost.getAcqBranchIq(), item.getAccount().getOwningBranch())){
				txnFeeAmt = BigDecimal.ZERO;
			}else{
				chargeWaiveFee = calculator.getFeeAmount(txnFee.tierInd, txnFee.chargeRates, txnPost.getPostAmt());
				txnFeeAmt = calculator.fixWaiveFee(chargeWaiveFee, txnFee.minChargeWaiveFee, txnFee.maxChargeWaiveFee);
			}
		}else{
			//溢缴款
			if(currBal.abs().compareTo(txnPost.getPostAmt()) >= 0){
				//是否免除溢缴款费用
				if(calculator.isWaive(txnFee.despositWaiveInd, txnPost.getAcqBranchIq(), item.getAccount().getOwningBranch())){
					txnFeeAmt = BigDecimal.ZERO;
				}else{
					despositWaiveFee = txnPost.getPostAmt().multiply(txnFee.despositRate).add(txnFee.despositBaseFee);
					txnFeeAmt = calculator.fixWaiveFee(despositWaiveFee, txnFee.minDespositWaiveFee, txnFee.maxDespositWaiveFee);
				}
			}
			//透支+溢缴
			else{
				//是否免除透支费用
				if(!calculator.isWaive(txnFee.chargeWaiveInd, txnPost.getAcqBranchIq(), item.getAccount().getOwningBranch())){
					chargeWaiveFee = calculator.getFeeAmount(txnFee.tierInd, txnFee.chargeRates, txnPost.getPostAmt().subtract(currBal.abs()));
					txnFeeAmt = calculator.fixWaiveFee(chargeWaiveFee, txnFee.minChargeWaiveFee, txnFee.maxChargeWaiveFee);
				}
				//是否免除溢缴款费用
				if(!calculator.isWaive(txnFee.despositWaiveInd, txnPost.getAcqBranchIq(), item.getAccount().getOwningBranch())){
					despositWaiveFee = currBal.abs().multiply(txnFee.despositRate).add(txnFee.despositBaseFee);
					txnFeeAmt = calculator.fixWaiveFee(chargeWaiveFee.add(despositWaiveFee), txnFee.minChargeWaiveFee, txnFee.maxChargeWaiveFee);
				}
			}
		}
		
		if (txnFeeAmt.compareTo(BigDecimal.ZERO) <= 0) return null;
		
		// 根据生成的内部交易类型，确定入账的逻辑模块
		TxnCd txnCode = parameterFacility.loadParameter(txnFee.feeTxnCd, TxnCd.class);
		
		// 交易主键生成
		CcsTxnSeq txnSeq = new CcsTxnSeq();
		txnSeq.setOrg(item.getAccount().getOrg());
		em.persist(txnSeq);

		ProductCredit productCredit = parameterFacility.loadParameter(item.getAccount().getProductCd(), ProductCredit.class);
		String acqAcceptorId=productCredit.financeOrgNo;
		// 生成内部交易
		String planNbr = planManager.getPlanNbrByTxnCd(txnCode, productCode);
		CcsPostingTmp newTxn = generateTrans(txnSeq, item, txnPost.getCardNbr(), txnPost.getLogicCardNbr(), txnPost.getProductCd(), txnCode, txnFeeAmt, batchDate, planNbr, "",null,acqAcceptorId);
		newTxn.setCardBasicNbr(txnPost.getCardBasicNbr()); // 逻辑卡主卡卡号
		newTxn.setTxnCode(txnFee.feeTxnCd); // 交易码
		newTxn.setCardBlockCode(txnPost.getCardBlockCode()); // 卡片锁定码
		newTxn.setTxnCurrency(txnPost.getPostCurrency()); // 内部交易费币种按入账币种收取
		newTxn.setPostCurrency(txnPost.getPostCurrency()); // 入账币种代码
		newTxn.setAcqBranchIq(txnPost.getAcqBranchIq()); // 受理分行代码
		newTxn.setAcqTerminalId(txnPost.getAcqTerminalId()); // 受理机构终端标识码
		newTxn.setAcqAcceptorId(txnPost.getAcqAcceptorId()); // 受卡方标识码
		newTxn.setAcqAddress(txnPost.getAcqAddress()); // 受理机构名称地址
		newTxn.setMcc(txnPost.getMcc()); // 商户类别代码
		newTxn.setOrigTxnCode(txnFee.feeTxnCd); // 原交易交易码
		newTxn.setVoucherNo(txnPost.getVoucherNo()); // 销售单凭证号
		newTxn.setRefNbr(txnPost.getRefNbr());
		em.persist(newTxn);
		
		newTxnPosts.add(newTxn);
		
		// 调用内部交易入账处理
		transactionPost.posting(item, newTxn, batchDate);
		
		return newTxn;
	}


	/** 
	 * 账单日，生成分期转移交易
	 * @param account
	 * @param xsfOutPlan
	 * @param xsfInPlan
	 * @param loan
	 * @param xfrAmt
	 * @return
	 */
	public List<CcsPostingTmp> generateLoanTransferTransaction(S6000AcctInfo item, CcsPlan xfrOutPlan, CcsPlan xfrInPlan, CcsLoan loan, BigDecimal xfrAmt, Date batchDate) {
		
		return generateLoanTransferTransaction(item, xfrOutPlan, xfrInPlan, loan, xfrAmt, batchDate,SysTxnCd.S01,SysTxnCd.S02);
	}
	/**
	 * 分期转移交易，指定转出和转入的内部交易码
	 * @param item
	 * @param xfrOutPlan
	 * @param xfrInPlan
	 * @param loan
	 * @param xfrAmt
	 * @param batchDate
	 * @param from
	 * @param to
	 * @return
	 */
	public List<CcsPostingTmp> generateLoanTransferTransaction(S6000AcctInfo item, CcsPlan xfrOutPlan, CcsPlan xfrInPlan, CcsLoan loan, BigDecimal xfrAmt, Date batchDate,SysTxnCd from,SysTxnCd to) {
		
		// 返回结果变量
		List<CcsPostingTmp> newTxns = new ArrayList<CcsPostingTmp>();
		if(xfrAmt.compareTo(BigDecimal.ZERO) == 0){
			return newTxns;
		}
		// 查找系统内部交易类型对照表
		SysTxnCdMapping sysTxnCdOut = parameterFacility.loadParameter(String.valueOf(from), SysTxnCdMapping.class);
		// 根据交易码，查找交易码对象
		TxnCd txnCodeOut = parameterFacility.loadParameter(sysTxnCdOut.txnCd, TxnCd.class);
		// 查找产品代码
		ProductCredit productCr = calculator.getLCardNoByProductCr(loan.getLogicCardNbr());
		// 交易主键生成
		CcsTxnSeq txnSeq = new CcsTxnSeq();
		txnSeq.setOrg(item.getAccount().getOrg());
		em.persist(txnSeq);
		String acqAcceptorId=productCr.financeOrgNo;		
		
		// 生成分期转出交易
		CcsPostingTmp newTxn = generateTrans(txnSeq, item, loan.getCardNbr(), loan.getLogicCardNbr(), productCr.productCd, txnCodeOut, xfrAmt, loan.getOrigTransDate(), xfrOutPlan.getPlanNbr(), xfrOutPlan.getRefNbr(),xfrOutPlan.getTerm(),acqAcceptorId);
		newTxn.setCardBasicNbr(xfrOutPlan.getLogicCardNbr()); // 逻辑卡主卡卡号
		newTxn.setOrigTxnAmt(BigDecimal.ZERO); // 原交易交易金额
		newTxn.setOrigSettAmt(BigDecimal.ZERO); // 原交易清算金额
		
		newTxns.add(newTxn);
		
		// 查找系统内部交易类型对照表
		SysTxnCdMapping sysTxnCdIn = parameterFacility.loadParameter(String.valueOf(to), SysTxnCdMapping.class);
		// 根据交易码，查找交易码对象
		TxnCd txnCodeIn = parameterFacility.loadParameter(sysTxnCdIn.txnCd, TxnCd.class);
		// 交易主键生成
		txnSeq = new CcsTxnSeq();
		txnSeq.setOrg(item.getAccount().getOrg());
		em.persist(txnSeq);
				
		
		// 生成分期转入交易
		newTxn = generateTrans(txnSeq, item, loan.getCardNbr(), loan.getLogicCardNbr(), productCr.productCd, txnCodeIn, xfrAmt, loan.getOrigTransDate(), xfrInPlan.getPlanNbr(), xfrInPlan.getRefNbr(),xfrInPlan.getTerm(),acqAcceptorId);
		newTxn.setCardBasicNbr(xfrInPlan.getLogicCardNbr()); // 逻辑卡主卡卡号
		newTxn.setOrigTxnAmt(BigDecimal.ZERO); // 原交易交易金额
		newTxn.setOrigSettAmt(BigDecimal.ZERO); // 原交易清算金额
		newTxn.setTxnDesc(loan.getLoanType().getDescription()+"，第"+loan.getCurrTerm()+"期/共"+loan.getLoanInitTerm()+"期");
		newTxn.setTxnShortDesc(loan.getLoanType().getDescription()+"，第"+loan.getCurrTerm()+"期/共"+loan.getLoanInitTerm()+"期");
		
		newTxns.add(newTxn);
		
		for (CcsPostingTmp txnPost : newTxns) {
			// 数据持久化
			em.persist(txnPost);
			// 保存生成的交易
			item.getTxnPosts().add(txnPost);
		}
		// 调用内部交易入账处理
		// 分期转移交易只需要生成流水，不需要做入账处理；
		return newTxns;
	}
	/**
	 * 账单日，生成分期手续费，并入账
	 * @return
	 */
	public CcsPostingTmp generateLoanFee(S6000AcctInfo item, CcsPlan xfrInPlan, CcsLoan tmLoan, Date batchDate) {

		// 分期手续费收取
		BigDecimal loanFee = BigDecimal.ZERO;
		if(tmLoan.getLoanStatus() == LoanStatus.R 
				&& DateUtils.truncatedCompareTo(
						batchUtils.getNextStmtDay(item.getAccount().getProductCd(), item.getAccount().getCycleDay(), tmLoan.getExtendDate()), 
						batchStatusFacility.getBatchDate(), 
						Calendar.DATE) ==0
				&& tmLoan.getLoanFeeMethod() == LoanFeeMethod.F){
			loanFee = tmLoan.getExtendFirstTermFee();
		}else if(tmLoan.getLoanStatus() == LoanStatus.T && tmLoan.getTerminalReasonCd() == LoanTerminateReason.P){
			//如果分期状态时提前还款终止状态，终止这一期不收手续费,费用联机已处理（只考虑提前还款情况）
			return null;
		}else{
			if (tmLoan.getCurrTerm() == 1) {
				loanFee = tmLoan.getLoanFirstTermFee();
			} else {
				if (tmLoan.getRemainTerm() <= 1) {
					loanFee = tmLoan.getLoanFinalTermFee();
				} else {
					loanFee = tmLoan.getLoanFixedFee();
				}
			}
		}
		if (loanFee.compareTo(BigDecimal.ZERO) <= 0) return null;
		// 查找系统内部交易类型对照表
		SysTxnCdMapping sysTxnCd = parameterFacility.loadParameter(String.valueOf(SysTxnCd.S03), SysTxnCdMapping.class);
		// 根据交易码，查找交易码对象
		TxnCd txnCode = parameterFacility.loadParameter(sysTxnCd.txnCd, TxnCd.class);
		// 查找产品代码
		ProductCredit productCr = calculator.getLCardNoByProductCr(tmLoan.getLogicCardNbr());
		// 交易主键生成
		CcsTxnSeq txnSeq = new CcsTxnSeq();
		txnSeq.setOrg(item.getAccount().getOrg());
		em.persist(txnSeq);
				
		String acqAcceptorId=productCr.financeOrgNo;
		
		// 生成分期手续费
		CcsPostingTmp newTxn = generateTrans(txnSeq, item, tmLoan.getCardNbr(), tmLoan.getLogicCardNbr(), productCr.productCd, txnCode, loanFee, tmLoan.getOrigTransDate(), productCr.planNbrList.get(txnCode.planType), xfrInPlan.getRefNbr(),xfrInPlan.getTerm(),acqAcceptorId);
		newTxn.setCardBasicNbr(xfrInPlan.getLogicCardNbr()); // 逻辑卡主卡卡号
		newTxn.setFeePayout(BigDecimal.ZERO); // 原交易交易手续费
		newTxn.setFeeProfit(BigDecimal.ZERO); // 发卡方应得手续费收入
		
		// 数据持久化
		em.persist(newTxn);
		// 保存超限费交易
		item.getTxnPosts().add(newTxn);
		// 调用内部交易入账处理
		transactionPost.posting(item, newTxn, batchDate);

		return newTxn;
	}

	/**
	 * 收取超限费，并入账
	 * @return
	 */
	public CcsPostingTmp generateOverLimitFee(S6000AcctInfo item, BigDecimal overAmt, Date batchDate) {

		// 查找系统内部交易类型对照表
		SysTxnCdMapping sysTxnCd = parameterFacility.loadParameter(String.valueOf(SysTxnCd.S04), SysTxnCdMapping.class);
		// 根据交易码，查找交易码对象
		TxnCd txnCode = parameterFacility.loadParameter(sysTxnCd.txnCd, TxnCd.class);
		// 根据产品代码，查找超限参数对象
		ProductCredit productCr = parameterFacility.loadParameter(item.getAccount().getProductCd(), ProductCredit.class);
		// 获得组织机构参数
		Organization org = parameterFacility.loadParameter(null, Organization.class);
		// 超限费参数
		OverlimitCharge oLCharge = null;
		if (org.baseCurrCd.equals(item.getAccount().getCurrency())) oLCharge = productCr.overlimitCharge;
		else oLCharge = productCr.dualOverlimitCharge;
		// 计算超限费
		BigDecimal overLimitFee = calculator.getFeeAmount(oLCharge.tierInd, oLCharge.chargeRates, overAmt).setScale(2, BigDecimal.ROUND_HALF_UP);
		// 最小收费金额
		if (overLimitFee.compareTo(oLCharge.minCharge) < 0) overLimitFee = oLCharge.minCharge;
		// 最大收费金额
		if (overLimitFee.compareTo(oLCharge.maxCharge) > 0) overLimitFee = oLCharge.maxCharge;
		// 本年最大收费金额
		if (overLimitFee.compareTo(oLCharge.yearMaxCharge.subtract(item.getAccount().getYtdOvrlmtFeeAmt())) > 0) {
			overLimitFee = oLCharge.yearMaxCharge.subtract(item.getAccount().getYtdOvrlmtFeeAmt());
		}
		// 本年超限费收取笔数
		if (oLCharge.yearMaxCnt - item.getAccount().getYtdOvrlmtFeeCnt() <= 0) {
			overLimitFee = BigDecimal.ZERO;
		}
		// 如果超限金额为零，则不生成超限费交易
		if (overLimitFee.compareTo(BigDecimal.ZERO) <= 0) return null;
		
		// 交易主键生成
		CcsTxnSeq txnSeq = new CcsTxnSeq();
		txnSeq.setOrg(item.getAccount().getOrg());
		em.persist(txnSeq);
				
		String acqAcceptorId=productCr.financeOrgNo;
		
		// 生成内部交易
		String planNbr = planManager.getPlanNbrByTxnCd(txnCode, item.getAccount().getProductCd());
		CcsPostingTmp newTxn = generateTrans(txnSeq, item, item.getAccount().getDefaultLogicCardNbr(), item.getAccount().getDefaultLogicCardNbr(), productCr.productCd, txnCode, overLimitFee, batchDate, planNbr, "",null,acqAcceptorId);
		
		// 数据持久化
		em.persist(newTxn);
		// 保存超限费交易
		item.getTxnPosts().add(newTxn);
		// 调用内部交易入账处理
		transactionPost.posting(item, newTxn, batchDate);
		
		return newTxn;
	}
	
	/**
	 * 收取短信费，并入账
	 * @return
	 */
	public CcsPostingTmp generateSmsFee(S6000AcctInfo item, ProductCredit productCredit, Date batchDate) {
		
		//获取系统内部交易码
		SysTxnCdMapping sysTxnCd = parameterFacility.loadParameter(String.valueOf(SysTxnCd.S07), SysTxnCdMapping.class);
		//获取交易码
		TxnCd txnCode = parameterFacility.loadParameter(sysTxnCd.txnCd, TxnCd.class);
		
		//交易主键生成
		CcsTxnSeq txnSeq = new CcsTxnSeq();
		txnSeq.setOrg(item.getAccount().getOrg());
		em.persist(txnSeq);
				
		String acqAcceptorId=productCredit.financeOrgNo;
		
		//生成内部交易
		String planNbr = planManager.getPlanNbrByTxnCd(txnCode, item.getAccount().getProductCd());
		CcsPostingTmp ttTxnPost = generateTrans(txnSeq, item, item.getAccount().getDefaultLogicCardNbr(), item.getAccount().getDefaultLogicCardNbr(), productCredit.productCd, txnCode, productCredit.fee.smsFee, batchDate, planNbr, "",null,acqAcceptorId);
		
		//数据持久化
		em.persist(ttTxnPost);
		//保存短信费费交易
		item.getTxnPosts().add(ttTxnPost);
		//调用内部交易入账处理
		transactionPost.posting(item, ttTxnPost, batchDate);
		
		return ttTxnPost;
	}
	

	/**
	 * 收取滞纳金，并入账
	 * @param isReplace 是否为代收滞纳金
	 * @return
	 */
	public CcsPostingTmp generateLateChargeFee(S6000AcctInfo item, BigDecimal latePayAmt, Date batchDate, Boolean isReplace) {

		// 查找系统内部交易类型对照表-->等额本息收取滞纳金，增加新的系统交易码ymk20160126
		SysTxnCdMapping sysTxnCd = null;
		if(item.getLoans() != null && item.getLoans().size() > 0 ){
			for(CcsLoan loan : item.getLoans()){
				if(loan.getLoanType() == LoanType.MCAT){
					sysTxnCd = parameterFacility.loadParameter(String.valueOf(SysTxnCd.S05), SysTxnCdMapping.class);
					break;
				}else if (loan.getLoanType() == LoanType.MCEI){
					if(isReplace){
						sysTxnCd = parameterFacility.loadParameter(String.valueOf(SysTxnCd.D14), SysTxnCdMapping.class);
					}else{
						sysTxnCd = parameterFacility.loadParameter(String.valueOf(SysTxnCd.D10), SysTxnCdMapping.class);
					}
					break;
				}else{
					sysTxnCd = parameterFacility.loadParameter(String.valueOf(SysTxnCd.S05), SysTxnCdMapping.class);
					break;
				}
			}
		}else{
			sysTxnCd = parameterFacility.loadParameter(String.valueOf(SysTxnCd.S05), SysTxnCdMapping.class);
		}
		
		// 根据交易码，查找交易码对象
		TxnCd txnCode = parameterFacility.loadParameter(sysTxnCd.txnCd, TxnCd.class);
		// 根据产品代码，查找超限参数对象
		ProductCredit productCr = parameterFacility.loadParameter(item.getAccount().getProductCd(), ProductCredit.class);
		// 获得组织机构参数
		Organization org = parameterFacility.loadParameter(null, Organization.class);
		// 滞纳金参数
		LatePaymentCharge latePayCharge = null;
		if (org.baseCurrCd.equals(item.getAccount().getCurrency())) {
			if(isReplace) latePayCharge = productCr.replaceLatePaymentCharge;
			else latePayCharge = productCr.latePaymentCharge;
		}
		else latePayCharge = productCr.dualLatePaymentCharge;
		// 计算滞纳金
		BigDecimal latePayFee = calculator.getFeeAmount(latePayCharge.tierInd, latePayCharge.chargeRates, latePayAmt).setScale(2, BigDecimal.ROUND_HALF_UP);
		// 最小收费金额
		if (latePayFee.compareTo(latePayCharge.minCharge) < 0) latePayFee = latePayCharge.minCharge;
		// 最大收费金额
		if (latePayFee.compareTo(latePayCharge.maxCharge) > 0) latePayFee = latePayCharge.maxCharge;
		// 本年最大收费金额
		if (latePayFee.compareTo(latePayCharge.yearMaxCharge.subtract(item.getAccount().getYtdLateFeeAmt())) > 0) {
			latePayFee = latePayCharge.yearMaxCharge.subtract(item.getAccount().getYtdLateFeeAmt());
		}
		// 本年最大收取笔数
		if (latePayCharge.yearMaxCnt - item.getAccount().getYtdLateFeeCnt() <= 0) {
			latePayFee = BigDecimal.ZERO;
		}
		if (latePayFee.compareTo(BigDecimal.ZERO) <= 0) return null;
		
		// 交易主键生成
		CcsTxnSeq txnSeq = new CcsTxnSeq();
		txnSeq.setOrg(item.getAccount().getOrg());
		em.persist(txnSeq);
				
		String planNbr = planManager.getPlanNbrByTxnCd(txnCode, item.getAccount().getProductCd());
		
		
		String acqAcceptorId=productCr.financeOrgNo;
		String refNbr = "";
		Integer term = null;
		for(CcsLoan loan : item.getLoans()){
			if(loan.getLoanType() == LoanType.MCAT){
				// 随借随还滞纳金收取
				refNbr = loan.getRefNbr();
				break;
			}else if (LoanType.MCEI.equals(loan.getLoanType()) || LoanType.MCEP.equals(loan.getLoanType())){
				// 等额本息或等额本金滞纳金收取，入到当前期数的plan中
				refNbr = loan.getRefNbr();
				if(item.getPlans() != null && item.getPlans().size() > 0 ){
					int currTerm = 0;
					for(CcsPlan plan : item.getPlans()){
						int planTerm = plan.getTerm()==null?0:plan.getTerm();
						if(loan.getRefNbr().equals(plan.getRefNbr()) && loan.getCurrTerm()==planTerm && PlanType.Q.equals(plan.getPlanType())){
							currTerm = planTerm;
							break;
						}
					}
					if (currTerm < 1){
						logger.debug("等额本息/金滞纳金收取，根据loan未找到对应的当期转入计划，loan借据号["+loan.getDueBillNo()
								+"],当期期数 ["+loan.getCurrTerm()+"]");
						return null;
					}
					term = currTerm;
					break;
				}else{
					logger.debug("等额本息/金滞纳金收取，根据loan未找到对应的当期转入计划，loan借据号["+loan.getDueBillNo()
							+"],当期期数 ["+loan.getCurrTerm()+"]");
					return null;
				}
			}
		}
		
		// 生成内部交易
		CcsPostingTmp newTxn = generateTrans(txnSeq, item, item.getAccount().getDefaultLogicCardNbr(), item.getAccount().getDefaultLogicCardNbr(), productCr.productCd, txnCode, latePayFee, batchDate, planNbr, refNbr,term,acqAcceptorId);			

		// 数据持久化
		em.persist(newTxn);
		// 保存超限费交易
		item.getTxnPosts().add(newTxn);
		// 调用内部交易入账处理
		transactionPost.posting(item, newTxn, batchDate);
		
		return newTxn;
	}

	/**
	 * 根据给定的内部交易码生成利息交易记录
	 * @param item
	 * @param plan
	 * @param interestAmt
	 * @param batchDate
	 * @param sysTxnCd
	 * @return
	 */
	public CcsPostingTmp generateInterestTransaction(S6000AcctInfo item,CcsPlan plan,BigDecimal interestAmt,Date batchDate,SysTxnCd sysTxnCd){
		if(interestAmt.compareTo(BigDecimal.ZERO) == 0)
			return null;
		SysTxnCdMapping txnCdMap = parameterFacility.loadParameter(String.valueOf(sysTxnCd), SysTxnCdMapping.class);
		TxnCd txnCode = parameterFacility.loadParameter(txnCdMap.txnCd, TxnCd.class);
		CcsTxnSeq txnSeq = new CcsTxnSeq();
		txnSeq.setOrg(item.getAccount().getOrg());
		em.persist(txnSeq);
		
		ProductCredit productCredit = parameterFacility.loadParameter(item.getAccount().getProductCd(), ProductCredit.class);
		String acqAcceptorId=productCredit.financeOrgNo;
		CcsPostingTmp newTxn = generateTrans(txnSeq, item, item.getAccount().getDefaultLogicCardNbr(), plan.getLogicCardNbr(), item.getAccount().getProductCd(), txnCode, interestAmt, batchDate, plan.getPlanNbr(), plan.getRefNbr(),plan.getTerm(),acqAcceptorId);
		// 数据持久化
		em.persist(newTxn);
		// 保存超限费交易
		item.getGenerateTxns().add(newTxn);
		// 调用内部交易入账处理
		transactionPost.posting(item, newTxn, batchDate);
		
		return newTxn;
	}
	/**
	 * 账单日，利息交易生成，并入账
	 * @param item
	 * @param plan 计划可以为空；若为空表示合并入账；则卡产品和逻辑卡号使用账户上的默认值
	 * @param interestAmt 利息金额
	 * @param batchDate 批量日期
	 * @return
	 */
	public CcsPostingTmp generateInterestTransaction(S6000AcctInfo item, CcsPlan plan, BigDecimal interestAmt, Date batchDate) {

		if (interestAmt.compareTo(BigDecimal.ZERO) <= 0) return null;
		
		// 查找系统内部交易类型对照表
		SysTxnCdMapping sysTxnCd = null;
		// 逻辑卡号
		String logicCardNbr = null;
		// 查找产品代码
		String productCd = null;
		if (plan == null || plan.getPlanNbr() == null) {
			sysTxnCd = parameterFacility.loadParameter(String.valueOf(SysTxnCd.S06), SysTxnCdMapping.class);
			// 逻辑卡号
			logicCardNbr = item.getAccount().getDefaultLogicCardNbr();
			// 查找产品代码
			productCd = item.getAccount().getProductCd();
		} else {
			// 根据交易的信用计划号，查找信用计划模板，如果未找到，将抛出异常中断批量
			PlanTemplate planTemplate = parameterFacility.loadParameter(plan.getPlanNbr(), PlanTemplate.class);
			switch (planTemplate.planType){
				case C: sysTxnCd = parameterFacility.loadParameter(String.valueOf(SysTxnCd.S22), SysTxnCdMapping.class); break;
				case R: sysTxnCd = parameterFacility.loadParameter(String.valueOf(SysTxnCd.S23), SysTxnCdMapping.class); break;
				case O: sysTxnCd = parameterFacility.loadParameter(String.valueOf(SysTxnCd.S24), SysTxnCdMapping.class); break;
				case I: sysTxnCd = parameterFacility.loadParameter(String.valueOf(SysTxnCd.S25), SysTxnCdMapping.class); break;
				case D: sysTxnCd = parameterFacility.loadParameter(String.valueOf(SysTxnCd.S26), SysTxnCdMapping.class); break;
				case P:;
				case Q:;
				case J:;
				case L:sysTxnCd = parameterFacility.loadParameter(String.valueOf(SysTxnCd.S46), SysTxnCdMapping.class); break;
					
				default: throw new IllegalArgumentException("计划类型设置不正确");
			}
			// 逻辑卡号
			logicCardNbr = plan.getLogicCardNbr();
			// 查找产品代码
			productCd = plan.getProductCd();
		}
		// 计划号
		String planNbr = null;
		// 交易参考号
		String refNbr = null;
		Integer term = null;
		// 根据交易码，查找交易码对象
		TxnCd txnCode = parameterFacility.loadParameter(sysTxnCd.txnCd, TxnCd.class);
		if (plan == null || plan.getPlanNbr() == null) {
			planNbr = planManager.getPlanNbrByTxnCd(txnCode, item.getAccount().getProductCd());
			term = 0;
		} else {
			planNbr = plan.getPlanNbr();
			refNbr = plan.getRefNbr();
			term = plan.getTerm();
		}
		
		// 交易主键生成
		CcsTxnSeq txnSeq = new CcsTxnSeq();
		txnSeq.setOrg(item.getAccount().getOrg());
		em.persist(txnSeq);
		
		ProductCredit productCredit = parameterFacility.loadParameter(productCd, ProductCredit.class);
		String acqAcceptorId=productCredit.financeOrgNo;

		// 生成内部交易
		CcsPostingTmp newTxn = generateTrans(txnSeq, item, logicCardNbr, logicCardNbr, productCd, txnCode, interestAmt, batchDate, planNbr, refNbr,term,acqAcceptorId);
		// 数据持久化
		em.persist(newTxn);
		// 保存超限费交易
		item.getTxnPosts().add(newTxn);
		// 调用内部交易入账处理
		transactionPost.posting(item, newTxn, batchDate);
		
		return newTxn;
	}
	
	
	/**
	 * 账单日，内部交易生成，并入账
	 * @param item
	 * @param plan 计划可以为空；若为空表示合并入账；则卡产品和逻辑卡号使用账户上的默认值
	 * @param interestAmt 利息金额
	 * @param batchDate 批量日期
	 * @return
	 */
	public CcsPostingTmp generateTxnTransaction(S6000AcctInfo item, CcsPlan plan, BigDecimal txnAmt, Date batchDate,BucketType bucket) {

		if (txnAmt.compareTo(BigDecimal.ZERO) <= 0) return null;
		
		// 查找系统内部交易类型对照表
		SysTxnCdMapping sysTxnCd = null;
		// 逻辑卡号
		String logicCardNbr = null;
		// 查找产品代码
		String productCd = null;
		if (plan == null || plan.getPlanNbr() == null) {
			sysTxnCd = getSysTxnCdForBucket(bucket);
			
			
			// 逻辑卡号
			logicCardNbr = item.getAccount().getDefaultLogicCardNbr();
			// 查找产品代码
			productCd = item.getAccount().getProductCd();
		} else {
			// 根据交易的信用计划号，查找信用计划模板，如果未找到，将抛出异常中断批量
			PlanTemplate planTemplate = parameterFacility.loadParameter(plan.getPlanNbr(), PlanTemplate.class);
			switch (planTemplate.planType){
				case C:;
				case R:;
				case O:;
				case I: sysTxnCd = getSysTxnCdForBucket(bucket);
				case D:;
				case P:;
				case Q:sysTxnCd = getSysTxnCdForBucket(bucket); break;
				case J:;
				case L:sysTxnCd = getSysTxnCdForBucket(bucket); break;
					
				default: throw new IllegalArgumentException("计划类型设置不正确");
			}
			// 逻辑卡号
			logicCardNbr = plan.getLogicCardNbr();
			// 查找产品代码
			productCd = plan.getProductCd();
		}
		// 计划号
		String planNbr = null;
		// 交易参考号
		String refNbr = null;
		Integer term = null;
		// 根据交易码，查找交易码对象
		TxnCd txnCode = parameterFacility.loadParameter(sysTxnCd.txnCd, TxnCd.class);
		if (plan == null || plan.getPlanNbr() == null) {
			planNbr = planManager.getPlanNbrByTxnCd(txnCode, item.getAccount().getProductCd());
			term = 0;
		} else {
			planNbr = plan.getPlanNbr();
			refNbr = plan.getRefNbr();
			term = plan.getTerm();
		}
		
		// 交易主键生成
		CcsTxnSeq txnSeq = new CcsTxnSeq();
		txnSeq.setOrg(item.getAccount().getOrg());
		em.persist(txnSeq);

		ProductCredit productCredit = parameterFacility.loadParameter(productCd, ProductCredit.class);
		String acqAcceptorId=productCredit.financeOrgNo;
		
		// 生成内部交易
		CcsPostingTmp newTxn = generateTrans(txnSeq, item, logicCardNbr, logicCardNbr, productCd, txnCode, txnAmt, batchDate, planNbr, refNbr,term,acqAcceptorId);
		// 数据持久化
		em.persist(newTxn);
		// 保存交易
		item.getTxnPosts().add(newTxn);
		// 调用内部交易入账处理
		transactionPost.posting(item, newTxn, batchDate);
		
		return newTxn;
	}
	
	public SysTxnCdMapping getSysTxnCdForBucket(BucketType bucket){
		SysTxnCdMapping sysTxnCd = null;
		if(BucketType.LifeInsuFee == bucket){
			sysTxnCd = parameterFacility.loadParameter(String.valueOf(SysTxnCd.S72), SysTxnCdMapping.class);
		}else if(BucketType.StampDuty == bucket){
			sysTxnCd = parameterFacility.loadParameter(String.valueOf(SysTxnCd.S71), SysTxnCdMapping.class);
		}else if(BucketType.InsuranceFee == bucket){
			sysTxnCd = parameterFacility.loadParameter(String.valueOf(SysTxnCd.S70), SysTxnCdMapping.class);
		}else if(BucketType.Mulct == bucket){
			sysTxnCd = parameterFacility.loadParameter(String.valueOf(SysTxnCd.S73), SysTxnCdMapping.class);
		}else if(BucketType.SVCFee == bucket){
			sysTxnCd = parameterFacility.loadParameter(String.valueOf(SysTxnCd.S03), SysTxnCdMapping.class);
		}else if(BucketType.ReplaceSvcFee == bucket) {
			sysTxnCd = parameterFacility.loadParameter(String.valueOf(SysTxnCd.D09), SysTxnCdMapping.class);
		}else if(BucketType.ReplacePenalty == bucket){
			sysTxnCd = parameterFacility.loadParameter(String.valueOf(SysTxnCd.D13), SysTxnCdMapping.class);
		}else if(BucketType.ReplaceMulct == bucket){
			sysTxnCd = parameterFacility.loadParameter(String.valueOf(SysTxnCd.D12), SysTxnCdMapping.class);
		}else if(BucketType.ReplaceLatePaymentCharge == bucket){
			sysTxnCd = parameterFacility.loadParameter(String.valueOf(SysTxnCd.D14), SysTxnCdMapping.class);
		}else if(BucketType.PrepayPkg==bucket){
			sysTxnCd = parameterFacility.loadParameter(String.valueOf(SysTxnCd.D17), SysTxnCdMapping.class);
		}else{
			return null;
		}
		return sysTxnCd;
	}
	
	/**
	 * 内部积分交易生成，并入账
	 * 
	 * @param item
	 * @param txnPost
	 * @param txnCd
	 * @param returnPoint
	 * @param batchDate
	 * @param newTxnPosts 
	 * @return
	 */
	public CcsPostingTmp generatePoint(S6000AcctInfo item, CcsPostingTmp txnPost, TxnCd txnCd, BigDecimal returnPoint, Date batchDate, List<CcsPostingTmp> newTxnPosts) {
		logger.info("内部积分交易生成");
		
		// 交易主键生成
		CcsTxnSeq txnSeq = new CcsTxnSeq();
		txnSeq.setOrg(txnPost.getOrg());
		em.persist(txnSeq);

		// 生成内部交易
		CcsPostingTmp newTxn = new CcsPostingTmp();
		newTxn.setOrg(txnPost.getOrg()); // 机构号
		newTxn.setTxnSeq(txnSeq.getTxnSeq()); // 交易流水号
		newTxn.setAcctNbr(txnPost.getAcctNbr()); // 账户编号
		newTxn.setAcctType(txnPost.getAcctType()); // 账户类型
		newTxn.setCardNbr(txnPost.getCardNbr()); // 介质卡号
		newTxn.setLogicCardNbr(txnPost.getLogicCardNbr()); // 逻辑卡号
		newTxn.setCardBasicNbr(txnPost.getCardBasicNbr()); // 逻辑卡主卡卡号
		newTxn.setProductCd(txnPost.getProductCd()); // 产品代码
		newTxn.setTxnDate(batchDate); // 交易日期
		newTxn.setTxnTime(new Date()); // 交易时间
		newTxn.setPostTxnType(PostTxnType.P); // 入账交易类型
		newTxn.setTxnCode(txnCd.txnCd); // 交易码
		newTxn.setDbCrInd(txnCd.logicMod.getDbCrInd()); // 借贷标志
		newTxn.setTxnAmt(returnPoint); // 交易金额
		newTxn.setPostAmt(returnPoint); // 入账币种金额
		newTxn.setPostDate(batchDate); // 入账日期
		newTxn.setAuthCode(null); // 授权码
		newTxn.setCardBlockCode(txnPost.getCardBlockCode()); // 卡片锁定码
		newTxn.setTxnCurrency(txnPost.getTxnCurrency()); // 交易币种代码
		newTxn.setPostCurrency(txnPost.getPostCurrency()); // 入账币种代码
		newTxn.setOrigTransDate(batchDate); // 原始交易日期
		newTxn.setPlanNbr(planManager.getPlanNbrByTxnCd(txnCd, txnPost.getProductCd())); // 信用计划号
		newTxn.setRefNbr(null); // 交易参考号
		newTxn.setTxnDesc(txnCd.description); // 交易描述
		newTxn.setTxnShortDesc(txnCd.shortDesc);
		newTxn.setPoints(BigDecimal.ZERO); // 积分数值
		newTxn.setPostingFlag(PostingFlag.F00); // 入账结果标示码
		newTxn.setPrePostingFlag(PostingFlag.F00);
		newTxn.setRelPmtAmt(BigDecimal.ZERO); // 公司卡还款金额
		newTxn.setOrigPmtAmt(BigDecimal.ZERO); // 还款交易原始金额
		newTxn.setAcqBranchIq(txnPost.getAcqBranchIq()); // 受理分行代码
		newTxn.setAcqTerminalId(txnPost.getAcqTerminalId()); // 受理机构终端标识码
		newTxn.setAcqAcceptorId(txnPost.getAcqAcceptorId()); // 受卡方标识码
		newTxn.setAcqAddress(txnPost.getAcqAddress()); // 受理机构名称地址
		newTxn.setMcc(txnPost.getMcc()); // 商户类别代码
		newTxn.setOrigTxnCode(txnCd.txnCd); // 原交易交易码
		newTxn.setOrigTxnAmt(returnPoint); // 原交易交易金额
		newTxn.setOrigSettAmt(returnPoint); // 原交易清算金额
		newTxn.setInterchangeFee(BigDecimal.ZERO); // 原交易货币转换费
		newTxn.setFeePayout(BigDecimal.ZERO); // 原交易交易手续费
		newTxn.setFeeProfit(BigDecimal.ZERO); // 发卡方应得手续费收入
		newTxn.setLoanIssueProfit(BigDecimal.ZERO); // 分期交易发卡行收益
		newTxn.setStmtDate(null); // 账单日期
		newTxn.setVoucherNo(null); // 销售单凭证号
		// 数据持久化
		em.persist(newTxn);
		newTxnPosts.add(newTxn);
		// 调用内部交易入账处理
		transactionPost.posting(item, newTxn, batchDate);
		
		return newTxn;
	}
	
	public CcsPostingTmp generateLoanFee(S6000AcctInfo item, CcsPostingTmp txnPost, CcsLoan loan, CcsPlan plan, TxnCd txnCd, BigDecimal returnFeeAmt, Date batchDate, List<CcsPostingTmp> newTxnPosts) {

		// 交易主键生成
		CcsTxnSeq txnSeq = new CcsTxnSeq();
		txnSeq.setOrg(item.getAccount().getOrg());
		em.persist(txnSeq);
		ProductCredit productCredit = parameterFacility.loadParameter(item.getAccount().getProductCd(), ProductCredit.class);
		String acqAcceptorId=productCredit.financeOrgNo;
		// 生成分期手续费
		CcsPostingTmp newTxn = generateTrans(txnSeq, item, txnPost.getCardNbr(), txnPost.getLogicCardNbr(), txnPost.getProductCd(), txnCd, returnFeeAmt, loan.getOrigTransDate(), plan.getPlanNbr(), loan.getRefNbr(),plan.getTerm(),acqAcceptorId);
		newTxn.setOrigTxnAmt(BigDecimal.ZERO); // 原交易交易金额
		newTxn.setOrigSettAmt(BigDecimal.ZERO); // 原交易清算金额
		// 数据持久化
		em.persist(newTxn);
		newTxnPosts.add(newTxn);		
		// 调用内部交易入账处理
		transactionPost.posting(item, newTxn, batchDate);

		return newTxn;
	}
	
	/**
	 * 
	 * @param txnSeq
	 * @param item
	 * @param cardNo
	 * @param logicCardNo
	 * @param productCd
	 * @param txnCd
	 * @param amt
	 * @param origTransDate
	 * @param planNbr
	 * @param refNbr
	 * @return
	 */
	private CcsPostingTmp generateTrans(CcsTxnSeq txnSeq,S6000AcctInfo item,String cardNo,String logicCardNo,String productCd,TxnCd txnCd,BigDecimal amt,Date origTransDate,String planNbr,String refNbr,Integer term,String acqAcceptorId){
		Date batchDate = batchStatusFacility.getBatchDate();
		CcsPostingTmp newTxn = new CcsPostingTmp();
		newTxn.setOrg(item.getAccount().getOrg()); // 机构号
		newTxn.setTxnSeq(txnSeq.getTxnSeq()); // 交易流水号
		newTxn.setAcctNbr(item.getAccount().getAcctNbr()); // 账户编号
		newTxn.setAcctType(item.getAccount().getAcctType()); // 账户类型
		newTxn.setCardNbr(cardNo); // 介质卡号
		newTxn.setLogicCardNbr(logicCardNo); // 逻辑卡号
		newTxn.setCardBasicNbr(logicCardNo); // 逻辑卡主卡卡号
		newTxn.setProductCd(productCd); // 产品代码
		newTxn.setTxnDate(batchDate); // 交易日期
		newTxn.setTxnTime(new Date()); // 交易时间
		newTxn.setPostTxnType(PostTxnType.M); // 入账交易类型
		newTxn.setTxnCode(txnCd.txnCd); // 交易码
		newTxn.setDbCrInd(txnCd.logicMod.getDbCrInd()); // 借贷标志
		newTxn.setTxnAmt(amt); // 交易金额
		newTxn.setPostAmt(amt); // 入账币种金额
		newTxn.setPostDate(batchDate); // 入账日期
		newTxn.setAuthCode(""); // 授权码
		newTxn.setCardBlockCode(""); // 卡片锁定码
		newTxn.setTxnCurrency(item.getAccount().getCurrency()); // 交易币种代码
		newTxn.setPostCurrency(item.getAccount().getCurrency()); // 入账币种代码
		newTxn.setOrigTransDate(origTransDate); // 原始交易日期
		// 如何确定分期交易原先使用的卡产品？是使用帐户上的默认卡产品，还是要根据原交易来确定？
		newTxn.setPlanNbr(planNbr); // 信用计划号
		newTxn.setRefNbr(refNbr); // 交易参考号
		newTxn.setTxnDesc(txnCd.description); // 交易描述
		newTxn.setTxnShortDesc(txnCd.shortDesc);
		newTxn.setPoints(BigDecimal.ZERO); // 积分数值
		newTxn.setPostingFlag(PostingFlag.F00); // 入账结果标示码
		newTxn.setPrePostingFlag(PostingFlag.F00);
		newTxn.setRelPmtAmt(BigDecimal.ZERO); // 公司卡还款金额
		newTxn.setOrigPmtAmt(BigDecimal.ZERO); // 还款交易原始金额
		newTxn.setAcqBranchIq(""); // 受理分行代码
		newTxn.setAcqTerminalId(""); // 受理机构终端标识码
		newTxn.setAcqAcceptorId(""); // 受卡方标识码
		newTxn.setAcqAddress(""); // 受理机构名称地址
		newTxn.setMcc(""); // 商户类别代码
		newTxn.setOrigTxnCode(""); // 原交易交易码
		newTxn.setOrigTxnAmt(amt); // 原交易交易金额
		newTxn.setOrigSettAmt(amt); // 原交易清算金额
		newTxn.setInterchangeFee(BigDecimal.ZERO); // 原交易货币转换费
		newTxn.setFeePayout(BigDecimal.ZERO); // 原交易交易手续费
		newTxn.setFeeProfit(BigDecimal.ZERO); // 发卡方应得手续费收入
		newTxn.setLoanIssueProfit(BigDecimal.ZERO); // 分期交易发卡行收益
		newTxn.setStmtDate(item.getAccount().getNextStmtDate()); // 账单日期
		newTxn.setVoucherNo(""); // 销售单凭证号
		newTxn.setTerm(term);
		return newTxn;
	}
}
