package com.sunline.ccs.batch.cc6000;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ccs.batch.cc6000.common.AgeController;
import com.sunline.ccs.batch.cc6000.common.Calculator;
import com.sunline.ccs.batch.common.DateUtils;
import com.sunline.ccs.batch.common.EnumUtils;
import com.sunline.ccs.batch.front.FrontBatchUtil;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.CcsPostingTmp;
import com.sunline.ccs.infrastructure.shared.model.CcsRepaySchedule;
import com.sunline.ccs.infrastructure.shared.model.CcsRepayScheduleHst;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnHst;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnReject;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnUnstatement;
import com.sunline.ccs.infrastructure.shared.model.QCcsOrder;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.param.def.PlanTemplate;
import com.sunline.pcm.param.def.Product;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.DbCrInd;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanStatus;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.LoanUsage;
import com.sunline.ppy.dictionary.enums.OrderStatus;
import com.sunline.ppy.dictionary.enums.PlanType;
import com.sunline.ppy.dictionary.enums.PostTxnType;
import com.sunline.ppy.dictionary.enums.PostingFlag;
import com.sunline.ppy.dictionary.report.ccs.LoanXfrRptItem;
import com.sunline.ppy.dictionary.report.ccs.OverContributionRptItem;
import com.sunline.ppy.dictionary.report.ccs.RejectTxnJournalRptItem;
import com.sunline.ppy.dictionary.report.ccs.TxnJournalRptItem;


