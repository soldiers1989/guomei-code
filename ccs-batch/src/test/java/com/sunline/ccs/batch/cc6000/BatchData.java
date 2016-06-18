package com.sunline.ccs.batch.cc6000;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.sunline.ark.support.utils.DateUtils;
import com.sunline.ccs.batch.front.FrontBatchData;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomerCrlmt;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.CcsPostingTmp;
import com.sunline.ccs.infrastructure.shared.model.CcsRepaySchedule;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnHst;
import com.sunline.ccs.param.def.enums.BucketObject;
import com.sunline.ccs.param.def.enums.LimitType;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LimitCategory;
import com.sunline.ppy.dictionary.enums.LoanAction;
import com.sunline.ppy.dictionary.enums.LoanFeeMethod;
import com.sunline.ppy.dictionary.enums.LoanRegStatus;
import com.sunline.ppy.dictionary.enums.LoanStatus;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.PlanType;

public class BatchData {
	
	static String loanCode="1234";
	static String refNbr="123123";
	static String logicCardNbr = "6251560000000001";
	static String org = "000000000001";
	static SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
	
	public static CcsAcct genAcct()  throws Exception{
		CcsAcct acct = new CcsAcct();
		FrontBatchData.setNotNullField(CcsAcct.class, acct);
		acct.setOrg(org);
		acct.setProductCd("000001");
		acct.setCurrBal(BigDecimal.valueOf(2000));
		acct.setAcctNbr(Long.parseLong("1101500"));
		acct.setAcctType(AccountType.E);
		acct.setContrNbr("300116110101101500");
		acct.setName("lisy");
		Date date = sf.parse("2015-08-28");
		acct.setNextStmtDate(date);
		acct.setAgeCode("0");
		acct.setAgeCodeGl("0");
		acct.setBegBal(BigDecimal.ZERO);
		acct.setCustLmtId(1L);
		acct.setCreditLmt(BigDecimal.valueOf(10000));
		acct.setLastLimitAdjDate(DateUtils.addDays(new Date(), -5));
		return acct;
	}
	
	public static CcsAcctO genAcctO()  throws Exception{
		CcsAcctO acct = new CcsAcctO();
		FrontBatchData.setNotNullField(CcsAcctO.class, acct);
		acct.setOrg(org);
		acct.setProductCd("000001");
		acct.setCurrBal(BigDecimal.valueOf(2000));
		acct.setAcctNbr(Long.parseLong("1101500"));
		acct.setAcctType(AccountType.E);
		acct.setContrNbr("300116110101101500");
		Date date = sf.parse("2015-08-28");
		
		return acct;
	}
	
	public static List<CcsRepaySchedule> genSchedule() throws ParseException {
		List<CcsRepaySchedule> schedules = new ArrayList<CcsRepaySchedule>();
		
		Date date = sf.parse("2015-06-01");
		
		for(int i =1 ;i<4;i++){
			CcsRepaySchedule schedule = new CcsRepaySchedule();
			schedule.setOrg(org);
			schedule.setAcctNbr(Long.parseLong("1"));
			schedule.setAcctType(AccountType.E);
			schedule.setCardNbr(logicCardNbr);
			schedule.setLoanId(Long.parseLong("1"));
			schedule.setCurrTerm(i);
			schedule.setLoanInitPrin(BigDecimal.valueOf(3000));
			schedule.setLoanInitTerm(3);
			schedule.setLoanTermPrin(BigDecimal.valueOf(1000));//本金
			schedule.setLoanTermInt(BigDecimal.valueOf(100));//利息
			schedule.setLoanTermFee(BigDecimal.valueOf(10));//手续费
			schedule.setLoanStampdutyAmt(BigDecimal.valueOf(1));//印花税
			schedule.setLoanInsuranceAmt(BigDecimal.valueOf(150));//保费
			schedule.setLoanLifeInsuAmt(BigDecimal.valueOf(0));
			schedule.setLoanPmtDueDate(DateUtils.addMonths(date, i));
			schedule.setLoanGraceDate(new Date());
			schedules.add(schedule);
		}
		
		return schedules;
	}

	public static CcsLoan genLoan() throws Exception{
		CcsLoan loan = new CcsLoan();
		loan.setOrg(org);
		loan.setLoanCode(loanCode);
		loan.setLoanInitTerm(3);
		loan.setCurrTerm(3);
		loan.setContrNbr("300116110101101500");
		loan.setInsuranceRate(new BigDecimal(0.005));
		loan.setInterestRate(new BigDecimal(0.005));
		loan.setFloatRate(new BigDecimal(0));
		loan.setUnstmtLifeInsuAmt(new BigDecimal(10));
		loan.setUnstmtPrin(new BigDecimal(1000));
		loan.setUnstmtStampdutyAmt(new BigDecimal(10));
		loan.setRefNbr(refNbr);
		loan.setLoanInitPrin(new BigDecimal(3000));
		loan.setOverdueDate(sf.parse("2015-06-01"));
		loan.setActiveDate(sf.parse("2015-06-01"));
		loan.setStampCustomInd(Indicator.N);
		loan.setLastPenaltyDate(sf.parse("2015-07-01"));
		loan.setBefExtendFinalTermFee(new BigDecimal(10));
		loan.setBefExtendFinalTermPrin(new BigDecimal(10));
		loan.setBefExtendFirstTermFee(new BigDecimal(10));
		loan.setBefExtendFirstTermPrin(new BigDecimal(10));
		loan.setBefExtendFixedFee(new BigDecimal(10));
		loan.setBefExtendFixedPmtPrin(new BigDecimal(10));
		loan.setLoanExpireDate(new Date());
		FrontBatchData.setNotNullField(CcsLoan.class, loan);
		return loan;
	}
	
