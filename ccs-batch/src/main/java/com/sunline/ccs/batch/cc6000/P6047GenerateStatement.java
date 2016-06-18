package com.sunline.ccs.batch.cc6000;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ccs.batch.cc6000.common.AgeController;
import com.sunline.ccs.batch.cc6000.common.BnpManager;
import com.sunline.ccs.batch.cc6000.common.Calculator;
import com.sunline.ccs.batch.common.BatchUtils;
import com.sunline.ccs.batch.common.EnumUtils;
import com.sunline.ccs.facility.BlockCodeUtils;
import com.sunline.ccs.facility.DateUtils;
import com.sunline.ccs.facility.MicroCreditRescheduleUtils;
import com.sunline.ccs.infrastructure.server.repos.RCcsStatement;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.CcsPostingTmp;
import com.sunline.ccs.infrastructure.shared.model.CcsRepaySchedule;
import com.sunline.ccs.infrastructure.shared.model.CcsStatement;
import com.sunline.ccs.infrastructure.shared.model.CcsStatementKey;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnUnstatement;
import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ccs.param.def.PlanTemplate;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.TxnCd;
import com.sunline.ccs.param.def.enums.BnpPeriod;
import com.sunline.ccs.param.def.enums.TxnType;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.BucketType;
import com.sunline.ppy.dictionary.enums.DbCrInd;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.PlanType;
import com.sunline.ppy.dictionary.enums.PostingFlag;


