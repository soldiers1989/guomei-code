package com.sunline.ccs.loan;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.rmi.server.RMIClassLoader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.facility.Card2ProdctAcctFacility;
import com.sunline.ccs.facility.DateUtils;
import com.sunline.ccs.facility.MicroCreditRescheduleUtils;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctOKey;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.CcsRepaySchedule;
import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.ccs.param.def.EarlyRepayDef;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.ReplaceEarlyRepayDef;
import com.sunline.ccs.param.def.enums.FloatRate;
import com.sunline.ccs.param.def.enums.PrepaymentFeeMethod;
import com.sunline.pcm.param.def.Mulct;
import com.sunline.pcm.param.def.MulctDef;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanAction;
import com.sunline.ppy.dictionary.enums.LoanMold;
import com.sunline.ppy.dictionary.enums.LoanRegStatus;
import com.sunline.ppy.dictionary.enums.LoanUsage;
import com.sunline.ppy.dictionary.enums.PlanType;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * ，用于贷款的处理
 * 等额本息
 * @author 刘启
 *
 */
@Component
public class McLoanProvideImpl {

	@Autowired
	private MicroCreditRescheduleUtils rescheduleUtils;
	@Autowired
	private UnifiedParamFacilityProvide unifiedParamFacilityProvide;
	
    @Autowired
	private UnifiedParameterFacility parameterFacility;
	@Autowired
	private Card2ProdctAcctFacility cardProctFacility;
	
	@PersistenceContext
	private EntityManager em;
	
//	@Autowired
//	private final static int YEAR_MONTHS = 12;
//	@Autowired
//	private final static String EOM = "29,30,31";

	/**
	 * 生成贷款接口文件
	 * @param term 贷款期数
	 * @param initPrin 贷款总金额
	 * @param refNbr  
	 * @param logicCardNbr 逻辑号
	 * @param cardNbr  卡号
	 * @param LoanPlan
	 * @param acctNbr TODO
	 * @param acctType TODO
	 * @return
	 * @throws ProcessException   
	 * @exception   
	 * @since  1.0.0
	 */
	public CcsLoanReg genLoanReg(Integer term, BigDecimal initPrin, String refNbr, String logicCardNbr, 
			String cardNbr, LoanPlan loanPlan, Long acctNbr, AccountType acctType, String loanCode, Date busDate,BigDecimal insuranceRate) throws ProcessException {
		LoanFeeDef loanFeeDef = rescheduleUtils.getLoanFeeDef(loanPlan.loanCode, term, initPrin,null);
		CcsLoanReg loanReg = new CcsLoanReg();
		loanReg.setOrg(OrganizationContextHolder.getCurrentOrg());
		loanReg.setAcctNbr(acctNbr);
		loanReg.setAcctType(acctType);
		loanReg.setRegisterDate(busDate);
		loanReg.setRequestTime(new Date());
		loanReg.setLogicCardNbr(logicCardNbr);
		loanReg.setCardNbr(cardNbr);
		loanReg.setRefNbr(refNbr);
		loanReg.setLoanType(loanPlan.loanType);
		loanReg.setLoanRegStatus(LoanRegStatus.A);
		loanReg.setLoanInitTerm(term);// 分期期数
		loanReg.setLoanInitPrin(initPrin);// 分期总本金
		loanReg.setLoanCode(loanCode);//分期计划代码
		loanReg.setLoanAction(LoanAction.A);
		loanReg.setMatched(Indicator.N);
		loanReg.setInterestRate(loanFeeDef.feeRate);
		//申请信息如果有保费费率,印花税率、寿险计划包费率，保存到reg中
		//这里不考虑参数变更
		loanReg.setInsuranceRate(insuranceRate);
		unifiedParamFacilityProvide.BusinessDate();
		

		return loanReg;
	}
	
	/**
	 * 获取贷款的欠款
	 * @param CcsLoan loan
	 * @return
	 * @throws ProcessException   
	 * @exception   
	 * @since  1.0.0
	 */
	public BigDecimal genLoanBal(CcsLoan loan) throws ProcessException {
		BigDecimal currbal = BigDecimal.ZERO;
		if(loan == null){
			return currbal;
		}
		List<CcsPlan> plans = rescheduleUtils.getPlanIListByLoanId(loan);
		
		if(plans != null){
			for(CcsPlan plan : plans){
				currbal = currbal.add(plan.getCurrBal());
			}
		}
		
		return currbal;
	}
	
	/**
	 * 获取贷款的溢缴款
	 * @param CcsLoan loan
	 * @return
	 * @throws ProcessException   
	 * @exception   
	 * @since  1.0.0
	 */
	public BigDecimal genLoanDeposit(CcsLoan loan) throws ProcessException {
		BigDecimal currbal = BigDecimal.ZERO;
		if(loan == null){
			return currbal;
		}
		List<CcsPlan> plans = rescheduleUtils.getPlanDListByLoanId(loan);
		
		if(plans != null){
			for(CcsPlan plan : plans){
				currbal = currbal.add(plan.getCurrBal().abs());
			}
		}
		
		return currbal;
	}
	
	
	/**
	 * 获取小额贷期数<br>
	 * <b>判断期数是否在计价方式区间内</b>
	 * @param loanInitTerm
	 * @param loanPlan
	 * @return
	 * @throws AuthException
	 */
/*	public LoanFeeDef getLoanFeeDef(Integer loanInitTerm, LoanPlan loanPlan) throws ProcessException{

		if (loanPlan.loanType == LoanType.MCEI || loanPlan.loanType == LoanType.MCEP) {
			if ((loanPlan.minCycle != null && loanInitTerm < loanPlan.minCycle) || (loanPlan.maxCycle != null && loanInitTerm > loanPlan.maxCycle)) {
				throw new ProcessException("报文上送的小额贷期数不在项目活动小额贷期数列表中");
			}
		}
		
		int key = -1;
		for (int i : loanPlan.loanFeeDefMap.keySet()) {
			// 期数相等直接返回
			if (i == loanInitTerm) {
				key = i;
				break;
			} else {
				// 判断期数，获取当前期数的下一个期数
				if (i > loanInitTerm) {
					if (key == -1) {
						key = i;
					} else {
						if (i < key) {
							key = i;
						}
					}
				}
			}
		}
		if (key == -1) {
			throw new ProcessException("报文上送的小额贷期数不在项目活动小额贷期数列表中");
		}
		return loanPlan.loanFeeDefMap.get(key);
	}*/
	
	/**
	 * 获取loanReg的浮动比例
	 * D=下浮
	 * U=上浮
	 * N=不浮动
	 * 空格=取账户浮动比例
	 * 
	 * @param ipVal
	 * @param acctFloatRate
	 * @return
	 * @throws AuthException
	 */
	public BigDecimal getFlatRate(String ipVal , BigDecimal acctFloatRate) throws ProcessException{
		// 获取浮动比例标识
		String frInd = ipVal.substring(32, 33).trim();
		if (StringUtils.isBlank(frInd)) {
			return acctFloatRate;
		}
		try {
			FloatRate floatRateInd = FloatRate.valueOf(frInd.toUpperCase());
			BigDecimal floatRate = new BigDecimal(ipVal.substring(33, 38).trim()).movePointLeft(4);
			// 上浮下浮判断并赋值
			if (floatRateInd == FloatRate.D) {
				return floatRate.negate();
			} else if (floatRateInd == FloatRate.U) {
				return floatRate;
			} else if (floatRateInd == FloatRate.N) {
				return BigDecimal.ZERO;
			}
		} catch (Exception e) {
			throw new ProcessException("报文上送的小额贷浮动比例异常");
		}
		return acctFloatRate;
	}

	
	/**
	 * 根据loanreg 生成对应的还款计划列表
	 * @param loanReg 贷款注册信息
	 * @param loanFeeDef 贷款对应期数的收费参数
	 * @param batDate 贷款建立日期
	 * @return
	 * @throws Exception
	
		等额本息还款公式：
		    基础公式1：月均还款 ＝ 贷款额*月利率*(1+月利率)^总月数/[(1+月利率)^总月数-1]
		    基础公式2：第n月本金 = 贷款额*月利率(1+月利率)^(第n月-1)/[(1+月利率)^总月数-1]
		    基础公式3：月利率 = 年利率* 1/12
		    基础公式4：日利率 = 年利率* 1/360
		    基础公式5：保费总还款额 =  贷款额*保费费率
		    基础公式6：保费月还款额 =  保费总还款额/期数
		    基础公式7：印花税总还款额 =  贷款额*印花税率
		    基础公式8：寿险计划包费总还款额 =  贷款额*寿险计划包费率
		    基础公式9：寿险计划包费月还款额 =  寿险计划包费总还款额/期数
		    基础公式10：代收服务费总还款额 =  贷款额*代收服务费率
		    基础公式11：代收服务费月还款额 =  代收服务费总还款额/期数
		    基础公式12：手续费总还款额 =  贷款额*手续费率
		    基础公式13：手续费月还款额 =  手续费总还款额/期数
		    基础公式14：服务费总还款额 =  贷款额*服务费率
		    基础公式15：服务费月还款额 =  服务费总还款额/期数
		    本金金额四舍五入, 精确到分
		    利率四舍五入, 精确到小数点后20位
		    利息金额四舍五入, 精确到分
		    保费金额四舍五入, 精确到分
		    印花税金额四舍五入, 精确到角
		    寿险计划包费金额四舍五入, 精确到分
		    代收服务费金额四舍五入, 精确到分
		    服务费金额四舍五入, 精确到分
		    手续费金额四舍五入, 精确到分
		    
		              
		    第一步：计算首期本金、首期利息
		        这里首期区分是否合并账单日，目前都采用不合并账单日（主要区分在利息上）
		        贷款额 = 贷款总本金 = B
		        总月数 = 贷款总期数
		        首期本金：由公式2 = B1
		        合并账单日：
		              首期利息 = 贷款额*日利率*天数 = X1
		        不合并账单日
		                月均还款额 = 由公式1 = BX
		                首期利息 = BX - B1 = X1
		        首期保费：由公式6 = B2
		        印花税首期收取：由公式7 = B3
		        首期寿险计划包费：由公式9 = B4
		        首期代收服务费：由公式11 = B5
		        首期手续费：由公式13 = B6
		        首期服务费：由公式15 = B7
		                
		    第二步：其他月（非首期和末期每期）还款本金、利息（等同于不合并账单日的首期）
		        贷款额 = 贷款总本金 = B
		        总月数 = 贷款总期数
		        其他月均还款 ＝ 由公式1 = BX'
		        其他月还贷本金 = 由公式2 = B'
		        其他月利息 = BX' - B' = X'
		        其他月保费：由公式6 = B2
		        其他月寿险计划包费：由公式9 = B4
		        其他月代收服务费：由公式11 = B5
		        其他月手续费：由公式13 = B6
		        其他月服务费：由公式15 = B7
		              
		    第三步：末期本金补差、利息补差
		        末期本金补差 = 贷款总本金 - 每期本金汇总 = FB1
		        末期利息补差 = 末期本金补差 *月利率 *贷款总期数 = FX 
		        末期保费补差 = 贷款总保费 - 每期保费汇总 = FB2
		        末期寿险计划包费补差 = 贷款总寿险计划包费 - 每期寿险计划包费汇总 = FB4
		        末期代收服务费补差：贷款总代收服务费 - 每期代收服务费汇总 = FB5
		        末期手续费补差：贷款总手续费 - 每期手续费汇总 = FB6
		        末期服务费补差：贷款总服务费 - 每期服务费汇总 = FB7
		        末期本金 = B'+ FB1
		        末期利息 = X' + FX
		        末期保费 = B2 + FB2
		        末期印花税 = B3 + FB3
		        末期增值服务费 = B4 +  FB4
		        末期代收服务费 = B5 +  FB5
		        末期手续费 = B6 +  FB6
		        末期服务费 = B7 +  FB7
		*/
   
