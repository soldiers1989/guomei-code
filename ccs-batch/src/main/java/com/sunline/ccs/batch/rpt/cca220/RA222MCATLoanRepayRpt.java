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
import com.sunline.ccs.batch.rpt.cca220.item.MCATLoanRepayRptItem;
import com.sunline.ccs.batch.rpt.common.RptBatchUtil;
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
 * 还款结果查询报表文件
 * @author wanghl
 *
 */
public class RA222MCATLoanRepayRpt extends KeyBasedStreamReader<CcsAcctKey,MCATLoanRepayRptItem>{
	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@PersistenceContext
    private EntityManager em;
	@Autowired
	BatchStatusFacility batchStatusFacility;
	@Autowired
	private RptParamFacility codeProv;
	@Autowired
	private RptBatchUtil rptBatchUtil;

	private QCcsRepayHst qRepay = QCcsRepayHst.ccsRepayHst;
	private QCcsLoan qLoan = QCcsLoan.ccsLoan;
	private QCcsAcct qAcct = QCcsAcct.ccsAcct;
	
	@Override
	protected List<CcsAcctKey> loadKeys() {
		Date batchDate = batchStatusFacility.getBatchDate();
		List<CcsAcctKey> keyList = new ArrayList<CcsAcctKey>();

		List<String> productCdList = codeProv.loadProductCdList(Ownership.O, LoanType.MCAT, null);
		if(productCdList == null || productCdList.size() <=0 ) return keyList;
		
		List<Tuple> repayTuple = new JPAQuery(em).from(qRepay, qAcct)
				.where(qRepay.batchDate.eq(batchDate)
						.and(qRepay.acctNbr.eq(qAcct.acctNbr)
								.and(qRepay.acctType.eq(qAcct.acctType))
								.and(qRepay.planType.in(PlanType.J, PlanType.L))
								.and(qAcct.productCd.in(productCdList)))
							)
				.distinct()
				.list(qRepay.acctNbr, qRepay.acctType);
		for(Tuple t : repayTuple){
			CcsAcctKey key = new CcsAcctKey(t.get(qRepay.acctNbr), t.get(qRepay.acctType));
			keyList.add(key);
		}
		return keyList;
	}

	@Override
	protected MCATLoanRepayRptItem loadItemByKey(CcsAcctKey key) {
		MCATLoanRepayRptItem item = new MCATLoanRepayRptItem();
		rptBatchUtil.setCurrOrgNoToContext();
		Date batchDate = batchStatusFacility.getBatchDate();
		
		logger.info("=====账户[{}][{}]=====", key.getAcctNbr(), key.getAcctType());
		List<Tuple> accts = new JPAQuery(em).from(qAcct)
				.where(qAcct.acctNbr.eq(key.getAcctNbr()).and(qAcct.acctType.eq(key.getAcctType())))
				.list(qAcct.name, qAcct.custId, qAcct.productCd, qAcct.org);
		Tuple acct = accts.get(0);
		item.name = acct.get(qAcct.name);
		item.idNo = acct.get(qAcct.custId).toString();
		String productCd  = acct.get(qAcct.productCd);

		//获取当日所有还款分配信息
		List<CcsRepayHst> repayExList = new JPAQuery(em).from(qRepay)
				.where(qRepay.acctNbr.eq(key.getAcctNbr())
						.and(qRepay.acctType.eq(key.getAcctType()))
						.and(qRepay.batchDate.eq(batchDate))
						.and(qRepay.planType.in(PlanType.J, PlanType.L))
						).list(qRepay);
		
		logger.info("=====账户[{}][{}]还款分配信息[{}]条", key.getAcctNbr(), key.getAcctType(), repayExList.size());
		
		//还款分配按交易参考号分组，汇总相同余额成分的分配金额
		addBnpAmt(item, repayExList);
		
		Tuple loan = new JPAQuery(em).from(qLoan)
				.where(qLoan.acctNbr.eq(key.getAcctNbr())
						.and(qLoan.acctType.eq(key.getAcctType()))
				).singleResult(qLoan.loanInitPrin, qLoan.contrNbr, qLoan.activeDate, qLoan.loanCode);
		logger.info("合同号[{}]",loan.get(qLoan.contrNbr));
		
		item.contrNbr = loan.get(qLoan.contrNbr);
		
		Product product = codeProv.loadParameter(productCd , Product.class, acct.get(qAcct.org));
		LoanPlan loanPlan = codeProv.loadParameter(loan.get(qLoan.loanCode),LoanPlan.class, acct.get(qAcct.org));

		item.productCd = product.productCode;
		item.productDesc = product.description;
		item.loanCode = loanPlan.loanCode;
		item.loanDesc = loanPlan.description;
		
		item.repayDate = batchDate;
		return item;
	}