/** 
 * @see 类名：P6068UpdateData
 * @see 描述：维护主表
 *
 * @see 创建日期：   2015年6月25日 下午2:39:32
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Component
public class P6068UpdateData implements ItemProcessor<S6000AcctInfo, S6000AcctInfo> {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private UnifiedParameterFacility parameterFacility;
	@Autowired
	private Calculator calculator;
	@Autowired
    private BatchStatusFacility batchFacility;
	@PersistenceContext
	private EntityManager em;
	@Autowired
	private AgeController ageController;
	@Autowired
	private FrontBatchUtil frontBatchUtil;

	@Override
	public S6000AcctInfo process(S6000AcctInfo item) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("维护主表：Org["+item.getAccount().getOrg()
					+"],AcctNo["+item.getAccount().getAcctNbr()
					+"],AcctType["+item.getAccount().getAcctType()
					+"],TxnPosts.size["+item.getTxnPosts().size()
					+"]");
		}
		updateLoan(item.getLoans(), item.getPlans(),item);
		// 更新账户的本金余额、取现余额、额度内分期余额、历史最高本金欠款、历史最高溢缴款、历史最高余额
		this.updateAccountBal(item.getAccount(), item.getPlans());
		//更新loan的逾期起始日期
		logger.info("更新CPD和DPD");
		if(item.getAccount().getPmtDueDate() != null && batchFacility.shouldProcess(item.getAccount().getPmtDueDate())){
			logger.info("开始处理更新CPD和DPD");
			ageController.updateOverdueDate(item.getLoans(), item.getPlans(),true,item.getAccount());
		}else{
			logger.info("未到到期还款日，不更新CPD和DPD");
		}
		logger.info("更新CPD和DPD最大值");
		for(CcsLoan loan :item.getLoans()){
			ageController.updateMaxOverdueDate(loan);
		}
		// 账户交易历史、交易报表处理
		for (CcsPostingTmp txnPost : item.getTxnPosts()) {
			if (logger.isDebugEnabled()) {
				logger.debug("维护主表：TxnSeq["+txnPost.getTxnSeq()
						+"],PostingFlag["+txnPost.getPostingFlag()
						+"]");
			}
			// 成功入账交易
			if (PostingFlag.F00 == txnPost.getPostingFlag()) {
				// 入账交易历史表
				this.addTxnHst(item, txnPost);
				// 当日交易流水报表
				this.addTxnJournal(item, txnPost);
				// 未出账单历史表
				this.addTxnUnstmt(item, txnPost);
			} 
			// 挂账交易
			else {
				// 挂账交易历史表
				this.addTxnReject(item, txnPost);
				// 当日挂账交易流水报表
				this.addRejectTxnJournal(item, txnPost);
			}

			if (txnPost.getPostTxnType() == PostTxnType.M ) {
				// 根据交易的信用计划号，查找信用计划模板
				PlanTemplate planTemplate = parameterFacility.loadParameter(txnPost.getPlanNbr(), PlanTemplate.class);
				// 计划类型为：转出计划或转入计划，且借贷方向为：MEMO类交易
				if ((PlanType.O == planTemplate.planType || PlanType.I == planTemplate.planType) 
					&& DbCrInd.M == txnPost.getDbCrInd())  {
					// 分期XFR报表
					this.addLoanXfr(item, txnPost);
				}
			}
		}
		this.addOverflowContribution(item);
		
		transferOutOverflow(item);
		
		return item;
	}
	
	/**
	 * 溢缴款转出
	 * 
	 * @param item
	 */
	private void transferOutOverflow(S6000AcctInfo item) {
		
		CcsAcct acct = item.getAccount();
		
		if (logger.isDebugEnabled()) {
			logger.debug("开始处理账户的溢缴款转出，AcctNbr:[" + acct.getAcctNbr() + "],AcctType:[" + acct.getAcctType() + "]");
		}
		
		List<CcsLoan> loans = item.getLoans();
		List<CcsPlan> plans = item.getPlans();
		
		for (CcsLoan loan : loans) {
			
			// 存在贷款未结清，不转出溢缴款
			if (loan.getPaidOutDate() == null) {
				if (logger.isDebugEnabled()) {
					logger.debug("贷款未结清，不转出溢缴款");
				}
				return;
			}
		}
		
		// 若有处理中订单不转出
		if (getWOrderCount(acct.getAcctNbr(), acct.getAcctType()) > 0) {
			if (logger.isDebugEnabled()) {
				logger.debug("存在处理中订单，不转出溢缴款");
			}
			return;
		}
		
		// 账户存在挂账交易不转出
		if (frontBatchUtil.getTxnRejectCount(acct.getAcctNbr(), acct.getAcctType()) > 0 ) {
			if (logger.isDebugEnabled()) {
				logger.debug("存在挂账交易，不转出溢缴款");
			}
			return;
		}
		
		// 循环plan计算溢缴款
		BigDecimal overflowBal = BigDecimal.ZERO; // 溢缴款
		for(CcsPlan plan : plans) {
			if (PlanType.D.equals(plan.getPlanType())) {
				overflowBal = overflowBal.add(plan.getCurrBal());
			}
		}
		
		// 存在溢缴款，则转出
		if (overflowBal.abs().compareTo(BigDecimal.ZERO) > 0) {
			
			CcsCustomer cust = em.find(CcsCustomer.class, acct.getCustId());
			// 生成订单
			frontBatchUtil.initOrder(acct, cust, null, LoanUsage.D, overflowBal.abs(), null);
		}
		
	}
	
	/**
	 * 查询账户是否有处理中订单
	 * @param acctNbr
	 * @param acctType
	 * @return
	 */
	private int getWOrderCount(Long acctNbr, AccountType acctType){
		QCcsOrder o = QCcsOrder.ccsOrder;
		return new JPAQuery(em).from(o)
				.where(o.acctNbr.eq(acctNbr).and(o.acctType.eq(acctType))
					.and(o.orderStatus.eq(OrderStatus.W)))
				.list(o.orderId).size();
	}
	
	/**
	 * 更新分期主表的统计信息
	 * 
	 * @param loans
	 * @param plans
	 */
	private void updateLoan(List<CcsLoan> loans, List<CcsPlan> plans,S6000AcctInfo item) {
		CcsAcct acct = item.getAccount();
		for(CcsLoan loan : loans){
			BigDecimal loanCurrBal = BigDecimal.ZERO;
			BigDecimal loanBalXfrout = BigDecimal.ZERO;
			BigDecimal loanBalXfrin = BigDecimal.ZERO;
			BigDecimal tolLoanCurrBal = BigDecimal.ZERO;
			for(CcsPlan plan : plans){
				if(loan.getRefNbr().equals(plan.getRefNbr())){

					loanCurrBal = loanCurrBal.add(plan.getCurrBal());
					//贷款余额=plan余额+罚息累计+复利累计+非延时利息累计+往期延时利息累计+当期延时利息累计
					tolLoanCurrBal = tolLoanCurrBal.add(plan.getCurrBal())
											.add(plan.getPenaltyAcru().setScale(2, RoundingMode.HALF_UP))
											.add(plan.getCompoundAcru().setScale(2, RoundingMode.HALF_UP))
											.add(plan.getNodefbnpIntAcru().setScale(2, RoundingMode.HALF_UP))
											.add(plan.getBegDefbnpIntAcru().setScale(2, RoundingMode.HALF_UP))
											.add(plan.getCtdDefbnpIntAcru().setScale(2, RoundingMode.HALF_UP))
											//+代收罚息累计
											.add(plan.getReplacePenaltyAcru().setScale(2,RoundingMode.HALF_UP));
					if(plan.getPlanType().isXfrOut()){
						loanBalXfrout = loanBalXfrout.add(plan.getCurrBal());
					}
					if(plan.getPlanType().isXfrIn()){
						loanBalXfrin = loanBalXfrin.add(plan.getCurrBal());
					}
				}
			}
			// 增加loan没有剩余期数判断结清条件
			if(loan.getRemainTerm() < 1 && tolLoanCurrBal.compareTo(BigDecimal.ZERO) == 0 && loan.getPaidOutDate() == null && loan.getUnstmtPrin().compareTo(BigDecimal.ZERO)==0){
				//随借随还到期后才会更新paidoutdate
				if(loan.getLoanType() == LoanType.MCAT){
					if(DateUtils.truncatedCompareTo(loan.getLoanExpireDate(),batchFacility.getBatchDate(),Calendar.DATE) <= 0){
						loan.setPaidOutDate(batchFacility.getBatchDate());
					}
				}else{
					loan.setPaidOutDate(batchFacility.getBatchDate());
				}
			}
			if( loan.getPaidOutDate() != null ){
				if(loan.getLoanStatus() == LoanStatus.A ){
					loan.setLoanStatus(LoanStatus.F);
				}
				if(loan.getRemainTerm() != 0){
					loan.setRemainTerm(0);
				}
			}
			loan.setLoanCurrBal(loanCurrBal);
			loan.setLoanBalXfrout(loanBalXfrout);
			loan.setLoanBalXfrin(loanBalXfrin);
			//在宽限日对贷款的还款信息做更新
			if((acct.getGraceDate() != null && batchFacility.shouldProcess(acct.getGraceDate())) || (loan.getPaidOutDate() != null && batchFacility.shouldProcess(loan.getPaidOutDate()))){
				if(EnumUtils.in(loan.getLoanType(), LoanType.MCAT,LoanType.MCEI,LoanType.MCEP)){
					updateSummaryAtGraceDay(loan,plans);
				}
			}
			if(loan.getLoanType().getUseSchedule() == Indicator.Y){
				if(loan.getPaidOutDate()!=null && loan.getLoanExpireDate()!=null && loan.getLoanExpireDate().compareTo(batchFacility.getBatchDate()) <= 0){
					for(CcsRepaySchedule t : item.getSchedules()){
						if(t.getLoanId().equals(loan.getLoanId())){
							CcsRepayScheduleHst hst = new CcsRepayScheduleHst();
							hst.updateFromMap(t.convertToMap());
							hst.setRegisterId(loan.getRegisterId());
							em.merge(hst);
							em.remove(t);
						}
					}
				}
			}
		}
		
	}
	
	public static void main(String[] args) throws ParseException{
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		if(DateUtils.truncatedCompareTo(df.parse("2014-12-12"),df.parse("2014-12-10"),Calendar.DATE)>=0){
			System.out.println(1);
		}else{
			System.out.println(2);
		}
		SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd:hhmmss");
		if(df2.parse("2014-12-11:121212").before(df.parse("2014-12-12"))){
			System.out.println(3);
		}else{
			System.out.println(4);
		}
	}
	/**
	 * 这里需要更新贷款loan的
	 * 1、24个月还款状态
	 * 2、最大逾期期数
	 * 3、当期还款额
	 * 
	 *	\/未开立账户
	 * * 当月不需要还款
	 * N 正常还款（已归还当月应还款额，且没有拖欠）
	 * 1 逾期1-30天
	 * 2 逾期31-60天
	 * 3 逾期61-90天
	 * 4 逾期91-120天
	 * 5-表示逾期121-150天；
		6-表示逾期151-180天；
		7-表示逾期180天以上；
		C-结清（借款人的该笔贷款全部还清，贷款余额为0。包括正常结清、提前结清、以资抵债结清、担保人代还结清等情况）；
		G-结束（除结清外，其他任何形态的终止账户，如核销）。
	 * @param loan
	 * @param plans
	 */
	private void updateSummaryAtGraceDay(CcsLoan loan, List<CcsPlan> plans) {
		int currCd = 0;//当前逾期期数
		Date earlest = null;//拖欠最早期数
		boolean paidOut = true;//是否全额还款
		boolean paid = false;//当月是否有还款
		BigDecimal payment = BigDecimal.ZERO;
		
		for(CcsPlan plan : plans){
			int term = plan.getTerm()==null?0:plan.getTerm();
			if(loan.getRefNbr().equals(plan.getRefNbr()) && loan.getCurrTerm()>=term){
				payment = payment.add(plan.getCtdAmtCr());
				if(plan.getPlanType().isXfrIn()){
					if(plan.getCurrBal().abs().compareTo(BigDecimal.ZERO)>0){
						if(earlest == null){
							earlest = plan.getPlanAddDate();
						}else{
							if(plan.getPlanAddDate().before(earlest)){
								earlest = plan.getPlanAddDate();
							}
						}
						currCd++;
						paidOut = false;
					}
					if(plan.getCtdAmtCr().compareTo(BigDecimal.ZERO)>0){
						paid = true;
					}
				}
			}
		}
		String paid24 = loan.getPaymentHst();
		int days;
		if(earlest != null){
			days = DateUtils.getIntervalDays(earlest, batchFacility.getBatchDate());
			//设置逾期起始日期
		}else{
			days = 0;
		}
		
		if(!StringUtils.hasText(paid24)){
			paid24 = "////////////////////////";
		}
		if(loan.getLoanStatus()  == LoanStatus.T){
			paid24+="G";
		}else if(loan.getLoanExpireDate().before(batchFacility.getBatchDate())){
			if(loan.getPaidOutDate() != null){
				paid24+="C";
			}else{
				paid24+=getCdStr(days);
			}
		}else{
			if(paidOut){
				if(paid){
					paid24 = paid24+"N";
				}else{
					paid24+="*";
				}
			}else{
				paid24+=getCdStr(days);
			}
		}
		if(!StringUtils.hasText(loan.getLoanAgeCode())){
			loan.setLoanAgeCode(String.valueOf(currCd));
		}else{
			if(currCd> Integer.parseInt(loan.getLoanAgeCode())){
				loan.setLoanAgeCode(String.valueOf(currCd));
			}
		}
		loan.setCtdRepayAmt(payment);
		loan.setPaymentHst(paid24.substring(paid24.length()-24,paid24.length()));
	}
	
	
	
	/**
	 * 根据拖欠天数判断拖欠级别
	 * @param days
	 * @return
	 */
	private String getCdStr(int days){
		if(days<30){
			return "1";
		}
		if(days>=31 && days<60){
			return "2";
		}
		if(days>=61 && days<90){
			return "3";
		}
		if(days>=91 && days<120){
			return "4";
		}
		if(days>=121 && days<150){
			return "5";
		}
		if(days>=151 && days<180){
			return "6";
		}
		return "7";
		
	}
	/**
	 * 更新账户的余额
	 * @param account
	 * @param plans
	 */
	private void updateAccountBal(CcsAcct account, List<CcsPlan> plans) {
		// 历史最高溢缴款	LTD_HIGHEST_CR_BAL
		BigDecimal ltdHighestCrBal = BigDecimal.ZERO;
		// 本金余额
		BigDecimal planPrincipalBal = BigDecimal.ZERO;
		// 取现余额
		BigDecimal planCashBal = BigDecimal.ZERO;
		// 额度内分期余额
		BigDecimal planLoanBal = BigDecimal.ZERO;
		
		for (CcsPlan plan : plans) {
			// 账户-本金余额
			planPrincipalBal = planPrincipalBal.add(plan.getCtdPrincipal().add(plan.getPastPrincipal()));
			// 账户-取现余额
			if (PlanType.C == plan.getPlanType() || PlanType.D == plan.getPlanType()) {
				planCashBal = planCashBal.add(plan.getCurrBal());
				// 汇总溢缴款
				if (PlanType.D == plan.getPlanType()) {
					ltdHighestCrBal = ltdHighestCrBal.add(plan.getCurrBal());
				}
			}
			// 账户-额度内分期余额
			if (PlanType.O.equals(plan.getPlanType()) || PlanType.I.equals(plan.getPlanType())) {
				planLoanBal = planLoanBal.add(plan.getCurrBal());
			}
		}
		// 更新账户-本金余额
		account.setPrincipalBal(planPrincipalBal);
		// 更新账户-取现余额
		account.setCashBal(planCashBal);
		// 更新账户-分期余额
		account.setLoanBal(planLoanBal);
		
		if (logger.isDebugEnabled()) {
			logger.debug("维护主表:planPrincipalBal["+planPrincipalBal
					+"],历史最高本金欠款["+account.getLtdHighestPrin()
					+"],ltdHighestCrBal["+ltdHighestCrBal.abs()
					+"],历史最高溢缴款["+account.getLtdHighestCrBal().abs()
					+"],CurrBal["+account.getCurrBal()
					+"],历史最高余额["+account.getLtdHighestBal()
					+"]");
		}
		// 历史最高本金欠款	LTD_HIGHEST_PRINCIPAL
		if (planPrincipalBal.compareTo(account.getLtdHighestPrin()) > 0) 
			account.setLtdHighestPrin(planPrincipalBal);
		// 历史最高溢缴款	LTD_HIGHEST_CR_BAL
		if (ltdHighestCrBal.abs().compareTo(account.getLtdHighestCrBal().abs()) > 0) 
			account.setLtdHighestCrBal(ltdHighestCrBal);
		// 历史最高余额	LTD_HIGHEST_BAL
		if (account.getCurrBal().compareTo(account.getLtdHighestBal()) > 0) 
			account.setLtdHighestBal(account.getCurrBal());
		
		if (logger.isDebugEnabled()) {
			logger.debug("维护主表:planPrincipalBal["+planPrincipalBal
					+"],历史最高本金欠款["+account.getLtdHighestPrin()
					+"],ltdHighestCrBal["+ltdHighestCrBal.abs()
					+"],历史最高溢缴款["+account.getLtdHighestCrBal().abs()
					+"],CurrBal["+account.getCurrBal()
					+"],历史最高余额["+account.getLtdHighestBal()
					+"]");
		}

		// 下一账单日=当前批量日期，或上一批量日期<下一账单日<当前批量日期
		if (batchFacility.shouldProcess(account.getNextStmtDate())) {
			// 当期最高超限金额清零
			account.setCtdMaxOvrlmtAmt(BigDecimal.ZERO);
			// 更新账户的连续超限账期（当前批量日期到超限日期所过的账期）
			if(account.getOvrlmtDate() != null){
				account.setOvrlmtNbrOfCyc(calculator.getOvrlmtNbrOfCyc(account.getProductCd(), account.getOvrlmtDate()));
			}
		}
		
		// 需要在维护主表时，判断是否超限，如果不超限将账户的超限日期更新为空
		// 计算超限金额
		BigDecimal overAmt = calculator.getOverLimitAmt(account, plans, batchFacility.getBatchDate());
		// 如果超限金额小于等于零，则将账户的超限日期更新为空
		if (overAmt.compareTo(BigDecimal.ZERO) <= 0) {
			account.setOvrlmtDate(null);
		}
	}
	
	/**
	 * 未出账单历史
	 * 
	 * @param item
	 * @param txnPost
	 */
	private void addTxnUnstmt(S6000AcctInfo acctInfo, CcsPostingTmp txnPost) {
		
		CcsTxnUnstatement txnUnstmt = new CcsTxnUnstatement();
		txnUnstmt.updateFromMap(txnPost.convertToMap());
		
		// 数据持久化
		em.persist(txnUnstmt);
		acctInfo.getTxnUnstmts().add(txnUnstmt);
	}

	/**
	 * 入账交易历史表
	 * @param acctInfo
	 * @param txnPost
	 */
	private void addTxnHst(S6000AcctInfo acctInfo, CcsPostingTmp txnPost) {
		
		CcsTxnHst txnHst = new CcsTxnHst();
		txnHst.updateFromMap(txnPost.convertToMap());
		
		// 数据持久化
		em.persist(txnHst);
		acctInfo.getTxnHsts().add(txnHst);
	}

	/**
	 * 当日交易流水报表【包括：当日内部生成交易表】
	 * @param acctInfo
	 * @param txnPost
	 */
	private void addTxnJournal(S6000AcctInfo acctInfo, CcsPostingTmp txnPost) {
		
		TxnJournalRptItem txnJournal = new TxnJournalRptItem();
		txnJournal.org = txnPost.getOrg();
		txnJournal.acctNo = txnPost.getAcctNbr();
		txnJournal.acctType = txnPost.getAcctType();
		txnJournal.postingFlag = txnPost.getPostingFlag();
		txnJournal.prePostingFlag = txnPost.getPrePostingFlag();
		txnJournal.cardNo = txnPost.getCardNbr();
		txnJournal.txnCode = txnPost.getTxnCode();
		txnJournal.txnDate = txnPost.getTxnDate();
		txnJournal.txnTime = txnPost.getTxnTime();
		txnJournal.glPostAmt = txnPost.getPostAmt();
		txnJournal.planNbr = txnPost.getPlanNbr();
		txnJournal.refNbr = txnPost.getRefNbr();
		txnJournal.acctBlockCd = acctInfo.getAccount().getBlockCode();
		txnJournal.cardBlockCd = txnPost.getCardBlockCode();
		txnJournal.productCd = txnPost.getProductCd();
		txnJournal.acqBranchId = txnPost.getAcqBranchIq();
		txnJournal.txnShortDesc = txnPost.getTxnShortDesc();
		
		acctInfo.getTxnJournals().add(txnJournal);
	}

	/**
	 * 挂账交易历史表
	 * @param acctInfo
	 * @param txnPost
	 */
	private void addTxnReject(S6000AcctInfo acctInfo, CcsPostingTmp txnPost) {
		
		CcsTxnReject txnReject = new CcsTxnReject();
		txnReject.updateFromMap(txnPost.convertToMap());
		
		// 数据持久化
		em.persist(txnReject);
		acctInfo.getTxnRejects().add(txnReject);
	}

	/**
	 * 当日挂账交易流水报表
	 * @param acctInfo
	 * @param txnPost
	 */
	private void addRejectTxnJournal(S6000AcctInfo acctInfo, CcsPostingTmp txnPost) {
		
		RejectTxnJournalRptItem rejectTxnJournal = new RejectTxnJournalRptItem();
		rejectTxnJournal.org = txnPost.getOrg();
		rejectTxnJournal.acctNo = txnPost.getAcctNbr();
		rejectTxnJournal.acctType = txnPost.getAcctType();
		rejectTxnJournal.postingFlag = txnPost.getPostingFlag();
		rejectTxnJournal.prePostingFlag = txnPost.getPrePostingFlag();
		rejectTxnJournal.cardNo = txnPost.getCardNbr();
		rejectTxnJournal.txnCode = txnPost.getTxnCode();
		rejectTxnJournal.txnDate = txnPost.getTxnDate();
		rejectTxnJournal.txnTime = txnPost.getTxnTime();
		rejectTxnJournal.glPostAmt = txnPost.getPostAmt();
		rejectTxnJournal.planNbr = txnPost.getPlanNbr();
		rejectTxnJournal.refNbr = txnPost.getRefNbr();
		rejectTxnJournal.acctBlockCd = acctInfo.getAccount().getBlockCode();
		rejectTxnJournal.cardBlockCd = txnPost.getCardBlockCode();
		rejectTxnJournal.productCd = txnPost.getProductCd();
		rejectTxnJournal.acqBranchId = txnPost.getAcqBranchIq();
		rejectTxnJournal.txnShortDesc = txnPost.getTxnShortDesc();
		
		acctInfo.getRejectTxnJournals().add(rejectTxnJournal);
	}

	/**
	 * 分期XFR报表
	 * @param acctInfo
	 * @param txnPost
	 */
	private void addLoanXfr(S6000AcctInfo acctInfo, CcsPostingTmp txnPost) {
		
		LoanXfrRptItem loanXfr = new LoanXfrRptItem();
		loanXfr.org = txnPost.getOrg();
		loanXfr.acctNo = txnPost.getAcctNbr();
		loanXfr.acctType = txnPost.getAcctType();
		loanXfr.cardNo = txnPost.getCardNbr();
		loanXfr.txnDate = txnPost.getTxnDate();
		loanXfr.txnCode = txnPost.getTxnCode();
		loanXfr.glPostAmt = txnPost.getPostAmt();
		loanXfr.planNbr = txnPost.getPlanNbr();
		loanXfr.refNbr = txnPost.getRefNbr();
		
		acctInfo.getLoanXfrs().add(loanXfr);
	}
	
	/**
	 * 溢缴款报表
	 * @param acctInfo
	 * @param txnPost
	 */
	private void addOverflowContribution(S6000AcctInfo acctInfo) {
		CcsPlan newPlan = null;
		for(CcsPlan plan:acctInfo.getPlans()){
			if(plan.getPlanType() == PlanType.D){
				newPlan=plan;
				break;
			}
		}
		CcsPlan prePlan = null;
		for(CcsPlan plan:acctInfo.getPrePlans()){
			if(plan.getPlanType() == PlanType.D){
				prePlan=plan;
				break;
			}
		}
		boolean  isChange = false;
		if(newPlan != null && prePlan !=null && newPlan.getCurrBal().compareTo(prePlan.getCurrBal()) != 0){
			isChange = true ;
		}
		if(newPlan == null && prePlan !=null && prePlan.getCurrBal().compareTo(BigDecimal.ZERO) != 0){
			isChange = true ;
		}
		if(newPlan != null && newPlan.getCurrBal().compareTo(BigDecimal.ZERO) != 0){
			isChange = true ;
		}
		if(isChange){
			OverContributionRptItem loanAmt = new OverContributionRptItem();

			loanAmt.org = acctInfo.getAccount().getOrg();
			loanAmt.acctNo = acctInfo.getAccount().getAcctNbr();
			loanAmt.acctType = acctInfo.getAccount().getAcctType();
			loanAmt.productCd = acctInfo.getAccount().getProductCd();
			Product product = parameterFacility.loadParameter(acctInfo.getAccount().getProductCd(), Product.class);
			loanAmt.productDescription = product.description;
			if(acctInfo.getAccount().getAcctType() == AccountType.E && acctInfo.getLoans().size() > 0){
				loanAmt.loanCode = acctInfo.getLoans().get(0).getLoanCode();
				LoanPlan loanPlan = parameterFacility.loadParameter(loanAmt.loanCode, LoanPlan.class);
				loanAmt.loanDescription = loanPlan.description;
			}else{
				loanAmt.loanCode = "";
				loanAmt.loanDescription = "";
			}
			
			loanAmt.contrNbr = acctInfo.getAccount().getContrNbr();
			loanAmt.name = acctInfo.getAccount().getName();
			loanAmt.custId = acctInfo.getAccount().getCustId();
			
			loanAmt.updateDate = batchFacility.getBatchDate();
			
			BigDecimal oldAmt = BigDecimal.ZERO;
			if(prePlan !=null){
				oldAmt = prePlan.getCurrBal().abs();
			}
			BigDecimal newAmt = BigDecimal.ZERO;
			if(newPlan !=null){
				newAmt = newPlan.getCurrBal().abs();
			}
			
			if(newAmt.compareTo(oldAmt) >= 0){
				loanAmt.OverflowContributionAdd = newAmt.subtract(oldAmt);
				loanAmt.OverflowContributionLess = BigDecimal.ZERO;
			}else{
				loanAmt.OverflowContributionAdd = BigDecimal.ZERO;
				loanAmt.OverflowContributionLess = oldAmt.subtract(newAmt);
			}
			loanAmt.OverflowContributionBal = newAmt;
			
			acctInfo.getOverflowContributions().add(loanAmt);
		}
		
	}
}
