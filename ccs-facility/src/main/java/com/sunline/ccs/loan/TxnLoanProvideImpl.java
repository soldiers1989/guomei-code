package com.sunline.ccs.loan;

import java.math.BigDecimal;
import java.util.Date;

import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsPostingTmp;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnUnstatement;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ppy.dictionary.enums.LoanType;

/**
 * 
 *  消费转分期，手续费计算实现类 StmtLoanProvideImple
 * TxnLoanProvideImpl  
 * 
		  
 * 张海涛  
 * 2012-9-27 下午4:39:23  
 * 
		  
 * @version 1.0.0  
 *
 */
public class TxnLoanProvideImpl extends AbstractLoanProvide {
	
	BigDecimal txnAmt;
	Date txnDate;
	String authCode;
	
	public TxnLoanProvideImpl(LoanType loanType, LoanFeeDef loanFeeDef, CcsTxnUnstatement unstmt){
		super.loanType = loanType;
		super.loanFeeDef = loanFeeDef;
		this.txnAmt = unstmt.getTxnAmt();
		this.txnDate = unstmt.getTxnDate();
		this.authCode = unstmt.getAuthCode();
	}
	
	public TxnLoanProvideImpl(LoanType loanType, LoanFeeDef loanFeeDef,CcsPostingTmp post){
		super.loanType = loanType;
		super.loanFeeDef = loanFeeDef;
		this.txnAmt = post.getTxnAmt();
		this.txnDate = post.getTxnDate();
		this.authCode = post.getAuthCode();
	}

	@Override
	void calcuteOrig(CcsLoanReg loanReg) {
		loanReg.setOrigTxnAmt(this.txnAmt);// 原始交易金额
		loanReg.setOrigTransDate(this.txnDate);// 原始交易日期
		loanReg.setOrigAuthCode(this.authCode);// 原始交易授权
	}
	
}
