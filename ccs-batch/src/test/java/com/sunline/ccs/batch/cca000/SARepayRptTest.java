package com.sunline.ccs.batch.cca000;

import java.math.BigDecimal;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.sunline.ccs.batch.cca000.SA000ParamDataUtil;
import com.sunline.ccs.batch.sdk.BatchDateUtil;
import com.sunline.ccs.batch.utils.JUnit4ClassRunner;
import com.sunline.ccs.batch.utils.MakeDataExt;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcct;
import com.sunline.ccs.infrastructure.server.repos.RCcsLoan;
import com.sunline.ccs.infrastructure.server.repos.RCcsPlan;
import com.sunline.ccs.infrastructure.server.repos.RCcsRepayHst;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.CcsRepayHst;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.PlanType;

@RunWith(JUnit4ClassRunner.class)
@ContextConfiguration("/cca000/test-context-cca.xml")
public class SARepayRptTest {
	Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired 
	private JobLauncherTestUtils jobLauncherTestUtils;
	@Autowired
	private BatchDateUtil batchDateUtil;
	@Autowired
	private SA000ParamDataUtil paramUtil;
	@Autowired
	private RCcsAcct racct;
	@Autowired
	private RCcsRepayHst rrepay;
	@Autowired
	private RCcsLoan rloan;
	@Autowired
	private RCcsPlan rplan;
	
	private final static String BATCH_DATE_STR = "2019-07-29";
	@Before
	public void init() throws Exception{
		batchDateUtil.setBatchDate(BATCH_DATE_STR);
		paramUtil.loadParamFromDir();
		initData();
	}
	
	@Test
	public void saTest(){
		
		logger.info(">>>>>>>>>>>>Batch Date [{}]<<<<<<<<<<<<", batchDateUtil.getBatchDate());
		JobExecution jobExecution = jobLauncherTestUtils.launchStep("cca212MsLoanRepayRpt");
		ExitStatus exitStatus = jobExecution.getExitStatus();
		Assert.assertEquals(exitStatus,ExitStatus.COMPLETED);
	}

	private void initData() throws Exception {
		Long acctNbr = 1001L;
		AccountType acctType = AccountType.E;
		Long planId = 1000001L;
		String productCd = "000421";
		String loanCd = "3001";
		
		for(int i=0;i<5;i++){
			logger.info(">>>>>>>>>保存还款分配表数据<<<<<<<<<<");
			CcsRepayHst r1 = new CcsRepayHst();
			MakeDataExt.setDefaultValue(r1);
			logger.info(ReflectionToStringBuilder.toString(r1, ToStringStyle.MULTI_LINE_STYLE));
			r1.setAcctNbr(acctNbr);
			r1.setAcctType(acctType);
			r1.setPlanType(PlanType.Q);
			r1.setRepayAmt(new BigDecimal("100.23"));
			r1.setPlanId(planId);
			r1.setBatchDate(batchDateUtil.getBatchDate());
			rrepay.save(r1);
		}
		
		CcsAcct  a1 = new CcsAcct();
		MakeDataExt.setDefaultValue(a1);
		logger.info(ReflectionToStringBuilder.toString(a1, ToStringStyle.MULTI_LINE_STYLE));
		a1.setAcctNbr(acctNbr);
		a1.setAcctType(acctType);
		a1.setProductCd(productCd);
		racct.save(a1);
		
		CcsLoan l1 = new CcsLoan();
		MakeDataExt.setDefaultValue(l1);
		l1.setAcctNbr(acctNbr);
		l1.setAcctType(acctType);
		l1.setLoanCode(loanCd);
		rloan.save(l1);
		
		CcsPlan p1 = new CcsPlan();
		MakeDataExt.setDefaultValue(p1);
		p1.setAcctNbr(acctNbr);
		p1.setAcctType(acctType);
		p1.setPlanId(1000001L);
		p1.setPlanType(PlanType.L);
		rplan.save(p1);
	}
}
