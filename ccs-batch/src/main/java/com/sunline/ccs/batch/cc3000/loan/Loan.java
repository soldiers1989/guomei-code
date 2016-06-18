package com.sunline.ccs.batch.cc3000.loan;

import java.math.BigDecimal;

import com.sunline.ccs.batch.cc3000.S3001LoanHandler;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.param.def.LoanFeeDef;

/**
 * 
 * @see 类名：Loan
 * @see 描述：TODO 中文描述
 *
 * @see 创建日期：   2015-6-23下午7:42:58
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public abstract class Loan {
	
	private Integer befInitTerm;
	private BigDecimal befInitPrin;
	
	public abstract boolean needApprove();
	public abstract boolean needTimeOut();
	public abstract boolean needMatchTxn();
	public abstract boolean isOnline();
	
	
	public boolean isTimeOut() {
		// TODO Auto-generated method stub
		return false;
	}
	
	
	/**
	 * 
	 * @see 方法名：add 
	 * @see 描述：新建分期
	 * @see 创建日期：2015-6-23下午7:43:17
	 * @author ChengChun
	 *  
	 * @param loanReg
	 * @throws Exception
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public abstract void add(CcsLoanReg loanReg) throws Exception;
	
	/**
	 * 
	 * @see 方法名：reschedule 
	 * @see 描述：展期
	 * @see 创建日期：2015-6-23下午7:43:27
	 * @author ChengChun
	 *  
	 * @param output
	 * @param loanReg
	 * @param loan
	 * @throws Exception
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public abstract void reschedule(S3001LoanHandler output, CcsLoanReg loanReg, CcsLoan loan) throws Exception;
	
	/**
	 * @see 方法名：prepayment 
	 * @see 描述：提前还款
	 * @see 创建日期：2015-6-23下午7:43:44
	 * @author ChengChun
	 *  
	 * @param output
	 * @param loanReg
	 * @throws Exception
	 * @param  istoday 是否当日结清
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public abstract void prepayment(S3001LoanHandler output, CcsLoanReg loanReg,boolean isToday) throws Exception;
	
	/**
	 * @see 方法名：shorten 
	 * @see 描述：缩期
	 * @see 创建日期：2015-6-23下午7:43:55
	 * @author ChengChun
	 *  
	 * @param output
	 * @param loanReg
	 * @param loan
	 * @throws Exception
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public abstract void shorten(S3001LoanHandler output, CcsLoanReg loanReg, CcsLoan loan) throws Exception;

	
	
	
	public Integer getBefInitTerm() {
		return befInitTerm;
	}

	public void setBefInitTerm(Integer befInitTerm) {
		this.befInitTerm = befInitTerm;
	}

	public BigDecimal getBefInitPrin() {
		return befInitPrin;
	}

	public void setBefInitPrin(BigDecimal befInitPrin) {
		this.befInitPrin = befInitPrin;
	}
	
	/**
	 * 设置loan的利率
	 * @param loan
	 * @param loanReg
	 * @param loanFeeDef
	 * @param agreement 是否使用协议利率
	 * @return
	 */
	public CcsLoan setLoanRate(CcsLoan loan,CcsLoanReg loanReg,LoanFeeDef loanFeeDef,boolean agreement){
		//使用协议费率，这些费率都取loanreg中的
		//利息利率
		if(agreement){
			loan.setInterestRate(loanReg.getInterestRate());
			loan.setPenaltyRate(loanReg.getPenaltyRate());
			loan.setCompoundRate(loanReg.getCompoundRate());
			//印花税费率
			loan.setStampdutyRate(loanReg.getStampdutyRate());
			loan.setStampAmt(loanReg.getStampAmt());
			//服务费率
			loan.setFeeAmt(loanReg.getFeeAmt());
			loan.setFeeRate(loanReg.getFeeRate());
			//寿险费率
			loan.setLifeInsuFeeAmt(loanReg.getLifeInsuFeeAmt());
			loan.setLifeInsuFeeRate(loanReg.getLifeInsuFeeRate());
			//保险费率
			loan.setInsAmt(loanReg.getInsAmt());
			loan.setInsuranceRate(loanReg.getInsuranceRate());
			//手续费率
			loan.setInstallmentFeeAmt(loanReg.getInstallmentFeeAmt());
			loan.setInstallmentFeeRate(loanReg.getInstallmentFeeRate());
			//灵活还款计划包费率
			loan.setPrepayPkgFeeAmt(loanReg.getPrepayPkgFeeAmt());
			loan.setPrepayPkgFeeRate(loanReg.getPrepayPkgFeeRate());
			//代收服务费率
			loan.setReplaceSvcFeeRate(loanReg.getReplaceSvcFeeRate());
			loan.setReplaceSvcFeeAmt(loanReg.getReplaceSvcFeeAmt());
			
		}else{
			//全部取参数设置
			//利息利率
			loan.setInterestRate(loanFeeDef.interestRate);
			loan.setPenaltyRate(loanFeeDef.penaltyIntTableId);
			loan.setCompoundRate(loanFeeDef.compoundIntTableId);
			//设置费率
			if(loanFeeDef.stampRate != null){
				loan.setStampdutyRate(loanFeeDef.stampRate);
			}
			if(loanFeeDef.stampAMT != null){
				loan.setStampAmt(loanFeeDef.stampAMT);
			}
			//服务费率
			if(loanFeeDef.feeAmount != null){
				loan.setFeeAmt(loanFeeDef.feeAmount);
			}
			if(loanFeeDef.feeRate != null){
				loan.setFeeRate(loanFeeDef.feeRate);
			}
			//寿险费率
			if(loanFeeDef.lifeInsuFeeAmt != null){
				loan.setLifeInsuFeeAmt(loanFeeDef.lifeInsuFeeAmt);
			}
			if(loanFeeDef.lifeInsuFeeRate != null){
				loan.setLifeInsuFeeRate(loanFeeDef.lifeInsuFeeRate);
			}
			//保险费率
			if(loanFeeDef.insAmt != null){
				loan.setInsAmt(loanFeeDef.insAmt);
			}
			if(loanFeeDef.insRate != null){
				loan.setInsuranceRate(loanFeeDef.insRate);
			}
			//手续费率
			if(loanFeeDef.installmentFeeAmt != null){
				loan.setInstallmentFeeAmt(loanFeeDef.installmentFeeAmt);
			}
			if(loanFeeDef.installmentFeeRate != null){
				loan.setInstallmentFeeRate(loanFeeDef.installmentFeeRate);
			}
			//灵活还款计划包费率
			if(loanFeeDef.prepayPkgFeeAmount != null){
				loan.setPrepayPkgFeeAmt(loanFeeDef.prepayPkgFeeAmount);
			}
			if(loanFeeDef.prepayPkgFeeAmountRate != null){
				loan.setPrepayPkgFeeRate(loanFeeDef.prepayPkgFeeAmountRate);
			}
			
			// 代收服务费
			if(loanFeeDef.replaceFeeAmt != null){
				loan.setReplaceSvcFeeAmt(loanFeeDef.replaceFeeAmt);
			}
			if(loanFeeDef.replaceFeeRate != null){
				loan.setReplaceSvcFeeRate(loanFeeDef.replaceFeeRate);
			}
		}
		loan.setStampCustomInd(loanFeeDef.stampCustomInd);
		loan.setIsOffsetRate(loanFeeDef.isOffsetRate);
		return loan;
	}
	
}
