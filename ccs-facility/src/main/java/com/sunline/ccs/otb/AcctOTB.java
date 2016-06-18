package com.sunline.ccs.otb;

import static com.sunline.ccs.facility.CaclUtils.checkPositive;
import static com.sunline.ccs.facility.CaclUtils.setScale2HalfUp;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.infrastructure.server.repos.RCcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoanReg;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.enums.CashLoanLimitType;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.LoanStatus;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * 账户级可用额度计算
* @author fanghj
 * 
 * 账户级可用额度包括账户OTB和账户的取现OTB，
 * 需要使用TM_ACCT_O表和PRODUCT_CREDIT中的字段进行计算
 */
@Service
public class AcctOTB {
    	private Logger logger = LoggerFactory.getLogger(getClass());
	/**
	 * 公共组件
	 */
	@Autowired
	private CommProvide commonProvide;
	@Autowired
	private RCcsLoan rCcsLoan;
	@PersistenceContext
	private EntityManager em;
	QCcsLoanReg qCcsLoanReg = QCcsLoanReg.ccsLoanReg;
	@Autowired
	private CustAcctCardFacility custAcctCardQueryFacility;
	/**
	 * 账户可用额度
	 * @param 介质卡号 cardNbr
	 * @param 币种 orgNum
	 * @param 是否共享额度 sharedCredit
	 * @return
	 * @throws ProcessException 
	 */
	public BigDecimal acctOTB(String cardNbr, String currencyCode, boolean sharedCredit ,Date processDate) throws ProcessException {
		AccountType acctType = getAcctType(currencyCode, sharedCredit);
		if (acctType == null) throw new ProcessException("币种[" + currencyCode + "]查询不到对应的账户类型");
		return this.acctOTB(cardNbr, acctType ,processDate);
	}

	/**
	 * 账户可用额度
	 * OTB = 账户有效额度*授权超限比例 – 账户余额 – 未达借记授权金额 + 未达贷记授权金额 + 争议金额
	 * @param 介质卡号 cardNbr
	 * @param 账户类型 acctType
	 * @return
	 */
	public BigDecimal acctOTB(String cardNbr, AccountType acctType ,Date processDate) {
		CcsAcctO accountO = commonProvide.findAcctOByCardNbr(cardNbr, acctType);
		return this.acctOTB(accountO, commonProvide.retrieveProductCredit(accountO.getProductCd()), processDate);
	}
	
	/**
	 * 查询账户可用额度，考虑临时额度
	 * @param accountO
	 * @param productCr
	 * @param processDate
	 * @return
	 */
	public BigDecimal acctOTB(CcsAcctO accountO ,ProductCredit productCr ,Date processDate){
		// 获取账户有效信用额度，考虑临时额度
		BigDecimal creditLimit = commonProvide.getCurrCreditLmt(processDate,accountO);
		return adjustAcctOTB(accountO, productCr, processDate, creditLimit);
	}

	/**
	 * 账户查询可用额度，不考虑临时额度
	 * @param accountO
	 * @param productCr
	 * @param processDate
	 * @return
	 */
	public BigDecimal acctInqOTB(CcsAcctO accountO ,ProductCredit productCr ,Date processDate){
		// 获取账户有效信用额度
//		BigDecimal creditLimit = commonProvide.getCurrCreditLmt(processDate, accountO);
		BigDecimal creditLimit = accountO.getCreditLmt();
		return adjustAcctOTB(accountO, productCr, processDate, creditLimit);
	}
	
