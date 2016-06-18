package com.sunline.ccs.batch.rpt.cca200;

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
import com.mysema.query.types.expr.BooleanExpression;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.batch.KeyBasedStreamReader;
import com.sunline.ccs.batch.rpt.cca200.items.LoanRptItem;
import com.sunline.ccs.batch.rpt.common.RptParamFacility;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.pcm.param.def.Product;
import com.sunline.ppy.dictionary.enums.LoanStatus;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.OrderStatus;
import com.sunline.ppy.dictionary.enums.Ownership;

/**
 * 放款结果查询送报表
 * @author wanghl
 *
 */
public class RA201LoanRpt extends KeyBasedStreamReader<Long, LoanRptItem>  {
	private static final Logger logger = LoggerFactory.getLogger(RA201LoanRpt.class);
	@PersistenceContext
    private EntityManager em;
	@Autowired
	BatchStatusFacility batchStatusFacility;
	@Autowired
	private RptParamFacility codeProv;

	private QCcsAcct qAcct = QCcsAcct.ccsAcct;
	private QCcsLoan qLoan = QCcsLoan.ccsLoan;
	@Override
	protected List<Long> loadKeys() {
		JPAQuery query = new JPAQuery(em);
		Date batchDate = batchStatusFacility.getBatchDate();
		List<String> loanCdList = codeProv.loadLoanCdList(Ownership.P, LoanType.MCEI, codeProv.YG_ACQ_ACCEPTOR_ID);
		if(loanCdList == null || loanCdList.size() <= 0) return new ArrayList<Long>();
		
		BooleanExpression exp = 
				qLoan.activeDate.eq(batchDate)
					.and(qLoan.loanStatus.eq(LoanStatus.A))
					.and(qLoan.loanCode.in(loanCdList));
		
		return query.from(qLoan).where(exp).list(qLoan.loanId);
	}

	@Override
	protected LoanRptItem loadItemByKey(Long key) {
		
		CcsLoan loan = em.find(CcsLoan.class, key);
		Tuple acct = new JPAQuery(em).from(qAcct)
				.where(qAcct.acctNbr.eq(loan.getAcctNbr())
						.and(qAcct.acctType.eq(loan.getAcctType())))
				.singleResult(qAcct.name, qAcct.productCd, qAcct.custId);
		logger.info("合同号："+loan.getContrNbr());
		LoanRptItem item = new LoanRptItem();
		
		Product product = codeProv.loadProduct(acct.get(qAcct.productCd));
		LoanPlan loanPlan = codeProv.loadLoanPlan(loan.getLoanCode());
		item.productCd = product.productCode;
		item.productDesc = product.description;
		item.loanCode = loanPlan.loanCode;
		item.loanDesc = loanPlan.description;
		
		item.contrNbr = loan.getContrNbr();
		item.name = acct.get(qAcct.name);
		item.idNo = acct.get(qAcct.custId).toString();
		item.loanDate = loan.getActiveDate();
		item.orderStatus = OrderStatus.S; //TODO 怎么取该贷款申请的订单状态
		item.loanAmt = loan.getLoanInitPrin().setScale(2, RoundingMode.HALF_UP);
		item.loanTerm = loan.getLoanInitTerm();
		item.interestRate = loan.getInterestRate();
		item.insRate = loan.getInsuranceRate();
		return item;
	}


}
