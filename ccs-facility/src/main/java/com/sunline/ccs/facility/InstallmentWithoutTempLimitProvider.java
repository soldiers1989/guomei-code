package com.sunline.ccs.facility;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Service;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsStatement;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnUnstatement;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoanReg;
import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.ccs.param.def.LoanFeeDef;

/**
 * 临时额度不允许办理分期时，计算最大可分期金额，账单分期、消费分期
 * 
* @author fanghj
 *
 */
@Service
public class InstallmentWithoutTempLimitProvider {
	
	@PersistenceContext
	private EntityManager em;
	
	QCcsLoanReg qLoanReg = QCcsLoanReg.ccsLoanReg;
	
	/**
	 * 当临时额度不允许做分期时，计算账单分期最大可分期的金额
	 * 可账单分期最大金额=min(可账单分期金额,可分期额度,本期新增消费*总本金可转分期比例)
	 * @param stmt
	 * @param acct
	 * @param acctType
	 * @param loanFeeDef
	 * @return 账单分期最大可分期金额
	 */
	public BigDecimal StmtLoanMaxAmt(CcsStatement stmt, CcsAcct acct, CcsCard card,
			AccountAttribute accountAttribute, LoanFeeDef loanFeeDef, Date businessDate, BigDecimal isLoanAmt){
		//可账单分期最大金额
		BigDecimal stmtLoanMaxAmt = BigDecimal.ZERO;
		//可账单分期金额
		BigDecimal stmtLoanAmt = StmtLoanAmt(stmt, acct, isLoanAmt);
		//可分期额度
		BigDecimal stmtLoanLimit = StmtLoanLimit(acct, card, accountAttribute, businessDate);
		//本期新增消费*总本金可转分期比例
		BigDecimal ctdRetailLoanAmt = stmt.getCtdRetailAmt().multiply(loanFeeDef.maxAmountRate).setScale(2, BigDecimal.ROUND_HALF_UP);
		//可账单分期最大金额=min(可账单分期金额,可分期额度,本期新增消费*总本金可转分期比例)
		stmtLoanMaxAmt = stmtLoanAmt.compareTo(stmtLoanLimit) > 0 ? stmtLoanLimit : stmtLoanAmt;
		stmtLoanMaxAmt = stmtLoanMaxAmt.compareTo(ctdRetailLoanAmt) > 0 ? ctdRetailLoanAmt : stmtLoanMaxAmt;
		
		return stmtLoanMaxAmt.compareTo(BigDecimal.ZERO) > 0 ? stmtLoanMaxAmt : BigDecimal.ZERO;
	}
	
	/**
	 * 可账单分期的金额 = max( min(本期新增消费 - 本期已分期的消费，本期账单金额),0) – max((本期账单金额-信用额度),0)
	 * @param stmt
	 * @param acct
	 * @return 可账单分期的金额
	 */
	public BigDecimal StmtLoanAmt(CcsStatement stmt, CcsAcct acct, BigDecimal isLoanAmt){
		//可账单分期的金额
		BigDecimal stmtLoanAmt = BigDecimal.ZERO;
		//min(本期新增消费，本期账单金额)
		BigDecimal minCtdRetailAmtQualGraceBal = BigDecimal.ZERO;
		//本期新增消费 - 本期已经申请分期的消费
		BigDecimal ctdRetailAmt = stmt.getCtdRetailAmt().subtract(isLoanAmt);
		//本期账单金额
		BigDecimal qualGraceBal = stmt.getQualGraceBal();
		//min(本期新增消费，本期账单金额)
		minCtdRetailAmtQualGraceBal = ctdRetailAmt.compareTo(qualGraceBal) > 0 ? qualGraceBal : ctdRetailAmt;
		//与0修正
		minCtdRetailAmtQualGraceBal = minCtdRetailAmtQualGraceBal.compareTo(BigDecimal.ZERO) > 0 ? minCtdRetailAmtQualGraceBal : BigDecimal.ZERO;
		//本期账单金额-信用额度
		BigDecimal qualGraceBalSubtractionCreditLimit = qualGraceBal.subtract(acct.getCreditLmt()).setScale(2, BigDecimal.ROUND_HALF_UP);
		//与0修正
		qualGraceBalSubtractionCreditLimit = qualGraceBalSubtractionCreditLimit.compareTo(BigDecimal.ZERO) > 0 ? 
				qualGraceBalSubtractionCreditLimit : BigDecimal.ZERO;
		
		stmtLoanAmt = minCtdRetailAmtQualGraceBal.subtract(qualGraceBalSubtractionCreditLimit).setScale(2, BigDecimal.ROUND_HALF_UP);
		return stmtLoanAmt;
	}
	