	/**
	 * 
	 * @see 方法名：adjustAcctOTB 
	 * @see 描述：计算校准账户OTB
	 * 			1、超限比例
	 * 			2、分期余额
	 * 			3、溢缴款
	 * 			4、未达贷记授权
	 * 			5、争议交易金额
	 * 			6、当天申请没有入账的消费分期金额
	 * @see 创建日期：2015年6月23日上午11:26:51
	 * @author liruilin
	 *  
	 * @param accountO
	 * @param productCr
	 * @param processDate
	 * @param creditLimit
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private BigDecimal adjustAcctOTB(CcsAcctO accountO, ProductCredit productCr, Date processDate, BigDecimal creditLimit) {
		BigDecimal acctOtbAmt = BigDecimal.ZERO;
		// 获得授权超限比例
		BigDecimal ovrLmtRate = commonProvide.getOvrlmtRate(accountO, productCr, processDate);
		// Account OTB = 账户有效信用额度*(1+授权超限比例 OVRLMT_RATE)–未匹配借记UNMATCH_DB+额度内分期余额LoanBal
		acctOtbAmt =creditLimit.multiply(BigDecimal.ONE.add(ovrLmtRate))
				.subtract(accountO.getMemoDb()).add(accountO.getLoanBal());
		
		//判断额度内分期余额是否占用OTB
		//IF  额度内分期余额占用OTB(OTB_INCL_FRZN = Y)
		//   SUBSTRACT 额度内分期余额LOAN_BAL  FROM  OTB
		//END_IF
		if (productCr.otbInclFrzn) {
			acctOtbAmt = acctOtbAmt.subtract(accountO.getLoanBal());
		}
		
		//判断溢缴款是否提升OTB
		//IF  溢缴款提升OTB (OTB_INCL_CRBAL = Y)
		//   SUBSTRACT 当前账户余额CURR_BALANCE  FROM  OTB
		//ELSE
		//   IF  当前账户余额CURR_BALANCE > = 0
		//	  SUBSTRACT 当前账户余额CURR_BALANCE  FROM  OTB
		//   END_IF
		//END_IF
		if (productCr.otbInclCrbal) {
			acctOtbAmt = acctOtbAmt.subtract(accountO.getCurrBal());
		} else {
			if (accountO.getCurrBal().compareTo(BigDecimal.ZERO) >= 0)
				acctOtbAmt = acctOtbAmt.subtract(accountO.getCurrBal());
		}
		
		//判断未达贷记授权是否提升OTB
		//IF  未达贷记授权提升OTB(OTB_INCL_CRATH = Y)
		//   ADD 未达贷记授权金额UNMATCH_CR  TO  OTB
		//END_IF
		if (productCr.otbInclCrath) {
			acctOtbAmt = acctOtbAmt.add(accountO.getMemoCr());
		}
		//判断争议交易金额是否占用OTB
		//IF  争议交易金额不占用OTB (OTB_INCL_DSPT = N)
		//   ADD 争议交易金额DISPUTE_AMT  FROM  OTB
		//END_IF
		if (!productCr.otbInclDspt) {
			acctOtbAmt = acctOtbAmt.add(accountO.getDisputeAmt());
		}
		
		// 查询账户下当天申请没有入账的消费分期金额
		BigDecimal appedAmt = BigDecimal.ZERO;
		JPAQuery queryLoanReg = new JPAQuery(em);
		List<CcsLoanReg> loanRegList = queryLoanReg
				.from(qCcsLoanReg)
				.where(qCcsLoanReg.acctNbr.eq(accountO.getAcctNbr()).and(qCcsLoanReg.acctType.eq(accountO.getAcctType()))
				.and(qCcsLoanReg.loanType.eq(LoanType.R).and(qCcsLoanReg.org.eq(accountO.getOrg())))).list(qCcsLoanReg);
		if (!loanRegList.isEmpty()) {
			for (CcsLoanReg isloanReg : loanRegList) {
				appedAmt = appedAmt.add(isloanReg.getLoanInitPrin());
			}
			logger.debug("appendAmt等于：-------------------------"+appedAmt);
		}
		acctOtbAmt = acctOtbAmt.subtract(appedAmt);
//		accountOtbAmt = accountOtbAmt.setScale(2, BigDecimal.ROUND_HALF_UP);
//		return accountOtbAmt.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : accountOtbAmt;
		return setScale2HalfUp(checkPositive(acctOtbAmt));
	}
	
	
	
	/**
	 * 账户可用取现额度
	 * CASH OTB = 账户有效取现额度*授权超限比例 – 账户取现余额 – 未达借记取现授权金额
	 * @param 介质卡号 cardNbr
	 * @param 币种 orgNum
	 * @param 是否共享额度 sharedCredit
	 * @return
	 * @throws ProcessException
	 */
	public BigDecimal acctCashOTB(String cardNbr, String currencyCode, boolean sharedCredit,Date processDate) throws ProcessException {
		AccountType acctType = getAcctType(currencyCode, sharedCredit);
		if (acctType == null) throw new ProcessException("币种[" + currencyCode + "]查询不到对应的账户类型");
		return this.acctCashOTB(cardNbr, acctType ,processDate);
	}