	public List<CcsRepaySchedule> getLSchedule(CcsLoanReg loanReg,LoanFeeDef loanFeeDef,Date batDate,CcsAcct acct) throws Exception {
		
		// 获取账户
		ProductCredit productCredit = parameterFacility.loadParameter(acct.getProductCd(), ProductCredit.class);
		AccountAttribute acctAttr =	parameterFacility.loadParameter(productCredit.accountAttributeId, AccountAttribute.class);
		//月利率
		BigDecimal monthIntRt = rescheduleUtils.calcMonthIntRt(loanReg.getInterestRate(),loanReg.getFloatRate());
		//当期月本金
		BigDecimal currMonthPrin = BigDecimal.ZERO;
		//当期月利息
		BigDecimal currMonthInt = BigDecimal.ZERO;
		//当月手续费用
		BigDecimal currMonthInstallmentFee = BigDecimal.ZERO;
		//当月服务费用
		BigDecimal currMonthSVCFee = BigDecimal.ZERO;
		//保险费
		BigDecimal currMonthInsurance = BigDecimal.ZERO;
		//印花税
		BigDecimal currMonthStampTAX = BigDecimal.ZERO;
		//寿险计划包费
		BigDecimal currMonthLifeInsuFee = BigDecimal.ZERO;
		//当月代收服务费用
		BigDecimal currMonthReplaceSvcFee = BigDecimal.ZERO;
		//当月灵活还款计划包
		BigDecimal currMonthPrepayPkg = BigDecimal.ZERO;
		//当期还款日
		Date loanPmtDueDate;
	
		//如果loanreg表中存在手续费率使用，loanreg中的费率，否则使用参数设置
		if(Indicator.Y.equals(loanReg.getAgreementRateInd())){
			loanReg = setLoanRegRate(loanReg, loanFeeDef,true);
		}else{
			loanReg = setLoanRegRate(loanReg, loanFeeDef, false);
		}
		if (loanReg.getLoanFeeMethod()  == null) {
			loanReg.setLoanInsFeeMethod(loanFeeDef.loanFeeMethod);// 保费收取方式
		} 
		//如果服务费
		if (loanReg.getSvcfeeMethod() == null) {
			loanReg.setSvcfeeMethod(loanFeeDef.loanFeeMethod);
		} 
		
		if (loanReg.getLoanInsFeeMethod()  == null) {
			loanReg.setLoanInsFeeMethod(loanFeeDef.insCollMethod);// 保费收取方式
		} 
		//如果loanreg表中存在印花税使用，loanreg中的费率，否则使用参数设置
		if (loanReg.getStampdutyMethod() == null) {
			loanReg.setStampdutyMethod(loanFeeDef.stampMethod);
		} 
		//如果loanreg表中存在寿险计划包费使用，loanreg中的费率，否则使用参数设置
		if (loanReg.getLifeInsuFeeMethod()  == null) {
			loanReg.setLifeInsuFeeMethod(loanFeeDef.lifeInsuFeeMethod);
		} 
		
		//如果loanReg中不存在代收服务费的收取方式，则取loanFeeDef的参数设置
		if (loanReg.getReplaceSvcFeeMethod()  == null) {
			loanReg.setReplaceSvcFeeMethod(loanFeeDef.replaceFeeMethod);
		} 
		
		if(loanReg.getPrepayPkgFeeMethod() == null){
			loanReg.setPrepayPkgFeeMethod(loanFeeDef.prepayPkgFeeMethod);
		}
		
		//总手续费
		BigDecimal totalInstallmentFeeFee = LoanInstallmentFeeMethodimple.valueOf(loanReg.getLoanFeeMethod().toString()).loanInstallmentFee(loanReg, loanFeeDef,0);
		//总服务费
		BigDecimal totalSVCFee = LoanSVCFeeMethodimple.valueOf(loanReg.getLoanFeeMethod().toString()).loanSVCFee(loanReg, loanFeeDef,0);
				
		//总保险费
		BigDecimal totalnsurance = LoanlnsuranceMethodimple.valueOf(loanReg.getLoanInsFeeMethod().toString()).loanlnsurance(loanReg, loanFeeDef,0);
		
		//总代收服务费
		BigDecimal totalReplaceSvcFee = LoanReplaceSvcFeeMethodimple.valueOf(loanReg.getReplaceSvcFeeMethod().toString()).loanReplaceSvcFee(loanReg, loanFeeDef,0);
		//BigDecimal totalReplaceSvcFee = BigDecimal.ZERO;
		
		//总灵活还款计划包
		BigDecimal totalPrepayPkg = BigDecimal.ZERO;
		if(loanReg.getPrepayPkgInd() == Indicator.Y){
			totalPrepayPkg = PrepayPkgMethodimple.valueOf(loanReg.getPrepayPkgFeeMethod().toString()).loanPrepayPkg(loanReg, loanFeeDef, 0);
		}
		
		//总印花税
		BigDecimal totalStampTAX = BigDecimal.ZERO;
		if(loanFeeDef.stampCustomInd.equals(Indicator.Y)){
			totalStampTAX = LoanStampTAXMethodimple.valueOf(loanReg.getStampdutyMethod().toString()).loanStampTAX(loanReg, loanFeeDef,0);
				
		}
		
		//总寿险计划包费,增加"购买寿险计划包标识",购买时收取,否则置为0
		BigDecimal totalLifeInsuFee = BigDecimal.ZERO;
		if(loanReg.getJoinLifeInsuInd() != null && loanReg.getJoinLifeInsuInd() == Indicator.Y){
			totalLifeInsuFee =  LoanLifeInsuFeeMethodimple.valueOf(loanReg.getLifeInsuFeeMethod().toString()).loanLifeInsuFee(loanReg, loanFeeDef,0);			
		}
		
		// 用剩余总本金作为贷款本金, 计算剩余期数(总期数-1)的等额本息每期应还款额, 剩余总本金 = 贷款总本金 - 首期本金
		//每月应还款额
		BigDecimal monthPmt = BigDecimal.ZERO;
		BigDecimal firstMonthPrin = BigDecimal.ZERO;
		if(Indicator.Y.equals(acctAttr.isMergeBillDay)){
			// 首期本金
			firstMonthPrin = rescheduleUtils.calcCurrMonthPrinForMCEI(loanReg.getLoanInitPrin(), monthIntRt, loanReg.getLoanInitTerm(), 1);
			monthPmt = rescheduleUtils.calcMonthPmtForMCEI(loanReg.getLoanInitPrin().subtract(firstMonthPrin), monthIntRt, loanReg.getLoanInitTerm()-1);
		}else{
			//不合并账单日的情况，首期的本金和利息同非末期
			monthPmt = rescheduleUtils.calcMonthPmtForMCEI(loanReg.getLoanInitPrin(), monthIntRt, loanReg.getLoanInitTerm());
		}
		
		BigDecimal pmtPrin = BigDecimal.ZERO;
		BigDecimal pmtFee = BigDecimal.ZERO;
		BigDecimal pmtlnsurance = BigDecimal.ZERO;
		BigDecimal pmtStampTAX = BigDecimal.ZERO;
		BigDecimal pmtLifeInsuFee = BigDecimal.ZERO;
		BigDecimal pmtSVCFee = BigDecimal.ZERO;
		BigDecimal pmtReplaceSvcFee = BigDecimal.ZERO;
		BigDecimal pmtPrepayPkg = BigDecimal.ZERO;
		
		List<CcsRepaySchedule> ccsRepayScheduleList = new ArrayList<CcsRepaySchedule>();
		for (int i = 1; i <= loanReg.getLoanInitTerm(); i++) {
			// 首期以日计息
			//如果账户上使用的合并账单日，则第一期的利息特殊处理
			//TODO
			if(i==1){
				if(Indicator.Y.equals(acctAttr.isMergeBillDay)){
					BigDecimal dayIntRt = rescheduleUtils.calcDayIntRt(loanReg.getInterestRate());
					int firstTermDays = DateUtils.getIntervalDays(batDate, acct.getNextStmtDate()) +1; // 计息天数=间隔天数+1
					currMonthInt = loanReg.getLoanInitPrin().multiply(dayIntRt).multiply(new BigDecimal(firstTermDays)).setScale(2, RoundingMode.HALF_UP); // 利息
					currMonthPrin = firstMonthPrin;
				}else{
					//不合并账单日的情况，首期的本金和利息同非末期
					currMonthPrin = rescheduleUtils.calcCurrMonthPrinForMCEI(loanReg.getLoanInitPrin(), monthIntRt, loanReg.getLoanInitTerm(), i);
					currMonthInt = monthPmt.subtract(currMonthPrin).setScale(2, RoundingMode.HALF_UP);
					firstMonthPrin = currMonthPrin;
				}
				
				currMonthInstallmentFee = LoanInstallmentFeeMethodimple.valueOf(loanReg.getLoanFeeMethod().toString()).loanInstallmentFee(loanReg, loanFeeDef,2);
				currMonthSVCFee = LoanSVCFeeMethodimple.valueOf(loanReg.getSvcfeeMethod().toString()).loanSVCFee(loanReg, loanFeeDef,2);
				
				//保费计算
				currMonthInsurance = LoanlnsuranceMethodimple.valueOf(loanReg.getLoanInsFeeMethod().toString()).loanlnsurance(loanReg, loanFeeDef,2);
				//印花税计算
				if(loanFeeDef.stampCustomInd.equals(Indicator.Y)){
					currMonthStampTAX = LoanStampTAXMethodimple.valueOf(loanReg.getStampdutyMethod().toString()).loanStampTAX(loanReg, loanFeeDef,2);
				}
				//寿险计划包费计算,增加"购买寿险计划包标识",购买时收取,否则置为0
				if(loanReg.getJoinLifeInsuInd() != null && loanReg.getJoinLifeInsuInd() == Indicator.Y){
					currMonthLifeInsuFee = LoanLifeInsuFeeMethodimple.valueOf(loanReg.getLifeInsuFeeMethod().toString()).loanLifeInsuFee(loanReg, loanFeeDef,2);				
				}
				
				// 代收服务费计算
				currMonthReplaceSvcFee = LoanReplaceSvcFeeMethodimple.valueOf(loanReg.getReplaceSvcFeeMethod().toString()).loanReplaceSvcFee(loanReg, loanFeeDef,2);
				
				if(loanReg.getPrepayPkgInd() == Indicator.Y){
					currMonthPrepayPkg = PrepayPkgMethodimple.valueOf(loanReg.getPrepayPkgFeeMethod().toString()).loanPrepayPkg(loanReg, loanFeeDef, 2);
				}
				//落地 4舍五入
				currMonthPrin =  currMonthPrin.setScale(2, RoundingMode.HALF_UP); 
				pmtPrin = currMonthPrin;
				pmtFee = currMonthInstallmentFee;
				pmtSVCFee = currMonthSVCFee;
				pmtlnsurance = currMonthInsurance;
				pmtStampTAX = currMonthStampTAX;
				pmtLifeInsuFee = currMonthLifeInsuFee;
				pmtReplaceSvcFee = currMonthReplaceSvcFee;
				pmtPrepayPkg = currMonthPrepayPkg;
			}else{
				if(Indicator.Y.equals(acctAttr.isMergeBillDay)){
					currMonthPrin = rescheduleUtils.calcCurrMonthPrinForMCEI(loanReg.getLoanInitPrin().subtract(firstMonthPrin), monthIntRt, loanReg.getLoanInitTerm()-1, i-1);
				}else{
					currMonthPrin = rescheduleUtils.calcCurrMonthPrinForMCEI(loanReg.getLoanInitPrin(), monthIntRt, loanReg.getLoanInitTerm(), i);
				}
				currMonthInstallmentFee = LoanInstallmentFeeMethodimple.valueOf(loanReg.getLoanFeeMethod().toString()).loanInstallmentFee(loanReg, loanFeeDef,1);
				currMonthSVCFee = LoanSVCFeeMethodimple.valueOf(loanReg.getSvcfeeMethod().toString()).loanSVCFee(loanReg, loanFeeDef,1);
				
				currMonthInt = monthPmt.subtract(currMonthPrin).setScale(2, RoundingMode.HALF_UP);
				//保费计算
				currMonthInsurance = LoanlnsuranceMethodimple.valueOf(loanReg.getLoanInsFeeMethod().toString()).loanlnsurance(loanReg, loanFeeDef,1);
				//印花税计算
				if(loanFeeDef.stampCustomInd.equals(Indicator.Y)){
					currMonthStampTAX = LoanStampTAXMethodimple.valueOf(loanReg.getStampdutyMethod().toString()).loanStampTAX(loanReg, loanFeeDef,1);
				}
				//寿险计划包费计算,增加"购买寿险计划包标识",购买时收取,否则置为0
				if(loanReg.getJoinLifeInsuInd() != null && loanReg.getJoinLifeInsuInd() == Indicator.Y){
					currMonthLifeInsuFee = LoanLifeInsuFeeMethodimple.valueOf(loanReg.getLifeInsuFeeMethod().toString()).loanLifeInsuFee(loanReg, loanFeeDef,1);					
				}
				
				// 代收服务费计算
				currMonthReplaceSvcFee = LoanReplaceSvcFeeMethodimple.valueOf(loanReg.getReplaceSvcFeeMethod().toString()).loanReplaceSvcFee(loanReg, loanFeeDef,1);
				
				if(loanReg.getPrepayPkgInd() == Indicator.Y){
					currMonthPrepayPkg = PrepayPkgMethodimple.valueOf(loanReg.getPrepayPkgFeeMethod().toString()).loanPrepayPkg(loanReg, loanFeeDef, 1);
				}
				
				//落地 4舍五入
				currMonthPrin =  currMonthPrin.setScale(2, RoundingMode.HALF_UP); 
				pmtPrin = pmtPrin.add(currMonthPrin);
				pmtFee = pmtFee.add(currMonthInstallmentFee);
				pmtlnsurance = pmtlnsurance.add(currMonthInsurance);
				pmtStampTAX = pmtStampTAX.add(currMonthStampTAX);
				pmtLifeInsuFee = pmtLifeInsuFee.add(currMonthLifeInsuFee);
				pmtSVCFee = pmtSVCFee.add(currMonthSVCFee);
				pmtReplaceSvcFee = pmtReplaceSvcFee.add(currMonthReplaceSvcFee);
				pmtPrepayPkg = pmtPrepayPkg.add(currMonthPrepayPkg);
				
				// 末期补差
				if(i == loanReg.getLoanInitTerm()){
					BigDecimal fixPrin = loanReg.getLoanInitPrin().subtract(pmtPrin); // 本金修正
					currMonthPrin = currMonthPrin.add(fixPrin);
					BigDecimal fixInt = fixPrin.multiply(monthIntRt).multiply(new BigDecimal(i)).setScale(2, RoundingMode.HALF_UP); // 利息修正
					currMonthInt = currMonthInt.add(fixInt);
					
					BigDecimal fixFee = totalInstallmentFeeFee.subtract(pmtFee); // 手续费修正
					currMonthInstallmentFee = currMonthInstallmentFee.add(fixFee);
					
					BigDecimal fixSVCFee = totalSVCFee.subtract(pmtSVCFee); // 服务 费修正
					currMonthSVCFee = currMonthSVCFee.add(fixSVCFee);
					
					BigDecimal fixlnsurance = totalnsurance.subtract(pmtlnsurance); // 保费修正
					currMonthInsurance = currMonthInsurance.add(fixlnsurance);
					
					BigDecimal fixStampTAX = totalStampTAX.subtract(pmtStampTAX); // 印花税修正
					currMonthStampTAX = currMonthStampTAX.add(fixStampTAX);
					
					BigDecimal fixLifeInsuFee = totalLifeInsuFee.subtract(pmtLifeInsuFee); // 寿险计划包费修正
					currMonthLifeInsuFee = currMonthLifeInsuFee.add(fixLifeInsuFee);
					
					// 代收服务费修正
					BigDecimal fixReplaceSvcFee = totalReplaceSvcFee.subtract(pmtReplaceSvcFee);
					currMonthReplaceSvcFee = currMonthReplaceSvcFee.add(fixReplaceSvcFee);
					
					// 代收服务费修正
					BigDecimal fixPrepayPkg = totalPrepayPkg.subtract(pmtPrepayPkg);
					currMonthPrepayPkg = currMonthPrepayPkg.add(fixPrepayPkg);
					
					currMonthPrin =  currMonthPrin.setScale(2, RoundingMode.HALF_UP); 
				}
			}
			loanPmtDueDate = rescheduleUtils.getLoanPmtDueDate(acct.getNextStmtDate(), loanFeeDef, i,acct.getCycleDay());
			ccsRepayScheduleList.add(rescheduleUtils.generateRepaySchedule(acctAttr,loanReg, null, i, currMonthPrin, currMonthInstallmentFee,currMonthSVCFee,currMonthInt,currMonthInsurance,currMonthStampTAX,currMonthLifeInsuFee,currMonthReplaceSvcFee,currMonthPrepayPkg,loanPmtDueDate));
		}
		return ccsRepayScheduleList;
	}
	

	
	// 手续费收取方式
		public enum LoanInstallmentFeeMethodimple {
			/**
			 * 一次性收取
			 */
			F {
				@Override
				public BigDecimal loanInstallmentFee(CcsLoanReg loanReg, LoanFeeDef loanFeeDef,int Flag) {
					// 每期手续费 
					BigDecimal loanTermFee = InstallmentFeeCalcMethodimple.valueOf(
							loanFeeDef.installmentFeeCalMethod.toString()).calcuteInstallmentFee(
									loanReg, loanFeeDef);
					// 总手续费 
					BigDecimal loanInitFee = loanTermFee.multiply(new BigDecimal(loanReg.getLoanInitTerm())).setScale(2, RoundingMode.HALF_UP);
					// 
					switch (Flag) {
					case 0:
						// 分期总手续费
						return loanInitFee;
					case 1:
						// 分期每期手续费
						return BigDecimal.ZERO;
					case 2:
						// 首期手续费
						return loanInitFee;
					default:
						return new BigDecimal(0);
					}
				}
			},
			/**
			 * 分期收取
			 */
			E {
				@Override
				public BigDecimal loanInstallmentFee(CcsLoanReg loanReg, LoanFeeDef loanFeeDef,int Flag) {

					// 每期手续费
					BigDecimal loanTermFee = InstallmentFeeCalcMethodimple.valueOf(
							loanFeeDef.installmentFeeCalMethod.toString()).calcuteInstallmentFee(
									loanReg, loanFeeDef);
					// 总手续费
					BigDecimal loanInitFee = loanTermFee.multiply(new BigDecimal(loanReg.getLoanInitTerm())).setScale(2, RoundingMode.HALF_UP);
					// 分期总手续费 = 每期的手续费*分期期数
					switch (Flag) {
					case 0:
						// 分期总手续费
						return loanInitFee;
					case 1:
						// 分期每期手续费
						return loanTermFee;
					case 2:
						// 首期手续费
						return loanTermFee;
					default:
						return new BigDecimal(0);
					}
				}
			};
			/**
			 * 手续费每期计算
			 * @param loanReg
			 * @param loanFeeDef
			 * @param Flag 0为总费用，1为每期费用，2为首期费用
			 * @return
			 */
			public abstract BigDecimal loanInstallmentFee(CcsLoanReg loanReg, LoanFeeDef loanFeeDef,int Flag);
		}
		


