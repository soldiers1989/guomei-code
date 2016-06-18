package com.sunline.ccs.batch.cc3000.loan;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ccs.batch.cc3000.S3001LoanHandler;
import com.sunline.ccs.batch.cc3000.U3001LoanAction;
import com.sunline.ccs.batch.cc3000.U3001LoanPrepare;
import com.sunline.ccs.batch.common.TxnPrepare;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.MicroCreditRescheduleUtils;
import com.sunline.ccs.infrastructure.server.repos.RCcsLoan;
import com.sunline.ccs.infrastructure.server.repos.RCcsRepaySchedule;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsPostingTmp;
import com.sunline.ccs.infrastructure.shared.model.CcsRepaySchedule;
import com.sunline.ccs.loan.McLoanProvideImpl;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ccs.param.def.enums.SysTxnCd;
import com.sunline.ppy.dictionary.enums.Indicator;

/**
 * @see 类名：LoanMCEI
 * @see 描述：小额贷款-等额本息
 *
 * @see 创建日期：   2015-6-23下午7:46:59
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Component
public class LoanMCEI extends Loan {
	
	@Autowired
	private CustAcctCardFacility queryFacility;
	@Autowired
    private BatchStatusFacility batchFacility;
	@Autowired
	private RCcsLoan rCccLoan;
	@Autowired
	private RCcsRepaySchedule rTmSchedule;
	@Autowired
	private U3001LoanPrepare loanPrepare;
	@Autowired
	private U3001LoanAction loanAction;
	@Autowired
	private MicroCreditRescheduleUtils rescheduleUtils;
	@Autowired
	private McLoanProvideImpl loanProvideImpl;
	@Autowired
	private TxnPrepare txnPrepare;
	
	@Override
	public boolean needApprove() {
		return true;
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
		
		// 获取参数
		LoanFeeDef loanFeeDef = rescheduleUtils.getLoanFeeDef(loanReg.getLoanCode(), loanReg.getLoanInitTerm(), 
				loanReg.getLoanInitPrin(),loanReg.getLoanFeeDefId());
		
		// 创建分期信息表记录
		CcsLoan loan = loanPrepare.generateLoan(loanReg);
		// 是否使用协议利率
		if(Indicator.Y.equals(loanReg.getAgreementRateInd())){
			loan = setLoanRate(loan, loanReg, loanFeeDef,true);
		}else{
			loan = setLoanRate(loan, loanReg, loanFeeDef, false);
		}
	
		loan.setLoanExpireDate(rescheduleUtils.getLoanPmtDueDate(acct.getNextStmtDate(), loanFeeDef, loanReg.getLoanInitTerm(),acct.getCycleDay()));
		rCccLoan.save(loan);
		
		List<CcsRepaySchedule> repaySchedules = loanProvideImpl.getLSchedule(loanReg,loanFeeDef,batchFacility.getBatchDate(),acct);
		for(CcsRepaySchedule rs:repaySchedules){
			rs.setLoanId(loan.getLoanId());
			rTmSchedule.save(rs);
		}
		//若收取趸交费，则首期生成一笔趸交费入账
		if(Indicator.Y==loanReg.getPremiumInd()){
			CcsPostingTmp premiumMemo = loanPrepare.generatePostingTmp(loanReg, SysTxnCd.S82, loanReg.getCardNbr(),loanReg.getLogicCardNbr(), null,null,loanReg.getPremiumAmt(),0);
			txnPrepare.txnPrepare(premiumMemo, null);
		}
	}


	@Override
	public void reschedule(S3001LoanHandler output, CcsLoanReg loanReg, CcsLoan loan) throws Exception {
		// 获取账户
		CcsAcct acct = queryFacility.getAcctByAcctNbr(loanReg.getAcctType(), loanReg.getAcctNbr());
		
		// 获取展期前下期schedule
		List<CcsRepaySchedule> origSchedules = rescheduleUtils.getRepayScheduleListByLoanId(loan.getLoanId());
		CcsRepaySchedule origNextSchedule = null;
		for (CcsRepaySchedule s : origSchedules) {
			if(s.getCurrTerm().intValue() == loan.getCurrTerm().intValue()+1){
				origNextSchedule = s;
				break;
			}
		}

		LoanFeeDef loanFeeDef = rescheduleUtils.getLoanFeeDef(loanReg.getLoanCode(), loanReg.getExtendTerm(), 
				loan.getUnstmtPrin().subtract(origNextSchedule.getLoanTermPrin()),loanReg.getLoanFeeDefId());
		// 重新生成分配计划
		List<CcsRepaySchedule> reSchedules = rescheduleUtils.genRescheduleForMCEI(loanReg, loan, acct.getNextStmtDate(), origNextSchedule, loanFeeDef);
		
		loanAction.microCreditReschedule(output, loanReg, loan, acct.getNextStmtDate(), origSchedules, reSchedules, loanFeeDef);
	}


	@Override
	public void prepayment(S3001LoanHandler output, CcsLoanReg loanReg,boolean isToday) throws Exception {
		if(isToday){
			//当日结清
			//这里考虑结清交易在途的情况，结清日期改为order中的业务日期
			loanAction.MCLoanTodaySettlement(output, loanReg);
		}else{
			loanAction.microCreditPrepayment(output, loanReg);
		}
		
	}

	@Override
	public void shorten(S3001LoanHandler output, CcsLoanReg loanReg, CcsLoan loan) throws Exception {
		// 获取账户
		CcsAcct acct = queryFacility.getAcctByAcctNbr(loanReg.getAcctType(), loanReg.getAcctNbr());
		
		
		// 获取缩期前下期schedule
		List<CcsRepaySchedule> origSchedules = rescheduleUtils.getRepayScheduleListByLoanId(loan.getLoanId());
		CcsRepaySchedule origNextSchedule = null;
		for (CcsRepaySchedule s : origSchedules) {
			if(s.getCurrTerm().intValue() == loan.getCurrTerm().intValue()+1){
				origNextSchedule = s;
				break;
			}
		}
		LoanFeeDef loanFeeDef = rescheduleUtils.getLoanFeeDef(loanReg.getLoanCode(), loanReg.getShortedTerm(), 
				loan.getUnstmtPrin().subtract(origNextSchedule.getLoanTermPrin()),loanReg.getLoanFeeDefId());
		
		// 重新生成分配计划
		List<CcsRepaySchedule> reSchedules = rescheduleUtils.genShortedForMCEI(loanReg, loan, acct.getNextStmtDate(), origNextSchedule, loanFeeDef);
		
		loanAction.microCreditShorted(output, loanReg, loan, acct.getNextStmtDate(), origSchedules, reSchedules, loanFeeDef);
		
	}

}
