package com.sunline.ccs.batch.cc9100;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.sunline.ppy.dictionary.report.ccs.T9002ToPBOCRptItem;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;

/**
 * @see 类名：LoanStepOpen
 * @see 描述：新开立贷款
 *
 * @see 创建日期：   2015-6-24下午2:43:25
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Component
public class LoanStepOpen extends LoanStep {

	@Override
	public void setPboc(S9102PBOC item, T9002ToPBOCRptItem pboc) {
		CcsLoan loan = item.getTmLoan();
		pboc.dataItem04_2301 = loan.getRegisterDate();	//对于新开立的信贷业务，此数据项填“开户日期”
		pboc.dataItem04_2107 = loan.getRegisterDate();	//自账户开立以来无还款历史的账户，填写开户日期
		pboc.dataItem04_1105 = new BigDecimal(0);
		pboc.dataItem04_1107 = new BigDecimal(0);	
		pboc.dataItem04_1109 = loan.getLoanInitPrin();
		pboc.dataItem04_4109 = "0";
		pboc.dataItem04_1111 = new BigDecimal(0);
		pboc.dataItem04_1113 = new BigDecimal(0);
		pboc.dataItem04_1115 = new BigDecimal(0);
		pboc.dataItem04_1117 = new BigDecimal(0);
		pboc.dataItem04_1119 = new BigDecimal(0);
		pboc.dataItem04_4312 = "0";
		pboc.dataItem04_4107 = "0";
		pboc.dataItem04_7105 = "1";
		pboc.dataItem04_7109 = "1";
		pboc.dataItem04_7107 = get24Pay();
		pboc.dataItem04_1210 = new BigDecimal(0);
		pboc.dataItem04_7121 = "2";
	}
	
	/**
	 * @see 方法名：get24Pay 
	 * @see 描述：24个月还款情况
	 * @see 创建日期：2015-6-24下午2:43:40
	 * @author ChengChun
	 *  
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private String get24Pay(){
		StringBuffer sb = new StringBuffer();
		for(int i = 0;i<24;i++){
			sb.append("/");
		}		
		sb.append("*");
		return sb.substring(sb.length()-24,sb.length());
	}

}
