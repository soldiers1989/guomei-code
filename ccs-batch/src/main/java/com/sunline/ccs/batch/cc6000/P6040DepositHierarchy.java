package com.sunline.ccs.batch.cc6000;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ccs.batch.cc6000.common.AgeController;
import com.sunline.ccs.batch.cc6000.common.BnpManager;
import com.sunline.ccs.batch.cc6000.common.Calculator;
import com.sunline.ccs.batch.cc6000.common.PaymentHier;
import com.sunline.ccs.batch.cc6000.common.PlanManager;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.param.def.Organization;
import com.sunline.ccs.param.def.PlanTemplate;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.SysTxnCdMapping;
import com.sunline.ccs.param.def.TxnCd;
import com.sunline.ccs.param.def.enums.BucketObject;
import com.sunline.ccs.param.def.enums.SysTxnCd;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.BucketType;
import com.sunline.ppy.dictionary.enums.DbCrInd;
import com.sunline.ppy.dictionary.enums.ExceptionType;
import com.sunline.ppy.dictionary.enums.PlanType;
import com.sunline.ppy.dictionary.enums.PostGlIndicator;


/** 
 * @see 类名：P6040DepositHierarchy
 * @see 描述：溢缴款还款分配
 *
 * @see 创建日期：   2015年6月25日 下午2:22:13
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Component
public class P6040DepositHierarchy implements ItemProcessor<S6000AcctInfo, S6000AcctInfo> {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private UnifiedParameterFacility parameterFacility;
	@Autowired
	private Calculator calculator;
	@Autowired
	private BnpManager bnpManager;
	@Autowired
	private PaymentHier paymentHier;
	@Autowired
	private PlanManager createPlan;
	@Autowired
    private BatchStatusFacility batchFacility;
	@Autowired
    private AgeController ageController;

	@Override
	public S6000AcctInfo process(S6000AcctInfo item) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("溢缴款还款分配：Org["+item.getAccount().getOrg()
					+"],AcctNo["+item.getAccount().getAcctNbr()
					+"],AcctType["+item.getAccount().getAcctType()
					+"],Plan.size["+item.getPlans().size()
					+"],ProductCd["+item.getAccount().getProductCd()
					+"]");
		}
		/**
		 * 将账户下所有有贷记余额的信用计划余额相加(溢缴款计划的余额为贷记余额，其余计划如果余额小于零也认为有贷记余额)，
		 * 记为TOTPLAN_CR_AMT，将账户下所有有借记余额的信用计划余额相加，记为TOTPLAN_DB_AMT 
		 * 如果TOTPLAN_CR_AMT > 0 and TOTPLAN_DB_AMT > 0则执行溢缴款还欠款逻辑
		 */
		
		//获取参数
		ProductCredit productCredit = parameterFacility.loadParameter(item.getAccount().getProductCd(), ProductCredit.class);
		
		// 贷记余额
		BigDecimal totPlanCrAmt = BigDecimal.ZERO;
		// 溢缴款收集结转金额
		BigDecimal xfrPlanCrAmt = BigDecimal.ZERO;
		// 总账交易分组唯一标识
		String glTxnUUID = UUID.randomUUID().toString();
		
		for (CcsPlan plan : item.getPlans()) {
			// 处理Plan的余额成份
			for (BucketObject bnp : BucketObject.values()) {
				if (logger.isDebugEnabled()) {
					logger.debug("收集前:bnp["+bnp
							+"],PlanNbr["+plan.getPlanNbr()
							+"],LogicalCardNo["+plan.getLogicCardNbr()
							+"],bnpAmt["+ bnpManager.getBnpAmt(plan, bnp)
							+"],glTxnUUID["+ glTxnUUID
							+"]");
				}
				// Bnp金额
				BigDecimal bnpAmt = bnpManager.getBnpAmt(plan, bnp);
				// 如果余额小于零，为贷记余额
				if (bnpAmt.compareTo(BigDecimal.ZERO) < 0) {
					totPlanCrAmt = totPlanCrAmt.add(bnpAmt);
					if (PlanType.D != plan.getPlanType()) {
						xfrPlanCrAmt = xfrPlanCrAmt.add(bnpAmt);
						// 查找系统内部交易类型对照表-转入
						SysTxnCdMapping sysTxnCdIn = parameterFacility.loadParameter(String.valueOf(SysTxnCd.S19), SysTxnCdMapping.class);
						TxnCd txnCd = parameterFacility.loadParameter(sysTxnCdIn.txnCd, TxnCd.class);
						// 增加总账的交易流水-转入
						item.getGlTxnItemList().add(calculator.makeGlTxn(
								item.getAccount().getOrg(), item.getAccount().getDefaultLogicCardNbr(), item.getAccount().getAcctNbr(), item.getAccount().getAcctType(), 
								glTxnUUID, item.getAccount().getCurrency(), item.getPreAccount().getAgeCodeGl(), sysTxnCdIn.txnCd, txnCd.description, DbCrInd.M, 
								batchFacility.getBatchDate(), bnpAmt.abs(), PostGlIndicator.N, item.getAccount().getOwningBranch(), 
								null, plan.getPlanNbr(), bnp.getBucketType()));
					}
					// Bnp清零
					bnpManager.setBnpAmt(plan, bnp, BigDecimal.ZERO);
				}
				if (logger.isDebugEnabled()) {
					logger.debug("收集后:bnp["+bnp
							+"],PlanNbr["+plan.getPlanNbr()
							+"],LogicalCardNo["+plan.getLogicCardNbr()
							+"],bnpAmt["+ bnpManager.getBnpAmt(plan, bnp)
							+"],glTxnUUID["+ glTxnUUID
							+"],totPlanCrAmt["+totPlanCrAmt
							+"],xfrPlanCrAmt["+xfrPlanCrAmt
							+"]");
				}
			}
		}
		// 如果溢缴款收集结转余额小于零，出总账交易流水
		if (xfrPlanCrAmt.compareTo(BigDecimal.ZERO) < 0) {
			// 查找系统内部交易类型对照表-转出
			SysTxnCdMapping sysTxnCdOut = parameterFacility.loadParameter(String.valueOf(SysTxnCd.S18), SysTxnCdMapping.class);
			// 获取交易码对应，要生成的内部交易费集合，判断是否需伴随生成交易费
			TxnCd txnCd = parameterFacility.loadParameter(sysTxnCdOut.txnCd, TxnCd.class);
			// 增加总账的交易流水-转出
			item.getGlTxnItemList().add(calculator.makeGlTxn(
					item.getAccount().getOrg(), item.getAccount().getDefaultLogicCardNbr(), item.getAccount().getAcctNbr(), item.getAccount().getAcctType(), 
					glTxnUUID, item.getAccount().getCurrency(), "C", sysTxnCdOut.txnCd, txnCd.description, DbCrInd.M, 
					batchFacility.getBatchDate(), xfrPlanCrAmt.abs(), PostGlIndicator.N, item.getAccount().getOwningBranch(), 
					null, productCredit.planNbrList.get(PlanType.D), BucketType.Pricinpal));
		}
		
		// 贷记余额在PLAN中是“负值”；如果贷记余额大于等于零，则退出
		if (totPlanCrAmt.compareTo(BigDecimal.ZERO) >= 0) return item;
		// 贷记余额在PLAN中是“负值”，将汇总的贷记金额变为“正值”
		totPlanCrAmt = totPlanCrAmt.abs();
		
		// 根据交易的信用计划号，查找信用计划模板
		Organization org = parameterFacility.loadParameter(null, Organization.class);
		PlanTemplate planTemplate = parameterFacility.loadParameter(org.depositPlanNbr, PlanTemplate.class);
		// 查找交易对应信用计划
		List<CcsPlan> findPlans = createPlan.findTxnPlans(item.getAccount().getDefaultLogicCardNbr(), 
				org.depositPlanNbr, planTemplate.planType, null, item.getPlans(),null);
		CcsPlan plan = null;
		// 信用计划不存在
		if (findPlans.size() < 1) {
			// 建立信用计划
			try {
				plan = createPlan.generateTxnPlan(item.getAccount(),item.getLoans(), 
						item.getAccount().getDefaultLogicCardNbr(), item.getAccount().getProductCd(), null, planTemplate, batchFacility.getBatchDate(),null);
			} catch (Exception e) {
				// 增加异常账户报表记录
				calculator.makeExceptionAccount(item, org.depositPlanNbr, null, ExceptionType.E12);
				return item;
			}
			// 增加新建信用计划
			item.getPlans().add(plan);
		}
		// F13|找到多个信用计划
		else if (findPlans.size() > 1 && planTemplate.planType != PlanType.D) {
			// 增加异常账户报表记录
			calculator.makeExceptionAccount(item, org.depositPlanNbr, null, ExceptionType.E13);
			return item;
		} else plan = findPlans.get(findPlans.size() - 1);

		// 还款交易分配及入账处理,这里的日期传空，没有交易日期
		paymentHier.paymentHierarchy(item, totPlanCrAmt,null, plan);

		for (CcsPlan tmplan : item.getPlans()) {
			for (BucketObject bnp : BucketObject.values()) {
				if (logger.isDebugEnabled()) {
					logger.debug("分配后:bnp["+bnp
							+"],PlanNbr["+tmplan.getPlanNbr()
							+"],LogicalCardNo["+tmplan.getLogicCardNbr()
							+"],bnpAmt["+ bnpManager.getBnpAmt(tmplan, bnp)
							+"]");
				}	
			}
			
		}
		
		// 增加总账的交易流水
		this.addGlTxn(item, totPlanCrAmt,plan);
		
		//更新loan中的逾期起始日期
		ageController.updateOverdueDate(item.getLoans(), item.getPlans(),false,item.getAccount());

		return item;
	}

	/**
	 * 增加总账的交易流水
	 * @param item
	 * @param prePlans 入账前计划列表
	 * @param txnPost 入账交易
	 */
	private void addGlTxn(S6000AcctInfo item, BigDecimal totPlanCrAmt,CcsPlan depositePlan) {
		List<CcsPlan> prePlans = item.getPaymentPrePlans();
		// 总账交易分组唯一标识
		String glTxnUUID = UUID.randomUUID().toString();
		
		for (int i = 0; i < item.getPlans().size(); i++) {
			CcsPlan plan = item.getPlans().get(i);
			// 是否为默认溢缴款计划
			if (PlanType.D.equals(plan.getPlanType()) && depositePlan.getLogicCardNbr().equals(plan.getLogicCardNbr())) {
				// 获取Plan的余额成份
				BigDecimal currBnpAmt = bnpManager.getBnpAmt(item.getPlans().get(i), BucketObject.ctdPrincipal)
									.add(bnpManager.getBnpAmt(item.getPlans().get(i), BucketObject.pastPrincipal));
				BigDecimal preBnpAmt = totPlanCrAmt;
				BigDecimal glPostAmt = preBnpAmt.abs().subtract(currBnpAmt.abs()).abs();
				if (logger.isDebugEnabled()) {
					logger.debug("生成总账的交易流水-溢缴款plan:PlanNbr["+item.getPlans().get(i).getPlanNbr()
							+"],LogicalCardNo["+item.getPlans().get(i).getLogicCardNbr()
							+"],currBnpAmt["+currBnpAmt
							+"],perBnpAmt["+preBnpAmt
							+"],glPostAmt["+glPostAmt
							+"]");
				}
				if (glPostAmt.compareTo(BigDecimal.ZERO) > 0) {
					// 查找系统内部交易类型对照表-转入
					SysTxnCdMapping sysTxnCdIn = parameterFacility.loadParameter(String.valueOf(SysTxnCd.S19), SysTxnCdMapping.class);
					TxnCd txnCd = parameterFacility.loadParameter(sysTxnCdIn.txnCd, TxnCd.class);
					// 增加总账的交易流水-转入
					item.getGlTxnItemList().add(calculator.makeGlTxn(
							item.getAccount().getOrg(), item.getAccount().getDefaultLogicCardNbr(), item.getAccount().getAcctNbr(), item.getAccount().getAcctType(), 
							glTxnUUID, item.getAccount().getCurrency(), "C", sysTxnCdIn.txnCd, txnCd.description, DbCrInd.M, 
							batchFacility.getBatchDate(), glPostAmt, PostGlIndicator.N, item.getAccount().getOwningBranch(), 
							null, item.getPlans().get(i).getPlanNbr(), BucketType.Pricinpal));
				}
			}
			else {
				// 根据交易的信用计划号，查找信用计划模板
				PlanTemplate planT = parameterFacility.loadParameter(item.getPlans().get(i).getPlanNbr(), PlanTemplate.class);
				// 是否参与还款分配标志
				if (!planT.pmtAssignInd) continue;
				
				for (BucketObject bnp : BucketObject.values()) {
					// 更新Plan的余额成份
					BigDecimal currBnpAmt = bnpManager.getBnpAmt(item.getPlans().get(i), bnp);
					BigDecimal perBnpAmt = bnpManager.getBnpAmt(prePlans.get(i), bnp);
					BigDecimal glPostAmt = perBnpAmt.subtract(currBnpAmt).abs();
					if (logger.isDebugEnabled()) {
						logger.debug("生成总账的交易流水-普通plan:bnp["+bnp
								+"],PlanNbr["+item.getPlans().get(i).getPlanNbr()
								+"],preBnp["+prePlans.get(i).getPlanNbr()
								+"],prePlanNbr["+prePlans.get(i).getPlanNbr()
								+"],LogicalCardNo["+item.getPlans().get(i).getLogicCardNbr()
								+"],bnpAmt["+ bnpManager.getBnpAmt(item.getPlans().get(i), bnp)
								+"],currBnpAmt["+currBnpAmt
								+"],perBnpAmt["+perBnpAmt
								+"],glPostAmt["+glPostAmt
								+"]");
					}
					if (glPostAmt.compareTo(BigDecimal.ZERO) > 0) {
						// 查找系统内部交易类型对照表-转出
						SysTxnCdMapping sysTxnCdOut = parameterFacility.loadParameter(String.valueOf(SysTxnCd.S18), SysTxnCdMapping.class);
						TxnCd txnCd = parameterFacility.loadParameter(sysTxnCdOut.txnCd, TxnCd.class);
						// 增加总账的交易流水-转出
						item.getGlTxnItemList().add(calculator.makeGlTxn(
								item.getAccount().getOrg(), item.getAccount().getDefaultLogicCardNbr(), item.getAccount().getAcctNbr(), item.getAccount().getAcctType(), 
								glTxnUUID, item.getAccount().getCurrency(), item.getPreAccount().getAgeCodeGl(), sysTxnCdOut.txnCd, txnCd.description, DbCrInd.M, 
								batchFacility.getBatchDate(), glPostAmt, PostGlIndicator.N, item.getAccount().getOwningBranch(), 
								null, item.getPlans().get(i).getPlanNbr(), bnp.getBucketType()));
					}
				}
			}
		}
	}
	
}