		// 手续费计算方式 CalcMethodimple
		public enum InstallmentFeeCalcMethodimple {
			// 按照百分比计算
			R {
				@Override
				BigDecimal calcuteInstallmentFee(CcsLoanReg loanReg, LoanFeeDef loanFeeDef) {
					if(loanReg.getInstallmentFeeRate() == null ){
						return loanReg.getLoanInitPrin().multiply(loanFeeDef.installmentFeeRate).setScale(2, BigDecimal.ROUND_HALF_UP);
					}else{
						return loanReg.getLoanInitPrin().multiply(loanReg.getInstallmentFeeRate()).setScale(2, BigDecimal.ROUND_HALF_UP);
					}
					
				}
					
			},
			// 固定金额
			A {
				@Override
				BigDecimal calcuteInstallmentFee(CcsLoanReg loanReg, LoanFeeDef loanFeeDef) {
					if(loanReg.getInstallmentFeeAmt() == null ){
						return loanFeeDef.installmentFeeAmt.setScale(2, BigDecimal.ROUND_HALF_UP);
					}else{
						return loanReg.getInstallmentFeeAmt().setScale(2, BigDecimal.ROUND_HALF_UP);
					}
				}
			};

			/**
			 * 计算分期手续费
			 * @param loanInitPrin
			 * @param loanFeeDef
			 * @param loanInsuranceRate 费率
			 * @return
			 */
			abstract BigDecimal calcuteInstallmentFee(CcsLoanReg loanReg,LoanFeeDef loanFeeDef);
		}
		

		// 服务费收取方式
			public enum LoanSVCFeeMethodimple {
				/**
				 * 一次性收取
				 */
				F {
					@Override
					public BigDecimal loanSVCFee(CcsLoanReg loanReg, LoanFeeDef loanFeeDef,int Flag) {
						// 每期服务费
						BigDecimal termSvcFee = SVCFeeCalcMethodimple.valueOf(
								loanFeeDef.loanFeeCalcMethod.toString()).calcuteSVCFee(
										loanReg, loanFeeDef);
						// 总服务费
						BigDecimal loanInitFee = termSvcFee.multiply(new BigDecimal(loanReg.getLoanInitTerm())).setScale(2, RoundingMode.HALF_UP);
						
						switch (Flag) {
						case 0:
							// 分期总服务费
							return loanInitFee;
						case 1:
							// 分期每期服务费
							return BigDecimal.ZERO;
						case 2:
							// 首期服务费
							return loanInitFee;
						default:
							return new BigDecimal(0);
						}
					}
				},
				/**
				 * 分期收取
				 */
				E {
					@Override
					public BigDecimal loanSVCFee(CcsLoanReg loanReg, LoanFeeDef loanFeeDef,int Flag) {

						// 每期服务费
						BigDecimal termSvcFee = SVCFeeCalcMethodimple.valueOf(
								loanFeeDef.loanFeeCalcMethod.toString()).calcuteSVCFee(
										loanReg, loanFeeDef);
						// 总服务费
						BigDecimal loanInitFee = termSvcFee.multiply(new BigDecimal(loanReg.getLoanInitTerm())).setScale(2, RoundingMode.HALF_UP);
						
						switch (Flag) {
						case 0:
							// 分期总服务费
							return loanInitFee;
						case 1:
							// 分期每期服务费
							return termSvcFee;
						case 2:
							// 首期服务费
							return termSvcFee;
						default:
							return new BigDecimal(0);
						}
					}
				};
				/**
				 * 服务费每期计算
				 * @param loanReg
				 * @param loanFeeDef
				 * @param Flag 0为总费用，1为每期费用，2为首期费用
				 * @return
				 */
				public abstract BigDecimal loanSVCFee(CcsLoanReg loanReg, LoanFeeDef loanFeeDef,int Flag);
			}
			


			// 服务费计算方式 CalcMethodimple
			public enum SVCFeeCalcMethodimple {
				// 按照百分比计算
				R {
					@Override
					BigDecimal calcuteSVCFee(CcsLoanReg loanReg, LoanFeeDef loanFeeDef) {
						if(loanReg.getFeeRate() == null ){
							return loanReg.getLoanInitPrin().multiply(loanFeeDef.feeRate).setScale(2, BigDecimal.ROUND_HALF_UP);
						}else{
							return loanReg.getLoanInitPrin().multiply(loanReg.getFeeRate()).setScale(2, BigDecimal.ROUND_HALF_UP);
						}
						
					}
						
				},
				// 固定金额
				A {
					@Override
					BigDecimal calcuteSVCFee(CcsLoanReg loanReg, LoanFeeDef loanFeeDef) {
						if(loanReg.getFeeAmt() == null){
							return loanFeeDef.feeAmount.setScale(2, BigDecimal.ROUND_HALF_UP);
						}else{
							return loanReg.getFeeAmt().setScale(2, BigDecimal.ROUND_HALF_UP);
						}
						
					}
				};

				/**
				 * 计算分期服务费
				 * @param loanInitPrin
				 * @param loanFeeDef
				 * @return
				 */
				abstract BigDecimal calcuteSVCFee(CcsLoanReg loanReg,LoanFeeDef loanFeeDef);
			}

	
	// 保费收取方式
		public enum LoanlnsuranceMethodimple {
			/**
			 * 一次性收取
			 */
			F {
				@Override
				public BigDecimal loanlnsurance(CcsLoanReg loanReg, LoanFeeDef loanFeeDef,int Flag) {
					// 每期保费
					BigDecimal loanFixedlnsurance = lnsuranceCalcMethodimple.valueOf(
							loanFeeDef.insCalcMethod.toString()).calcutelnsurance(
							loanReg, loanFeeDef);
					// 总的保费// 总的保费
					BigDecimal loanInitlnsurance = loanFixedlnsurance
							.multiply(new BigDecimal(loanReg.getLoanInitTerm())).setScale(2, RoundingMode.HALF_UP);
					
					switch (Flag) {
					case 0:
						// 分期总保险费
						return loanInitlnsurance;
					case 1:
						// 分期每期保险费
						return BigDecimal.ZERO;
					case 2:
						// 首期保险费
						return loanInitlnsurance;
					default:
						return new BigDecimal(0);
					}
				}
			},
			/**
			 * 分期收取
			 */
			E {
				@Override
				public BigDecimal loanlnsurance(CcsLoanReg loanReg, LoanFeeDef loanFeeDef,int Flag) {

					// 每期保费
					BigDecimal loanFixedlnsurance = lnsuranceCalcMethodimple.valueOf(
							loanFeeDef.insCalcMethod.toString()).calcutelnsurance(
							loanReg, loanFeeDef);
					// 总的保费// 总的保费
					BigDecimal loanInitlnsurance = loanFixedlnsurance
							.multiply(new BigDecimal(loanReg.getLoanInitTerm())).setScale(2, RoundingMode.HALF_UP);

					// 分期总手续费 = 每期的手续费*分期期数
					switch (Flag) {
					case 0:
						// 分期总保险费
						return loanInitlnsurance;
					case 1:
						// 分期每期保险费
						return loanFixedlnsurance;
					case 2:
						// 首期保险费
						return loanFixedlnsurance;
					default:
						return new BigDecimal(0);
					}
				}
			};
			/**
			 * 保费每期计算
			 * @param loanReg
			 * @param loanFeeDef
			 * @param Flag 0为总费用，1为每期费用，2为首期费用
			 * @return
			 */
			public abstract BigDecimal loanlnsurance(CcsLoanReg loanReg, LoanFeeDef loanFeeDef,int Flag);
		}
		


