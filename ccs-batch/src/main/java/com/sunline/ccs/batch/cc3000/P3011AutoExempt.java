/**
 * 
 */
package com.sunline.ccs.batch.cc3000;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.batch.common.DateUtils;
import com.sunline.ccs.batch.common.TxnPrepare;
import com.sunline.ccs.batch.front.FrontBatchUtil;
import com.sunline.ccs.facility.BlockCodeUtils;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.CcsPostingTmp;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnWaiveLog;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnWaiveLogHst;
import com.sunline.ccs.infrastructure.shared.model.QCcsPlan;
import com.sunline.ccs.infrastructure.shared.model.QCcsPostingTmp;
import com.sunline.ccs.infrastructure.shared.model.QCcsTxnWaiveLog;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.param.def.SysTxnCdMapping;
import com.sunline.ccs.param.def.TxnCd;
import com.sunline.ccs.param.def.enums.AdjState;
import com.sunline.ccs.param.def.enums.SysTxnCd;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.BucketType;
import com.sunline.ppy.dictionary.enums.DbCrInd;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.PostTxnType;
import com.sunline.ppy.dictionary.enums.PostingFlag;

/**  
 * @描述		: 自动豁免processor
 *  
 * @作者		: JiaoJian 
 * @创建时间	: 2015年11月12日  上午11:07:18   
 */
public class P3011AutoExempt implements ItemProcessor<CcsLoan, CcsLoan>{
	
	private static final Logger logger = LoggerFactory.getLogger(P3011AutoExempt.class);
	
	@Autowired
	private UnifiedParameterFacility parameterFacility;
	
	@Autowired
	private BatchStatusFacility batchFacility;
	
	@PersistenceContext
	private EntityManager em;
	
	@Autowired
	private TxnPrepare txnPrepare;
	@Autowired
	private CustAcctCardFacility queryFacility;
	
