package com.sunline.ccs.batch.cc6000.paymentassign;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ccs.batch.cc6000.S6000AcctInfo;
import com.sunline.ccs.batch.cc6000.common.TransactionGenerator;
import com.sunline.ccs.batch.common.DateUtils;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.ccs.param.def.PlanTemplate;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.enums.AgePmtHierInd;
import com.sunline.ccs.param.def.enums.BucketObject;
import com.sunline.ccs.param.def.enums.SysTxnCd;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.PlanType;

/**
 * @see 类名：Assign
 * @see 描述：针对不同贷款plan的还款分配父类
 * 
 * @see 创建日期： 2015-6-24下午6:36:16
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public abstract class Assign {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private UnifiedParameterFacility parameterFacility;
    @Autowired
    private TransactionGenerator generatorTrans;
    @Autowired
    private BatchStatusFacility batchStatusFacility;

    /**
     * @see 方法名：needAssign
     * @see 描述：还款分配预处理
     * @see 创建日期：2015-6-24下午6:36:49
     * @author ChengChun
     * 
     * @param plan
     * @param item
     * @param bal
     * @param bnps
     * @param agePmtInd
     * @return
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    public boolean needAssign(CcsPlan plan, S6000AcctInfo item, BigDecimal bal, List<BucketObject> bnps,
	    AgePmtHierInd agePmtInd) {
	pretreatInterest(plan, item, bal);
	if (acceptPay(plan)) {
	    return true;
	} else {
	    return false;
	}
    }

    /**
     * @see 方法名：pretreatInterest
     * @see 描述：对plan的利息字段做预处理
     * @see 创建日期：2015-6-24下午6:37:24
     * @author ChengChun
     * 
     * @param plan
     * @param item
     * @param bal
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    private void pretreatInterest(CcsPlan plan, S6000AcctInfo item, BigDecimal bal) {
	logger.debug("对plan的利息字段做预处理...");
	if (needSettleInt(plan, item, bal)) {
	    settleInterest(plan, item);
	}
	if (needWaiveInt(plan, item, bal)) {
	    waiveInterest(plan, item, bal);
	}
    }

    /**
     * @see 方法名：needWaiveInt
     * @see 描述：是否需要免息
     * @see 创建日期：2015-6-24下午6:38:41
     * @author ChengChun
     * 
     * @param plan
     * @param item
     * @param bal
     * @return
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    protected abstract boolean needWaiveInt(CcsPlan plan, S6000AcctInfo item, BigDecimal bal);

    /**
     * @see 方法名：needSettleInt
     * @see 描述：是否需要结息
     * @see 创建日期：2015-6-24下午6:39:00
     * @author ChengChun
     * 
     * @param plan
     * @param item
     * @param bal
     * @return
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    protected abstract boolean needSettleInt(CcsPlan plan, S6000AcctInfo item, BigDecimal bal);

    /**
     * @see 方法名：acceptPay
     * @see 描述：TODO 方法描述
     * @see 创建日期：2015-6-24下午6:39:23
     * @author ChengChun
     * 
     * @param plan
     * @return
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    private boolean acceptPay(CcsPlan plan) {
		PlanTemplate planT = parameterFacility.loadParameter(plan.getPlanNbr(), PlanTemplate.class);
		boolean acceptPay = false;
		// 如果为溢缴款计划，或“是否参与还款分配标志”为“否”，则退出
		if (PlanType.D.equals(plan.getPlanType()) || !planT.pmtAssignInd) {
		    acceptPay = false;
		} else {
		    acceptPay = true;
		}
	
		return acceptPay && acceptPay();
    }

    /**
     * @see 方法名：acceptPay
     * @see 描述：是否接受还款分配
     * @see 创建日期：2015-6-24下午6:39:35
     * @author ChengChun
     * 
     * @return
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    protected abstract boolean acceptPay();

    /**
     * 
     * @see 方法名：isExpired
     * @see 描述：判断是否已过宽限日 1、账户的宽限日<批量日期，已经过宽限日 2、第二个账单日之后，是过期
     *      之所以增加宽限日，是防止到期后直接展期时，信用计划的建立日期偏早的问题
     * @see 创建日期：2015-6-24下午6:39:53
     * @author ChengChun
     * 
     * @param plan
     * @param item
     * @return
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    protected boolean isExpired(CcsPlan plan, S6000AcctInfo item) {

	ProductCredit productCredit = parameterFacility.loadParameter(plan.getProductCd(), ProductCredit.class);
	AccountAttribute acctAttr =
		parameterFacility.loadParameter(productCredit.accountAttributeId, AccountAttribute.class);

	if (item.getAccount().getGraceDate() != null) {
	    if (DateUtils.truncatedCompareTo(item.getAccount().getGraceDate(), batchStatusFacility.getBatchDate(),
					     Calendar.DATE) < 0) {
		return true;
	    } else if (DateUtils.getIntervalDays(plan.getPlanAddDate(), batchStatusFacility.getBatchDate()) >= ((DateUtils.getIntervalDays(plan.getPlanAddDate(),item.getAccount().getNextStmtDate())) + acctAttr.pmtGracePrd)) {
		return true;
	    } else {
		return false;
	    }
	} else {
	    return false;
	}
    }

    /**
     * @see 方法名：settleInterest
     * @see 描述：对非延时利息、复利、罚息执行结息操作
     * @see 创建日期：2015-6-24下午6:40:18
     * @author ChengChun
     * 
     * @param plan
     * @param item
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    private void settleInterest(CcsPlan plan, S6000AcctInfo item) {
	logger.debug("对复利、罚息执行结息操作...");
	generatorTrans.generateInterestTransaction(item, plan, plan.getCompoundAcru().setScale(2, RoundingMode.HALF_UP),
						   batchStatusFacility.getBatchDate(), SysTxnCd.S44);
	plan.setCompoundAcru(BigDecimal.ZERO);
	generatorTrans.generateInterestTransaction(item, plan, plan.getPenaltyAcru().setScale(2, RoundingMode.HALF_UP),
						   batchStatusFacility.getBatchDate(), SysTxnCd.S43);
	plan.setPenaltyAcru(BigDecimal.ZERO);
	generatorTrans.generateInterestTransaction(item, plan, plan.getNodefbnpIntAcru().setScale(2, RoundingMode.HALF_UP),
			   batchStatusFacility.getBatchDate(), SysTxnCd.S46);
	plan.setNodefbnpIntAcru(BigDecimal.ZERO);
	generatorTrans.generateInterestTransaction(item,plan,plan.getReplacePenaltyAcru().setScale(2,RoundingMode.HALF_UP),
			batchStatusFacility.getBatchDate(),SysTxnCd.D13);
	plan.setReplacePenaltyAcru(BigDecimal.ZERO);
    }
    /**
     * 非延时利息结息
     * @param plan
     * @param item
     */
    private void settleNodefbnpInt(CcsPlan plan, S6000AcctInfo item) {
    	logger.debug("对非延时利息执行结息操作...");
    	generatorTrans.generateInterestTransaction(item, plan, plan.getNodefbnpIntAcru().setScale(2, RoundingMode.HALF_UP),
    						   batchStatusFacility.getBatchDate(), SysTxnCd.S46);
    	plan.setNodefbnpIntAcru(BigDecimal.ZERO);
        }

    /**
     * @see 方法名：waiveInterest
     * @see 描述：非延时利息减免
     * @see 创建日期：2015-6-24下午6:40:56
     * @author ChengChun
     * 
     * @param plan
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    private void waiveInterest(CcsPlan plan, S6000AcctInfo item, BigDecimal bal) {
		logger.debug("利息减免...");
		plan.setCompoundAcru(BigDecimal.ZERO);
		plan.setPenaltyAcru(BigDecimal.ZERO);
		plan.setReplacePenaltyAcru(BigDecimal.ZERO);
		plan.setGraceNodefbnpIntAcru(BigDecimal.ZERO);
		if(bal.compareTo(plan.getCurrBal().add(plan.getNodefbnpIntAcru().setScale(2, RoundingMode.HALF_UP)))>=0){
			settleNodefbnpInt(plan, item);
		}
	
    }

}
