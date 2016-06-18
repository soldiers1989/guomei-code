package com.sunline.ccs.facility;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.CcsRepaySchedule;
import com.sunline.ccs.infrastructure.shared.model.QCcsPlan;
import com.sunline.ccs.infrastructure.shared.model.QCcsRepaySchedule;
import com.sunline.ccs.loan.McLoanProvideImpl.EarlySettleMethodimple;
import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.enums.LoanFeeDefStatus;
import com.sunline.ccs.param.def.enums.PrepaymentFeeMethod;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.api.CcServProConstants;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanAction;
import com.sunline.ppy.dictionary.enums.PlanType;
import com.sunline.ppy.dictionary.exception.ProcessException;

@Component
public class MicroCreditRescheduleUtils {
	public Logger logger = LoggerFactory.getLogger(getClass());
	
	private final static int YEAR_MONTHS = 12;
	public final static int YEAR_DAYS = 360;
	/**
	 * 每月还款额不变，期数减少
	 */
	public static final String SHORTEDTYPE_T = "T";
	/**
	 * 期数不变，每月还款额降低
	 */
	public static final String SHORTEDTYPE_A = "A";
	/**
	 * 指定缩期后期数，调整每月还款额
	 */
	public static final String SHORTEDTYPE_S = "S";
	/**
	 * 指定每月还款额，调整期数（未实现）
	 */
	public static final String SHORTEDTYPE_P = "P";
	
	@Autowired
	private Card2ProdctAcctFacility productFacility;
	@Autowired
	private UnifiedParameterFacility parameterFacility;
	@PersistenceContext
	private EntityManager em;
	
	//初始化跳跃账单日对应关系  如 交易日期1月29日，允许跳跃，则账单日为2，下一账单日为下下个月的2号
    private static Map<Integer,Integer> cycleDayMap;
	static {
		cycleDayMap = new HashMap<Integer, Integer>();
		cycleDayMap.put(29, 2);
		cycleDayMap.put(30, 3);
		cycleDayMap.put(31, 4);
	}
	
	/**
	 * 重新生成等额本息展期后的还款分配
	 * 
	 * @param loanReg
	 * @param loan
	 * @param nextStmtDate
	 * @param origNextSchedule
	 * @param loanFeeDef
	 * @return
	 */
	public List<CcsRepaySchedule> genRescheduleForMCEI(CcsLoanReg loanReg, CcsLoan loan, Date nextStmtDate, CcsRepaySchedule origNextSchedule, LoanFeeDef loanFeeDef) {
		List<CcsRepaySchedule> reSchedules = new ArrayList<CcsRepaySchedule>();
		
		// 展期当期，按原计划收取
		CcsRepaySchedule s = copySchedule(origNextSchedule);
		reSchedules.add(s);
		
		BigDecimal monthIntRt = this.calcMonthIntRt(loan.getInterestRate(),loan.getFloatRate());
		// 重新分配本金 = 未还本金 - 展期当期本金
		BigDecimal rePrin = loan.getUnstmtPrin().subtract(origNextSchedule.getLoanTermPrin());
		// 重新分配期数 = 展期后期数 - 当期期数 - 1
		Integer reTerm = loanReg.getExtendTerm()-loan.getCurrTerm()-1;
		
		// 每期应还金额
		BigDecimal monthPmt = this.calcMonthPmtForMCEI(rePrin, monthIntRt, reTerm);
		BigDecimal currMonthPrin;
		BigDecimal currMonthInt;
		BigDecimal pmtPrin = BigDecimal.ZERO;
		Date loanPmtDueDate;
		
		for (int i = 1; i <= reTerm; i++) {
			currMonthPrin = this.calcCurrMonthPrinForMCEI(rePrin, monthIntRt, reTerm, i); // 当期应还本金
			pmtPrin = pmtPrin.add(currMonthPrin); // 每期本金累计
			currMonthInt = monthPmt.subtract(currMonthPrin); // 当期利息
			loanPmtDueDate = this.getLoanPmtDueDate(DateUtils.addMonths(nextStmtDate, 1), loanFeeDef, i);

			// 末期补差
			if(i == reTerm){
				BigDecimal fixPrin = rePrin.subtract(pmtPrin); // 本金补差
				currMonthPrin = currMonthPrin.add(fixPrin); // 本金修正
				BigDecimal fixInt = fixPrin.multiply(monthIntRt).multiply(new BigDecimal(i)).setScale(2, RoundingMode.HALF_UP); // 利息补差
				currMonthInt = currMonthInt.add(fixInt); // 利息修正
			}
			//缩期的金额计算暂不实现
			BigDecimal currMonthInsurance=null;
			BigDecimal currMonthStampduty=null;
			BigDecimal currMonthAddValTAX=null;
			BigDecimal currMonthFee=null;
			BigDecimal currMonthSVCFee=null;
			// 代收服务费
			BigDecimal currMonthReplaceSvcFee = null;
			BigDecimal currMonthPrepayPkg = null;
			reSchedules.add(this.generateRepaySchedule(loanReg, loan.getLoanId(), loan.getCurrTerm()+i+1, currMonthPrin,currMonthFee,currMonthSVCFee, currMonthInt,currMonthInsurance,currMonthStampduty,currMonthAddValTAX,currMonthReplaceSvcFee,currMonthPrepayPkg, loanPmtDueDate));
		}
		return reSchedules;
	}


