package com.sunline.ccs.batch.cc8500;

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
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctCrlmtAdjLog;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.param.def.enums.AdjState;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.Gender;
import com.sunline.ppy.dictionary.enums.IdType;
@RunWith(JUnit4ClassRunner.class)
@ContextConfiguration("/cc8500/test-context-cc8500.xml")
public class SA8500JobTest {
	
	@PersistenceContext
	private EntityManager em;
	
	@Autowired
	private RCcsAcctCrlmtAdjLog rAdj;
	
	@Autowired
	private RCcsAcct rAcct;
	
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
		
		CcsAcctCrlmtAdjLog adjLog1 = getAdjLog();
		adjLog1.setAcctNbr(1000000L);
		adjLog1.setAcctType(AccountType.E);
		adjLog1.setAdjState(AdjState.A);
		rAdj.save(adjLog1);
		
		CcsAcctCrlmtAdjLog adjLog2 = getAdjLog();
		adjLog2.setAcctNbr(2000000L);
		adjLog2.setAcctType(AccountType.E);
		adjLog2.setAdjState(AdjState.A);
		rAdj.save(adjLog2);
		
		rAdj.flush();
		
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
		
		rAcct.flush();
		
		CcsCustomer customer1 = getCustomer();
		customer1.setCustId(1L);
		rCust.save(customer1);
		
		CcsCustomer customer2 = getCustomer();
		customer2.setCustId(2L);
		rCust.save(customer2);
		
		rCust.flush();
		
		JobExecution jobExecution = jobLauncherTestUtils.launchStep("cc8502");
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

	private CcsAcctCrlmtAdjLog getAdjLog() {
		CcsAcctCrlmtAdjLog adjLog = new CcsAcctCrlmtAdjLog();
		adjLog.setAcctNbr(1101500L);
		adjLog.setAcctType(AccountType.E);
		adjLog.setAdjState(AdjState.R);
		adjLog.setCardNbr("1101500");
		adjLog.setCreateTime(new Date());
		adjLog.setCreateUser("Hello");
		adjLog.setCreditLmtNew(BigDecimal.valueOf(1000));
		adjLog.setCreditLmtOrig(BigDecimal.valueOf(50));
		adjLog.setLstUpdTime(new Date());
		adjLog.setLstUpdUser("World");
		adjLog.setOpSeq(2000L);
		adjLog.setOpTime(new Date());
		adjLog.setOrg("000000000001");
		adjLog.setProcDate(new Date());
		adjLog.setOpId("1111111111111111");
		return adjLog;
	}

}
