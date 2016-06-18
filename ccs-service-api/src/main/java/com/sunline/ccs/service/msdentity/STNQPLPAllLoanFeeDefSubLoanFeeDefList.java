package com.sunline.ccs.service.msdentity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ccs.param.def.enums.CalcBaseInd;
import com.sunline.ccs.param.def.enums.CalcMethod;
import com.sunline.ccs.param.def.enums.FirstCardFeeInd;
import com.sunline.ccs.param.def.enums.LoanPlanStatus;
import com.sunline.ccs.param.def.enums.PaymentIntervalUnit;
import com.sunline.ccs.param.def.enums.PrepaymentFeeMethod;
import com.sunline.ccs.param.def.enums.TierInd;
import com.sunline.pcm.param.def.enums.MulctMethod;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanFeeMethod;
import com.sunline.ppy.dictionary.enums.LoanMold;
import com.sunline.ppy.dictionary.enums.LoanType;


/**
 * 根据金额、期数获取产品信息接口返回报文
 * 提前还款手续费计算规则列表
 * @author zqx
 *
 */
public class STNQPLPAllLoanFeeDefSubLoanFeeDefList implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	
	/*
	*贷款产品代码
	*/
	@JsonProperty(value="LOAN_CODE")
	public String  loanCode; 
	
	/*
	*贷款子产品代码
	*/
	@JsonProperty(value="LOAN_FEE_DEF_ID")
	public String  loanFeeDefId; 

	/*
	*贷款产品描述
	*/
	@JsonProperty(value="DESC")
	public String  desc; 

	/*
	*贷款类型
	*/
	@JsonProperty(value="LOAN_TYPE")
	public LoanType  loanType; 

	/*
	*中止账龄
	*/
	@JsonProperty(value="TERMINATE_AGE_CD")
	public String  terminateAgeCd; 

	/*
	*产品有效期
	*/
	@JsonProperty(value="LOAN_VALIDITY")
	public String  loanValidity; 

	/*
	*贷款产品状态
	*/
	@JsonProperty(value="LOAN_STATUS")
	public LoanPlanStatus  loanStatus; 

	/*
	*放款类型
	*/
	@JsonProperty(value="LOAN_MOLD")
	public LoanMold  loanMold; 

	/*
	*贷款最短周期
	*/
	@JsonProperty(value="MIN_CYCLE")
	public Integer  minCycle; 

	/*
	*贷款最长周期
	*/
	@JsonProperty(value="MAX_CYCLE")
	public Integer  maxCycle; 

	/*
	*犹豫期
	*/
	@JsonProperty(value="HESITATION_DAYS")
	public Integer  hesitationDays; 

	/*
	*贷款期数
	*/
	@JsonProperty(value="LOAN_TERM")
	public Integer  loanTerm; 

	/*
	*基准利率
	*/
	@JsonProperty(value="INTEREST_RATE")
	public BigDecimal  interestRate; 

	/*
	*罚息利率
	*/
	@JsonProperty(value="PENALTY_INT_RATE")
	public BigDecimal  penaltyIntRate; 

	/*
	*复利利率
	*/
	@JsonProperty(value="COMPOUND_INT_RATE")
	public BigDecimal  compoundIntRate; 

	/*
	*还款间隔单位
	*/
	@JsonProperty(value="PAYMENT_UNIT")
	public PaymentIntervalUnit  paymentUnit; 

	/*
	*还款间隔周期
	*/
	@JsonProperty(value="PAYMENT_PERIOD")
	public Integer  paymentPeriod; 

	/*
	*罚金收取方式
	*/
	@JsonProperty(value="MULCT_METHOD")
	public MulctMethod  mulctMethod; 

	/*
	*罚金计息基准年
	*/
	
	@JsonProperty(value="MULCT_BASE_YEAR")
	public Integer  mulctBaseYear;
	
	/*
	*合作方_罚息利率
	*/
	@JsonProperty(value="COOPER_PENALTY_INT_RATE")
	public BigDecimal  cooperPenaltyIntRate;
	
	/*
	*罚金规则列表
	*/
	@JsonProperty(value="MULCT_RULES_LIST")
	public List<STNQPLSPbyAmtTermRespSubMulctRule>  mulctRulesList = new ArrayList<STNQPLSPbyAmtTermRespSubMulctRule>(); 
	
	/*
	*贷款服务费收取方式
	*/
	@JsonProperty(value="SVC_FEE_CHARGE_MTD")
	public LoanFeeMethod  instlmtFeeChargeMtd; 

	/*
	*贷款服务费计算方式
	*/
	@JsonProperty(value="SVC_FEE_CAL_MTD")
	public PrepaymentFeeMethod  instlmtFeeCalMtd; 


	/*
	*贷款服务费金额
	*/
	@JsonProperty(value="SVC_FEE_AMT")
	public BigDecimal  instlmtFeeAmt; 


	/*
	*贷款服务费费率
	*/
	@JsonProperty(value="SVC_FEE_CAL_RATE")
	public BigDecimal  instlmtFeeRate; 

	/*
	*分期手续费收取方式
	*/
	@JsonProperty(value="INSTLMT_FEE_CHARGE_MTD")
	public LoanFeeMethod  svcFeeChargeMtd; 

	/*
	*分期手续费计算方式
	*/
	@JsonProperty(value="INSTLMT_FEE_CAL_MTD")
	public CalcMethod  svcFeeCalMtd; 

	/*
	*分期手续费金额
	*/
	@JsonProperty(value="INSTLMT_FEE_AMT")
	public BigDecimal  svcFeeAmt; 

	/*
	*分期手续费费率
	*/
	@JsonProperty(value="INSTLMT_FEE_RATE")
	public BigDecimal  svcFeeCalRate; 

	/*
	*提现手续费费率
	*/
	@JsonProperty(value="EXTRACT_FEE_RATE")
	public BigDecimal  extractFeeRate; 

	/*
	*提现手续费固定附加
	*/
	@JsonProperty(value="EXTRACT_FEE_ADDI_AMT")
	public BigDecimal  extractFeeAddiAmt; 

	/*
	*首次年费收取方式
	*/
	@JsonProperty(value="ANNUAL_FEE_CHARGE_MTD")
	public FirstCardFeeInd  annualFeeChargeMtd; 

	/*
	*年费金额
	*/
	@JsonProperty(value="ANNUAL_FEE_AMT")
	public BigDecimal  annualFeeAmt; 

	/*
	*是否允许展期标志
	*/
	@JsonProperty(value="RESCHEDULE_IND")
	public Boolean  rescheduleInd; 

	/*
	*提前还款手续费计算方式
	*/
	@JsonProperty(value="PREPAY_CAL_MTD")
	public PrepaymentFeeMethod  prepayCalMtd; 

	/*
	*是否支持预约提前结清
	*/
	@JsonProperty(value="EARLY_SETTLE_IND")
	public Indicator  earlySettleInd; 

	/*
	*预约提前结清提前天数
	*/
	@JsonProperty(value="EARLY_SETTLE_APP_DAYS")
	public Integer  earlySettleAppDays; 

	/*
	*提前还款手续费计算规则列表
	*/
	@JsonProperty(value="PREPAY_RULES_LIST")
	public List<STNQPLSPbyAmtTermRespSubPrepayRule>  prepayRulesList = new ArrayList<STNQPLSPbyAmtTermRespSubPrepayRule>(); 

	/*
	*是否允许缩期标志
	*/
	@JsonProperty(value="SHORT_SCHED_IND")
	public Boolean  shortSchedInd; 

	/*
	*违约金收取标志
	*/
	@JsonProperty(value="DUE_PENALTY_IND")
	public Indicator  duePenaltyInd; 

	/*
	*违约金单笔最小金额
	*/
	@JsonProperty(value="DUE_PENALTY_MIN_AMT")
	public BigDecimal  duePenaltyMinAmt; 

	/*
	*违约金单笔最大金额
	*/
	@JsonProperty(value="DUE_PENALTY_MAX_AMT")
	public BigDecimal  duePenaltyMaxAmt; 

	/*
	*违约金年累计最大金额
	*/
	@JsonProperty(value="DUE_PENALTY_YEAR_MAX_AMT")
	public BigDecimal  duePenaltyYearMaxAmt; 

	/*
	*违约金年累计最大次数
	*/
	@JsonProperty(value="DUE_PENALTY_YEAR_MAX_CNT")
	public Integer  duePenaltyYearMaxCnt; 

	/*
	*违约金计算基准金额
	*/
	@JsonProperty(value="DUE_PENALTY_BASE")
	public CalcBaseInd  duePenaltyBase; 

	/*
	*违约金计算方式
	*/
	@JsonProperty(value="DUE_PENALTY_CHARGE_MTD")
	public TierInd  duePenaltyChargeMtd; 

	/*
	*违约金对应手续费费率
	*/
	@JsonProperty(value="DUE_PENALTY_RATE")
	public BigDecimal  duePenaltyRate; 

	/*
	*违约金固定附加金额
	*/
	@JsonProperty(value="DUE_PENALTY_ADDI_AMT")
	public BigDecimal  duePenaltyAddiAmt; 
	/*
	*合作方违约金收取标志
	*/
	@JsonProperty(value="COOPER_DUE_PENALTY_IND")
	public Indicator  cooperDuePenaltyInd; 

	/*
	*合作方违约金单笔最小金额
	*/
	@JsonProperty(value="COOPER_DUE_PENALTY_MIN_AMT")
	public BigDecimal  cooperDuePenaltyMinAmt; 

	/*
	*合作方违约金单笔最大金额
	*/
	@JsonProperty(value="COOPER_DUE_PENALTY_MAX_AMT")
	public BigDecimal  cooperDuePenaltyMaxAmt; 

	/*
	*合作方违约金年累计最大金额
	*/
	@JsonProperty(value="COOPER_DUE_PENALTY_YEAR_MAX_AMT")
	public BigDecimal  cooperDuePenaltyYearMaxAmt; 

	/*
	*合作方违约金年累计最大次数
	*/
	@JsonProperty(value="COOPER_DUE_PENALTY_YEAR_MAX_CNT")
	public Integer  cooperDuePenaltyYearMaxCnt; 

	/*
	*合作方违约金计算基准金额
	*/
	@JsonProperty(value="COOPER_DUE_PENALTY_BASE")
	public CalcBaseInd  cooperDuePenaltyBase; 

	/*
	*合作方违约金计算方式
	*/
	@JsonProperty(value="COOPER_DUE_PENALTY_CHARGE_MTD")
	public TierInd  cooperDuePenaltyChargeMtd; 

	/*
	*合作方违约金对应手续费费率
	*/
	@JsonProperty(value="COOPER_DUE_PENALTY_RATE")
	public BigDecimal  cooperDuePenaltyRate; 

	/*
	*合作方违约金固定附加金额
	*/
	@JsonProperty(value="COOPER_DUE_PENALTY_ADDI_AMT")
	public BigDecimal  cooperDuePenaltyAddiAmt; 
	
	/*
	*是否退还趸交费
	*/
	@JsonProperty(value="PREMIUM_RETURN")
	public Indicator  premiumReturn; 

	/*
	*寿险包收取方式
	*/
	@JsonProperty(value="LIFEINSU_FEE_CHARGE_MTD")
	public LoanFeeMethod  lifeinsuFeeChargeMtd; 

	/*
	*寿险包计算方式
	*/
	@JsonProperty(value="LIFEINSU_FEE_CAL_MTD")
	public PrepaymentFeeMethod  lifeinsuFeeCalMtd; 

	/*
	*寿险包费率
	*/
	@JsonProperty(value="LIFEINSU_FEE_RATE")
	public BigDecimal  lifeinsuFeeRate; 

	/*
	*寿险包固定金额
	*/
	@JsonProperty(value="LIFEINSU_FEE_AMT")
	public BigDecimal  lifeinsuFeeAmt; 
	
	/*
	*灵活还款包固定金额
	*PREPAY_PKG_AMT
	*/
	@JsonProperty(value="PREPAY_PKG_AMT")
	public BigDecimal  prepayPkgAmt; 
	/*
	*灵活还款包费率
	*PREPAY_PKG_RATE
	*/
	@JsonProperty(value="PREPAY_PKG_RATE")
	public BigDecimal  prepayPkgRate; 
	
	/*
	*灵活还款包计算方式
	*PREPAY_PKG_CAL_MTD
	*/
	@JsonProperty(value="PREPAY_PKG_CAL_MTD")
	public PrepaymentFeeMethod  prepayPkgCalMtd; 
	
	/*
	*灵活还款包收取方式
	*PREPAY_PKG_CHARGE_MTD
	*/
	@JsonProperty(value="PREPAY_PKG_CHARGE_MTD")
	public LoanFeeMethod  prepayPkgChargeMtd; 
	

	/*
	*印花税收取方式
	*/
	@JsonProperty(value="STAMP_CHARGE_MTD")
	public LoanFeeMethod  stampChargeMtd; 

	/*
	*印花税计算方式
	*/
	@JsonProperty(value="STAMP_CAL_MTD")
	public PrepaymentFeeMethod  stampCalMtd; 

	/*
	*印花税率
	*/
	@JsonProperty(value="STAMP_RATE")
	public BigDecimal  stampRate; 

	/*
	*印花税固定金额
	*/
	@JsonProperty(value="STAMP_AMT")
	public BigDecimal  stampAmt; 

	/*
	*印花税是否冲减利息
	*/
	@JsonProperty(value="STAMP_OFFSET_INT_IND")
	public Indicator  stampOffsetIntInd; 

	/*
	*印花税是否入客户帐
	*/
	@JsonProperty(value="STAMP_CUST_CHARGE_IND")
	public Indicator  stampCustChargeInd;
	
	/*
	 * 最小分期金额
	 */
	@JsonProperty(value="MIN_AMOUNTS")
	public BigDecimal minAmounts;
	
	/*
	 * 最大允许分期金额
	 */
	@JsonProperty(value="MAX_AMOUNTS")
	public BigDecimal maxAmounts;
	
	/*
	 * 代收服务费收取方式
	 * @return
	 */
	@JsonProperty(value="AGENT_FEE_CHARGE_MTD")
	public LoanFeeMethod agentFeeChargeMtd;
	
	/*
	 * 代收服务费计算方式
	 * @return
	 */
	@JsonProperty(value="AGENT_FEE_CAL_MTD")
	public CalcMethod agentFeeCalMtd;
	
	/*
	 * 代收服务费金额
	 * @return
	 */
	@JsonProperty(value="MONTHLY_AGENT_FEE_AMT")
	public BigDecimal monthlyAgentFeeAmt;
	
	/*
	 * 代收服务费费率
	 * @return
	 */
	@JsonProperty(value="MONTHLY_AGENT_FEE_RATE")
	public BigDecimal monthlyAgentFeeRate;
	
	/*
	 * 终端类型列表
	 */
	@JsonProperty(value="SUB_TERMINAL_LIST")
	public List<String> subTerminalList;

	/*
	 * 提现最小金额
	 */
	@JsonProperty(value="WITHDRAW_LOWLIMIT")
	public BigDecimal withDrawLowlimit;
	
	/*
	 * 还款最小金额
	 */
	@JsonProperty(value="REPAY_LOWLIMIT")
	public BigDecimal repayLowlimit;
	/*
	*合作方罚金规则列表
	*/
	@JsonProperty(value="COOPER_MULCT_RULES_LIST")
	public List<STNQPLSPbyAmtTermRespCooperMulctRule>  cooperMulctRulesList = new ArrayList<STNQPLSPbyAmtTermRespCooperMulctRule>(); 
	
	/*
	*合作方提前还款手续费计算规则列表
	*/
	@JsonProperty(value="COOPER_PREPAY_RULES_LIST")
	public List<STNQPLSPbyAmtTermRespCooperSubPrepayRule>  cooperPrepayRulesList = new ArrayList<STNQPLSPbyAmtTermRespCooperSubPrepayRule>(); 
	
	
	
	public List<STNQPLSPbyAmtTermRespCooperMulctRule> getCooperMulctRulesList() {
		return cooperMulctRulesList;
	}

	public void setCooperMulctRulesList(
			List<STNQPLSPbyAmtTermRespCooperMulctRule> cooperMulctRulesList) {
		this.cooperMulctRulesList = cooperMulctRulesList;
	}

	public List<STNQPLSPbyAmtTermRespCooperSubPrepayRule> getCooperPrepayRulesList() {
		return cooperPrepayRulesList;
	}

	public void setCooperPrepayRulesList(
			List<STNQPLSPbyAmtTermRespCooperSubPrepayRule> cooperPrepayRulesList) {
		this.cooperPrepayRulesList = cooperPrepayRulesList;
	}

	public List<STNQPLSPbyAmtTermRespSubMulctRule> getMulctRulesList() {
		return mulctRulesList;
	}

	public void setMulctRulesList(
			List<STNQPLSPbyAmtTermRespSubMulctRule> mulctRulesList) {
		this.mulctRulesList = mulctRulesList;
	}

	public LoanFeeMethod getInstlmtFeeChargeMtd() {
		return instlmtFeeChargeMtd;
	}

	public void setInstlmtFeeChargeMtd(LoanFeeMethod instlmtFeeChargeMtd) {
		this.instlmtFeeChargeMtd = instlmtFeeChargeMtd;
	}

	public PrepaymentFeeMethod getInstlmtFeeCalMtd() {
		return instlmtFeeCalMtd;
	}

	public void setInstlmtFeeCalMtd(PrepaymentFeeMethod instlmtFeeCalMtd) {
		this.instlmtFeeCalMtd = instlmtFeeCalMtd;
	}

	public BigDecimal getInstlmtFeeAmt() {
		return instlmtFeeAmt;
	}

	public void setInstlmtFeeAmt(BigDecimal instlmtFeeAmt) {
		this.instlmtFeeAmt = instlmtFeeAmt;
	}

	public BigDecimal getInstlmtFeeRate() {
		return instlmtFeeRate;
	}

	public void setInstlmtFeeRate(BigDecimal instlmtFeeRate) {
		this.instlmtFeeRate = instlmtFeeRate;
	}

	public LoanFeeMethod getSvcFeeChargeMtd() {
		return svcFeeChargeMtd;
	}

	public void setSvcFeeChargeMtd(LoanFeeMethod svcFeeChargeMtd) {
		this.svcFeeChargeMtd = svcFeeChargeMtd;
	}

	public CalcMethod getSvcFeeCalMtd() {
		return svcFeeCalMtd;
	}

	public void setSvcFeeCalMtd(CalcMethod svcFeeCalMtd) {
		this.svcFeeCalMtd = svcFeeCalMtd;
	}

	public BigDecimal getSvcFeeAmt() {
		return svcFeeAmt;
	}

	public void setSvcFeeAmt(BigDecimal svcFeeAmt) {
		this.svcFeeAmt = svcFeeAmt;
	}

	public BigDecimal getSvcFeeCalRate() {
		return svcFeeCalRate;
	}

	public void setSvcFeeCalRate(BigDecimal svcFeeCalRate) {
		this.svcFeeCalRate = svcFeeCalRate;
	}

	public BigDecimal getExtractFeeRate() {
		return extractFeeRate;
	}

	public void setExtractFeeRate(BigDecimal extractFeeRate) {
		this.extractFeeRate = extractFeeRate;
	}

	public BigDecimal getExtractFeeAddiAmt() {
		return extractFeeAddiAmt;
	}

	public void setExtractFeeAddiAmt(BigDecimal extractFeeAddiAmt) {
		this.extractFeeAddiAmt = extractFeeAddiAmt;
	}

	public FirstCardFeeInd getAnnualFeeChargeMtd() {
		return annualFeeChargeMtd;
	}

	public void setAnnualFeeChargeMtd(FirstCardFeeInd annualFeeChargeMtd) {
		this.annualFeeChargeMtd = annualFeeChargeMtd;
	}

	public BigDecimal getAnnualFeeAmt() {
		return annualFeeAmt;
	}

	public void setAnnualFeeAmt(BigDecimal annualFeeAmt) {
		this.annualFeeAmt = annualFeeAmt;
	}

	public Boolean getRescheduleInd() {
		return rescheduleInd;
	}

	public void setRescheduleInd(Boolean rescheduleInd) {
		this.rescheduleInd = rescheduleInd;
	}

	public PrepaymentFeeMethod getPrepayCalMtd() {
		return prepayCalMtd;
	}

	public void setPrepayCalMtd(PrepaymentFeeMethod prepayCalMtd) {
		this.prepayCalMtd = prepayCalMtd;
	}

	public Indicator getEarlySettleInd() {
		return earlySettleInd;
	}

	public void setEarlySettleInd(Indicator earlySettleInd) {
		this.earlySettleInd = earlySettleInd;
	}

	public Integer getEarlySettleAppDays() {
		return earlySettleAppDays;
	}

	public void setEarlySettleAppDays(Integer earlySettleAppDays) {
		this.earlySettleAppDays = earlySettleAppDays;
	}

	public List<STNQPLSPbyAmtTermRespSubPrepayRule> getPrepayRulesList() {
		return prepayRulesList;
	}

	public void setPrepayRulesList(
			List<STNQPLSPbyAmtTermRespSubPrepayRule> prepayRulesList) {
		this.prepayRulesList = prepayRulesList;
	}

	public Boolean getShortSchedInd() {
		return shortSchedInd;
	}

	public void setShortSchedInd(Boolean shortSchedInd) {
		this.shortSchedInd = shortSchedInd;
	}

	public Indicator getDuePenaltyInd() {
		return duePenaltyInd;
	}

	public void setDuePenaltyInd(Indicator duePenaltyInd) {
		this.duePenaltyInd = duePenaltyInd;
	}

	public BigDecimal getDuePenaltyMinAmt() {
		return duePenaltyMinAmt;
	}

	public void setDuePenaltyMinAmt(BigDecimal duePenaltyMinAmt) {
		this.duePenaltyMinAmt = duePenaltyMinAmt;
	}

	public BigDecimal getDuePenaltyMaxAmt() {
		return duePenaltyMaxAmt;
	}

	public void setDuePenaltyMaxAmt(BigDecimal duePenaltyMaxAmt) {
		this.duePenaltyMaxAmt = duePenaltyMaxAmt;
	}

	public BigDecimal getDuePenaltyYearMaxAmt() {
		return duePenaltyYearMaxAmt;
	}

	public void setDuePenaltyYearMaxAmt(BigDecimal duePenaltyYearMaxAmt) {
		this.duePenaltyYearMaxAmt = duePenaltyYearMaxAmt;
	}

	public Integer getDuePenaltyYearMaxCnt() {
		return duePenaltyYearMaxCnt;
	}

	public void setDuePenaltyYearMaxCnt(Integer duePenaltyYearMaxCnt) {
		this.duePenaltyYearMaxCnt = duePenaltyYearMaxCnt;
	}

	public CalcBaseInd getDuePenaltyBase() {
		return duePenaltyBase;
	}

	public void setDuePenaltyBase(CalcBaseInd duePenaltyBase) {
		this.duePenaltyBase = duePenaltyBase;
	}

	public TierInd getDuePenaltyChargeMtd() {
		return duePenaltyChargeMtd;
	}

	public void setDuePenaltyChargeMtd(TierInd duePenaltyChargeMtd) {
		this.duePenaltyChargeMtd = duePenaltyChargeMtd;
	}

	public BigDecimal getDuePenaltyRate() {
		return duePenaltyRate;
	}

	public void setDuePenaltyRate(BigDecimal duePenaltyRate) {
		this.duePenaltyRate = duePenaltyRate;
	}

	public BigDecimal getDuePenaltyAddiAmt() {
		return duePenaltyAddiAmt;
	}

	public void setDuePenaltyAddiAmt(BigDecimal duePenaltyAddiAmt) {
		this.duePenaltyAddiAmt = duePenaltyAddiAmt;
	}

	public LoanFeeMethod getLifeinsuFeeChargeMtd() {
		return lifeinsuFeeChargeMtd;
	}

	public void setLifeinsuFeeChargeMtd(LoanFeeMethod lifeinsuFeeChargeMtd) {
		this.lifeinsuFeeChargeMtd = lifeinsuFeeChargeMtd;
	}

	public PrepaymentFeeMethod getLifeinsuFeeCalMtd() {
		return lifeinsuFeeCalMtd;
	}

	public void setLifeinsuFeeCalMtd(PrepaymentFeeMethod lifeinsuFeeCalMtd) {
		this.lifeinsuFeeCalMtd = lifeinsuFeeCalMtd;
	}

	public BigDecimal getLifeinsuFeeRate() {
		return lifeinsuFeeRate;
	}

	public void setLifeinsuFeeRate(BigDecimal lifeinsuFeeRate) {
		this.lifeinsuFeeRate = lifeinsuFeeRate;
	}

	public BigDecimal getLifeinsuFeeAmt() {
		return lifeinsuFeeAmt;
	}

	public void setLifeinsuFeeAmt(BigDecimal lifeinsuFeeAmt) {
		this.lifeinsuFeeAmt = lifeinsuFeeAmt;
	}

	public LoanFeeMethod getStampChargeMtd() {
		return stampChargeMtd;
	}

	public void setStampChargeMtd(LoanFeeMethod stampChargeMtd) {
		this.stampChargeMtd = stampChargeMtd;
	}

	public PrepaymentFeeMethod getStampCalMtd() {
		return stampCalMtd;
	}

	public void setStampCalMtd(PrepaymentFeeMethod stampCalMtd) {
		this.stampCalMtd = stampCalMtd;
	}

	public BigDecimal getStampRate() {
		return stampRate;
	}

	public void setStampRate(BigDecimal stampRate) {
		this.stampRate = stampRate;
	}

	public BigDecimal getStampAmt() {
		return stampAmt;
	}

	public void setStampAmt(BigDecimal stampAmt) {
		this.stampAmt = stampAmt;
	}

	public Indicator getStampOffsetIntInd() {
		return stampOffsetIntInd;
	}

	public void setStampOffsetIntInd(Indicator stampOffsetIntInd) {
		this.stampOffsetIntInd = stampOffsetIntInd;
	}

	public Indicator getStampCustChargeInd() {
		return stampCustChargeInd;
	}

	public void setStampCustChargeInd(Indicator stampCustChargeInd) {
		this.stampCustChargeInd = stampCustChargeInd;
	}

	public LoanFeeMethod getAgentFeeChargeMtd() {
		return agentFeeChargeMtd;
	}

	public void setAgentFeeChargeMtd(LoanFeeMethod agentFeeChargeMtd) {
		this.agentFeeChargeMtd = agentFeeChargeMtd;
	}

	public CalcMethod getAgentFeeCalMtd() {
		return agentFeeCalMtd;
	}

	public void setAgentFeeCalMtd(CalcMethod agentFeeCalMtd) {
		this.agentFeeCalMtd = agentFeeCalMtd;
	}

	public BigDecimal getMonthlyAgentFeeAmt() {
		return monthlyAgentFeeAmt;
	}

	public void setMonthlyAgentFeeAmt(BigDecimal monthlyAgentFeeAmt) {
		this.monthlyAgentFeeAmt = monthlyAgentFeeAmt;
	}

	public BigDecimal getMonthlyAgentFeeRate() {
		return monthlyAgentFeeRate;
	}

	public void setMonthlyAgentFeeRate(BigDecimal monthlyAgentFeeRate) {
		this.monthlyAgentFeeRate = monthlyAgentFeeRate;
	}

	public List<String> getSubTerminalList() {
		return subTerminalList;
	}

	public void setSubTerminalList(List<String> subTerminalList) {
		this.subTerminalList = subTerminalList;
	}

	public BigDecimal getWithDrawLowlimit() {
		return withDrawLowlimit;
	}

	public void setWithDrawLowlimit(BigDecimal withDrawLowlimit) {
		this.withDrawLowlimit = withDrawLowlimit;
	}

	public BigDecimal getRepayLowlimit() {
		return repayLowlimit;
	}

	public void setRepayLowlimit(BigDecimal repayLowlimit) {
		this.repayLowlimit = repayLowlimit;
	}

	public String getLoanCode() {
		return loanCode;
	}

	public void setLoanCode(String loanCode) {
		this.loanCode = loanCode;
	}

	public String getLoanFeeDefId() {
		return loanFeeDefId;
	}

	public void setLoanFeeDefId(String loanFeeDefId) {
		this.loanFeeDefId = loanFeeDefId;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public LoanType getLoanType() {
		return loanType;
	}

	public void setLoanType(LoanType loanType) {
		this.loanType = loanType;
	}

	public String getTerminateAgeCd() {
		return terminateAgeCd;
	}

	public void setTerminateAgeCd(String terminateAgeCd) {
		this.terminateAgeCd = terminateAgeCd;
	}

	public String getLoanValidity() {
		return loanValidity;
	}

	public void setLoanValidity(String loanValidity) {
		this.loanValidity = loanValidity;
	}

	public LoanPlanStatus getLoanStatus() {
		return loanStatus;
	}

	public void setLoanStatus(LoanPlanStatus loanStatus) {
		this.loanStatus = loanStatus;
	}

	public LoanMold getLoanMold() {
		return loanMold;
	}

	public void setLoanMold(LoanMold loanMold) {
		this.loanMold = loanMold;
	}

	public Integer getMinCycle() {
		return minCycle;
	}

	public void setMinCycle(Integer minCycle) {
		this.minCycle = minCycle;
	}

	public Integer getMaxCycle() {
		return maxCycle;
	}

	public void setMaxCycle(Integer maxCycle) {
		this.maxCycle = maxCycle;
	}

	public Integer getHesitationDays() {
		return hesitationDays;
	}

	public void setHesitationDays(Integer hesitationDays) {
		this.hesitationDays = hesitationDays;
	}

	public Integer getLoanTerm() {
		return loanTerm;
	}

	public void setLoanTerm(Integer loanTerm) {
		this.loanTerm = loanTerm;
	}

	public BigDecimal getInterestRate() {
		return interestRate;
	}

	public void setInterestRate(BigDecimal interestRate) {
		this.interestRate = interestRate;
	}

	public BigDecimal getPenaltyIntRate() {
		return penaltyIntRate;
	}

	public void setPenaltyIntRate(BigDecimal penaltyIntRate) {
		this.penaltyIntRate = penaltyIntRate;
	}

	public BigDecimal getCompoundIntRate() {
		return compoundIntRate;
	}

	public void setCompoundIntRate(BigDecimal compoundIntRate) {
		this.compoundIntRate = compoundIntRate;
	}

	public PaymentIntervalUnit getPaymentUnit() {
		return paymentUnit;
	}

	public void setPaymentUnit(PaymentIntervalUnit paymentUnit) {
		this.paymentUnit = paymentUnit;
	}

	public Integer getPaymentPeriod() {
		return paymentPeriod;
	}

	public void setPaymentPeriod(Integer paymentPeriod) {
		this.paymentPeriod = paymentPeriod;
	}

	public MulctMethod getMulctMethod() {
		return mulctMethod;
	}

	public void setMulctMethod(MulctMethod mulctMethod) {
		this.mulctMethod = mulctMethod;
	}

	public Integer getMulctBaseYear() {
		return mulctBaseYear;
	}

	public void setMulctBaseYear(Integer mulctBaseYear) {
		this.mulctBaseYear = mulctBaseYear;
	}

	public Indicator getCooperDuePenaltyInd() {
		return cooperDuePenaltyInd;
	}

	public void setCooperDuePenaltyInd(Indicator cooperDuePenaltyInd) {
		this.cooperDuePenaltyInd = cooperDuePenaltyInd;
	}

	public BigDecimal getCooperDuePenaltyMinAmt() {
		return cooperDuePenaltyMinAmt;
	}

	public void setCooperDuePenaltyMinAmt(BigDecimal cooperDuePenaltyMinAmt) {
		this.cooperDuePenaltyMinAmt = cooperDuePenaltyMinAmt;
	}

	public BigDecimal getCooperDuePenaltyMaxAmt() {
		return cooperDuePenaltyMaxAmt;
	}

	public void setCooperDuePenaltyMaxAmt(BigDecimal cooperDuePenaltyMaxAmt) {
		this.cooperDuePenaltyMaxAmt = cooperDuePenaltyMaxAmt;
	}

	public BigDecimal getCooperDuePenaltyYearMaxAmt() {
		return cooperDuePenaltyYearMaxAmt;
	}

	public void setCooperDuePenaltyYearMaxAmt(BigDecimal cooperDuePenaltyYearMaxAmt) {
		this.cooperDuePenaltyYearMaxAmt = cooperDuePenaltyYearMaxAmt;
	}

	public Integer getCooperDuePenaltyYearMaxCnt() {
		return cooperDuePenaltyYearMaxCnt;
	}

	public void setCooperDuePenaltyYearMaxCnt(Integer cooperDuePenaltyYearMaxCnt) {
		this.cooperDuePenaltyYearMaxCnt = cooperDuePenaltyYearMaxCnt;
	}

	public CalcBaseInd getCooperDuePenaltyBase() {
		return cooperDuePenaltyBase;
	}

	public void setCooperDuePenaltyBase(CalcBaseInd cooperDuePenaltyBase) {
		this.cooperDuePenaltyBase = cooperDuePenaltyBase;
	}

	public TierInd getCooperDuePenaltyChargeMtd() {
		return cooperDuePenaltyChargeMtd;
	}

	public void setCooperDuePenaltyChargeMtd(TierInd cooperDuePenaltyChargeMtd) {
		this.cooperDuePenaltyChargeMtd = cooperDuePenaltyChargeMtd;
	}

	public BigDecimal getCooperDuePenaltyRate() {
		return cooperDuePenaltyRate;
	}

	public void setCooperDuePenaltyRate(BigDecimal cooperDuePenaltyRate) {
		this.cooperDuePenaltyRate = cooperDuePenaltyRate;
	}

	public BigDecimal getCooperDuePenaltyAddiAmt() {
		return cooperDuePenaltyAddiAmt;
	}

	public void setCooperDuePenaltyAddiAmt(BigDecimal cooperDuePenaltyAddiAmt) {
		this.cooperDuePenaltyAddiAmt = cooperDuePenaltyAddiAmt;
	}

	public Indicator getPremiumReturn() {
		return premiumReturn;
	}

	public void setPremiumReturn(Indicator premiumReturn) {
		this.premiumReturn = premiumReturn;
	}

	public BigDecimal getCooperPenaltyIntRate() {
		return cooperPenaltyIntRate;
	}

	public void setCooperPenaltyIntRate(BigDecimal cooperPenaltyIntRate) {
		this.cooperPenaltyIntRate = cooperPenaltyIntRate;
	}

	public BigDecimal getMinAmounts() {
		return minAmounts;
	}

	public void setMinAmounts(BigDecimal minAmounts) {
		this.minAmounts = minAmounts;
	}

	public BigDecimal getMaxAmounts() {
		return maxAmounts;
	}

	public void setMaxAmounts(BigDecimal maxAmounts) {
		this.maxAmounts = maxAmounts;
	}

	public BigDecimal getPrepayPkgAmt() {
		return prepayPkgAmt;
	}

	public void setPrepayPkgAmt(BigDecimal prepayPkgAmt) {
		this.prepayPkgAmt = prepayPkgAmt;
	}

	public BigDecimal getPrepayPkgRate() {
		return prepayPkgRate;
	}

	public void setPrepayPkgRate(BigDecimal prepayPkgRate) {
		this.prepayPkgRate = prepayPkgRate;
	}

	public PrepaymentFeeMethod getPrepayPkgCalMtd() {
		return prepayPkgCalMtd;
	}

	public void setPrepayPkgCalMtd(PrepaymentFeeMethod prepayPkgCalMtd) {
		this.prepayPkgCalMtd = prepayPkgCalMtd;
	}

	public LoanFeeMethod getPrepayPkgChargeMtd() {
		return prepayPkgChargeMtd;
	}

	public void setPrepayPkgChargeMtd(LoanFeeMethod prepayPkgChargeMtd) {
		this.prepayPkgChargeMtd = prepayPkgChargeMtd;
	}

	
	
	

}
