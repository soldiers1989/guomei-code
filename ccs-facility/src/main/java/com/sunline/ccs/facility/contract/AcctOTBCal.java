package com.sunline.ccs.facility.contract;

import java.math.BigDecimal;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.otb.AcctOTB;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.LoanMold;

@Service
public class AcctOTBCal {
	@Autowired
	private AcctOTB accountOTB;
	@Autowired
	UnifiedParameterFacility unifiedParameterFacility;
	@Autowired
	UnifiedParamFacilityProvide unifiedParamFacilityProvide;
	
	public BigDecimal getAcctOTB(LoanPlan loanPlan,CcsAcct acct, CcsAcctO acctO,Date bizDate){
		BigDecimal loanOTB = BigDecimal.ZERO;
		ProductCredit productCredit = unifiedParameterFacility.loadParameter(acct.getProductCd(), ProductCredit.class);

		if(loanPlan == null){
			String loanCode = productCredit.loanPlansMap.get(productCredit.defaultLoanType);
		    loanPlan = unifiedParamFacilityProvide.loanPlan(loanCode);
		}
		
		if(loanPlan.loanMold.equals(LoanMold.C)){
			//待确认 fix ok ?
			//loanOTB = accountOTB.acctCashLoanOTB(txnInfo.getCardNo(), acct.getAcctType(), txnInfo.getBizDate());
			loanOTB = accountOTB.acctCashLoanOTB(acctO,productCredit,bizDate);
		}else{
			//TODO 账户额度-放款累计本金-未入账借记金额 = 账户可用金额
			loanOTB = accountOTB.loanNoCycleOTB(acctO, acct);
			
		}
		return loanOTB;
	}
}
