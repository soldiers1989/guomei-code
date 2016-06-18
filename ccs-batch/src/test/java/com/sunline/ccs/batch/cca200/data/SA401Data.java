package com.sunline.ccs.batch.cca200.data;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

import com.sunline.ccs.batch.sdk.AbstractPrepareData;
import com.sunline.ccs.batch.utils.MakeDataExt;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsRepayHst;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnHst;
import com.sunline.ccs.param.def.enums.BucketObject;
import com.sunline.ppy.dictionary.enums.AccountType;

@Service(value="SA401JobData")
public class SA401Data extends AbstractPrepareData {
	
	@Override
	public void prepareData() throws Exception {
		Long acctNo = 1001L;
		List<BucketObject> bnpTypes = Arrays.asList(
				BucketObject.ctdReplaceSvcFee, BucketObject.pastReplaceSvcFee,
				BucketObject.ctdReplacePenalty,BucketObject.pastReplacePenalty, 
				BucketObject.ctdReplaceLpc,BucketObject.pastReplaceLpc,
				BucketObject.ctdReplaceMulct,BucketObject.pastReplaceMulct,
				BucketObject.ctdReplaceTxnFee, BucketObject.pastReplaceTxnFee);
		
		CcsCustomer c = new CcsCustomer();
		MakeDataExt.setDefaultValue(c);
		c.setCustId(10000001L);
		c = saveEntity(c);
		
		CcsAcct a =  new CcsAcct();
		MakeDataExt.setDefaultValue(a);
		a.setAcctNbr(acctNo);
		a.setAcctType(AccountType.E);
		a.setName("Hurricane");
		a.setProductCd("000401");
		a.setAcqId("00130000");
		a.setOrg("000000000001");
		a.setCustId(c.getCustId());
		saveEntity(a);
		
		for(BucketObject bnp : bnpTypes){
			CcsRepayHst r1 = new CcsRepayHst();
			r1.setAcctNbr(acctNo);
			r1.setAcctType(AccountType.E);
			r1.setBatchDate(getBatchDate());
			r1.setBnpType(bnp);
			r1.setRepayAmt(new BigDecimal(MakeDataExt.getRandomNum(4)));
			saveEntity(r1);
		}
		
		CcsTxnHst t1 = new CcsTxnHst();
		MakeDataExt.setDefaultValue(t1);
		t1.setAcctNbr(acctNo);
		t1.setAcctType(AccountType.E);
		t1.setTxnCode("T920");
		t1.setPostDate(getBatchDate());
		saveEntity(t1);
		
		CcsTxnHst t2 = new CcsTxnHst();
		MakeDataExt.setDefaultValue(t2);
		t2.setAcctNbr(acctNo);
		t2.setAcctType(AccountType.E);
		t2.setTxnCode("T948");
		t2.setPostDate(getBatchDate());
		saveEntity(t2);
		
		
		
		
	}

}
