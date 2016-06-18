package com.sunline.ccs.service.provide.loan;

import java.math.BigDecimal;
import java.util.Date;

import org.springframework.stereotype.Service;

import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanRegStatus;

/** 
 * @see 类名：MicroCreditProvide
 * @see 描述：小额贷款变更(缩期,展期)的手续费计算
 *
 * @see 创建日期：   2015年6月24日 下午2:49:22
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class MicroCreditProvide {

	/**
	 * 保存小额贷展期、缩期注册表公共部分
	 * @param loan
	 * @param acctO
	 * @param loanFeeDef
	 * @param busDate
	 * @param loanPlan
	 * @return
	 */
	public static CcsLoanReg genCcsLoanReg(CcsLoan loan, CcsAcctO acctO, LoanFeeDef loanFeeDef, Date busDate,
			LoanPlan loanPlan) {
		CcsLoanReg loanReg = new CcsLoanReg();
		loanReg.setOrg(OrganizationContextHolder.getCurrentOrg());
		loanReg.setAcctNbr(loan.getAcctNbr());
		loanReg.setAcctType(loan.getAcctType());
		loanReg.setRegisterDate(busDate);
		loanReg.setRequestTime(busDate);
		loanReg.setLogicCardNbr(loan.getLogicCardNbr());
		loanReg.setCardNbr(loan.getCardNbr());
		loanReg.setRefNbr(loan.getRefNbr());
		loanReg.setLoanType(loan.getLoanType());
		loanReg.setLoanRegStatus(LoanRegStatus.N);
		loanReg.setLoanInitTerm(loan.getLoanInitTerm());// 分期期数
		loanReg.setLoanInitPrin(loan.getLoanInitPrin());// 分期总本金
		loanReg.setLoanCode(loanPlan.loanCode);//分期计划代码
		loanReg.setLoanFeeMethod(loan.getLoanFeeMethod());
		loanReg.setOrigTxnAmt(BigDecimal.ZERO);// 原始交易金额
		loanReg.setOrigTransDate(null);// 原始交易日期
		//几个利率的取值2014-6-26由王伟民确认从取参数改为取loan表中
		loanReg.setCompoundRate(loan.getCompoundRate());
		loanReg.setFloatRate(loan.getFloatRate());
		loanReg.setInterestRate(loan.getInterestRate());
		loanReg.setPenaltyRate(loan.getPenaltyRate());
		loanReg.setLoanFirstTermPrin(BigDecimal.ZERO);
		loanReg.setLoanFixedPmtPrin(BigDecimal.ZERO);
		loanReg.setLoanFinalTermPrin(BigDecimal.ZERO);
		loanReg.setLoanInitFee(BigDecimal.ZERO);// 分期总手续费
		loanReg.setLoanFirstTermFee(BigDecimal.ZERO);// 分期首期手续费
		loanReg.setLoanFixedFee(BigDecimal.ZERO);// 分期每期手续费
		loanReg.setLoanFinalTermFee(BigDecimal.ZERO);// 分期末期手续费
		loanReg.setDueBillNo(loan.getDueBillNo());
		loanReg.setAdvPmtAmt(BigDecimal.ZERO);
		loanReg.setMatched(Indicator.N);
		
		return loanReg;
		
	}

}
