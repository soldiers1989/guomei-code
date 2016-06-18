package com.sunline.ccs.loan;

import java.math.BigDecimal;

import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ppy.dictionary.enums.LoanType;

/**
 * 
* @author fanghj
 *
 */
public class CashLoanProvideImpl extends AbstractLoanProvide {
	
	public CashLoanProvideImpl(LoanFeeDef loanFeeDef,LoanType loanType){
		this.loanFeeDef = loanFeeDef;
		this.loanType = loanType;
	}
	@Override
	void calcuteOrig(CcsLoanReg loanReg) {
		loanReg.setOrigTxnAmt(BigDecimal.ZERO);// 原始交易金额
		loanReg.setOrigTransDate(null);// 原始交易日期
	}

}