	/**
	 * 重新生成等额本金展期后的还款分配
	 * 
	 * @param loanReg
	 * @param loan
	 * @param nextStmtDate
	 * @param origNextSchedule
	 * @param loanFeeDef
	 * @return
	 */
	public List<CcsRepaySchedule> genRescheduleForMCEP(CcsLoanReg loanReg, CcsLoan loan, Date nextStmtDate, CcsRepaySchedule origNextSchedule, LoanFeeDef loanFeeDef) {
		List<CcsRepaySchedule> reSchedules = new ArrayList<CcsRepaySchedule>();
		
		// 展期当期，按原计划收取
		CcsRepaySchedule s = copySchedule(origNextSchedule);
		reSchedules.add(s);

		
		BigDecimal monthIntRt = this.calcMonthIntRt(loanReg.getInterestRate(),loan.getFloatRate());
		// 重新分配本金 = 未还本金 - 展期当期本金
		BigDecimal rePrin = loan.getUnstmtPrin().subtract(origNextSchedule.getLoanTermPrin());
		// 重新分配期数 = 展期后期数 - 当期期数 - 1
		Integer reTerm = loanReg.getExtendTerm()-loan.getCurrTerm()-1;
		
		// 每期本金
		BigDecimal monthPrin = rePrin.divide(new BigDecimal(reTerm), 2, RoundingMode.HALF_UP);
		BigDecimal currMonthInt;
		Date loanPmtDueDate;
		
		for (int i = 1; i <= reTerm; i++) {
			currMonthInt = this.calcMonthIntForMCEP(rePrin, monthIntRt, reTerm, i); // 当期利息
			loanPmtDueDate = this.getLoanPmtDueDate(DateUtils.addMonths(nextStmtDate, 1), loanFeeDef, i);
			
			// 末期补差
			if(i == reTerm){
				BigDecimal fixPrin = rePrin.subtract(monthPrin.multiply(new BigDecimal(i))); // 本金补差
				monthPrin = monthPrin.add(fixPrin); // 本金修正
				BigDecimal fixInt = fixPrin.multiply(monthIntRt).multiply(new BigDecimal(i)).setScale(2, RoundingMode.HALF_UP); // 利息补差
				currMonthInt = currMonthInt.add(fixInt); // 利息修正
			}
			//缩期的金额计算暂不实现
			BigDecimal currMonthInsurance=null;
			BigDecimal currMonthStampduty=null;
			BigDecimal currMonthAddValTAX=null;
			BigDecimal currMonthFee=null;
			BigDecimal currMonthSVCFee=null;
			
			// 代收服务费
			BigDecimal currMonthReplaceSvcFee = null;
			BigDecimal currMonthPrepayPkg = null;
			reSchedules.add(this.generateRepaySchedule(loanReg, loan.getLoanId(), loan.getCurrTerm()+i+1, monthPrin,currMonthFee,currMonthSVCFee, currMonthInt,currMonthInsurance,currMonthStampduty,currMonthAddValTAX, currMonthReplaceSvcFee,currMonthPrepayPkg,loanPmtDueDate));
		}
		return reSchedules;
	}
	
	
	/**
	 * 重新生成等额本息缩期后的还款分配
	 * 
	 * @param loanReg
	 * @param loan
	 * @param nextStmtDate
	 * @param origNextSchedule
	 * @param loanFeeDef
	 * @return
	 */
	public List<CcsRepaySchedule> genShortedForMCEI(CcsLoanReg loanReg, CcsLoan loan, Date nextStmtDate, CcsRepaySchedule origNextSchedule, LoanFeeDef loanFeeDef) {
		List<CcsRepaySchedule> reSchedules = new ArrayList<CcsRepaySchedule>();
		
		// 缩期当期，按原计划收取，提前还款额合并在缩期当期
		CcsRepaySchedule s = copySchedule(origNextSchedule);
		s.setLoanTermPrin(origNextSchedule.getLoanTermPrin().add(loanReg.getShortedPmtDue()));
		reSchedules.add(s);
		
		
		// 月利率
		BigDecimal monthIntRt = this.calcMonthIntRt(loanReg.getInterestRate(),loan.getFloatRate());
		// 重新分配本金 = 未还本金 - 偿还本金 - 缩期当期本金
		BigDecimal rePrin = loan.getUnstmtPrin().subtract(loanReg.getShortedPmtDue()).subtract(origNextSchedule.getLoanTermPrin());
		// 重新分配期数
		Integer reTerm = null;
		Integer shortedTerm = null;
		
		BigDecimal monthPmt = null;
		BigDecimal currMonthPrin;
		BigDecimal currMonthInt;
		BigDecimal pmtPrin = BigDecimal.ZERO;
		Date loanPmtDueDate;
		
		// 期数不变，每月还款额降低
		if(SHORTEDTYPE_A.equals(loanReg.getShortedType())){
			shortedTerm = loan.getRemainTerm();
			reTerm = shortedTerm -1;
			monthPmt = this.calcMonthPmtForMCEI(rePrin, monthIntRt, reTerm);
		}
		// 每月还款额不变，期数减少
		else if(SHORTEDTYPE_T.equals(loanReg.getShortedType())){
			BigDecimal origPmt = origNextSchedule.getLoanTermPrin().add(origNextSchedule.getLoanTermInt());
			ShortedResult rt = this.calcShortedResult(loan, loanReg.getShortedPmtDue(), origPmt, origNextSchedule.getLoanTermPrin(), new BigDecimal(999999999), loan.getLoanInitTerm(), origPmt);
			
			shortedTerm = loan.getCurrTerm()+rt.getTerm();
			reTerm = rt.getTerm();
			monthPmt = rt.getMonthPmt();
		}
		// 指定缩期后期数，调整每月还款额
		else if(SHORTEDTYPE_S.equals(loanReg.getShortedType())){
			shortedTerm = loanReg.getShortedTerm();
			reTerm = shortedTerm-loan.getCurrTerm()-1;
			monthPmt = this.calcMonthPmtForMCEI(rePrin, monthIntRt, reTerm);
		}
		
		for (int i = 1; i <= reTerm; i++) {
			currMonthPrin = this.calcCurrMonthPrinForMCEI(rePrin, monthIntRt, reTerm, i); // 当期应还本金
			pmtPrin = pmtPrin.add(currMonthPrin); // 本金累计
			currMonthInt = monthPmt.subtract(currMonthPrin); // 当期利息
			loanPmtDueDate = this.getLoanPmtDueDate(DateUtils.addMonths(nextStmtDate, 1), loanFeeDef, i);
			
			// 末期补差
			if(i == reTerm){
				BigDecimal fixPrin = rePrin.subtract(pmtPrin); // 本金补差
				currMonthPrin = currMonthPrin.add(fixPrin); // 本金修正
				BigDecimal fixInt = fixPrin.multiply(monthIntRt).multiply(new BigDecimal(i)).setScale(2, RoundingMode.HALF_UP); // 利息补差
				currMonthInt = currMonthInt.add(fixInt); // 利息修正
				
			}
			//缩期的金额计算暂不实现
			BigDecimal currMonthInsurance=null;
			BigDecimal currMonthStampduty=null;
			BigDecimal currMonthAddValTAX=null;
			BigDecimal currMonthFee=null;
			BigDecimal currMonthSVCFee=null;
			
			// 代收服务费
			BigDecimal currMonthReplaceSvcFee = null;
			BigDecimal currMonthPrepayPkg = null;
			reSchedules.add(this.generateRepaySchedule(loanReg, loan.getLoanId(), loan.getCurrTerm()+i+1, currMonthPrin,currMonthFee,currMonthSVCFee, currMonthInt,currMonthInsurance,currMonthStampduty,currMonthAddValTAX,currMonthReplaceSvcFee,currMonthPrepayPkg, loanPmtDueDate));
		}
		return reSchedules;
	}
	
	
	/**
	 * 重新生成等额本金缩期后的还款分配
	 * 
	 * @param loanReg
	 * @param loan
	 * @param nextStmtDate
	 * @param origNextSchedule
	 * @param loanFeeDef
	 * @return
	 */
	public List<CcsRepaySchedule> genShortedForMCEP(CcsLoanReg loanReg, CcsLoan loan, Date nextStmtDate, CcsRepaySchedule origNextSchedule, LoanFeeDef loanFeeDef) {
		List<CcsRepaySchedule> reSchedules = new ArrayList<CcsRepaySchedule>();
		
		// 缩期当期，按原计划收取，提前还款额合并在缩期当期
		CcsRepaySchedule s = copySchedule(origNextSchedule);
		s.setLoanTermPrin(origNextSchedule.getLoanTermPrin().add(loanReg.getShortedPmtDue()));
		reSchedules.add(s);
		
		// 月利率
		BigDecimal monthIntRt = this.calcMonthIntRt(loanReg.getInterestRate(),loan.getFloatRate());
		// 重新分配本金 = 未还本金 - 偿还本金 - 缩期当期本金
		BigDecimal rePrin = loan.getUnstmtPrin().subtract(loanReg.getShortedPmtDue()).subtract(origNextSchedule.getLoanTermPrin());
		// 重新分配期数
		Integer reTerm = null;
		Integer shortedTerm = null;
		
		BigDecimal currMonthInt;
		Date loanPmtDueDate;
		
		// 期数不变，每月还款额降低
		if(SHORTEDTYPE_A.equals(loanReg.getShortedType())){
			shortedTerm = loan.getRemainTerm();
			reTerm = shortedTerm-1;
		}
		// 每月还款额不变，期数减少
		else if(SHORTEDTYPE_T.equals(loanReg.getShortedType())){
			// 试算可以缩掉几期
			int t = loanReg.getShortedPmtDue().divide(origNextSchedule.getLoanTermPrin(), 0, RoundingMode.DOWN).intValue();
			
			shortedTerm = loan.getRemainTerm()-t;
			reTerm = loan.getRemainTerm()-t-1;
		}
		// 指定缩期后期数，调整每月还款额
		else if(SHORTEDTYPE_S.equals(loanReg.getShortedType())){
			shortedTerm = loanReg.getShortedTerm();
			reTerm = shortedTerm-loan.getCurrTerm()-1;
		}
		BigDecimal monthPrin = rePrin.divide(new BigDecimal(reTerm), 2, RoundingMode.HALF_UP);
		
		for (int i = 1; i <= reTerm; i++) {
			currMonthInt = this.calcMonthIntForMCEP(rePrin, monthIntRt, reTerm, i); // 当期利息
			loanPmtDueDate = this.getLoanPmtDueDate(DateUtils.addMonths(nextStmtDate, 1), loanFeeDef, i);
		
			// 末期补差
			if(i == reTerm){
				BigDecimal fixPrin = rePrin.subtract(monthPrin.multiply(new BigDecimal(i))); // 本金补差
				monthPrin = monthPrin.add(fixPrin); // 本金修正
				BigDecimal fixInt = fixPrin.multiply(monthIntRt).multiply(new BigDecimal(i)).setScale(2, RoundingMode.HALF_UP); // 利息补差
				currMonthInt = currMonthInt.add(fixInt); // 利息修正
			}
			
			//缩期的金额计算暂不实现
			BigDecimal currMonthInsurance=null;
			BigDecimal currMonthStampduty=null;
			BigDecimal currMonthAddValTAX=null;
			BigDecimal currMonthFee=null;
			BigDecimal currMonthSVCFee=null;
			
			// 代收服务费
			BigDecimal currMonthReplaceSvcFee = null;
			BigDecimal currMonthPrepayPkg = null;
			reSchedules.add(this.generateRepaySchedule(loanReg, loan.getLoanId(), loan.getCurrTerm()+i+1, monthPrin,currMonthFee,currMonthSVCFee, currMonthInt,currMonthInsurance,currMonthStampduty,currMonthAddValTAX,currMonthReplaceSvcFee,currMonthPrepayPkg, loanPmtDueDate));
		}
		
		return reSchedules;
	}
	
	
	/**
	 * 重新生成等额本息(金)提前还款后的还款分配
	 * 
	 * @param loanReg
	 * @param loan
	 * @param nextStmtDate
	 * @param origNextSchedule
	 * @return
	 */
	public CcsRepaySchedule genPrepayment(CcsLoanReg loanReg, CcsLoan loan, Date nextStmtDate, CcsRepaySchedule origNextSchedule) {
		BigDecimal monthIntRt = this.calcMonthIntRt(loanReg.getInterestRate(),loan.getFloatRate());
		//剩余利息重算
		BigDecimal monthInt = (loan.getUnstmtPrin().subtract(origNextSchedule.getLoanTermPrin())).multiply(monthIntRt).setScale(2, RoundingMode.HALF_UP); // 剩余应还本金 *月利率
		//还款当期利息不变
		monthInt = monthInt.add(origNextSchedule.getLoanTermInt());
		BigDecimal currMonthInsurance=null;
		BigDecimal currMonthStampduty=null;
		BigDecimal currMonthAddValTAX=null;
		BigDecimal currMonthFee=null;
		BigDecimal currMonthSVCFee=null;
		
		// 代收服务费
		BigDecimal currMonthReplaceSvcFee = null;
		// 灵活还款包费
		BigDecimal currMonthPrepayPkg = null;
		return this.generateRepaySchedule(loanReg, loan.getLoanId(), loan.getCurrTerm()+1, loan.getUnstmtPrin(), monthInt,currMonthFee,currMonthSVCFee,currMonthInsurance,currMonthStampduty,currMonthAddValTAX,currMonthReplaceSvcFee,currMonthPrepayPkg, nextStmtDate);
	}
	
