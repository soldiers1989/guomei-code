package com.sunline.ccs.loan;

import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ccs.param.def.ProgramFeeDef;
import com.sunline.ccs.param.def.enums.DistributeMethod;
import com.sunline.ppy.dictionary.enums.LoanFeeMethod;
import com.sunline.ppy.dictionary.enums.LoanType;

/**
 * 
 * 账单分期本金，手续费计算实现类 StmtLoanProvideImple
 *  
 * @version 1.0.0
 * 
 */
public class AuthLoanProvideImpl extends AbstractLoanProvide {

	/**
	 * 分期生成reg信息
	 * @param loanType
	 * @param feeDef
	 * @param loanFeeMehod
	 */
	public AuthLoanProvideImpl(LoanType loanType, ProgramFeeDef feeDef, LoanFeeMethod loanFeeMehod) {
		LoanFeeDef fee = new LoanFeeDef();
		fee.feeRate = feeDef.feeRate;
		fee.feeAmount = feeDef.feeAmount;
		fee.loanFeeCalcMethod = feeDef.loanFeeCalcMethod;
		fee.loanFeeMethod = loanFeeMehod;
		// TODO 暂不支持分配表方式
		fee.distributeMethod = DistributeMethod.F;

		super.loanType = loanType;
		super.loanFeeDef = fee;
	}
	
	/**
	 * 小额贷生成reg信息
	 * @param loanType
	 * @param feeDef
	 * @param loanFeeMehod
	 */
	public AuthLoanProvideImpl(LoanType loanType, LoanFeeDef feeDef, LoanFeeMethod loanFeeMehod) {
		LoanFeeDef fee = new LoanFeeDef();
		fee.feeRate = feeDef.feeRate;
		fee.feeAmount = feeDef.feeAmount;
		fee.loanFeeCalcMethod = feeDef.loanFeeCalcMethod;
		fee.loanFeeMethod = loanFeeMehod;
		// TODO 暂不支持分配表方式
		fee.distributeMethod = DistributeMethod.F;

		super.loanType = loanType;
		super.loanFeeDef = fee;
	}

	@Override
	void calcuteOrig(CcsLoanReg loanReg) {
		loanReg.setOrigTxnAmt(null);// 原始交易金额
		loanReg.setOrigTransDate(null);// 原始交易日期
		loanReg.setOrigAuthCode(null);// 原始交易授权
	}
	
	public static void main(String[] args) {
	}
}
