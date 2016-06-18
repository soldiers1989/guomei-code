package com.sunline.ccs.batch.rpt.cca220;

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
import com.sunline.ccs.batch.rpt.cca220.item.MCATLoanRptItem;
import com.sunline.ccs.batch.rpt.common.RptParamFacility;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctKey;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnHst;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsTxnHst;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.TxnFee;
import com.sunline.pcm.param.def.Product;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.OrderStatus;
import com.sunline.ppy.dictionary.enums.Ownership;
import com.sunline.ppy.dictionary.enums.PostingFlag;

/**
 * 随借随还放款结果查询送报表
 * @author wanghl
 *
 */
public class RA221MCATLoanRpt extends KeyBasedStreamReader<CcsAcctKey, MCATLoanRptItem>  {
	private static final Logger logger = LoggerFactory.getLogger(RA221MCATLoanRpt.class);
	@PersistenceContext
    private EntityManager em;
	@Autowired
	BatchStatusFacility batchStatusFacility;
	@Autowired
	private RptParamFacility codeProv;

	private QCcsAcct qAcct = QCcsAcct.ccsAcct;
	private QCcsLoan qLoan = QCcsLoan.ccsLoan;
	private QCcsTxnHst qTxnHst = QCcsTxnHst.ccsTxnHst;
	
	@Override
	protected List<CcsAcctKey> loadKeys() {
		List<CcsAcctKey> keyList = new ArrayList<CcsAcctKey>();
		
		//从txn_hst 出记录
		Date batchDate = batchStatusFacility.getBatchDate();
		
		List<String> productCdList = codeProv.loadProductCdList(Ownership.O, LoanType.MCAT, null);
		if(productCdList == null || productCdList.size() <=0 ) return keyList;
		
		//FIXME 交易码直接写死了
		List<Tuple> acctTuple = new JPAQuery(em).from(qTxnHst)
				.where(qTxnHst.txnCode.eq(codeProv.MCAT_LOANING_TXN_CD)
					.and(qTxnHst.productCd.in(productCdList))
					.and(qTxnHst.postingFlag.eq(PostingFlag.F00))
					.and(qTxnHst.postDate.eq(batchDate)))
				.distinct()
				.list(qTxnHst.acctNbr, qTxnHst.acctType);
		for(Tuple t : acctTuple){
			CcsAcctKey key = new CcsAcctKey(t.get(qTxnHst.acctNbr), t.get(qTxnHst.acctType));
			keyList.add(key);
		}
		return keyList;
	}

	@Override
	protected MCATLoanRptItem loadItemByKey(CcsAcctKey key) {
		MCATLoanRptItem item = new MCATLoanRptItem();
		Date batchDate = batchStatusFacility.getBatchDate();
		
		logger.info("=====账户[{}][{}]", key.getAcctNbr(), key.getAcctType());
		
		List<CcsTxnHst> txnHstList = new JPAQuery(em).from(qTxnHst)
				.where(qTxnHst.acctNbr.eq(key.getAcctNbr())
						.and(qTxnHst.acctType.eq(key.getAcctType()))
						.and(qTxnHst.txnCode.eq(codeProv.MCAT_LOANING_TXN_CD))
						.and(qTxnHst.postingFlag.eq(PostingFlag.F00))
						.and(qTxnHst.postDate.eq(batchDate))
						)
				.list(qTxnHst);
		
		Date postDate = batchDate;
		BigDecimal postAmt = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
		for(CcsTxnHst txnHst : txnHstList){
//			postDate = txnHst.getPostDate();
			postAmt = postAmt.add(txnHst.getPostAmt());
		}
		List<CcsLoan> loans = new JPAQuery(em).from(qLoan)
				.where(qLoan.acctNbr.eq(key.getAcctNbr())
						.and(qLoan.acctType.eq(key.getAcctType())))
				.list(qLoan);
		//一个账户目前只可能有一个Loan
		CcsLoan loan = null;
		if(loans.size()>0){
			loan= loans.get(0);
			logger.info("合同号："+loan.getContrNbr());
		}else{
			logger.error("通过账户[{}][{}]未能找到Loan", key.getAcctNbr(), key.getAcctType());
			return null;
		}
		Tuple acct = new JPAQuery(em).from( qAcct)
				.where( qAcct.acctNbr.eq(key.getAcctNbr()) 
						.and(qAcct.acctType.eq(key.getAcctType()))
						)
				.singleResult(qAcct.name, qAcct.custId, qAcct.productCd);
		
		Product product = codeProv.loadProduct(acct.get(qAcct.productCd));
		LoanPlan loanPlan = codeProv.loadLoanPlan(loan.getLoanCode());
		ProductCredit productCredit = codeProv.loadProductCredit(acct.get(qAcct.productCd));
		
		item.productCd = product.productCode;
		item.productDesc = product.description;
		item.loanCode = loanPlan.loanCode;
		item.loanDesc = loanPlan.description;
		item.contrNbr = loan.getContrNbr();
		item.name = acct.get(qAcct.name);
		item.idNo = acct.get(qAcct.custId).toString();
		item.loanDate = postDate;
		item.orderStatus = OrderStatus.S; 
		item.loanAmt = postAmt.setScale(2, RoundingMode.HALF_UP);
		
		if(loan.getInterestRate().compareTo(BigDecimal.ZERO) > 0){
			item.interestRate = loan.getInterestRate().setScale(8, RoundingMode.HALF_UP).toPlainString();
		}else{
			item.interestRate = BigDecimal.ZERO.setScale(8).toPlainString();
		}
			
		
		List<TxnFee> listFee = productCredit.txnFeeList.get(codeProv.MCAT_LOANING_TXN_CD);
		if(listFee != null && listFee.size()>0){
			TxnFee txnFee = listFee.get(0);
			List<CcsTxnHst> withDrawFeeList = new JPAQuery(em).from(qTxnHst)
					.where(qTxnHst.txnCode.eq(txnFee.feeTxnCd)
							.and(qTxnHst.postDate.eq(batchDate))
							.and(qTxnHst.acctNbr.eq(key.getAcctNbr()))
							.and(qTxnHst.acctType.eq(key.getAcctType())))
					.list(qTxnHst);
			
			BigDecimal withDrawFee = BigDecimal.ZERO.setScale(2);
			for(CcsTxnHst feeTxn : withDrawFeeList){
				withDrawFee = withDrawFee.add(feeTxn.getPostAmt());
			}
			item.withdraw = withDrawFee.setScale(2, RoundingMode.HALF_UP);
		}else{
			item.withdraw = BigDecimal.ZERO.setScale(2);
		}
		return item;
	}
	
	public static void main(String[] args) {
		System.out.println(new BigDecimal("123330.120").setScale(-2, RoundingMode.HALF_UP).toPlainString());
	}
}