	/**
	 * 
	 * @see 方法名：getAcctType 
	 * @see 描述：匹配枚举，获取账户类型
	 * @see 创建日期：2015年6月23日上午11:52:44
	 * @author liruilin
	 *  
	 * @param currencyCode
	 * @param sharedCredit
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private AccountType getAcctType(String currencyCode, boolean sharedCredit) {
		AccountType acctType = null;
		for (AccountType accountTpye : AccountType.values()) {
			if ((accountTpye.getCurrencyCode().equals(currencyCode)) && (accountTpye.isSharedCredit() == sharedCredit)) {
				acctType = accountTpye;
				break;
			}
		}
		return acctType;
	}

	/**
	 * @param cardNbr
	 * @param acctType
	 * @param processDate
	 * @return
	 */
	public BigDecimal acctCashOTB(String cardNbr,  AccountType acctType,Date processDate)  {
		CcsAcctO accountO = commonProvide.findAcctOByCardNbr(cardNbr, acctType);
		ProductCredit productCr = commonProvide.retrieveProductCredit(accountO.getProductCd());
		return this.acctCashOTB(accountO, productCr, processDate);
	}

	/**
	 * 取现可用额度
	 * @param accountO
	 * @param productCr
	 * @param processDate
	 * @return
	 */
	public BigDecimal acctCashOTB(CcsAcctO accountO ,ProductCredit productCr ,Date processDate){
		BigDecimal acctCashOtbAmt = BigDecimal.ZERO;

		// 获取账户有效信用额度
		BigDecimal creditLimit = commonProvide.getCurrCreditLmt(processDate, accountO);
		// 获得授权超限比例
		BigDecimal ovrLmtRate = commonProvide.getOvrlmtRate(accountO, productCr, processDate);
		// 获得取现额度比例
		BigDecimal cashLimitRt = commonProvide.getCashLmtRate(accountO, productCr, processDate);
		// CASH OTB = 账户有效取现额度*(1+授权超限比例 OVRLMT_RATE)*取现额度比例 CASH_LIMIT_RT–未达借记授权UNMATCH_CASH
		acctCashOtbAmt = creditLimit.multiply(BigDecimal.ONE.add(ovrLmtRate)) 
				.multiply(cashLimitRt).subtract(accountO.getMemoCash());
		
		//判断溢缴款是否提升CASH OTB
		//IF  溢缴款提升OTB (COTB_INCL_CRBAL = Y)
		//   SUBSTRACT 当前账户取现余额CASH_ BAL  FROM  CASH OTB
		//ELSE
		//   IF  当前账户取现余额CASH_BAL > = 0
		//SUBSTRACT 当前账户取现余额CASH_ BAL  FROM  CASH OTB
		//   END_IF
		//END_IF
		if (productCr.cotbInclCrbal) {
			acctCashOtbAmt = acctCashOtbAmt.subtract(accountO.getCashBal());
		} else {
			if (accountO.getCashBal().compareTo(BigDecimal.ZERO) >= 0) {
				acctCashOtbAmt = acctCashOtbAmt.subtract(accountO.getCashBal());
			}
		}
		//判断未达贷记授权是否提升CASH OTB
		//IF  未达贷记授权提升CASH OTB(COTB_INCL_CRATH = Y)
		//   ADD 未达贷记授权金额UNMATCH_CR  TO  CASH OTB
		//END_IF
		if (productCr.cotbInclCrath) {
			acctCashOtbAmt = acctCashOtbAmt.add(accountO.getMemoCr());
		}
		//如果现金分期使用取现额度，取现分期的余额占用取现额度
		if(productCr.cashLoanLimitType == CashLoanLimitType.C){
			QCcsLoan q = QCcsLoan.ccsLoan;
			Iterable<CcsLoan> iter = rCcsLoan.findAll(q.acctNbr.eq(accountO.getAcctNbr()).and(q.org.eq(OrganizationContextHolder.getCurrentOrg())).and(q.loanType.eq(LoanType.C)).and(q.loanStatus.in(LoanStatus.A,LoanStatus.I,LoanStatus.R)));
			if(iter !=null){
				for(CcsLoan l : iter){
					acctCashOtbAmt = acctCashOtbAmt.subtract(l.getLoanInitPrin());
				}
			}
		}
		
		//使用账户OTB修正账户CASH OTB
		//IF   CAHS  OTB  >  OTB
		//则将账户CASH OTB降低为同账户OTB 相等
		//END_IF
		BigDecimal acctOtbAmt = acctOTB(accountO, productCr ,processDate);
		if (acctCashOtbAmt.compareTo(acctOtbAmt) > 0) {
			acctCashOtbAmt = acctOtbAmt;
		}
		
//		accountCashOtbAmt = accountCashOtbAmt.setScale(2, BigDecimal.ROUND_HALF_UP);
//		return accountCashOtbAmt.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : accountCashOtbAmt;
		return setScale2HalfUp(checkPositive(acctCashOtbAmt));
	}

