package com.sunline.ccs.batch.rpt.cca210;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
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
import com.sunline.ccs.batch.rpt.cca210.item.MsLoanAppWithYgInsRptItem;
import com.sunline.ccs.batch.rpt.common.RptBatchUtil;
import com.sunline.ccs.facility.MicroCreditRescheduleUtils;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanType;

/**
 * 寿险新增客户日报
 * 
 * @author wanghl
 *
 */
public class RA214MsLoanAppWithYgInsRpt extends KeyBasedStreamReader<Long, MsLoanAppWithYgInsRptItem> {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	@PersistenceContext
	private EntityManager em;

	@Autowired
	private BatchStatusFacility batchStatusFacility;
	@Autowired
	private MicroCreditRescheduleUtils rescheduleUtils;
	@Autowired
	private RptBatchUtil rptBatchUtil;

	private QCcsAcct qAcct = QCcsAcct.ccsAcct;

	private QCcsLoan qLoan = QCcsLoan.ccsLoan;

	private QCcsCustomer qCust = QCcsCustomer.ccsCustomer;

	@Override
	protected List<Long> loadKeys() {
		Date batchDate = batchStatusFacility.getBatchDate();
		// 当日激活并加入寿险计划的Loan
		return new JPAQuery(em)
				.from(qLoan, qAcct)
				.where(qLoan.acctNbr.eq(qAcct.acctNbr)
						.and(qLoan.acctType.eq(qAcct.acctType))
						.and(qLoan.activeDate.eq(batchDate)
								.or(qAcct.lastPmtDueDate.eq(batchDate).and(qAcct.currBal.gt(BigDecimal.ZERO)))
							)
						.and(qLoan.joinLifeInsuInd.eq(Indicator.Y))).list(qLoan.loanId);
	}

	@Override
	protected MsLoanAppWithYgInsRptItem loadItemByKey(Long key) {
		Date batchDate = batchStatusFacility.getBatchDate();
		MsLoanAppWithYgInsRptItem item = new MsLoanAppWithYgInsRptItem();
		CcsLoan loan = em.find(CcsLoan.class, key);
		Tuple custAcct = new JPAQuery(em)
				.from(qCust, qAcct)
				.where(qAcct.custId.eq(qCust.custId).and(qAcct.acctNbr.eq(loan.getAcctNbr()))
						.and(qAcct.acctType.eq(loan.getAcctType())))
				.singleResult(qCust.name, qCust.idType, qCust.idNo, qAcct.name, qAcct.creditLmt, 
						qAcct.gender, qAcct.nextStmtDate, qAcct.lastPmtDueDate, qAcct.productCd);
		item.name = custAcct.get(qAcct.name);
		item.gender = custAcct.get(qAcct.gender);
		item.idType = custAcct.get(qCust.idType);
		item.idNo = custAcct.get(qCust.idNo);
		
		Date beginDate = null;
		if(batchDate.equals(loan.getActiveDate()))
			beginDate = loan.getActiveDate();
		else if(batchDate.equals(custAcct.get(qAcct.lastPmtDueDate))){
			beginDate = custAcct.get(qAcct.lastPmtDueDate);
		}
		else
			return null;
		
		item.lifeInsValidDate = beginDate;
		// 取参数前设置机构号
		rptBatchUtil.setCurrOrgNoToContext(loan.getOrg());
		// 结束日期统一为下个还款日前一天
		Date nextPmtDueDate = rescheduleUtils.getNextPaymentDay(custAcct.get(qAcct.productCd), custAcct.get(qAcct.nextStmtDate));
		Calendar cldEndDate = Calendar.getInstance();
		cldEndDate.setTime(nextPmtDueDate);
		cldEndDate.add(Calendar.DAY_OF_MONTH, -1);
		item.lifeInsEndDate = cldEndDate.getTime();
		
		if(loan.getLoanType().equals(LoanType.MCAT)){
			item.loanAmt = custAcct.get(qAcct.creditLmt).multiply(new BigDecimal(6)).divide(new BigDecimal(10)).setScale(2, RoundingMode.HALF_UP);
			
		}else if(loan.getLoanType().equals(LoanType.MCEI)){
			item.loanAmt = loan.getLoanInitPrin().setScale(2, RoundingMode.HALF_UP);

		}else
			return null;
		
		return item;
	}

}
