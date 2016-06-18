package com.sunline.ccs.batch.cca000;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SA000ArrayData {

	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	private static SA000ArrayDataTestUtil u = new SA000ArrayDataTestUtil();
	private static SA000ParamDataUtil param = new SA000ParamDataUtil();
	public static void prepareParam(){
		param.prepareParam();
	}
	public static void genOrderHstData(Date batchDate) throws ParseException,
			IllegalAccessException {
		/*
		 * DUE_BILL_NO, BUSINESS_DATE, ORI_ORDER_ID, OPT_DATETIME,  Order_id
		 */
		String[][] orderHsts = {
				{"10001", "20150820", null, "20150820", "1"}	
		};
		u.prepareOrderHstData(batchDate, orderHsts);
	}

	
	public static void genScheduleData(Date batchDate) throws ParseException {
		/*
		 	0_loanId, 1_acctNbr, 2_cardNbr, 3_loanInitPrin, 4_loanInitTerm
			5_currTerm, 6_loanTermPrin, 7_loanTermFee, 8_loanTermInt, 9_loanPmtDueDate
			10_loanInsuranceAmt, 11_loanStampdutyAmt, 12_loanAddedvalueTaxMax
		 */
		String[][] schedules = {
				{	
					"1","1","123456789012","1000.00","10",
					"1","100.00","1.00","5.00","20150607",
					"2.00","0.10","0.50"
				},
				{
					"1","1","123456789012","1000.00","10",
					"2","100.00","1.00","5.00","20150707",
					"2.00","0.10","0.50"
				},
				{
					"1","1","123456789012","1000.00","10",
					"3","100.00","1.00","5.00","20150807",
					"2.00","0.10","0.50"
				}					 
		};
		u.prepareScheduleData(batchDate, schedules);
	}

	public static void genClaimData(Date batchDate) throws ParseException,
			IllegalAccessException {
		/*
		 * loanId,claimAmt,settleDate, dueBillNo
		 */
		String[][] claimSettles = {
				
				{"1","1234.00","20150820","100001"}
		};
		u.prepareSettleData(batchDate, claimSettles);
	}

	public static void genRepayHstData(Date batchDate) throws ParseException {
		/*
		 * planId, bnpType, repayAmt, batchDate, acctNbr, acctType, financialOrg
		 */
		String[][] repays = {
				{"1","ctdPrincipal","10.00","20150707","1","E","yg001"}
				,{"1","ctdInterest","5.00",sdf.format(batchDate),"1","E","yg001"}
				,{"1","ctdPenalty","4.00",sdf.format(batchDate),"1","E","yg001"}
				,{"1","pastSvcFee","100.00",sdf.format(batchDate),"1","E","yg001"}
				,{"1","ctdIns","2.00",sdf.format(batchDate),"1","E","yg001"}
		};
		u.prepareRepayData(batchDate, repays);
	}

	public static void genPlanData(Date batchDate) throws Exception {
		/*
		 * TermNo, CtdPrin, CtdInterest, CtdPenalty, acctNbr, 
		 * acctType, planType, currTerm, planAddDate, pastPrin, 
		 * pastInterest, pastIns, refNbr
		 */
		String[][] ctdPlan = {
				{
					"1","1000.00","5.00","9.00","1", 
					"E","I", "1", "20150507", "1000.00"
					,"5.00", "9.00", "1"}
				,{
					"2","1000.00","5.00","6.00","1", 
					"E","I", "2", "20150607", "1000.00"
					,"5.00","9.00", "1"}
				,{
					"3","1000.00","5.00","0.00","1", 
					"E","I", "3", "20150707", "1000.00"
					,"5.00","9.00", "1"}
		};
		u.preparePlanData(batchDate, ctdPlan);
	}

	public static void genLoanRegData(Date batchDate) throws Exception {
		/*
		 * registerId, dueBillNo, LoanInitPrin, loanInitTerm, acctNbr, 
		 * acctType, regStatus, guarantyId, refNbr,	B007TxnTime,
		 * CardNbr, validateDate
		 */
		String[][] loanRegs = {
				{ 
					"1", "10001", "1000.00", "12", "1", 
					"E", "S", "1000001", "1", "2015050721",
					"123456789012", "2015050721:23:30"
					}
//					,{ "2", "10002", "2000.00", "12", "2", "E", "S", "1000002"}
		};
		u.prepareLoanRegData(batchDate, loanRegs);
	}

	public static void genCustData(Date batchDate) throws IllegalAccessException,
			Exception {
		String[][] customers = {
				{"www","410033202010100011","I", "1"}
		};
		u.prepareCustData(customers);
	}

	/**
	 * @param batchDate
	 * @throws Exception
	 */
	public static void genLoanData(Date batchDate) throws Exception {
		/*
		 * loanId, dueBillNo, LoanInitPrin, loanInitTerm, acctNbr, 
		 * acctType, guarantyId, currTerm, overDueDate, refNbr
		 * cardNbr,  activeDate
		 */
		String[][] loans = {
				{ 	"1", "10001", "1000.00", "12", "1", 
					"E", "1000001", "3", "20150608", "1",
					"123456789012","20150820"	}
		};
		u.prepareLoanData(batchDate, loans);
	}

	public static void genAcctData(Date batchDate) throws IllegalAccessException {
		String[][] accts = {
				{}
		};
		u.prepareAcctData(batchDate, accts);
	}


}
