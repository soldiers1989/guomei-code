package com.sunline.ccs.batch.front;

import java.math.BigDecimal;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctOKey;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.QCcsPlan;
import com.sunline.ccs.loan.McLoanProvideImpl;
import com.sunline.ccs.loan.TrialResp;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanUsage;

/**
 * 提前还款代扣
 * @author zhangqiang
 *
 */
@Component
public class P3000Prepayment implements ItemProcessor<SFrontInfo, SFrontInfo> {
	
	private static final Logger logger = LoggerFactory.getLogger(P3000Prepayment.class);
	
	@Autowired
	private FrontBatchUtil frontBatchUtil;
	
	@Autowired
	private BatchStatusFacility batchStatusFacility;
	
	@PersistenceContext
	private EntityManager em;
	@Autowired
	private McLoanProvideImpl mcLoanProvideImpl;
	
	@Override
	public SFrontInfo process(SFrontInfo info) throws Exception {
		// 不存在非活动的贷款时，不允许结清
		if(info.getLoan()==null){
			return null;
		}
		if (logger.isDebugEnabled()) {
			logger.debug("提前还款代扣：Org["+info.getAcct().getOrg()
					+"],AcctType["+info.getAcct().getAcctType()
					+"],AcctNo["+info.getAcct().getAcctNbr()
					+"],DueBillNo["+info.getLoan().getDueBillNo()
					+"]");
		}
		OrganizationContextHolder.setCurrentOrg(info.getAcct().getOrg());
		// 调用试算方法
		QCcsPlan q = QCcsPlan.ccsPlan;
		List<CcsPlan> plans = new JPAQuery(em).from(q).where(q.acctNbr.eq(info.getAcct().getAcctNbr()).and(q.acctType.eq(info.getAcct().getAcctType()))).list(q);
		TrialResp trialResp = mcLoanProvideImpl.mCLoanTodaySettlement(info.getLoan(),info.getLoanReg().getRegisterDate(), batchStatusFacility.getSystemStatus().getBusinessDate(), LoanUsage.M, new TrialResp(), plans,info.getLoanReg());
//		CcsAcctO accto = em.find(CcsAcctO.class, new CcsAcctOKey(info.getLoan().getAcctNbr(), info.getLoan().getAcctType()));
		
		// 试算金额
		BigDecimal txnAmt = trialResp.getTotalAMT();
		
		// 减去豁免金额
		txnAmt = txnAmt.subtract(frontBatchUtil.getTxnWaiveAmt(info.getLoan().getLoanId()));
		// 未匹配贷记金额
//		BigDecimal memoCr = accto.getMemoCr();
		// 未匹配借记金额
//		BigDecimal memoDb = accto.getMemoDb();
		// 减去未匹配贷记金额
//		txnAmt = txnAmt.subtract(memoCr).add(memoDb);
		
		// 试算金额不大于0，即存在溢缴款或持平
		if(txnAmt.compareTo(BigDecimal.ZERO)<=0){
			info.getLoanReg().setDdRspFlag(Indicator.Y);
			info.getLoanReg().setValidDate(batchStatusFacility.getSystemStatus().getBusinessDate());
			info.getLoanReg().setAdvPmtAmt(BigDecimal.ZERO);
			return null;
		}
		
		// 更新到loanReg
		info.getLoanReg().setPreAdAmt(txnAmt);
		
		if(txnAmt.compareTo(BigDecimal.ZERO) <= 0)
			return null;
		
		frontBatchUtil.initOrder(info.getAcct(), info.getCust(), info.getLoan(), LoanUsage.M, txnAmt, null);
		return info;
	}

}
