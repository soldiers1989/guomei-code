package com.sunline.ccs.batch.cc9100;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sunline.ppy.dictionary.report.ccs.T9002ToPBOCRptItem;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;

/**
 * @see 类名：LoanStepActive
 * @see 描述：还款中贷款,宽限日报送，此时已过免息期，根据paidOutDate判断是否全额还款
 *
 * @see 创建日期：   2015-6-24下午2:34:39
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Component
public class LoanStepActive extends LoanStep {
	@Autowired U9101PBOCUtil util;
	
	@Override
	public void setPboc(S9102PBOC item, T9002ToPBOCRptItem pboc) {
		CcsLoan loan = item.getTmLoan();
		pboc.dataItem04_2301 = get2301(item);//非按月还款的贷款，填写本月的最后一天；
		pboc.dataItem04_2107 = get2107(item);	
		pboc.dataItem04_1105 = get1105(item);
		pboc.dataItem04_1107 = loan.getCtdRepayAmt();	
		pboc.dataItem04_1109 = loan.getLoanCurrBal();
		pboc.dataItem04_4109 = get4109(item);
		pboc.dataItem04_1111 = get1111(item);
		pboc.dataItem04_1113 = get1113(item);
		pboc.dataItem04_1115 = get1115(item);
		pboc.dataItem04_1117 = get1117(item);
		pboc.dataItem04_1119 = get1119(item);
		pboc.dataItem04_4312 = get4312(item);
		pboc.dataItem04_4107 = loan.getLoanCode();
		pboc.dataItem04_7105 = get7105(item);
		pboc.dataItem04_7109 = get7109(item, pboc);
		pboc.dataItem04_7107 = loan.getPaymentHst();
		pboc.dataItem04_1210 = new BigDecimal(0);
		pboc.dataItem04_7121 = "1";
	}
	/**
	 * @see 方法名：get1111 
	 * @see 描述：所有应还未还本息之和，需要累加非延时罚息、复利
	 * @see 创建日期：2015-6-24下午2:39:40
	 * @author ChengChun
	 *  
	 * @param item
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private BigDecimal get1111(S9102PBOC item) {
		BigDecimal b = BigDecimal.ZERO;
		for(CcsPlan plan : item.getPlans()){
			if(isXfrIn(plan) && plan.getPaidOutDate() == null){
				b = b.add(plan.getCurrBal());
			}
		}
		return b;
	}
	/**
	 * @see 方法名：get7109 
	 * @see 描述：一个账户只要有应还未还的情况，账户状态就算逾期，而非整个账户到期后未还清才算逾期。一次还本分期付息，利息未还也算逾期。
		代码表：
		业务种类为贷款：
		1-正常； 
		2-逾期； 
		3-结清；
		4-呆账（待核销）；
		5-转出
	 * @see 创建日期：2015-6-24下午2:39:54
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
		if("0".equals(pboc.dataItem04_4109)){
			return "1";
		}
		return "2";
	}
	
}