	/**
	 * 可用取现查询额度，目前直接调用acctCashOTB
	 * @param accountO
	 * @param productCr
	 * @param processDate
	 * @return
	 */
	public BigDecimal acctCashInqOTB(CcsAcctO accountO ,ProductCredit productCr ,Date processDate){
		return acctCashOTB(accountO, productCr, processDate);
	}

	/**
	 * 账户溢缴款取现可用额度调用接口
	 * @param accountO
	 * @param productCr
	 * @param processDate
	 * @return
	 */
	public BigDecimal acctDepositeCashOTB(CcsAcctO accountO ,ProductCredit productCr ,Date processDate){
		BigDecimal acctDepositeCashOtbAmt = BigDecimal.ZERO;

		// 当前取现余额大于等于零
		if (accountO.getCashBal().compareTo(BigDecimal.ZERO) >= 0) {
			// 溢缴款取现可用额度为零
			return acctDepositeCashOtbAmt;
		}
		// 当前取现余额小于零
		else {
			BigDecimal unmatchDb = accountO.getMemoDb();
			// 当前未匹配借记金额小于零
			if (accountO.getMemoDb().compareTo(BigDecimal.ZERO) < 0) {
				unmatchDb = BigDecimal.ZERO;
			} 
			// 溢缴款取现可用额度 = 当前取现余额的绝对值 - 未匹配借记金额
			acctDepositeCashOtbAmt = accountO.getCashBal().abs().subtract(unmatchDb);
			
			if(acctDepositeCashOtbAmt.compareTo(BigDecimal.ZERO)<0) return BigDecimal.ZERO;
		}
		
//		accountDepositeCashOtbAmt = accountDepositeCashOtbAmt.setScale(2, BigDecimal.ROUND_HALF_UP);
//		return accountDepositeCashOtbAmt.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : accountDepositeCashOtbAmt;
		return setScale2HalfUp(checkPositive(acctDepositeCashOtbAmt));
	}
	/**
	 * 现金分期OTB计算
	 * @param accountO
	 * @param productCr
	 * @param processDate
	 * @return
	 */
	public BigDecimal acctCashLoanOTB(CcsAcctO accountO ,ProductCredit productCr ,Date processDate){
		BigDecimal acctCashLoanOtbAmt = BigDecimal.ZERO;
		if(productCr.cashLoanLimitType == CashLoanLimitType.L){
			acctCashLoanOtbAmt = acctOTB(accountO, productCr, processDate);
		}else{
			acctCashLoanOtbAmt = acctCashOTB(accountO, productCr, processDate);
		}
		
//		return accountCashLoanOtbAmt.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : accountCashLoanOtbAmt;
		return setScale2HalfUp(checkPositive(acctCashLoanOtbAmt));
	}
	/**
	 * 现金分期OTB计算
	 * @param cardNbr
	 * @param acctType
	 * @param processDate
	 * @return
	 */
	public BigDecimal acctCashLoanOTB(String cardNbr,  AccountType acctType,Date processDate)  {
		CcsAcctO accountO = commonProvide.findAcctOByCardNbr(cardNbr, acctType);
		ProductCredit productCr = commonProvide.retrieveProductCredit(accountO.getProductCd());
		return this.acctCashLoanOTB(accountO, productCr, processDate);
	}
	