	/**
	 * 累加同一Loan同余额成分的金额
	 */
	private void addBnpAmt(MCATLoanRepayRptItem item, List<CcsRepayHst> repayList) {
		
		BigDecimal repayPrincipal = new BigDecimal(0);
		BigDecimal repayInterest = new BigDecimal(0);
		BigDecimal repaySvcFee = new BigDecimal(0);
		BigDecimal repayTxnFee = new BigDecimal(0);
		BigDecimal repayLpc = new BigDecimal(0);
		BigDecimal repayAnnualFee = new BigDecimal(0);
		BigDecimal repayPenalty = new BigDecimal(0);
		BigDecimal repayLifeInsAmt = new BigDecimal(0);
		BigDecimal repayAmtsucc = new BigDecimal(0);
		
		
		for(CcsRepayHst repay : repayList){
			logger.info("repayId[{}]",  repay.getPaymentId());
			
			repayAmtsucc = repayAmtsucc.add(repay.getRepayAmt());
			
			switch (repay.getBnpType()) {
			case pastPrincipal:
			case ctdPrincipal: repayPrincipal = repayPrincipal.add(repay.getRepayAmt()); break;
			case pastInterest:
			case ctdInterest: repayInterest = repayInterest.add(repay.getRepayAmt()); break;
			case ctdTxnFee: 
			case pastTxnFee: repayTxnFee = repayTxnFee.add(repay.getRepayAmt()); break;
			case pastLpc:
			case ctdLpc: repayLpc = repayLpc.add(repay.getRepayAmt()); break;
			case pastCardFee:
			case ctdCardFee: repayAnnualFee = repayAnnualFee.add(repay.getRepayAmt()); break;
			case pastSvcFee:
			case ctdSvcFee: repaySvcFee = repaySvcFee.add(repay.getRepayAmt()); break;
			case pastLifeInsuFee:
			case ctdLifeInsuFee: repayLifeInsAmt = repayLifeInsAmt.add(repay.getRepayAmt()); break;
			case pastPenalty:
			case ctdPenalty: repayPenalty = repayPenalty.add(repay.getRepayAmt()); break;
			default: break;
			}
		}
		item.actualRepayInterest = repayInterest.setScale(2, RoundingMode.HALF_UP);
		item.actualRepaySvcFee = repaySvcFee.setScale(2, RoundingMode.HALF_UP);
		item.actualRepayLpc = repayLpc.setScale(2, RoundingMode.HALF_UP);
		item.actualRepayPrincipal = repayPrincipal.setScale(2, RoundingMode.HALF_UP);
		item.actualRepayTxnFee = repayTxnFee.setScale(2, RoundingMode.HALF_UP);
		item.actualRepayAnnualFee = repayAnnualFee.setScale(2, RoundingMode.HALF_UP);
		item.actualRepayPenalty = repayPenalty.setScale(2, RoundingMode.HALF_UP);
		item.actualRepayLifeInsFee = repayLifeInsAmt.setScale(2, RoundingMode.HALF_UP);
		item.repayAmt = repayAmtsucc.setScale(2, RoundingMode.HALF_UP);
	}
}
