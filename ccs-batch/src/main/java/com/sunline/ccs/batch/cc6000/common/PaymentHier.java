package com.sunline.ccs.batch.cc6000.common;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ccs.batch.cc6000.S6000AcctInfo;
import com.sunline.ccs.batch.cc6000.paymentassign.Assign;
import com.sunline.ccs.batch.cc6000.paymentassign.AssignFactory;
import com.sunline.ccs.batch.common.EnumUtils;
import com.sunline.ccs.infrastructure.server.repos.RCcsPlan;
import com.sunline.ccs.infrastructure.server.repos.RCcsRepayHst;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.CcsRepayHst;
import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.ccs.param.def.PaymentHierarchy;
import com.sunline.ccs.param.def.PlanTemplate;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.enums.AgePmtHierInd;
import com.sunline.ccs.param.def.enums.BucketObject;
import com.sunline.pcm.param.def.FinancialOrg;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.AcctTypeGroup;
import com.sunline.ppy.dictionary.enums.BucketType;
import com.sunline.ppy.dictionary.enums.PlanType;


/** 
 * @see 类名：PaymentHier
 * @see 描述：还款分配公共处理逻辑
 *
 * @see 创建日期：   2015年6月25日 下午3:05:47
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Component
public class PaymentHier {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private UnifiedParameterFacility parameterFacility;
	@Autowired
	private Calculator calculator;
	@Autowired
	private BnpManager bnpManager;
	@Autowired
	private AssignFactory assignFactory;
	@Autowired
	private BatchStatusFacility batchStatusFacility;
	
	@Autowired
	private RCcsRepayHst rTmPaymentHst;
	@Autowired
	private RCcsPlan rCcsPlan;
	
	/**
	 * 指定计划还款
	 * @param item
	 * @param txnAmt
	 * @param depositPlan
	 * @param refNbr
	 */
	public void assignedPaymentHierarchy(S6000AcctInfo item, BigDecimal txnAmt,Date txnDate, CcsPlan depositPlan,String refNbr){
		CcsAcct account = item.getAccount();
		List<CcsPlan> plans = item.getPlans();
		// 账户属性参数
		ProductCredit productCredit = parameterFacility.loadParameter(item.getAccount().getProductCd(), ProductCredit.class);
		AccountAttribute acctAttr = parameterFacility.loadParameter(productCredit.accountAttributeId, AccountAttribute.class);

		// 还款剩余金额
		BigDecimal assignBal = BigDecimal.ZERO;
		// 根据账户属性，获取对应账龄优先标识
		AgePmtHierInd agePmtInd = acctAttr.agesPmtHierInd.get(account.getAgeCode());
		if(item.getAccount().getAcctType().getAcctTypeGroup() == AcctTypeGroup.L){
			loanPlanSort(plans,agePmtInd);
		}else{
			// 计划排序
			this.planSort(plans, agePmtInd);
		}
		
		// 克隆账户对应所有计划
		item.setPaymentPrePlans(calculator.clonePlan(plans));

		// 根据账户属性，获取对应账龄还款表ID
		Integer payId = acctAttr.agesPmtHierId.get(account.getAgeCode());
		// 根据应账龄还款表ID，获得所有BNP优先标识
		PaymentHierarchy payHier = parameterFacility.loadParameter(payId, PaymentHierarchy.class);
		// 对所有BNP进行排序
		List<BucketObject> bnps = payHier.paymentHier;
		
		if (logger.isDebugEnabled()) {
			logger.debug("还款分配:AgeCd["+account.getAgeCode()
					+"],txnAmt["+txnAmt
					+"],AgePmtHierInd["+agePmtInd
					+"],payId["+payId
					+"]");
		}
		// 执行还款分配处理
		assignBal = this.payAssign(plans, bnps, txnAmt,txnDate, agePmtInd,item,refNbr);
		// 溢缴款入账处理
		if (assignBal.compareTo(BigDecimal.ZERO) > 0) {
			// 溢缴计划-当前余额
			depositPlan.setCurrBal(depositPlan.getCurrBal().add(assignBal.negate()));
			// 溢缴计划-当期本金
			depositPlan.setCtdPrincipal(depositPlan.getCtdPrincipal().add(assignBal.negate()));
		}
	}
	/**
	 * 普通还款分配
	 * @param account
	 * @param txnAmt 分配金额
	 * @param plans 所有计划，必须包含溢缴款计划
	 */
	public void paymentHierarchy (S6000AcctInfo item, BigDecimal txnAmt,Date txnDate, CcsPlan depositPlan) {
		assignedPaymentHierarchy(item, txnAmt,txnDate, depositPlan,null);
	}
	
	


	/**
	 * 指定借据还款，先还转入建立早的计划，最后还建立日期最晚的转出计划
	 * 排序原则：
	 * 转入计划在前，建立时间早的在前
	 * @param plans
	 */
	private void loanPlanSort(List<CcsPlan> plans,final AgePmtHierInd agePmtInd) {
		Collections.sort(plans, new Comparator<CcsPlan>() {
			@Override
			public int compare(CcsPlan t1, CcsPlan t2) {
				if (t1.getPlanType()== t2.getPlanType()){
					if(DateUtils.truncatedEquals(t1.getPlanAddDate(), t2.getPlanAddDate(), Calendar.DATE)){
						return PaymentHier.this.compare(agePmtInd, t1, t2);
					}else{
						return t1.getPlanAddDate().compareTo(t2.getPlanAddDate());
					}
				} else {
					if(EnumUtils.in(t1.getPlanType(),PlanType.I,PlanType.Q,PlanType.L)){
						return -1;
					}else
						return 1;
				}
			}
		});
	}

	/**
	 * 计划排序
	 * @param plans 排序计划数组
	 * @param agePmtInd 还款帐龄冲销优先方式
	 */
	public void planSort(List<CcsPlan> plans, final AgePmtHierInd agePmtInd){
		Collections.sort(plans, new Comparator<CcsPlan>() {
			@Override
			public int compare(CcsPlan t1, CcsPlan t2) {
				return PaymentHier.this.compare(agePmtInd,t1,t2);
			}
		});
	}
	/**
	 * plan的默认排序比较对象
* @author fanghj
	 * @since 2014/3/10
	 */
	private int compare(AgePmtHierInd agePmtInd,CcsPlan t1,CcsPlan t2){
		
		// 根据交易的信用计划号，查找信用计划模板
		PlanTemplate template1 = parameterFacility.loadParameter(t1.getPlanNbr(), PlanTemplate.class);
		PlanTemplate template2 = parameterFacility.loadParameter(t2.getPlanNbr(), PlanTemplate.class);
		
		// D|有DUE(往期欠款)的PLAN优先
		if (AgePmtHierInd.D.equals(agePmtInd)){
			// 判断往期DUE是否大于零
			int o1 = bnpManager.getPastDueSum(t1).compareTo(BigDecimal.ZERO);
			int o2 = bnpManager.getPastDueSum(t2).compareTo(BigDecimal.ZERO);
			// 逆序排列
			int order = Integer.valueOf(o2).compareTo(Integer.valueOf(o1));
			if (order == 0){
				// 正序排列
				int flag = template1.pmtPriority.compareTo(template2.pmtPriority);
				if (flag == 0) {
					// 正序排列
					int i = t1.getPlanAddDate().compareTo(t2.getPlanAddDate());
					if(i == 0){
						return t1.getPlanId().compareTo(t2.getPlanId());
					}else{
						return i;
					}
				} else {
					return flag;
				}
			} else {
				return order;
			}
		} else {
			// 正序排列
			int flag = template1.pmtPriority.compareTo(template2.pmtPriority);
			if (flag == 0) {
				// 正序排列
				return t1.getPlanAddDate().compareTo(t2.getPlanAddDate());
			} else {
				return flag;
			}
		}
	}
	/**
	 * 执行还款分配处理
	 * @param plans 排序后的计划数组
	 * @param bnps 排序后的bnp数组
	 * @param assignBal 分配金额
	 * @param agePmtInd 还款帐龄冲销优先方式
	 * @return
	 */
	public BigDecimal payAssign(List<CcsPlan> plans, List<BucketObject> bnps, BigDecimal assignBal,Date txnDate, AgePmtHierInd agePmtInd,S6000AcctInfo item,String refNbr) {
		// B|BUCKET优先
		if (AgePmtHierInd.B.equals(agePmtInd)){
			for (BucketObject bnp : bnps) {
				for (int i = 0; i < plans.size(); i++) {
					if (logger.isDebugEnabled()) {
						logger.debug("还款分配BNP优先-前:bnp["+bnp
								+"],PlanNbr["+plans.get(i).getPlanNbr()
								+"],LogicalCardNo["+plans.get(i).getLogicCardNbr()
								+"],planBnpBal["+bnpManager.getBnpAmt(plans.get(i), bnp)
								+"]");
					}
					// 获取卡产品信息
					ProductCredit productCredit = parameterFacility.loadParameter(plans.get(i).getProductCd(), ProductCredit.class);
					FinancialOrg financialOrg = parameterFacility.loadParameter(productCredit.financeOrgNo, FinancialOrg.class);
					Assign pa = assignFactory.getPaymentAssign(plans.get(i).getPlanType());
					if(!isAssignedPlan(refNbr,plans.get(i))){//指定refNbr还款
						continue;
					}
					
					if(!pa.needAssign(plans.get(i), item, assignBal, bnps, agePmtInd)){
						continue;
					}
					
					
					// PlanBnp的还入金额
					BigDecimal planBnpPay = BigDecimal.ZERO;
					// 获取Plan的对应余额成份
					BigDecimal planBnpBal = bnpManager.getBnpAmt(plans.get(i), bnp);

					// Plan的对应余额成份<=零，则退出循环
					if (planBnpBal.compareTo(BigDecimal.ZERO) <= 0) continue;
					// 假如还款剩余金额大于零
					if (assignBal.subtract(planBnpBal).compareTo(BigDecimal.ZERO) > 0) {
						assignBal = assignBal.subtract(planBnpBal);
						planBnpPay = planBnpBal;
					} else {
						planBnpPay = assignBal;
						assignBal = BigDecimal.ZERO;
					}
					// 更新Plan的余额成份
					bnpManager.setBnpAmt(plans.get(i), bnp, planBnpBal.subtract(planBnpPay));
					plans.get(i).setLastPmtDate(batchStatusFacility.getBatchDate());
					
					
					if(planBnpPay.compareTo(BigDecimal.ZERO)>0){
						// 生成一笔还款分配历史
						CcsPlan payPlan = plans.get(i);
						if(payPlan.getPlanId() == null){
							// 增加新建信用计划
							payPlan = rCcsPlan.save(payPlan);//这里增加一个save方法，为了获取id，后续还款分配时用的到
						}
						this.genTmPaymentHst(payPlan, bnp, planBnpPay,financialOrg.acqAcceptorId,item);
					}
					
					
					if (logger.isDebugEnabled()) {
						logger.debug("还款分配BNP优先-后:bnp["+bnp
								+"],PlanNbr["+plans.get(i).getPlanNbr()
								+"],LogicalCardNo["+plans.get(i).getLogicCardNbr()
								+"],planBnpBal["+bnpManager.getBnpAmt(plans.get(i), bnp)
								+"]");
					}
				}
			}
		} else if(AgePmtHierInd.I.equals(agePmtInd)){
			//如果带交易日期，需要对日期进行区分plan，按照交易日期进行划分
			if(txnDate != null){
				List<CcsPlan> planBeforTxnDate = new ArrayList<CcsPlan>();
				List<CcsPlan> planAfterTxnDate = new ArrayList<CcsPlan>();
				for(CcsPlan plan: plans){
					if(plan.getPlanAddDate().compareTo(txnDate) <= 0){
						planBeforTxnDate.add(plan);
					}else{
						planAfterTxnDate.add(plan);
					}
				}
				//存在交易日期前的plan先参与还款分配
				if(planBeforTxnDate.size()>0){
					for (int i = 0; i < planBeforTxnDate.size(); i++) {
						//指定余额成分类型还款
						assignBal = payAssignByBucket(planBeforTxnDate.get(i), bnps, assignBal, agePmtInd,item,refNbr,BucketType.InsuranceFee);
					}
					
					for (int i = 0; i < planBeforTxnDate.size(); i++) {
						// 根据交易的信用计划号，查找信用计划模板
						assignBal = payAssignByPlan(planBeforTxnDate.get(i), bnps, assignBal, agePmtInd,item,refNbr);
					}
				}
				//存在交易日期后的plan后参与还款分配
				if(planAfterTxnDate.size()>0 && assignBal.compareTo(BigDecimal.ZERO) > 0){
					for (int i = 0; i < planAfterTxnDate.size(); i++) {
						//指定余额成分类型还款
						assignBal = payAssignByBucket(planAfterTxnDate.get(i), bnps, assignBal, agePmtInd,item,refNbr,BucketType.InsuranceFee);
					}
					
					for (int i = 0; i < planAfterTxnDate.size(); i++) {
						// 根据交易的信用计划号，查找信用计划模板
						assignBal = payAssignByPlan(planAfterTxnDate.get(i), bnps, assignBal, agePmtInd,item,refNbr);
					}
				}
				
			}else{
				//如果不存在交易日期，就认为是普通还款
				for (int i = 0; i < plans.size(); i++) {
					//指定余额成分类型还款
					assignBal = payAssignByBucket(plans.get(i), bnps, assignBal, agePmtInd,item,refNbr,BucketType.InsuranceFee);
				}
				
				for (int i = 0; i < plans.size(); i++) {
					// 根据交易的信用计划号，查找信用计划模板
					assignBal = payAssignByPlan(plans.get(i), bnps, assignBal, agePmtInd,item,refNbr);
				}
			}
			
			
			
			
		}else{
			for (int i = 0; i < plans.size(); i++) {
				// 根据交易的信用计划号，查找信用计划模板
				assignBal = payAssignByPlan(plans.get(i), bnps, assignBal, agePmtInd,item,refNbr);
			}
		}
		
		return assignBal;
	}
	
	
	/**
	 * 生成一笔还款分配历史
	 * 
	 * @param plan
	 * @param bnp
	 */
	private void genTmPaymentHst(CcsPlan plan, BucketObject bnp, BigDecimal planBnpPay,String acqAcceptorId,S6000AcctInfo item) {
		CcsRepayHst paymentHst = new CcsRepayHst();
		
		paymentHst.setPlanId(plan.getPlanId());
		paymentHst.setAcctNbr(plan.getAcctNbr());
		paymentHst.setAcctType(plan.getAcctType());
		paymentHst.setPlanNbr(plan.getPlanNbr());
		paymentHst.setPlanType(plan.getPlanType());
		paymentHst.setBnpType(bnp);
		paymentHst.setRepayAmt(planBnpPay);
		paymentHst.setBatchDate(batchStatusFacility.getBatchDate());
		//设置获取卡产品所属机构
		paymentHst.setAcqId(acqAcceptorId);
		rTmPaymentHst.save(paymentHst);
		updateLoanRepay(plan, bnp, planBnpPay, item);
	}

	/**
	 * 更新loan的已偿还金额
	 * @param plan
	 * @param bnp
	 * @param planBnpPay
	 * @param item
	 * @return
	 */
	private CcsLoan updateLoanRepay(CcsPlan plan,BucketObject bnp, BigDecimal planBnpPay,S6000AcctInfo item){
		CcsLoan loan = null;
		if(item.getLoans().size() <= 0){
			return loan;
		}
		
		for(CcsLoan l :item.getLoans()){
			logger.debug("还款更新查找loan,plan的refnbr=["+plan.getRefNbr()+"],loan的refnbr=["+l.getRefNbr()+"]");
			if(l.getRefNbr().equals(plan.getRefNbr())){
				logger.debug("找到loan,更新bucket=["+bnp.getBucketType()+"],amt=["+planBnpPay+"]");
				loan = updateLoanBnp(l, plan, bnp, planBnpPay);
				break;
			}
		}
		return loan;
	}
	/**
	 * 更新loan的已偿还金额
	 * @param loan
	 * @param plan
	 * @param bnp
	 * @param planBnpPay
	 * @return
	 */
	private CcsLoan updateLoanBnp(CcsLoan loan,CcsPlan plan,BucketObject bnp, BigDecimal planBnpPay){
		switch (bnp.getBucketType()){
		case Pricinpal: 
			//本金
			if(loan.getPaidPrincipal()==null){
				loan.setPaidPrincipal(BigDecimal.ZERO);
			}
			loan.setPaidPrincipal(loan.getPaidPrincipal().add(planBnpPay));
			if(plan.getPlanType() == PlanType.J){
				// 更新分期信息表(TM_LOAN)的未出账单分期本金
				loan.setUnstmtPrin(loan.getUnstmtPrin().subtract(planBnpPay));		
				//更新分期信息表(TM_LOAN)的分期已出账单本金
				loan.setLoanPrinXfrin(loan.getLoanPrinXfrin().add(planBnpPay));
				//更新分期信息表(TM_LOAN)的分期未到期本金
				loan.setLoanPrinXfrout(loan.getLoanPrinXfrout().subtract(planBnpPay));
			}
			break;
		case Interest: 
			//利息
		case Compound: 
			//复利
		case Penalty: 
			//罚息
		case ReplacePenalty:
			//代收罚息
			if(loan.getPaidInterest()==null){
				loan.setPaidInterest(BigDecimal.ZERO);
			}
			loan.setPaidInterest(loan.getPaidInterest().add(planBnpPay));
			break;
		case Mulct:
			//罚金
		case ReplaceMulct:
			//代收罚金
		case LatePaymentCharge: 
			//滞纳金
		case ReplaceLatePaymentCharge:
			//代收滞纳金
		case CardFee:
			//年费
		case OverLimitFee:
			//滞纳金
		case TXNFee:
			//交易费
		case ReplaceTxnFee:
			//服务费
			if(loan.getPaidFee()==null){
				loan.setPaidFee(BigDecimal.ZERO);
			}
			loan.setPaidFee(loan.getPaidFee().add(planBnpPay));
			break;
		case SVCFee:
			//服务费
			if(loan.getPaidSvcFee()==null){
				loan.setPaidSvcFee(BigDecimal.ZERO);
			}
			loan.setPaidSvcFee(loan.getPaidSvcFee().add(planBnpPay));
			break;
		case LifeInsuFee: 
			//寿险计划包费
			if(loan.getPaidLifeInsuAmt()==null){
				loan.setPaidLifeInsuAmt(BigDecimal.ZERO);
			}
			loan.setPaidLifeInsuAmt(loan.getPaidLifeInsuAmt().add(planBnpPay));
			break;
		case StampDuty: 
			//印花税
			if(loan.getPaidStampdutyAmt()==null){
				loan.setPaidStampdutyAmt(BigDecimal.ZERO);
			}
			loan.setPaidStampdutyAmt(loan.getPaidStampdutyAmt().add(planBnpPay));
			break;
		case ReplaceSvcFee: 
			// 代收服务费
			if(loan.getPaidReplaceSvcFee() == null){
				loan.setPaidReplaceSvcFee(BigDecimal.ZERO);
			}
			loan.setPaidReplaceSvcFee(loan.getPaidReplaceSvcFee().add(planBnpPay));
			break;
		case InsuranceFee: 
			//保险费
			if(loan.getPaidInsuranceAmt()==null){
				loan.setPaidInsuranceAmt(BigDecimal.ZERO);
			}
			loan.setPaidInsuranceAmt(loan.getPaidInsuranceAmt().add(planBnpPay));
			break;
		default : logger.debug("更新loan中的已偿还金额,费loan中的月成分字段,planid=["+plan.getPlanId()+"],loanid=["+loan.getLoanId()+"],bnp=["+bnp+"],amt=["+planBnpPay+"]");
		}
		return loan;
	}
	
	
	/**
	 * 判断是否指定的refNbr计划，如果refNbr为null，则为正常还款，返回true，如果refNbr不为空，则plan的refNbr=refNbr时返回true，否则为false
	 * @param refNbr
	 * @param tmPlan
	 * @return
	 */
	private boolean isAssignedPlan(String refNbr, CcsPlan tmPlan) {
		if(!StringUtils.isBlank(refNbr)){
			return refNbr.equals(tmPlan.getRefNbr());
		}
		return true;
	}
	/**
	 * 按计划优先还款
	 * @param plan
	 * @param bnps
	 * @param bal
	 * @param agePmtInd
	 * @return
	 */
	public BigDecimal payAssignByPlan(CcsPlan plan, List<BucketObject> bnps,BigDecimal bal, AgePmtHierInd agePmtInd,S6000AcctInfo item,String refNbr) {
		// 根据交易的信用计划号，查找信用计划模板
		
		Assign pa = assignFactory.getPaymentAssign(plan.getPlanType());
		if(!pa.needAssign(plan, item, bal, bnps, agePmtInd)){
			return bal;
		}
		if(!isAssignedPlan(refNbr, plan)){
			return bal;
		}
		// 获取卡产品信息
		ProductCredit productCredit = parameterFacility.loadParameter(plan.getProductCd(), ProductCredit.class);
		FinancialOrg financialOrg = parameterFacility.loadParameter(productCredit.financeOrgNo, FinancialOrg.class);
		
		for (BucketObject bnp : bnps) {
			if (logger.isDebugEnabled()) {
				logger.debug("还款分配Plan优先-前:bnp["+bnp
						+"],PlanNbr["+plan.getPlanNbr()
						+"],LogicalCardNo["+plan.getLogicCardNbr()
						+"],planBnpBal["+bnpManager.getBnpAmt(plan, bnp)
						+"]");
			}
			
			// PlanBnp的还入金额
			BigDecimal planBnpPay = BigDecimal.ZERO;
			// 获取Plan的对应余额成份
			BigDecimal planBnpBal = bnpManager.getBnpAmt(plan, bnp);
			// Plan的对应余额成份<=零，则退出循环
			if (planBnpBal.compareTo(BigDecimal.ZERO) <= 0) continue;
			// 假如还款剩余金额大于零
			if (bal.subtract(planBnpBal).compareTo(BigDecimal.ZERO) > 0) {
				bal = bal.subtract(planBnpBal);
				planBnpPay = planBnpBal;
			} else {
				planBnpPay = bal;
				bal = BigDecimal.ZERO;
			}
			// 更新Plan的余额成份
			bnpManager.setBnpAmt(plan, bnp, planBnpBal.subtract(planBnpPay));
			plan.setLastPmtDate(batchStatusFacility.getBatchDate());
			
			// 生成一笔还款分配历史
			if(planBnpPay.compareTo(BigDecimal.ZERO)>0){
				// 生成一笔还款分配历史
				if(plan.getPlanId() == null){
					// 增加新建信用计划
					plan = rCcsPlan.save(plan);//这里增加一个save方法，为了获取id，后续还款分配时用的到
				}
				this.genTmPaymentHst(plan, bnp, planBnpPay,financialOrg.acqAcceptorId,item);
			}
			
			
			if (logger.isDebugEnabled()) {
				logger.debug("还款分配Plan优先-后:bnp["+bnp
						+"],PlanNbr["+plan.getPlanNbr()
						+"],LogicalCardNo["+plan.getLogicCardNbr()
						+"],planBnpBal["+bnpManager.getBnpAmt(plan, bnp)
						+"]");
			}
			
		}
		return bal;
	}
	/**
	 * 按制定bucket还款分配
	 * @param plan
	 * @param bnps
	 * @param bal
	 * @param agePmtInd
	 * @return
	 */
	public BigDecimal payAssignByBucket(CcsPlan plan, List<BucketObject> bnps,BigDecimal bal, AgePmtHierInd agePmtInd,S6000AcctInfo item,String refNbr,BucketType backet ) {
		// 根据交易的信用计划号，查找信用计划模板
		// 获取卡产品信息
		ProductCredit productCredit = parameterFacility.loadParameter(plan.getProductCd(), ProductCredit.class);
		FinancialOrg financialOrg = parameterFacility.loadParameter(productCredit.financeOrgNo, FinancialOrg.class);
		for (BucketObject bnp : bnps) {
			//与指定的余额成分类型匹配
			if(bnp.getBucketType().equals(backet)){
				
				if (logger.isDebugEnabled()) {
					logger.debug("指定余额成分还款-前:bnp["+bnp
							+"],PlanNbr["+plan.getPlanNbr()
							+"],LogicalCardNo["+plan.getLogicCardNbr()
							+"],planBnpBal["+bnpManager.getBnpAmt(plan, bnp)
							+"],bucketType["+backet+"]");
				}
				
				// PlanBnp的还入金额
				BigDecimal planBnpPay = BigDecimal.ZERO;
				// 获取Plan的对应余额成份
				BigDecimal planBnpBal = bnpManager.getBnpAmt(plan, bnp);
				// Plan的对应余额成份<=零，则退出循环
				if (planBnpBal.compareTo(BigDecimal.ZERO) <= 0) continue;
				// 假如还款剩余金额大于零
				if (bal.subtract(planBnpBal).compareTo(BigDecimal.ZERO) > 0) {
					bal = bal.subtract(planBnpBal);
					planBnpPay = planBnpBal;
				} else {
					planBnpPay = bal;
					bal = BigDecimal.ZERO;
				}
				// 更新Plan的余额成份
				bnpManager.setBnpAmt(plan, bnp, planBnpBal.subtract(planBnpPay));
				plan.setLastPmtDate(batchStatusFacility.getBatchDate());
				
				// 生成一笔还款分配历史
				if(planBnpPay.compareTo(BigDecimal.ZERO)>0){
					// 生成一笔还款分配历史
					if(plan.getPlanId() == null){
						// 增加新建信用计划
						plan = rCcsPlan.save(plan);//这里增加一个save方法，为了获取id，后续还款分配时用的到
					}
					this.genTmPaymentHst(plan, bnp, planBnpPay,financialOrg.acqAcceptorId,item);
				}
				
				
				if (logger.isDebugEnabled()) {
					logger.debug("指定余额成分还款-后:bnp["+bnp
							+"],PlanNbr["+plan.getPlanNbr()
							+"],LogicalCardNo["+plan.getLogicCardNbr()
							+"],planBnpBal["+bnpManager.getBnpAmt(plan, bnp)
							+"],bucketType["+backet+"]");
				}
				
			}
			
		}
		return bal;
	}
	
}
