package com.sunline.ccs.batch.cc9100;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.sunline.ppy.dictionary.report.ccs.T9002ToPBOCRptItem;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;

/**
 * @see 类名：LoanStepClose
 * @see 描述：贷款结清,结清有3种情况
            1、应还款日结清
            2、宽限日之内还清
            3、delay还清
            4、提前还清
 *
 * @see 创建日期：   2015-6-24下午2:40:20
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Component
public class LoanStepClose extends LoanStep {
	@Override
	public void setPboc(S9102PBOC item, T9002ToPBOCRptItem pboc) {
		CcsLoan loan = item.getTmLoan();
		pboc.dataItem04_2301 = loan.getPaidOutDate();
		pboc.dataItem04_2107 = get2107(item);	
		pboc.dataItem04_1105 = loan.getLoanCurrBal(); 
		pboc.dataItem04_1107 = loan.getCtdRepayAmt();
		pboc.dataItem04_1109 = loan.getLoanCurrBal();
		pboc.dataItem04_4109 = "0";
		pboc.dataItem04_1111 = new BigDecimal(0);
		pboc.dataItem04_1113 = new BigDecimal(0);
		pboc.dataItem04_1115 = new BigDecimal(0);
		pboc.dataItem04_1117 = new BigDecimal(0);
		pboc.dataItem04_1119 = new BigDecimal(0);
		pboc.dataItem04_4312 = get4312(item);
		pboc.dataItem04_4107 = loan.getLoanCode();
		pboc.dataItem04_7105 = "1";
		pboc.dataItem04_7109 = "3";//结清
		pboc.dataItem04_7107 = loan.getPaymentHst();
		pboc.dataItem04_1210 = new BigDecimal(0);
		pboc.dataItem04_7121 = "1";
	}
}
