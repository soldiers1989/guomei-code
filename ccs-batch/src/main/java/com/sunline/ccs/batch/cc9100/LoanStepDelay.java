package com.sunline.ccs.batch.cc9100;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

import com.sunline.ppy.dictionary.report.ccs.T9002ToPBOCRptItem;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;

/**
 * @see 类名：LoanStepDelay
 * @see 描述：逾期贷款
 *
 * @see 创建日期：   2015-6-24下午2:41:35
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Component
public class LoanStepDelay extends LoanStep {
	@Override
	public void setPboc(S9102PBOC item, T9002ToPBOCRptItem pboc) {
		CcsLoan loan = item.getTmLoan();
		pboc.dataItem04_2301 = get2301(item);//到期后没有结清的贷款，填写本月的最后一天
		pboc.dataItem04_2107 = get2107(item);
		pboc.dataItem04_1105 = loan.getLoanCurrBal();
		pboc.dataItem04_1107 = loan.getCtdRepayAmt();
		pboc.dataItem04_1109 = loan.getLoanCurrBal();
		pboc.dataItem04_4109 = get4109(item);
		pboc.dataItem04_1111 = get1111(item);
		
		pboc.dataItem04_4312 = get4312(item);
		pboc.dataItem04_4107 = loan.getLoanCode();
		
		pboc.dataItem04_7107 = loan.getPaymentHst();
		pboc.dataItem04_1210 = new BigDecimal(0);
		
		//根据逾期期数判断
		pboc.dataItem04_7105 = get7105(item);
		pboc.dataItem04_1113 = get1113(item);
		pboc.dataItem04_1115 = get1115(item);
		pboc.dataItem04_1117 = get1117(item);
		pboc.dataItem04_1119 = get1119(item);
		pboc.dataItem04_7109 = get7109(item,pboc);
		
	}
	/**
	 * @see 方法名：get7109 
	 * @see 描述：业务种类为贷款：
		1-正常； 
		2-逾期； 
		3-结清；
		4-呆账（待核销）；
		5-转出。
	 * @see 创建日期：2015-6-24下午2:42:00
	 * @author ChengChun
	 *  
	 * @param item
	 * @param pboc
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private String get7109(S9102PBOC item, T9002ToPBOCRptItem pboc) {
		if("2".equals(pboc.dataItem04_7105)){
			return "2";
		}
		return "4";
	}
	
	/**
	 * @see 方法名：get1111 
	 * @see 描述：所有应还未还本息之和，需要累加非延时罚息、复利
	 * @see 创建日期：2015-6-24下午2:42:15
	 * @author ChengChun
	 *  
	 * @param item
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	protected BigDecimal get1111(S9102PBOC item) {
		BigDecimal b = BigDecimal.ZERO;
		for(CcsPlan plan : item.getPlans()){
			if(isXfrIn(plan) && plan.getPaidOutDate() == null){
				b = b.add(plan.getCurrBal().add(plan.getCompoundAcru().setScale(2, RoundingMode.HALF_UP))
						.add(plan.getNodefbnpIntAcru().setScale(2, RoundingMode.HALF_UP))
						.add(plan.getPenaltyAcru().setScale(2, RoundingMode.HALF_UP)))
						.add(plan.getReplacePenaltyAcru().setScale(2,RoundingMode.HALF_UP));
			}
		}
		return b;
	}
}
