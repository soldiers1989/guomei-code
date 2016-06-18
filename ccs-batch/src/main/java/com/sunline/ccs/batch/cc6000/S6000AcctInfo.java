package com.sunline.ccs.batch.cc6000;

import java.util.ArrayList;
import java.util.List;

import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAuthmemoDelTmp;
import com.sunline.ccs.infrastructure.shared.model.CcsAuthmemoO;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.CcsPostingTmp;
import com.sunline.ccs.infrastructure.shared.model.CcsRepaySchedule;
import com.sunline.ccs.infrastructure.shared.model.CcsSettlePlatformRec;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnHst;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnReject;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnUnstatement;
import com.sunline.ppy.dictionary.enums.UnmatchStatus;
import com.sunline.ppy.dictionary.exchange.CollectionItem;
import com.sunline.ppy.dictionary.exchange.DdRequestInterfaceItem;
import com.sunline.ppy.dictionary.exchange.GlTxnItem;
import com.sunline.ppy.dictionary.exchange.PlanBnpSumItem;
import com.sunline.ppy.dictionary.exchange.StmtInterfaceItem;
import com.sunline.ppy.dictionary.report.ccs.ExceptionAccountRptItem;
import com.sunline.ppy.dictionary.report.ccs.ExpiredAuthJournalRptItem;
import com.sunline.ppy.dictionary.report.ccs.InterestAccrualItem;
import com.sunline.ppy.dictionary.report.ccs.LoanSuccessRptItem;
import com.sunline.ppy.dictionary.report.ccs.LoanXfrRptItem;
import com.sunline.ppy.dictionary.report.ccs.MatchAuthJournalRptItem;
import com.sunline.ppy.dictionary.report.ccs.OverLimitAccountRptItem;
import com.sunline.ppy.dictionary.report.ccs.OverContributionRptItem;
import com.sunline.ppy.dictionary.report.ccs.RejectTxnJournalRptItem;
import com.sunline.ppy.dictionary.report.ccs.TxnJournalRptItem;
import com.sunline.ppy.dictionary.report.ccs.TxnPointsRptItem;
import com.sunline.ppy.dictionary.report.ccs.UnmatchAuthJournalRptItem;