		// 保费计算方式 CalcMethodimple
		public enum lnsuranceCalcMethodimple {
			// 按照百分比计算
			R {
				@Override
				BigDecimal calcutelnsurance(CcsLoanReg loanReg, LoanFeeDef loanFeeDef) {
					if(loanReg.getInsuranceRate() == null ){
						return loanReg.getLoanInitPrin().multiply(loanFeeDef.insRate).setScale(2, BigDecimal.ROUND_HALF_UP);
					}else{
						return loanReg.getLoanInitPrin().multiply(loanReg.getInsuranceRate()).setScale(2, BigDecimal.ROUND_HALF_UP);
					}
					
				}
					
			},
			// 固定金额
			A {
				@Override
				BigDecimal calcutelnsurance(CcsLoanReg loanReg, LoanFeeDef loanFeeDef) {
					if(loanReg.getInsAmt() == null ){
						return loanFeeDef.insAmt.setScale(2, BigDecimal.ROUND_HALF_UP);
					}else{
						return loanReg.getInsAmt().setScale(2, BigDecimal.ROUND_HALF_UP);
					}
					
				}
			};
			/**
			 * 保费计算方式
			 * @param loanInitPrin
			 * @param loanFeeDef
			 * @param loanInsuranceRate 费率
			 * @return
			 */
			abstract BigDecimal calcutelnsurance(CcsLoanReg loanReg,LoanFeeDef loanFeeDef);
		}
		
		

		// 印花税收取方式
			public enum LoanStampTAXMethodimple {
				/**
				 * 一次性收取
				 */
				F {
					@Override
					public BigDecimal loanStampTAX(CcsLoanReg loanReg, LoanFeeDef loanFeeDef,int Flag) {

						
						// 每期
						BigDecimal loanFixedStampTAX = StampTAXCalcMethodimple.valueOf(
								loanFeeDef.stampCalcMethod.toString()).calcuteStampTAX(
								loanReg, loanFeeDef );
						// 总的
						BigDecimal loanInitStampTAX = loanFixedStampTAX.multiply(new BigDecimal(loanReg.getLoanInitTerm())).setScale(1, RoundingMode.HALF_UP);

						switch (Flag) {
						case 0:
							// 分期总
							return loanInitStampTAX;
						case 1:
							// 分期每期
							return BigDecimal.ZERO;
						case 2:
							// 首期
							return loanInitStampTAX;
						default:
							return new BigDecimal(0);
						}
						
					}
				},
				/**
				 * 分期收取
				 */
				E {
					@Override
					public BigDecimal loanStampTAX(CcsLoanReg loanReg, LoanFeeDef loanFeeDef,int Flag) {

						// 每期
						BigDecimal loanFixedStampTAX = StampTAXCalcMethodimple.valueOf(
								loanFeeDef.stampCalcMethod.toString()).calcuteStampTAX(
								loanReg, loanFeeDef );
						// 总的
						BigDecimal loanInitStampTAX = loanFixedStampTAX.multiply(new BigDecimal(loanReg.getLoanInitTerm())).setScale(1, RoundingMode.HALF_UP);

						
						switch (Flag) {
						case 0:
							// 分期总保险费
							return loanInitStampTAX;
						case 1:
							// 分期每期保险费
							return loanFixedStampTAX;
						case 2:
							// 分期首期期保险费
							return loanFixedStampTAX;
						default:
							return new BigDecimal(0);
						}
					}
				};
				/**
				 * 印花税每期计算
				 * @param loanReg
				 * @param loanFeeDef
				 * @param Flag 0为总费用，1为每期费用，2为首期费用
				 * @return
				 */
				public abstract BigDecimal loanStampTAX(CcsLoanReg loanReg, LoanFeeDef loanFeeDef,int Flag);
			}
			


			// 印花税计算方式 CalcMethodimple
			public enum StampTAXCalcMethodimple {
				// 按照百分比计算
				R {
					@Override
					BigDecimal calcuteStampTAX(CcsLoanReg loanReg, LoanFeeDef loanFeeDef) {
						if(loanReg.getStampdutyRate() == null ){
							return loanReg.getLoanInitPrin().multiply(loanFeeDef.stampRate).setScale(1, BigDecimal.ROUND_HALF_UP);
						}else{
							return loanReg.getLoanInitPrin().multiply(loanReg.getStampdutyRate()).setScale(1, BigDecimal.ROUND_HALF_UP);
						}
						
					}
						
				},
				// 固定金额
				A {
					@Override
					BigDecimal calcuteStampTAX(CcsLoanReg loanReg, LoanFeeDef loanFeeDef) {
						if(loanReg.getStampAmt() == null ){
							return loanFeeDef.stampAMT.setScale(1, BigDecimal.ROUND_HALF_UP);
						}else{
							return loanReg.getStampAmt().setScale(1, BigDecimal.ROUND_HALF_UP);
						}
						
					}
				};

				// 计算印花税手续费
				abstract BigDecimal calcuteStampTAX(CcsLoanReg loanReg,LoanFeeDef loanFeeDef);
			}
			
			
			// 寿险计划包费收取方式
			public enum LoanLifeInsuFeeMethodimple {
				/**
				 * 一次性收取
				 */
				F {
					@Override
					public BigDecimal loanLifeInsuFee(CcsLoanReg loanReg, LoanFeeDef loanFeeDef,int Flag) {
					
						// 每期
						BigDecimal loanFixedLifeInsuFee = LifeInsuFeeCalcMethodimple.valueOf(
								loanFeeDef.lifeInsuFeeCalMethod.toString()).calcuteLifeInsuFee(
								loanReg, loanFeeDef); 
						// 总的
						BigDecimal loanInitLifeInsuFee = loanFixedLifeInsuFee.multiply(new BigDecimal(loanReg.getLoanInitTerm()))
								.setScale( 2, RoundingMode.HALF_UP);

						switch (Flag) {
						case 0:
							// 总费
							return loanInitLifeInsuFee;
						case 1:
							// 分期每期
							return BigDecimal.ZERO;
						case 2:
							// 分期首期
							return loanInitLifeInsuFee;	
						default:
							return new BigDecimal(0);
						}
					}
				},
				/**
				 * 分期收取
				 */
				E {
					@Override
					public BigDecimal loanLifeInsuFee(CcsLoanReg loanReg, LoanFeeDef loanFeeDef,int Flag) {
						// 每期
						BigDecimal loanFixedLifeInsuFee = LifeInsuFeeCalcMethodimple.valueOf(
								loanFeeDef.lifeInsuFeeCalMethod.toString()).calcuteLifeInsuFee(
								loanReg, loanFeeDef); 
						// 总的
						BigDecimal loanInitLifeInsuFee = loanFixedLifeInsuFee.multiply(new BigDecimal(loanReg.getLoanInitTerm()))
								.setScale( 2, RoundingMode.HALF_UP);
						
						switch (Flag) {
						case 0:
							// 总费
							return loanInitLifeInsuFee;
						case 1:
							// 分期每期
							return loanFixedLifeInsuFee;
						case 2:
							// 分期首期
							return loanFixedLifeInsuFee;	
						default:
							return new BigDecimal(0);
						}
					}
				};
				/**
				 * 寿险计划包费每期计算
				 * @param loanReg
				 * @param loanFeeDef
				 * @param Flag 0为总费用，1为每期费用，2为首期费用
				 * @return
				 */
				public abstract BigDecimal loanLifeInsuFee(CcsLoanReg loanReg, LoanFeeDef loanFeeDef,int Flag);
			}
			


			// 寿险计划包费计算方式 CalcMethodimple
			public enum LifeInsuFeeCalcMethodimple {
				// 按照百分比计算
				R {
					@Override
					BigDecimal calcuteLifeInsuFee(CcsLoanReg loanReg, LoanFeeDef loanFeeDef) {
						if(loanReg.getLifeInsuFeeRate() == null){
							return loanReg.getLoanInitPrin().multiply(loanFeeDef.lifeInsuFeeRate).setScale(2, BigDecimal.ROUND_HALF_UP);
						}else{
							return loanReg.getLoanInitPrin().multiply(loanReg.getLifeInsuFeeRate()).setScale(2, BigDecimal.ROUND_HALF_UP);
						}
						
					}
						
				},
				// 固定金额
				A {
					@Override
					BigDecimal calcuteLifeInsuFee(CcsLoanReg loanReg, LoanFeeDef loanFeeDef) {
						if(loanReg.getLifeInsuFeeAmt() == null ){
							return loanFeeDef.lifeInsuFeeAmt.setScale(2, BigDecimal.ROUND_HALF_UP);
						}else{
							return loanReg.getLifeInsuFeeAmt().setScale(2, BigDecimal.ROUND_HALF_UP);
						}
						
					}
				};

				// 计算分期手续费
				abstract BigDecimal calcuteLifeInsuFee(CcsLoanReg loanReg,LoanFeeDef loanFeeDef);
			}
			
						
	