	/**
	 * 账户分期可用额度调用接口
	 * @return
	 */
	public BigDecimal loanOTB(CcsAcctO accountO ,ProductCredit productCredit ,BigDecimal sumloanInitPrin,BigDecimal sumUnmatchLoanChbTxnAmt, Date processDate){
		/**
		 * loanOTB = credit_limit *loanRate*(1+overlimitRate)  
		 * - sum(同一账户下 tmloan主表中状态不为已终结的所有交易的分期本金 )  
		 * - sum(同一账户下 tm_unmatch_o中交易类型为loan且finalaction为A前交易状态为normal的交易 chbtxnamt)
		 */
		// 获取账户有效信用额度
		BigDecimal creditLimit = commonProvide.getCurrCreditLmt(processDate,accountO);
		return adjustLoanOTB(	accountO, productCredit, sumloanInitPrin, sumUnmatchLoanChbTxnAmt, processDate,
								creditLimit);
	}

	/**
	 * 账户分期可用额度调用接口（不包含临时额度）
	 * @return
	 */
	public BigDecimal loanInqOTB(CcsAcctO accountO ,ProductCredit productCredit ,BigDecimal sumloanInitPrin,BigDecimal sumUnmatchLoanChbTxnAmt, Date processDate){
		/**
		 * loanOTB = credit_limit *loanRate*(1+overlimitRate)  
		 * - sum(同一账户下 tmloan主表中状态不为已终结的所有交易的分期本金 )  
		 * - sum(同一账户下 tm_unmatch_o中交易类型为loan且finalaction为A前交易状态为normal的交易 chbtxnamt)
		 */
		// 获取账户有效信用额度
		BigDecimal creditLimit =  accountO.getCreditLmt();
		return adjustLoanOTB(	accountO, productCredit, sumloanInitPrin, sumUnmatchLoanChbTxnAmt, processDate,
								creditLimit);
	}
	