/** 
 * @see 类名：S6000AcctInfo
 * @see 描述：核心入账程序数据收集
 *
 * @see 创建日期：   2015年6月25日 下午2:40:53
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class S6000AcctInfo{

	/**
	 * 账户信息
	 */
	private CcsAcct account;

	/**
	 * 账户对应的信用计划List
	 */
	private List<CcsPlan> plans = new ArrayList<CcsPlan>();

	/**
	 * 账户对应的分期信息List
	 */
	private List<CcsLoan> loans = new ArrayList<CcsLoan>();
	
	/**
	 * 账户对应的贷款计划信息
	 */
	private List<CcsRepaySchedule> schedules = new ArrayList<CcsRepaySchedule>();

	/**
	 * 账户对应的当日入账交易List
	 */
	private List<CcsPostingTmp> txnPosts = new ArrayList<CcsPostingTmp>();

	/**
	 * 账户对应的授权未匹配List
	 */
	private List<CcsAuthmemoO> unmatchs = new ArrayList<CcsAuthmemoO>();

	/**
	 * 授权匹配状态：M-成功匹配；U-未匹配；E-过期授权
	 */
	private List<UnmatchStatus>  unmatchStatuses = new ArrayList<UnmatchStatus>();

	/**
	 * 入账交易历史表(TM_TXN_HST)
	 */
	private List<CcsTxnHst> txnHsts = new ArrayList<CcsTxnHst>();

	/**
	 * 挂账交易历史表(TM_TXN_REJECT)
	 */
	private List<CcsTxnReject> txnRejects = new ArrayList<CcsTxnReject>();

	/**
	 * 未达授权删除列表
	 */
	private List<CcsAuthmemoDelTmp> unmatchDeletes = new ArrayList<CcsAuthmemoDelTmp>();

	/**
	 * 未出账单交易历史表(TM_TXN_UNSTMT)
	 */
	private List<CcsTxnUnstatement> txnUnstmts = new ArrayList<CcsTxnUnstatement>();

	/**
	 * 批量起始时账户状态
	 * 用于处理总账结转交易生成
	 */
	private CcsAcct preAccount;

	/**
	 * 批量起始时信用计划状态
	 * 用于处理总账结转交易生成
	 */
	private List<CcsPlan> prePlans = new ArrayList<CcsPlan>();

	/**
	 * 约定还款请求文件
	 */
	private List<DdRequestInterfaceItem> ddRequestItemList = new ArrayList<DdRequestInterfaceItem>();

	/**
	 * 催收接口文件
	 */
	private List<CollectionItem> collectionItems = new ArrayList<CollectionItem>();

	/**
	 * 账单统计信息列表
	 * 用于生成账单统计信息文件
	 */
	private List<StmtInterfaceItem> stmt = new ArrayList<StmtInterfaceItem>();

	/**
	 * 输入总账交易流水
	 */
	private List<GlTxnItem> glTxnItemList = new ArrayList<GlTxnItem>();

	/**
	 * 异常账户日志文件
	 */
	private List<ExceptionAccountRptItem> exceptionAccounts = new ArrayList<ExceptionAccountRptItem>();

	/**
	 * 当日交易流水报表
	 */
	private List<TxnJournalRptItem> txnJournals = new ArrayList<TxnJournalRptItem>();
	
	/**
	 * 当日挂账交易流水表
	 */
	private List<RejectTxnJournalRptItem> rejectTxnJournals = new ArrayList<RejectTxnJournalRptItem>();
	
	/**
	 * 授权成功匹配报表
	 */
	private List<MatchAuthJournalRptItem> matchAuthJournals = new ArrayList<MatchAuthJournalRptItem>();
	
	/**
	 * 授权未匹配报表
	 */
	private List<UnmatchAuthJournalRptItem> unmatchAuthJournals = new ArrayList<UnmatchAuthJournalRptItem>();
	
	/**
	 * 过期授权报表
	 */
	private List<ExpiredAuthJournalRptItem> expiredAuthJournals = new ArrayList<ExpiredAuthJournalRptItem>();
	
	/**
	 * 超限账户报表
	 */
	private List<OverLimitAccountRptItem> overLimitAccounts = new ArrayList<OverLimitAccountRptItem>();
	
	/**
	 * 内部生成积分交易报表
	 */
	private List<TxnPointsRptItem> txnPointss = new ArrayList<TxnPointsRptItem>();
	
	/**
	 * 分期XFR报表
	 */
	private List<LoanXfrRptItem> loanXfrs = new ArrayList<LoanXfrRptItem>();
	
	/**
	 * 分户账汇总信息文件
	 */
	private List<PlanBnpSumItem> planBnpSums = new ArrayList<PlanBnpSumItem>();

	/**
	 * 利息累积日报表
	 */
	private List<InterestAccrualItem> intAccrualItems = new ArrayList<InterestAccrualItem>();
	
	/**
	 * 消费转分期报表
	 */
	private List<LoanSuccessRptItem> loanSuccessRptItems = new ArrayList<LoanSuccessRptItem>();
	
	/**
	 * 保存还款分配前，信用计划状态
	 * 用于处理总账结转交易生成
	 */
	private List<CcsPlan> paymentPrePlans = new ArrayList<CcsPlan>();
	
	/**
	 * 入账过程中生成的临时交易
	 */
	private List<CcsPostingTmp> generateTxns = new ArrayList<CcsPostingTmp>();
	
	/**
	 * 账户溢缴款报表
	 */
	private List<OverContributionRptItem> overflowContributions = new ArrayList<OverContributionRptItem>();
	
	/**
	 * 送结算平台记录
	 */
	private List<CcsSettlePlatformRec> settlePlatformRecs = new ArrayList<CcsSettlePlatformRec>();

	public List<CcsSettlePlatformRec> getSettlePlatformRecs() {
		return settlePlatformRecs;
	}

	public void setSettlePlatformRecs(List<CcsSettlePlatformRec> settlePlatformRecs) {
		this.settlePlatformRecs = settlePlatformRecs;
	}

	private int txnUnstmtCount;
	
	public List<OverContributionRptItem> getOverflowContributions() {
		return overflowContributions;
	}

	public void setOverflowContributions(
			List<OverContributionRptItem> overflowContributions) {
		this.overflowContributions = overflowContributions;
	}
	
	public CcsAcct getAccount() {
		return account;
	}

	public void setAccount(CcsAcct account) {
		this.account = account;
	}

	public List<CcsPlan> getPlans() {
		return plans;
	}

	public void setPlans(List<CcsPlan> plans) {
		this.plans = plans;
	}

	public List<CcsLoan> getLoans() {
		return loans;
	}

	public void setLoans(List<CcsLoan> loans) {
		this.loans = loans;
	}

	public List<CcsPostingTmp> getTxnPosts() {
		return txnPosts;
	}

	public void setTxnPosts(List<CcsPostingTmp> txnPosts) {
		this.txnPosts = txnPosts;
	}

	public List<CcsAuthmemoO> getUnmatchs() {
		return unmatchs;
	}

	public void setUnmatchs(List<CcsAuthmemoO> unmatchs) {
		this.unmatchs = unmatchs;
	}

	public List<UnmatchStatus> getUnmatchStatuses() {
		return unmatchStatuses;
	}

	public void setUnmatchStatuses(List<UnmatchStatus> unmatchStatuses) {
		this.unmatchStatuses = unmatchStatuses;
	}

	public List<CcsTxnHst> getTxnHsts() {
		return txnHsts;
	}

	public void setTxnHsts(List<CcsTxnHst> txnHsts) {
		this.txnHsts = txnHsts;
	}

	public List<CcsTxnReject> getTxnRejects() {
		return txnRejects;
	}

	public void setTxnRejects(List<CcsTxnReject> txnRejects) {
		this.txnRejects = txnRejects;
	}

	public List<CcsAuthmemoDelTmp> getUnmatchDeletes() {
		return unmatchDeletes;
	}

	public void setUnmatchDeletes(List<CcsAuthmemoDelTmp> unmatchDeletes) {
		this.unmatchDeletes = unmatchDeletes;
	}

	public List<CcsTxnUnstatement> getTxnUnstmts() {
		return txnUnstmts;
	}

	public void setTxnUnstmts(List<CcsTxnUnstatement> txnUnstmts) {
		this.txnUnstmts = txnUnstmts;
	}

	public CcsAcct getPreAccount() {
		return preAccount;
	}

	public void setPreAccount(CcsAcct preAccount) {
		this.preAccount = preAccount;
	}

	public List<CcsPlan> getPrePlans() {
		return prePlans;
	}

	public void setPrePlans(List<CcsPlan> prePlans) {
		this.prePlans = prePlans;
	}

	public List<DdRequestInterfaceItem> getDdRequestItemList() {
		return ddRequestItemList;
	}

	public void setDdRequestItemList(List<DdRequestInterfaceItem> ddRequestItemList) {
		this.ddRequestItemList = ddRequestItemList;
	}

	public List<CollectionItem> getCollectionItems() {
		return collectionItems;
	}

	public void setCollectionItems(List<CollectionItem> collectionItems) {
		this.collectionItems = collectionItems;
	}

	public List<StmtInterfaceItem> getStmt() {
		return stmt;
	}

	public void setStmt(List<StmtInterfaceItem> stmt) {
		this.stmt = stmt;
	}

	public List<GlTxnItem> getGlTxnItemList() {
		return glTxnItemList;
	}

	public void setGlTxnItemList(List<GlTxnItem> glTxnItemList) {
		this.glTxnItemList = glTxnItemList;
	}

	public List<ExceptionAccountRptItem> getExceptionAccounts() {
		return exceptionAccounts;
	}

	public void setExceptionAccounts(List<ExceptionAccountRptItem> exceptionAccounts) {
		this.exceptionAccounts = exceptionAccounts;
	}

	public List<TxnJournalRptItem> getTxnJournals() {
		return txnJournals;
	}

	public void setTxnJournals(List<TxnJournalRptItem> txnJournals) {
		this.txnJournals = txnJournals;
	}

	public List<RejectTxnJournalRptItem> getRejectTxnJournals() {
		return rejectTxnJournals;
	}

	public void setRejectTxnJournals(List<RejectTxnJournalRptItem> rejectTxnJournals) {
		this.rejectTxnJournals = rejectTxnJournals;
	}

	public List<MatchAuthJournalRptItem> getMatchAuthJournals() {
		return matchAuthJournals;
	}

	public void setMatchAuthJournals(List<MatchAuthJournalRptItem> matchAuthJournals) {
		this.matchAuthJournals = matchAuthJournals;
	}

	public List<UnmatchAuthJournalRptItem> getUnmatchAuthJournals() {
		return unmatchAuthJournals;
	}

	public void setUnmatchAuthJournals(
			List<UnmatchAuthJournalRptItem> unmatchAuthJournals) {
		this.unmatchAuthJournals = unmatchAuthJournals;
	}

	public List<ExpiredAuthJournalRptItem> getExpiredAuthJournals() {
		return expiredAuthJournals;
	}

	public void setExpiredAuthJournals(
			List<ExpiredAuthJournalRptItem> expiredAuthJournals) {
		this.expiredAuthJournals = expiredAuthJournals;
	}

	public List<OverLimitAccountRptItem> getOverLimitAccounts() {
		return overLimitAccounts;
	}

	public void setOverLimitAccounts(List<OverLimitAccountRptItem> overLimitAccounts) {
		this.overLimitAccounts = overLimitAccounts;
	}

	public List<TxnPointsRptItem> getTxnPointss() {
		return txnPointss;
	}

	public void setTxnPointss(List<TxnPointsRptItem> txnPointss) {
		this.txnPointss = txnPointss;
	}

	public List<LoanXfrRptItem> getLoanXfrs() {
		return loanXfrs;
	}

	public void setLoanXfrs(List<LoanXfrRptItem> loanXfrs) {
		this.loanXfrs = loanXfrs;
	}

	public List<PlanBnpSumItem> getPlanBnpSums() {
		return planBnpSums;
	}

	public void setPlanBnpSums(List<PlanBnpSumItem> planBnpSums) {
		this.planBnpSums = planBnpSums;
	}

	public List<InterestAccrualItem> getIntAccrualItems() {
		return intAccrualItems;
	}

	public void setIntAccrualItems(List<InterestAccrualItem> intAccrualItems) {
		this.intAccrualItems = intAccrualItems;
	}
	
	public List<LoanSuccessRptItem> getLoanSuccessRptItems() {
		return loanSuccessRptItems;
	}

	public void setLoanSuccessRptItems(List<LoanSuccessRptItem> loanSuccessRptItems) {
		this.loanSuccessRptItems = loanSuccessRptItems;
	}

	/**
	 * 保存还款分配前，信用计划状态
	 * 用于处理总账结转交易生成
	 */
	public List<CcsPlan> getPaymentPrePlans() {
		return paymentPrePlans;
	}

	/**
	 * 保存还款分配前，信用计划状态
	 * 用于处理总账结转交易生成
	 */
	public void setPaymentPrePlans(List<CcsPlan> paymentPrePlans) {
		this.paymentPrePlans = paymentPrePlans;
	}

	public List<CcsPostingTmp> getGenerateTxns() {
		return generateTxns;
	}

	public void setGenerateTxns(List<CcsPostingTmp> generateTxns) {
		this.generateTxns = generateTxns;
	}

	public List<CcsRepaySchedule> getSchedules() {
		return schedules;
	}

	public void setSchedules(List<CcsRepaySchedule> schedules) {
		this.schedules = schedules;
	}

	public int getTxnUnstmtCount() {
		return txnUnstmtCount;
	}

	public void setTxnUnstmtCount(int txnUnstmtCount) {
		this.txnUnstmtCount = txnUnstmtCount;
	}
	
}
