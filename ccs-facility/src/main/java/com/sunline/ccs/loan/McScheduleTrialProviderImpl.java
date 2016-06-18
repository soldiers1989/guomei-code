//package com.sunline.ccs.loan;
//
//import java.math.BigDecimal;
//import java.math.RoundingMode;
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.List;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import com.sunline.ark.support.utils.DateUtils;
//import com.sunline.ccs.facility.MicroCreditRescheduleUtils;
//import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
//import com.sunline.ccs.infrastructure.shared.model.CcsRepaySchedule;
//import com.sunline.ccs.loan.McLoanProvideImpl.LoanFeeMethodimple;
//import com.sunline.ccs.loan.McLoanProvideImpl.LoanLifeInsuFeeMethodimple;
//import com.sunline.ccs.loan.McLoanProvideImpl.LoanStampTAXMethodimple;
//import com.sunline.ccs.loan.McLoanProvideImpl.LoanlnsuranceMethodimple;
//import com.sunline.ccs.param.def.AccountAttribute;
//import com.sunline.ccs.param.def.LoanFeeDef;
//import com.sunline.ccs.param.def.LoanPlan;
//import com.sunline.ccs.param.def.ProductCredit;
//import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
//import com.sunline.ppy.dictionary.enums.Indicator;
//
//@Component
//public class McScheduleTrialProviderImpl {
//	@Autowired
//	private MicroCreditRescheduleUtils rescheduleUtils;
//	@Autowired
//	private UnifiedParameterFacility parameterFacility;
//	public List<CcsRepaySchedule> getLSchedule(CcsLoanReg loanReg,LoanFeeDef loanFeeDef,Date businessDate) throws Exception {
//		LoanPlan loanPlan = parameterFacility.loadParameter(loanReg.getLoanCode(), LoanPlan.class);
//		ProductCredit product;
//
//		product = parameterFacility.loadParameter(loanPlan.productCode, ProductCredit.class);
//	
////		product.
//		AccountAttribute accountAttribute = parameterFacility.loadParameter(product.accountAttributeId, AccountAttribute.class);
//		// 获取账户
////		CcsAcct acct = queryFacility.getAcctByAcctNbr(loanReg.getAcctType(), loanReg.getAcctNbr());
////		ProductCredit productCredit = parameterFacility.loadParameter(acct.getProductCd(), ProductCredit.class);
////		AccountAttribute acctAttr =	parameterFacility.loadParameter(productCredit.accountAttributeId, AccountAttribute.class);
//		//月利率
//		BigDecimal monthIntRt = rescheduleUtils.calcMonthIntRt(loanReg.getInterestRate(),loanReg.getFloatRate());
//		//当期月本金
//		BigDecimal currMonthPrin = BigDecimal.ZERO;
//		//当期月利息
//		BigDecimal currMonthInt = BigDecimal.ZERO;
//		//当月费用
//		BigDecimal currMonthFee = BigDecimal.ZERO;
//		//保险费
//		BigDecimal currMonthInsurance = BigDecimal.ZERO;
//		//印花税
//		BigDecimal currMonthStampTAX = BigDecimal.ZERO;
//		//寿险计划包费
//		BigDecimal currMonthLifeInsuFee = BigDecimal.ZERO;
//		//当期还款日
//		Date loanPmtDueDate;
//		
//	
//		//如果loanreg表中存在手续费率使用，loanreg中的费率，否则使用参数设置
//		
//		if (loanReg.getLoanFeeMethod()  == null) {
//			loanReg.setLoanInsFeeMethod(loanFeeDef.loanFeeMethod);// 保费收取方式
//		} 
//		
//		//如果loanreg表中存在保费费率使用，loanreg中的费率，否则使用参数设置
//		
//		if (loanReg.getLoanInsFeeMethod()  == null) {
//			loanReg.setLoanInsFeeMethod(loanFeeDef.insCollMethod);// 保费收取方式
//		} 
//		if(loanReg.getInsuranceRate()== null){
//			loanReg.setInsuranceRate(loanFeeDef.insRate);
//		}
//		//如果loanreg表中存在印花税使用，loanreg中的费率，否则使用参数设置
//		if (loanReg.getStampdutyMethod() == null) {
//			loanReg.setStampdutyMethod(loanFeeDef.stampMethod);
//		} 
//		if(loanReg.getStampdutyRate() == null ){
//			loanReg.setStampdutyRate(loanFeeDef.stampRate );
//		}
//		//如果loanreg表中存在寿险计划包费使用，loanreg中的费率，否则使用参数设置
//		if (loanReg.getLifeInsuFeeMethod()  == null) {
//			loanReg.setLifeInsuFeeMethod(loanFeeDef.lifeInsuFeeMethod);
//		} 
//		if(loanReg.getLifeInsuFeeRate() == null ){
//			loanReg.setLifeInsuFeeRate(loanFeeDef.lifeInsuFeeRate );
//		}
//		
//		//总手续费
//		BigDecimal totalFee = LoanFeeMethodimple.valueOf(loanReg.getLoanFeeMethod().toString()).loanFee(loanReg, loanFeeDef,0);
//		//总保险费
//		BigDecimal totalnsurance = LoanlnsuranceMethodimple.valueOf(loanReg.getLoanInsFeeMethod().toString()).loanlnsurance(loanReg, loanFeeDef,0);
//		
//		//总印花税
//		BigDecimal totalStampTAX = BigDecimal.ZERO;
//		if(loanFeeDef.stampCustomInd.equals(Indicator.Y)){
//			totalStampTAX = LoanStampTAXMethodimple.valueOf(loanReg.getStampdutyMethod().toString()).loanStampTAX(loanReg, loanFeeDef,0);
//				
//		}
//		//总寿险计划包费
//		BigDecimal totalLifeInsuFee =  LoanLifeInsuFeeMethodimple.valueOf(loanReg.getLifeInsuFeeMethod().toString()).loanLifeInsuFee(loanReg, loanFeeDef,0);
//		
//		// 用剩余总本金作为贷款本金, 计算剩余期数(总期数-1)的等额本息每期应还款额, 剩余总本金 = 贷款总本金 - 首期本金
//		//每月应还款额
//		BigDecimal monthPmt = BigDecimal.ZERO;
//		BigDecimal firstMonthPrin = BigDecimal.ZERO;
////		if(Indicator.Y.equals(acctAttr.isMergeBillDay)){
//			// 首期本金
////			firstMonthPrin = rescheduleUtils.calcCurrMonthPrinForMCEI(loanReg.getLoanInitPrin(), monthIntRt, loanReg.getLoanInitTerm(), 1);
////			monthPmt = rescheduleUtils.calcMonthPmtForMCEI(loanReg.getLoanInitPrin().subtract(firstMonthPrin), monthIntRt, loanReg.getLoanInitTerm()-1);
////		}else{
////			//不合并账单日的情况，首期的本金和利息同非末期
//		if(monthIntRt.compareTo(BigDecimal.ZERO)<=0)
//			monthPmt = loanReg.getLoanInitPrin().divide(BigDecimal.valueOf(loanReg.getLoanInitTerm()),6);
//		else
//			monthPmt = rescheduleUtils.calcMonthPmtForMCEI(loanReg.getLoanInitPrin(), monthIntRt, loanReg.getLoanInitTerm());
////		}
//		
//		BigDecimal pmtPrin = BigDecimal.ZERO;
//		BigDecimal pmtFee = BigDecimal.ZERO;
//		BigDecimal pmtlnsurance = BigDecimal.ZERO;
//		BigDecimal pmtStampTAX = BigDecimal.ZERO;
//		BigDecimal pmtLifeInsuFee = BigDecimal.ZERO;
//		
//		List<CcsRepaySchedule> ccsRepayScheduleList = new ArrayList<CcsRepaySchedule>();
//		for (int i = 1; i <= loanReg.getLoanInitTerm(); i++) {
//			// 首期以日计息
//			//如果账户上使用的合并账单日，则第一期的利息特殊处理
//			//TODO
//			if(i==1){
////				if(Indicator.Y.equals(acctAttr.isMergeBillDay)){
////					BigDecimal dayIntRt = rescheduleUtils.calcDayIntRt(loanReg.getInterestRate());
////					int firstTermDays = DateUtils.getIntervalDays(batDate, acct.getNextStmtDate()) +1; // 计息天数=间隔天数+1
////					currMonthInt = loanReg.getLoanInitPrin().multiply(dayIntRt).multiply(new BigDecimal(firstTermDays)).setScale(2, RoundingMode.HALF_UP); // 利息
////					currMonthPrin = firstMonthPrin;
////				}else{
//					//不合并账单日的情况，首期的本金和利息同非末期
//					currMonthPrin = rescheduleUtils.calcCurrMonthPrinForMCEI(loanReg.getLoanInitPrin(), monthIntRt, loanReg.getLoanInitTerm(), i);
//					currMonthInt = monthPmt.subtract(currMonthPrin).setScale(2, RoundingMode.HALF_UP);
//					firstMonthPrin = currMonthPrin;
////				}
//				
//				currMonthFee = LoanFeeMethodimple.valueOf(loanReg.getLoanFeeMethod().toString()).loanFee(loanReg, loanFeeDef,2);
//				//保费计算
//				currMonthInsurance = LoanlnsuranceMethodimple.valueOf(loanReg.getLoanInsFeeMethod().toString()).loanlnsurance(loanReg, loanFeeDef,2);
//				//印花税计算
//				if(loanFeeDef.stampCustomInd.equals(Indicator.Y)){
//					currMonthStampTAX = LoanStampTAXMethodimple.valueOf(loanReg.getStampdutyMethod().toString()).loanStampTAX(loanReg, loanFeeDef,2);
//				}
//				//寿险计划包费计算
//				currMonthLifeInsuFee = LoanLifeInsuFeeMethodimple.valueOf(loanReg.getLifeInsuFeeMethod().toString()).loanLifeInsuFee(loanReg, loanFeeDef,2);
//				
//				
//				//落地 4舍五入
//				currMonthPrin =  currMonthPrin.setScale(2, RoundingMode.HALF_UP); 
//				pmtPrin = currMonthPrin;
//				pmtFee = currMonthFee;
//				pmtlnsurance = currMonthInsurance;
//				pmtStampTAX = currMonthStampTAX;
//				pmtLifeInsuFee = currMonthLifeInsuFee;
//			}else{
////				if(Indicator.Y.equals(acctAttr.isMergeBillDay)){
////					currMonthPrin = rescheduleUtils.calcCurrMonthPrinForMCEI(loanReg.getLoanInitPrin().subtract(firstMonthPrin), monthIntRt, loanReg.getLoanInitTerm()-1, i-1);
////				}else{
//					currMonthPrin = rescheduleUtils.calcCurrMonthPrinForMCEI(loanReg.getLoanInitPrin(), monthIntRt, loanReg.getLoanInitTerm(), i);
////				}
//				currMonthFee = LoanFeeMethodimple.valueOf(loanReg.getLoanFeeMethod().toString()).loanFee(loanReg, loanFeeDef,1);
//				
//				currMonthInt = monthPmt.subtract(currMonthPrin).setScale(2, RoundingMode.HALF_UP);
//				//保费计算
//				currMonthInsurance = LoanlnsuranceMethodimple.valueOf(loanReg.getLoanInsFeeMethod().toString()).loanlnsurance(loanReg, loanFeeDef,1);
//				//印花税计算
//				if(loanFeeDef.stampCustomInd.equals(Indicator.Y)){
//					currMonthStampTAX = LoanStampTAXMethodimple.valueOf(loanReg.getStampdutyMethod().toString()).loanStampTAX(loanReg, loanFeeDef,1);
//				}
//				//寿险计划包费计算
//				currMonthLifeInsuFee = LoanLifeInsuFeeMethodimple.valueOf(loanReg.getLifeInsuFeeMethod().toString()).loanLifeInsuFee(loanReg, loanFeeDef,1);
//				
//				//落地 4舍五入
//				currMonthPrin =  currMonthPrin.setScale(2, RoundingMode.HALF_UP); 
//				
//				pmtPrin = pmtPrin.add(currMonthPrin);
//				pmtFee = pmtFee.add(currMonthFee);
//				pmtlnsurance = pmtlnsurance.add(currMonthInsurance);
//				pmtStampTAX = pmtStampTAX.add(currMonthStampTAX);
//				pmtLifeInsuFee = pmtLifeInsuFee.add(currMonthLifeInsuFee);
//				
//				
//				// 末期补差
//				if(i == loanReg.getLoanInitTerm()){
//					BigDecimal fixPrin = loanReg.getLoanInitPrin().subtract(pmtPrin); // 本金修正
//					currMonthPrin = currMonthPrin.add(fixPrin);
//					BigDecimal fixInt = fixPrin.multiply(monthIntRt).multiply(new BigDecimal(i)).setScale(2, RoundingMode.HALF_UP); // 利息修正
//					currMonthInt = currMonthInt.add(fixInt);
//					
//					BigDecimal fixFee = totalFee.subtract(pmtFee); // 手续费修正
//					currMonthFee = currMonthFee.add(fixFee);
//					
//					BigDecimal fixlnsurance = totalnsurance.subtract(pmtlnsurance); // 保费修正
//					currMonthInsurance = currMonthInsurance.add(fixlnsurance);
//					
//					BigDecimal fixStampTAX = totalStampTAX.subtract(pmtStampTAX); // 印花税修正
//					currMonthStampTAX = currMonthStampTAX.add(fixStampTAX);
//					
//					BigDecimal fixLifeInsuFee = totalLifeInsuFee.subtract(pmtLifeInsuFee); // 寿险计划包费修正
//					currMonthLifeInsuFee = currMonthLifeInsuFee.add(fixLifeInsuFee);
//					
//					currMonthPrin =  currMonthPrin.setScale(2, RoundingMode.HALF_UP); 
//				}
//			}
//			//账单日暂时使用默认还款日
////			Calendar stmtCldr = Calendar.getInstance();
////			stmtCldr.setTime(businessDate);
////			stmtCldr.set(Calendar.DAY_OF_MONTH, product.dfltCycleDay);
////			if(stmtCldr.get(Calendar.DAY_OF_MONTH)> product.dfltCycleDay){
////				stmtCldr.set(Calendar.MONTH, 1);
////			}
//			//下一个账单日为初次放款日期的下一个月当天
//			loanPmtDueDate = rescheduleUtils.getLoanPmtDueDate(businessDate, loanFeeDef, 2);
//			ccsRepayScheduleList.add(
//					generateRepaySchedule(loanReg, null, i, currMonthPrin, currMonthFee,currMonthInt,
//							currMonthInsurance,currMonthStampTAX,currMonthLifeInsuFee, loanPmtDueDate ));
//		}
//		return ccsRepayScheduleList;
//	}
//	
//	public CcsRepaySchedule generateRepaySchedule(CcsLoanReg loanReg, Long loanId, 
//			Integer currTerm, BigDecimal loanTermPrin, BigDecimal loanTermFee, BigDecimal loanTermInterest,BigDecimal currMonthInsurance,BigDecimal currMonthStampduty,BigDecimal currMonthLifeInsuFee, Date loanPmtDueDate ) {
//		CcsRepaySchedule schedule = new CcsRepaySchedule();
//		
////		schedule.setScheduleId(); //自增
//		schedule.setOrg(loanReg.getOrg());
//		schedule.setLoanId(loanId);
//		schedule.setAcctNbr(loanReg.getAcctNbr());
//		schedule.setAcctType(loanReg.getAcctType());
//		schedule.setLogicCardNbr(loanReg.getLogicCardNbr());
//		schedule.setCardNbr(loanReg.getCardNbr());
//		schedule.setLoanInitPrin(loanReg.getLoanInitPrin());
//		schedule.setLoanInitTerm(loanReg.getLoanInitTerm());
//		schedule.setCurrTerm(currTerm);
//		schedule.setLoanTermPrin(loanTermPrin);
//		schedule.setLoanTermFee(loanTermFee); // 目前业务没有额外费用
//		schedule.setLoanTermInt(loanTermInterest);
//		schedule.setLoanPmtDueDate(loanPmtDueDate);
//		
//		//赋值保险费，印花税，寿险计划包费
//		schedule.setLoanInsuranceAmt(currMonthInsurance);
//		schedule.setLoanStampdutyAmt(currMonthStampduty);
//		schedule.setLoanLifeInsuAmt(currMonthLifeInsuFee);
//		
////		AccountAttribute acctAttr = productFacility.CardNoTOAccountAttribute(loanReg.getCardNbr(), loanReg.getAcctType().getCurrencyCode());
////		schedule.setLoanGraceDate(DateUtils.addDays(loanPmtDueDate, acctAttr.pmtGracePrd));
//		
//		return schedule;
//	}
//
//}
