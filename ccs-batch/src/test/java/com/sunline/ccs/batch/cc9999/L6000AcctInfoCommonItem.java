package com.sunline.ccs.batch.cc9999;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;

import com.sunline.ccs.batch.cc6000.S6000AcctInfo;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanAction;
import com.sunline.ppy.dictionary.enums.LoanRegStatus;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.PlanType;

public class L6000AcctInfoCommonItem {
	
	static SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
	S6000AcctInfo item = new S6000AcctInfo();
	
	public S6000AcctInfo getItemMCEI(){
		return new SubS6000AcctInfoItemMCEI().getItemInstance();
	}
	
	public S6000AcctInfo getItemMCAT(){
		return new SubS6000AcctInfoItemMCAT().getItemInstance();
	}
	
	private class SubS6000AcctInfoItemMCEI extends SubS6000AcctInfoItem{
		@Override
		public S6000AcctInfo getItemInstance() {
			loan.setLoanType(LoanType.MCEI);
			
			xfrInPlan.setPlanType(PlanType.Q);
			xfrInPlan.setTerm(1);
			
			xfrInPlan2.setPlanType(PlanType.Q);
			xfrInPlan2.setTerm(2);
			
			xfrOutPlan.setPlanType(PlanType.P);
			
			item.getLoans().add(loan);
			item.getPlans().add(xfrInPlan);
			item.getPlans().add(xfrInPlan2);
			item.getPlans().add(xfrOutPlan);
			item.setAccount(acct);
			return item;
		}
	}
	
	private class SubS6000AcctInfoItemMCAT extends SubS6000AcctInfoItem{
		@Override
		public S6000AcctInfo getItemInstance() {
			loan.setLoanType(LoanType.MCAT);
			
			xfrInPlan.setPlanType(PlanType.L);
			xfrInPlan.setPastInterest(BigDecimal.ZERO);
			
			xfrOutPlan.setPlanType(PlanType.J);
			
			item.getLoans().add(loan);
			item.getPlans().add(xfrInPlan);
			item.getPlans().add(xfrOutPlan);
			item.setAccount(acct);
			return item;
		}
	}
	
	private abstract class SubS6000AcctInfoItem{
		
		public abstract S6000AcctInfo getItemInstance();
		public CcsLoan loan = new CcsLoan();
		CcsPlan xfrInPlan = new CcsPlan();
		CcsPlan xfrInPlan2 = new CcsPlan();
		CcsPlan xfrOutPlan = new CcsPlan();
		S6000AcctInfo item = new S6000AcctInfo();
		CcsAcct acct = new CcsAcct();
		
