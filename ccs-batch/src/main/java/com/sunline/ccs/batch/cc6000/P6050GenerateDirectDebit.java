package com.sunline.ccs.batch.cc6000;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.DdIndicator;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.exchange.DdRequestInterfaceItem;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ccs.batch.cc6000.common.Calculator;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.otb.CommProvide;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.acm.service.sdk.BatchStatusFacility;


/** 
 * @see 类名：P6050GenerateDirectDebit
 * @see 描述：生成约定扣款文件
 *
 * @see 创建日期：   2015年6月25日 下午2:28:09
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class P6050GenerateDirectDebit implements ItemProcessor<S6000AcctInfo, S6000AcctInfo> {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private UnifiedParameterFacility parameterFacility;
	@Autowired
	private Calculator commonComputeClass;
	@Autowired
	private BatchStatusFacility batchStatusFacility;
	@Autowired
	private CommProvide commonProvide;

	@Override
	public S6000AcctInfo process(S6000AcctInfo item) {
		if (logger.isDebugEnabled()) {
			logger.debug("生成约定还款文件：Org["+item.getAccount().getOrg()
					+"],AcctNo["+item.getAccount().getAcctNbr()
					+"],AcctType["+item.getAccount().getAcctType()
					+"],DdInd["+item.getAccount().getDdInd()
					+"],DdDate["+item.getAccount().getDdDate()
					+"]");
		}
		CcsAcct acct = item.getAccount();
		
		//	判断是否是约定还款日期 && 客户设定了约定还款
		if (acct.getDdInd() != DdIndicator.N && acct.getDdDate() != null){
			Date batchDate = batchStatusFacility.getBatchDate();
			
			boolean needDD = false;
			BigDecimal ddAmount = BigDecimal.ZERO;
			if(batchStatusFacility.shouldProcess(acct.getDdDate())){
				needDD = true;
				ddAmount = calcDdAmount(acct, item.getPlans());
			}else{
				// 小额贷款约定还款文件处理
				if(acct.getAcctType() == AccountType.E){
					// 到期
					boolean isExpire = false;
					List<CcsPlan> expirePlans = new ArrayList<CcsPlan>();
					// 逾期
					boolean overdue = false;
					List<CcsPlan> overduePlans = new ArrayList<CcsPlan>();
					// 未逾期
					List<CcsPlan> unearnedPlans = new ArrayList<CcsPlan>();
					
					for(CcsLoan loan : item.getLoans()){
						if(DateUtils.truncatedCompareTo(loan.getLoanExpireDate(), batchDate, Calendar.DATE) <=0){
							isExpire = true;
							addPlans(item, expirePlans, loan);
						}else{
							if(loan.getLoanAgeCode()!=null && loan.getLoanAgeCode().compareTo("2") >=0 && loan.getLoanAgeCode().compareTo("9") <=0 ){
								overdue = true;
								addPlans(item, overduePlans, loan);
							}else{
								addPlans(item, unearnedPlans, loan);
							}
						}
					}
				
				
					// 末期后持续扣款
					ProductCredit productCr = parameterFacility.loadParameter(acct.getProductCd(), ProductCredit.class);
					if(isExpire && productCr.ddAfterLastTerm==Indicator.Y){
						needDD = true;
						ddAmount = commonComputeClass.calcQualGraceBal(expirePlans);
						// 贷款到期与贷款逾期合并处理, 另追加到期贷款plan的Acru
						ddAmount = appendInterest(item, expirePlans, ddAmount);
					}
	
					// 逾期后持续扣款
					if(overdue && productCr.ddAfterDelin == Indicator.Y){
						needDD = true;
						ddAmount = ddAmount.add(commonComputeClass.calcQualGraceBal(overduePlans));
						ddAmount = appendInterest(item, overduePlans, ddAmount);
					}
					
					// 未逾期持续扣N日
					Date ddDate = DateUtils.addDays(acct.getDdDate(), productCr.keepingDDDays);
					if(DateUtils.truncatedCompareTo(ddDate, batchDate, Calendar.DATE) >=0){
						needDD = true;
						ddAmount = ddAmount.add(commonComputeClass.calcQualGraceBal(unearnedPlans));
						ddAmount = appendInterest(item, unearnedPlans, ddAmount);
					}
				}
			}
			// 出约定还款文件 && 约定还款金额大于0
			if (needDD && ddAmount.compareTo(BigDecimal.ZERO) > 0){
				//	生成约定还款文件定义
				item.getDdRequestItemList().add(addDirectDebitFile(item.getAccount(), ddAmount));
				logger.info("约定还款文件记录已生成, 约定还款金额:[" + ddAmount + "]");
				
				// 更新上次约定还款日期和约定还款金额
				acct.setLastDdAmt(ddAmount);
				acct.setLastDdDate(batchDate);
			}
		}
		return item;
	}


	/**
	 * 增加将该贷款下未还清的XfrInPlan
	 * 
	 * @param item
	 * @param plans
	 * @param loan
	 */
	private void addPlans(S6000AcctInfo item, List<CcsPlan> plans, CcsLoan loan) {
		for(CcsPlan plan : item.getPlans()){
			if(loan.getRefNbr().equals(plan.getRefNbr())
					&& plan.getPlanType().isXfrIn()
					&& plan.getPaidOutDate() == null){
				plans.add(plan);
			}
		}
	}

	
	/**
	 * 追加plan上Acru的利息
	 * 
	 * @param item
	 * @param plans
	 * @param ddAmount
	 * @return
	 */
	private BigDecimal appendInterest(S6000AcctInfo item, List<CcsPlan> plans , BigDecimal ddAmount) {
		for(CcsPlan plan : plans){
			ddAmount = ddAmount
					.add(plan.getNodefbnpIntAcru().setScale(2, RoundingMode.HALF_UP))
					.add(plan.getPenaltyAcru().setScale(2, RoundingMode.HALF_UP))
					.add(plan.getCompoundAcru().setScale(2, RoundingMode.HALF_UP))
					.add(plan.getReplacePenaltyAcru().setScale(2,RoundingMode.HALF_UP));
		}
		return ddAmount;
	}

	/**
	 * 计算约定还款金额
	 * @param item
	 * @return
	 */
	private BigDecimal calcDdAmount(CcsAcct acct, List<CcsPlan> plans){
		BigDecimal ddAmount = null;
		
		// 根据账户约定的还款类型进行处理
		switch (acct.getDdInd()){
		// 全额还款
		case F: 
			ddAmount = commonProvide.getRemainGraceBal(plans);
			// 判断是否为溢缴款，溢缴款返回0，否则返回应还金额
			return ddAmount.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : ddAmount;
		
		// 最小额还款
		case M: 
			return acct.getTotDueAmt();
		
		// 最小额购汇
		case C: 
			// TODO 处理最小额购汇
			return BigDecimal.ZERO;
		
		// 全额购汇
		case E: 
			// TODO 处理全额购汇
			return BigDecimal.ZERO;
		default: throw new IllegalArgumentException("账户表中的约定还款类型不正确");
		}
	}
	
	/**
	 * 增加约定还款文件明细记录
	 * @param item
	 * @param ddAmount
	 */
	private DdRequestInterfaceItem addDirectDebitFile(CcsAcct acct, BigDecimal ddAmount){
		DdRequestInterfaceItem ddr = new DdRequestInterfaceItem();
		
		ddr.acctNo = acct.getAcctNbr();
		ddr.acctType = acct.getAcctType();
		ddr.name = acct.getName();
		ddr.dbBankAcctName = acct.getDdBankAcctName();
		ddr.ddAmount = ddAmount;
		ddr.ddBankAcctNo = acct.getDdBankAcctNbr();
		ddr.ddBankBranch = acct.getDdBankBranch();
		ddr.ddBankName = acct.getDdBankName();
		ddr.owningBranch = acct.getOwningBranch();
		ddr.ddDate = acct.getDdDate();
		//	TODO 逻辑卡和客户物理卡号可能不一致，后来需要考虑如何获取物理卡号
		ddr.defaultCardNo = acct.getDefaultLogicCardNbr();
		ddr.directDbInd = acct.getDdInd();
		ddr.org = acct.getOrg();
		
		return ddr;
	}

}
