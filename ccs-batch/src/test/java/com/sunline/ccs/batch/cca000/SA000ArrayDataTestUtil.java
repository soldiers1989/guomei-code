package com.sunline.ccs.batch.cca000;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sunline.ccs.batch.tools.MakeData;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsOrderHst;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.CcsRepayHst;
import com.sunline.ccs.infrastructure.shared.model.CcsRepaySchedule;
import com.sunline.ccs.infrastructure.shared.model.CcsSettleClaim;
import com.sunline.ccs.param.def.enums.BucketObject;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.IdType;
import com.sunline.ppy.dictionary.enums.LoanFeeMethod;
import com.sunline.ppy.dictionary.enums.LoanRegStatus;
import com.sunline.ppy.dictionary.enums.LoanStatus;
import com.sunline.ppy.dictionary.enums.PlanType;

@Component
@Transactional
public class SA000ArrayDataTestUtil {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@PersistenceContext
    private EntityManager em;
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	private SimpleDateFormat sdfTime = new SimpleDateFormat("yyyyMMddHH:mm:ss");
	
	private Long acctNbr;
	private String dueBillNo;

	 
	/**
	 * @param batchDate
	 * @param schedules
	 * <p>0_loanId, 1_acctNbr, 2_cardNbr, 3_loanInitPrin, 4_loanInitTerm</p>
	 * <p>5_currTerm, 6_loanTermPrin, 7_loanTermFee, 8_loanTermInt, 9_loanPmtDueDate</p>
	 * <p>10_loanInsuranceAmt, 11_loanStampdutyAmt, 12_loanAddedvalueTaxMax</p>
	 * @throws ParseException
	 */
	public void prepareScheduleData(Date batchDate, String[][] schedules) throws ParseException {
		Long loanTermInit = new Long(schedules[0][4]);
		for(int i=1;i<=loanTermInit;i++){
			CcsRepaySchedule schedule = new CcsRepaySchedule();
			getOneSchedule(schedules[0],schedule, i);
			logger.info("shedule,term="+i+":"+ReflectionToStringBuilder.toString(schedule, ToStringStyle.SHORT_PREFIX_STYLE));
			em.persist(schedule);
		}
	}


	/**
	 * @param schedules
	 * @param s
	 * @throws ParseException
	 */
	 
	public void getOneSchedule(String[] schedules, CcsRepaySchedule s, int term)
			throws ParseException {
		Calendar cld = Calendar.getInstance();
		cld.setTime(sdf.parse(schedules[9]));
		cld.add(Calendar.MONTH, term - 1);
		Date loanPmtDueDate = cld.getTime();
		
		s.setScheduleId(null);
		s.setLoanId(new Long(schedules[0]));
		s.setOrg("");
		s.setAcctNbr(new Long(schedules[1]));
		s.setAcctType(AccountType.E);
		s.setCardNbr(schedules[2]);
		s.setLoanInitPrin(new BigDecimal(schedules[3]));
		s.setLoanInitTerm(new Integer(schedules[4]));
		s.setCurrTerm(term);
		s.setLoanTermPrin(new BigDecimal(schedules[6]));
		s.setLoanTermFee(new BigDecimal(schedules[7]));
		s.setLoanTermInt(new BigDecimal(schedules[8]));
		s.setLoanPmtDueDate(sdf.parse(schedules[9]));
		cld.add(Calendar.DAY_OF_MONTH, 1);
		s.setLoanGraceDate(cld.getTime());
//		s.setLogicCardNbr("");
		s.setLoanInsuranceAmt(new BigDecimal(schedules[10]));
		s.setLoanStampdutyAmt(new BigDecimal(schedules[11]));
		s.setLoanLifeInsuAmt(new BigDecimal(schedules[12]));
		
		s.setLoanPmtDueDate(loanPmtDueDate);
		
	}

	 
	/**
	 * @param batchDate
	 * @param loanRegs
	 * <p>0_registerId, 1_dueBillNo, 2_LoanInitPrin, 3_loanInitTerm, 4_acctNbr </p>
	 * <p>5_acctType, 6_regStatus, 7_guarantyId, 8_refNbr,	9_B007TxnTime</p>
	 * <p>10_CardNbr, 11_validateDate</p>
	 * @throws Exception
	 */
	public void prepareLoanRegData(Date batchDate, String[][] loanRegs) throws Exception {
		Long acctNo = 1L;
		for(int i=0;i<loanRegs.length;i++){
			String[] s = loanRegs[i];
			CcsLoanReg reg = new CcsLoanReg();
			MakeData.setDefaultValue(reg);
			reg.setRegisterId(null);
			reg.setAcctNbr(acctNo);
			reg.setAcctType(AccountType.E);
			reg.setDueBillNo(s[1]);
			reg.setRegisterDate(batchDate);
			reg.setLoanRegStatus(LoanRegStatus.S);
			reg.setLoanInitPrin(new BigDecimal(s[2]));
			reg.setLoanInsFeeMethod(LoanFeeMethod.E);
			reg.setLoanInitTerm(new Integer(s[3]));
			reg.setGuarantyId(s[7]);
			reg.setRefNbr(s[8]);
			reg.setB007TxnTime(s[9]);
			reg.setCardNbr(s[10]);
			reg.setValidDate(sdfTime.parse(s[11]));
			em.persist(reg);
			logger.info(ReflectionToStringBuilder.toString(reg, ToStringStyle.SHORT_PREFIX_STYLE));
		}
	}

