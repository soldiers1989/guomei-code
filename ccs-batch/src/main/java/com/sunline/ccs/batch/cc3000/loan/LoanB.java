package com.sunline.ccs.batch.cc3000.loan;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sunline.ccs.batch.cc3000.S3001LoanHandler;
import com.sunline.ccs.batch.cc3000.U3001LoanAction;
import com.sunline.ccs.batch.cc3000.U3001LoanPrepare;
import com.sunline.ccs.batch.common.TxnPrepare;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.infrastructure.server.repos.RCcsLoan;
import com.sunline.ccs.infrastructure.server.repos.RCcsPlan;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.CcsPostingTmp;
import com.sunline.ccs.infrastructure.shared.model.QCcsPlan;
import com.sunline.ccs.param.def.enums.SysTxnCd;
import com.sunline.ppy.dictionary.enums.PlanType;

/**
 * 
 * @see 类名：LoanB
 * @see 描述：账单转分期
 *
 * @see 创建日期：   2015-6-23下午7:44:31
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Component
public class LoanB extends Loan {
	
	@Autowired
	private CustAcctCardFacility queryFacility;
	@Autowired
	private RCcsLoan rTmLoan;
	@Autowired
	private RCcsPlan rTmPlan;
	@Autowired
	private TxnPrepare txnPrepare;
	@Autowired
	private U3001LoanPrepare loanPrepare;
	@Autowired
	private U3001LoanAction loanAction;
	
	@Override
	public boolean needApprove() {
		return false;
	}
	
	@Override
	public boolean needTimeOut() {
		return false;
	}
	
	@Override
	public boolean needMatchTxn() {
		return false;
	}
	
	@Override
	public boolean isOnline() {
		return false;
	}
	
	@Override
	public void add(CcsLoanReg loanReg) throws Exception {
		// 获取账户
		CcsAcct acct = queryFacility.getAcctByAcctNbr(loanReg.getAcctType(), loanReg.getAcctNbr());
		// 获取账户的所有消费计划
		QCcsPlan qTmPlan = QCcsPlan.ccsPlan;
		Iterable<CcsPlan> iterableTmPlan = rTmPlan.findAll(qTmPlan.acctNbr.eq(loanReg.getAcctNbr())
				.and(qTmPlan.acctType.eq(loanReg.getAcctType()))
				.and(qTmPlan.planType.eq(PlanType.R)));
		// 从TM_PLAN中找到的分期计划形成列表，对列表中的每个计划依次做如下操作
		BigDecimal wsLoanInitPrin = loanReg.getLoanInitPrin();
		for (CcsPlan plan : iterableTmPlan) {
			
			if (plan.getPastPrincipal().compareTo(wsLoanInitPrin) <= 0) {
				// 使用该计划的PAST_PRIN作为交易金额 ，为该计划做一笔贷调交易（交易码不同于转分期）
				// 创建贷调交易
				CcsPostingTmp cAdjTxnPost = loanPrepare.generatePostingTmp(loanReg, SysTxnCd.S28, loanReg.getCardNbr(), 
						loanReg.getLogicCardNbr(), acct.getDefaultLogicCardNbr(), plan.getProductCd(), plan.getPastPrincipal(), null);
				txnPrepare.txnPrepare(cAdjTxnPost, null);
				
				// WS_LOAN_INIT_PRIN = WS_LOAN_INIT_PRIN - TM_PLAN中的PAST_PRIN
				wsLoanInitPrin = wsLoanInitPrin.subtract(plan.getPastPrincipal());
			} else {
				// 使用WS_LOAN_INIT_PRIN作为交易金额 ，为该计划做一笔贷调交易（交易码不同于转分期）
				// 创建贷调交易
				CcsPostingTmp cAdjTxnPost = loanPrepare.generatePostingTmp(loanReg, SysTxnCd.S28, loanReg.getCardNbr(), 
						loanReg.getLogicCardNbr(), acct.getDefaultLogicCardNbr(), plan.getProductCd(), wsLoanInitPrin, null);
				txnPrepare.txnPrepare(cAdjTxnPost, null);
				
				// WS_LOAN_INIT_PRIN = 0
				wsLoanInitPrin = BigDecimal.ZERO;
		        // 跳出信用计划的循环
				break;
			}
		}
		
		// 使用TT_LOAN_REG的LOAN_INIT_PRIN作为交易金额、ACCT_NO、ACCT_TYPE、LOGICAL_CARD_NO、CARD_NO生成一笔XFR OUT计划的借调交易（交易码不同于转分期）
		// 创建借调交易
		CcsPostingTmp dAdjTxnPost = loanPrepare.generatePostingTmp(loanReg, SysTxnCd.S29, loanReg.getCardNbr(), 
				loanReg.getLogicCardNbr(), acct.getDefaultLogicCardNbr(), null, loanReg.getLoanInitPrin(), null);
		txnPrepare.txnPrepare(dAdjTxnPost, null);
		
		// 创建分期信息表记录
		rTmLoan.save(loanPrepare.generateLoan(loanReg));
	}

	@Override
	public void reschedule(S3001LoanHandler output, CcsLoanReg loanReg, CcsLoan loan) throws Exception {
		loanAction.generalReschedule(loanReg, loan);
	}

	@Override
	public void prepayment(S3001LoanHandler output, CcsLoanReg loanReg,boolean isToday) throws Exception {
		loanAction.generalPrepayment(loanReg);
	}

	@Override
	public void shorten(S3001LoanHandler output, CcsLoanReg loanReg, CcsLoan loan) {
		// TODO Auto-generated method stub
		
	}


}