			/**
			 * 小额贷款结清金额,根据扣款用户返回扣款金额
			 * 当天结清
			 * 保费，利息，印花税重新计算，按天计算到当天=剩余本金*利率
			 * 这里只做理赔和提前结清的
			 * 提前结清不允许有逾期，本金费用等字段不包括未偿还的部分,也不包括罚金
			 * @param loanReg
			 * @param batDate,制定日期结清
			 * loanUsage 贷款用途
			 * @throws Exception
			 */
			public TrialResp mCLoanTodaySettlement(CcsLoan loan,Date registerDate,Date batDate,LoanUsage loanUsage,TrialResp trialResp,List<CcsPlan> plans,CcsLoanReg loanReg) throws Exception{
				
				LoanFeeDef loanFeeDef = rescheduleUtils.getLoanFeeDef(loan.getLoanCode(), loan.getLoanInitTerm(), 
						loan.getLoanInitPrin(),loan.getLoanFeeDefId());
				
				List<CcsRepaySchedule> origSchedules = rescheduleUtils.getRepayScheduleListByLoanId(loan.getLoanId());
				
				switch(loanUsage){
				case R:
				case M:
					// 判断是否犹豫期内
					if(loanFeeDef.hesitationDays!=null){
						Date hesitationDate = DateUtils.addDays(loan.getActiveDate(), loanFeeDef.hesitationDays);
						if(DateUtils.truncatedCompareTo(loan.getActiveDate(), registerDate, Calendar.DATE)<0
								&& DateUtils.truncatedCompareTo(hesitationDate, registerDate, Calendar.DATE)>=0)
							//判断是否退还趸交费
							if(Indicator.Y==loanFeeDef.premiumReturnInd){
								return this.loanSettlementHes(loan, plans,loanReg,true);
							}else{
								return this.loanSettlementHes(loan, plans,loanReg,false);
							}
					}
					return this.loanSettlementM(loan, origSchedules, loanFeeDef, plans, batDate, trialResp,loanReg);
				case C:
					return this.loanSettlementC(loan, origSchedules, loanFeeDef, plans, batDate, trialResp);
				default :
					//其他用途不作处理
					return trialResp;
				}
			}
			
			
			private TrialResp loanSettlementC(CcsLoan loan,List<CcsRepaySchedule> origSchedules,LoanFeeDef loanFeeDef,List<CcsPlan> plans,Date batDate,TrialResp trialResp){
				
				// 理赔 计算到当日的逾期天数
				if(loan.getOverdueDate()==null){
					return trialResp;
				}
				int days = DateUtils.getIntervalDays(loan.getOverdueDate(),batDate);
				
				//计算逾期80天所在的期数
				Date days80 = null;
				ProductCredit productCr = cardProctFacility.CardNoToProductCr(loan.getCardNbr());
				int claimDays = productCr.claimsDays;
				if(days>claimDays){
					days80 = DateUtils.addDays(loan.getOverdueDate(),claimDays);
					origSchedules = this.sortSchedule(origSchedules);
				}else{
					days80 = batDate;
				}
				
				
				//获取下一期的还款计划和80天所在的期数
				int currTerm = 0;
				CcsRepaySchedule ccsRepaySchedule = null;
				for (CcsRepaySchedule s : origSchedules) {
					if(s.getLoanPmtDueDate().compareTo(days80)>0){
						ccsRepaySchedule= s;
						currTerm=s.getCurrTerm()-1;
						break;
					}
				}
				//如果下一期是空的，说明贷款已经终止
				if(ccsRepaySchedule ==null){
					currTerm=loan.getLoanInitTerm();
				}

				
				// 获取上一期还款计划
				CcsRepaySchedule origSchedule = null;
				for (CcsRepaySchedule s : origSchedules) {
					if(s.getCurrTerm().intValue() == currTerm){
						origSchedule= s;
						break;
					}
				}
				
				
				//获取理赔的当前的起始日期
				Date beginDate = null;
				if(origSchedule == null){
					beginDate = loan.getActiveDate();
				}else{
					beginDate = DateUtils.addDays(origSchedule.getLoanPmtDueDate(), 1);;
				}
				
				//计算理赔当期的利息天数
				int calcuteDate =0;
				if(beginDate.compareTo(days80)<0){
					calcuteDate = DateUtils.getIntervalDays(beginDate,days80);
				}
				
				BigDecimal currPricinpal = BigDecimal.ZERO;
				BigDecimal currmonthInt = BigDecimal.ZERO;
				BigDecimal currMonthInsurance = BigDecimal.ZERO;
				BigDecimal currMonthStampduty = BigDecimal.ZERO;
				BigDecimal currMonthLifeInsuFee = BigDecimal.ZERO;
				
				//往期欠款代收服务费
				BigDecimal pastReplaceSvcFee = BigDecimal.ZERO;
				
				//剩余利息重算，还款计划中的利息*天数/30
				if(ccsRepaySchedule != null ){
					if(calcuteDate >= 30){
						currmonthInt = ccsRepaySchedule.getLoanTermInt();
					}else{
						currmonthInt = ccsRepaySchedule.getLoanTermInt().multiply(new BigDecimal(calcuteDate)).divide(new BigDecimal(30), 2, RoundingMode.HALF_UP);
					}
				}
				
				//剩余印花税=未出账单的印花税
				if(loan.getStampCustomInd().equals(Indicator.Y)){
					currMonthStampduty = loan.getUnstmtStampdutyAmt();
				}
				//剩余寿险计划包费=未出账单的寿险计划包费
				currMonthLifeInsuFee = loan.getUnstmtLifeInsuAmt();
				BigDecimal currMonthLoanTermFee = ccsRepaySchedule==null ? BigDecimal.ZERO: ccsRepaySchedule.getLoanTermFee() ;
				BigDecimal currMonthLoanSVCFee = ccsRepaySchedule==null ? BigDecimal.ZERO: ccsRepaySchedule.getLoanSvcFee() ;
				
				//剩余代收服务费
				BigDecimal currMonthReplaceSvcFee = ccsRepaySchedule==null?BigDecimal.ZERO: ccsRepaySchedule.getLoanReplaceSvcFee();
				
				//总应还款额
				BigDecimal totalRepayAMT = BigDecimal.ZERO;
				//总费用
				BigDecimal initFee =  BigDecimal.ZERO;
//				//罚金的金额
				BigDecimal ctdMulctAMT = BigDecimal.ZERO;
				//往期欠款本金
				BigDecimal pastPricinpalAMT = BigDecimal.ZERO;
				//往期欠款利息
				BigDecimal pastInterestAMT = BigDecimal.ZERO;
				//往期欠款保费
				BigDecimal pastInsuranceAMT = BigDecimal.ZERO;
				//往期欠款罚金
				BigDecimal pastMulctAMT = BigDecimal.ZERO;
				//往期欠款手续费
				BigDecimal pastInitFee = BigDecimal.ZERO;
				//往期欠款印花税费
				BigDecimal pastStampdutyAMT = BigDecimal.ZERO;
				//往期欠款寿险计划包费
				BigDecimal pastLifeInsuFeeAMT = BigDecimal.ZERO;
				//服务费
				BigDecimal pastSVCFee = BigDecimal.ZERO;
				
				//获取所有往期未还的余额
				for(CcsPlan plan : plans){
					int term = plan.getTerm()==null?0:plan.getTerm();
					if(loan.getRefNbr().equals(plan.getRefNbr()) && currTerm >=term){
						//转入计划的期数在贷款当前期数内，并且是转入计划
						if(plan.getPlanType().isXfrIn()){
							pastPricinpalAMT = pastPricinpalAMT.add(plan.getPastPrincipal()).add(plan.getCtdPrincipal());
							pastInterestAMT = pastInterestAMT.add(plan.getPastInterest()).add(plan.getCtdInterest());
							pastInsuranceAMT = pastInsuranceAMT.add(plan.getPastInsurance()).add(plan.getCtdInsurance());
							pastMulctAMT = pastMulctAMT.add(plan.getPastMulctAmt()).add(plan.getCtdMulctAmt());
							pastInitFee = pastInitFee.add(plan.getPastTxnFee()).add(plan.getCtdTxnFee());
							pastStampdutyAMT = pastStampdutyAMT.add(plan.getPastStampdutyAmt()).add(plan.getCtdStampdutyAmt());
							pastLifeInsuFeeAMT = pastLifeInsuFeeAMT.add(plan.getPastLifeInsuAmt()).add(plan.getCtdLifeInsuAmt());
							pastSVCFee = pastSVCFee.add(plan.getPastSvcFee()).add(plan.getCtdSvcFee());
							pastReplaceSvcFee = pastReplaceSvcFee.add(plan.getPastReplaceSvcFee()).add(plan.getCtdReplaceSvcFee());
						}
					}
					
					// 非延迟利息，记到当期利息中
					currmonthInt = currmonthInt.add(plan.getNodefbnpIntAcru().setScale(2, RoundingMode.HALF_UP));
				}
				//取得已出的本金
				for (CcsRepaySchedule s : origSchedules) {
					if(s.getCurrTerm().intValue() <= currTerm){
						currPricinpal = currPricinpal.add(s.getLoanTermPrin());
					}
				}
				//计算当期的本金
				currPricinpal = loan.getLoanInitPrin().subtract(currPricinpal);
				//理赔结清
				//总金额包括，未出账单本金+重算利息+重算印花税+重算寿险计划包费+
				//未偿还的本金+未偿还的利息+未偿还的印花税+未偿还的寿险计划包费+未偿还本金
				//未收的保费不收，已收保费未还的不收，已收保费已还的不处理(每月会跟阳光结算)
				totalRepayAMT = currPricinpal.add(currmonthInt).add(ctdMulctAMT).add(currMonthStampduty).add(currMonthLifeInsuFee).add(initFee)
						.add(pastPricinpalAMT).add(pastLifeInsuFeeAMT).add(pastStampdutyAMT).add(pastInitFee).add(pastMulctAMT).add(pastInterestAMT)
						.add(pastSVCFee).add(currMonthLoanSVCFee)
						.add(pastReplaceSvcFee).add(currMonthReplaceSvcFee);;
				pastInsuranceAMT =  BigDecimal.ZERO;
					
				trialResp.setTotalAMT(totalRepayAMT);
				trialResp.setCtdPricinpalAMT(currPricinpal);
				trialResp.setCtdInterestAMT(currmonthInt);
				trialResp.setCtdInsuranceAMT(currMonthInsurance);
				trialResp.setCtdMulctAMT(ctdMulctAMT);
				trialResp.setCtdStampdutyAMT(currMonthStampduty);
				trialResp.setCtdLifeInsuFeeAMT(currMonthLifeInsuFee);
				trialResp.setCtdInitFee(initFee.add(currMonthLoanSVCFee));
				trialResp.setCtdLoanTermFeeAMT(currMonthLoanTermFee);
				trialResp.setPastLifeInsuFeeAMT(pastLifeInsuFeeAMT);
				trialResp.setPastInitFee(pastInitFee);
				trialResp.setPastInsuranceAMT(pastInsuranceAMT);
				trialResp.setPastInterestAMT(pastInterestAMT);
				trialResp.setPastMulctAMT(pastMulctAMT);
				trialResp.setPastPricinpalAMT(pastPricinpalAMT);
				trialResp.setPastStampdutyAMT(pastStampdutyAMT);
				trialResp.setPastLoanTermFeeAMT(pastSVCFee);
				trialResp.setPastReplaceSvcAMT(pastReplaceSvcFee);
				trialResp.setCtdReplaceSvcAMT(currMonthReplaceSvcFee);
				return trialResp;
			}
			
			/**
			 * 犹豫期提前结清仅收取本金
			 * @param loan
			 * @return
			 */
			private TrialResp loanSettlementHes(CcsLoan loan, List<CcsPlan> plans,CcsLoanReg loanReg,boolean isReturnPremium){
				TrialResp resp = new TrialResp();
				
				BigDecimal totalRepayAMT = BigDecimal.ZERO;
				BigDecimal pastPricinpalAMT = BigDecimal.ZERO;
				BigDecimal deposit = BigDecimal.ZERO;
				//获取所有往期未还的本金
				for(CcsPlan plan : plans){
					int term = plan.getTerm()==null?0:plan.getTerm();
					if(loan.getRefNbr().equals(plan.getRefNbr()) && loan.getCurrTerm()>=term){
						//转入计划的期数在贷款当前期数内，并且是转入计划
						if(plan.getPlanType().isXfrIn()){
							pastPricinpalAMT = pastPricinpalAMT.add(plan.getPastPrincipal()).add(plan.getCtdPrincipal());
						}
					}
					//如果是溢缴款计划
					if(plan.getPlanType() == PlanType.D){
						deposit = deposit.add(plan.getCurrBal().abs());
					}
				}
				
				totalRepayAMT = loan.getUnstmtPrin().add(pastPricinpalAMT);
				
				if(deposit.compareTo(BigDecimal.ZERO)>0){
					LoanPlan loanPlan = parameterFacility.loadParameter(loan.getLoanCode(), LoanPlan.class);
					if(loanPlan.loanMold == LoanMold.S){
						totalRepayAMT= totalRepayAMT.subtract(deposit);
					}
				}
				
				// 对于扣款失败的情况，考虑MemoCr,MemoDb
				if(loanReg!=null){
//					if(Indicator.N==loanReg.getDdRspFlag()){
						CcsAcctO acctO = em.find(CcsAcctO.class, new CcsAcctOKey(loan.getAcctNbr(),loan.getAcctType()));
						totalRepayAMT = totalRepayAMT.add(acctO.getMemoDb()).subtract(acctO.getMemoCr());
						resp.setMemoAmt(acctO.getMemoDb().subtract(acctO.getMemoCr()));
						if(totalRepayAMT.compareTo(BigDecimal.ZERO)<0){
							totalRepayAMT = BigDecimal.ZERO;
						}
//					}
				}
				
				//如果退还趸交费，则还款金额=贷款本金-趸交费
				if(isReturnPremium){
					totalRepayAMT = totalRepayAMT.subtract(loan.getPremiumAmt());
					resp.setPremiumAmt(loan.getPremiumAmt());
				}
				
				resp.setTotalAMT(totalRepayAMT);
				resp.setCtdPricinpalAMT(totalRepayAMT);
				return resp;
			}
			
