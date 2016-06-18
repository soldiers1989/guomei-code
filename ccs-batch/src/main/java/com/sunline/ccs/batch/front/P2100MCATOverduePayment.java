package com.sunline.ccs.batch.front;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.batch.common.DateUtils;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctKey;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctOKey;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsOrder;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.pcm.param.def.FinancialOrg;
import com.sunline.pcm.param.def.Split;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanUsage;
import com.sunline.ppy.dictionary.enums.OrderStatus;

/**
 * 逾期拆分代扣
 * @author zhangqiang
 *
 */
@Component
public class P2100MCATOverduePayment implements ItemProcessor<CcsLoan, CcsLoan> {
	
	private static final Logger logger = LoggerFactory.getLogger(P2100MCATOverduePayment.class);
	
	@Autowired
	private BatchStatusFacility batchStatusFacility;
	
	@Autowired
	private UnifiedParameterFacility unifiedParameterFacility;
	
	@Autowired
	private FrontBatchUtil frontBatchUtil;
	
	@PersistenceContext
	private EntityManager em;
	
	@Override
	public CcsLoan process(CcsLoan loan) throws Exception {
		
		// 若有处理中订单一律不出代扣
		if(frontBatchUtil.getWOrderCount(loan.getAcctNbr(), loan.getAcctType())>0){
			return null;
		}
		CcsAcct acct = em.find(CcsAcct.class, new CcsAcctKey(loan.getAcctNbr(), loan.getAcctType()));
		CcsCustomer cust = em.find(CcsCustomer.class, acct.getCustId());
		CcsAcctO accto = em.find(CcsAcctO.class, new CcsAcctOKey(loan.getAcctNbr(), loan.getAcctType()));
		
		OrganizationContextHolder.setCurrentOrg(acct.getOrg());
		
		// 所有欠款
		BigDecimal loanBal = acct.getTotDueAmt();
		// 当期欠款
		BigDecimal currBal = acct.getCurrDueAmt();
		// 未匹配贷记金额
		BigDecimal memoCr = accto.getMemoCr();
		if (logger.isDebugEnabled()) {
			logger.debug("逾期代扣：Org["+acct.getOrg()
					+"],AcctType["+acct.getAcctType()
					+"],AcctNo["+acct.getAcctNbr()
					+"],DueBillNo["+loan.getDueBillNo()
					+"],loanBal["+loanBal
					+"],currBal["+currBal
					+"],memoCr["+memoCr+"]");
		}
		
		// 待拆分金额
		BigDecimal needSplitAmt = BigDecimal.ZERO;
		if(DateUtils.truncatedCompareTo(acct.getDdDate(), batchStatusFacility.getSystemStatus().getBusinessDate(), Calendar.DATE) == 0){
			// 若在约定还款日当天, 往期欠款 - 未匹配贷记金额 <= dpd容差, 则不出逾期扣款, 出正常扣款
			BigDecimal dpdToleLmt = frontBatchUtil.getDpdToleLmt(loan);
			if(loanBal.subtract(currBal).subtract(memoCr).compareTo(dpdToleLmt) <= 0)
				return loan;
			// 待拆分金额  = 欠款 - 未匹配贷记金额
			needSplitAmt = loanBal.subtract(memoCr);
		}else if(DateUtils.truncatedCompareTo(acct.getDdDate(), batchStatusFacility.getSystemStatus().getBusinessDate(), Calendar.DATE) > 0
				&& DateUtils.truncatedCompareTo(acct.getLastStmtDate(), batchStatusFacility.getSystemStatus().getBusinessDate(), Calendar.DATE) < 0){
			// 若在账单日与还款日之间, 则需要减去当期due
			// 待拆分金额  = 欠款 - 当期due - 未匹配贷记金额
			needSplitAmt = loanBal.subtract(currBal).subtract(memoCr);
		}else{
			// 待拆分金额  = 欠款 - 未匹配贷记金额
			needSplitAmt = loanBal.subtract(memoCr);
		}
		// 减去豁免金额
		needSplitAmt = needSplitAmt.subtract(frontBatchUtil.getTxnWaiveAmt(loan.getLoanId()));
				
		if (logger.isDebugEnabled()) {
			logger.debug("逾期代扣：Org["+acct.getOrg()
					+"],AcctType["+acct.getAcctType()
					+"],AcctNo["+acct.getAcctNbr()
					+"],DueBillNo["+loan.getDueBillNo()
					+"],loanBal["+loanBal
					+"],currBal["+currBal
					+"],memoCr["+memoCr
					+"],needSplitAmt["+needSplitAmt
					+"]");
		}
		if(needSplitAmt.compareTo(BigDecimal.ZERO) <= 0)
			return loan;
		
		// 使用追偿拆分规则
		ProductCredit pc = unifiedParameterFacility.loadParameter(acct.getProductCd(), ProductCredit.class);
		FinancialOrg finanicalOrg = unifiedParameterFacility.loadParameter(pc.financeOrgNo, FinancialOrg.class);
		Split split = unifiedParameterFacility.loadParameter(finanicalOrg.splitTableId, Split.class);
		
		// 生成原始订单
		CcsOrder origOrder = frontBatchUtil.initOrder(acct, cust, loan, LoanUsage.O, needSplitAmt, null);
		origOrder.setOrderStatus(OrderStatus.G);
		origOrder.setMatchInd(Indicator.N);
		
		//*如果扣款银行为光大银行
		if("0303".equals(origOrder.getOpenBankId())){
			BigDecimal splitAmt1 = needSplitAmt.multiply(new BigDecimal(0.8)).setScale(2,BigDecimal.ROUND_HALF_UP);
			CcsOrder splitOrder = frontBatchUtil.initOrder(acct, cust, loan, LoanUsage.O, splitAmt1, null);
			splitOrder.setOriOrderId(origOrder.getOrderId());
			CcsOrder splitOrder2 = frontBatchUtil.initOrder(acct, cust, loan, LoanUsage.O, needSplitAmt.subtract(splitAmt1), null);
			splitOrder2.setOriOrderId(origOrder.getOrderId());
		}else{
			// 拆分后金额
			List<BigDecimal> splitAmts = frontBatchUtil.splitPayment(needSplitAmt, split);
			for(BigDecimal splitAmt : splitAmts){
				CcsOrder splitOrder = frontBatchUtil.initOrder(acct, cust, loan, LoanUsage.O, splitAmt, null);
				splitOrder.setOriOrderId(origOrder.getOrderId());
			}
		}

		return loan;
	}

}
