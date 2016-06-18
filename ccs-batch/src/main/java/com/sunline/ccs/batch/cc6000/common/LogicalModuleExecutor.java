package com.sunline.ccs.batch.cc6000.common;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ccs.batch.cc6000.S6000AcctInfo;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.CcsPostingTmp;
import com.sunline.ccs.param.def.enums.LogicMod;
import com.sunline.ppy.dictionary.enums.BucketType;
import com.sunline.ppy.dictionary.enums.PlanType;
import com.sunline.ppy.dictionary.enums.PostingFlag;

/**
 * @see 类名：ExecuteLogicalModule
 * @see 描述： * 交易入账逻辑模块
              01：消费
              02：退货
              03：本金借记调整
              04：本金贷记调整
              05：利息借记调整
              06：利息贷记调整
              07：服务费借记调整
              08：服务费贷记调整
              09：滞纳金借记调整
              10：滞纳金贷记调整
              11：年费借记调整
              12：年费贷记调整
              13：超限费借记调整
              14：超限费贷记调整
              15：交易费借记调整
              16：交易费贷记调整
              23：本金强制借记调整
              24：本金强制贷记调整
              26: 账单分期本金贷记调整
              30：还款
              31：还款撤销
              51：自定义费1借记
              52：自定义费1贷记
              61：积分增加
              62：积分减少
              63：积分兑换
              80：争议解决（有利于公司，收费）
              96：争议提出
              97：争议解决（有利于持卡人）
              98：争议解决（有利于公司，不收费）
 *
 * @see 创建日期：   2015-6-24下午8:45:14
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Component
public class LogicalModuleExecutor {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private PaymentHier paymentHier;
	@Autowired
    private BatchStatusFacility batchFacility;

	
	
	
	/**
	 * @see 方法名：executeLogicalModule 
	 * @see 描述：调用交易入账逻辑
	 * @see 创建日期：2015-6-24下午8:48:37
	 * @author ChengChun
	 *  
	 * @param logicMod
	 * @param item
	 * @param txnPost
	 * @param plan
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public BucketType executeLogicalModule(LogicMod logicMod, S6000AcctInfo item, CcsPostingTmp txnPost, CcsPlan plan) {
		logger.info("交易编码编码：" + txnPost.getTxnCode());

		switch (logicMod) {
		case L01: return this.L01_Sales(item.getAccount(), txnPost, plan);	// 01：消费
		case L02: return this.L02_Return(item.getAccount(), txnPost, plan);	// 02：退货
		case A03: return this.L03_PrincipalDebit(item.getAccount(), txnPost, plan);	// 03：本金借记调整
		case A04: return this.L04_PrincipalCredit(item.getAccount(), txnPost, plan);	// 04：本金贷记调整
		case L05: return this.L05_InterestDebit(item.getAccount(), txnPost, plan,false);	// 05：利息入账
		case L27: return L27_penaltyDebit(item.getAccount(),txnPost,plan,false);//罚息入账
		case L29: return L29_compoundDebit(item.getAccount(),txnPost,plan,false);//复利入账
		case L06: return this.L06_InterestCredit(item.getAccount(), txnPost, plan,false);	// 06：利息退还
		case A72: return this.L72_penaltyCredit(item.getAccount(), txnPost, plan, true);	// 06：利息退还
		case A92: return this.L92_compoundCredit(item.getAccount(), txnPost, plan, true);// 06：利息退还
		case L07: return this.L07_SvcFeeDebit(item.getAccount(), txnPost, plan,false);	// 07：服务费入账
		case L08: return this.L08_SvcFeeCredit(item.getAccount(), txnPost, plan,false);	// 08：服务费退还
		case L09: return this.L09_LpcDebit(item.getAccount(), txnPost, plan,false);	// 09：滞纳金入账
		case L10: return this.L10_LpcCredit(item.getAccount(), txnPost, plan,false);	// 10：滞纳金退还
		case L11: return this.L11_CardFeeDebit(item.getAccount(), txnPost, plan,false);	// 11：年费入账
		case L12: return this.L12_CardFeeCredit(item.getAccount(), txnPost, plan,false);	// 12：年费退还
		case L13: return this.L13_OvrlmtFeeDebit(item.getAccount(), txnPost, plan,false);	// 13：超限费入账
		case L14: return this.L14_OvrlmtFeeCredit(item.getAccount(), txnPost, plan,false);	// 14：超限费退还
		case L15: return this.L15_TxnFeeDebit(item.getAccount(), txnPost, plan,false);	// 15：交易费入账
		case L16: return this.L16_TxnFeeCredit(item.getAccount(), txnPost, plan,false);	// 16：交易费退还
		//马上start
		case L40: return this.L40_MulctDebit(item.getAccount(), txnPost, plan,false);	// 40：罚金入账
		case L41: return this.L41_MulctCredit(item.getAccount(), txnPost, plan,false);	// 41：罚金回溯
		case L42: return this.L42_InsuranceFeeDebit(item.getAccount(), txnPost, plan,false);	// 42：保险费入账
		case L43: return this.L43_InsuranceFeeCredit(item.getAccount(), txnPost, plan,false);	// 43：保险费贷调
		case L44: return this.L44_StampDutyDebit(item.getAccount(), txnPost, plan,false);	// 44：印花税入账
		case L45: return this.L45_LifeInsuFeeDebit(item.getAccount(), txnPost, plan,false);	// 45：寿险计划包费入账
		case L46: return this.L46_LifeInsuFeeCredit(item.getAccount(), txnPost, plan,false);	// 46：寿险计划包费贷调
		case L47: return this.L47_ReplaceSvcFeeDebit(item.getAccount(), txnPost, plan,false);	// 45：代收服务费入账
		case L48: return this.L48_ReplaceSvcFeeCredit(item.getAccount(), txnPost, plan,false);	// 46：代收服务费贷调
		//马上end
		case A05: return this.L05_InterestDebit(item.getAccount(), txnPost, plan,true);	// 05：利息借记调整
		case A06: return this.L06_InterestCredit(item.getAccount(), txnPost, plan,true);	// 06：利息贷记调整
		case A07: return this.L07_SvcFeeDebit(item.getAccount(), txnPost, plan,true);	// 07：服务费借记调整
		case A08: return this.L08_SvcFeeCredit(item.getAccount(), txnPost, plan,true);	// 08：服务费贷记调整
		case A09: return this.L09_LpcDebit(item.getAccount(), txnPost, plan,true);	// 09：滞纳金借记调整
		case A10: return this.L10_LpcCredit(item.getAccount(), txnPost, plan,true);	// 10：滞纳金贷记调整
		case A11: return this.L11_CardFeeDebit(item.getAccount(), txnPost, plan,true);	// 11：年费借记调整
		case A12: return this.L12_CardFeeCredit(item.getAccount(), txnPost, plan,true);	// 12：年费贷记调整
		case A13: return this.L13_OvrlmtFeeDebit(item.getAccount(), txnPost, plan,true);	// 13：超限费借记调整
		case A14: return this.L14_OvrlmtFeeCredit(item.getAccount(), txnPost, plan,true);	// 14：超限费贷记调整
		case A15: return this.L15_TxnFeeDebit(item.getAccount(), txnPost, plan,true);	// 15：交易费借记调整
		case A16: return this.L16_TxnFeeCredit(item.getAccount(), txnPost, plan,true);	// 16：交易费贷记调整
		case A23: return this.L23_PrincipalForceDebit(item.getAccount(), txnPost, plan);	// 23：本金强制借记调整
		case A24: return this.L24_PrincipalForceCredit(item.getAccount(), txnPost, plan);	// 24：本金强制贷记调整
		case A26: return this.L26(item.getAccount(), txnPost, plan); // 26: 账单分期本金贷记调整
		case L30: return this.L30_Payment(item, txnPost, plan);	// 30：还款
		case L31: return this.L31_PaymentReversal(item.getAccount(), txnPost, plan);	// 31：还款撤销
		case L32: return this.L32_AssignPayment(item, txnPost, plan);	// 32:指定借据号还款
		case L51: return this.L51_UserFee1Debit(item.getAccount(), txnPost, plan,false);	// 51：自定义费1借记
		case L52: return this.L52_UserFee1Credit(item.getAccount(), txnPost, plan,false);	// 52：自定义费1贷记
		case A51: return this.L51_UserFee1Debit(item.getAccount(), txnPost, plan,true);	// 51：自定义费1借记调整
		case A52: return this.L52_UserFee1Credit(item.getAccount(), txnPost, plan,true);	// 52：自定义费1贷记调整
		case L61: return this.L61_PointsEarned(item.getAccount(), txnPost);	// 61：积分增加
		case L62: return this.L62_PointsAdjust(item.getAccount(), txnPost);	// 62：积分减少
		case L63: return this.L63_PointsDisburse(item.getAccount(), txnPost);	// 63：积分兑换
		case L80: return this.L80_DisputeReleaseBankInterest(item.getAccount(), txnPost, plan);	// 80：争议解决（有利于公司，收费）
		case L96: return this.L96_DisputeAdd(item.getAccount(), txnPost, plan);	// 96：争议提出
		case L97: return this.L97_DisputeReleaseCustomer(item.getAccount(), txnPost, plan);	// 97：争议解决（有利于持卡人）
		case L98: return this.L98_DisputeReleaseBankNoInterest(item.getAccount(), txnPost, plan);	// 98：争议解决（有利于公司，不收费）
		
		case L49: return this.L49_Premium(item.getAccount(), txnPost, plan);//49: 趸交费入账备忘交易
		case L53: return this.L53_R_PenaltyDebit(item.getAccount(), txnPost, plan, true);//代收罚息入账
		case L54: return this.L54_R_PenaltyCredit(item.getAccount(), txnPost, plan, false);//代收罚息回溯
		case L55: return this.L55_R_MulctDebit(item.getAccount(), txnPost, plan, false);//代收罚金入账
		case L56: return this.L56_R_MulctCredit(item.getAccount(), txnPost, plan, false);//代收罚金回溯
		case L57: return this.L57_R_LpcDebit(item.getAccount(), txnPost, plan, false);//代收滞纳金入账
		case L58: return this.L58_R_LpcCredit(item.getAccount(), txnPost, plan, false);//代收滞纳金贷调
		case L59: return this.L59_R_TxnFeeDebit(item.getAccount(), txnPost, plan, false);//代收手续费入账
		case L60: return this.L60_R_TxnFeeCredit(item.getAccount(), txnPost, plan, false);//代收手续费贷调
		
		case L64: return this.L64_PrepayPkgAmtDebit(item.getAccount(),txnPost,plan);//灵活还款计划包入账
		case L65: return this.L65_PrepayPkgAmtCedit(item.getAccount(),txnPost,plan);//灵活还款计划包回溯
		case L67: return this.L67_CouponsInterestCedit(item.getAccount(),txnPost,plan);//优惠券抵扣利息交易
		default: return this.defaultModule(txnPost);
		}
	}

	/**
	 * @see 方法名：L01_Sales 
	 * @see 描述：01：消费
	 * @see 创建日期：2015-6-24下午8:48:51
	 * @author ChengChun
	 *  
	 * @param account
	 * @param txnPost
	 * @param plan
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public BucketType L01_Sales(CcsAcct account, CcsPostingTmp txnPost, CcsPlan plan) {
		
		// 计划-当前余额
		plan.setCurrBal(plan.getCurrBal().add(txnPost.getPostAmt()));
		// 计划-当期本金
		plan.setCtdPrincipal(plan.getCtdPrincipal().add(txnPost.getPostAmt()));
		// 计划-当期借记金额
		plan.setCtdAmtDb(plan.getCtdAmtDb().add(txnPost.getPostAmt()));
		// 计划-当期借记笔数
		plan.setCtdNbrDb(plan.getCtdNbrDb()+1);
		
		// 账户-账户余额
		account.setCurrBal(account.getCurrBal().add(txnPost.getPostAmt()));
		// 账户-本金余额
		account.setPrincipalBal(account.getPrincipalBal().add(txnPost.getPostAmt()));		
		// 账户-消费交易
		if (PlanType.R.equals(plan.getPlanType()) || PlanType.O.equals(plan.getPlanType())) {
			// 账户-首次消费处理
			if (account.getFirstRetlAmt().compareTo(BigDecimal.ZERO) == 0 
					|| account.getFirstRetlDate() == null) {
				// 首次消费日期-批量日期
				account.setFirstRetlDate(txnPost.getPostDate());
				// 首次消费金额
				account.setFirstRetlAmt(txnPost.getPostAmt());
			}
			// 账户-当期消费金额
			account.setCtdRetailAmt(account.getCtdRetailAmt().add(txnPost.getPostAmt()));
			// 账户-当期消费笔数
			account.setCtdRetailCnt(account.getCtdRetailCnt() + 1);
			// 账户-本月消费金额
			account.setMtdRetailAmt(account.getMtdRetailAmt().add(txnPost.getPostAmt()));
			// 账户-本月消费笔数
			account.setMtdRetailCnt(account.getMtdRetailCnt() + 1);
			// 账户-本年消费金额
			account.setYtdRetailAmt(account.getYtdRetailAmt().add(txnPost.getPostAmt()));
			// 账户-本年消费笔数
			account.setYtdRetailCnt(account.getYtdRetailCnt() + 1);
			// 账户-历史消费金额
			account.setLtdRetailAmt(account.getLtdRetailAmt().add(txnPost.getPostAmt()));
			// 账户-历史消费笔数
			account.setLtdRetailCnt(account.getLtdRetailCnt() + 1);
		}
		// 账户-取现交易
		if (PlanType.C.equals(plan.getPlanType())) {
			// 账户-首次消费处理
			if (account.getFirstRetlAmt().compareTo(BigDecimal.ZERO) == 0 
					|| account.getFirstRetlDate() == null) {
				// 首次消费日期-批量日期
				account.setFirstRetlDate(txnPost.getPostDate());
				// 首次消费金额
				account.setFirstRetlAmt(txnPost.getPostAmt());
			}
			// 账户-取现余额
			account.setCashBal(account.getCashBal().add(txnPost.getPostAmt()));
			// 账户-当期取现金额
			account.setCtdCashAmt(account.getCtdCashAmt().add(txnPost.getPostAmt()));
			// 账户-当期取现笔数
			account.setCtdCashCnt(account.getCtdCashCnt() + 1);
			// 账户-本月取现金额
			account.setMtdCashAmt(account.getMtdCashAmt().add(txnPost.getPostAmt()));
			// 账户-本月取现笔数
			account.setMtdCashCnt(account.getMtdCashCnt() + 1);
			// 账户-本年取现金额
			account.setYtdCashAmt(account.getYtdCashAmt().add(txnPost.getPostAmt()));
			// 账户-本年取现笔数
			account.setYtdCashCnt(account.getYtdCashCnt() + 1);
			// 账户-历史取现金额
			account.setLtdCashAmt(account.getLtdCashAmt().add(txnPost.getPostAmt()));
			// 账户-历史取现笔数
			account.setLtdCashCnt(account.getLtdCashCnt() + 1);
		}
		
		//马上贷增加，放款成功后，将放款金额累计到账户的“累计放款金额”字段
		if(plan.getPlanType() == PlanType.J || plan.getPlanType() == PlanType.P || plan.getPlanType() == PlanType.P){
			account.setLtdLoanAmt(account.getLtdLoanAmt() == null?txnPost.getPostAmt():account.getLtdLoanAmt().add(txnPost.getPostAmt()));
		}
		
		return BucketType.Pricinpal;
	}

	/**
	 * @see 方法名：L02_Return 
	 * @see 描述：02：退货
	 * @see 创建日期：2015-6-24下午8:49:08
	 * @author ChengChun
	 *  
	 * @param account
	 * @param txnPost
	 * @param plan
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public BucketType L02_Return(CcsAcct account, CcsPostingTmp txnPost, CcsPlan plan) {

		// 计划-当前余额
		plan.setCurrBal(plan.getCurrBal().subtract(txnPost.getPostAmt()));
		// 计划-当期本金
		BigDecimal remainingAmt = txnPost.getPostAmt().subtract(plan.getCtdPrincipal());
		if (remainingAmt.compareTo(BigDecimal.ZERO) >= 0) {
			plan.setCtdPrincipal(BigDecimal.ZERO);
			plan.setPastPrincipal(plan.getPastPrincipal().subtract(remainingAmt));
		} else {
			plan.setCtdPrincipal(plan.getCtdPrincipal().subtract(txnPost.getPostAmt()));
		}
		// 计划-当期贷记金额
		plan.setCtdAmtCr(plan.getCtdAmtCr().add(txnPost.getPostAmt()));
		// 计划-当期贷记笔数
		plan.setCtdNbrCr(plan.getCtdNbrCr()+1);
		
		// 账户-账户余额	
		account.setCurrBal(account.getCurrBal().subtract(txnPost.getPostAmt()));
		// 账户-本金余额
		account.setPrincipalBal(account.getPrincipalBal().subtract(txnPost.getPostAmt()));
		// 账户-当期退货金额
		account.setCtdRefundAmt(account.getCtdRefundAmt().add(txnPost.getPostAmt()));
		// 账户-当期退货笔数
		account.setCtdRefundCnt(account.getCtdRefundCnt() + 1);
		// 账户-本月退货金额
		account.setMtdRefundAmt(account.getMtdRefundAmt().add(txnPost.getPostAmt()));
		// 账户-本月退货笔数
		account.setMtdRefundCnt(account.getMtdRefundCnt() + 1);
		// 账户-本年退货金额
		account.setYtdRefundAmt(account.getYtdRefundAmt().add(txnPost.getPostAmt()));
		// 账户-本年退货笔数
		account.setYtdRefundCnt(account.getYtdRefundCnt() + 1);
		// 账户-历史退货金额
		account.setLtdRefundAmt(account.getLtdRefundAmt().add(txnPost.getPostAmt()));
		// 账户-历史退货笔数
		account.setLtdRefundCnt(account.getLtdRefundCnt() + 1);
		
		return BucketType.Pricinpal;
	}

	/**
	 * @see 方法名：L03_PrincipalDebit 
	 * @see 描述：03：本金借记调整
	 * @see 创建日期：2015-6-24下午8:49:24
	 * @author ChengChun
	 *  
	 * @param account
	 * @param txnPost
	 * @param plan
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public BucketType L03_PrincipalDebit(CcsAcct account, CcsPostingTmp txnPost, CcsPlan plan) {

		// 计划-当前余额
		plan.setCurrBal(plan.getCurrBal().add(txnPost.getPostAmt()));
		// 计划-当期本金
		plan.setCtdPrincipal(plan.getCtdPrincipal().add(txnPost.getPostAmt()));
		// 计划-当期借记金额
		plan.setCtdAmtDb(plan.getCtdAmtDb().add(txnPost.getPostAmt()));
		// 计划-当期借记笔数
		plan.setCtdNbrDb(plan.getCtdNbrDb()+1);
		
		// 账户-账户余额
		account.setCurrBal(account.getCurrBal().add(txnPost.getPostAmt()));
		// 账户-本金余额
		account.setPrincipalBal(account.getPrincipalBal().add(txnPost.getPostAmt()));
		// 账户-取现交易
		if (PlanType.C.equals(plan.getPlanType())) {
			// 账户-当期取现余额
			account.setCashBal(account.getCashBal().add(txnPost.getPostAmt()));
		}
		// 账户-当期借记调整金额
		account.setCtdDbAdjAmt(account.getCtdDbAdjAmt().add(txnPost.getPostAmt()));
		// 账户-当期借记调整笔数
		account.setCtdDbAdjCnt(account.getCtdDbAdjCnt() + 1);
		
		return BucketType.Pricinpal;
	}

	/**
	 * @see 方法名：L04_PrincipalCredit 
	 * @see 描述：04：本金贷记调整
	 * @see 创建日期：2015-6-24下午8:49:36
	 * @author ChengChun
	 *  
	 * @param account
	 * @param txnPost
	 * @param plan
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public BucketType L04_PrincipalCredit(CcsAcct account, CcsPostingTmp txnPost, CcsPlan plan) {
		
		// 计划-当前余额
		plan.setCurrBal(plan.getCurrBal().subtract(txnPost.getPostAmt()));
		// 计划-当期本金
		BigDecimal remainingAmt = txnPost.getPostAmt().subtract(plan.getCtdPrincipal());
		if (remainingAmt.compareTo(BigDecimal.ZERO) >= 0) {
			plan.setCtdPrincipal(BigDecimal.ZERO);
			plan.setPastPrincipal(plan.getPastPrincipal().subtract(remainingAmt));
		} else {
			plan.setCtdPrincipal(plan.getCtdPrincipal().subtract(txnPost.getPostAmt()));
		}
		// 计划-当期贷记金额
		plan.setCtdAmtCr(plan.getCtdAmtCr().add(txnPost.getPostAmt()));
		// 计划-当期贷记笔数
		plan.setCtdNbrCr(plan.getCtdNbrCr()+1);
		
		// 账户-账户余额
		account.setCurrBal(account.getCurrBal().subtract(txnPost.getPostAmt()));
		// 账户-本金余额
		account.setPrincipalBal(account.getPrincipalBal().subtract(txnPost.getPostAmt()));
		// 账户-取现交易
		if (PlanType.C.equals(plan.getPlanType())) {
			// 账户-当期取现余额
			account.setCashBal(account.getCashBal().subtract(txnPost.getPostAmt()));
		}
		// 账户-当期贷记调整金额
		account.setCtdCrAdjAmt(account.getCtdCrAdjAmt().add(txnPost.getPostAmt()));
		// 账户-当期贷记调整笔数
		account.setCtdCrAdjCnt(account.getCtdCrAdjCnt() + 1);
		
		return BucketType.Pricinpal;
	}

	/**
	 * @see 方法名：L05_InterestDebit 
	 * @see 描述：05：利息借记调整
	 * @see 创建日期：2015-6-24下午8:50:09
	 * @author ChengChun
	 *  
	 * @param account
	 * @param txnPost
	 * @param plan
	 * @param isAdjust
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public BucketType L05_InterestDebit(CcsAcct account, CcsPostingTmp txnPost, CcsPlan plan, boolean isAdjust) {
		
		// 计划-当前余额
		plan.setCurrBal(plan.getCurrBal().add(txnPost.getPostAmt()));
		// 计划-当期利息
		plan.setCtdInterest(plan.getCtdInterest().add(txnPost.getPostAmt()));
		// 计划-当期借记金额
		plan.setCtdAmtDb(plan.getCtdAmtDb().add(txnPost.getPostAmt()));
		// 计划-当期借记笔数
		plan.setCtdNbrDb(plan.getCtdNbrDb()+1);
		
		// 账户-账户余额
		account.setCurrBal(account.getCurrBal().add(txnPost.getPostAmt()));
		
		if(isAdjust){
			// 账户-当期借记调整金额
			account.setCtdDbAdjAmt(account.getCtdDbAdjAmt().add(txnPost.getPostAmt()));
			// 账户-当期借记调整笔数
			account.setCtdDbAdjCnt(account.getCtdDbAdjCnt() + 1);
		}else{
			// 账户-当期利息
			account.setCtdInterestAmt(account.getCtdInterestAmt().add(txnPost.getPostAmt()));
			// 账户-当期利息笔数
			account.setCtdInterestCnt(account.getCtdInterestCnt() + 1);
		}
		
		return BucketType.Interest;
	}
	/**
	 * @see 方法名：L27_penaltyDebit 
	 * @see 描述：罚息入账
	 * @see 创建日期：2015-6-24下午8:50:25
	 * @author ChengChun
	 *  
	 * @param account
	 * @param txnPost
	 * @param plan
	 * @param isAdjust
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public BucketType L27_penaltyDebit(CcsAcct account, CcsPostingTmp txnPost, CcsPlan plan, boolean isAdjust) {
		
		// 计划-当前余额
		plan.setCurrBal(plan.getCurrBal().add(txnPost.getPostAmt()));
		// 计划-当期利息
		plan.setCtdPenalty(plan.getCtdPenalty().add(txnPost.getPostAmt()));
		// 计划-当期借记金额
		plan.setCtdAmtDb(plan.getCtdAmtDb().add(txnPost.getPostAmt()));
		// 计划-当期借记笔数
		plan.setCtdNbrDb(plan.getCtdNbrDb()+1);
		
		// 账户-账户余额
		account.setCurrBal(account.getCurrBal().add(txnPost.getPostAmt()));
		
		if(isAdjust){
			// 账户-当期借记调整金额
			account.setCtdDbAdjAmt(account.getCtdDbAdjAmt().add(txnPost.getPostAmt()));
			// 账户-当期借记调整笔数
			account.setCtdDbAdjCnt(account.getCtdDbAdjCnt() + 1);
		}else{
			// 账户-当期利息
			account.setCtdInterestAmt(account.getCtdInterestAmt().add(txnPost.getPostAmt()));
			// 账户-当期利息笔数
			account.setCtdInterestCnt(account.getCtdInterestCnt() + 1);
		}
		
		return BucketType.Interest;
	}
	/**
	 * @see 方法名：L29_compoundDebit 
	 * @see 描述：TODO 方法描述
	 * @see 创建日期：2015-6-24下午8:50:44
	 * @author ChengChun
	 *  
	 * @param account
	 * @param txnPost
	 * @param plan
	 * @param isAdjust
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public BucketType L29_compoundDebit(CcsAcct account, CcsPostingTmp txnPost, CcsPlan plan, boolean isAdjust) {
		
		// 计划-当前余额
		plan.setCurrBal(plan.getCurrBal().add(txnPost.getPostAmt()));
		// 计划-当期利息
		plan.setCtdCompound(plan.getCtdCompound().add(txnPost.getPostAmt()));
		// 计划-当期借记金额
		plan.setCtdAmtDb(plan.getCtdAmtDb().add(txnPost.getPostAmt()));
		// 计划-当期借记笔数
		plan.setCtdNbrDb(plan.getCtdNbrDb()+1);
		
		// 账户-账户余额
		account.setCurrBal(account.getCurrBal().add(txnPost.getPostAmt()));
		
		if(isAdjust){
			// 账户-当期借记调整金额
			account.setCtdDbAdjAmt(account.getCtdDbAdjAmt().add(txnPost.getPostAmt()));
			// 账户-当期借记调整笔数
			account.setCtdDbAdjCnt(account.getCtdDbAdjCnt() + 1);
		}else{
			// 账户-当期利息
			account.setCtdInterestAmt(account.getCtdInterestAmt().add(txnPost.getPostAmt()));
			// 账户-当期利息笔数
			account.setCtdInterestCnt(account.getCtdInterestCnt() + 1);
		}
		
		return BucketType.Interest;
	}
	/**
	 * @see 方法名：L06_InterestCredit 
	 * @see 描述：06：利息贷记调整
	 * @see 创建日期：2015-6-24下午8:51:18
	 * @author ChengChun
	 *  
	 * @param account
	 * @param txnPost
	 * @param plan
	 * @param isAdjust
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public BucketType L06_InterestCredit(CcsAcct account, CcsPostingTmp txnPost, CcsPlan plan, boolean isAdjust) {
		
		// 计划-当前余额
		plan.setCurrBal(plan.getCurrBal().subtract(txnPost.getPostAmt()));
		// 计划-当期利息
		BigDecimal remainingAmt = txnPost.getPostAmt().subtract(plan.getPastInterest());
		if (remainingAmt.compareTo(BigDecimal.ZERO) >= 0) {
			plan.setPastInterest(BigDecimal.ZERO);
			plan.setCtdInterest(plan.getCtdInterest().subtract(remainingAmt));
		} else {
			plan.setPastInterest(plan.getPastInterest().subtract(txnPost.getPostAmt()));
		}
		// 计划-当期贷记金额
		plan.setCtdAmtCr(plan.getCtdAmtCr().add(txnPost.getPostAmt()));
		// 计划-当期贷记笔数
		plan.setCtdNbrCr(plan.getCtdNbrCr()+1);
		
		// 账户-账户余额
		account.setCurrBal(account.getCurrBal().subtract(txnPost.getPostAmt()));
		
		if(isAdjust){
			// 账户-当期贷记调整金额
			account.setCtdCrAdjAmt(account.getCtdCrAdjAmt().add(txnPost.getPostAmt()));
			// 账户-当期贷记调整笔数
			account.setCtdCrAdjCnt(account.getCtdCrAdjCnt() + 1);
		}else{
			// 账户-当期利息
			account.setCtdInterestAmt(account.getCtdInterestAmt().subtract(txnPost.getPostAmt()));
			// 账户-当期利息笔数
			account.setCtdInterestCnt(account.getCtdInterestCnt() + 1);
		}
		
		return BucketType.Interest;
	}
	/**
	 * @see 方法名：L72_penaltyCredit 
	 * @see 描述：罚息贷记调整
	 * @see 创建日期：2015-6-24下午8:51:31
	 * @author ChengChun
	 *  
	 * @param account
	 * @param txnPost
	 * @param plan
	 * @param isAdjust
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public BucketType L72_penaltyCredit(CcsAcct account, CcsPostingTmp txnPost, CcsPlan plan, boolean isAdjust) {
		
		// 计划-当前余额
		plan.setCurrBal(plan.getCurrBal().subtract(txnPost.getPostAmt()));
		// 计划-当期利息
		BigDecimal remainingAmt = txnPost.getPostAmt().subtract(plan.getPastPenalty());
		if (remainingAmt.compareTo(BigDecimal.ZERO) >= 0) {
			plan.setPastPenalty(BigDecimal.ZERO);
			plan.setCtdPenalty(plan.getCtdPenalty().subtract(remainingAmt));
		} else {
			plan.setPastPenalty(plan.getPastPenalty().subtract(txnPost.getPostAmt()));
		}
		// 计划-当期贷记金额
		plan.setCtdAmtCr(plan.getCtdAmtCr().add(txnPost.getPostAmt()));
		// 计划-当期贷记笔数
		plan.setCtdNbrCr(plan.getCtdNbrCr()+1);
		
		// 账户-账户余额
		account.setCurrBal(account.getCurrBal().subtract(txnPost.getPostAmt()));
		
		if(isAdjust){
			// 账户-当期贷记调整金额
			account.setCtdCrAdjAmt(account.getCtdCrAdjAmt().add(txnPost.getPostAmt()));
			// 账户-当期贷记调整笔数
			account.setCtdCrAdjCnt(account.getCtdCrAdjCnt() + 1);
		}else{
			// 账户-当期利息
			account.setCtdInterestAmt(account.getCtdInterestAmt().subtract(txnPost.getPostAmt()));
			// 账户-当期利息笔数
			account.setCtdInterestCnt(account.getCtdInterestCnt() + 1);
		}
		
		return BucketType.Interest;
	}
	/**
	 * @see 方法名：L92_compoundCredit 
	 * @see 描述：复利贷记调整
	 * @see 创建日期：2015-6-24下午8:51:48
	 * @author ChengChun
	 *  
	 * @param account
	 * @param txnPost
	 * @param plan
	 * @param isAdjust
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public BucketType L92_compoundCredit(CcsAcct account, CcsPostingTmp txnPost, CcsPlan plan, boolean isAdjust) {
		
		// 计划-当前余额
		plan.setCurrBal(plan.getCurrBal().subtract(txnPost.getPostAmt()));
		// 计划-当期利息
		BigDecimal remainingAmt = txnPost.getPostAmt().subtract(plan.getPastCompound());
		if (remainingAmt.compareTo(BigDecimal.ZERO) >= 0) {
			plan.setPastCompound(BigDecimal.ZERO);
			plan.setCtdCompound(plan.getCtdCompound().subtract(remainingAmt));
		} else {
			plan.setPastCompound(plan.getPastCompound().subtract(txnPost.getPostAmt()));
		}
		// 计划-当期贷记金额
		plan.setCtdAmtCr(plan.getCtdAmtCr().add(txnPost.getPostAmt()));
		// 计划-当期贷记笔数
		plan.setCtdNbrCr(plan.getCtdNbrCr()+1);
		
		// 账户-账户余额
		account.setCurrBal(account.getCurrBal().subtract(txnPost.getPostAmt()));
		
		if(isAdjust){
			// 账户-当期贷记调整金额
			account.setCtdCrAdjAmt(account.getCtdCrAdjAmt().add(txnPost.getPostAmt()));
			// 账户-当期贷记调整笔数
			account.setCtdCrAdjCnt(account.getCtdCrAdjCnt() + 1);
		}else{
			// 账户-当期利息
			account.setCtdInterestAmt(account.getCtdInterestAmt().subtract(txnPost.getPostAmt()));
			// 账户-当期利息笔数
			account.setCtdInterestCnt(account.getCtdInterestCnt() + 1);
		}
		
		return BucketType.Interest;
	}
	/**
	 * @see 方法名：L07_SvcFeeDebit 
	 * @see 描述：服务费借记调整
	 * @see 创建日期：2015-6-24下午8:52:18
	 * @author ChengChun
	 *  
	 * @param account
	 * @param txnPost
	 * @param plan
	 * @param isAdjust
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public BucketType L07_SvcFeeDebit(CcsAcct account, CcsPostingTmp txnPost, CcsPlan plan, boolean isAdjust) {
		
		// 计划-当前余额
		plan.setCurrBal(plan.getCurrBal().add(txnPost.getPostAmt()));
		// 计划-当期服务费
		plan.setCtdSvcFee(plan.getCtdSvcFee().add(txnPost.getPostAmt()));
		// 计划-当期借记金额
		plan.setCtdAmtDb(plan.getCtdAmtDb().add(txnPost.getPostAmt()));
		// 计划-当期借记笔数
		plan.setCtdNbrDb(plan.getCtdNbrDb()+1);
		
		// 账户-账户余额
		account.setCurrBal(account.getCurrBal().add(txnPost.getPostAmt()));
		
		if(isAdjust){
			// 账户-当期借记调整金额
			account.setCtdDbAdjAmt(account.getCtdDbAdjAmt().add(txnPost.getPostAmt()));
			// 账户-当期借记调整笔数
			account.setCtdDbAdjCnt(account.getCtdDbAdjCnt() + 1);
		}else{
			// 账户-当期费用金额
			account.setCtdFeeAmt(account.getCtdFeeAmt().add(txnPost.getPostAmt()));
			// 账户-当期费用笔数
			account.setCtdFeeCnt(account.getCtdFeeCnt() + 1);
		}
		
		return BucketType.SVCFee;
	}

	/**
	 * @see 方法名：L08_SvcFeeCredit 
	 * @see 描述：08：服务费贷记调整
	 * @see 创建日期：2015-6-24下午8:52:41
	 * @author ChengChun
	 *  
	 * @param account
	 * @param txnPost
	 * @param plan
	 * @param isAdjust
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public BucketType L08_SvcFeeCredit(CcsAcct account, CcsPostingTmp txnPost, CcsPlan plan, boolean isAdjust) {
		
		// 计划-当前余额
		plan.setCurrBal(plan.getCurrBal().subtract(txnPost.getPostAmt()));
		// 计划-当期服务费
		BigDecimal remainingAmt = txnPost.getPostAmt().subtract(plan.getPastSvcFee());
		if (remainingAmt.compareTo(BigDecimal.ZERO) >= 0) {
			plan.setPastSvcFee(BigDecimal.ZERO);
			plan.setCtdSvcFee(plan.getCtdSvcFee().subtract(remainingAmt));
		} else {
			plan.setPastSvcFee(plan.getPastSvcFee().subtract(txnPost.getPostAmt()));
		}
		// 计划-当期贷记金额
		plan.setCtdAmtCr(plan.getCtdAmtCr().add(txnPost.getPostAmt()));
		// 计划-当期贷记笔数
		plan.setCtdNbrCr(plan.getCtdNbrCr() + 1);
		
		// 账户-账户余额
		account.setCurrBal(account.getCurrBal().subtract(txnPost.getPostAmt()));
		
		if(isAdjust){
			// 账户-当期贷记调整金额
			account.setCtdCrAdjAmt(account.getCtdCrAdjAmt().add(txnPost.getPostAmt()));
			// 账户-当期贷记调整笔数
			account.setCtdCrAdjCnt(account.getCtdCrAdjCnt() + 1);
		}else{
			// 账户-当期费用金额
			account.setCtdFeeAmt(account.getCtdFeeAmt().subtract(txnPost.getPostAmt()));
			// 账户-当期费用笔数
			account.setCtdFeeCnt(account.getCtdFeeCnt() + 1);
		}
		
		return BucketType.SVCFee;
	}

	/**
	 * @see 方法名：L09_LpcDebit 
	 * @see 描述：09：滞纳金借记调整
	 * @see 创建日期：2015-6-24下午8:52:54
	 * @author ChengChun
	 *  
	 * @param account
	 * @param txnPost
	 * @param plan
	 * @param isAdjust
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public BucketType L09_LpcDebit(CcsAcct account, CcsPostingTmp txnPost, CcsPlan plan, boolean isAdjust) {
		
		// 计划-当前余额
		plan.setCurrBal(plan.getCurrBal().add(txnPost.getPostAmt()));
		// 计划-当期滞纳金
		plan.setCtdLateFee(plan.getCtdLateFee().add(txnPost.getPostAmt()));
		// 计划-当期借记金额
		plan.setCtdAmtDb(plan.getCtdAmtDb().add(txnPost.getPostAmt()));
		// 计划-当期借记笔数
		plan.setCtdNbrDb(plan.getCtdNbrDb()+1);
		
		// 账户-账户余额
		account.setCurrBal(account.getCurrBal().add(txnPost.getPostAmt()));
		
		if(isAdjust){
			// 账户-当期借记调整金额
			account.setCtdDbAdjAmt(account.getCtdDbAdjAmt().add(txnPost.getPostAmt()));
			// 账户-当期借记调整笔数
			account.setCtdDbAdjCnt(account.getCtdDbAdjCnt() + 1);
		}else{
			// 账户-当期费用金额
			account.setCtdFeeAmt(account.getCtdFeeAmt().add(txnPost.getPostAmt()));
			// 账户-当期费用笔数
			account.setCtdFeeCnt(account.getCtdFeeCnt() + 1);
		}
		
		return BucketType.LatePaymentCharge;
	}

	/**
	 * @see 方法名：L10_LpcCredit 
	 * @see 描述：10：滞纳金贷记调整
	 * @see 创建日期：2015-6-24下午8:53:08
	 * @author ChengChun
	 *  
	 * @param account
	 * @param txnPost
	 * @param plan
	 * @param isAdjust
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public BucketType L10_LpcCredit(CcsAcct account, CcsPostingTmp txnPost, CcsPlan plan, boolean isAdjust) {
		
		// 计划-当前余额
		plan.setCurrBal(plan.getCurrBal().subtract(txnPost.getPostAmt()));
		// 计划-当期滞纳金
		BigDecimal remainingAmt = txnPost.getPostAmt().subtract(plan.getPastLateFee());
		if (remainingAmt.compareTo(BigDecimal.ZERO) >= 0) {
			plan.setPastLateFee(BigDecimal.ZERO);
			plan.setCtdLateFee(plan.getCtdLateFee().subtract(remainingAmt));
		} else {
			plan.setPastLateFee(plan.getPastLateFee().subtract(txnPost.getPostAmt()));
		}
		// 计划-当期贷记金额
		plan.setCtdAmtCr(plan.getCtdAmtCr().add(txnPost.getPostAmt()));
		// 计划-当期贷记笔数
		plan.setCtdNbrCr(plan.getCtdNbrCr()+1);
		
		// 账户-账户余额
		account.setCurrBal(account.getCurrBal().subtract(txnPost.getPostAmt()));
		
		if(isAdjust){
			// 账户-当期贷记调整金额
			account.setCtdCrAdjAmt(account.getCtdCrAdjAmt().add(txnPost.getPostAmt()));
			// 账户-当期贷记调整笔数
			account.setCtdCrAdjCnt(account.getCtdCrAdjCnt() + 1);
		}else{
			// 账户-当期费用金额
			account.setCtdFeeAmt(account.getCtdFeeAmt().subtract(txnPost.getPostAmt()));
			// 账户-当期费用笔数
			account.setCtdFeeCnt(account.getCtdFeeCnt() + 1);
		}
		
		return BucketType.LatePaymentCharge;
	}

	/**
	 * @see 方法名：L11_CardFeeDebit 
	 * @see 描述：11：年费借记调整
	 * @see 创建日期：2015-6-24下午8:53:21
	 * @author ChengChun
	 *  
	 * @param account
	 * @param txnPost
	 * @param plan
	 * @param isAdjust
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public BucketType L11_CardFeeDebit(CcsAcct account, CcsPostingTmp txnPost, CcsPlan plan, boolean isAdjust) {
		
		// 计划-当前余额
		plan.setCurrBal(plan.getCurrBal().add(txnPost.getPostAmt()));
		// 计划-当期年费
		plan.setCtdCardFee(plan.getCtdCardFee().add(txnPost.getPostAmt()));
		// 计划-当期借记金额
		plan.setCtdAmtDb(plan.getCtdAmtDb().add(txnPost.getPostAmt()));
		// 计划-当期借记笔数
		plan.setCtdNbrDb(plan.getCtdNbrDb() + 1);
		
		// 账户-账户余额
		account.setCurrBal(account.getCurrBal().add(txnPost.getPostAmt()));
		
		if(isAdjust){
			// 账户-当期借记调整金额
			account.setCtdDbAdjAmt(account.getCtdDbAdjAmt().add(txnPost.getPostAmt()));
			// 账户-当期借记调整笔数
			account.setCtdDbAdjCnt(account.getCtdDbAdjCnt() + 1);
		}else{
			// 账户-当期费用金额
			account.setCtdFeeAmt(account.getCtdFeeAmt().add(txnPost.getPostAmt()));
			// 账户-当期费用笔数
			account.setCtdFeeCnt(account.getCtdFeeCnt() + 1);
		}
		return BucketType.CardFee;
	}

	/**
	 * 12：年费贷记调整
	 * @param isAdjust 
	 */
	public BucketType L12_CardFeeCredit(CcsAcct account, CcsPostingTmp txnPost, CcsPlan plan, boolean isAdjust) {
		
		// 计划-当前余额
		plan.setCurrBal(plan.getCurrBal().subtract(txnPost.getPostAmt()));
		// 计划-当期年费
		BigDecimal remainingAmt = txnPost.getPostAmt().subtract(plan.getPastCardFee());
		if (remainingAmt.compareTo(BigDecimal.ZERO) >= 0) {
			plan.setPastCardFee(BigDecimal.ZERO);
			plan.setCtdCardFee(plan.getCtdCardFee().subtract(remainingAmt));
		} else {
			plan.setPastCardFee(plan.getPastCardFee().subtract(txnPost.getPostAmt()));
		}
		// 计划-当期贷记金额
		plan.setCtdAmtCr(plan.getCtdAmtCr().add(txnPost.getPostAmt()));
		// 计划-当期贷记笔数
		plan.setCtdNbrCr(plan.getCtdNbrCr()+1);
		
		// 账户-账户余额
		account.setCurrBal(account.getCurrBal().subtract(txnPost.getPostAmt()));
		
		if(isAdjust){
			// 账户-当期贷记调整金额
			account.setCtdCrAdjAmt(account.getCtdCrAdjAmt().add(txnPost.getPostAmt()));
			// 账户-当期贷记调整笔数
			account.setCtdCrAdjCnt(account.getCtdCrAdjCnt() + 1);
		}else{
			// 账户-当期费用金额
			account.setCtdFeeAmt(account.getCtdFeeAmt().subtract(txnPost.getPostAmt()));
			// 账户-当期费用笔数
			account.setCtdFeeCnt(account.getCtdFeeCnt() + 1);
		}
		return BucketType.CardFee;
	}

	/**
	 * 13：超限费借记调整
	 * @param isAdjust 
	 */
	public BucketType L13_OvrlmtFeeDebit(CcsAcct account, CcsPostingTmp txnPost, CcsPlan plan, boolean isAdjust) {
		
		// 计划-当前余额
		plan.setCurrBal(plan.getCurrBal().add(txnPost.getPostAmt()));
		// 计划-当期超限费
		plan.setCtdOvrlmtFee(plan.getCtdOvrlmtFee().add(txnPost.getPostAmt()));
		// 计划-当期借记金额
		plan.setCtdAmtDb(plan.getCtdAmtDb().add(txnPost.getPostAmt()));
		// 计划-当期借记笔数
		plan.setCtdNbrDb(plan.getCtdNbrDb()+1);
		
		// 账户-账户余额
		account.setCurrBal(account.getCurrBal().add(txnPost.getPostAmt()));
		
		if(isAdjust){
			// 账户-当期借记调整金额
			account.setCtdDbAdjAmt(account.getCtdDbAdjAmt().add(txnPost.getPostAmt()));
			// 账户-当期借记调整笔数
			account.setCtdDbAdjCnt(account.getCtdDbAdjCnt() + 1);
		}else{
			// 账户-当期费用金额
			account.setCtdFeeAmt(account.getCtdFeeAmt().add(txnPost.getPostAmt()));
			// 账户-当期费用笔数
			account.setCtdFeeCnt(account.getCtdFeeCnt() + 1);
		}
		return BucketType.OverLimitFee;
	}

	/**
	 * 14：超限费贷记调整
	 * @param isAdjust 
	 */
	public BucketType L14_OvrlmtFeeCredit(CcsAcct account, CcsPostingTmp txnPost, CcsPlan plan, boolean isAdjust) {

		// 计划-当前余额
		plan.setCurrBal(plan.getCurrBal().subtract(txnPost.getPostAmt()));
		// 计划-当期超限费
		BigDecimal remainingAmt = txnPost.getPostAmt().subtract(plan.getPastOvrlmtFee());
		if (remainingAmt.compareTo(BigDecimal.ZERO) >= 0) {
			plan.setPastOvrlmtFee(BigDecimal.ZERO);
			plan.setCtdOvrlmtFee(plan.getCtdOvrlmtFee().subtract(remainingAmt));
		} else {
			plan.setPastOvrlmtFee(plan.getPastOvrlmtFee().subtract(txnPost.getPostAmt()));
		}
		// 计划-当期贷记金额
		plan.setCtdAmtCr(plan.getCtdAmtCr().add(txnPost.getPostAmt()));
		// 计划-当期贷记笔数
		plan.setCtdNbrCr(plan.getCtdNbrCr()+1);
		
		// 账户-账户余额
		account.setCurrBal(account.getCurrBal().subtract(txnPost.getPostAmt()));
		
		if(isAdjust){
			// 账户-当期贷记调整金额
			account.setCtdCrAdjAmt(account.getCtdCrAdjAmt().add(txnPost.getPostAmt()));
			// 账户-当期贷记调整笔数
			account.setCtdCrAdjCnt(account.getCtdCrAdjCnt() + 1);
		}else{
			// 账户-当期费用金额
			account.setCtdFeeAmt(account.getCtdFeeAmt().subtract(txnPost.getPostAmt()));
			// 账户-当期费用笔数
			account.setCtdFeeCnt(account.getCtdFeeCnt() + 1);
		}
		return BucketType.OverLimitFee;
	}

	/**
	 * 15：交易费借记调整
	 * @param isAdjust 
	 */
	public BucketType L15_TxnFeeDebit(CcsAcct account, CcsPostingTmp txnPost, CcsPlan plan, boolean isAdjust) {
		
		// 计划-当前余额
		plan.setCurrBal(plan.getCurrBal().add(txnPost.getPostAmt()));
		// 计划-当期交易费
		plan.setCtdTxnFee(plan.getCtdTxnFee().add(txnPost.getPostAmt()));
		// 计划-当期借记金额
		plan.setCtdAmtDb(plan.getCtdAmtDb().add(txnPost.getPostAmt()));
		// 计划-当期借记笔数
		plan.setCtdNbrDb(plan.getCtdNbrDb()+1);
		
		// 账户-账户余额
		account.setCurrBal(account.getCurrBal().add(txnPost.getPostAmt()));
		
		if(isAdjust){
			// 账户-当期借记调整金额
			account.setCtdDbAdjAmt(account.getCtdDbAdjAmt().add(txnPost.getPostAmt()));
			// 账户-当期借记调整笔数
			account.setCtdDbAdjCnt(account.getCtdDbAdjCnt() + 1);
		}else{
			// 账户-当期费用金额
			account.setCtdFeeAmt(account.getCtdFeeAmt().add(txnPost.getPostAmt()));
			// 账户-当期费用笔数
			account.setCtdFeeCnt(account.getCtdFeeCnt() + 1);
		}
		return BucketType.TXNFee;
	}

	/**
	 * 16：交易费贷记调整
	 * @param isAdjust 
	 */
	public BucketType L16_TxnFeeCredit(CcsAcct account, CcsPostingTmp txnPost, CcsPlan plan, boolean isAdjust) {
		
		// 计划-当前余额
		plan.setCurrBal(plan.getCurrBal().subtract(txnPost.getPostAmt()));
		// 计划-当期交易费
		BigDecimal remainingAmt = txnPost.getPostAmt().subtract(plan.getPastTxnFee());
		if (remainingAmt.compareTo(BigDecimal.ZERO) >= 0) {
			plan.setPastTxnFee(BigDecimal.ZERO);
			plan.setCtdTxnFee(plan.getCtdTxnFee().subtract(remainingAmt));
		} else {
			plan.setPastTxnFee(plan.getPastTxnFee().subtract(txnPost.getPostAmt()));
		}
		// 计划-当期贷记金额
		plan.setCtdAmtCr(plan.getCtdAmtCr().add(txnPost.getPostAmt()));
		// 计划-当期贷记笔数
		plan.setCtdNbrCr(plan.getCtdNbrCr()+1);
		
		// 账户-账户余额
		account.setCurrBal(account.getCurrBal().subtract(txnPost.getPostAmt()));
		
		if(isAdjust){
			// 账户-当期贷记调整金额
			account.setCtdCrAdjAmt(account.getCtdCrAdjAmt().add(txnPost.getPostAmt()));
			// 账户-当期贷记调整笔数
			account.setCtdCrAdjCnt(account.getCtdCrAdjCnt() + 1);
		}else{
			// 账户-当期费用金额
			account.setCtdFeeAmt(account.getCtdFeeAmt().subtract(txnPost.getPostAmt()));
			// 账户-当期费用笔数
			account.setCtdFeeCnt(account.getCtdFeeCnt() + 1);
		}
		return BucketType.TXNFee;
	}

	/**
	 * 23：本金强制借记调整
	 */
	public BucketType L23_PrincipalForceDebit(CcsAcct account, CcsPostingTmp txnPost, CcsPlan plan) {

		// 计划-当前余额
		plan.setCurrBal(plan.getCurrBal().add(txnPost.getPostAmt()));
		// 计划-当期本金
		plan.setCtdPrincipal(plan.getCtdPrincipal().add(txnPost.getPostAmt()));
		// 计划-当期借记金额
		plan.setCtdAmtDb(plan.getCtdAmtDb().add(txnPost.getPostAmt()));
		// 计划-当期借记笔数
		plan.setCtdNbrDb(plan.getCtdNbrDb() + 1);
		
		// 账户-账户余额
		account.setCurrBal(account.getCurrBal().add(txnPost.getPostAmt()));
		// 账户-当期本金
		account.setPrincipalBal(account.getPrincipalBal().add(txnPost.getPostAmt()));
		// 账户-取现交易
		if (PlanType.C.equals(plan.getPlanType())) {
			// 账户-当期取现余额
			account.setCashBal(account.getCashBal().add(txnPost.getPostAmt()));
		}
		// 账户-当期借记调整金额
		account.setCtdDbAdjAmt(account.getCtdDbAdjAmt().add(txnPost.getPostAmt()));
		// 账户-当期借记调整笔数
		account.setCtdDbAdjCnt(account.getCtdDbAdjCnt() + 1);
		
		return BucketType.Pricinpal;
	}

	/**
	 * 24：本金强制贷记调整
	 */
	public BucketType L24_PrincipalForceCredit(CcsAcct account, CcsPostingTmp txnPost, CcsPlan plan) {
		
		// 计划-当前余额
		plan.setCurrBal(plan.getCurrBal().subtract(txnPost.getPostAmt()));
		// 计划-当期本金
		BigDecimal remainingAmt = txnPost.getPostAmt().subtract(plan.getCtdPrincipal());
		if (remainingAmt.compareTo(BigDecimal.ZERO) >= 0) {
			plan.setCtdPrincipal(BigDecimal.ZERO);
			plan.setPastPrincipal(plan.getPastPrincipal().subtract(remainingAmt));
		} else {
			plan.setCtdPrincipal(plan.getCtdPrincipal().subtract(txnPost.getPostAmt()));
		}
		// 计划-当期贷记金额
		plan.setCtdAmtCr(plan.getCtdAmtCr().add(txnPost.getPostAmt()));
		// 计划-当期贷记笔数
		plan.setCtdNbrCr(plan.getCtdNbrCr()+1);
		
		// 账户-账户余额
		account.setCurrBal(account.getCurrBal().subtract(txnPost.getPostAmt()));
		// 账户-当期本金
		account.setPrincipalBal(account.getPrincipalBal().subtract(txnPost.getPostAmt()));
		// 账户-取现交易
		if (PlanType.C.equals(plan.getPlanType())) {
			// 账户-当期取现余额
			account.setCashBal(account.getCashBal().subtract(txnPost.getPostAmt()));
		}
		// 账户-当期贷记调整金额
		account.setCtdCrAdjAmt(account.getCtdCrAdjAmt().add(txnPost.getPostAmt()));
		// 账户-当期贷记调整笔数
		account.setCtdCrAdjCnt(account.getCtdCrAdjCnt() + 1);
		
		return BucketType.Pricinpal;
	}

	/**
	 * 账单分期本金贷记调整
	 * @param account
	 * @param txnPost
	 * @param plan
	 * @return
	 */
	private BucketType L26(CcsAcct account, CcsPostingTmp txnPost, CcsPlan plan) {
		
		// 计划-当前余额
		plan.setCurrBal(plan.getCurrBal().subtract(txnPost.getPostAmt()));
		// 计划-当期本金
		BigDecimal remainingAmt = txnPost.getPostAmt().subtract(plan.getPastPrincipal());
		if (remainingAmt.compareTo(BigDecimal.ZERO) >= 0) {
			plan.setPastPrincipal(BigDecimal.ZERO);
			plan.setCtdPrincipal(plan.getCtdPrincipal().subtract(remainingAmt));
		} else {
			plan.setPastPrincipal(plan.getPastPrincipal().subtract(txnPost.getPostAmt()));
		}
		// 计划-当期贷记金额
		plan.setCtdAmtCr(plan.getCtdAmtCr().add(txnPost.getPostAmt()));
		// 计划-当期贷记笔数
		plan.setCtdNbrCr(plan.getCtdNbrCr()+1);
		
		// 账户-账户余额
		account.setCurrBal(account.getCurrBal().subtract(txnPost.getPostAmt()));
		// 账户-当期本金
		account.setPrincipalBal(account.getPrincipalBal().subtract(txnPost.getPostAmt()));
		// 账户-当期贷记调整金额
		account.setCtdCrAdjAmt(account.getCtdCrAdjAmt().add(txnPost.getPostAmt()));
		// 账户-当期贷记调整笔数
		account.setCtdCrAdjCnt(account.getCtdCrAdjCnt() + 1);
		
		return BucketType.Pricinpal;
	}

	/**
	 * 30：还款
	 */
	public BucketType L30_Payment(S6000AcctInfo item, CcsPostingTmp txnPost, CcsPlan depositPlan) {

		// 计划-当期贷记金额
		depositPlan.setCtdAmtCr(depositPlan.getCtdAmtCr().add(txnPost.getPostAmt()));
		// 计划-当期贷记笔数
		depositPlan.setCtdNbrCr(depositPlan.getCtdNbrCr()+1);

		// 账户-账户余额
		item.getAccount().setCurrBal(item.getAccount().getCurrBal().subtract(txnPost.getPostAmt()));
		// 账户-当期还款金额
		item.getAccount().setCtdRepayAmt(item.getAccount().getCtdRepayAmt().add(txnPost.getPostAmt()));
		// 账户-当期还款笔数
		item.getAccount().setCtdRepayCnt(item.getAccount().getCtdRepayCnt() + 1);
		// 账户-本月还款金额
		item.getAccount().setMtdPaymentAmt(item.getAccount().getMtdPaymentAmt().add(txnPost.getPostAmt()));
		// 账户-本月还款笔数
		item.getAccount().setMtdPaymentCnt(item.getAccount().getMtdPaymentCnt() + 1);
		// 账户-本年还款金额
		item.getAccount().setYtdRepayAmt(item.getAccount().getYtdRepayAmt().add(txnPost.getPostAmt()));
		// 账户-本年还款笔数
		item.getAccount().setYtdRepayCnt(item.getAccount().getYtdRepayCnt() + 1);
		// 账户-历史还款金额
		item.getAccount().setLtdRepayAmt(item.getAccount().getLtdRepayAmt().add(txnPost.getPostAmt()));
		// 账户-历史还款笔数
		item.getAccount().setLtdRepayCnt(item.getAccount().getLtdRepayCnt() + 1);
		// 上次还款金额
		item.getAccount().setLastPmtAmt(txnPost.getPostAmt());
		// 上次还款日期
		item.getAccount().setLastPmtDate(batchFacility.getBatchDate());
		
		// 还款交易分配及入账处理
		paymentHier.paymentHierarchy(item, txnPost.getPostAmt(),txnPost.getTxnDate(), depositPlan);
		
		
		
		return null;
	}

	/**
	 * 31：还款撤销
	 */
	public BucketType L31_PaymentReversal(CcsAcct account, CcsPostingTmp txnPost, CcsPlan plan) {
		logger.info("交易编码编码：" + txnPost.getTxnCode() + "[还款撤销]成功！");
		
		return null;
	}
	/**
	 * 指定借据还款
	 * @param item
	 * @param txnPost
	 * @param plan
	 * @return
	 */
	private BucketType L32_AssignPayment(S6000AcctInfo item, CcsPostingTmp txnPost,CcsPlan plan) {
		// 计划-当期贷记金额
		plan.setCtdAmtCr(plan.getCtdAmtCr().add(txnPost.getPostAmt()));
		// 计划-当期贷记笔数
		plan.setCtdNbrCr(plan.getCtdNbrCr()+1);

		// 账户-账户余额
		item.getAccount().setCurrBal(item.getAccount().getCurrBal().subtract(txnPost.getPostAmt()));
		// 账户-当期还款金额
		item.getAccount().setCtdRepayAmt(item.getAccount().getCtdRepayAmt().add(txnPost.getPostAmt()));
		// 账户-当期还款笔数
		item.getAccount().setCtdRepayCnt(item.getAccount().getCtdRepayCnt() + 1);
		// 账户-本月还款金额
		item.getAccount().setMtdPaymentAmt(item.getAccount().getMtdPaymentAmt().add(txnPost.getPostAmt()));
		// 账户-本月还款笔数
		item.getAccount().setMtdPaymentCnt(item.getAccount().getMtdPaymentCnt() + 1);
		// 账户-本年还款金额
		item.getAccount().setYtdRepayAmt(item.getAccount().getYtdRepayAmt().add(txnPost.getPostAmt()));
		// 账户-本年还款笔数
		item.getAccount().setYtdRepayCnt(item.getAccount().getYtdRepayCnt() + 1);
		// 账户-历史还款金额
		item.getAccount().setLtdRepayAmt(item.getAccount().getLtdRepayAmt().add(txnPost.getPostAmt()));
		// 账户-历史还款笔数
		item.getAccount().setLtdRepayCnt(item.getAccount().getLtdRepayCnt() + 1);
		// 上次还款金额
		item.getAccount().setLastPmtAmt(txnPost.getPostAmt());
		// 上次还款日期
		item.getAccount().setLastPmtDate(batchFacility.getBatchDate());
		
		// 还款交易分配及入账处理,这里的交易日期传null,不进行区分交易和plan的先后
		paymentHier.assignedPaymentHierarchy(item, txnPost.getPostAmt(),null, plan, txnPost.getRefNbr());
		
		return null;
	}
	/**
	 * 51：自定义费1借记
	 */
	public BucketType L51_UserFee1Debit(CcsAcct account, CcsPostingTmp txnPost, CcsPlan plan,boolean isAdjust) {
		
		// 计划-当前余额
		plan.setCurrBal(plan.getCurrBal().add(txnPost.getPostAmt()));
		// 计划-当期自定义费1
		plan.setCtdUserFee1(plan.getCtdUserFee1().add(txnPost.getPostAmt()));
		// 计划-当期借记金额
		plan.setCtdAmtDb(plan.getCtdAmtDb().add(txnPost.getPostAmt()));
		// 计划-当期借记笔数
		plan.setCtdNbrDb(plan.getCtdNbrDb()+1);
		
		// 账户-账户余额
		account.setCurrBal(account.getCurrBal().add(txnPost.getPostAmt()));
		
		if(isAdjust){
			// 账户-当期借记调整金额
			account.setCtdDbAdjAmt(account.getCtdDbAdjAmt().add(txnPost.getPostAmt()));
			// 账户-当期借记调整笔数
			account.setCtdDbAdjCnt(account.getCtdDbAdjCnt() + 1);
		}else{
			// 账户-当期费用金额
			account.setCtdFeeAmt(account.getCtdFeeAmt().add(txnPost.getPostAmt()));
			// 账户-当期费用笔数
			account.setCtdFeeCnt(account.getCtdFeeCnt() + 1);
		}
		
		return BucketType.UserFee1;
	}

	/**
	 * 52：自定义费1贷记
	 */
	public BucketType L52_UserFee1Credit(CcsAcct account, CcsPostingTmp txnPost, CcsPlan plan,boolean isAdjust) {
		
		// 计划-当前余额
		plan.setCurrBal(plan.getCurrBal().subtract(txnPost.getPostAmt()));
		// 计划-当期自定义费1
		BigDecimal remainingAmt = txnPost.getPostAmt().subtract(plan.getPastUserFee1());
		if (remainingAmt.compareTo(BigDecimal.ZERO) >= 0) {
			plan.setPastUserFee1(BigDecimal.ZERO);
			plan.setCtdUserFee1(plan.getCtdUserFee1().subtract(remainingAmt));
		} else {
			plan.setPastUserFee1(plan.getPastUserFee1().subtract(txnPost.getPostAmt()));
		}
		// 计划-当期贷记金额
		plan.setCtdAmtCr(plan.getCtdAmtCr().add(txnPost.getPostAmt()));
		// 计划-当期贷记笔数
		plan.setCtdNbrCr(plan.getCtdNbrCr()+1);
		
		// 账户-账户余额
		account.setCurrBal(account.getCurrBal().subtract(txnPost.getPostAmt()));
		
		if(isAdjust){
			// 账户-当期贷记调整金额
			account.setCtdCrAdjAmt(account.getCtdCrAdjAmt().add(txnPost.getPostAmt()));
			// 账户-当期贷记调整笔数
			account.setCtdCrAdjCnt(account.getCtdCrAdjCnt() + 1);
		}else{
			// 账户-当期费用金额
			account.setCtdFeeAmt(account.getCtdFeeAmt().subtract(txnPost.getPostAmt()));
			// 账户-当期费用笔数
			account.setCtdFeeCnt(account.getCtdFeeCnt() + 1);
		}
		
		return BucketType.UserFee1;
	}

	/**
	 * 61：积分增加
	 */
	public BucketType L61_PointsEarned(CcsAcct account, CcsPostingTmp txnPost) {
		
		// 账户-积分余额
		account.setPointsBal(account.getPointsBal().add(txnPost.getPostAmt().setScale(0, BigDecimal.ROUND_CEILING)));
		// 账户-当期新增积分
		account.setCtdPoints(account.getCtdPoints().add(txnPost.getPostAmt().setScale(0, BigDecimal.ROUND_CEILING)));

		// 当前交易-积分数值
		txnPost.setPoints(txnPost.getPoints().add(txnPost.getPostAmt().setScale(0, BigDecimal.ROUND_CEILING)));
		
		return null;
	}

	/**
	 * 62：积分减少
	 */
	public BucketType L62_PointsAdjust(CcsAcct account, CcsPostingTmp txnPost) {
		
		// 账户-积分余额
		account.setPointsBal(account.getPointsBal().subtract(txnPost.getPostAmt().setScale(0, BigDecimal.ROUND_CEILING)));
		// 账户-当期调整积分
		account.setCtdAdjPoints(account.getCtdAdjPoints().add(txnPost.getPostAmt().setScale(0, BigDecimal.ROUND_CEILING)));

		// 当前交易-积分数值
		txnPost.setPoints(txnPost.getPoints().add(txnPost.getPostAmt().setScale(0, BigDecimal.ROUND_CEILING)));
		
		return null;
	}

	/**
	 * 63：积分兑换
	 */
	public BucketType L63_PointsDisburse(CcsAcct account, CcsPostingTmp txnPost) {
		
		// 账户-积分余额
		account.setPointsBal(account.getPointsBal().subtract(txnPost.getPostAmt().setScale(0, BigDecimal.ROUND_CEILING)));
		// 账户-当期兑换积分
		account.setCtdSpendPoints(account.getCtdSpendPoints().add(txnPost.getPostAmt().setScale(0, BigDecimal.ROUND_CEILING)));

		// 当前交易-积分数值
		txnPost.setPoints(txnPost.getPoints().add(txnPost.getPostAmt().setScale(0, BigDecimal.ROUND_CEILING)));
		
		return null;
	}

	/**
	 * 80：争议解决（有利于银行，收利息）
	 */
	public BucketType L80_DisputeReleaseBankInterest(CcsAcct account, CcsPostingTmp txnPost, CcsPlan plan) {
		logger.info("80：交易编码编码：" + txnPost.getTxnCode() + "[争议解决（有利于银行，收利息）]成功！");
		
		return null;
	}

	/**
	 * 96：争议提出
	 */
	public BucketType L96_DisputeAdd(CcsAcct account, CcsPostingTmp txnPost, CcsPlan plan) {
		logger.info("96：交易编码编码：" + txnPost.getTxnCode() + "[争议提出]成功！");
		
		return null;
	}

	/**
	 * 97：争议解决（有利于持卡人，不收利息）
	 */
	public BucketType L97_DisputeReleaseCustomer(CcsAcct account, CcsPostingTmp txnPost, CcsPlan plan) {
		logger.info("97：交易编码编码：" + txnPost.getTxnCode() + "[争议解决（有利于持卡人，不收利息）]成功！");
		
		return null;
	}

	/**
	 * 98：争议解决（有利于银行，不收利息）
	 */
	public BucketType L98_DisputeReleaseBankNoInterest(CcsAcct account, CcsPostingTmp txnPost, CcsPlan plan) {
		logger.info("98：交易编码编码：" + txnPost.getTxnCode() + "[争议解决（有利于银行，不收利息）]成功！");
		
		return null;
	}

	/**
	 * 
	 * @param txnPost
	 * @return
	 */
	public BucketType defaultModule(CcsPostingTmp txnPost) {
		// 03：入账逻辑模块不合法
		txnPost.setPostingFlag(PostingFlag.F03);
		
		return null;
	}
	
	/**
	 * @see 方法名：L40_MulctDebit 
	 * @see 描述：罚金入账
	 * @see 创建日期：2015-6-24下午8:50:25
	 * @author liuqi
	 *  
	 * @param account
	 * @param txnPost
	 * @param plan
	 * @param isAdjust
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public BucketType L40_MulctDebit(CcsAcct account, CcsPostingTmp txnPost, CcsPlan plan, boolean isAdjust) {
		
		// 计划-当前余额
		plan.setCurrBal(plan.getCurrBal().add(txnPost.getPostAmt()));
		// 计划-当期罚金
		plan.setCtdMulctAmt(plan.getCtdMulctAmt().add(txnPost.getPostAmt()));
		// 计划-当期借记金额
		plan.setCtdAmtDb(plan.getCtdAmtDb().add(txnPost.getPostAmt()));
		// 计划-当期借记笔数
		plan.setCtdNbrDb(plan.getCtdNbrDb()+1);
		
		// 账户-账户余额
		account.setCurrBal(account.getCurrBal().add(txnPost.getPostAmt()));
		
		if(isAdjust){
			// 账户-当期借记调整金额
			account.setCtdDbAdjAmt(account.getCtdDbAdjAmt().add(txnPost.getPostAmt()));
			// 账户-当期借记调整笔数
			account.setCtdDbAdjCnt(account.getCtdDbAdjCnt() + 1);
		}else{
			// 账户-当期费用金额
			account.setCtdFeeAmt(account.getCtdFeeAmt().add(txnPost.getPostAmt()));
			// 账户-当期费用笔数
			account.setCtdFeeCnt(account.getCtdFeeCnt() + 1);
	}
		
		return BucketType.Mulct;
	}
	/**
	 * @see 方法名：L41_MulctCredit 
	 * @see 描述：罚金回溯
	 * @see 创建日期：2015-6-24下午8:50:25
	 * @author liuqi
	 *  
	 * @param account
	 * @param txnPost
	 * @param plan
	 * @param isAdjust
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public BucketType L41_MulctCredit(CcsAcct account, CcsPostingTmp txnPost, CcsPlan plan, boolean isAdjust) {
		

		// 计划-当前余额
		plan.setCurrBal(plan.getCurrBal().subtract(txnPost.getPostAmt()));
		// 计划-当期罚金
		BigDecimal mulctAmt = txnPost.getPostAmt().subtract(plan.getPastMulctAmt());
		if (mulctAmt.compareTo(BigDecimal.ZERO) >= 0) {
			plan.setPastMulctAmt(BigDecimal.ZERO);
			plan.setCtdMulctAmt(plan.getCtdMulctAmt().subtract(mulctAmt));
		} else {
			plan.setPastMulctAmt(plan.getPastMulctAmt().subtract(txnPost.getPostAmt()));
		}
		// 计划-当期贷记金额
		plan.setCtdAmtCr(plan.getCtdAmtCr().add(txnPost.getPostAmt()));
		// 计划-当期贷记笔数
		plan.setCtdNbrCr(plan.getCtdNbrCr()+1);
		
		// 账户-账户余额
		account.setCurrBal(account.getCurrBal().subtract(txnPost.getPostAmt()));
		
		if(isAdjust){
			// 账户-当期贷记调整金额
			account.setCtdCrAdjAmt(account.getCtdCrAdjAmt().add(txnPost.getPostAmt()));
			// 账户-当期贷记调整笔数
			account.setCtdCrAdjCnt(account.getCtdCrAdjCnt() + 1);
		}else{
			// 账户-当期费用金额
			account.setCtdFeeAmt(account.getCtdFeeAmt().subtract(txnPost.getPostAmt()));
			// 账户-当期费用笔数
			account.setCtdFeeCnt(account.getCtdFeeCnt() + 1);
		}
		
		return BucketType.Mulct;
	}

	/**
	 * @see 方法名：L42_InsuranceFeeDebit 
	 * @see 描述：保险费入账
	 * @see 创建日期：2015-09-01
	 * @author liuqi
	 *  
	 * @param account
	 * @param txnPost
	 * @param plan
	 * @param isAdjust
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public BucketType L42_InsuranceFeeDebit(CcsAcct account, CcsPostingTmp txnPost, CcsPlan plan, boolean isAdjust) {
		
		// 计划-当前余额
		plan.setCurrBal(plan.getCurrBal().add(txnPost.getPostAmt()));
		// 计划-当期保险费
		plan.setCtdInsurance(plan.getCtdInsurance().add(txnPost.getPostAmt()));
		// 计划-当期借记金额
		plan.setCtdAmtDb(plan.getCtdAmtDb().add(txnPost.getPostAmt()));
		// 计划-当期借记笔数
		plan.setCtdNbrDb(plan.getCtdNbrDb()+1);
		
		// 账户-账户余额
		account.setCurrBal(account.getCurrBal().add(txnPost.getPostAmt()));
		
		if(isAdjust){
			// 账户-当期借记调整金额
			account.setCtdDbAdjAmt(account.getCtdDbAdjAmt().add(txnPost.getPostAmt()));
			// 账户-当期借记调整笔数
			account.setCtdDbAdjCnt(account.getCtdDbAdjCnt() + 1);
		}else{
			// 账户-当期利息
			// 账户-当期费用金额
			account.setCtdFeeAmt(account.getCtdFeeAmt().add(txnPost.getPostAmt()));
			// 账户-当期费用笔数
			account.setCtdFeeCnt(account.getCtdFeeCnt() + 1);
		}
		
		return BucketType.InsuranceFee;
	}
	/**
	 * @see 方法名：L43_InsuranceFeeCredit 
	 * @see 描述：保险费贷调
	 * @see 创建日期：2015-09-01
	 * @author liuqi
	 *  
	 * @param account
	 * @param txnPost
	 * @param plan
	 * @param isAdjust
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public BucketType L43_InsuranceFeeCredit(CcsAcct account, CcsPostingTmp txnPost, CcsPlan plan, boolean isAdjust) {
		

		// 计划-当前余额
		plan.setCurrBal(plan.getCurrBal().subtract(txnPost.getPostAmt()));
		// 计划-当期保险费
		BigDecimal insuranceFeeAmt = txnPost.getPostAmt().subtract(plan.getPastInsurance());
		if (insuranceFeeAmt.compareTo(BigDecimal.ZERO) >= 0) {
			plan.setPastInsurance(BigDecimal.ZERO);
			plan.setCtdInsurance(plan.getCtdInsurance().subtract(insuranceFeeAmt));
		} else {
			plan.setPastInsurance(plan.getPastInsurance().subtract(txnPost.getPostAmt()));
		}
		// 计划-当期贷记金额
		plan.setCtdAmtCr(plan.getCtdAmtCr().add(txnPost.getPostAmt()));
		// 计划-当期贷记笔数
		plan.setCtdNbrCr(plan.getCtdNbrCr()+1);
		
		// 账户-账户余额
		account.setCurrBal(account.getCurrBal().subtract(txnPost.getPostAmt()));
		
		if(isAdjust){
			// 账户-当期贷记调整金额
			account.setCtdCrAdjAmt(account.getCtdCrAdjAmt().add(txnPost.getPostAmt()));
			// 账户-当期贷记调整笔数
			account.setCtdCrAdjCnt(account.getCtdCrAdjCnt() + 1);
		}else{
			// 账户-当期费用金额
			account.setCtdFeeAmt(account.getCtdFeeAmt().subtract(txnPost.getPostAmt()));
			// 账户-当期费用笔数
			account.setCtdFeeCnt(account.getCtdFeeCnt() + 1);
		}
		
		return BucketType.InsuranceFee;
	}

	/**
	 * @see 方法名：L44_StampDutyDebit 
	 * @see 描述：印花税入账
	 * @see 创建日期：2015-09-01
	 * @author liuqi
	 *  
	 * @param account
	 * @param txnPost
	 * @param plan
	 * @param isAdjust
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public BucketType L44_StampDutyDebit(CcsAcct account, CcsPostingTmp txnPost, CcsPlan plan, boolean isAdjust) {
		
		// 计划-当前余额
		plan.setCurrBal(plan.getCurrBal().add(txnPost.getPostAmt()));
		// 计划-当期印花税
		plan.setCtdStampdutyAmt(plan.getCtdStampdutyAmt().add(txnPost.getPostAmt()));
		// 计划-当期借记金额
		plan.setCtdAmtDb(plan.getCtdAmtDb().add(txnPost.getPostAmt()));
		// 计划-当期借记笔数
		plan.setCtdNbrDb(plan.getCtdNbrDb()+1);
		
		// 账户-账户余额
		account.setCurrBal(account.getCurrBal().add(txnPost.getPostAmt()));
		
		if(isAdjust){
			// 账户-当期借记调整金额
			account.setCtdDbAdjAmt(account.getCtdDbAdjAmt().add(txnPost.getPostAmt()));
			// 账户-当期借记调整笔数
			account.setCtdDbAdjCnt(account.getCtdDbAdjCnt() + 1);
		}else{
			// 账户-当期利息
			// 账户-当期费用金额
			account.setCtdFeeAmt(account.getCtdFeeAmt().add(txnPost.getPostAmt()));
			// 账户-当期费用笔数
			account.setCtdFeeCnt(account.getCtdFeeCnt() + 1);
		}
		
		return BucketType.StampDuty;
	}

	/**
	 * @see 方法名：L45_LifeInsuFeeDebit 
	 * @see 描述：寿险计划包费入账
	 * @see 创建日期：2015-09-01
	 * @author liuqi
	 *  
	 * @param account
	 * @param txnPost
	 * @param plan
	 * @param isAdjust
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public BucketType L45_LifeInsuFeeDebit(CcsAcct account, CcsPostingTmp txnPost, CcsPlan plan, boolean isAdjust) {
		
		// 计划-当前余额
		plan.setCurrBal(plan.getCurrBal().add(txnPost.getPostAmt()));
		// 计划-当期寿险计划包费
		plan.setCtdLifeInsuAmt(plan.getCtdLifeInsuAmt().add(txnPost.getPostAmt()));
		// 计划-当期借记金额
		plan.setCtdAmtDb(plan.getCtdAmtDb().add(txnPost.getPostAmt()));
		// 计划-当期借记笔数
		plan.setCtdNbrDb(plan.getCtdNbrDb()+1);
		
		// 账户-账户余额
		account.setCurrBal(account.getCurrBal().add(txnPost.getPostAmt()));
		
		if(isAdjust){
			// 账户-当期借记调整金额
			account.setCtdDbAdjAmt(account.getCtdDbAdjAmt().add(txnPost.getPostAmt()));
			// 账户-当期借记调整笔数
			account.setCtdDbAdjCnt(account.getCtdDbAdjCnt() + 1);
		}else{
			// 账户-当期利息
			// 账户-当期费用金额
			account.setCtdFeeAmt(account.getCtdFeeAmt().add(txnPost.getPostAmt()));
			// 账户-当期费用笔数
			account.setCtdFeeCnt(account.getCtdFeeCnt() + 1);
		}
		
		return BucketType.LifeInsuFee;
	}

	/**
	 * @see 方法名：L46_LifeInsuFeeCredit 
	 * @see 描述：寿险计划包费贷调
	 * @see 创建日期：2015-09-01
	 * @author liuqi
	 *  
	 * @param account
	 * @param txnPost
	 * @param plan
	 * @param isAdjust
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public BucketType L46_LifeInsuFeeCredit(CcsAcct account, CcsPostingTmp txnPost, CcsPlan plan, boolean isAdjust) {
		
		// 计划-当前余额
		plan.setCurrBal(plan.getCurrBal().subtract(txnPost.getPostAmt()));
		// 计划-当期寿险费
		BigDecimal CtdLifeInsuAmt = txnPost.getPostAmt().subtract(plan.getPastLifeInsuAmt());
		if (CtdLifeInsuAmt.compareTo(BigDecimal.ZERO) >= 0) {
			plan.setPastLifeInsuAmt(BigDecimal.ZERO);
			plan.setCtdLifeInsuAmt(plan.getCtdLifeInsuAmt().subtract(CtdLifeInsuAmt));
		} else {
			plan.setPastLifeInsuAmt(plan.getPastLifeInsuAmt().subtract(txnPost.getPostAmt()));
		}
		// 计划-当期贷记金额
		plan.setCtdAmtCr(plan.getCtdAmtCr().add(txnPost.getPostAmt()));
		// 计划-当期贷记笔数
		plan.setCtdNbrCr(plan.getCtdNbrCr()+1);
		
		// 账户-账户余额
		account.setCurrBal(account.getCurrBal().subtract(txnPost.getPostAmt()));
		
		if(isAdjust){
			// 账户-当期贷记调整金额
			account.setCtdCrAdjAmt(account.getCtdCrAdjAmt().add(txnPost.getPostAmt()));
			// 账户-当期贷记调整笔数
			account.setCtdCrAdjCnt(account.getCtdCrAdjCnt() + 1);
		}else{
			// 账户-当期费用金额
			account.setCtdFeeAmt(account.getCtdFeeAmt().subtract(txnPost.getPostAmt()));
			// 账户-当期费用笔数
			account.setCtdFeeCnt(account.getCtdFeeCnt() + 1);
		}
		
		return BucketType.LifeInsuFee;
	}
	
	/**
	 * @see 方法名：L47_ReplaceSvcFeeDebit 
	 * @see 描述：代收服务费入账
	 * @see 创建日期：2015-12-18
	 * @author MengXiang
	 *  
	 * @param account
	 * @param txnPost
	 * @param plan
	 * @param isAdjust
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public BucketType L47_ReplaceSvcFeeDebit(CcsAcct account, CcsPostingTmp txnPost, CcsPlan plan, boolean isAdjust) {
		
		// 计划-当前余额
		plan.setCurrBal(plan.getCurrBal().add(txnPost.getPostAmt()));
		// 计划-当期代收服务费
		plan.setCtdReplaceSvcFee(plan.getCtdReplaceSvcFee().add(txnPost.getPostAmt()));
		// 计划-当期借记金额
		plan.setCtdAmtDb(plan.getCtdAmtDb().add(txnPost.getPostAmt()));
		// 计划-当期借记笔数
		plan.setCtdNbrDb(plan.getCtdNbrDb()+1);
		
		// 账户-账户余额
		account.setCurrBal(account.getCurrBal().add(txnPost.getPostAmt()));
		
		if(isAdjust){
			// 账户-当期借记调整金额
			account.setCtdDbAdjAmt(account.getCtdDbAdjAmt().add(txnPost.getPostAmt()));
			// 账户-当期借记调整笔数
			account.setCtdDbAdjCnt(account.getCtdDbAdjCnt() + 1);
		}else{
			// 账户-当期利息
			// 账户-当期费用金额
			account.setCtdFeeAmt(account.getCtdFeeAmt().add(txnPost.getPostAmt()));
			// 账户-当期费用笔数
			account.setCtdFeeCnt(account.getCtdFeeCnt() + 1);
		}
		
		return BucketType.ReplaceSvcFee;
	}

	/**
	 * @see 方法名：L48_ReplaceSvcFeeDebit 
	 * @see 描述：代收贷调
	 * @see 创建日期：2015-12-18
	 * @author MengXiang
	 *  
	 * @param account
	 * @param txnPost
	 * @param plan
	 * @param isAdjust
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public BucketType L48_ReplaceSvcFeeCredit(CcsAcct account, CcsPostingTmp txnPost, CcsPlan plan, boolean isAdjust) {
		
		// 计划-当前余额
		plan.setCurrBal(plan.getCurrBal().subtract(txnPost.getPostAmt()));
		// 计划-当期代收服务费
		BigDecimal ctdReplaceSvcFee = txnPost.getPostAmt().subtract(plan.getPastReplaceSvcFee());
		if (ctdReplaceSvcFee.compareTo(BigDecimal.ZERO) >= 0) {
			plan.setPastReplaceSvcFee(BigDecimal.ZERO);
			plan.setCtdReplaceSvcFee(plan.getCtdReplaceSvcFee().subtract(ctdReplaceSvcFee));
		} else {
			plan.setPastReplaceSvcFee(plan.getPastReplaceSvcFee().subtract(txnPost.getPostAmt()));
		}
		// 计划-当期贷记金额
		plan.setCtdAmtCr(plan.getCtdAmtCr().add(txnPost.getPostAmt()));
		// 计划-当期贷记笔数
		plan.setCtdNbrCr(plan.getCtdNbrCr()+1);
		
		// 账户-账户余额
		account.setCurrBal(account.getCurrBal().subtract(txnPost.getPostAmt()));
		
		if(isAdjust){
			// 账户-当期贷记调整金额
			account.setCtdCrAdjAmt(account.getCtdCrAdjAmt().add(txnPost.getPostAmt()));
			// 账户-当期贷记调整笔数
			account.setCtdCrAdjCnt(account.getCtdCrAdjCnt() + 1);
		}else{
			// 账户-当期费用金额
			account.setCtdFeeAmt(account.getCtdFeeAmt().subtract(txnPost.getPostAmt()));
			// 账户-当期费用笔数
			account.setCtdFeeCnt(account.getCtdFeeCnt() + 1);
		}
		
		return BucketType.ReplaceSvcFee;
	}
	
	/**
	 * 49：趸交费入账
	 */
	public BucketType L49_Premium(CcsAcct account, CcsPostingTmp txnPost, CcsPlan plan) {
		logger.info("49：交易编码编码：" + txnPost.getTxnCode() + "[趸交费入账(Memo交易)]成功！");
		
		return null;
	}
	
	/**
	 * 代收罚金入账
	 */
	public BucketType L55_R_MulctDebit(CcsAcct account, CcsPostingTmp txnPost, CcsPlan plan, boolean isAdjust) {
		
		// 计划-当前余额
		plan.setCurrBal(plan.getCurrBal().add(txnPost.getPostAmt()));
		// 计划-当期罚金
		plan.setCtdReplaceMulct(plan.getCtdReplaceMulct().add(txnPost.getPostAmt()));
		// 计划-当期借记金额
		plan.setCtdAmtDb(plan.getCtdAmtDb().add(txnPost.getPostAmt()));
		// 计划-当期借记笔数
		plan.setCtdNbrDb(plan.getCtdNbrDb()+1);
		
		// 账户-账户余额
		account.setCurrBal(account.getCurrBal().add(txnPost.getPostAmt()));
		
		if(isAdjust){
			// 账户-当期借记调整金额
			account.setCtdDbAdjAmt(account.getCtdDbAdjAmt().add(txnPost.getPostAmt()));
			// 账户-当期借记调整笔数
			account.setCtdDbAdjCnt(account.getCtdDbAdjCnt() + 1);
		}else{
			// 账户-当期费用金额
			account.setCtdFeeAmt(account.getCtdFeeAmt().add(txnPost.getPostAmt()));
			// 账户-当期费用笔数
			account.setCtdFeeCnt(account.getCtdFeeCnt() + 1);
	}
		
		return BucketType.ReplaceMulct;
	}
	/**
	 * @see 方法名：L56_MulctCredit 
	 * @see 描述：代收罚金贷调
	 * @author lisy
	 *  
	 * @param account
	 * @param txnPost
	 * @param plan
	 * @param isAdjust
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public BucketType L56_R_MulctCredit(CcsAcct account, CcsPostingTmp txnPost, CcsPlan plan, boolean isAdjust) {
		

		// 计划-当前余额
		plan.setCurrBal(plan.getCurrBal().subtract(txnPost.getPostAmt()));
		// 计划-当期罚金
		BigDecimal mulctAmt = txnPost.getPostAmt().subtract(plan.getPastReplaceMulct());
		if (mulctAmt.compareTo(BigDecimal.ZERO) >= 0) {
			plan.setPastReplaceMulct(BigDecimal.ZERO);
			plan.setCtdReplaceMulct(plan.getCtdReplaceMulct().subtract(mulctAmt));
		} else {
			plan.setPastReplaceMulct(plan.getPastReplaceMulct().subtract(txnPost.getPostAmt()));
		}
		// 计划-当期贷记金额
		plan.setCtdAmtCr(plan.getCtdAmtCr().add(txnPost.getPostAmt()));
		// 计划-当期贷记笔数
		plan.setCtdNbrCr(plan.getCtdNbrCr()+1);
		
		// 账户-账户余额
		account.setCurrBal(account.getCurrBal().subtract(txnPost.getPostAmt()));
		
		if(isAdjust){
			// 账户-当期贷记调整金额
			account.setCtdCrAdjAmt(account.getCtdCrAdjAmt().add(txnPost.getPostAmt()));
			// 账户-当期贷记调整笔数
			account.setCtdCrAdjCnt(account.getCtdCrAdjCnt() + 1);
		}else{
			// 账户-当期费用金额
			account.setCtdFeeAmt(account.getCtdFeeAmt().subtract(txnPost.getPostAmt()));
			// 账户-当期费用笔数
			account.setCtdFeeCnt(account.getCtdFeeCnt() + 1);
		}
		
		return BucketType.ReplaceMulct;
	}
	
	/**
	 * 代收滞纳金入账
	 * @param account
	 * @param txnPost
	 * @param plan
	 * @param isAdjust
	 * @return
	 */
	public BucketType L57_R_LpcDebit(CcsAcct account, CcsPostingTmp txnPost, CcsPlan plan, boolean isAdjust) {
		
		// 计划-当前余额
		plan.setCurrBal(plan.getCurrBal().add(txnPost.getPostAmt()));
		// 计划-当期滞纳金
		plan.setCtdReplaceLateFee(plan.getCtdReplaceLateFee().add(txnPost.getPostAmt()));
		// 计划-当期借记金额
		plan.setCtdAmtDb(plan.getCtdAmtDb().add(txnPost.getPostAmt()));
		// 计划-当期借记笔数
		plan.setCtdNbrDb(plan.getCtdNbrDb()+1);
		
		// 账户-账户余额
		account.setCurrBal(account.getCurrBal().add(txnPost.getPostAmt()));
		
		if(isAdjust){
			// 账户-当期借记调整金额
			account.setCtdDbAdjAmt(account.getCtdDbAdjAmt().add(txnPost.getPostAmt()));
			// 账户-当期借记调整笔数
			account.setCtdDbAdjCnt(account.getCtdDbAdjCnt() + 1);
		}else{
			// 账户-当期费用金额
			account.setCtdFeeAmt(account.getCtdFeeAmt().add(txnPost.getPostAmt()));
			// 账户-当期费用笔数
			account.setCtdFeeCnt(account.getCtdFeeCnt() + 1);
		}
		
		return BucketType.ReplaceLatePaymentCharge;
	}

	/**
	 * @author Lisy
	 *  
	 * @param account
	 * @param txnPost
	 * @param plan
	 * @param isAdjust
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public BucketType L58_R_LpcCredit(CcsAcct account, CcsPostingTmp txnPost, CcsPlan plan, boolean isAdjust) {
		
		// 计划-当前余额
		plan.setCurrBal(plan.getCurrBal().subtract(txnPost.getPostAmt()));
		// 计划-当期滞纳金
		BigDecimal remainingAmt = txnPost.getPostAmt().subtract(plan.getPastReplaceLateFee());
		if (remainingAmt.compareTo(BigDecimal.ZERO) >= 0) {
			plan.setPastReplaceLateFee(BigDecimal.ZERO);
			plan.setCtdReplaceLateFee(plan.getCtdReplaceLateFee().subtract(remainingAmt));
		} else {
			plan.setPastReplaceLateFee(plan.getPastReplaceLateFee().subtract(txnPost.getPostAmt()));
		}
		// 计划-当期贷记金额
		plan.setCtdAmtCr(plan.getCtdAmtCr().add(txnPost.getPostAmt()));
		// 计划-当期贷记笔数
		plan.setCtdNbrCr(plan.getCtdNbrCr()+1);
		
		// 账户-账户余额
		account.setCurrBal(account.getCurrBal().subtract(txnPost.getPostAmt()));
		
		if(isAdjust){
			// 账户-当期贷记调整金额
			account.setCtdCrAdjAmt(account.getCtdCrAdjAmt().add(txnPost.getPostAmt()));
			// 账户-当期贷记调整笔数
			account.setCtdCrAdjCnt(account.getCtdCrAdjCnt() + 1);
		}else{
			// 账户-当期费用金额
			account.setCtdFeeAmt(account.getCtdFeeAmt().subtract(txnPost.getPostAmt()));
			// 账户-当期费用笔数
			account.setCtdFeeCnt(account.getCtdFeeCnt() + 1);
		}
		
		return BucketType.ReplaceLatePaymentCharge;
	}
	
	/**
	 * 代收罚息入账
	 */
	public BucketType L53_R_PenaltyDebit(CcsAcct account, CcsPostingTmp txnPost, CcsPlan plan, boolean isAdjust) {
		
		// 计划-当前余额
		plan.setCurrBal(plan.getCurrBal().add(txnPost.getPostAmt()));
		// 计划-当期利息
		plan.setCtdReplacePenalty(plan.getCtdReplacePenalty().add(txnPost.getPostAmt()));
		// 计划-当期借记金额
		plan.setCtdAmtDb(plan.getCtdAmtDb().add(txnPost.getPostAmt()));
		// 计划-当期借记笔数
		plan.setCtdNbrDb(plan.getCtdNbrDb()+1);
		
		// 账户-账户余额
		account.setCurrBal(account.getCurrBal().add(txnPost.getPostAmt()));
		
		if(isAdjust){
			// 账户-当期借记调整金额
			account.setCtdDbAdjAmt(account.getCtdDbAdjAmt().add(txnPost.getPostAmt()));
			// 账户-当期借记调整笔数
			account.setCtdDbAdjCnt(account.getCtdDbAdjCnt() + 1);
		}else{
			// 账户-当期利息
			account.setCtdInterestAmt(account.getCtdInterestAmt().add(txnPost.getPostAmt()));
			// 账户-当期利息笔数
			account.setCtdInterestCnt(account.getCtdInterestCnt() + 1);
		}
		
		return BucketType.Interest;
	}
	
	/**
	 * 代收罚息贷调
	 * @param account
	 * @param txnPost
	 * @param plan
	 * @param isAdjust
	 * @return
	 */
	public BucketType L54_R_PenaltyCredit(CcsAcct account, CcsPostingTmp txnPost, CcsPlan plan, boolean isAdjust) {
		
		// 计划-当前余额
		plan.setCurrBal(plan.getCurrBal().subtract(txnPost.getPostAmt()));
		// 计划-当期利息
		BigDecimal remainingAmt = txnPost.getPostAmt().subtract(plan.getPastReplacePenalty());
		if (remainingAmt.compareTo(BigDecimal.ZERO) >= 0) {
			plan.setPastReplacePenalty(BigDecimal.ZERO);
			plan.setCtdReplacePenalty(plan.getCtdReplacePenalty().subtract(remainingAmt));
		} else {
			plan.setPastReplacePenalty(plan.getPastReplacePenalty().subtract(txnPost.getPostAmt()));
		}
		// 计划-当期贷记金额
		plan.setCtdAmtCr(plan.getCtdAmtCr().add(txnPost.getPostAmt()));
		// 计划-当期贷记笔数
		plan.setCtdNbrCr(plan.getCtdNbrCr()+1);
		
		// 账户-账户余额
		account.setCurrBal(account.getCurrBal().subtract(txnPost.getPostAmt()));
		
		if(isAdjust){
			// 账户-当期贷记调整金额
			account.setCtdCrAdjAmt(account.getCtdCrAdjAmt().add(txnPost.getPostAmt()));
			// 账户-当期贷记调整笔数
			account.setCtdCrAdjCnt(account.getCtdCrAdjCnt() + 1);
		}else{
			// 账户-当期利息
			account.setCtdInterestAmt(account.getCtdInterestAmt().subtract(txnPost.getPostAmt()));
			// 账户-当期利息笔数
			account.setCtdInterestCnt(account.getCtdInterestCnt() + 1);
		}
		
		return BucketType.Interest;
	}

	/**
	 * 代收手续费入账
	 * @param account
	 * @param txnPost
	 * @param plan
	 * @param isAdjust
	 * @return
	 */
	public BucketType L59_R_TxnFeeDebit(CcsAcct account,
			CcsPostingTmp txnPost, CcsPlan plan, boolean isAdjust) {// 计划-当前余额
		plan.setCurrBal(plan.getCurrBal().add(txnPost.getPostAmt()));
		// 计划-当期交易费
		plan.setCtdReplaceTxnFee(plan.getCtdReplaceTxnFee().add(txnPost.getPostAmt()));
		// 计划-当期借记金额
		plan.setCtdAmtDb(plan.getCtdAmtDb().add(txnPost.getPostAmt()));
		// 计划-当期借记笔数
		plan.setCtdNbrDb(plan.getCtdNbrDb()+1);
		
		// 账户-账户余额
		account.setCurrBal(account.getCurrBal().add(txnPost.getPostAmt()));
		
		if(isAdjust){
			// 账户-当期借记调整金额
			account.setCtdDbAdjAmt(account.getCtdDbAdjAmt().add(txnPost.getPostAmt()));
			// 账户-当期借记调整笔数
			account.setCtdDbAdjCnt(account.getCtdDbAdjCnt() + 1);
		}else{
			// 账户-当期费用金额
			account.setCtdFeeAmt(account.getCtdFeeAmt().add(txnPost.getPostAmt()));
			// 账户-当期费用笔数
			account.setCtdFeeCnt(account.getCtdFeeCnt() + 1);
		}
		return BucketType.ReplaceTxnFee;
	}
	
	/**
	 * 代收手续费贷调
	 * @param account
	 * @param txnPost
	 * @param plan
	 * @param isAdjust
	 * @return
	 */
	public BucketType L60_R_TxnFeeCredit(CcsAcct account,
			CcsPostingTmp txnPost, CcsPlan plan, boolean isAdjust) {
		
		// 计划-当前余额
		plan.setCurrBal(plan.getCurrBal().subtract(txnPost.getPostAmt()));
		// 计划-当期交易费
		BigDecimal remainingAmt = txnPost.getPostAmt().subtract(plan.getPastReplaceTxnFee());
		if (remainingAmt.compareTo(BigDecimal.ZERO) >= 0) {
			plan.setPastReplaceTxnFee(BigDecimal.ZERO);
			plan.setCtdReplaceTxnFee(plan.getCtdReplaceTxnFee().subtract(remainingAmt));
		} else {
			plan.setPastReplaceTxnFee(plan.getPastReplaceTxnFee().subtract(txnPost.getPostAmt()));
		}
		// 计划-当期贷记金额
		plan.setCtdAmtCr(plan.getCtdAmtCr().add(txnPost.getPostAmt()));
		// 计划-当期贷记笔数
		plan.setCtdNbrCr(plan.getCtdNbrCr()+1);
		
		// 账户-账户余额
		account.setCurrBal(account.getCurrBal().subtract(txnPost.getPostAmt()));
		
		if(isAdjust){
			// 账户-当期贷记调整金额
			account.setCtdCrAdjAmt(account.getCtdCrAdjAmt().add(txnPost.getPostAmt()));
			// 账户-当期贷记调整笔数
			account.setCtdCrAdjCnt(account.getCtdCrAdjCnt() + 1);
		}else{
			// 账户-当期费用金额
			account.setCtdFeeAmt(account.getCtdFeeAmt().subtract(txnPost.getPostAmt()));
			// 账户-当期费用笔数
			account.setCtdFeeCnt(account.getCtdFeeCnt() + 1);
		}
		return BucketType.ReplaceTxnFee;
	}
	
	/**
	 * @see 方法名：L64_PrepayPkgAmtDebit 
	 * @see 描述：灵活还款计划包入账
	 * @author Lisy
	 *  
	 * @param account
	 * @param txnPost
	 * @param plan
	 * @param isAdjust
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public BucketType L64_PrepayPkgAmtDebit(CcsAcct account, CcsPostingTmp txnPost, CcsPlan plan) {
		
		// 计划-当前余额
		plan.setCurrBal(plan.getCurrBal().add(txnPost.getPostAmt()));
		// 计划-当期灵活还款计划包
		plan.setCtdPrepayPkgFee(plan.getCtdPrepayPkgFee().add(txnPost.getPostAmt()));
		// 计划-当期借记金额
		plan.setCtdAmtDb(plan.getCtdAmtDb().add(txnPost.getPostAmt()));
		// 计划-当期借记笔数
		plan.setCtdNbrDb(plan.getCtdNbrDb()+1);
		
		// 账户-账户余额
		account.setCurrBal(account.getCurrBal().add(txnPost.getPostAmt()));
		
		// 账户-当期费用金额
		account.setCtdFeeAmt(account.getCtdFeeAmt().add(txnPost.getPostAmt()));
		// 账户-当期费用笔数
		account.setCtdFeeCnt(account.getCtdFeeCnt() + 1);
		
		return BucketType.PrepayPkg;
	}
	
	/**
	 * @see 方法名：L65_PrepayPkgAmtCedit 
	 * @see 描述：灵活还款计划包贷调
	 * @author Lisy
	 *  
	 * @param account
	 * @param txnPost
	 * @param plan
	 * @param isAdjust
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public BucketType L65_PrepayPkgAmtCedit(CcsAcct account, CcsPostingTmp txnPost, CcsPlan plan) {
		
		// 计划-当前余额
		plan.setCurrBal(plan.getCurrBal().subtract(txnPost.getPostAmt()));
		// 计划-当期代收服务费
		BigDecimal ctdPrepayPkg = txnPost.getPostAmt().subtract(plan.getPastPrepayPkgFee());
		if (ctdPrepayPkg.compareTo(BigDecimal.ZERO) >= 0) {
			plan.setPastPrepayPkgFee(BigDecimal.ZERO);
			plan.setCtdPrepayPkgFee(plan.getCtdPrepayPkgFee().subtract(ctdPrepayPkg));
		} else {
			plan.setPastPrepayPkgFee(plan.getPastPrepayPkgFee().subtract(txnPost.getPostAmt()));
		}
		// 计划-当期贷记金额
		plan.setCtdAmtCr(plan.getCtdAmtCr().add(txnPost.getPostAmt()));
		// 计划-当期贷记笔数
		plan.setCtdNbrCr(plan.getCtdNbrCr()+1);
		
		// 账户-账户余额
		account.setCurrBal(account.getCurrBal().subtract(txnPost.getPostAmt()));
		
		// 账户-当期费用金额
		account.setCtdFeeAmt(account.getCtdFeeAmt().subtract(txnPost.getPostAmt()));
		// 账户-当期费用笔数
		account.setCtdFeeCnt(account.getCtdFeeCnt() + 1);
		
		return BucketType.PrepayPkg;
	}
	
	/**
	 * @see 方法名：L67_CouponsInterestCedit
	 * @see 描述：优惠券抵扣利息(回溯利息)
	 * @author Lisy
	 */
	public BucketType L67_CouponsInterestCedit(CcsAcct acct,CcsPostingTmp txnPost,CcsPlan plan){
		BigDecimal creditAmt = txnPost.getPostAmt();
		// 计划-实际抵扣金额
		plan.setCurrBal(plan.getCurrBal().subtract(creditAmt));
		// 优先抵扣往期利息
		if(plan.getPastInterest().compareTo(creditAmt)>0){
			plan.setPastInterest(plan.getPastInterest().subtract(creditAmt));
		}else{
			plan.setCtdInterest(plan.getCtdInterest().subtract(creditAmt.subtract(plan.getPastInterest())));
			plan.setPastInterest(BigDecimal.ZERO);
		}
		// 计划-当期贷记金额
		plan.setCtdAmtCr(plan.getCtdAmtCr().add(creditAmt));
		// 计划-当期贷记笔数
		plan.setCtdNbrCr(plan.getCtdNbrCr()+1);
				
		// 账户-账户余额
		acct.setCurrBal(acct.getCurrBal().subtract(creditAmt));
		
		// 账户-当期利息金额
		acct.setCtdInterestAmt(acct.getCtdInterestAmt().subtract(creditAmt));
		// 账户-当期利息笔数
		acct.setCtdInterestCnt(acct.getCtdInterestCnt() + 1);
		return BucketType.Interest;
	}
}
