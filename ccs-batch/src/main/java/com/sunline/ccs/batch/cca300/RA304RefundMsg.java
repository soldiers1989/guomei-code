package com.sunline.ccs.batch.cca300;

import java.math.BigDecimal;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.batch.KeyBasedStreamReader;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanRegHst;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoanRegHst;
import com.sunline.ppy.dictionary.enums.LoanAction;
import com.sunline.ppy.dictionary.enums.LoanRegStatus;
/**
 * 退货欠款提醒短信 reader
 * @author lizz
 *
 */
public class RA304RefundMsg extends KeyBasedStreamReader<Long, CcsLoanRegHst>{

	@PersistenceContext
	private EntityManager em;
	QCcsLoanRegHst qLoanRegHst = QCcsLoanRegHst.ccsLoanRegHst;
	QCcsAcct qAcct = QCcsAcct.ccsAcct;
	@Autowired
	private BatchStatusFacility batchFacility;
	
	@Override
	protected List<Long> loadKeys() {
		List<Long> keyList = new JPAQuery(em).from(qLoanRegHst,qAcct)
				.where(qLoanRegHst.loanRegStatus.eq(LoanRegStatus.A)
				.and(qLoanRegHst.loanAction.eq(LoanAction.T)
				.and(qLoanRegHst.registerDate.eq(batchFacility.getBatchDate()))
				.and(qLoanRegHst.acctNbr.eq(qAcct.acctNbr))
				.and(qLoanRegHst.acctType.eq(qAcct.acctType))
//				.and(qAcct.currBal.gt(BigDecimal.ZERO))
				)).list(qLoanRegHst.registerId);
		return keyList;
	}

	@Override
	protected CcsLoanRegHst loadItemByKey(Long key) {
		return em.find(CcsLoanRegHst.class,key);
	}

}