	/**
	 * 
	 * @param batchDate
	 * @param loans 
	 * 	<p>0_loanId, 1_dueBillNo, 2_LoanInitPrin, 3_loanInitTerm, 4_acctNbr </p>
	 *	<p>5_acctType, 6_guarantyId, 7_currTerm, 8_overDueDate, 9_refNbr </p>
	 *	<p>10_cardNbr,  11_activeDate </p>
	 * @throws Exception
	 */
	public void prepareLoanData(Date batchDate, String[][] loans) throws Exception{
		for(int i=0;i<loans.length;i++){
			String[] arr = loans[i];
			CcsLoan loan = new CcsLoan();
			MakeData.setDefaultValue(loan);
			loan.setLoanId(null);
			loan.setAcctNbr(new Long(arr[4]));
			loan.setAcctType(AccountType.E);
			loan.setLoanCode("123");
			loan.setLoanStatus(LoanStatus.A);
			loan.setDueBillNo(arr[1]);
			loan.setLoanStatus(LoanStatus.A);
			loan.setLoanInitPrin(new BigDecimal(arr[2]));
			loan.setLoanInsFeeMethod(LoanFeeMethod.E);
			loan.setLoanInitTerm(new Integer(arr[3]));
			
			Calendar cld = Calendar.getInstance();
			cld.setTime(batchDate);
			cld.add(Calendar.MONTH, loan.getLoanInitTerm());
			
			loan.setLoanExpireDate(cld.getTime());
			loan.setGuarantyId(arr[6]);
			loan.setCurrTerm(new Integer(arr[7]));
			loan.setOverdueDate(sdf.parse(arr[8]));
			loan.setRefNbr(arr[9]);
			loan.setCardNbr(arr[10]);
			loan.setActiveDate(batchDate);//sdfTime.parse(arr[11])
			em.persist(loan);
			logger.info(ReflectionToStringBuilder.toString(loan, ToStringStyle.SHORT_PREFIX_STYLE));
		}
		
	}
	
	 
	/**
	 * @param batchDate
	 * @param repays
	 * <p>0_planId, 1_bnpType, 2_repayAmt, 3_batchDate, 4_acctNbr </p>
	 * <p>5_acctType, 6_financialOrg</p>
	 * @throws ParseException
	 */
	public void prepareRepayData(Date batchDate, String[][] repays) throws ParseException {
		Long acctNo = 1L; 
		
		for(int i=0;i<repays.length;i++){
			String[] r = repays[i];
			CcsRepayHst repay = new CcsRepayHst();
			repay.setAcctNbr(new Long(r[4]));
			repay.setAcctType(AccountType.E);
			repay.setBatchDate(sdf.parse(r[3]));
			repay.setPlanId(new Long(r[0]));
			repay.setRepayAmt(new BigDecimal(r[2]));
			repay.setBnpType(BucketObject.valueOf(r[1]));
			repay.setAcqId(r[6]);
			em.persist(repay);
		}
	}
	
	 
	/**
	 * @param batchDate
	 * @param ctdPlan
	 * <p>0_TermNo, 1_CtdPrin, 2_CtdInterest, 3_CtdPenalty, 4_acctNbr</P>
	 * <p>5_acctType, 6_planType, 7_currTerm, 8_planAddDate, 9_pastPrin</P>
	 * <p>10_pastInterest, 11_pastIns, 12_refNbr</P>
	 * @throws Exception
	 */
	public void preparePlanData(Date batchDate, String[][] ctdPlan) throws Exception{
		for(int i=0;i<ctdPlan.length;i++){
			CcsPlan plan = new CcsPlan();
			MakeData.setDefaultValue(plan);
			plan.setPlanId(null);
			plan.setTerm(new Integer(ctdPlan[i][0]));
			plan.setCtdPrincipal(new BigDecimal(ctdPlan[i][1]));
			plan.setCtdInterest(new BigDecimal(ctdPlan[i][2]));
			plan.setCtdPenalty(new BigDecimal(ctdPlan[i][3]));
			plan.setAcctNbr(new Long(ctdPlan[i][4]));
			plan.setAcctType(AccountType.valueOf(ctdPlan[i][5]));
			plan.setPlanType(PlanType.valueOf(ctdPlan[i][6]));
			plan.setTerm(new Integer(ctdPlan[i][7]));
//			Date planAddDate = batchDate;
			plan.setPlanAddDate(sdf.parse(ctdPlan[i][8]));
			plan.setPastPrincipal(new BigDecimal(ctdPlan[i][9]));
			plan.setPastInterest(new BigDecimal(ctdPlan[i][10]));
			plan.setPastInsurance(new BigDecimal(ctdPlan[i][11]));
			plan.setRefNbr(ctdPlan[i][12]);
			
			if(i == ctdPlan.length-1){
				
			}
			em.persist(plan);
		}
	}
	/**
	 * @param orderHsts  
	 * <p>0_dueBillNo, 1_businessDate, 2_oriOrderId, 3_optDateTime, 4_orderId </p>
	 */
	public void prepareOrderHstData(Date batchDate, String[][] orderHsts) throws ParseException, IllegalAccessException {
		
		for(int i=0;i<orderHsts.length;i++){
			String[] orderHst = orderHsts[i];
			CcsOrderHst o = new CcsOrderHst();
			MakeData.setDefaultValue(o);
			o.setOrderId(new Long(orderHst[4]));
			o.setDueBillNo(orderHst[0]);
			o.setBusinessDate(sdf.parse(orderHst[1]));
			o.setOriOrderId(orderHst[2]==null?null:new Long(orderHst[2]));
			o.setOptDatetime(sdf.parse(orderHst[3]));
			em.persist(o);
		}
		
	}
	

	 
	public void prepareAcctData(Date batchDate, String[][] accts) throws IllegalAccessException {
		CcsAcct a = new CcsAcct();
		MakeData.setDefaultValue(a);
		a.setAcctNbr(new Long("1"));
		a.setAcctType(AccountType.E);
		a.setCustId(new Long(1));
		em.persist(a);
	}
	
	 
	/**
	 * @param customers
	 * <p>name, idNo, idType </p>
	 */
	public void prepareCustData(String[][] customers) throws IllegalAccessException {
		for(int i=0;i<customers.length;i++){
			CcsCustomer c = new CcsCustomer();
			MakeData.setDefaultValue(c);
			c.setCustId(null);
			c.setName(customers[i][0]);
			c.setIdNo(customers[i][1]);
			c.setIdType(IdType.I);
			em.persist(c);
		}
	}



	/**
	 * claimSettles: 
	 * <p>0_loanId, 1_claimAmt, 2_settleDate, 3_dueBillNo</p>
	 * @param batchDate
	 * @param claimSettles
	 */
	public void prepareSettleData(Date batchDate, String[][] claimSettles) throws ParseException, IllegalAccessException {
		
		for(int i=0;i<claimSettles.length;i++){
			String[] c = claimSettles[i];
			CcsSettleClaim t = new CcsSettleClaim();
			MakeData.setDefaultValue(t);
			t.setLoanId(new Long(c[0]));
			t.setSettleAmt(new BigDecimal(c[1]));
			t.setSettleDate(sdf.parse(c[2]));
			t.setDueBillNo(c[3]);
			em.persist(t);
		}
	}
	public Long getAcctNbr() {
		return acctNbr;
	}


	public void setAcctNbr(Long acctNbr) {
		this.acctNbr = acctNbr;
	}


	public String getDueBillNo() {
		return dueBillNo;
	}


	public void setDueBillNo(String dueBillNo) {
		this.dueBillNo = dueBillNo;
	}


}
