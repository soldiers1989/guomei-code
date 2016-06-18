package com.sunline.ccs.batch.cc3000.loan;

import org.springframework.stereotype.Component;

import com.sunline.ccs.batch.cc3000.S3001LoanHandler;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;

/**
 * @see 类名：LoanM
 * @see 描述：大额分期(专项分期)
 *
 * @see 创建日期：   2015-6-23下午7:46:20
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Component
public class LoanM extends Loan {
	
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
		
	}

	@Override
	public void reschedule(S3001LoanHandler output, CcsLoanReg loanReg, CcsLoan loan) throws Exception {
		
	}

	@Override
	public void prepayment(S3001LoanHandler output, CcsLoanReg loanReg,boolean isToday) throws Exception {
		
	}

	@Override
	public void shorten(S3001LoanHandler output, CcsLoanReg loanReg, CcsLoan loan) {
		// TODO Auto-generated method stub
		
	}


}