		public SubS6000AcctInfoItem(){
			loan.setLoanCode("3001");
			//逾期5天
			loan.setOverdueDate(DateUtils.addDays(new Date(), -5));
			loan.setCurrTerm(2);
			loan.setLoanFeeDefId("1");
			loan.setRefNbr("666666");
			
			xfrInPlan.setPenaltyAcru(BigDecimal.ONE);
			xfrInPlan.setPenaltyRate(new BigDecimal(0.003));
			xfrInPlan.setPlanNbr("001");
			xfrInPlan.setCtdPrincipal(new BigDecimal("0"));
			xfrInPlan.setPastPrincipal(new BigDecimal("100"));
			xfrInPlan.setUsePlanRate(Indicator.Y);
			xfrInPlan.setBegDefbnpIntAcru(BigDecimal.ZERO);
			xfrInPlan.setAccruPrinSum(BigDecimal.ZERO);
			xfrInPlan.setCtdDefbnpIntAcru(BigDecimal.ZERO);
			xfrInPlan.setNodefbnpIntAcru(BigDecimal.ZERO);
			xfrInPlan.setRefNbr("666666");
			xfrInPlan.setProductCd("1201");
			xfrInPlan.setNodefbnpIntAcru(BigDecimal.ZERO);
			xfrInPlan.setInterestRate(new BigDecimal(0.005));
			xfrInPlan.setReplacePenaltyRate(new BigDecimal(0.1));
			xfrInPlan.setReplacePenaltyAcru(BigDecimal.ZERO);
			xfrInPlan.setPastInterest(new BigDecimal(50));
			xfrInPlan.setCtdInterest(new BigDecimal(100));
			xfrInPlan.setCompoundAcru(BigDecimal.ZERO);
			xfrInPlan.setCompoundRate(new BigDecimal(0.001));
			xfrInPlan.setLogicCardNbr("cardNbr123");
			xfrInPlan.setCurrBal(new BigDecimal(100));
			xfrInPlan.setCtdMulctAmt(BigDecimal.ZERO);
			xfrInPlan.setCtdAmtDb(BigDecimal.ZERO);
			xfrInPlan.setCtdNbrDb(0);
			xfrInPlan.setPlanId(1L);
			
			xfrInPlan2.setPenaltyAcru(BigDecimal.ONE);
			xfrInPlan2.setPenaltyRate(new BigDecimal(0.003));
			xfrInPlan2.setPlanNbr("001");
			xfrInPlan2.setCtdPrincipal(new BigDecimal("1000"));
			xfrInPlan2.setPastPrincipal(new BigDecimal("200"));
			xfrInPlan2.setUsePlanRate(Indicator.Y);
			xfrInPlan2.setBegDefbnpIntAcru(BigDecimal.ZERO);
			xfrInPlan2.setAccruPrinSum(BigDecimal.ZERO);
			xfrInPlan2.setCtdDefbnpIntAcru(BigDecimal.ZERO);
			xfrInPlan2.setNodefbnpIntAcru(BigDecimal.ZERO);
			xfrInPlan2.setRefNbr("666666");
			xfrInPlan2.setProductCd("1201");
			xfrInPlan2.setNodefbnpIntAcru(BigDecimal.ZERO);
			xfrInPlan2.setInterestRate(new BigDecimal(0.005));
			xfrInPlan2.setReplacePenaltyRate(new BigDecimal(0.1));
			xfrInPlan2.setReplacePenaltyAcru(BigDecimal.ZERO);
			xfrInPlan2.setPastInterest(new BigDecimal(50));
			xfrInPlan2.setCtdInterest(new BigDecimal(100));
			xfrInPlan2.setCompoundAcru(BigDecimal.ZERO);
			xfrInPlan2.setCompoundRate(new BigDecimal(0.001));
			xfrInPlan2.setLogicCardNbr("cardNbr123");
			xfrInPlan2.setCurrBal(new BigDecimal(1200));
			xfrInPlan2.setCtdMulctAmt(BigDecimal.ZERO);
			xfrInPlan2.setCtdAmtDb(BigDecimal.ZERO);
			xfrInPlan2.setCtdNbrDb(0);
			xfrInPlan2.setPlanId(2L);
			
			xfrOutPlan.setPenaltyAcru(BigDecimal.ZERO);
			xfrOutPlan.setPenaltyRate(BigDecimal.ZERO);
			xfrOutPlan.setPlanNbr("002");
			xfrOutPlan.setCtdPrincipal(new BigDecimal("3000"));
			xfrOutPlan.setPastPrincipal(new BigDecimal("0"));
			xfrOutPlan.setUsePlanRate(Indicator.Y);
			xfrOutPlan.setAccruPrinSum(BigDecimal.ZERO);
			xfrOutPlan.setBegDefbnpIntAcru(BigDecimal.ZERO);
			xfrOutPlan.setCtdDefbnpIntAcru(BigDecimal.ZERO);
			xfrOutPlan.setNodefbnpIntAcru(BigDecimal.ZERO);
			xfrOutPlan.setRefNbr("666666");
			xfrOutPlan.setProductCd("1201");
			xfrOutPlan.setNodefbnpIntAcru(BigDecimal.ZERO);
			xfrOutPlan.setInterestRate(new BigDecimal(0.005));
			xfrOutPlan.setReplacePenaltyRate(BigDecimal.ZERO);
			xfrOutPlan.setReplacePenaltyAcru(BigDecimal.ZERO);
			xfrOutPlan.setPastInterest(BigDecimal.ZERO);
			xfrOutPlan.setCtdInterest(BigDecimal.ZERO);
			xfrOutPlan.setCompoundAcru(BigDecimal.ZERO);
			xfrOutPlan.setCompoundRate(BigDecimal.ZERO);
			xfrOutPlan.setLogicCardNbr("cardNbr123");
			xfrOutPlan.setCurrBal(BigDecimal.ZERO);
			xfrOutPlan.setCtdMulctAmt(BigDecimal.ZERO);
			xfrOutPlan.setCtdAmtDb(BigDecimal.ZERO);
			xfrOutPlan.setCtdNbrDb(0);
			xfrOutPlan.setPlanId(3L);
			
			acct.setSetupDate(DateUtils.addMonths(new Date(), -1));
			acct.setCtdDbAdjAmt(BigDecimal.ZERO);
			acct.setCtdDbAdjCnt(0);
			acct.setCtdFeeAmt(BigDecimal.ZERO);
			acct.setCtdFeeCnt(0);
			acct.setCurrBal(new BigDecimal(1300));
			acct.setAcctNbr(1L);
			acct.setAcctType(AccountType.E);
		}
	}
	
