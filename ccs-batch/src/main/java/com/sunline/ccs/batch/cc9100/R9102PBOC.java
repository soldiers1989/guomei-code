package com.sunline.ccs.batch.cc9100;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsPlan;
import com.sunline.ccs.infrastructure.shared.model.QCcsRepaySchedule;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ark.batch.KeyBasedStreamReader;
import com.mysema.query.jpa.impl.JPAQuery;

/**
 * @see 类名：R9102PBOC
 * @see 描述： 人行征信报送文件
 *
 * @see 创建日期：   2015-6-24下午2:48:01
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class R9102PBOC extends KeyBasedStreamReader<Long, S9102PBOC> {
	@PersistenceContext
	protected EntityManager em;

	@Override
	protected List<Long> loadKeys() {
		QCcsLoan q = QCcsLoan.ccsLoan;
		/**
		 * 查询贷款记录，报送时间点为
		 * 1 开户日registerDate = batchDate 
		 * 2 应还款日expiryDate = batchDate
		 * 3 宽限日graceDate=batchDate
		 * 4 结清日paidOutDate=batchDate
		 * 5 提前结束日terminateDate=batchDate
		 * 6 月底 isMonthEnd
		 */
		return new JPAQuery(em).from(q).where(q.loanType.in(LoanType.MCAT, LoanType.MCEI, LoanType.MCEP)).list(q.loanId);
		
	}

	@Override
	protected S9102PBOC loadItemByKey(Long key) {
		S9102PBOC i = new S9102PBOC();
		CcsLoan loan = em.find(CcsLoan.class, key);
		i.setTmLoan(loan);
		
		QCcsRepaySchedule qTmSchedule = QCcsRepaySchedule.ccsRepaySchedule;
		i.setSchedules(new JPAQuery(em)
			.from(qTmSchedule)
			.where(qTmSchedule.loanId.eq(loan.getLoanId()))
			.list(qTmSchedule));
		QCcsPlan qTmPlan = QCcsPlan.ccsPlan;
		i.setPlans(new JPAQuery(em)
				.from(qTmPlan)
				.where(qTmPlan.acctNbr.eq(loan.getAcctNbr()).and(qTmPlan.acctType.eq(loan.getAcctType())).and(qTmPlan.refNbr.eq(loan.getRefNbr())))
				.list(qTmPlan));
		QCcsAcct qAccount = QCcsAcct.ccsAcct;
		i.setAcct(new JPAQuery(em).from(qAccount)
			.where(qAccount.acctNbr.eq(loan.getAcctNbr()).and(qAccount.acctType.eq(loan.getAcctType()))).singleResult(qAccount));
		return i;
	}

}
