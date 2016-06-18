package com.sunline.ccs.batch.cc3000.loan;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ccs.batch.cc3000.S3001LoanHandler;
import com.sunline.ccs.batch.cc3000.U3001LoanAction;
import com.sunline.ccs.batch.cc3000.U3001LoanPrepare;
import com.sunline.ccs.batch.common.DateUtils;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.MicroCreditRescheduleUtils;
import com.sunline.ccs.infrastructure.server.repos.RCcsLoan;
import com.sunline.ccs.infrastructure.server.repos.RCcsRepaySchedule;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsRepaySchedule;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ppy.dictionary.enums.Indicator;

/**
 * @see 类名：LoanMCEP
 * @see 描述：小额贷款-等额本金
 *
 * @see 创建日期：   2015-6-23下午7:47:14
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Component
public class LoanMCEP extends Loan {
	
	@Autowired
	private CustAcctCardFacility queryFacility;
	@Autowired
    private BatchStatusFacility batchFacility;
	@Autowired
	private RCcsLoan rTmLoan;
	@Autowired
	private RCcsRepaySchedule rTmSchedule;
	@Autowired
	private U3001LoanPrepare loanPrepare;
	@Autowired
	private U3001LoanAction loanAction;
	@Autowired
	private MicroCreditRescheduleUtils rescheduleUtils;
	
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
		loan.setLoanExpireDate(rescheduleUtils.getLoanPmtDueDate(acct.getNextStmtDate(), loanFeeDef, loanReg.getLoanInitTerm()));

		// 是否使用协议利率
		if(loanReg.getAgreementRateInd().equals(Indicator.Y) ){
			loan = setLoanRate(loan, loanReg, loanFeeDef,true);
		}else{
			loan = setLoanRate(loan, loanReg, loanFeeDef, false);
		}
		loan.setStampCustomInd(loanFeeDef.stampCustomInd);
		loan.setIsOffsetRate(loanFeeDef.isOffsetRate);
		rTmLoan.save(loan);
		
		BigDecimal monthIntRt = rescheduleUtils.calcMonthIntRt(loanReg.getInterestRate(),loan.getFloatRate());
		BigDecimal monthPrin = loanReg.getLoanInitPrin().divide(new BigDecimal(loanReg.getLoanInitTerm()), 2, RoundingMode.HALF_UP); // 每期本金
		BigDecimal currMonthInt;
		Date loanPmtDueDate;
		for (int i = 1; i <= loanReg.getLoanInitTerm(); i++) {
			loanPmtDueDate = rescheduleUtils.getLoanPmtDueDate(acct.getNextStmtDate(), loanFeeDef, i);
			
			// 首期以日计息
			if(i == 1){
				int firstTermDays = DateUtils.getIntervalDays(batchFacility.getBatchDate(), acct.getNextStmtDate()) +1; //计息天数=间隔天数+1
				BigDecimal dayIntRt = rescheduleUtils.calcDayIntRt(loanReg.getInterestRate());
				currMonthInt = loanReg.getLoanInitPrin().multiply(dayIntRt).multiply(new BigDecimal(firstTermDays)).setScale(2, RoundingMode.HALF_UP);
			}else{
				currMonthInt = rescheduleUtils.calcMonthIntForMCEP(loanReg.getLoanInitPrin(), monthIntRt, loanReg.getLoanInitTerm(), i);
				// 末期补差
				if(i == loanReg.getLoanInitTerm()){
					BigDecimal fixPrin = loanReg.getLoanInitPrin().subtract(monthPrin.multiply(new BigDecimal(i))); // 本金补差
					monthPrin = monthPrin.add(fixPrin); // 本金修正
					BigDecimal fixInt = fixPrin.multiply(monthIntRt).multiply(new BigDecimal(i)).setScale(2, RoundingMode.HALF_UP); // 利息补差
					currMonthInt = currMonthInt.add(fixInt); // 利息修正
				}
			}
			BigDecimal currMonthInsurance = new BigDecimal(0);//保险费
			BigDecimal currMonthStampduty = new BigDecimal(0);//印花税
			BigDecimal currMonthLifeInsuFee = new BigDecimal(0);//寿险计划包费
			BigDecimal currMonthFee = new BigDecimal(0);//手续费
			BigDecimal currMonthSVCFee = new BigDecimal(0);//手续费
			BigDecimal currMonthReplaceSvcFee = new BigDecimal(0);//代收服务费
			BigDecimal currMonthPrepayPkg = new BigDecimal(0);//灵活还款计划包
			rTmSchedule.save(rescheduleUtils.generateRepaySchedule(loanReg, loan.getLoanId(), i, monthPrin,currMonthFee,currMonthSVCFee, currMonthInt,currMonthInsurance,currMonthStampduty,currMonthLifeInsuFee,currMonthReplaceSvcFee,currMonthPrepayPkg, loanPmtDueDate));
		}
	}

	@Override
	public void reschedule(S3001LoanHandler output, CcsLoanReg loanReg, CcsLoan loan) throws Exception {
		// 获取账户
		CcsAcct acct = queryFacility.getAcctByAcctNbr(loanReg.getAcctType(), loanReg.getAcctNbr());
		
		
		//展期前
		List<CcsRepaySchedule> origSchedules = rescheduleUtils.getRepayScheduleListByLoanId(loan.getLoanId());
		
		// 获取展期前下期schedule
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
		List<CcsRepaySchedule> reSchedules = rescheduleUtils.genRescheduleForMCEP(loanReg, loan, acct.getNextStmtDate(), origNextSchedule, loanFeeDef);
		
		loanAction.microCreditReschedule(output, loanReg, loan, acct.getNextStmtDate(), origSchedules, reSchedules, loanFeeDef);
	}

	@Override
	public void prepayment(S3001LoanHandler output, CcsLoanReg loanReg,boolean isToday) throws Exception {
		loanAction.microCreditPrepayment(output, loanReg);
	}

	@Override
	public void shorten(S3001LoanHandler output, CcsLoanReg loanReg, CcsLoan loan) throws Exception {
		// 获取账户
		CcsAcct acct = queryFacility.getAcctByAcctNbr(loanReg.getAcctType(), loanReg.getAcctNbr());
		
		
		//缩期前
		List<CcsRepaySchedule> origSchedules = rescheduleUtils.getRepayScheduleListByLoanId(loan.getLoanId());
		
		// 获取缩期前下期schedule
		CcsRepaySchedule origNextSchedule = null;
		for (CcsRepaySchedule s : origSchedules) {
			if(s.getCurrTerm().intValue() == loan.getCurrTerm().intValue()+1){
				origNextSchedule = s;
				break;
			}
		}
		LoanFeeDef loanFeeDef = rescheduleUtils.getLoanFeeDef(loanReg.getLoanCode(), loanReg.getShortedTerm(), 
				loan.getUnstmtPrin().subtract(origNextSchedule.getLoanTermPrin()),loan.getLoanFeeDefId());
		
		// 重新生成分配计划
		List<CcsRepaySchedule> reSchedules = rescheduleUtils.genShortedForMCEP(loanReg, loan, acct.getNextStmtDate(),	origNextSchedule, loanFeeDef);
		
		loanAction.microCreditShorted(output, loanReg, loan, acct.getNextStmtDate(), origSchedules, reSchedules, loanFeeDef);
	}

}