	/**
	 * 可分期额度 = 信用额度*可分期比例-已分期余额
	 * 已分期余额 = loanBal+当天申请分期未入账的分期金额
	 * @param acct
	 * @param card
	 * @param acctType
	 * @return 可分期额度
	 */
	public BigDecimal StmtLoanLimit(CcsAcct acct, CcsCard card, AccountAttribute accountAttribute, Date businessDate){
		
		//可分期额度
		BigDecimal stmtLaonLimit = BigDecimal.ZERO;
		//已分期余额
		BigDecimal isLoanBal = BigDecimal.ZERO;
		//查询当天申请没有入账的分期金额
		JPAQuery queryLoanReg = new JPAQuery(em);
		List<CcsLoanReg> loanRegList = queryLoanReg.from(qLoanReg).where(qLoanReg.acctNbr.eq(acct.getAcctNbr()).and(qLoanReg.acctType.eq(acct.getAcctType()))
				.and(qLoanReg.cardNbr.eq(card.getLogicCardNbr()).and(qLoanReg.org.eq(acct.getOrg())))).list(qLoanReg);
		if (!loanRegList.isEmpty()) {
			for(CcsLoanReg isloanReg : loanRegList){
				isLoanBal = isLoanBal.add(isloanReg.getLoanInitPrin());
			}
		}
		isLoanBal = isLoanBal.add(acct.getLoanBal()).setScale(2, BigDecimal.ROUND_HALF_UP);
		
		//可分期额度 = 信用额度*可分期比例-已分期余额
		BigDecimal creditLimit = acct.getCreditLmt();
		
		if(acct.getLoanLmtRate() != null){
			stmtLaonLimit = creditLimit.multiply(acct.getLoanLmtRate()).subtract(isLoanBal).setScale(2, BigDecimal.ROUND_HALF_UP);
		}else {
			stmtLaonLimit = creditLimit.multiply(accountAttribute.loanLimitRate).subtract(isLoanBal).setScale(2, BigDecimal.ROUND_HALF_UP);
		}
		return stmtLaonLimit;
	}
	
	/**
	 * 2014-3-11（王伟民、高寒）最终版消费分期最终公式之后，该公式弃用
	 * 
	 * 当临时额度不允许分期时，计算消费分期最大可分期金额
	 * 消费分期最大可分期金额 = min(可消费转分期金额, 可分期额度, 申请转分期消费原交易金额*总本金可转分期比例)
	 * @param unstmt
	 * @param acct
	 * @param card
	 * @param currBalList 消费计划的currBalList
	 * @param accountAttribute
	 * @param loanFeeDef
	 * @return 消费分期最大可分期金额
	 */
	public BigDecimal TxnLoanMaxAmt(CcsTxnUnstatement unstmt, CcsAcct acct, CcsCard card, 
			List<BigDecimal> currBalList, AccountAttribute accountAttribute, LoanFeeDef loanFeeDef, Date businessDate){
		//消费分期最大可分期金额
		BigDecimal txnLoanMaxAmt = BigDecimal.ZERO;
		//可消费转分期金额
		BigDecimal txnLoanAmt = TxnLoanAmt(unstmt, acct, currBalList);
		//可分期额度
		BigDecimal txnLoanLimit = TxnLoanLimit(acct, card, accountAttribute, businessDate);
		//申请转分期消费原交易金额*总本金可转分期比例
		BigDecimal ctdRetailLoanAmt = unstmt.getTxnAmt().multiply(loanFeeDef.maxAmountRate).setScale(2, BigDecimal.ROUND_HALF_UP);
		//min(可消费转分期金额, 可分期额度, 申请转分期消费原交易金额*总本金可转分期比例)
		txnLoanMaxAmt = txnLoanAmt.compareTo(txnLoanLimit) > 0 ? txnLoanLimit : txnLoanAmt;
		txnLoanMaxAmt = txnLoanMaxAmt.compareTo(ctdRetailLoanAmt) > 0 ? ctdRetailLoanAmt : txnLoanMaxAmt;
		
		return txnLoanMaxAmt.compareTo(BigDecimal.ZERO) > 0 ? txnLoanMaxAmt : BigDecimal.ZERO;
	}
	
