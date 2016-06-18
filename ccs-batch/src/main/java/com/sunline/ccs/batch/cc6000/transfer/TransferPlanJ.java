package com.sunline.ccs.batch.cc6000.transfer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ccs.batch.cc6000.S6000AcctInfo;
import com.sunline.ccs.batch.cc6000.common.AgeController;
import com.sunline.ccs.batch.cc6000.common.Calculator;
import com.sunline.ccs.batch.cc6000.common.PlanManager;
import com.sunline.ccs.batch.cc6000.common.TransactionGenerator;
import com.sunline.ccs.facility.BlockCodeUtils;
import com.sunline.ccs.facility.MicroCreditRescheduleUtils;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.CcsPostingTmp;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.param.def.PlanTemplate;
import com.sunline.ccs.param.def.enums.CalcMethod;
import com.sunline.ccs.param.def.enums.LoanTerminateReason;
import com.sunline.ccs.param.def.enums.PaymentCalcMethod;
import com.sunline.ccs.param.def.enums.PrepaymentFeeMethod;
import com.sunline.ccs.param.def.enums.SysTxnCd;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.BucketType;
import com.sunline.ppy.dictionary.enums.DbCrInd;
import com.sunline.ppy.dictionary.enums.ExceptionType;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanStatus;
import com.sunline.ppy.dictionary.enums.PostGlIndicator;