			private TrialResp loanSettlementM(CcsLoan loan,List<CcsRepaySchedule> origSchedules,LoanFeeDef loanFeeDef,List<CcsPlan> plans,Date batDate,TrialResp trialResp,CcsLoanReg loanReg){
				
				// 获取上一期还款计划
				CcsRepaySchedule origNextSchedule = null;
				//获取下一期的还款计划
				CcsRepaySchedule ccsRepaySchedule = null;
				for (CcsRepaySchedule s : origSchedules) {
					if(s.getCurrTerm().intValue() == loan.getCurrTerm().intValue()){
						origNextSchedule = s;
					}
					if(s.getCurrTerm().intValue() == loan.getCurrTerm().intValue()+1){
						ccsRepaySchedule = s;
						break;
					}
				}
				
				int  days = 0;
				//获取上一还款日到当日的天数
				//如果传入的业务日期比上一还款早，days=0
				Date beginDate = null;
				if(origNextSchedule == null){
					beginDate = loan.getActiveDate();
				}else{
					beginDate = DateUtils.addDays(origNextSchedule.getLoanPmtDueDate(), 1);
				}
				if(beginDate.before(batDate)){
					days = DateUtils.getIntervalDays(beginDate,batDate);
				}
				BigDecimal monthInt = BigDecimal.ZERO;
				//剩余利息重算，还款计划中的利息*天数/30
				if(ccsRepaySchedule != null ){
					if(days >= 30){
						monthInt = ccsRepaySchedule.getLoanTermInt();
					}else{
						monthInt = ccsRepaySchedule.getLoanTermInt().multiply(new BigDecimal(days)).divide(new BigDecimal(30), 2, RoundingMode.HALF_UP); // 剩余应还本金 *月利率
					}
				}
				BigDecimal currMonthInsurance = BigDecimal.ZERO;
				if(loanFeeDef.insCalcMethod == PrepaymentFeeMethod.A){
					BigDecimal currTolMonthInsurance = ccsRepaySchedule==null ? BigDecimal.ZERO: ccsRepaySchedule.getLoanInsuranceAmt() ;
					if(days >= 30){
						currMonthInsurance = currTolMonthInsurance;
					}else{
						currMonthInsurance = currTolMonthInsurance.multiply(new BigDecimal(days)).divide(new BigDecimal(30), 2, RoundingMode.HALF_UP); //按天折算
					}
				}else{
					//保费月利率
					BigDecimal monthInsRt = loan.getInsuranceRate().divide(new BigDecimal(loan.getLoanInitTerm()), 20, RoundingMode.HALF_UP);
					//剩余保费重算,剩余本金*月利率*天数/30
					currMonthInsurance = loan.getLoanInitPrin().multiply(monthInsRt).multiply(new BigDecimal(days)).divide(new BigDecimal(30),2,RoundingMode.HALF_UP); // 总本金 *月利率
				}
				
				BigDecimal currMonthStampduty = BigDecimal.ZERO;
				//剩余印花税=未出账单的印花税
				if(loan.getStampCustomInd().equals(Indicator.Y)){
					//入客户账则出印花税
					currMonthStampduty=loan.getStampCustomInd()==Indicator.Y?loan.getUnstmtStampdutyAmt():BigDecimal.ZERO;
				}
				//剩余寿险计划包费=当月的寿险计划包费
				BigDecimal currMonthLifeInsuFee = ccsRepaySchedule==null ? BigDecimal.ZERO: ccsRepaySchedule.getLoanLifeInsuAmt() ;
				//剩余服务费=当月的服务费
				BigDecimal currMonthLoanTermFee = ccsRepaySchedule==null ? BigDecimal.ZERO: ccsRepaySchedule.getLoanTermFee() ;
				//剩余分期手续费=当月分期手续费
				BigDecimal currMonthLoanSVCFee = ccsRepaySchedule==null ? BigDecimal.ZERO: ccsRepaySchedule.getLoanSvcFee() ;
				//剩余代收服务费
				BigDecimal currMonthReplaceSvcFee = ccsRepaySchedule==null?BigDecimal.ZERO: ccsRepaySchedule.getLoanReplaceSvcFee();
				//剩余灵活还款计划包
				BigDecimal currMonthPrepayPkg = ccsRepaySchedule==null?BigDecimal.ZERO: ccsRepaySchedule.getLoanPrepayPkgAmt();
				//总应还款额
				BigDecimal totalRepayAMT = BigDecimal.ZERO;
				//总费用
				BigDecimal initFee =  BigDecimal.ZERO;
//				//罚金的金额
				BigDecimal ctdMulctAMT = BigDecimal.ZERO;
				//往期欠款本金
				BigDecimal pastPricinpalAMT = BigDecimal.ZERO;
				//往期欠款利息
				BigDecimal pastInterestAMT = BigDecimal.ZERO;
				//往期欠款保费
				BigDecimal pastInsuranceAMT = BigDecimal.ZERO;
				//往期欠款罚金
				BigDecimal pastMulctAMT = BigDecimal.ZERO;
				//往期欠款手续费
				BigDecimal pastInitFee = BigDecimal.ZERO;
				//往期欠款印花税费
				BigDecimal pastStampdutyAMT = BigDecimal.ZERO;
				//往期欠款寿险计划包费
				BigDecimal pastLifeInsuFeeAMT = BigDecimal.ZERO;
				//往期欠款服务费
				BigDecimal pastLoanTermFee = BigDecimal.ZERO;
				//往期欠款代收服务费
				BigDecimal pastReplaceSvcFee = BigDecimal.ZERO;
				//往期贷款手续费
				BigDecimal pastLoanTermSvc = BigDecimal.ZERO;
				//往期灵活还款计划包
				BigDecimal pastPrepayPkg = BigDecimal.ZERO;
				
				//plan上罚息复利
				BigDecimal intreseAmt = BigDecimal.ZERO;
				
				//plan上代收罚息
				BigDecimal replacePenalty = BigDecimal.ZERO;
				//plan上代收罚金
				BigDecimal replaceMulct = BigDecimal.ZERO;
				//plan是代收滞纳金
				BigDecimal replaceLpc = BigDecimal.ZERO;
				//代收提前还款手续费
				BigDecimal replacePrepayTxnFee = BigDecimal.ZERO;
				
				LoanPlan loanPlan = parameterFacility.loadParameter(loan.getLoanCode(), LoanPlan.class);
				
				BigDecimal deposit = BigDecimal.ZERO;
				//获取所有往期未还的余额
				for(CcsPlan plan : plans){
					int term = plan.getTerm()==null?0:plan.getTerm();
					if(loan.getRefNbr().equals(plan.getRefNbr()) && loan.getCurrTerm()>=term){
						//转入计划的期数在贷款当前期数内，并且是转入计划
						if(plan.getPlanType().isXfrIn()){
							pastPricinpalAMT = pastPricinpalAMT.add(plan.getPastPrincipal()).add(plan.getCtdPrincipal());
							pastInterestAMT = pastInterestAMT.add(plan.getPastInterest()).add(plan.getCtdInterest());
							pastInsuranceAMT = pastInsuranceAMT.add(plan.getPastInsurance()).add(plan.getCtdInsurance());
							pastMulctAMT = pastMulctAMT.add(plan.getPastMulctAmt()).add(plan.getCtdMulctAmt());
							pastInitFee = pastInitFee.add(plan.getPastTxnFee()).add(plan.getCtdTxnFee());
							pastStampdutyAMT = pastStampdutyAMT.add(plan.getPastStampdutyAmt()).add(plan.getCtdStampdutyAmt());
							pastLifeInsuFeeAMT = pastLifeInsuFeeAMT.add(plan.getPastLifeInsuAmt()).add(plan.getCtdLifeInsuAmt());
							pastLoanTermFee = pastLoanTermFee.add(plan.getCtdSvcFee()).add(plan.getPastSvcFee());
							pastReplaceSvcFee = pastReplaceSvcFee.add(plan.getPastReplaceSvcFee()).add(plan.getCtdReplaceSvcFee());
							pastLoanTermSvc = pastLoanTermSvc.add(plan.getPastSvcFee()).add(plan.getCtdSvcFee());
							pastPrepayPkg = pastPrepayPkg.add(plan.getPastPrepayPkgFee()).add(plan.getCtdPrepayPkgFee());
							intreseAmt = intreseAmt.add(plan.getCompoundAcru().setScale(2, RoundingMode.HALF_UP).add(plan.getPenaltyAcru().setScale(2, RoundingMode.HALF_UP)
							//考虑容差内当前往期罚息/复利
									.add(plan.getCtdCompound().add(plan.getPastCompound()).add(plan.getCtdPenalty().add(plan.getPastPenalty())))));
							replacePenalty = replacePenalty.add(plan.getReplacePenaltyAcru().setScale(2,RoundingMode.HALF_UP).add(plan.getCtdReplacePenalty().add(plan.getPastReplacePenalty())));
							replaceMulct = replaceMulct.add(plan.getPastReplaceMulct().add(plan.getCtdReplaceMulct()));
							replaceLpc = replaceLpc.add(plan.getPastReplaceLateFee().add(plan.getCtdReplaceLateFee()));
						}
					}
					//如果是溢缴款计划
					if(plan.getPlanType() == PlanType.D){
						deposit = deposit.add(plan.getCurrBal().abs());
					}
					
					// 非延迟利息记到当期利息中
					monthInt = monthInt.add(plan.getNodefbnpIntAcru().setScale(2, RoundingMode.HALF_UP)); 
				}
				
//				//利息
				//这里一期不考虑罚息，规则：逾期不可做提前结清，不考虑罚金
				// 手续费金额重算
				//当前期数=已出期数+1
				initFee = EarlySettleMethodimple.valueOf(
						loanFeeDef.prepaymentFeeMethod.toString()).calcuteEarlySettle(
						loan.getUnstmtPrin(), loanFeeDef,loan.getCurrTerm()+1);
				
				//代收提前还款手续费
				if(loanFeeDef.replacePrepaymentFeeMethod!=null){
					replacePrepayTxnFee = ReplacePrepayMethodimple.valueOf(
							loanFeeDef.replacePrepaymentFeeMethod.toString()).calcuteEarlySettle(
							loan.getUnstmtPrin(), loanFeeDef,loan.getCurrTerm()+1);
				}
				
				//如果购买了灵活还款计划包并满足优惠的期数，则不收取提前还款手续费
				//不配置参数的认为是不考虑这个参数，即旧产品，仍然收取手续费
				if(loanFeeDef.disPrepaymentApplyTerm!=null){
					if(loan.getPrepayPkgInd() == Indicator.Y && loan.getCurrTerm() >= loanFeeDef.disPrepaymentApplyTerm){
						initFee = BigDecimal.ZERO;
						replacePrepayTxnFee = BigDecimal.ZERO;
					}
				}
				
				//预约提前结清
				//总金额包括，未出账单本金+重算利息+重算保费+重算印花税+重算寿险计划包费+重算手续费+提前结清手续费+手续费+欠款部分
				totalRepayAMT = loan.getUnstmtPrin().add(monthInt)
						.add(currMonthInsurance).add(ctdMulctAMT).add(currMonthStampduty)
						.add(currMonthLifeInsuFee).add(currMonthLoanTermFee).add(initFee)
						.add(pastPricinpalAMT).add(pastLifeInsuFeeAMT).add(pastLoanTermFee)
						.add(pastStampdutyAMT).add(pastInitFee).add(pastMulctAMT).add(pastInsuranceAMT)
						.add(pastInterestAMT).add(currMonthLoanSVCFee)
						.add(pastReplaceSvcFee).add(currMonthReplaceSvcFee)
						.add(pastLoanTermSvc)
						.add(intreseAmt)
						//+代收罚息+代收罚金+代收滞纳金+代收提前还款手续费
						.add(replacePenalty)
						.add(replaceMulct)
						.add(replaceLpc)
						.add(replacePrepayTxnFee)
						.add(pastPrepayPkg)
						.add(currMonthPrepayPkg);
					
				if(deposit.compareTo(BigDecimal.ZERO)>0){
					if(loanPlan.loanMold == LoanMold.S){
						totalRepayAMT= totalRepayAMT.subtract(deposit);
						//如果溢缴款大于总金额
						if(totalRepayAMT.compareTo(BigDecimal.ZERO)<0) totalRepayAMT=BigDecimal.ZERO;
					}
				}
				
				// 考虑其MemoCr,MemoDb
				if(loanReg!=null){
					CcsAcctO acctO = em.find(CcsAcctO.class, new CcsAcctOKey(loan.getAcctNbr(),loan.getAcctType()));
					totalRepayAMT = totalRepayAMT.subtract(acctO.getMemoCr()).add(acctO.getMemoDb());
					trialResp.setMemoAmt(acctO.getMemoDb().subtract(acctO.getMemoCr()));
				}
				
				totalRepayAMT = totalRepayAMT.setScale(2,BigDecimal.ROUND_HALF_UP);
				trialResp.setTotalAMT(totalRepayAMT);
				trialResp.setCtdPricinpalAMT(loan.getUnstmtPrin());
				trialResp.setCtdInterestAMT(monthInt);
				trialResp.setCtdInsuranceAMT(currMonthInsurance);
				trialResp.setCtdMulctAMT(ctdMulctAMT);
				trialResp.setCtdStampdutyAMT(currMonthStampduty);
				trialResp.setCtdLifeInsuFeeAMT(currMonthLifeInsuFee);
				trialResp.setCtdLoanTermSvc(currMonthLoanSVCFee);
				trialResp.setCtdInitFee(initFee);
				trialResp.setCtdReplaceSvcAMT(currMonthReplaceSvcFee);
				trialResp.setCtdLoanTermFeeAMT(currMonthLoanTermFee);
				
				trialResp.setPastLifeInsuFeeAMT(pastLifeInsuFeeAMT);
				trialResp.setPastLoanTermFeeAMT(pastLoanTermFee);
				trialResp.setPastInitFee(pastInitFee);
				trialResp.setPastInsuranceAMT(pastInsuranceAMT);
				trialResp.setPastInterestAMT(pastInterestAMT);
				trialResp.setPastMulctAMT(pastMulctAMT);
				trialResp.setPastPricinpalAMT(pastPricinpalAMT);
				trialResp.setPastStampdutyAMT(pastStampdutyAMT);
				trialResp.setPastReplaceSvcAMT(pastReplaceSvcFee);
				trialResp.setPastLoanTermSvc(pastLoanTermSvc);

				trialResp.setReplaceLpc(replaceLpc);
				trialResp.setReplaceMulct(replaceMulct);
				trialResp.setReplacePenalty(replacePenalty);
				trialResp.setDeposit(deposit);
				trialResp.setReplacePrepayFee(replacePrepayTxnFee);
				
				trialResp.setPastPrepayPkg(pastPrepayPkg);
				trialResp.setCtdMonthPrepayPkg(currMonthPrepayPkg);
				
				return trialResp;
			}
	
			