	/**
	 * 
	 * @see 方法名：adjustLoanOTB 
	 * @see 描述：计算校准分期OTB
	 * 			1、超限比例
	 * 			2、分期额度比例
	 * 			3、loanOTB = credit_limit *loanRate*(1+overlimitRate)-sumloanInitPrin－sumUnmatchLoanChbTxnAmt
	 * @see 创建日期：2015年6月23日下午3:08:15
	 * @author liruilin
	 *  
	 * @param accountO
	 * @param productCredit
	 * @param sumloanInitPrin
	 * @param sumUnmatchLoanChbTxnAmt
	 * @param processDate
	 * @param creditLimit
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private BigDecimal adjustLoanOTB(CcsAcctO accountO, ProductCredit productCredit, BigDecimal sumloanInitPrin,
			BigDecimal sumUnmatchLoanChbTxnAmt, Date processDate, BigDecimal creditLimit) {
		BigDecimal loanOTB = BigDecimal.ZERO;
		// 获得授权超限比例
		BigDecimal ovrLmtRate = commonProvide.getOvrlmtRate(accountO, productCredit, processDate);
		// 获得分期额度比例
		BigDecimal loanLmtRate = commonProvide.getLoanLmtRate(accountO, productCredit, processDate);
		
		loanOTB = creditLimit.multiply(loanLmtRate).multiply(ovrLmtRate.add(BigDecimal.ONE)).subtract(sumloanInitPrin).subtract(sumUnmatchLoanChbTxnAmt);
		
//		return loanOTB.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : loanOTB.setScale(2,BigDecimal.ROUND_HALF_UP);
		return setScale2HalfUp(checkPositive(loanOTB));
	}
	
	/**
	 * 账户已用金额，和零修正
	 * @param cardNbr
	 * @param orgNum
	 * @param processDate
	 * @return
	 * @throws ProcessException
	 */
	public BigDecimal acctUsedAmt(CcsAcctO accountO ,ProductCredit productCr ,Date processDate){
		BigDecimal accountUsedAmt = BigDecimal.ZERO;
		
		// 账户已用金额 = + 未匹配借记UNMATCH_DB - 额度内分期余额LoanBal
		accountUsedAmt = accountUsedAmt.add(accountO.getMemoDb()).subtract(accountO.getLoanBal());
		
		//判断额度内分期余额是否占用OTB
		//IF  额度内分期余额占用OTB(OTB_INCL_FRZN = Y)
		//   add 额度内分期余额LOAN_BAL  to  accountUsedAmt
		//END_IF
		
		if (productCr.otbInclFrzn) {
			accountUsedAmt = accountUsedAmt.add(accountO.getLoanBal());
		}
		//判断溢缴款是否提升OTB
		//IF  溢缴款提升OTB (OTB_INCL_CRBAL = Y)
		//		add 当前账户余额CURR_BALANCE  to  accountUsedAmt
		//ELSE
		//   IF  当前账户余额CURR_BALANCE > = 0
		//	  	add 当前账户余额CURR_BALANCE  to  accountUsedAmt
		//   END_IF
		//END_IF
		if (productCr.otbInclCrbal) {
			accountUsedAmt = accountUsedAmt.add(accountO.getCurrBal());
		} else {
			if (accountO.getCurrBal().compareTo(BigDecimal.ZERO) >= 0)
				accountUsedAmt = accountUsedAmt.add(accountO.getCurrBal());
		}
		//判断未达贷记授权是否提升OTB
		//IF  未达贷记授权提升OTB(OTB_INCL_CRATH = Y)
		//   subtract 未达贷记授权金额UNMATCH_CR  from  accountUsedAmt
		//END_IF
		if (productCr.otbInclCrath) {
			accountUsedAmt = accountUsedAmt.subtract(accountO.getMemoCr());
		}
		//判断争议交易金额是否占用OTB
		//IF  争议交易金额不占用OTB (OTB_INCL_DSPT = N)
		//   subtract 争议交易金额DISPUTE_AMT  from  accountUsedAmt
		//END_IF
		if (!productCr.otbInclDspt) {
			accountUsedAmt = accountUsedAmt.subtract(accountO.getDisputeAmt());
		}
//		accountUsedAmt = accountUsedAmt.setScale(2, BigDecimal.ROUND_HALF_UP);
//		return accountUsedAmt;
		return setScale2HalfUp(accountUsedAmt);
	}
	
	/**
	 * 账户可用额度 = 账户信用额度-在途db-累计放款本金
	 * 账户非循环可用额度调用接口
	 * @return
	 */
	public BigDecimal loanNoCycleOTB(CcsAcctO accto ,CcsAcct acct){
		// 获取账户有效信用额度
		
		BigDecimal loanOTB = acct.getCreditLmt().
				subtract(accto.getMemoDb()==null?BigDecimal.ZERO:accto.getMemoDb()).
				subtract(acct.getLtdLoanAmt()==null?BigDecimal.ZERO:acct.getLtdLoanAmt());
		return loanOTB;
	}
}
