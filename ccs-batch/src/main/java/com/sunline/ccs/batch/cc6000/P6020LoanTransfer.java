package com.sunline.ccs.batch.cc6000;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.sunline.ppy.dictionary.enums.ExceptionType;
import com.sunline.ppy.dictionary.enums.PlanType;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ccs.batch.cc6000.common.Calculator;
import com.sunline.ccs.batch.cc6000.transfer.Transfer;
import com.sunline.ccs.batch.cc6000.transfer.TransferDefault;
import com.sunline.ccs.batch.cc6000.transfer.TransferPlanJ;
import com.sunline.ccs.batch.cc6000.transfer.TransferPlanP;
import com.sunline.ccs.batch.common.EnumUtils;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.param.def.PlanTemplate;
import com.sunline.acm.service.sdk.BatchStatusFacility;


/** 
 * @see 类名：P6020LoanTransfer
 * @see 描述：账单日分期计划余额的正常转移
 * 			  Loan_Bal(分期余额)大于零调用
 * 			  正常分期计划转移、人工终止分期计划转移、逾期终止分期计划转移
 *
 * @see 创建日期：   2015年6月25日 下午1:30:15
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class P6020LoanTransfer implements ItemProcessor<S6000AcctInfo, S6000AcctInfo> {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private UnifiedParameterFacility parameterFacility;
	@Autowired
	private Calculator calculator;
	@Autowired
    private BatchStatusFacility batchFacility;
	@Autowired
	private TransferDefault xfrDefault;
	@Autowired
	private TransferPlanJ xfrJ;
	@Autowired
	private TransferPlanP xfrP;
	

	@Override
	public S6000AcctInfo process(S6000AcctInfo item) throws Exception {
		
		// 下一账单日=当前批量日期，或上一批量日期<下一账单日<当前批量日期，否则退出
		if (!batchFacility.shouldProcess(item.getAccount().getNextStmtDate())){
			logger.debug("分期计划转移: 账户["+item.getAccount().getAcctNbr()+item.getAccount().getAcctType()+"], 当前批量日期["+batchFacility.getBatchDate()+"], 不做处理");
			return item;
		}
		logger.debug("分期计划转移: 账户["+item.getAccount().getAcctNbr()+item.getAccount().getAcctType()+"], 当前批量日期["+batchFacility.getBatchDate()+"], 进行处理");

		// 循环处理所有分期转出计划
		for (int i = 0; i < item.getPlans().size(); i++) {
			CcsPlan xfrOutPlan = item.getPlans().get(i);
			// 判断是否为分期转出交易
			if (!needXfr(xfrOutPlan)){
				continue;
			}
			
			logger.debug("分期计划转移: 当前计划["+xfrOutPlan.getPlanId()+"]");
			// 查找分期转出计划对应分期信息表[Tm_Loan]记录
			CcsLoan tmLoan = getTmLoan(item, xfrOutPlan);
			if(tmLoan == null){
				logger.debug("未找到分期信息，不做转移PlanId={}",xfrOutPlan.getPlanId());
				continue;
			}
			// 如果剩余期数小于等于零，则不做分期转移
			if (tmLoan.getRemainTerm() <= 0){
				logger.debug("分期计划转移: 剩余期数小于等于零, 不做分期转移");
				continue;
			}
			
			// 根据交易的信用计划号，查找分期转出信用计划模板
			PlanTemplate xfrOutTemplate = parameterFacility.loadParameter(xfrOutPlan.getPlanNbr(), PlanTemplate.class);
			// 根据交易的信用计划号，查找分期转入信用计划模板
			PlanTemplate xfrInTemplate = parameterFacility.loadParameter(xfrOutTemplate.dualXfrPlanNbr, PlanTemplate.class);
			
			//这里开始分家，等额本息从loanSchedule获得转移数据，随借随还直接转移xfrOut的非延时利息累计，其它使用原有逻辑处理
			Transfer xfr = defineXfr(xfrOutPlan);
			CcsPlan xfrInPlan = xfr.getXfrInPlan(item, xfrOutTemplate, xfrInTemplate, xfrOutPlan, tmLoan);
			
			xfr.xfr(item, xfrOutPlan, xfrInPlan, tmLoan);
			
		}

		return item;
	}
	
	/**
	 * 根据信用计划类型选择不同的xfr对象
	 * @param plan
	 * @return
	 */
	private Transfer defineXfr(CcsPlan plan){
		if(plan.getPlanType() == PlanType.P){
			return xfrP;
		}
		if(plan.getPlanType() == PlanType.J){
			return xfrJ;
		}
		return xfrDefault;
	}
	/**
	 * 转出计划 && (当前余额>0||当期累计非延时利息>0)，则执行xfr
	 * @param xfrOutPlan
	 * @return
	 */
	private boolean needXfr(CcsPlan xfrOutPlan){
		return EnumUtils.in(xfrOutPlan.getPlanType(), PlanType.O) && xfrOutPlan.getCurrBal().compareTo(BigDecimal.ZERO) > 0;
	}
	/**
	 * 查找分期信息
	 * @param item
	 * @param xfrOutPlan
	 * @return
	 */
	private CcsLoan getTmLoan(S6000AcctInfo item,CcsPlan xfrOutPlan){
		CcsLoan tmLoan = null;
		int count = 0;
		for (CcsLoan loan : item.getLoans()) {
			// 计划的交易参考号=分期信息表中的交易参考号
			if (xfrOutPlan.getRefNbr().equals(loan.getRefNbr())) {
				tmLoan = loan;
				count++;
				logger.debug("分期计划转移: 找到当前计划对应的Loan["+loan.getLoanId()+"]");
			}
		}
		// 分期信息记录不存在，需要做异常处理
		if (count == 0) {
			// 增加异常账户报表记录
			calculator.makeExceptionAccount(item, xfrOutPlan.getPlanNbr(), xfrOutPlan.getRefNbr(), ExceptionType.E05);
			logger.debug("分期计划转移: 分期信息记录不存在，增加异常账户报表记录");
			return null;
		}
		// 找到多条分期信息记录
		else if (count >= 2) {
			// 增加异常账户报表记录
			calculator.makeExceptionAccount(item, xfrOutPlan.getPlanNbr(), xfrOutPlan.getRefNbr(), ExceptionType.E06);
			logger.debug("分期计划转移: 找到多条分期信息记录，增加异常账户报表记录");
			return null;
		}
		return tmLoan;
	}
	
	
	
}