			// 提前还款收取方式 CalcMethodimple
			public enum EarlySettleMethodimple {
				// 按照百分比计算
				R {
					@Override
					public
					BigDecimal calcuteEarlySettle(BigDecimal loanInitPrin, LoanFeeDef loanFeeDef,int currTerm) {
						List<EarlyRepayDef> earlyRepayDefs = sortEarlyRepayDef(loanFeeDef.earlyRepayDefs);
						EarlyRepayDef erDef = null;
						for(EarlyRepayDef earlyRepayDef:earlyRepayDefs){
							if(earlyRepayDef.adCurPeriod >= currTerm){
								erDef =earlyRepayDef;
								break;
							}
						}
						if(erDef == null)
							return BigDecimal.ZERO;
							
						
						return loanInitPrin.multiply(erDef.adFeeScale).setScale(2, BigDecimal.ROUND_HALF_UP);
					}
						
				},
				// 固定金额
				A {
					@Override
					public
					BigDecimal calcuteEarlySettle(BigDecimal loanInitPrin, LoanFeeDef loanFeeDef,int currTerm) {
						List<EarlyRepayDef> earlyRepayDefs = sortEarlyRepayDef(loanFeeDef.earlyRepayDefs);
						EarlyRepayDef erDef = null;
						for(EarlyRepayDef earlyRepayDef:earlyRepayDefs){
							if(earlyRepayDef.adCurPeriod >= currTerm){
								erDef =earlyRepayDef;
								break;
							}
						}
						if(erDef == null)
							return BigDecimal.ZERO;
							
						
						return erDef.adFeeAmt;
					}
				};

				// 提前还款收取方式
				/**
				 * 提前还款收取方式
				 * @param loanInitPrin
				 * @param loanFeeDef
				 * @param currTerm 当前期数
				 * @return
				 */
				public abstract BigDecimal calcuteEarlySettle(BigDecimal loanInitPrin,LoanFeeDef loanFeeDef,int currTerm);
			}
			
			
			
			/**
			 * 提前还款收取方式按照期数大小进行排序
			 * @param arr
			 * @return
			 */
			private static List<EarlyRepayDef> sortEarlyRepayDef(List<EarlyRepayDef> arr) { // 交换排序->冒泡排序
			   EarlyRepayDef temp = null;
		        boolean exchange = false;
		        for (int i = 0; i < arr.size(); i++) {
		            exchange = false;
		            for (int j = arr.size() - 2; j >= i; j--) {
		                if (arr.get(j + 1).adCurPeriod.compareTo(arr.get(j).adCurPeriod) <= 0) {
		                    temp = arr.get(j + 1);
		                    arr.set(j + 1,  arr.get(j));
		                    arr.set(j, temp);
		                    exchange = true;
		                }
		            }
		            if (!exchange)
		                break;
		        }
		        return arr;
		    }
		   /**
			 * 还款计划收取方式按照期数大小进行排序
			 * @param arr
			 * @return
			 */
		   private List<CcsRepaySchedule> sortSchedule(List<CcsRepaySchedule> sches) { // 交换排序->冒泡排序
			   CcsRepaySchedule sche = null;
		        boolean exchange = false;
		        for (int i = 0; i < sches.size(); i++) {
		            exchange = false;
		            for (int j = sches.size() - 2; j >= i; j--) {
		                if (sches.get(j + 1).getCurrTerm().compareTo(sches.get(j).getCurrTerm()) <= 0) {
		                	sche = sches.get(j + 1);
		                	sches.set(j + 1, sches.get(j));
		                	sches.set(j, sche);
		                    exchange = true;
		                }
		            }
		            if (!exchange)
		                break;
		        }
		        return sches;
		    }
		   
		   /**
		    * 计算罚金
		    * @param loan
		    * @param plans
		    */
		   public List<BigDecimal> calculateMulct(CcsLoan loan, Date calculateDate,List<CcsPlan> plans) {
			   List<BigDecimal> amtList = new ArrayList<BigDecimal>();
			   
			   //获取贷款的罚金收取参数
			   // 获取参数
				LoanFeeDef loanFeeDef = rescheduleUtils.getLoanFeeDef(loan.getLoanCode(), loan.getLoanInitTerm(), 
						loan.getLoanInitPrin(),loan.getLoanFeeDefId());
				if(loanFeeDef.mulctTableId == null || loanFeeDef.mulctTableId.equals("")){
					   return amtList;
				}else{
					Mulct fine = parameterFacility.loadParameter(loanFeeDef.mulctTableId, Mulct.class);
					return mulctMethodimple.valueOf(fine.mulctMethod.toString()).calcuteMulce(loan,fine,calculateDate,amtList,plans);
						
				}
				
			}
		   
		   

			
			// 罚金收取方式 CalcMethodimple
			public enum mulctMethodimple {
				// CPD|按CPD规则收取"
				CPD {
					@Override
					public
					List<BigDecimal> calcuteMulce(CcsLoan loan, Mulct fine,Date calculateDate,List<BigDecimal> amtList,List<CcsPlan> plans) {
						if(loan.getCpdBeginDate() == null || loan.getOverdueDate() == null ) return amtList;
						//到指定日期的cpd起始日期的天数
						 int newCPDOverdueDay = DateUtils.getIntervalDays(loan.getCpdBeginDate(), calculateDate);
						 
						 int oldCPDOverdueDay = 0 ;
						 if(loan.getLastPenaltyDate() != null){
							//重算上一罚金收取日期的逾期天数
							 oldCPDOverdueDay = DateUtils.getIntervalDays(loan.getCpdBeginDate(), loan.getLastPenaltyDate());
							
						 }
						//只剩罚金不收取罚金
						BigDecimal mucleAMT = BigDecimal.ZERO;
						BigDecimal currBal = BigDecimal.ZERO;
						for(CcsPlan plan : plans){
							int term = plan.getTerm()==null?0:plan.getTerm();
							if(loan.getRefNbr().equals(plan.getRefNbr()) && loan.getCurrTerm()>=term && plan.getPlanType().isXfrIn()){
								mucleAMT = mucleAMT.add(plan.getCtdMulctAmt()).add(plan.getPastMulctAmt());
								currBal = currBal.add(plan.getCurrBal());
							}
						}
						if(mucleAMT.compareTo(currBal) == 0){
							return amtList;
						}
						for(MulctDef fineDef : fine.mulctDefs){
							if (fineDef.cpdOverDays > oldCPDOverdueDay && fineDef.cpdOverDays <= newCPDOverdueDay ){
								amtList.add(MucleCalcMethodimple.valueOf(fine.mulctCalMethod.toString()).calcMucle(loan.getLoanInitPrin(), fineDef));
							}
						}
						
						return amtList;
					}
						
				},
				//DPD|按DPD规则收取
				DPD {
					@Override
					public
					List<BigDecimal> calcuteMulce(CcsLoan loan, Mulct fine,Date calculateDate,List<BigDecimal> amtList,List<CcsPlan> plans) {
						if(loan.getOverdueDate() == null) return amtList;
						//到指定日期的dpd起始日期的天数
						 int newDPDOverdueDay = DateUtils.getIntervalDays(loan.getOverdueDate(), calculateDate);

						 int oldDPDOverdueDay = 0 ;
						 if(loan.getLastPenaltyDate() != null){
							 //重算上一罚金收取日期的逾期天数
							 oldDPDOverdueDay = DateUtils.getIntervalDays(loan.getOverdueDate(), loan.getLastPenaltyDate());
							
						 }
						for(MulctDef fineDef : fine.mulctDefs){
							if(fineDef.mulctOverDays > oldDPDOverdueDay && fineDef.mulctOverDays <= newDPDOverdueDay){
								amtList.add(MucleCalcMethodimple.valueOf(fine.mulctCalMethod.toString()).calcMucle(loan.getLoanInitPrin(), fineDef));
							}
						}
						 
						return amtList;
					}
				};

				// 罚金收取方式
				/**
				 * 罚金收取方式
				 * @param loan
				 * @param fine
				 * @param calculateDate
				 * @param amtList 
				 * @return
				 */
				public abstract List<BigDecimal> calcuteMulce(CcsLoan loan,Mulct fine,Date calculateDate,List<BigDecimal> amtList,List<CcsPlan> plans);
			}
			// 罚金计算方式 CalcMucle
			public enum MucleCalcMethodimple {
				// 按照百分比计算
				R {
					@Override
					BigDecimal calcMucle(BigDecimal AMT, MulctDef fineDef) {
						return AMT.multiply(fineDef.mulctOverRate).setScale(2, BigDecimal.ROUND_HALF_UP);
					}
						
				},
				// 固定金额
				D {
					@Override
					BigDecimal calcMucle(BigDecimal AMT, MulctDef fineDef) {
						return fineDef.mulctOverAmt.setScale(2, BigDecimal.ROUND_HALF_UP);
					}
				};

				// 计算分期手续费
				abstract BigDecimal calcMucle(BigDecimal AMT,MulctDef fineDef);
			}
			
			/**
			 * 设置loanReg的利率
			 * @param loanReg
			 * @param loanFeeDef
			 * @param agreement 是否使用协议利率
			 * @return
			 */
			public CcsLoanReg setLoanRegRate(CcsLoanReg loanReg,LoanFeeDef loanFeeDef,boolean agreement){
				//使用协议费率，这些费率都取loanreg中的
				//利息利率
				if(agreement){
//					if(loanReg.getInterestRate() == null){
//						loanReg.setInterestRate(loanFeeDef.interestRate);
//					}
//					if(loanReg.getPenaltyRate() == null){
//						loanReg.setPenaltyRate(loanFeeDef.penaltyIntTableId);
//					}
//					if(loanReg.getCompoundRate() == null ){
//						loanReg.setCompoundRate(loanFeeDef.compoundIntTableId);
//					}
//					//印花税费率
//					if(loanReg.getStampdutyRate() == null && loanFeeDef.stampRate != null){
//						loanReg.setStampdutyRate(loanFeeDef.stampRate);
//					}
//					if(loanReg.getStampAmt() == null && loanFeeDef.stampAMT != null){
//						loanReg.setStampAmt(loanFeeDef.stampAMT);
//					}
//					
//					//服务费率
//					if(loanReg.getFeeAmt() == null && loanFeeDef.feeAmount != null){
//						loanReg.setFeeAmt(loanFeeDef.feeAmount);
//					}
//					if(loanReg.getFeeRate() == null && loanFeeDef.feeRate != null){
//						loanReg.setFeeRate(loanFeeDef.feeRate);
//					}
//					
//					//寿险费率
//					if(loanReg.getLifeInsuFeeAmt() == null && loanFeeDef.lifeInsuFeeAmt != null){
//						loanReg.setLifeInsuFeeAmt(loanFeeDef.lifeInsuFeeAmt);
//					}
//					if(loanReg.getLifeInsuFeeRate() == null && loanFeeDef.lifeInsuFeeRate != null){
//						loanReg.setLifeInsuFeeRate(loanFeeDef.lifeInsuFeeRate);
//					}
//					
//					//保险费率
//					if(loanReg.getInsAmt() == null && loanFeeDef.insAmt != null){
//						loanReg.setInsAmt(loanFeeDef.insAmt);
//					}
//					if(loanReg.getInsuranceRate() == null && loanFeeDef.insRate != null){
//						loanReg.setInsuranceRate(loanFeeDef.insRate);
//					}
//					
//					//手续费率
//					if(loanReg.getInstallmentFeeAmt() == null && loanFeeDef.installmentFeeAmt != null){
//						loanReg.setInstallmentFeeAmt(loanFeeDef.installmentFeeAmt);
//					}
//					if(loanReg.getInstallmentFeeRate() == null && loanFeeDef.installmentFeeRate != null){
//						loanReg.setInstallmentFeeRate(loanFeeDef.installmentFeeRate);
//					}
//					
//					//提前还款计划包费率
//					if(loanReg.getPrepayPkgFeeAmt() == null && loanFeeDef.prepayPkgFeeAmount != null){
//						loanReg.setPrepayPkgFeeAmt(loanFeeDef.prepayPkgFeeAmount);
//					}
//					if(loanReg.getPrepayPkgFeeRate() == null && loanFeeDef.prepayPkgFeeAmountRate != null){
//						loanReg.setPrepayPkgFeeRate(loanFeeDef.prepayPkgFeeAmountRate);
//					}
				}else{
					//全部取参数设置
					//利息利率
					loanReg.setInterestRate(loanFeeDef.interestRate);
					loanReg.setPenaltyRate(loanFeeDef.penaltyIntTableId);
					loanReg.setCompoundRate(loanFeeDef.compoundIntTableId);
					//设置费率
					if(loanFeeDef.stampRate != null){
						loanReg.setStampdutyRate(loanFeeDef.stampRate);
					}
					if(loanFeeDef.stampAMT != null){
						loanReg.setStampdutyRate(loanFeeDef.stampAMT);
					}
					//服务费率
					if(loanFeeDef.feeAmount != null){
						loanReg.setFeeAmt(loanFeeDef.feeAmount);
					}
					if(loanFeeDef.feeRate != null){
						loanReg.setFeeRate(loanFeeDef.feeRate);
					}
					//寿险费率
					if(loanFeeDef.lifeInsuFeeAmt != null){
						loanReg.setLifeInsuFeeAmt(loanFeeDef.lifeInsuFeeAmt);
					}
					if(loanFeeDef.lifeInsuFeeRate != null){
						loanReg.setLifeInsuFeeRate(loanFeeDef.lifeInsuFeeRate);
					}
					//保险费率
					if(loanFeeDef.insAmt != null){
						loanReg.setInsAmt(loanFeeDef.insAmt);
					}
					if(loanFeeDef.insRate != null){
						loanReg.setInsuranceRate(loanFeeDef.insRate);
					}
					//手续费率
					if(loanFeeDef.installmentFeeAmt != null){
						loanReg.setInstallmentFeeAmt(loanFeeDef.installmentFeeAmt);
					}
					if(loanFeeDef.installmentFeeRate != null){
						loanReg.setInstallmentFeeRate(loanFeeDef.installmentFeeRate);
					}
					//灵活还款计划包费率
					if(loanFeeDef.prepayPkgFeeAmount != null){
						loanReg.setPrepayPkgFeeRate(loanFeeDef.prepayPkgFeeAmountRate);
					}
					if(loanFeeDef.prepayPkgFeeAmountRate != null){
						loanReg.setPrepayPkgFeeAmt(loanFeeDef.prepayPkgFeeAmount);
					}
					
					// 代收服务费率
					if(loanFeeDef.replaceFeeRate != null){
						loanReg.setReplaceSvcFeeRate(loanFeeDef.replaceFeeRate);
					}
					if(loanFeeDef.replaceFeeAmt !=null){
						loanReg.setReplaceSvcFeeAmt(loanFeeDef.replaceFeeAmt);
					}
				}
				return loanReg;
			}
			