	/**
	 * * 重新生成等额本息(金)提前还款后的还款分配
	 * 生成还款计划为当日，保费，印花税，寿险计划包费，重新计算
	 * @param loanReg
	 * @param loan
	 * @param days 利息重算的天数
	 * @param nextStmtDate
	 * @param isHesitation 是否是犹豫期
	 * @param ccsRepaySchedule上一期的还款计划或者上上一期的还款计划
	 * @return
	 */
	public CcsRepaySchedule genPrepaymentRetry(CcsLoanReg loanReg, CcsLoan loan, int days,Date nextStmtDate,boolean isHesitation, CcsRepaySchedule ccsRepaySchedule,LoanFeeDef loanFeeDef) {
		
		// 判断是否犹豫期内
		if(isHesitation){
			return this.generateRepaySchedule(loanReg, loan.getLoanId(), loan.getCurrTerm()+1, loan.getUnstmtPrin(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,BigDecimal.ZERO, nextStmtDate);
		}
		
		BigDecimal monthInt = BigDecimal.ZERO;
		BigDecimal currMonthInsurance = BigDecimal.ZERO;
		
		
		//剩余利息重算，还款计划中的利息*天数/30
		if(days >= 30){
			monthInt = ccsRepaySchedule.getLoanTermInt();
		}else{
			monthInt = ccsRepaySchedule.getLoanTermInt().multiply(new BigDecimal(days)).divide(new BigDecimal(30), 2, RoundingMode.HALF_UP); // 剩余应还本金 *月利率
		}
		BigDecimal currMonthStampduty=BigDecimal.ZERO;
		if(loan.getStampCustomInd().equals(Indicator.Y)){
			currMonthStampduty=loan.getUnstmtStampdutyAmt();
		}
		
		BigDecimal currMonthFee = ccsRepaySchedule!=null ? ccsRepaySchedule.getLoanTermFee() : BigDecimal.ZERO;
		BigDecimal currMonthSVCFee = ccsRepaySchedule!=null ? ccsRepaySchedule.getLoanSvcFee() : BigDecimal.ZERO;
		
		// 代收服务费
		BigDecimal currMonthReplaceSvcFee = ccsRepaySchedule!=null ? ccsRepaySchedule.getLoanReplaceSvcFee() : BigDecimal.ZERO;
		BigDecimal currMonthPrepayPkg = ccsRepaySchedule!=null?ccsRepaySchedule.getLoanPrepayPkgAmt():BigDecimal.ZERO;
		BigDecimal currMonthLifeInsuFee = ccsRepaySchedule!=null ? ccsRepaySchedule.getLoanLifeInsuAmt() : BigDecimal.ZERO;
		//如果是理赔结清，后面的保费不在收取
		if(loanReg.getLoanAction() != LoanAction.C){
			if(loanFeeDef.insCalcMethod == PrepaymentFeeMethod.A){
				BigDecimal currTolMonthInsurance = ccsRepaySchedule==null ? BigDecimal.ZERO: ccsRepaySchedule.getLoanInsuranceAmt() ;
				if(days >= 30){
					currMonthInsurance = currTolMonthInsurance;
				}else{
					currMonthInsurance = currTolMonthInsurance.multiply(new BigDecimal(days)).divide(new BigDecimal(30), 2, RoundingMode.HALF_UP); //按天折算
				}
			}else{
				//保费月利率
				BigDecimal monthInsRt = loan.getInsuranceRate().divide(new BigDecimal(loan.getLoanInitTerm()), 20, RoundingMode.HALF_UP);;
				//剩余保费重算,剩余本金*月利率*天数/30
				currMonthInsurance = loan.getLoanInitPrin().multiply(monthInsRt).multiply(new BigDecimal(days)).divide(new BigDecimal(30),2,RoundingMode.HALF_UP); // 总本金 *月利率

			}
		}
		
		return this.generateRepaySchedule(loanReg, loan.getLoanId(), loan.getCurrTerm()+1, loan.getUnstmtPrin(),currMonthFee,currMonthSVCFee, monthInt,currMonthInsurance,currMonthStampduty,currMonthLifeInsuFee,currMonthReplaceSvcFee,currMonthPrepayPkg,nextStmtDate);
	}
	
	/**
	 * * 等额本息(金)退货结清后的重新生成还款计划
	 * 根据退货金额是否能全额结清判断生成一个或两个还款计划，利息截至当天，保费，印花税，贷款服务费，分期手续费，代收服务费，寿险计划包费等，重新计算
	 * @param loanReg
	 * @param loan
	 * @param days 利息重算的天数
	 * @param nextStmtDate
	 * @param isHesitation 是否是犹豫期
	 * @param ccsRepaySchedule上一期的还款计划或者上上一期的还款计划
	 * @return
	 */
	public List<CcsRepaySchedule> genRefundRetry(CcsAcct acct, CcsLoanReg loanReg, CcsLoan loan, int days,Date batDate,boolean isHesitation, CcsRepaySchedule ccsRepaySchedule,LoanFeeDef loanFeeDef) {
		
		List<CcsRepaySchedule> newScheduleList = new ArrayList<CcsRepaySchedule>();
		// 获取账户上的账单日
		Date nextStmtDate = acct.getNextStmtDate();
		// 退货金额
		BigDecimal refundAmt = loanReg.getAdDebitAmt();
		// 判断是否犹豫期内
		if(isHesitation){
			// 当天为账单日时，能全部结清,则生成当日账单日schedule全部结出,如果不能全部结清,只结出与退货金额相等部分,在当天转入,剩余部分在下个账单日转入
			if(batDate.compareTo(nextStmtDate) == 0){
				// 全部还清,只生成当日账单日schedule
				if (refundAmt.compareTo(loan.getUnstmtPrin()) >= 0){
					CcsRepaySchedule firstSchedule =  this.generateRepaySchedule(loanReg, loan.getLoanId(), loan.getCurrTerm()+1, loan.getUnstmtPrin(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,BigDecimal.ZERO, nextStmtDate);
					newScheduleList.add(firstSchedule);
				}else{
					CcsRepaySchedule firstSchedule =  this.generateRepaySchedule(loanReg, loan.getLoanId(), loan.getCurrTerm()+1, refundAmt, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,BigDecimal.ZERO, nextStmtDate);
					// 剩余部分放在下一个schedule中，本金部分需要计算从当日到下一还款日的利息
					Date nextSchedulePmtDay = this.getLoanPmtDueDate(nextStmtDate,loanFeeDef,2,acct.getCycleDay());
					int	intCalDays = DateUtils.getIntervalDays(batDate,nextSchedulePmtDay);
					BigDecimal dayIntRt = this.calcDayIntRt(loan.getInterestRate());
					BigDecimal nextSchedulePrin = loan.getUnstmtPrin().subtract(refundAmt);
					BigDecimal nextScheduleInt = nextSchedulePrin.multiply(dayIntRt).multiply(new BigDecimal(intCalDays)).setScale(2, RoundingMode.HALF_UP);
					CcsRepaySchedule nextSchedule = this.generateRepaySchedule(loanReg, loan.getLoanId(), loan.getCurrTerm()+2, nextSchedulePrin, BigDecimal.ZERO, BigDecimal.ZERO, nextScheduleInt, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,BigDecimal.ZERO, nextSchedulePmtDay);
					newScheduleList.add(firstSchedule);
					newScheduleList.add(nextSchedule);
				}
			}else{
				// 若当日不为账单日，则直接全部结出,还款日为下个账单日；
				Date nextSchedulePmtDay = null;
				// 如果当天日期小于下个账单日,则生成下个账单日还款计划,全部结出
				if(batDate.compareTo(nextStmtDate) < 0){
					nextSchedulePmtDay = nextStmtDate;
				}else{
					// 否则，重算下个账单日，生成1个还款计划，全部结出
					for(int i = 2;;i++){
						nextSchedulePmtDay = this.getLoanPmtDueDate(nextStmtDate,loanFeeDef,i,acct.getCycleDay());
						if (batDate.compareTo(nextSchedulePmtDay) < 0){
							break;
						}
					}
				}
				CcsRepaySchedule firstSchedule =  this.generateRepaySchedule(loanReg, loan.getLoanId(), loan.getCurrTerm()+1, loan.getUnstmtPrin(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,BigDecimal.ZERO, nextSchedulePmtDay);
				newScheduleList.add(firstSchedule);
			}
			return newScheduleList;
		}
		
		BigDecimal monthInt = BigDecimal.ZERO;
		//剩余利息重算，还款计划中的利息*天数/30
		if(days >= 30){
			monthInt = ccsRepaySchedule.getLoanTermInt();
		}else{
			monthInt = ccsRepaySchedule.getLoanTermInt().multiply(new BigDecimal(days)).divide(new BigDecimal(30), 2, RoundingMode.HALF_UP); // 剩余应还本金 *月利率
		}
		BigDecimal currMonthStampduty=BigDecimal.ZERO;
		if(loan.getStampCustomInd().equals(Indicator.Y)){
			currMonthStampduty=loan.getUnstmtStampdutyAmt();
		}
		
		BigDecimal currMonthFee = ccsRepaySchedule!=null ? ccsRepaySchedule.getLoanTermFee() : BigDecimal.ZERO;
		BigDecimal currMonthSVCFee = ccsRepaySchedule!=null ? ccsRepaySchedule.getLoanSvcFee() : BigDecimal.ZERO;
		
		BigDecimal currMonthLifeInsuFee = ccsRepaySchedule!=null ? ccsRepaySchedule.getLoanLifeInsuAmt() : BigDecimal.ZERO;
		// 代收服务费
		BigDecimal currMonthReplaceSvcFee = ccsRepaySchedule!=null ? ccsRepaySchedule.getLoanReplaceSvcFee() : BigDecimal.ZERO;
		BigDecimal currMonthPrepayPkg = ccsRepaySchedule!=null? ccsRepaySchedule.getLoanPrepayPkgAmt():BigDecimal.ZERO;
		BigDecimal currMonthInsurance = BigDecimal.ZERO;
		//如果是理赔结清，后面的保费不在收取
		if(loanReg.getLoanAction() != LoanAction.C){
			if(loanFeeDef.insCalcMethod == PrepaymentFeeMethod.A){
				BigDecimal currTolMonthInsurance = ccsRepaySchedule==null ? BigDecimal.ZERO: ccsRepaySchedule.getLoanInsuranceAmt() ;
				if(days >= 30){
					currMonthInsurance = currTolMonthInsurance;
				}else{
					currMonthInsurance = currTolMonthInsurance.multiply(new BigDecimal(days)).divide(new BigDecimal(30), 2, RoundingMode.HALF_UP); //按天折算
				}
			}else{
				//保费月利率
				BigDecimal monthInsRt = loan.getInsuranceRate().divide(new BigDecimal(loan.getLoanInitTerm()), 20, RoundingMode.HALF_UP);;
				//剩余保费重算,剩余本金*月利率*天数/30
				currMonthInsurance = loan.getLoanInitPrin().multiply(monthInsRt).multiply(new BigDecimal(days)).divide(new BigDecimal(30),2,RoundingMode.HALF_UP); // 总本金 *月利率
			}
		}
		
		//退货结出的欠款
		BigDecimal totSettleAmt = BigDecimal.ZERO;
		totSettleAmt = loan.getUnstmtPrin().add(monthInt)
				.add(currMonthStampduty).add(currMonthFee).add(currMonthSVCFee)
				.add(currMonthReplaceSvcFee).add(currMonthLifeInsuFee).add(currMonthInsurance)
				.add(currMonthPrepayPkg);
		
		// 往期总欠款
		BigDecimal pastTotAmt = BigDecimal.ZERO;
		
		List<CcsPlan> xfinPlans = this.getPlanIListByLoanId(loan);
		if(xfinPlans != null && xfinPlans.size() > 0 ){
			for (CcsPlan plan : xfinPlans) {
				pastTotAmt = pastTotAmt.add(plan.getCurrBal()).add(plan.getNodefbnpIntAcru().setScale(2, RoundingMode.HALF_UP));
				if(DateUtils.truncatedCompareTo(batDate, acct.getGraceDate(), Calendar.DATE) > 0){
					pastTotAmt = pastTotAmt.add(plan.getPenaltyAcru().setScale(2, RoundingMode.HALF_UP)).add(plan.getCompoundAcru().setScale(2, RoundingMode.HALF_UP))
							.add(plan.getReplacePenaltyAcru().setScale(2,RoundingMode.HALF_UP));
				}
			}
		}
		
		// 当天不为账单日时，直接全部结出，还款日为下个账单日
		if(batDate.compareTo(nextStmtDate) != 0){
			// 若当日不为账单日，则直接全部结出,还款日为下个账单日；
			Date nextSchedulePmtDay = null;
			// 如果当天日期小于下个账单日,则生成下个账单日还款计划,全部结出
			if(batDate.compareTo(nextStmtDate) < 0){
				nextSchedulePmtDay = nextStmtDate;
			}else{
				// 否则，重算下个账单日，生成1个还款计划，全部结出
				for(int i = 2;;i++){
					nextSchedulePmtDay = this.getLoanPmtDueDate(nextStmtDate,loanFeeDef,i,acct.getCycleDay());
					if (batDate.compareTo(nextSchedulePmtDay) < 0){
						break;
					}
				}
			}
			CcsRepaySchedule firstSchedule =  this.generateRepaySchedule(loanReg, loan.getLoanId(), loan.getCurrTerm()+1, 
					loan.getUnstmtPrin(),currMonthFee,currMonthSVCFee, monthInt,currMonthInsurance,
					currMonthStampduty,currMonthLifeInsuFee,currMonthReplaceSvcFee,currMonthPrepayPkg,nextSchedulePmtDay);
			newScheduleList.add(firstSchedule);
		}else{
			// 当天为账单日时，能全部结清,则生成当日账单日schedule全部结出；
			if(refundAmt.compareTo(totSettleAmt.add(pastTotAmt)) >= 0){
				CcsRepaySchedule firstSchedule =  this.generateRepaySchedule(loanReg, loan.getLoanId(), loan.getCurrTerm()+1, 
						loan.getUnstmtPrin(),currMonthFee,currMonthSVCFee, monthInt,currMonthInsurance,
						currMonthStampduty,currMonthLifeInsuFee,currMonthReplaceSvcFee,currMonthPrepayPkg,nextStmtDate);
				newScheduleList.add(firstSchedule);
			}else{
				// 如果不能全部结清,且退货金额<往期欠款，则当天不结任何金额，剩余部分生成下个账单日schedule；否则只结出与（退货金额-往期欠款）相等部分schedule，还款日为当天账单日，剩余部分生成下个账单日的schedule
				// 退货金额<往期欠款时，当天不结出任何金额，全部结入下个账单日schedule
				if(refundAmt.compareTo(pastTotAmt) <= 0){
					Date nextSchedulePmtDay = this.getLoanPmtDueDate(nextStmtDate,loanFeeDef,2,acct.getCycleDay());
					CcsRepaySchedule firstSchedule =  this.generateRepaySchedule(loanReg, loan.getLoanId(), loan.getCurrTerm()+1, 
							loan.getUnstmtPrin(),currMonthFee,currMonthSVCFee, monthInt,currMonthInsurance,
							currMonthStampduty,currMonthLifeInsuFee,currMonthReplaceSvcFee,currMonthPrepayPkg,nextSchedulePmtDay);
					newScheduleList.add(firstSchedule);					
				}else{
					// 还清往期欠款剩余部分
					BigDecimal paidRemain = refundAmt.subtract(pastTotAmt);
					// 当天转入本金
					BigDecimal currDaySchedulePrin = BigDecimal.ZERO;
					// 当天转入利息
					BigDecimal currDayScheduleInt = BigDecimal.ZERO;
					// 当天转入印花税
					BigDecimal currDayScheduleStampduty = BigDecimal.ZERO;
					// 当天转入贷款服务费
					BigDecimal currDayScheduleMonthFee = BigDecimal.ZERO;
					// 当天转入贷款分期手续费
					BigDecimal currDayScheduleSVCFee = BigDecimal.ZERO;
					// 当天转入寿险计划费
					BigDecimal currDayScheduleLifeInsuFee = BigDecimal.ZERO;
					// 当天转入代收服务费
					BigDecimal currDayScheduleReplaceSvcFee = BigDecimal.ZERO;
					// 当天转入保费
					BigDecimal currDayScheduleInsurance = BigDecimal.ZERO;
					// 当天转入灵活还款计划包
					BigDecimal currDaySchedulePrepayPkg = BigDecimal.ZERO;
					// 每次计算转入成分后剩余部分
					BigDecimal subBal = BigDecimal.ZERO;
					// 计算本金
					if(paidRemain.compareTo(loan.getUnstmtPrin()) >= 0){
						currDaySchedulePrin = loan.getUnstmtPrin();
					}else{
						currDaySchedulePrin = paidRemain;
					}
					subBal = paidRemain.subtract(currDaySchedulePrin);
					
					// 计算利息
					if(subBal.compareTo(BigDecimal.ZERO) > 0 ){
						if(subBal.compareTo(monthInt) >= 0){
							currDayScheduleInt = monthInt;	
						}else{
							currDayScheduleInt = subBal;
						}
						subBal = subBal.subtract(currDayScheduleInt);
					}

					// 计算印花税
					if(subBal.compareTo(BigDecimal.ZERO) > 0 ){
						if(subBal.compareTo(currMonthStampduty) >= 0){
							currDayScheduleStampduty = currMonthStampduty;	
						}else{
							currDayScheduleStampduty = subBal;
						}
						subBal = subBal.subtract(currDayScheduleStampduty);
					}

					// 计算贷款服务费
					if(subBal.compareTo(BigDecimal.ZERO) > 0 ){
						if(subBal.compareTo(currMonthFee) >= 0){
							currDayScheduleMonthFee = currMonthFee;	
						}else{
							currDayScheduleMonthFee = subBal;
						}
						subBal = subBal.subtract(currDayScheduleMonthFee);
					}

					// 计算贷款分期手续费
					if(subBal.compareTo(BigDecimal.ZERO) > 0 ){
						if(subBal.compareTo(currMonthSVCFee) >= 0){
							currDayScheduleSVCFee = currMonthSVCFee;	
						}else{
							currDayScheduleSVCFee = subBal;
						}
						subBal = subBal.subtract(currDayScheduleSVCFee);
					}

					// 计算寿险服务费
					if(subBal.compareTo(BigDecimal.ZERO) > 0 ){
						if(subBal.compareTo(currMonthLifeInsuFee) >= 0){
							currDayScheduleLifeInsuFee = currMonthLifeInsuFee;	
						}else{
							currDayScheduleLifeInsuFee = subBal;
						}
						subBal = subBal.subtract(currDayScheduleLifeInsuFee);
					}
					
					// 计算代收服务费
					if(subBal.compareTo(BigDecimal.ZERO) > 0 ){
						if(subBal.compareTo(currMonthReplaceSvcFee) >= 0){
							currDayScheduleReplaceSvcFee = currMonthReplaceSvcFee;	
						}else{
							currDayScheduleReplaceSvcFee = subBal;
						}
						subBal = subBal.subtract(currDayScheduleReplaceSvcFee);
					}
					
					// 计算保费
					if(subBal.compareTo(BigDecimal.ZERO) > 0 ){
						if(subBal.compareTo(currMonthInsurance) >= 0){
							currDayScheduleInsurance = currMonthInsurance;	
						}else{
							currDayScheduleInsurance = subBal;
						}
						subBal = subBal.subtract(currDayScheduleInsurance);
					}
					
					// 计算灵活还款计划包
					if(subBal.compareTo(BigDecimal.ZERO)>0){
						if(subBal.compareTo(currMonthPrepayPkg) >= 0){
							currDaySchedulePrepayPkg = currMonthPrepayPkg;	
						}else{
							currDaySchedulePrepayPkg = subBal;
						}
						subBal = subBal.subtract(currDaySchedulePrepayPkg);
					}
					
					// 当天账单日结出（退货金额-往期欠款）部分，写死的按照本金、利息、费用成分依次计算
					CcsRepaySchedule firstSchedule =  this.generateRepaySchedule(loanReg, loan.getLoanId(), loan.getCurrTerm()+1, 
							currDaySchedulePrin,currDayScheduleMonthFee,currDayScheduleSVCFee, currDayScheduleInt,currDayScheduleInsurance,
							currDayScheduleStampduty,currDayScheduleLifeInsuFee,currDayScheduleReplaceSvcFee,currDaySchedulePrepayPkg,nextStmtDate);
					
					// 剩余部分放在下一个schedule中，本金部分需要计算从当日到下一还款日的利息
					// 判断当日是否等于账户下一账单日，如果是，则下一还款日期为下一账单日，否则下一还款日期为当前账户下一账单日
					// 下期转入本金
					BigDecimal nextSchedulePrin = loan.getUnstmtPrin().subtract(currDaySchedulePrin);
					// 下期转入利息
					BigDecimal nextScheduleInt = monthInt.subtract(currDayScheduleInt);
					// 下期转入印花税
					BigDecimal nextScheduleStampduty = currMonthStampduty.subtract(currDayScheduleStampduty);
					// 下期转入贷款服务费
					BigDecimal nextScheduleMonthFee = currMonthFee.subtract(currDayScheduleMonthFee);
					// 下期转入贷款分期手续费
					BigDecimal nextScheduleSVCFee = currMonthSVCFee.subtract(currDayScheduleSVCFee);
					// 下期转入寿险计划费
					BigDecimal nextScheduleLifeInsuFee = currMonthLifeInsuFee.subtract(currDayScheduleLifeInsuFee);
					// 下期转入代收服务费
					BigDecimal nextScheduleReplaceSvcFee = currMonthReplaceSvcFee.subtract(currDayScheduleReplaceSvcFee);
					// 下期转入保费
					BigDecimal nextScheduleInsurance = currMonthInsurance.subtract(currDayScheduleInsurance);
					// 下期转入灵活还款计划包
					BigDecimal nextSchedulePrepayPkg = currMonthPrepayPkg.subtract(currDaySchedulePrepayPkg);
					Date nextSchedulePmtDay = null;
					if(batDate.compareTo(nextStmtDate) == 0){
						for(int i = 2;;i++){
							nextSchedulePmtDay = this.getLoanPmtDueDate(nextStmtDate,loanFeeDef,i,acct.getCycleDay());
							if (batDate.compareTo(nextSchedulePmtDay) < 0){
								break;
							}
						}
					}else{
						nextSchedulePmtDay = nextStmtDate;
					}
					
					int	intCalDays = DateUtils.getIntervalDays(batDate,nextSchedulePmtDay);
					BigDecimal dayIntRt = this.calcDayIntRt(loan.getInterestRate());
					nextScheduleInt = nextScheduleInt.add(nextSchedulePrin.multiply(dayIntRt).multiply(new BigDecimal(intCalDays)).setScale(2, RoundingMode.HALF_UP));

					CcsRepaySchedule nextSchedule =  this.generateRepaySchedule(loanReg, loan.getLoanId(), loan.getCurrTerm()+2, 
							nextSchedulePrin,nextScheduleMonthFee,nextScheduleSVCFee, nextScheduleInt,nextScheduleInsurance,
							nextScheduleStampduty,nextScheduleLifeInsuFee,nextScheduleReplaceSvcFee,nextSchedulePrepayPkg,nextSchedulePmtDay);

					newScheduleList.add(firstSchedule);
					newScheduleList.add(nextSchedule);
				}
			}
		}
		return newScheduleList;
	}
	
	/**
	 *  重新计算下一期的还款计划
	 * 生成还款计划为当日，保费，印花税，寿险计划包费，重新计算
	 * @param loan
	 * @param isHesitation
	 * @return
	 */
	public CcsRepaySchedule nextScheduletRetry(CcsLoan loan,CcsRepaySchedule ctdSchedule,int days,Date batchDate,boolean isHesitation) {
		// 判断是否犹豫期内
		if(isHesitation){
			return this.generateSchedule(loan, loan.getCurrTerm()+1, loan.getUnstmtPrin(),BigDecimal.ZERO,BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO, batchDate);
		}
		BigDecimal monthInt = BigDecimal.ZERO;
		BigDecimal currMonthInsurance = BigDecimal.ZERO;
		
		//剩余利息重算，还款计划中的利息*天数/30
		if(days >= 30){
			monthInt = ctdSchedule.getLoanTermInt();
		}else{
			monthInt = ctdSchedule.getLoanTermInt().multiply(new BigDecimal(days)).divide(new BigDecimal(30), 2, RoundingMode.HALF_UP); // 剩余应还本金 *月利率
		}
		BigDecimal currMonthStampduty=BigDecimal.ZERO;
		if(loan.getStampCustomInd().equals(Indicator.Y)){
			currMonthStampduty=loan.getUnstmtStampdutyAmt();
		}
		
		BigDecimal currMonthFee = ctdSchedule!=null ? ctdSchedule.getLoanTermFee() : BigDecimal.ZERO;
		BigDecimal currMonthSVCFee = ctdSchedule!=null ? ctdSchedule.getLoanSvcFee() : BigDecimal.ZERO;
		BigDecimal currMonthLifeInsuFee = ctdSchedule!=null ? ctdSchedule.getLoanLifeInsuAmt() : BigDecimal.ZERO;
		//剩余保费重算,*天数/30
		BigDecimal currTolMonthInsurance = ctdSchedule!=null ? ctdSchedule.getLoanInsuranceAmt() : BigDecimal.ZERO;
		//剩余保费重算,*天数/30
		currMonthInsurance = currTolMonthInsurance.multiply(new BigDecimal(days)).divide(new BigDecimal(30), 2, RoundingMode.HALF_UP); // 剩余应还本金 *月利率
	
		// 代收服务费
		BigDecimal currMonthReplaceSvcFee = ctdSchedule!=null ? ctdSchedule.getLoanReplaceSvcFee() : BigDecimal.ZERO;
		BigDecimal currMonthPrepayPkg = ctdSchedule!=null? ctdSchedule.getLoanPrepayPkgAmt() : BigDecimal.ZERO;
		return this.generateSchedule(loan, loan.getCurrTerm()+1, loan.getUnstmtPrin(),currMonthFee,currMonthSVCFee, 
				monthInt,currMonthInsurance,currMonthStampduty,currMonthLifeInsuFee,currMonthReplaceSvcFee,currMonthPrepayPkg, batchDate);
	}
	/**
	 * 创建贷款分配计划schedule
	 * 
	 * @param loanReg
	 * @param loanId
	 * @param currTerm
	 * @param loanTermPrin
	 * @param loanTermFee1
	 * @param loanTermInterest
	 * @param loanPmtDueDate
	 * @return
	 */
	public CcsRepaySchedule generateRepaySchedule(CcsLoanReg loanReg, Long loanId, 
			Integer currTerm, BigDecimal loanTermPrin, BigDecimal loanTermFee, BigDecimal loanSVCFee, 
			BigDecimal loanTermInterest,BigDecimal currMonthInsurance,BigDecimal currMonthStampduty,BigDecimal currMonthLifeInsuFee,
			BigDecimal currMonthReplaceSvcFee,BigDecimal currMonthPrepayPkg,Date loanPmtDueDate) {
		CcsRepaySchedule schedule = new CcsRepaySchedule();
		
//		schedule.setScheduleId(); //自增
		schedule.setOrg(loanReg.getOrg());
		schedule.setLoanId(loanId);
		schedule.setAcctNbr(loanReg.getAcctNbr());
		schedule.setAcctType(loanReg.getAcctType());
		schedule.setLogicCardNbr(loanReg.getLogicCardNbr());
		schedule.setCardNbr(loanReg.getCardNbr());
		schedule.setLoanInitPrin(loanReg.getLoanInitPrin());
		schedule.setLoanInitTerm(loanReg.getLoanInitTerm());
		schedule.setCurrTerm(currTerm);
		schedule.setLoanTermPrin(loanTermPrin);
		schedule.setLoanTermFee(loanTermFee); // 服务费
		schedule.setLoanTermInt(loanTermInterest);
		schedule.setLoanPmtDueDate(loanPmtDueDate);
		schedule.setLoanSvcFee(loanSVCFee);
		//赋值保险费，印花税，寿险计划包费
		schedule.setLoanInsuranceAmt(currMonthInsurance);
		schedule.setLoanStampdutyAmt(currMonthStampduty);
		schedule.setLoanLifeInsuAmt(currMonthLifeInsuFee);
		
		// 代收服务费
		schedule.setLoanReplaceSvcFee(currMonthReplaceSvcFee);
		schedule.setLoanPrepayPkgAmt(currMonthPrepayPkg);
		
		AccountAttribute acctAttr = productFacility.CardNoTOAccountAttribute(loanReg.getCardNbr(), loanReg.getAcctType().getCurrencyCode());
		schedule.setLoanGraceDate(DateUtils.addDays(loanPmtDueDate, acctAttr.pmtGracePrd));
		
		return schedule;
	}
	

	/**
	 * 根据loan新建贷款分配计划schedule 
	 * @param loan
	 * @param currTerm
	 * @param loanTermPrin
	 * @param loanTermFee
	 * @param loanTermInterest
	 * @param currMonthInsurance
	 * @param currMonthStampduty
	 * @param currMonthLifeInsuFee
	 * @param loanPmtDueDate
	 * @return
	 */
	public CcsRepaySchedule generateSchedule(CcsLoan loan, 
			Integer currTerm, BigDecimal loanTermPrin, BigDecimal loanTermFee, BigDecimal loanSVCFee, 
			BigDecimal loanTermInterest,BigDecimal currMonthInsurance,BigDecimal currMonthStampduty,BigDecimal currMonthLifeInsuFee,
			BigDecimal currMonthReplaceSvcFee, BigDecimal currMonthPrepayPkg,Date loanPmtDueDate) {
		CcsRepaySchedule schedule = new CcsRepaySchedule();
		
//		schedule.setScheduleId(); //自增
		schedule.setOrg(loan.getOrg());
		schedule.setLoanId(loan.getLoanId());
		schedule.setAcctNbr(loan.getAcctNbr());
		schedule.setAcctType(loan.getAcctType());
		schedule.setLogicCardNbr(loan.getLogicCardNbr());
		schedule.setCardNbr(loan.getCardNbr());
		schedule.setLoanInitPrin(loan.getLoanInitPrin());
		schedule.setLoanInitTerm(loan.getLoanInitTerm());
		schedule.setCurrTerm(currTerm);
		schedule.setLoanTermPrin(loanTermPrin);
		schedule.setLoanTermFee(loanTermFee); // 服务费
		schedule.setLoanTermInt(loanTermInterest);
		schedule.setLoanPmtDueDate(loanPmtDueDate);
		schedule.setLoanSvcFee(loanSVCFee);
		//赋值保险费，印花税，寿险计划包费
		schedule.setLoanInsuranceAmt(currMonthInsurance);
		schedule.setLoanStampdutyAmt(currMonthStampduty);
		schedule.setLoanLifeInsuAmt(currMonthLifeInsuFee);
		
		// 代收服务费
		schedule.setLoanReplaceSvcFee(currMonthReplaceSvcFee);
		schedule.setLoanPrepayPkgAmt(currMonthPrepayPkg);
		
		AccountAttribute acctAttr = productFacility.CardNoTOAccountAttribute(loan.getCardNbr(), loan.getAcctType().getCurrencyCode());
		schedule.setLoanGraceDate(DateUtils.addDays(loanPmtDueDate, acctAttr.pmtGracePrd));
		
		return schedule;
	}
	
	/**
	 * 创建贷款分配计划schedule,generateRepaySchedule重载 从外部传入账户参数
	 * 
	 * @param AccountAttribute acctAttr
	 * @param loanReg
	 * @param loanId
	 * @param currTerm
	 * @param loanTermPrin
	 * @param loanTermFee1
	 * @param loanTermInterest
	 * @param loanPmtDueDate
	 * @return
	 */
	public CcsRepaySchedule generateRepaySchedule(AccountAttribute acctAttr, CcsLoanReg loanReg, Long loanId, 
			Integer currTerm, BigDecimal loanTermPrin, BigDecimal InstallmentFee,BigDecimal SVCFee, BigDecimal loanTermInterest,BigDecimal currMonthInsurance,BigDecimal currMonthStampduty,BigDecimal currMonthLifeInsuFee,BigDecimal loanReplaceSvcFee,BigDecimal currMonthPrepayPkg,Date loanPmtDueDate) {
		CcsRepaySchedule schedule = new CcsRepaySchedule();
		
//		schedule.setScheduleId(); //自增
		schedule.setOrg(loanReg.getOrg());
		schedule.setLoanId(loanId);
		schedule.setAcctNbr(loanReg.getAcctNbr());
		schedule.setAcctType(loanReg.getAcctType());
		schedule.setLogicCardNbr(loanReg.getLogicCardNbr());
		schedule.setCardNbr(loanReg.getCardNbr());
		schedule.setLoanInitPrin(loanReg.getLoanInitPrin());
		schedule.setLoanInitTerm(loanReg.getLoanInitTerm());
		schedule.setCurrTerm(currTerm);
		schedule.setLoanTermPrin(loanTermPrin);
		schedule.setLoanTermFee(InstallmentFee); // 手续费
		schedule.setLoanSvcFee(SVCFee); // 服务费
		schedule.setLoanTermInt(loanTermInterest);
		schedule.setLoanPmtDueDate(loanPmtDueDate);
		
		//赋值保险费，印花税，寿险计划包费
		schedule.setLoanInsuranceAmt(currMonthInsurance);
		schedule.setLoanStampdutyAmt(currMonthStampduty);
		schedule.setLoanLifeInsuAmt(currMonthLifeInsuFee);
		
		// 代收服务费
		schedule.setLoanReplaceSvcFee(loanReplaceSvcFee);
		schedule.setLoanPrepayPkgAmt(currMonthPrepayPkg);
//		AccountAttribute acctAttr = productFacility.CardNoTOAccountAttribute(loanReg.getCardNbr(), loanReg.getAcctType().getCurrencyCode());
		schedule.setLoanGraceDate(DateUtils.addDays(loanPmtDueDate, acctAttr.pmtGracePrd));
		
		return schedule;
	}
	
	/**
	 * 计算等额本息每期应还款额(月为单位)
	 * 
	 * 月均还款 ＝ 贷款额×月利率×（1＋月利率）^总月数 ÷〔（1＋月利率）^总月数－1〕
	 * 
	 * @param loanInitPrin
	 * @param monthIntRt
	 * @param loanInitTerm
	 * @return
	 */
	public BigDecimal calcMonthPmtForMCEI(BigDecimal loanInitPrin, BigDecimal monthIntRt, Integer loanInitTerm) {
		BigDecimal j = (monthIntRt.add(BigDecimal.ONE)).pow(loanInitTerm);
		if (monthIntRt.compareTo(BigDecimal.ZERO)==0) {
			return loanInitPrin.divide(new BigDecimal(loanInitTerm.intValue()), 6, RoundingMode.HALF_UP);
		}else {
			return loanInitPrin.multiply(monthIntRt).multiply(j).divide(j.subtract(BigDecimal.ONE), 6, RoundingMode.HALF_UP);
		}
	}
	
	
	/**
	 * 计算等额本息每期本金(月为单位)
	 * 
	 * 第n个月还贷本金 = 贷款额*月利率(1+月利率)^(第n月-1)/[(1+月利率)^总月数-1]
	 * 
	 * @param loanInitPrin
	 * @param monthIntRt
	 * @param loanInitTerm
	 * @param currTerm
	 * @return
	 */
	public BigDecimal calcCurrMonthPrinForMCEI(BigDecimal loanInitPrin, BigDecimal monthIntRt, Integer loanInitTerm, Integer currTerm) {
		BigDecimal j = (monthIntRt.add(BigDecimal.ONE)).pow(currTerm-1);
		BigDecimal k = (monthIntRt.add(BigDecimal.ONE)).pow(loanInitTerm);
		if (monthIntRt.compareTo(BigDecimal.ZERO)==0) {
			return loanInitPrin.divide(new BigDecimal(loanInitTerm.intValue()), 6, RoundingMode.HALF_UP);
		}else {
			return loanInitPrin.multiply(monthIntRt).multiply(j).divide(k.subtract(BigDecimal.ONE), 6, RoundingMode.HALF_UP);
		}
	}
	
	
	/**
	 * 计算等额本金第n期利息(月为单位)
	 * 
	 * n月利息 ＝ 总贷款数×〔 1－（第n月-1）÷ 总月数 〕×月利率
	 * 
	 * @param loanInitPrin
	 * @param monthIntRt
	 * @param loanInitTerm
	 * @param currTerm
	 * @return
	 */
	public BigDecimal calcMonthIntForMCEP(BigDecimal loanInitPrin, BigDecimal monthIntRt, Integer loanInitTerm, Integer currTerm) {
		BigDecimal j = BigDecimal.ONE.subtract(new BigDecimal(currTerm).subtract(BigDecimal.ONE).divide(new BigDecimal(loanInitTerm), 20, RoundingMode.HALF_UP));
		return loanInitPrin.multiply(monthIntRt).multiply(j).setScale(2, RoundingMode.HALF_UP);
	}
	
	
	/**
	 * 计算月利率
	 * 
	 * @param interestRate
	 * @param bigDecimal 
	 * @return
	 */
	public BigDecimal calcMonthIntRt(BigDecimal interestRate, BigDecimal floatRate) {
		return interestRate.multiply(floatRate.add(BigDecimal.ONE)).multiply(new BigDecimal(1.0/YEAR_MONTHS).setScale(20, RoundingMode.HALF_UP));
	}
	
	/**
	 * 计算日利率
	 * 
	 * @param interestRate
	 * @return
	 */
	public BigDecimal calcDayIntRt(BigDecimal interestRate) {
		return interestRate.multiply(new BigDecimal(1.0/YEAR_DAYS).setScale(20, RoundingMode.HALF_UP));
	}
	
	/**
	 * 获取到期还款日期
	 * 
	 * @param nextStmtDate
	 * @param loanFeeDef
	 * @param currTerm
	 * @return
	 */
	public Date getLoanPmtDueDate(Date nextStmtDate, LoanFeeDef loanFeeDef, Integer currTerm){
		Integer intervalMonth = this.getIntervalMonth(loanFeeDef);
		
		// 下个账账单日直接XFRIN
		return DateUtils.addMonths(nextStmtDate, intervalMonth*(currTerm-1));
	}
	
	/**
	 * 获取到期还款日期
	 * 
	 * @param nextStmtDate
	 * @param loanFeeDef
	 * @param currTerm
	 * @return
	 */
	public Date getLoanPmtDueDate(Date nextStmtDate, LoanFeeDef loanFeeDef, Integer currTerm,String date){
		
		Integer intervalMonth = this.getIntervalMonth(loanFeeDef);
		
		int cycleDate = Integer.parseInt(date);
		Calendar calendar = Calendar.getInstance(); 
		calendar.setTime(nextStmtDate); 
		// 下个账账单日直接XFRIN
		calendar.add(Calendar.MONTH, intervalMonth*(currTerm-1)); 
		
		if(cycleDate > calendar.get(Calendar.DAY_OF_MONTH)){
			///如果下一个账单日的日期在date范围之内,下一个还款日特殊处理未下一月的最后一天
			int days = calendar.getActualMaximum(Calendar.DAY_OF_MONTH); 
			if(cycleDate <=days){
				calendar.set(Calendar.DAY_OF_MONTH,cycleDate);
			}
			if(cycleDate > days){
				calendar.set(Calendar.DAY_OF_MONTH,days); 
			}
		}
		return calendar.getTime();
		
	}
	
//	public static void main(String[] args) {
//		MicroCreditRescheduleUtils u = new MicroCreditRescheduleUtils();
//		DateFormat format = new SimpleDateFormat("yyyyMMdd"); 
//		LoanFeeDef loanFeeDef = new LoanFeeDef();
//		loanFeeDef.paymentIntervalUnit = PaymentIntervalUnit .M;
//		loanFeeDef.paymentIntervalPeriod = 1;
//		try {
//			Date bizDate = format.parse("20151020");
//			Date paymentDay = null;
//			Date stmtDay = u.getLoanPmtDueDate(bizDate, loanFeeDef, 2);
//			System.out.println("stmtDay:"+format.format(stmtDay));
//			
//			Calendar calendar = Calendar.getInstance();  
//			calendar.setTime(stmtDay);
//			calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DATE));  
//			
//			System.out.println("stmtDay-last:"+format.format(calendar.getTime()));
//			
//			paymentDay = DateUtils.addDays(stmtDay, 2);
//			Calendar calendarPay = Calendar.getInstance();  
//			calendarPay.setTime(paymentDay);
//			
//			System.out.println("paymentDay:"+format.format(calendarPay.getTime()));
//			if((calendarPay.get(Calendar.MONTH) + 1) != (calendar.get(Calendar.MONTH) + 1)){
//				paymentDay = calendar.getTime();
//			}
//			System.out.println("paymentDay-o:"+format.format(paymentDay));
//			
//			System.out.println("graceDay-o:"+format.format(DateUtils.addDays(paymentDay, 1)));
//		} catch (ParseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
	
//	public static void main(String[] args) throws ParseException {
//		// TODO Auto-generated method stub
//		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
//		Date nextStmtDate = df.parse("2015-08-29 ");
//		String date = "29,30,31";
//		Calendar calendar = Calendar.getInstance(); 
//		calendar.setTime(nextStmtDate); 
//		if(date.contains(calendar.get(Calendar.DAY_OF_MONTH)+"")){
//			///如果下一个账单日的日期在date范围之内,下一个还款日特殊处理未下一月的最后一天
//	        calendar.add(Calendar.MONTH, 2); 
//	        int index = calendar.get(Calendar.DAY_OF_MONTH); 
//	        calendar.add(Calendar.DATE, (-index)); 
//
//	        System.out.println(df.format(calendar.getTime()));
//		}else{
//			// 下个账账单日直接XFRIN
//			System.out.println(df.format(DateUtils.addMonths(nextStmtDate, 1)));
//		}
//		Date testDate = df.parse("2015-09-03 ");
//		System.out.println(DateUtils.getIntervalDays(testDate,nextStmtDate));
//	}
	/**
	 * 根据间隔单位间隔周期换算为间隔月数
	 * 
	 * 暂只支持间隔单位为月，间隔周期为1，否则报错
	 * 
	 * @param loanFeeDef
	 * @return
	 */
	public Integer getIntervalMonth(LoanFeeDef loanFeeDef) {
		Integer intervalMonth;
		switch (loanFeeDef.paymentIntervalUnit) {
//		case D: break;
//		case H: break;
//		case W: break;
		case M: intervalMonth =  1; break;
//		case Q: intervalMonth =  3; break;
//		case Y: intervalMonth = 12; break;
		default:
			throw new IllegalArgumentException("不支持的还款间隔单位:" +loanFeeDef.paymentIntervalUnit);
		}
		
		if(loanFeeDef.paymentIntervalPeriod != 1){
			throw new IllegalArgumentException("不支持的还款间隔周期:" +loanFeeDef.paymentIntervalPeriod);
		}
		
		return intervalMonth *loanFeeDef.paymentIntervalPeriod;
	}
	
	/**
	 * 计算小额贷款展期手续费
	 * 
	 * @param unearnedPrin
	 * @param loanFeeDef
	 * @return
	 */
	public BigDecimal calcRescheduleFeeAmt(BigDecimal unearnedPrin, LoanFeeDef loanFeeDef){
		BigDecimal feeAmt;
		switch (loanFeeDef.rescheduleCalcMethod){
		case A : 
			feeAmt = loanFeeDef.rescheduleFeeAmount;
			break;
		case R :
			feeAmt = unearnedPrin.multiply(loanFeeDef.rescheduleFeeRate); 
			break;
		default: throw new IllegalArgumentException("不支持的手续费计算方式:" +loanFeeDef.rescheduleCalcMethod);
		}
		return feeAmt;
	}
	
	
	/**
	 * 计算小额贷款缩期手续费
	 * 
	 * @param shortedPmtDue
	 * @param loanFeeDef
	 * @return
	 */
	public BigDecimal calcShortedFeeAmt(BigDecimal shortedPmtDue, LoanFeeDef loanFeeDef){
		BigDecimal feeAmt;
		switch (loanFeeDef.shortedRescCalcMethod){
		case A : 
			feeAmt = loanFeeDef.shortedRescFeeAmount;
			break;
		case R :
			feeAmt = shortedPmtDue.multiply(loanFeeDef.shortedRescFeeAmountRate);
			break;
		default: throw new IllegalArgumentException("不支持的手续费计算方式:" +loanFeeDef.shortedRescCalcMethod);
		}
		return feeAmt;
	}
	
	/**
	 * 计算小额贷提前还款手续费
	 * 
	 * @param prepaymentPmtDue
	 * @param loanFeeDef
	 * @return
	 */
	public BigDecimal prepaymentFeeAmt(BigDecimal prepaymentPmtDue, LoanFeeDef loanFeeDef,int currTerm){
		BigDecimal feeAmt = BigDecimal.ZERO;
		feeAmt = EarlySettleMethodimple.valueOf(
				loanFeeDef.prepaymentFeeMethod.toString()).calcuteEarlySettle(
						prepaymentPmtDue, loanFeeDef,currTerm);
		return feeAmt;
	}
	
	/**
	 * 小额贷款获取LoanFeeDef
	 * 
	 * @param loanCode
	 * @param term
	 * @return
	 */
/*	public LoanFeeDef getLoanFeeDefForMicroCredit(String loanCode, Integer term) {
		LoanPlan loanPlan = parameterFacility.retrieveParameterObject(loanCode,LoanPlan.class);
		Map<Integer,LoanFeeDef> m = loanPlan.loanFeeDefMap;
		int key = -1;
		for(int i : m.keySet()){
			if(i==term.intValue()){
				key = i;
				break;
			}else{
				if(i>term.intValue()){
					if(key == -1){
						key = i;
					}else{
						if(i<key){
							key = i;
						}
					}
				}
			}
		}
		if(key == -1){
			throw new IllegalArgumentException("未找到贷款定价参数，loanCode="+loanCode+"，term="+term);
		}
		LoanFeeDef fee = loanPlan.loanFeeDefMap.get(key);
		return fee;
	}*/
	
	
	/**
	 * 根据产品信息,分期期数，分期金额，获取分期定价参数
	 * 
	 * @param loanCode
	 *            贷款产品编号
	 * @param term
	 *            定价参数键值
	 * @param amt
	 *            定价参数键值
	 * @param loanFeeDefId
	 *            子产品编号
	 * @return
	 * @throws ProcessException
	 * @since 1.0.0
	 */
	public LoanFeeDef getLoanFeeDef(String loanCode, Integer term, BigDecimal amt,String loanFeeDefId) throws ProcessException {
		
		try {
			
			if (logger.isDebugEnabled()) {
				logger.debug("获取子产品,参数为：" + "LoanCode" + "[" + loanCode + "]," + "Term" + "[" + term + "]," 
							+ "Amt" + "Term" + "[" + amt + "]," + "LoanFeeDefId" + "[" + loanFeeDefId + "]");
			}
			
			LoanFeeDef loanFeeDef = null;
			
			LoanPlan loanPlan = parameterFacility.retrieveParameterObject(loanCode,LoanPlan.class);
			
			if(loanPlan == null){
				throw new ProcessException("分期失败，不存在分期计划参数");
			}
			
			Map<Integer, LoanFeeDef> loanFeeDefMap = loanPlan.loanFeeDefMap;
			
			// 子产品编号不为null，则根据子产品编号查询
			if (loanFeeDefId != null) {
				for (Integer loanFeeDefKey: loanFeeDefMap.keySet()) {
					LoanFeeDef tmpLoanFeeDef = loanFeeDefMap.get(loanFeeDefKey);
					if (Integer.valueOf(loanFeeDefId).equals(tmpLoanFeeDef.loanFeeDefId)) {
						loanFeeDef = tmpLoanFeeDef;
						break;
					}
				}
			}
			
			if (loanFeeDef == null) {
				
				List<LoanFeeDef> loanFeeDefList = new ArrayList<LoanFeeDef>();
				
				//按期数、分期金额匹配参数、定价参数金额区间匹配子产品
				for(Integer loanFeeDefKey: loanFeeDefMap.keySet()){
				    
					LoanFeeDef tmpLoanFeeDef = loanFeeDefMap.get(loanFeeDefKey);
					
				    if (term.equals(tmpLoanFeeDef.initTerm) 
				    		&& amt.compareTo(tmpLoanFeeDef.minAmount)>=0 
				    		&& amt.compareTo( tmpLoanFeeDef.maxAmount)<=0) {
						
						// 找到启用中的第一个子产品，直接返回
						if (LoanFeeDefStatus.A.equals(tmpLoanFeeDef.loanFeeDefStatus)) {
							loanFeeDef = tmpLoanFeeDef;
							break;
						}
						
						// 加到List中，当找不到启用中的子产品时，需要匹配一条子产品
						loanFeeDefList.add(tmpLoanFeeDef);
					}
				}
				
				if (loanFeeDef == null) {
					//没有匹配到分期定价参数
					if (loanFeeDefList.size() < 1) {
						throw new ProcessException("分期失败，不存在分期定价参数");
					} else {
						loanFeeDef = loanFeeDefList.get(0);
					}
				}
				
			}
			
			if (logger.isDebugEnabled()) {
				logger.debug("获取子产品ID为：[" + loanFeeDef.loanFeeDefId + "]");
			}
			
			return loanFeeDef;
					
		} catch (Exception ex) {
			if(logger.isErrorEnabled())
				logger.error(ex.getMessage(), ex);
			throw new ProcessException(CcServProConstants.MES_SYS_PARAM_ERROR);
		}
	}
	
	/**
	 * 根据loanId查询该笔loan的还款分配计划
	 * 
	 * @param loanId
	 * @return
	 */
	public List<CcsRepaySchedule> getRepayScheduleListByLoanId(Long loanId) {
		QCcsRepaySchedule qRepaySchedule = QCcsRepaySchedule.ccsRepaySchedule;
		List<CcsRepaySchedule> list = new JPAQuery(em).from(qRepaySchedule)
				.where(qRepaySchedule.loanId.eq(loanId))
				.orderBy(qRepaySchedule.currTerm.asc())
				.list(qRepaySchedule);
		return list;
	}
	/**
	 * 根据loanId查询该笔loan的转入计划
	 * 
	 * @param loanId
	 * @return
	 */
	public List<CcsPlan> getPlanIListByLoanId(CcsLoan loan) {
		QCcsPlan qCcsPlan = QCcsPlan.ccsPlan;
		List<CcsPlan> list = new JPAQuery(em).from(qCcsPlan)
				.where(qCcsPlan.refNbr.eq(loan.getRefNbr())
						.and(qCcsPlan.acctNbr.eq(loan.getAcctNbr()))
						.and(qCcsPlan.acctType.eq(loan.getAcctType()))
						.and(qCcsPlan.planType.in(PlanType.I, PlanType.Q, PlanType.L)))
				.list(qCcsPlan);
		return list;
	}
	
	/**
	 * 根据loanId查询该笔loan的溢缴款计划
	 * @param loan
	 * @return
	 */
	public List<CcsPlan> getPlanDListByLoanId(CcsLoan loan) {
		QCcsPlan qCcsPlan = QCcsPlan.ccsPlan;
		List<CcsPlan> list = new JPAQuery(em).from(qCcsPlan)
				.where(qCcsPlan.acctNbr.eq(loan.getAcctNbr())
						.and(qCcsPlan.acctType.eq(loan.getAcctType()))
						.and(qCcsPlan.planType.eq(PlanType.D)))
				.list(qCcsPlan);
		return list;
	}
	
	/**
	 * 获取贷款指定期数的还款分配计划
	 * 
	 * @param loanId
	 * @param term
	 * @return
	 */
	public CcsRepaySchedule getRepayScheduleByTerm(Long loanId, Integer term) {
		QCcsRepaySchedule qRepaySchedule = QCcsRepaySchedule.ccsRepaySchedule;
		CcsRepaySchedule s = new JPAQuery(em).from(qRepaySchedule)
				.where(qRepaySchedule.loanId.eq(loanId).and(qRepaySchedule.currTerm.eq(term)))
				.singleResult(qRepaySchedule);
		return s;
	}
	
	/**
	 * 缩期试算结果
	 */
	public class ShortedResult{
		Integer term;
		BigDecimal monthPmt;
		LoanFeeDef loanFeeDef;
		
		public Integer getTerm() {
			return term;
		}
		public void setTerm(Integer term) {
			this.term = term;
		}
		public BigDecimal getMonthPmt() {
			return monthPmt;
		}
		public void setMonthPmt(BigDecimal monthPmt) {
			this.monthPmt = monthPmt;
		}
		public LoanFeeDef getLoanFeeDef() {
			return loanFeeDef;
		}
		public void setLoanFeeDef(LoanFeeDef loanFeeDef) {
			this.loanFeeDef = loanFeeDef;
		}
	}
	
	
	/**
	 * 试算部分提前还款后的期数及每期还款额
	 * 
	 * @param loan
	 * @param shortedPmtDue
	 * @param origPmt
	 * @param nextPrin
	 * @param diff
	 * @param i
	 * @param lastPmt
	 * @return
	 */
	public ShortedResult calcShortedResult(CcsLoan loan, BigDecimal shortedPmtDue, BigDecimal origPmt, BigDecimal nextPrin, BigDecimal diff, int i, BigDecimal lastPmt){
		LoanFeeDef loanFeeDef = this.getLoanFeeDef(loan.getLoanCode(), i, loan.getUnstmtPrin().subtract(shortedPmtDue).subtract(nextPrin),loan.getLoanFeeDefId());
		BigDecimal monthIntRt = this.calcMonthIntRt(loanFeeDef.interestRate,loan.getFloatRate());
		BigDecimal pmt = this.calcMonthPmtForMCEI(loan.getUnstmtPrin().subtract(shortedPmtDue).subtract(nextPrin), monthIntRt, i);
		BigDecimal newDiff = origPmt.subtract(pmt).abs();
		if(diff.compareTo(newDiff) >=0){
			if(i==1){
				ShortedResult rt = new ShortedResult();
				rt.setMonthPmt(pmt);
				rt.setTerm(i);// 需要期数
				return rt;
			}else{
				return calcShortedResult(loan, shortedPmtDue, origPmt, nextPrin, newDiff, i-1, pmt);
			}
		}else{
			ShortedResult rt = new ShortedResult();
			rt.setMonthPmt(lastPmt);
			rt.setTerm(i+1);// 需要期数
			return rt;
		}
	}
	
	
	private CcsRepaySchedule copySchedule(CcsRepaySchedule origNextSchedule) {
		CcsRepaySchedule s = new CcsRepaySchedule();
		s.updateFromMap(origNextSchedule.convertToMap());
		s.setScheduleId(null);
		return s;
	}
	
	/**
	 * @see 方法名：getNextPaymentDay 
	 * @see 描述：获取下期账单的还款日
	 * @see 创建日期：2015-11-21
	 * @author MengXiang
	 * @param productCd
	 * @param nextStmtDate
	 * @return nextPaymentDay
	 */
	public Date getNextPaymentDay(String productCd, Date nextStmtDate){
		
		// 还款日
		Date paymentDay = null;
		
		// 获取参数
		ProductCredit productCredit = parameterFacility.loadParameter(productCd, ProductCredit.class);
		AccountAttribute acctAttr = parameterFacility.loadParameter(productCredit.accountAttributeId, AccountAttribute.class);
		
		//根据账户属性中的还款日类型，决定哪天作为最后还款日
		switch (acctAttr.paymentDueDay){
		//最后还款日类型为账单日
		case C: 
			switch(acctAttr.cycleBaseInd){
			case W:
				paymentDay = DateUtils.addDays(nextStmtDate, acctAttr.cycleBaseMult *7); break;
			case M:
				paymentDay = DateUtils.addMonths(nextStmtDate, acctAttr.cycleBaseMult); break;
			default : throw new IllegalArgumentException("账单周期类型参数不正确");
			}
			break;
		
		//最后还款日类型为账单日后的固定天数
		//最后还款日= 上一账单日 + 固定天数
		case D: 
			//按大小月修正到期还款日期
			if(acctAttr.pmtDueDayFix!=null && acctAttr.pmtDueDayFix){
				Calendar c = Calendar.getInstance();
				c.setTime(nextStmtDate);
				int nextStmtDay = c.get(Calendar.DAY_OF_MONTH);
				//还款日期月份增量
				int pmtMonth = (nextStmtDay+acctAttr.pmtDueDays)/acctAttr.pmtDueDayFixUnit;
				//还款日期日期=min((账单周期+免息期)%30，28)
				int pmtDay = (nextStmtDay+acctAttr.pmtDueDays)%acctAttr.pmtDueDayFixUnit>28 ? 28 : (nextStmtDay+acctAttr.pmtDueDays)%acctAttr.pmtDueDayFixUnit;
				if(pmtDay==0){
					pmtDay=28;
					pmtMonth=pmtMonth-1;
				}
				
				c.add(Calendar.MONTH, pmtMonth);
				c.set(Calendar.DAY_OF_MONTH, pmtDay);
				paymentDay = c.getTime();
			}else{
				paymentDay = DateUtils.addDays(nextStmtDate, acctAttr.pmtDueDays);
				//还款日跳过月末, 29、30、31固定为2、3、4
				if(acctAttr.skipEom.equals(Indicator.Y)){
					Calendar c = Calendar.getInstance();
					c.setTime(paymentDay);
					int nextPaymentDay = c.get(Calendar.DAY_OF_MONTH);
					if(nextPaymentDay == 29){
						c.add(Calendar.MONTH, 1); 
						c.set(Calendar.DAY_OF_MONTH,2);
					}else if(nextPaymentDay == 30){
						c.add(Calendar.MONTH, 1); 
						c.set(Calendar.DAY_OF_MONTH,3);
					}else if(nextPaymentDay == 31){
						c.add(Calendar.MONTH, 1); 
						c.set(Calendar.DAY_OF_MONTH,4);
					}
					paymentDay = c.getTime();
				}
				
			}
			break;
		
		//最后还款日类型为账单日后固定日期
		case F: 
			paymentDay = DateUtils.setDays(nextStmtDate, acctAttr.pmtDueDate);
			while (paymentDay.before(nextStmtDate)){
				paymentDay = DateUtils.addMonths(paymentDay, 1);
			}
			break;
		
		//找不到值抛异常
		default : throw new IllegalArgumentException("账户属性中还款日类型不正确");
		}
		
		return paymentDay;
	}
	
	/**
	 * @see 方法名：getNextGraceDay 
	 * @see 描述：获取单个账户的下期账单的宽限日
	 * @see 创建日期：2015-6-24下午4:57:41
	 * @author ChengChun
	 *  
	 * @param productCd
	 * @param nextStmtDate
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public Date getNextGraceDay(String productCd, Date nextStmtDate){
		
		// 获取参数
		ProductCredit productCredit = parameterFacility.loadParameter(productCd, ProductCredit.class);
		AccountAttribute acctAttr = parameterFacility.loadParameter(productCredit.accountAttributeId, AccountAttribute.class);
		
		// 宽限日
		Date graceDay = null;
		
		// 下一还款日
		Date paymentDay = getNextPaymentDay(productCd, nextStmtDate);
		
		//根据账户属性中的还款日类型，决定哪天作为最后还款日
		switch (acctAttr.paymentDueDay){
		//最后还款日类型为账单日
		//当还款日类型为账单日时，不考虑宽限期
		case C: graceDay = paymentDay; break;
		
		//最后还款日类型为账单日后的固定天数或者账单日后固定日期
		//最后还款日= 还款日 + 宽限期天数
		case D: 
		case F: 
			graceDay = DateUtils.addDays(paymentDay, acctAttr.pmtGracePrd);
			break;
		
		//找不到值抛异常
		default : throw new IllegalArgumentException("账户属性中还款日类型不正确");
		}
		
		return graceDay;
	}
	
	/**
	 * 账单日跳过月末
	 * @param acctAttr
	 * @param cycleDay
	 * @return
	 */
	public Date skipEndOfMonth(AccountAttribute acctAttr,Date bizDate){
		Date fixDate = bizDate;//修正日期
		Calendar bizCal = Calendar.getInstance();
		bizCal.setTime(bizDate);
		int cycleDay = bizCal.get(Calendar.DATE);// 账单日
		
		int newCycleDay = cycleDay;
		if(logger.isDebugEnabled())
			logger.debug("cycleDay:[{}] -bizDate:[{}]- skipEom:[{}]",newCycleDay,bizDate,acctAttr.skipEom);
		
		if((Indicator.Y).equals(acctAttr.skipEom) && cycleDayMap.get(newCycleDay)!=null){
			//跳过月末
			newCycleDay = cycleDayMap.get(newCycleDay);
			Calendar fixCal = Calendar.getInstance();
			fixCal.setTime(fixDate);
			fixCal.set(Calendar.DAY_OF_MONTH, newCycleDay);
			fixCal.add(Calendar.MONTH, 1);
			
			fixDate = fixCal.getTime();
			if(logger.isDebugEnabled())
				logger.debug("跳过月末  cycleDay:[{}],fixDate:[{}]",newCycleDay,fixDate);
		}
		
		return fixDate;
		
	}
	
//	public static void main(String[] args) throws ParseException {
//		String[] aa = {"yyyyMMdd"};
//		Calendar cal = Calendar.getInstance();
//		cal.setTime(com.sunline.ccs.facility.DateUtils.parseDate("20150129", aa));
//		System.out.println(com.sunline.ccs.facility.DateUtils.formatDate2String(cal.getTime(),"yyyyMMdd"));
//		
//		MicroCreditRescheduleUtils uu = new MicroCreditRescheduleUtils();
//		AccountAttribute acctAttr = new AccountAttribute();
//		acctAttr.skipEom = Indicator.Y;
//		Date ss = uu.skipEndOfMonth(acctAttr, cal.getTime());
//		
//		System.out.println(com.sunline.ccs.facility.DateUtils.formatDate2String(ss,"yyyyMMdd"));
//		
//		System.out.println(com.sunline.ccs.facility.DateUtils.formatDate2String(DateUtils.addMonths(ss, 1),"yyyyMMdd"));
//	}

}
