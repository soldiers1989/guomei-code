/**
 * 
 */
package com.sunline.ccs.batch.rpt.cca230;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.query.Tuple;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.QTuple;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.batch.KeyBasedStreamReader;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;

/**  
 * @描述		: 生成合同状态对账文件reader
 *  
 * @作者		: JiaoJian 
 * @创建时间	: 2015年11月26日  下午3:39:00   
 */
public class RA231YZFContractStatusCompareFile extends KeyBasedStreamReader<Long, ContractCompareCcsLoanKey> {

	@PersistenceContext
	private EntityManager em;
	
	@Autowired
	private BatchStatusFacility batchStatusFacility;
	
	@Override
	protected List<Long> loadKeys() {
		JPAQuery query = new JPAQuery(em);
		QCcsLoan qccsLoan = QCcsLoan.ccsLoan;
		Date batchDate = batchStatusFacility.getBatchDate();
		
		return query.from(qccsLoan)
					.where(qccsLoan.activeDate.eq(batchDate).or(qccsLoan.paidOutDate.eq(batchDate)))
					.list(qccsLoan.loanId);
	}

	@Override
	protected ContractCompareCcsLoanKey loadItemByKey(Long key) {
		JPAQuery query = new JPAQuery(em);
		QCcsLoan qccsLoan = QCcsLoan.ccsLoan;
		
		Tuple singleResult = query.from(qccsLoan).where(qccsLoan.loanId.eq(key))
							      .singleResult(new QTuple(qccsLoan.createTime, qccsLoan.paidOutDate, 
							    		 qccsLoan.contrNbr, qccsLoan.loanInitPrin, qccsLoan.acctNbr,
							    		 qccsLoan.activeDate, qccsLoan.paidOutDate));
		
		
		ContractCompareCcsLoanKey ccsLoanKey = new ContractCompareCcsLoanKey();
		ccsLoanKey.setLoanId(key);
		ccsLoanKey.setCreateTime(singleResult.get(qccsLoan.createTime));
		ccsLoanKey.setContrNbr(singleResult.get(qccsLoan.contrNbr));
		ccsLoanKey.setLoanInitPrin(singleResult.get(qccsLoan.loanInitPrin));
		ccsLoanKey.setAcctNbr(singleResult.get(qccsLoan.acctNbr));
		ccsLoanKey.setActiveDate(singleResult.get(qccsLoan.activeDate));
		ccsLoanKey.setPaidOutDate(singleResult.get(qccsLoan.paidOutDate));
		
		return ccsLoanKey;
	}

}
