package com.sunline.ccs.batch.front;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.batch.KeyBasedStreamReader;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctKey;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoanReg;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanStatus;
import com.sunline.ppy.dictionary.enums.LoanUsage;

/**
 * 提前还款
 * @author zhangqiang
 *
 */
public class R3000Prepayment extends KeyBasedStreamReader<Long, SFrontInfo> {
	
	@Autowired
	private FrontBatchUtil frontBatchUtil;
	
	@Autowired
	private BatchStatusFacility batchStatusFacility;
	
	@PersistenceContext
	private EntityManager em;
	
	@Override
	protected SFrontInfo loadItemByKey(Long key) {
		CcsLoanReg loanReg = em.find(CcsLoanReg.class, key);
		// 只要有处理中订单就不代扣
		if(frontBatchUtil.getWOrderCount(loanReg.getAcctNbr(), loanReg.getAcctType())>0)
			return null;
		// 若当日有成功的提前实时代扣则不代扣
		if(frontBatchUtil.getOrderCount(loanReg.getAcctNbr(), loanReg.getAcctType(), Indicator.Y, null, LoanUsage.M)>0)
			return null;
		
		SFrontInfo info = new SFrontInfo();
		
		CcsAcct acct = em.find(CcsAcct.class, new CcsAcctKey(loanReg.getAcctNbr(), loanReg.getAcctType()));
		CcsCustomer cust = em.find(CcsCustomer.class, acct.getCustId());
		
		QCcsLoan l = QCcsLoan.ccsLoan;
		CcsLoan loan = new JPAQuery(em).from(l).where(l.acctNbr.eq(loanReg.getAcctNbr()).and(l.acctType.eq(loanReg.getAcctType()))
							.and(l.dueBillNo.eq(loanReg.getDueBillNo())).and(l.loanStatus.eq(LoanStatus.A)))
						.singleResult(l);
		
		info.setAcct(acct);
		info.setCust(cust);
		info.setLoan(loan);
		info.setLoanReg(loanReg);
		
		return info;
	}

	@Override
	protected List<Long> loadKeys() {
		QCcsLoanReg qReg = QCcsLoanReg.ccsLoanReg;
		// 预约还款日 = 业务日期
		return new JPAQuery(em).from(qReg)
				.where(qReg.preAdDate.eq(batchStatusFacility.getSystemStatus().getBusinessDate()))
				.list(qReg.registerId);
	}
	
	
	
}