	// 逾期1个月
	public static CcsLoan genCcsLoan1(){
		CcsLoan loan = new CcsLoan();
		
		loan.setLoanStatus(LoanStatus.A);
		
		Calendar c = Calendar.getInstance();
		c.add(Calendar.MONTH, -1);
		loan.setOverdueDate(c.getTime());
		
		return loan;
	}
	
	// 逾期3个月
	public static CcsLoan genCcsLoan2(){
		CcsLoan loan = new CcsLoan();
		
		loan.setLoanStatus(LoanStatus.A);
		
		Calendar c = Calendar.getInstance();
		c.add(Calendar.MONTH, -3);
		loan.setOverdueDate(c.getTime());
		
		return loan;
	}
	public static List<CcsPlan> genPlans() throws ParseException{
		List<CcsPlan> plans= new ArrayList<CcsPlan>();
		for (int i = 1; i < 4 ; i ++){
			CcsPlan plan = new CcsPlan();
			plan.setRefNbr(refNbr);
			plan.setTerm(i);
			plan.setPlanType(PlanType.Q);
			plan.setProductCd("000001");
			plan.setPlanNbr("111111");
			int j =5+i;
			plan.setPlanAddDate(sf.parse("2015-0"+j+"-01"));
			plan.setCurrBal(new BigDecimal(1030));
	
			plan.setCtdCardFee(new BigDecimal(0));
			plan.setCtdInsurance(new BigDecimal(0));
			plan.setCtdInterest(new BigDecimal(0));
			plan.setCtdLateFee(new BigDecimal(0));
			plan.setCtdNsfundFee(new BigDecimal(0));
			plan.setCtdOvrlmtFee(new BigDecimal(0));
			plan.setCtdPrincipal(new BigDecimal(0));
			plan.setCtdSvcFee(new BigDecimal(0));
			plan.setCtdTxnFee(new BigDecimal(0));
			plan.setCtdUserFee1(new BigDecimal(0));
			plan.setCtdUserFee2(new BigDecimal(0));
			plan.setCtdUserFee3(new BigDecimal(0));
			plan.setCtdUserFee4(new BigDecimal(0));
			plan.setCtdUserFee5(new BigDecimal(0));
			plan.setCtdUserFee6(new BigDecimal(0));
			plan.setCtdPenalty(new BigDecimal(0));
			plan.setCtdCompound(new BigDecimal(0));
			plan.setCtdInsurance(new BigDecimal(0));
			plan.setCtdStampdutyAmt(new BigDecimal(0));
			plan.setCtdLifeInsuAmt(new BigDecimal(0));
			plan.setCtdMulctAmt(new BigDecimal(0));
			
			plan.setPastCardFee(new BigDecimal(0));
			plan.setPastInsurance(new BigDecimal(10));
			plan.setPastInterest(new BigDecimal(10));
			plan.setPastLateFee(new BigDecimal(0));
			plan.setPastNsfundFee(new BigDecimal(0));
			plan.setPastOvrlmtFee(new BigDecimal(0));
			plan.setPastPrincipal(new BigDecimal(1000));
			plan.setPastSvcFee(new BigDecimal(0));
			plan.setPastTxnFee(new BigDecimal(0));
			plan.setPastUserFee1(new BigDecimal(0));
			plan.setPastUserFee2(new BigDecimal(0));
			plan.setPastUserFee3(new BigDecimal(0));
			plan.setPastUserFee4(new BigDecimal(0));
			plan.setPastUserFee5(new BigDecimal(0));
			plan.setPastUserFee6(new BigDecimal(0));
			plan.setPastPenalty(new BigDecimal(0));
			plan.setPastCompound(new BigDecimal(0));
			plan.setPastInsurance(new BigDecimal(10));
			plan.setPastStampdutyAmt(new BigDecimal(10));
			plan.setPastLifeInsuAmt(new BigDecimal(10));
			plan.setPastMulctAmt(new BigDecimal(50));
			plan.setTerm(i);
	
			plans.add(plan);
			
		}
		return plans;
	}
	
