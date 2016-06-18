package com.sunline.ccs.batch.rpt.cca000;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.batch.KeyBasedStreamReader;
import com.sunline.ccs.batch.rpt.common.RptParamFacility;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctKey;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoanReg;
import com.sunline.ccs.param.def.enums.LoanTerminateReason;
import com.sunline.ppy.dictionary.enums.LoanRegStatus;
import com.sunline.ppy.dictionary.enums.LoanStatus;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.Ownership;

/**
 * 送阳光-贷款明细文件
 * @author wanghl
 *
 */
public class RA001YGLoanDetail extends KeyBasedStreamReader<RA001Key, SA001YGLoanDetailInfo> {
	private Logger logger = LoggerFactory.getLogger(RA001YGLoanDetail.class);
	@PersistenceContext
    private EntityManager em;
	
	@Autowired
	private BatchStatusFacility batchStatusFacility;
	
	@Autowired
	private RptParamFacility codeProv;

	private QCcsLoan qLoan = QCcsLoan.ccsLoan;
	private QCcsLoanReg qLoanReg = QCcsLoanReg.ccsLoanReg;
	
	@Override
	protected List<RA001Key> loadKeys() {
		List<RA001Key> keyList = new ArrayList<RA001Key>();
		Date batchDate = batchStatusFacility.getBatchDate();
		
		List<String> loanCdList = codeProv.loadLoanCdList(Ownership.P, LoanType.MCEI, codeProv.YG_ACQ_ACCEPTOR_ID);
		if(loanCdList == null || loanCdList.size() <= 0) return keyList;
		
		/* 
		 * 新建贷款：激活日期为批量日期。
		 * 终止贷款：贷款状态为终止，且还清日期为批量日期。
		 * 贷款正常结清： 贷款状态F，还清日期paidOutDate为批量日期
		 * 放款处理中:贷款申请LoanReg注册状态为F|实时放款失败。
		 * 放款失败：贷款申请LoanReg注册状态为C|放款中。
		 */
		List<Long> loanIds = new JPAQuery(em).from(qLoan)
				.where(qLoan.loanCode.in(loanCdList)
						.and(qLoan.activeDate.eq(batchDate)
							.or(qLoan.loanStatus.eq(LoanStatus.T )
									.and(qLoan.terminalReasonCd.eq(LoanTerminateReason.C))
									.and(qLoan.paidOutDate.isNotNull().and(qLoan.paidOutDate.eq(batchDate)))
								)
							.or(qLoan.loanStatus.eq(LoanStatus.T)
									.and(qLoan.terminalReasonCd.eq(LoanTerminateReason.P))
									.and(qLoan.paidOutDate.isNotNull().and(qLoan.paidOutDate.eq(batchDate))))
							.or(qLoan.loanStatus.eq(LoanStatus.F)
									.and(qLoan.paidOutDate.isNotNull().and(qLoan.paidOutDate.eq(batchDate))))))
				.list(qLoan.loanId);
		
		List<Long> regIds = new JPAQuery(em).from(qLoanReg)
				.where(qLoanReg.loanCode.in(loanCdList)
						.and(qLoanReg.loanRegStatus.in(LoanRegStatus.C, LoanRegStatus.F))
						.and(qLoanReg.registerDate.eq(batchDate)))
				.list(qLoanReg.registerId);
				
		for(Long id : loanIds){
			keyList.add(new RA001Key(id, true));
		}
		for(Long id : regIds){
			keyList.add(new RA001Key(id, false));
		}
		return keyList;
	}

	@Override
	protected SA001YGLoanDetailInfo loadItemByKey(RA001Key key) {

		SA001YGLoanDetailInfo info = new SA001YGLoanDetailInfo();
		CcsAcct acct = null;
		if(key.getIsLoanEstablished()){
			logger.info("LoanId[{}]", key.getId());
			CcsLoan loan = em.find(CcsLoan.class, key.getId());
			acct = em.find(CcsAcct.class, new CcsAcctKey(loan.getAcctNbr(), loan.getAcctType()));
			info.setCustomer(em.find(CcsCustomer.class, acct.getCustId()));
			info.setLoan(loan);
		}else{
			logger.info("LoanRegId[{}]", key.getId());
			CcsLoanReg loanReg = em.find(CcsLoanReg.class, key.getId());
			acct = em.find(CcsAcct.class, new CcsAcctKey(loanReg.getAcctNbr(), loanReg.getAcctType()));
			info.setCustomer(em.find(CcsCustomer.class, acct.getCustId()));
			info.setLoanReg(loanReg);
		}
		info.setDdBankAcctNbr(acct.getDdBankAcctNbr());
		info.setIsLoanEstablished(key.getIsLoanEstablished());
		return info; 
	}

}
