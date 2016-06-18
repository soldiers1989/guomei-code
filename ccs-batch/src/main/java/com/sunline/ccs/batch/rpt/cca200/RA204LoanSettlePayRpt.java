package com.sunline.ccs.batch.rpt.cca200;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.query.Tuple;
import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.batch.KeyBasedStreamReader;
import com.sunline.ccs.batch.rpt.cca200.items.LoanSettlePayRptItem;
import com.sunline.ccs.batch.rpt.common.RptParamFacility;
import com.sunline.ccs.infrastructure.shared.model.CcsRepayHst;
import com.sunline.ccs.infrastructure.shared.model.CcsSettleLoanHst;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsPlan;
import com.sunline.ccs.infrastructure.shared.model.QCcsRepayHst;
import com.sunline.ccs.infrastructure.shared.model.QCcsSettleLoanHst;
import com.sunline.ccs.infrastructure.shared.model.QCcsTxnHst;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.SysTxnCdMapping;
import com.sunline.ccs.param.def.TxnCd;
import com.sunline.ccs.param.def.enums.BucketObject;
import com.sunline.ccs.param.def.enums.SysTxnCd;
import com.sunline.pcm.param.def.FinancialOrg;
import com.sunline.pcm.param.def.Product;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.Ownership;
import com.sunline.ppy.dictionary.enums.PlanType;

/**
 * 	与阳光结算支付报表(保费、提前还款违约金分成)
 * 	@author wanghl
 */
public class RA204LoanSettlePayRpt extends KeyBasedStreamReader<RA204Key, LoanSettlePayRptItem>{
	Logger logger = LoggerFactory.getLogger(this.getClass());
	@PersistenceContext
    private EntityManager em;
	@Autowired
	BatchStatusFacility batchStatusFacility;
	@Autowired
	private RptParamFacility codeProv;
	private QCcsSettleLoanHst qSettleLoanHst = QCcsSettleLoanHst.ccsSettleLoanHst;
	private QCcsRepayHst qrepay = QCcsRepayHst.ccsRepayHst;
	private QCcsLoan qLoan = QCcsLoan.ccsLoan;
	private QCcsPlan qPlan = QCcsPlan.ccsPlan;
	private QCcsAcct qAcct = QCcsAcct.ccsAcct;
	QCcsTxnHst qTxnHst = QCcsTxnHst.ccsTxnHst;
	
	@Override
	protected List<RA204Key> loadKeys() {
		Map<String,RA204Key> keymap = new HashMap<String,RA204Key>();
		List<RA204Key> keyList = new ArrayList<RA204Key>();
		
		List<String> productCdList = codeProv.loadProductCdList(Ownership.P, LoanType.MCEI, codeProv.YG_ACQ_ACCEPTOR_ID);
		if(productCdList == null || productCdList.size() <=0 ) return keyList;
		
		CcsSettleLoanHst settle = new JPAQuery(em).from(qSettleLoanHst)
				.where(qSettleLoanHst.settleDate.eq(batchStatusFacility.getBatchDate()))
				.singleResult(qSettleLoanHst);
		if(settle == null){
			return keyList;
		}
		FinancialOrg financialOrg = codeProv.loadParameter(settle.getFinancialOrgNo(), FinancialOrg.class);
		SysTxnCdMapping sysTxnCdMapping = codeProv.loadParameter(SysTxnCd.S75, SysTxnCdMapping.class);
		TxnCd txnCd = codeProv.loadParameter(sysTxnCdMapping.txnCd, TxnCd.class);
		
		List<CcsRepayHst> repayList = new JPAQuery(em).from(qrepay, qAcct)
				.where(qrepay.acctNbr.eq(qAcct.acctNbr)
						.and(qrepay.acctType.eq(qAcct.acctType))
						.and(qAcct.productCd.in(productCdList))
						.and(qrepay.batchDate.between(settle.getBeginDate(), settle.getEndDate()))
						.and(qrepay.acqId.eq(financialOrg.acqAcceptorId))
						.and(qrepay.planType.in(PlanType.I, PlanType.Q))
						.and(qrepay.bnpType.in(
								BucketObject.ctdIns, BucketObject.pastIns, 
								BucketObject.ctdTxnFee, BucketObject.pastTxnFee)))
				.list(qrepay);
		
		for(CcsRepayHst repay : repayList){
			String contrNbr = new JPAQuery(em).from(qLoan,qPlan)
					.where(qLoan.refNbr.eq(qPlan.refNbr)
							.and(qPlan.planId.eq(repay.getPlanId()))
							.and(qPlan.planType.in(PlanType.I, PlanType.Q)))
					.singleResult(qLoan.contrNbr);
			
			if(keymap.containsKey(contrNbr)){
				RA204Key key = keymap.get(contrNbr);
				switch (repay.getBnpType()) {
				case ctdIns:
				case pastIns:
					key.setInsAmt(key.getInsAmt().add(repay.getRepayAmt())); break;
				case ctdTxnFee:
				case pastTxnFee:
					key.setFineAmt(key.getFineAmt().add(repay.getRepayAmt())); break;
				default: break;
				}
			}else{
				RA204Key key = new RA204Key();
				switch (repay.getBnpType()) {
				case ctdIns:
				case pastIns:
					BigDecimal DInsuranceFee = getRepayAmtOfTxnCd(repay.getAcctNbr(), repay.getAcctType(), txnCd.txnCd , financialOrg.acqAcceptorId, settle);
					key.setInsAmt(DInsuranceFee == null?repay.getRepayAmt():repay.getRepayAmt().subtract(DInsuranceFee)); 
					break;
				case ctdTxnFee:
				case pastTxnFee:
					key.setFineAmt(repay.getRepayAmt()); break;
				default: break;
				}
				
				key.setContrNbr(contrNbr);
				if(key.getFineAmt().compareTo(BigDecimal.ZERO)>0 || key.getInsAmt().compareTo(BigDecimal.ZERO)>0){
					keymap.put(contrNbr, key);
				}
			}
		}
		
		Iterator<RA204Key> keyIt = keymap.values().iterator();
		while(keyIt.hasNext()){
			keyList.add(keyIt.next());
		}
		return keyList;
	}

