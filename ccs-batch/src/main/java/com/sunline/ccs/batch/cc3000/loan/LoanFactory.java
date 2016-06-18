package com.sunline.ccs.batch.cc3000.loan;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
/**
 * @see 类名：LoanFactory
 * @see 描述：TODO 中文描述
 *
 * @see 创建日期：   2015-6-23下午7:46:03
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Component
public class LoanFactory {
	
	@Autowired
	private LoanR loanR;
	@Autowired
	private LoanB loanB;
	@Autowired
	private LoanC loanC;
	@Autowired
	private LoanP loanP;
	@Autowired
	private LoanM loanM;
	@Autowired
	private LoanMCAT loanMCAT;
	@Autowired
	private LoanMCEI loanMCEI;
	@Autowired
	private LoanMCEP loanMCEP;
	
	/**
	 * @see 方法名：defineLoan 
	 * @see 描述：确定分期(贷款)类型
	 * @see 创建日期：2015-6-23下午7:45:48
	 * @author ChengChun
	 *  
	 * @param loanReg
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public Loan defineLoan(CcsLoanReg loanReg) {
		switch (loanReg.getLoanType()){
		case R : return loanR;
		case B : return loanB;
		case C : return loanC;
		case P : return loanP;
		case M : return loanM;
		case MCAT : return loanMCAT;
		case MCEI : return loanMCEI;
		case MCEP : return loanMCEP;
		default: throw new IllegalArgumentException("分期类型不存在，分期类型" +loanReg.getLoanType()+"，申请号{}" +loanReg.getRegisterId());
		}
	}

}
