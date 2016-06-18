package com.sunline.ccs.batch.front;

import java.math.BigDecimal;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctKey;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctOKey;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ppy.dictionary.enums.LoanUsage;

public class P1100MCATNormalPayment implements ItemProcessor<CcsLoan, CcsLoan> {
	
	private static final Logger logger = LoggerFactory.getLogger(P1100MCATNormalPayment.class);
	
	@Autowired
	private FrontBatchUtil frontBatchUtil;
	
	@PersistenceContext
	private EntityManager em;
	
	@Override
	public CcsLoan process(CcsLoan loan) throws Exception {
		
		// 存在处理中的订单，不出代扣
		if(frontBatchUtil.getWOrderCount(loan.getAcctNbr(), loan.getAcctType()) > 0)
			return null;
		
		CcsAcct acct = em.find(CcsAcct.class, new CcsAcctKey(loan.getAcctNbr(), loan.getAcctType()));
		CcsAcctO accto = em.find(CcsAcctO.class, new CcsAcctOKey(loan.getAcctNbr(), loan.getAcctType()));
		
		// 所有欠款
		BigDecimal loanBal = acct.getTotDueAmt();
		// 当期欠款
		BigDecimal currBal = acct.getCurrDueAmt();
		// 未匹配贷记金额
		BigDecimal memoCr = accto.getMemoCr();
		
		
		
		// 逾期
		if(loan.getOverdueDate() != null){
			BigDecimal dpdToleLmt = frontBatchUtil.getDpdToleLmt(loan);
			// 若往期欠款 - 未匹配贷记金额 > dpd容差, 则不出正常扣款, 出逾期扣款
			if(loanBal.subtract(currBal).subtract(memoCr).compareTo(dpdToleLmt) > 0)
				return loan;
		}
		
		// 若已经还清则不再发送
		if(loanBal.subtract(memoCr).compareTo(BigDecimal.ZERO) <= 0)
			return null;
		
		if(logger.isDebugEnabled())
			logger.debug("随借随还正常扣款,loanId:" + loan.getLoanId());
		
		BigDecimal txnAmt = loanBal.subtract(memoCr);
		
		// 减去豁免金额
		txnAmt = txnAmt.subtract(frontBatchUtil.getTxnWaiveAmt(loan.getLoanId()));
		
		// 生成订单
		CcsCustomer cust = em.find(CcsCustomer.class, acct.getCustId());
		frontBatchUtil.initOrder(acct, cust, loan, LoanUsage.N, txnAmt, null);
		
		return loan;
	}
	
}
