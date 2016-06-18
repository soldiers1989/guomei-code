package com.sunline.ccs.batch.cc3000.loan;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sunline.ccs.batch.cc3000.S3001LoanHandler;
import com.sunline.ccs.batch.cc3000.U3001LoanAction;
import com.sunline.ccs.batch.cc3000.U3001LoanPrepare;
import com.sunline.ccs.infrastructure.server.repos.RCcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;

/**
 * @see 类名：LoanP
 * @see 描述：商户分期
 *
 * @see 创建日期：   2015-6-23下午7:47:26
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Component
public class LoanP extends Loan {

	@Autowired
	private RCcsLoan rTmLoan;
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
		return true;
	}
	
	@Override
	public boolean isOnline() {
		return true;
	}
	
	@Override
	public void add(CcsLoanReg loanReg) throws Exception {
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