/**
 * @see 类名：TransferPlanJ
 * @see 描述：随借随还的转入
			  普通账单日只需要将累计非延时利息转入到转入计划的非延时利息累计
			  到期后，全部转入
 *
 * @see 创建日期：   2015-6-24下午6:26:28
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Component
public class TransferPlanJ extends Transfer {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private UnifiedParameterFacility parameterFacility;
	@Autowired
	private TransactionGenerator generatorTransaction;
	@Autowired
	private PlanManager createPlan;
	@Autowired
	private Calculator calculator;
	@Autowired
	private BlockCodeUtils blockCodeUtils;
	@Autowired
	private BatchStatusFacility batchFacility;
	@Autowired
	private AgeController ageController;
	@Autowired
	private MicroCreditRescheduleUtils rescheduleUtils;
	private final static int MONTHS_DAYS = 30;
	
	
	@Override
	public CcsPlan getXfrInPlan(S6000AcctInfo item, PlanTemplate xfrOut,
			PlanTemplate xfrIn, CcsPlan xfrOutPlan, CcsLoan loan) {
		List<CcsPlan> findPlans = createPlan.findTxnPlans(xfrOutPlan.getLogicCardNbr(), xfrIn.planNbr, xfrIn.planType, xfrOutPlan.getRefNbr(), item.getPlans(),null);
		CcsPlan xfrInPlan = null;
		// 信用计划不存在
		if (findPlans.size() < 1) {
			// 建立信用计划
			xfrInPlan = createPlan.generateXfrInPlan(xfrOutPlan, loan, xfrIn, batchFacility.getBatchDate(), null);
			// 增加新建信用计划
			item.getPlans().add(xfrInPlan);
		}
		// 找到多个转入信用计划
		else if (findPlans.size() > 1) {
			// 增加异常账户报表记录
			calculator.makeExceptionAccount(item, xfrIn.planNbr, xfrOutPlan.getRefNbr(), ExceptionType.E10);
			return null;
		} else{ 
			xfrInPlan = findPlans.get(0);
		}
		return xfrInPlan;
	}

	@Override
	public void xfr(S6000AcctInfo item, CcsPlan xfrOutPlan, CcsPlan xfrInPlan,
			CcsLoan loan) throws Exception {
		// 根据分期类型代码，查找分期控制参数
		LoanPlan loanPlan = parameterFacility.loadParameter(loan.getLoanCode(), LoanPlan.class);
		CcsAcct account = item.getAccount();
		BigDecimal interest = BigDecimal.ZERO;
		BigDecimal bal = BigDecimal.ZERO;
		CcsAcct acct = item.getAccount();
		// 人工终止转移
		if (loan.getLoanStatus() == LoanStatus.T
				&& (loan.getTerminalReasonCd()==LoanTerminateReason.V || loan.getTerminalReasonCd()==LoanTerminateReason.M)){
			loan.setTerminalDate(batchFacility.getBatchDate());
			loan.setRemainTerm(1);
			loan.setLoanExpireDate(batchFacility.getBatchDate());
			interest = interest.add(xfrOutPlan.getNodefbnpIntAcru());
			bal = bal.add(xfrOutPlan.getCurrBal());
			acct.setBlockCode(blockCodeUtils.addBlockCode(acct.getBlockCode(), I_CODE));
		}
		// 逾期终止转移
		else if (!account.getAgeCode().equals("C")
				&& Integer.valueOf(account.getAgeCode()) >= Integer.valueOf(loanPlan.terminateAgeCd)) {
			loan.setLastLoanStatus(loan.getLoanStatus());
			loan.setLoanStatus(LoanStatus.T);
			loan.setRemainTerm(1);
			loan.setLoanExpireDate(batchFacility.getBatchDate());
			loan.setTerminalReasonCd(LoanTerminateReason.D);
			loan.setTerminalDate(batchFacility.getBatchDate());
			interest = interest.add(xfrOutPlan.getNodefbnpIntAcru());
			bal = bal.add(xfrOutPlan.getCurrBal());
			acct.setBlockCode(blockCodeUtils.addBlockCode(acct.getBlockCode(), I_CODE));
		}
		// 锁定码终止转移：B|要求全部余额进行还款
		else if (PaymentCalcMethod.B == blockCodeUtils.getMergedPaymentInd(account.getBlockCode())) {
			loan.setLastLoanStatus(loan.getLoanStatus());
			loan.setLoanStatus(LoanStatus.T);
			loan.setRemainTerm(1);
			loan.setLoanExpireDate(batchFacility.getBatchDate());
			loan.setTerminalReasonCd(LoanTerminateReason.R);
			loan.setTerminalDate(batchFacility.getBatchDate());
			interest = interest.add(xfrOutPlan.getNodefbnpIntAcru());
			bal = bal.add(xfrOutPlan.getCurrBal());
			acct.setBlockCode(blockCodeUtils.addBlockCode(acct.getBlockCode(), I_CODE));
		}
		// 正常转移
		else {
			// 如果剩余期数<=1，则为期末，转移信用计划全部余额
			if (loan.getRemainTerm() <= 1) {
				loan.setLastLoanStatus(loan.getLoanStatus());
				loan.setLoanStatus(LoanStatus.F);
				interest = interest.add(xfrOutPlan.getNodefbnpIntAcru());
				bal = bal.add(xfrOutPlan.getCurrBal());
			} else if (loan.getLoanStatus() == LoanStatus.A && loan.getLoanExpireDate().compareTo(batchFacility.getBatchDate()) <=0 && loan.getRemainTerm() > 1 ){
				loan.setLastLoanStatus(loan.getLoanStatus());
				loan.setLoanStatus(LoanStatus.F);
				loan.setRemainTerm(1);
				interest = interest.add(xfrOutPlan.getNodefbnpIntAcru());
				bal = bal.add(xfrOutPlan.getCurrBal());
			} else {
				// 当前计划余额 > 分期每期应还本金, 正常转移1期金额
				interest = interest.add(xfrOutPlan.getNodefbnpIntAcru());
				//从转出计划中转出本金作为最小还款的本金
				bal=ageController.getCommonMinDue(xfrOutPlan); 
			}
		}
		generateLoanAndPlan(item, xfrOutPlan, xfrInPlan, loan, bal.setScale(2, RoundingMode.HALF_UP),SysTxnCd.S51,SysTxnCd.S52);
		//累计利息入账到转入计划，转出计划利息清零
		//是否锁定码免除服务费
		if(!blockCodeUtils.getMergedIntWaiveInd(item.getAccount().getBlockCode())){
			logger.debug("不免除利息");
			generatorTransaction.generateInterestTransaction(item, xfrInPlan, interest.setScale(2, RoundingMode.HALF_UP), batchFacility.getBatchDate());
		}else{
			logger.debug("免除利息");
		}
		xfrOutPlan.setNodefbnpIntAcru(BigDecimal.ZERO);
		
		LoanFeeDef loanFeeDef = rescheduleUtils.getLoanFeeDef(loan.getLoanCode(), loan.getLoanInitTerm(), 
				loan.getLoanInitPrin(),loan.getLoanFeeDefId());
		
		BigDecimal initPrin = xfrOutPlan.getAccruPrinSum();
//		if(item.getAccount().getGraceDaysFullInd() == Indicator.N){
			initPrin = initPrin.add(xfrInPlan.getAccruPrinSum());
//		}
		//是否锁定码免除服务费
		if(!blockCodeUtils.getMergedSvcfeeInd(item.getAccount().getBlockCode()) && loanFeeDef != null 
				&& initPrin.compareTo(BigDecimal.ZERO)>0){
			//服务费
			BigDecimal svcFee = BigDecimal.ZERO;
			if(loanFeeDef.loanFeeCalcMethod.equals(CalcMethod.R)){
				BigDecimal fRate = loan.getFeeRate();
				if(fRate == null){
					fRate = loanFeeDef.feeRate;
				}
				//服务费计算按比例计算
				BigDecimal feeRate = fRate.multiply(new BigDecimal(1.0/MONTHS_DAYS).setScale(20, RoundingMode.HALF_UP));
				svcFee = feeRate.multiply(initPrin.setScale(2, RoundingMode.HALF_UP));
			}else{
				//服务费计算按固定金额
				BigDecimal feeAmt = loan.getFeeAmt();
				if(feeAmt == null){
					feeAmt = loanFeeDef.feeAmount;
				}
				svcFee = feeAmt;
			}
			//服务费入账
			if(svcFee.compareTo(BigDecimal.ZERO) > 0){
				//生成一笔服务费入账到转入计划
				generatorTransaction.generateTxnTransaction(item, xfrInPlan, svcFee, batchFacility.getBatchDate(),BucketType.SVCFee);
				// 更新分期信息表(TM_LOAN)的总手续费？？？loan上没有总的服务费
				loan.setLoanInitFee(loan.getLoanInitFee().add(svcFee));
			}
		}
		
		//是否锁定码免除寿险费
		if(!blockCodeUtils.getMergedOtherfeeInd(item.getAccount().getBlockCode()) && loanFeeDef != null 
				&& initPrin.compareTo(BigDecimal.ZERO)>0 && loan.getJoinLifeInsuInd() == Indicator.Y ){
			//寿险费
			BigDecimal lifeInsuFee = BigDecimal.ZERO;
			if(loanFeeDef.lifeInsuFeeCalMethod.equals(PrepaymentFeeMethod.R)){
				BigDecimal lifeRate = loan.getLifeInsuFeeRate();
				if(lifeRate == null){
					lifeRate = loanFeeDef.lifeInsuFeeRate;
				}
				//寿险费计算按比例计算
				BigDecimal lifeInsuRate = lifeRate.multiply(new BigDecimal(1.0/MONTHS_DAYS).setScale(20, RoundingMode.HALF_UP));
				lifeInsuFee = lifeInsuRate.multiply(initPrin.setScale(2, RoundingMode.HALF_UP));
			}else {
				//寿险费计算按固定金额】
				BigDecimal lifeAmt = loan.getLifeInsuFeeAmt();
				if(lifeAmt == null){
					lifeAmt = loanFeeDef.lifeInsuFeeAmt;
				}
				lifeInsuFee = lifeAmt;
			}
			//寿险费入账
			if(lifeInsuFee.compareTo(BigDecimal.ZERO) > 0){
				//生成一笔寿险费入账到转入计划
				generatorTransaction.generateTxnTransaction(item, xfrInPlan, lifeInsuFee, batchFacility.getBatchDate(),BucketType.LifeInsuFee);
				// 更新分期信息表(TM_LOAN)的总寿险费
				loan.setTotLifeInsuAmt(loan.getTotLifeInsuAmt().add(lifeInsuFee));	
			}
		}
		
		//是否锁定码免除代收服务费
		if(!blockCodeUtils.getMergedOtherfeeInd(item.getAccount().getBlockCode()) && loanFeeDef != null 
				&& initPrin.compareTo(BigDecimal.ZERO)>0){
			// 代收服务费
			BigDecimal replaceSvcFee = BigDecimal.ZERO;
			if(CalcMethod.R.equals(loanFeeDef.replaceFeeCalMethod)){
				BigDecimal replaceSvcFeeRate = loan.getReplaceSvcFeeRate();
				if(replaceSvcFeeRate == null){
					replaceSvcFeeRate = loanFeeDef.replaceFeeRate;
				}
				replaceSvcFeeRate = replaceSvcFeeRate.multiply(new BigDecimal(1.0/MONTHS_DAYS).setScale(20, RoundingMode.HALF_UP));
				replaceSvcFee = replaceSvcFeeRate.multiply(initPrin.setScale(2, RoundingMode.HALF_UP));
			}else {
				BigDecimal replaceSvcFeeAmt = loan.getReplaceSvcFeeAmt();
				if(replaceSvcFeeAmt == null){
					replaceSvcFeeAmt = loanFeeDef.replaceFeeAmt;
				}
				replaceSvcFee = replaceSvcFeeAmt;
			}
			// 代收服务费入账
			if(replaceSvcFee.compareTo(BigDecimal.ZERO) > 0){
				//生成一笔代收服务费入账到转入计划
				generatorTransaction.generateTxnTransaction(item, xfrInPlan, replaceSvcFee, batchFacility.getBatchDate(),BucketType.ReplaceSvcFee);
				// 更新分期信息表(TM_LOAN)的总代收服务费
				loan.setTotReplaceSvcFee(loan.getTotReplaceSvcFee().add(replaceSvcFee));	
			}
		}
		
		// 是否锁定码免除灵活还款计划包
		if (!blockCodeUtils.getMergedOtherfeeInd(item.getAccount().getBlockCode())
				&& loanFeeDef != null
				&& initPrin.compareTo(BigDecimal.ZERO) > 0
				&& loan.getPrepayPkgInd() == Indicator.Y) {
			// 灵活还款计划包
			BigDecimal prepayPkg = BigDecimal.ZERO;
			if (CalcMethod.R.equals(loanFeeDef.prepayPkgFeeCalMethod)) {
				BigDecimal prepayPkgRate = loan.getPrepayPkgFeeRate();
				if (prepayPkgRate == null) {
					prepayPkgRate = loanFeeDef.prepayPkgFeeAmountRate;
				}
				prepayPkgRate = prepayPkgRate
						.multiply(new BigDecimal(1.0 / MONTHS_DAYS).setScale(20,RoundingMode.HALF_UP));
				prepayPkg = prepayPkgRate.multiply(initPrin.setScale(2,RoundingMode.HALF_UP));
			} else {
				BigDecimal prepayPkgAmt = loan.getPrepayPkgFeeAmt();
				if (prepayPkgAmt == null) {
					prepayPkgAmt = loanFeeDef.prepayPkgFeeAmount;
				}
				prepayPkg = prepayPkgAmt;
			}
			// 灵活还款计划包入账
			if (prepayPkg.compareTo(BigDecimal.ZERO) > 0) {
				// 生成一笔灵活还款计划包入账到转入计划
				generatorTransaction.generateTxnTransaction(item, xfrInPlan,prepayPkg, batchFacility.getBatchDate(),BucketType.PrepayPkg);
				// 更新分期信息表(TM_LOAN)的总灵活还款计划包
				loan.setTotPrepayPkgAmt(loan.getTotPrepayPkgAmt().add(prepayPkg));
			}
		}
	}

	/**
	 * @see 方法名：generateLoanAndPlan 
	 * @see 描述：更新loan、plan，生成入账交易
	 * @see 创建日期：2015-6-24下午6:28:29
	 * @author ChengChun
	 *  
	 * @param item
	 * @param xfrOutPlan
	 * @param xfrInPlan
	 * @param loan
	 * @param xfrAmt
	 * @param from
	 * @param to
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private void generateLoanAndPlan(S6000AcctInfo item, CcsPlan xfrOutPlan,CcsPlan xfrInPlan, CcsLoan loan, BigDecimal xfrAmt,SysTxnCd from,SysTxnCd to) {
		if (loan.getRemainTerm() >= 1) {
        	loan.setCurrTerm(loan.getCurrTerm() + 1);
        	loan.setRemainTerm(loan.getRemainTerm() - 1);
        }
		if(xfrAmt.equals(BigDecimal.ZERO)){//0金额不做xfr
			return;
		}
		logger.debug("信用计划转移,planId={}",xfrOutPlan.getPlanId());
		logger.debug("信用计划转移转入计划,xfrInPlanid={"+xfrInPlan.getPlanId()+"},xfrInPlanNbr={"+xfrInPlan.getPlanNbr()+"}");
		List<CcsPostingTmp> l = generatorTransaction.generateLoanTransferTransaction(item, xfrOutPlan, xfrInPlan, loan, xfrAmt, batchFacility.getBatchDate(),from,to);
		String txnSeq = UUID.randomUUID().toString();
		for(CcsPostingTmp t : l){
			//将分期转出和转入送入glp
			item.getGlTxnItemList().add(calculator.makeGlTxn(
					item.getAccount().getOrg(), item.getAccount().getDefaultLogicCardNbr(), item.getAccount().getAcctNbr(), item.getAccount().getAcctType(), 
					txnSeq, item.getAccount().getCurrency(), item.getAccount().getAgeCodeGl(), t.getTxnCode(), t.getTxnDesc(), DbCrInd.M, 
					batchFacility.getBatchDate(), t.getTxnAmt(), PostGlIndicator.N, item.getAccount().getOwningBranch(), 
					t.getAcqBranchIq(), t.getPlanNbr(), BucketType.Pricinpal));
		}
		
		// 信用计划余额
		xfrOutPlan.setCurrBal(xfrOutPlan.getCurrBal().subtract(xfrAmt));
		xfrInPlan.setCurrBal(xfrInPlan.getCurrBal().add(xfrAmt));
		// 信用计划本金
		BigDecimal outPrincipal = xfrOutPlan.getCtdPrincipal().subtract(xfrAmt);
		if (outPrincipal.compareTo(BigDecimal.ZERO) >= 0) {
			xfrOutPlan.setCtdPrincipal(outPrincipal);
		} else {
			xfrOutPlan.setCtdPrincipal(BigDecimal.ZERO);
			xfrOutPlan.setPastPrincipal(xfrOutPlan.getPastPrincipal().subtract(outPrincipal.abs()));
		}
		xfrInPlan.setCtdPrincipal(xfrInPlan.getCtdPrincipal().add(xfrAmt));
		
		// 更新分期信息表(TM_LOAN)的未出账单分期本金
		loan.setUnstmtPrin(loan.getUnstmtPrin().subtract(xfrAmt));		
		//更新分期信息表(TM_LOAN)的分期已出账单本金
		loan.setLoanPrinXfrin(loan.getLoanPrinXfrin().add(xfrAmt));
		//更新分期信息表(TM_LOAN)的分期未到期本金
		loan.setLoanPrinXfrout(loan.getLoanPrinXfrout().subtract(xfrAmt));
	}
	
	

}
