package com.sunline.ccs.batch.rpt.cca200;

import java.math.RoundingMode;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.query.Tuple;
import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.batch.KeyBasedStreamReader;
import com.sunline.ccs.batch.rpt.cca200.items.LoanSettleConfirmRptItem;
import com.sunline.ccs.batch.rpt.common.RptParamFacility;
import com.sunline.ccs.infrastructure.shared.model.CcsSettleClaim;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsSettleClaim;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.pcm.param.def.Product;
import com.sunline.ppy.dictionary.enums.Indicator;

/**
 * 理赔结果报表文件
 * @author wanghl
 *
 */
public class RA205LoanSettleConfirmRpt extends KeyBasedStreamReader<Long, LoanSettleConfirmRptItem> {
	@PersistenceContext
    private EntityManager em;
	@Autowired
	BatchStatusFacility batchStatusFacility;
	@Autowired
	private RptParamFacility codeProv;
	
	private QCcsLoan qLoan = QCcsLoan.ccsLoan;
	private QCcsAcct qAcct = QCcsAcct.ccsAcct;
	private QCcsSettleClaim qClaim = QCcsSettleClaim.ccsSettleClaim;
	@Override
	protected List<Long> loadKeys() {
		Date batchDate = batchStatusFacility.getBatchDate();
		
		List<Long> claim = new JPAQuery(em).from(qClaim)
				.where(qClaim.settleSucDate.eq(batchDate)
						.and(qClaim.settleFlag.eq(Indicator.Y)))
				.list(qClaim.loanId);
		return claim;
	}

	@Override
	protected LoanSettleConfirmRptItem loadItemByKey(Long key) {
		
		CcsSettleClaim settle = em.find(CcsSettleClaim.class, key);
		Tuple acct = new JPAQuery(em).from(qAcct)
				.where(qAcct.acctNbr.eq(settle.getAcctNbr()))
				.singleResult(qAcct.name, qAcct.custId, qAcct.productCd);
		Tuple loan = new JPAQuery(em).from(qLoan)
				.where(qLoan.loanId.eq(settle.getLoanId()))
				.singleResult(qLoan.activeDate, qLoan.loanCode);
		
		LoanSettleConfirmRptItem item = new LoanSettleConfirmRptItem();
		item.contrNbr = settle.getContrNbr();
		item.name = acct.get(qAcct.name);
		item.idNo = acct.get(qAcct.custId).toString();
		item.activeDate = loan.get(qLoan.activeDate);//FIXME settleClaim增加贷款激活日期
		item.loanAmt = settle.getLoanInitPrin().setScale(2, RoundingMode.HALF_UP);
		item.settleClaimPri = settle.getSettlePrincipal().setScale(2, RoundingMode.HALF_UP);
		item.settleClaimInt = settle.getSettleInterest().setScale(2, RoundingMode.HALF_UP);
		item.settleClaimMulct = settle.getSettleMulct().setScale(2, RoundingMode.HALF_UP);
		item.settleClaimDate = settle.getSettleDate();
		
		LoanPlan loanPlan = codeProv.loadLoanPlan(loan.get(qLoan.loanCode));
		Product product = codeProv.loadProduct(acct.get(qAcct.productCd));
		item.productCd = product.productCode;
		item.productDesc = product.description;
		item.loanCode = loanPlan.loanCode;
		item.loanDesc = loanPlan.description;
		return item;
	}

}
