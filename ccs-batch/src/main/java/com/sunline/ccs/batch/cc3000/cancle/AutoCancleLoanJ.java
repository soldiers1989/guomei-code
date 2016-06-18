package com.sunline.ccs.batch.cc3000.cancle;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ccs.facility.BlockCodeUtils;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnHst;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnUnstatement;
import com.sunline.ccs.infrastructure.shared.model.QCcsTxnHst;
import com.sunline.ccs.infrastructure.shared.model.QCcsTxnUnstatement;

@Component
public class AutoCancleLoanJ implements AutoCancle {
	
	
	@Autowired
	private BatchStatusFacility batchFacility;
	
	@PersistenceContext
	private EntityManager em;
	
	@Autowired
	private CustAcctCardFacility queryFacility;
	
	@Autowired
	private BlockCodeUtils blockcodeUtils;
	//逾期终止锁定码
	private static final String D_CODE = "D";
	
	@Override
	public void cancle(CcsLoan loan) {
		// 获取账户
		CcsAcct acct = queryFacility.getAcctByAcctNbr(loan.getAcctType(), loan.getAcctNbr());
		
		//设置交易账单为旧批量日期的改为当日
		QCcsTxnUnstatement q = QCcsTxnUnstatement.ccsTxnUnstatement;
		QCcsTxnHst qtxn = QCcsTxnHst.ccsTxnHst;
		List<CcsTxnUnstatement> txnUnstatements = new JPAQuery(em)
			.from(q).where(q.acctNbr.eq(acct.getAcctNbr())
				.and(q.acctType.eq(acct.getAcctType()))
				.and(q.stmtDate.eq(acct.getNextStmtDate()))
				).orderBy(q.txnSeq.asc()).list(q);
		List<CcsTxnHst> ccsTxnHsts = new JPAQuery(em)
		.from(qtxn).where(qtxn.acctNbr.eq(acct.getAcctNbr())
			.and(qtxn.acctType.eq(acct.getAcctType()))
			.and(qtxn.stmtDate.eq(acct.getNextStmtDate()))
			).orderBy(qtxn.txnSeq.asc()).list(qtxn);
		
		if(txnUnstatements.size()>0){
			for(CcsTxnUnstatement ccsTxnUnstatement:txnUnstatements){
				ccsTxnUnstatement.setStmtDate(batchFacility.getBatchDate());
			}
		}
		if(ccsTxnHsts.size()>0){
			for(CcsTxnHst ccsTxnHst:ccsTxnHsts){
				ccsTxnHst.setStmtDate(batchFacility.getBatchDate());
			}
		}
		
		// 将当前日期设置为下一账单日, 使其在当日进行贷款转移
		acct.setNextStmtDate(batchFacility.getBatchDate());
		// 上锁定码使其终止转移
		acct.setBlockCode(blockcodeUtils.addBlockCode(acct.getBlockCode(), D_CODE));
		acct.setBlockCode(blockcodeUtils.addBlockCode(acct.getBlockCode(), I_CODE));
	}

}
