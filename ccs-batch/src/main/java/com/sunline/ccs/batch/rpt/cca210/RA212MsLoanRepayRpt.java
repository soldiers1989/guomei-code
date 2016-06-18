package com.sunline.ccs.batch.rpt.cca210;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.query.Tuple;
import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.batch.KeyBasedStreamReader;
import com.sunline.ccs.batch.rpt.cca210.item.MsLoanRepayRptItem;
import com.sunline.ccs.batch.rpt.common.RptParamFacility;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctKey;
import com.sunline.ccs.infrastructure.shared.model.CcsRepayHst;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsRepayHst;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.pcm.param.def.Product;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.Ownership;
import com.sunline.ppy.dictionary.enums.PlanType;

/**
 * 马上贷还款结果查询报表文件
 * @author wanghl
 *
 */
public class RA212MsLoanRepayRpt extends KeyBasedStreamReader<CcsAcctKey,MsLoanRepayRptItem>{
	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@PersistenceContext
    private EntityManager em;
	@Autowired
	BatchStatusFacility batchStatusFacility;
	@Autowired
	private RptParamFacility codeProv;

	private QCcsRepayHst qRepay = QCcsRepayHst.ccsRepayHst;
	private QCcsLoan qLoan = QCcsLoan.ccsLoan;
	private QCcsAcct qAcct = QCcsAcct.ccsAcct;
	
	@Override
	protected List<CcsAcctKey> loadKeys() {
		List<CcsAcctKey> keyList = new ArrayList<CcsAcctKey>();
		List<String> productCdList = codeProv.loadProductCdList(Ownership.O, LoanType.MCEI, null);
		if(productCdList == null || productCdList.size() <=0 ) return keyList;
		
		Date batchDate = batchStatusFacility.getBatchDate();
			
		List<Tuple> repayTuple = new JPAQuery(em).from(qRepay, qAcct)
				.where(qRepay.batchDate.eq(batchDate)
						.and(qRepay.acctNbr.eq(qAcct.acctNbr)
								.and(qRepay.acctType.eq(qAcct.acctType))
								.and(qAcct.productCd.in(productCdList))))
				.distinct()
				.list(qRepay.acctNbr, qRepay.acctType);
		for(Tuple t : repayTuple){
			CcsAcctKey key = new CcsAcctKey(t.get(qRepay.acctNbr), t.get(qRepay.acctType));
			keyList.add(key);
		}
		
		return keyList;
	}

