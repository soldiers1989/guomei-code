package com.sunline.ccs.batch.cc6000;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
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
import com.sunline.ccs.batch.cc3000.U3001LoanPrepare;
import com.sunline.ccs.batch.cc6000.common.TransactionPost;
import com.sunline.ccs.batch.common.TxnPrepare;
import com.sunline.ccs.facility.BlockCodeUtils;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.CcsPostingTmp;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoanReg;
import com.sunline.ccs.loan.TxnLoanProvideImpl;
import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.enums.SysTxnCd;
import com.sunline.ppy.dictionary.enums.LoanRegStatus;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.PlanType;
import com.sunline.ppy.dictionary.enums.PostingFlag;


/**
 * @see 类名：P6014AutoLoanR
 * @see 描述：自动消费转分期
 *
 * @see 创建日期：   2015-6-24上午10:06:35
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class P6014AutoLoanR implements ItemProcessor<S6000AcctInfo, S6000AcctInfo> {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@PersistenceContext
	private EntityManager em;
	@Autowired
	private UnifiedParamFacilityProvide unifiedParameterFacilityProvide;
	@Autowired
	private BatchStatusFacility batchStatusFacility;
	@Autowired
	private CustAcctCardFacility custAcctCardQueryFacility;
	@Autowired
	private BlockCodeUtils blockCodeUtils;
	@Autowired
	private U3001LoanPrepare loanPrepare;
	@Autowired
	private TransactionPost transactionPost;
	@Autowired
	private TxnPrepare txnPrepare;
	
	QCcsLoanReg qTmLoanReg = QCcsLoanReg.ccsLoanReg;
	
	
	@Override
	public S6000AcctInfo process(S6000AcctInfo item) throws Exception {
		logger.debug("自动消费转分期检查,Org:" + item.getAccount().getOrg()
				+ "] AcctNo:[" + item.getAccount().getAcctNbr()
				+ "] AcctType:[" + item.getAccount().getAcctType()
				+ "],TxnPost.size[" + item.getTxnPosts().size()
				+ "]");
		//账户检查
		if(!checkAccount(item.getAccount())){
			return item;
		}
		
		List<CcsPostingTmp> addTxnPosts = new ArrayList<CcsPostingTmp>();
		// 待入账流水
		for(CcsPostingTmp txnPost : item.getTxnPosts()){
			//卡片检查
			if(!checkCard(txnPost)){
				continue;
			}
			//是否支持自动消费转分期
			ProductCredit pc = unifiedParameterFacilityProvide.productCredit(txnPost.getProductCd());
			if(pc.autoLoanR==null||!pc.autoLoanR){
				continue;
			}
			// 获取分期计划参数
			LoanPlan loanPlan = unifiedParameterFacilityProvide.loanPlan(txnPost.getProductCd(), LoanType.R);
			//流水检查
			if(!checkTxnPost(txnPost, loanPlan, pc)){
				continue;
			}
			// 获取账户层参数
			AccountAttribute accountAttribute = unifiedParameterFacilityProvide.acct_attribute(item.getAccount().getProductCd());
			addTxnPosts.addAll(autoLoanR(item, txnPost, loanPlan, accountAttribute, pc));
		}
		
		item.getTxnPosts().addAll(addTxnPosts);
		
		return item;
	}
	
	/**
	 * 自动消费转分期,生成TmLoanReg
	 * @param txnPost
	 * @param tmAccount
	 * @param loanPlan
	 * @param accountAttribute
	 * @param pc
	 * 
	 * @throws Exception 
	 */
	private List<CcsPostingTmp> autoLoanR(S6000AcctInfo item, CcsPostingTmp txnPost, LoanPlan loanPlan, AccountAttribute accountAttribute, ProductCredit pc) throws Exception{
		logger.info("开始自动消费转分期处理,RefNbr:[" + txnPost.getRefNbr() + "]");
		List<CcsPostingTmp> ttTxnPosts = new ArrayList<CcsPostingTmp>(); 
		
		CcsAcct tmAccount = item.getAccount();
		//当前消费计划总本金
		BigDecimal retailPrin = getRetailPrin(item.getPlans());
		//原交易金额
		BigDecimal origRetailAmt = txnPost.getPostAmt();
		//总余额
		BigDecimal currBal = tmAccount.getCurrBal();
		//信用额度
		BigDecimal creditLimit = getCreditLmt(tmAccount, loanPlan);
		//已分期余额
		BigDecimal appedAmt = getAppedAmt(tmAccount);
		//分期余额
		BigDecimal loanBal = tmAccount.getLoanBal();
		//额度内可分期比例
		BigDecimal loanLimitRate = getLoanLimitRate(tmAccount, accountAttribute);
		
		BigDecimal r0 = origRetailAmt;
		BigDecimal r1 = getNonNegativeAmt(retailPrin.subtract(appedAmt));
		BigDecimal r2 = getNonNegativeAmt(creditLimit.multiply(loanLimitRate).subtract(appedAmt).subtract(loanBal));
		BigDecimal r4 = getNonNegativeAmt(currBal.subtract(appedAmt).subtract(loanBal));
		
		//排序取最小值
		ArrayList<BigDecimal> appAmtList = new ArrayList<BigDecimal>();
		appAmtList.add(r0);
		appAmtList.add(r1);
		appAmtList.add(r2);
		appAmtList.add(r4);
		Collections.sort(appAmtList);
		
		//最终申请分期金额
		BigDecimal appAmt = appAmtList.get(0).setScale(2, BigDecimal.ROUND_HALF_UP);
		
		// 获取分期参数
		LoanFeeDef loanFeeDef = unifiedParameterFacilityProvide.loanFeeDef(tmAccount.getProductCd(), LoanType.R, pc.autoLoanRInitTerm);
		// 判断申请金额是否小于分期最小允许金额LOAN_MIN_AMT
		if (appAmt.compareTo(loanFeeDef.minAmount) < 0) {
			return ttTxnPosts;
		}
		//判断申请金额是否大于分期最大允许金额
		if(appAmt.compareTo(loanFeeDef.maxAmount) > 0){
			return ttTxnPosts;
		}
		//生成TmLoanReg
		if (loanFeeDef.distributeMethod == null) {
			throw new IllegalArgumentException("LoanFeeDef参数异常");
		}
		TxnLoanProvideImpl txnLoanProvideImpl = new TxnLoanProvideImpl(LoanType.R, loanFeeDef, txnPost);
		CcsLoanReg loanReg = txnLoanProvideImpl.genLoanReg(pc.autoLoanRInitTerm, appAmt, txnPost.getRefNbr(), txnPost.getLogicCardNbr(), txnPost.getCardNbr(), pc.autoLoanRFeeMethod,
				txnPost.getAcctNbr(), txnPost.getAcctType(), loanPlan.loanCode, unifiedParameterFacilityProvide.BusinessDate());
		//将分期注册状态设置为R|自动消费转分期
		loanReg.setLoanRegStatus(LoanRegStatus.R);
		//已经日切,不能使用业务日期而使用批量日期
		loanReg.setRegisterDate(batchStatusFacility.getBatchDate());
		
		// 保存分期数据
		em.persist(loanReg);
		//生成分期信息及交易入账
		ttTxnPosts.addAll(addLoanRAndTransactionPost(item, loanReg));
		//生成报表
		item.getLoanSuccessRptItems().add(loanPrepare.makeLoanSuccessItem(loanReg));
		//保存reg历史数据
		em.persist(loanPrepare.generateLoanRegHst(loanReg, loanReg.getLoanInitTerm(), loanReg.getLoanInitPrin()));
		em.remove(loanReg);
		// TODO 暂不发短信
		
		return ttTxnPosts;
	}
	
	/**
	 * 将自动转分期注册信息生成分期信息,并将对应的贷调及借调交易入账
	 * @param item
	 * @param loanReg
	 * @throws Exception
	 */
	private List<CcsPostingTmp> addLoanRAndTransactionPost(S6000AcctInfo item, CcsLoanReg loanReg) throws Exception {
		List<CcsPostingTmp> addTxnPosts = new ArrayList<CcsPostingTmp>();
		
		// 创建消费计划贷调交易
		CcsPostingTmp origTxn = loanPrepare.generatePostingTmp(loanReg, SysTxnCd.S12, loanReg.getCardNbr(), loanReg.getLogicCardNbr(), null, null, loanReg.getLoanInitPrin(), null);
		txnPrepare.txnPrepare(origTxn, null);
		addTxnPosts.add(origTxn);
		
		// 创建TRANSFER OUT分期计划的借调交易
		CcsPostingTmp loanTxn = loanPrepare.generatePostingTmp(loanReg, SysTxnCd.S13, loanReg.getCardNbr(), loanReg.getLogicCardNbr(), null, null, loanReg.getLoanInitPrin(), null);
		txnPrepare.txnPrepare(loanTxn, null);
		addTxnPosts.add(loanTxn);
		
		// 交易入账
		addTxnPosts.addAll(transactionPost.posting(item, origTxn, batchStatusFacility.getBatchDate()));
		addTxnPosts.addAll(transactionPost.posting(item, loanTxn, batchStatusFacility.getBatchDate()));
		
		// 创建分期信息表记录
		em.persist(loanPrepare.generateLoan(loanReg));
		
		return addTxnPosts;
	}

	/**
	 * 将负数数值设为0
	 * @param amt
	 * @return
	 */
	private BigDecimal getNonNegativeAmt(BigDecimal amt) {
		return amt.compareTo(BigDecimal.ZERO) > 0 ? amt : BigDecimal.ZERO;
	}

	/**
	 * 当前消费计划总本金
	 * 
	 * @param plans
	 * @return
	 */
	private BigDecimal getRetailPrin(List<CcsPlan> plans) {
		// 当前消费计划总本金
		BigDecimal retailPrin = BigDecimal.ZERO;
		for (CcsPlan plan : plans) {
			if(plan.getPlanType() == PlanType.R){
				retailPrin = retailPrin.add(plan.getCurrBal());
			}
		}
		return retailPrin;
	}

	/**
	 * 已分期余额
	 * @param tmAccount
	 * @return
	 */
	private BigDecimal getAppedAmt(CcsAcct tmAccount) {
		BigDecimal appedAmt = BigDecimal.ZERO;
		//查询账户下当天申请没有入账的消费分期金额
		JPAQuery queryLoanReg = new JPAQuery(em);
		List<CcsLoanReg> loanRegList = queryLoanReg.from(qTmLoanReg).where(qTmLoanReg.acctNbr.eq(tmAccount.getAcctNbr()).and(qTmLoanReg.acctType.eq(tmAccount.getAcctType()))
				.and(qTmLoanReg.loanType.eq(LoanType.R).and(qTmLoanReg.org.eq(tmAccount.getOrg())))).list(qTmLoanReg);
		if (!loanRegList.isEmpty()) {
			for(CcsLoanReg isloanReg : loanRegList){
				appedAmt = appedAmt.add(isloanReg.getLoanInitPrin());
			}
		}
		return appedAmt;
	}

	/**
	 * 获取信用额度
	 * @param tmAccount
	 * @param loanPlan
	 * @return
	 */
	private BigDecimal getCreditLmt(CcsAcct tmAccount, LoanPlan loanPlan) {
		BigDecimal creditLimit = BigDecimal.ZERO;
		//当临额参与分期时并且临额在有效期内,信用额度 = 临额额度
		if(loanPlan.useTemplimit){
			// 临时额度是否有效
			Date businessDate = unifiedParameterFacilityProvide.BusinessDate();
			if (tmAccount.getTempLmtBegDate() != null && tmAccount.getTempLmtEndDate() != null && businessDate.compareTo(tmAccount.getTempLmtBegDate()) >= 0
					&& businessDate.compareTo(tmAccount.getTempLmtEndDate()) <= 0){
				creditLimit = tmAccount.getTempLmt();
			}else {
				creditLimit = tmAccount.getCreditLmt();
			}
		}else {
			creditLimit = tmAccount.getCreditLmt();
		}
		return creditLimit;
	}
	
	/**
	 * 额度内可分期比例
	 * @param tmAccount
	 * @param accountAttribute
	 * @return
	 */
	private BigDecimal getLoanLimitRate(CcsAcct tmAccount,
			AccountAttribute accountAttribute) {
		BigDecimal loanLimitRate = BigDecimal.ZERO;
		if(tmAccount.getLoanLmtRate() != null){
			loanLimitRate = tmAccount.getLoanLmtRate();
		}else {
			loanLimitRate = accountAttribute.loanLimitRate;
		}
		return loanLimitRate;
	}
	
	/**
	 * 检查入账流水是否可消费转分期
	 * @param txnPost
	 * @param loanPlan
	 * @return
	 */
	private boolean checkTxnPost(CcsPostingTmp txnPost, LoanPlan loanPlan, ProductCredit pc){
		// 挂账交易不做自动转分期
		if(txnPost.getPostingFlag()!=PostingFlag.F00){
			return false;
		}
		// 判断交易码是否可以做分期
		if (!loanPlan.txnCdList.contains(txnPost.getTxnCode())) {
			return false;
		}
		// 判断交易金额是否达到最低转分期消费金额
		if(txnPost.getTxnAmt().compareTo(pc.minAutoLoanRAmt)<0){
			return false;
		}
		return true;
	}
	
	/**
	 * 检查卡片是否可消费转分期
	 * @param txnPost
	 * @return
	 */
	private boolean checkCard(CcsPostingTmp txnPost){
		// 查询卡片信息
		CcsCard tmCard = custAcctCardQueryFacility.getCardByCardNbr(txnPost.getCardNbr());
		if(tmCard == null){
			throw new IllegalArgumentException("卡片信息不存在,卡号:" + txnPost.getCardNbr());
		}
		// 判断转分期交易对应的卡片是否过有效期
		if (batchStatusFacility.getBatchDate().compareTo(tmCard.getCardExpireDate()) > 0) {
			return false;
		}
		// 判断卡片上是否有锁定码存在，存在则无法分期
		if (!blockCodeUtils.getMergedLoanInd(tmCard.getBlockCode())) {
			return false;
		}
		return true;
	}
	
	/**
	 * 检查账户是否可消费转分期
	 * @param tmAccount
	 * @return
	 */
	private boolean checkAccount(CcsAcct tmAccount){
		//判断账户逾期状态
		//TODO 3（总期数内逾期多少次）、2（总期数内连续逾期多少次）、6（总期数）应为参数部分，这里先固定写
		if(isOverdue(tmAccount.getPaymentHst(),3, 2, 6)){
			return false;
		}
		//判断账户上是否有锁定码存在，存在则无法分期
		if (!blockCodeUtils.getMergedLoanInd(tmAccount.getBlockCode())) {
			return false;
		}
		return true;
	}
	
	/**
	 * 判断账户逾期状态 {@link com.sunline.ccs.service.busimpl.LoanServiceImpl.isOverdue}
	 * @param time1	toltime月内超过time1次逾期
	 * @param time2	toltime月内连续超过time2次逾期
	 * @param toltime
	 * @return false为未逾期，true为逾期
	 */
	public boolean isOverdue(String PaymentHist,int time1,int time2,int toltime){
		String payHis="";
		char u='U';//U|还款未达最小还款额
		char n='N';//N|未还款
		boolean overdue=false;
		if(PaymentHist==null) PaymentHist="";
		if(PaymentHist.length()>=toltime){
			payHis=PaymentHist.substring(0, toltime);
		}else{
			payHis=PaymentHist;
		}
		int times=counterTimes(payHis,u,n);
		if(times>=time1){
			overdue=true;
		}
		times=counterMaxTimes(payHis,u,n);
		if(times>=time2){
			overdue=true;
		}
		return overdue;
	}
	public int counterTimes(String s,char c,char b){
		  int count=0;
		  for(int i=0;i<s.length();i++){
		   if(s.charAt(i)==c||s.charAt(i)==b)
		    count++;
		  }
		  return count;
	}
	public int counterMaxTimes(String s,char c,char b){
		  int count=0;
		  int maxTimes=0;
		  for(int i=0;i<s.length();i++){
			  if(s.charAt(i)==c||s.charAt(i)==b){
				  count++;
			  }else{
				  count=0;
			  }
			  if(count>maxTimes){
				  maxTimes=count;									   
			  }
		  }
		  return maxTimes;
	}
}
