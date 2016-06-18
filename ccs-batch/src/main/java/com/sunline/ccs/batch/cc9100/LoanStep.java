package com.sunline.ccs.batch.cc9100;

import java.math.BigDecimal;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import com.sunline.ppy.dictionary.enums.PlanType;
import com.sunline.ppy.dictionary.report.ccs.T9002ToPBOCRptItem;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ccs.batch.common.EnumUtils;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.CcsRepaySchedule;
import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.acm.service.sdk.BatchStatusFacility;

/**
 * @see 类名：LoanStep
 * @see 描述：贷款阶段
 *
 * @see 创建日期：   2015-6-24下午2:34:24
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public abstract class LoanStep {
	@Autowired
	private U9101PBOCUtil util;
	@Autowired
	private BatchStatusFacility batchStatusFacility;
	@Autowired 
	private UnifiedParameterFacility parameterFacility;
	/**
	 * @see 方法名：setPboc 
	 * @see 描述：根据贷款阶段的不同，对字段赋值
	 * @see 创建日期：2015-6-24下午2:35:58
	 * @author ChengChun
	 *  
	 * @param item
	 * @param pboc
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	protected abstract void setPboc(S9102PBOC item ,T9002ToPBOCRptItem pboc);

	/**
	 * @see 方法名：get2107 
	 * @see 描述：获得最近一次实际还款日期
	 * @see 创建日期：2015-6-24下午2:36:13
	 * @author ChengChun
	 *  
	 * @param item
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	protected Date get2107(S9102PBOC item) {
		Date d = item.getTmLoan().getRegisterDate();
		for(CcsPlan plan : item.getPlans()){
			if(plan.getLastPmtDate()!=null && d.compareTo(plan.getLastPmtDate())<0){
				d = plan.getLastPmtDate();
			}
		}
		return d;
	}

	/**
	 * @see 方法名：get4109 
	 * @see 描述：对于贷款，此数据项为当前应还未还的贷款期数，到期后不再累计
	 * @see 创建日期：2015-6-24下午2:36:25
	 * @author ChengChun
	 *  
	 * @param item
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	protected String get4109(S9102PBOC item) {
		int i = 0;
		for(CcsPlan plan : item.getPlans()){
			if(isXfrIn(plan)){
				if(plan.getPaidOutDate() == null){
					i++;
				}
			}
		}
		return String.valueOf(i);
	}
	
	/**
	 * @see 方法名：isXfrIn 
	 * @see 描述：判断是否转入计划
	 * @see 创建日期：2015-6-24下午2:36:45
	 * @author ChengChun
	 *  
	 * @param plan
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	protected boolean isXfrIn(CcsPlan plan){
		return EnumUtils.in(plan.getPlanType(),PlanType.L,PlanType.Q);
	}

	
	/**
	 * @see 方法名：get111X 
	 * @see 描述：逾期from-to天未归还贷款本金 
	 *          根据plan的生成日期和批量日期间隔来计算
	 * @see 创建日期：2015-6-24下午2:36:57
	 * @author ChengChun
	 *  
	 * @param item
	 * @param dayFrom
	 * @param dayTo
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private BigDecimal get111X(S9102PBOC item,int dayFrom,int dayTo) {
		BigDecimal b = BigDecimal.ZERO;
		for(CcsPlan plan : item.getPlans()){
			if(isXfrIn(plan) && plan.getPaidOutDate() == null){
				int day = util.getIntervalDays(plan.getPlanAddDate(), batchStatusFacility.getBatchDate());
				if(day>=dayFrom && day<=dayTo){
					b = b.add(plan.getCtdPrincipal().add(plan.getPastPrincipal()));
				}
			}
		}
		return b;
	}
	/**
	 * @see 方法名：get1113 
	 * @see 描述：逾期31-60天未归还贷款本金 
	 * @see 创建日期：2015-6-24下午2:37:17
	 * @author ChengChun
	 *  
	 * @param item
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	protected BigDecimal get1113(S9102PBOC item) {
		return get111X(item,31,60);
	}
	
	/**
	 * @see 方法名：get1115 
	 * @see 描述：逾期61-90天未归还贷款本金
	 * @see 创建日期：2015-6-24下午2:37:32
	 * @author ChengChun
	 *  
	 * @param item
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	protected BigDecimal get1115(S9102PBOC item) {
		return get111X(item, 61, 90);
	}
	/**
	 * @see 方法名：get1117 
	 * @see 描述：逾期91-180天未归还贷款本金
	 * @see 创建日期：2015-6-24下午2:37:47
	 * @author ChengChun
	 *  
	 * @param item
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	protected BigDecimal get1117(S9102PBOC item) {
		return get111X(item, 91, 180);
	}
	/**
	 * @see 方法名：get1119 
	 * @see 描述：逾期181天以上未归还贷款本金
	 * @see 创建日期：2015-6-24下午2:38:03
	 * @author ChengChun
	 *  
	 * @param item
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	protected BigDecimal get1119(S9102PBOC item) {
		return get111X(item, 181, Integer.MAX_VALUE);
	}
        /**
         * @see 方法名：get4312 
         * @see 描述：TODO 方法描述
         * @see 创建日期：2015-6-24下午2:38:28
         * @author ChengChun
         *  
         * @param item
         * @return
         * 
         * @see 修改记录： 
         * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
         */
	protected String get4312(S9102PBOC item) {
		ProductCredit p = parameterFacility.loadParameter(item.getAcct().getProductCd(), ProductCredit.class);
		AccountAttribute at = parameterFacility.loadParameter(p.accountAttributeId, AccountAttribute.class);
		int i = 0;
		for(CcsPlan plan:item.getPlans()){
			if(isXfrIn(plan)){
				if(plan.getPaidOutDate() == null){
					i++;
				}else if(util.getIntervalDays(plan.getPlanAddDate(), plan.getPaidOutDate())>at.pmtGracePrd){
					i++;
				}
			}
		}
		return String.valueOf(i);
	}
        /**
         * @see 方法名：get2301 
         * @see 描述：TODO 方法描述
         * @see 创建日期：2015-6-24下午2:38:38
         * @author ChengChun
         *  
         * @param item
         * @return
         * 
         * @see 修改记录： 
         * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
         */
	protected Date get2301(S9102PBOC item) {
		for(CcsRepaySchedule a : item.getSchedules()){
			if(a.getCurrTerm() == item.getTmLoan().getCurrTerm()){
				return a.getLoanPmtDueDate();
			}
		}
		return item.getTmLoan().getRegisterDate();
	}

	/**
	 * @see 方法名：get1105 
	 * @see 描述：获得本月应还款金额
	 * @see 创建日期：2015-6-24下午2:38:47
	 * @author ChengChun
	 *  
	 * @param item
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	protected BigDecimal get1105(S9102PBOC item) {
		int currTerm = item.getTmLoan().getCurrTerm();
		for(CcsRepaySchedule t : item.getSchedules()){
			if(t.getCurrTerm() == currTerm){
				return t.getLoanTermFee().add(t.getLoanTermInt()).add(t.getLoanTermPrin());
			}
		}
		return BigDecimal.ZERO;
	}

	/**
	 * @see 方法名：get7105 
	 * @see 描述：根据最大拖欠天数计算
    	 *      1-正常；
    		2-关注；
    		3-次级；
    		4-可疑；
    		5-损失；
    		9-未知。
	 * @see 创建日期：2015-6-24下午2:39:01
	 * @author ChengChun
	 *  
	 * @param item
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	protected String get7105(S9102PBOC item) {
		CcsPlan plan = null;
		Date d = util.getBatchDate();
		for(CcsPlan t : item.getPlans()){
			if(isXfrIn(t) && t.getPaidOutDate() == null){
				if(d.compareTo(t.getPlanAddDate())>0){
					d = t.getPlanAddDate();
					plan = t;
				}
			}
		}
		if(plan != null){
			int days = util.getIntervalDays(plan.getPlanAddDate(), d);
			if(days>=31 && days<=60){
				return "2";
			}else if(days>=61 && days<=90){
				return "2";
			}else if(days>=91 && days<=180){
				return "3";
			}else if(days>180){
				return "4";
			}
		}
		return "1";
	}
	
}
