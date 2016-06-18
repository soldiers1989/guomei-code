package com.sunline.ccs.batch.cca200.data;

import org.springframework.stereotype.Service;

import com.sunline.ccs.batch.sdk.AbstractPrepareData;
import com.sunline.ccs.batch.utils.MakeDataExt;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsRepayHst;
import com.sunline.ccs.param.def.enums.BucketObject;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.PlanType;

@Service(value="cca212MsLoanRepayRptData")
public class Cca212MsLoanRepayRptData extends AbstractPrepareData {

	@Override
	public void prepareData() throws Exception {
		Long acctNbr = 1001L;
		AccountType acctType = AccountType.E;

		for(BucketObject o : BucketObject.values()){
			CcsRepayHst r = new CcsRepayHst();
			MakeDataExt.setDefaultValue(r);
			r.setAcctNbr(acctNbr);
			r.setAcctType(acctType);
			r.setBnpType(o);
			r.setRepayAmt(MakeDataExt.getRandomDecimal(5, 2));
			r.setPlanType(PlanType.Q);
			r.setBatchDate(getBatchDate());
			saveEntity(r);
		}
		
		CcsLoan l = new CcsLoan();
		MakeDataExt.setDefaultValue(l);
		l.setAcctNbr(acctNbr);
		l.setAcctType(acctType);
		l.setLoanCode("3201");
		saveEntity(l);
		
		CcsAcct a = new CcsAcct();
		MakeDataExt.setDefaultValue(a);
		a.setAcctNbr(acctNbr);
		a.setAcctType(acctType);
		a.setProductCd("003201");
		saveEntity(a);
	}

}
