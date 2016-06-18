package com.sunline.ccs.batch.cc6000;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ccs.batch.cc6000.common.InterestAcru;
import com.sunline.ccs.batch.cc6000.common.TransactionGenerator;
import com.sunline.ccs.facility.BlockCodeUtils;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.loan.McLoanProvideImpl;
import com.sunline.ccs.param.def.PlanTemplate;
import com.sunline.ccs.param.def.enums.LoanTerminateReason;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.BucketType;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanStatus;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.PlanType;
import com.sunline.ppy.dictionary.report.ccs.InterestAccrualItem;


/** 
 * @see 类名：P6022InterestAcruToday
 * @see 描述：当日利息累计
 *
 * @see 创建日期：   2015年6月25日 下午1:33:19
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class P6022InterestAcruToday implements ItemProcessor<S6000AcctInfo, S6000AcctInfo> {
	private Logger logger = LoggerFactory.getLogger(getClass());	
	
	@Autowired
	private InterestAcru interestAccrual;
	@Autowired
    private BatchStatusFacility batchFacility;
	@Autowired
	private TransactionGenerator generatorTransaction;
	@Autowired
	private McLoanProvideImpl mcLoanProvideImpl;
	@Autowired
	private BlockCodeUtils blockCodeUtils;
	@Autowired
	private UnifiedParameterFacility parameterFacility;
	
	@Override
	public S6000AcctInfo process(S6000AcctInfo item) {
		if (logger.isDebugEnabled()) {
			logger.debug("当期利息累计：Org["+item.getAccount().getOrg()
					+"],AcctType["+item.getAccount().getAcctType()
					+"],AcctNo["+item.getAccount().getAcctNbr()
					+"]");
		}
		Date batchDate = batchFacility.getBatchDate();

		//当日计息
		interestAccrual.accumulateInterest(item, 1, batchDate);

		// 生成利息累积报表
		InterestAccrualItem rptItem= null;
		BigDecimal begDefbnpIntAcruDiff = null;
		BigDecimal nodefbnpIntAcruDiff = null;
		BigDecimal ctdDefbnpIntAcruDiff = null;
		
		// 1-生成变更之前的信用计划Map
		Map<Long, CcsPlan> prePlanMap = new HashMap<Long, CcsPlan>();
		for (CcsPlan plan : item.getPrePlans()){
			logger.info("当期利息累计:"+plan.getPlanId());
			prePlanMap.put(plan.getPlanId(), plan);
		}
		
		// 2-循环所有处理后的信用计划
		for (CcsPlan plan : item.getPlans()){
			// 计算当日利息累积的差量
			CcsPlan prePlan = prePlanMap.get(plan.getPlanId());
			if (prePlan != null){
				begDefbnpIntAcruDiff = plan.getBegDefbnpIntAcru().subtract(prePlan.getBegDefbnpIntAcru());
				nodefbnpIntAcruDiff = plan.getNodefbnpIntAcru().subtract(prePlan.getNodefbnpIntAcru());
				ctdDefbnpIntAcruDiff = plan.getCtdDefbnpIntAcru().subtract(prePlan.getCtdDefbnpIntAcru());
			}
			else
			{
				begDefbnpIntAcruDiff = plan.getBegDefbnpIntAcru();
				nodefbnpIntAcruDiff = plan.getNodefbnpIntAcru();
				ctdDefbnpIntAcruDiff = plan.getCtdDefbnpIntAcru();
			}
			// 如果任意一个差量不等于0，则进入利息累积当日报表
			if (begDefbnpIntAcruDiff.compareTo(BigDecimal.ZERO) != 0
				|| nodefbnpIntAcruDiff.compareTo(BigDecimal.ZERO) != 0
				|| ctdDefbnpIntAcruDiff.compareTo(BigDecimal.ZERO) != 0)
			{
				rptItem = new InterestAccrualItem();
				rptItem.acctNo = plan.getAcctNbr();
				rptItem.acctType = plan.getAcctType();
				rptItem.logicalCardNo = plan.getLogicCardNbr();
				rptItem.org = plan.getOrg();
				rptItem.planNbr = plan.getPlanNbr();
				rptItem.planType = plan.getPlanType();
				rptItem.planId = plan.getPlanId();
				rptItem.begDefbnpIntAcru = plan.getBegDefbnpIntAcru();
				rptItem.nodefbnpIntAcru = plan.getNodefbnpIntAcru();
				rptItem.ctdDefbnpIntAcru = plan.getCtdDefbnpIntAcru();
				rptItem.begDefbnpIntAcruDiff = begDefbnpIntAcruDiff;
				rptItem.nodefbnpIntAcru = plan.getNodefbnpIntAcru();
				rptItem.ctdDefbnpIntAcru = plan.getCtdDefbnpIntAcru();
				rptItem.ctdDefbnpIntAcruDiff = ctdDefbnpIntAcruDiff;
				rptItem.nodefbnpIntAcruDiff = nodefbnpIntAcruDiff;
				// FIXME 修改获取天数差异的方法
				rptItem.accrualDays = Integer.parseInt(String.valueOf((batchFacility.getBatchDate().getTime() - batchFacility.getLastBatchDate().getTime())/1000/60/60/24));
				
				item.getIntAccrualItems().add(rptItem);
			}
		}
		//如果锁定码中不存在标志'I'表示停止计息计费不累计本金基数
		if (blockCodeUtils.getMergedIntAccuralInd(item.getAccount().getBlockCode())){
			for(CcsPlan plan:item.getPlans()){
				PlanTemplate planTemplate = parameterFacility.loadParameter(plan.getPlanNbr(), PlanTemplate.class);
				//累计本金基数
				if(planTemplate.isAccruPrinSum.equals(Indicator.Y)){
					BigDecimal prin = plan.getCtdPrincipal().add(plan.getPastPrincipal());
					plan.setAccruPrinSum(plan.getAccruPrinSum().add(prin));
				}
			}
		}

		logger.debug("当期罚金累计：Org["+item.getAccount().getOrg()
					+"],AcctType["+item.getAccount().getAcctType()
					+"],AcctNo["+item.getAccount().getAcctNbr()
					+"]");
		// 检查账户锁定码免除罚金标志
		if (blockCodeUtils.getMergedMulctInd(item.getAccount().getBlockCode())) return item;
				
		//罚金补记，按贷款补收
		for(CcsLoan loan: item.getLoans()){
			logger.debug("当期罚金累计，loan借据号["+loan.getDueBillNo()
						+"],当期期数 ["+loan.getCurrTerm()+"],DPD逾期起始日期["+loan.getOverdueDate()+"],CPD逾期起始日期["+loan.getCpdBeginDate()+"]");
			if(loan.getOverdueDate() == null && loan.getCpdBeginDate() == null) continue;
			if(loan.getLoanStatus() == LoanStatus.T && loan.getTerminalReasonCd() != LoanTerminateReason.P ) continue;
			
			List<BigDecimal> amtList = mcLoanProvideImpl.calculateMulct(loan, batchDate,item.getPlans());
			//代收罚金收取列表
			List<BigDecimal> replaceMulctList = mcLoanProvideImpl.calculateReplaceMulct(loan, batchDate, item.getPlans());
			
			//查找loan对应的最近一期的转入计划
			CcsPlan xfrInPlan = null;
			
			// 随借随还，直接取转入计划
			if (LoanType.MCAT.equals(loan.getLoanType())) {
				for(CcsPlan plan : item.getPlans()){
					if (PlanType.L.equals(plan.getPlanType())) {
						xfrInPlan = plan;
						break;
					}
				}
			}
			
			// 等额本息和等额本息匹配对应的转入计划
			if (LoanType.MCEI.equals(loan.getLoanType()) || LoanType.MCEP.equals(loan.getLoanType())) {
				for(CcsPlan plan : item.getPlans()){
					int term = plan.getTerm()==null?0:plan.getTerm();
					if(loan.getRefNbr().equals(plan.getRefNbr()) && loan.getCurrTerm()==term){
						xfrInPlan=plan;
						break;
					}
				}
			}
			
			if(xfrInPlan == null){
				logger.debug("当期罚金累计，根据loan未找到对应的当期转入计划，loan借据号["+loan.getDueBillNo()
						+"],当期期数 ["+loan.getCurrTerm()+"]");
				continue;
			}
			
			if(amtList.size()>0){
				for(BigDecimal txnAmt : amtList){
					//生成一笔罚金入账到转入计划
					generatorTransaction.generateTxnTransaction(item, xfrInPlan, txnAmt, batchDate,BucketType.Mulct);
					loan.setLastPenaltyDate(batchDate);
				}
			}
			
			if(replaceMulctList.size()>0){
				for(BigDecimal txnAmt : replaceMulctList){
					//生成一笔代收罚金入账到转入计划
					generatorTransaction.generateTxnTransaction(item, xfrInPlan, txnAmt, batchDate,BucketType.ReplaceMulct);
					loan.setLastPenaltyDate(batchDate);
				}
			}
		}
		
		return item;
	}

}
