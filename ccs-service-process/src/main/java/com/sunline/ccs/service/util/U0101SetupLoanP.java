package com.sunline.ccs.service.util;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsPostingTmp;
import com.sunline.ccs.loan.AuthLoanProvideImpl;
import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.ccs.param.def.LoanMerchant;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.Program;
import com.sunline.ccs.param.def.ProgramFeeDef;
import com.sunline.ccs.param.def.enums.CtrlListInd;
import com.sunline.ccs.param.def.enums.ProgramStatus;
import com.sunline.ccs.param.def.enums.RecStatus;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanFeeMethod;
import com.sunline.ppy.dictionary.enums.LoanRegStatus;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.PostingFlag;


/**
 * @see 类名：U0101SetupLoanP
 * @see 描述：指定POS分期开卡校验工具类
 *
 * @see 创建日期：   2015-6-23下午7:16:43
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Component
public class U0101SetupLoanP {

	@Autowired
	private UnifiedParameterFacility parameterFacility;
	

	/**
	 * @see 方法名：generateLoanReg 
	 * @see 描述：生成分期注册信息
	 * @see 创建日期：2015-6-23下午7:14:32
	 * @author ChengChun
	 *  
	 * @param productCredit
	 * @param acct
	 * @param txnPost
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public LoanRegInfo generateLoanReg(ProductCredit productCredit, CcsAcct acct, CcsPostingTmp txnPost) {
		LoanRegInfo loanRegInfo = new LoanRegInfo();
		
		// 获取产品定义参数
		Integer loanInitTerm = productCredit.setupLoanPInitTerm;
		String programId = productCredit.setupLoanPProgramId;
		String merchantId = productCredit.setupLoanPMerchantNo;
		LoanFeeMethod loanFeeMethod = productCredit.setupLoanPFeeMethod;
		
		// 获取分期活动及计价参数
		Program program = parameterFacility.retrieveParameterObject(programId, Program.class);
		if(program == null){
			loanRegInfo.setResult(PostingFlag.F70);
			return loanRegInfo;
		}
		ProgramFeeDef programFeeDef = program.programFeeDef.get(loanInitTerm);
		if(programFeeDef == null){
			loanRegInfo.setResult(PostingFlag.F71);
			return loanRegInfo;
		}
		
		// 账户检查
		PostingFlag checkAccountResult = checkAccount(productCredit, acct);
		if(checkAccountResult!=null){
			loanRegInfo.setResult(checkAccountResult);
			return loanRegInfo;
		}
		
		// 商户检查
		LoanMerchant loanMerchant = parameterFacility.retrieveParameterObject(merchantId, LoanMerchant.class);
		PostingFlag checkMerchantResult = checkMerchant(loanMerchant);
		if(checkMerchantResult!=null){
			loanRegInfo.setResult(checkMerchantResult);
			return loanRegInfo;
		}
		
		// 交易检查
		PostingFlag checkAuthLoanResult = checkAuthLoan(program, programFeeDef, loanMerchant, acct, txnPost);
		if(checkAuthLoanResult!=null){
			loanRegInfo.setResult(checkAuthLoanResult);
			return loanRegInfo;
		}
		
		// 手续费收取方式默认取上送，未送取参数
		if(loanFeeMethod == null){
			loanFeeMethod = programFeeDef.loanFeeMethod;
		}
		
		AuthLoanProvideImpl authLoanProvideImple = new AuthLoanProvideImpl(LoanType.P, programFeeDef, loanFeeMethod);
		CcsLoanReg loanReg = authLoanProvideImple.genLoanReg(productCredit.setupLoanPInitTerm, txnPost.getTxnAmt(), txnPost.getRefNbr(), 
				txnPost.getCardNbr(), txnPost.getCardNbr(), loanFeeMethod, acct.getAcctNbr(), acct.getAcctType(), program.loanPlanId, new Date());
		
		//将分期注册状态设置为P|指定POS开卡转分期
		loanReg.setLoanRegStatus(LoanRegStatus.P);
		//原始交易
		loanReg.setOrigTxnAmt(txnPost.getTxnAmt());
		loanReg.setOrigTransDate(txnPost.getTxnTime());
		loanReg.setOrigAuthCode(txnPost.getAuthCode());
		
		loanRegInfo.setTmLoanReg(loanReg);
		
		// TODO 暂不发短信
		
		return loanRegInfo;
	}
	
	/**
	 * @see 方法名：checkAccount 
	 * @see 描述：账户验证
	 * @see 创建日期：2015-6-23下午7:15:03
	 * @author ChengChun
	 *  
	 * @param productCredit
	 * @param acct
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private PostingFlag checkAccount(ProductCredit productCredit, CcsAcct acct) {
		
		// 额度内分期比例检查，暂时使用额度为分期金额，暂时判断>1
		AccountAttribute acctAttr = parameterFacility.loadParameter(productCredit.accountAttributeId, AccountAttribute.class);
		if(acctAttr.loanLimitRate.compareTo(BigDecimal.ONE) < 0){
			return PostingFlag.F87;
		}
		
		return null;
	}
	
	
	/**
	 * @see 方法名：checkMerchant 
	 * @see 描述：商户验证
	 * @see 创建日期：2015-6-23下午7:15:28
	 * @author ChengChun
	 *  
	 * @param loanMerchant
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private PostingFlag checkMerchant(LoanMerchant loanMerchant) {
		
		// 商户检查
		if(loanMerchant == null){
			return PostingFlag.F72;
		}
		
		// 商户状态检查
		if(loanMerchant.recStatus == RecStatus.P){
			return PostingFlag.F73;
		}
		
		// 商户分期支持检查
		if(loanMerchant.posLoanSupportInd == Indicator.N){
			return PostingFlag.F74;
		}
		
		return null; 
	}
	
	
	/**
	 * @see 方法名：checkAuthLoan 
	 * @see 描述：交易验证
	 * @see 创建日期：2015-6-23下午7:16:04
	 * @author ChengChun
	 *  
	 * @param program
	 * @param programFeeDef
	 * @param loanMerchant
	 * @param acct
	 * @param txnPost
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private PostingFlag checkAuthLoan(Program program, ProgramFeeDef programFeeDef, LoanMerchant loanMerchant, CcsAcct acct, CcsPostingTmp txnPost) {
		
		// 分期活动状态检查
		if(program.programStatus!=ProgramStatus.A){
			return PostingFlag.F75;
		}
		
		// 分期活动有效期检查
		if(DateUtils.truncatedCompareTo(new Date(), program.programStartDate, Calendar.DATE)<0
				|| DateUtils.truncatedCompareTo(new Date(), program.programEndDate, Calendar.DATE)>0){
			return PostingFlag.F76;
		}
		
		// 发卡分行支持检查
		if((program.ctrlBranchInd==CtrlListInd.O && !program.ctrlBranchList.contains(acct.getOwningBranch()))
				|| (program.ctrlBranchInd==CtrlListInd.A && program.ctrlBranchList.contains(acct.getOwningBranch()))){
			return PostingFlag.F77;
		}
		
		// 卡产品支持检查
		if((program.ctrlProdCreditInd==CtrlListInd.O && !program.ctrlProdCreditList.contains(acct.getProductCd()))
				|| (program.ctrlProdCreditInd==CtrlListInd.A && program.ctrlProdCreditList.contains(acct.getProductCd()))){
			return PostingFlag.F78;
		}
		
		// 商户支持检查
		if(!program.programMerList.contains(loanMerchant.merId)){
			return PostingFlag.F79;
		}
		
		// MCC支持检查
		if((program.ctrlMccInd==CtrlListInd.O && !program.ctrlMccList.contains(txnPost.getMcc()))
				|| (program.ctrlMccInd==CtrlListInd.A && program.ctrlMccList.contains(txnPost.getMcc()))){
			return PostingFlag.F80;
		}
		
		// 交易金额检查
		BigDecimal amt = txnPost.getTxnAmt();
		if(amt.compareTo(program.programMaxAmount)>0){
			return PostingFlag.F81;
		}
		if(amt.compareTo(program.programMinAmount)<0){
			return PostingFlag.F82;
		}
		if(amt.compareTo(programFeeDef.maxAmount)>0){
			return PostingFlag.F83;
		} 
		if(amt.compareTo(programFeeDef.minAmount)<0){
			return PostingFlag.F84;
		}
		if(amt.compareTo(loanMerchant.posLoanSingleAmtMax)>0){
			return PostingFlag.F85;
		} 
		if(amt.compareTo(loanMerchant.posLoanSingleAmtMin)<0){
			return PostingFlag.F86;
		}
		
		return null;
	}

	
	public class LoanRegInfo{
		private CcsLoanReg tmLoanReg;
		private PostingFlag result;
		
		public CcsLoanReg getTmLoanReg() {
			return tmLoanReg;
		}
		public void setTmLoanReg(CcsLoanReg tmLoanReg) {
			this.tmLoanReg = tmLoanReg;
		}
		public PostingFlag getResult() {
			return result;
		}
		public void setResult(PostingFlag result) {
			this.result = result;
		}
	}

}