	private BigDecimal getRepayAmtOfTxnCd(Long acctNbr, AccountType acctType, String txnCd, String acqAcceptorId, CcsSettleLoanHst settle ) {
		BigDecimal DInsuranceFee = new JPAQuery(em).from(qTxnHst).where(qTxnHst.acqAcceptorId.eq(acqAcceptorId)
				.and(qTxnHst.txnCode.eq(txnCd))
				.and(qTxnHst.postDate.between(settle.getBeginDate(), settle.getEndDate()))
				.and(qTxnHst.acctNbr.eq(acctNbr))
				.and(qTxnHst.acctType.eq(acctType)))
				.singleResult(qTxnHst.postAmt.sum());
				
		return DInsuranceFee;
	}

	@Override
	protected LoanSettlePayRptItem loadItemByKey(RA204Key key) {
		
		LoanSettlePayRptItem item = new LoanSettlePayRptItem();
		
		Tuple tuple = new JPAQuery(em).from(qLoan, qAcct)
				.where(qLoan.contrNbr.eq(key.getContrNbr())
						.and(qLoan.acctNbr.eq(qAcct.acctNbr)))
				.singleResult(qLoan.loanInitPrin, qLoan.activeDate, qLoan.contrNbr, 
						qAcct.custId, qAcct.name, qAcct.org, qAcct.productCd, qLoan.loanCode);
		
		item.contrNbr = tuple.get(qLoan.contrNbr);
		item.name = tuple.get(qAcct.name);
		item.idNo = tuple.get(qAcct.custId).toString();
		item.activeDate = tuple.get(qLoan.activeDate);
		item.loanAmt = tuple.get(qLoan.loanInitPrin).setScale(2);
		item.paymentDate = batchStatusFacility.getBatchDate();
		item.insuranceFeeAmt = key.getInsAmt()==null?BigDecimal.ZERO.setScale(2):key.getInsAmt().setScale(2, RoundingMode.HALF_UP);
		
		ProductCredit productCredit = codeProv.loadParameter(tuple.get(qAcct.productCd), ProductCredit.class);
		FinancialOrg financialOrg = codeProv.loadParameter(productCredit.financeOrgNo, FinancialOrg.class);
		item.loanSVCFeeAmt = key.getFineAmt() == null?BigDecimal.ZERO.setScale(3):key.getFineAmt().multiply(financialOrg.adFeeScale).setScale(3, RoundingMode.HALF_UP);
//		item.loanSVCFeeAmt = key.getFineAmt().multiply(new BigDecimal("0.5")).setScale(3);
		
		LoanPlan loanPlan = codeProv.loadLoanPlan(tuple.get(qLoan.loanCode));
		Product product = codeProv.loadProduct(tuple.get(qAcct.productCd));
		item.productCd = product.productCode;
		item.productDesc = product.description;
		item.loanCode = loanPlan.loanCode;
		item.loanDesc = loanPlan.description;
		
		return item;
	}

}