	public List<CcsLoanReg> P3001LoanRegTest(){
		//正常数据--回盘成功今日回盘
		CcsLoanReg reg1 = new CcsLoanReg();
		reg1.setAcctNbr(1L);
		reg1.setAcctType(AccountType.E);
		reg1.setDdRspFlag(Indicator.Y);
		reg1.setValidDate(new Date());
		reg1.setRegisterDate(DateUtils.addDays(new Date(), -2));
		reg1.setPreAdDate(DateUtils.addDays(new Date(), -1));
		reg1.setLoanAction(LoanAction.O);
		reg1.setLoanRegStatus(LoanRegStatus.A);
		reg1.setRegisterId(1L);
		reg1.setLoanType(LoanType.MCEI);
		
		//等待回盘
		CcsLoanReg reg2 = new CcsLoanReg();
		reg2.setAcctNbr(2L);
		reg2.setAcctType(AccountType.E);
		reg2.setRegisterDate(DateUtils.addDays(new Date(), -2));
		reg2.setPreAdDate(DateUtils.addDays(new Date(), -1));
		reg2.setLoanAction(LoanAction.O);
		reg2.setLoanRegStatus(LoanRegStatus.A);
		reg2.setRegisterId(2L);
		reg2.setLoanType(LoanType.MCEI);
		
		//回盘失败--有扣款状态，无生效日期
		CcsLoanReg reg3 = new CcsLoanReg();
		reg3.setAcctNbr(3L);
		reg3.setAcctType(AccountType.E);
		reg3.setRegisterDate(DateUtils.addDays(new Date(), -2));
		reg3.setDdRspFlag(Indicator.N);
		reg3.setPreAdDate(DateUtils.addDays(new Date(), -1));
		reg3.setLoanAction(LoanAction.O);
		reg3.setLoanRegStatus(LoanRegStatus.A);
		reg3.setRegisterId(3L);
		reg3.setLoanType(LoanType.MCEI);
		
		//未达预约日期
		CcsLoanReg reg4 = new CcsLoanReg();
		reg4.setAcctNbr(4L);
		reg4.setAcctType(AccountType.E);
		reg4.setRegisterDate(DateUtils.addDays(new Date(), -1));
		reg4.setPreAdDate(DateUtils.addDays(new Date(), -2));
		reg4.setLoanAction(LoanAction.O);
		reg4.setLoanRegStatus(LoanRegStatus.A);
		reg4.setRegisterId(4L);
		reg4.setLoanType(LoanType.MCEI);
		
		List<CcsLoanReg> regList = new ArrayList<CcsLoanReg>();
		regList.add(reg4);
		regList.add(reg3);
		regList.add(reg2);
		regList.add(reg1);
		return regList;
	}
}