	@Override
	protected MsLoanRepayRptItem loadItemByKey(CcsAcctKey key) {
		logger.info("马上贷还款结果==账户[{}][{}]==", key.getAcctNbr(), key.getAcctType());

		Tuple loan = new JPAQuery(em).from(qLoan)
				.where(qLoan.acctNbr.eq(key.getAcctNbr())
						.and(qLoan.acctType.eq(key.getAcctType()))
						)
				.singleResult(qLoan.loanInitPrin, qLoan.contrNbr, qLoan.activeDate, qLoan.loanCode);
		Tuple acct = new JPAQuery(em).from( qAcct)
				.where(qAcct.acctNbr.eq(key.getAcctNbr())
						.and(qAcct.acctType.eq(key.getAcctType()))
						)
				.singleResult(qAcct.productCd, qAcct.name, qAcct.custId, qAcct.org);
		logger.debug("合同号[{}]",loan.get(qLoan.contrNbr));
		
		MsLoanRepayRptItem item = new MsLoanRepayRptItem();
		Date batchDate = batchStatusFacility.getBatchDate();
		
		//获取当日所有还款分配信息，关联Plan表取得交易参考号
		List<CcsRepayHst> repayExList = new JPAQuery(em).from(qRepay)
				.where(qRepay.acctNbr.eq(key.getAcctNbr())
						.and(qRepay.acctType.eq(key.getAcctType()))
						.and(qRepay.batchDate.eq(batchDate))
						.and(qRepay.planType.in(PlanType.I, PlanType.Q))
						).list(qRepay);
		
		logger.info("还款分配信息[{}]条", repayExList.size());
		addBnpAmt(item, repayExList);

		Product product = codeProv.loadParameter(acct.get(qAcct.productCd) , Product.class, acct.get(qAcct.org));
		LoanPlan loanPlan = codeProv.loadParameter(loan.get(qLoan.loanCode),LoanPlan.class, acct.get(qAcct.org));
		
		item.contrNbr = loan.get(qLoan.contrNbr);
		item.productCd = product.productCode;
		item.productDesc = product.description;
		item.loanCode = loanPlan.loanCode;
		item.loanDesc = loanPlan.description;
		item.activeDate = loan.get(qLoan.activeDate);
		item.loanAmt = loan.get(qLoan.loanInitPrin);
		item.name = acct.get(qAcct.name);
		item.idNo = acct.get(qAcct.custId).toString();
		item.repayDate = batchDate;
		return item;
	}
	/**
	 * 累加同一Loan同余额成分的金额
	 */
	private void addBnpAmt(MsLoanRepayRptItem item, List<CcsRepayHst> repayList) {
		
		BigDecimal repayPrincipal = new BigDecimal(0);
		BigDecimal repayInterest = new BigDecimal(0);
		BigDecimal repayTxnFee = new BigDecimal(0);
		BigDecimal repaySvcFee = new BigDecimal(0);
		BigDecimal repayOptionalSvcFee = new BigDecimal(0);
		BigDecimal repayMulct = new BigDecimal(0);
		BigDecimal repayPenalty = new BigDecimal(0);
		BigDecimal repayLpc = new BigDecimal(0);
		BigDecimal replaceSvcFee = new BigDecimal(0);
		BigDecimal replacePenalty = new BigDecimal(0);
		BigDecimal replaceLateFee = new BigDecimal(0);
		BigDecimal replaceMulctAmt = new BigDecimal(0);
		BigDecimal replaceTxnFee = new BigDecimal(0);
		BigDecimal repayPrepayPkgAmt = new BigDecimal(0);
		BigDecimal repayAmtsucc = new BigDecimal(0);
		
		for(CcsRepayHst repay : repayList){
			logger.debug("repayId[{}]bnpType[{}]repayAmt[{}]", 
					repay.getPaymentId(), repay.getBnpType(), repay.getRepayAmt());
			
			repayAmtsucc = repayAmtsucc.add(repay.getRepayAmt());
			
			switch (repay.getBnpType()) {
			case pastPrincipal:
			case ctdPrincipal: repayPrincipal = repayPrincipal.add(repay.getRepayAmt()); break;
			case pastInterest:
			case ctdInterest: repayInterest = repayInterest.add(repay.getRepayAmt()); break;
			case pastMulct:
			case ctdMulct: repayMulct = repayMulct.add(repay.getRepayAmt()); break;
			case pastSvcFee: 
			case ctdSvcFee: repaySvcFee = repaySvcFee.add(repay.getRepayAmt()); break;
			case pastLifeInsuFee:
			case ctdLifeInsuFee: repayOptionalSvcFee = repayOptionalSvcFee.add(repay.getRepayAmt()); break;
			case ctdTxnFee: 
			case pastTxnFee: repayTxnFee = repayTxnFee.add(repay.getRepayAmt()); break;
			case pastPenalty:
			case ctdPenalty: repayPenalty = repayPenalty.add(repay.getRepayAmt()); break;
			case pastLpc:
			case ctdLpc: repayLpc = repayLpc.add(repay.getRepayAmt()); break;
			case pastReplaceSvcFee:
			case ctdReplaceSvcFee: replaceSvcFee = replaceSvcFee.add(repay.getRepayAmt()); break;
			case pastReplacePenalty: 
			case ctdReplacePenalty:replacePenalty = replacePenalty.add(repay.getRepayAmt()); break;
			case pastReplaceLpc: 
			case ctdReplaceLpc: replaceLateFee = replaceLateFee.add(repay.getRepayAmt()); break;
			case pastReplaceMulct:
			case ctdReplaceMulct: replaceMulctAmt = replaceMulctAmt.add(repay.getRepayAmt()); break;
			case pastReplaceTxnFee: 
			case ctdReplaceTxnFee: replaceTxnFee = replaceTxnFee.add(repay.getRepayAmt()); break;
			case pastPrepayPkg:
			case ctdPrepayPkg: repayPrepayPkgAmt = repayPrepayPkgAmt.add(repay.getRepayAmt()); break;
			default: break;
			}
		}
		item.actualRepayPrincipal = repayPrincipal.setScale(2, RoundingMode.HALF_UP);
		item.actualRepayInterest = repayInterest.setScale(2, RoundingMode.HALF_UP);
		item.actualRepayMulct = repayMulct.setScale(2, RoundingMode.HALF_UP);
		item.actualRepaySvcFee = repaySvcFee.setScale(2, RoundingMode.HALF_UP);
		item.actualRepayLifeInsFee = repayOptionalSvcFee.setScale(2, RoundingMode.HALF_UP);
		item.actualRepayTxnFee = repayTxnFee.setScale(2, RoundingMode.HALF_UP);
		item.actualRepayPrePayPKGAmt = repayPrepayPkgAmt.setScale(2, RoundingMode.HALF_UP);
		item.actualRepayPenalty = repayPenalty.setScale(2, RoundingMode.HALF_UP);
		item.actualRepayLpc = repayLpc.setScale(2, RoundingMode.HALF_UP);
		item.actualRepayRepalaceSvcFee = replaceSvcFee.setScale(2, RoundingMode.HALF_UP);
		item.actualRepayReplacePenalty = replacePenalty.setScale(2, RoundingMode.HALF_UP);
		item.actualRepayReplaceLateFee = replaceLateFee.setScale(2, RoundingMode.HALF_UP);
		item.actualRepayReplaceMulctAmt = replaceMulctAmt.setScale(2, RoundingMode.HALF_UP);
		item.actualRepayReplaceTxnFee = replaceTxnFee.setScale(2, RoundingMode.HALF_UP);
		item.repayAmt = repayAmtsucc.setScale(2, RoundingMode.HALF_UP);
	}
}
