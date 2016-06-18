package com.sunline.ccs.batch.cc6000.common;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.param.def.enums.BnpPeriod;
import com.sunline.ccs.param.def.enums.BucketObject;
import com.sunline.ppy.dictionary.enums.BucketType;


/**
 * @see 类名：BnpManager
 * @see 描述：余额成分管理类
 * 
 * @see 创建日期：   2015年6月25日 下午7:09:07
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Component
public class BnpManager {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private static final String BNP_TYPE_ERROR = "余额成份类型不正确";
	
	/**
	 * @see 方法名：getBucketAmount 
	 * @see 描述：获取信用计划中不同余额成份类型的实际金额
	 * @see 创建日期：2015-6-24下午7:00:29
	 * @author ChengChun
	 *  
	 * @param plan
	 * @param bucketType
	 * @param bnpPeriod
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public BigDecimal getBucketAmount(CcsPlan plan, BucketType bucketType, BnpPeriod bnpPeriod){
		switch (bucketType){
		case Pricinpal:
			return bnpPeriod == BnpPeriod.CTD ? plan.getCtdPrincipal() : plan.getPastPrincipal(); 
		case Interest:
			return bnpPeriod == BnpPeriod.CTD ? plan.getCtdInterest() : plan.getPastInterest();
		case CardFee:
			return bnpPeriod == BnpPeriod.CTD ? plan.getCtdCardFee() : plan.getPastCardFee();
		case InsuranceFee:
			return bnpPeriod == BnpPeriod.CTD ? plan.getCtdInsurance() : plan.getPastInsurance();
		case Mulct:
			return bnpPeriod == BnpPeriod.CTD ? plan.getCtdMulctAmt() : plan.getPastMulctAmt();
		case StampDuty:
			return bnpPeriod == BnpPeriod.CTD ? plan.getCtdStampdutyAmt() : plan.getPastStampdutyAmt();
		case LifeInsuFee:
			return bnpPeriod == BnpPeriod.CTD ? plan.getCtdLifeInsuAmt() : plan.getPastLifeInsuAmt();
		case LatePaymentCharge:
			return bnpPeriod == BnpPeriod.CTD ? plan.getCtdLateFee() : plan.getPastLateFee();
		case NSFCharge:
			return bnpPeriod == BnpPeriod.CTD ? plan.getCtdNsfundFee() : plan.getPastNsfundFee();
		case OverLimitFee:
			return bnpPeriod == BnpPeriod.CTD ? plan.getCtdOvrlmtFee() : plan.getPastOvrlmtFee();
		case SVCFee:
			return bnpPeriod == BnpPeriod.CTD ? plan.getCtdSvcFee() : plan.getPastSvcFee();
		case TXNFee:
			return bnpPeriod == BnpPeriod.CTD ? plan.getCtdTxnFee() : plan.getPastTxnFee();
		case UserFee1:
			return bnpPeriod == BnpPeriod.CTD ? plan.getCtdUserFee1() : plan.getPastUserFee1();
		case UserFee2:
			return bnpPeriod == BnpPeriod.CTD ? plan.getCtdUserFee2() : plan.getPastUserFee2();
		case UserFee3:
			return bnpPeriod == BnpPeriod.CTD ? plan.getCtdUserFee3() : plan.getPastUserFee3();
		case UserFee4:
			return bnpPeriod == BnpPeriod.CTD ? plan.getCtdUserFee4() : plan.getPastUserFee4();
		case UserFee5:
			return bnpPeriod == BnpPeriod.CTD ? plan.getCtdUserFee5() : plan.getPastUserFee5();
		case UserFee6:
			return bnpPeriod == BnpPeriod.CTD ? plan.getCtdUserFee6() : plan.getPastUserFee6();
		case Compound:
			return bnpPeriod == BnpPeriod.CTD ? plan.getCtdCompound() : plan.getPastCompound();
		case Penalty:
			return bnpPeriod == BnpPeriod.CTD ? plan.getCtdPenalty() : plan.getPastPenalty();
		case ReplaceSvcFee:
			return bnpPeriod == BnpPeriod.CTD ? plan.getCtdReplaceSvcFee() : plan.getPastReplaceSvcFee();
		case ReplacePenalty:
			return bnpPeriod == BnpPeriod.CTD ? plan.getCtdReplacePenalty() : plan.getPastReplacePenalty();
		case ReplaceMulct:
			return bnpPeriod == BnpPeriod.CTD ? plan.getCtdReplaceMulct() : plan.getPastReplaceMulct();
		case ReplaceLatePaymentCharge:
			return bnpPeriod == BnpPeriod.CTD ? plan.getCtdReplaceLateFee() : plan.getPastReplaceLateFee();
		case ReplaceTxnFee:
			return bnpPeriod == BnpPeriod.CTD ? plan.getCtdReplaceTxnFee() : plan.getPastReplaceTxnFee();
		case PrepayPkg:
			return bnpPeriod == BnpPeriod.CTD ? plan.getCtdPrepayPkgFee() : plan.getPastPrepayPkgFee();
		default: throw new IllegalArgumentException(BNP_TYPE_ERROR);
		}
	}

	/**
	 * @see 方法名：setBucketAmount 
	 * @see 描述：设置信用计划中不同余额成份类型的实际金额
	 * @see 创建日期：2015-6-24下午7:00:46
	 * @author ChengChun
	 *  
	 * @param plan
	 * @param bucketType
	 * @param bnpPeriod
	 * @param newAmount
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public void setBucketAmount(CcsPlan plan, BucketType bucketType, BnpPeriod bnpPeriod, BigDecimal newAmount)
	{
		switch (bucketType){
		case Pricinpal: 
			if (bnpPeriod == BnpPeriod.CTD){ plan.setCtdPrincipal(newAmount); } else {plan.setPastPrincipal(newAmount); }
			break; 
		case Interest:
			if (bnpPeriod == BnpPeriod.CTD){ plan.setCtdInterest(newAmount); } else {plan.setPastInterest(newAmount); } 
			break;
		case CardFee:
			if (bnpPeriod == BnpPeriod.CTD){ plan.setCtdCardFee(newAmount); } else {plan.setPastCardFee(newAmount); }
			break;
		case InsuranceFee:
			if (bnpPeriod == BnpPeriod.CTD){ plan.setCtdInsurance(newAmount); } else {plan.setPastInsurance(newAmount); }
			break;
		case Mulct:
			if (bnpPeriod == BnpPeriod.CTD){ plan.setCtdMulctAmt(newAmount); } else {plan.setPastMulctAmt(newAmount); }
			break;
		case StampDuty:
			if (bnpPeriod == BnpPeriod.CTD){ plan.setCtdStampdutyAmt(newAmount); } else {plan.setPastStampdutyAmt(newAmount); }
			break;
		case LifeInsuFee:
			if (bnpPeriod == BnpPeriod.CTD){ plan.setCtdLifeInsuAmt(newAmount); } else {plan.setPastLifeInsuAmt(newAmount); }
			break;
		case LatePaymentCharge:
			if (bnpPeriod == BnpPeriod.CTD){ plan.setCtdLateFee(newAmount); } else {plan.setPastLateFee(newAmount); }
			break;
		case NSFCharge:
			if (bnpPeriod == BnpPeriod.CTD){ plan.setCtdNsfundFee(newAmount); } else {plan.setPastNsfundFee(newAmount); }
			break;
		case OverLimitFee:
			if (bnpPeriod == BnpPeriod.CTD){ plan.setCtdOvrlmtFee(newAmount); } else {plan.setPastOvrlmtFee(newAmount); }
			break;
		case SVCFee:
			if (bnpPeriod == BnpPeriod.CTD){ plan.setCtdSvcFee(newAmount); } else {plan.setPastSvcFee(newAmount); }
			break;
		case TXNFee:
			if (bnpPeriod == BnpPeriod.CTD){ plan.setCtdTxnFee(newAmount); } else {plan.setPastTxnFee(newAmount); }
			break;
		case UserFee1:
			if (bnpPeriod == BnpPeriod.CTD){ plan.setCtdUserFee1(newAmount); } else {plan.setPastUserFee1(newAmount); }
			break;
		case UserFee2:
			if (bnpPeriod == BnpPeriod.CTD){ plan.setCtdUserFee2(newAmount); } else {plan.setPastUserFee2(newAmount); }
			break;
		case UserFee3:
			if (bnpPeriod == BnpPeriod.CTD){ plan.setCtdUserFee3(newAmount); } else {plan.setPastUserFee3(newAmount); }
			break;
		case UserFee4:
			if (bnpPeriod == BnpPeriod.CTD){ plan.setCtdUserFee4(newAmount); } else {plan.setPastUserFee4(newAmount); }
			break;
		case UserFee5:
			if (bnpPeriod == BnpPeriod.CTD){ plan.setCtdUserFee5(newAmount); } else {plan.setPastUserFee5(newAmount); }
			break;
		case UserFee6:
			if (bnpPeriod == BnpPeriod.CTD){ plan.setCtdUserFee6(newAmount); } else {plan.setPastUserFee6(newAmount); }
			break;
		case Compound:
			if (bnpPeriod == BnpPeriod.CTD){ plan.setCtdCompound(newAmount); } else {plan.setPastCompound(newAmount); }
			break;
		case Penalty:
			if (bnpPeriod == BnpPeriod.CTD){ plan.setCtdPenalty(newAmount); } else {plan.setPastPenalty(newAmount); }
			break;
		case ReplaceSvcFee:
			if (bnpPeriod == BnpPeriod.CTD){ plan.setCtdReplaceSvcFee(newAmount); } else {plan.setPastReplaceSvcFee(newAmount); }
			break;
		case ReplacePenalty:
			if (bnpPeriod == BnpPeriod.CTD){ plan.setCtdReplacePenalty(newAmount); } else {plan.setPastReplacePenalty(newAmount); }
			break;
		case ReplaceMulct:
			if (bnpPeriod == BnpPeriod.CTD){ plan.setCtdReplaceMulct(newAmount); } else {plan.setPastReplaceMulct(newAmount); }
			break;
		case ReplaceLatePaymentCharge:
			if (bnpPeriod == BnpPeriod.CTD){ plan.setCtdReplaceLateFee(newAmount); } else {plan.setPastReplaceLateFee(newAmount); }
			break;
		case ReplaceTxnFee:
			if (bnpPeriod == BnpPeriod.CTD){ plan.setCtdReplaceTxnFee(newAmount); } else {plan.setPastReplaceTxnFee(newAmount); }
			break;
		case PrepayPkg:
			if (bnpPeriod == BnpPeriod.CTD){ plan.setCtdPrepayPkgFee(newAmount); } else {plan.setPastPrepayPkgFee(newAmount); }
			break;
		default: throw new IllegalArgumentException(BNP_TYPE_ERROR);
		}

	}


	/**
	 * @see 方法名：getPastDueSum 
	 * @see 描述：汇总往期欠款
	 * @see 创建日期：2015-6-24下午7:00:59
	 * @author ChengChun
	 *  
	 * @param plan
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public BigDecimal getPastDueSum(CcsPlan plan){
		logger.info("调用往期欠款汇总方法");
		
		BigDecimal pastDue = plan.getPastCardFee()
				.add(plan.getPastInsurance())
				.add(plan.getPastInterest())
				.add(plan.getPastLateFee())
				.add(plan.getPastNsfundFee())
				.add(plan.getPastOvrlmtFee())
				.add(plan.getPastPrincipal())
				.add(plan.getPastSvcFee())
				.add(plan.getPastTxnFee())
				.add(plan.getPastUserFee1())
				.add(plan.getPastUserFee2())
				.add(plan.getPastUserFee3())
				.add(plan.getPastUserFee4())
				.add(plan.getPastUserFee5())
				.add(plan.getPastUserFee6())
				.add(plan.getPastPenalty())
				.add(plan.getPastCompound())
				.add(plan.getPastStampdutyAmt())
				.add(plan.getPastLifeInsuAmt())
				.add(plan.getPastMulctAmt())
				.add(plan.getPastReplaceTxnFee())
				.add(plan.getPastReplaceSvcFee())
				.add(plan.getPastReplacePenalty())
				.add(plan.getPastReplaceMulct())
				.add(plan.getPastReplaceLateFee())
				.add(plan.getPastPrepayPkgFee());
		return pastDue;
	}
	
	/**
	 * @see 方法名：getCtdDueSum 
	 * @see 描述：汇总当期期欠款
	 * @see 创建日期：2015-6-24下午7:01:13
	 * @author ChengChun
	 *  
	 * @param plan
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public BigDecimal getCtdDueSum(CcsPlan plan){
		logger.info("调用往期欠款汇总方法");
		
		BigDecimal ctdDue = plan.getCtdCardFee()
				.add(plan.getCtdInsurance())
				.add(plan.getCtdInterest())
				.add(plan.getCtdLateFee())
				.add(plan.getCtdNsfundFee())
				.add(plan.getCtdOvrlmtFee())
				.add(plan.getCtdPrincipal())
				.add(plan.getCtdSvcFee())
				.add(plan.getCtdTxnFee())
				.add(plan.getCtdUserFee1())
				.add(plan.getCtdUserFee2())
				.add(plan.getCtdUserFee3())
				.add(plan.getCtdUserFee4())
				.add(plan.getCtdUserFee5())
				.add(plan.getCtdUserFee6())
				.add(plan.getCtdPenalty())
				.add(plan.getCtdCompound())
				.add(plan.getCtdInsurance())
				.add(plan.getCtdStampdutyAmt())
				.add(plan.getCtdLifeInsuAmt())
				.add(plan.getCtdMulctAmt())
				.add(plan.getCtdReplaceTxnFee())
				.add(plan.getCtdReplaceSvcFee())
				.add(plan.getCtdReplacePenalty())
				.add(plan.getCtdReplaceMulct())
				.add(plan.getCtdReplaceLateFee())
				.add(plan.getCtdPrepayPkgFee());
		return ctdDue;
	}

	/**
	 * @see 方法名：getBnpAmt 
	 * @see 描述：获取信用计划中不同余额成份类型的实际金额
	 * @see 创建日期：2015-6-24下午7:01:26
	 * @author ChengChun
	 *  
	 * @param plan
	 * @param bucketObj
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public BigDecimal getBnpAmt(CcsPlan plan, BucketObject bucketObj) {
		switch (bucketObj) {
		case ctdPrincipal: return plan.getCtdPrincipal();
		case ctdInterest: return plan.getCtdInterest();
		case ctdCardFee: return plan.getCtdCardFee();
		case ctdOvrlmtFee: return plan.getCtdOvrlmtFee();
		case ctdLpc: return plan.getCtdLateFee();
		case ctdNsfFee: return plan.getCtdNsfundFee();
		case ctdSvcFee: return plan.getCtdSvcFee();
		case ctdTxnFee: return plan.getCtdTxnFee();
		case ctdIns: return plan.getCtdInsurance();
		case ctdMulct: return plan.getCtdMulctAmt();
		case ctdStampduty: return plan.getCtdStampdutyAmt();
		case ctdLifeInsuFee: return plan.getCtdLifeInsuAmt();
		case ctdPenalty:return plan.getCtdPenalty();
		case ctdCompound:return plan.getCtdCompound();
		case ctdUserFee1: return plan.getCtdUserFee1();
		case ctdUserFee2: return plan.getCtdUserFee2();
		case ctdUserFee3: return plan.getCtdUserFee3();
		case ctdUserFee4: return plan.getCtdUserFee4();
		case ctdUserFee5: return plan.getCtdUserFee5();
		case ctdUserFee6: return plan.getCtdUserFee6();
		case ctdReplaceSvcFee: return plan.getCtdReplaceSvcFee();
		case pastPrincipal: return plan.getPastPrincipal();
		case pastInterest: return plan.getPastInterest();
		case pastCardFee: return plan.getPastCardFee();
		case pastOvrlmtFee: return plan.getPastOvrlmtFee();
		case pastLpc: return plan.getPastLateFee();
		case pastNsfFee: return plan.getPastNsfundFee();
		case pastTxnFee: return plan.getPastTxnFee();
		case pastSvcFee: return plan.getPastSvcFee();
		case pastIns: return plan.getPastInsurance();
		case pastMulct: return plan.getPastMulctAmt();
		case pastStampduty: return plan.getPastStampdutyAmt();
		case pastLifeInsuFee: return plan.getPastLifeInsuAmt();
		case pastPenalty:return plan.getPastPenalty();
		case pastCompound:return plan.getPastCompound();
		case pastUserFee1: return plan.getPastUserFee1();
		case pastUserFee2: return plan.getPastUserFee2();
		case pastUserFee3: return plan.getPastUserFee3();
		case pastUserFee4: return plan.getPastUserFee4();
		case pastUserFee5: return plan.getPastUserFee5();
		case pastUserFee6: return plan.getPastUserFee6();
		case pastReplaceSvcFee: return plan.getPastReplaceSvcFee();
		case ctdReplacePenalty: return plan.getCtdReplacePenalty();
		case pastReplacePenalty: return plan.getPastReplacePenalty();
		case ctdReplaceMulct: return plan.getCtdReplaceMulct();
		case pastReplaceMulct: return plan.getPastReplaceMulct();
		case ctdReplaceLpc: return plan.getCtdReplaceLateFee();
		case pastReplaceLpc: return plan.getPastReplaceLateFee();
		case ctdReplaceTxnFee: return plan.getCtdReplaceTxnFee();
		case pastReplaceTxnFee: return plan.getPastReplaceTxnFee();
		case ctdPrepayPkg: return plan.getCtdPrepayPkgFee();
		case pastPrepayPkg: return plan.getPastPrepayPkgFee();
		default: throw new IllegalArgumentException(BNP_TYPE_ERROR);
		}
	}

	/**
	 * @see 方法名：generatePlanCurrBal 
	 * @see 描述：汇总计划的所有Bnp余额，更新plan的当前余额
	 * @see 创建日期：2015-6-24下午7:01:53
	 * @author ChengChun
	 *  
	 * @param plan
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private void generatePlanCurrBal(CcsPlan plan) {
		plan.setCurrBal(this.getCtdDueSum(plan).add(this.getPastDueSum(plan)));
	}
	
	/**
	 * @see 方法名：setBnpAmt 
	 * @see 描述：设置信用计划中不同余额成份类型的实际金额
	 * @see 创建日期：2015-6-24下午7:02:28
	 * @author ChengChun
	 *  
	 * @param plan
	 * @param bucketObj
	 * @param newAmt
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public void setBnpAmt(CcsPlan plan, BucketObject bucketObj, BigDecimal newAmt) {
		switch (bucketObj) {
		case ctdPrincipal: plan.setCtdPrincipal(newAmt); this.generatePlanCurrBal(plan); break;
		case ctdInterest: plan.setCtdInterest(newAmt); this.generatePlanCurrBal(plan); break;
		case ctdCardFee: plan.setCtdCardFee(newAmt); this.generatePlanCurrBal(plan); break;
		case ctdOvrlmtFee: plan.setCtdOvrlmtFee(newAmt); this.generatePlanCurrBal(plan); break;
		case ctdLpc: plan.setCtdLateFee(newAmt); this.generatePlanCurrBal(plan); break;
		case ctdNsfFee: plan.setCtdNsfundFee(newAmt); this.generatePlanCurrBal(plan); break;
		case ctdSvcFee: plan.setCtdSvcFee(newAmt); this.generatePlanCurrBal(plan); break;
		case ctdTxnFee: plan.setCtdTxnFee(newAmt); this.generatePlanCurrBal(plan); break;
		case ctdIns: plan.setCtdInsurance(newAmt); this.generatePlanCurrBal(plan); break;
		case ctdMulct: plan.setCtdMulctAmt(newAmt);this.generatePlanCurrBal(plan);break;
		case ctdStampduty: plan.setCtdStampdutyAmt(newAmt); this.generatePlanCurrBal(plan); break;
		case ctdLifeInsuFee: plan.setCtdLifeInsuAmt(newAmt); this.generatePlanCurrBal(plan); break;
		case ctdPenalty:plan.setCtdPenalty(newAmt);this.generatePlanCurrBal(plan);break;
		case ctdCompound:plan.setCtdCompound(newAmt);this.generatePlanCurrBal(plan);break;
		case ctdUserFee1: plan.setCtdUserFee1(newAmt); this.generatePlanCurrBal(plan); break; 
		case ctdUserFee2: plan.setCtdUserFee2(newAmt); this.generatePlanCurrBal(plan); break;
		case ctdUserFee3: plan.setCtdUserFee3(newAmt); this.generatePlanCurrBal(plan); break;
		case ctdUserFee4: plan.setCtdUserFee4(newAmt); this.generatePlanCurrBal(plan); break;
		case ctdUserFee5: plan.setCtdUserFee5(newAmt); this.generatePlanCurrBal(plan); break;
		case ctdUserFee6: plan.setCtdUserFee6(newAmt); this.generatePlanCurrBal(plan); break;
		case ctdReplaceSvcFee: plan.setCtdReplaceSvcFee(newAmt); this.generatePlanCurrBal(plan); break;
		case pastPrincipal: plan.setPastPrincipal(newAmt); this.generatePlanCurrBal(plan); break;
		case pastInterest: plan.setPastInterest(newAmt); this.generatePlanCurrBal(plan); break;
		case pastCardFee: plan.setPastCardFee(newAmt); this.generatePlanCurrBal(plan); break;
		case pastOvrlmtFee: plan.setPastOvrlmtFee(newAmt); this.generatePlanCurrBal(plan); break;
		case pastLpc: plan.setPastLateFee(newAmt); this.generatePlanCurrBal(plan); break;
		case pastNsfFee: plan.setPastNsfundFee(newAmt); this.generatePlanCurrBal(plan); break;
		case pastTxnFee: plan.setPastTxnFee(newAmt); this.generatePlanCurrBal(plan); break;
		case pastSvcFee: plan.setPastSvcFee(newAmt); this.generatePlanCurrBal(plan); break;
		case pastIns: plan.setPastInsurance(newAmt); this.generatePlanCurrBal(plan); break;
		case pastMulct: plan.setPastMulctAmt(newAmt);this.generatePlanCurrBal(plan);break;
		case pastStampduty: plan.setPastStampdutyAmt(newAmt); this.generatePlanCurrBal(plan); break;
		case pastLifeInsuFee: plan.setPastLifeInsuAmt(newAmt); this.generatePlanCurrBal(plan); break;
		case pastPenalty:plan.setPastPenalty(newAmt);this.generatePlanCurrBal(plan);break;
		case pastCompound:plan.setPastCompound(newAmt);this.generatePlanCurrBal(plan);break;
		case pastUserFee1: plan.setPastUserFee1(newAmt); this.generatePlanCurrBal(plan); break; 
		case pastUserFee2: plan.setPastUserFee2(newAmt); this.generatePlanCurrBal(plan); break;
		case pastUserFee3: plan.setPastUserFee3(newAmt); this.generatePlanCurrBal(plan); break;
		case pastUserFee4: plan.setPastUserFee4(newAmt); this.generatePlanCurrBal(plan); break;
		case pastUserFee5: plan.setPastUserFee5(newAmt); this.generatePlanCurrBal(plan); break;
		case pastUserFee6: plan.setPastUserFee6(newAmt); this.generatePlanCurrBal(plan); break;
		case pastReplaceSvcFee: plan.setPastReplaceSvcFee(newAmt); this.generatePlanCurrBal(plan); break;
		case ctdReplacePenalty: plan.setCtdReplacePenalty(newAmt);this.generatePlanCurrBal(plan);break;
		case pastReplacePenalty: plan.setPastReplacePenalty(newAmt);this.generatePlanCurrBal(plan);break;
		case ctdReplaceMulct: plan.setCtdReplaceMulct(newAmt);this.generatePlanCurrBal(plan);break;
		case pastReplaceMulct: plan.setPastReplaceMulct(newAmt);this.generatePlanCurrBal(plan);break;
		case ctdReplaceLpc: plan.setCtdReplaceLateFee(newAmt);this.generatePlanCurrBal(plan);break;
		case pastReplaceLpc: plan.setPastReplaceLateFee(newAmt);this.generatePlanCurrBal(plan);break;
		case ctdReplaceTxnFee: plan.setCtdReplaceTxnFee(newAmt); this.generatePlanCurrBal(plan); break;
		case pastReplaceTxnFee: plan.setPastReplaceTxnFee(newAmt);this.generatePlanCurrBal(plan);break;
		case ctdPrepayPkg: plan.setCtdPrepayPkgFee(newAmt); this.generatePlanCurrBal(plan); break;
		case pastPrepayPkg: plan.setPastPrepayPkgFee(newAmt);this.generatePlanCurrBal(plan);break;
		default: throw new IllegalArgumentException(BNP_TYPE_ERROR);
		}
	}
	
}
