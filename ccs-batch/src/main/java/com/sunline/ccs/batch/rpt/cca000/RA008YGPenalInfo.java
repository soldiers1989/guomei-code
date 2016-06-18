package com.sunline.ccs.batch.rpt.cca000;

import java.math.BigDecimal;
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
import com.mysema.query.types.expr.BooleanExpression;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.batch.KeyBasedStreamReader;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.batch.rpt.cca000.items.YGPenalInfoItem;
import com.sunline.ccs.batch.rpt.common.RptParamFacility;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctKey;
import com.sunline.ccs.infrastructure.shared.model.CcsRepayHst;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsRepayHst;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.enums.BucketObject;
import com.sunline.pcm.param.def.FinancialOrg;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.Ownership;

/**
 * 提前还款时所收违约金信息
 * @author wanghl
 *
 */
public class RA008YGPenalInfo extends KeyBasedStreamReader<Long, YGPenalInfoItem>{
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	@PersistenceContext
    private EntityManager em;
	@Autowired
	private UnifiedParameterFacility parameter;
	@Autowired
	private BatchStatusFacility batchStatusFacility;
	@Autowired
	private RptParamFacility codeProv;
	
	@Autowired
	private RCcsAcct rCcsAcct;
	
	private QCcsRepayHst qRepay = QCcsRepayHst.ccsRepayHst;
	private QCcsLoan qLoan = QCcsLoan.ccsLoan;
	private QCcsAcct qAcct = QCcsAcct.ccsAcct;
	
	@Override
	protected List<Long> loadKeys() {
		Date batchDate = batchStatusFacility.getBatchDate();
		List<String> productCdList = codeProv.loadProductCdList(Ownership.P, LoanType.MCEI, codeProv.YG_ACQ_ACCEPTOR_ID);
		if(productCdList == null || productCdList.size() <=0 ) return new ArrayList<Long>();
		
		BooleanExpression exp = 
				qRepay.acctNbr.eq(qAcct.acctNbr).and(qRepay.acctType.eq(qAcct.acctType))
				.and(qAcct.productCd.in(productCdList))
				.and(qRepay.batchDate.eq(batchDate ))
				.and(qRepay.bnpType.in(BucketObject.ctdTxnFee, BucketObject.pastTxnFee));
		return new JPAQuery(em).from(qRepay, qAcct).where(exp).distinct().list(qRepay.planId);
	}

	@Override
	protected YGPenalInfoItem loadItemByKey(Long key) {
		Date batchDate = batchStatusFacility.getBatchDate();
		List<CcsRepayHst> repayHsts = new JPAQuery(em).from(qRepay)
			.where(qRepay.batchDate.eq(batchDate )
					.and(qRepay.planId.eq(key))
					.and(qRepay.bnpType.in(BucketObject.ctdTxnFee, BucketObject.pastTxnFee)))
			.list(qRepay);
		Tuple loanTuple = new JPAQuery(em).from(qLoan)
				.where(qLoan.acctNbr.eq(repayHsts.get(0).getAcctNbr()).and(qLoan.acctType.eq(repayHsts.get(0).getAcctType())))
				.singleResult(qLoan.dueBillNo, qLoan.guarantyId, qLoan.cardNbr);
		
		YGPenalInfoItem item = new YGPenalInfoItem();
		
		item.putOutNo = loanTuple.get(qLoan.dueBillNo);
		item.guarantyID = loanTuple.get(qLoan.guarantyId);
		
		BigDecimal sum = new BigDecimal(0);
		for(CcsRepayHst repay : repayHsts){
			logger.info("planId[{}]paymentID[{}]acct[{}][{}]",key,repay.getPaymentId(),repay.getAcctNbr(),repay.getAcctType());
			sum = sum.add(repay.getRepayAmt());
		}
		CcsAcct acct = rCcsAcct.findOne(new CcsAcctKey(repayHsts.get(0).getAcctNbr(), repayHsts.get(0).getAcctType()));
		OrganizationContextHolder.setCurrentOrg(acct.getOrg());
		ProductCredit pc = parameter.loadParameter(acct.getProductCd(), ProductCredit.class);
		FinancialOrg financialOrg = parameter.loadParameter(pc.financeOrgNo, FinancialOrg.class);
		
		item.fbPaymentAcctNo = acct.getDdBankAcctNbr();
		item.penalsum = sum.multiply(financialOrg.adFeeScale).setScale(2, BigDecimal.ROUND_HALF_UP);
		item.branchBank = "";
		item.subBranchBank = "";
	
		return item;
	}

}
