package com.sunline.ccs.batch.cc6000.common;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.PlanType;
import com.sunline.ccs.batch.common.EnumUtils;
import com.sunline.ccs.facility.MicroCreditRescheduleUtils;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.param.def.PlanTemplate;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.TxnCd;
import com.sunline.ccs.param.def.enums.CompensateStatus;
import com.sunline.ccs.param.def.enums.InterestAdjMethod;


/**
 * @see 类名：PlanManager
 * @see 描述：信用计划管理类
              信用计划模板不存在；呆账账户不允许建借记计划；
 *
 * @see 创建日期：   2015-6-24下午7:31:53
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Component
public class PlanManager {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private UnifiedParameterFacility parameterFacility;
	
	@Autowired
	private MicroCreditRescheduleUtils rescheduleUtils;

	/**
	 * @see 方法名：generateTxnPlan 
	 * @see 描述：根据交易，建立信用计划
	 * @see 创建日期：2015-6-24下午7:32:33
	 * @author ChengChun
	 *  
	 * @param account
	 * @param loans
	 * @param logicCardNbr
	 * @param productCd
	 * @param refNbr
	 * @param template
	 * @param batchDate
	 * @param term
	 * @return
	 * @throws Exception
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public CcsPlan generateTxnPlan(CcsAcct account, List<CcsLoan> loans, String logicCardNbr,
			String productCd, String refNbr, PlanTemplate template, Date batchDate, Integer term) throws Exception {
		
		CcsPlan plan = new CcsPlan();
		switch (template.planType){
		case I:
		case O:
			setRefNbr(refNbr, plan);
			plan.setInterestRate(BigDecimal.ZERO);
			plan.setPenaltyRate(BigDecimal.ZERO);
			plan.setCompoundRate(BigDecimal.ZERO);
			plan.setReplacePenaltyRate(BigDecimal.ZERO);
			plan.setUsePlanRate(Indicator.N);
			break;
		case P:
			setRefNbr(refNbr, plan);
			plan.setInterestRate(BigDecimal.ZERO);
			plan.setPenaltyRate(BigDecimal.ZERO);
			plan.setCompoundRate(BigDecimal.ZERO);
			plan.setReplacePenaltyRate(BigDecimal.ZERO);
			plan.setUsePlanRate(Indicator.Y);
			break;
		case J:
			setRefNbr(refNbr, plan);
			setAgreementRate(plan, matchTmLoan(refNbr, loans), template.planType);
			plan.setUsePlanRate(Indicator.Y);
			break;
		case Q:
		case L:
			setRefNbr(refNbr, plan);
			setAgreementRate(plan, matchTmLoan(refNbr, loans), template.planType);
			plan.setUsePlanRate(Indicator.Y);
			plan.setTerm(term);
			break;
		case C:
		case D:
		case R:
		case Z:
			plan.setInterestRate(BigDecimal.ZERO);
			plan.setPenaltyRate(BigDecimal.ZERO);
			plan.setCompoundRate(BigDecimal.ZERO);
			plan.setReplacePenaltyRate(BigDecimal.ZERO);
			plan.setUsePlanRate(Indicator.N);
			break;
		default: throw new IllegalArgumentException("新建分期或贷款计划时，计划类型异常"+template.planType);
		}
		
		plan.setOrg(account.getOrg());
		//plan.setPlanId(null);
		plan.setAcctNbr(account.getAcctNbr());
		plan.setAcctType(account.getAcctType());
		plan.setLogicCardNbr(logicCardNbr);
		plan.setPlanNbr(template.planNbr);
		plan.setPlanType(template.planType);
		plan.setProductCd(productCd);
		plan.setCurrBal(BigDecimal.ZERO);
		plan.setBegBal(BigDecimal.ZERO);
		plan.setDisputeAmt(BigDecimal.ZERO);
		plan.setTotDueAmt(BigDecimal.ZERO);
		plan.setPlanAddDate(batchDate);
		plan.setPaidOutDate(null);
		plan.setPastPrincipal(BigDecimal.ZERO);
		plan.setPastInterest(BigDecimal.ZERO);
		plan.setPastCardFee(BigDecimal.ZERO);
		plan.setPastOvrlmtFee(BigDecimal.ZERO);
		plan.setPastLateFee(BigDecimal.ZERO);
		plan.setPastNsfundFee(BigDecimal.ZERO);
		plan.setPastTxnFee(BigDecimal.ZERO);
		plan.setPastSvcFee(BigDecimal.ZERO);
		plan.setPastInsurance(BigDecimal.ZERO);
		plan.setPastUserFee1(BigDecimal.ZERO);
		plan.setPastUserFee2(BigDecimal.ZERO);
		plan.setPastUserFee3(BigDecimal.ZERO);
		plan.setPastUserFee4(BigDecimal.ZERO);
		plan.setPastUserFee5(BigDecimal.ZERO);
		plan.setPastUserFee6(BigDecimal.ZERO);
		plan.setCtdPrincipal(BigDecimal.ZERO);
		plan.setCtdInterest(BigDecimal.ZERO);
		plan.setCtdCardFee(BigDecimal.ZERO);
		plan.setCtdOvrlmtFee(BigDecimal.ZERO);
		plan.setCtdLateFee(BigDecimal.ZERO);
		plan.setCtdNsfundFee(BigDecimal.ZERO);
		plan.setCtdSvcFee(BigDecimal.ZERO);
		plan.setCtdTxnFee(BigDecimal.ZERO);
		plan.setCtdInsurance(BigDecimal.ZERO);
		plan.setCtdUserFee1(BigDecimal.ZERO);
		plan.setCtdUserFee2(BigDecimal.ZERO);
		plan.setCtdUserFee3(BigDecimal.ZERO);
		plan.setCtdUserFee4(BigDecimal.ZERO);
		plan.setCtdUserFee5(BigDecimal.ZERO);
		plan.setCtdUserFee6(BigDecimal.ZERO);
		plan.setCtdAmtDb(BigDecimal.ZERO);
		plan.setCtdAmtCr(BigDecimal.ZERO);
		plan.setCtdNbrDb(0);
		plan.setCtdNbrCr(0);
		plan.setNodefbnpIntAcru(BigDecimal.ZERO);
		plan.setBegDefbnpIntAcru(BigDecimal.ZERO);
		plan.setCtdDefbnpIntAcru(BigDecimal.ZERO);
		plan.setUserNumber1(0);
		plan.setUserNumber2(0);
		plan.setUserNumber3(0);
		plan.setUserNumber4(0);
		plan.setUserNumber5(0);
		plan.setUserNumber6(0);
		plan.setUserAmt1(BigDecimal.ZERO);
		plan.setUserAmt2(BigDecimal.ZERO);
		plan.setUserAmt3(BigDecimal.ZERO);
		plan.setUserAmt4(BigDecimal.ZERO);
		plan.setUserAmt5(BigDecimal.ZERO);
		plan.setUserAmt6(BigDecimal.ZERO);
		
		plan.setCtdPenalty(BigDecimal.ZERO);
		plan.setPastPenalty(BigDecimal.ZERO);
		plan.setCtdCompound(BigDecimal.ZERO);
		plan.setPastCompound(BigDecimal.ZERO);
		plan.setPenaltyAcru(BigDecimal.ZERO);
		plan.setCompoundAcru(BigDecimal.ZERO);
		
		plan.setCtdLifeInsuAmt(BigDecimal.ZERO);
		plan.setPastLifeInsuAmt(BigDecimal.ZERO);
		plan.setCtdMulctAmt(BigDecimal.ZERO);
		plan.setPastMulctAmt(BigDecimal.ZERO);
		plan.setCtdStampdutyAmt(BigDecimal.ZERO);
		plan.setPastStampdutyAmt(BigDecimal.ZERO);
		plan.setAccruPrinSum(BigDecimal.ZERO);
		plan.setLastAccruPrinSum(BigDecimal.ZERO);
		plan.setPastPrepayPkgFee(BigDecimal.ZERO);
		plan.setCtdPrepayPkgFee(BigDecimal.ZERO);
		
		plan.setCtdReplaceSvcFee(BigDecimal.ZERO);
		plan.setPastReplaceSvcFee(BigDecimal.ZERO);
		
		plan.setCtdReplacePenalty(BigDecimal.ZERO);
		plan.setPastReplacePenalty(BigDecimal.ZERO);
		plan.setCtdReplaceMulct(BigDecimal.ZERO);
		plan.setPastReplaceMulct(BigDecimal.ZERO);
		plan.setCtdReplaceLateFee(BigDecimal.ZERO);
		plan.setPastReplaceLateFee(BigDecimal.ZERO);
		plan.setReplacePenaltyAcru(BigDecimal.ZERO);
		plan.setCtdReplaceTxnFee(BigDecimal.ZERO);
		plan.setPastReplaceTxnFee(BigDecimal.ZERO);
		// 代偿统计
		plan.setCompensateAmt(BigDecimal.ZERO);
		plan.setCompensateStatus(CompensateStatus.NotCompensated);
		plan.setGraceNodefbnpIntAcru(BigDecimal.ZERO);
		
		return plan;
	}

	/** 
	 * @see 方法名：matchTmLoan 
	 * @see 描述：匹配Loan
	 * @see 创建日期：2015年12月16日
	 * @author MengXiang
	 *  
	 * @param refNbr
	 * @param loans
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private CcsLoan matchTmLoan(String refNbr, List<CcsLoan> loans) {
		
		CcsLoan loan = null;
		
		for(CcsLoan loanTemp : loans){
			if(refNbr.trim().equals(loanTemp.getRefNbr().trim())){
				loan = loanTemp;
				break;
			}
		}
		
		if(loan == null ){
			logger.error("未匹配到Loan,RefNbr=["+refNbr+"]");
			throw new IllegalArgumentException("新建分期或贷款计划时，未匹配到Loan");
		}
		
		return loan;
	}

	/** 
	 * @see 方法名：setAgreementRate 
	 * @see 描述：设置协议利率
	 * @see 创建日期：2015年12月16日
	 * @author MengXiang
	 *  
	 * @param loan
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private void setAgreementRate(CcsPlan plan,CcsLoan loan,PlanType planType) {
		
		LoanFeeDef loanFeeDef = rescheduleUtils.getLoanFeeDef(loan.getLoanCode(), loan.getLoanInitTerm(), 
				loan.getLoanInitPrin(),loan.getLoanFeeDefId());
		
		BigDecimal interestRate = loan.getInterestRate()==null ? loanFeeDef.interestRate : loan.getInterestRate();//基础利率
		BigDecimal penaltyRate = loan.getPenaltyRate()==null ? loanFeeDef.penaltyIntTableId : loan.getPenaltyRate();//罚息利率
		BigDecimal compoundRate = loan.getCompoundRate() == null ? loanFeeDef.compoundIntTableId : loan.getCompoundRate();//复利利率
		BigDecimal replacePenaltyRate = loan.getReplacePenaltyRate()==null?loanFeeDef.replacePenaltyRate:loan.getReplacePenaltyRate();//代收罚息利率
		// 使用浮动利率
		if (InterestAdjMethod.L.equals(loanFeeDef.interestAdjMethod) && loan.getFloatRate() != null) {
			interestRate = interestRate.multiply(BigDecimal.ONE.add(loan.getFloatRate()));
			penaltyRate = penaltyRate.multiply(BigDecimal.ONE.add(loan.getFloatRate()));
			compoundRate = compoundRate.multiply(BigDecimal.ONE.add(loan.getFloatRate()));
			replacePenaltyRate = replacePenaltyRate.multiply(BigDecimal.ONE.add(loan.getFloatRate()));
		}
		
		// 等额本息转出计划：基本利率-0、罚息利率-0、复利利率-0
		// 随借随还转出计划：基本利率-Loan的基本利率、罚息利率-0、复利利率-0
		// 等额本息和随借随还转入计划：基本利率-不使用逾期利率为0，使用逾期利率为Loan的基本利率、罚息利率-Loan的罚息利率、复利利率-Loan的复利利率
		if (PlanType.P.equals(planType)) {
			plan.setInterestRate(BigDecimal.ZERO);
			plan.setPenaltyRate(BigDecimal.ZERO);
			plan.setCompoundRate(BigDecimal.ZERO);
			plan.setReplacePenaltyRate(BigDecimal.ZERO);
		} else if (PlanType.J.equals(planType)) {
			plan.setInterestRate(interestRate);
			plan.setPenaltyRate(BigDecimal.ZERO);
			plan.setCompoundRate(BigDecimal.ZERO);
			plan.setReplacePenaltyRate(BigDecimal.ZERO);
		} else if (PlanType.Q.equals(planType) || PlanType.L.equals(planType)) {
			plan.setInterestRate(interestRate);
			plan.setPenaltyRate(penaltyRate);
			plan.setCompoundRate(compoundRate);
			plan.setReplacePenaltyRate(replacePenaltyRate);
			// 逾期计息则取Loan上的利率，否则设为0
			LoanPlan loanPlan= parameterFacility.loadParameter(loan.getLoanCode(), LoanPlan.class);
			if (Indicator.N.equals(loanPlan.isOverAccruIns)) {
				plan.setInterestRate(BigDecimal.ZERO);
			}
		}
	}
	
	/** 
	 * @see 方法名：setRefNbr 
	 * @see 描述：设置refNbr
	 * @see 创建日期：2015年6月25日下午4:47:40
	 * @author yuyang
	 *  
	 * @param refNbr
	 * @param newPlan
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private void setRefNbr(String refNbr, CcsPlan newPlan) {
		if(refNbr!=null){
			newPlan.setRefNbr(refNbr);
		}else{
			throw new IllegalArgumentException("新建分期或贷款计划时，refNbr不允许为空");
		}
	}

	/**
	 * @see 方法名：generateXfrInPlan 
	 * @see 描述：根据分期转出计划，建立分期转入信用计划
	 * @see 创建日期：2015-6-24下午7:33:13
	 * @author ChengChun
	 *  
	 * @param xfrOutPlan
	 * @param loan
	 * @param xfrInTemplate
	 * @param batchDate
	 * @param term
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public CcsPlan generateXfrInPlan(CcsPlan xfrOutPlan, CcsLoan loan, PlanTemplate xfrInTemplate, Date batchDate, Integer term) {
		
		logger.debug("新建信用计划:bscLogiccardNo["+xfrOutPlan.getLogicCardNbr()
				+"],planNbr["+xfrInTemplate.planNbr+"],planType["+xfrInTemplate.planType+"],refNbr["+xfrOutPlan.getRefNbr()+"]");
		
		CcsPlan newPlan = new CcsPlan();
		
		newPlan.setOrg(xfrOutPlan.getOrg());
		//newPlan.setPlanId(Integer.valueOf(0));
		newPlan.setAcctNbr(xfrOutPlan.getAcctNbr());
		newPlan.setAcctType(xfrOutPlan.getAcctType());
		newPlan.setLogicCardNbr(xfrOutPlan.getLogicCardNbr());
		newPlan.setPlanNbr(xfrInTemplate.planNbr);
		newPlan.setPlanType(xfrInTemplate.planType);
		newPlan.setProductCd(xfrOutPlan.getProductCd());
		newPlan.setRefNbr(xfrOutPlan.getRefNbr());
		newPlan.setCurrBal(BigDecimal.ZERO);
		newPlan.setBegBal(BigDecimal.ZERO);
		newPlan.setDisputeAmt(BigDecimal.ZERO);
		newPlan.setTotDueAmt(BigDecimal.ZERO);
		newPlan.setPlanAddDate(batchDate);
		newPlan.setPaidOutDate(null);
		newPlan.setPastPrincipal(BigDecimal.ZERO);
		newPlan.setPastInterest(BigDecimal.ZERO);
		newPlan.setPastCardFee(BigDecimal.ZERO);
		newPlan.setPastOvrlmtFee(BigDecimal.ZERO);
		newPlan.setPastLateFee(BigDecimal.ZERO);
		newPlan.setPastNsfundFee(BigDecimal.ZERO);
		newPlan.setPastTxnFee(BigDecimal.ZERO);
		newPlan.setPastSvcFee(BigDecimal.ZERO);
		newPlan.setPastInsurance(BigDecimal.ZERO);
		newPlan.setPastUserFee1(BigDecimal.ZERO);
		newPlan.setPastUserFee2(BigDecimal.ZERO);
		newPlan.setPastUserFee3(BigDecimal.ZERO);
		newPlan.setPastUserFee4(BigDecimal.ZERO);
		newPlan.setPastUserFee5(BigDecimal.ZERO);
		newPlan.setPastUserFee6(BigDecimal.ZERO);
		newPlan.setCtdPrincipal(BigDecimal.ZERO);
		newPlan.setCtdInterest(BigDecimal.ZERO);
		newPlan.setCtdCardFee(BigDecimal.ZERO);
		newPlan.setCtdOvrlmtFee(BigDecimal.ZERO);
		newPlan.setCtdLateFee(BigDecimal.ZERO);
		newPlan.setCtdNsfundFee(BigDecimal.ZERO);
		newPlan.setCtdSvcFee(BigDecimal.ZERO);
		newPlan.setCtdTxnFee(BigDecimal.ZERO);
		newPlan.setCtdInsurance(BigDecimal.ZERO);
		newPlan.setCtdUserFee1(BigDecimal.ZERO);
		newPlan.setCtdUserFee2(BigDecimal.ZERO);
		newPlan.setCtdUserFee3(BigDecimal.ZERO);
		newPlan.setCtdUserFee4(BigDecimal.ZERO);
		newPlan.setCtdUserFee5(BigDecimal.ZERO);
		newPlan.setCtdUserFee6(BigDecimal.ZERO);
		newPlan.setCtdAmtDb(BigDecimal.ZERO);
		newPlan.setCtdAmtCr(BigDecimal.ZERO);
		newPlan.setCtdNbrDb(0);
		newPlan.setCtdNbrCr(0);
		newPlan.setNodefbnpIntAcru(BigDecimal.ZERO);
		newPlan.setBegDefbnpIntAcru(BigDecimal.ZERO);
		newPlan.setCtdDefbnpIntAcru(BigDecimal.ZERO);
		newPlan.setUserNumber1(0);
		newPlan.setUserNumber2(0);
		newPlan.setUserNumber3(0);
		newPlan.setUserNumber4(0);
		newPlan.setUserNumber5(0);
		newPlan.setUserNumber6(0);
		newPlan.setUserAmt1(BigDecimal.ZERO);
		newPlan.setUserAmt2(BigDecimal.ZERO);
		newPlan.setUserAmt3(BigDecimal.ZERO);
		newPlan.setUserAmt4(BigDecimal.ZERO);
		newPlan.setUserAmt5(BigDecimal.ZERO);
		newPlan.setUserAmt6(BigDecimal.ZERO);
		
		newPlan.setCtdPenalty(BigDecimal.ZERO);
		newPlan.setPastPenalty(BigDecimal.ZERO);
		newPlan.setCtdCompound(BigDecimal.ZERO);
		newPlan.setPastCompound(BigDecimal.ZERO);
		newPlan.setPenaltyAcru(BigDecimal.ZERO);
		newPlan.setCompoundAcru(BigDecimal.ZERO);
		
		newPlan.setCtdLifeInsuAmt(BigDecimal.ZERO);
		newPlan.setPastLifeInsuAmt(BigDecimal.ZERO);
		newPlan.setCtdMulctAmt(BigDecimal.ZERO);
		newPlan.setPastMulctAmt(BigDecimal.ZERO);
		newPlan.setCtdStampdutyAmt(BigDecimal.ZERO);
		newPlan.setPastStampdutyAmt(BigDecimal.ZERO);
		
		// 初始化利率为0，不使用Plan利率
		newPlan.setPenaltyRate(BigDecimal.ZERO);
		newPlan.setCompoundRate(BigDecimal.ZERO);
		newPlan.setInterestRate(BigDecimal.ZERO);
		newPlan.setReplacePenaltyRate(BigDecimal.ZERO);
		newPlan.setUsePlanRate(Indicator.N);
		
		// 贷款使用plan利率
		if (EnumUtils.in(xfrInTemplate.planType, PlanType.J,PlanType.L,PlanType.P,PlanType.Q)) {
			newPlan.setUsePlanRate(Indicator.Y);
		}
		
		// 处理协议利率
		setAgreementRate(newPlan,loan,xfrInTemplate.planType);
		
		newPlan.setAccruPrinSum(BigDecimal.ZERO);
		newPlan.setLastAccruPrinSum(BigDecimal.ZERO);
		newPlan.setTerm(term);
		newPlan.setPastPrepayPkgFee(BigDecimal.ZERO);
		newPlan.setCtdPrepayPkgFee(BigDecimal.ZERO);
		newPlan.setCtdReplaceSvcFee(BigDecimal.ZERO);
		newPlan.setPastReplaceSvcFee(BigDecimal.ZERO);
		
		newPlan.setCtdReplacePenalty(BigDecimal.ZERO);
		newPlan.setPastReplacePenalty(BigDecimal.ZERO);
		newPlan.setCtdReplaceMulct(BigDecimal.ZERO);
		newPlan.setPastReplaceMulct(BigDecimal.ZERO);
		newPlan.setCtdReplaceLateFee(BigDecimal.ZERO);
		newPlan.setPastReplaceLateFee(BigDecimal.ZERO);
		newPlan.setReplacePenaltyAcru(BigDecimal.ZERO);
		newPlan.setCtdReplaceTxnFee(BigDecimal.ZERO);
		newPlan.setPastReplaceTxnFee(BigDecimal.ZERO);
		// 代偿统计
		newPlan.setCompensateAmt(BigDecimal.ZERO);
		newPlan.setCompensateStatus(CompensateStatus.NotCompensated);
		newPlan.setGraceNodefbnpIntAcru(BigDecimal.ZERO);
		return newPlan;
	}
	
	/** 
	 * @see 方法名：findTxnPlans 
	 * @see 描述：查找plan
	 * @see 创建日期：2015年6月25日下午4:31:44
	 * @author yuyang
	 *  
	 * @param bscLogiccardNo
	 * @param planNbr
	 * @param planType
	 * @param refNbr
	 * @param plans
	 * @param term
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public List<CcsPlan> findTxnPlans(String bscLogiccardNo, String planNbr, PlanType planType, String refNbr, List<CcsPlan> plans,Integer term) {
		if(refNbr!=null){
			refNbr = refNbr.trim();
		}
		logger.debug("查找信用计划-交易信息:bscLogiccardNo["+bscLogiccardNo+"],planNbr["+planNbr+"],planType["+planType+"],refNbr["+refNbr+"]");
		
		List<CcsPlan> findPlans = new ArrayList<CcsPlan>();
		// 循环所有信用计划，查找交易对应信用计划索引号
		for (CcsPlan plan : plans) {
			logger.debug("查找信用计划-计划信息:bscLogiccardNo["+plan.getLogicCardNbr()
					+"],planNbr["+plan.getPlanNbr()+"],planType["+plan.getPlanType()+"],refNbr["+plan.getRefNbr()+"]");
			
			// 查找分期信用计划
			if (EnumUtils.in(planType, PlanType.O,PlanType.I,PlanType.J,PlanType.L,PlanType.P,PlanType.Q)) {
				// 得到此交易对应，分期信用计划
				if (refNbr != null && bscLogiccardNo.equals(plan.getLogicCardNbr())
						&& planNbr.equals(plan.getPlanNbr())
						&& planType.equals(plan.getPlanType())
						&& refNbr.equals(plan.getRefNbr())) {
					if(term != null && term!=0
							//随借随还不匹配日期
							&&PlanType.L!=planType&&PlanType.J!=planType){
						if(term.equals(plan.getTerm())){
							findPlans.add(plan);
							logger.debug("查找信用计划-找到计划:planId["+plan.getPlanId()+"]");
						}
					}else{
						findPlans.add(plan);
						logger.debug("查找信用计划-找到计划:planId["+plan.getPlanId()+"]");
					}
				}
			} 
			// 查找普通信用计划
			else {
				// 得到此交易对应，普通信用计划
				if (bscLogiccardNo.equals(plan.getLogicCardNbr())
						&& planNbr.equals(plan.getPlanNbr())
						&& planType.equals(plan.getPlanType())) {
					findPlans.add(plan);
					
					logger.debug("查找信用计划-找到计划:planId["+plan.getPlanId()+"]");
				}
			}
		}
		
		return findPlans;
	}
	
	/**
	 * @see 方法名：getPlanNbrByTxnCd 
	 * @see 描述：根据入账交易码和产品号，获取信用计划编号
	 * @see 创建日期：2015-6-24下午7:12:51
	 * @author ChengChun
	 *  
	 * @param txnCd
	 * @param productCode
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public String getPlanNbrByTxnCd(TxnCd txnCd, String productCode){
		ProductCredit productCredit = parameterFacility.loadParameter(productCode, ProductCredit.class);
		return productCredit.planNbrList.get(txnCd.planType);
	}
}
