package com.sunline.ccs.batch.cca200;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.sunline.acm.service.sdk.GlobalManagementServiceMock;
import com.sunline.ccs.batch.cc6000.BatchData;
import com.sunline.ccs.batch.utils.JUnit4ClassRunner;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcct;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcctCrlmtAdjLog;
import com.sunline.ccs.infrastructure.server.repos.RCcsCustomer;
import com.sunline.ccs.infrastructure.server.repos.RCcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.Gender;
import com.sunline.ppy.dictionary.enums.IdType;
import com.sunline.ppy.dictionary.enums.Indicator;
@RunWith(JUnit4ClassRunner.class)
@ContextConfiguration("/cca214/test-context-cca214.xml")
public class SA214JobTest {
	
	@PersistenceContext
	private EntityManager em;
	
	@Autowired
	private RCcsAcctCrlmtAdjLog rAdj;
	
	@Autowired
	private RCcsAcct rAcct;
	
	@Autowired
	private RCcsLoan rLoan;
	
	@Autowired
	private RCcsCustomer rCust;
	
	@Autowired 
	private JobLauncherTestUtils jobLauncherTestUtils;

	@Autowired
	private GlobalManagementServiceMock managementMock;
	
	@Before
	public void init(){
		managementMock.setupBatchDate(new Date(),new Date());
	}
	
	@Test
	public void doTest() throws Exception{
		
		CcsLoan loan1 = BatchData.genLoan();
		loan1.setLoanId(1L);
		loan1.setAcctNbr(1000000L);
		loan1.setAcctType(AccountType.E);
		loan1.setActiveDate(new Date());
		loan1.setJoinLifeInsuInd(Indicator.Y);
		rLoan.save(loan1);
		
		CcsLoan loan2 = BatchData.genLoan();
		loan2.setLoanId(2L);
		loan2.setAcctNbr(2000000L);
		loan2.setAcctType(AccountType.E);
		loan2.setActiveDate(new Date());
		loan2.setJoinLifeInsuInd(Indicator.Y);
		rLoan.save(loan2);
		
		CcsLoan loan3 = BatchData.genLoan();
		loan3.setLoanId(3L);
		loan3.setAcctNbr(3000000L);
		loan3.setAcctType(AccountType.E);
		loan3.setActiveDate(new Date());
		loan3.setJoinLifeInsuInd(Indicator.Y);
		rLoan.save(loan3);
		
		rLoan.flush();
		
		CcsAcct acct1 = BatchData.genAcct();
		acct1.setAcctNbr(1000000L);
		acct1.setContrNbr("aaaaaaaaa");
		acct1.setCustId(1L);
		acct1.setName("孟翔");
		rAcct.save(acct1);
		
		CcsAcct acct2 = BatchData.genAcct();
		acct2.setAcctNbr(2000000L);
		acct2.setContrNbr("bbbbbbbb");
		acct2.setCustId(2L);
		acct2.setName("孟翔");
		rAcct.save(acct2);
		
		CcsAcct acct3 = BatchData.genAcct();
		acct3.setAcctNbr(3000000L);
		acct3.setContrNbr("bbbbbbbb");
		acct3.setCustId(2L);
		acct3.setName("孟翔");
		rAcct.save(acct3);
		
		rAcct.flush();
		
		CcsCustomer customer1 = getCustomer();
		customer1.setCustId(1L);
		rCust.save(customer1);
		
		CcsCustomer customer2 = getCustomer();
		customer2.setCustId(2L);
		rCust.save(customer2);
		
		CcsCustomer customer3 = getCustomer();
		customer3.setCustId(2L);
		rCust.save(customer3);
		
		rCust.flush();
		
		JobExecution jobExecution = jobLauncherTestUtils.launchStep("cca214");
		Assert.assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());
	}

	private CcsCustomer getCustomer() {
		CcsCustomer customer = new CcsCustomer();
		customer.setOrg("000000000001");
		customer.setIdNo("342222198906030018");
		customer.setBirthday(new Date());
		customer.setGender(Gender.F);
		customer.setIdType(IdType.C);
		customer.setMobileNo("hello");
		customer.setName("孟翔");
		customer.setOncardName("sunline");
		customer.setSetupDate(new Date());
		customer.setSocialInsAmt(BigDecimal.valueOf(0));
		customer.setInternalCustomerId("1111111111");
		customer.setUserAmt1(BigDecimal.ZERO);
		customer.setUserAmt2(BigDecimal.ZERO);
		customer.setUserAmt3(BigDecimal.ZERO);
		customer.setUserAmt4(BigDecimal.ZERO);
		customer.setUserAmt5(BigDecimal.ZERO);
		customer.setUserAmt6(BigDecimal.ZERO);
		customer.setUserNumber1(0);
		customer.setUserNumber2(0);
		customer.setUserNumber3(0);
		customer.setUserNumber4(0);
		customer.setUserNumber5(0);
		customer.setUserNumber6(0);
		return customer;
	}

}
