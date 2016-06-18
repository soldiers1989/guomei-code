package com.sunline.ccs.service.auth.context;

import java.math.BigDecimal;

import com.sunline.ppy.dictionary.enums.LoanFeeMethod;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ccs.param.def.LoanMerchant;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.param.def.Program;
import com.sunline.ccs.param.def.ProgramFeeDef;
/**
 * 
 * @see 类名：LoanInfo
 * @see 描述：授权分期
 *
 * @see 创建日期：   2015年6月24日下午3:17:22
 * @author liruilin
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class LoanInfo {
	// 分期期数
	private int loanInitTerm;
	// 项目编号
	private String programId;
	// 分期手续费收取方式
	private LoanFeeMethod loanFeeMethod;
	// 商户号（42域）
	private String merchantId;
	// 分期活动参数
	private Program program;
	// 分期商户信息
	private LoanMerchant loanMerchant;
	// 分期类型
	private LoanType loanType;
	// 分期活动计价方式
	private ProgramFeeDef programFeeDef;
	// 交易参考号
	private String refNbr;
	// 分期注册表信息
	private CcsLoanReg ccsLoanReg;

	private String b007TxnTime;
	private String b011Trace;
	private String b032AcqInst;
	private String b033FwdIns;
	private String authCode;
	
	/**
	 * 根据上送报文的小额贷编号获取loanPlan
	 */
	private String loanCode;
	
	/**
	 * 分期计划参数
	 */
	private LoanPlan loanPlan;
	
	
	/**
	 * 分期计划计价方式
	 */
	private LoanFeeDef loanFeeDef; 
	/**
	 * 浮动比例
	 */
	private BigDecimal floatRate;
	
	public String getLoanCode() {
		return loanCode;
	}

	public void setLoanCode(String loanCode) {
		this.loanCode = loanCode;
	}

	public LoanPlan getLoanPlan() {
		return loanPlan;
	}

	public void setLoanPlan(LoanPlan loanPlan) {
		this.loanPlan = loanPlan;
	}

	public LoanFeeDef getLoanFeeDef() {
		return loanFeeDef;
	}

	public void setLoanFeeDef(LoanFeeDef loanFeeDef) {
		this.loanFeeDef = loanFeeDef;
	}

	/**
	 * 浮动比例
	 */
	public BigDecimal getFloatRate() {
		return floatRate;
	}

	/**
	 * 浮动比例
	 */
	public void setFloatRate(BigDecimal floatRate) {
		this.floatRate = floatRate;
	}

	public String getB007TxnTime() {
		return b007TxnTime;
	}

	public void setB007TxnTime(String b007TxnTime) {
		this.b007TxnTime = b007TxnTime;
	}

	public String getB011Trace() {
		return b011Trace;
	}

	public void setB011Trace(String b011Trace) {
		this.b011Trace = b011Trace;
	}

	public String getB032AcqInst() {
		return b032AcqInst;
	}

	public void setB032AcqInst(String b032AcqInst) {
		this.b032AcqInst = b032AcqInst;
	}

	public String getB033FwdIns() {
		return b033FwdIns;
	}

	public void setB033FwdIns(String b033FwdIns) {
		this.b033FwdIns = b033FwdIns;
	}

	public String getAuthCode() {
		return authCode;
	}

	public void setAuthCode(String authCode) {
		this.authCode = authCode;
	}

	/**
	 * 交易参考号
	 * 
	 * @return
	 */
	public String getRefNbr() {
		return refNbr;
	}

	/**
	 * 交易参考号
	 * 
	 * @param refNbr
	 */
	public void setRefNbr(String refNbr) {
		this.refNbr = refNbr;
	}

	/**
	 * 分期注册表信息
	 * 
	 * @return
	 */
	public CcsLoanReg getCcsLoanReg() {
		return ccsLoanReg;
	}

	/**
	 * 分期注册表信息
	 * 
	 * @param ccsLoanReg
	 */
	public void setCcsLoanReg(CcsLoanReg ccsLoanReg) {
		this.ccsLoanReg = ccsLoanReg;
	}

	/**
	 * 分期类型
	 * 
	 * @return
	 */
	public LoanType getLoanType() {
		return loanType;
	}

	/**
	 * 分期类型
	 * 
	 * @param loanType
	 */
	public void setLoanType(LoanType loanType) {
		this.loanType = loanType;
	}

	/**
	 * 分期活动计价方式
	 * 
	 * @return
	 */
	public ProgramFeeDef getProgramFeeDef() {
		return programFeeDef;
	}

	/**
	 * 分期活动计价方式
	 * 
	 * @param programFeeDef
	 */
	public void setProgramFeeDef(ProgramFeeDef programFeeDef) {
		this.programFeeDef = programFeeDef;
	}

	/**
	 * 分期活动计划信息
	 * 
	 * @return
	 */
	public Program getProgram() {
		return program;
	}

	/**
	 * 分期活动计划信息
	 * 
	 * @param program
	 */
	public void setProgram(Program program) {
		this.program = program;
	}

	/**
	 * 分期商户信息
	 * 
	 * @return
	 */
	public LoanMerchant getLoanMerchant() {
		return loanMerchant;
	}

	/**
	 * 分期商户信息
	 * 
	 * @param loanMerchant
	 */
	public void setLoanMerchant(LoanMerchant loanMerchant) {
		this.loanMerchant = loanMerchant;
	}

	/**
	 * 分期期数
	 */
	public int getLoanInitTerm() {
		return loanInitTerm;
	}

	/**
	 * 分期期数
	 */
	public void setLoanInitTerm(int loanInitTerm) {
		this.loanInitTerm = loanInitTerm;
	}

	/**
	 * 项目编号
	 */
	public String getProgramId() {
		return programId;
	}

	/**
	 * 项目编号
	 */
	public void setProgramId(String programId) {
		this.programId = programId;
	}

	/**
	 * 报文中上送的分期手续费收取方式<br>
	 * 手续费收取方式：商户不允许自定义收取方式时，使用商户覆盖报文中上送的收取方式
	 * 
	 * @return
	 */
	public LoanFeeMethod getLoanFeeMethod() {
		return loanFeeMethod;
	}

	/**
	 * 报文中上送的分期手续费收取方式<br>
	 * 手续费收取方式：商户不允许自定义收取方式时，使用商户覆盖报文中上送的收取方式
	 * 
	 * @param loanFeeMethod
	 */
	public void setLoanFeeMethod(LoanFeeMethod loanFeeMethod) {
		this.loanFeeMethod = loanFeeMethod;
	}

	/**
	 * 商户号（42域）
	 */
	public String getMerchantId() {
		return merchantId;
	}

	/**
	 * 商户号（42域）
	 */
	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}

}
