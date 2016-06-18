package com.sunline.ccs.batch.cc6000.transfer;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sunline.ppy.dictionary.enums.BucketType;
import com.sunline.ppy.dictionary.enums.DbCrInd;
import com.sunline.ppy.dictionary.enums.ExceptionType;
import com.sunline.ppy.dictionary.enums.LoanStatus;
import com.sunline.ppy.dictionary.enums.PostGlIndicator;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ccs.batch.cc6000.S6000AcctInfo;
import com.sunline.ccs.batch.cc6000.common.Calculator;
import com.sunline.ccs.batch.cc6000.common.PlanManager;
import com.sunline.ccs.batch.cc6000.common.TransactionGenerator;
import com.sunline.ccs.facility.BlockCodeUtils;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.CcsPostingTmp;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.param.def.PlanTemplate;
import com.sunline.ccs.param.def.enums.LoanTerminateReason;
import com.sunline.ccs.param.def.enums.PaymentCalcMethod;
import com.sunline.acm.service.sdk.BatchStatusFacility;

/**
 * @see 类名：TransferDefault
 * @see 描述：默认信用计划转移处理
 *
 * @see 创建日期：   2015-6-24下午6:19:36
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Component
public class TransferDefault extends Transfer {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private UnifiedParameterFacility parameterFacility;
	@Autowired
	private TransactionGenerator generatorTransaction;
	@Autowired
	private PlanManager createPlan;
	@Autowired
	private Calculator calculator;
	@Autowired
	private BlockCodeUtils blockCodeUtils;
	@Autowired
	private BatchStatusFacility batchFacility;
	
	
	@Override
	public CcsPlan getXfrInPlan(S6000AcctInfo item, PlanTemplate xfrOut,
			PlanTemplate xfrIn,CcsPlan xfrOutPlan,CcsLoan tmLoan) {
		// 查找交易对应信用计划
		List<CcsPlan> findPlans = createPlan.findTxnPlans(xfrOutPlan.getLogicCardNbr(), xfrIn.planNbr, xfrIn.planType, xfrOutPlan.getRefNbr(), item.getPlans(),null);
		CcsPlan xfrInPlan = null;
		// 信用计划不存在
		if (findPlans.size() < 1) {
			// 建立信用计划
			xfrInPlan = createPlan.generateXfrInPlan(xfrOutPlan, tmLoan, xfrIn, batchFacility.getBatchDate(),null);
			// 增加新建信用计划
			item.getPlans().add(xfrInPlan);
		}
		// 找到多个转入信用计划
		else if (findPlans.size() > 1) {
			// 增加异常账户报表记录
			calculator.makeExceptionAccount(item, xfrIn.planNbr, xfrOutPlan.getRefNbr(), ExceptionType.E10);
			return null;
		} else{ 
			xfrInPlan = findPlans.get(0);
		}
		return xfrInPlan;
	}
	
	@Override
	public void xfr(S6000AcctInfo item, CcsPlan xfrOutPlan,CcsPlan xfrInPlan,CcsLoan tmLoan)throws Exception{
		// 分期转移金额
		BigDecimal xfrAmt = getloanPlanxfrAmt(xfrOutPlan, item.getAccount(), tmLoan);
		
		logger.debug("分期计划转移: out.PlanNbr["+xfrOutPlan.getPlanNbr()+"],in.PlanNbr["+xfrInPlan.getPlanNbr()+"],xfrAmt["+xfrAmt+"]");
		
		// 更新分期信息表(TM_LOAN)的当前期数和剩余期数
		if (tmLoan.getRemainTerm() >= 1) {
			tmLoan.setCurrTerm(tmLoan.getCurrTerm() + 1);
			tmLoan.setRemainTerm(tmLoan.getRemainTerm() - 1);
		}

		// 生成两笔分期转移交易，并增加到账户交易List中
		List<CcsPostingTmp> l = generatorTransaction.generateLoanTransferTransaction(item, xfrOutPlan, xfrInPlan, tmLoan, xfrAmt, batchFacility.getBatchDate());
		String txnSeq = UUID.randomUUID().toString();
		for(CcsPostingTmp t : l){
			//将分期转出和转入送入glp
			item.getGlTxnItemList().add(calculator.makeGlTxn(
					item.getAccount().getOrg(), item.getAccount().getDefaultLogicCardNbr(), item.getAccount().getAcctNbr(), item.getAccount().getAcctType(), 
					txnSeq, item.getAccount().getCurrency(), item.getAccount().getAgeCodeGl(), t.getTxnCode(), t.getTxnDesc(), DbCrInd.M, 
					batchFacility.getBatchDate(), t.getTxnAmt(), PostGlIndicator.N, item.getAccount().getOwningBranch(), 
					t.getAcqBranchIq(), t.getPlanNbr(), BucketType.Pricinpal));
		}
        
        // 生成一笔分期手续费
        CcsPostingTmp txnPost = generatorTransaction.generateLoanFee(item, xfrInPlan, tmLoan, batchFacility.getBatchDate());
        
		// 分期计划转移计划(Tm_plan)和分期信息表(Tm_Loan)，金额更新
		generateloanPlanxfrAmt(xfrOutPlan, xfrInPlan, tmLoan, xfrAmt, txnPost == null ? BigDecimal.ZERO : txnPost.getPostAmt());
	}
	/**
	 * @see 方法名：getloanPlanxfrAmt 
	 * @see 描述：分期计划转移，金额计算
	 * @see 创建日期：2015-6-24下午6:20:06
	 * @author ChengChun
	 *  
	 * @param xfrOutPlan
	 * @param account
	 * @param loan
	 * @return
	 * @throws Exception
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public BigDecimal getloanPlanxfrAmt(CcsPlan xfrOutPlan, CcsAcct account, CcsLoan loan) throws Exception {
		// 分期转移金额
		BigDecimal xfrAmt = BigDecimal.ZERO;
		// 根据分期类型代码，查找分期控制参数
		LoanPlan loanPlan = parameterFacility.loadParameter(loan.getLoanCode(), LoanPlan.class);
		
		// 人工终止转移
		if (loan.getLoanStatus() == LoanStatus.T
				&& (loan.getTerminalReasonCd()==LoanTerminateReason.V 
					|| loan.getTerminalReasonCd()==LoanTerminateReason.M
					|| loan.getTerminalReasonCd()==LoanTerminateReason.P)){
			xfrAmt = xfrOutPlan.getCurrBal();
//			loan.setLastLoanStatus(loan.getLoanStatus());//FIXME 联机赋值
			loan.setTerminalDate(batchFacility.getBatchDate());
		}
		// 逾期终止转移
		else if (!account.getAgeCode().equals("C")
				&& Integer.valueOf(account.getAgeCode()) >= Integer.valueOf(loanPlan.terminateAgeCd)) {
			xfrAmt = xfrOutPlan.getCurrBal();
			loan.setLastLoanStatus(loan.getLoanStatus());
			loan.setLoanStatus(LoanStatus.T);
			loan.setTerminalReasonCd(LoanTerminateReason.D);
			loan.setTerminalDate(batchFacility.getBatchDate());
		}
		// 锁定码终止转移：B|要求全部余额进行还款
		else if (PaymentCalcMethod.B == blockCodeUtils.getMergedPaymentInd(account.getBlockCode())) {
			xfrAmt = xfrOutPlan.getCurrBal();
			loan.setLastLoanStatus(loan.getLoanStatus());
			loan.setLoanStatus(LoanStatus.T);
			loan.setTerminalReasonCd(LoanTerminateReason.R);
			loan.setTerminalDate(batchFacility.getBatchDate());
		}
		// 正常转移
		else {
			// 如果剩余期数<=1，则为期末，转移信用计划全部余额
			if (loan.getRemainTerm() <= 1) {
				xfrAmt = xfrOutPlan.getCurrBal();
				loan.setLastLoanStatus(loan.getLoanStatus());
				loan.setLoanStatus(LoanStatus.F);
			} else {
				// 当前计划余额 > 分期每期应还本金, 正常转移1期金额
				if (xfrOutPlan.getCurrBal().compareTo(loan.getLoanFixedPmtPrin()) > 0) {
					xfrAmt = loan.getLoanFixedPmtPrin();
				}
				// 余额不足, 全部转移, 分期完成
				else {
					xfrAmt = xfrOutPlan.getCurrBal();
					loan.setLastLoanStatus(loan.getLoanStatus());
					loan.setLoanStatus(LoanStatus.F);
				}
			}
		}
		
		return xfrAmt;
	}
	/**
	 * @see 方法名：generateloanPlanxfrAmt 
	 * @see 描述：分期计划转移，金额更新
	 * @see 创建日期：2015-6-24下午6:20:27
	 * @author ChengChun
	 *  
	 * @param xfrOutPlan
	 * @param xfrInPlan
	 * @param loan
	 * @param xfrAmt
	 * @param loanFeeAmt
	 * @throws Exception
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public void generateloanPlanxfrAmt(CcsPlan xfrOutPlan, CcsPlan xfrInPlan, CcsLoan loan, BigDecimal xfrAmt, BigDecimal loanFeeAmt) throws Exception {
		
		// 信用计划余额
		xfrOutPlan.setCurrBal(xfrOutPlan.getCurrBal().subtract(xfrAmt));
		xfrInPlan.setCurrBal(xfrInPlan.getCurrBal().add(xfrAmt));
		
		// 信用计划本金
		BigDecimal outPrincipal = xfrOutPlan.getCtdPrincipal().subtract(xfrAmt);
		if (outPrincipal.compareTo(BigDecimal.ZERO) >= 0) {
			xfrOutPlan.setCtdPrincipal(outPrincipal);
		} else {
			xfrOutPlan.setCtdPrincipal(BigDecimal.ZERO);
			xfrOutPlan.setPastPrincipal(xfrOutPlan.getPastPrincipal().subtract(outPrincipal.abs()));
		}
		xfrInPlan.setCtdPrincipal(xfrInPlan.getCtdPrincipal().add(xfrAmt));
		
		// 更新分期信息表(TM_LOAN)的未出账单分期本金
		loan.setUnstmtPrin(loan.getUnstmtPrin().subtract(xfrAmt));
		// 更新分期信息表(TM_LOAN)的未出账单分期手续费
		loan.setUnstmtFee(loan.getUnstmtFee().subtract(loanFeeAmt));
		//更新分期信息表(TM_LOAN)的分期已出账单手续费
		loan.setLoanFeeXfrin(loan.getLoanFeeXfrin().add(loanFeeAmt));
		//更新分期信息表(TM_LOAN)的分期未到期手续费
		loan.setLoanFeeXfrout(loan.getLoanFeeXfrout().subtract(loanFeeAmt));
		//更新分期信息表(TM_LOAN)的分期已出账单本金
		loan.setLoanPrinXfrin(loan.getLoanPrinXfrin().add(xfrAmt));
		//更新分期信息表(TM_LOAN)的分期未到期本金
		loan.setLoanPrinXfrout(loan.getLoanPrinXfrout().subtract(xfrAmt));
	}
}