	@Autowired
	private BlockCodeUtils blockcodeUtils;
	@Autowired
	private FrontBatchUtil frontBatchUtil;
	
	
	@Override
	public CcsLoan process(CcsLoan loan) throws Exception {
		logger.debug("自动豁免，处理借据：{}", loan.getDueBillNo());
		OrganizationContextHolder.setCurrentOrg(loan.getOrg());
		
		LoanPlan loanPlanParam = parameterFacility.loadParameter(loan.getLoanCode(), LoanPlan.class);
		
		if(loanPlanParam.isAutoWaive.equals(Indicator.N)) return null; //不支持自动豁免
		if(isExistWaive(loan) > 0) return null;//存在手工豁免
		if(isExistPosting(loan) > 0) return null;//存在入账交易
		if (frontBatchUtil.getWOrderCount(loan.getAcctNbr(), loan.getAcctType()) > 0) return null;// 若有处理中订单
//		if () return null;// 当日已有成功扣款
		
		//计算逾期天数
		int overdueDays = DateUtils.getIntervalDays(loan.getCpdBeginDate(), batchFacility.getBatchDate());
		//逾期天数（CPD）>=自动豁免天数 才进行豁免
		if(overdueDays < loanPlanParam.autoWaiveCpdDays) {
			logger.debug("借据号：{}， cpd开始日期：{}， 批量日期：{}，计算出的逾期天数：{}, 自动豁免天数：{}，  不支持自动豁免",
					loan.getDueBillNo(), loan.getCpdBeginDate(), batchFacility.getBatchDate(),overdueDays, loanPlanParam.autoWaiveCpdDays);
			return null;
		}
		
		//获取loan下面的所有plan
		QCcsPlan p = QCcsPlan.ccsPlan;
		List<CcsPlan> planList = new JPAQuery(em).from(p)
								    .where(p.acctNbr.eq(loan.getAcctNbr())
								    	.and(p.acctType.eq(loan.getAcctType()))
								    	.and(p.refNbr.eq(loan.getRefNbr()))
								    	).list(p);
		
		if(null == planList || planList.isEmpty()) {
			return null;
		}
		
		BigDecimal totalOverdueMoney = BigDecimal.ZERO; //所有欠款金额
		BigDecimal prin   = BigDecimal.ZERO;   //本金
		
		//获取累计欠款（不包含本金，如果有未还的本金，该loan不能豁免）
		for(CcsPlan ccsPlan : planList) {
			prin = prin.add(ccsPlan.getPastPrincipal()).add(ccsPlan.getCtdPrincipal());
			totalOverdueMoney = totalOverdueMoney.add(ccsPlan.getCurrBal());
		}
		
		//逾期欠款金额小于自动豁免金额阀值才能豁免
		if(prin.compareTo(BigDecimal.ZERO) > 0 || totalOverdueMoney.compareTo(loanPlanParam.autoWaiveAMT) > 0) {
			logger.debug("借据号：{}，逾期欠款金额：{}， 自动豁免最小金额：{}，本金欠款金额：{}， 不满足自动豁免条件",
					loan.getDueBillNo(), totalOverdueMoney, loanPlanParam.autoWaiveAMT,prin);
			return null;
		}
		
		//根据信用计划豁免
		for(CcsPlan ccsPlan : planList) {
			
			BigDecimal interest   = BigDecimal.ZERO;   //利息
			BigDecimal cardFee = BigDecimal.ZERO; // 年费
			BigDecimal lateFee = BigDecimal.ZERO; // 滞纳金
			BigDecimal serviceFee = BigDecimal.ZERO; //服务费
			BigDecimal fine = BigDecimal.ZERO;       //罚金
			BigDecimal lifeAmt = BigDecimal.ZERO;    //寿险计划
			BigDecimal txnFee = BigDecimal.ZERO; //交易费
			BigDecimal insurance = BigDecimal.ZERO; //保费
			BigDecimal replaceSvcFee = BigDecimal.ZERO; // 代收服务费
			
			//豁免利息交易
			interest = interest.add(ccsPlan.getPastInterest()).add(ccsPlan.getCtdInterest());
			logger.info("planId：{}，利息：{}", ccsPlan.getPlanId(), interest);
			if(interest.compareTo(BigDecimal.ZERO) > 0) {
				CcsPostingTmp postingTmpInterest = generatePostingTmp(loan, loanPlanParam, SysTxnCd.C03, 
						interest,ccsPlan.getPlanNbr(),ccsPlan.getTerm());
				txnPrepare.txnPrepare(postingTmpInterest, null);
				insertTxnWaiveLogHst(loan, interest,BucketType.Interest,SysTxnCd.C03);
			}
			
			//豁免年费交易
			cardFee = cardFee.add(ccsPlan.getPastCardFee()).add(ccsPlan.getCtdCardFee());
			logger.info("planId：{}，年费：{}", ccsPlan.getPlanId(), cardFee);
			if(cardFee.compareTo(BigDecimal.ZERO) > 0) {
				CcsPostingTmp postingTmpInterest = generatePostingTmp(loan, loanPlanParam, SysTxnCd.C02, 
						cardFee,ccsPlan.getPlanNbr(),ccsPlan.getTerm());
				txnPrepare.txnPrepare(postingTmpInterest, null);
				insertTxnWaiveLogHst(loan, cardFee,BucketType.CardFee,SysTxnCd.C02);
			}
			
			//豁免滞纳金交易
			lateFee = lateFee.add(ccsPlan.getPastLateFee()).add(ccsPlan.getCtdLateFee());
			logger.info("planId：{}，滞纳金：{}", ccsPlan.getPlanId(), lateFee);
			if(lateFee.compareTo(BigDecimal.ZERO) > 0) {
				CcsPostingTmp postingTmpInterest = generatePostingTmp(loan, loanPlanParam, SysTxnCd.S05, 
						lateFee,ccsPlan.getPlanNbr(),ccsPlan.getTerm());
				txnPrepare.txnPrepare(postingTmpInterest, null);
				insertTxnWaiveLogHst(loan, lateFee,BucketType.LatePaymentCharge,SysTxnCd.S05);
			}
			
			//豁免服务费交易
			serviceFee = serviceFee.add(ccsPlan.getPastSvcFee()).add(ccsPlan.getCtdSvcFee());
			logger.info("planId：{}，服务费：{}", ccsPlan.getPlanId(), serviceFee);
			if(serviceFee.compareTo(BigDecimal.ZERO) > 0) {
				CcsPostingTmp postingTmpServiceFee = generatePostingTmp(loan,loanPlanParam, SysTxnCd.S78, serviceFee,
						ccsPlan.getPlanNbr(),ccsPlan.getTerm());
				txnPrepare.txnPrepare(postingTmpServiceFee, null);
				insertTxnWaiveLogHst(loan, serviceFee,BucketType.SVCFee,SysTxnCd.S78);
			}
			
			//豁免罚金交易
			fine = fine.add(ccsPlan.getPastMulctAmt()).add(ccsPlan.getCtdMulctAmt());
			logger.info("planId：{}，罚金：{}", ccsPlan.getPlanId(), fine);
			if(fine.compareTo(BigDecimal.ZERO) > 0) {
				CcsPostingTmp postingTmpFine = generatePostingTmp(loan,loanPlanParam,  SysTxnCd.S74, fine,
						ccsPlan.getPlanNbr(),ccsPlan.getTerm());
				txnPrepare.txnPrepare(postingTmpFine, null);
				insertTxnWaiveLogHst(loan, fine,BucketType.Mulct,SysTxnCd.S74);
			}
			
			//豁免寿险费交易
			lifeAmt = lifeAmt.add(ccsPlan.getPastLifeInsuAmt()).add(ccsPlan.getCtdLifeInsuAmt());
			logger.info("planId：{}，寿险费：{}", ccsPlan.getPlanId(), lifeAmt);
			if(lifeAmt.compareTo(BigDecimal.ZERO) > 0) {
				
				CcsPostingTmp postingTmpLifeAmt = generatePostingTmp(loan, loanPlanParam, SysTxnCd.S76, lifeAmt,
						ccsPlan.getPlanNbr(),ccsPlan.getTerm());
				txnPrepare.txnPrepare(postingTmpLifeAmt, null);
				insertTxnWaiveLogHst(loan,lifeAmt,BucketType.LifeInsuFee,SysTxnCd.S76);
			}
			
			//豁免交易费交易
			txnFee = txnFee.add(ccsPlan.getPastTxnFee()).add(ccsPlan.getCtdTxnFee());
			logger.info("planId：{}，交易费：{}", ccsPlan.getPlanId(), txnFee);
			if(txnFee.compareTo(BigDecimal.ZERO) > 0) {
				CcsPostingTmp postingTmpServiceFee = generatePostingTmp(loan,loanPlanParam, SysTxnCd.C01, txnFee,
						ccsPlan.getPlanNbr(),ccsPlan.getTerm());
				txnPrepare.txnPrepare(postingTmpServiceFee, null);
				insertTxnWaiveLogHst(loan, txnFee,BucketType.TXNFee,SysTxnCd.C01);
			}
			
			//豁免保费交易
			insurance = insurance.add(ccsPlan.getPastInsurance()).add(ccsPlan.getCtdInsurance());
			logger.info("planId：{}，保费：{}", ccsPlan.getPlanId(), insurance);
			if(insurance.compareTo(BigDecimal.ZERO) > 0) {
				CcsPostingTmp postingTmpServiceFee = generatePostingTmp(loan,loanPlanParam, SysTxnCd.S75, insurance,
						ccsPlan.getPlanNbr(),ccsPlan.getTerm());
				txnPrepare.txnPrepare(postingTmpServiceFee, null);
				insertTxnWaiveLogHst(loan, insurance,BucketType.InsuranceFee,SysTxnCd.S75);
			}
			
			//豁免代收服务费交易
			replaceSvcFee = replaceSvcFee.add(ccsPlan.getPastReplaceSvcFee()).add(ccsPlan.getCtdReplaceSvcFee());
			logger.info("planId：{}，代收服务费：{}", ccsPlan.getPlanId(), replaceSvcFee);
			if(replaceSvcFee.compareTo(BigDecimal.ZERO) > 0) {
				CcsPostingTmp postingTmpServiceFee = generatePostingTmp(loan,loanPlanParam, SysTxnCd.C09, replaceSvcFee,
						ccsPlan.getPlanNbr(),ccsPlan.getTerm());
				txnPrepare.txnPrepare(postingTmpServiceFee, null);
				insertTxnWaiveLogHst(loan, replaceSvcFee,BucketType.ReplaceSvcFee,SysTxnCd.C09);
			}
			
		}
		
		CcsAcct acct = queryFacility.getAcctByAcctNbr(loan.getAcctType(), loan.getAcctNbr());
		acct.setBlockCode(blockcodeUtils.addBlockCode(acct.getBlockCode(), "I"));
		return loan;
	}
	
	
	/**
	 * @描述		: 生成豁免日志记录
	 *
	 * @作者		: JiaoJian
	 * @创建时间	: 2015年11月13日 下午5:27:48
	 *
	 * @参数说明	:
	 * @param loan
	 * @param txnAmt
	 *
	 */
	private void insertTxnWaiveLogHst(CcsLoan loan, BigDecimal txnAmt,BucketType b,SysTxnCd sysTxnCd) {
		
		SysTxnCdMapping sysTxnCdMapping = parameterFacility.loadParameter(sysTxnCd.toString(), SysTxnCdMapping.class);
		TxnCd txnCd = parameterFacility.loadParameter(sysTxnCdMapping.txnCd, TxnCd.class);
		
		CcsTxnWaiveLog ccsTxnWaiveLog = new CcsTxnWaiveLog();
		ccsTxnWaiveLog.setContrNbr(loan.getContrNbr());
		ccsTxnWaiveLog.setOrg(loan.getOrg());
		ccsTxnWaiveLog.setOpTime(new Date());
		ccsTxnWaiveLog.setOpId("batch");
		ccsTxnWaiveLog.setAcctNbr(loan.getAcctNbr());
		ccsTxnWaiveLog.setAcctType(loan.getAcctType());
		ccsTxnWaiveLog.setCardNbr(loan.getCardNbr());
		ccsTxnWaiveLog.setTxnAmt(txnAmt);
		ccsTxnWaiveLog.setTxnCode(txnCd.txnCd);
		ccsTxnWaiveLog.setDbCrInd(DbCrInd.C);
		ccsTxnWaiveLog.setCurrency("156");
		ccsTxnWaiveLog.setRefNbr(loan.getRefNbr());
		ccsTxnWaiveLog.setRemark("自动豁免");
		ccsTxnWaiveLog.setTxnDate(batchFacility.getBatchDate());
		ccsTxnWaiveLog.setLogBizDate(batchFacility.getBatchDate());
		ccsTxnWaiveLog.setCreateTime(new Date());
		ccsTxnWaiveLog.setCreateUser("自动豁免");
		ccsTxnWaiveLog.setLstUpdTime(new Date());
		ccsTxnWaiveLog.setLstUpdUser("自动豁免");
		ccsTxnWaiveLog.setAdjState(AdjState.A);
		ccsTxnWaiveLog.setBucketType(b);
		ccsTxnWaiveLog.setLoanId(loan.getLoanId());
		ccsTxnWaiveLog.setLoanType(loan.getLoanType());
		em.persist(ccsTxnWaiveLog);
		
		CcsTxnWaiveLogHst txnWaiveLogHst = new CcsTxnWaiveLogHst();
		txnWaiveLogHst.updateFromMap(ccsTxnWaiveLog.convertToMap());
		em.persist(txnWaiveLogHst);
		em.remove(ccsTxnWaiveLog);
		
	}
	
	
	/**
	 * @描述		: 生成豁免交易
	 *
	 * @作者		: JiaoJian
	 * @创建时间	: 2015年11月13日 上午11:40:27
	 *
	 * @参数说明	:
	 * @param loan
	 * @param sysTxnCd
	 * @param txnAmt
	 * @return
	 *
	 */
	private CcsPostingTmp generatePostingTmp(CcsLoan loan, LoanPlan loanPlanParam, SysTxnCd sysTxnCd, BigDecimal txnAmt,String planNbr,Integer term) {
		
		SysTxnCdMapping sysTxnCdMapping = parameterFacility.loadParameter(sysTxnCd.toString(), SysTxnCdMapping.class);
		TxnCd txnCd = parameterFacility.loadParameter(sysTxnCdMapping.txnCd, TxnCd.class);

		
		CcsPostingTmp ttTxnPost = new CcsPostingTmp();
		
		ttTxnPost.setRefNbr(loan.getRefNbr()); //交易参考号
		ttTxnPost.setPlanNbr(planNbr);// 信用计划号
		ttTxnPost.setTerm(term);
		ttTxnPost.setAcctNbr(loan.getAcctNbr());
		ttTxnPost.setAcctType(loan.getAcctType());
		ttTxnPost.setCardNbr(loan.getCardNbr());
		ttTxnPost.setTxnCurrency(loan.getAcctType().getCurrencyCode());
		ttTxnPost.setTxnAmt(txnAmt);
		ttTxnPost.setPostCurrency(loan.getAcctType().getCurrencyCode());
		ttTxnPost.setPostAmt(txnAmt);
		ttTxnPost.setPostTxnType(PostTxnType.M);
		ttTxnPost.setTxnCode(txnCd.txnCd);
		ttTxnPost.setOrigTxnCode(txnCd.txnCd);
		ttTxnPost.setDbCrInd(txnCd.logicMod.getDbCrInd());
		ttTxnPost.setTxnDesc(txnCd.description);
		ttTxnPost.setTxnShortDesc(txnCd.shortDesc);
		ttTxnPost.setOrigTxnAmt(txnAmt);
		ttTxnPost.setOrigSettAmt(txnAmt);
		ttTxnPost.setProductCd(loanPlanParam.productCode);
		
		
		ttTxnPost.setOrg(loan.getOrg());
		ttTxnPost.setTxnTime(batchFacility.getBatchDate());
		ttTxnPost.setPostDate(batchFacility.getBatchDate());
		ttTxnPost.setTxnDate(batchFacility.getBatchDate());
		ttTxnPost.setOrigTransDate(loan.getRegisterDate());
		
		ttTxnPost.setAuthCode(loan.getOrigAuthCode());
		ttTxnPost.setOrigPmtAmt(BigDecimal.ZERO);
		ttTxnPost.setPoints(BigDecimal.ZERO);
		ttTxnPost.setPrePostingFlag(PostingFlag.F00);
		ttTxnPost.setPostingFlag(PostingFlag.F00);
		ttTxnPost.setRelPmtAmt(BigDecimal.ZERO);
		ttTxnPost.setInterchangeFee(BigDecimal.ZERO);
		ttTxnPost.setFeePayout(BigDecimal.ZERO);
		ttTxnPost.setFeeProfit(BigDecimal.ZERO);
		ttTxnPost.setLoanIssueProfit(BigDecimal.ZERO);
		ttTxnPost.setLoanCode(loan.getLoanCode());
		return ttTxnPost;
	}
	private int isExistWaive(CcsLoan loan){
		QCcsTxnWaiveLog q = QCcsTxnWaiveLog.ccsTxnWaiveLog;
		return new JPAQuery(em).from(q)
				.where(q.acctNbr.eq(loan.getAcctNbr())
						.and(q.acctType.eq(loan.getAcctType()))
						.and(q.loanId.eq(loan.getLoanId())))
				.list(q.opSeq).size();
	}
	
	private int isExistPosting(CcsLoan loan){
		QCcsPostingTmp q = QCcsPostingTmp.ccsPostingTmp;
		return new JPAQuery(em).from(q)
				.where(q.acctNbr.eq(loan.getAcctNbr())
						.and(q.acctType.eq(loan.getAcctType())))
				.list(q.txnSeq).size();
	}

}