	public static List<BucketObject> genBucketObjectList(){
		List<BucketObject> bnps= new ArrayList<BucketObject>();
		
		bnps.add(BucketObject.pastIns);
		bnps.add(BucketObject.ctdIns);
		bnps.add(BucketObject.pastInterest);
		bnps.add(BucketObject.ctdInterest);
		bnps.add(BucketObject.pastPrincipal);
		bnps.add(BucketObject.ctdPrincipal);
		bnps.add(BucketObject.pastMulct);
		bnps.add(BucketObject.ctdMulct);
		bnps.add(BucketObject.pastStampduty);
		bnps.add(BucketObject.ctdStampduty);
		bnps.add(BucketObject.pastLifeInsuFee);
		bnps.add(BucketObject.ctdLifeInsuFee);
		return bnps;
	}
	
	public static CcsLoanReg genLoanReg(){
		CcsLoanReg loanReg = new CcsLoanReg();
		loanReg.setOrg(org);
		loanReg.setLoanType(LoanType.MCEI);
		loanReg.setLoanRegStatus(LoanRegStatus.S);
		loanReg.setAcctNbr(Long.parseLong("1"));
		loanReg.setAcctType(AccountType.E);
		loanReg.setLoanCode(loanCode);
		loanReg.setLoanInitTerm(6);
		loanReg.setRefNbr(refNbr);
		loanReg.setLogicCardNbr(logicCardNbr);
		loanReg.setCardNbr(logicCardNbr);
		loanReg.setRegisterDate(new Date());
		loanReg.setLoanInitPrin(new BigDecimal(3000));
		loanReg.setLoanFirstTermPrin(BigDecimal.ZERO);
		loanReg.setLoanFixedPmtPrin(BigDecimal.ZERO);
		loanReg.setLoanFinalTermPrin(BigDecimal.ZERO);
		loanReg.setLoanInitFee(BigDecimal.ZERO);
		loanReg.setLoanFirstTermFee(BigDecimal.ZERO);
		loanReg.setLoanFixedFee(BigDecimal.ZERO);
		loanReg.setLoanFinalTermFee(BigDecimal.ZERO);
		loanReg.setOrigAuthCode("123");
		loanReg.setOrigTransDate(new Date());
		loanReg.setOrigTxnAmt(new BigDecimal(3000));
		loanReg.setRegisterId(Long.parseLong("1"));
		loanReg.setRequestTime(new Date());
		loanReg.setLoanFeeMethod(LoanFeeMethod.E);
		loanReg.setInterestRate(new BigDecimal(0.0018));
		loanReg.setPenaltyRate(new BigDecimal(0));
		loanReg.setFloatRate(new BigDecimal(0));
		loanReg.setDueBillNo("123");
		loanReg.setInsuranceAmt(new BigDecimal(30));
		loanReg.setStampdutyAmt(new BigDecimal(30));
		loanReg.setTotLifeInsuAmt(new BigDecimal(0));
		loanReg.setInsuranceRate(new BigDecimal(0.0001));
		loanReg.setStampdutyRate(new BigDecimal(0.00001));
		loanReg.setLifeInsuFeeRate(new BigDecimal(0));
		loanReg.setLoanInsFeeMethod(LoanFeeMethod.E);
		loanReg.setStampdutyMethod(LoanFeeMethod.F);
		loanReg.setLifeInsuFeeMethod(LoanFeeMethod.E);
		loanReg.setLoanAction(LoanAction.C);
		
		
		return loanReg;
	}
	public static CcsPostingTmp genCcsPostingTmp() throws ParseException{
		CcsPostingTmp  postTmp = new CcsPostingTmp();
		postTmp.setPostAmt(new BigDecimal(1030));
		postTmp.setPostDate(sf.parse("2015-07-01"));
		postTmp.setTxnDate(sf.parse("2015-06-01"));
		
		return postTmp;
	}
	public static List<CcsTxnHst> genTxnHstList() throws Exception{
		List<CcsTxnHst>  txnHstList = new ArrayList<CcsTxnHst>();
		for(int i=1;i<4;i++){
			CcsTxnHst txn = new CcsTxnHst();
			FrontBatchData.setNotNullField(CcsTxnHst.class, txn);
			txn.setPostDate(sf.parse("2015-06-10"));
			txn.setAcctNbr(1L);
			txn.setAcctType(AccountType.E);
			txn.setRefNbr(refNbr);
			txn.setTxnCode("T904");
			txn.setPostAmt(new BigDecimal(50));
			txnHstList.add(txn);
		}
		
		return txnHstList;
	}

	public static CcsCustomerCrlmt genCcsCustomerCrlmt() {
		CcsCustomerCrlmt custLmt=new CcsCustomerCrlmt();
		custLmt.setCreditLmt(BigDecimal.valueOf(13000L));
		custLmt.setLmtCalcMethod(LimitType.H);
		custLmt.setLmtCategroy(LimitCategory.CreditLimit);
		custLmt.setOrg(org);
		return custLmt;
	}
	
}