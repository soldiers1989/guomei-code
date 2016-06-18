package com.sunline.ccs.batch.cc6000;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sunline.ccs.batch.cc6000.common.BnpManager;
import com.sunline.ccs.batch.cc6000.common.Calculator;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.param.def.enums.BucketObject;
import com.sunline.ppy.dictionary.enums.ExceptionType;


/**
 * @see 类名：P6001CheckData
 * @see 描述： 账户状态异常，如信息增加不完整、set to purge，出异常报表
 *
 * @see 创建日期：   2015-6-24上午9:59:17
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Component
public class P6001CheckData implements ItemProcessor<S6000AcctInfo, S6000AcctInfo> {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private Calculator calculator;
	@Autowired
	private BnpManager bnpManager;

	@Override
	public S6000AcctInfo process(S6000AcctInfo item) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("异常账户状态：Org["+item.getAccount().getOrg()
					+"],AcctType["+item.getAccount().getAcctType()
					+"],AcctNo["+item.getAccount().getAcctNbr()
					+"],BlockCode["+item.getAccount().getBlockCode()
					+"],CashLimitRt["+item.getAccount().getCashLmtRate()
					+"],LoanLimitRt["+item.getAccount().getLoanLmtRate()
					+"]");
		}
		// 如果账户的锁定码BLOCK_CD = P表示账户关闭
		if (item.getAccount().getBlockCode() != null && item.getAccount().getBlockCode().indexOf("P") >= 0) {
			// 增加异常账户报表记录
			calculator.makeExceptionAccount(item, null, null, ExceptionType.E16);
		}
		
		// 检查账户的取现额度是否大于账户信用额度
		
		if (item.getAccount().getCashLmtRate() != null && BigDecimal.ONE.compareTo(item.getAccount().getCashLmtRate()) < 0) {
			// 增加异常账户报表记录
			calculator.makeExceptionAccount(item, null, null, ExceptionType.E01);
		}
		// 检查账户的分期额度是否大于账户信用额度
		if (item.getAccount().getLoanLmtRate() != null && BigDecimal.ONE.compareTo(item.getAccount().getLoanLmtRate()) < 0) {
			// 增加异常账户报表记录
			calculator.makeExceptionAccount(item, null, null, ExceptionType.E02);
		}
		// 循环所有Plan
		BigDecimal sumPlanAmt = BigDecimal.ZERO;
		for (CcsPlan plan : item.getPlans()) {
			sumPlanAmt = sumPlanAmt.add(plan.getCurrBal());
			// 循环所有BNP
			BigDecimal sumBnpAmt = BigDecimal.ZERO;
			for (BucketObject bnp : BucketObject.values()) {
				sumBnpAmt = sumBnpAmt.add(bnpManager.getBnpAmt(plan, bnp));
				if (logger.isDebugEnabled()) {
					logger.debug("PlanNbr["+plan.getPlanNbr()
							+"],CurrBal["+plan.getCurrBal()
							+"],bnp["+bnp
							+"],BnpAmt["+bnpManager.getBnpAmt(plan, bnp)
							+"]");
				}
			}
			// 检查PLAN的余额是否为本PLAN各BNP余额之和
			if (sumBnpAmt.compareTo(plan.getCurrBal()) != 0) {
				// 增加异常账户报表记录
				calculator.makeExceptionAccount(item, plan.getPlanNbr(), plan.getRefNbr(), ExceptionType.E03);
			}
		}
		// 检查账户的余额是否为PLAN余额之和
		if (sumPlanAmt.compareTo(item.getAccount().getCurrBal()) != 0) {
			// 增加异常账户报表记录
			calculator.makeExceptionAccount(item, null, null, ExceptionType.E04);
		}
		
		return item;
	}
}
