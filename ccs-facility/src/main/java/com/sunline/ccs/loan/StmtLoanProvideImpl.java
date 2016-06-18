package com.sunline.ccs.loan;

import java.math.BigDecimal;

import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ppy.dictionary.enums.LoanType;

/**
 * 
 * 账单分期本金，手续费计算实现类 StmtLoanProvideImple
 *  
 * @version 1.0.0
 * 
 */
public class StmtLoanProvideImpl extends AbstractLoanProvide {

	public StmtLoanProvideImpl(LoanType loanType, LoanFeeDef loanFeeDef) {
		super.loanType = loanType;
		super.loanFeeDef = loanFeeDef;
	}

	@Override
	void calcuteOrig(CcsLoanReg loanReg) {
		loanReg.setOrigTxnAmt(BigDecimal.ZERO);// 原始交易金额
		loanReg.setOrigTransDate(null);// 原始交易日期
		loanReg.setOrigAuthCode(null);// 原始交易授权
	}


}
