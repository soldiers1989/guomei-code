package com.sunline.ccs.loan;

import java.math.BigDecimal;

public class TrialResp {

	/**
	 * 结清总金额
	 */
	BigDecimal totalAMT;
	/**
	 * 当期本金
	 */
	BigDecimal ctdPricinpalAMT;
	/**
	 * 当期利息
	 */
	BigDecimal ctdInterestAMT;
	/**
	 * 当期保费
	 */
	BigDecimal ctdInsuranceAMT;
	/**
	 * 当期罚金
	 */
	BigDecimal ctdMulctAMT;
	/**
	 * 当期手续费
	 */
	BigDecimal ctdInitFee;
	/**
	 * 当期印花税费
	 */
	BigDecimal ctdStampdutyAMT;
	/**
	 * 当期寿险计划包费
	 */
	BigDecimal ctdLifeInsuFeeAMT;
	/**
	 * 当期服务费
	 */
	BigDecimal ctdLoanTermFeeAMT;
	/**
	 * 当前代收服务费
	 */
	BigDecimal ctdReplaceSvcAMT;
	/**
	 * 当期分期手续费
	 */
	BigDecimal ctdLoanTermSvc;
	/**
	 * 往期欠款本金
	 */
	BigDecimal pastPricinpalAMT;
	/**
	 * 往期欠款利息
	 */
	BigDecimal pastInterestAMT;
	/**
	 * 往期欠款保费
	 */
	BigDecimal pastInsuranceAMT;
	/**
	 * 往期欠款罚金
	 */
	BigDecimal pastMulctAMT;
	/**
	 * 往期欠款手续费
	 */
	BigDecimal pastInitFee;
	/**
	 * 往期欠款印花税费
	 */
	BigDecimal pastStampdutyAMT;
	/**
	 * 往期欠款寿险计划包费
	 */
	BigDecimal pastLifeInsuFeeAMT;
	/**
	 * 往期欠款服务费
	 */
	BigDecimal pastLoanTermFeeAMT;
	/**
	 * 往期代收服务费
	 */
	BigDecimal pastReplaceSvcAMT;
	/**
	 * 往期分期手续费
	 */
	BigDecimal pastLoanTermSvc;
	
	/**
	 * 趸交费
	 */
	BigDecimal premiumAmt;
	/**
	 * 溢缴款
	 */
	BigDecimal deposit;
	/**
	 * 未匹配金额
	 */
	BigDecimal memoAmt;
	/**
	 * 代收罚息
	 */
	BigDecimal replacePenalty;
	/**
	 * 代收罚金
	 */
	BigDecimal replaceMulct;
	/**
	 * 代收提前还款手续费
	 */
	BigDecimal replacePrepayFee;
	/**
	 * 代收滞纳金
	 */
	BigDecimal replaceLpc;
	/**
	 * 当期灵活还款计划包
	 */
	BigDecimal ctdMonthPrepayPkg;
	/**
	 * 往期灵活还款计划包
	 */
	BigDecimal pastPrepayPkg;
	
	public TrialResp(){
		this.totalAMT = BigDecimal.ZERO;
		this.ctdPricinpalAMT = BigDecimal.ZERO;
		this.ctdInterestAMT = BigDecimal.ZERO;
		this.ctdInsuranceAMT = BigDecimal.ZERO;
		this.ctdInitFee = BigDecimal.ZERO;
		this.ctdMulctAMT = BigDecimal.ZERO;
		this.ctdStampdutyAMT = BigDecimal.ZERO;
		this.ctdLifeInsuFeeAMT = BigDecimal.ZERO;
		this.ctdLoanTermFeeAMT = BigDecimal.ZERO;
		this.ctdReplaceSvcAMT = BigDecimal.ZERO;
		this.ctdLoanTermSvc = BigDecimal.ZERO;
		this.pastPricinpalAMT = BigDecimal.ZERO;
		this.pastInterestAMT = BigDecimal.ZERO;
		this.pastInsuranceAMT = BigDecimal.ZERO;
		this.pastInitFee = BigDecimal.ZERO;
		this.pastMulctAMT = BigDecimal.ZERO;
		this.pastStampdutyAMT = BigDecimal.ZERO;
		this.pastLifeInsuFeeAMT = BigDecimal.ZERO;
		this.pastLoanTermFeeAMT = BigDecimal.ZERO;
		this.pastReplaceSvcAMT = BigDecimal.ZERO;
		this.pastLoanTermSvc = BigDecimal.ZERO;
		this.replaceLpc = BigDecimal.ZERO;
		this.replacePrepayFee = BigDecimal.ZERO;
		this.replaceMulct = BigDecimal.ZERO;
		this.replacePenalty = BigDecimal.ZERO;
		this.premiumAmt = BigDecimal.ZERO;
		this.deposit = BigDecimal.ZERO;
		this.memoAmt = BigDecimal.ZERO;
		this.ctdMonthPrepayPkg = BigDecimal.ZERO;
		this.pastPrepayPkg = BigDecimal.ZERO;
	}