	// 代收服务费收取方式
	public enum LoanReplaceSvcFeeMethodimple {
		/**
		 * 一次性收取
		 */
		F {
			@Override
			public BigDecimal loanReplaceSvcFee(CcsLoanReg loanReg, LoanFeeDef loanFeeDef,int Flag) {
				
				// 每期的服务费
				BigDecimal loanFixedFee = ReplaceSvcFeeCalMethodimple.valueOf(
						loanFeeDef.replaceFeeCalMethod.toString()).calcuteReplaceSvcFee(
								loanReg, loanFeeDef);
				// 总的服务费
				BigDecimal loanInitFee = loanFixedFee.multiply(new BigDecimal(loanReg.getLoanInitTerm())).setScale( 2, RoundingMode.HALF_UP);
				
				switch (Flag) {
				case 0:
					// 总代收服务费
					return loanInitFee;
				case 1:
					// 每期代收服务费
					return BigDecimal.ZERO;
				case 2:
					// 首期代收服务费
					return loanInitFee;
				default:
					return BigDecimal.ZERO;
				}
			}
		},
		/**
		 * 分期收取
		 */
		E {
			@Override
			public BigDecimal loanReplaceSvcFee(CcsLoanReg loanReg, LoanFeeDef loanFeeDef,int Flag) {

				// 每期的服务费
				BigDecimal loanFixedFee = ReplaceSvcFeeCalMethodimple.valueOf(
						loanFeeDef.replaceFeeCalMethod.toString()).calcuteReplaceSvcFee(
								loanReg, loanFeeDef);
				// 总的服务费
				BigDecimal loanInitFee = loanFixedFee.multiply(new BigDecimal(loanReg.getLoanInitTerm())).setScale( 2, RoundingMode.HALF_UP);
				
				switch (Flag) {
				case 0:
					// 总代收服务费
					return loanInitFee;
				case 1:
					// 每期代收服务费
					return loanFixedFee;
				case 2:
					// 首期代收服务费
					return loanFixedFee;
				default:
					return BigDecimal.ZERO;
				}
			}
		};
		/**
		 * 代收服务费计算
		 * @param loanReg
		 * @param loanFeeDef
		 * @param Flag 0为总费用，1为每期费用，2为首期费用
		 * @return
		 */
		public abstract BigDecimal loanReplaceSvcFee(CcsLoanReg loanReg, LoanFeeDef loanFeeDef,int Flag);
	}
	
	// 代收服务服务费计算方式
	public enum ReplaceSvcFeeCalMethodimple {
		// 按照百分比计算
		R {
			@Override
			BigDecimal calcuteReplaceSvcFee(CcsLoanReg loanReg, LoanFeeDef loanFeeDef) {
				if(loanReg.getReplaceSvcFeeRate() == null ){
					return loanReg.getLoanInitPrin().multiply(loanFeeDef.replaceFeeRate).setScale(2, BigDecimal.ROUND_HALF_UP);
				}else{
					return loanReg.getLoanInitPrin().multiply(loanReg.getReplaceSvcFeeRate()).setScale(2, BigDecimal.ROUND_HALF_UP);
				}
			}
				
		},
		// 固定金额
		A {
			@Override
			BigDecimal calcuteReplaceSvcFee(CcsLoanReg loanReg, LoanFeeDef loanFeeDef) {
				if(loanReg.getReplaceSvcFeeAmt() == null){
					return loanFeeDef.replaceFeeAmt.setScale(2, BigDecimal.ROUND_HALF_UP);
				}else{
					return loanReg.getReplaceSvcFeeAmt().setScale(2, BigDecimal.ROUND_HALF_UP);
				}
			}
		};

		/**
		 * 计算代收服务费
		 * @param loanInitPrin
		 * @param loanFeeDef
		 * @return
		 */
		abstract BigDecimal calcuteReplaceSvcFee(CcsLoanReg loanReg,LoanFeeDef loanFeeDef);
	}
	
	//灵活还款计划包费计算
	public enum PrepayPkgMethodimple {
		/**
		 * 一次性收取
		 */
		F {
			@Override
			public BigDecimal loanPrepayPkg(CcsLoanReg loanReg,
					LoanFeeDef loanFeeDef, int Flag) {

				// 每期灵活还款计划包
				BigDecimal loanFixedFee = PrepayPkgCalMethodimple.valueOf(
						loanFeeDef.prepayPkgFeeCalMethod.toString())
						.calcutePrepayPkg(loanReg, loanFeeDef);
				// 总灵活还款计划包
				BigDecimal loanInitFee = loanFixedFee.multiply(new BigDecimal(
						loanReg.getLoanInitTerm())).setScale(2, RoundingMode.HALF_UP);
				switch (Flag) {
				case 0:
					// 总灵活还款计划包
					return loanInitFee;
				case 1:
					// 每期灵活还款计划包
					return BigDecimal.ZERO;
				case 2:
					// 首期灵活还款计划包
					return loanInitFee;
				default:
					return BigDecimal.ZERO;
				}
			}
		},
		/**
		 * 分期收取
		 */
		E {
			@Override
			public BigDecimal loanPrepayPkg(CcsLoanReg loanReg,
					LoanFeeDef loanFeeDef, int Flag) {

				// 每期灵活还款计划包
				BigDecimal loanFixedFee = PrepayPkgCalMethodimple.valueOf(
						loanFeeDef.prepayPkgFeeCalMethod.toString())
						.calcutePrepayPkg(loanReg, loanFeeDef);
				// 总灵活还款计划包
				BigDecimal loanInitFee = loanFixedFee.multiply(new BigDecimal(
						loanReg.getLoanInitTerm())).setScale(2, RoundingMode.HALF_UP);
				switch (Flag) {
				case 0:
					// 总灵活还款计划包
					return loanInitFee;
				case 1:
					// 每期灵活还款计划包
					return loanFixedFee;
				case 2:
					// 首期灵活还款计划包
					return loanFixedFee;
				default:
					return BigDecimal.ZERO;
				}
			}
		};
		/**
		 * 灵活还款计划包计算
		 * 
		 * @param loanReg
		 * @param loanFeeDef
		 * @param Flag
		 *            0为总费用，1为每期费用，2为首期费用
		 * @return
		 */
		public abstract BigDecimal loanPrepayPkg(CcsLoanReg loanReg,
				LoanFeeDef loanFeeDef, int Flag);
	}

	// 灵活还款计划包计算方式
	public enum PrepayPkgCalMethodimple {
		// 按照百分比计算
		R {
			@Override
			BigDecimal calcutePrepayPkg(CcsLoanReg loanReg,
					LoanFeeDef loanFeeDef) {
				if (loanReg.getPrepayPkgFeeRate() == null) {
					return loanReg.getLoanInitPrin()
							.multiply(loanFeeDef.prepayPkgFeeAmountRate)
							.setScale(2, BigDecimal.ROUND_HALF_UP);
				} else {
					return loanReg.getLoanInitPrin()
							.multiply(loanReg.getPrepayPkgFeeRate())
							.setScale(2, BigDecimal.ROUND_HALF_UP);
				}
			}

		},
		// 固定金额
		A {
			@Override
			BigDecimal calcutePrepayPkg(CcsLoanReg loanReg,
					LoanFeeDef loanFeeDef) {
				if (loanReg.getPrepayPkgFeeAmt() == null) {
					return loanFeeDef.prepayPkgFeeAmount.setScale(2,
							BigDecimal.ROUND_HALF_UP);
				} else {
					return loanReg.getPrepayPkgFeeAmt().setScale(2,
							BigDecimal.ROUND_HALF_UP);
				}
			}
		};

		/**
		 * 计算灵活还款计划包
		 * 
		 * @param loanInitPrin
		 * @param loanFeeDef
		 * @return
		 */
		abstract BigDecimal calcutePrepayPkg(CcsLoanReg loanReg,
				LoanFeeDef loanFeeDef);
	}
		
	//代收罚金收取列表
	public List<BigDecimal> calculateReplaceMulct(CcsLoan loan, Date calculateDate,List<CcsPlan> plans) {
		List<BigDecimal> amtList = new ArrayList<BigDecimal>();
		//获取贷款的罚金收取参数
		// 获取参数
		LoanFeeDef loanFeeDef = rescheduleUtils.getLoanFeeDef(loan.getLoanCode(), loan.getLoanInitTerm(), 
			loan.getLoanInitPrin(),loan.getLoanFeeDefId());
		if(loanFeeDef.replaceMulctTableId == null || loanFeeDef.replaceMulctTableId.equals("")){
			return amtList;
		}else{
			Mulct fine = parameterFacility.loadParameter(loanFeeDef.replaceMulctTableId, Mulct.class);
		return mulctMethodimple.valueOf(fine.mulctMethod.toString()).calcuteMulce(loan,fine,calculateDate,amtList,plans);
		}
	}
	
	//代收提前还款手续费计算
	// 提前还款收取方式 CalcMethodimple
	public enum ReplacePrepayMethodimple {
		// 按照百分比计算
		R {
			@Override
			public BigDecimal calcuteEarlySettle(BigDecimal loanInitPrin, LoanFeeDef loanFeeDef,
					int currTerm) {
				List<ReplaceEarlyRepayDef> earlyRepayDefs = sortReplacePrepayDef(loanFeeDef.replaceEarlyRepayDef);
				ReplaceEarlyRepayDef erDef = null;
				for (ReplaceEarlyRepayDef earlyRepayDef : earlyRepayDefs) {
					if (earlyRepayDef.replaceAdCurPeriod >= currTerm) {
						erDef = earlyRepayDef;
						break;
					}
				}
				if (erDef == null)
					return BigDecimal.ZERO;

				return loanInitPrin.multiply(erDef.replaceAdFeeScale).setScale(2,
						BigDecimal.ROUND_HALF_UP);
			}

		},
		// 固定金额
		A {
			@Override
			public BigDecimal calcuteEarlySettle(BigDecimal loanInitPrin, LoanFeeDef loanFeeDef,
					int currTerm) {
				List<ReplaceEarlyRepayDef> earlyRepayDefs = sortReplacePrepayDef(loanFeeDef.replaceEarlyRepayDef);
				ReplaceEarlyRepayDef erDef = null;
				for (ReplaceEarlyRepayDef earlyRepayDef : earlyRepayDefs) {
					if (earlyRepayDef.replaceAdCurPeriod >= currTerm) {
						erDef = earlyRepayDef;
						break;
					}
				}
				if (erDef == null)
					return BigDecimal.ZERO;

				return erDef.replaceAdFeeAmt;
			}
		};

		// 提前还款收取方式
		/**
		 * 代收提前还款收取方式
		 * 
		 * @param loanInitPrin
		 * @param loanFeeDef
		 * @param currTerm
		 *            当前期数
		 * @return
		 */
		public abstract BigDecimal calcuteEarlySettle(BigDecimal loanInitPrin,
				LoanFeeDef loanFeeDef, int currTerm);
	}

	/**
	 * 提前还款收取方式按照期数大小进行排序
	 * 
	 * @param arr
	 * @return
	 */
	private static List<ReplaceEarlyRepayDef> sortReplacePrepayDef(List<ReplaceEarlyRepayDef> arr) { // 交换排序->冒泡排序
		ReplaceEarlyRepayDef temp = null;
		boolean exchange = false;
		for (int i = 0; i < arr.size(); i++) {
			exchange = false;
			for (int j = arr.size() - 2; j >= i; j--) {
				if (arr.get(j + 1).replaceAdCurPeriod.compareTo(arr.get(j).replaceAdCurPeriod) <= 0) {
					temp = arr.get(j + 1);
					arr.set(j + 1, arr.get(j));
					arr.set(j, temp);
					exchange = true;
				}
			}
			if (!exchange)
				break;
		}
		return arr;
	}
}
