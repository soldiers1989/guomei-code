package com.sunline.ccs.facility.contract;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.sunline.ccs.facility.BlockCodeUtils;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.CcsRepaySchedule;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ccs.param.def.enums.PrepaymentFeeMethod;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanStatus;
import com.sunline.ppy.dictionary.enums.LoanType;

@Service
public class AcctUnpostAmtCalUtil {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private BlockCodeUtils blockCodeUtils;
		
	private final static int MONTHS_DAYS = 30;
	
	/*
	 * 计算账户下必定会入账但还未入账的息费类金额 (不包括提现手续费)
	 */
	public BigDecimal AcctUnpostAmtCal(LoanFeeDef loanFeeDef,CcsAcct acct,CcsLoan loan,List<CcsPlan> planList,
			List<CcsRepaySchedule> scheduleList,Date bizDate){
		
		BigDecimal acctUnpostAmt= BigDecimal.ZERO;  //账户下必定会入账但还未入账的息费类金额 (不包括提现手续费)
		BigDecimal intAcru = BigDecimal.ZERO;//延时利息累计
		BigDecimal penaltyInt = BigDecimal.ZERO; //罚息 复利累计
		BigDecimal accruPrinSum = BigDecimal.ZERO; //本金积数和
		BigDecimal lifeInsuFee = BigDecimal.ZERO;  //未入账首先费
		BigDecimal svcFee = BigDecimal.ZERO; //服务费
		BigDecimal installFee = BigDecimal.ZERO; //手续费
		BigDecimal insuranceFee = BigDecimal.ZERO; //保费
		BigDecimal prepayFee = BigDecimal.ZERO; //提前还款计划报费
		BigDecimal replaceSvcFee = BigDecimal.ZERO; //代收服务费
		
		for(CcsPlan plan : planList){
			//计算非延迟利息 无论等额本息 随借随还都直接计算
			intAcru = intAcru.add(plan.getNodefbnpIntAcru());
			
			//计算罚息 复利 当前日期在宽限日期之前才算
			if(bizDate!=null && bizDate.compareTo(acct.getGraceDate()) > 0){
				penaltyInt = penaltyInt.add(plan.getPenaltyAcru()==null?BigDecimal.ZERO:plan.getPenaltyAcru())
						.add(plan.getCompoundAcru()==null?BigDecimal.ZERO:plan.getCompoundAcru());
			}
			
			//随借随还要计算本金积数
			if(loan.getLoanType()==LoanType.MCAT){
				accruPrinSum = accruPrinSum.add(plan.getAccruPrinSum()==null?BigDecimal.ZERO:plan.getAccruPrinSum());
			}
		}
		
		//根据本金积数 计算未入账服务费
		if(loan.getLoanType()==LoanType.MCAT){
			//计算未入账服务费
			if(!blockCodeUtils.getMergedSvcfeeInd(acct.getBlockCode()) && loanFeeDef != null 
					&& accruPrinSum.compareTo(BigDecimal.ZERO)>0){
				//贷款服务费
				if(loanFeeDef.installmentFeeCalMethod.equals(PrepaymentFeeMethod.R)){
					BigDecimal installmentFeeRate = loan.getInstallmentFeeRate();
					if(installmentFeeRate == null){
						installmentFeeRate = loanFeeDef.installmentFeeRate;
					}
					//服务费计算按比例计算
					BigDecimal dayInstallmentFeeRate = installmentFeeRate.multiply(new BigDecimal(1.0/loan.getLoanInitTerm()).setScale(20, RoundingMode.HALF_UP)).multiply(new BigDecimal(1.0/MONTHS_DAYS).setScale(20, RoundingMode.HALF_UP));
					svcFee = dayInstallmentFeeRate.multiply(accruPrinSum).setScale(2, RoundingMode.HALF_UP);
				}
				//分期手续费 -- 随借随还 暂时不支持分期手续费 -- by lizz 20160225
//				if(loanFeeDef.loanFeeCalcMethod.equals(CalcMethod.R)){
//					BigDecimal feeRate = loan.getFeeRate();
//					if(feeRate == null){
//						feeRate = loanFeeDef.feeRate;
//					}
//					//分期手续费计算按比例计算
//					BigDecimal dayFeeRate = feeRate.multiply(new BigDecimal(1.0/loan.getLoanInitTerm()).setScale(20, RoundingMode.HALF_UP)).multiply(new BigDecimal(1.0/MONTHS_DAYS).setScale(20, RoundingMode.HALF_UP));
//					installFee = dayFeeRate.multiply(accruPrinSum.setScale(2, RoundingMode.HALF_UP));
//				}
			}
			
			//计算未入账寿险费
			if(!blockCodeUtils.getMergedSvcfeeInd(acct.getBlockCode()) && loanFeeDef != null 
					&& accruPrinSum.compareTo(BigDecimal.ZERO)>0){
				
				if(null !=acct.getJoinLifeInsuInd() && Indicator.Y == acct.getJoinLifeInsuInd() && loanFeeDef.lifeInsuFeeCalMethod.equals(PrepaymentFeeMethod.R)){
					BigDecimal lifeRate = loan.getLifeInsuFeeRate();
					if(lifeRate == null){
						lifeRate = loanFeeDef.lifeInsuFeeRate;
					}
					//寿险费计算按比例计算
					BigDecimal lifeInsuRate = lifeRate.multiply(new BigDecimal(1.0/loan.getLoanInitTerm()).setScale(20, RoundingMode.HALF_UP)).multiply(new BigDecimal(1.0/MONTHS_DAYS).setScale(20, RoundingMode.HALF_UP));
					lifeInsuFee = lifeInsuRate.multiply(accruPrinSum).setScale(2, RoundingMode.HALF_UP);
				}			
			}
		}
	
		//对于等额本息贷款退货情况 计算未入账息费
		if(loan.getLoanType()== LoanType.MCEI && loan.getLoanStatus()== LoanStatus.T){
			for(CcsRepaySchedule schedule:scheduleList){
				if(loan.getCurrTerm().compareTo(schedule.getCurrTerm()) < 0){
					intAcru=intAcru.add(schedule.getLoanTermInt()==null?BigDecimal.ZERO:schedule.getLoanTermInt());
					svcFee=svcFee.add(schedule.getLoanTermFee()==null?BigDecimal.ZERO:schedule.getLoanTermFee());
					insuranceFee=insuranceFee.add(schedule.getLoanInsuranceAmt()==null?BigDecimal.ZERO:schedule.getLoanInsuranceAmt());
					lifeInsuFee=lifeInsuFee.add(schedule.getLoanLifeInsuAmt()==null?BigDecimal.ZERO:schedule.getLoanLifeInsuAmt());
					prepayFee=prepayFee.add(schedule.getLoanPrepayPkgAmt()==null?BigDecimal.ZERO:schedule.getLoanPrepayPkgAmt());
					replaceSvcFee=replaceSvcFee.add(schedule.getLoanReplaceSvcFee()==null?BigDecimal.ZERO:schedule.getLoanReplaceSvcFee());
				    installFee=installFee.add(schedule.getLoanSvcFee()==null?BigDecimal.ZERO:schedule.getLoanSvcFee());
				}
			}
		}
		
		intAcru=intAcru.setScale(2, RoundingMode.HALF_UP);
		penaltyInt=penaltyInt.setScale(2, RoundingMode.HALF_UP);
		lifeInsuFee=lifeInsuFee.setScale(2, RoundingMode.HALF_UP);
		svcFee=svcFee.setScale(2, RoundingMode.HALF_UP);
		installFee=installFee.setScale(2, RoundingMode.HALF_UP);
		insuranceFee=insuranceFee.setScale(2, RoundingMode.HALF_UP);
		prepayFee=prepayFee.setScale(2, RoundingMode.HALF_UP);
		replaceSvcFee=replaceSvcFee.setScale(2, RoundingMode.HALF_UP);
		acctUnpostAmt=acctUnpostAmt
			.add(intAcru)
			.add(penaltyInt)
			.add(lifeInsuFee)
			.add(svcFee)
			.add(installFee)
			.add(insuranceFee)
			.add(prepayFee)
			.add(replaceSvcFee);
		
		acctUnpostAmt=acctUnpostAmt.setScale(2, RoundingMode.HALF_UP);
		if(logger.isDebugEnabled())
			logger.debug("未入帐金额试算結果acctUnpostAmt[{}],未入账利息[{}],罚息[{}],寿险费[{}],服务费[{}],手续费[{}],保费[{}],提前还款计划包费[{}],代收服务费[{}]",
					acctUnpostAmt,intAcru,penaltyInt,lifeInsuFee,svcFee,installFee,insuranceFee,prepayFee,replaceSvcFee);
		
		return acctUnpostAmt;
	}
	

}
