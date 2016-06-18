package com.sunline.ccs.batch.cca200.data;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;

import org.springframework.stereotype.Service;

import com.sunline.ccs.batch.sdk.AbstractPrepareData;
import com.sunline.ccs.batch.utils.MakeDataExt;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.PlanType;

@Service(value="SAReplaceLateFeeJobData")
public class SAReplaceLateFeeJobData  extends AbstractPrepareData {

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	
	@Override
	public void prepareData() throws Exception {
		Long acctNbr = 1001L;
		String logiCardNo = "6004017000000036";
		CcsCustomer c = new CcsCustomer();
		MakeDataExt.setDefaultValue(c);
		c.setCustId(10000001L);
		c = saveEntity(c);
		
		CcsAcct a =  new CcsAcct();
		MakeDataExt.setDefaultValue(a);
		a.setAcctNbr(acctNbr);
		a.setAcctType(AccountType.E);
		a.setName("Hurricane");
		a.setProductCd("000401");
		a.setAcqId("00130000");
		a.setOrg("000000000001");
		a.setCustId(c.getCustId());
		a.setBlockCode("");
		a.setWaiveLatefeeInd(Indicator.N);
		a.setNextStmtDate(sdf.parse("20160403"));
		a.setPmtDueDate(sdf.parse("20160403"));
		a.setDefaultLogicCardNbr(logiCardNo);
		
		a.setTotDueAmt(new BigDecimal("1000"));
		a.setCurrDueAmt(new BigDecimal("1000"));
		saveEntity(a);
		
		CcsLoan l = new CcsLoan();
		MakeDataExt.setDefaultValue(l);
		l.setAcctNbr(acctNbr);
		l.setAcctType(AccountType.E);
		l.setRefNbr("1604011928400066006003");
		saveEntity(l);
		
		CcsPlan po = new CcsPlan();
		po.setAcctNbr(acctNbr);
		po.setAcctType(AccountType.E);
		po.setRefNbr(logiCardNo);
		po.setPlanNbr("500001");
		po.setPlanType(PlanType.P);
		saveEntity(po);
		
		CcsPlan p1 = new CcsPlan();
		p1.setAcctNbr(acctNbr);
		p1.setAcctType(AccountType.E);
		p1.setRefNbr(logiCardNo);
		p1.setPlanType(PlanType.Q);
		p1.setPlanNbr("510001");
		
	}

}