	/**
	 * 可消费转分期金额 = min(申请转分期消费原交易金额，消费计划余额)–max((当前余额-信用额度),0)
	 * @param unstmt
	 * @param acct
	 * @param currBalList
	 * @return 可消费转分期金额
	 */
	public BigDecimal TxnLoanAmt(CcsTxnUnstatement unstmt, CcsAcct acct, List<BigDecimal> currBalList){
		//可消费转分期金额
		BigDecimal txnLoanAmt = BigDecimal.ZERO;
		//消费计划余额
		BigDecimal txnPlanBal = BigDecimal.ZERO;
		for(BigDecimal currBal : currBalList){
			txnPlanBal = txnPlanBal.add(currBal);
		}
		//min(申请转分期消费原交易金额，消费计划余额)
		BigDecimal minTxnAmtAndTxnPlanBal = txnPlanBal.compareTo(unstmt.getTxnAmt()) > 0 ? unstmt.getTxnAmt() : txnPlanBal;
		//当前余额-信用额度
		BigDecimal currBalSubtractCreditLimit = acct.getCurrBal().subtract(acct.getCreditLmt());
		//与0修正
		currBalSubtractCreditLimit = currBalSubtractCreditLimit.compareTo(BigDecimal.ZERO) > 0 ? currBalSubtractCreditLimit : BigDecimal.ZERO;
		//min(申请转分期消费原交易金额，消费计划余额)–max((当前余额-信用额度),0)
		txnLoanAmt = minTxnAmtAndTxnPlanBal.subtract(currBalSubtractCreditLimit).setScale(2, BigDecimal.ROUND_HALF_UP);
		return txnLoanAmt;
	}
	
	/**
	 * 可消费分期的额度=信用额度*可分期比例-已分期余额
	 * 已分期余额 = loanBal+当天申请分期未入账的分期金额
	 * @param acct
	 * @param card
	 * @param accountAttribute
	 * @return 可消费分期的额度
	 */
	public BigDecimal TxnLoanLimit(CcsAcct acct, CcsCard card, AccountAttribute accountAttribute, Date businessDate){
		//可消费分期的额度
		BigDecimal txnLoanLimit = BigDecimal.ZERO;
		//已分期余额
		BigDecimal isLoanBal = BigDecimal.ZERO;
		//查询当天申请没有入账的分期金额
		JPAQuery queryLoanReg = new JPAQuery(em);
		List<CcsLoanReg> loanRegList = queryLoanReg.from(qLoanReg).where(qLoanReg.acctNbr.eq(acct.getAcctNbr()).and(qLoanReg.acctType.eq(acct.getAcctType()))
				.and(qLoanReg.cardNbr.eq(card.getLogicCardNbr()).and(qLoanReg.org.eq(acct.getOrg())))).list(qLoanReg);
		if (!loanRegList.isEmpty()) {
			for(CcsLoanReg isloanReg : loanRegList){
				isLoanBal = isLoanBal.add(isloanReg.getLoanInitPrin());
			}
		}
		isLoanBal = isLoanBal.add(acct.getLoanBal()).setScale(2, BigDecimal.ROUND_HALF_UP);
		//可消费分期的额度=信用额度*可分期比例-已分期余额
		// 临时额度是否有效
		BigDecimal creditLimit = BigDecimal.ZERO;
		if (acct.getTempLmtBegDate() != null && acct.getTempLmtEndDate() != null && businessDate.compareTo(acct.getTempLmtBegDate()) >= 0
				&& businessDate.compareTo(acct.getTempLmtEndDate()) <= 0){
			creditLimit = acct.getTempLmt();
		}else {
			creditLimit = acct.getCreditLmt();
		}
		if(acct.getLoanLmtRate() != null){
			txnLoanLimit = creditLimit.multiply(acct.getLoanLmtRate()).subtract(isLoanBal).setScale(2, BigDecimal.ROUND_HALF_UP);
		}else {
			txnLoanLimit = creditLimit.multiply(accountAttribute.loanLimitRate).subtract(isLoanBal).setScale(2, BigDecimal.ROUND_HALF_UP);
		}
		return txnLoanLimit;
	}
}