/** 
 * @see 类名：P6047GenerateStatement
 * @see 描述：账单汇总信息处理
 *
 * @see 创建日期：   2015年6月25日 下午2:23:24
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class P6047GenerateStatement implements ItemProcessor<S6000AcctInfo, S6000AcctInfo> {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private UnifiedParameterFacility parameterFacility;
	@Autowired
	private Calculator calculator;
	@Autowired
	private BnpManager bnpManager;
	@Autowired
	private BlockCodeUtils blockCodeUtils;
	@Autowired
	private AgeController ageController;
	@Autowired
	private BatchUtils batchUtils;
	@Autowired
	private MicroCreditRescheduleUtils rescheduleUtils;
	@Autowired
	private RCcsStatement rTmStmtHst;
	@PersistenceContext
	private EntityManager em;
	
	@Value("#{env.correctAcctSetupDate}")
	private String correctAcctSetupDate;

	/**
	 * 批量日期
	 */
	@Autowired
	private BatchStatusFacility batchStatusFacility;

	@Override
	public S6000AcctInfo process(S6000AcctInfo item) {
		logger.debug("账单汇总信息处理：Org["+item.getAccount().getOrg()
				+"],AcctNo["+item.getAccount().getAcctNbr()
				+"],AcctType["+item.getAccount().getAcctType()
				+"],BatchDate["+batchStatusFacility.getBatchDate()
				+"],NextStmtDate["+item.getAccount().getNextStmtDate()
				+"]");
		Date nextStmtDate = item.getAccount().getNextStmtDate();

		// 判断是否账单日
		if (batchStatusFacility.shouldProcess(nextStmtDate)) {
			// 批量日期是下一账单日
			logger.info("开始账单处理");
			// 新建账单历史统计信息
			CcsStatement stmtHst = createStmtHst(item.getAccount(), item.getPlans(), item.getTxnPosts(), item.getTxnUnstmts());
			em.persist(stmtHst);

			// 更新账户相关信息
			updateAccountAtStmtDay(item);
			
			// 更新信用计划相关信息
			updatePlanAtStmtDay(item);
			//	更新账龄历史记录
			logger.info("更新账龄历史记录");
			ageController.setAgeCdHst(item.getAccount());

			return item;
		} else {
			// 批量日期不是下一账单日
			logger.info("不是账单日，跳过账单处理过程");

			return item;
		}
	}

	/**
	 * 账单日更新帐户状态
	 * 
	 * @param accountInfo
	 */
	private void updateAccountAtStmtDay(S6000AcctInfo accountInfo) {
		// 修改注意事项：
		// 下一账单日字段必须最后更新，否则还款日等相关字段计算会错误

		CcsAcct account = accountInfo.getAccount();
		

		// 设置期初余额 = 当前余额
		account.setBegBal(account.getCurrBal());

														
		// 设置当期调整积分为0
		account.setCtdAdjPoints(BigDecimal.ZERO);

		// 设置当期取现金额为0
		account.setCtdCashAmt(BigDecimal.ZERO);

		// 设置当期取现笔数为0
		account.setCtdCashCnt(0);

		// 设置当期贷记调整金额为0
		account.setCtdCrAdjAmt(BigDecimal.ZERO);

		// 设置当期贷记调整笔数为0
		account.setCtdCrAdjCnt(0);
		
		// 设置当期借记调整金额为0
		account.setCtdDbAdjAmt(BigDecimal.ZERO);

		// 设置当期借记调整笔数为0
		account.setCtdDbAdjCnt(0);

		// 设置当期兑换积分为0
		account.setCtdSpendPoints(BigDecimal.ZERO);

		// 设置当期新增积分为0
		account.setCtdPoints(BigDecimal.ZERO);

		// 设置当期费用金额为0
		account.setCtdFeeAmt(BigDecimal.ZERO);

		// 设置当期费用笔数为0
		account.setCtdFeeCnt(0);

		// 设置当期最高超限金额为0
		account.setCtdMaxOvrlmtAmt(BigDecimal.ZERO);

		// 设置当期入账利息金额为0
		account.setCtdInterestAmt(BigDecimal.ZERO);
		account.setCtdInterestCnt(0);
		
		// 设置当期还款金额为0
		account.setCtdRepayAmt(BigDecimal.ZERO);

		// 设置当期还款笔数为0
		account.setCtdRepayCnt(0);

		// 设置当期退货金额为0
		account.setCtdRefundAmt(BigDecimal.ZERO);

		// 设置当期退货笔数为0
		account.setCtdRefundCnt(0);

		// 设置当期消费金额为0
		account.setCtdRetailAmt(BigDecimal.ZERO);

		// 设置当期消费笔数为0
		account.setCtdRetailCnt(0);

		// 设置约定还款日期，必须在更新下一账单日之前更新
		account.setDdDate(batchUtils.getNextDdDay(account.getProductCd(), account.getNextStmtDate()));

		// FIXME 设定下次溢缴款购汇还款日期
		account.setDualBillingDate(null);

		// 设定免息日期
		// 免息日期的设定必须在账单日设定之前
		account.setGraceDate(rescheduleUtils.getNextGraceDay(account.getProductCd(), account.getNextStmtDate()));

		// 设置全额还款标志
		account.setGraceDaysFullInd(Indicator.N);

		// 设置上个当期还款日为当前还款日
		account.setLastPmtDueDate(account.getPmtDueDate());

		// 设置上个账单日
		account.setLastStmtDate(batchStatusFacility.getBatchDate());

		// 设置下个还款日
		account.setPmtDueDate(rescheduleUtils.getNextPaymentDay(account.getProductCd(), account.getNextStmtDate()));
		
		// 特殊处理12月15日前开户且账单日在29、30、31日三天的账户，12月15日前为不跳过月末，12月15日上线版本后为跳过月末，导致上线前后计算的下个还款日不同，现修改为与原生成还款计划还款日保持一致
		logger.debug("特殊处理账户开户日期：" + correctAcctSetupDate + "，开始处理：");
		if(DateUtils.formatDate2String(account.getSetupDate(), "yyyyMMdd").compareTo(correctAcctSetupDate) <= 0){
			Calendar c = Calendar.getInstance();
			c.setTime(account.getNextStmtDate());
			int stmtDay = c.get(Calendar.DAY_OF_MONTH);
			if(accountInfo.getLoans().size()>0 && LoanType.MCEI == accountInfo.getLoans().get(0).getLoanType() && (stmtDay == 29 || stmtDay == 30 || stmtDay == 31)){
				logger.debug("满足条件账户：" + account.getAcctNbr() + "，当前账单日：" + account.getNextStmtDate() + "，当前还款日：" + account.getPmtDueDate());
				if(accountInfo.getLoans().size()>0 && accountInfo.getSchedules().size()>0){
					CcsLoan loan = accountInfo.getLoans().get(0);
					CcsRepaySchedule currSchedule = null;
					for(CcsRepaySchedule t : accountInfo.getSchedules()){
						if(t.getLoanId().intValue() == loan.getLoanId().intValue()){
							if(t.getCurrTerm().intValue() == (loan.getCurrTerm())){
								currSchedule = t;
								break;
							}
						}
					}
					if(currSchedule!=null && currSchedule.getLoanPmtDueDate().compareTo(account.getNextStmtDate()) >= 0){
						account.setPmtDueDate(currSchedule.getLoanPmtDueDate());
					}else{
						account.setPmtDueDate(account.getNextStmtDate());
					}
					
				}else{
					account.setPmtDueDate(account.getNextStmtDate());
				}
				logger.debug("修正后还款日为：" + account.getPmtDueDate());
			}
		}

		// 设置还款日余额为0
		account.setPmtDueDayBal(BigDecimal.ZERO);

		// 设置积分期初余额
		account.setPointsBegBal(account.getPointsBal());

		// 设置全部应还款额
		account.setQualGraceBal(calculator.calcQualGraceBal(accountInfo.getPlans()));

		//LoanFeeDef loanFeeDef = null;
		Date currStmtDate = account.getNextStmtDate();
		// 设置下个账单日，必须最后更新
		//如果下一账单日已经设置的大于当前批量日，就不再设置下一账单日（转入的时候已经设置下一还款日）
		if(accountInfo.getLoans().size()>0 && accountInfo.getSchedules().size()>0){
			CcsLoan loan = accountInfo.getLoans().get(0);
			//loanFeeDef = rescheduleUtils.getLoanFeeDef(loan.getLoanCode(), 0, BigDecimal.ZERO, loan.getLoanFeeDefId());
			CcsRepaySchedule nextSchedule = null;
			for(CcsRepaySchedule t : accountInfo.getSchedules()){
				if(t.getLoanId().intValue() == loan.getLoanId().intValue()){
					if(t.getCurrTerm().intValue() == (loan.getCurrTerm()+1)){
						nextSchedule = t;
						break; 
					}
				}
			}
			if(nextSchedule!=null){
				account.setNextStmtDate(nextSchedule.getLoanPmtDueDate());
			}else{
				// 贷款账户计算下个账单日时，统一按照loanPlan上的还款间隔单位和还款间隔周期计算
				if(account.getAcctType() == AccountType.E){
/*					if(loanFeeDef != null){
						account.setNextStmtDate(rescheduleUtils.getLoanPmtDueDate(currStmtDate, loanFeeDef, 2, account.getCycleDay()));						
					}else{
						account.setNextStmtDate(ageController.getCycleDate(currStmtDate, account.getCycleDay(), "-1"));
					}*/
					// 先固定按月来处理
					account.setNextStmtDate(ageController.getCycleDate(currStmtDate, account.getCycleDay(), "-1"));
				}else{
					//非贷款账户仍然保持原有系统逻辑
					account.setNextStmtDate(batchUtils.getNextStmtDay(account.getProductCd(), account.getCycleDay(), batchStatusFacility.getSystemStatus().getProcessDate()));					
				}
			}
			
		}else{
			// 贷款账户计算下个账单日时，统一按照loanPlan上的还款间隔单位和还款间隔周期计算
			if(account.getAcctType() == AccountType.E){
				// 先固定按月来处理了
				account.setNextStmtDate(ageController.getCycleDate(currStmtDate, account.getCycleDay(), "-1"));
			}else{
				//非贷款账户仍然保持原有系统逻辑
				account.setNextStmtDate(batchUtils.getNextStmtDay(account.getProductCd(), account.getCycleDay(), batchStatusFacility.getSystemStatus().getProcessDate()));
			}
		}
			
	}
	
	/**
	 * 创建账单统计信息 必须在账户状态更新之前进行
	 * 
	 * @param accountInfo
	 * @param batchDate
	 * @return
	 */
	private CcsStatement createStmtHst(CcsAcct account, List<CcsPlan> plans, List<CcsPostingTmp> posts, List<CcsTxnUnstatement> txnUnstmts) {
		CcsStatement stmtHst = new CcsStatement();
		
		//	TODO 有时间将convertToMap修改为指定赋值
		stmtHst.updateFromMap(account.convertToMap());
		// 设置账单日期
		stmtHst.setStmtDate(batchStatusFacility.getBatchDate());
		// 设置还款日
		stmtHst.setPmtDueDate(rescheduleUtils.getNextPaymentDay(account.getProductCd(), account.getNextStmtDate()));
		// 设置生成账单类型
		stmtHst.setStmtFlag(calcStatementFlag(account, posts, txnUnstmts.size()));
		// 设置上期账单期初余额
		stmtHst.setStmtBegBal(account.getBegBal());
		// 设置账单应还款额
		stmtHst.setQualGraceBal(calculator.calcQualGraceBal(plans));
		
		// 当期借方发生额需要减去当期分期转出金额，加上当期分期转入金额   mantis4492 by dingxl
		BigDecimal ctdLoanOutAmt = calcLoanOutAmt(posts, txnUnstmts);
		BigDecimal ctdLoanInAmt = calcLoanInAmt(posts, txnUnstmts);
		// 设置当期借记金额 = 当期取现金额 + 当期消费金额 + 当期借记调整金额 + 当期费用+当期利息 
		//              -当期分期转出金额 + 当期分期转入金额  mantis4492 by dingxl
		stmtHst.setCtdAmtDb(account.getCtdCashAmt().add(account.getCtdRetailAmt())
				.add(account.getCtdDbAdjAmt()).add(account.getCtdFeeAmt()).add(account.getCtdInterestAmt()).subtract(ctdLoanOutAmt).add(ctdLoanInAmt));

		// 设置当期借记笔数
		stmtHst.setCtdNbrDb(account.getCtdCashCnt() + account.getCtdRetailCnt()
				+ account.getCtdDbAdjCnt()+account.getCtdFeeCnt()+account.getCtdInterestCnt());

		// 设置当期贷记金额 = 当期贷记调整金额 + 当期退货金额 + 当期还款金额
		stmtHst.setCtdAmtCr(account.getCtdCrAdjAmt().add(account.getCtdRefundAmt())
				.add(account.getCtdRepayAmt()));

		// 设置当期贷记笔数
		stmtHst.setCtdNbrCr(account.getCtdCrAdjCnt() + account.getCtdRefundCnt()
				+ account.getCtdRepayCnt());

		Date beginDate = account.getTempLmtBegDate();
		Date endDate = account.getTempLmtEndDate();
		// 判定批量日期时临时额度是否有效，无效则将账单历史中的临时额度相关字段清空
		if (beginDate != null 
				&& !(batchStatusFacility.getBatchDate().before(endDate) 
						&& batchStatusFacility.getBatchDate().after(beginDate))) {
			stmtHst.setStmtBegBal(BigDecimal.ZERO);
			stmtHst.setTempLmtBegDate(null);
			stmtHst.setTempLmtEndDate(null);
		}
	
		//	设置当前余额
		stmtHst.setStmtCurrBal(account.getCurrBal());
		
		//	 账单上的最小还款额需要包括超限部分
		BigDecimal totDue = account.getTotDueAmt().add(calculator.getOverLimitAmt(account, plans, batchStatusFacility.getBatchDate()));
		if(totDue.compareTo(stmtHst.getQualGraceBal())>0){
			totDue = stmtHst.getQualGraceBal();
		}
		stmtHst.setTotDueAmt(totDue);
		if(stmtHst.getCashLmtRate() == null){
			ProductCredit productCredit = parameterFacility.retrieveParameterObject(account.getProductCd(), ProductCredit.class);
			AccountAttribute attr = parameterFacility.loadParameter(productCredit.accountAttributeId, AccountAttribute.class);
			stmtHst.setCashLmtRate(attr.cashLimitRate);
		}
		stmtHst.setCtdStmtBal(getCtdStmtBal(plans));
		stmtHst.setLastStmtBal(getLastStmtBal(account));
		return stmtHst;
	}
	
	/**
	 * 账单日更新信用计划统计信息
	 * @param item
	 */
	private void updatePlanAtStmtDay(S6000AcctInfo item){
		for (CcsPlan plan : item.getPlans()){
			// 期初余额统计用临时变量
			BigDecimal beginBal = BigDecimal.ZERO;
			
			// 当期余额
			BigDecimal ctdBal = null;
			// 往期余额
			BigDecimal pastBal = null;
			
			// 循环所有余额成份
			for (BucketType bt : BucketType.values()){
				ctdBal = bnpManager.getBucketAmount(plan, bt, BnpPeriod.CTD);
				pastBal = bnpManager.getBucketAmount(plan, bt, BnpPeriod.PAST);
				
				beginBal = beginBal.add(ctdBal).add(pastBal);
				bnpManager.setBucketAmount(plan, bt, BnpPeriod.PAST, pastBal.add(ctdBal));
				bnpManager.setBucketAmount(plan, bt, BnpPeriod.CTD, BigDecimal.ZERO);
			}
			
			// 设置期初余额
			plan.setBegBal(beginBal);
			
			// 设置当前余额
			plan.setCurrBal(beginBal);
			
			// 设置当期贷记借记金额
			plan.setCtdAmtCr(BigDecimal.ZERO);
			plan.setCtdNbrCr(0);
			plan.setCtdAmtDb(BigDecimal.ZERO);
			plan.setCtdNbrDb(0);
			plan.setLastAccruPrinSum(plan.getAccruPrinSum());
			plan.setAccruPrinSum(BigDecimal.ZERO);
		}
	}
	
	/**
	 * 判断是否出账单
	 * 
	 * @param account
	 * @param txnUnstmts
	 * @return
	 */
	private Indicator calcStatementFlag(CcsAcct account, List<CcsPostingTmp> posts, int txnUnstmtCount) {
		if (account == null) {
			throw new IllegalArgumentException("账户不存在");
		}

		// 账户属性参数
		ProductCredit productCredit = parameterFacility.loadParameter(account.getProductCd(), ProductCredit.class);
		AccountAttribute acctAttr = parameterFacility.loadParameter(productCredit.accountAttributeId, AccountAttribute.class);
		
		// 客户要求不出账单
		if (account.getStmtFlag() == Indicator.N) {
			logger.debug("判断是否出账单：客户要求不出账单");
			logger.debug("账户出账单标识："+account.getStmtFlag());
			return Indicator.N;
		}
		
		// 锁定码指示不出账单
		if(!blockCodeUtils.getMergedStmtInd(account.getBlockCode())){
			logger.debug("判断是否出账单：锁定码指示不出账单");
			logger.debug("账户锁定码："+account.getBlockCode());
			return Indicator.N;
		}
		
		// 本期有交易, 出账单
		if(txnUnstmtCount + posts.size() != 0){
			logger.debug("本期有交易, 出账单");
			logger.debug("未出账单交易记录数："+txnUnstmtCount);
			logger.debug("当天入账交易记录数："+posts.size()); 
			return Indicator.Y;
		}
		
		logger.debug("账户余额："+account.getCurrBal());
		logger.debug("溢缴款免出账单最大金额："+acctAttr.crMaxbalNoStmt);
		logger.debug("免出账单最大欠款："+acctAttr.stmtMinBal);
		logger.debug("仅有积分需出账单："+acctAttr.stmtOnBpt);
		logger.debug("积分余额："+account.getPointsBal());
		
		// 贷方余额>=免出账单最大金额, 出
		if (account.getCurrBal().negate().compareTo(acctAttr.crMaxbalNoStmt) >= 0){
			logger.debug("贷方余额>=免出账单最大金额, 出账单");
			return Indicator.Y;
		}
		
		// 账户余额>=免出账单最小余额, 出
		if (account.getCurrBal().compareTo(acctAttr.stmtMinBal) >= 0){
			logger.debug("账户余额>=免出账单最小余额, 出账单");
			return Indicator.Y;
		}
		
		// 仅积分出账单, 且积分>0, 出
		if(acctAttr.stmtOnBpt && account.getPointsBal().compareTo(BigDecimal.ZERO) > 0){
			logger.debug("积分出账单, 且有积分, 出账单");
			return Indicator.Y;
		}
		
		logger.debug("出账单条件均不满足，不出账单");
		return Indicator.N;
	}
	
	/**
	 * 计算账户当期分期转出发生额
	 * 
	 * @param posts 当天入账交易
	 * @param txnUnstmts 未出账单交易
	 * @return sumLoanOutAmt 当期分期转出发生额
	 */
	private BigDecimal calcLoanOutAmt(List<CcsPostingTmp> posts,List<CcsTxnUnstatement> txnUnstmts){
		logger.debug("计算账户当期分期转出发生额");
		BigDecimal sumLoanOutAmt = BigDecimal.ZERO;
		
		//当天入账交易
		for(CcsPostingTmp txnPost : posts){
			// 交易码参数
			TxnCd txnCd = parameterFacility.loadParameter(txnPost.getTxnCode(), TxnCd.class);
			if(EnumUtils.in(txnCd.planType, PlanType.O,PlanType.P,PlanType.J)
					&&txnPost.getPostingFlag() == PostingFlag.F00
					&&EnumUtils.in(txnCd.txnType, TxnType.T06, TxnType.T09)
					&&!DbCrInd.M.equals(txnPost.getDbCrInd())){
				sumLoanOutAmt = sumLoanOutAmt.add(txnPost.getPostAmt());
			}
		}
		
		//未出账单交易
		for(CcsTxnUnstatement txnUnstmt : txnUnstmts){
			// 交易码参数
			TxnCd txnCd = parameterFacility.loadParameter(txnUnstmt.getTxnCode(), TxnCd.class);
			if(EnumUtils.in(txnCd.planType, PlanType.O,PlanType.P,PlanType.J)
					&&txnUnstmt.getPostingFlag() == PostingFlag.F00
					&&EnumUtils.in(txnCd.txnType, TxnType.T06, TxnType.T09)
					&&!DbCrInd.M.equals(txnUnstmt.getDbCrInd())){
				sumLoanOutAmt = sumLoanOutAmt.add(txnUnstmt.getPostAmt());
			}
		}
		
		logger.debug("账户当期分期转出发生额："+sumLoanOutAmt);
		return sumLoanOutAmt;	
	}
	
	/**
	 * 计算账户当期分期入发生额
	 * 
	 * @param posts 当天入账交易
	 * @param txnUnstmts 未出账单交易
	 * @return sumLoanInAmt 当期分期转入发生额
	 */
	private BigDecimal calcLoanInAmt(List<CcsPostingTmp> posts,List<CcsTxnUnstatement> txnUnstmts){
		logger.debug("计算账户当期分期转入发生额");
		BigDecimal sumLoanInAmt = BigDecimal.ZERO;
		
		//当天入账交易
		for(CcsPostingTmp txnPost : posts){
			// 交易码参数
			TxnCd txnCd = parameterFacility.loadParameter(txnPost.getTxnCode(), TxnCd.class);
			if(EnumUtils.in(txnCd.planType, PlanType.I,PlanType.Q,PlanType.L)
					&&txnPost.getPostingFlag() == PostingFlag.F00
					&&EnumUtils.in(txnCd.txnType, TxnType.T06, TxnType.T09)){
				sumLoanInAmt = sumLoanInAmt.add(txnPost.getPostAmt());
			}
		}
		
		//未出账单交易
		for(CcsTxnUnstatement txnUnstmt : txnUnstmts){
			// 交易码参数
			TxnCd txnCd = parameterFacility.loadParameter(txnUnstmt.getTxnCode(), TxnCd.class);
			if(EnumUtils.in(txnCd.planType, PlanType.I,PlanType.Q,PlanType.L)
					&&txnUnstmt.getPostingFlag() == PostingFlag.F00
					&&EnumUtils.in(txnCd.txnType, TxnType.T06, TxnType.T09)){
				sumLoanInAmt = sumLoanInAmt.add(txnUnstmt.getPostAmt());
			}
		}
		
		logger.debug("账户当期分期转入发生额："+sumLoanInAmt);
		return sumLoanInAmt;	
	}
	
	/**
	 * 获取账单期初金额
	 * 
	 * @param account 信用计划
	 * @return lastStmtBal 账单期初金额
	 */
	private BigDecimal getLastStmtBal(CcsAcct account) {
		logger.debug("获取账单期初金额");
		BigDecimal lastStmtBal = BigDecimal.ZERO;
		//如果上期账单日期为空表示第一期账单，则直接返回0
		if (account.getLastStmtDate() == null){
			return lastStmtBal;
		}
		
		//获取上期账单记录
		CcsStatementKey key = new CcsStatementKey();
		key.setAcctNbr(account.getAcctNbr());
		key.setAcctType(account.getAcctType());
		key.setStmtDate(account.getLastStmtDate());
		
		CcsStatement stmtHst = rTmStmtHst.findOne(key);
		if (stmtHst == null){
			throw new IllegalArgumentException("账单历史不存在:账号[" + key.getAcctNbr() 
						+ ", 账户类型["+ key.getAcctType() 
						+ ",账单日期[" + key.getStmtDate() + "]");
		}
		
		lastStmtBal=stmtHst.getCtdStmtBal();
		
		return lastStmtBal;
	}
	
	/**
	 * 获取账单当期金额
	 * 
	 * @param plans 信用计划
	 * @return ctdStmtBal 账单当期金额
	 */
	private BigDecimal getCtdStmtBal(List<CcsPlan> plans) {
		logger.debug("获取账单当期金额");
		BigDecimal ctdStmtBal = BigDecimal.ZERO;
		// 循环所有的Plan
		for (CcsPlan plan : plans) {
			// 获取Plan参数
			PlanTemplate planTemplate = parameterFacility.retrieveParameterObject(plan.getPlanNbr(), PlanTemplate.class);

			// 循环所有余额成分
			// 判断余额成分对应的是否计入全额应还款金额参数为true的余额成分计入全额还款金额
			for (BucketType bucketType : BucketType.values()) {
				if(planTemplate.intParameterBuckets.get(bucketType) == null) continue;
				Boolean b = planTemplate.intParameterBuckets.get(bucketType).graceQualify;
				if (b==null?false:b) {
					ctdStmtBal = ctdStmtBal.add(bnpManager.getBucketAmount(plan, bucketType, BnpPeriod.PAST))
										.add(bnpManager.getBucketAmount(plan, bucketType, BnpPeriod.CTD));
				}
			}
		}
		//对于不同机构可能溢缴款余额不参与全额还款金额计算，所以仍需统计溢缴款信用计划余额之和
		if (ctdStmtBal.compareTo(BigDecimal.ZERO)<=0)
		{
			ctdStmtBal = BigDecimal.ZERO;
			for (CcsPlan plan : plans) {
				if (PlanType.D == plan.getPlanType()) {
					ctdStmtBal = ctdStmtBal.add(plan.getCurrBal());
				}
			}
		}
		
		return ctdStmtBal;
	}
}
