/**
 * 
 */
package com.sunline.ccs.batch.cc3000;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.ark.batch.KeyBasedStreamReader;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ppy.dictionary.enums.LoanStatus;
import com.sunline.ppy.dictionary.enums.LoanType;

/**  
 * @描述		: 自动豁免Reader
 *  
 * @作者		: JiaoJian 
 * @创建时间	: 2015年11月12日  上午10:15:19   
 */
public class R3011AutoExempt extends KeyBasedStreamReader<Long, CcsLoan>{
	
	@PersistenceContext
	private EntityManager em;

	@Override
	protected List<Long> loadKeys() {
		QCcsLoan q = QCcsLoan.ccsLoan;
		/**
		 * 查询已逾期（CPD）,状态为T/F
		 */
		return new JPAQuery(em).from(q)
				.where(q.cpdBeginDate.isNotNull()
				  .and(q.loanStatus.in(LoanStatus.T, LoanStatus.F)
				  .and(q.paidOutDate.isNull())
				  .and(q.loanType.eq(LoanType.MCEI)))
				 )
				.list(q.loanId);
	}

	@Override
	protected CcsLoan loadItemByKey(Long key) {
		return em.find(CcsLoan.class, key);
				
	}

}