	public BigDecimal getCtdLoanTermSvc() {
		return ctdLoanTermSvc;
	}


	public void setCtdLoanTermSvc(BigDecimal ctdLoanTermSvc) {
		this.ctdLoanTermSvc = ctdLoanTermSvc;
	}


	public BigDecimal getPastLoanTermSvc() {
		return pastLoanTermSvc;
	}


	public void setPastLoanTermSvc(BigDecimal pastLoanTermSvc) {
		this.pastLoanTermSvc = pastLoanTermSvc;
	}
	
	public BigDecimal getTotalAMT() {
		return totalAMT;
	}


	public void setTotalAMT(BigDecimal totalAMT) {
		this.totalAMT = totalAMT;
	}




	public BigDecimal getCtdPricinpalAMT() {
		return ctdPricinpalAMT;
	}


	public void setCtdPricinpalAMT(BigDecimal ctdPricinpalAMT) {
		this.ctdPricinpalAMT = ctdPricinpalAMT;
	}


	public BigDecimal getCtdInterestAMT() {
		return ctdInterestAMT;
	}


	public void setCtdInterestAMT(BigDecimal ctdInterestAMT) {
		this.ctdInterestAMT = ctdInterestAMT;
	}


	public BigDecimal getCtdInsuranceAMT() {
		return ctdInsuranceAMT;
	}


	public void setCtdInsuranceAMT(BigDecimal ctdInsuranceAMT) {
		this.ctdInsuranceAMT = ctdInsuranceAMT;
	}


	public BigDecimal getCtdMulctAMT() {
		return ctdMulctAMT;
	}


	public void setCtdMulctAMT(BigDecimal ctdMulctAMT) {
		this.ctdMulctAMT = ctdMulctAMT;
	}


	public BigDecimal getCtdInitFee() {
		return ctdInitFee;
	}


	public void setCtdInitFee(BigDecimal ctdInitFee) {
		this.ctdInitFee = ctdInitFee;
	}


	public BigDecimal getCtdStampdutyAMT() {
		return ctdStampdutyAMT;
	}


	public void setCtdStampdutyAMT(BigDecimal ctdStampdutyAMT) {
		this.ctdStampdutyAMT = ctdStampdutyAMT;
	}


	public BigDecimal getCtdLifeInsuFeeAMT() {
		return ctdLifeInsuFeeAMT;
	}


	public void setCtdLifeInsuFeeAMT(BigDecimal ctdLifeInsuFeeAMT) {
		this.ctdLifeInsuFeeAMT = ctdLifeInsuFeeAMT;
	}


	public BigDecimal getPastPricinpalAMT() {
		return pastPricinpalAMT;
	}


	public void setPastPricinpalAMT(BigDecimal pastPricinpalAMT) {
		this.pastPricinpalAMT = pastPricinpalAMT;
	}


	public BigDecimal getPastInterestAMT() {
		return pastInterestAMT;
	}


	public void setPastInterestAMT(BigDecimal pastInterestAMT) {
		this.pastInterestAMT = pastInterestAMT;
	}


	public BigDecimal getPastInsuranceAMT() {
		return pastInsuranceAMT;
	}


	public void setPastInsuranceAMT(BigDecimal pastInsuranceAMT) {
		this.pastInsuranceAMT = pastInsuranceAMT;
	}


	public BigDecimal getPastMulctAMT() {
		return pastMulctAMT;
	}


	public void setPastMulctAMT(BigDecimal pastMulctAMT) {
		this.pastMulctAMT = pastMulctAMT;
	}


	public BigDecimal getPastInitFee() {
		return pastInitFee;
	}


	public void setPastInitFee(BigDecimal pastInitFee) {
		this.pastInitFee = pastInitFee;
	}


	public BigDecimal getPastStampdutyAMT() {
		return pastStampdutyAMT;
	}


	public void setPastStampdutyAMT(BigDecimal pastStampdutyAMT) {
		this.pastStampdutyAMT = pastStampdutyAMT;
	}


	public BigDecimal getPastLifeInsuFeeAMT() {
		return pastLifeInsuFeeAMT;
	}


	public void setPastLifeInsuFeeAMT(BigDecimal pastLifeInsuFeeAMT) {
		this.pastLifeInsuFeeAMT = pastLifeInsuFeeAMT;
	}


	public BigDecimal getCtdLoanTermFeeAMT() {
		return ctdLoanTermFeeAMT;
	}


	public void setCtdLoanTermFeeAMT(BigDecimal ctdLoanTermFeeAMT) {
		this.ctdLoanTermFeeAMT = ctdLoanTermFeeAMT;
	}


	public BigDecimal getPastLoanTermFeeAMT() {
		return pastLoanTermFeeAMT;
	}


	public void setPastLoanTermFeeAMT(BigDecimal pastLoanTermFeeAMT) {
		this.pastLoanTermFeeAMT = pastLoanTermFeeAMT;
	}
	
	public BigDecimal getCtdReplaceSvcAMT() {
		return ctdReplaceSvcAMT;
	}


	public void setCtdReplaceSvcAMT(BigDecimal ctdReplaceSvcAMT) {
		this.ctdReplaceSvcAMT = ctdReplaceSvcAMT;
	}


	public BigDecimal getPastReplaceSvcAMT() {
		return pastReplaceSvcAMT;
	}


	public void setPastReplaceSvcAMT(BigDecimal pastReplaceSvcAMT) {
		this.pastReplaceSvcAMT = pastReplaceSvcAMT;
	}
	
	public BigDecimal getPremiumAmt() {
		return premiumAmt;
	}


	public void setPremiumAmt(BigDecimal premiumAmt) {
		this.premiumAmt = premiumAmt;
	}


	public BigDecimal getDeposit() {
		return deposit;
	}


	public void setDeposit(BigDecimal deposit) {
		this.deposit = deposit;
	}


	public BigDecimal getMemoAmt() {
		return memoAmt;
	}


	public void setMemoAmt(BigDecimal memoAmt) {
		this.memoAmt = memoAmt;
	}


	public BigDecimal getReplacePenalty() {
		return replacePenalty;
	}


	public void setReplacePenalty(BigDecimal replacePenalty) {
		this.replacePenalty = replacePenalty;
	}


	public BigDecimal getReplaceMulct() {
		return replaceMulct;
	}


	public void setReplaceMulct(BigDecimal replaceMulct) {
		this.replaceMulct = replaceMulct;
	}


	public BigDecimal getReplacePrepayFee() {
		return replacePrepayFee;
	}


	public void setReplacePrepayFee(BigDecimal replacePrepayFee) {
		this.replacePrepayFee = replacePrepayFee;
	}


	public BigDecimal getReplaceLpc() {
		return replaceLpc;
	}


	public void setReplaceLpc(BigDecimal replaceLpc) {
		this.replaceLpc = replaceLpc;
	}

	public BigDecimal getCtdMonthPrepayPkg() {
		return ctdMonthPrepayPkg;
	}


	public void setCtdMonthPrepayPkg(BigDecimal ctdMonthPrepayPkg) {
		this.ctdMonthPrepayPkg = ctdMonthPrepayPkg;
	}


	public BigDecimal getPastPrepayPkg() {
		return pastPrepayPkg;
	}


	public void setPastPrepayPkg(BigDecimal pastPrepayPkg) {
		this.pastPrepayPkg = pastPrepayPkg;
	}
}
